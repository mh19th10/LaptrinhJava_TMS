# Hướng Dẫn Tích Hợp SePay - Tự Động Nhận Tiền

## 🎯 Tổng Quan

SePay là dịch vụ miễn phí giúp tự động nhận thông báo khi có tiền vào tài khoản ngân hàng. Khi người dùng chuyển khoản, SePay sẽ gọi webhook vào server của bạn để tự động cập nhật trạng thái thanh toán.

---

## ✅ Đã Tích Hợp

### Files đã tạo:
1. ✅ `SePayWebhookRequest.java` - DTO nhận webhook từ SePay
2. ✅ `SePayWebhookController.java` - Controller xử lý webhook
3. ✅ `PaymentService.confirmPaymentSuccess()` - Method xác nhận thanh toán

### Cấu hình đã cập nhật:
1. ✅ `SecurityConfig.java` - Cho phép `/api/sepay/webhook` không cần auth
2. ✅ `application.properties` - Thêm cấu hình SePay

---

## 🚀 Các Bước Tích Hợp Thực Tế

### Bước 1: Đăng Ký SePay

1. Truy cập: https://my.sepay.vn
2. Đăng ký tài khoản
3. Thêm tài khoản ngân hàng (MB Bank, Vietcombank, etc.)
4. Làm theo hướng dẫn kết nối (quét QR, cấp quyền SMS/Notification)

### Bước 2: Public Localhost (BẮT BUỘC)

SePay không thể gọi vào `localhost:8080`. Bạn cần dùng **Ngrok**:

**Cài đặt Ngrok:**
1. Tải từ: https://ngrok.com/download
2. Giải nén và chạy:
   ```bash
   ngrok http 8080
   ```
3. Copy link HTTPS, ví dụ: `https://abc123.ngrok-free.app`

**Lưu ý:** 
- Link Ngrok free sẽ thay đổi mỗi lần restart
- Nên dùng Ngrok paid để có link cố định

### Bước 3: Cấu Hình Webhook trên SePay

1. Vào Dashboard SePay → **"Tích hợp Webhook"**
2. Click **"Thêm Webhook"**
3. **Webhook URL:** `https://abc123.ngrok-free.app/api/sepay/webhook`
4. **Method:** POST
5. **Events:** Chọn "Tiền vào" (Incoming transactions)
6. Click **Lưu**

### Bước 4: Test Thực Tế

1. **Khởi động Spring Boot:**
   ```bash
   mvn spring-boot:run
   ```

2. **Chạy Ngrok:**
   ```bash
   ngrok http 8080
   ```

3. **Tạo mã thanh toán:**
   - Vào: `http://localhost:8080/tuition_student.html`
   - Click "Thanh toán" cho một học phí
   - Copy Transaction ID từ modal (ví dụ: `TXN-1705123456789-ABC12345`)

4. **Chuyển khoản thật:**
   - Mở app ngân hàng
   - Quét QR code hoặc chuyển khoản thủ công
   - **QUAN TRỌNG:** Trong nội dung chuyển khoản, phải có Transaction ID
   - Ví dụ: `TXN-1705123456789-ABC12345` hoặc `chuyen khoan TXN-1705123456789-ABC12345`

5. **Quan sát:**
   - SePay nhận được SMS/Notification từ ngân hàng (10-30 giây)
   - SePay gọi webhook vào server
   - Console log hiển thị: `📥 Nhận Webhook SePay: ...`
   - Database tự động cập nhật Payment và Fee

---

## 🧪 Test Webhook Thủ Công

### Test với cURL:

```bash
curl -X POST http://localhost:8080/api/sepay/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "id": 12345,
    "gateway": "MBBank",
    "transactionDate": "2024-01-15T10:30:00",
    "accountNumber": "2730788922276",
    "content": "NGUYEN VAN A chuyen khoan TXN-1705123456789-ABC12345 hoc phi",
    "transferType": "in",
    "transferAmount": 500000,
    "accumulated": 10000000
  }'
```

**Response mong đợi:**
```
Success
```

**Kiểm tra:**
- Payment với Transaction ID `TXN-1705123456789-ABC12345` có status = "SUCCESS"
- Fee đã được cập nhật (nếu có feeId)

---

## 🔍 Cách Extract Transaction ID

### Logic Extract:

```java
// Regex pattern: (?i)(TXN-[A-Za-z0-9-]+)
// Case-insensitive: Bắt cả "TXN" và "txn"
// Tìm chuỗi bắt đầu bằng TXN-, theo sau là số, chữ, dấu gạch ngang
```

### Ví dụ Nội Dung Chuyển Khoản:

| Nội dung ngân hàng | Transaction ID tìm được |
|-------------------|-------------------------|
| `NGUYEN VAN A chuyen khoan TXN-1705123456789-ABC12345 hoc phi` | ✅ `TXN-1705123456789-ABC12345` |
| `TXN-1705123456789-ABC12345` | ✅ `TXN-1705123456789-ABC12345` |
| `chuyen tien TXN-1705123456789-ABC12345` | ✅ `TXN-1705123456789-ABC12345` |
| `chuyen tien txn-1705123456789-ABC12345` | ✅ `TXN-1705123456789-ABC12345` (uppercase) |
| `chuyen khoan hoc phi` | ❌ Không tìm thấy |
| `TXN-` | ❌ Không hợp lệ (thiếu phần sau) |

---

## 🔐 Bảo Mật Webhook

### Option 1: API Key (Khuyến nghị)

1. **Lấy API Key từ SePay:**
   - Vào Dashboard SePay → Settings → API Keys
   - Tạo API Key mới

2. **Cập nhật `application.properties`:**
   ```properties
   sepay.api.key=your-api-key-here
   ```

3. **Cấu hình SePay:**
   - Khi tạo webhook, thêm header: `Authorization: Bearer your-api-key-here`

4. **Code đã xử lý:**
   - SePayWebhookController sẽ kiểm tra API key trong header
   - Nếu không khớp → trả về 401 Unauthorized

---

## 📊 Luồng Hoạt Động với SePay

```
1. Người dùng → Tạo mã thanh toán
2. Server → Tạo Payment (status = PENDING) + QR code
3. Người dùng → Quét QR và chuyển khoản
4. Ngân hàng → Gửi SMS/Notification
5. SePay → Đọc SMS/Notification
6. SePay → Gọi webhook: POST /api/sepay/webhook
7. Server → Extract Transaction ID từ content
8. Server → Tìm Payment theo Transaction ID
9. Server → Cập nhật Payment status = SUCCESS
10. Server → Tự động cập nhật Fee
11. Frontend → Polling phát hiện status = SUCCESS
12. Frontend → Hiển thị "Thanh toán thành công"
```

---

## ⚠️ Lưu Ý Quan Trọng

### 1. Nội Dung Chuyển Khoản

**Vấn đề:** Nếu người dùng tự gõ nội dung mà quên ghi Transaction ID, hệ thống sẽ không nhận diện được.

**Giải pháp:**
- Hướng dẫn người dùng: "Vui lòng giữ nguyên nội dung chuyển khoản"
- Hoặc: Thêm Transaction ID vào QR code data để app ngân hàng tự điền

### 2. Regex Pattern

Regex hiện tại: `(?i)(TXN-[A-Za-z0-9-]+)`

**Hoạt động với:**
- ✅ `TXN-1705123456789-ABC12345`
- ✅ `txn-1705123456789-ABC12345` (case-insensitive)
- ✅ `TXN-123-ABC`

**Không hoạt động với:**
- ❌ `TXN-` (thiếu phần sau)
- ❌ `chuyen khoan hoc phi` (không có Transaction ID)

### 3. Idempotency

- Hệ thống đã xử lý idempotency
- Nếu SePay gọi webhook nhiều lần, chỉ xử lý 1 lần
- Payment đã SUCCESS sẽ không được cập nhật lại

### 4. Error Handling

- Nếu không tìm thấy Transaction ID → Trả về 200 (để SePay không retry)
- Nếu Payment không tồn tại → Trả về 200 (để SePay không retry)
- Log tất cả errors để debug

---

## 🐛 Troubleshooting

### Lỗi: "Unauthorized" khi SePay gọi webhook

**Nguyên nhân:** API key không khớp hoặc chưa cấu hình

**Giải pháp:**
1. Kiểm tra API key trong `application.properties`
2. Kiểm tra header Authorization trong SePay webhook config
3. Tạm thời để trống `sepay.api.key=` để test

### Lỗi: "No Transaction ID found"

**Nguyên nhân:** Nội dung chuyển khoản không chứa Transaction ID

**Giải pháp:**
1. Kiểm tra nội dung chuyển khoản trong SePay dashboard
2. Đảm bảo người dùng giữ nguyên nội dung khi chuyển khoản
3. Test với nội dung có chứa Transaction ID

### Webhook không được gọi

**Nguyên nhân:**
- Ngrok không chạy
- Link Ngrok đã thay đổi
- SePay webhook config sai URL

**Giải pháp:**
1. Kiểm tra Ngrok đang chạy: `ngrok http 8080`
2. Kiểm tra link Ngrok có đúng không
3. Cập nhật lại webhook URL trong SePay dashboard
4. Test webhook thủ công bằng cURL

### Payment không được cập nhật

**Nguyên nhân:**
- Transaction ID không khớp
- Payment không tồn tại
- Lỗi khi cập nhật

**Giải pháp:**
1. Xem log server: `📥 Nhận Webhook SePay: ...`
2. Kiểm tra Transaction ID có đúng không
3. Kiểm tra Payment có tồn tại trong database không
4. Xem log errors

---

## ✅ Checklist Tích Hợp

- [x] Đã tạo SePayWebhookRequest.java
- [x] Đã tạo SePayWebhookController.java
- [x] Đã thêm method confirmPaymentSuccess() vào PaymentService
- [x] Đã cập nhật SecurityConfig (cho phép /api/sepay/webhook)
- [x] Đã cấu hình application.properties
- [ ] Đã đăng ký tài khoản SePay
- [ ] Đã thêm tài khoản ngân hàng vào SePay
- [ ] Đã cài đặt và chạy Ngrok
- [ ] Đã cấu hình webhook trên SePay dashboard
- [ ] Đã test webhook thủ công (cURL)
- [ ] Đã test chuyển khoản thật
- [ ] Đã verify Payment và Fee được cập nhật

---

## 🎉 Kết Luận

**Code đã sẵn sàng!** Bây giờ bạn chỉ cần:

1. ✅ Restart ứng dụng
2. ✅ Cấu hình SePay và Ngrok
3. ✅ Test chuyển khoản thật
4. ✅ Hệ thống sẽ tự động nhận tiền và cập nhật! 🚀

---

**Chúc bạn tích hợp thành công! 🎉**

