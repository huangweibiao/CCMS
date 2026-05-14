package com.ccms.repository.system.permission;

import com.ccms.entity.system.permission.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 菜单Repository接口
 * 基于D:\aitols\base-app项目的标准进行设计
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    /**
     * 根据菜单名称查询菜单
     */
    Optional<Menu> findByMenuName(String menuName);

    /**
     * 根据权限标识查询菜单
     */
    Optional<Menu> findByPermissionCode(String permissionCode);

    /**
     * 根据菜单类型查询菜单列表
     */
    List<Menu> findByMenuType(String menuType);

    /**
     * 根据父菜单ID查询子菜单列表
     */
    List<Menu> findByParentId(Long parentId);

    /**
     * 根据父菜单ID和状态查询子菜单列表
     */
    List<Menu> findByParentIdAndStatus(Long parentId, Integer status);

    /**
     * 根据可见性查询菜单列表
     */
    List<Menu> findByVisible(Boolean visible);

    /**
     * 根据状态查询菜单列表
     */
    List<Menu> findByStatus(Integer status);

    /**
     * 查询所有启用的菜单列表按排序升序
     */
    List<Menu> findByStatusOrderBySortOrderAsc(Integer status);

    /**
     * 查询所有一级菜单（parentId为0）
     */
    @Query("SELECT m FROM Menu m WHERE m.parentId = 0 AND m.status = 1 ORDER BY m.sortOrder ASC")
    List<Menu> findRootMenus();

    /**
     * 查询用户有权限的菜单列表
     */
    @Query("SELECT DISTINCT m FROM Menu m " +
           "JOIN RoleMenu rm ON m.id = rm.menuId " +
           "JOIN SysUserRole ur ON rm.roleId = ur.roleId " +
           "WHERE ur.userId = :userId AND m.status = 1 AND m.visible = true " +
           "ORDER BY m.sortOrder ASC")
    List<Menu> findUserMenus(@Param("userId") Long userId);

    /**
     * 查询角色关联的菜单列表
     */
    @Query("SELECT m FROM Menu m " +
           "JOIN RoleMenu rm ON m.id = rm.menuId " +
           "WHERE rm.roleId = :roleId AND m.status = 1 " +
           "ORDER BY m.sortOrder ASC")
    List<Menu> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询菜单树形结构
     */
    @Query("SELECT m FROM Menu m WHERE m.status = 1 AND m.visible = true ORDER BY m.sortOrder ASC")
    List<Menu> findMenuTree();

    /**
     * 检查菜单名称是否已存在（排除当前菜单）
     */
    @Query("SELECT COUNT(m) > 0 FROM Menu m WHERE m.menuName = :menuName AND m.id != :excludeId")
    boolean existsByMenuNameAndIdNot(@Param("menuName") String menuName, @Param("excludeId") Long excludeId);

    /**
     * 检查权限标识是否已存在（排除当前菜单）
     */
    @Query("SELECT COUNT(m) > 0 FROM Menu m WHERE m.permissionCode = :permissionCode AND m.id != :excludeId AND m.permissionCode IS NOT NULL")
    boolean existsByPermissionCodeAndIdNot(@Param("permissionCode") String permissionCode, @Param("excludeId") Long excludeId);

    /**
     * 根据路径查询菜单
     */
    Optional<Menu> findByPath(String path);

    /**
     * 查询用户的菜单权限编码列表
     */
    @Query("SELECT DISTINCT m.permissionCode FROM Menu m " +
           "JOIN RoleMenu rm ON m.id = rm.menuId " +
           "JOIN com.ccms.entity.system.user.SysUserRole ur ON rm.roleId = ur.roleId " +
           "WHERE ur.userId = :userId AND m.status = 1 AND m.visible = true AND m.permissionCode IS NOT NULL")
    Set<String> findUserPermissionCodes(@Param("userId") Long userId);

    /**
     * 检查用户是否对菜单有访问权限
     */
    @Query("SELECT COUNT(m) > 0 FROM Menu m " +
           "JOIN RoleMenu rm ON m.id = rm.menuId " +
           "JOIN com.ccms.entity.system.user.SysUserRole ur ON rm.roleId = ur.roleId " +
           "WHERE ur.userId = :userId AND m.permissionCode = :permissionCode AND m.status = 1")
    boolean hasMenuPermission(@Param("userId") Long userId, @Param("permissionCode") String permissionCode);

    /**
     * 查询用户权限菜单树（包含层次结构）
     */
    @Query("SELECT DISTINCT m FROM Menu m " +
           "JOIN RoleMenu rm ON m.id = rm.menuId " +
           "JOIN com.ccms.entity.system.user.SysUserRole ur ON rm.roleId = ur.roleId " +
           "WHERE ur.userId = :userId AND m.status = 1 AND m.visible = true " +
           "ORDER BY m.parentId, m.sortOrder ASC")
    List<Menu> findUserMenuTree(@Param("userId") Long userId);
}