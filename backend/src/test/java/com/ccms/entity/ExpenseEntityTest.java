package com.ccms.entity;

import com.ccms.BaseTest;
import com.ccms.entity.expense.ExpenseApply;
import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.expense.LoanMain;
import com.ccms.entity.expense.LoanRepayment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 费用模块实体类单元测试
 */
class ExpenseEntityTest extends BaseTest {

    @Test
    void shouldCreateExpenseApply() {
        // Given & When
        ExpenseApply apply = new ExpenseApply();
        apply.setId(1L);
        apply.setApplyNo("EA202501010001");
        apply.setTitle("差旅费申请");
        apply.setApplyUserId(1L);
        apply.setApplyUserName("张三");
        apply.setDeptId(1L);
        apply.setDeptName("技术部");
        apply.setApplyDate(LocalDate.now());
        apply.setTotalAmount(new BigDecimal("5000.00"));
        apply.setCurrency("CNY");
        apply.setStatus(0);
        apply.setRemark("出差北京");

        // Then
        assertEquals(1L, apply.getId());
        assertEquals("EA202501010001", apply.getApplyNo());
        assertEquals("差旅费申请", apply.getTitle());
        assertEquals("张三", apply.getApplyUserName());
        assertEquals(new BigDecimal("5000.00"), apply.getTotalAmount());
        assertEquals(0, apply.getStatus());
    }

    @Test
    void shouldCreateExpenseReimburse() {
        // Given & When
        ExpenseReimburse reimburse = new ExpenseReimburse();
        reimburse.setId(1L);
        reimburse.setReimburseNo("RE202501010001");
        reimburse.setTitle("差旅费报销");
        reimburse.setApplyUserId(1L);
        reimburse.setApplyUserName("张三");
        reimburse.setDeptId(1L);
        reimburse.setDeptName("技术部");
        reimburse.setTotalAmount(new BigDecimal("4500.00"));
        reimburse.setRealAmount(new BigDecimal("4500.00"));
        reimburse.setStatus(0);

        // Then
        assertEquals(1L, reimburse.getId());
        assertEquals("RE202501010001", reimburse.getReimburseNo());
        assertEquals("差旅费报销", reimburse.getTitle());
        assertEquals(new BigDecimal("4500.00"), reimburse.getTotalAmount());
        assertEquals(new BigDecimal("4500.00"), reimburse.getRealAmount());
    }

    @Test
    void shouldCreateLoanMain() {
        // Given & When
        LoanMain loan = new LoanMain();
        loan.setId(1L);
        loan.setLoanNo("L202501010001");
        loan.setLoanUserId(1L);
        loan.setLoanDeptId(1L);
        loan.setLoanAmount(new BigDecimal("10000.00"));
        loan.setRepaidAmount(new BigDecimal("3000.00"));
        loan.setExpectRepayDate(LocalDate.now().plusMonths(3));
        loan.setStatus(1);
        loan.setPurpose("项目备用金");

        // Then
        assertEquals(1L, loan.getId());
        assertEquals("L202501010001", loan.getLoanNo());
        assertEquals(new BigDecimal("10000.00"), loan.getLoanAmount());
        assertEquals(new BigDecimal("3000.00"), loan.getRepaidAmount());
        assertEquals(1, loan.getStatus());
        assertEquals("项目备用金", loan.getPurpose());
    }

    @Test
    void shouldCalculateLoanBalance() {
        // Given
        LoanMain loan = new LoanMain();
        loan.setLoanAmount(new BigDecimal("10000.00"));
        loan.setRepaidAmount(new BigDecimal("3000.00"));

        // When & Then
        assertEquals(new BigDecimal("7000.00"), loan.getBalanceAmount());
    }

    @Test
    void shouldCreateLoanRepayment() {
        // Given & When
        LoanRepayment repayment = new LoanRepayment();
        repayment.setId(1L);
        repayment.setLoanId(1L);
        repayment.setRepaymentAmount(new BigDecimal("3000.00"));
        repayment.setRepaymentType(1);
        repayment.setStatus(1);

        // Then
        assertEquals(1L, repayment.getId());
        assertEquals(1L, repayment.getLoanId());
        assertEquals(new BigDecimal("3000.00"), repayment.getRepaymentAmount());
        assertEquals(1, repayment.getRepaymentType());
    }
}
