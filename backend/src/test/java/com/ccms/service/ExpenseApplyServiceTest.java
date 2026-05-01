package com.ccms.service;

import com.ccms.BaseTest;
import com.ccms.entity.expense.ExpenseApplyDetail;
import com.ccms.entity.expense.ExpenseApplyMain;
import com.ccms.repository.ExpenseApplyDetailRepository;
import com.ccms.repository.ExpenseApplyMainRepository;
import com.ccms.service.impl.ExpenseApplyServiceImpl;
import com.ccms.service.BudgetService;
import com.ccms.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 费用申请服务测试类
 */
@DisplayName("费用申请服务测试")
class ExpenseApplyServiceTest extends BaseTest {

    @Mock
    private ExpenseApplyMainRepository expenseApplyMainRepository;
    
    @Mock
    private ExpenseApplyDetailRepository expenseApplyDetailRepository;
    
    @Mock
    private BudgetService budgetService;
    
    @Mock
    private MessageService messageService;

    @InjectMocks
    private ExpenseApplyServiceImpl expenseApplyService;

    private ExpenseApplyMain testExpenseApplyMain;
    private ExpenseApplyDetail testExpenseApplyDetail;

    @BeforeEach
    public void setUp() {
        super.setUp();
        
        // 创建测试费用申请主表数据
        testExpenseApplyMain = new ExpenseApplyMain();
        testExpenseApplyMain.setId(1L);
        testExpenseApplyMain.setApplyNo("EXP2024010001");
        testExpenseApplyMain.setTitle("差旅费申请");
        testExpenseApplyMain.setDepartment("技术部");
        testExpenseApplyMain.setApplicant("张三");
        testExpenseApplyMain.setTotalAmount(new BigDecimal("5000.00"));
        testExpenseApplyMain.setStatus("PENDING");
        testExpenseApplyMain.setApplyTime(LocalDateTime.now());
        testExpenseApplyMain.setCreateTime(LocalDateTime.now());
        testExpenseApplyMain.setUpdateTime(LocalDateTime.now());

        // 创建测试费用申请明细数据
        testExpenseApplyDetail = new ExpenseApplyDetail();
        testExpenseApplyDetail.setId(1L);
        testExpenseApplyDetail.setExpenseApplyMainId(1L);
        testExpenseApplyDetail.setFeeType("差旅费");
        testExpenseApplyDetail.setAmount(new BigDecimal("3000.00"));
        testExpenseApplyDetail.setRemarks("往返车票费用");
        testExpenseApplyDetail.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建费用申请成功")
    void createExpenseApply_success() {
        // 设置Mock行为
        when(expenseApplyMainRepository.save(any(ExpenseApplyMain.class)))
            .thenReturn(testExpenseApplyMain);
        when(expenseApplyDetailRepository.save(any(ExpenseApplyDetail.class)))
            .thenReturn(testExpenseApplyDetail);
        when(budgetService.checkBudgetLimit(anyString(), anyInt(), any(BigDecimal.class)))
            .thenReturn(true);

        // 执行测试
        ExpenseApplyMain result = expenseApplyService.createExpenseApply(
            testExpenseApplyMain, 
            Arrays.asList(testExpenseApplyDetail)
        );

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getApplyNo()).startsWith("EXP");
        
        // 验证Mock调用
        verify(expenseApplyMainRepository, times(1)).save(any(ExpenseApplyMain.class));
        verify(expenseApplyDetailRepository, times(1)).save(any(ExpenseApplyDetail.class));
        verify(budgetService, times(1)).checkBudgetLimit(anyString(), anyInt(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("创建费用申请失败 - 预算不足")
    void createExpenseApply_budgetInsufficient() {
        // 设置Mock行为 - 预算检查失败
        when(budgetService.checkBudgetLimit(anyString(), anyInt(), any(BigDecimal.class)))
            .thenReturn(false);

        // 执行测试并验证异常
        assertThatThrownBy(() -> 
            expenseApplyService.createExpenseApply(
                testExpenseApplyMain, 
                Arrays.asList(testExpenseApplyDetail)
            )
        ).isInstanceOf(RuntimeException.class)
         .hasMessageContaining("预算不足");

        // 验证Mock调用 - 数据库保存不会被执行
        verify(expenseApplyMainRepository, never()).save(any(ExpenseApplyMain.class));
        verify(expenseApplyDetailRepository, never()).save(any(ExpenseApplyDetail.class));
    }

    @Test
    @DisplayName("根据ID获取费用申请成功")
    void getExpenseApplyById_success() {
        // 设置Mock行为
        when(expenseApplyMainRepository.findById(1L)).thenReturn(Optional.of(testExpenseApplyMain));

        // 执行测试
        Optional<ExpenseApplyMain> result = expenseApplyService.getExpenseApplyById(1L);

        // 验证结果
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getTitle()).isEqualTo("差旅费申请");
        
        // 验证Mock调用
        verify(expenseApplyMainRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("根据状态获取费用申请列表")
    void getExpenseAppliesByStatus_success() {
        // 创建测试数据列表
        List<ExpenseApplyMain> applies = Arrays.asList(testExpenseApplyMain);
        
        // 设置Mock行为
        when(expenseApplyMainRepository.findByStatus("PENDING")).thenReturn(applies);

        // 执行测试
        List<ExpenseApplyMain> result = expenseApplyService.getExpenseAppliesByStatus("PENDING");

        // 验证结果
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
        
        // 验证Mock调用
        verify(expenseApplyMainRepository, times(1)).findByStatus("PENDING");
    }

    @Test
    @DisplayName("获取费用申请详情列表")
    void getExpenseApplyDetails_success() {
        // 创建测试数据
        List<ExpenseApplyDetail> details = Arrays.asList(testExpenseApplyDetail);
        
        // 设置Mock行为
        when(expenseApplyDetailRepository.findByExpenseApplyMainId(1L)).thenReturn(details);

        // 执行测试
        List<ExpenseApplyDetail> result = expenseApplyService.getExpenseApplyDetails(1L);

        // 验证结果
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFeeType()).isEqualTo("差旅费");
        
        // 验证Mock调用
        verify(expenseApplyDetailRepository, times(1)).findByExpenseApplyMainId(1L);
    }

    @Test
    @DisplayName("更新费用申请状态成功")
    void updateExpenseApplyStatus_success() {
        // 设置Mock行为
        when(expenseApplyMainRepository.findById(1L)).thenReturn(Optional.of(testExpenseApplyMain));
        when(expenseApplyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(testExpenseApplyMain);

        // 执行测试
        boolean result = expenseApplyService.updateExpenseApplyStatus(1L, "APPROVED");

        // 验证结果
        assertThat(result).isTrue();
        
        // 验证Mock调用
        verify(expenseApplyMainRepository, times(1)).save(any(ExpenseApplyMain.class));
    }

    @Test
    @DisplayName("更新费用申请状态失败 - 申请不存在")
    void updateExpenseApplyStatus_notFound() {
        // 设置Mock行为 - 返回空的Optional
        when(expenseApplyMainRepository.findById(99L)).thenReturn(Optional.empty());

        // 执行测试
        boolean result = expenseApplyService.updateExpenseApplyStatus(99L, "APPROVED");

        // 验证结果
        assertThat(result).isFalse();
        
        // 验证Mock调用
        verify(expenseApplyMainRepository, never()).save(any(ExpenseApplyMain.class));
    }

    @Test
    @DisplayName("提交费用申请审批")
    void submitForApproval_success() {
        // 设置Mock行为
        when(expenseApplyMainRepository.findById(1L)).thenReturn(Optional.of(testExpenseApplyMain));
        when(expenseApplyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(testExpenseApplyMain);

        // 执行测试
        boolean result = expenseApplyService.submitForApproval(1L);

        // 验证结果
        assertThat(result).isTrue();
        
        // 验证Mock调用
        verify(expenseApplyMainRepository, times(1)).save(any(ExpenseApplyMain.class));
        // TODO: 验证审批流程相关的调用
    }

    @Test
    @DisplayName("费用申请金额统计")
    void getExpenseStatistics_success() {
        // 创建测试数据
        List<ExpenseApplyMain> applies = Arrays.asList(
            createExpenseApplyWithAmount("1000.00"),
            createExpenseApplyWithAmount("2000.00")
        );
        
        // 设置Mock行为
        when(expenseApplyMainRepository.findByDepartment("技术部")).thenReturn(applies);

        // 执行测试 - 这里需要实现统计方法
        // BigDecimal total = expenseApplyService.getDepartmentExpenseTotal("技术部");
        // assertThat(total).isEqualTo(new BigDecimal("3000.00"));
    }

    @Test
    @DisplayName("删除费用申请")
    void deleteExpenseApply_success() {
        // 设置Mock行为
        when(expenseApplyMainRepository.existsById(1L)).thenReturn(true);

        // 执行测试
        boolean result = expenseApplyService.deleteExpenseApply(1L);

        // 验证结果
        assertThat(result).isTrue();
        
        // 验证Mock调用
        verify(expenseApplyMainRepository, times(1)).deleteById(1L);
        verify(expenseApplyDetailRepository, times(1)).deleteByExpenseApplyMainId(1L);
    }

    @Test
    @DisplayName("删除费用申请失败 - 申请不存在")
    void deleteExpenseApply_notFound() {
        // 设置Mock行为
        when(expenseApplyMainRepository.existsById(99L)).thenReturn(false);

        // 执行测试
        boolean result = expenseApplyService.deleteExpenseApply(99L);

        // 验证结果
        assertThat(result).isFalse();
        
        // 验证Mock调用 - 删除方法不会被调用
        verify(expenseApplyMainRepository, never()).deleteById(anyLong());
        verify(expenseApplyDetailRepository, never()).deleteByExpenseApplyMainId(anyLong());
    }

    // 辅助方法
    private ExpenseApplyMain createExpenseApplyWithAmount(String amount) {
        ExpenseApplyMain apply = new ExpenseApplyMain();
        apply.setTotalAmount(new BigDecimal(amount));
        return apply;
    }
}