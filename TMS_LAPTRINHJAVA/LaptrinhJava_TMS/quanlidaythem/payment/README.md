# Payment Module - Chức Năng Thanh Toán QR Code

## 📁 Cấu Trúc Thư Mục

```
payment/
├── controller/
│   └── PaymentController.java          # REST API endpoints
├── domain/
│   └── Payment.java                    # Payment entity
├── dto/
│   ├── Request/
│   │   ├── CreatePaymentRequest.java   # Request tạo thanh toán
│   │   └── PaymentCallbackRequest.java # Request callback
│   └── Response/
│       └── PaymentResponse.java        # Response với QR code
├── repository/
│   └── PaymentRepository.java          # JPA Repository
├── service/
│   ├── PaymentService.java             # Business logic
│   └── VietQRGenerator.java            # Tạo QR code theo chuẩn VietQR
└── docs/
    ├── PAYMENT_API_GUIDE.md            # Tài liệu API chi tiết
    ├── HUONG_DAN_SU_DUNG_THANH_TOAN_QR.md  # Hướng dẫn sử dụng đầy đủ
    └── QUICK_START_PAYMENT.md          # Quick start guide
```

## 🎯 Chức Năng

Module này cung cấp chức năng thanh toán QR code tự động:

1. **Tạo mã thanh toán:** Người dùng yêu cầu tạo mã thanh toán với số tiền cụ thể
2. **Tạo QR code:** Hệ thống tạo QR code theo chuẩn VietQR (EMV QR Code)
3. **Xử lý callback:** Tự động cập nhật trạng thái khi thanh toán thành công
4. **Cập nhật Fee:** Tự động cập nhật học phí sau khi thanh toán

## 📦 Package Structure

- **Package:** `vn.edu.uth.quanlidaythem.payment`
- **Sub-packages:**
  - `payment.controller` - REST Controllers
  - `payment.domain` - Entity classes
  - `payment.dto` - Data Transfer Objects
  - `payment.repository` - JPA Repositories
  - `payment.service` - Business logic services

## 🔌 API Endpoints

- `POST /api/payments/create` - Tạo mã thanh toán
- `GET /api/payments/{transactionId}` - Kiểm tra trạng thái
- `POST /api/payments/callback` - Webhook callback
- `POST /api/payments/{transactionId}/confirm` - Xác nhận (testing)

## 📚 Tài Liệu

Xem thêm trong folder `docs/`:
- `PAYMENT_API_GUIDE.md` - Tài liệu API đầy đủ
- `HUONG_DAN_SU_DUNG_THANH_TOAN_QR.md` - Hướng dẫn sử dụng chi tiết
- `QUICK_START_PAYMENT.md` - Quick start guide

## ⚙️ Cấu Hình

Cấu hình trong `application.properties`:

```properties
payment.vietqr.enabled=true
payment.vietqr.merchant.name=TMS - Quản Lý Dạy Thêm
payment.vietqr.merchant.city=Hà Nội
payment.vietqr.merchant.country=VN
payment.vietqr.currency=VND
payment.vietqr.base.url=http://localhost:8080
payment.vietqr.merchant.account.info=970422|1234567890|TMS
```

## 🚀 Sử Dụng

1. Import các class từ package `vn.edu.uth.quanlidaythem.payment.*`
2. Inject `PaymentService` vào controller hoặc service khác
3. Gọi các method trong `PaymentService` để xử lý thanh toán

## 📝 Lưu Ý

- QR code được tạo theo chuẩn **VietQR (EMV QR Code)**
- Tự động cập nhật `Fee` khi thanh toán thành công
- Hỗ trợ idempotency (xử lý callback nhiều lần)

