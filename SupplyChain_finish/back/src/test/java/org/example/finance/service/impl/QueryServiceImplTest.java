package org.example.finance.service.impl;

import cn.hutool.core.util.StrUtil;
import org.example.finance.model.Result;
import org.example.finance.model.vo.ReceiptVO;
import org.example.finance.service.IQueryService;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@SpringBootTest
@RunWith(SpringRunner.class)
public class QueryServiceImplTest {

    static private String TEST_USER_ADDRESS = "0xf95177c2f60270a354c8f5691a08d5bee3cacdff";

    @Resource
    private IQueryService queryService;


    @BeforeEach
    public void init() {
        System.out.println("-----------------开始测试-----------------");
    }

    @AfterEach
    public void after() {
        System.out.println("-----------------测试结束-----------------");
    }

/**
    @Test
    void listCompany() {
        System.out.println("-----------------开始listCompany功能测试-----------------");

        Result<String> _result = queryService.listCompany(TEST_USER_ADDRESS);
        Assert.assertEquals("listCompany函数测试1验证失败！！！！", 200,_result.getCode());
        Assert.assertNotNull("listCompany函数测试2验证失败！！！！", _result.getData());
    }
**/
    @Test
    void listBank() {
        System.out.println("-----------------开始listBank功能测试-----------------");

        Result<String> _result = queryService.listCompany(TEST_USER_ADDRESS);
        Assert.assertEquals("listBank函数测试1验证失败！！！！", 200,_result.getCode());
        Assert.assertNotNull("listBank函数测试2验证失败！！！！", _result.getData());
    }
/**
    @Test
    void getReceiptDetail() {
        System.out.println("-----------------开始getReceiptDetail功能测试-----------------");

        ReceiptVO _result = queryService.getReceiptDetail(3,TEST_USER_ADDRESS);
        Assert.assertTrue("getReceiptDetail函数测试1验证失败！！！！", StrUtil.isNotEmpty(_result.getAccepterAddress()));
//        Assert.assertEquals("getReceiptDetail函数测试1验证失败！！！！", ReceiptVO.class,_result.getClass());
        Assert.assertNotNull("getReceiptDetail函数测试2验证失败！！！！", _result);
    }

**/
    @Test
    void getBankEntity() {
        System.out.println("-----------------开始getBankEntity功能测试-----------------");

        Result _result = queryService.getBankEntity(TEST_USER_ADDRESS,"0xf95177c2f60270a354c8f5691a08d5bee3cacdff");
        Assert.assertEquals("getBankEntity函数测试1验证失败！！！！", 200,_result.getCode());
        Assert.assertNotNull("getBankEntity函数测试2验证失败！！！！", _result);
    }
}