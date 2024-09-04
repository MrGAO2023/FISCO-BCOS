package org.example.finance.model.vo;

import java.util.List;

public class BankEntityVO {
    BankVO bankVO;
    List<ReceiptVO> senderReceiptList;
    List<ReceiptVO> accepterReceiptList;

    public BankVO getBankVO() {
        return bankVO;
    }

    public void setBankVO(BankVO bankVO) {
        this.bankVO = bankVO;
    }

    public List<ReceiptVO> getSenderReceiptList() {
        return senderReceiptList;
    }

    public void setSenderReceiptList(List<ReceiptVO> senderReceiptList) {
        this.senderReceiptList = senderReceiptList;
    }

    public List<ReceiptVO> getAccepterReceiptList() {
        return accepterReceiptList;
    }

    public void setAccepterReceiptList(List<ReceiptVO> accepterReceiptList) {
        this.accepterReceiptList = accepterReceiptList;
    }
}
