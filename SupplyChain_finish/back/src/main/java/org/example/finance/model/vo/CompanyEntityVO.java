package org.example.finance.model.vo;

import java.util.List;

public class CompanyEntityVO {
    CompanyVO companyVO;
    List<ReceiptVO> senderReceiptList;
    List<ReceiptVO> accepterReceiptList;

    public CompanyVO getCompanyVO() {
        return companyVO;
    }

    public void setCompanyVO(CompanyVO companyVO) {
        this.companyVO = companyVO;
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
