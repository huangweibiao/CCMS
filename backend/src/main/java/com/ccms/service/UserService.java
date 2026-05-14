package com.ccms.service;

import com.ccms.entity.system.user.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 用户管理服务接口
 *
 * @author 系统生成
 */
public interface UserService {

    /**
     * 用户登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录响应信息
     */
    Object login(String username, String password);

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
    String generateToken(User user);

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
    User getUserByToken(String token);

    /**
     * 修改用户密码
     *
     * @param token 访问令牌
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(String token, String oldPassword, String newPassword);

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
    User register(User user, List<Long> roleIds);

    /**
     * 校验权限
     *
     * @param token 访问令牌
     * @param permission 权限编码
     * @return 是否有权限
     */
    boolean checkPermission(String token, String permission);

    /**
     * 获取用户分页列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名（可选）
     * @param deptId 部门ID（可选）
     * @return 用户分页列表
     */
    Page<User> getUserList(int page, int size, String username, Long deptId);

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 创建的用户信息
     */
    User createUser(User user);

    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUser(User user);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(Long userId);

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态值
     * @return 是否更新成功
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 分配用户角色
     *
     * @param userId 用户ID
     * @param roleIds 角色ID数组
     * @return 是否分配成功
     */
    boolean assignUserRoles(Long userId, Long[] roleIds);

    /**
     * 获取用户统计信息
     *
     * @param deptId 部门ID（可选）
     * @return 用户统计数据
     */
    Map<String, Object> getUserStatistics(Long deptId);

    /**
     * 根据用户名加载用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    User loadUserByUsername(String username);

    /**
     * 根据令牌获取用户名
     *
     * @param token 访问令牌
     * @return 用户名
     */
    String getUsernameFromToken(String token);

    /**
     * 刷新令牌
     *
     * @param token 旧令牌
     * @return 新令牌
     */
    String refreshToken(String token);

    /**
     * 获取用户权限列表
     *
     * @param token 访问令牌
     * @return 权限列表
     */
    Map<String, Object> getUserPermissions(String token);

    /**
     * 获取用户个人信息
     *
     * @param token 访问令牌
     * @return 用户信息
     */
    Map<String, Object> getUserProfileByToken(String token);

    /**
     * 获取用户菜单权限树
     *
     * @param token 访问令牌
     * @return 菜单权限树
     */
    List<Map<String, Object>> getUserMenuTree(String token);

    /**
     * 获取用户菜单列表（平铺格式）
     *
     * @param token 访问令牌
     * @return 菜单列表
     */
    List<Map<String, Object>> getUserMenuList(String token);

    /**
     * 检查用户对菜单的访问权限
     *
     * @param token 访问令牌
     * @param menuCode 菜单编码
     * @return 是否有访问权限
     */
    boolean checkMenuPermission(String token, String menuCode);

    /**
     * 获取用户所有权限标识
     *
     * @param token 访问令牌
     * @return 权限标识列表
     */
    List<String> getUserPermissionCodes(String token);
}
