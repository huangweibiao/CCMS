package com.ccms.vo;

import java.io.Serializable;
import java.util.Objects;

public class Result<T> implements Serializable {
    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 200;
    
    /**
     * 失败状态码
     */
    public static final int ERROR_CODE = 500;
    
    /**
     * 状态码
     */
    private int code;
    
    /**
     * 返回消息
     */
    private String message;
    
    /**
     * 返回数据
     */
    private T data;

    // Setter methods
    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }
    
    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        return success("操作成功", data);
    }
    
    /**
     * 成功响应
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    /**
     * 成功响应（数据在前，消息在后）
     */
    public static <T> Result<T> success(T data, String message) {
        return success(message, data);
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return error("操作失败");
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        return error(ERROR_CODE, message);
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code == SUCCESS_CODE;
    }
    
    // Getter方法
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public T getData() {
        return data;
    }
    
    // equals方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result<?> result = (Result<?>) o;
        return code == result.code &&
                Objects.equals(message, result.message) &&
                Objects.equals(data, result.data);
    }
    
    // hashCode方法
    @Override
    public int hashCode() {
        return Objects.hash(code, message, data);
    }
    
    // toString方法
    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}