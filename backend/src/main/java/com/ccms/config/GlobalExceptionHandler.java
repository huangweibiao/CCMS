package com.ccms.config;

import com.ccms.exception.ApprovalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理各种异常并返回标准错误响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理ApprovalException异常
     */
    @ExceptionHandler(ApprovalException.class)
    public ResponseEntity<Map<String, Object>> handleApprovalException(ApprovalException ex) {
        log.error("ApprovalException: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        
        HttpStatus status = determineHttpStatus(ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", ex.getErrorCode());
        response.put("message", ex.getMessage());
        response.put("details", ex.getErrorDetail());
        
        if (ex.getBusinessId() != null) {
            response.put("businessId", ex.getBusinessId());
        }
        
        response.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(response, status);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        log.warn("参数验证失败: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "VALIDATION_ERROR");
        response.put("message", "参数验证失败");
        response.put("errors", errors);
        response.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.warn("违反约束条件: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "CONSTRAINT_VIOLATION");
        response.put("message", "违反约束条件");
        response.put("details", ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理IllegalArgumentException异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.warn("非法参数: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "ILLEGAL_ARGUMENT");
        response.put("message", "参数错误");
        response.put("details", ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理IllegalStateException异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException ex) {
        log.error("非法状态: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "ILLEGAL_STATE");
        response.put("message", "系统状态错误");
        response.put("details", ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * 处理SecurityException异常
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(
            SecurityException ex) {
        log.warn("安全异常: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "SECURITY_ERROR");
        response.put("message", "权限不足");
        response.put("details", ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * 处理其他RuntimeException异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {
        log.error("运行时异常: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "RUNTIME_ERROR");
        response.put("message", "系统内部错误");
        response.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("系统异常: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "SYSTEM_ERROR");
        response.put("message", "系统异常");
        response.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 根据异常类型确定HTTP状态码
     */
    private HttpStatus determineHttpStatus(ApprovalException ex) {
        if (ex.isValidationError()) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex.isSecurityError()) {
            return HttpStatus.FORBIDDEN;
        } else if (ex.isBusinessError()) {
            return HttpStatus.CONFLICT;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}