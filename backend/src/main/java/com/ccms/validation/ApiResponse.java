package com.ccms.validation;

import java.util.List;

/**
 * API统一响应格式
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Integer code;
    private List<String> errorDetails;
    
    // 成功响应
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = "操作成功";
        response.data = data;
        response.code = 200;
        return response;
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        response.code = 200;
        return response;
    }
    
    // 错误响应
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.code = 400;
        return response;
    }
    
    public static <T> ApiResponse<T> error(String message, Integer code) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.code = code;
        return response;
    }
    
    public static <T> ApiResponse<T> error(String message, List<String> errorDetails) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.errorDetails = errorDetails;
        response.code = 400;
        return response;
    }
    
    // Getter和Setter
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    public List<String> getErrorDetails() {
        return errorDetails;
    }
    
    public void setErrorDetails(List<String> errorDetails) {
        this.errorDetails = errorDetails;
    }
}