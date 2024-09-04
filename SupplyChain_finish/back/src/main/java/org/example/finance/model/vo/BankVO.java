package org.example.finance.model.vo;

import cn.hutool.json.JSONArray;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "银行VO")
public class BankVO {
    @ApiModelProperty(value = "银行区块链账户地址")
    String address;
    @ApiModelProperty(value = "银行名称")
    String name;
    @ApiModelProperty(value = "银行余额")
    Integer amount;
    @ApiModelProperty(value = "接收存证索引列表")
    JSONArray acceptReceiptIndex;
    @ApiModelProperty(value = "发送存证索引列表")
    JSONArray sendReceiptIndex;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public JSONArray getAcceptReceiptIndex() {
        return acceptReceiptIndex;
    }

    public void setAcceptReceiptIndex(JSONArray acceptReceiptIndex) {
        this.acceptReceiptIndex = acceptReceiptIndex;
    }

    public JSONArray getSendReceiptIndex() {
        return sendReceiptIndex;
    }

    public void setSendReceiptIndex(JSONArray sendReceiptIndex) {
        this.sendReceiptIndex = sendReceiptIndex;
    }
}
