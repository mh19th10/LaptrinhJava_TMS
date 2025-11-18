package vn.edu.uth.quanlidaythem.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.uth.quanlidaythem.payment.dto.Request.PaymentCallbackRequest;
import vn.edu.uth.quanlidaythem.payment.dto.Response.PaymentResponse;
import vn.edu.uth.quanlidaythem.payment.service.PaymentService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/payments/callback
     * Webhook/callback để nhận thông báo khi thanh toán thành công (dùng cho testing)
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> paymentCallback(@RequestBody PaymentCallbackRequest request) {
        PaymentResponse response = paymentService.processPaymentCallback(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Cập nhật thanh toán thành công");
        result.put("payment", response);
        
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/payments/{transactionId}
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String transactionId) {
        PaymentResponse response = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/payments/{transactionId}/confirm
     * Endpoint để simulate việc xác nhận thanh toán (dùng cho testing)
     * Trong thực tế, SePay webhook sẽ tự động gọi confirmPaymentSuccess()
     */
    @PostMapping("/{transactionId}/confirm")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @PathVariable String transactionId,
            @RequestParam(defaultValue = "SUCCESS") String status) {
        
        PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest();
        callbackRequest.setTransactionId(transactionId);
        callbackRequest.setStatus(status);
        callbackRequest.setPaymentMethod("SEPAY_BANK_TRANSFER");
        callbackRequest.setNotes("Thanh toán được xác nhận");

        PaymentResponse response = paymentService.processPaymentCallback(callbackRequest);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Xác nhận thanh toán thành công");
        result.put("payment", response);
        
        return ResponseEntity.ok(result);
    }
}

