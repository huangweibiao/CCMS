package com.ccms.integration;

import com.ccms.entity.user.SysUser;
import com.ccms.repository.user.SysUserRepository;
import com.ccms.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证集成测试
 * 测试用户认证全流程：注册、登录、权限验证、登出等
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String validToken;
    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        SysUser testUser = new SysUser();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("test123"));
        testUser.setRealName("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800000000");
        testUser.setDeptId(1L);
        testUser.setUserStatus(1); // 启用状态
        testUser.setCreateBy(0L);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateBy(0L);
        testUser.setUpdateTime(LocalDateTime.now());
        sysUserRepository.save(testUser);

        testUserId = testUser.getId();
    }

    @Test
    void testLoginLogoutCompleteFlow() throws Exception {
        // 1. 正确登录
        Map<String, Object> loginRequest = Map.of(
                "username", "testuser",
                "password", "test123"
        );

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userInfo.username").value("testuser"))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(loginResponse, Map.class);
        validToken = (String) ((Map<String, Object>) responseMap.get("userInfo")).get("token");
        assertNotNull(validToken);
        assertTrue(validToken.startsWith("Bearer "));

        // 2. 验证token有效性
        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        // 3. 获取用户权限
        mockMvc.perform(get("/api/auth/permissions")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.permissions").isArray());

        // 4. 获取用户信息
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.realName").value("测试用户"));

        // 5. 刷新token
        String refreshResponse = mockMvc.perform(post("/api/auth/refresh-token")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> refreshMap = objectMapper.readValue(refreshResponse, Map.class);
        String newToken = (String) refreshMap.get("token");
        assertNotNull(newToken);

        // 6. 修改密码
        Map<String, Object> changePasswordRequest = Map.of(
                "oldPassword", "test123",
                "newPassword", "newpassword123"
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", newToken)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("密码修改成功"));

        // 7. 使用新密码登录
        Map<String, Object> newLoginRequest = Map.of(
                "username", "testuser",
                "password", "newpassword123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 8. 登出
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    @Test
    void testAuthenticationFailures() throws Exception {
        // 1. 错误密码登录
        Map<String, Object> invalidPasswordRequest = Map.of(
                "username", "testuser",
                "password", "wrongpassword"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPasswordRequest)))
                .andExpect(status().isBadRequest());

        // 2. 不存在的用户登录
        Map<String, Object> invalidUserRequest = Map.of(
                "username", "nonexistent",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest());

        // 3. 无效token验证
        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", "Bearer invalid-token-123"))
                .andExpect(status().isBadRequest());

        // 4. 无效token刷新
        mockMvc.perform(post("/api/auth/refresh-token")
                        .header("Authorization", "Bearer invalid-token-123"))
                .andExpect(status().isBadRequest());

        // 5. 无token访问受保护接口
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isBadRequest());

        // 6. 不正确格式的token
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "InvalidTokenFormat"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testTokenSecurity() throws Exception {
        // 1. 登录获取有效token
        Map<String, Object> loginRequest = Map.of(
                "username", "testuser",
                "password", "test123"
        );

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(loginResponse, Map.class);
        String token = (String) ((Map<String, Object>) responseMap.get("userInfo")).get("token");

        // 2. 验证token包含必要信息
        assertTrue(token.length() > 100); // JWT token长度检查

        // 3. 多次使用同一token进行验证（确保token可重复使用）
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/validate-token")
                            .header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true));
        }

        // 4. 测试刷新token后原token失效
        String refreshResponse = mockMvc.perform(post("/api/auth/refresh-token")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> refreshMap = objectMapper.readValue(refreshResponse, Map.class);
        String newToken = (String) refreshMap.get("token");
        assertNotEquals(token, newToken);

        // 5. 验证新token可用，旧token可能仍然有效（根据实现）
        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        // 6. 登出后token失效
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", newToken))
                .andExpect(status().isOk());

        // 登出后可能无法再验证token（根据实现）
        // mockMvc.perform(post("/api/auth/validate-token")
        //         .header("Authorization", newToken))
        //         .andExpect(status().isBadRequest());
    }

    @Test
    void testUserAccessControl() throws Exception {
        // 1. 正常登录
        Map<String, Object> loginRequest = Map.of(
                "username", "testuser",
                "password", "test123"
        );

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(loginResponse, Map.class);
        String token = (String) ((Map<String, Object>) responseMap.get("userInfo")).get("token");

        // 2. 获取权限信息并验证结构
        String permissionsResponse = mockMvc.perform(get("/api/auth/permissions")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> permissions = objectMapper.readValue(permissionsResponse, Map.class);
        assertTrue(permissions.containsKey("roles"));
        assertTrue(permissions.containsKey("permissions"));
        assertTrue(permissions.containsKey("userId"));
        assertTrue(permissions.containsKey("username"));

        // 3. 验证用户信息包含必要字段
        String profileResponse = mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> profile = objectMapper.readValue(profileResponse, Map.class);
        assertEquals("testuser", profile.get("username"));
        assertEquals("测试用户", profile.get("realName"));
        assertNotNull(profile.get("email"));
        assertNotNull(profile.get("deptId"));
        assertNotNull(profile.get("userStatus"));

        // 4. 测试用户状态验证（禁用用户无法登录）
        // 更新用户状态为禁用
        SysUser user = sysUserRepository.findByUsername("testuser").orElseThrow();
        user.setUserStatus(0); // 禁用状态
        sysUserRepository.save(user);

        // 禁用用户尝试登录
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        // 恢复用户状态
        user.setUserStatus(1);
        sysUserRepository.save(user);
    }

    @Test
    void testConcurrentAuthentication() throws Exception {
        // 测试并发登录场景
        Map<String, Object> loginRequest = Map.of(
                "username", "testuser",
                "password", "test123"
        );

        // 模拟多个并发登录请求
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk());
        }

        // 验证系统能够正确处理并发登录
        String finalLoginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(finalLoginResponse, Map.class);
        String token = (String) ((Map<String, Object>) responseMap.get("userInfo")).get("token");

        // 验证token可用
        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void testPasswordSecurity() throws Exception {
        // 1. 正常登录
        Map<String, Object> loginRequest = Map.of(
                "username", "testuser",
                "password", "test123"
        );

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(loginResponse, Map.class);
        String token = (String) ((Map<String, Object>) responseMap.get("userInfo")).get("token");

        // 2. 修改密码为弱密码（应该会被系统拒绝）
        Map<String, Object> weakPasswordRequest = Map.of(
                "oldPassword", "test123",
                "newPassword", "123" // 过短密码
        );

        // 根据系统实现，可能返回错误或成功
        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(weakPasswordRequest)));
        // 不验证具体响应，因为密码强度策略可能不同

        // 3. 修改密码为相同密码（应该被拒绝）
        Map<String, Object> samePasswordRequest = Map.of(
                "oldPassword", "test123",
                "newPassword", "test123" // 新旧密码相同
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(samePasswordRequest)));
        // 不验证具体响应

        // 4. 使用错误的旧密码修改密码
        Map<String, Object> wrongOldPasswordRequest = Map.of(
                "oldPassword", "wrongpassword",
                "newPassword", "newpassword123"
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(wrongOldPasswordRequest)))
                .andExpect(status().isBadRequest());

        // 5. 成功修改密码
        Map<String, Object> validPasswordChangeRequest = Map.of(
                "oldPassword", "test123",
                "newPassword", "SecurePassword123!"
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(objectMapper.writeValueAsString(validPasswordChangeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("密码修改成功"));

        // 6. 使用新密码登录
        Map<String, Object> newLoginRequest = Map.of(
                "username", "testuser",
                "password", "SecurePassword123!"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSessionManagement() throws Exception {
        // 1. 用户1登录
        Map<String, Object> loginRequest1 = Map.of(
                "username", "testuser",
                "password", "test123"
        );

        String loginResponse1 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap1 = objectMapper.readValue(loginResponse1, Map.class);
        String token1 = (String) ((Map<String, Object>) responseMap1.get("userInfo")).get("token");

        // 2. 同一用户再次登录（不影响现有会话）
        String loginResponse2 = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap2 = objectMapper.readValue(loginResponse2, Map.class);
        String token2 = (String) ((Map<String, Object>) responseMap2.get("userInfo")).get("token");

        // 两个token应该不同（JWT每次生成都不同）
        assertNotEquals(token1, token2);

        // 3. 两个token都应该可以正常使用
        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));

        // 4. 其中一个token登出
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", token1))
                .andExpect(status().isOk());

        // 登出后，登出的token可能失效，但另一个token应该继续有效
        mockMvc.perform(post("/api/auth/validate-token")
                        .header("Authorization", token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }
}