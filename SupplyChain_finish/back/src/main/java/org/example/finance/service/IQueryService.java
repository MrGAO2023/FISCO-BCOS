package org.example.finance.service;

import cn.hutool.json.JSONArray;
import org.example.finance.model.Result;
import org.example.finance.model.vo.BankVO;
import org.example.finance.model.vo.CompanyEntityVO;
import org.example.finance.model.vo.CompanyVO;
import org.example.finance.model.vo.ReceiptVO;

import java.util.List;

public interface IQueryService {

    Result listAllReceipt(String userAddress);
    List<ReceiptVO> listReceiptByIndex(JSONArray index, String userAddress);
    ReceiptVO getReceiptDetail(Integer index, String userAddress);
    Result listCompany(String userAddress);
    Result listBank(String userAddress);
    Result<CompanyEntityVO> getCompanyEntity(String userAddress, String queryAddress);
    Result getBankEntity(String userAddress, String queryAddress);
    CompanyVO getCompanyDetail(String userAddress, String queryAddress);
    BankVO getBankDetail(String userAddress, String queryAddress);
}
