package com.ccms.service;

import com.ccms.entity.system.SysUser;

import java.util.List;
import java.util.Map;

/**
 * 用户认证与权限服务接口
 * 
 * @author 系统生成
 */
public interface AuthService {
    
    /**
     * 用户登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @return 用户信息（登录成功）或 null（登录失败）
     */
    SysUser login(String username, String password);
    
    /**
     * 用户登出
     * 
     * @param token 访问令牌
     */
    void logout(String token);
    
    /**
     * 验证用户权限
     * 
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permissionCode);
    
    /**
     * 获取用户角色列表
     * 
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoles(Long userId);
    
    /**
     * 生成访问令牌
     * 
     * @param user 用户信息
     * @return 访问令牌
     */
    String generateToken(SysUser user);
    
    /**
     * 验证访问令牌
     * 
     * @param token 访问令牌
     * @return 是否有效
     */
    boolean validateToken(String token);
    
    /**
     * 根据令牌获取用户信息
     * 
     * @param token 访问令牌
     * @return 用户信息
     */
    SysUser getUserByToken(String token);
    
    /**
     * 修改用户密码
     * 
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @return 新密码
     */
    String resetPassword(Long userId);
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExist(String username);
    
    /**
     * 注册新用户
     * 
     * @param user 用户信息
     * @param roleIds 角色ID列表
     * @return 注册成功的用户信息
     */
    SysUser register(SysUser user, List<Long> roleIds);
}