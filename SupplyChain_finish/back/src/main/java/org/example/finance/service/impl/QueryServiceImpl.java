package org.example.finance.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.util.ArrayList;
import java.util.List;
import org.example.finance.model.Result;
import org.example.finance.model.vo.*;
import org.example.finance.service.IQueryService;
import org.example.finance.utils.WeBASEUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class QueryServiceImpl implements IQueryService {
    @Autowired
    WeBASEUtils weBASEUtils;


    public static final String ABI = org.example.finance.utils.IOUtil.readResourceAsString("abi/SupplyChainFin.abi");

    /**
     * 按照索引先后顺序获取所有存证信息
     * */
    @Override
    public Result listAllReceipt(String userAddress) {

        String _result = weBASEUtils.funcPost(userAddress,"receiptIndex",new ArrayList());
        JSONArray _resultJson = JSONUtil.parseArray(_result);
        String a = _resultJson.get(0).toString();
        Integer receiptIndex = Integer.valueOf(a);
        List<ReceiptVO> receiptList = new ArrayList<>();
        for(int i=1;i<=receiptIndex;i++) {
            ReceiptVO receiptVO = getReceiptDetail(i, userAddress);
            receiptList.add(receiptVO);
        }
        return Result.success(receiptList);
    }

    /**
     * 根据输入的存证索引获取对应的存证详细信息
     * */
    @Override
    public List<ReceiptVO> listReceiptByIndex(JSONArray index, String userAddress) {
        List<ReceiptVO> receiptList = new ArrayList<>();
        for (int i =0;i < index.size();i++) {
            ReceiptVO receiptVO = getReceiptDetail((Integer)index.get(i), userAddress);
            receiptList.add(receiptVO);
        }
        return receiptList;
    }

    /**
     * 根据具体索引获取存证详细信息
     * */
    @Override
    public ReceiptVO getReceiptDetail(Integer index, String userAddress) {
        JSONArray abiJSON = JSONUtil.parseArray(ABI);
        JSONObject data = JSONUtil.createObj();

        List funcParam = new ArrayList();
        funcParam.add(index);

        String _result = weBASEUtils.funcPost(userAddress,"getReceipt",funcParam);
        JSONArray _resultJson = JSONUtil.parseArray(_result);

        ReceiptVO receiptVO = new ReceiptVO();
        receiptVO.setId(index);
        receiptVO.setSenderAddress((String)_resultJson.get(0));
        receiptVO.setAccepterAddress((String)_resultJson.get(1));
        receiptVO.setReceiptType(_resultJson.getInt(2));
        receiptVO.setTransferType(_resultJson.getInt(3));
        receiptVO.setAmount(_resultJson.getInt(4));

        return receiptVO;


    }

    /**
     * 获取所有公司数据，不包含存证详细信息
     * */
    @Override
    public Result listCompany(String userAddress){

        String _result = weBASEUtils.funcPost(userAddress,"getAllCompanyAddress",new ArrayList());

        try {
            JSONArray respArray = JSONUtil.parseArray(_result);
            String a = respArray.get(0).toString();
            JSONArray addressArray = JSONUtil.parseArray(a);
            List<CompanyVO> companyList = new ArrayList<>();
            for(Object obj: addressArray) {
                CompanyVO companyVO = getCompanyDetail(userAddress, (String) obj);
                companyList.add(companyVO);
            }
            return Result.success(companyList);
        }catch (Exception e) {
            ResultVO resultVO = ResultVO.CONTRACT_ERROR;
            resultVO.setData(_result);
            return Result.error(resultVO);
        }
    }

    /**
     * 获取所有银行信息，不包含存证详细信息
     * */
    @Override
    public Result listBank(String userAddress) {

        String _result = weBASEUtils.funcPost(userAddress,"getAllBankAddress",new ArrayList());

        try {
            JSONArray respArray = JSONUtil.parseArray(_result);
            String a = respArray.get(0).toString();
            JSONArray addressArray = JSONUtil.parseArray(a);
            List<BankVO> bankList = new ArrayList<>();
            for(Object obj: addressArray) {
                BankVO bankVO = getBankDetail(userAddress, (String) obj);
                bankList.add(bankVO);
            }
            return Result.success(bankList);
        }catch (Exception e) {
            ResultVO resultVO = ResultVO.CONTRACT_ERROR;
            resultVO.setData(_result);
            return Result.error(resultVO);
        }
    }

    /**
     * 获取包括公司详情，公司发送凭证列表，公司接收凭证列表
     * */
    @Override
    public Result<CompanyEntityVO> getCompanyEntity(String userAddress, String queryAddress) {
        CompanyVO companyVO = getCompanyDetail(userAddress, queryAddress);
        List<ReceiptVO> senderReceiptList = listReceiptByIndex(companyVO.getSendReceiptIndex(), userAddress);
        List<ReceiptVO> accepterReceiptList = listReceiptByIndex(companyVO.getAcceptReceiptIndex(), userAddress);
        CompanyEntityVO companyEntityVO = new CompanyEntityVO();
        companyEntityVO.setCompanyVO(companyVO);
        companyEntityVO.setSenderReceiptList(senderReceiptList);
        companyEntityVO.setAccepterReceiptList(accepterReceiptList);
        return Result.success(companyEntityVO);
    }

    /**
     * 获取包括银行详情，银行发送凭证列表，银行接收凭证列表
     * */
    @Override
    public Result getBankEntity(String userAddress, String queryAddress){
        BankVO bankVO = getBankDetail(userAddress, queryAddress);
        List<ReceiptVO> senderReceiptList = listReceiptByIndex(bankVO.getSendReceiptIndex(), userAddress);
        List<ReceiptVO> accepterReceiptList = listReceiptByIndex(bankVO.getAcceptReceiptIndex(), userAddress);
        BankEntityVO bankEntityVO = new BankEntityVO();
        bankEntityVO.setBankVO(bankVO);
        bankEntityVO.setSenderReceiptList(senderReceiptList);
        bankEntityVO.setAccepterReceiptList(accepterReceiptList);
        return Result.success(bankEntityVO);
    }

    /**
     * 获取公司详情实现方法
     * */
    @Override
    public CompanyVO getCompanyDetail(String userAddress, String queryAddress) {

        List funcParam = new ArrayList();
        funcParam.add(queryAddress);

        String _result = weBASEUtils.funcPost(userAddress,"getCompany",funcParam);

        JSONArray companyArray = JSONUtil.parseArray(_result);
        JSONArray acceptReceiptIndex = JSONUtil.parseArray(companyArray.get(3));
        JSONArray sendReceiptIndex = JSONUtil.parseArray(companyArray.get(4));
        CompanyVO companyVO = new CompanyVO();
        companyVO.setName((String)companyArray.get(0));
        companyVO.setAddress((String)companyArray.get(1));
        companyVO.setAmount(companyArray.getInt(2));
        companyVO.setAcceptReceiptIndex(acceptReceiptIndex);
        companyVO.setSendReceiptIndex(sendReceiptIndex);
        return companyVO;
    }

    /**
     * 获取银行详情实现方法
     * */
    @Override
    public BankVO getBankDetail(String userAddress, String queryAddress) {

        List funcParam = new ArrayList();
        funcParam.add(queryAddress);

        String _result = weBASEUtils.funcPost(userAddress,"getBank",funcParam);

        JSONArray bankArray = JSONUtil.parseArray(_result);
        JSONArray acceptReceiptIndex = JSONUtil.parseArray(bankArray.get(3));
        JSONArray sendReceiptIndex = JSONUtil.parseArray(bankArray.get(4));
        BankVO bankVO = new BankVO();
        bankVO.setName((String)bankArray.get(0));
        bankVO.setAddress((String)bankArray.get(1));
        bankVO.setAmount(bankArray.getInt(2));
        bankVO.setAcceptReceiptIndex(acceptReceiptIndex);
        bankVO.setSendReceiptIndex(sendReceiptIndex);
        return bankVO;
    }
}
