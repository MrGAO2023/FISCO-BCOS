package org.example.finance.service.impl;

import org.example.finance.model.Result;
import org.example.finance.model.bo.TransactionBO;
import org.example.finance.service.ITransactionService;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.lang.reflect.Method;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TransactionServiceImplTest {

    static private String TEST_USER_ADDRESS = "0xf95177c2f60270a354c8f5691a08d5bee3cacdff";

    @Resource
    private ITransactionService transactionService;


    @BeforeEach
    public void init() {
        System.out.println("-----------------开始测试-----------------");
    }

    @AfterEach
    public void after() {
        System.out.println("-----------------测试结束-----------------");
    }

    @Test
    void commonProcess() throws Exception{
        System.out.println("-----------------开始listBank功能测试-----------------");

        TransactionBO transactionBO = new TransactionBO();
        transactionBO.setAccepterAddress("0x7ecaebda47faf68e04284102a3692b5aba5677b5");
        transactionBO.setReceiptType(1);
        transactionBO.setSenderAddress("0xf95177c2f60270a354c8f5691a08d5bee3cacdff");
        transactionBO.setAmount(11);

        String funcName = "companyToBankReceipt";

        Class<? extends ITransactionService> clazz = transactionService.getClass();
        Method _mothod= clazz.getDeclaredMethod("commonProcess", TransactionBO.class,String.class);
        _mothod.setAccessible(true);
        Result<String> _result = (Result<String>) _mothod.invoke(transactionService, transactionBO,funcName);

        Assert.assertEquals("listBank函数测试1验证失败！！！！", 200,_result.getCode());
        Assert.assertNotNull("listBank函数测试2验证失败！！！！", _result.getData());
    }

}