package com.ccms.controller.system;

import com.ccms.entity.system.user.User;
import com.ccms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证授权控制器
 * 对应设计文档：用户认证与权限相关接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("用户名和密码不能为空");
        }

        Object result = userService.login(username, password);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(401).body("登录失败：用户名或密码错误");
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        userService.logout(token);
        return ResponseEntity.ok().build();
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        User registeredUser = userService.register(user, request.getRoleIds());
        return ResponseEntity.ok(registeredUser);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        boolean success = userService.changePassword(token, oldPassword, newPassword);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
            @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            String newToken = userService.refreshToken(token);
            Map<String, String> result = new HashMap<>();
            result.put("token", newToken);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(
            @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Map<String, Object> profile = userService.getUserProfileByToken(token);
        if (profile != null) {
            return ResponseEntity.ok(profile);
        }
        return ResponseEntity.status(401).build();
    }

    /**
     * 获取用户权限列表
     */
    @GetMapping("/permissions")
    public ResponseEntity<Map<String, Object>> getUserPermissions(
            @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Map<String, Object> permissions = userService.getUserPermissions(token);
        return ResponseEntity.ok(permissions);
    }

    /**
     * 校验权限
     */
    @PostMapping("/check-permission")
    public ResponseEntity<Map<String, Boolean>> checkPermission(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String permission = request.get("permission");
        boolean hasPermission = userService.checkPermission(token, permission);
        Map<String, Boolean> result = new HashMap<>();
        result.put("hasPermission", hasPermission);
        return ResponseEntity.ok(result);
    }

    /**
     * 校验菜单权限
     */
    @PostMapping("/check-menu-permission")
    public ResponseEntity<Map<String, Boolean>> checkMenuPermission(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String menuCode = request.get("menuCode");
        boolean hasPermission = userService.checkMenuPermission(token, menuCode);
        Map<String, Boolean> result = new HashMap<>();
        result.put("hasPermission", hasPermission);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户菜单树
     */
    @GetMapping("/menu-tree")
    public ResponseEntity<List<Map<String, Object>>> getMenuTree(
            @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        List<Map<String, Object>> menuTree = userService.getUserMenuTree(token);
        return ResponseEntity.ok(menuTree);
    }

    /**
     * 获取用户菜单列表
     */
    @GetMapping("/menu-list")
    public ResponseEntity<List<Map<String, Object>>> getMenuList(
            @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        List<Map<String, Object>> menuList = userService.getUserMenuList(token);
        return ResponseEntity.ok(menuList);
    }

    /**
     * 获取用户所有权限标识
     */
    @GetMapping("/permission-codes")
    public ResponseEntity<List<String>> getPermissionCodes(
            @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        List<String> permissionCodes = userService.getUserPermissionCodes(token);
        return ResponseEntity.ok(permissionCodes);
    }

    /**
     * 验证令牌有效性
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(
            @RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        boolean valid = userService.validateToken(token);
        Map<String, Boolean> result = new HashMap<>();
        result.put("valid", valid);
        return ResponseEntity.ok(result);
    }

    /**
     * 注册请求DTO
     */
    public static class RegisterRequest {
        private String username;
        private String password;
        private String name;
        private String email;
        private String phone;
        private List<Long> roleIds;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public List<Long> getRoleIds() { return roleIds; }
        public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
    }
}
