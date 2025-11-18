# Kiểm Tra Cấu Hình VietQR

## ✅ Cấu Hình Hiện Tại

Bạn đã cấu hình thông tin tài khoản ngân hàng trong `application.properties`:

```properties
payment.vietqr.merchant.account.info=970422|2730788922276|NGUYEN HONG DONGDONG
```

**Giải thích:**
- **970422**: Mã ngân hàng (Bank Code/GUID)
  - 970422 = VietQR (NAPAS)
  - Các mã khác: 970415 (Vietcombank), 970416 (BIDV), 970419 (Techcombank), etc.
- **2730788922276**: Số tài khoản ngân hàng
- **NGUYEN HONG DONGDONG**: Tên chủ tài khoản

---

## 🔍 Cách Kiểm Tra QR Code Có Đúng Thông Tin

### Cách 1: Quét QR Code bằng App Ngân Hàng

1. **Tạo QR code thanh toán** (qua giao diện web hoặc API)
2. **Mở app ngân hàng** (Vietcombank, BIDV, Techcombank, MoMo, ZaloPay, etc.)
3. **Quét QR code**
4. **Kiểm tra thông tin hiển thị:**
   - ✅ Số tài khoản: `2730788922276`
   - ✅ Tên chủ tài khoản: `NGUYEN HONG DONGDONG`
   - ✅ Số tiền: Đúng với số tiền bạn đã nhập
   - ✅ Merchant name: `TMS - Quản Lý Dạy Thêm`

### Cách 2: Đọc QR Code Data

**Bước 1:** Tạo payment và lấy QR code data:
```bash
curl -X POST http://localhost:8080/api/payments/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"amount": 500000, "studentName": "Test"}'
```

**Bước 2:** Copy `qrCodeData` từ response

**Bước 3:** Parse QR code data (EMV format):
- QR code data sẽ chứa: `970422|2730788922276|NGUYEN HONG DONGDONG`
- Tìm field `38` (Merchant Account Information)
- Kiểm tra có chứa số tài khoản và tên không

### Cách 3: Sử dụng QR Code Reader Online

1. Tạo QR code và lưu ảnh
2. Vào: https://www.qr-code-generator.com/qr-code-reader/
3. Upload ảnh QR code
4. Xem dữ liệu bên trong

---

## 📋 Danh Sách Mã Ngân Hàng Phổ Biến

| Mã | Ngân hàng |
|----|-----------|
| 970422 | VietQR (NAPAS) |
| 970415 | Vietcombank (VCB) |
| 970416 | BIDV |
| 970419 | Techcombank (TCB) |
| 970418 | Agribank |
| 970403 | Vietinbank (CTG) |
| 970405 | ACB |
| 970407 | Sacombank (STB) |
| 970409 | Eximbank |
| 970412 | VPBank |
| 970414 | SHB |
| 970420 | TPBank |
| 970421 | HDBank |
| 970423 | MSB |
| 970424 | OCB |
| 970425 | PVcomBank |
| 970426 | VIB |
| 970427 | SeABank |
| 970428 | PGBank |
| 970429 | Nam A Bank |
| 970430 | ABBank |
| 970431 | VietABank |
| 970432 | BaoVietBank |
| 970433 | SCB |
| 970434 | VietBank |
| 970435 | PublicBank |
| 970436 | NCB |
| 970437 | OceanBank |
| 970438 | KienLongBank |
| 970439 | GPBank |
| 970440 | LienVietPostBank |
| 970441 | DongABank |
| 970442 | BacABank |
| 970443 | VietCapitalBank |
| 970444 | SaigonBank |
| 970445 | BanVietBank |
| 970446 | VietBank |
| 970447 | ABBank |
| 970448 | VietBank |
| 970449 | VietBank |
| 970450 | VietBank |

**Lưu ý:** Mã `970422` là mã chung cho VietQR, có thể được sử dụng bởi nhiều ngân hàng thông qua hệ thống NAPAS.

---

## 🔧 Cách Cập Nhật Cấu Hình

### Cập Nhật Trong `application.properties`:

```properties
# Format: MãNgânHàng|SốTàiKhoản|TênChủTàiKhoản
payment.vietqr.merchant.account.info=970422|2730788922276|NGUYEN HONG DONGDONG
```

**Lưu ý:**
- Tên chủ tài khoản tối đa 25 ký tự (theo chuẩn EMV)
- Số tài khoản không có khoảng trắng
- Dùng dấu `|` để phân cách các trường

### Sau Khi Cập Nhật:

1. **Restart ứng dụng:**
   ```bash
   # Dừng ứng dụng (Ctrl+C)
   # Khởi động lại
   mvn spring-boot:run
   ```

2. **Kiểm tra cấu hình đã load:**
   - Tạo payment mới
   - Kiểm tra QR code có chứa thông tin mới không

---

## 🧪 Test Cấu Hình

### Script Test Nhanh:

```javascript
// Chạy trong Browser Console sau khi tạo payment
async function testVietQRConfig() {
  const token = localStorage.getItem('authToken') || localStorage.getItem('jwtToken');
  
  // Tạo payment
  const res = await fetch('/api/payments/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({
      amount: 500000,
      studentName: 'Test',
      notes: 'Test VietQR config'
    })
  });
  
  const payment = await res.json();
  console.log('📋 Payment created:', payment);
  
  // Kiểm tra QR code data
  const qrData = payment.qrCodeData;
  console.log('📱 QR Code Data:', qrData);
  
  // Tìm Merchant Account Information (field 38)
  const field38Match = qrData.match(/38(\d{2})(.+?)(?=\d{2}|$)/);
  if (field38Match) {
    const merchantAccount = field38Match[2];
    console.log('🏦 Merchant Account Info:', merchantAccount);
    
    // Parse thông tin
    const parts = merchantAccount.split('|');
    console.log('📊 Parsed Info:');
    console.log('  - Mã ngân hàng:', parts[0]);
    console.log('  - Số tài khoản:', parts[1]);
    console.log('  - Tên tài khoản:', parts[2]);
    
    // Verify
    const expectedBankCode = '970422';
    const expectedAccount = '2730788922276';
    const expectedName = 'NGUYEN HONG DONGDONG';
    
    if (parts[0] === expectedBankCode && 
        parts[1] === expectedAccount && 
        parts[2] === expectedName) {
      console.log('✅ Cấu hình ĐÚNG!');
    } else {
      console.log('❌ Cấu hình SAI!');
      console.log('Expected:', expectedBankCode, expectedAccount, expectedName);
      console.log('Actual:', parts[0], parts[1], parts[2]);
    }
  }
  
  return payment;
}

// Chạy test
testVietQRConfig();
```

---

## ⚠️ Lưu Ý Quan Trọng

1. **Tên tài khoản:**
   - Tối đa 25 ký tự (theo chuẩn EMV)
   - Nếu dài hơn sẽ bị cắt
   - Không có dấu tiếng Việt (nên dùng không dấu)

2. **Số tài khoản:**
   - Không có khoảng trắng
   - Chỉ chứa số
   - Độ dài tùy theo ngân hàng

3. **Mã ngân hàng:**
   - Phải đúng mã của ngân hàng bạn sử dụng
   - 970422 = VietQR (dùng chung cho nhiều ngân hàng qua NAPAS)

4. **Bảo mật:**
   - Không commit file `application.properties` có thông tin thật lên Git
   - Sử dụng environment variables hoặc config file riêng cho production

---

## 🔐 Cấu Hình Production (Khuyến Nghị)

Thay vì hardcode trong `application.properties`, nên dùng environment variables:

```properties
# application.properties
payment.vietqr.merchant.account.info=${VIETQR_ACCOUNT_INFO:970422|2730788922276|NGUYEN HONG DONGDONG}
```

Sau đó set environment variable:
```bash
export VIETQR_ACCOUNT_INFO="970422|2730788922276|NGUYEN HONG DONGDONG"
```

---

## ✅ Checklist Kiểm Tra

- [ ] Cấu hình đã được cập nhật trong `application.properties`
- [ ] Ứng dụng đã được restart
- [ ] QR code được tạo thành công
- [ ] QR code có thể quét được bằng app ngân hàng
- [ ] Thông tin hiển thị đúng: số tài khoản, tên tài khoản
- [ ] QR code data chứa đúng thông tin (field 38)

---

**Cấu hình của bạn đã sẵn sàng! 🎉**

QR code sẽ chứa thông tin:
- Mã ngân hàng: **970422** (VietQR)
- Số tài khoản: **2730788922276**
- Tên tài khoản: **NGUYEN HONG DONGDONG**

