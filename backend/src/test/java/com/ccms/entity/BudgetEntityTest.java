package com.ccms.entity;

import com.ccms.BaseTest;
import com.ccms.entity.budget.BudgetMain;
import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetAdjust;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 预算实体类单元测试
 */
class BudgetEntityTest extends BaseTest {

    @Test
    void shouldCreateBudgetMain() {
        // Given & When
        BudgetMain budget = new BudgetMain();
        budget.setId(1L);
        budget.setBudgetNo("B202501010001");
        budget.setBudgetYear(2025);
        budget.setDeptId(1L);
        budget.setTotalAmount(new BigDecimal("100000.00"));
        budget.setUsedAmount(new BigDecimal("50000.00"));
        budget.setStatus(1);

        // Then
        assertEquals(1L, budget.getId());
        assertEquals("B202501010001", budget.getBudgetNo());
        assertEquals(2025, budget.getBudgetYear());
        assertEquals(new BigDecimal("100000.00"), budget.getTotalAmount());
        assertEquals(new BigDecimal("50000.00"), budget.getUsedAmount());
        assertEquals(1, budget.getStatus());
    }

    @Test
    void shouldCalculateBudgetBalance() {
        // Given
        BudgetMain budget = new BudgetMain();
        budget.setTotalAmount(new BigDecimal("100000.00"));
        budget.setUsedAmount(new BigDecimal("30000.00"));

        // When & Then
        assertEquals(new BigDecimal("70000.00"), budget.getTotalAmount().subtract(budget.getUsedAmount()));
    }

    @Test
    void shouldCreateBudgetDetail() {
        // Given & When
        BudgetDetail detail = new BudgetDetail();
        detail.setId(1L);
        detail.setBudgetId(1L);
        detail.setFeeTypeId(1L);
        detail.setBudgetAmount(new BigDecimal("20000.00"));
        detail.setUsedAmount(new BigDecimal("5000.00"));

        // Then
        assertEquals(1L, detail.getId());
        assertEquals(1L, detail.getBudgetId());
        assertEquals(1L, detail.getFeeTypeId());
        assertEquals(new BigDecimal("20000.00"), detail.getBudgetAmount());
    }

    @Test
    void shouldCreateBudgetAdjust() {
        // Given & When
        BudgetAdjust adjust = new BudgetAdjust();
        adjust.setId(1L);
        adjust.setBudgetId(1L);
        adjust.setAdjustType(1);
        adjust.setAdjustAmount(new BigDecimal("10000.00"));
        adjust.setReason("年度调整");
        adjust.setStatus(0);

        // Then
        assertEquals(1L, adjust.getId());
        assertEquals(1L, adjust.getBudgetId());
        assertEquals(1, adjust.getAdjustType());
        assertEquals(new BigDecimal("10000.00"), adjust.getAdjustAmount());
        assertEquals("年度调整", adjust.getReason());
    }
}
