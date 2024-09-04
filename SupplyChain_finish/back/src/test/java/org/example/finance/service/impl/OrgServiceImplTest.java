package org.example.finance.service.impl;

import org.example.finance.model.Result;
import org.example.finance.model.bo.RegisterBO;
import org.example.finance.service.IOrgService;
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
public class OrgServiceImplTest {

    @Resource
    private IOrgService orgService;


    @BeforeEach
    public void init() {
        System.out.println("-----------------开始测试-----------------");
    }

    @AfterEach
    public void after() {
        System.out.println("-----------------测试结束-----------------");
    }


    @Test
    void register() {
        System.out.println("-----------------开始register功能测试-----------------");
        RegisterBO registerBO = new RegisterBO();
        registerBO.setAddress("0xf95177c2f60270a354c8f5691a08d5bee3cacdff");
        registerBO.setOrgType(1);
        registerBO.setUsername("公司999");

        Result<String> _result = orgService.register(registerBO);

        Assert.assertEquals("register函数测试失败！！！！", 200,_result.getCode());
    }
}