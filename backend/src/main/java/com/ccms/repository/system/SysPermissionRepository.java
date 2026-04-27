package com.ccms.repository.system;

import com.ccms.entity.system.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {

    /**
     * 根据权限编码查询权限
     * 
     * @param permissionCode 权限编码
     * @return 权限信息
     */
    Optional<SysPermission> findByPermissionCode(String permissionCode);

    /**
     * 根据权限名称查询权限
     * 
     * @param permissionName 权限名称
     * @return 权限信息
     */
    Optional<SysPermission> findByPermissionName(String permissionName);

    /**
     * 根据父权限ID查询子权限列表
     * 
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    List<SysPermission> findByParentId(Long parentId);

    /**
     * 根据权限类型查询权限列表
     * 
     * @param permissionType 权限类型：0-菜单，1-按钮，2-接口
     * @return 权限列表
     */
    List<SysPermission> findByPermissionType(Integer permissionType);

    /**
     * 查询所有可用的权限列表
     * 
     * @return 可用权限列表
     */
    @Query("SELECT p FROM SysPermission p WHERE p.status = 1 ORDER BY p.sort ASC")
    List<SysPermission> findAllActivePermissions();

    /**
     * 根据角色ID查询权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Query("SELECT p FROM SysPermission p JOIN SysRolePermission rp ON p.id = rp.permissionId WHERE rp.roleId = :roleId AND p.status = 1 ORDER BY p.sort ASC")
    List<SysPermission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询权限列表
     * 
     * @param userId 用户ID
     * @return 权限列表
     */
    @Query("SELECT DISTINCT p FROM SysPermission p " +
           "JOIN SysRolePermission rp ON p.id = rp.permissionId " +
           "JOIN SysUserRole ur ON rp.roleId = ur.roleId " +
           "WHERE ur.userId = :userId AND p.status = 1")
    List<SysPermission> findByUserId(@Param("userId") Long userId);

    /**
     * 查询树形结构的权限列表
     * 
     * @return 树形权限列表
     */
    @Query("SELECT p FROM SysPermission p WHERE p.parentId = 0 AND p.status = 1 ORDER BY p.sort ASC")
    List<SysPermission> findRootPermissions();

    /**
     * 根据权限路径查询权限
     * 
     * @param permissionPath 权限路径
     * @return 权限信息
     */
    Optional<SysPermission> findByPermissionPath(String permissionPath);
}