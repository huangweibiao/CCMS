package com.ccms.controller;

import com.ccms.BaseControllerTest;
import com.ccms.dto.LoginRequest;
import com.ccms.dto.LoginResponse;
import com.ccms.dto.ChangePasswordRequest;
import com.ccms.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController单元测试
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest extends BaseControllerTest {

    @MockBean
    private AuthService authService;

    @Test
    void shouldReturnLoginResponse_whenLoginSuccess() throws Exception {
        // Given
        String username = "testuser";
        String password = "password123";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        LoginResponse mockResponse = new LoginResponse();
        mockResponse.setAccessToken("test-access-token");
        mockResponse.setRefreshToken("test-refresh-token");
        mockResponse.setExpiresIn(3600L);

        when(authService.login(eq(username), eq(password))).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("test-refresh-token"));
    }

    @Test
    void shouldReturnBadRequest_whenLoginFails() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("invaliduser");
        loginRequest.setPassword("wrongpassword");

        when(authService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException("登录失败"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnSuccess_whenLogout() throws Exception {
        // Given
        String token = "Bearer test-token";
        doNothing().when(authService).logout(eq(token));

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    @Test
    void shouldReturnSuccess_whenChangePassword() throws Exception {
        // Given
        String token = "Bearer test-token";
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass123");
        request.setNewPassword("newPass456");

        doNothing().when(authService).changePassword(eq(token), eq("oldPass123"), eq("newPass456"));

        // When & Then
        mockMvc.perform(post("/api/auth/change-password")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("密码修改成功"));
    }

    @Test
    void shouldReturnValidStatus_whenValidateToken() throws Exception {
        // Given
        String token = "Bearer test-token";
        when(authService.validateToken(eq(token))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/auth/validate-token")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void shouldReturnNewToken_whenRefreshToken() throws Exception {
        // Given
        String token = "Bearer test-token";
        String newToken = "Bearer new-test-token";
        when(authService.refreshToken(eq(token))).thenReturn(newToken);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh-token")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(newToken));
    }

    @Test
    void shouldReturnPermissions_whenGetPermissions() throws Exception {
        // Given
        String token = "Bearer test-token";
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("roles", new String[]{"ADMIN", "USER"});
        permissions.put("permissions", new String[]{"read", "write"});

        when(authService.getUserPermissions(eq(token))).thenReturn(permissions);

        // When & Then
        mockMvc.perform(get("/api/auth/permissions")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.permissions[1]").value("write"));
    }

    @Test
    void shouldReturnProfile_whenGetProfile() throws Exception {
        // Given
        String token = "Bearer test-token";
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", "testuser");
        profile.put("email", "test@example.com");
        profile.put("department", "技术部");

        when(authService.getUserProfileByToken(eq(token))).thenReturn(profile);

        // When & Then
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.department").value("技术部"));
    }

    @Test
    void shouldHandleExceptions_whenServiceThrowsException() throws Exception {
        // Given
        String token = "Bearer test-token";
        when(authService.getUserProfileByToken(eq(token)))
                .thenThrow(new RuntimeException("服务异常"));

        // When & Then
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("获取用户信息失败"));
    }
}