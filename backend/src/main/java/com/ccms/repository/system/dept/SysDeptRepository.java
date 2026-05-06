package com.ccms.repository.system.dept;

import com.ccms.entity.system.dept.SysDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 部门表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface SysDeptRepository extends JpaRepository<SysDept, Long> {

    /**
     * 根据部门编码查询部门
     * 
     * @param deptCode 部门编码
     * @return 部门信息
     */
    Optional<SysDept> findByDeptCode(String deptCode);

    /**
     * 根据部门名称查询部门
     * 
     * @param deptName 部门名称
     * @return 部门信息
     */
    Optional<SysDept> findByDeptName(String deptName);

    /**
     * 根据父部门ID查询子部门列表
     * 
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<SysDept> findByParentId(Long parentId);

    /**
     * 根据状态查询部门列表
     * 
     * @param status 部门状态：0-禁用，1-启用
     * @return 部门列表
     */
    List<SysDept> findByStatus(Integer status);

    /**
     * 查询所有可用的部门列表
     * 
     * @return 可用部门列表
     */
    @Query("SELECT d FROM SysDept d WHERE d.status = 1 ORDER BY d.sort ASC")
    List<SysDept> findAllActiveDepartments();

    /**
     * 查询树形结构的部门列表
     * 
     * @return 树形部门列表
     */
    @Query("SELECT d FROM SysDept d WHERE d.parentId = 0 AND d.status = 1 ORDER BY d.sort ASC")
    List<SysDept> findRootDepartments();

    /**
     * 根据部门路径查询部门
     * 
     * @param deptPath 部门路径
     * @return 部门信息
     */
    Optional<SysDept> findByDeptPath(String deptPath);

    /**
     * 检查部门编码是否存在
     * 
     * @param deptCode 部门编码
     * @return 是否存在
     */
    boolean existsByDeptCode(String deptCode);

    /**
     * 查询特定层级的所有部门
     * 
     * @param level 部门层级
     * @return 部门列表
     */
    List<SysDept> findByLevel(Integer level);

    /**
     * 根据部门负责人ID查询部门
     * 
     * @param managerId 部门负责人ID
     * @return 部门列表
     */
    List<SysDept> findByManagerId(Long managerId);
}