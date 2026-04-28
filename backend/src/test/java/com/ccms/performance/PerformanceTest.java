package com.ccms.performance;

import com.ccms.entity.budget.Budget;
import com.ccms.entity.expense.ExpenseApply;
import com.ccms.entity.approval.Approval;
import com.ccms.entity.user.SysUser;
import com.ccms.repository.budget.BudgetRepository;
import com.ccms.repository.expense.ExpenseApplyRepository;
import com.ccms.repository.approval.ApprovalRepository;
import com.ccms.repository.user.SysUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 性能测试和压力测试
 * 包括负载测试、并发测试、压力测试和性能基准测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseApplyRepository expenseApplyRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testUserId;
    private Long testBudgetId;
    private final String validToken = "Bearer performance-test-token";
    private final int CONCURRENT_THREADS = 10;
    private final int BATCH_SIZE = 100;

    @BeforeAll
    void setUpTestData() {
        // 创建测试用户
        SysUser testUser = new SysUser();
        testUser.setUsername("perftest");
        testUser.setPassword("password");
        testUser.setRealName("性能测试用户");
        testUser.setEmail("perftest@example.com");
        testUser.setDeptId(1L);
        testUser.setUserStatus(1);
        testUser.setCreateBy(0L);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateBy(0L);
        testUser.setUpdateTime(LocalDateTime.now());
        sysUserRepository.save(testUser);
        testUserId = testUser.getId();

        // 创建测试预算
        Budget testBudget = new Budget();
        testBudget.setBudgetYear(2024);
        testBudget.setBudgetCode("PERF-TEST-BGT");
        testBudget.setBudgetName("性能测试预算");
        testBudget.setTotalAmount(new BigDecimal("500000.00"));
        testBudget.setDeptId(1L);
        testBudget.setBudgetStatus(1);
        testBudget.setCreateBy(testUserId);
        testBudget.setCreateTime(LocalDateTime.now());
        testBudget.setUpdateBy(testUserId);
        testBudget.setUpdateTime(LocalDateTime.now());
        budgetRepository.save(testBudget);
        testBudgetId = testBudget.getId();

        // 创建批量测试数据
        createBatchTestData();
    }

    private void createBatchTestData() {
        logger.info("开始创建批量性能测试数据...");
        List<ExpenseApply> batchApplies = new ArrayList<>();
        List<Approval> batchApprovals = new ArrayList<>();

        // 创建100个费用申请
        for (int i = 1; i <= BATCH_SIZE; i++) {
            ExpenseApply apply = new ExpenseApply();
            apply.setExpenseTitle("性能测试费用申请-" + i);
            apply.setExpenseType("TEST");
            apply.setApplyAmount(new BigDecimal(1000 + i * 10));
            apply.setBudgetId(testBudgetId);
            apply.setDeptId(1L);
            apply.setApplyUserId(testUserId);
            apply.setApplyStatus(0);
            apply.setDescription("批量性能测试数据");
            apply.setCreateBy(testUserId);
            apply.setCreateTime(LocalDateTime.now());
            apply.setUpdateBy(testUserId);
            apply.setUpdateTime(LocalDateTime.now());
            batchApplies.add(apply);
        }
        expenseApplyRepository.saveAll(batchApplies);

        // 创建关联的审批流程
        List<ExpenseApply> savedApplies = expenseApplyRepository.findAll();
        for (ExpenseApply apply : savedApplies) {
            Approval approval = new Approval();
            approval.setBusinessType("EXPENSE_APPLY");
            approval.setBusinessId(apply.getId());
            approval.setApplicantId(testUserId);
            approval.setCurrentApproverId(testUserId);
            approval.setApprovalStatus(0);
            approval.setCreateBy(testUserId);
            approval.setCreateTime(LocalDateTime.now());
            approval.setUpdateBy(testUserId);
            approval.setUpdateTime(LocalDateTime.now());
            batchApprovals.add(approval);
        }
        approvalRepository.saveAll(batchApprovals);

        logger.info("批量性能测试数据创建完成: {}个费用申请, {}个审批流程", 
                   BATCH_SIZE, BATCH_SIZE);
    }

    @Test
    void testBasicPerformanceBenchmark() throws Exception {
        // 基础性能基准测试
        String[] apiEndpoints = {
            "/api/budgets?page=0&size=10",
            "/api/expense-applies?page=0&size=10",
            "/api/approvals?page=0&size=10",
            "/api/users?page=0&size=5"
        };

        for (String endpoint : apiEndpoints) {
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < 10; i++) {
                mockMvc.perform(get(endpoint)
                                .header("Authorization", validToken))
                        .andExpect(status().isOk());
            }
            
            long endTime = System.currentTimeMillis();
            long avgResponseTime = (endTime - startTime) / 10;
            
            logger.info("API {} - 平均响应时间: {}ms", endpoint, avgResponseTime);
            assertTrue(avgResponseTime < 500, endpoint + " 响应时间过长: " + avgResponseTime + "ms");
        }
    }

    @Test
    void testConcurrentApiCalls() throws Exception {
        // 并发API调用测试
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS * 5);

        long startTime = System.currentTimeMillis();

        // 并发执行多个API调用
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            executorService.submit(() -> {
                try {
                    // 执行5种不同的API调用
                    for (int j = 0; j < 5; j++) {
                        mockMvc.perform(get("/api/budgets")
                                        .param("page", "0")
                                        .param("size", "10")
                                        .header("Authorization", validToken))
                                .andExpect(status().isOk());
                        successCount.incrementAndGet();
                        latch.countDown();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    latch.countDown();
                }
            });
        }

        // 等待所有任务完成
        assertTrue(latch.await(30, TimeUnit.SECONDS), "并发测试超时");
        executorService.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        logger.info("并发测试结果 - 成功: {}, 失败: {}, 总时间: {}ms", 
                   successCount.get(), errorCount.get(), totalTime);

        assertTrue(successCount.get() >= 40, "并发成功率过低: " + successCount.get() + "/50");
        assertTrue(totalTime < 10000, "并发响应时间过长: " + totalTime + "ms");
    }

    @Test
    void testLoadTestingWithBatchOperations() throws Exception {
        // 批量操作负载测试
        long startTime = System.currentTimeMillis();
        int batchSize = 20;

        // 批量创建操作
        CountDownLatch createLatch = new CountDownLatch(batchSize);
        ExecutorService createService = Executors.newFixedThreadPool(5);

        for (int i = 0; i < batchSize; i++) {
            final int index = i;
            createService.submit(() -> {
                try {
                    Map<String, Object> createRequest = Map.of(
                            "expenseTitle", "负载测试申请-" + index,
                            "expenseType", "TEST",
                            "applyAmount", 1000 + index * 50,
                            "budgetId", testBudgetId,
                            "deptId", 1L,
                            "applyUserId", testUserId,
                            "description", "负载测试创建"
                    );

                    mockMvc.perform(post("/api/expense-applies")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("Authorization", validToken)
                                    .content(objectMapper.writeValueAsString(createRequest)))
                            .andExpect(status().isOk());
                    
                    createLatch.countDown();
                } catch (Exception e) {
                    createLatch.countDown();
                }
            });
        }

        assertTrue(createLatch.await(60, TimeUnit.SECONDS), "批量创建操作超时");
        createService.shutdown();

        long endTime = System.currentTimeMillis();
        logger.info("批量创建操作完成 - 耗时: {}ms", endTime - startTime);

        // 批量查询操作测试
        startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/api/expense-applies")
                            .param("page", "0")
                            .param("size", "20")
                            .header("Authorization", validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        endTime = System.currentTimeMillis();
        logger.info("批量查询操作完成 - 耗时: {}ms", endTime - startTime);
        assertTrue((endTime - startTime) < 5000, "批量查询响应时间过长");
    }

    @Test
    void testDatabaseQueryPerformance() throws Exception {
        // 数据库查询性能测试
        String[] queryTypes = {
            "简单查询（不分页）",
            "分页查询（小数据量）",
            "分页查询（大数据量）",
            "条件查询",
            "统计查询"
        };

        long[] queryTimes = new long[queryTypes.length];

        // 简单查询
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/budgets/" + testBudgetId)
                        .header("Authorization", validToken))
                .andExpect(status().isOk());
        queryTimes[0] = System.currentTimeMillis() - startTime;

        // 小数据量分页查询
        startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/budgets")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", validToken))
                .andExpect(status().isOk());
        queryTimes[1] = System.currentTimeMillis() - startTime;

        // 大数据量分页查询
        startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/expense-applies")
                        .param("page", "0")
                        .param("size", "50")
                        .header("Authorization", validToken))
                .andExpect(status().isOk());
        queryTimes[2] = System.currentTimeMillis() - startTime;

        // 条件查询
        startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/expense-applies")
                        .param("page", "0")
                        .param("size", "10")
                        .param("applicantId", testUserId.toString())
                        .param("year", "2024")
                        .header("Authorization", validToken))
                .andExpect(status().isOk());
        queryTimes[3] = System.currentTimeMillis() - startTime;

        // 统计查询
        startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/budgets/statistics/annual")
                        .param("year", "2024")
                        .header("Authorization", validToken))
                .andExpect(status().isOk());
        queryTimes[4] = System.currentTimeMillis() - startTime;

        // 输出性能报告
        for (int i = 0; i < queryTypes.length; i++) {
            logger.info("{} - 响应时间: {}ms", queryTypes[i], queryTimes[i]);
            assertTrue(queryTimes[i] < 1000, queryTypes[i] + " 响应时间过长: " + queryTimes[i] + "ms");
        }
    }

    @Test
    void testMemoryAndResourceUsage() throws Exception {
        // 内存和资源使用测试
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // 执行一系列资源密集型操作
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < 20; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // 复杂查询操作
                    for (int j = 0; j < 5; j++) {
                        mockMvc.perform(get("/api/expense-applies")
                                        .param("page", "0")
                                        .param("size", "20")
                                        .param("year", "2024")
                                        .header("Authorization", validToken))
                                .andExpect(status().isOk());
                    }
                } catch (Exception e) {
                    // 忽略测试中的异常
                }
            });
            futures.add(future);
        }

        // 等待所有操作完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        logger.info("内存使用测试 - 初始: {}MB, 最终: {}MB, 增长: {}MB",
                   initialMemory / 1024 / 1024,
                   finalMemory / 1024 / 1024,
                   memoryIncrease / 1024 / 1024);

        // 验证内存增长在合理范围内（应小于50MB）
        assertTrue(memoryIncrease < 50 * 1024 * 1024, "内存泄漏检测: 增长 " + memoryIncrease / 1024 / 1024 + "MB");
        
        // 强制垃圾回收后检查内存
        System.gc();
        Thread.sleep(1000);
        
        long afterGcMemory = runtime.totalMemory() - runtime.freeMemory();
        logger.info("GC后内存使用: {}MB", afterGcMemory / 1024 / 1024);
    }

    @Test
    void testStressTesting() throws Exception {
        // 压力测试 - 极限条件下的性能表现
        int stressIterations = 100;
        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);

        // 创建线程池进行压力测试
        ExecutorService stressExecutor = Executors.newFixedThreadPool(20);
        CountDownLatch stressLatch = new CountDownLatch(stressIterations);

        for (int i = 0; i < stressIterations; i++) {
            final int iteration = i;
            stressExecutor.submit(() -> {
                try {
                    // 模拟混合操作
                    if (iteration % 3 == 0) {
                        // 查询操作
                        mockMvc.perform(get("/api/budgets")
                                        .param("page", "0")
                                        .param("size", "10")
                                        .header("Authorization", validToken))
                                .andExpect(status().isOk());
                    } else if (iteration % 3 == 1) {
                        // 复杂查询
                        mockMvc.perform(get("/api/expense-applies/statistics")
                                        .param("year", "2024")
                                        .param("deptId", "1")
                                        .header("Authorization", validToken))
                                .andExpect(status().isOk());
                    } else {
                        // 创建操作
                        Map<String, Object> request = Map.of(
                                "expenseTitle", "压力测试-" + iteration,
                                "expenseType", "TEST",
                                "applyAmount", 1000,
                                "budgetId", testBudgetId,
                                "deptId", 1L,
                                "applyUserId", testUserId
                        );
                        mockMvc.perform(post("/api/expense-applies")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .header("Authorization", validToken)
                                        .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 压力测试允许部分失败
                } finally {
                    stressLatch.countDown();
                }
            });
        }

        // 等待压力测试完成
        assertTrue(stressLatch.await(120, TimeUnit.SECONDS), "压力测试超时");
        stressExecutor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        logger.info("压力测试结果 - 成功: {}/{}, 总耗时: {}ms, 平均响应时间: {}ms",
                   successCount.get(), stressIterations, totalTime, 
                   totalTime / Math.max(successCount.get(), 1));

        // 成功率应大于80%
        double successRate = (double) successCount.get() / stressIterations * 100;
        assertTrue(successRate > 80.0, "压力测试成功率过低: " + successRate + "%");
        assertTrue(totalTime < 60000, "压力测试总时间过长: " + totalTime + "ms");
    }

    @Test
    void testPerformanceDegradationAnalysis() throws Exception {
        // 性能退化分析测试
        int[] loadLevels = {10, 50, 100, 200};
        long[] responseTimes = new long[loadLevels.length];

        for (int i = 0; i < loadLevels.length; i++) {
            long startTime = System.currentTimeMillis();
            
            // 模拟不同负载级别
            ExecutorService executor = Executors.newFixedThreadPool(loadLevels[i]);
            CountDownLatch latch = new CountDownLatch(loadLevels[i]);
            
            for (int j = 0; j < loadLevels[i]; j++) {
                executor.submit(() -> {
                    try {
                        mockMvc.perform(get("/api/budgets")
                                        .param("page", "0")
                                        .param("size", "5")
                                        .header("Authorization", validToken))
                                .andExpect(status().isOk());
                    } catch (Exception e) {
                        // 忽略测试异常
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();
            
            long endTime = System.currentTimeMillis();
            responseTimes[i] = endTime - startTime;
            
            logger.info("负载级别 {} - 响应时间: {}ms", loadLevels[i], responseTimes[i]);
        }

        // 分析性能退化趋势
        double degradationRate = (double) responseTimes[responseTimes.length - 1] / responseTimes[0];
        logger.info("性能退化率: {}x", degradationRate);
        
        // 验证性能退化在可接受范围内（不超过10倍）
        assertTrue(degradationRate < 10.0, "性能退化率过高: " + degradationRate + "x");
    }
}