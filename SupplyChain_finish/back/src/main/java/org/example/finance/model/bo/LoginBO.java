package org.example.finance.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "用户登录 BO")
public class LoginBO {
    @ApiModelProperty(value = "用户地址")
    String address;
    @ApiModelProperty(value = "用户类型")
    Integer orgType;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getOrgType() {
        return orgType;
    }

    public void setOrgType(Integer orgType) {
        this.orgType = orgType;
    }
}
