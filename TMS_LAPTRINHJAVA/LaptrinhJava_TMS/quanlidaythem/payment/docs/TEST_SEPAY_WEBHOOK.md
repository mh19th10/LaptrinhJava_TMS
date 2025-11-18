# Test SePay Webhook - Hướng Dẫn Chi Tiết

## ✅ Bạn Đã Hoàn Thành
- [x] Đăng ký SePay
- [x] Cài Ngrok
- [x] Cấu hình webhook trên SePay

---

## 🧪 Bước 1: Test Webhook Thủ Công (Khuyến Nghị)

Trước khi test với chuyển khoản thật, hãy test webhook thủ công để đảm bảo endpoint hoạt động.

### 1.1. Khởi động ứng dụng

```bash
mvn spring-boot:run
```

### 1.2. Tạo một Payment trước

**Cách 1: Qua giao diện web**
1. Vào: `http://localhost:8080/tuition_student.html`
2. Click "Thanh toán" cho một học phí
3. Copy Transaction ID từ modal (ví dụ: `TXN-1705123456789-ABC12345`)

**Cách 2: Qua API**

```bash
curl -X POST http://localhost:8080/api/payments/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "amount": 50000,
    "feeId": 1,
    "studentName": "Test Student",
    "notes": "Test SePay webhook"
  }'
```

Lưu lại `transactionId` từ response.

### 1.3. Test Webhook với cURL

**Thay `TXN-XXX-XXX` bằng Transaction ID thật của bạn:**

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
    "transferAmount": 50000,
    "accumulated": 10000000
  }'
```

**Response mong đợi:**
```
Success
```

**Kiểm tra Console Log:**
Bạn sẽ thấy:
```
📥 Nhận Webhook SePay: SePayWebhookRequest{...}
🔍 Tìm thấy Transaction ID: TXN-1705123456789-ABC12345
✅ Đã xác nhận thanh toán thành công cho Transaction ID: TXN-1705123456789-ABC12345
```

**Kiểm tra Database:**
```sql
SELECT * FROM payments WHERE transaction_id = 'TXN-1705123456789-ABC12345';
-- Status phải = 'SUCCESS'
-- completed_at phải có giá trị
```

---

## 🧪 Bước 2: Test với Ngrok (Nếu Webhook URL dùng Ngrok)

### 2.1. Chạy Ngrok

```bash
ngrok http 8080
```

Copy link HTTPS (ví dụ: `https://abc123.ngrok-free.app`)

### 2.2. Test Webhook qua Ngrok

```bash
curl -X POST https://abc123.ngrok-free.app/api/sepay/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "id": 12345,
    "gateway": "MBBank",
    "content": "NGUYEN VAN A chuyen khoan TXN-1705123456789-ABC12345 hoc phi",
    "transferType": "in",
    "transferAmount": 50000
  }'
```

**Lưu ý:** 
- Đảm bảo Ngrok đang chạy
- Link Ngrok phải khớp với link đã cấu hình trên SePay

---

## 💰 Bước 3: Test với Chuyển Khoản Thật

### 3.1. Tạo mã thanh toán

1. Vào: `http://localhost:8080/tuition_student.html`
2. Click "Thanh toán" cho một học phí
3. **QUAN TRỌNG:** Copy Transaction ID (ví dụ: `TXN-1705123456789-ABC12345`)

### 3.2. Chuyển khoản thật

1. Mở app ngân hàng (MB Bank, Vietcombank, etc.)
2. Chọn "Chuyển khoản"
3. Nhập số tài khoản: `2730788922276`
4. Nhập số tiền: Đúng với số tiền trong QR code
5. **QUAN TRỌNG:** Nội dung chuyển khoản PHẢI chứa Transaction ID:
   - ✅ Đúng: `TXN-1705123456789-ABC12345`
   - ✅ Đúng: `chuyen khoan TXN-1705123456789-ABC12345 hoc phi`
   - ❌ Sai: `chuyen khoan hoc phi` (thiếu Transaction ID)

### 3.3. Quan sát

**Trong Console Log (sau 10-30 giây):**
```
📥 Nhận Webhook SePay: SePayWebhookRequest{...}
🔍 Tìm thấy Transaction ID: TXN-1705123456789-ABC12345
✅ Đã xác nhận thanh toán thành công cho Transaction ID: TXN-1705123456789-ABC12345
```

**Trên Giao Diện Web:**
- Modal thanh toán tự động cập nhật: "Thanh toán thành công"
- Bảng học phí tự động reload
- Học phí hiển thị "Đã thanh toán"

**Trong Database:**
```sql
-- Kiểm tra Payment
SELECT * FROM payments WHERE transaction_id = 'TXN-1705123456789-ABC12345';
-- Status = 'SUCCESS'
-- completed_at có giá trị
-- payment_method = 'SEPAY_MBBank' (hoặc gateway tương ứng)

-- Kiểm tra Fee (nếu có feeId)
SELECT * FROM fees WHERE id = {feeId};
-- paid đã tăng lên
-- status = 'PAID' hoặc 'PARTIAL'
```

---

## 🔍 Bước 4: Kiểm Tra Logs

### 4.1. Xem Logs trong Console

Khi SePay gọi webhook, bạn sẽ thấy:

**Thành công:**
```
📥 Nhận Webhook SePay: SePayWebhookRequest{id=12345, gateway='MBBank', ...}
🔍 Tìm thấy Transaction ID: TXN-1705123456789-ABC12345
✅ Đã xác nhận thanh toán thành công cho Payment ID: 1, Transaction ID: TXN-1705123456789-ABC12345
```

**Không tìm thấy Transaction ID:**
```
📥 Nhận Webhook SePay: SePayWebhookRequest{...}
⚠️ Không tìm thấy Transaction ID trong nội dung: chuyen khoan hoc phi
```

**Payment không tồn tại:**
```
📥 Nhận Webhook SePay: SePayWebhookRequest{...}
🔍 Tìm thấy Transaction ID: TXN-1705123456789-ABC12345
⚠️ Không tìm thấy Payment với Transaction ID: TXN-1705123456789-ABC12345
```

### 4.2. Xem Logs trong SePay Dashboard

1. Vào Dashboard SePay
2. Vào mục "Webhook Logs" hoặc "Lịch sử Webhook"
3. Xem các request đã gửi:
   - Status: 200 (thành công) hoặc lỗi
   - Response: "Success" hoặc message lỗi
   - Thời gian gọi

---

## 🐛 Troubleshooting

### Vấn đề 1: Webhook không được gọi

**Kiểm tra:**
1. ✅ Ngrok đang chạy?
2. ✅ Link Ngrok có đúng không?
3. ✅ Webhook URL trên SePay có đúng không?
4. ✅ SePay đã nhận được SMS/Notification từ ngân hàng?

**Giải pháp:**
- Kiểm tra SePay Dashboard → Webhook Logs
- Test webhook thủ công bằng cURL
- Đảm bảo Ngrok link không thay đổi

### Vấn đề 2: "No Transaction ID found"

**Nguyên nhân:** Nội dung chuyển khoản không chứa Transaction ID

**Giải pháp:**
- Hướng dẫn người dùng giữ nguyên nội dung khi quét QR
- Hoặc thêm Transaction ID vào QR code data

### Vấn đề 3: "Payment not found"

**Nguyên nhân:** Transaction ID không khớp hoặc Payment chưa được tạo

**Giải pháp:**
- Kiểm tra Transaction ID có đúng không
- Kiểm tra Payment có tồn tại trong database không
- Đảm bảo đã tạo Payment trước khi chuyển khoản

### Vấn đề 4: Payment không được cập nhật

**Kiểm tra:**
1. Xem logs có lỗi không
2. Kiểm tra database có được cập nhật không
3. Kiểm tra transaction có rollback không

**Giải pháp:**
- Xem logs chi tiết
- Kiểm tra database connection
- Kiểm tra có exception nào không

---

## ✅ Checklist Test

- [ ] Đã test webhook thủ công (cURL) → Response: "Success"
- [ ] Đã kiểm tra logs console → Thấy log "✅ Đã xác nhận thanh toán thành công"
- [ ] Đã kiểm tra database → Payment status = "SUCCESS"
- [ ] Đã test với chuyển khoản thật
- [ ] Đã kiểm tra SePay Dashboard → Webhook được gọi thành công
- [ ] Đã kiểm tra Fee được cập nhật (nếu có)
- [ ] Đã kiểm tra frontend tự động cập nhật

---

## 🎯 Test Script Nhanh

Chạy script này trong Browser Console để test nhanh:

```javascript
async function testSePayWebhook() {
  const token = localStorage.getItem('authToken') || localStorage.getItem('jwtToken');
  
  // 1. Tạo payment
  const createRes = await fetch('/api/payments/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({
      amount: 50000,
      studentName: 'Test Student',
      notes: 'Test SePay'
    })
  });
  
  const payment = await createRes.json();
  console.log('✅ Payment created:', payment.transactionId);
  
  // 2. Test webhook
  const webhookRes = await fetch('/api/sepay/webhook', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      id: 12345,
      gateway: 'MBBank',
      content: `chuyen khoan ${payment.transactionId} hoc phi`,
      transferType: 'in',
      transferAmount: 50000
    })
  });
  
  const result = await webhookRes.text();
  console.log('✅ Webhook response:', result);
  
  // 3. Kiểm tra status
  const statusRes = await fetch(`/api/payments/${payment.transactionId}`, {
    headers: {
      'Authorization': 'Bearer ' + token
    }
  });
  
  const finalStatus = await statusRes.json();
  console.log('✅ Final status:', finalStatus.status);
  
  if (finalStatus.status === 'SUCCESS') {
    alert('✅ Test thành công! Payment đã được cập nhật.');
  } else {
    alert('❌ Test thất bại! Status: ' + finalStatus.status);
  }
}

// Chạy test
testSePayWebhook();
```

---

**Chúc bạn test thành công! 🎉**

