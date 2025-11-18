# Hướng Dẫn Test Chức Năng Thanh Toán QR Code

## 🎯 Mục Lục

1. [Chuẩn Bị](#chuẩn-bị)
2. [Test Qua Giao Diện Web](#test-qua-giao-diện-web)
3. [Test Qua API (Postman/cURL)](#test-qua-api-postmancurl)
4. [Test Callback Thanh Toán](#test-callback-thanh-toán)
5. [Kiểm Tra Kết Quả](#kiểm-tra-kết-quả)

---

## 📋 Chuẩn Bị

### Bước 1: Khởi động ứng dụng

```bash
cd TMS_LAPTRINHJAVA/LaptrinhJava_TMS/quanlidaythem
mvn clean install
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

### Bước 2: Đăng nhập với tài khoản STUDENT

1. Mở trình duyệt: `http://localhost:8080/login.html`
2. Đăng nhập với tài khoản có role `STUDENT`
3. Lưu JWT token (sẽ cần cho test API)

### Bước 3: Chuẩn bị dữ liệu Fee (nếu chưa có)

**Option 1: Tạo Fee qua Database**
```sql
INSERT INTO fees (student_name, class_name, month, amount, paid, due_date, status) 
VALUES ('Nguyễn Văn A', 'Toán nâng cao 9', 'Tháng 10', 500000, 0, '2024-10-31', 'UNPAID');
```

**Option 2: Tạo Fee qua Admin Panel** (nếu có)

---

## 🌐 Test Qua Giao Diện Web

### Test Case 1: Tạo mã thanh toán và hiển thị QR code

**Bước 1:** Truy cập trang học phí
- URL: `http://localhost:8080/tuition_student.html`
- Đảm bảo đã đăng nhập với role STUDENT

**Bước 2:** Kiểm tra danh sách học phí
- Xem bảng hiển thị các học phí
- Học phí chưa thanh toán sẽ có nút "Thanh toán" màu xanh

**Bước 3:** Click nút "Thanh toán"
- Modal sẽ hiển thị
- Thông tin: Lớp, Tháng, Số tiền
- QR code sẽ được tạo và hiển thị sau vài giây

**Bước 4:** Kiểm tra QR code
- QR code hiển thị rõ ràng
- Transaction ID hiển thị ở dưới QR code
- Trạng thái: "Đang chờ thanh toán"

**Kết quả mong đợi:**
- ✅ Modal hiển thị đúng
- ✅ QR code được tạo thành công
- ✅ Transaction ID hiển thị
- ✅ Polling tự động bắt đầu (kiểm tra mỗi 3 giây)

---

### Test Case 2: Simulate thanh toán thành công

**Cách 1: Sử dụng endpoint confirm (Testing)**

1. **Lấy Transaction ID:**
   - Từ modal thanh toán, copy Transaction ID
   - Hoặc mở Developer Console (F12) → Network tab → Xem response của `/api/payments/create`

2. **Gọi API confirm:**
   ```bash
   curl -X POST "http://localhost:8080/api/payments/TXN-XXXXX-XXXXX/confirm?status=SUCCESS" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json"
   ```

3. **Kiểm tra kết quả:**
   - Modal tự động cập nhật: "Thanh toán thành công"
   - Bảng học phí tự động reload
   - Học phí đã thanh toán sẽ hiển thị "Đã thanh toán"

**Cách 2: Sử dụng JavaScript Console**

Mở Developer Console (F12) và chạy:

```javascript
// Lấy transactionId từ modal (nếu đang mở)
const transactionId = document.getElementById('transactionIdText').textContent.split(': ')[1];

// Hoặc lấy từ biến global (nếu có)
// const transactionId = currentPaymentTransactionId;

// Xác nhận thanh toán
fetch(`/api/payments/${transactionId}/confirm?status=SUCCESS`, {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('authToken') || localStorage.getItem('jwtToken'),
    'Content-Type': 'application/json'
  }
})
.then(r => r.json())
.then(data => {
  console.log('✅ Payment confirmed:', data);
  alert('Thanh toán thành công!');
  // Reload trang
  location.reload();
});
```

---

## 🔌 Test Qua API (Postman/cURL)

### Test 1: Tạo mã thanh toán

**cURL:**
```bash
curl -X POST http://localhost:8080/api/payments/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "amount": 500000,
    "feeId": 1,
    "studentName": "Nguyễn Văn A",
    "notes": "Thanh toán học phí tháng 10"
  }'
```

**Postman:**
- Method: `POST`
- URL: `http://localhost:8080/api/payments/create`
- Headers:
  - `Content-Type: application/json`
  - `Authorization: Bearer YOUR_JWT_TOKEN`
- Body (JSON):
  ```json
  {
    "amount": 500000,
    "feeId": 1,
    "studentName": "Nguyễn Văn A",
    "notes": "Thanh toán học phí tháng 10"
  }
  ```

**Response mong đợi:**
```json
{
  "id": 1,
  "transactionId": "TXN-1705123456789-ABC12345",
  "amount": 500000,
  "status": "PENDING",
  "feeId": 1,
  "studentName": "Nguyễn Văn A",
  "paymentMethod": "QR_CODE",
  "qrCodeBase64": "data:image/png;base64,iVBORw0KGgo...",
  "qrCodeData": "000201010212...",
  "createdAt": "2024-01-15T10:30:00",
  "notes": "Thanh toán học phí tháng 10"
}
```

**Kiểm tra:**
- ✅ Status code: 200
- ✅ Có `transactionId`
- ✅ Có `qrCodeBase64` (bắt đầu với `data:image/png;base64,`)
- ✅ Status = "PENDING"

---

### Test 2: Kiểm tra trạng thái thanh toán

**cURL:**
```bash
curl http://localhost:8080/api/payments/TXN-1705123456789-ABC12345 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "id": 1,
  "transactionId": "TXN-1705123456789-ABC12345",
  "amount": 500000,
  "status": "PENDING",
  ...
}
```

---

### Test 3: Xác nhận thanh toán (Testing)

**cURL:**
```bash
curl -X POST "http://localhost:8080/api/payments/TXN-1705123456789-ABC12345/confirm?status=SUCCESS" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Xác nhận thanh toán thành công",
  "payment": {
    "id": 1,
    "transactionId": "TXN-1705123456789-ABC12345",
    "status": "SUCCESS",
    "completedAt": "2024-01-15T10:35:00",
    ...
  }
}
```

---

### Test 4: Test Callback (Webhook)

**cURL:**
```bash
curl -X POST http://localhost:8080/api/payments/callback \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN-1705123456789-ABC12345",
    "status": "SUCCESS",
    "paymentMethod": "QR_CODE",
    "notes": "Thanh toán thành công qua MoMo"
  }'
```

**Lưu ý:** Endpoint này không cần authentication (public endpoint)

**Response:**
```json
{
  "success": true,
  "message": "Cập nhật thanh toán thành công",
  "payment": {
    "id": 1,
    "transactionId": "TXN-1705123456789-ABC12345",
    "status": "SUCCESS",
    ...
  }
}
```

---

## 🔄 Test Luồng Hoàn Chỉnh

### Scenario 1: Thanh toán thành công

1. **Tạo thanh toán:**
   ```bash
   POST /api/payments/create
   → Lấy transactionId từ response
   ```

2. **Kiểm tra trạng thái (PENDING):**
   ```bash
   GET /api/payments/{transactionId}
   → Status = "PENDING"
   ```

3. **Xác nhận thanh toán:**
   ```bash
   POST /api/payments/{transactionId}/confirm?status=SUCCESS
   → Status = "SUCCESS"
   ```

4. **Kiểm tra lại trạng thái:**
   ```bash
   GET /api/payments/{transactionId}
   → Status = "SUCCESS"
   → completedAt có giá trị
   ```

5. **Kiểm tra Fee đã được cập nhật:**
   ```bash
   GET /api/fees
   → Fee có feeId tương ứng:
     - paid đã tăng lên
     - status = "PAID" hoặc "PARTIAL"
   ```

---

### Scenario 2: Test Idempotency (Callback nhiều lần)

1. Tạo thanh toán và xác nhận lần 1:
   ```bash
   POST /api/payments/{transactionId}/confirm?status=SUCCESS
   → Status = "SUCCESS"
   ```

2. Gọi lại callback lần 2:
   ```bash
   POST /api/payments/callback
   {
     "transactionId": "...",
     "status": "SUCCESS"
   }
   → Error: "Giao dịch đã được xử lý trước đó"
   ```

**Kết quả mong đợi:**
- ✅ Lần 1: Thành công
- ✅ Lần 2: Báo lỗi (idempotency hoạt động)

---

### Scenario 3: Thanh toán thất bại

1. Tạo thanh toán
2. Xác nhận với status = "FAILED":
   ```bash
   POST /api/payments/{transactionId}/confirm?status=FAILED
   ```

3. Kiểm tra:
   - Payment status = "FAILED"
   - Fee KHÔNG được cập nhật (paid không thay đổi)

---

## 🧪 Test Với QR Code Thực Tế

### Bước 1: Tạo QR code

1. Vào trang học phí: `http://localhost:8080/tuition_student.html`
2. Click "Thanh toán" cho một học phí chưa thanh toán
3. Copy QR code data từ modal (hoặc từ response API)

### Bước 2: Quét QR code

**Option 1: Sử dụng app thanh toán thật**
- Mở app MoMo, ZaloPay, VNPay, hoặc app ngân hàng
- Quét QR code
- Xem thông tin thanh toán hiển thị

**Option 2: Sử dụng QR code reader online**
- Vào: https://www.qr-code-generator.com/qr-code-reader/
- Upload ảnh QR code hoặc quét
- Xem dữ liệu bên trong QR code

**Kiểm tra QR code data:**
- Format: EMV QR Code (VietQR)
- Chứa: Transaction ID, Amount, Merchant Info, CRC

### Bước 3: Verify QR code data

QR code data sẽ có format:
```
00020101021238520410...
```

Các trường chính:
- `00 02 01` - Payload Format Indicator
- `01 02 12` - Point of Initiation (Dynamic)
- `38 XX ...` - Merchant Account Information
- `54 XX ...` - Transaction Amount
- `62 XX ...` - Additional Data (Transaction ID)
- `63 04 XXXX` - CRC Checksum

---

## 📊 Kiểm Tra Kết Quả

### 1. Kiểm tra Database

**Kiểm tra bảng `payments`:**
```sql
SELECT * FROM payments ORDER BY id DESC LIMIT 5;
```

**Kiểm tra:**
- ✅ Có record mới với status = "PENDING" hoặc "SUCCESS"
- ✅ `transactionId` unique
- ✅ `qrCodeData` không null
- ✅ `createdAt` có giá trị
- ✅ Nếu đã thanh toán: `completedAt` có giá trị

**Kiểm tra bảng `fees`:**
```sql
SELECT * FROM fees WHERE id = {feeId};
```

**Kiểm tra:**
- ✅ `paid` đã tăng lên (nếu thanh toán thành công)
- ✅ `status` = "PAID" hoặc "PARTIAL" (nếu thanh toán thành công)

---

### 2. Kiểm tra Logs

Xem console log của ứng dụng:
- Kiểm tra có lỗi không
- Xem SQL queries
- Xem payment flow

---

### 3. Kiểm tra Frontend

**Browser Console (F12):**
- Xem Network tab: Các API calls
- Xem Console tab: Logs và errors
- Xem Application tab: LocalStorage (JWT token)

**Kiểm tra:**
- ✅ API `/api/payments/create` trả về 200
- ✅ QR code hiển thị trong modal
- ✅ Polling hoạt động (requests mỗi 3 giây)
- ✅ Sau khi thanh toán: Modal cập nhật, bảng reload

---

## 🐛 Troubleshooting

### Lỗi: "Unauthorized" khi gọi API

**Nguyên nhân:** JWT token không hợp lệ hoặc hết hạn

**Giải pháp:**
1. Đăng nhập lại để lấy token mới
2. Kiểm tra token trong localStorage:
   ```javascript
   console.log(localStorage.getItem('authToken') || localStorage.getItem('jwtToken'));
   ```
3. Đảm bảo token được gửi trong header:
   ```javascript
   headers: {
     'Authorization': 'Bearer ' + token
   }
   ```

---

### Lỗi: "Không tìm thấy học phí"

**Nguyên nhân:** FeeId không tồn tại trong database

**Giải pháp:**
1. Kiểm tra fee có tồn tại:
   ```sql
   SELECT * FROM fees WHERE id = {feeId};
   ```
2. Tạo fee mới nếu chưa có
3. Hoặc không truyền `feeId` (để null) nếu thanh toán độc lập

---

### QR code không hiển thị

**Nguyên nhân:**
- Base64 data không đúng format
- Lỗi khi tạo QR code image

**Giải pháp:**
1. Kiểm tra `qrCodeBase64` trong response:
   - Phải bắt đầu với `data:image/png;base64,`
   - Phải có dữ liệu sau dấu phẩy
2. Kiểm tra console có lỗi không
3. Thử hiển thị QR code trực tiếp:
   ```html
   <img src="data:image/png;base64,..." />
   ```

---

### Polling không hoạt động

**Nguyên nhân:**
- JavaScript error
- TransactionId không đúng

**Giểm pháp:**
1. Mở Developer Console (F12)
2. Xem có lỗi JavaScript không
3. Kiểm tra `currentPaymentTransactionId` có giá trị không
4. Kiểm tra Network tab: Có requests đến `/api/payments/{transactionId}` không

---

### Fee không được cập nhật sau thanh toán

**Nguyên nhân:**
- FeeId = null
- Fee không tồn tại
- Lỗi khi cập nhật

**Giải pháp:**
1. Kiểm tra Payment có `feeId` không:
   ```sql
   SELECT fee_id FROM payments WHERE transaction_id = '...';
   ```
2. Kiểm tra Fee có tồn tại không
3. Xem log server có lỗi không
4. Kiểm tra `updateFeeAfterPayment()` có được gọi không

---

## ✅ Checklist Test

- [ ] Tạo thanh toán thành công
- [ ] QR code hiển thị đúng
- [ ] Transaction ID hiển thị
- [ ] Polling hoạt động (kiểm tra mỗi 3 giây)
- [ ] Xác nhận thanh toán thành công
- [ ] Payment status cập nhật = "SUCCESS"
- [ ] Fee được cập nhật (paid tăng, status thay đổi)
- [ ] Bảng học phí tự động reload
- [ ] Idempotency hoạt động (callback nhiều lần)
- [ ] QR code có thể quét được
- [ ] QR code data đúng format VietQR

---

## 🎯 Test Script Nhanh

**Chạy script này trong Browser Console để test nhanh:**

```javascript
// 1. Tạo thanh toán
async function testPayment() {
  const token = localStorage.getItem('authToken') || localStorage.getItem('jwtToken');
  
  // Tạo payment
  const createRes = await fetch('/api/payments/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({
      amount: 500000,
      feeId: 1,
      studentName: 'Test Student',
      notes: 'Test payment'
    })
  });
  
  const payment = await createRes.json();
  console.log('✅ Payment created:', payment);
  
  // Đợi 2 giây
  await new Promise(r => setTimeout(r, 2000));
  
  // Xác nhận thanh toán
  const confirmRes = await fetch(`/api/payments/${payment.transactionId}/confirm?status=SUCCESS`, {
    method: 'POST',
    headers: {
      'Authorization': 'Bearer ' + token
    }
  });
  
  const result = await confirmRes.json();
  console.log('✅ Payment confirmed:', result);
  
  // Kiểm tra trạng thái
  const statusRes = await fetch(`/api/payments/${payment.transactionId}`, {
    headers: {
      'Authorization': 'Bearer ' + token
    }
  });
  
  const status = await statusRes.json();
  console.log('✅ Final status:', status);
  
  return { payment, result, status };
}

// Chạy test
testPayment().then(data => {
  console.log('🎉 Test completed!', data);
  alert('Test hoàn tất! Xem console để xem kết quả.');
});
```

---

**Chúc bạn test thành công! 🎉**

