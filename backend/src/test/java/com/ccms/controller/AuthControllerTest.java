package com.ccms.controller;

import com.ccms.entity.SysUser;
import com.ccms.service.impl.AuthServiceImpl;
import com.ccms.vo.ResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthServiceImpl authService;

    @InjectMocks
    private AuthController authController;

    private SysUser validUser;

    @BeforeEach
    void setUp() {
        // 创建有效的用户数据
        validUser = new SysUser();
        validUser.setId(1L);
        validUser.setUsername("testuser");
        validUser.setPassword("password123");
        validUser.setRealName("测试用户");
        validUser.setDeptId(101L);
        validUser.setStatus(1); // 启用状态
    }

    @Test
    void testLogin_Success() {
        // 模拟登录成功
        when(authService.login("testuser", "password123")).thenReturn(validUser);

        // 执行登录请求
        ResponseEntity<ResultVO<?>> response = authController.login("testuser", "password123");

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        
        verify(authService, times(1)).login("testuser", "password123");
    }

    @Test
    void testLogin_InvalidCredentials() {
        // 模拟登录失败
        when(authService.login("invaliduser", "wrongpassword")).thenReturn(null);

        // 执行登录请求
        ResponseEntity<ResultVO<?>> response = authController.login("invaliduser", "wrongpassword");

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户名或密码错误", response.getBody().getMessage());
        
        verify(authService, times(1)).login("invaliduser", "wrongpassword");
    }

    @Test
    void testLogin_NullUsername() {
        // 执行登录请求（用户名为空）
        ResponseEntity<ResultVO<?>> response = authController.login(null, "password123");

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户名不能为空", response.getBody().getMessage());
        
        verify(authService, never()).login(anyString(), anyString());
    }

    @Test
    void testLogin_EmptyPassword() {
        // 执行登录请求（密码为空）
        ResponseEntity<ResultVO<?>> response = authController.login("testuser", "");

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("密码不能为空", response.getBody().getMessage());
        
        verify(authService, never()).login(anyString(), anyString());
    }

    @Test
    void testGetCurrentUser_Success() {
        // 模拟获取当前用户成功
        when(authService.getUserById(1L)).thenReturn(validUser);

        // 执行获取当前用户请求
        ResponseEntity<ResultVO<?>> response = authController.getCurrentUser(1L);

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        
        verify(authService, times(1)).getUserById(1L);
    }

    @Test
    void testGetCurrentUser_UserNotFound() {
        // 模拟用户不存在
        when(authService.getUserById(999L)).thenReturn(null);

        // 执行获取当前用户请求
        ResponseEntity<ResultVO<?>> response = authController.getCurrentUser(999L);

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户不存在", response.getBody().getMessage());
        
        verify(authService, times(1)).getUserById(999L);
    }

    @Test
    void testChangePassword_Success() {
        // 模拟修改密码成功
        when(authService.changePassword(1L, "oldPassword", "newPassword")).thenReturn(true);

        // 执行修改密码请求
        ResponseEntity<ResultVO<?>> response = authController.changePassword(1L, "oldPassword", "newPassword");

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("密码修改成功", response.getBody().getMessage());
        
        verify(authService, times(1)).changePassword(1L, "oldPassword", "newPassword");
    }

    @Test
    void testChangePassword_Failed() {
        // 模拟修改密码失败
        when(authService.changePassword(1L, "wrongOldPassword", "newPassword")).thenReturn(false);

        // 执行修改密码请求
        ResponseEntity<ResultVO<?>> response = authController.changePassword(1L, "wrongOldPassword", "newPassword");

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("原密码错误或修改失败", response.getBody().getMessage());
        
        verify(authService, times(1)).changePassword(1L, "wrongOldPassword", "newPassword");
    }

    @Test
    void testChangePassword_InvalidNewPassword() {
        // 执行修改密码请求（新密码为空）
        ResponseEntity<ResultVO<?>> response = authController.changePassword(1L, "oldPassword", "");

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("新密码不能为空", response.getBody().getMessage());
        
        verify(authService, never()).changePassword(anyLong(), anyString(), anyString());
    }

    @Test
    void testResetPassword_Success() {
        // 模拟重置密码成功
        when(authService.resetPassword(1L, "newPassword")).thenReturn(true);

        // 执行重置密码请求
        ResponseEntity<ResultVO<?>> response = authController.resetPassword(1L, "newPassword");

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("密码重置成功", response.getBody().getMessage());
        
        verify(authService, times(1)).resetPassword(1L, "newPassword");
    }

    @Test
    void testResetPassword_Failed() {
        // 模拟重置密码失败
        when(authService.resetPassword(999L, "newPassword")).thenReturn(false);

        // 执行重置密码请求
        ResponseEntity<ResultVO<?>> response = authController.resetPassword(999L, "newPassword");

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("重置密码失败", response.getBody().getMessage());
        
        verify(authService, times(1)).resetPassword(999L, "newPassword");
    }

    @Test
    void testLogout_Success() {
        // 模拟登出成功
        when(authService.logout(1L)).thenReturn(true);

        // 执行登出请求
        ResponseEntity<ResultVO<?>> response = authController.logout(1L);

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("退出登录成功", response.getBody().getMessage());
        
        verify(authService, times(1)).logout(1L);
    }

    @Test
    void testRegister_Success() {
        // 模拟注册成功
        when(authService.register(any(SysUser.class))).thenReturn(validUser);

        // 准备注册数据
        SysUser registerUser = new SysUser();
        registerUser.setUsername("newuser");
        registerUser.setPassword("password123");
        registerUser.setRealName("新用户");
        registerUser.setDeptId(101L);

        // 执行注册请求
        ResponseEntity<ResultVO<?>> response = authController.register(registerUser);

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("注册成功", response.getBody().getMessage());
        
        verify(authService, times(1)).register(any(SysUser.class));
    }

    @Test
    void testRegister_AlreadyExists() {
        // 模拟用户名已存在
        when(authService.register(any(SysUser.class))).thenReturn(null);

        // 准备注册数据
        SysUser registerUser = new SysUser();
        registerUser.setUsername("existinguser");
        registerUser.setPassword("password123");

        // 执行注册请求
        ResponseEntity<ResultVO<?>> response = authController.register(registerUser);

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户名已存在", response.getBody().getMessage());
        
        verify(authService, times(1)).register(any(SysUser.class));
    }

    @Test
    void testRegister_InvalidUserData() {
        // 准备无效的注册数据（缺少用户名）
        SysUser invalidUser = new SysUser();
        invalidUser.setPassword("password123");

        // 执行注册请求
        ResponseEntity<ResultVO<?>> response = authController.register(invalidUser);

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户名和密码不能为空", response.getBody().getMessage());
        
        verify(authService, never()).register(any(SysUser.class));
    }

    @Test
    void testValidateToken_Success() {
        // 模拟Token验证成功
        when(authService.validateToken("validToken", 1L)).thenReturn(true);

        // 执行Token验证请求
        ResponseEntity<ResultVO<?>> response = authController.validateToken("validToken", 1L);

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Token验证成功", response.getBody().getMessage());
        
        verify(authService, times(1)).validateToken("validToken", 1L);
    }

    @Test
    void testValidateToken_Invalid() {
        // 模拟Token验证失败
        when(authService.validateToken("invalidToken", 1L)).thenReturn(false);

        // 执行Token验证请求
        ResponseEntity<ResultVO<?>> response = authController.validateToken("invalidToken", 1L);

        // 验证结果
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Token无效或已过期", response.getBody().getMessage());
        
        verify(authService, times(1)).validateToken("invalidToken", 1L);
    }
}