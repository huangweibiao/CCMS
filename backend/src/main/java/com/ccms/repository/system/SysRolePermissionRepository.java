package com.ccms.repository.system;

import com.ccms.entity.system.permission.SysRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色权限关联表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface SysRolePermissionRepository extends JpaRepository<SysRolePermission, Long> {

    /**
     * 根据角色ID查询关联关系
     * 
     * @param roleId 角色ID
     * @return 关联关系列表
     */
    List<SysRolePermission> findByRoleId(Long roleId);

    /**
     * 根据权限ID查询关联关系
     * 
     * @param permissionId 权限ID
     * @return 关联关系列表
     */
    List<SysRolePermission> findByPermissionId(Long permissionId);

    /**
     * 根据角色ID和权限ID查询关联关系
     * 
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 关联关系
     */
    SysRolePermission findByRoleIdAndPermissionId(Long roleId, Long permissionId);

    /**
     * 根据角色ID删除所有关联关系
     * 
     * @param roleId 角色ID
     */
    @Modifying
    @Query("DELETE FROM SysRolePermission rp WHERE rp.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID删除所有关联关系
     * 
     * @param permissionId 权限ID
     */
    @Modifying
    @Query("DELETE FROM SysRolePermission rp WHERE rp.permissionId = :permissionId")
    void deleteByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据角色ID和权限ID列表删除关联关系
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    @Modifying
    @Query("DELETE FROM SysRolePermission rp WHERE rp.roleId = :roleId AND rp.permissionId IN :permissionIds")
    void deleteByRoleIdAndPermissionIds(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 批量插入角色权限关联关系
     * 
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    default void saveRolePermissions(Long roleId, List<Long> permissionIds) {
        deleteByRoleId(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                save(rolePermission);
            }
        }
    }

    /**
     * 查询角色是否拥有指定权限
     * 
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否拥有
     */
    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    /**
     * 查询角色拥有的权限ID列表
     * 
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Query("SELECT rp.permissionId FROM SysRolePermission rp WHERE rp.roleId = :roleId")
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);
}