package org.example.finance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.finance.model.Result;
import org.example.finance.model.vo.BankEntityVO;
import org.example.finance.model.vo.CompanyEntityVO;
import org.example.finance.service.IQueryService;
import org.example.finance.service.impl.QueryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "查询相关接口", tags = "查询相关接口")
@RestController
@RequestMapping("/finance/query")
public class QueryController {
    @Autowired
    IQueryService queryService;

    @ApiOperation(value = "列出所有公司信息", notes = "列出所有公司信息")
    @RequestMapping(value = "listCompany", method = RequestMethod.GET)
    public Result listCompany(@RequestParam("address") String address){
        return queryService.listCompany(address);
    }

    @ApiOperation(value = "列出所有银行信息", notes = "列出所有银行信息")
    @RequestMapping(value = "listBank", method = RequestMethod.GET)
    public Result listBank(@RequestParam("address") String address) {
        return queryService.listBank(address);
    }

    @ApiOperation(value = "列出所有凭证信息", notes = "列出所有凭证信息")
    @RequestMapping(value = "listAllReceipt", method = RequestMethod.GET)
    public Result listAllReceipt(@RequestParam("address") String address) {
        return queryService.listAllReceipt(address);
    }

    @ApiOperation(value = "获取公司实体详细信息(详情，凭证列表)", notes = "获取公司实体详细信息(详情，凭证列表)")
    @RequestMapping(value = "getCompanyEntity", method = RequestMethod.GET)
    public Result<CompanyEntityVO> getCompanyEntity(@RequestParam("address") String address,
                                                    @RequestParam("queryAddress") String queryAddress) {
        return queryService.getCompanyEntity(address, queryAddress);
    }

    @ApiOperation(value = "获取银行实体详细信息(详情，凭证列表)", notes = "获取银行实体详细信息(详情，凭证列表)")
    @RequestMapping(value = "getBankEntity", method = RequestMethod.GET)
    public Result<BankEntityVO> getBankEntity(@RequestParam("address") String address,
                                                 @RequestParam("queryAddress") String queryAddress) {
        return queryService.getBankEntity(address, queryAddress);
    }
}
