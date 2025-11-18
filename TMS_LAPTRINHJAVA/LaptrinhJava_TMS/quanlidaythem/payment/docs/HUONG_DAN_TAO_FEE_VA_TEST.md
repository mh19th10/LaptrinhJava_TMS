# Hướng Dẫn Tạo Fee và Test Thanh Toán

## 📋 Bước 1: Tạo Khoản Nợ (Fee) trong MySQL

### Cách 1: Chạy SQL Script (Khuyến nghị)

1. **Mở MySQL Workbench hoặc MySQL Command Line**

2. **Chọn database:**
   ```sql
   USE tms;
   ```

3. **Chạy script SQL:**

   ```sql
   -- Tạo khoản nợ mẫu
   INSERT INTO fees (student_name, class_name, month, amount, paid, due_date, status) VALUES
   ('Nguyễn Văn A', 'Toán nâng cao 9', 'Tháng 11/2024', 500000, 0, '2024-11-30', 'UNPAID'),
   ('Nguyễn Văn A', 'Văn luyện thi', 'Tháng 11/2024', 750000, 0, '2024-11-30', 'UNPAID'),
   ('Trần Thị B', 'Toán cơ bản 8', 'Tháng 11/2024', 600000, 300000, '2024-11-30', 'PARTIAL');
   ```

4. **Kiểm tra dữ liệu:**
   ```sql
   SELECT * FROM fees;
   ```

### Cách 2: Tạo Fee cho học sinh cụ thể

**Thay đổi thông tin theo học sinh của bạn:**

```sql
INSERT INTO fees (student_name, class_name, month, amount, paid, due_date, status) 
VALUES 
('TÊN_HỌC_SINH', 'TÊN_LỚP', 'Tháng 11/2024', 500000, 0, '2024-11-30', 'UNPAID');
```

**Giải thích các trường:**
- `student_name`: Tên học sinh (phải khớp với tên đăng nhập hoặc tên trong hệ thống)
- `class_name`: Tên lớp học
- `month`: Tháng học phí (ví dụ: "Tháng 11/2024")
- `amount`: Tổng số tiền cần thanh toán (VND)
- `paid`: Số tiền đã thanh toán (0 nếu chưa thanh toán)
- `due_date`: Ngày hết hạn thanh toán (format: YYYY-MM-DD)
- `status`: Trạng thái
  - `UNPAID`: Chưa thanh toán
  - `PARTIAL`: Thanh toán một phần
  - `PAID`: Đã thanh toán đủ
  - `OVERDUE`: Quá hạn

---

## 🧪 Bước 2: Test trên Localhost

### 2.1. Khởi động ứng dụng

```bash
mvn spring-boot:run
```

### 2.2. Đăng nhập với tài khoản STUDENT

1. Vào: `http://localhost:8080/login.html`
2. Đăng nhập với tài khoản có role `STUDENT`
3. **Lưu ý:** Tên học sinh trong database phải khớp với tên trong hệ thống

### 2.3. Vào trang học phí

1. Vào: `http://localhost:8080/tuition_student.html`
2. Bạn sẽ thấy danh sách học phí đã tạo

### 2.4. Test thanh toán

1. **Click nút "Thanh toán"** (màu xanh) cho một học phí chưa thanh toán
2. **Modal hiển thị:**
   - Thông tin: Lớp, Tháng, Số tiền
   - QR code sẽ được tạo tự động
   - Transaction ID hiển thị ở dưới QR code

3. **Test webhook SePay:**
   - Copy Transaction ID từ modal
   - Test webhook thủ công (xem hướng dẫn bên dưới)

---

## 🧪 Bước 3: Test Webhook SePay

### Test thủ công với cURL:

```bash
# Thay TXN-XXX-XXX bằng Transaction ID thật từ modal
curl -X POST http://localhost:8080/api/sepay/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "id": 12345,
    "gateway": "MBBank",
    "content": "NGUYEN VAN A chuyen khoan TXN-1705123456789-ABC12345 hoc phi",
    "transferType": "in",
    "transferAmount": 500000
  }'
```

**Response:** `Success`

**Kiểm tra:**
- Payment status = "SUCCESS"
- Fee đã được cập nhật (paid tăng lên)

---

## 📊 Kiểm Tra Kết Quả

### 1. Kiểm tra trong Database:

```sql
-- Xem tất cả fees
SELECT * FROM fees;

-- Xem payments đã tạo
SELECT * FROM payments ORDER BY id DESC;

-- Xem fee đã được cập nhật chưa
SELECT 
    f.id,
    f.student_name,
    f.class_name,
    f.amount,
    f.paid,
    f.status,
    p.transaction_id,
    p.status as payment_status
FROM fees f
LEFT JOIN payments p ON f.id = p.fee_id
ORDER BY f.id DESC;
```

### 2. Kiểm tra trên giao diện:

- Vào lại trang học phí: `http://localhost:8080/tuition_student.html`
- Học phí đã thanh toán sẽ hiển thị "Đã thanh toán"
- Nút "Thanh toán" sẽ biến mất

---

## 🎯 Ví Dụ Hoàn Chỉnh

### Tạo Fee cho học sinh "Nguyễn Văn A":

```sql
INSERT INTO fees (student_name, class_name, month, amount, paid, due_date, status) 
VALUES 
('Nguyễn Văn A', 'Toán nâng cao 9', 'Tháng 11/2024', 500000, 0, '2024-11-30', 'UNPAID');
```

### Test thanh toán:

1. Đăng nhập với tài khoản học sinh "Nguyễn Văn A"
2. Vào trang học phí → Thấy học phí 500,000 VNĐ
3. Click "Thanh toán" → Modal hiển thị QR code
4. Copy Transaction ID (ví dụ: `TXN-1705123456789-ABC12345`)
5. Test webhook:
   ```bash
   curl -X POST http://localhost:8080/api/sepay/webhook \
     -H "Content-Type: application/json" \
     -d '{
       "id": 12345,
       "gateway": "MBBank",
       "content": "chuyen khoan TXN-1705123456789-ABC12345",
       "transferType": "in",
       "transferAmount": 500000
     }'
   ```
6. Kiểm tra:
   - Payment status = "SUCCESS"
   - Fee paid = 500000
   - Fee status = "PAID"

---

## ⚠️ Lưu Ý

1. **Tên học sinh phải khớp:**
   - Tên trong bảng `fees` phải khớp với tên học sinh đăng nhập
   - Hoặc hệ thống phải filter theo user hiện tại

2. **Status values:**
   - `UNPAID`: Chưa thanh toán (paid = 0)
   - `PARTIAL`: Thanh toán một phần (paid < amount)
   - `PAID`: Đã thanh toán đủ (paid >= amount)
   - `OVERDUE`: Quá hạn

3. **Số tiền:**
   - `amount`: Tổng số tiền (VND)
   - `paid`: Số tiền đã thanh toán (VND)
   - `amount - paid`: Số tiền còn nợ

---

## ✅ Checklist

- [ ] Đã tạo fee trong MySQL
- [ ] Đã khởi động ứng dụng
- [ ] Đã đăng nhập với tài khoản STUDENT
- [ ] Đã vào trang học phí và thấy danh sách
- [ ] Đã click "Thanh toán" và thấy QR code
- [ ] Đã test webhook thủ công
- [ ] Đã kiểm tra Payment được tạo
- [ ] Đã kiểm tra Fee được cập nhật

---

**Chúc bạn test thành công! 🎉**

