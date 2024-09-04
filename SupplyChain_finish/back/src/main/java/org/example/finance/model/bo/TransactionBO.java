package org.example.finance.model.bo;

public class TransactionBO {
    String senderAddress;
    String accepterAddress;
    Integer amount;
    Integer receiptType;

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getAccepterAddress() {
        return accepterAddress;
    }

    public void setAccepterAddress(String accepterAddress) {
        this.accepterAddress = accepterAddress;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getReceiptType() {
        return receiptType;
    }

    public void setReceiptType(Integer receiptType) {
        this.receiptType = receiptType;
    }
}
