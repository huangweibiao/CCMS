package com.ccms.service;

import com.ccms.entity.system.SysUser;
import com.ccms.repository.system.SysUserRepository;
import com.ccms.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 用户认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SysUserRepository sysUserRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private PasswordEncoder passwordEncoder;
    private SysUser normalUser;
    private SysUser adminUser;
    private SysUser disabledUser;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        
        // 创建测试用户
        normalUser = new SysUser();
        normalUser.setId(1L);
        normalUser.setUsername("normaluser");
        normalUser.setPassword(passwordEncoder.encode("password123"));
        normalUser.setStatus(1);
        normalUser.setEmail("normal@example.com");
        normalUser.setRealName("Normal User");
        normalUser.setCreateTime(LocalDateTime.now());

        adminUser = new SysUser();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setStatus(1);
        adminUser.setEmail("admin@example.com");
        adminUser.setRealName("Admin User");
        adminUser.setCreateTime(LocalDateTime.now());

        disabledUser = new SysUser();
        disabledUser.setId(3L);
        disabledUser.setUsername("disabled");
        disabledUser.setPassword(passwordEncoder.encode("password123"));
        disabledUser.setStatus(0); // 禁用状态
        disabledUser.setEmail("disabled@example.com");
        disabledUser.setRealName("Disabled User");
        disabledUser.setCreateTime(LocalDateTime.now());
    }

    @Test
    void testLogin_Success() {
        // 模拟Repository返回正常用户
        when(sysUserRepository.findByUsername("normaluser")).thenReturn(Optional.of(normalUser));

        // 执行登录
        SysUser result = authService.login("normaluser", "password123");

        // 验证结果
        assertNotNull(result);
        assertEquals("normaluser", result.getUsername());
        assertTrue(result.getLastLoginTime().isAfter(LocalDateTime.now().minusMinutes(1)));
        
        // 验证Repository调用
        verify(sysUserRepository, times(1)).findByUsername("normaluser");
    }

    @Test
    void testLogin_UserNotFound() {
        // 模拟Repository返回空
        when(sysUserRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // 执行登录
        SysUser result = authService.login("nonexistent", "password123");

        // 验证结果
        assertNull(result);
        verify(sysUserRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void testLogin_WrongPassword() {
        // 模拟Repository返回正常用户
        when(sysUserRepository.findByUsername("normaluser")).thenReturn(Optional.of(normalUser));

        // 执行登录（错误密码）
        SysUser result = authService.login("normaluser", "wrongpassword");

        // 验证结果
        assertNull(result);
        verify(sysUserRepository, times(1)).findByUsername("normaluser");
    }

    @Test
    void testLogin_UserDisabled() {
        // 模拟Repository返回禁用用户
        when(sysUserRepository.findByUsername("disabled")).thenReturn(Optional.of(disabledUser));

        // 执行登录
        SysUser result = authService.login("disabled", "password123");

        // 验证结果
        assertNull(result);
        verify(sysUserRepository, times(1)).findByUsername("disabled");
    }

    @Test
    void testLogout_Success() {
        // 准备测试数据
        String token = authService.generateToken(normalUser);
        assertTrue(authService.validateToken(token));

        // 执行登出
        authService.logout(token);

        // 验证令牌已失效
        assertFalse(authService.validateToken(token));
    }

    @Test
    void testHasPermission_AdminUser() {
        // 模拟Repository返回管理员用户
        when(sysUserRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        // 验证管理员权限
        boolean result = authService.hasPermission(2L, "ANY_PERMISSION");

        // 验证结果
        assertTrue(result);
        verify(sysUserRepository, times(1)).findById(2L);
    }

    @Test
    void testHasPermission_NormalUserRead() {
        // 模拟Repository返回普通用户
        when(sysUserRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        // 验证读取权限
        boolean result = authService.hasPermission(1L, "READ_DATA");

        // 验证结果
        assertTrue(result);
        verify(sysUserRepository, times(1)).findById(1L);
    }

    @Test
    void testHasPermission_NormalUserExpense() {
        // 模拟Repository返回普通用户
        when(sysUserRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        // 验证费用申请权限
        boolean result = authService.hasPermission(1L, "EXPENSE_APPLY");

        // 验证结果
        assertTrue(result);
        verify(sysUserRepository, times(1)).findById(1L);
    }

    @Test
    void testHasPermission_UserNotFound() {
        // 模拟Repository返回空
        when(sysUserRepository.findById(999L)).thenReturn(Optional.empty());

        // 验证权限检查
        boolean result = authService.hasPermission(999L, "READ_DATA");

        // 验证结果
        assertFalse(result);
        verify(sysUserRepository, times(1)).findById(999L);
    }

    @Test
    void testGetUserRoles_Admin() {
        // 模拟Repository返回管理员用户
        when(sysUserRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        // 获取管理员角色
        var roles = authService.getUserRoles(2L);

        // 验证结果
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.contains("ADMIN"));
        verify(sysUserRepository, times(1)).findById(2L);
    }

    @Test
    void testGetUserRoles_NormalUser() {
        // 模拟Repository返回普通用户
        when(sysUserRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        // 获取普通用户角色
        var roles = authService.getUserRoles(1L);

        // 验证结果
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.contains("USER"));
        verify(sysUserRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserRoles_UserNotFound() {
        // 模拟Repository返回空
        when(sysUserRepository.findById(999L)).thenReturn(Optional.empty());

        // 获取角色
        var roles = authService.getUserRoles(999L);

        // 验证结果
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
        verify(sysUserRepository, times(1)).findById(999L);
    }

    @Test
    void testGenerateTokenAndValidation() {
        // 生成令牌
        String token = authService.generateToken(normalUser);

        // 验证令牌
        assertNotNull(token);
        assertTrue(authService.validateToken(token));
        
        // 通过令牌获取用户
        when(sysUserRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        SysUser user = authService.getUserByToken(token);
        
        assertNotNull(user);
        assertEquals(normalUser.getId(), user.getId());
        verify(sysUserRepository, times(1)).findById(1L);
    }

    @Test
    void testGetUserByToken_InvalidToken() {
        // 使用无效令牌
        SysUser user = authService.getUserByToken("invalid_token");

        // 验证结果
        assertNull(user);
        verify(sysUserRepository, never()).findById(any());
    }

    @Test
    void testChangePassword_Success() {
        // 模拟Repository返回用户
        when(sysUserRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(sysUserRepository.save(any(SysUser.class))).thenReturn(normalUser);

        // 执行修改密码
        boolean result = authService.changePassword(1L, "password123", "newpassword456");

        // 验证结果
        assertTrue(result);
        verify(sysUserRepository, times(1)).findById(1L);
        verify(sysUserRepository, times(1)).save(normalUser);
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        // 模拟Repository返回用户
        when(sysUserRepository.findById(1L)).thenReturn(Optional.of(normalUser));

        // 执行修改密码（错误旧密码）
        boolean result = authService.changePassword(1L, "wrongpassword", "newpassword456");

        // 验证结果
        assertFalse(result);
        verify(sysUserRepository, times(1)).findById(1L);
        verify(sysUserRepository, never()).save(any(SysUser.class));
    }

    @Test
    void testChangePassword_UserNotFound() {
        // 模拟Repository返回空
        when(sysUserRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行修改密码
        boolean result = authService.changePassword(999L, "password123", "newpassword456");

        // 验证结果
        assertFalse(result);
        verify(sysUserRepository, times(1)).findById(999L);
        verify(sysUserRepository, never()).save(any(SysUser.class));
    }

    @Test
    void testResetPassword_Success() {
        // 模拟Repository返回用户
        when(sysUserRepository.findById(1L)).thenReturn(Optional.of(normalUser));
        when(sysUserRepository.save(any(SysUser.class))).thenReturn(normalUser);

        // 执行重置密码
        String newPassword = authService.resetPassword(1L);

        // 验证结果
        assertNotNull(newPassword);
        assertEquals(8, newPassword.length());
        verify(sysUserRepository, times(1)).findById(1L);
        verify(sysUserRepository, times(1)).save(normalUser);
    }

    @Test
    void testResetPassword_UserNotFound() {
        // 模拟Repository返回空
        when(sysUserRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行重置密码
        String newPassword = authService.resetPassword(999L);

        // 验证结果
        assertNull(newPassword);
        verify(sysUserRepository, times(1)).findById(999L);
        verify(sysUserRepository, never()).save(any(SysUser.class));
    }

    @Test
    void testIsUsernameExist_True() {
        // 模拟Repository返回用户名存在
        when(sysUserRepository.existsByUsername("normaluser")).thenReturn(true);

        // 检查用户名是否存在
        boolean result = authService.isUsernameExist("normaluser");

        // 验证结果
        assertTrue(result);
        verify(sysUserRepository, times(1)).existsByUsername("normaluser");
    }

    @Test
    void testIsUsernameExist_False() {
        // 模拟Repository返回用户名不存在
        when(sysUserRepository.existsByUsername("nonexistent")).thenReturn(false);

        // 检查用户名是否存在
        boolean result = authService.isUsernameExist("nonexistent");

        // 验证结果
        assertFalse(result);
        verify(sysUserRepository, times(1)).existsByUsername("nonexistent");
    }

    @Test
    void testRegister_Success() {
        // 模拟Repository
        when(sysUserRepository.existsByUsername("newuser")).thenReturn(false);
        when(sysUserRepository.save(any(SysUser.class))).thenReturn(normalUser);

        // 创建新用户
        SysUser newUser = new SysUser();
        newUser.setUsername("newuser");
        newUser.setPassword("plainpassword");
        newUser.setEmail("new@example.com");
        newUser.setRealName("New User");

        // 执行注册
        SysUser result = authService.register(newUser, null);

        // 验证结果
        assertNotNull(result);
        assertEquals("normaluser", result.getUsername());
        assertNotNull(result.getPassword());
        assertNotEquals("plainpassword", result.getPassword()); // 密码应该被加密
        assertEquals(1, result.getStatus().intValue()); // 默认启用状态
        
        verify(sysUserRepository, times(1)).existsByUsername("newuser");
        verify(sysUserRepository, times(1)).save(any(SysUser.class));
    }

    @Test
    void testRegister_UsernameExists() {
        // 模拟Repository返回用户名已存在
        when(sysUserRepository.existsByUsername("existinguser")).thenReturn(true);

        // 创建用户
        SysUser newUser = new SysUser();
        newUser.setUsername("existinguser");

        // 验证异常
        assertThrows(RuntimeException.class, () -> {
            authService.register(newUser, null);
        });

        verify(sysUserRepository, times(1)).existsByUsername("existinguser");
        verify(sysUserRepository, never()).save(any(SysUser.class));
    }

    @Test
    void testRegister_WithDefaultPassword() {
        // 模拟Repository
        when(sysUserRepository.existsByUsername("newuser")).thenReturn(false);
        when(sysUserRepository.save(any(SysUser.class))).thenAnswer(invocation -> {
            SysUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // 创建用户（不设置密码）
        SysUser newUser = new SysUser();
        newUser.setUsername("newuser");

        // 执行注册
        SysUser result = authService.register(newUser, null);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getPassword());
        assertNotEquals("", result.getPassword()); // 应该有默认密码
        
        verify(sysUserRepository, times(1)).existsByUsername("newuser");
        verify(sysUserRepository, times(1)).save(any(SysUser.class));
    }
}