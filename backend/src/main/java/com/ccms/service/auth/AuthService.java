package com.ccms.service.auth;

import com.ccms.entity.system.user.User;

import java.util.List;
import java.util.Set;

/**
 * 权限认证服务接口
 */
public interface AuthService {

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 验证用户密码
     */
    boolean validatePassword(String rawPassword, String encodedPassword);

    /**
     * 获取用户权限列表
     */
    Set<String> getUserPermissions(Long userId);

    /**
     * 获取用户角色列表
     */
    Set<String> getUserRoles(Long userId);

    /**
     * 检查用户是否有指定权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 检查用户是否有指定角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 获取用户数据权限范围（部门ID列表）
     */
    List<Long> getUserDataScope(Long userId);

    /**
     * 检查用户是否能够访问指定部门的数据
     */
    boolean canAccessDepartment(Long userId, Long departmentId);

    /**
     * 检查是否能够访问模块
     */
    boolean canAccessModule(Long userId, String moduleCode);

    /**
     * 验证用户访问权限
     */
    boolean verifyAccess(Long userId, String resourceCode, String operation);

    /**
     * 生成访问令牌
     */
    String generateAccessToken(User user);

    /**
     * 验证访问令牌
     */
    boolean validateAccessToken(String token);

    /**
     * 根据令牌获取用户信息
     */
    User getUserByToken(String token);

    /**
     * 刷新访问令牌
     */
    String refreshAccessToken(String oldToken);

    /**
     * 记录登录日志
     */
    void recordLoginLog(Long userId, String ipAddress, boolean success, String message);

    /**
     * 记录操作日志
     */
    void recordOperationLog(Long userId, String operation, String resource, String description);
}