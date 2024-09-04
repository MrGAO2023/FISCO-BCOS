package org.example.finance.service;

import org.example.finance.model.Result;
import org.example.finance.model.bo.TransactionBO;

public interface ITransactionService {
    Result<String> bankToCompanyReceipt(TransactionBO transactionBO);
    Result<String> companyToCompanyReceipt(TransactionBO transactionBO);
    Result<String> companyToBankReceipt(TransactionBO transactionBO);
}
