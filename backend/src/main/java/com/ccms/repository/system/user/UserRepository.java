package com.ccms.repository.system.user;

import com.ccms.entity.system.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据员工编号查找用户
     */
    Optional<User> findByEmployeeNo(String employeeNo);

    /**
     * 根据用户状态查找用户列表
     */
    List<User> findByStatus(User.UserStatus status);

    /**
     * 根据部门查找用户列表
     */
    List<User> findByDepartmentId(Long departmentId);

    /**
     * 根据角色查找用户列表
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    List<User> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色编码查找用户列表
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleCode = :roleCode")
    List<User> findByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 查找启用状态的用户
     */
    List<User> findByEnabledTrue();

    /**
     * 查找所有登录过的用户
     */
    List<User> findByLoginCountGreaterThan(int count);

    /**
     * 根据用户名或姓名模糊搜索用户
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.name LIKE %:keyword%")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查员工编号是否存在
     */
    boolean existsByEmployeeNo(String employeeNo);

    /**
     * 根据部门ID列表查找用户
     */
    @Query("SELECT u FROM User u WHERE u.department.id IN :deptIds")
    List<User> findByDepartmentIds(@Param("deptIds") List<Long> deptIds);

    /**
     * 查找用户的直属下级
     */
    List<User> findByLeaderId(Long leaderId);

    /**
     * 统计部门的用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.department.id = :departmentId")
    Integer countByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 统计角色下的用户数量
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    Integer countByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户状态和部门统计数量
     */
    @Query("SELECT u.status, COUNT(u) FROM User u WHERE u.department.id = :departmentId GROUP BY u.status")
    List<Object[]> countByStatusAndDepartment(@Param("departmentId") Long departmentId);
}