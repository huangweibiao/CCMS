package com.ccms.service;

import com.ccms.BaseTest;
import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetMain;
import com.ccms.repository.BudgetDetailRepository;
import com.ccms.repository.BudgetMainRepository;
import com.ccms.service.impl.BudgetServiceImpl;
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
 * 预算服务测试类
 */
@DisplayName("预算服务测试")
class BudgetServiceTest extends BaseTest {

    @Mock
    private BudgetMainRepository budgetMainRepository;
    
    @Mock
    private BudgetDetailRepository budgetDetailRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private BudgetMain testBudgetMain;
    private BudgetDetail testBudgetDetail;

    @BeforeEach
    public void setUp() {
        super.setUp();
        
        // 创建测试预算主表数据
        testBudgetMain = new BudgetMain();
        testBudgetMain.setId(1L);
        testBudgetMain.setBudgetYear(2024);
        testBudgetMain.setDepartment("技术部");
        testBudgetMain.setTotalAmount(new BigDecimal("100000.00"));
        testBudgetMain.setUsedAmount(new BigDecimal("20000.00"));
        testBudgetMain.setAvailableAmount(new BigDecimal("80000.00"));
        testBudgetMain.setStatus("ACTIVE");
        testBudgetMain.setPlanName("2024年度技术部预算");
        testBudgetMain.setVersion(1);
        testBudgetMain.setCreateTime(LocalDateTime.now());
        testBudgetMain.setUpdateTime(LocalDateTime.now());

        // 创建测试预算明细数据
        testBudgetDetail = new BudgetDetail();
        testBudgetDetail.setId(1L);
        testBudgetDetail.setBudgetMainId(1L);
        testBudgetDetail.setFeeType("差旅费");
        testBudgetDetail.setBudgetAmount(new BigDecimal("50000.00"));
        testBudgetDetail.setUsedAmount(new BigDecimal("10000.00"));
        testBudgetDetail.setAvailableAmount(new BigDecimal("40000.00"));
        testBudgetDetail.setCreateTime(LocalDateTime.now());
        testBudgetDetail.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("根据ID获取预算主表成功")
    void getBudgetMainById_success() {
        // 设置Mock行为
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(testBudgetMain));

        // 执行测试
        Optional<BudgetMain> result = budgetService.getBudgetMainById(1L);

        // 验证结果
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getBudgetYear()).isEqualTo(2024);
        assertThat(result.get().getDepartment()).isEqualTo("技术部");
        
        // 验证Mock调用
        verify(budgetMainRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("根据部门获取预算主表成功")
    void getBudgetMainByDepartmentAndYear_success() {
        // 设置Mock行为
        when(budgetMainRepository.findByDepartmentAndBudgetYear("技术部", 2024))
            .thenReturn(Optional.of(testBudgetMain));

        // 执行测试
        Optional<BudgetMain> result = budgetService.getBudgetMainByDepartmentAndYear("技术部", 2024);

        // 验证结果
        assertThat(result).isPresent();
        assertThat(result.get().getDepartment()).isEqualTo("技术部");
        assertThat(result.get().getBudgetYear()).isEqualTo(2024);
        
        // 验证Mock调用
        verify(budgetMainRepository, times(1)).findByDepartmentAndBudgetYear("技术部", 2024);
    }

    @Test
    @DisplayName("检查预算限额 - 有足够预算")
    void checkBudgetLimit_sufficientBudget() {
        // 设置Mock行为
        when(budgetMainRepository.findByDepartmentAndBudgetYear("技术部", 2024))
            .thenReturn(Optional.of(testBudgetMain));
        
        // 执行测试
        boolean result = budgetService.checkBudgetLimit("技术部", 2024, new BigDecimal("5000.00"));

        // 验证结果
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查预算限额 - 预算不足")
    void checkBudgetLimit_insufficientBudget() {
        // 修改测试数据的可用金额为较小值
        testBudgetMain.setAvailableAmount(new BigDecimal("1000.00"));
        
        when(budgetMainRepository.findByDepartmentAndBudgetYear("技术部", 2024))
            .thenReturn(Optional.of(testBudgetMain));
        
        // 执行测试
        boolean result = budgetService.checkBudgetLimit("技术部", 2024, new BigDecimal("5000.00"));

        // 验证结果
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("获取预算详情列表")
    void getBudgetDetails_success() {
        // 创建测试数据
        List<BudgetDetail> details = Arrays.asList(testBudgetDetail);
        
        // 设置Mock行为
        when(budgetDetailRepository.findByBudgetMainId(1L)).thenReturn(details);

        // 执行测试
        List<BudgetDetail> result = budgetService.getBudgetDetails(1L);

        // 验证结果
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFeeType()).isEqualTo("差旅费");
        
        // 验证Mock调用
        verify(budgetDetailRepository, times(1)).findByBudgetMainId(1L);
    }

    @Test
    @DisplayName("扣减预算金额成功")
    void deductBudget_success() {
        // 设置Mock行为
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(testBudgetMain));
        when(budgetMainRepository.save(any(BudgetMain.class))).thenReturn(testBudgetMain);

        // 执行测试
        boolean result = budgetService.deductBudget(1L, new BigDecimal("5000.00"));

        // 验证结果
        assertThat(result).isTrue();
        
        // 验证Mock调用 - 预算金额被更新
        verify(budgetMainRepository, times(1)).save(any(BudgetMain.class));
    }

    @Test
    @DisplayName("扣减预算金额失败 - 预算不足")
    void deductBudget_insufficientBudget() {
        // 设置可用金额为较小值
        testBudgetMain.setAvailableAmount(new BigDecimal("1000.00"));
        
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(testBudgetMain));

        // 执行测试
        boolean result = budgetService.deductBudget(1L, new BigDecimal("5000.00"));

        // 验证结果
        assertThat(result).isFalse();
        
        // 验证Mock调用 - save方法不会被调用
        verify(budgetMainRepository, never()).save(any(BudgetMain.class));
    }

    @Test
    @DisplayName("扣减预算金额失败 - 预算不存在")
    void deductBudget_budgetNotFound() {
        // 设置Mock行为 - 返回空的Optional
        when(budgetMainRepository.findById(99L)).thenReturn(Optional.empty());

        // 执行测试
        boolean result = budgetService.deductBudget(99L, new BigDecimal("5000.00"));

        // 验证结果
        assertThat(result).isFalse();
        
        // 验证Mock调用
        verify(budgetMainRepository, never()).save(any(BudgetMain.class));
    }

    @Test
    @DisplayName("返还预算金额成功")
    void returnBudget_success() {
        // 设置已使用金额
        testBudgetMain.setUsedAmount(new BigDecimal("20000.00"));
        
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(testBudgetMain));
        when(budgetMainRepository.save(any(BudgetMain.class))).thenReturn(testBudgetMain);

        // 执行测试
        boolean result = budgetService.returnBudget(1L, new BigDecimal("5000.00"));

        // 验证结果
        assertThat(result).isTrue();
        
        // 验证Mock调用
        verify(budgetMainRepository, times(1)).save(any(BudgetMain.class));
    }

    @Test
    @DisplayName("乐观锁并发控制测试")
    void optimisticLocking_test() {
        // 设置初始版本的预算数据
        testBudgetMain.setVersion(1);
        
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(testBudgetMain));
        
        // 模拟并发更新时的版本冲突
        when(budgetMainRepository.save(any(BudgetMain.class)))
            .thenAnswer(invocation -> {
                BudgetMain budget = invocation.getArgument(0);
                if (budget.getVersion() != 1) {
                    throw new RuntimeException("Version conflict");
                }
                // 模拟版本号递增
                budget.setVersion(2);
                return budget;
            });

        // 执行测试 - 这里需要实际实现并发控制逻辑
        assertThat(budgetService.deductBudget(1L, new BigDecimal("1000.00"))).isTrue();
    }
}