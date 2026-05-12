package com.ccms.repository.system.permission;

import com.ccms.entity.system.permission.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    /**
     * 根据角色编码查询角色
     * 
     * @param roleCode 角色编码
     * @return 角色信息
     */
    Optional<SysRole> findByRoleCode(String roleCode);

    /**
     * 根据角色名称查询角色
     * 
     * @param roleName 角色名称
     * @return 角色信息
     */
    Optional<SysRole> findByRoleName(String roleName);

    /**
     * 根据状态查询角色列表
     * 
     * @param status 角色状态：0-禁用，1-启用
     * @return 角色列表
     */
    List<SysRole> findByStatus(Integer status);

    /**
     * 查询所有可用的角色列表
     * 
     * @return 可用角色列表
     */
    @Query("SELECT r FROM SysRole r WHERE r.status = 1 ORDER BY r.roleCode ASC")
    List<SysRole> findAllActiveRoles();

    /**
     * 检查角色编码是否存在
     * 
     * @param roleCode 角色编码
     * @return 是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 根据用户ID查询角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query("SELECT r FROM SysRole r JOIN SysUserRole ur ON r.id = ur.roleId WHERE ur.userId = :userId AND r.status = 1")
    List<SysRole> findByUserId(@Param("userId") Long userId);


}