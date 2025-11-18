package vn.edu.uth.quanlidaythem.payment.dto.Request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO để nhận webhook từ SePay
 * SePay sẽ gửi thông tin giao dịch khi có tiền vào tài khoản
 */
public class SePayWebhookRequest {

    @JsonProperty("id")
    private Long id; // ID giao dịch bên SePay

    @JsonProperty("gateway")
    private String gateway; // Ví dụ: MBBank, VCB, Techcombank

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("subAccount")
    private String subAccount;

    @JsonProperty("content")
    private String content; // QUAN TRỌNG: Nội dung chuyển khoản (Chứa TXN-...)

    @JsonProperty("transferType")
    private String transferType; // "in" hoặc "out"

    @JsonProperty("transferAmount")
    private Long transferAmount; // Số tiền

    @JsonProperty("accumulated")
    private Long accumulated; // Số dư lũy kế

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSubAccount() {
        return subAccount;
    }

    public void setSubAccount(String subAccount) {
        this.subAccount = subAccount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public Long getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(Long transferAmount) {
        this.transferAmount = transferAmount;
    }

    public Long getAccumulated() {
        return accumulated;
    }

    public void setAccumulated(Long accumulated) {
        this.accumulated = accumulated;
    }

    @Override
    public String toString() {
        return "SePayWebhookRequest{" +
                "id=" + id +
                ", gateway='" + gateway + '\'' +
                ", transactionDate='" + transactionDate + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", content='" + content + '\'' +
                ", transferType='" + transferType + '\'' +
                ", transferAmount=" + transferAmount +
                ", accumulated=" + accumulated +
                '}';
    }
}

