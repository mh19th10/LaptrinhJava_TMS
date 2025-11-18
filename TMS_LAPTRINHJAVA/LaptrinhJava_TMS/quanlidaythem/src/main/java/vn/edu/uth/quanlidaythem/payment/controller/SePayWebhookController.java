package vn.edu.uth.quanlidaythem.payment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.uth.quanlidaythem.payment.dto.Request.SePayWebhookRequest;
import vn.edu.uth.quanlidaythem.payment.service.PaymentService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller xử lý webhook từ SePay
 * SePay sẽ gọi endpoint này khi có tiền vào tài khoản ngân hàng
 */
@RestController
@RequestMapping("/api/sepay")
public class SePayWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(SePayWebhookController.class);
    
    private final PaymentService paymentService;
    
    // Regex pattern để tìm Transaction ID trong nội dung chuyển khoản
    // Format: TXN-{timestamp}-{UUID} hoặc TXN-{any alphanumeric and dash}
    // Case-insensitive để bắt cả "TXN" và "txn"
    private static final Pattern TXN_PATTERN = Pattern.compile("(?i)(TXN-[A-Za-z0-9-]+)");

    // Cấu hình API Key của SePay (để bảo mật - tùy chọn)
    @Value("${sepay.api.key:}")
    private String sepayApiKey;

    public SePayWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Endpoint nhận webhook từ SePay
     * SePay sẽ gọi endpoint này khi phát hiện có tiền vào tài khoản
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleSePayWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SePayWebhookRequest request
    ) {
        logger.info("📥 Nhận Webhook SePay: {}", request);

        // 1. Kiểm tra bảo mật (Nếu bạn có cấu hình API Key trong SePay)
        // Format header của SePay: "Bearer YOUR_API_KEY"
        if (sepayApiKey != null && !sepayApiKey.isEmpty()) {
            if (authHeader == null || !authHeader.startsWith("Bearer " + sepayApiKey)) {
                logger.warn("⚠️ Unauthorized webhook request - API key không khớp");
                return ResponseEntity.status(401).body("Unauthorized");
            }
        }

        // 2. Chỉ xử lý giao dịch "in" (tiền vào)
        if (!"in".equalsIgnoreCase(request.getTransferType())) {
            logger.info("⏭️ Bỏ qua giao dịch 'out' (tiền ra)");
            return ResponseEntity.ok("Skipped - not incoming transaction");
        }

        // 3. Bóc tách Transaction ID từ nội dung chuyển khoản (content)
        String transactionId = extractTransactionId(request.getContent());
        
        if (transactionId == null) {
            logger.warn("⚠️ Không tìm thấy Transaction ID trong nội dung: {}", request.getContent());
            // Vẫn trả về 200 để SePay không gửi lại (vì đây có thể là chuyển khoản không liên quan)
            return ResponseEntity.ok("No Transaction ID found - acknowledged");
        }

        logger.info("🔍 Tìm thấy Transaction ID: {}", transactionId);

        // 4. Gọi Service để xử lý thanh toán thành công
        try {
            String paymentMethod = "SEPAY_" + (request.getGateway() != null ? request.getGateway() : "BANK");
            paymentService.confirmPaymentSuccess(transactionId, paymentMethod);
            logger.info("✅ Đã xác nhận thanh toán thành công cho Transaction ID: {}", transactionId);
            return ResponseEntity.ok("Success");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            if (e.getStatusCode().value() == 404) {
                logger.warn("⚠️ Không tìm thấy Payment với Transaction ID: {}", transactionId);
                // Trả về 200 để SePay không spam retry
                return ResponseEntity.ok("Payment not found - acknowledged");
            }
            logger.error("❌ Lỗi xử lý thanh toán: {}", e.getMessage());
            return ResponseEntity.ok("Error but acknowledged");
        } catch (Exception e) {
            logger.error("❌ Lỗi xử lý thanh toán: {}", e.getMessage(), e);
            // Trả về 200 để SePay không spam retry
            return ResponseEntity.ok("Error but acknowledged");
        }
    }

    /**
     * Hàm dùng Regex để tìm chuỗi bắt đầu bằng TXN- (case-insensitive)
     * Ví dụ: 
     * - "Nguyen Van A chuyen khoan TXN-12345678-ABC12345 abc" -> "TXN-12345678-ABC12345"
     * - "chuyen tien txn-12345678-ABC12345" -> "txn-12345678-ABC12345" (sẽ được normalize thành uppercase)
     * 
     * @param content Nội dung chuyển khoản từ ngân hàng
     * @return Transaction ID nếu tìm thấy (uppercase), null nếu không tìm thấy
     */
    private String extractTransactionId(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        // Regex tìm chuỗi bắt đầu bằng TXN (case-insensitive), theo sau là số, chữ, dấu gạch ngang
        // Cấu trúc Transaction ID: TXN-{timestamp}-{8 ký tự UUID}
        // Ví dụ: TXN-1705123456789-ABC12345
        Matcher matcher = TXN_PATTERN.matcher(content);
        
        if (matcher.find()) {
            String transactionId = matcher.group(1).toUpperCase(); // Normalize thành uppercase
            logger.debug("🔍 Extract Transaction ID: {} từ content: {}", transactionId, content);
            return transactionId;
        }

        return null;
    }
}

