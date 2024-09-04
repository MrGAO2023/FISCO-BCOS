package org.example.finance.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.finance.model.Result;
import org.example.finance.model.bo.LoginBO;
import org.example.finance.model.bo.RegisterBO;
import org.example.finance.service.IOrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Arthur
 * @since 2021-09-07
 */
@Api(value = "组织（公司、银行）相关接口", tags = "组织（公司、银行）相关接口")
@RestController
@RequestMapping("/finance/org")
public class OrgController {

    @Autowired
    IOrgService orgService;


    @ApiOperation(value = "登录接口", notes = "登录接口")
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Result<String> login(@RequestBody LoginBO loginBO) {
        return orgService.login(loginBO);
    }

    @ApiOperation(value = "注册接口", notes = "注册接口")
    @RequestMapping(value = "register", method = RequestMethod.POST)
    public Result<String> register(@RequestBody RegisterBO registerBO) {
        return orgService.register(registerBO);
    }

//    @ApiOperation(value = "重新绑定地址接口", notes = "重新绑定地址接口")
//    @RequestMapping(value = "rebind", method = RequestMethod.GET)
//    public Result<String> rebind(@RequestParam String username, @RequestParam String address) {
//        return orgService.rebind(username, address);
//    }


}
