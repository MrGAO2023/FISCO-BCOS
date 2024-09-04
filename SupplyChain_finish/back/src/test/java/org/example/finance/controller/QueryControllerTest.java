package org.example.finance.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.example.finance.Application;
import org.example.finance.model.Result;
import org.example.finance.service.IQueryService;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = QueryController.class)
class QueryControllerTest {

    static private String TEST_USER_ADDRESS = "0xf95177c2f60270a354c8f5691a08d5bee3cacdff";

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private IQueryService queryService;

    MockMvc mockMvc;

    Logger logger = LoggerFactory.getLogger(QueryControllerTest.class);


    @BeforeEach
    public void init() {
        System.out.println("-----------------QueryControllerTest开始测试-----------------");
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    public void after() {
        System.out.println("-----------------QueryControllerTest测试结束-----------------");
    }

    @Test
    void listBank() throws Exception{
        System.out.println("-----------------开始listBank功能测试-----------------");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("address", TEST_USER_ADDRESS);

        Mockito.when(queryService.listBank(TEST_USER_ADDRESS)).thenReturn(Result.success("ok"));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/finance/query/listBank").params(params)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        logger.info(mvcResult.getResponse().getContentAsString());
//        String expected = "[{\"id\":\"1001\",\"name\":\"ramostear\",\"alias\":\"谭朝红\",\"roles\":[{\"id\":\"1001\",\"name\":\"admin\",\"description\":\"all permissions for this role.\"}]}]";
//        JSONAssert.assertEquals(expected,mvcResult.getResponse().getContentAsString(),false);

    }


    @Test
    void getBankEntity() throws Exception{
        System.out.println("-----------------开始getBankEntity功能测试-----------------");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("address", TEST_USER_ADDRESS);
        params.add("queryAddress", "0xf95177c2f60270a23232f56912332323233gihfj");

        Mockito.when(queryService.getBankEntity(TEST_USER_ADDRESS,"0xf95177c2f60270a23232f56912332323233gihfj")).thenReturn(Result.success("ok"));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/finance/query/getBankEntity").params(params)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        logger.info(mvcResult.getResponse().getContentAsString());

    }
}