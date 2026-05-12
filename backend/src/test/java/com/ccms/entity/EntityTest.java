package com.ccms.entity;

import com.ccms.BaseTest;
import com.ccms.entity.system.user.User;
import com.ccms.entity.system.dept.Department;
import com.ccms.entity.expense.ExpenseApply;
import com.ccms.entity.budget.BudgetMain;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实体类单元测试
 */
class EntityTest extends BaseTest {

    @Test
    void shouldCreateUser() {
        // Given & When
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setName("测试用户");
        user.setPhone("13800138000");
        user.setEmail("test@example.com");
        user.setStatus(User.UserStatus.ACTIVE);
        user.setPassword("password123");
        user.setLastLoginTime(LocalDateTime.now());
        user.setEmployeeNo("TEST001");

        // Then
        assertEquals(1L, user.getId());
        assertEquals("TEST001", user.getEmployeeNo());
        assertEquals("testuser", user.getUsername());
        assertEquals("测试用户", user.getName());
        assertEquals("13800138000", user.getPhone());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void shouldCreateExpenseApply() {
        // Given & When
        ExpenseApply apply = new ExpenseApply();
        apply.setId(1L);
        apply.setApplyNo("EA202501010001");
        apply.setTitle("测试费用申请");
        apply.setApplyUserId(1L);
        apply.setApplyUserName("测试用户");
        apply.setDeptId(1L);
        apply.setDeptName("技术部");
        apply.setApplyDate(LocalDate.now());
        apply.setTotalAmount(new BigDecimal("1000.00"));
        apply.setCurrency("CNY");
        apply.setStatus(0);
        apply.setRemark("测试备注");

        // Then
        assertEquals(1L, apply.getId());
        assertEquals("EA202501010001", apply.getApplyNo());
        assertEquals("测试费用申请", apply.getTitle());
        assertEquals(new BigDecimal("1000.00"), apply.getTotalAmount());
        assertEquals(0, apply.getStatus());
    }

    @Test
    void shouldCreateBudgetMain() {
        // Given & When
        BudgetMain budget = new BudgetMain();
        budget.setId(1L);
        budget.setBudgetNo("B202501010001");
        budget.setBudgetYear(2025);
        budget.setDeptId(1L);
        budget.setTotalAmount(new BigDecimal("100000.00"));
        budget.setUsedAmount(BigDecimal.ZERO);
        // budget.setAvailableAmount(new BigDecimal("100000.00"));
        budget.setStatus(1);

        // Then
        assertEquals(1L, budget.getId());
        assertEquals("B202501010001", budget.getBudgetNo());
        assertEquals(2025, budget.getBudgetYear());
        assertEquals(new BigDecimal("100000.00"), budget.getTotalAmount());
        assertEquals(1, budget.getStatus());
    }

    @Test
    void shouldTestBaseEntity() {
        // Given & When - 使用Department作为BaseEntity的具体实现
        Department entity = new Department();
        entity.setId(1L);
        entity.setDeptName("测试部门");
        entity.setVersion(0);
        entity.setDeleted(false);
        entity.setCreateBy(1L);
        entity.setUpdateBy(1L);

        // Then
        assertEquals(1L, entity.getId());
        assertEquals(0, entity.getVersion());
        assertEquals(false, entity.getDeleted());
        assertEquals(1L, entity.getCreateBy());
        assertEquals(1L, entity.getUpdateBy());
    }
}
