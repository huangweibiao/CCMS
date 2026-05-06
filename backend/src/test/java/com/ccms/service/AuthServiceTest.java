package com.ccms.service;

import com.ccms.entity.system.SysUser;
import com.ccms.repository.system.SysUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 */
class AuthServiceTest extends BaseServiceTest {

    @Mock
    private SysUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private SysUser testUser;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
    }

    @Test
    void shouldLoginSuccessfully_withValidCredentials() {
        // Given
        when(userRepository.findByUsername(eq("admin"))).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("password"), anyString())).thenReturn(true);

        // When
        Object result = authService.login("admin", "password");

        // Then
        assertNotNull(result);
    }

    @Test
    void shouldFailLogin_withInvalidUsername() {
        // Given
        when(userRepository.findByUsername(eq("invalid"))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.login("invalid", "password"));
    }

    @Test
    void shouldFailLogin_withInvalidPassword() {
        // Given
        when(userRepository.findByUsername(eq("admin"))).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> authService.login("admin", "wrongpassword"));
    }

    @Test
    void shouldLogoutSuccessfully() {
        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> authService.logout("token123"));
    }

    @Test
    void shouldCheckPermissionSuccessfully() {
        // Given
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));

        // When
        boolean hasPermission = authService.hasPermission(1L, "user:view");

        // Then
        assertTrue(hasPermission);
    }

    @Test
    void shouldGetUserRolesSuccessfully() {
        // Given
        List<String> roles = Arrays.asList("ROLE_ADMIN", "ROLE_USER");
        when(userRepository.findRolesByUserId(eq(1L))).thenReturn(roles);

        // When
        List<String> result = authService.getUserRoles(1L);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("ROLE_ADMIN"));
    }

    @Test
    void shouldGenerateTokenSuccessfully() {
        // When
        String token = authService.generateToken(testUser);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // Given
        String token = authService.generateToken(testUser);

        // When
        boolean isValid = authService.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldGetUserByTokenSuccessfully() {
        // Given
        String token = authService.generateToken(testUser);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // When
        SysUser result = authService.getUserByToken(token);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        // Given
        String token = authService.generateToken(testUser);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(eq("oldPassword"), anyString())).thenReturn(true);
        when(passwordEncoder.encode(eq("newPassword"))).thenReturn("encodedNewPassword");
        when(userRepository.save(any(SysUser.class))).thenReturn(testUser);

        // When
        boolean result = authService.changePassword(token, "oldPassword", "newPassword");

        // Then
        assertTrue(result);
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        // Given
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(SysUser.class))).thenReturn(testUser);

        // When
        String newPassword = authService.resetPassword(1L);

        // Then
        assertNotNull(newPassword);
        assertFalse(newPassword.isEmpty());
    }

    @Test
    void shouldCheckUsernameExists() {
        // Given
        when(userRepository.existsByUsername(eq("existing"))).thenReturn(true);
        when(userRepository.existsByUsername(eq("newuser"))).thenReturn(false);

        // When & Then
        assertTrue(authService.isUsernameExist("existing"));
        assertFalse(authService.isUsernameExist("newuser"));
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        SysUser newUser = createTestUser();
        newUser.setId(null);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(SysUser.class))).thenReturn(testUser);

        // When
        SysUser result = authService.register(newUser, Arrays.asList(1L, 2L));

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
    }

    @Test
    void shouldGetUserListSuccessfully() {
        // Given
        Page Page<SysUser> page = new PageImpl<>(Arrays.asList(testUser), PageRequest.of(0, 10), 1);
        when(userRepository.findByUsernameContainingAndDeptId(anyString(), any(), any(PageRequest.class))).thenReturn(page);

        // When
        Page Page<SysUser> result = authService.getUserList(0, 10, "admin", null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));

        // When
        SysUser result = authService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
    }

    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        SysUser newUser = createTestUser();
        newUser.setId(null);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(SysUser.class))).thenReturn(testUser);

        // When
        SysUser result = authService.createUser(newUser);

        // Then
        assertNotNull(result);
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        // Given
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(SysUser.class))).thenReturn(testUser);

        // When
        SysUser result = authService.updateUser(testUser);

        // Then
        assertNotNull(result);
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userRepository.existsById(eq(1L))).thenReturn(true);
        doNothing().when(userRepository).deleteById(eq(1L));

        // When
        boolean result = authService.deleteUser(1L);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldUpdateUserStatusSuccessfully() {
        // Given
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(SysUser.class))).thenReturn(testUser);

        // When
        boolean result = authService.updateUserStatus(1L, 0);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldAssignUserRolesSuccessfully() {
        // Given
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));

        // When
        boolean result = authService.assignUserRoles(1L, new Long[]{1L, 2L});

        // Then
        assertTrue(result);
    }

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        // Given
        when(userRepository.findByUsername(eq("admin"))).thenReturn(Optional.of(testUser));

        // When
        SysUser result = authService.loadUserByUsername("admin");

        // Then
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        // Given
        String oldToken = authService.generateToken(testUser);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // When
        String newToken = authService.refreshToken(oldToken);

        // Then
        assertNotNull(newToken);
        assertFalse(newToken.isEmpty());
    }

    private SysUser createTestUser() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encodedPassword");
        user.setRealName("管理员");
        user.setStatus(1);
        return user;
    }
}
