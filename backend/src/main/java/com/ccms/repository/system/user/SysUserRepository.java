package com.ccms.repository.system.user;

import com.ccms.entity.system.user.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    Optional<SysUser> findByUsername(String username);

    /**
     * 根据工号查询用户
     * 
     * @param userCode 工号
     * @return 用户信息
     */
    Optional<SysUser> findByUserCode(String userCode);

    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<SysUser> findByEmail(String email);

    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户信息
     */
    Optional<SysUser> findByPhone(String phone);

    /**
     * 根据部门ID查询用户列表
     * 
     * @param deptId 部门ID
     * @return 用户列表
     */
    List<SysUser> findByDeptId(Long deptId);

    /**
     * 根据状态查询用户列表
     * 
     * @param status 用户状态：0-禁用，1-启用
     * @return 用户列表
     */
    List<SysUser> findByStatus(Integer status);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查工号是否存在
     * 
     * @param userCode 工号
     * @return 是否存在
     */
    boolean existsByUserCode(String userCode);

    /**
     * 根据角色ID查询关联的用户列表
     * 
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Query("SELECT u FROM SysUser u JOIN SysUserRole ur ON u.id = ur.userId WHERE ur.roleId = :roleId")
    List<SysUser> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询包含指定角色的用户列表
     * 
     * @param roleIds 角色ID列表
     * @return 用户列表
     */
    @Query("SELECT DISTINCT u FROM SysUser u JOIN SysUserRole ur ON u.id = ur.userId WHERE ur.roleId IN :roleIds")
    List<SysUser> findByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据用户名或姓名模糊查询用户
     * 
     * @param keyword 关键字
     * @return 用户列表
     */
    @Query("SELECT u FROM SysUser u WHERE u.username LIKE %:keyword% OR u.name LIKE %:keyword%")
    List<SysUser> findByUsernameOrNameContaining(@Param("keyword") String keyword);
    
    /**
     * 根据状态统计用户数量
     * 
     * @param status 状态值
     * @return 用户数量
     */
    long countByStatus(Integer status);
}