package org.example.finance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.finance.model.Result;
import org.example.finance.model.bo.TransactionBO;
import org.example.finance.service.ITransactionService;
import org.example.finance.service.impl.TransactionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "交易相关接口", tags = "交易相关接口")
@RestController
@RequestMapping("/finance/transaction")
public class TransactionController {

    @Autowired
    ITransactionService transactionService;

    @ApiOperation(value = "银行向公司交易（公司向银行提供交易存证）", notes = "银行向公司交易（公司向银行提供交易存证）")
    @RequestMapping(value = "bankToCompanyReceipt", method = RequestMethod.POST)
    public Result<String> bankToCompanyReceipt(@RequestBody TransactionBO transactionBO) {
        return transactionService.bankToCompanyReceipt(transactionBO);
    }

    @ApiOperation(value = "接收存证的公司需要给发送存证的公司转账存证对应的数额", notes = "接收存证的公司需要给发送存证的公司转账存证对应的数额")
    @RequestMapping(value = "companyToCompanyReceipt", method = RequestMethod.POST)
    public Result<String> companyToCompanyReceipt(@RequestBody TransactionBO transactionBO) {
        return transactionService.companyToCompanyReceipt(transactionBO);
    }

    @ApiOperation(value = "公司与银行交易(银行向公司发送凭证)", notes = "公司与银行交易(银行向公司发送凭证)")
    @RequestMapping(value = "companyToBankReceipt", method = RequestMethod.POST)
    public Result<String> companyToBankReceipt(@RequestBody TransactionBO transactionBO) {
        return transactionService.companyToBankReceipt(transactionBO);
    }
}
