package com.ccms.repository.system.permission;

import com.ccms.entity.system.permission.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色Repository接口
 * 基于D:\aitols\base-app项目的标准进行设计
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色编码查询角色
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 根据角色名称查询角色
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * 根据状态查询角色列表
     */
    List<Role> findByStatus(Integer status);

    /**
     * 查询所有启用的角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.status = 1 ORDER BY r.roleCode ASC")
    List<Role> findAllActiveRoles();

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 检查角色编码是否存在（排除当前角色）
     */
    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.roleCode = :roleCode AND r.id != :excludeId")
    boolean existsByRoleCodeAndIdNot(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);

    /**
     * 根据用户ID查询角色列表
     */
    @Query("SELECT r FROM Role r " +
           "JOIN com.ccms.entity.system.user.SysUserRole ur ON r.id = ur.roleId " +
           "WHERE ur.userId = :userId AND r.status = 1")
    List<Role> findByUserId(@Param("userId") Long userId);

    /**
     * 查询包含指定菜单权限的角色列表
     */
    @Query("SELECT DISTINCT r FROM Role r " +
           "JOIN RoleMenu rm ON r.id = rm.roleId " +
           "WHERE rm.menuId = :menuId AND r.status = 1")
    List<Role> findByMenuId(@Param("menuId") Long menuId);

    /**
     * 根据数据范围查询角色列表
     */
    List<Role> findByDataScope(Integer dataScope);

    /**
     * 查询系统内置角色
     */
    @Query("SELECT r FROM Role r WHERE r.roleCode LIKE 'SYS_%' AND r.status = 1")
    List<Role> findSystemRoles();

    /**
     * 分页查询角色列表（按创建时间倒序）
     */
    @Query("SELECT r FROM Role r ORDER BY r.createTime DESC")
    List<Role> findAllOrderByCreateTimeDesc();

    /**
     * 统计活跃角色数量
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.status = 1")
    Long countActiveRoles();
}