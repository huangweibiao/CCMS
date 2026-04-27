package com.ccms.controller;

import com.ccms.dto.LoginRequest;
import com.ccms.dto.LoginResponse;
import com.ccms.dto.ChangePasswordRequest;
import com.ccms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、登出、权限验证等认证相关API
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 用户登出
     * 
     * @param token 用户token
     * @return 登出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String token) {
        try {
            authService.logout(token);
            return ResponseEntity.ok(Map.of("message", "登出成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "登出失败"));
        }
    }

    /**
     * 修改密码
     * 
     * @param changePasswordRequest 修改密码请求
     * @param token 用户token
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody ChangePasswordRequest changePasswordRequest,
            @RequestHeader("Authorization") String token) {
        try {
            authService.changePassword(token, changePasswordRequest.getOldPassword(), 
                                     changePasswordRequest.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "密码修改成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "密码修改失败: " + e.getMessage()));
        }
    }

    /**
     * 验证token有效性
     * 
     * @param token 用户token
     * @return 验证结果
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
        try {
            boolean isValid = authService.validateToken(token);
            return ResponseEntity.ok(Map.of("valid", isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "token验证失败"));
        }
    }

    /**
     * 刷新token
     * 
     * @param token 旧token
     * @return 新token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            String newToken = authService.refreshToken(token);
            return ResponseEntity.ok(Map.of("token", newToken));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "token刷新失败: " + e.getMessage()));
        }
    }

    /**
     * 获取当前用户权限信息
     * 
     * @param token 用户token
     * @return 权限信息
     */
    @GetMapping("/permissions")
    public ResponseEntity<Map<String, Object>> getPermissions(@RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> permissions = authService.getUserPermissions(token);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取权限失败"));
        }
    }

    /**
     * 获取当前用户信息
     * 
     * @param token 用户token
     * @return 用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> profile = authService.getUserProfile(token);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "获取用户信息失败"));
        }
    }
}