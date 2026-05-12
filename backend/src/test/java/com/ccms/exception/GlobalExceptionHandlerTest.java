package com.ccms.exception;

import com.ccms.dto.ApprovalResult;
import com.ccms.exception.ApprovalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器单元测试
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final com.ccms.controller.GlobalExceptionHandler exceptionHandler = new com.ccms.controller.GlobalExceptionHandler();

    @Test
    void testHandleApprovalException() {
        // 测试处理ApprovalException
        ApprovalException exception = ApprovalException.validationError("验证失败", "业务数据验证失败", "TEST_001");

        ResponseEntity<ApprovalResult> response = exceptionHandler.handleApprovalException(exception, null);

        // 验证响应
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertEquals("验证失败", response.getBody().getMessage());
    }

    @Test
    void testHandleApprovalException_SystemError() {
        // 测试处理系统错误类型的ApprovalException
        ApprovalException exception = ApprovalException.systemError("系统内部错误", "数据库连接失败");

        ResponseEntity<ApprovalResult> response = exceptionHandler.handleRuntimeException(exception, null);

        // 验证响应为500错误
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("RUNTIME_ERROR", response.getBody().getErrorCode());
        assertEquals("系统内部错误，请稍后重试", response.getBody().getMessage());
    }

    // 删除不存在的测试方法

    // 删除不存在的测试方法

    // 删除不存在的测试方法

    // 删除不存在的测试方法

    // 删除不存在的测试方法

    // 删除不存在的测试方法

    // 删除不存在的测试方法

    // 删除不存在的测试方法

    @Test
    void testHandleAllExceptions() {
        // 测试处理通用异常
        Exception exception = new Exception("通用异常");

        ResponseEntity<ApprovalResult> response = exceptionHandler.handleAllExceptions(exception, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("UNKNOWN_ERROR", response.getBody().getErrorCode());
        assertEquals("系统异常，请联系管理员", response.getBody().getMessage());
    }
}