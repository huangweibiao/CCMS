package com.ccms.controller;

import com.ccms.entity.system.SysUser;
import com.ccms.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 * 处理用户信息的增删改查等管理操作
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    @Autowired
    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 获取用户列表（分页）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名（可选）
     * @param deptId 部门ID（可选）
     * @param token 认证token
     * @return 用户分页列表
     */
    @GetMapping
    public ResponseEntity<Page<SysUser>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long deptId,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            authService.checkPermission(token, "user:list");
            
            org.springframework.data.domain.Page<SysUser> users = authService.getUserList(page, size, username, deptId);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取用户详情
     * 
     * @param userId 用户ID
     * @param token 认证token
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    public ResponseEntity<SysUser> getUserDetail(@PathVariable Long userId,
                                                @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            authService.checkPermission(token, "user:view");
            
            SysUser user = authService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 创建新用户
     * 
     * @param user 用户信息
     * @param token 认证token
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@RequestBody SysUser user,
                                                         @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            authService.checkPermission(token, "user:create");
            
            authService.createUser(user);
            return ResponseEntity.ok(Map.of("message", "用户创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户创建失败: " + e.getMessage()));
        }
    }

    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param user 更新后的用户信息
     * @param token 认证token
     * @return 更新结果
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, String>> updateUser(@PathVariable Long userId,
                                                         @RequestBody SysUser user,
                                                         @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            authService.checkPermission(token, "user:update");
            
            user.setId(userId);
            authService.updateUser(user);
            return ResponseEntity.ok(Map.of("message", "用户更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户更新失败: " + e.getMessage()));
        }
    }

    /**
     * 删除用户
     * 
     * @param userId 用户ID
     * @param token 认证token
     * @return 删除结果
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId,
                                                         @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            authService.checkPermission(token, "user:delete");
            
            authService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "用户删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户删除失败: " + e.getMessage()));
        }
    }

    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @param token 认证token
     * @return 重置结果
     */
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long userId,
                                                            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            authService.checkPermission(token, "user:reset-password");
            
            authService.resetPassword(userId);
            return ResponseEntity.ok(Map.of("message", "密码重置成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "密码重置失败: " + e.getMessage()));
        }
    }

    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 新状态
     * @param token 认证token
     * @return 更新结果
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<Map<String, String>> updateUserStatus(@PathVariable Long userId,
                                                               @RequestBody Map<String, Integer> status,
                                                               @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            authService.checkPermission(token, "user:update");
            
            authService.updateUserStatus(userId, status.get("status"));
            return ResponseEntity.ok(Map.of("message", "用户状态更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户状态更新失败: " + e.getMessage()));
        }
    }

    /**
     * 分配用户角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @param token 认证token
     * @return 分配结果
     */
    @PostMapping("/{userId}/assign-roles")
    public ResponseEntity<Map<String, String>> assignRoles(@PathVariable Long userId,
                                                          @RequestBody Map<String, Long[]> roleIds,
                                                          @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            authService.checkPermission(token, "user:assign-roles");
            
            authService.assignUserRoles(userId, roleIds.get("roleIds"));
            return ResponseEntity.ok(Map.of("message", "角色分配成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "角色分配失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户统计信息
     * 
     * @param deptId 部门ID（可选）
     * @param token 认证token
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@RequestParam(required = false) Long deptId,
                                                                @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            authService.checkPermission(token, "user:statistics");
            
            Map<String, Object> statistics = authService.getUserStatistics(deptId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}