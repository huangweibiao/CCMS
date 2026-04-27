package com.ccms.service;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.expense.ExpenseReimburseDetail;
import com.ccms.repository.expense.ExpenseReimburseRepository;
import com.ccms.repository.expense.ExpenseReimburseDetailRepository;
import com.ccms.service.impl.ExpenseReimburseServiceImpl;
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
 * 费用报销服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class ExpenseReimburseServiceTest {

    @Mock
    private ExpenseReimburseRepository expenseReimburseRepository;

    @Mock
    private ExpenseReimburseDetailRepository expenseReimburseDetailRepository;

    @Mock
    private BudgetServiceImpl budgetService;

    @InjectMocks
    private ExpenseReimburseServiceImpl expenseReimburseService;

    private ExpenseReimburse draftReimburse;
    private ExpenseReimburse submittedReimburse;
    private ExpenseReimburse approvedReimburse;
    private ExpenseReimburseDetail reimburseDetail;

    @BeforeEach
    void setUp() {
        // 创建草稿报销申请
        draftReimburse = new ExpenseReimburse();
        draftReimburse.setId(1L);
        draftReimburse.setReimburseNo("ER20250001");
        draftReimburse.setDeptId(101L);
        draftReimburse.setApplicantId(1001L);
        draftReimburse.setReimburseDate(LocalDate.now());
        draftReimburse.setTotalAmount(new BigDecimal("3000.00"));
        draftReimburse.setReimburseReason("差旅费用报销");
        draftReimburse.setStatus(0); // 草稿
        draftReimburse.setApprovalStatus(0); // 待提交
        draftReimburse.setCreateTime(LocalDateTime.now());

        // 创建已提交报销申请
        submittedReimburse = new ExpenseReimburse();
        submittedReimburse.setId(2L);
        submittedReimburse.setReimburseNo("ER20250002");
        submittedReimburse.setStatus(1); // 审批中
        submittedReimburse.setApprovalStatus(1); // 审批中

        // 创建已批准报销申请
        approvedReimburse = new ExpenseReimburse();
        approvedReimburse.setId(3L);
        approvedReimburse.setReimburseNo("ER20250003");
        approvedReimburse.setStatus(3); // 已批准
        approvedReimburse.setApprovalStatus(3); // 已批准

        // 创建报销明细
        reimburseDetail = new ExpenseReimburseDetail();
        reimburseDetail.setId(1L);
        reimburseDetail.setReimburseId(1L);
        reimburseDetail.setExpenseType(1); // 差旅费
        reimburseDetail.setAmount(new BigDecimal("1000.00"));
        reimburseDetail.setExpenseDate(LocalDate.now().minusDays(3));
        reimburseDetail.setDescription("交通费用报销");
        reimburseDetail.setVoucherNo("VOUCHER001");
        reimburseDetail.setAttachmentPath("/attachments/receipt001.jpg");
    }

    @Test
    void testCreateReimburse_Success() {
        // 模拟Repository保存
        when(expenseReimburseRepository.save(any(ExpenseReimburse.class))).thenReturn(draftReimburse);

        // 执行创建报销申请
        ExpenseReimburse result = expenseReimburseService.createReimburse(draftReimburse);

        // 验证结果
        assertNotNull(result);
        assertEquals("差旅费用报销", result.getReimburseReason());
        assertEquals(0, result.getStatus().intValue()); // 草稿状态
        assertEquals(0, result.getApprovalStatus().intValue()); // 待提交
        assertNotNull(result.getReimburseNo());
        
        verify(expenseReimburseRepository, times(1)).save(any(ExpenseReimburse.class));
    }

    @Test
    void testUpdateReimburse_Success() {
        // 模拟Repository查找和保存
        when(expenseReimburseRepository.findById(1L)).thenReturn(Optional.of(draftReimburse));
        when(expenseReimburseRepository.save(any(ExpenseReimburse.class))).thenReturn(draftReimburse);

        // 准备更新数据
        ExpenseReimburse updateData = new ExpenseReimburse();
        updateData.setId(1L);
        updateData.setReimburseReason("更新后的报销原因");
        updateData.setTotalAmount(new BigDecimal("4000.00"));

        // 执行更新
        ExpenseReimburse result = expenseReimburseService.updateReimburse(updateData);

        // 验证结果
        assertNotNull(result);
        assertEquals("更新后的报销原因", result.getReimburseReason());
        assertEquals(new BigDecimal("4000.00"), result.getTotalAmount());
        
        verify(expenseReimburseRepository, times(1)).findById(1L);
        verify(expenseReimburseRepository, times(1)).save(any(ExpenseReimburse.class));
    }

    @Test
    void testUpdateReimburse_ReimburseNotFound() {
        // 模拟Repository返回空
        when(expenseReimburseRepository.findById(999L)).thenReturn(Optional.empty());

        // 准备更新数据
        ExpenseReimburse updateData = new ExpenseReimburse();
        updateData.setId(999L);

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseReimburseService.updateReimburse(updateData);
        });

        assertEquals("报销申请不存在", exception.getMessage());
        verify(expenseReimburseRepository, times(1)).findById(999L);
        verify(expenseReimburseRepository, never()).save(any(ExpenseReimburse.class));
    }

    @Test
    void testUpdateReimburse_NonDraftStatus() {
        // 模拟Repository返回已提交的报销
        when(expenseReimburseRepository.findById(2L)).thenReturn(Optional.of(submittedReimburse));

        // 准备更新数据
        ExpenseReimburse updateData = new ExpenseReimburse();
        updateData.setId(2L);

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseReimburseService.updateReimburse(updateData);
        });

        assertEquals("非草稿状态的报销申请不允许修改", exception.getMessage());
        verify(expenseReimburseRepository, times(1)).findById(2L);
        verify(expenseReimburseRepository, never()).save(any(ExpenseReimburse.class));
    }

    @Test
    void testSubmitForReimburseApproval_Success() {
        // 模拟Repository查找和保存
        when(expenseReimburseRepository.findById(1L)).thenReturn(Optional.of(draftReimburse));
        when(expenseReimburseDetailRepository.findByReimburseId(1L)).thenReturn(List.of(reimburseDetail));
        when(expenseReimburseRepository.save(any(ExpenseReimburse.class))).thenReturn(draftReimburse);

        // 执行提交审批
        expenseReimburseService.submitForReimburseApproval(1L);

        // 验证状态更新
        assertEquals(1, draftReimburse.getStatus().intValue()); // 审批中
        assertEquals(1, draftReimburse.getApprovalStatus().intValue()); // 审批中
        
        verify(expenseReimburseRepository, times(1)).findById(1L);
        verify(expenseReimburseDetailRepository, times(1)).findByReimburseId(1L);
        verify(expenseReimburseRepository, times(1)).save(any(ExpenseReimburse.class));
    }

    @Test
    void testSubmitForReimburseApproval_ReimburseNotFound() {
        // 模拟Repository返回空
        when(expenseReimburseRepository.findById(999L)).thenReturn(Optional.empty());

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseReimburseService.submitForReimburseApproval(999L);
        });

        assertEquals("报销申请不存在", exception.getMessage());
        verify(expenseReimburseRepository, times(1)).findById(999L);
        verify(expenseReimburseDetailRepository, never()).findByReimburseId(anyLong());
    }

    @Test
    void testSubmitForReimburseApproval_InvalidStatus() {
        // 模拟Repository返回已提交的报销
        when(expenseReimburseRepository.findById(2L)).thenReturn(Optional.of(submittedReimburse));

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseReimburseService.submitForReimburseApproval(2L);
        });

        assertEquals("报销申请状态不允许提交审批", exception.getMessage());
        verify(expenseReimburseRepository, times(1)).findById(2L);
        verify(expenseReimburseDetailRepository, never()).findByReimburseId(anyLong());
    }

    @Test
    void testSubmitForReimburseApproval_NoDetails() {
        // 模拟Repository查找
        when(expenseReimburseRepository.findById(1L)).thenReturn(Optional.of(draftReimburse));
        when(expenseReimburseDetailRepository.findByReimburseId(1L)).thenReturn(List.of());

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseReimburseService.submitForReimburseApproval(1L);
        });

        assertEquals("报销申请明细不能为空", exception.getMessage());
        verify(expenseReimburseRepository, times(1)).findById(1L);
        verify(expenseReimburseDetailRepository, times(1)).findByReimburseId(1L);
        verify(expenseReimburseRepository, never()).save(any(ExpenseReimburse.class));
    }

    @Test
    void testApproveReimburse_Approved() {
        // 模拟Repository查找和保存
        when(expenseReimburseRepository.findById(2L)).thenReturn(Optional.of(submittedReimburse));
        when(expenseReimburseDetailRepository.findByReimburseId(2L)).thenReturn(List.of(reimburseDetail));
        when(expenseReimburseRepository.save(any(ExpenseReimburse.class))).thenReturn(submittedReimburse);

        // 执行审批通过
        expenseReimburseService.approveReimburse(2L, true, "同意报销");

        // 验证状态更新
        assertEquals(3, submittedReimburse.getStatus().intValue()); // 已批准
        assertEquals(3, submittedReimburse.getApprovalStatus().intValue()); // 已批准
        assertEquals("同意报销", submittedReimburse.getApprovalComment());
        assertNotNull(submittedReimburse.getApprovalTime());
        
        verify(expenseReimburseRepository, times(1)).findById(2L);
        verify(expenseReimburseDetailRepository, times(1)).findByReimburseId(2L);
        verify(expenseReimburseRepository, times(1)).save(any(ExpenseReimburse.class));
    }

    @Test
    void testApproveReimburse_Rejected() {
        // 模拟Repository查找和保存
        when(expenseReimburseRepository.findById(2L)).thenReturn(Optional.of(submittedReimburse));
        when(expenseReimburseRepository.save(any(ExpenseReimburse.class))).thenReturn(submittedReimburse);

        // 执行审批拒绝
        expenseReimburseService.approveReimburse(2L, false, "报销凭证不符合要求");

        // 验证状态更新
        assertEquals(4, submittedReimburse.getStatus().intValue()); // 已拒绝
        assertEquals(2, submittedReimburse.getApprovalStatus().intValue()); // 已拒绝
        assertEquals("报销凭证不符合要求", submittedReimburse.getApprovalComment());
        assertNotNull(submittedReimburse.getApprovalTime());
        
        verify(expenseReimburseRepository, times(1)).findById(2L);
        verify(expenseReimburseRepository, times(1)).save(any(ExpenseReimburse.class));
        verify(expenseReimburseDetailRepository, never()).findByReimburseId(anyLong());
    }

    @Test
    void testAddReimburseDetail_Success() {
        // 模拟Repository查找和保存
        when(expenseReimburseRepository.findById(1L)).thenReturn(Optional.of(draftReimburse));
        when(expenseReimburseDetailRepository.save(any(ExpenseReimburseDetail.class))).thenReturn(reimburseDetail);

        // 执行添加明细
        ExpenseReimburseDetail result = expenseReimburseService.addReimburseDetail(reimburseDetail);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getReimburseId());
        assertEquals(1, result.getExpenseType().intValue());
        assertEquals("VOUCHER001", result.getVoucherNo());
        
        verify(expenseReimburseRepository, times(1)).findById(1L);
        verify(expenseReimburseDetailRepository, times(1)).save(any(ExpenseReimburseDetail.class));
    }

    @Test
    void testAddReimburseDetail_ReimburseNotFound() {
        // 模拟Repository返回空
        when(expenseReimburseRepository.findById(999L)).thenReturn(Optional.empty());

        // 准备明细数据
        ExpenseReimburseDetail detail = new ExpenseReimburseDetail();
        detail.setReimburseId(999L);

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseReimburseService.addReimburseDetail(detail);
        });

        assertEquals("报销申请不存在", exception.getMessage());
        verify(expenseReimburseRepository, times(1)).findById(999L);
        verify(expenseReimburseDetailRepository, never()).save(any(ExpenseReimburseDetail.class));
    }

    @Test
    void testUpdateReimburseDetail_Success() {
        // 模拟Repository查找和保存
        when(expenseReimburseDetailRepository.findById(1L)).thenReturn(Optional.of(reimburseDetail));
        when(expenseReimburseRepository.findById(1L)).thenReturn(Optional.of(draftReimburse));
        when(expenseReimburseDetailRepository.save(any(ExpenseReimburseDetail.class))).thenReturn(reimburseDetail);

        // 准备更新数据
        ExpenseReimburseDetail updateData = new ExpenseReimburseDetail();
        updateData.setId(1L);
        updateData.setReimburseId(1L);
        updateData.setAmount(new BigDecimal("1500.00"));
        updateData.setDescription("更新后的报销描述");

        // 执行更新
        ExpenseReimburseDetail result = expenseReimburseService.updateReimburseDetail(updateData);

        // 验证结果
        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), result.getAmount());
        
        verify(expenseReimburseDetailRepository, times(1)).findById(1L);
        verify(expenseReimburseRepository, times(1)).findById(1L);
        verify(expenseReimburseDetailRepository, times(1)).save(any(ExpenseReimburseDetail.class));
    }

    @Test
    void testDeleteReimburse_Success() {
        // 模拟Repository查找
        when(expenseReimburseRepository.findById(1L)).thenReturn(Optional.of(draftReimburse));
        when(expenseReimburseDetailRepository.findByReimburseId(1L)).thenReturn(List.of());

        // 执行删除
        expenseReimburseService.deleteReimburse(1L);

        // 验证删除调用
        verify(expenseReimburseRepository, times(1)).findById(1L);
        verify(expenseReimburseDetailRepository, times(1)).findByReimburseId(1L);
        verify(expenseReimburseRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteReimburse_NonDraftStatus() {
        // 模拟Repository返回已提交的报销
        when(expenseReimburseRepository.findById(2L)).thenReturn(Optional.of(submittedReimburse));

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseReimburseService.deleteReimburse(2L);
        });

        assertEquals("非草稿状态的报销申请不允许删除", exception.getMessage());
        verify(expenseReimburseRepository, times(1)).findById(2L);
        verify(expenseReimburseDetailRepository, never()).findByReimburseId(anyLong());
        verify(expenseReimburseRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetReimburseById_Found() {
        // 模拟Repository返回报销
        when(expenseReimburseRepository.findById(1L)).thenReturn(Optional.of(draftReimburse));
        when(expenseReimburseDetailRepository.findByReimburseId(1L)).thenReturn(List.of(reimburseDetail));

        // 执行查询
        ExpenseReimburse result = expenseReimburseService.getReimburseById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("差旅费用报销", result.getReimburseReason());
        assertNotNull(result.getDetails());
        
        verify(expenseReimburseRepository, times(1)).findById(1L);
        verify(expenseReimburseDetailRepository, times(1)).findByReimburseId(1L);
    }

    @Test
    void testGetReimburseById_NotFound() {
        // 模拟Repository返回空
        when(expenseReimburseRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行查询
        ExpenseReimburse result = expenseReimburseService.getReimburseById(999L);

        // 验证结果
        assertNull(result);
        verify(expenseReimburseRepository, times(1)).findById(999L);
        verify(expenseReimburseDetailRepository, never()).findByReimburseId(anyLong());
    }

    @Test
    void testGetReimbursesByApplicant() {
        // 模拟Repository返回列表
        when(expenseReimburseRepository.findByApplicantId(1001L)).thenReturn(List.of(draftReimburse, submittedReimburse));

        // 执行查询
        List<ExpenseReimburse> result = expenseReimburseService.getReimbursesByApplicant(1001L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(expenseReimburseRepository, times(1)).findByApplicantId(1001L);
    }

    @Test
    void testGetReimbursesByDept() {
        // 模拟Repository返回列表
        when(expenseReimburseRepository.findByDeptId(101L)).thenReturn(List.of(draftReimburse, submittedReimburse));

        // 执行查询
        List<ExpenseReimburse> result = expenseReimburseService.getReimbursesByDept(101L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(expenseReimburseRepository, times(1)).findByDeptId(101L);
    }

    @Test
    void testCalculateReimburseStatistics() {
        // 模拟Repository返回统计信息
        when(expenseReimburseRepository.countByApplicantIdAndMonth(1001L, 2025, 1)).thenReturn(5L);
        when(expenseReimburseRepository.sumAmountByApplicantIdAndMonth(1001L, 2025, 1)).thenReturn(new BigDecimal("15000.00"));
        when(expenseReimburseRepository.countByDeptIdAndMonth(101L, 2025, 1)).thenReturn(20L);
        when(expenseReimburseRepository.sumAmountByDeptIdAndMonth(101L, 2025, 1)).thenReturn(new BigDecimal("50000.00"));

        // 执行统计计算
        var result = expenseReimburseService.calculateReimburseStatistics(1001L, 101L, 2025, 1);

        // 验证结果
        assertNotNull(result);
        assertEquals(5L, result.getApplicationCount().longValue());
        assertEquals(new BigDecimal("15000.00"), result.getApplicationTotalAmount());
        assertEquals(20L, result.getDeptTotalCount().longValue());
        assertEquals(new BigDecimal("50000.00"), result.getDeptTotalAmount());
        
        verify(expenseReimburseRepository, times(1)).countByApplicantIdAndMonth(1001L, 2025, 1);
        verify(expenseReimburseRepository, times(1)).sumAmountByApplicantIdAndMonth(1001L, 2025, 1);
        verify(expenseReimburseRepository, times(1)).countByDeptIdAndMonth(101L, 2025, 1);
        verify(expenseReimburseRepository, times(1)).sumAmountByDeptIdAndMonth(101L, 2025, 1);
    }

    @Test
    void testValidateVoucherNumber_Valid() {
        // 模拟Repository检查凭证号
        when(expenseReimburseDetailRepository.existsByVoucherNo("VOUCHER001")).thenReturn(false);

        // 执行凭证号验证
        boolean result = expenseReimburseService.validateVoucherNumber("VOUCHER001");

        // 验证结果
        assertTrue(result);
        verify(expenseReimburseDetailRepository, times(1)).existsByVoucherNo("VOUCHER001");
    }

    @Test
    void testValidateVoucherNumber_Duplicate() {
        // 模拟Repository检查凭证号（存在重复）
        when(expenseReimburseDetailRepository.existsByVoucherNo("DUPLICATE001")).thenReturn(true);

        // 执行凭证号验证
        boolean result = expenseReimburseService.validateVoucherNumber("DUPLICATE001");

        // 验证结果
        assertFalse(result);
        verify(expenseReimburseDetailRepository, times(1)).existsByVoucherNo("DUPLICATE001");
    }
}