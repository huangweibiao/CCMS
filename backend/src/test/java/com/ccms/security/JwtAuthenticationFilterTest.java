package com.ccms.security;

import com.ccms.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    public void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(authService);
        // 清理SecurityContext
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testDoFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        // 准备测试数据
        String validJwt = "valid.jwt.token";
        String username = "testuser";
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // 设置mock行为
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
        when(authService.validateToken(validJwt)).thenReturn(true);
        when(authService.getUsernameFromToken(validJwt)).thenReturn(username);
        when(authService.loadUserByUsername(username)).thenReturn(userDetails);

        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证认证已设置
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();

        // 验证filterChain被调用
        verify(filterChain).doFilter(request, response);
        verify(authService).validateToken(validJwt);
        verify(authService).getUsernameFromToken(validJwt);
        verify(authService).loadUserByUsername(username);
    }

    @Test
    public void testDoFilterInternal_InvalidToken_HandleGracefully() throws ServletException, IOException {
        // 设置无效的token
        String invalidJwt = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidJwt);
        when(authService.validateToken(invalidJwt)).thenReturn(false);

        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证认证未设置
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // 验证filterChain被调用
        verify(filterChain).doFilter(request, response);
        verify(authService).validateToken(invalidJwt);
        verify(authService, never()).getUsernameFromToken(any());
        verify(authService, never()).loadUserByUsername(any());
    }

    @Test
    public void testDoFilterInternal_NoAuthorizationHeader_ProceedsWithoutAuthentication() throws ServletException, IOException {
        // 设置没有Authorization头
        when(request.getHeader("Authorization")).thenReturn(null);

        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证认证未设置
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // 验证filterChain被调用，没有token相关调用
        verify(filterChain).doFilter(request, response);
        verify(authService, never()).validateToken(any());
        verify(authService, never()).getUsernameFromToken(any());
        verify(authService, never()).loadUserByUsername(any());
    }

    @Test
    public void testDoFilterInternal_MalformedAuthorizationHeader_HandleGracefully() throws ServletException, IOException {
        // 设置格式错误的Authorization头
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证认证未设置
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // 验证filterChain被调用，没有token相关调用
        verify(filterChain).doFilter(request, response);
        verify(authService, never()).validateToken(any());
        verify(authService, never()).getUsernameFromToken(any());
        verify(authService, never()).loadUserByUsername(any());
    }

    @Test
    public void testDoFilterInternal_ValidTokenButNoUserDetails_HandleGracefully() throws ServletException, IOException {
        // 设置有效的token但没有用户详情
        String validJwt = "valid.jwt.token";
        String username = "nonexistentuser";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
        when(authService.validateToken(validJwt)).thenReturn(true);
        when(authService.getUsernameFromToken(validJwt)).thenReturn(username);
        when(authService.loadUserByUsername(username)).thenReturn(null); // 用户不存在

        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证认证未设置
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        // 验证filterChain被调用
        verify(filterChain).doFilter(request, response);
        verify(authService).validateToken(validJwt);
        verify(authService).getUsernameFromToken(validJwt);
        verify(authService).loadUserByUsername(username);
    }

    @Test
    public void testDoFilterInternal_ValidTokenAlreadyAuthenticated_OverwriteAuthentication() throws ServletException, IOException {
        // 设置已认证的上下文
        UserDetails previousUserDetails = User.builder()
                .username("previoususer")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        org.springframework.security.core.Authentication previousAuth = 
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        previousUserDetails, null, previousUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(previousAuth);

        // 新的有效token
        String validJwt = "valid.jwt.token";
        String username = "newuser";
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + validJwt);
        when(authService.validateToken(validJwt)).thenReturn(true);
        when(authService.getUsernameFromToken(validJwt)).thenReturn(username);
        when(authService.loadUserByUsername(username)).thenReturn(userDetails);

        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证认证已被更新
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);

        // 验证filterChain被调用
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testDoFilterInternal_TokenValidationException_HandleGracefully() throws ServletException, IOException {
        // 设置token验证时抛出异常
        String invalidJwt = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidJwt);
        when(authService.validateToken(invalidJwt)).thenThrow(new RuntimeException("Token validation failed"));

        // 执行过滤器（应处理异常并继续）
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证filterChain仍被调用
        verify(filterChain).doFilter(request, response);
        
        // 验证认证未设置
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void testGetJwtFromRequest_ValidBearerToken() {
        // 创建测试实例
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authService);
        
        // 伪造request对象获取jwt的逻辑
        String bearerToken = "Bearer test.jwt.token";
        String jwt = filter.getJwtFromRequest(new HttpServletRequest() {
            @Override
            public String getHeader(String name) {
                return "Authorization".equals(name) ? bearerToken : null;
            }
            // 省略其他接口方法的实现
            @Override public String getAuthType() { return null; }
            @Override public java.util.Enumeration<String> getHeaderNames() { return null; }
            // ... 其他接口方法
        });

        assertThat(jwt).isEqualTo("test.jwt.token");
    }

    @Test
    public void testGetJwtFromRequest_NoBearerPrefix() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authService);
        
        String tokenWithoutBearer = "Basic abc123";
        String jwt = filter.getJwtFromRequest(new HttpServletRequest() {
            @Override
            public String getHeader(String name) {
                return "Authorization".equals(name) ? tokenWithoutBearer : null;
            }
            @Override public String getAuthType() { return null; }
            @Override public java.util.Enumeration<String> getHeaderNames() { return null; }
            // ... 其他接口方法
        });

        assertThat(jwt).isNull();
    }

    @Test
    public void testGetJwtFromRequest_NullAuthorizationHeader() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authService);
        
        String jwt = filter.getJwtFromRequest(new HttpServletRequest() {
            @Override
            public String getHeader(String name) {
                return null;
            }
            @Override public String getAuthType() { return null; }
            @Override public java.util.Enumeration<String> getHeaderNames() { return null; }
            // ... 其他接口方法
        });

        assertThat(jwt).isNull();
    }

    @Test
    public void testGetJwtFromRequest_EmptyAuthorizationHeader() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authService);
        
        String jwt = filter.getJwtFromRequest(new HttpServletRequest() {
            @Override
            public String getHeader(String name) {
                return "";
            }
            @Override public String getAuthType() { return null; }
            @Override public java.util.Enumeration<String> getHeaderNames() { return null; }
            // ... 其他接口方法
        });

        assertThat(jwt).isNull();
    }
}