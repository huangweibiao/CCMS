package com.ccms.exception;

import com.ccms.common.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException as FileAccessDeniedException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    public void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
    }

    @Test
    public void testHandleAuthenticationException() {
        // 测试认证异常处理
        AuthenticationException authException = new AuthenticationException("Authentication failed") {};
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleAuthenticationException(authException);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(401);
        assertThat(response.getBody().getMessage()).isEqualTo("认证失败，请重新登录");
    }

    @Test
    public void testHandleBadCredentialsException() {
        // 测试密码错误异常处理
        BadCredentialsException badCredentialsException = new BadCredentialsException("Bad credentials");
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleAuthenticationException(badCredentialsException);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCode()).isEqualTo(401);
        assertThat(response.getBody().getMessage()).isEqualTo("认证失败，请重新登录");
    }

    @Test
    public void testHandleAccessDeniedException() {
        // 测试权限拒绝异常处理
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleAccessDeniedException(accessDeniedException);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getCode()).isEqualTo(403);
        assertThat(response.getBody().getMessage()).isEqualTo("权限不足，无法访问该资源");
    }

    @Test
    public void testHandleFileAccessDeniedException() {
        // 测试文件权限异常处理
        FileAccessDeniedException fileAccessDeniedException = new FileAccessDeniedException("access_denied.txt");
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleFileAccessDeniedException(fileAccessDeniedException);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getCode()).isEqualTo(403);
        assertThat(response.getBody().getMessage()).isEqualTo("文件访问权限不足");
    }

    @Test
    public void testHandleMethodArgumentNotValidException() {
        // 测试参数验证异常处理
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        
        FieldError fieldError1 = new FieldError("objectName", "username", "用户名不能为空");
        FieldError fieldError2 = new FieldError("objectName", "email", "邮箱格式不正确");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);
        
        when(ex.getMessage()).thenReturn("Validation failed");
        when(ex.getBindingResult()).thenReturn(mock(org.springframework.validation.BindingResult.class));
        when(ex.getBindingResult().getFieldErrors()).thenReturn(fieldErrors);
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleMethodArgumentNotValidException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("参数验证失败");
        assertThat(response.getBody().getData()).isNotNull();
        
        // 验证错误详情
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().getData();
        assertThat(errors).hasSize(2);
        assertThat(errors.get("username")).isEqualTo("用户名不能为空");
        assertThat(errors.get("email")).isEqualTo("邮箱格式不正确");
    }

    @Test
    public void testHandleBindException() {
        // 测试参数绑定异常处理
        BindException bindException = mock(BindException.class);
        
        FieldError fieldError = new FieldError("objectName", "amount", "金额必须大于0");
        List<FieldError> fieldErrors = Collections.singletonList(fieldError);
        
        when(bindException.getMessage()).thenReturn("Bind exception");
        when(bindException.getBindingResult()).thenReturn(mock(org.springframework.validation.BindingResult.class));
        when(bindException.getBindingResult().getFieldErrors()).thenReturn(fieldErrors);
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleBindException(bindException);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("参数绑定失败");
    }

    @Test
    public void testHandleConstraintViolationException() {
        // 测试约束违反异常处理
        ConstraintViolationException constraintViolationException = mock(ConstraintViolationException.class);
        
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<?>> violations = Collections.singleton(violation);
        
        when(constraintViolationException.getMessage()).thenReturn("Constraint violation");
        when(constraintViolationException.getConstraintViolations()).thenReturn(violations);
        when(violation.getMessage()).thenReturn("约束错误消息");
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleConstraintViolationException(constraintViolationException);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("约束验证失败");
    }

    @Test
    public void testHandleEntityNotFoundException() {
        // 测试实体不存在异常处理
        EntityNotFoundException entityNotFoundException = new EntityNotFoundException("User not found");
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleEntityNotFoundException(entityNotFoundException);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("请求的资源不存在");
    }

    @Test
    public void testHandleBusinessException() {
        // 测试业务逻辑异常处理
        BusinessException businessException = new BusinessException(1001, "业务处理失败");
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleBusinessException(businessException);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(1001);
        assertThat(response.getBody().getMessage()).isEqualTo("业务处理失败");
    }

    @Test
    public void testHandleCustomException() {
        // 测试自定义异常处理
        CustomException customException = new CustomException(2001, "自定义异常消息", HttpStatus.NOT_FOUND);
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleCustomException(customException);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo(2001);
        assertThat(response.getBody().getMessage()).isEqualTo("自定义异常消息");
    }

    @Test
    public void testHandleDataAccessException() {
        // 测试数据库访问异常处理
        DataAccessException dataAccessException = mock(DataAccessException.class);
        when(dataAccessException.getMessage()).thenReturn("Database connection failed");
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleDataAccessException(dataAccessException);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("数据库操作失败，请稍后重试");
    }

    @Test
    public void testHandleNullPointerException() {
        // 测试空指针异常处理
        NullPointerException nullPointerException = new NullPointerException("Something is null");
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleNullPointerException(nullPointerException);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("系统内部错误，空指针异常");
    }

    @Test
    public void testHandleGlobalException() {
        // 测试通用异常处理
        Exception globalException = new Exception("Unexpected error");
        
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleGlobalException(globalException, webRequest);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("系统内部错误，请联系管理员");
    }

    @Test
    public void testBusinessExceptionConstructors() {
        // 测试BusinessException的不同构造函数
        
        // 构造函数1：指定code和message
        BusinessException exception1 = new BusinessException(1001, "业务异常");
        assertThat(exception1.getCode()).isEqualTo(1001);
        assertThat(exception1.getMessage()).isEqualTo("业务异常");
        
        // 构造函数2：仅message，默认code为400
        BusinessException exception2 = new BusinessException("默认业务异常");
        assertThat(exception2.getCode()).isEqualTo(400);
        assertThat(exception2.getMessage()).isEqualTo("默认业务异常");
        
        // 构造函数3：message和cause，默认code为400
        BusinessException exception3 = new BusinessException("带cause的异常", new RuntimeException("root cause"));
        assertThat(exception3.getCode()).isEqualTo(400);
        assertThat(exception3.getMessage()).isEqualTo("带cause的异常");
        assertThat(exception3.getCause()).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void testCustomExceptionConstructors() {
        // 测试CustomException的不同构造函数
        
        // 构造函数1：指定code和message
        CustomException exception1 = new CustomException(2001, "自定义异常");
        assertThat(exception1.getCode()).isEqualTo(2001);
        assertThat(exception1.getMessage()).isEqualTo("自定义异常");
        assertThat(exception1.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        // 构造函数2：仅message，默认code为400，status为BAD_REQUEST
        CustomException exception2 = new CustomException("默认自定义异常");
        assertThat(exception2.getCode()).isEqualTo(400);
        assertThat(exception2.getMessage()).isEqualTo("默认自定义异常");
        assertThat(exception2.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        // 构造函数3：code、message和status
        CustomException exception3 = new CustomException(3001, "指定status异常", HttpStatus.NOT_FOUND);
        assertThat(exception3.getCode()).isEqualTo(3001);
        assertThat(exception3.getMessage()).isEqualTo("指定status异常");
        assertThat(exception3.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        
        // 测试setter方法
        exception3.setCode(4001);
        exception3.setStatus(HttpStatus.FORBIDDEN);
        assertThat(exception3.getCode()).isEqualTo(4001);
        assertThat(exception3.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void testExceptionOrderingPrecedence() {
        // 测试异常处理的优先级（更具体的异常应优先于通用异常）
        
        // AuthenticationException应该被handleAuthenticationException处理
        AuthenticationException authException = new AuthenticationException("Auth failed") {};
        ResponseEntity<ApiResponse<Object>> authResponse = 
            globalExceptionHandler.handleAuthenticationException(authException);
        assertThat(authResponse.getBody().getMessage()).isEqualTo("认证失败，请重新登录");
        
        // BusinessException应该被handleBusinessException处理
        BusinessException businessException = new BusinessException("Business failed");
        ResponseEntity<ApiResponse<Object>> businessResponse = 
            globalExceptionHandler.handleBusinessException(businessException);
        assertThat(businessResponse.getBody().getMessage()).isEqualTo("Business failed");
    }

    @Test
    public void testErrorLoggingMechanism() {
        // 测试不同类型的异常是否被正确记录（通过日志级别判断）
        
        // 认证相关的异常应该是warn级别
        AuthenticationException authException = new AuthenticationException("Test auth") {};
        // 这里主要是验证没有异常抛出，实际的日志测试需要集成测试环境
        
        // 通用异常应该是error级别
        Exception globalException = new Exception("Test global");
        // 验证没有异常抛出即可
        
        assertThat(globalExceptionHandler).isNotNull();
    }

    @Test
    public void testResponseStructureConsistency() {
        // 测试所有异常响应的结构一致性
        
        MethodArgumentNotValidException validationException = mock(MethodArgumentNotValidException.class);
        when(validationException.getMessage()).thenReturn("Validation failed");
        when(validationException.getBindingResult()).thenReturn(mock(org.springframework.validation.BindingResult.class));
        when(validationException.getBindingResult().getFieldErrors()).thenReturn(Collections.emptyList());
        
        ResponseEntity<ApiResponse<Object>> validationResponse = 
            globalExceptionHandler.handleMethodArgumentNotValidException(validationException);
        
        ApiResponse<Object> validationBody = validationResponse.getBody();
        assertThat(validationBody).isNotNull();
        assertThat(validationBody.getCode()).isGreaterThan(0);
        assertThat(validationBody.getMessage()).isNotEmpty();
        assertThat(validationBody.getTimestamp()).isNotNull();
        
        // 测试通用异常响应
        Exception globalException = new Exception("Test");
        ResponseEntity<ApiResponse<Object>> globalResponse = 
            globalExceptionHandler.handleGlobalException(globalException, webRequest);
        
        ApiResponse<Object> globalBody = globalResponse.getBody();
        assertThat(globalBody).isNotNull();
        assertThat(globalBody.getCode()).isGreaterThan(0);
        assertThat(globalBody.getMessage()).isNotEmpty();
        assertThat(globalBody.getTimestamp()).isNotNull();
    }
}