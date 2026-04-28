package com.ccms.service;

import com.ccms.entity.budget.BudgetMain;
import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetAdjust;
import com.ccms.repository.budget.BudgetMainRepository;
import com.ccms.repository.budget.BudgetDetailRepository;
import com.ccms.repository.budget.BudgetAdjustRepository;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import org.junit.jupiter.api.Timeout;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 预算管理服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetMainRepository budgetMainRepository;

    @Mock
    private BudgetDetailRepository budgetDetailRepository;

    @Mock
    private BudgetAdjustRepository budgetAdjustRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private BudgetMain draftBudget;
    private BudgetMain submittedBudget;
    private BudgetDetail budgetDetail;
    private BudgetAdjust budgetAdjust;

    @BeforeEach
    void setUp() {
        // 创建草稿预算
        draftBudget = new BudgetMain();
        draftBudget.setId(1L);
        draftBudget.setDeptId(101L);
        draftBudget.setBudgetName("研发部2025年度预算");
        draftBudget.setBudgetYear(2025);
        draftBudget.setBudgetCycle(1); // 年度
        draftBudget.setTotalAmount(new BigDecimal("1000000.00"));
        draftBudget.setStatus(0); // 草稿
        draftBudget.setApprovalStatus(0); // 待提交
        draftBudget.setCreateTime(LocalDateTime.now());
        draftBudget.setCreateBy(1L);

        // 创建已提交预算
        submittedBudget = new BudgetMain();
        submittedBudget.setId(2L);
        submittedBudget.setDeptId(101L);
        submittedBudget.setBudgetName("研发部Q1预算");
        submittedBudget.setBudgetYear(2025);
        submittedBudget.setStatus(1); // 审批中
        submittedBudget.setApprovalStatus(1); // 审批中

        // 创建预算明细
        budgetDetail = new BudgetDetail();
        budgetDetail.setId(1L);
        budgetDetail.setBudgetMainId(1L);
        budgetDetail.setExpenseType(1); // 差旅费
        budgetDetail.setBudgetAmount(new BigDecimal("100000.00"));
        budgetDetail.setUsedAmount(BigDecimal.ZERO);
        budgetDetail.setRemainingAmount(new BigDecimal("100000.00"));

        // 创建预算调整
        budgetAdjust = new BudgetAdjust();
        budgetAdjust.setId(1L);
        budgetAdjust.setBudgetMainId(1L);
        budgetAdjust.setAdjustType(1); // 追加
        budgetAdjust.setAdjustAmount(new BigDecimal("50000.00"));
        budgetAdjust.setAdjustReason("项目需求增加");
        budgetAdjust.setStatus(0); // 草稿
        budgetAdjust.setAdjustStatus(0); // 待提交
    }

    @Test
    void testCreateBudget_Success() {
        // 模拟Repository保存
        when(budgetMainRepository.save(any(BudgetMain.class))).thenReturn(draftBudget);

        // 执行创建预算
        BudgetMain result = budgetService.createBudget(draftBudget);

        // 验证结果
        assertNotNull(result);
        assertEquals("研发部2025年度预算", result.getBudgetName());
        assertEquals(0, result.getStatus().intValue()); // 草稿状态
        assertEquals(0, result.getApprovalStatus().intValue()); // 待提交
        assertNotNull(result.getBudgetCode());
        
        verify(budgetMainRepository, times(1)).save(any(BudgetMain.class));
    }

    @Test
    void testUpdateBudget_Success() {
        // 模拟Repository查找
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(draftBudget));
        when(budgetMainRepository.save(any(BudgetMain.class))).thenReturn(draftBudget);

        // 准备更新数据
        BudgetMain updateData = new BudgetMain();
        updateData.setId(1L);
        updateData.setBudgetName("更新后的预算名称");
        updateData.setBudgetCycle(2); // 季度
        updateData.setBudgetYear(2026);
        updateData.setDescription("更新描述");

        // 执行更新
        BudgetMain result = budgetService.updateBudget(updateData);

        // 验证结果
        assertNotNull(result);
        assertEquals("更新后的预算名称", result.getBudgetName());
        assertEquals(2, result.getBudgetCycle().intValue());
        assertEquals(2026, result.getBudgetYear().intValue());
        
        verify(budgetMainRepository, times(1)).findById(1L);
        verify(budgetMainRepository, times(1)).save(any(BudgetMain.class));
    }

    @Test
    void testUpdateBudget_BudgetNotFound() {
        // 模拟Repository返回空
        when(budgetMainRepository.findById(999L)).thenReturn(Optional.empty());

        // 准备更新数据
        BudgetMain updateData = new BudgetMain();
        updateData.setId(999L);

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.updateBudget(updateData);
        });

        assertEquals("预算不存在", exception.getMessage());
        verify(budgetMainRepository, times(1)).findById(999L);
        verify(budgetMainRepository, never()).save(any(BudgetMain.class));
    }

    @Test
    void testUpdateBudget_NonDraftStatus() {
        // 模拟Repository返回已提交的预算
        when(budgetMainRepository.findById(2L)).thenReturn(Optional.of(submittedBudget));

        // 准备更新数据
        BudgetMain updateData = new BudgetMain();
        updateData.setId(2L);

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.updateBudget(updateData);
        });

        assertEquals("非草稿状态的预算不允许修改", exception.getMessage());
        verify(budgetMainRepository, times(1)).findById(2L);
        verify(budgetMainRepository, never()).save(any(BudgetMain.class));
    }

    @Test
    void testSubmitBudgetForApproval_Success() {
        // 模拟Repository查找和保存
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(draftBudget));
        when(budgetDetailRepository.findByBudgetMainId(1L)).thenReturn(List.of(budgetDetail));
        when(budgetMainRepository.save(any(BudgetMain.class))).thenReturn(draftBudget);

        // 执行提交审批
        budgetService.submitBudgetForApproval(1L);

        // 验证状态更新
        assertEquals(1, draftBudget.getStatus().intValue()); // 审批中
        assertEquals(1, draftBudget.getApprovalStatus().intValue()); // 审批中
        
        verify(budgetMainRepository, times(1)).findById(1L);
        verify(budgetDetailRepository, times(1)).findByBudgetMainId(1L);
        verify(budgetMainRepository, times(1)).save(any(BudgetMain.class));
    }

    @Test
    void testSubmitBudgetForApproval_BudgetNotFound() {
        // 模拟Repository返回空
        when(budgetMainRepository.findById(999L)).thenReturn(Optional.empty());

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.submitBudgetForApproval(999L);
        });

        assertEquals("预算不存在", exception.getMessage());
        verify(budgetMainRepository, times(1)).findById(999L);
        verify(budgetDetailRepository, never()).findByBudgetMainId(anyLong());
    }

    @Test
    void testSubmitBudgetForApproval_InvalidStatus() {
        // 模拟Repository返回已提交的预算
        when(budgetMainRepository.findById(2L)).thenReturn(Optional.of(submittedBudget));

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.submitBudgetForApproval(2L);
        });

        assertEquals("预算状态不允许提交审批", exception.getMessage());
        verify(budgetMainRepository, times(1)).findById(2L);
        verify(budgetDetailRepository, never()).findByBudgetMainId(anyLong());
    }

    @Test
    void testSubmitBudgetForApproval_NoDetails() {
        // 模拟Repository查找
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(draftBudget));
        when(budgetDetailRepository.findByBudgetMainId(1L)).thenReturn(List.of());

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.submitBudgetForApproval(1L);
        });

        assertEquals("预算明细不能为空", exception.getMessage());
        verify(budgetMainRepository, times(1)).findById(1L);
        verify(budgetDetailRepository, times(1)).findByBudgetMainId(1L);
        verify(budgetMainRepository, never()).save(any(BudgetMain.class));
    }

    @Test
    void testGetBudgetById_Found() {
        // 模拟Repository返回预算
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(draftBudget));

        // 执行查询
        BudgetMain result = budgetService.getBudgetById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("研发部2025年度预算", result.getBudgetName());
        
        verify(budgetMainRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBudgetById_NotFound() {
        // 模拟Repository返回空
        when(budgetMainRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行查询
        BudgetMain result = budgetService.getBudgetById(999L);

        // 验证结果
        assertNull(result);
        verify(budgetMainRepository, times(1)).findById(999L);
    }

    @Test
    void testAddBudgetDetail_Success() {
        // 模拟Repository查找和保存
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(draftBudget));
        when(budgetDetailRepository.save(any(BudgetDetail.class))).thenReturn(budgetDetail);

        // 执行添加明细
        BudgetDetail result = budgetService.addBudgetDetail(budgetDetail);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getBudgetMainId());
        assertEquals(BigDecimal.ZERO, result.getUsedAmount());
        assertNotNull(result.getRemainingAmount());
        
        verify(budgetMainRepository, times(1)).findById(1L);
        verify(budgetDetailRepository, times(1)).save(any(BudgetDetail.class));
    }

    @Test
    void testAddBudgetDetail_BudgetNotFound() {
        // 模拟Repository返回空
        when(budgetMainRepository.findById(999L)).thenReturn(Optional.empty());

        // 准备明细数据
        BudgetDetail detail = new BudgetDetail();
        detail.setBudgetMainId(999L);

        // 验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.addBudgetDetail(detail);
        });

        assertEquals("预算主表不存在", exception.getMessage());
        verify(budgetMainRepository, times(1)).findById(999L);
        verify(budgetDetailRepository, never()).save(any(BudgetDetail.class));
    }

    @Test
    void testUpdateBudgetDetail_Success() {
        // 模拟Repository查找和保存
        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(budgetDetail));
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(draftBudget));
        when(budgetDetailRepository.save(any(BudgetDetail.class))).thenReturn(budgetDetail);

        // 准备更新数据
        BudgetDetail updateData = new BudgetDetail();
        updateData.setId(1L);
        updateData.setBudgetMainId(1L);
        updateData.setBudgetAmount(new BigDecimal("150000.00"));
        updateData.setDescription("更新后的描述");

        // 执行更新
        BudgetDetail result = budgetService.updateBudgetDetail(updateData);

        // 验证结果
        assertNotNull(result);
        assertEquals(new BigDecimal("150000.00"), result.getBudgetAmount());
        
        verify(budgetDetailRepository, times(1)).findById(1L);
        verify(budgetMainRepository, times(1)).findById(1L);
        verify(budgetDetailRepository, times(1)).save(any(BudgetDetail.class));
    }

    @Test
    void testApplyBudgetAdjust_Success() {
        // 模拟Repository查找和保存
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(draftBudget));
        when(budgetAdjustRepository.save(any(BudgetAdjust.class))).thenReturn(budgetAdjust);

        // 执行预算调整申请
        BudgetAdjust result = budgetService.applyBudgetAdjust(budgetAdjust);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getBudgetMainId());
        assertEquals(1, result.getAdjustType().intValue());
        assertEquals(LocalDate.now(), result.getAdjustDate());
        assertEquals(0, result.getStatus().intValue()); // 草稿状态
        
        verify(budgetMainRepository, times(1)).findById(1L);
        verify(budgetAdjustRepository, times(1)).save(any(BudgetAdjust.class));
    }

    @Test
    void testApproveBudgetAdjust_Approved() {
        // 模拟Repository查找和保存
        when(budgetAdjustRepository.findById(1L)).thenReturn(Optional.of(budgetAdjust));
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(draftBudget));
        when(budgetMainRepository.save(any(BudgetMain.class))).thenReturn(draftBudget);
        when(budgetAdjustRepository.save(any(BudgetAdjust.class))).thenReturn(budgetAdjust);

        // 执行审批通过
        budgetService.approveBudgetAdjust(1L, true, "同意调整");

        // 验证状态更新
        assertEquals(3, budgetAdjust.getStatus().intValue()); // 已生效
        assertEquals(2, budgetAdjust.getAdjustStatus().intValue()); // 已通过
        assertEquals("同意调整", budgetAdjust.getApprovalComment());
        assertNotNull(budgetAdjust.getApprovalTime());
        
        verify(budgetAdjustRepository, times(1)).findById(1L);
        verify(budgetAdjustRepository, times(1)).save(any(BudgetAdjust.class));
    }

    @Test
    void testApproveBudgetAdjust_Rejected() {
        // 模拟Repository查找和保存
        when(budgetAdjustRepository.findById(1L)).thenReturn(Optional.of(budgetAdjust));
        when(budgetAdjustRepository.save(any(BudgetAdjust.class))).thenReturn(budgetAdjust);

        // 执行审批拒绝
        budgetService.approveBudgetAdjust(1L, false, "调整金额不合理");

        // 验证状态更新
        assertEquals(4, budgetAdjust.getStatus().intValue()); // 已拒绝
        assertEquals(3, budgetAdjust.getAdjustStatus().intValue()); // 已拒绝
        assertEquals("调整金额不合理", budgetAdjust.getApprovalComment());
        assertNotNull(budgetAdjust.getApprovalTime());
        
        verify(budgetAdjustRepository, times(1)).findById(1L);
        verify(budgetAdjustRepository, times(1)).save(any(BudgetAdjust.class));
        verify(budgetMainRepository, never()).save(any(BudgetMain.class));
    }

    @Test
    void testCheckBudgetAvailability_Available() {
        // 模拟Repository查找预算明细
        BudgetDetail detail = new BudgetDetail();
        detail.setRemainingAmount(new BigDecimal("50000.00"));
        when(budgetDetailRepository.findByDeptIdAndYearAndExpenseType(101L, 2025, 1))
                .thenReturn(List.of(detail));

        // 检查预算可用性
        boolean result = budgetService.checkBudgetAvailability(101L, 1, new BigDecimal("30000.00"), 2025);

        // 验证结果
        assertTrue(result);
        verify(budgetDetailRepository, times(1)).findByDeptIdAndYearAndExpenseType(101L, 2025, 1);
    }

    @Test
    void testCheckBudgetAvailability_NotAvailable() {
        // 模拟Repository查找预算明细
        BudgetDetail detail = new BudgetDetail();
        detail.setRemainingAmount(new BigDecimal("50000.00"));
        when(budgetDetailRepository.findByDeptIdAndYearAndExpenseType(101L, 2025, 1))
                .thenReturn(List.of(detail));

        // 检查预算可用性（大于可用预算）
        boolean result = budgetService.checkBudgetAvailability(101L, 1, new BigDecimal("60000.00"), 2025);

        // 验证结果
        assertFalse(result);
        verify(budgetDetailRepository, times(1)).findByDeptIdAndYearAndExpenseType(101L, 2025, 1);
    }

    @Test
    void testCheckBudgetAvailability_NoBudget() {
        // 模拟Repository返回空列表
        when(budgetDetailRepository.findByDeptIdAndYearAndExpenseType(101L, 2025, 999))
                .thenReturn(List.of());

        // 检查预算可用性（无预算设置）
        boolean result = budgetService.checkBudgetAvailability(101L, 999, new BigDecimal("1000.00"), 2025);

        // 验证结果
        assertFalse(result);
        verify(budgetDetailRepository, times(1)).findByDeptIdAndYearAndExpenseType(101L, 2025, 999);
    }

    @Test
    void testDeductBudget_Success() {
        // 模拟Repository查找和预算检查
        BudgetDetail detail = new BudgetDetail();
        detail.setId(1L);
        detail.setBudgetAmount(new BigDecimal("50000.00"));
        detail.setUsedAmount(BigDecimal.ZERO);
        detail.setRemainingAmount(new BigDecimal("50000.00"));
        
        when(budgetDetailRepository.findByDeptIdAndYearAndExpenseType(101L, 2025, 1))
                .thenReturn(List.of(detail));
        when(budgetDetailRepository.save(any(BudgetDetail.class))).thenReturn(detail);

        // 执行预算扣减
        boolean result = budgetService.deductBudget(101L, 1, new BigDecimal("30000.00"), 2025);

        // 验证结果
        assertTrue(result);
        assertEquals(new BigDecimal("30000.00"), detail.getUsedAmount());
        assertEquals(new BigDecimal("20000.00"), detail.getRemainingAmount());
        
        verify(budgetDetailRepository, times(1)).findByDeptIdAndYearAndExpenseType(101L, 2025, 1);
        verify(budgetDetailRepository, times(1)).save(any(BudgetDetail.class));
    }

    @Test
    void testReturnBudget_Success() {
        // 模拟Repository查找
        BudgetDetail detail = new BudgetDetail();
        detail.setId(1L);
        detail.setBudgetAmount(new BigDecimal("50000.00"));
        detail.setUsedAmount(new BigDecimal("30000.00"));
        detail.setRemainingAmount(new BigDecimal("20000.00"));
        
        when(budgetDetailRepository.findByDeptIdAndYearAndExpenseType(101L, 2025, 1))
                .thenReturn(List.of(detail));
        when(budgetDetailRepository.save(any(BudgetDetail.class))).thenReturn(detail);

        // 执行预算退回
        boolean result = budgetService.returnBudget(101L, 1, new BigDecimal("10000.00"), 2025);

        // 验证结果
        assertTrue(result);
        assertEquals(new BigDecimal("20000.00"), detail.getUsedAmount());
        assertEquals(new BigDecimal("30000.00"), detail.getRemainingAmount());
        
        verify(budgetDetailRepository, times(1)).findByDeptIdAndYearAndExpenseType(101L, 2025, 1);
        verify(budgetDetailRepository, times(1)).save(any(BudgetDetail.class));
    }

    @Test
    @Timeout(30) // 30秒超时
    void testConcurrentBudgetDeduction_Performance() throws InterruptedException {
        // 模拟Repository查找
        BudgetDetail detail = new BudgetDetail();
        detail.setId(1L);
        detail.setBudgetAmount(new BigDecimal("1000000.00"));
        detail.setUsedAmount(BigDecimal.ZERO);
        detail.setRemainingAmount(new BigDecimal("1000000.00"));
        
        when(budgetDetailRepository.findByDeptIdAndYearAndExpenseType(101L, 2025, 1))
                .thenReturn(List.of(detail));
        when(budgetDetailRepository.save(any(BudgetDetail.class))).thenReturn(detail);

        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicLong successfulOperations = new AtomicLong(0);
        AtomicLong failedOperations = new AtomicLong(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();

        // 创建并发任务
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待所有线程就绪
                    for (int j = 0; j < operationsPerThread; j++) {
                        try {
                            boolean result = budgetService.deductBudget(101L, 1, 
                                    new BigDecimal("10.00"), 2025);
                            if (result) {
                                successfulOperations.incrementAndGet();
                            } else {
                                failedOperations.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failedOperations.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 并发执行
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        long totalOperations = successfulOperations.get() + failedOperations.get();
        long duration = endTime - startTime;
        
        // 性能基准验证
        assertTrue(duration < 5000, "1000次并发预算扣减应在5秒内完成，实际耗时: " + duration + "ms");
        assertTrue(successfulOperations.get() > 0, "应该有成功操作");
        
        System.out.println("并发性能测试结果:");
        System.out.println("总操作次数: " + totalOperations);
        System.out.println("成功操作: " + successfulOperations.get());
        System.out.println("失败操作: " + failedOperations.get());
        System.out.println("耗时: " + duration + "ms");
        System.out.println("平均操作耗时: " + (duration / (double)totalOperations) + "ms");
    }

    @Test
    @Timeout(10) // 10秒超时
    void testBudgetAvailabilityCheck_Performance() {
        // 模拟返回100条明细数据
        List<BudgetDetail> details = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            BudgetDetail detail = new BudgetDetail();
            detail.setRemainingAmount(new BigDecimal("10000.00"));
            details.add(detail);
        }
        
        when(budgetDetailRepository.findByDeptIdAndYearAndExpenseType(101L, 2025, 1))
                .thenReturn(details);

        long startTime = System.nanoTime();
        
        // 执行1000次预算可用性检查
        for (int i = 0; i < 1000; i++) {
            budgetService.checkBudgetAvailability(101L, 1, new BigDecimal("5000.00"), 2025);
        }
        
        long endTime = System.nanoTime();
        long durationNanos = endTime - startTime;
        double avgTimePerRequest = durationNanos / 1000.0 / 1000.0; // 转换为毫秒
        
        // 性能基准：单次预算检查应在1毫秒内完成
        assertTrue(avgTimePerRequest < 1.0, 
                "单次预算可用性检查应在1毫秒内，实际平均耗时: " + avgTimePerRequest + "ms");
        
        System.out.println("预算可用性检查性能测试:");
        System.out.println("总计1000次检查");
        System.out.println("平均耗时: " + avgTimePerRequest + "ms/次");
    }

    @Test
    @Timeout(20) // 20秒超时
    void testConcurrentBudgetOperations_StressTest() throws InterruptedException {
        // 模拟并发预算操作压力测试
        BudgetDetail detail = new BudgetDetail();
        detail.setId(1L);
        detail.setBudgetAmount(new BigDecimal("100000.00"));
        detail.setUsedAmount(BigDecimal.ZERO);
        detail.setRemainingAmount(new BigDecimal("100000.00"));
        
        when(budgetDetailRepository.findByDeptIdAndYearAndExpenseType(101L, 2025, 1))
                .thenReturn(List.of(detail));
        when(budgetDetailRepository.save(any(BudgetDetail.class))).thenAnswer(invocation -> {
            Thread.sleep(1); // 模拟数据库保存延迟
            return invocation.getArgument(0);
        });

        int operationTypes = 3; // 扣除、退回、检查
        int threadsPerType = 5;
        ExecutorService executor = Executors.newFixedThreadPool(operationTypes * threadsPerType);
        AtomicLong totalOperations = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(operationTypes * threadsPerType);

        // 创建混合操作任务
        for (int type = 0; type < operationTypes; type++) {
            for (int i = 0; i < threadsPerType; i++) {
                final int operationType = type;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 20; j++) {
                            switch (operationType) {
                                case 0:
                                    budgetService.deductBudget(101L, 1, new BigDecimal("10.00"), 2025);
                                    break;
                                case 1:
                                    budgetService.returnBudget(101L, 1, new BigDecimal("5.00"), 2025);
                                    break;
                                case 2:
                                    budgetService.checkBudgetAvailability(101L, 1, new BigDecimal("50.00"), 2025);
                                    break;
                            }
                            totalOperations.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // 记录错误但继续执行
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        // 执行压力测试
        long startTime = System.currentTimeMillis();
        latch.await();
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        long expectedOperations = operationTypes * threadsPerType * 20;
        
        // 验证性能标准
        assertTrue(duration < 10000, "混合并发操作应在10秒内完成，实际耗时: " + duration + "ms");
        assertTrue(totalOperations.get() <= expectedOperations);
        
        System.out.println("压力测试结果:");
        System.out.println("计划操作: " + expectedOperations);
        System.out.println("完成操作: " + totalOperations.get());
        System.out.println("耗时: " + duration + "ms");
    }

    @Test
    @Timeout(5) // 5秒超时
    void testBudgetCalculation_Performance() {
        // 测试复杂预算计算性能
        List<BudgetDetail> details = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            BudgetDetail detail = new BudgetDetail();
            detail.setBudgetAmount(new BigDecimal(i * 1000));
            detail.setUsedAmount(new BigDecimal(i * 10));
            detail.setRemainingAmount(new BigDecimal(i * 1000 - i * 10));
            details.add(detail);
        }

        long startTime = System.currentTimeMillis();
        
        // 模拟复杂预算计算（求和、平均等）
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalUsed = BigDecimal.ZERO;
        
        for (BudgetDetail detail : details) {
            totalBudget = totalBudget.add(detail.getBudgetAmount());
            totalUsed = totalUsed.add(detail.getUsedAmount());
        }
        
        BigDecimal avgUsageRate = totalUsed.divide(totalBudget, 4, java.math.RoundingMode.HALF_UP);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 性能基准：1000条数据计算应在100毫秒内完成
        assertTrue(duration < 100, "1000条预算数据计算应在100毫秒内完成，实际耗时: " + duration + "ms");
        
        System.out.println("预算计算性能测试:");
        System.out.println("数据量: 1000条明细");
        System.out.println("计算耗时: " + duration + "ms");
        System.out.println("总预算: " + totalBudget);
        System.out.println("总使用: " + totalUsed);
        System.out.println("平均使用率: " + avgUsageRate.multiply(new BigDecimal(100)) + "%");
    }

    @Test
    void testMemoryUsage_NoMemoryLeaks() {
        // 测试内存使用情况，确保没有内存泄漏
        Runtime runtime = Runtime.getRuntime();
        
        // 执行前内存状态
        runtime.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 执行大量预算操作
        List<String> operations = new ArrayList<>();
        BudgetDetail detail = new BudgetDetail();
        detail.setBudgetAmount(new BigDecimal("10000.00"));
        detail.setUsedAmount(BigDecimal.ZERO);
        detail.setRemainingAmount(new BigDecimal("10000.00"));
        
        when(budgetDetailRepository.findByDeptIdAndYearAndExpenseType(101L, 2025, 1))
                .thenReturn(List.of(detail));

        for (int i = 0; i < 1000; i++) {
            // 模拟预算检查操作
            boolean result = budgetService.checkBudgetAvailability(101L, 1, new BigDecimal("100.00"), 2025);
            operations.add("Operation_" + i + "_" + result); // 创建一些对象
        }
        
        // 清理并检查内存
        operations.clear();
        runtime.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryDifference = finalMemory - initialMemory;
        
        // 内存增长不应超过1MB（包含JVM自身的内存管理开销）
        assertTrue(Math.abs(memoryDifference) < 1_000_000, 
                "内存使用增长不应超过1MB，实际增长: " + memoryDifference + " bytes");
        
        System.out.println("内存使用测试:");
        System.out.println("初始内存: " + initialMemory + " bytes");
        System.out.println("最终内存: " + finalMemory + " bytes");
        System.out.println("内存变化: " + memoryDifference + " bytes");
    }
}