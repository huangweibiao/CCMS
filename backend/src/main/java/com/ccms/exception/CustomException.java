package com.ccms.exception;

import org.springframework.http.HttpStatus;

/**
 * 自定义异常类
 * 
 * @author 系统生成
 */
public class CustomException extends RuntimeException {
    
    private int code;
    private HttpStatus status;

    public CustomException(int code, String message) {
        super(message);
        this.code = code;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public CustomException(String message) {
        super(message);
        this.code = 400;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public CustomException(int code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}