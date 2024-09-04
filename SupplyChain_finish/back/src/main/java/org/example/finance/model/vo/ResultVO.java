package org.example.finance.model.vo;

public class ResultVO<T> {
    private int code;
    private String msg;
    private T data;

    public ResultVO(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 统一返回内容
     * */
    //成功:200
    public static ResultVO SUCCESS = new ResultVO(200, "正常", "正常");
    //请求参数缺失:404001
    public static ResultVO PARAM_EMPTY = new ResultVO(404001, "请求参数缺失", "请求参数缺失");
    //查询内容缺失:404002
    public static ResultVO QUERY_EMPTY = new ResultVO(404002, "查询内容不存在", "查询内容不存在");
    //信息已存在:500001
    public static ResultVO QUERY_EXISTS = new ResultVO(500001, "信息已存在！", "信息已存在！");

    public static ResultVO CONTRACT_ERROR = new ResultVO(500002, "智能合约请求存在问题","");

    public static ResultVO TOKEN_EMPTY = new ResultVO(500003, "Token已失效","Token已失效");

    public static ResultVO ADDRESS_INVALID = new ResultVO(500004, "操作地址不合法","操作地址不合法");

    public static ResultVO COMPANY_NOT_EXIST = new ResultVO(500005, "公司不存在","公司不存在");
    public static ResultVO BANK_NOT_EXIST = new ResultVO(500006, "公司不存在","公司不存在");
    public static ResultVO AMOUNT_NOT_ENOUGH = new ResultVO(500007, "金额不足","金额不足");

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
