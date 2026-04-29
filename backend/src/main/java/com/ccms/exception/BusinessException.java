package com.ccms.exception;

/**
 * 业务逻辑异常
 * 
 * @author 系统生成
 */
public class BusinessException extends RuntimeException {
    
    private int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
    
    // 业务异常子类
    public static class ApplyStatusException extends BusinessException {
        public ApplyStatusException(String message) {
            super(1001, message);
        }
    }
    
    public static class BudgetException extends BusinessException {
        public BudgetException(String message) {
            super(1002, message);
        }
    }
    
    public static class ApprovalException extends BusinessException {
        public ApprovalException(String message) {
            super(1003, message);
        }
    }
    
    public static class ReimburseStatusException extends BusinessException {
        public ReimburseStatusException(String message) {
            super(1004, message);
        }
    }
}