package org.example.finance.controller;

import cn.hutool.json.JSONUtil;
import org.example.finance.model.Result;
import org.example.finance.model.bo.TransactionBO;
import org.example.finance.service.ITransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = TransactionController.class)
class TransactionControllerTest {


    @Autowired
    private WebApplicationContext context;

    @MockBean
    private ITransactionService transactionService;

    MockMvc mockMvc;

    Logger logger = LoggerFactory.getLogger(TransactionControllerTest.class);


    @BeforeEach
    public void init() {
        System.out.println("-----------------TransactionControllerTest开始测试-----------------");
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    public void after() {
        System.out.println("-----------------TransactionControllerTest测试结束-----------------");
    }

    @Test
    void bankToCompanyReceipt() throws Exception{
        System.out.println("-----------------开始bankToCompanyReceipt功能测试-----------------");

        TransactionBO transactionBO = new TransactionBO();
        transactionBO.setAccepterAddress("0xf95177c2f60270a354c8f5691a08d5bee3cacdff");
        transactionBO.setReceiptType(1);
        transactionBO.setSenderAddress("111111111111111");
        transactionBO.setAmount(1000);

        Mockito.when(transactionService.bankToCompanyReceipt(transactionBO)).thenReturn(Result.success("ok"));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/finance/transaction/bankToCompanyReceipt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtil.toJsonStr(transactionBO)))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    void companyToBankReceipt() throws Exception{
        System.out.println("-----------------开始companyToBankReceipt功能测试-----------------");

        TransactionBO transactionBO = new TransactionBO();
        transactionBO.setAccepterAddress("2222222222222");
        transactionBO.setReceiptType(1);
        transactionBO.setSenderAddress("0xf95177c2f60270a354c8f5691a08d5bee3cacdff");
        transactionBO.setAmount(11);

        Mockito.when(transactionService.companyToBankReceipt(transactionBO)).thenReturn(Result.success("ok"));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/finance/transaction/companyToBankReceipt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.toJsonStr(transactionBO)))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        logger.info(mvcResult.getResponse().getContentAsString());
    }

}