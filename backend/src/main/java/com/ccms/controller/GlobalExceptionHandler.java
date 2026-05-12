package com.ccms.controller;

import com.ccms.dto.ApprovalResult;
import com.ccms.exception.ApprovalException;
import com.ccms.exception.ApprovalNotFoundException;
import com.ccms.exception.ApprovalStateException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理审批业务异常
     */
    @ExceptionHandler(ApprovalException.class)
    public ResponseEntity<ApprovalResult> handleApprovalException(ApprovalException ex, WebRequest request) {
        log.warn("审批业务异常: {} - {}", ex.getErrorCode(), ex.getErrorMessage());
        
        ApprovalResult result = buildErrorResult(ex.getErrorCode(), ex.getErrorMessage());
        
        // 根据异常类型设置不同的HTTP状态码
        if (ex instanceof ApprovalNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } else if (ex instanceof ApprovalStateException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApprovalResult> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("参数验证异常: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String errorMessage = "参数验证失败: " + errors.toString();
        ApprovalResult result = buildErrorResult("VALIDATION_ERROR", errorMessage);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApprovalResult> handleBindException(BindException ex) {
        log.warn("参数绑定异常: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String errorMessage = "参数绑定失败: " + errors.toString();
        ApprovalResult result = buildErrorResult("BINDING_ERROR", errorMessage);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApprovalResult> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("约束违反异常: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String errorMessage = "约束违反: " + errors.toString();
        ApprovalResult result = buildErrorResult("CONSTRAINT_VIOLATION", errorMessage);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * 处理未预期的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApprovalResult> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("运行时异常: ", ex);
        
        ApprovalResult result = buildErrorResult("RUNTIME_ERROR", "系统内部错误，请稍后重试");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
    
    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApprovalResult> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("未处理的异常: ", ex);
        
        ApprovalResult result = buildErrorResult("UNKNOWN_ERROR", "系统异常，请联系管理员");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
    
    /**
     * 构建错误响应结果
     */
    private ApprovalResult buildErrorResult(String errorCode, String errorMessage) {
        return ApprovalResult.builder()
                .success(false)
                .message(errorMessage)
                .errorCode(errorCode)
                .errorDetails(errorMessage)
                .build();
    }
    
    /**
     * 构建详细错误响应对象
     */
    private ErrorResponse buildErrorResponse(String errorCode, String errorMessage, HttpStatus status) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(errorMessage)
                .errorCode(errorCode)
                .path("审批系统API")
                .build();
    }
    
    /**
     * 错误响应对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String errorCode;
        private String path;
    }
}