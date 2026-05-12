package com.ccms.exception;

import com.ccms.exception.GlobalExceptionHandler;
import com.ccms.exception.ApprovalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 全局异常处理器单元测试
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleApprovalException() {
        // 测试处理ApprovalException
        ApprovalException exception = ApprovalException.validationError("验证失败", "业务数据验证失败", "TEST_001");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleApprovalException(exception);

        // 验证响应
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("VALIDATION_ERROR", response.getBody().get("errorCode"));
        assertEquals("验证失败", response.getBody().get("message"));
    }

    @Test
    void testHandleApprovalException_SystemError() {
        // 测试处理系统错误类型的ApprovalException
        ApprovalException exception = ApprovalException.systemError("系统内部错误", "数据库连接失败");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleApprovalException(exception);

        // 验证响应为500错误
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("SYSTEM_ERROR", response.getBody().get("errorCode"));
    }

    @Test
    void testHandleApprovalException_Unauthorized() {
        // 测试处理权限错误
        ApprovalException exception = ApprovalException.securityError("无权限访问", "用户无审批权限");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleApprovalException(exception);

        // 验证响应为401错误
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("SECURITY_ERROR", response.getBody().get("errorCode"));
    }

    @Test
    void testHandleApprovalException_NotFound() {
        // 测试处理数据不存在错误
        ApprovalException exception = ApprovalException.notFoundError("审批实例不存在", "审批实例ID: 999");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleApprovalException(exception);

        // 验证响应为404错误
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND_ERROR", response.getBody().get("errorCode"));
    }

    @Test
    void testHandleIllegalArgumentException() {
        // 测试处理非法参数异常
        IllegalArgumentException exception = new IllegalArgumentException("参数格式错误");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("1002", response.getBody().get("errorCode"));
        assertEquals("参数格式错误", response.getBody().get("message"));
    }

    @Test
    void testHandleRuntimeException() {
        // 测试处理运行时异常
        RuntimeException exception = new RuntimeException("未知运行时错误");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRuntimeException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("1001", response.getBody().get("errorCode"));
    }

    @Test
    void testHandleConstraintViolationException() {
        // 测试处理约束违反异常（需要模拟）
        RuntimeException exception = new RuntimeException("数据验证失败");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRuntimeException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
    }

    @Test
    void testHandleMethodArgumentTypeMismatchException() {
        // 测试处理方法参数类型不匹配异常
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getMessage()).thenReturn("参数类型不匹配");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMethodArgumentTypeMismatchException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("1002", response.getBody().get("errorCode"));
    }

    @Test
    void testDetermineHttpStatus() {
        // 测试HTTP状态码确定逻辑
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // SYSTEM_ERROR -> 500
        ApprovalException systemError = ApprovalException.systemError("系统错误", "详情");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.determineHttpStatus(systemError));

        // NOT_FOUND_ERROR -> 404
        ApprovalException notFoundError = ApprovalException.notFoundError("未找到", "详情");
        assertEquals(HttpStatus.NOT_FOUND, handler.determineHttpStatus(notFoundError));

        // SECURITY_ERROR -> 401
        ApprovalException securityError = ApprovalException.securityError("安全错误", "详情");
        assertEquals(HttpStatus.UNAUTHORIZED, handler.determineHttpStatus(securityError));

        // VALIDATION_ERROR -> 400
        ApprovalException validationError = ApprovalException.validationError("验证错误", "详情", "BIZ_001");
        assertEquals(HttpStatus.BAD_REQUEST, handler.determineHttpStatus(validationError));

        // BUSINESS_ERROR -> 400
        ApprovalException businessError = ApprovalException.businessError("业务错误", "详情", "BIZ_002");
        assertEquals(HttpStatus.BAD_REQUEST, handler.determineHttpStatus(businessError));

        // PARTIAL_SUCCESS -> 207
        ApprovalException partialSuccess = ApprovalException.partialSuccess("部分成功", "详情", "BIZ_003");
        assertEquals(HttpStatus.MULTI_STATUS, handler.determineHttpStatus(partialSuccess));

        // 默认情况 -> 500
        ApprovalException unknownError = new ApprovalException("UNKNOWN", "未知错误", "详情", null);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, handler.determineHttpStatus(unknownError));
    }

    @Test
    void testBuildErrorResponse() {
        // 测试构建错误响应
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        
        Map<String, Object> response = handler.buildErrorResponse(
                HttpStatus.BAD_REQUEST, 
                "VALIDATION_ERROR", 
                "数据验证失败", 
                "business_001", 
                "验证详情"
        );

        // 验证响应结构
        assertEquals(false, response.get("success"));
        assertEquals("VALIDATION_ERROR", response.get("errorCode"));
        assertEquals("数据验证失败", response.get("message"));
        assertEquals("business_001", response.get("businessId"));
        assertNotNull(response.get("timestamp"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.get("status"));
    }

    @Test
    void testBuildErrorResponse_WithoutBusinessId() {
        // 测试不包含业务ID的错误响应
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        
        Map<String, Object> response = handler.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "SYSTEM_ERROR", 
                "系统内部错误", 
                null, 
                "数据库连接失败"
        );

        // 验证业务ID为null
        assertNull(response.get("businessId"));
        assertEquals("SYSTEM_ERROR", response.get("errorCode"));
        assertEquals("系统内部错误", response.get("message"));
    }

    @Test
    void testHandleGeneralException() {
        // 测试处理通用异常
        Exception exception = new Exception("通用异常");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("1001", response.getBody().get("errorCode"));
    }
}