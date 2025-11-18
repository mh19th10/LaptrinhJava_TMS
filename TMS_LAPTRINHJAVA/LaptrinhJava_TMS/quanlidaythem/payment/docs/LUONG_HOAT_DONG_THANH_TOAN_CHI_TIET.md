# Luồng Hoạt Động Chi Tiết Hệ Thống Thanh Toán

## 📋 Mục Lục

1. [Tổng Quan Hệ Thống](#tổng-quan-hệ-thống)
2. [Kiến Trúc Hệ Thống](#kiến-trúc-hệ-thống)
3. [Luồng Thanh Toán QR Code](#luồng-thanh-toán-qr-code)
4. [Luồng SePay Webhook](#luồng-sepay-webhook)
5. [Cấu Trúc Dữ Liệu](#cấu-trúc-dữ-liệu)
6. [Các Thành Phần Chính](#các-thành-phần-chính)
7. [Xử Lý Lỗi và Edge Cases](#xử-lý-lỗi-và-edge-cases)

---

## 🎯 Tổng Quan Hệ Thống

Hệ thống thanh toán hỗ trợ **2 phương thức xác nhận thanh toán**:

1. **QR Code Callback** - Payment Gateway gọi callback API
2. **SePay Webhook** - SePay phát hiện tiền vào tài khoản và gọi webhook (ƯU VIỆT HƠN)

### Điểm Khác Biệt

| Đặc điểm | QR Code Callback | SePay Webhook |
|----------|-----------------|---------------|
| **Cơ chế** | Payment Gateway tự gọi API | SePay theo dõi tài khoản và gọi webhook |
| **Tự động** | Phụ thuộc vào Payment Gateway | Tự động 100% |
| **Bảo mật** | Cần IP whitelist/signature | API Key authentication |
| **Extract Transaction ID** | Có sẵn trong request | Dùng Regex từ nội dung chuyển khoản |
| **Idempotency** | Kiểm tra trong callback | Kiểm tra trong webhook |

---

## 🏗️ Kiến Trúc Hệ Thống

```
┌─────────────────────────────────────────────────────────────┐
│                      CLIENT LAYER                           │
│  (Frontend: Student Portal, Admin Dashboard)                │
└────────────────┬────────────────────────────────────────────┘
                 │
                 │ HTTP REST API (JWT Auth)
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                   CONTROLLER LAYER                          │
│  ┌────────────────────┐  ┌──────────────────────────────┐  │
│  │ PaymentController  │  │ SePayWebhookController       │  │
│  │ - create           │  │ - handleSePayWebhook         │  │
│  │ - getStatus        │  │                             │  │
│  │ - callback         │  │                             │  │
│  │ - confirm          │  │                             │  │
│  └────────┬───────────┘  └────────┬─────────────────────┘  │
│           │                        │                         │
└───────────┼────────────────────────┼─────────────────────────┘
            │                        │
            │                        │
            ▼                        ▼
┌─────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                            │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ PaymentService                                        │ │
│  │ - createPayment()                                     │ │
│  │ - processPaymentCallback()                           │ │
│  │ - confirmPaymentSuccess()  ← Webhook gọi             │ │
│  │ - getPaymentByTransactionId()                        │ │
│  │ - updateFeeAfterPayment()                            │ │
│  └────────┬──────────────────────────────────────────────┘ │
│           │                                                  │
│  ┌────────┴──────────────────────────────────────────────┐ │
│  │ VietQRGenerator                                       │ │
│  │ - generateCompleteVietQRData()                       │ │
│  │ - calculateCRC16()                                   │ │
│  │ - buildMerchantInfo()                                │ │
│  └───────────────────────────────────────────────────────┘ │
└───────────┬─────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│                   REPOSITORY LAYER                          │
│  ┌─────────────────┐  ┌──────────────────┐                │
│  │ PaymentRepository│  │ FeeRepository    │                │
│  │ - save()        │  │ - findById()     │                │
│  │ - findByTransactionId()│ - save()      │                │
│  └─────────────────┘  └──────────────────┘                │
└───────────┬─────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATABASE                               │
│  ┌──────────────┐         ┌──────────────┐                │
│  │ payments     │         │ fees         │                │
│  │ - id         │ 1──N    │ - id         │                │
│  │ - transactionId│        │ - amount     │                │
│  │ - feeId      │         │ - paid       │                │
│  │ - status     │         │ - status     │                │
│  └──────────────┘         └──────────────┘                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Luồng Thanh Toán QR Code

### **Bước 1: Client Yêu Cầu Tạo Thanh Toán**

**API Call:**
```http
POST /api/payments/create
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "amount": 500000,
  "feeId": 1,
  "studentName": "Nguyễn Văn A",
  "notes": "Thanh toán học phí tháng 10/2024"
}
```

**Security:**
- ✅ JWT Token required
- ✅ Role: `STUDENT` hoặc `ADMIN`
- ✅ Validated by `@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")`

---

### **Bước 2: PaymentController Xử Lý**

**File:** `PaymentController.java` (dòng 30-35)

```java
@PostMapping("/create")
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
public ResponseEntity<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
    PaymentResponse response = paymentService.createPayment(request);
    return ResponseEntity.ok(response);
}
```

**Xử lý:**
1. Spring Security kiểm tra JWT token và role
2. Validate request body (Jackson deserialization)
3. Gọi `PaymentService.createPayment()`

---

### **Bước 3: PaymentService.validateRequest()**

**File:** `PaymentService.java` (dòng 54-64)

```java
// Validate số tiền
if (request.getAmount() == null || request.getAmount() <= 0) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số tiền thanh toán không hợp lệ");
}

// Kiểm tra Fee tồn tại (nếu có feeId)
if (request.getFeeId() != null) {
    feeRepository.findById(request.getFeeId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phí"));
}
```

**Validation Rules:**
- ✅ `amount` phải > 0
- ✅ `feeId` phải tồn tại trong database (nếu có)

---

### **Bước 4: Tạo Payment Entity**

**File:** `PaymentService.java` (dòng 66-75)

```java
Payment payment = new Payment();
payment.setTransactionId(generateTransactionId());  // TXN-{timestamp}-{UUID}
payment.setAmount(request.getAmount());              // 500000
payment.setFeeId(request.getFeeId());                // 1 (optional)
payment.setStudentName(request.getStudentName() != null ? request.getStudentName() : "N/A");
payment.setPaymentMethod("QR_CODE");
payment.setStatus("PENDING");
payment.setNotes(request.getNotes());
payment.setCreatedAt(LocalDateTime.now());
```

**Transaction ID Format:**
```
TXN-{timestamp}-{8 ký tự UUID}
Ví dụ: TXN-1705123456789-ABC12345
```

**Logic:**
- `generateTransactionId()`: `"TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()`
- `status` mặc định: `"PENDING"`
- `paymentMethod` mặc định: `"QR_CODE"`

---

### **Bước 5: Tạo VietQR Data**

**File:** `PaymentService.java` (dòng 78-84)
**File:** `VietQRGenerator.java`

```java
String additionalData = request.getNotes() != null ? request.getNotes() : "";
String qrData = vietQRGenerator.generateCompleteVietQRData(
    payment.getTransactionId(), 
    payment.getAmount(),
    additionalData
);
payment.setQrCodeData(qrData);
```

**VietQR Data Structure (EMV QR Code - TLV Format):**

```
Tag-Length-Value (TLV) Format:

00 02 01                    // Payload Format Indicator (Tag 00, Length 02, Value "01")
01 02 12                    // Point of Initiation Method (Tag 01, Length 02, Value "12" = Dynamic)
38 XX {MerchantInfo}        // Merchant Account Information (Tag 38)
   00 0A A000000727         // GUID VietQR
   01 XX {Beneficiary}      // Beneficiary Organization
     00 06 970422           // Bank BIN
     01 13 2730788922276    // Account Number
   02 08 QRIBFTTA           // Service Code
52 04 0000                  // Merchant Category Code
53 03 704                   // Transaction Currency (704 = VND)
54 XX {amount}              // Transaction Amount
58 02 VN                    // Country Code
62 XX {AdditionalData}      // Additional Data Field
   08 XX {Content}          // Purpose of Transaction (chứa Transaction ID)
63 04 {CRC16}               // CRC Checksum
```

**Chi tiết VietQRGenerator:**

1. **Merchant Account Info (Tag 38):**
   ```java
   // Cấu hình từ application.properties
   payment.vietqr.merchant.account.info=970422|2730788922276|NGUYEN HONG DONGDONG
   
   // Parse: bankBin|accountNum|merchantName
   String bankBin = "970422";           // MB Bank
   String accountNum = "2730788922276";
   String merchantName = "NGUYEN HONG DONGDONG";
   ```

2. **Additional Data (Tag 62 - Purpose of Transaction):**
   ```java
   // Format: "TXN-{id} {notes}"
   String fullContent = transactionId + " " + cleanContent;
   // Ví dụ: "TXN-1705123456789-ABC12345 Thanh toan hoc phi..."
   ```
   - Transaction ID được đặt **ĐẦU TIÊN** trong nội dung
   - SePay sẽ extract Transaction ID từ nội dung chuyển khoản

3. **CRC16 Checksum (Tag 63):**
   ```java
   // Thuật toán CRC16-CCITT (0x1021)
   // Tính CRC cho toàn bộ string (bao gồm cả "6304")
   String crc = calculateCRC16(data + "6304");
   ```

---

### **Bước 6: Lưu Payment vào Database**

**File:** `PaymentService.java` (dòng 87)

```java
payment = paymentRepository.save(payment);
```

**Database Record:**
```sql
INSERT INTO payments (
    transaction_id, amount, fee_id, student_name, 
    payment_method, status, qr_code_data, 
    notes, created_at
) VALUES (
    'TXN-1705123456789-ABC12345', 
    500000, 
    1, 
    'Nguyễn Văn A',
    'QR_CODE', 
    'PENDING', 
    '00020101021238520410...',  -- VietQR data
    'Thanh toán học phí tháng 10/2024',
    '2024-01-15 10:30:00'
);
```

---

### **Bước 7: Tạo QR Code Image (Base64)**

**File:** `PaymentService.java` (dòng 244-282)

```java
private String generateQRCodeImage(String data) {
    // 1. Cấu hình QR Code
    Map<EncodeHintType, Object> hints = new HashMap<>();
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);  // Error correction cao nhất
    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
    hints.put(EncodeHintType.MARGIN, 1);
    
    // 2. Tạo QR Code BitMatrix
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300, hints);
    
    // 3. Vẽ QR Code thành BufferedImage
    BufferedImage qrImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = (Graphics2D) qrImage.getGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, 300, 300);
    graphics.setColor(Color.BLACK);
    
    for (int i = 0; i < 300; i++) {
        for (int j = 0; j < 300; j++) {
            if (bitMatrix.get(i, j)) {
                graphics.fillRect(i, j, 1, 1);
            }
        }
    }
    
    // 4. Convert thành PNG bytes và encode base64
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(qrImage, "PNG", baos);
    byte[] imageBytes = baos.toByteArray();
    return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
}
```

**Kết quả:**
```java
String qrCodeBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...";
```

**Frontend sử dụng:**
```html
<img src="data:image/png;base64,iVBORw0KGgo..." alt="QR Code" />
```

---

### **Bước 8: Trả về Response**

**File:** `PaymentService.java` (dòng 93-106)

```java
PaymentResponse response = new PaymentResponse();
response.setId(payment.getId());
response.setTransactionId(payment.getTransactionId());
response.setAmount(payment.getAmount());
response.setStatus(payment.getStatus());                    // "PENDING"
response.setFeeId(payment.getFeeId());
response.setStudentName(payment.getStudentName());
response.setPaymentMethod(payment.getPaymentMethod());      // "QR_CODE"
response.setQrCodeBase64(qrCodeBase64);                     // QR code image
response.setQrCodeData(qrData);                             // VietQR data (để test)
response.setCreatedAt(payment.getCreatedAt());
response.setNotes(payment.getNotes());
```

**Response JSON:**
```json
{
  "id": 1,
  "transactionId": "TXN-1705123456789-ABC12345",
  "amount": 500000,
  "status": "PENDING",
  "feeId": 1,
  "studentName": "Nguyễn Văn A",
  "paymentMethod": "QR_CODE",
  "qrCodeBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "qrCodeData": "00020101021238520410...",
  "createdAt": "2024-01-15T10:30:00",
  "notes": "Thanh toán học phí tháng 10/2024"
}
```

---

### **Bước 9: Người Dùng Quét QR Code**

**Quy trình:**
1. Người dùng mở app ngân hàng (MoMo, ZaloPay, VNPay, ...)
2. Chọn tính năng "Quét QR Code"
3. Quét QR code từ màn hình
4. App đọc QR code data (VietQR format)
5. App hiển thị thông tin:
   - **Số tiền:** 500,000 VND
   - **Người nhận:** NGUYEN HONG DONGDONG
   - **Số tài khoản:** 2730788922276
   - **Ngân hàng:** MB Bank
   - **Nội dung:** TXN-1705123456789-ABC12345 Thanh toan hoc phi...
6. Người dùng xác nhận thanh toán
7. App chuyển khoản với nội dung chứa **Transaction ID**

---

### **Bước 10: Payment Gateway Callback (Optional)**

**Nếu Payment Gateway hỗ trợ callback:**

```http
POST /api/payments/callback
Content-Type: application/json

{
  "transactionId": "TXN-1705123456789-ABC12345",
  "status": "SUCCESS",
  "paymentMethod": "QR_CODE",
  "notes": "Thanh toán thành công qua MoMo"
}
```

**File:** `PaymentService.processPaymentCallback()` (dòng 113-151)

**Xử lý:**
1. Tìm Payment theo transactionId
2. Kiểm tra idempotency (nếu đã SUCCESS thì throw exception)
3. Cập nhật Payment status = "SUCCESS"
4. Cập nhật Fee (nếu có feeId)

---

## 🌐 Luồng SePay Webhook (ƯU VIỆT HƠN)

### **Tổng Quan SePay**

SePay là dịch vụ theo dõi tài khoản ngân hàng và tự động phát hiện khi có tiền vào tài khoản. SePay sẽ:
1. Kết nối với ngân hàng (qua API hoặc screen scraping)
2. Theo dõi các giao dịch chuyển khoản vào
3. Khi phát hiện tiền vào, gọi webhook của hệ thống

**Ưu điểm:**
- ✅ Tự động 100%, không phụ thuộc Payment Gateway
- ✅ Hoạt động với mọi ngân hàng
- ✅ Extract Transaction ID từ nội dung chuyển khoản

---

### **Bước 1: SePay Phát Hiện Tiền Vào**

Khi có tiền vào tài khoản ngân hàng, SePay phát hiện và gửi webhook:

```http
POST /api/sepay/webhook
Authorization: Bearer {SEPAY_API_KEY}
Content-Type: application/json

{
  "id": 12345,
  "gateway": "MBBank",
  "transactionDate": "2024-01-15T10:35:00",
  "accountNumber": "2730788922276",
  "subAccount": "",
  "content": "TXN-1705123456789-ABC12345 Thanh toan hoc phi thang 10",
  "transferType": "in",
  "transferAmount": 500000,
  "accumulated": 50000000
}
```

**Request Fields:**
| Field | Mô tả | Ví dụ |
|-------|-------|-------|
| `id` | ID giao dịch bên SePay | `12345` |
| `gateway` | Tên ngân hàng | `MBBank`, `VCB`, `Techcombank` |
| `transactionDate` | Ngày giờ giao dịch | `"2024-01-15T10:35:00"` |
| `accountNumber` | Số tài khoản nhận | `"2730788922276"` |
| `content` | **QUAN TRỌNG:** Nội dung chuyển khoản (chứa Transaction ID) | `"TXN-1705123456789-ABC12345 Thanh toan..."` |
| `transferType` | Loại giao dịch | `"in"` (tiền vào) hoặc `"out"` (tiền ra) |
| `transferAmount` | Số tiền | `500000` |
| `accumulated` | Số dư lũy kế | `50000000` |

---

### **Bước 2: SePayWebhookController Nhận Webhook**

**File:** `SePayWebhookController.java` (dòng 44-95)

```java
@PostMapping("/webhook")
public ResponseEntity<String> handleSePayWebhook(
    @RequestHeader(value = "Authorization", required = false) String authHeader,
    @RequestBody SePayWebhookRequest request
) {
    logger.info("📥 Nhận Webhook SePay: {}", request);
    
    // Bước 1: Kiểm tra bảo mật
    // Bước 2: Lọc giao dịch (chỉ xử lý "in")
    // Bước 3: Extract Transaction ID
    // Bước 4: Xác nhận thanh toán
}
```

---

### **Bước 3: Kiểm Tra Bảo Mật**

**File:** `SePayWebhookController.java` (dòng 50-57)

```java
// Kiểm tra API Key
if (sepayApiKey != null && !sepayApiKey.isEmpty()) {
    if (authHeader == null || !authHeader.startsWith("Bearer " + sepayApiKey)) {
        logger.warn("⚠️ Unauthorized webhook request - API key không khớp");
        return ResponseEntity.status(401).body("Unauthorized");
    }
}
```

**Cấu hình:**
```properties
# application.properties
sepay.api.key=your_sepay_api_key_here
```

**Security:**
- ✅ SePay gửi `Authorization: Bearer {api_key}`
- ✅ So sánh với `sepay.api.key` trong config
- ✅ Trả về `401 Unauthorized` nếu không khớp

---

### **Bước 4: Lọc Giao Dịch**

**File:** `SePayWebhookController.java` (dòng 59-63)

```java
// Chỉ xử lý giao dịch "in" (tiền vào)
if (!"in".equalsIgnoreCase(request.getTransferType())) {
    logger.info("⏭️ Bỏ qua giao dịch 'out' (tiền ra)");
    return ResponseEntity.ok("Skipped - not incoming transaction");
}
```

**Logic:**
- ✅ Chỉ xử lý `transferType = "in"` (tiền vào)
- ✅ Bỏ qua `transferType = "out"` (tiền ra)
- ✅ Trả về `200 OK` để SePay không retry

---

### **Bước 5: Extract Transaction ID**

**File:** `SePayWebhookController.java` (dòng 66-74, 106-123)

```java
// Extract Transaction ID từ nội dung chuyển khoản
String transactionId = extractTransactionId(request.getContent());

if (transactionId == null) {
    logger.warn("⚠️ Không tìm thấy Transaction ID trong nội dung: {}", request.getContent());
    return ResponseEntity.ok("No Transaction ID found - acknowledged");
}
```

**Hàm extractTransactionId():**

```java
private static final Pattern TXN_PATTERN = Pattern.compile("(?i)(TXN-[A-Za-z0-9-]+)");

private String extractTransactionId(String content) {
    if (content == null || content.trim().isEmpty()) {
        return null;
    }
    
    // Regex tìm chuỗi bắt đầu bằng TXN (case-insensitive)
    Matcher matcher = TXN_PATTERN.matcher(content);
    
    if (matcher.find()) {
        String transactionId = matcher.group(1).toUpperCase();  // Normalize thành uppercase
        logger.debug("🔍 Extract Transaction ID: {} từ content: {}", transactionId, content);
        return transactionId;
    }
    
    return null;
}
```

**Regex Pattern:**
```
(?i)(TXN-[A-Za-z0-9-]+)
```
- `(?i)`: Case-insensitive (không phân biệt hoa/thường)
- `TXN-`: Bắt đầu bằng "TXN-"
- `[A-Za-z0-9-]+`: Theo sau là chữ, số, dấu gạch ngang

**Ví dụ:**
```
Content: "TXN-1705123456789-ABC12345 Thanh toan hoc phi"
Extract: "TXN-1705123456789-ABC12345"

Content: "txn-1705123456789-abc12345 Thanh toan hoc phi"
Extract: "TXN-1705123456789-ABC12345" (normalize thành uppercase)
```

**Edge Cases:**
- Nếu không tìm thấy Transaction ID → Trả về `200 OK` (chuyển khoản không liên quan)
- SePay không retry vì đã nhận `200 OK`

---

### **Bước 6: Xác Nhận Thanh Toán**

**File:** `SePayWebhookController.java` (dòng 77-94)

```java
try {
    String paymentMethod = "SEPAY_" + (request.getGateway() != null ? request.getGateway() : "BANK");
    paymentService.confirmPaymentSuccess(transactionId, paymentMethod);
    logger.info("✅ Đã xác nhận thanh toán thành công cho Transaction ID: {}", transactionId);
    return ResponseEntity.ok("Success");
} catch (org.springframework.web.server.ResponseStatusException e) {
    if (e.getStatusCode().value() == 404) {
        logger.warn("⚠️ Không tìm thấy Payment với Transaction ID: {}", transactionId);
        return ResponseEntity.ok("Payment not found - acknowledged");
    }
    logger.error("❌ Lỗi xử lý thanh toán: {}", e.getMessage());
    return ResponseEntity.ok("Error but acknowledged");
} catch (Exception e) {
    logger.error("❌ Lỗi xử lý thanh toán: {}", e.getMessage(), e);
    return ResponseEntity.ok("Error but acknowledged");
}
```

**Payment Method Format:**
```
SEPAY_{gateway}
Ví dụ:
- SEPAY_MBBank
- SEPAY_VCB
- SEPAY_Techcombank
- SEPAY_BANK (nếu gateway = null)
```

**Error Handling:**
- ✅ `404 NOT_FOUND`: Payment không tồn tại → Trả về `200 OK` (SePay không retry)
- ✅ `Exception`: Lỗi khác → Trả về `200 OK` (SePay không retry)
- ✅ **LUÔN** trả về `200 OK` để tránh SePay retry spam

---

### **Bước 7: PaymentService.confirmPaymentSuccess()**

**File:** `PaymentService.java` (dòng 157-182)

```java
@Transactional
public void confirmPaymentSuccess(String transactionId, String paymentMethod) {
    // 1. Tìm Payment theo transactionId
    Payment payment = paymentRepository.findByTransactionId(transactionId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, 
            "Không tìm thấy giao dịch với Transaction ID: " + transactionId
        ));
    
    // 2. Kiểm tra idempotency - nếu đã SUCCESS rồi thì không làm gì
    if ("SUCCESS".equals(payment.getStatus())) {
        logger.info("Payment {} đã được xử lý trước đó, bỏ qua", transactionId);
        return;
    }
    
    // 3. Cập nhật trạng thái
    payment.setStatus("SUCCESS");
    payment.setCompletedAt(LocalDateTime.now());
    payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "SEPAY_BANK_TRANSFER");
    payment.setNotes("Thanh toán thành công qua SePay - " + LocalDateTime.now());
    
    payment = paymentRepository.save(payment);
    
    // 4. Cập nhật Fee nếu có
    if (payment.getFeeId() != null) {
        updateFeeAfterPayment(payment);
    }
    
    logger.info("✅ Đã xác nhận thanh toán thành công cho Payment ID: {}, Transaction ID: {}", 
                payment.getId(), transactionId);
}
```

**Xử lý:**
1. ✅ Tìm Payment theo transactionId
2. ✅ **Idempotency Check**: Nếu đã `SUCCESS` thì return (không làm gì)
3. ✅ Cập nhật Payment:
   - `status = "SUCCESS"`
   - `completedAt = now()`
   - `paymentMethod = "SEPAY_{gateway}"`
   - `notes = "Thanh toán thành công qua SePay - {timestamp}"`
4. ✅ Cập nhật Fee (nếu có feeId)

---

### **Bước 8: Cập Nhật Fee Tự Động**

**File:** `PaymentService.java` (dòng 210-232)

```java
private void updateFeeAfterPayment(Payment payment) {
    if (payment.getFeeId() == null) {
        return;  // Không có feeId thì không cập nhật
    }
    
    Fee fee = feeRepository.findById(payment.getFeeId()).orElse(null);
    
    if (fee != null) {
        // Cập nhật số tiền đã thanh toán
        Long currentPaid = fee.getPaid() != null ? fee.getPaid() : 0L;
        fee.setPaid(currentPaid + payment.getAmount());
        
        // Cập nhật trạng thái
        if (fee.getAmount() != null && fee.getPaid() >= fee.getAmount()) {
            fee.setStatus("PAID");      // Đã thanh toán đủ
        } else {
            fee.setStatus("PARTIAL");   // Thanh toán một phần
        }
        
        feeRepository.save(fee);
    }
}
```

**Ví dụ Cập Nhật Fee:**

**Trước khi thanh toán:**
```sql
SELECT * FROM fees WHERE id = 1;
-- id: 1
-- amount: 1000000
-- paid: 0
-- status: 'UNPAID'
```

**Payment:**
```sql
-- Payment amount: 500000
-- Payment feeId: 1
```

**Sau khi thanh toán:**
```sql
UPDATE fees SET 
    paid = 0 + 500000,           -- 500000
    status = 'PARTIAL'            -- Chưa đủ (500000 < 1000000)
WHERE id = 1;
```

**Nếu thanh toán tiếp:**
```sql
-- Payment tiếp theo: 500000
UPDATE fees SET 
    paid = 500000 + 500000,      -- 1000000
    status = 'PAID'               -- Đã đủ (1000000 >= 1000000)
WHERE id = 1;
```

**Fee Status:**
| Status | Điều kiện | Mô tả |
|--------|-----------|-------|
| `UNPAID` | `paid = 0` hoặc `paid IS NULL` | Chưa thanh toán |
| `PARTIAL` | `0 < paid < amount` | Thanh toán một phần |
| `PAID` | `paid >= amount` | Đã thanh toán đủ |

---

## 📊 Cấu Trúc Dữ Liệu

### **Payment Entity**

**File:** `Payment.java`

```java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String transactionId;      // TXN-{timestamp}-{UUID}
    
    @Column(nullable = false)
    private Long amount;               // Số tiền (VND)
    
    @Column(nullable = false)
    private String status;             // PENDING, SUCCESS, FAILED, CANCELLED
    
    @Column(nullable = false)
    private Long feeId;                // ID của fee cần thanh toán (optional)
    
    @Column(nullable = false)
    private String studentName;        // Tên học sinh
    
    @Column
    private String paymentMethod;      // QR_CODE, SEPAY_MBBank, SEPAY_VCB, ...
    
    @Column(columnDefinition = "TEXT")
    private String qrCodeData;         // VietQR data string
    
    @Column
    private LocalDateTime createdAt;   // Thời gian tạo
    
    @Column
    private LocalDateTime completedAt; // Thời gian hoàn thành
    
    @Column(columnDefinition = "TEXT")
    private String notes;              // Ghi chú
}
```

**Database Schema:**
```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    amount BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    fee_id BIGINT NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    payment_method VARCHAR(100),
    qr_code_data TEXT,
    created_at DATETIME,
    completed_at DATETIME,
    notes TEXT,
    FOREIGN KEY (fee_id) REFERENCES fees(id)
);
```

**Indexes:**
```sql
CREATE UNIQUE INDEX idx_transaction_id ON payments(transaction_id);
CREATE INDEX idx_fee_id ON payments(fee_id);
CREATE INDEX idx_status ON payments(status);
```

---

### **Fee Entity**

**File:** `Fee.java`

```java
@Entity
@Table(name = "fees")
public class Fee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String studentName;
    private String className;
    private String month;
    private Long amount;        // Tổng số tiền cần thanh toán
    private Long paid;          // Số tiền đã thanh toán
    private String dueDate;
    private String status;      // UNPAID, PARTIAL, PAID
}
```

**Database Schema:**
```sql
CREATE TABLE fees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_name VARCHAR(255),
    class_name VARCHAR(255),
    month VARCHAR(50),
    amount BIGINT,
    paid BIGINT DEFAULT 0,
    due_date VARCHAR(50),
    status VARCHAR(50) DEFAULT 'UNPAID'
);
```

---

### **Mối Quan Hệ Payment - Fee**

```
Payment (N) ──> (1) Fee

- Một Payment có thể thanh toán cho 1 Fee (feeId)
- Một Fee có thể có nhiều Payment (thanh toán nhiều lần)
- Payment.feeId có thể NULL (thanh toán không liên quan đến fee)
```

**Ví dụ:**
```
Fee ID: 1
- amount: 1,000,000
- paid: 0
- status: UNPAID

Payment 1: amount = 500,000, feeId = 1
→ Fee.paid = 500,000, Fee.status = PARTIAL

Payment 2: amount = 500,000, feeId = 1
→ Fee.paid = 1,000,000, Fee.status = PAID
```

---

## 🔧 Các Thành Phần Chính

### **1. PaymentController**

**File:** `PaymentController.java`

**Endpoints:**
- `POST /api/payments/create` - Tạo thanh toán
- `GET /api/payments/{transactionId}` - Kiểm tra trạng thái
- `POST /api/payments/callback` - Callback từ Payment Gateway
- `POST /api/payments/{transactionId}/confirm` - Xác nhận thủ công (testing)

**Security:**
- ✅ JWT Authentication required
- ✅ Role: `STUDENT` hoặc `ADMIN`
- ✅ Callback endpoint: Public (không cần auth)

---

### **2. SePayWebhookController**

**File:** `SePayWebhookController.java`

**Endpoints:**
- `POST /api/sepay/webhook` - Nhận webhook từ SePay

**Security:**
- ✅ API Key authentication (Bearer token)
- ✅ Public endpoint (không cần JWT)
- ✅ IP whitelist (nên thêm trong production)

**Xử lý:**
1. Kiểm tra bảo mật (API Key)
2. Lọc giao dịch (chỉ "in")
3. Extract Transaction ID từ content
4. Xác nhận thanh toán

---

### **3. PaymentService**

**File:** `PaymentService.java`

**Methods:**

| Method | Mô tả | Sử dụng bởi |
|--------|-------|-------------|
| `createPayment()` | Tạo Payment và QR code | PaymentController.create() |
| `processPaymentCallback()` | Xử lý callback từ Payment Gateway | PaymentController.callback() |
| `confirmPaymentSuccess()` | Xác nhận thanh toán (SePay) | SePayWebhookController |
| `getPaymentByTransactionId()` | Lấy thông tin Payment | PaymentController.getStatus() |
| `updateFeeAfterPayment()` | Cập nhật Fee sau thanh toán | Private method |

---

### **4. VietQRGenerator**

**File:** `VietQRGenerator.java`

**Methods:**

| Method | Mô tả |
|--------|-------|
| `generateCompleteVietQRData()` | Tạo VietQR data hoàn chỉnh |
| `calculateCRC16()` | Tính CRC16 checksum |
| `buildMerchantInfo()` | Tạo Merchant Account Information |
| `buildAdditionalData()` | Tạo Additional Data Field |
| `removeAccents()` | Bỏ dấu tiếng Việt |

**Cấu hình:**
```properties
payment.vietqr.merchant.account.info=970422|2730788922276|NGUYEN HONG DONGDONG
```

---

## ⚠️ Xử Lý Lỗi và Edge Cases

### **1. Payment Không Tìm Thấy**

**Scenario:** SePay webhook gửi Transaction ID không tồn tại

**Xử lý:**
```java
// SePayWebhookController.java
catch (ResponseStatusException e) {
    if (e.getStatusCode().value() == 404) {
        logger.warn("⚠️ Không tìm thấy Payment với Transaction ID: {}", transactionId);
        return ResponseEntity.ok("Payment not found - acknowledged");
    }
}
```

**Kết quả:**
- ✅ Trả về `200 OK` (SePay không retry)
- ✅ Log warning
- ✅ Không throw exception

---

### **2. Payment Đã Được Xử Lý (Idempotency)**

**Scenario:** SePay gửi webhook nhiều lần cho cùng 1 Transaction ID

**Xử lý:**
```java
// PaymentService.confirmPaymentSuccess()
if ("SUCCESS".equals(payment.getStatus())) {
    logger.info("Payment {} đã được xử lý trước đó, bỏ qua", transactionId);
    return;  // Không làm gì, đảm bảo idempotency
}
```

**Kết quả:**
- ✅ Không cập nhật lại Payment
- ✅ Không throw exception
- ✅ Log info để tracking

---

### **3. Không Tìm Thấy Transaction ID trong Content**

**Scenario:** Người dùng chuyển khoản nhưng không nhập đúng Transaction ID trong nội dung

**Xử lý:**
```java
// SePayWebhookController.java
String transactionId = extractTransactionId(request.getContent());

if (transactionId == null) {
    logger.warn("⚠️ Không tìm thấy Transaction ID trong nội dung: {}", request.getContent());
    return ResponseEntity.ok("No Transaction ID found - acknowledged");
}
```

**Kết quả:**
- ✅ Trả về `200 OK` (SePay không retry)
- ✅ Log warning
- ✅ Đây là chuyển khoản không liên quan đến hệ thống

---

### **4. Fee Không Tồn Tại**

**Scenario:** Payment có feeId nhưng Fee không tồn tại trong database

**Xử lý:**
```java
// PaymentService.updateFeeAfterPayment()
Fee fee = feeRepository.findById(payment.getFeeId()).orElse(null);

if (fee != null) {
    // Cập nhật Fee
    fee.setPaid(...);
    fee.setStatus(...);
    feeRepository.save(fee);
}
// Nếu fee == null thì không làm gì, không throw exception
```

**Kết quả:**
- ✅ Không throw exception
- ✅ Payment vẫn được cập nhật thành công
- ✅ Chỉ Fee không được cập nhật

---

### **5. Nội Dung Chuyển Khoản Có Nhiều Transaction ID**

**Scenario:** Regex tìm thấy nhiều Transaction ID trong content

**Xử lý:**
```java
// Regex chỉ tìm Transaction ID ĐẦU TIÊN
Matcher matcher = TXN_PATTERN.matcher(content);
if (matcher.find()) {
    String transactionId = matcher.group(1).toUpperCase();
    return transactionId;  // Chỉ lấy match đầu tiên
}
```

**Kết quả:**
- ✅ Lấy Transaction ID đầu tiên
- ✅ Đảm bảo tính nhất quán

---

### **6. Transaction ID Không Đúng Format**

**Scenario:** Nội dung có "TXN-" nhưng format sai

**Xử lý:**
```java
// Regex pattern: (?i)(TXN-[A-Za-z0-9-]+)
// Chỉ match Transaction ID đúng format
// Ví dụ: "TXN-123-ABC", "TXN-abc-123"
// Không match: "TXN-", "TXN- ", "TXN-abc@123"
```

**Kết quả:**
- ✅ Chỉ extract Transaction ID đúng format
- ✅ Bỏ qua Transaction ID sai format

---

## 🔐 Bảo Mật

### **1. JWT Authentication**

**Endpoints yêu cầu JWT:**
- `POST /api/payments/create`
- `GET /api/payments/{transactionId}`
- `POST /api/payments/{transactionId}/confirm`

**Xử lý:**
```java
@PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
```

---

### **2. SePay API Key**

**Cấu hình:**
```properties
sepay.api.key=your_secret_api_key_here
```

**Xử lý:**
```java
if (sepayApiKey != null && !sepayApiKey.isEmpty()) {
    if (authHeader == null || !authHeader.startsWith("Bearer " + sepayApiKey)) {
        return ResponseEntity.status(401).body("Unauthorized");
    }
}
```

---

### **3. Idempotency**

**Đảm bảo:**
- ✅ Kiểm tra status trước khi cập nhật
- ✅ Không xử lý callback/webhook nhiều lần
- ✅ Tránh duplicate payment

---

### **4. Input Validation**

**Validation Rules:**
- ✅ `amount > 0`
- ✅ `feeId` tồn tại (nếu có)
- ✅ `transactionId` format đúng
- ✅ `status` trong danh sách hợp lệ

---

## 📝 Tóm Tắt Luồng Hoạt Động

### **Luồng QR Code (Callback)**

```
1. Client → POST /api/payments/create
2. Server → Tạo Payment (PENDING) + QR code
3. Client → Hiển thị QR code
4. User → Quét QR và thanh toán
5. Payment Gateway → POST /api/payments/callback
6. Server → Cập nhật Payment (SUCCESS) + Fee
```

### **Luồng SePay Webhook (Tự Động)**

```
1. Client → POST /api/payments/create
2. Server → Tạo Payment (PENDING) + QR code
3. Client → Hiển thị QR code
4. User → Quét QR và chuyển khoản
5. SePay → Phát hiện tiền vào → POST /api/sepay/webhook
6. Server → Extract Transaction ID từ content
7. Server → Cập nhật Payment (SUCCESS) + Fee
```

**Điểm khác biệt chính:**
- ✅ SePay tự động phát hiện, không cần Payment Gateway callback
- ✅ Extract Transaction ID từ nội dung chuyển khoản
- ✅ Tự động 100%, không phụ thuộc bên thứ 3

---

## 🎯 Best Practices

1. **Luôn trả về 200 OK cho SePay webhook** để tránh retry spam
2. **Kiểm tra idempotency** trước khi cập nhật Payment
3. **Log đầy đủ** để tracking và debug
4. **Validate input** ở mọi layer (Controller, Service)
5. **Sử dụng Transaction** để đảm bảo consistency
6. **Normalize Transaction ID** thành uppercase để nhất quán

---

**Chúc bạn sử dụng thành công! 🎉**

