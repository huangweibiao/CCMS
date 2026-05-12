package com.ccms.entity.expense;

import com.ccms.enums.ApplyStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExpenseApplyMain实体类单元测试
 */
class ExpenseApplyMainTest {

    private ExpenseApplyMain expenseApplyMain;

    @BeforeEach
    void setUp() {
        expenseApplyMain = new ExpenseApplyMain();
        expenseApplyMain.setApplyNo("EXP-20250101-001");
        expenseApplyMain.setApplyType(1);
        expenseApplyMain.setApplyUserId(1001L);
        expenseApplyMain.setApplyDeptId(2001L);
        expenseApplyMain.setApplyAmount(new BigDecimal("5000.00"));
        expenseApplyMain.setTotalAmount(new BigDecimal("5000.00"));
        expenseApplyMain.setReason("项目差旅费用申请");
        expenseApplyMain.setExpectedDate(LocalDate.now().plusDays(30));
        expenseApplyMain.setStatus(ApplyStatusEnum.DRAFT.getCode());
        expenseApplyMain.setCostCenterId(3001L);
    }

    @Test
    void testStatusDescription() {
        expenseApplyMain.setStatus(ApplyStatusEnum.DRAFT.getCode());
        assertEquals("草稿", expenseApplyMain.getStatusDescription());
        
        expenseApplyMain.setStatus(ApplyStatusEnum.APPROVING.getCode());
        assertEquals("审批中", expenseApplyMain.getStatusDescription());
        
        expenseApplyMain.setStatus(999); // 无效状态
        assertEquals("未知状态", expenseApplyMain.getStatusDescription());
    }

    @Test
    void testCanTransitionTo() {
        // 从草稿可以变更为审批中或作废
        expenseApplyMain.setStatus(ApplyStatusEnum.DRAFT.getCode());
        assertTrue(expenseApplyMain.canTransitionTo(ApplyStatusEnum.APPROVING.getCode()));
        assertTrue(expenseApplyMain.canTransitionTo(ApplyStatusEnum.CANCELLED.getCode()));
        assertFalse(expenseApplyMain.canTransitionTo(ApplyStatusEnum.APPROVED.getCode()));
        
        // 从审批中可以变更为多种状态
        expenseApplyMain.setStatus(ApplyStatusEnum.APPROVING.getCode());
        assertTrue(expenseApplyMain.canTransitionTo(ApplyStatusEnum.APPROVED.getCode()));
        assertTrue(expenseApplyMain.canTransitionTo(ApplyStatusEnum.REJECTED.getCode()));
        assertTrue(expenseApplyMain.canTransitionTo(ApplyStatusEnum.CANCELLED.getCode()));
    }

    @Test
    void testIsEditable() {
        // 草稿状态可编辑
        expenseApplyMain.setStatus(ApplyStatusEnum.DRAFT.getCode());
        assertTrue(expenseApplyMain.isEditable());
        
        // 已驳回状态可编辑
        expenseApplyMain.setStatus(ApplyStatusEnum.REJECTED.getCode());
        assertTrue(expenseApplyMain.isEditable());
        
        // 审批中状态不可编辑
        expenseApplyMain.setStatus(ApplyStatusEnum.APPROVING.getCode());
        assertFalse(expenseApplyMain.isEditable());
    }

    @Test
    void testIsInApprovalProcess() {
        expenseApplyMain.setStatus(ApplyStatusEnum.APPROVING.getCode());
        assertTrue(expenseApplyMain.isInApprovalProcess());
        
        expenseApplyMain.setStatus(ApplyStatusEnum.DRAFT.getCode());
        assertFalse(expenseApplyMain.isInApprovalProcess());
    }

    @Test
    void testIsApprovalCompleted() {
        expenseApplyMain.setStatus(ApplyStatusEnum.APPROVED.getCode());
        assertTrue(expenseApplyMain.isApprovalCompleted());
        
        expenseApplyMain.setStatus(ApplyStatusEnum.REJECTED.getCode());
        assertTrue(expenseApplyMain.isApprovalCompleted());
        
        expenseApplyMain.setStatus(ApplyStatusEnum.CANCELLED.getCode());
        assertTrue(expenseApplyMain.isApprovalCompleted());
        
        expenseApplyMain.setStatus(ApplyStatusEnum.APPROVING.getCode());
        assertFalse(expenseApplyMain.isApprovalCompleted());
    }

    @Test
    void testCalculateTotalAmount() {
        // 测试空明细
        assertEquals(BigDecimal.ZERO, expenseApplyMain.calculateTotalAmount());
        
        // 添加明细项
        ExpenseApplyDetail detail1 = new ExpenseApplyDetail();
        detail1.setAmount(new BigDecimal("2000.00"));
        expenseApplyMain.addExpenseApplyDetail(detail1);
        
        ExpenseApplyDetail detail2 = new ExpenseApplyDetail();
        detail2.setAmount(new BigDecimal("3000.00"));
        expenseApplyMain.addExpenseApplyDetail(detail2);
        
        assertEquals(new BigDecimal("5000.00"), expenseApplyMain.calculateTotalAmount());
    }

    @Test
    void testIsAmountConsistent() {
        // 测试不匹配的情况
        expenseApplyMain.setApplyAmount(new BigDecimal("1000.00"));
        expenseApplyMain.setTotalAmount(new BigDecimal("2000.00"));
        assertFalse(expenseApplyMain.isAmountConsistent());
        
        // 测试匹配的情况
        ExpenseApplyDetail detail1 = new ExpenseApplyDetail();
        detail1.setAmount(new BigDecimal("2000.00"));
        expenseApplyMain.addExpenseApplyDetail(detail1);
        
        ExpenseApplyDetail detail2 = new ExpenseApplyDetail();
        detail2.setAmount(new BigDecimal("3000.00"));
        expenseApplyMain.addExpenseApplyDetail(detail2);
        
        expenseApplyMain.syncAmountFromDetails();
        assertTrue(expenseApplyMain.isAmountConsistent());
    }

    @Test
    void testSyncAmountFromDetails() {
        ExpenseApplyDetail detail1 = new ExpenseApplyDetail();
        detail1.setAmount(new BigDecimal("1500.00"));
        expenseApplyMain.addExpenseApplyDetail(detail1);
        
        ExpenseApplyDetail detail2 = new ExpenseApplyDetail();
        detail2.setAmount(new BigDecimal("2500.00"));
        expenseApplyMain.addExpenseApplyDetail(detail2);
        
        expenseApplyMain.syncAmountFromDetails();
        
        assertEquals(new BigDecimal("4000.00"), expenseApplyMain.getApplyAmount());
        assertEquals(new BigDecimal("4000.00"), expenseApplyMain.getTotalAmount());
    }

    @Test
    void testValidateBasicInfo() {
        assertTrue(expenseApplyMain.validateBasicInfo());
        
        // 测试缺失必要信息的情况
        expenseApplyMain.setApplyNo(null);
        assertFalse(expenseApplyMain.validateBasicInfo());
    }

    @Test
    void testCanSubmitForApproval() {
        // 草稿状态且有明细项时可以提交
        expenseApplyMain.setStatus(ApplyStatusEnum.DRAFT.getCode());
        ExpenseApplyDetail detail = new ExpenseApplyDetail();
        detail.setAmount(new BigDecimal("1000.00"));
        expenseApplyMain.addExpenseApplyDetail(detail);
        
        assertTrue(expenseApplyMain.canSubmitForApproval());
        
        // 没有明细项时不能提交
        ExpenseApplyMain emptyMain = new ExpenseApplyMain();
        emptyMain.setApplyNo("TEST-001");
        emptyMain.setApplyType(1);
        emptyMain.setApplyUserId(1001L);
        emptyMain.setApplyDeptId(2001L);
        emptyMain.setReason("测试");
        emptyMain.setStatus(ApplyStatusEnum.DRAFT.getCode());
        
        assertFalse(emptyMain.canSubmitForApproval());
    }

    @Test
    void testSubmitForApproval() {
        expenseApplyMain.setStatus(ApplyStatusEnum.DRAFT.getCode());
        ExpenseApplyDetail detail = new ExpenseApplyDetail();
        detail.setAmount(new BigDecimal("1000.00"));
        expenseApplyMain.addExpenseApplyDetail(detail);
        
        expenseApplyMain.submitForApproval();
        
        assertEquals(ApplyStatusEnum.APPROVING.getCode(), expenseApplyMain.getStatus());
    }

    @Test
    void testGetDetailCount() {
        assertEquals(0, expenseApplyMain.getDetailCount());
        
        ExpenseApplyDetail detail = new ExpenseApplyDetail();
        expenseApplyMain.addExpenseApplyDetail(detail);
        
        assertEquals(1, expenseApplyMain.getDetailCount());
    }
}