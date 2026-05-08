package com.ccms.controller.system;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.system.user.User;
import com.ccms.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证授权控制器单元测试
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest extends ControllerTestBase {

    @MockBean
    private UserService userService;

    @Test
    void shouldLoginSuccess() throws Exception {
        // given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "zhangsan");
        loginRequest.put("password", "password123");

        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("token", "test-token");
        loginResult.put("username", "zhangsan");
        when(userService.login("zhangsan", "password123")).thenReturn(loginResult);

        // when & then
        performPost("/api/auth/login", loginRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"));
    }

    @Test
    void shouldReturnBadRequestWhenLoginWithEmptyCredentials() throws Exception {
        // given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "");
        loginRequest.put("password", "");

        // when & then
        performPost("/api/auth/login", loginRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginFailed() throws Exception {
        // given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "zhangsan");
        loginRequest.put("password", "wrongpassword");
        when(userService.login("zhangsan", "wrongpassword")).thenReturn(null);

        // when & then
        performPost("/api/auth/login", loginRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLogoutSuccess() throws Exception {
        // given
        doNothing().when(userService).logout(anyString());

        // when & then
        performPost("/api/auth/logout")
                .andExpect(status().isOk());
    }

    @Test
    void shouldRegisterSuccess() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("newuser");
        user.setName("新用户");
        user.setEmail("newuser@example.com");

        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("username", "newuser");
        registerRequest.put("password", "password123");
        registerRequest.put("name", "新用户");
        registerRequest.put("email", "newuser@example.com");
        registerRequest.put("phone", "13800138000");
        registerRequest.put("roleIds", Collections.singletonList(1L));

        when(userService.register(any(User.class), anyList())).thenReturn(user);

        // when & then
        performPost("/api/auth/register", registerRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void shouldChangePasswordSuccess() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("oldPassword", "oldpass123");
        request.put("newPassword", "newpass123");

        when(userService.changePassword(anyString(), eq("oldpass123"), eq("newpass123"))).thenReturn(true);

        // when & then
        performPost("/api/auth/change-password", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldRefreshTokenSuccess() throws Exception {
        // given
        when(userService.refreshToken(anyString())).thenReturn("new-token");

        // when & then
        performPost("/api/auth/refresh")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenFailed() throws Exception {
        // given
        when(userService.refreshToken(anyString())).thenThrow(new RuntimeException("Invalid token"));

        // when & then
        performPost("/api/auth/refresh")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetUserProfileSuccess() throws Exception {
        // given
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", "zhangsan");
        profile.put("name", "张三");
        when(userService.getUserProfileByToken(anyString())).thenReturn(profile);

        // when & then
        performGet("/api/auth/profile")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("zhangsan"));
    }

    @Test
    void shouldReturnUnauthorizedWhenGetProfileFailed() throws Exception {
        // given
        when(userService.getUserProfileByToken(anyString())).thenReturn(null);

        // when & then
        performGet("/api/auth/profile")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetUserPermissionsSuccess() throws Exception {
        // given
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("permissions", Arrays.asList("user:list", "user:create"));
        when(userService.getUserPermissions(anyString())).thenReturn(permissions);

        // when & then
        performGet("/api/auth/permissions")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions").isArray());
    }

    @Test
    void shouldCheckPermissionSuccess() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("permission", "user:create");
        when(userService.checkPermission(anyString(), eq("user:create"))).thenReturn(true);

        // when & then
        performPost("/api/auth/check-permission", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasPermission").value(true));
    }

    @Test
    void shouldValidateTokenSuccess() throws Exception {
        // given
        when(userService.validateToken(anyString())).thenReturn(true);

        // when & then
        performGet("/api/auth/validate")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void shouldValidateTokenReturnFalse() throws Exception {
        // given
        when(userService.validateToken(anyString())).thenReturn(false);

        // when & then
        performGet("/api/auth/validate")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }
}

