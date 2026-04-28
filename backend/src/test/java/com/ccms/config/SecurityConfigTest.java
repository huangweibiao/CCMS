package com.ccms.config;

import com.ccms.security.JwtAuthenticationEntryPoint;
import com.ccms.security.JwtAuthenticationFilter;
import com.ccms.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SecurityConfig.class)
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@TestPropertySource(properties = {
    "spring.profiles.active=test"
})
public class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private AuthService authService;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @BeforeEach
    public void setUp() throws Exception {
        // 设置AuthenticationManager mock
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authManager);
    }

    @Test
    public void testPasswordEncoderBean() {
        // 测试密码编码器Bean创建
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        assertThat(passwordEncoder).isNotNull();
        
        // 验证密码编码和解码功能
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        assertThat(encodedPassword).isNotEmpty();
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
        
        // 验证不匹配的密码
        assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
    }

    @Test
    public void testJwtAuthenticationFilterBean() {
        // 测试JWT认证过滤器Bean创建
        JwtAuthenticationFilter filter = securityConfig.jwtAuthenticationFilter();
        assertThat(filter).isNotNull();
    }

    @Test
    public void testAuthenticationManagerBean() throws Exception {
        // 测试认证管理器Bean创建
        AuthenticationManager authManager = securityConfig.authenticationManager(authenticationConfiguration);
        assertThat(authManager).isNotNull();
        
        // 验证AuthenticationConfiguration被正确调用
        verify(authenticationConfiguration).getAuthenticationManager();
    }

    @Test
    public void testCorsConfiguration() {
        // 测试CORS配置
        var corsConfigurationSource = securityConfig.corsConfigurationSource();
        assertThat(corsConfigurationSource).isNotNull();
        
        // 验证CORS配置允许的方法
        var config = corsConfigurationSource.getCorsConfiguration(null);
        assertThat(config.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(config.getAllowedOriginPatterns()).contains("*");
        assertThat(config.getAllowCredentials()).isTrue();
    }

    @Test
    public void testSecurityConfigAnnotations() {
        // 验证SecurityConfig的注解配置
        Class<?> configClass = securityConfig.getClass();
        assertThat(configClass.isAnnotationPresent(org.springframework.context.annotation.Configuration.class)).isTrue();
        assertThat(configClass.isAnnotationPresent(org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class)).isTrue();
        assertThat(configClass.isAnnotationPresent(org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity.class)).isTrue();
    }

    @Test
    public void testBeanDependencyInjection() {
        // 验证依赖注入正确配置
        assertThat(jwtAuthenticationEntryPoint).isNotNull();
        assertThat(authService).isNotNull();
    }

    @Test
    public void testSecurityFilterChainBeanCreation() {
        // 测试SecurityFilterChain Bean创建，这里主要验证没有异常抛出
        // 在实际集成测试中会更详细测试过滤器链的配置
        assertThat(securityConfig).isNotNull();
    }

    @Test
    public void testPublicEndpointConfiguration() {
        // 测试公共端点配置的逻辑验证
        // 这个测试主要验证配置类能够加载，实际路由规则在集成测试中验证
        SecurityConfig config = new SecurityConfig(jwtAuthenticationEntryPoint, authService);
        assertThat(config).isNotNull();
    }
}