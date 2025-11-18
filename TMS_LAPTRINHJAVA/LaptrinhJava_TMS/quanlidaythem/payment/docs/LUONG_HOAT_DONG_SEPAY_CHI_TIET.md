# Luồng Hoạt Động Chi Tiết Hệ Thống Thanh Toán SePay

## 📋 Mục Lục

1. [Tổng Quan Hệ Thống](#tổng-quan-hệ-thống)
2. [Kiến Trúc Hệ Thống](#kiến-trúc-hệ-thống)
3. [Cấu Hình](#cấu-hình)
4. [Luồng Hoạt Động Chi Tiết](#luồng-hoạt-động-chi-tiết)
5. [Cấu Trúc Code](#cấu-trúc-code)
6. [Cấu Trúc Database](#cấu-trúc-database)
7. [Xử Lý Lỗi](#xử-lý-lỗi)
8. [Ví Dụ Thực Tế](#ví-dụ-thực-tế)

---

## 🎯 Tổng Quan Hệ Thống

**Hệ thống thanh toán tự động với SePay:**

1. ✅ Admin tạo Payment thủ công trong MySQL
2. ✅ Thông báo cho người dùng thông tin thanh toán (Transaction ID)
3. ✅ Người dùng chuyển khoản với nội dung chứa Transaction ID
4. ✅ SePay tự động phát hiện tiền vào và gọi webhook
5. ✅ Hệ thống tự động cập nhật Payment + Fee

**Đặc điểm:**
- ✅ **Tự động 100%** - Không cần can thiệp thủ công
- ✅ **Không cần QR code** - Chỉ cần Transaction ID trong nội dung chuyển khoản
- ✅ **Hoạt động với mọi ngân hàng** - SePay kết nối với tất cả ngân hàng
- ✅ **Cập nhật Fee tự động** - Tự động cập nhật trạng thái học phí

---

## 🏗️ Kiến Trúc Hệ Thống

```
┌─────────────────────────────────────────────────────────────┐
│                      ADMIN/MYSQL                             │
│  Tạo Payment thủ công trong MySQL                           │
└────────────────┬────────────────────────────────────────────┘
                 │
                 │ INSERT INTO payments (transaction_id, ...)
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATABASE                                │
│  ┌──────────────┐         ┌──────────────┐                 │
│  │ payments     │ 1──N    │ fees         │                 │
│  │ - id         │         │ - id         │                 │
│  │ - transaction_id│      │ - amount     │                 │
│  │ - status: PENDING│     │ - paid       │                 │
│  │ - fee_id     │         │ - status     │                 │
│  └──────────────┘         └──────────────┘                 │
└─────────────────────────────────────────────────────────────┘
                 │
                 │ User chuyển khoản với nội dung: 
                 │ "TXN-{id} {ghi chú}"
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                    NGAN HANG / SE PAY                        │
│  SePay phát hiện tiền vào tài khoản                         │
│  Extract Transaction ID từ nội dung chuyển khoản            │
└────────────────┬────────────────────────────────────────────┘
                 │
                 │ POST /api/sepay/webhook
                 │ {
                 │   "content": "TXN-1705123456789-ABC12345...",
                 │   "transferType": "in",
                 │   "transferAmount": 500000,
                 │   ...
                 │ }
                 ▼
┌─────────────────────────────────────────────────────────────┐
│              SePayWebhookController                          │
│  POST /api/sepay/webhook                                     │
│  - Kiểm tra API Key                                          │
│  - Lọc giao dịch "in"                                        │
│  - Extract Transaction ID bằng Regex                         │
│  - Gọi PaymentService.confirmPaymentSuccess()               │
└────────────────┬────────────────────────────────────────────┘
                 │
                 │ paymentService.confirmPaymentSuccess()
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                  PaymentService                              │
│  confirmPaymentSuccess()                                     │
│  - Tìm Payment theo transactionId                            │
│  - Kiểm tra idempotency (nếu đã SUCCESS thì bỏ qua)        │
│  - Cập nhật Payment:                                         │
│    * status = "SUCCESS"                                      │
│    * completedAt = NOW()                                     │
│    * paymentMethod = "SEPAY_{gateway}"                      │
│  - Gọi updateFeeAfterPayment()                              │
└────────────────┬────────────────────────────────────────────┘
                 │
                 │ updateFeeAfterPayment()
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                  PaymentService                              │
│  updateFeeAfterPayment()                                     │
│  - Tìm Fee theo feeId                                        │
│  - Cập nhật Fee:                                             │
│    * paid = paid + payment.amount                            │
│    * status = "PAID" (nếu paid >= amount)                    │
│    * status = "PARTIAL" (nếu paid < amount)                  │
└────────────────┬────────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATABASE                                │
│  payments: status = "SUCCESS"                                │
│  fees: paid updated, status updated                          │
└─────────────────────────────────────────────────────────────┘
```

---

## ⚙️ Cấu Hình

### **application.properties**

```properties
# Cấu hình SePay Webhook
sepay.api.key=your_sepay_api_key_here
sepay.webhook.enabled=true
```

**Cấu hình trong SePay Dashboard:**
- **Webhook URL:** `https://your-domain.com/api/sepay/webhook`
- **API Key:** `your_sepay_api_key_here` (giống với `sepay.api.key`)

**Security:**
- ✅ SePay sẽ gửi header `Authorization: Bearer {api_key}`
- ✅ Hệ thống sẽ kiểm tra API key trước khi xử lý webhook

---

## 🔄 Luồng Hoạt Động Chi Tiết

### **BƯỚC 1: Tạo Payment Trong MySQL**

**Admin tạo Payment thủ công:**

```sql
USE tms;

INSERT INTO payments (
    transaction_id,
    amount,
    status,
    fee_id,
    student_name,
    payment_method,
    notes,
    created_at
) VALUES (
    'TXN-1705123456789-ABC12345',  -- Transaction ID (unique)
    500000,                         -- Số tiền (VND)
    'PENDING',                      -- Trạng thái ban đầu
    1,                              -- ID của Fee
    'Nguyễn Văn A',                -- Tên học sinh
    'SEPAY_BANK_TRANSFER',          -- Phương thức thanh toán
    'Thanh toán học phí tháng 11/2024',  -- Ghi chú
    NOW()                           -- Thời gian tạo
);
```

**Payment Entity:**

```java
// Payment.java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String transactionId;  // TXN-1705123456789-ABC12345
    
    @Column(nullable = false)
    private Long amount;  // 500000
    
    @Column(nullable = false)
    private String status;  // PENDING
    
    @Column(nullable = false)
    private Long feeId;  // 1
    
    @Column(nullable = false)
    private String studentName;  // "Nguyễn Văn A"
    
    @Column
    private String paymentMethod;  // "SEPAY_BANK_TRANSFER"
    
    @Column(columnDefinition = "TEXT")
    private String qrCodeData;  // NULL (không dùng)
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
}
```

**Kết quả trong Database:**

```sql
SELECT * FROM payments WHERE transaction_id = 'TXN-1705123456789-ABC12345';
```

```
+----+------------------------------+--------+---------+--------+---------------+---------------------+-------------+---------------------+-----------+------------------------------------------+
| id | transaction_id               | amount | status  | fee_id | student_name  | payment_method      | qr_code_data| created_at          | completed_at | notes                                   |
+----+------------------------------+--------+---------+--------+---------------+---------------------+-------------+---------------------+-----------+------------------------------------------+
|  1 | TXN-1705123456789-ABC12345   | 500000 | PENDING |      1 | Nguyễn Văn A  | SEPAY_BANK_TRANSFER | NULL        | 2024-01-15 10:30:00 | NULL      | Thanh toán học phí tháng 11/2024        |
+----+------------------------------+--------+---------+--------+---------------+---------------------+-------------+---------------------+-----------+------------------------------------------+
```

**Lưu ý:**
- ✅ `transaction_id` phải **unique** (không trùng lặp)
- ✅ `status` mặc định = `"PENDING"`
- ✅ `fee_id` phải tồn tại trong bảng `fees` (nếu có)
- ✅ `qr_code_data` = NULL (không dùng QR code)

---

### **BƯỚC 2: Thông Báo Cho Người Dùng**

**Admin gửi thông tin thanh toán cho học sinh/phụ huynh:**

**Thông tin thanh toán:**
- **Số tiền:** 500,000 VND
- **Số tài khoản:** 2730788922276 (MB Bank)
- **Chủ tài khoản:** NGUYEN HONG DONGDONG
- **Nội dung chuyển khoản (QUAN TRỌNG):** `TXN-1705123456789-ABC12345 Thanh toan hoc phi thang 11/2024`

**Lưu ý quan trọng:**
- ✅ **BẮT BUỘC:** Nội dung chuyển khoản phải chứa **Transaction ID**
- ✅ Format: `TXN-{id} {ghi chú}`
- ✅ Transaction ID phải khớp với Transaction ID trong database

---

### **BƯỚC 3: Người Dùng Chuyển Khoản**

**Người dùng chuyển khoản qua app ngân hàng:**

1. Mở app ngân hàng (MBBank, VCB, Techcombank, ...)
2. Chọn "Chuyển khoản"
3. Nhập số tài khoản: `2730788922276`
4. Nhập số tiền: `500000`
5. **Nhập nội dung:** `TXN-1705123456789-ABC12345 Thanh toan hoc phi thang 11/2024`
6. Xác nhận chuyển khoản

**Kết quả:**
- ✅ Tiền được chuyển vào tài khoản
- ✅ Nội dung chuyển khoản chứa Transaction ID: `TXN-1705123456789-ABC12345`

---

### **BƯỚC 4: SePay Phát Hiện Và Gửi Webhook**

**Khi có tiền vào tài khoản, SePay sẽ:**

1. Phát hiện giao dịch chuyển khoản vào
2. Đọc nội dung chuyển khoản
3. Gửi webhook đến server:

```http
POST /api/sepay/webhook
Authorization: Bearer your_sepay_api_key_here
Content-Type: application/json

{
  "id": 12345,
  "gateway": "MBBank",
  "transactionDate": "2024-01-15T10:35:00",
  "accountNumber": "2730788922276",
  "subAccount": "",
  "content": "TXN-1705123456789-ABC12345 Thanh toan hoc phi thang 11/2024",
  "transferType": "in",
  "transferAmount": 500000,
  "accumulated": 50000000
}
```

**SePayWebhookRequest DTO:**

```java
// SePayWebhookRequest.java
public class SePayWebhookRequest {
    @JsonProperty("id")
    private Long id;  // 12345
    
    @JsonProperty("gateway")
    private String gateway;  // "MBBank"
    
    @JsonProperty("transactionDate")
    private String transactionDate;  // "2024-01-15T10:35:00"
    
    @JsonProperty("accountNumber")
    private String accountNumber;  // "2730788922276"
    
    @JsonProperty("subAccount")
    private String subAccount;  // ""
    
    @JsonProperty("content")
    private String content;  // "TXN-1705123456789-ABC12345 Thanh toan hoc phi thang 11/2024"
    
    @JsonProperty("transferType")
    private String transferType;  // "in" hoặc "out"
    
    @JsonProperty("transferAmount")
    private Long transferAmount;  // 500000
    
    @JsonProperty("accumulated")
    private Long accumulated;  // 50000000
}
```

---

### **BƯỚC 5: SePayWebhookController Nhận Webhook**

**File:** `SePayWebhookController.java`

```java
@RestController
@RequestMapping("/api/sepay")
public class SePayWebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(SePayWebhookController.class);
    private final PaymentService paymentService;
    
    // Regex pattern để tìm Transaction ID trong nội dung chuyển khoản
    // Format: TXN-{timestamp}-{UUID} hoặc TXN-{any alphanumeric and dash}
    // Case-insensitive để bắt cả "TXN" và "txn"
    private static final Pattern TXN_PATTERN = Pattern.compile("(?i)(TXN-[A-Za-z0-9-]+)");
    
    @Value("${sepay.api.key:}")
    private String sepayApiKey;
    
    @PostMapping("/webhook")
    public ResponseEntity<String> handleSePayWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SePayWebhookRequest request
    ) {
        logger.info("📥 Nhận Webhook SePay: {}", request);
        
        // Bước 1: Kiểm tra bảo mật
        // Bước 2: Lọc giao dịch
        // Bước 3: Extract Transaction ID
        // Bước 4: Xác nhận thanh toán
    }
}
```

**Xử lý chi tiết:**

#### **5.1. Kiểm Tra Bảo Mật (API Key)**

```java
// SePayWebhookController.java - Dòng 50-57
// 1. Kiểm tra bảo mật (Nếu bạn có cấu hình API Key trong SePay)
// Format header của SePay: "Bearer YOUR_API_KEY"
if (sepayApiKey != null && !sepayApiKey.isEmpty()) {
    if (authHeader == null || !authHeader.startsWith("Bearer " + sepayApiKey)) {
        logger.warn("⚠️ Unauthorized webhook request - API key không khớp");
        return ResponseEntity.status(401).body("Unauthorized");
    }
}
```

**Logic:**
- ✅ Kiểm tra `sepay.api.key` trong `application.properties` có giá trị không
- ✅ So sánh với `Authorization` header từ SePay
- ✅ Format header: `"Bearer {api_key}"`
- ✅ Nếu không khớp → Trả về `401 Unauthorized`

**Ví dụ:**
```properties
# application.properties
sepay.api.key=my_secret_key_123
```

```http
# SePay gửi
Authorization: Bearer my_secret_key_123
```

**Kết quả:** ✅ Khớp → Tiếp tục xử lý

---

#### **5.2. Lọc Giao Dịch (Chỉ Xử Lý Tiền Vào)**

```java
// SePayWebhookController.java - Dòng 59-63
// 2. Chỉ xử lý giao dịch "in" (tiền vào)
if (!"in".equalsIgnoreCase(request.getTransferType())) {
    logger.info("⏭️ Bỏ qua giao dịch 'out' (tiền ra)");
    return ResponseEntity.ok("Skipped - not incoming transaction");
}
```

**Logic:**
- ✅ Chỉ xử lý `transferType = "in"` (tiền vào)
- ✅ Bỏ qua `transferType = "out"` (tiền ra)
- ✅ Trả về `200 OK` để SePay không retry

**Ví dụ:**
```json
// Tiền vào - Xử lý
{
  "transferType": "in",
  "transferAmount": 500000
}
```

```json
// Tiền ra - Bỏ qua
{
  "transferType": "out",
  "transferAmount": 300000
}
```

---

#### **5.3. Extract Transaction ID Từ Nội Dung**

```java
// SePayWebhookController.java - Dòng 65-74
// 3. Bóc tách Transaction ID từ nội dung chuyển khoản (content)
String transactionId = extractTransactionId(request.getContent());

if (transactionId == null) {
    logger.warn("⚠️ Không tìm thấy Transaction ID trong nội dung: {}", request.getContent());
    // Vẫn trả về 200 để SePay không gửi lại (vì đây có thể là chuyển khoản không liên quan)
    return ResponseEntity.ok("No Transaction ID found - acknowledged");
}

logger.info("🔍 Tìm thấy Transaction ID: {}", transactionId);
```

**Hàm extractTransactionId():**

```java
// SePayWebhookController.java - Dòng 106-123
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
```

**Regex Pattern:**

```java
private static final Pattern TXN_PATTERN = Pattern.compile("(?i)(TXN-[A-Za-z0-9-]+)");
```

**Giải thích Regex:**
- `(?i)`: Case-insensitive (không phân biệt hoa/thường)
- `TXN-`: Bắt đầu bằng "TXN-"
- `[A-Za-z0-9-]+`: Theo sau là chữ, số, dấu gạch ngang (1 hoặc nhiều ký tự)
- `()`: Capture group để lấy toàn bộ Transaction ID

**Ví dụ Extract:**

| Content | Extract Result |
|---------|----------------|
| `"TXN-1705123456789-ABC12345 Thanh toan hoc phi"` | `"TXN-1705123456789-ABC12345"` |
| `"txn-1705123456789-abc12345 Thanh toan"` | `"TXN-1705123456789-ABC12345"` (uppercase) |
| `"Nguyen Van A TXN-1705123456789-ABC12345 chuyen khoan"` | `"TXN-1705123456789-ABC12345"` |
| `"Thanh toan hoc phi"` | `null` (không tìm thấy) |
| `"TXN-123"` | `"TXN-123"` |

**Edge Cases:**
- Nếu không tìm thấy Transaction ID → Trả về `200 OK` (chuyển khoản không liên quan)
- SePay không retry vì đã nhận `200 OK`

---

#### **5.4. Xác Nhận Thanh Toán**

```java
// SePayWebhookController.java - Dòng 76-94
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
```

**Payment Method Format:**

```java
String paymentMethod = "SEPAY_" + (request.getGateway() != null ? request.getGateway() : "BANK");
```

**Ví dụ:**
- `gateway = "MBBank"` → `paymentMethod = "SEPAY_MBBank"`
- `gateway = "VCB"` → `paymentMethod = "SEPAY_VCB"`
- `gateway = null` → `paymentMethod = "SEPAY_BANK"`

**Error Handling:**
- ✅ `404 NOT_FOUND`: Payment không tồn tại → Trả về `200 OK` (SePay không retry)
- ✅ `Exception`: Lỗi khác → Trả về `200 OK` (SePay không retry)
- ✅ **LUÔN** trả về `200 OK` để tránh SePay retry spam

---

### **BƯỚC 6: PaymentService.confirmPaymentSuccess()**

**File:** `PaymentService.java` - Dòng 79-104

```java
/**
 * Xác nhận thanh toán thành công (dùng cho SePay webhook)
 * Tự động tìm transactionId và cập nhật status = SUCCESS
 */
@Transactional
public void confirmPaymentSuccess(String transactionId, String paymentMethod) {
    // Tìm Payment theo transactionId
    Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                "Không tìm thấy giao dịch với Transaction ID: " + transactionId));

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

    logger.info("✅ Đã xác nhận thanh toán thành công cho Payment ID: {}, Transaction ID: {}", 
                payment.getId(), transactionId);
}
```

**Xử lý chi tiết:**

#### **6.1. Tìm Payment Theo Transaction ID**

```java
Payment payment = paymentRepository.findByTransactionId(transactionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
            "Không tìm thấy giao dịch với Transaction ID: " + transactionId));
```

**Database Query:**

```sql
SELECT * FROM payments WHERE transaction_id = 'TXN-1705123456789-ABC12345';
```

**Nếu không tìm thấy:**
- ✅ Throw `ResponseStatusException(HttpStatus.NOT_FOUND, ...)`
- ✅ SePayWebhookController catch và trả về `200 OK`

---

#### **6.2. Kiểm Tra Idempotency**

```java
// Kiểm tra idempotency - nếu đã SUCCESS rồi thì không làm gì
if ("SUCCESS".equals(payment.getStatus())) {
    logger.info("Payment {} đã được xử lý trước đó, bỏ qua", transactionId);
    return;  // Không làm gì, đảm bảo idempotency
}
```

**Logic:**
- ✅ Kiểm tra `payment.status == "SUCCESS"`
- ✅ Nếu đã SUCCESS → Return (không làm gì)
- ✅ Tránh xử lý webhook nhiều lần (SePay có thể gửi lại)

**Ví dụ:**
```sql
-- Payment đã SUCCESS
SELECT * FROM payments WHERE transaction_id = 'TXN-1705123456789-ABC12345';
-- status = "SUCCESS"
```

**Kết quả:** ✅ Return ngay, không cập nhật lại

---

#### **6.3. Cập Nhật Payment**

```java
// Cập nhật trạng thái
payment.setStatus("SUCCESS");
payment.setCompletedAt(LocalDateTime.now());
payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "SEPAY_BANK_TRANSFER");
payment.setNotes("Thanh toán thành công qua SePay - " + LocalDateTime.now());

payment = paymentRepository.save(payment);
```

**Database Update:**

```sql
UPDATE payments SET
    status = 'SUCCESS',
    completed_at = '2024-01-15 10:35:00',
    payment_method = 'SEPAY_MBBank',
    notes = 'Thanh toán thành công qua SePay - 2024-01-15T10:35:00'
WHERE transaction_id = 'TXN-1705123456789-ABC12345';
```

**Kết quả:**

| Field | Trước | Sau |
|-------|-------|-----|
| `status` | `"PENDING"` | `"SUCCESS"` |
| `completed_at` | `NULL` | `2024-01-15 10:35:00` |
| `payment_method` | `"SEPAY_BANK_TRANSFER"` | `"SEPAY_MBBank"` |
| `notes` | `"Thanh toán học phí tháng 11/2024"` | `"Thanh toán thành công qua SePay - 2024-01-15T10:35:00"` |

---

#### **6.4. Cập Nhật Fee (Nếu Có)**

```java
// Cập nhật Fee nếu có
if (payment.getFeeId() != null) {
    updateFeeAfterPayment(payment);
}
```

---

### **BƯỚC 7: PaymentService.updateFeeAfterPayment()**

**File:** `PaymentService.java` - Dòng 132-154

```java
/**
 * Cập nhật Fee sau khi thanh toán thành công
 */
private void updateFeeAfterPayment(Payment payment) {
    if (payment.getFeeId() == null) {
        return;  // Không có feeId thì không cập nhật
    }

    Fee fee = feeRepository.findById(payment.getFeeId())
            .orElse(null);

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

**Xử lý chi tiết:**

#### **7.1. Kiểm Tra FeeId**

```java
if (payment.getFeeId() == null) {
    return;  // Không có feeId thì không cập nhật
}
```

**Logic:**
- ✅ Nếu `feeId == null` → Return (không cập nhật Fee)
- ✅ Payment có thể không liên quan đến Fee

---

#### **7.2. Tìm Fee**

```java
Fee fee = feeRepository.findById(payment.getFeeId())
        .orElse(null);

if (fee != null) {
    // Cập nhật Fee
}
```

**Database Query:**

```sql
SELECT * FROM fees WHERE id = 1;
```

**Nếu không tìm thấy:**
- ✅ `fee = null` → Không cập nhật
- ✅ Payment vẫn được cập nhật thành công

---

#### **7.3. Cập Nhật Số Tiền Đã Thanh Toán**

```java
// Cập nhật số tiền đã thanh toán
Long currentPaid = fee.getPaid() != null ? fee.getPaid() : 0L;
fee.setPaid(currentPaid + payment.getAmount());
```

**Ví dụ:**

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

**Sau khi cập nhật:**
```java
currentPaid = 0L;
fee.setPaid(0 + 500000);  // paid = 500000
```

**Database Update:**

```sql
UPDATE fees SET
    paid = 500000,
    status = 'PARTIAL'  -- Vì 500000 < 1000000
WHERE id = 1;
```

---

#### **7.4. Cập Nhật Trạng Thái Fee**

```java
// Cập nhật trạng thái
if (fee.getAmount() != null && fee.getPaid() >= fee.getAmount()) {
    fee.setStatus("PAID");      // Đã thanh toán đủ
} else {
    fee.setStatus("PARTIAL");   // Thanh toán một phần
}
```

**Logic:**

| Condition | Status | Mô tả |
|-----------|--------|-------|
| `paid >= amount` | `"PAID"` | Đã thanh toán đủ |
| `paid < amount` | `"PARTIAL"` | Thanh toán một phần |
| `paid = 0` hoặc `paid IS NULL` | `"UNPAID"` | Chưa thanh toán (chỉ khi tạo Fee mới) |

**Ví dụ:**

**Scenario 1: Thanh Toán Đủ**
```sql
-- Fee ban đầu
amount: 500000
paid: 0
status: UNPAID

-- Payment
amount: 500000
feeId: 1

-- Sau khi thanh toán
paid: 0 + 500000 = 500000
status: PAID (vì 500000 >= 500000)
```

**Scenario 2: Thanh Toán Một Phần**
```sql
-- Fee ban đầu
amount: 1000000
paid: 0
status: UNPAID

-- Payment
amount: 500000
feeId: 1

-- Sau khi thanh toán
paid: 0 + 500000 = 500000
status: PARTIAL (vì 500000 < 1000000)
```

**Scenario 3: Thanh Toán Nhiều Lần**
```sql
-- Fee ban đầu
amount: 1200000
paid: 0
status: UNPAID

-- Payment 1: 500000
paid: 0 + 500000 = 500000
status: PARTIAL (vì 500000 < 1200000)

-- Payment 2: 700000
paid: 500000 + 700000 = 1200000
status: PAID (vì 1200000 >= 1200000)
```

---

### **BƯỚC 8: Kiểm Tra Kết Quả**

**Kiểm tra Payment:**

```sql
SELECT * FROM payments WHERE transaction_id = 'TXN-1705123456789-ABC12345';
```

**Kết quả:**
```
+----+------------------------------+--------+----------+--------+---------------+-----------------+-------------+---------------------+---------------------+------------------------------------------+
| id | transaction_id               | amount | status   | fee_id | student_name  | payment_method  | qr_code_data| created_at          | completed_at        | notes                                   |
+----+------------------------------+--------+----------+--------+---------------+-----------------+-------------+---------------------+---------------------+------------------------------------------+
|  1 | TXN-1705123456789-ABC12345   | 500000 | SUCCESS  |      1 | Nguyễn Văn A  | SEPAY_MBBank    | NULL        | 2024-01-15 10:30:00 | 2024-01-15 10:35:00 | Thanh toán thành công qua SePay - ...    |
+----+------------------------------+--------+----------+--------+---------------+-----------------+-------------+---------------------+---------------------+------------------------------------------+
```

**Kiểm tra Fee:**

```sql
SELECT * FROM fees WHERE id = 1;
```

**Kết quả:**
```
+----+---------------+------------------+-----------------+---------+--------+------------+----------+
| id | student_name  | class_name       | month           | amount  | paid   | due_date   | status   |
+----+---------------+------------------+-----------------+---------+--------+------------+----------+
|  1 | Nguyễn Văn A  | Toán nâng cao 9  | Tháng 11/2024   | 1000000 | 500000 | 2024-11-30 | PARTIAL  |
+----+---------------+------------------+-----------------+---------+--------+------------+----------+
```

**Giải thích:**
- ✅ Payment đã được cập nhật: `status = "SUCCESS"`
- ✅ Fee đã được cập nhật: `paid = 500000`, `status = "PARTIAL"` (vì `500000 < 1000000`)

---

## 💻 Cấu Trúc Code

### **1. SePayWebhookController**

**File:** `SePayWebhookController.java`

**Chức năng:**
- ✅ Nhận webhook từ SePay
- ✅ Kiểm tra bảo mật (API Key)
- ✅ Lọc giao dịch (chỉ xử lý "in")
- ✅ Extract Transaction ID từ content
- ✅ Gọi PaymentService.confirmPaymentSuccess()

**Endpoint:**
```java
POST /api/sepay/webhook
```

**Dependencies:**
```java
@Value("${sepay.api.key:}")
private String sepayApiKey;

private final PaymentService paymentService;
```

---

### **2. PaymentService**

**File:** `PaymentService.java`

**Methods:**

| Method | Mô tả | Sử dụng bởi |
|--------|-------|-------------|
| `confirmPaymentSuccess()` | Xác nhận thanh toán thành công (SePay) | SePayWebhookController |
| `processPaymentCallback()` | Xử lý callback (testing) | PaymentController |
| `getPaymentByTransactionId()` | Lấy thông tin Payment | PaymentController |
| `updateFeeAfterPayment()` | Cập nhật Fee (private) | confirmPaymentSuccess() |

**Dependencies:**
```java
private final PaymentRepository paymentRepository;
private final FeeRepository feeRepository;
```

---

### **3. PaymentController**

**File:** `PaymentController.java`

**Endpoints:**

| Endpoint | Method | Mô tả | Auth |
|----------|--------|-------|------|
| `/api/payments/callback` | POST | Callback testing | No |
| `/api/payments/{transactionId}` | GET | Kiểm tra trạng thái | Yes (STUDENT/ADMIN) |
| `/api/payments/{transactionId}/confirm` | POST | Xác nhận testing | Yes (STUDENT/ADMIN) |

**Lưu ý:**
- ✅ Các endpoint này dùng cho **testing**, không phải flow chính
- ✅ Flow chính: SePay webhook → `confirmPaymentSuccess()`

---

### **4. Payment Entity**

**File:** `Payment.java`

**Fields:**

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | `Long` | Primary key |
| `transactionId` | `String` | Transaction ID (unique) |
| `amount` | `Long` | Số tiền (VND) |
| `status` | `String` | PENDING, SUCCESS, FAILED, CANCELLED |
| `feeId` | `Long` | ID của Fee (optional) |
| `studentName` | `String` | Tên học sinh |
| `paymentMethod` | `String` | SEPAY_MBBank, SEPAY_VCB, etc. |
| `qrCodeData` | `String` | NULL (không dùng) |
| `createdAt` | `LocalDateTime` | Thời gian tạo |
| `completedAt` | `LocalDateTime` | Thời gian hoàn thành |
| `notes` | `String` | Ghi chú |

---

### **5. SePayWebhookRequest DTO**

**File:** `SePayWebhookRequest.java`

**Fields:**

| Field | Type | Mô tả |
|-------|------|-------|
| `id` | `Long` | ID giao dịch bên SePay |
| `gateway` | `String` | Tên ngân hàng (MBBank, VCB, etc.) |
| `transactionDate` | `String` | Ngày giờ giao dịch |
| `accountNumber` | `String` | Số tài khoản nhận |
| `subAccount` | `String` | Sub account |
| `content` | `String` | **QUAN TRỌNG:** Nội dung chuyển khoản (chứa Transaction ID) |
| `transferType` | `String` | "in" hoặc "out" |
| `transferAmount` | `Long` | Số tiền |
| `accumulated` | `Long` | Số dư lũy kế |

---

## 🗄️ Cấu Trúc Database

### **Bảng payments**

```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    amount BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    fee_id BIGINT NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    payment_method VARCHAR(100),
    qr_code_data TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,
    notes TEXT,
    FOREIGN KEY (fee_id) REFERENCES fees(id)
);

CREATE UNIQUE INDEX idx_transaction_id ON payments(transaction_id);
CREATE INDEX idx_fee_id ON payments(fee_id);
CREATE INDEX idx_status ON payments(status);
```

**Fields:**
- ✅ `transaction_id`: UNIQUE (không trùng lặp)
- ✅ `status`: DEFAULT 'PENDING'
- ✅ `fee_id`: FOREIGN KEY đến `fees.id`

---

### **Bảng fees**

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

**Fields:**
- ✅ `paid`: DEFAULT 0
- ✅ `status`: DEFAULT 'UNPAID'

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

## ⚠️ Xử Lý Lỗi

### **1. Payment Không Tìm Thấy**

**Scenario:** SePay webhook gửi Transaction ID không tồn tại

**Code:**
```java
// SePayWebhookController.java - Dòng 82-87
catch (org.springframework.web.server.ResponseStatusException e) {
    if (e.getStatusCode().value() == 404) {
        logger.warn("⚠️ Không tìm thấy Payment với Transaction ID: {}", transactionId);
        return ResponseEntity.ok("Payment not found - acknowledged");
    }
}
```

**Xử lý:**
- ✅ PaymentService throw `ResponseStatusException(HttpStatus.NOT_FOUND)`
- ✅ SePayWebhookController catch và trả về `200 OK`
- ✅ Log warning
- ✅ SePay không retry (vì nhận `200 OK`)

**Nguyên nhân:**
- ❌ Transaction ID trong nội dung chuyển khoản không khớp với database
- ❌ Payment chưa được tạo trong database

**Giải pháp:**
```sql
-- Kiểm tra Payment có tồn tại không
SELECT * FROM payments WHERE transaction_id = 'TXN-...';

-- Nếu không có, tạo Payment mới
INSERT INTO payments (...);
```

---

### **2. Payment Đã Được Xử Lý (Idempotency)**

**Scenario:** SePay gửi webhook nhiều lần cho cùng 1 Transaction ID

**Code:**
```java
// PaymentService.java - Dòng 84-88
if ("SUCCESS".equals(payment.getStatus())) {
    logger.info("Payment {} đã được xử lý trước đó, bỏ qua", transactionId);
    return;  // Không làm gì, đảm bảo idempotency
}
```

**Xử lý:**
- ✅ Kiểm tra `status == "SUCCESS"` trước khi cập nhật
- ✅ Nếu đã SUCCESS → Return (không làm gì)
- ✅ Log info để tracking
- ✅ Đảm bảo idempotency (không xử lý lại)

**Ví dụ:**
```sql
-- Payment đã SUCCESS
SELECT * FROM payments WHERE transaction_id = 'TXN-1705123456789-ABC12345';
-- status = "SUCCESS"
```

**Kết quả:** ✅ Return ngay, không cập nhật lại

---

### **3. Không Tìm Thấy Transaction ID Trong Content**

**Scenario:** Người dùng chuyển khoản nhưng không nhập đúng Transaction ID trong nội dung

**Code:**
```java
// SePayWebhookController.java - Dòng 68-72
if (transactionId == null) {
    logger.warn("⚠️ Không tìm thấy Transaction ID trong nội dung: {}", request.getContent());
    return ResponseEntity.ok("No Transaction ID found - acknowledged");
}
```

**Xử lý:**
- ✅ `extractTransactionId()` trả về `null`
- ✅ Trả về `200 OK` (SePay không retry)
- ✅ Log warning
- ✅ Đây là chuyển khoản không liên quan đến hệ thống

**Ví dụ:**

| Content | Transaction ID | Kết quả |
|---------|----------------|---------|
| `"TXN-1705123456789-ABC12345 Thanh toan"` | `"TXN-1705123456789-ABC12345"` | ✅ Extract thành công |
| `"Thanh toan hoc phi"` | `null` | ❌ Không tìm thấy → `200 OK` |
| `"TXN-123"` | `"TXN-123"` | ✅ Extract thành công |

---

### **4. Fee Không Tồn Tại**

**Scenario:** Payment có `feeId` nhưng Fee không tồn tại trong database

**Code:**
```java
// PaymentService.java - Dòng 137-138
Fee fee = feeRepository.findById(payment.getFeeId())
        .orElse(null);

if (fee != null) {
    // Cập nhật Fee
}
```

**Xử lý:**
- ✅ `feeRepository.findById()` trả về `null` nếu không tìm thấy
- ✅ Không throw exception
- ✅ Payment vẫn được cập nhật thành công
- ✅ Chỉ Fee không được cập nhật

**Nguyên nhân:**
- ❌ Fee bị xóa sau khi tạo Payment
- ❌ Fee ID nhập sai

**Giải pháp:**
```sql
-- Kiểm tra Fee có tồn tại không
SELECT * FROM fees WHERE id = 1;

-- Nếu không có, tạo Fee mới hoặc cập nhật feeId của Payment
UPDATE payments SET fee_id = NULL WHERE fee_id = 1;
```

---

### **5. Nội Dung Chuyển Khoản Có Nhiều Transaction ID**

**Scenario:** Regex tìm thấy nhiều Transaction ID trong content

**Code:**
```java
// SePayWebhookController.java - Dòng 114-120
Matcher matcher = TXN_PATTERN.matcher(content);

if (matcher.find()) {
    String transactionId = matcher.group(1).toUpperCase(); // Chỉ lấy match đầu tiên
    return transactionId;
}
```

**Xử lý:**
- ✅ Regex chỉ tìm match đầu tiên
- ✅ `matcher.find()` trả về match đầu tiên
- ✅ Lấy match đầu tiên và return

**Ví dụ:**
```
Content: "TXN-123-ABC TXN-456-XYZ Thanh toan"
Extract: "TXN-123-ABC" (chỉ lấy match đầu tiên)
```

**Kết quả:** ✅ Lấy Transaction ID đầu tiên

---

### **6. Transaction ID Không Đúng Format**

**Scenario:** Nội dung có "TXN-" nhưng format sai

**Code:**
```java
// Regex pattern: (?i)(TXN-[A-Za-z0-9-]+)
// Chỉ match Transaction ID đúng format
```

**Regex Pattern:**
```java
private static final Pattern TXN_PATTERN = Pattern.compile("(?i)(TXN-[A-Za-z0-9-]+)");
```

**Ví dụ:**

| Content | Match | Kết quả |
|---------|-------|---------|
| `"TXN-123-ABC"` | ✅ | Extract: `"TXN-123-ABC"` |
| `"TXN-abc-123"` | ✅ | Extract: `"TXN-abc-123"` (uppercase) |
| `"TXN-"` | ❌ | Không match (sau TXN- phải có ký tự) |
| `"TXN- "` | ❌ | Không match (sau TXN- không được có space) |
| `"TXN-abc@123"` | ❌ | Không match (@ không được phép) |

**Kết quả:** ✅ Chỉ extract Transaction ID đúng format

---

## 📝 Ví Dụ Thực Tế

### **Ví Dụ 1: Thanh Toán Đủ Tiền**

**1. Tạo Fee:**
```sql
INSERT INTO fees (
    student_name, class_name, month, amount, paid, due_date, status
) VALUES (
    'Trần Thị B', 'Văn luyện thi', 'Tháng 11/2024', 750000, 0, '2024-11-30', 'UNPAID'
);
```

**2. Tạo Payment:**
```sql
INSERT INTO payments (
    transaction_id, amount, status, fee_id, student_name, payment_method, notes, created_at
) VALUES (
    'TXN-1705123456789-XYZ98765', 750000, 'PENDING', 2, 'Trần Thị B', 'SEPAY_BANK_TRANSFER', 
    'Thanh toán học phí tháng 11/2024', NOW()
);
```

**3. Thông báo cho người dùng:**
- **Nội dung chuyển khoản:** `TXN-1705123456789-XYZ98765 Thanh toan hoc phi thang 11/2024`
- **Số tiền:** 750,000 VND

**4. User chuyển khoản:**
- App ngân hàng chuyển 750,000 VND
- Nội dung: `TXN-1705123456789-XYZ98765 Thanh toan hoc phi thang 11/2024`

**5. SePay webhook:**
```json
{
  "content": "TXN-1705123456789-XYZ98765 Thanh toan hoc phi thang 11/2024",
  "transferType": "in",
  "transferAmount": 750000,
  "gateway": "MBBank"
}
```

**6. Hệ thống xử lý:**
- Extract Transaction ID: `TXN-1705123456789-XYZ98765`
- Cập nhật Payment: `status = "SUCCESS"`
- Cập nhật Fee: `paid = 750000`, `status = "PAID"` (vì `750000 >= 750000`)

**Kết quả:**

**Payment:**
```sql
SELECT * FROM payments WHERE transaction_id = 'TXN-1705123456789-XYZ98765';
-- status: SUCCESS
-- payment_method: SEPAY_MBBank
```

**Fee:**
```sql
SELECT * FROM fees WHERE id = 2;
-- paid: 750000
-- status: PAID
```

---

### **Ví Dụ 2: Thanh Toán Nhiều Lần**

**1. Tạo Fee:**
```sql
INSERT INTO fees (
    student_name, class_name, month, amount, paid, due_date, status
) VALUES (
    'Lê Văn C', 'Toán cơ bản 8', 'Tháng 11/2024', 1200000, 0, '2024-11-30', 'UNPAID'
);
```

**2. Payment 1 (500,000 VND):**
```sql
INSERT INTO payments (
    transaction_id, amount, status, fee_id, student_name, payment_method, notes, created_at
) VALUES (
    'TXN-1705123456789-DEF45678', 500000, 'PENDING', 3, 'Lê Văn C', 'SEPAY_BANK_TRANSFER', 
    'Thanh toán học phí tháng 11/2024 - Lần 1', NOW()
);
```

**3. User chuyển khoản lần 1:**
- Nội dung: `TXN-1705123456789-DEF45678 Thanh toan hoc phi lan 1`
- Số tiền: 500,000 VND

**4. SePay webhook lần 1:**
- Extract Transaction ID: `TXN-1705123456789-DEF45678`
- Cập nhật Payment: `status = "SUCCESS"`
- Cập nhật Fee: `paid = 500000`, `status = "PARTIAL"` (vì `500000 < 1200000`)

**5. Payment 2 (700,000 VND):**
```sql
INSERT INTO payments (
    transaction_id, amount, status, fee_id, student_name, payment_method, notes, created_at
) VALUES (
    'TXN-1705123456789-GHI78901', 700000, 'PENDING', 3, 'Lê Văn C', 'SEPAY_BANK_TRANSFER', 
    'Thanh toán học phí tháng 11/2024 - Lần 2', NOW()
);
```

**6. User chuyển khoản lần 2:**
- Nội dung: `TXN-1705123456789-GHI78901 Thanh toan hoc phi lan 2`
- Số tiền: 700,000 VND

**7. SePay webhook lần 2:**
- Extract Transaction ID: `TXN-1705123456789-GHI78901`
- Cập nhật Payment: `status = "SUCCESS"`
- Cập nhật Fee: `paid = 1200000` (500000 + 700000), `status = "PAID"` (vì `1200000 >= 1200000`)

**Kết quả:**

**Fee:**
```sql
SELECT * FROM fees WHERE id = 3;
-- paid: 1200000
-- status: PAID
```

**Payments:**
```sql
SELECT * FROM payments WHERE fee_id = 3;
-- Payment 1: status = SUCCESS, amount = 500000
-- Payment 2: status = SUCCESS, amount = 700000
```

---

### **Ví Dụ 3: Thanh Toán Không Có Fee**

**1. Tạo Payment (không liên quan đến Fee):**
```sql
INSERT INTO payments (
    transaction_id, amount, status, fee_id, student_name, payment_method, notes, created_at
) VALUES (
    'TXN-1705123456789-JKL23456', 300000, 'PENDING', 0, 'Phạm Thị D', 'SEPAY_BANK_TRANSFER', 
    'Thanh toán phụ phí', NOW()
);
```

**Lưu ý:**
- ✅ `fee_id = 0` hoặc NULL (không liên quan đến Fee)
- ✅ Sau khi thanh toán, chỉ Payment được cập nhật
- ✅ Fee không bị ảnh hưởng

**2. User chuyển khoản:**
- Nội dung: `TXN-1705123456789-JKL23456 Thanh toan phu phi`
- Số tiền: 300,000 VND

**3. SePay webhook:**
- Extract Transaction ID: `TXN-1705123456789-JKL23456`
- Cập nhật Payment: `status = "SUCCESS"`
- **Không cập nhật Fee** (vì `feeId = 0`)

**Kết quả:**

**Payment:**
```sql
SELECT * FROM payments WHERE transaction_id = 'TXN-1705123456789-JKL23456';
-- status: SUCCESS
-- fee_id: 0
```

**Fee:** ✅ Không bị ảnh hưởng

---

## 🔐 Bảo Mật

### **1. API Key Authentication**

**Cấu hình:**
```properties
# application.properties
sepay.api.key=your_secret_api_key_here
```

**Xử lý:**
```java
// SePayWebhookController.java
if (sepayApiKey != null && !sepayApiKey.isEmpty()) {
    if (authHeader == null || !authHeader.startsWith("Bearer " + sepayApiKey)) {
        return ResponseEntity.status(401).body("Unauthorized");
    }
}
```

**Security:**
- ✅ SePay gửi `Authorization: Bearer {api_key}`
- ✅ Hệ thống so sánh với `sepay.api.key` trong config
- ✅ Nếu không khớp → Trả về `401 Unauthorized`

---

### **2. Idempotency**

**Đảm bảo:**
- ✅ Kiểm tra `status == "SUCCESS"` trước khi cập nhật
- ✅ Không xử lý webhook nhiều lần
- ✅ Tránh duplicate payment

---

### **3. Input Validation**

**Validation Rules:**
- ✅ Transaction ID format đúng (`TXN-[A-Za-z0-9-]+`)
- ✅ Payment tồn tại trong database
- ✅ Fee tồn tại (nếu có feeId)

---

## 📊 Tóm Tắt Luồng Hoạt Động

```
1. Admin tạo Payment trong MySQL (INSERT)
   ↓
2. Thông báo cho người dùng nội dung chuyển khoản (chứa Transaction ID)
   ↓
3. Người dùng chuyển khoản qua app ngân hàng với nội dung đúng
   ↓
4. SePay tự động phát hiện tiền vào tài khoản
   ↓
5. SePay gửi webhook POST /api/sepay/webhook
   ↓
6. SePayWebhookController xử lý:
   - Kiểm tra API Key
   - Lọc giao dịch "in"
   - Extract Transaction ID bằng Regex
   ↓
7. PaymentService.confirmPaymentSuccess():
   - Tìm Payment theo transactionId
   - Kiểm tra idempotency
   - Cập nhật Payment (SUCCESS)
   - Cập nhật Fee (nếu có)
   ↓
8. Kết quả:
   - Payment.status = "SUCCESS"
   - Fee.paid updated
   - Fee.status updated (PAID/PARTIAL)
```

**Đặc điểm:**
- ✅ **Tự động 100%** - Không cần can thiệp thủ công
- ✅ **Không cần QR code** - Chỉ cần Transaction ID trong nội dung chuyển khoản
- ✅ **Hoạt động với mọi ngân hàng** - SePay kết nối với tất cả ngân hàng
- ✅ **Cập nhật Fee tự động** - Tự động cập nhật trạng thái học phí

---

**Chúc bạn sử dụng thành công! 🎉**

