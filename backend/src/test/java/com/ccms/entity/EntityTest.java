package com.ccms.entity;

import com.ccms.BaseTest;
import com.ccms.entity.system.SysUser;
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
    void shouldCreateSysUser() {
        // Given & When
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUserCode("TEST001");
        user.setUserName("测试用户");
        user.setMobile("13800138000");
        user.setEmail("test@example.com");
        user.setDeptId(1L);
        user.setStatus(1);
        user.setPassword("password123");
        user.setLastLoginTime(LocalDateTime.now());

        // Then
        assertEquals(1L, user.getId());
        assertEquals("TEST001", user.getUserCode());
        assertEquals("测试用户", user.getUserName());
        assertEquals("13800138000", user.getMobile());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(1L, user.getDeptId());
        assertEquals(1, user.getStatus());
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
        // Given & When - 使用SysUser作为BaseEntity的具体实现
        SysUser entity = new SysUser();
        entity.setId(1L);
        entity.setVersion(0);
        entity.setDeleted(false);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreateBy(1L);
        entity.setUpdateBy(1L);

        // Then
        assertEquals(1L, entity.getId());
        assertEquals(0, entity.getVersion());
        assertEquals(false, entity.getDeleted());
        assertNotNull(entity.getCreateTime());
        assertNotNull(entity.getUpdateTime());
        assertEquals(1L, entity.getCreateBy());
        assertEquals(1L, entity.getUpdateBy());
    }
}
