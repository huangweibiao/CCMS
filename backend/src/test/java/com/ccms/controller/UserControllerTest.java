package com.ccms.controller;

import com.ccms.entity.system.SysUser;
import com.ccms.service.AuthService;
import com.ccms.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest extends BaseControllerTest {

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private SysUser testUser;
    private Page<SysUser> testUserPage;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createSysUser();
        List<SysUser> users = List.of(
                TestDataFactory.createSysUser(),
                TestDataFactory.createSysUser(),
                TestDataFactory.createSysUser()
        );
        testUserPage = new PageImpl<>(users, PageRequest.of(0, 20), users.size());
    }

    @Test
    void shouldReturnUserList_whenGetUserListSuccess() throws Exception {
        // Given
        when(authService.checkPermission(anyString(), eq("user:list"))).thenReturn(true);
        when(authService.getUserList(0, 20, null, null)).thenReturn(testUserPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/users")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3));

        verify(authService, times(1)).checkPermission(anyString(), eq("user:list"));
        verify(authService, times(1)).getUserList(0, 20, null, null);
    }

    @Test
    void shouldReturnUserListWithFilters_whenGetUserListWithParameters() throws Exception {
        // Given
        when(authService.checkPermission(anyString(), eq("user:list"))).thenReturn(true);
        when(authService.getUserList(0, 10, "testuser", 123L)).thenReturn(testUserPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10")
                .param("username", "testuser")
                .param("deptId", "123")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk());

        verify(authService, times(1)).getUserList(0, 10, "testuser", 123L);
    }

    @Test
    void shouldReturnBadRequest_whenGetUserListAuthorizationFails() throws Exception {
        // Given
        when(authService.checkPermission(anyString(), eq("user:list")))
                .thenThrow(new RuntimeException("权限验证失败"));

        // When
        ResultActions result = mockMvc.perform(get("/api/users")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUserDetail_whenGetUserDetailSuccess() throws Exception {
        // Given
        Long userId = 1L;
        when(authService.checkPermission(anyString(), eq("user:view"))).thenReturn(true);
        when(authService.getUserById(userId)).thenReturn(testUser);

        // When
        ResultActions result = mockMvc.perform(get("/api/users/{userId}", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()));

        verify(authService, times(1)).getUserById(userId);
    }

    @Test
    void shouldReturnSuccess_whenCreateUserSuccess() throws Exception {
        // Given
        when(authService.checkPermission(anyString(), eq("user:create"))).thenReturn(true);
        doNothing().when(authService).createUser(any(SysUser.class));

        // When
        ResultActions result = mockMvc.perform(post("/api/users")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户创建成功"));

        verify(authService, times(1)).createUser(any(SysUser.class));
    }

    @Test
    void shouldReturnError_whenCreateUserFails() throws Exception {
        // Given
        when(authService.checkPermission(anyString(), eq("user:create"))).thenReturn(true);
        doThrow(new RuntimeException("创建失败")).when(authService).createUser(any(SysUser.class));

        // When
        ResultActions result = mockMvc.perform(post("/api/users")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("用户创建失败: 创建失败"));
    }

    @Test
    void shouldReturnSuccess_whenUpdateUserSuccess() throws Exception {
        // Given
        Long userId = 1L;
        when(authService.checkPermission(anyString(), eq("user:update"))).thenReturn(true);
        doNothing().when(authService).updateUser(any(SysUser.class));

        // When
        ResultActions result = mockMvc.perform(put("/api/users/{userId}", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户更新成功"));

        verify(authService, times(1)).updateUser(any(SysUser.class));
    }

    @Test
    void shouldReturnError_whenUpdateUserFails() throws Exception {
        // Given
        Long userId = 1L;
        when(authService.checkPermission(anyString(), eq("user:update"))).thenReturn(true);
        doThrow(new RuntimeException("更新失败")).when(authService).updateUser(any(SysUser.class));

        // When
        ResultActions result = mockMvc.perform(put("/api/users/{userId}", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("用户更新失败: 更新失败"));
    }

    @Test
    void shouldReturnSuccess_whenDeleteUserSuccess() throws Exception {
        // Given
        Long userId = 1L;
        when(authService.checkPermission(anyString(), eq("user:delete"))).thenReturn(true);
        doNothing().when(authService).deleteUser(userId);

        // When
        ResultActions result = mockMvc.perform(delete("/api/users/{userId}", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户删除成功"));

        verify(authService, times(1)).deleteUser(userId);
    }

    @Test
    void shouldReturnError_whenDeleteUserFails() throws Exception {
        // Given
        Long userId = 1L;
        when(authService.checkPermission(anyString(), eq("user:delete"))).thenReturn(true);
        doThrow(new RuntimeException("删除失败")).when(authService).deleteUser(userId);

        // When
        ResultActions result = mockMvc.perform(delete("/api/users/{userId}", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("用户删除失败: 删除失败"));
    }

    @Test
    void shouldReturnSuccess_whenResetPasswordSuccess() throws Exception {
        // Given
        Long userId = 1L;
        when(authService.checkPermission(anyString(), eq("user:reset-password"))).thenReturn(true);
        doNothing().when(authService).resetPassword(userId);

        // When
        ResultActions result = mockMvc.perform(post("/api/users/{userId}/reset-password", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("密码重置成功"));

        verify(authService, times(1)).resetPassword(userId);
    }

    @Test
    void shouldReturnError_whenResetPasswordFails() throws Exception {
        // Given
        Long userId = 1L;
        when(authService.checkPermission(anyString(), eq("user:reset-password"))).thenReturn(true);
        doThrow(new RuntimeException("重置失败")).when(authService).resetPassword(userId);

        // When
        ResultActions result = mockMvc.perform(post("/api/users/{userId}/reset-password", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("密码重置失败: 重置失败"));
    }

    @Test
    void shouldReturnSuccess_whenUpdateUserStatusSuccess() throws Exception {
        // Given
        Long userId = 1L;
        Map<String, Integer> status = Map.of("status", 1);

        when(authService.checkPermission(anyString(), eq("user:update"))).thenReturn(true);
        doNothing().when(authService).updateUserStatus(userId, 1);

        // When
        ResultActions result = mockMvc.perform(put("/api/users/{userId}/status", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(status)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户状态更新成功"));

        verify(authService, times(1)).updateUserStatus(userId, 1);
    }

    @Test
    void shouldReturnSuccess_whenAssignRolesSuccess() throws Exception {
        // Given
        Long userId = 1L;
        Map<String, Long[]> roleIds = Map.of("roleIds", new Long[]{2L, 3L});

        when(authService.checkPermission(anyString(), eq("user:assign-roles"))).thenReturn(true);
        doNothing().when(authService).assignUserRoles(userId, new Long[]{2L, 3L});

        // When
        ResultActions result = mockMvc.perform(post("/api/users/{userId}/assign-roles", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleIds)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("角色分配成功"));

        verify(authService, times(1)).assignUserRoles(userId, new Long[]{2L, 3L});
    }

    @Test
    void shouldReturnError_whenAssignRolesFails() throws Exception {
        // Given
        Long userId = 1L;
        Map<String, Long[]> roleIds = Map.of("roleIds", new Long[]{2L, 3L});

        when(authService.checkPermission(anyString(), eq("user:assign-roles"))).thenReturn(true);
        doThrow(new RuntimeException("分配失败")).when(authService).assignUserRoles(userId, new Long[]{2L, 3L});

        // When
        ResultActions result = mockMvc.perform(post("/api/users/{userId}/assign-roles", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleIds)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("角色分配失败: 分配失败"));
    }

    @Test
    void shouldReturnStatistics_whenGetUserStatisticsSuccess() throws Exception {
        // Given
        Map<String, Object> statistics = Map.of(
                "totalUsers", 100,
                "activeUsers", 85,
                "inactiveUsers", 15
        );

        when(authService.checkPermission(anyString(), eq("user:statistics"))).thenReturn(true);
        when(authService.getUserStatistics(123L)).thenReturn(statistics);

        // When
        ResultActions result = mockMvc.perform(get("/api/users/statistics")
                .param("deptId", "123")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.activeUsers").value(85));

        verify(authService, times(1)).getUserStatistics(123L);
    }

    @Test
    void shouldReturnBadRequest_whenGetUserStatisticsFails() throws Exception {
        // Given
        when(authService.checkPermission(anyString(), eq("user:statistics"))).thenReturn(true);
        when(authService.getUserStatistics(123L)).thenThrow(new RuntimeException("统计失败"));

        // When
        ResultActions result = mockMvc.perform(get("/api/users/statistics")
                .param("deptId", "123")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isBadRequest());

        verify(authService, times(1)).getUserStatistics(123L);
    }

    @Test
    void shouldReturnBadRequest_whenUpdateUserStatusFails() throws Exception {
        // Given
        Long userId = 1L;
        Map<String, Integer> status = Map.of("status", 1);

        when(authService.checkPermission(anyString(), eq("user:update"))).thenReturn(true);
        doThrow(new RuntimeException("状态更新失败")).when(authService).updateUserStatus(userId, 1);

        // When
        ResultActions result = mockMvc.perform(put("/api/users/{userId}/status", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(status)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("用户状态更新失败: 状态更新失败"));
    }

    @Test
    void shouldReturnBadRequest_whenGetUserDetailFails() throws Exception {
        // Given
        Long userId = 1L;
        when(authService.checkPermission(anyString(), eq("user:view"))).thenReturn(true);
        when(authService.getUserById(userId)).thenThrow(new RuntimeException("用户不存在"));

        // When
        ResultActions result = mockMvc.perform(get("/api/users/{userId}", userId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isBadRequest());

        verify(authService, times(1)).getUserById(userId);
    }

    @Test
    void shouldHandleEmptyUserList_whenNoUsersFound() throws Exception {
        // Given
        Page<SysUser> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(authService.checkPermission(anyString(), eq("user:list"))).thenReturn(true);
        when(authService.getUserList(0, 20, "nonexistent", 999L)).thenReturn(emptyPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/users")
                .param("username", "nonexistent")
                .param("deptId", "999")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(authService, times(1)).getUserList(0, 20, "nonexistent", 999L);
    }
}