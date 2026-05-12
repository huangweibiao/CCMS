package com.ccms.entity.expense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExpenseApplyDetail实体类单元测试
 */
class ExpenseApplyDetailTest {

    private ExpenseApplyDetail expenseApplyDetail;
    private ExpenseApplyMain expenseApplyMain;

    @BeforeEach
    void setUp() {
        expenseApplyDetail = new ExpenseApplyDetail();
        expenseApplyDetail.setFeeTypeId(101L);
        expenseApplyDetail.setAmount(new BigDecimal("1000.00"));
        expenseApplyDetail.setDescription("差旅费");
        expenseApplyDetail.setCostCenter("销售部成本中心");
        expenseApplyDetail.setBudgetId(201L);
        
        expenseApplyMain = new ExpenseApplyMain();
        expenseApplyMain.setId(1L);
        expenseApplyDetail.setExpenseApplyMain(expenseApplyMain);
    }

    @Test
    void testValidateDetailInfo() {
        // 基本信息完整的情况
        assertTrue(expenseApplyDetail.validateDetailInfo());
        
        // 费用类型ID缺失
        expenseApplyDetail.setFeeTypeId(null);
        assertFalse(expenseApplyDetail.validateDetailInfo());
        
        // 金额缺失
        expenseApplyDetail.setFeeTypeId(101L);
        expenseApplyDetail.setAmount(null);
        assertFalse(expenseApplyDetail.validateDetailInfo());
        
        // 金额为0或负数
        expenseApplyDetail.setAmount(BigDecimal.ZERO);
        assertFalse(expenseApplyDetail.validateDetailInfo());
        
        expenseApplyDetail.setAmount(new BigDecimal("-100.00"));
        assertFalse(expenseApplyDetail.validateDetailInfo());
        
        // 主表关联缺失
        expenseApplyDetail.setAmount(new BigDecimal("1000.00"));
        expenseApplyDetail.setExpenseApplyMain(null);
        assertFalse(expenseApplyDetail.validateDetailInfo());
    }

    @Test
    void testHasBudget() {
        // 有关联预算
        assertTrue(expenseApplyDetail.hasBudget());
        
        // 无关联预算
        expenseApplyDetail.setBudgetId(null);
        assertFalse(expenseApplyDetail.hasBudget());
    }

    @Test
    void testHasCostCenter() {
        // 有成本中心
        assertTrue(expenseApplyDetail.hasCostCenter());
        
        // 成本中心为空
        expenseApplyDetail.setCostCenter(null);
        assertFalse(expenseApplyDetail.hasCostCenter());
        
        // 成本中心为空白字符串
        expenseApplyDetail.setCostCenter("   ");
        assertFalse(expenseApplyDetail.hasCostCenter());
        
        // 成本中心为空字符串
        expenseApplyDetail.setCostCenter("");
        assertFalse(expenseApplyDetail.hasCostCenter());
    }

    @Test
    void testGetFeeTypeName() {
        // 关联预算的情况
        assertEquals("关联预算费用项", expenseApplyDetail.getFeeTypeName());
        
        // 无关联预算的情况
        expenseApplyDetail.setBudgetId(null);
        assertEquals("普通费用项", expenseApplyDetail.getFeeTypeName());
    }

    @Test
    void testIsAmountWithinBudgetLimit() {
        // 默认实现应该返回true
        assertTrue(expenseApplyDetail.isAmountWithinBudgetLimit());
        
        // 无预算关联的情况
        expenseApplyDetail.setBudgetId(null);
        assertTrue(expenseApplyDetail.isAmountWithinBudgetLimit());
        
        // 无金额的情况
        expenseApplyDetail.setAmount(null);
        expenseApplyDetail.setBudgetId(201L);
        assertTrue(expenseApplyDetail.isAmountWithinBudgetLimit());
    }

    @Test
    void testGetBudgetUsageRatio() {
        // 默认实现应该返回0
        assertEquals(BigDecimal.ZERO, expenseApplyDetail.getBudgetUsageRatio());
        
        // 无预算关联的情况
        expenseApplyDetail.setBudgetId(null);
        assertEquals(BigDecimal.ZERO, expenseApplyDetail.getBudgetUsageRatio());
        
        // 无金额或金额为0的情况
        expenseApplyDetail.setAmount(null);
        expenseApplyDetail.setBudgetId(201L);
        assertEquals(BigDecimal.ZERO, expenseApplyDetail.getBudgetUsageRatio());
        
        expenseApplyDetail.setAmount(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, expenseApplyDetail.getBudgetUsageRatio());
    }

    @Test
    void testToString() {
        String toString = expenseApplyDetail.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("ExpenseApplyDetail"));
        assertTrue(toString.contains("feeTypeId=101"));
        assertTrue(toString.contains("amount=1000.00"));
        assertTrue(toString.contains("description='差旅费'"));
        assertTrue(toString.contains("budgetId=201"));
        
        // 测试主表关联为null的情况
        expenseApplyDetail.setExpenseApplyMain(null);
        toString = expenseApplyDetail.toString();
        assertTrue(toString.contains("applyId=null"));
    }

    @Test
    void testGettersAndSetters() {
        // 测试getter和setter方法
        assertEquals(101L, expenseApplyDetail.getFeeTypeId());
        assertEquals(new BigDecimal("1000.00"), expenseApplyDetail.getAmount());
        assertEquals("差旅费", expenseApplyDetail.getDescription());
        assertEquals("销售部成本中心", expenseApplyDetail.getCostCenter());
        assertEquals(201L, expenseApplyDetail.getBudgetId());
        assertEquals(expenseApplyMain, expenseApplyDetail.getExpenseApplyMain());
        
        // 测试setter方法
        expenseApplyDetail.setFeeTypeId(102L);
        expenseApplyDetail.setAmount(new BigDecimal("2000.00"));
        expenseApplyDetail.setDescription("住宿费");
        expenseApplyDetail.setCostCenter("行政部成本中心");
        expenseApplyDetail.setBudgetId(202L);
        
        assertEquals(102L, expenseApplyDetail.getFeeTypeId());
        assertEquals(new BigDecimal("2000.00"), expenseApplyDetail.getAmount());
        assertEquals("住宿费", expenseApplyDetail.getDescription());
        assertEquals("行政部成本中心", expenseApplyDetail.getCostCenter());
        assertEquals(202L, expenseApplyDetail.getBudgetId());
    }

    @Test
    void testAssociationWithMain() {
        // 测试主表关联设置
        assertNotNull(expenseApplyDetail.getExpenseApplyMain());
        assertEquals(1L, expenseApplyDetail.getExpenseApplyMain().getId());
        
        // 测试关联变更
        ExpenseApplyMain newMain = new ExpenseApplyMain();
        newMain.setId(2L);
        expenseApplyDetail.setExpenseApplyMain(newMain);
        
        assertEquals(newMain, expenseApplyDetail.getExpenseApplyMain());
        assertEquals(2L, expenseApplyDetail.getExpenseApplyMain().getId());
    }
}