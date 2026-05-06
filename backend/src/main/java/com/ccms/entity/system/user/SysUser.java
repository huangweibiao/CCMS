package com.ccms.entity.system.user;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户表实体类
 * 对应表名：sys_user（与设计文档保持一致）
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_sys_user")
public class SysUser extends BaseEntity {
    
    /**
     * 部门负责人标识: 0-否 1-是
     */
    @Column(name = "is_dept_manager", nullable = false, columnDefinition = "tinyint default 0")
    private Integer isDeptManager = 0;

    /**
     * 工号
     */
    @Column(name = "user_code", length = 32, nullable = false)
    private String userCode;
    
    /**
     * 姓名
     */
    @Column(name = "user_name", length = 64, nullable = false)
    private String userName;
    
    /**
     * 手机号
     */
    @Column(name = "mobile", length = 20)
    private String mobile;
    
    /**
     * 邮箱
     */
    @Column(name = "email", length = 128)
    private String email;
    
    /**
     * 部门ID
     */
    @Column(name = "dept_id", nullable = false)
    private Long deptId;
    
    /**
     * 岗位ID
     */
    @Column(name = "post_id")
    private Long postId;
    
    /**
     * 角色ID
     */
    @Column(name = "role_id")
    private Long roleId;
    
    /**
     * 状态：0-禁用 1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 加密密码
     */
    @Column(name = "password", length = 128, nullable = false)
    private String password;
    
    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    // Getters and Setters
    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Integer getIsDeptManager() {
        return isDeptManager;
    }

    public void setIsDeptManager(Integer isDeptManager) {
        this.isDeptManager = isDeptManager;
    }
    
    // Compatible methods for service layer calls
    public String getUsername() {
        return userCode; // username might be mapped to userCode
    }
    
    public void setUsername(String username) {
        this.userCode = username;
    }
    
    public String getRealName() {
        return userName; // realName might be mapped to userName
    }
    
    public void setRealName(String realName) {
        this.userName = realName;
    }
    
    public String getPhone() {
        return mobile; // phone might be mapped to mobile
    }
    
    public void setPhone(String phone) {
        this.mobile = phone;
    }

    @Override
    public String toString() {
        return "SysUser{" +
                "id=" + getId() +
                ", userCode='" + userCode + '\'' +
                ", userName='" + userName + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                ", deptId=" + deptId +
                ", postId=" + postId +
                ", roleId=" + roleId +
                ", status=" + status +
                ", lastLoginTime=" + lastLoginTime +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}
