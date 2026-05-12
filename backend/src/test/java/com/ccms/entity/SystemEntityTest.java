package com.ccms.entity;

import com.ccms.BaseTest;

import com.ccms.entity.system.user.User;
import com.ccms.entity.system.dept.Department;
import com.ccms.entity.system.config.DataDict;
import com.ccms.entity.system.config.SystemConfig;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统模块实体类单元测试
 */
class SystemEntityTest extends BaseTest {

    @Test
    void shouldCreateUserWithOldMethod() {
        // Given & When
        User user = new User();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setName("张三");
        user.setPhone("13800138000");
        user.setEmail("zhangsan@company.com");
        user.setStatus(User.UserStatus.ACTIVE);
        user.setPassword("encrypted_password");
        user.setEmployeeNo("EMP001");

        // Then
        assertEquals(1L, user.getId());
        assertEquals("EMP001", user.getEmployeeNo());
        assertEquals("zhangsan", user.getUsername());
        assertEquals("张三", user.getName());
        assertEquals("13800138000", user.getPhone());
        assertEquals("zhangsan@company.com", user.getEmail());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void shouldCreateUserWithNewMethod() {
        // Given & When
        User user = new User();
        user.setId(1L);
        user.setUsername("zhangsan");
        user.setName("张三");
        user.setEmail("zhangsan@company.com");
        user.setEmployeeNo("EMP001");
        user.setStatus(User.UserStatus.ACTIVE);

        // Then
        assertEquals(1L, user.getId());
        assertEquals("zhangsan", user.getUsername());
        assertEquals("张三", user.getName());
        assertEquals("zhangsan@company.com", user.getEmail());
        assertEquals("EMP001", user.getEmployeeNo());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void shouldCreateDepartment() {
        // Given & When
        Department dept = new Department();
        dept.setId(1L);
        dept.setDeptCode("D001");
        dept.setDeptName("技术部");

        // Then
        assertEquals(1L, dept.getId());
        assertEquals("D001", dept.getDeptCode());
        assertEquals("技术部", dept.getDeptName());
    }

    @Test
    void shouldCreateDataDict() {
        // Given & When
        DataDict dict = new DataDict();
        dict.setId(1L);
        dict.setDictType("expense_type");
        dict.setDictCode("TRAVEL");
        dict.setDictName("差旅费");
        dict.setSortOrder(1);
        dict.setStatus(1);

        // Then
        assertEquals(1L, dict.getId());
        assertEquals("expense_type", dict.getDictType());
        assertEquals("TRAVEL", dict.getDictCode());
        assertEquals("差旅费", dict.getDictName());
        assertEquals(1, dict.getSortOrder());
        assertEquals(1, dict.getStatus());
    }

    @Test
    void shouldCreateSystemConfig() {
        // Given & When
        SystemConfig config = new SystemConfig();
        config.setId(1L);
        config.setConfigKey("system.name");
        config.setConfigValue("费控管理系统");
        config.setConfigName("系统名称");
        config.setDescription("系统名称配置");
        config.setEnabled(true);

        // Then
        assertEquals(1L, config.getId());
        assertEquals("system.name", config.getConfigKey());
        assertEquals("费控管理系统", config.getConfigValue());
        assertEquals("系统名称", config.getConfigName());
        assertEquals("系统名称配置", config.getDescription());
        assertTrue(config.isEnabled());
    }
}
