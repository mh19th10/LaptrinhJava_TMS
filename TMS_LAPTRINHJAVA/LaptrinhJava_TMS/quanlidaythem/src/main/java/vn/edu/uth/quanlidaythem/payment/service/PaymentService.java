package vn.edu.uth.quanlidaythem.payment.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.uth.quanlidaythem.domain.Fee;
import vn.edu.uth.quanlidaythem.payment.domain.Payment;
import vn.edu.uth.quanlidaythem.payment.dto.Request.PaymentCallbackRequest;
import vn.edu.uth.quanlidaythem.payment.dto.Response.PaymentResponse;
import vn.edu.uth.quanlidaythem.payment.repository.PaymentRepository;
import vn.edu.uth.quanlidaythem.repository.FeeRepository;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentRepository paymentRepository;
    private final FeeRepository feeRepository;

    public PaymentService(PaymentRepository paymentRepository, FeeRepository feeRepository) {
        this.paymentRepository = paymentRepository;
        this.feeRepository = feeRepository;
    }

    /**
     * Xử lý callback khi thanh toán thành công
     */
    @Transactional
    public PaymentResponse processPaymentCallback(PaymentCallbackRequest request) {
        // Tìm payment theo transactionId
        Payment payment = paymentRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch"));

        // Kiểm tra trạng thái hiện tại
        if ("SUCCESS".equals(payment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giao dịch đã được xử lý trước đó");
        }

        // Cập nhật trạng thái
        payment.setStatus(request.getStatus());
        payment.setCompletedAt(LocalDateTime.now());
        if (request.getNotes() != null) {
            payment.setNotes(request.getNotes());
        }

        payment = paymentRepository.save(payment);

        // Nếu thanh toán thành công, cập nhật Fee
        if ("SUCCESS".equals(request.getStatus()) && payment.getFeeId() != null) {
            updateFeeAfterPayment(payment);
        }

        // Tạo response
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setTransactionId(payment.getTransactionId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setFeeId(payment.getFeeId());
        response.setStudentName(payment.getStudentName());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setCreatedAt(payment.getCreatedAt());
        response.setCompletedAt(payment.getCompletedAt());
        response.setNotes(payment.getNotes());

        return response;
    }

    /**
     * Xác nhận thanh toán thành công (dùng cho SePay webhook)
     * Tự động tìm transactionId và cập nhật status = SUCCESS
     */
    @Transactional
    public void confirmPaymentSuccess(String transactionId, String paymentMethod) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch với Transaction ID: " + transactionId));

        // Kiểm tra idempotency - nếu đã SUCCESS rồi thì không làm gì
        if ("SUCCESS".equals(payment.getStatus())) {
            logger.info("Payment {} đã được xử lý trước đó, bỏ qua", transactionId);
            return;
        }

        // Cập nhật trạng thái
        payment.setStatus("SUCCESS");
        payment.setCompletedAt(LocalDateTime.now());
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "SEPAY_BANK_TRANSFER");
        payment.setNotes("Thanh toán thành công qua SePay - " + LocalDateTime.now());

        payment = paymentRepository.save(payment);

        // Cập nhật Fee nếu có
        if (payment.getFeeId() != null) {
            updateFeeAfterPayment(payment);
        }

        logger.info("✅ Đã xác nhận thanh toán thành công cho Payment ID: {}, Transaction ID: {}", payment.getId(), transactionId);
    }

    /**
     * Lấy thông tin payment theo transactionId
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch"));

        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setTransactionId(payment.getTransactionId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setFeeId(payment.getFeeId());
        response.setStudentName(payment.getStudentName());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setCreatedAt(payment.getCreatedAt());
        response.setCompletedAt(payment.getCompletedAt());
        response.setNotes(payment.getNotes());

        return response;
    }

    /**
     * Cập nhật Fee sau khi thanh toán thành công
     */
    private void updateFeeAfterPayment(Payment payment) {
        if (payment.getFeeId() == null) {
            return;
        }

        Fee fee = feeRepository.findById(payment.getFeeId())
                .orElse(null);

        if (fee != null) {
            // Cập nhật số tiền đã thanh toán
            Long currentPaid = fee.getPaid() != null ? fee.getPaid() : 0L;
            fee.setPaid(currentPaid + payment.getAmount());

            // Cập nhật trạng thái
            if (fee.getAmount() != null && fee.getPaid() >= fee.getAmount()) {
                fee.setStatus("PAID");
            } else {
                fee.setStatus("PARTIAL");
            }

            feeRepository.save(fee);
        }
    }

}

