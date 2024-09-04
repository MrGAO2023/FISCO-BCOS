package org.example.finance.model.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "注册BO")
public class RegisterBO {
    @ApiModelProperty(value = "用户名")
    String username;
    @ApiModelProperty(value = "组织类型")
    Integer orgType;
    @ApiModelProperty(value = "区块链账户地址")
    String address;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getOrgType() {
        return orgType;
    }

    public void setOrgType(Integer orgType) {
        this.orgType = orgType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
