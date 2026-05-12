package com.ccms.vo;

import java.util.Objects;

public class ResultVO<T> {
    private int code;
    private String msg;
    private T data;
    
    public ResultVO() {
    }
    
    public ResultVO(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    public static <T> ResultVO<T> success() {
        return new ResultVO<>(200, "操作成功", null);
    }
    
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "操作成功", data);
    }
    
    public static <T> ResultVO<T> success(String msg, T data) {
        return new ResultVO<>(200, msg, data);
    }
    
    public static <T> ResultVO<T> error(String msg) {
        return new ResultVO<>(500, msg, null);
    }
    
    public static <T> ResultVO<T> error(int code, String msg) {
        return new ResultVO<>(code, msg, null);
    }
    
    public String getMsg() {
        return msg;
    }
    
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public boolean isSuccess() {
        return code == 200;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
}