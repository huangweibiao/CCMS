package com.ccms.repository.system;

import com.ccms.entity.system.user.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {

    /**
     * 根据用户ID查询关联关系
     * 
     * @param userId 用户ID
     * @return 关联关系列表
     */
    List<SysUserRole> findByUserId(Long userId);

    /**
     * 根据角色ID查询关联关系
     * 
     * @param roleId 角色ID
     * @return 关联关系列表
     */
    List<SysUserRole> findByRoleId(Long roleId);

    /**
     * 根据用户ID和角色ID查询关联关系
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 关联关系
     */
    SysUserRole findByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 根据用户ID删除所有关联关系
     * 
     * @param userId 用户ID
     */
    @Modifying
    @Query("DELETE FROM SysUserRole ur WHERE ur.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID删除所有关联关系
     * 
     * @param roleId 角色ID
     */
    @Modifying
    @Query("DELETE FROM SysUserRole ur WHERE ur.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID和角色ID列表删除关联关系
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    @Modifying
    @Query("DELETE FROM SysUserRole ur WHERE ur.userId = :userId AND ur.roleId IN :roleIds")
    void deleteByUserIdAndRoleIds(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 批量插入用户角色关联关系
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    default void saveUserRoles(Long userId, List<Long> roleIds) {
        deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                save(userRole);
            }
        }
    }

    /**
     * 查询用户是否拥有指定角色
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否拥有
     */
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}