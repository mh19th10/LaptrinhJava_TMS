package vn.edu.uth.quanlidaythem.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Generator cho QR code theo chuẩn VietQR (EMV QR Code)
 * Code chuẩn đã được test - xử lý đúng cấu trúc TLV và CRC
 */
@Component
public class VietQRGenerator {

    // Format trong application.properties: 970422|STK|TEN
    @Value("${payment.vietqr.merchant.account.info:970422|2730788922276|NGUYEN HONG DONGDONG}")
    private String merchantAccountInfo;

    /**
     * Tạo QR code data hoàn chỉnh theo chuẩn VietQR
     * @param transactionId Transaction ID
     * @param amount Số tiền (VND)
     * @param content Nội dung chuyển khoản
     * @return QR code data string
     */
    public String generateCompleteVietQRData(String transactionId, Long amount, String content) {
        StringBuilder sb = new StringBuilder();

        // Tách thông tin cấu hình
        String[] infoParts = merchantAccountInfo.split("\\|");
        String bankBin = infoParts.length > 0 ? infoParts[0] : "970422";
        String accountNum = infoParts.length > 1 ? infoParts[1] : "2730788922276";

        // 1. Payload Format Indicator (00)
        appendTLV(sb, "00", "01");

        // 2. Point of Initiation Method (01) - 12 là Dynamic (có số tiền)
        appendTLV(sb, "01", "12");

        // 3. Merchant Account Information (38) - QUAN TRỌNG NHẤT
        String merchantInfo = buildMerchantInfo(bankBin, accountNum);
        appendTLV(sb, "38", merchantInfo);

        // 4. Merchant Category Code (52)
        appendTLV(sb, "52", "0000");

        // 5. Transaction Currency (53) - 704 là VND
        appendTLV(sb, "53", "704");

        // 6. Transaction Amount (54)
        if (amount != null && amount > 0) {
            appendTLV(sb, "54", String.valueOf(amount));
        }

        // 7. Country Code (58)
        appendTLV(sb, "58", "VN");

        // 8. Additional Data Field (62) - Chứa nội dung chuyển khoản
        // --- SỬA LỖI TẠI ĐÂY: Đưa transactionId lên đầu ---
        String cleanContent = (content != null ? removeAccents(content) : "");
        // Format: "TXN-123... Thanh toan hoc phi..."
        String fullContent = transactionId + " " + cleanContent; 
        
        String additionalData = buildAdditionalData(fullContent);
        appendTLV(sb, "62", additionalData);

        // 9. CRC (63) - Phải thêm ID 63 và Length 04 TRƯỚC KHI tính toán
        sb.append("6304");
        String crc = calculateCRC16(sb.toString());
        sb.append(crc);

        return sb.toString();
    }

    // --- CÁC HÀM HỖ TRỢ (HELPER) ---

    /**
     * Append TLV (Tag-Length-Value) format
     */
    private void appendTLV(StringBuilder sb, String id, String value) {
        if (value == null) value = "";
        sb.append(id).append(String.format("%02d", value.length())).append(value);
    }

    /**
     * Cấu trúc lồng nhau Tag 38 > Tag 00, 01
     * Tag 38: Merchant Account Information
     * - Tag 00: GUID VietQR (A000000727)
     * - Tag 01: Beneficiary Organization (lồng Tag 00, 01)
     * - Tag 00: Bank BIN
     * - Tag 01: Account Number
     * - Tag 02: Service Code (QRIBFTTA)
     */
    private String buildMerchantInfo(String bin, String accNum) {
        StringBuilder sb = new StringBuilder();

        // GUID VietQR
        appendTLV(sb, "00", "A000000727");

        // Beneficiary Organization (Tag 01 lồng tiếp Tag 00, 01)
        StringBuilder beneficiary = new StringBuilder();
        appendTLV(beneficiary, "00", bin);
        appendTLV(beneficiary, "01", accNum);

        appendTLV(sb, "01", beneficiary.toString());

        // Service Code
        appendTLV(sb, "02", "QRIBFTTA");

        return sb.toString();
    }

    /**
     * Build Additional Data Field (Tag 62)
     * Tag 08: Purpose of Transaction
     */
    private String buildAdditionalData(String content) {
        StringBuilder sb = new StringBuilder();

        // Tag 08: Purpose of Transaction
        // Cắt ngắn nếu quá dài để đảm bảo QR không quá lớn
        if (content.length() > 50) content = content.substring(0, 50); 
        appendTLV(sb, "08", content);

        return sb.toString();
    }

    /**
     * Thuật toán CRC16-CCITT (0x1021) chuẩn quốc tế
     * Tính CRC cho toàn bộ data string (bao gồm cả "6304")
     */
    private String calculateCRC16(String data) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        byte[] bytes = data.getBytes();
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }

        crc &= 0xffff;
        return String.format("%04X", crc).toUpperCase();
    }

    /**
     * Hàm bỏ dấu tiếng Việt (Để tránh lỗi font trên app ngân hàng)
     */
    public static String removeAccents(String text) {
        if (text == null) return "";
        String nfd = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfd).replaceAll("").replace("đ", "d").replace("Đ", "D");
    }
}