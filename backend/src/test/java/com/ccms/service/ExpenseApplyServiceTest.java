package com.ccms.service;

import com.ccms.entity.expense.ExpenseApply;
import com.ccms.entity.expense.ExpenseApplyDetail;
import com.ccms.repository.expense.ExpenseApplyRepository;
import com.ccms.repository.expense.ExpenseApplyDetailRepository;
import com.ccms.service.impl.ExpenseApplyServiceImpl;
import com.ccms.service.impl.BudgetServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 费用申请服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ExpenseApplyServiceTest {

    @Mock
    private ExpenseApplyRepository expenseApplyRepository;

    @Mock
    private ExpenseApplyDetailRepository expenseApplyDetailRepository;

    @Mock
    private BudgetServiceImpl budgetService;

    @InjectMocks
    private ExpenseApplyServiceImpl expenseApplyService;

    private ExpenseApply draftExpenseApply;
    private ExpenseApply submittedExpenseApply;
    private ExpenseApply approvedExpenseApply;
    private ExpenseApplyDetail expenseDetail;

    @BeforeEach
    void setUp() {
        // 创建草稿费用申请
        draftExpenseApply = new ExpenseApply();
        draftExpenseApply.setId(1L);
        draftExpenseApply.setApplyNo("EA20250001");
        draftExpenseApply.setDeptId(101L);
        draftExpenseApply.setApplicantId(1001L);
        draftExpenseApply.setApplyDate(LocalDate.now());
        draftExpenseApply.setTotalAmount(new BigDecimal("5000.00"));
        draftExpenseApply.setApplyReason("项目差旅费用申请");
        draftExpenseApply.setStatus(0); // 草稿
        draftExpenseApply.setApprovalStatus(0); // 待提交
        draftExpenseApply.setCreateTime(LocalDateTime.now());

        // 创建已提交费用申请
        submittedExpenseApply = new ExpenseApply();
        submittedExpenseApply.setId(2L);
        submittedExpenseApply.setApplyNo("EA20250002");
        submittedExpenseApply.setStatus(1); // 审批中
        submittedExpenseApply.setApprovalStatus(1); // 审批中

        // 创建已批准费用申请
        approvedExpenseApply = new ExpenseApply();
        approvedExpenseApply.setId(3L);
        approvedExpenseApply.setApplyNo("EA20250003");
        approvedExpenseApply.setStatus(3); // 已批准
        approvedExpenseApply.setApprovalStatus(3); // 已批准

        // 创建费用明细
        expenseDetail = new ExpenseApplyDetail();
        expenseDetail.setId(1L);
        expenseDetail.setExpenseApplyId(1L);
        expenseDetail.setExpenseType(1); // 差旅费
        expenseDetail.setAmount(new BigDecimal("3000.00"));
        expenseDetail.setExpenseDate(LocalDate.now().minusDays(5));
        expenseDetail.setDescription("北京至上海往返机票");
    }

    @Test
    void testCreateExpenseApply_Success() {
        // 模拟Repository保存
        when(expenseApplyRepository.save(any(ExpenseApply.class))).thenReturn(draftExpenseApply);

        // 执行创建费用申请
        ExpenseApply result = expenseApplyService.createExpenseApply(draftExpenseApply);

        // 验证结果
        assertNotNull(result);
        assertEquals("项目差旅费用申请", result.getApplyReason());
        assertEquals(0, result.getStatus().intValue()); // 草稿状态
        assertEquals(0, result.getApprovalStatus().intValue()); // 待提交
        assertNotNull(result.getApplyNo());
        
        verify(expenseApplyRepository, times(1)).save(any(ExpenseApply.class));
    }

    @Test
    void testUpdateExpenseApply_Success() {
        // 模拟Repository查找和保存
        when(expenseApplyRepository.findById(1L)).thenReturn(Optional.of(draftExpenseApply));
        when(expenseApplyRepository.save(any(ExpenseApply.class))).thenReturn(draftExpenseApply);

        // 准备更新数据
        ExpenseApply updateData = new ExpenseApply();
        updateData.setId(1L);
        updateData.setApplyReason("更新后的申请原因");
        updateData.setTotalAmount(new BigDecimal("8000.00"));

        // 执行更新
        ExpenseApply result = expenseApplyService.updateExpenseApply(updateData);

        // 验证结果
        assertNotNull(result);
        assertEquals("更新后的申请原因", result.getApplyReason());
        assertEquals(new BigDecimal("8000.00"), result.getTotalAmount());
        
        verify(expenseApplyRepository, times(1)).findById(1L);
        verify(expenseApplyRepository, times(1)).save(any(ExpenseApply.class));
    }

    @Test
    void testUpdateExpenseApply_ApplyNotFound() {
        // 模拟Repository返回空
        when(expenseApplyRepository.findById(999L)).thenReturn(Optional.empty());

        // 准备更新数据
        ExpenseApply updateData = new ExpenseApply();
        updateData.setId(999L);

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseApplyService.updateExpenseApply(updateData);
        });

        assertEquals("费用申请不存在", exception.getMessage());
        verify(expenseApplyRepository, times(1)).findById(999L);
        verify(expenseApplyRepository, never()).save(any(ExpenseApply.class));
    }

    @Test
    void testUpdateExpenseApply_NonDraftStatus() {
        // 模拟Repository返回已提交的申请
        when(expenseApplyRepository.findById(2L)).thenReturn(Optional.of(submittedExpenseApply));

        // 准备更新数据
        ExpenseApply updateData = new ExpenseApply();
        updateData.setId(2L);

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseApplyService.updateExpenseApply(updateData);
        });

        assertEquals("非草稿状态的费用申请不允许修改", exception.getMessage());
        verify(expenseApplyRepository, times(1)).findById(2L);
        verify(expenseApplyRepository, never()).save(any(ExpenseApply.class));
    }

    @Test
    void testSubmitForApproval_Success() {
        // 模拟Repository查找和预算检查
        when(expenseApplyRepository.findById(1L)).thenReturn(Optional.of(draftExpenseApply));
        when(expenseApplyDetailRepository.findByExpenseApplyId(1L)).thenReturn(List.of(expenseDetail));
        when(budgetService.checkBudgetAvailability(101L, 1, new BigDecimal("5000.00"), 2025)).thenReturn(true);
        when(expenseApplyRepository.save(any(ExpenseApply.class))).thenReturn(draftExpenseApply);

        // 执行提交审批
        expenseApplyService.submitForApproval(1L);

        // 验证状态更新
        assertEquals(1, draftExpenseApply.getStatus().intValue()); // 审批中
        assertEquals(1, draftExpenseApply.getApprovalStatus().intValue()); // 审批中
        
        verify(expenseApplyRepository, times(1)).findById(1L);
        verify(expenseApplyDetailRepository, times(1)).findByExpenseApplyId(1L);
        verify(budgetService, times(1)).checkBudgetAvailability(101L, 1, new BigDecimal("5000.00"), 2025);
        verify(expenseApplyRepository, times(1)).save(any(ExpenseApply.class));
    }

    @Test
    void testSubmitForApproval_ApplyNotFound() {
        // 模拟Repository返回空
        when(expenseApplyRepository.findById(999L)).thenReturn(Optional.empty());

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseApplyService.submitForApproval(999L);
        });

        assertEquals("费用申请不存在", exception.getMessage());
        verify(expenseApplyRepository, times(1)).findById(999L);
        verify(expenseApplyDetailRepository, never()).findByExpenseApplyId(anyLong());
    }

    @Test
    void testSubmitForApproval_InvalidStatus() {
        // 模拟Repository返回已提交的申请
        when(expenseApplyRepository.findById(2L)).thenReturn(Optional.of(submittedExpenseApply));

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseApplyService.submitForApproval(2L);
        });

        assertEquals("费用申请状态不允许提交审批", exception.getMessage());
        verify(expenseApplyRepository, times(1)).findById(2L);
        verify(expenseApplyDetailRepository, never()).findByExpenseApplyId(anyLong());
    }

    @Test
    void testSubmitForApproval_NoDetails() {
        // 模拟Repository查找
        when(expenseApplyRepository.findById(1L)).thenReturn(Optional.of(draftExpenseApply));
        when(expenseApplyDetailRepository.findByExpenseApplyId(1L)).thenReturn(List.of());

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseApplyService.submitForApproval(1L);
        });

        assertEquals("费用申请明细不能为空", exception.getMessage());
        verify(expenseApplyRepository, times(1)).findById(1L);
        verify(expenseApplyDetailRepository, times(1)).findByExpenseApplyId(1L);
        verify(expenseApplyRepository, never()).save(any(ExpenseApply.class));
    }

    @Test
    void testSubmitForApproval_BudgetExceeded() {
        // 模拟Repository查找和预算检查
        when(expenseApplyRepository.findById(1L)).thenReturn(Optional.of(draftExpenseApply));
        when(expenseApplyDetailRepository.findByExpenseApplyId(1L)).thenReturn(List.of(expenseDetail));
        when(budgetService.checkBudgetAvailability(101L, 1, new BigDecimal("5000.00"), 2025)).thenReturn(false);

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseApplyService.submitForApproval(1L);
        });

        assertEquals("预算不足，无法提交审批", exception.getMessage());
        verify(expenseApplyRepository, times(1)).findById(1L);
        verify(expenseApplyDetailRepository, times(1)).findByExpenseApplyId(1L);
        verify(budgetService, times(1)).checkBudgetAvailability(101L, 1, new BigDecimal("5000.00"), 2025);
        verify(expenseApplyRepository, never()).save(any(ExpenseApply.class));
    }

    @Test
    void testApproveExpenseApply_Approved() {
        // 模拟Repository查找和保存
        when(expenseApplyRepository.findById(2L)).thenReturn(Optional.of(submittedExpenseApply));
        when(expenseApplyDetailRepository.findByExpenseApplyId(2L)).thenReturn(List.of(expenseDetail));
        when(budgetService.deductBudget(101L, 1, new BigDecimal("5000.00"), 2025)).thenReturn(true);
        when(expenseApplyRepository.save(any(ExpenseApply.class))).thenReturn(submittedExpenseApply);

        // 执行审批通过
        expenseApplyService.approveExpenseApply(2L, true, "同意申请");

        // 验证状态更新
        assertEquals(3, submittedExpenseApply.getStatus().intValue()); // 已批准
        assertEquals(3, submittedExpenseApply.getApprovalStatus().intValue()); // 已批准
        assertEquals("同意申请", submittedExpenseApply.getApprovalComment());
        assertNotNull(submittedExpenseApply.getApprovalTime());
        
        verify(expenseApplyRepository, times(1)).findById(2L);
        verify(expenseApplyDetailRepository, times(1)).findByExpenseApplyId(2L);
        verify(budgetService, times(1)).deductBudget(101L, 1, new BigDecimal("5000.00"), 2025);
        verify(expenseApplyRepository, times(1)).save(any(ExpenseApply.class));
    }

    @Test
    void testApproveExpenseApply_Rejected() {
        // 模拟Repository查找和保存
        when(expenseApplyRepository.findById(2L)).thenReturn(Optional.of(submittedExpenseApply));
        when(expenseApplyRepository.save(any(ExpenseApply.class))).thenReturn(submittedExpenseApply);

        // 执行审批拒绝
        expenseApplyService.approveExpenseApply(2L, false, "申请原因不充分");

        // 验证状态更新
        assertEquals(4, submittedExpenseApply.getStatus().intValue()); // 已拒绝
        assertEquals(2, submittedExpenseApply.getApprovalStatus().intValue()); // 已拒绝
        assertEquals("申请原因不充分", submittedExpenseApply.getApprovalComment());
        assertNotNull(submittedExpenseApply.getApprovalTime());
        
        verify(expenseApplyRepository, times(1)).findById(2L);
        verify(expenseApplyRepository, times(1)).save(any(ExpenseApply.class));
        verify(budgetService, never()).deductBudget(anyLong(), anyInt(), any(BigDecimal.class), anyInt());
    }

    @Test
    void testAddExpenseDetail_Success() {
        // 模拟Repository查找和保存
        when(expenseApplyRepository.findById(1L)).thenReturn(Optional.of(draftExpenseApply));
        when(expenseApplyDetailRepository.save(any(ExpenseApplyDetail.class))).thenReturn(expenseDetail);

        // 执行添加明细
        ExpenseApplyDetail result = expenseApplyService.addExpenseDetail(expenseDetail);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getExpenseApplyId());
        assertEquals(1, result.getExpenseType().intValue());
        
        verify(expenseApplyRepository, times(1)).findById(1L);
        verify(expenseApplyDetailRepository, times(1)).save(any(ExpenseApplyDetail.class));
    }

    @Test
    void testAddExpenseDetail_ApplyNotFound() {
        // 模拟Repository返回空
        when(expenseApplyRepository.findById(999L)).thenReturn(Optional.empty());

        // 准备明细数据
        ExpenseApplyDetail detail = new ExpenseApplyDetail();
        detail.setExpenseApplyId(999L);

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseApplyService.addExpenseDetail(detail);
        });

        assertEquals("费用申请不存在", exception.getMessage());
        verify(expenseApplyRepository, times(1)).findById(999L);
        verify(expenseApplyDetailRepository, never()).save(any(ExpenseApplyDetail.class));
    }

    @Test
    void testUpdateExpenseDetail_Success() {
        // 模拟Repository查找和保存
        when(expenseApplyDetailRepository.findById(1L)).thenReturn(Optional.of(expenseDetail));
        when(expenseApplyRepository.findById(1L)).thenReturn(Optional.of(draftExpenseApply));
        when(expenseApplyDetailRepository.save(any(ExpenseApplyDetail.class))).thenReturn(expenseDetail);

        // 准备更新数据
        ExpenseApplyDetail updateData = new ExpenseApplyDetail();
        updateData.setId(1L);
        updateData.setExpenseApplyId(1L);
        updateData.setAmount(new BigDecimal("4000.00"));
        updateData.setDescription("更新后的描述");

        // 执行更新
        ExpenseApplyDetail result = expenseApplyService.updateExpenseDetail(updateData);

        // 验证结果
        assertNotNull(result);
        assertEquals(new BigDecimal("4000.00"), result.getAmount());
        
        verify(expenseApplyDetailRepository, times(1)).findById(1L);
        verify(expenseApplyRepository, times(1)).findById(1L);
        verify(expenseApplyDetailRepository, times(1)).save(any(ExpenseApplyDetail.class));
    }

    @Test
    void testDeleteExpenseApply_Success() {
        // 模拟Repository查找
        when(expenseApplyRepository.findById(1L)).thenReturn(Optional.of(draftExpenseApply));
        when(expenseApplyDetailRepository.findByExpenseApplyId(1L)).thenReturn(List.of());

        // 执行删除
        expenseApplyService.deleteExpenseApply(1L);

        // 验证删除调用
        verify(expenseApplyRepository, times(1)).findById(1L);
        verify(expenseApplyDetailRepository, times(1)).findByExpenseApplyId(1L);
        verify(expenseApplyRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteExpenseApply_NonDraftStatus() {
        // 模拟Repository返回已提交的申请
        when(expenseApplyRepository.findById(2L)).thenReturn(Optional.of(submittedExpenseApply));

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseApplyService.deleteExpenseApply(2L);
        });

        assertEquals("非草稿状态的费用申请不允许删除", exception.getMessage());
        verify(expenseApplyRepository, times(1)).findById(2L);
        verify(expenseApplyDetailRepository, never()).findByExpenseApplyId(anyLong());
        verify(expenseApplyRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetExpenseApplyById_Found() {
        // 模拟Repository返回申请
        when(expenseApplyRepository.findById(1L)).thenReturn(Optional.of(draftExpenseApply));
        when(expenseApplyDetailRepository.findByExpenseApplyId(1L)).thenReturn(List.of(expenseDetail));

        // 执行查询
        ExpenseApply result = expenseApplyService.getExpenseApplyById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("项目差旅费用申请", result.getApplyReason());
        assertNotNull(result.getDetails());
        
        verify(expenseApplyRepository, times(1)).findById(1L);
        verify(expenseApplyDetailRepository, times(1)).findByExpenseApplyId(1L);
    }

    @Test
    void testGetExpenseApplyById_NotFound() {
        // 模拟Repository返回空
        when(expenseApplyRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行查询
        ExpenseApply result = expenseApplyService.getExpenseApplyById(999L);

        // 验证结果
        assertNull(result);
        verify(expenseApplyRepository, times(1)).findById(999L);
        verify(expenseApplyDetailRepository, never()).findByExpenseApplyId(anyLong());
    }

    @Test
    void testGetExpenseAppliesByApplicant() {
        // 模拟Repository返回列表
        when(expenseApplyRepository.findByApplicantId(1001L)).thenReturn(List.of(draftExpenseApply, submittedExpenseApply));

        // 执行查询
        List<ExpenseApply> result = expenseApplyService.getExpenseAppliesByApplicant(1001L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(expenseApplyRepository, times(1)).findByApplicantId(1001L);
    }

    @Test
    void testGetExpenseAppliesByDept() {
        // 模拟Repository返回列表
        when(expenseApplyRepository.findByDeptId(101L)).thenReturn(List.of(draftExpenseApply, submittedExpenseApply));

        // 执行查询
        List<ExpenseApply> result = expenseApplyService.getExpenseAppliesByDept(101L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(expenseApplyRepository, times(1)).findByDeptId(101L);
    }
}