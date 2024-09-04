package org.example.finance.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.example.finance.model.Result;
import org.example.finance.model.bo.LoginBO;
import org.example.finance.model.bo.RegisterBO;
import org.example.finance.model.vo.ResultVO;
import org.example.finance.service.IOrgService;
import org.example.finance.utils.WeBASEUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;



/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Arthur
 * @since 2021-09-07
 */
@Service
public class OrgServiceImpl implements IOrgService {
    @Autowired
    WeBASEUtils weBASEUtils;

    private static final String OWNER_ADDRESS = "0x2ea60e71396714814e68f6411c12513b2c25725b";


    public static final String ABI = org.example.finance.utils.IOUtil.readResourceAsString("abi/SupplyChainFin.abi");

    /**
     * 登录Service
     * LoginBO loginBO
     **/
    @Override
    public Result<String> login(@RequestBody LoginBO loginBO) {
        if (StrUtil.isEmpty(loginBO.getAddress())
        ) {
            return Result.error(ResultVO.PARAM_EMPTY);
        }

        List funcParam = new ArrayList();
        funcParam.add(loginBO.getAddress());

        String funcName;
        if(loginBO.getOrgType() == 2){
            funcName ="getBank";
        }else{
            funcName ="getCompany";
        }

        String _result = weBASEUtils.funcPost(loginBO.getAddress(),funcName,funcParam);
        JSONArray _resultJson = JSONUtil.parseArray(_result);
        if (StrUtil.isEmpty(_resultJson.get(0).toString())){
            return Result.error(ResultVO.CONTRACT_ERROR);
        }

        return Result.success("ok");
    }


    /**
     * 注册Service
     * RegisterBO registerBO
     * */
    @Override
    public Result<String> register(RegisterBO registerBO) {
        if (StrUtil.isEmpty(registerBO.getUsername()) ||
                StrUtil.isEmpty(registerBO.getAddress()) ||
                registerBO.getOrgType() == null
        ) {
            return Result.error(ResultVO.PARAM_EMPTY);
        }

        List funcParam = new ArrayList();
        funcParam.add(registerBO.getUsername());
        funcParam.add(registerBO.getAddress());
        if(registerBO.getOrgType() == 2){
            funcParam.add(BigInteger.valueOf(1000));
        }

        String funcName;
        if(registerBO.getOrgType() == 2){
            funcName ="addBank";
        }else{
            funcName ="addCompany";
        }

        String _result = weBASEUtils.funcPost(OWNER_ADDRESS,funcName,funcParam);
        JSONObject _resultJson = JSONUtil.parseObj(_result);
        if (_resultJson.containsKey("statusOK") == false || _resultJson.getBool("statusOK") != true){ // _resultJson.getInt("code") > 0
            return Result.error(ResultVO.CONTRACT_ERROR);
        }

        return Result.success("ok");
    }


//    public Result<String> rebind(String username, String address) {
////        UpdateWrapper uw = new UpdateWrapper();
////        uw.eq("username", username);
////        uw.set("account_address", address);
////        update(uw);
//        return Result.success("ok");
//    }




}
