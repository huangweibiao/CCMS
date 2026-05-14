package com.ccms.service;

import com.ccms.entity.system.user.User;
import com.ccms.entity.system.permission.Role;
import com.ccms.repository.system.user.UserRepository;
import com.ccms.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RBAC完整流程集成测试
 * 验证用户认证、权限验证、角色验证的完整流程
 */
class RBACIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试1：普通用户完整认证和权限验证流程
     */
    @Test
    void testNormalUserAuthenticationAndPermissionFlow() {
        // 1. 准备测试数据 - 普通用户
        User normalUser = new User();
        normalUser.setId(1L);
        normalUser.setUsername("normaluser");
        normalUser.setPassword("encodedPassword");
        normalUser.setEnabled(true);

        // 创建普通用户角色并设置权限
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleCode("USER");
        userRole.setRoleName("普通用户");
        userRole.setPermissions("user:read,user:update");
        
        // 设置普通用户角色
        normalUser.setRoles(new HashSet<>(Arrays.asList(userRole)));

        // 2. Mock repository 返回
        when(userRepository.findByUsername("normaluser")).thenReturn(Optional.of(normalUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // 3. 执行认证
        Optional<User> authenticatedUser = userRepository.findByUsername("normaluser")
                .filter(user -> passwordEncoder.matches("password123", user.getPassword()));

        // 4. 验证认证成功
        assertTrue(authenticatedUser.isPresent());
        assertEquals("normaluser", authenticatedUser.get().getUsername());

        // 5. 验证权限信息
        assertTrue(authenticatedUser.get().hasPermission("user:read"));
        assertTrue(authenticatedUser.get().hasPermission("user:update"));
        assertFalse(authenticatedUser.get().hasPermission("user:delete"));
        assertFalse(authenticatedUser.get().hasPermission("admin:access"));

        // 6. 验证角色信息
        assertTrue(authenticatedUser.get().hasRole("USER"));
        assertFalse(authenticatedUser.get().hasRole("ADMIN"));
        assertFalse(authenticatedUser.get().hasRole("SUPER_ADMIN"));

        // 7. 验证用户状态
        assertTrue(authenticatedUser.get().isEnabled());
    }

    /**
     * 测试2：管理员用户完整认证和权限验证流程
     */
    @Test
    void testAdminUserAuthenticationAndPermissionFlow() {
        // 1. 准备测试数据 - 管理员用户
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("adminuser");
        adminUser.setPassword("encodedAdminPassword");
        adminUser.setEnabled(true);

        // 创建管理员角色并设置权限
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setRoleCode("ADMIN");
        adminRole.setRoleName("管理员");
        adminRole.setPermissions("user:create,user:read,user:update,user:delete,admin:access");
        
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleCode("USER");
        userRole.setRoleName("普通用户");
        userRole.setPermissions("user:read,user:update");

        // 设置管理员角色
        adminUser.setRoles(new HashSet<>(Arrays.asList(adminRole, userRole)));

        // 2. Mock repository 返回
        when(userRepository.findByUsername("adminuser")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("admin123", "encodedAdminPassword")).thenReturn(true);

        // 3. 执行认证
        Optional<User> authenticatedUser = userRepository.findByUsername("adminuser")
                .filter(user -> passwordEncoder.matches("admin123", user.getPassword()));

        // 4. 验证认证成功
        assertTrue(authenticatedUser.isPresent());
        assertEquals("adminuser", authenticatedUser.get().getUsername());

        // 5. 验证管理员权限
        assertTrue(authenticatedUser.get().hasPermission("admin:access"));
        assertTrue(authenticatedUser.get().hasPermission("user:create"));
        assertTrue(authenticatedUser.get().hasPermission("user:delete"));

        // 6. 验证管理员角色
        assertTrue(authenticatedUser.get().hasRole("ADMIN"));
        assertTrue(authenticatedUser.get().hasRole("USER"));

        // 7. 验证管理员状态
        assertTrue(authenticatedUser.get().isEnabled());
    }

    /**
     * 测试3：超级管理员认证和权限验证流程
     */
    @Test
    void testSuperAdminAuthenticationAndPermissionFlow() {
        // 1. 准备测试数据 - 超级管理员
        User superAdminUser = new User();
        superAdminUser.setId(3L);
        superAdminUser.setUsername("superadmin");
        superAdminUser.setPassword("encodedSuperPassword");
        superAdminUser.setEnabled(true);

        // 创建超级管理员角色并设置所有权限
        Role superAdminRole = new Role();
        superAdminRole.setId(3L);
        superAdminRole.setRoleCode("SUPER_ADMIN");
        superAdminRole.setRoleName("超级管理员");
        superAdminRole.setPermissions("user:read,user:create,user:update,user:delete,admin:access,system:*");

        // 设置超级管理员角色（没有普通用户权限）
        superAdminUser.setRoles(new HashSet<>(Arrays.asList(superAdminRole)));

        // 2. Mock repository 返回
        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.of(superAdminUser));
        when(passwordEncoder.matches("super123", "encodedSuperPassword")).thenReturn(true);

        // 3. 执行认证
        Optional<User> authenticatedUser = userRepository.findByUsername("superadmin")
                .filter(user -> passwordEncoder.matches("super123", user.getPassword()));

        // 4. 验证认证成功
        assertTrue(authenticatedUser.isPresent());
        assertEquals("superadmin", authenticatedUser.get().getUsername());

        // 5. 验证超级管理员权限
        assertTrue(authenticatedUser.get().hasPermission("admin:access"));
        assertTrue(authenticatedUser.get().hasPermission("user:create"));
        assertTrue(authenticatedUser.get().hasPermission("system:*"));

        // 6. 验证超级管理员角色
        assertTrue(authenticatedUser.get().hasRole("SUPER_ADMIN"));
        assertFalse(authenticatedUser.get().hasRole("ADMIN")); // 只有超级管理员角色

        // 7. 验证超级管理员状态
        assertTrue(authenticatedUser.get().isEnabled());
    }

    /**
     * 测试4：权限动态更新验证
     */
    @Test
    void testDynamicPermissionUpdate() {
        // 1. 初始用户状态
        User user = new User();
        user.setId(4L);
        user.setUsername("testuser");
        
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleCode("USER");
        userRole.setRoleName("普通用户");
        userRole.setPermissions("user:read");
        
        user.setRoles(new HashSet<>(Arrays.asList(userRole)));

        // 2. 验证初始权限
        assertTrue(user.hasPermission("user:read"));
        assertFalse(user.hasPermission("user:write"));

        // 3. 模拟权限升级 - 添加新角色（编辑器）
        Role editorRole = new Role();
        editorRole.setId(4L);
        editorRole.setRoleCode("EDITOR");
        editorRole.setRoleName("编辑者");
        editorRole.setPermissions("user:write,user:delete");
        
        user.getRoles().add(editorRole);

        // 4. 验证升级后的权限
        assertTrue(user.hasPermission("user:write"));
        assertTrue(user.hasPermission("user:delete"));
        assertTrue(user.hasRole("EDITOR"));

        // 5. 模拟权限撤销
        user.getRoles().remove(editorRole);

        // 6. 验证撤销后的权限
        assertFalse(user.hasPermission("user:write"));
        assertFalse(user.hasRole("EDITOR"));
        assertTrue(user.hasPermission("user:read")); // 基础权限保持不变
    }

    /**
     * 测试5：用户状态验证（禁用用户）
     */
    @Test
    void testDisabledUserAuthentication() {
        // 1. 准备禁用用户
        User disabledUser = new User();
        disabledUser.setId(5L);
        disabledUser.setUsername("disableduser");
        disabledUser.setPassword("encodedPassword");
        disabledUser.setEnabled(false); // 用户被禁用
        
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleCode("USER");
        userRole.setRoleName("普通用户");
        userRole.setPermissions("user:read");
        
        disabledUser.setRoles(new HashSet<>(Arrays.asList(userRole)));

        // 2. Mock repository 返回
        when(userRepository.findByUsername("disableduser")).thenReturn(Optional.of(disabledUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // 3. 执行认证（应该失败，因为用户被禁用）
        Optional<User> authenticatedUser = userRepository.findByUsername("disableduser")
                .filter(user -> passwordEncoder.matches("password123", user.getPassword())
                        && user.isEnabled());

        // 4. 验证认证失败
        assertFalse(authenticatedUser.isPresent());
    }

    /**
     * 测试6：权限组合验证
     */
    @Test
    void testPermissionCombinationValidation() {
        // 1. 创建具有多种权限的用户
        User powerUser = new User();
        powerUser.setId(6L);
        powerUser.setUsername("poweruser");

        // 创建多个角色
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setRoleCode("USER");
        userRole.setRoleName("普通用户");
        userRole.setPermissions("user:read,user:write");
        
        Role reporterRole = new Role();
        reporterRole.setId(5L);
        reporterRole.setRoleCode("REPORTER");
        reporterRole.setRoleName("报告者");
        reporterRole.setPermissions("report:generate");
        
        Role monitorRole = new Role();
        monitorRole.setId(6L);
        monitorRole.setRoleCode("MONITOR");
        monitorRole.setRoleName("监控者");
        monitorRole.setPermissions("system:monitor");

        // 设置复杂权限组合
        powerUser.setRoles(new HashSet<>(Arrays.asList(userRole, reporterRole, monitorRole)));

        // 2. 验证单权限检查
        assertTrue(powerUser.hasPermission("user:read"));
        assertTrue(powerUser.hasPermission("report:generate"));

        // 3. 验证角色检查
        assertTrue(powerUser.hasRole("REPORTER"));
        assertTrue(powerUser.hasRole("MONITOR"));

        // 4. 验证不存在的权限和角色
        assertFalse(powerUser.hasPermission("admin:access"));
        assertFalse(powerUser.hasRole("ADMIN"));

        // 5. 验证权限和角色的共存
        assertTrue(powerUser.hasPermission("user:read") && powerUser.hasRole("USER"));
        assertTrue(powerUser.hasPermission("report:generate") && powerUser.hasRole("REPORTER"));
    }

    /**
     * 测试7：边界情况处理
     */
    @Test
    void testEdgeCaseHandling() {
        // 1. 创建空权限用户
        User emptyUser = new User();
        emptyUser.setId(7L);
        emptyUser.setUsername("emptyuser");
        emptyUser.setRoles(new HashSet<>());

        // 2. 验证空权限和空角色
        assertFalse(emptyUser.hasPermission("any:permission"));
        assertFalse(emptyUser.hasRole("any:role"));

        // 3. 验证null处理
        emptyUser.setRoles(null);

        assertFalse(emptyUser.hasPermission("test:permission"));
        assertFalse(emptyUser.hasRole("test:role"));

        // 4. 创建只有超级管理员角色的用户
        User superOnlyUser = new User();
        superOnlyUser.setId(8L);
        superOnlyUser.setUsername("superonly");
        
        Role superAdminRole = new Role();
        superAdminRole.setId(3L);
        superAdminRole.setRoleCode("SUPER_ADMIN");
        superAdminRole.setRoleName("超级管理员");
        superAdminRole.setPermissions("*"); // 超级管理员有所有权限
        
        superOnlyUser.setRoles(new HashSet<>(Arrays.asList(superAdminRole)));

        // 5. 验证超级管理员有所有权限
        assertTrue(superOnlyUser.hasPermission("any:permission")); // 超级管理员有所有权限
        assertTrue(superOnlyUser.hasRole("SUPER_ADMIN"));
    }
}