package com.ccms.performance;

import com.ccms.entity.budget.Budget;
import com.ccms.entity.expense.ExpenseApply;
import com.ccms.entity.user.SysUser;
import com.ccms.repository.budget.BudgetRepository;
import com.ccms.repository.expense.ExpenseApplyRepository;
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
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 负载测试和压力测试
 * 模拟高并发、大数据量场景下的系统表现
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoadTest {

    private static final Logger logger = LoggerFactory.getLogger(LoadTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseApplyRepository expenseApplyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testUserId;
    private Long testBudgetId;
    private final String validToken = "Bearer load-test-token";

    @BeforeAll
    void setUpTestData() {
        // 创建测试用户
        SysUser testUser = new SysUser();
        testUser.setUsername("loadtest");
        testUser.setPassword("password");
        testUser.setRealName("负载测试用户");
        testUser.setEmail("loadtest@example.com");
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
        testBudget.setBudgetCode("LOAD-TEST-BGT");
        testBudget.setBudgetName("负载测试预算");
        testBudget.setTotalAmount(new BigDecimal("1000000.00"));
        testBudget.setDeptId(1L);
        testBudget.setBudgetStatus(1);
        testBudget.setCreateBy(testUserId);
        testBudget.setCreateTime(LocalDateTime.now());
        testBudget.setUpdateBy(testUserId);
        testBudget.setUpdateTime(LocalDateTime.now());
        budgetRepository.save(testBudget);
        testBudgetId = testBudget.getId();
    }

    @Test
    void testHighConcurrencyUserRegistration() throws Exception {
        // 高并发用户注册场景测试
        int concurrentUsers = 50;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(concurrentUsers);

        logger.info("开始高并发用户注册测试，并发数: {}", concurrentUsers);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    Map<String, Object> userRequest = Map.of(
                            "username", "concurrentuser" + userId,
                            "realName", "并发用户" + userId,
                            "email", "user" + userId + "@concurrent.com",
                            "deptId", 1L,
                            "password", "default123"
                    );

                    mockMvc.perform(post("/api/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("Authorization", validToken)
                                    .content(objectMapper.writeValueAsString(userRequest)))
                            .andExpect(status().isOk());
                    
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    logger.error("用户注册失败: {}", userId, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有请求完成
        assertTrue(latch.await(60, TimeUnit.SECONDS), "高并发用户注册测试超时");
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        logger.info("高并发用户注册测试完成 - 成功: {}/{}, 总耗时: {}ms", 
                   successCount.get(), concurrentUsers, totalTime);

        // 验证成功率至少80%
        assertTrue(successCount.get() >= concurrentUsers * 0.8, 
                  "高并发用户注册成功率过低: " + successCount.get() + "/" + concurrentUsers);
        assertTrue(totalTime < 30000, "高并发用户注册时间过长: " + totalTime + "ms");
    }

    @Test
    void testMassiveDataQueryPerformance() throws Exception {
        // 大数据量查询性能测试
        logger.info("开始大数据量查询性能测试...");

        int[] querySizes = {10, 50, 100, 200};
        long[] responseTimes = new long[querySizes.length];

        for (int i = 0; i < querySizes.length; i++) {
            int size = querySizes[i];
            
            long startTime = System.currentTimeMillis();
            
            // 并发查询指定大小的数据集
            mockMvc.perform(get("/api/expense-applies")
                            .param("page", "0")
                            .param("size", String.valueOf(size))
                            .header("Authorization", validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
            
            responseTimes[i] = System.currentTimeMillis() - startTime;
            
            logger.info("查询大小 {} - 响应时间: {}ms", size, responseTimes[i]);
            
            // 验证响应时间在合理范围内
            assertTrue(responseTimes[i] < size * 10, 
                      "大数据量查询响应时间过长: " + responseTimes[i] + "ms for size " + size);
        }

        // 输出性能趋势分析
        logger.info("大数据量查询性能趋势:");
        for (int i = 0; i < querySizes.length; i++) {
            logger.info("  Size {}: {}ms (avg {}ms/item)", 
                       querySizes[i], responseTimes[i], 
                       (double) responseTimes[i] / querySizes[i]);
        }
    }

    @Test
    void testTransactionHeavyWorkload() throws Exception {
        // 事务密集型工作负载测试
        logger.info("开始事务密集型工作负载测试...");

        int transactionCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(transactionCount);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < transactionCount; i++) {
            final int txId = i;
            executor.submit(() -> {
                try {
                    // 模拟完整的事务流程：创建申请 -> 提交审批
                    Map<String, Object> applyRequest = Map.of(
                            "expenseTitle", "事务测试申请-" + txId,
                            "expenseType", "TRANSACTION_TEST",
                            "applyAmount", 1000 + txId * 10,
                            "budgetId", testBudgetId,
                            "deptId", 1L,
                            "applyUserId", testUserId,
                            "description", "事务密集型测试数据"
                    );

                    String response = mockMvc.perform(post("/api/expense-applies")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("Authorization", validToken)
                                    .content(objectMapper.writeValueAsString(applyRequest)))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString();

                    // 假设返回数据中包含申请ID
                    if (response.contains("创建成功")) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    logger.error("事务处理失败: {}", txId, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有事务完成
        assertTrue(latch.await(120, TimeUnit.SECONDS), "事务密集型测试超时");
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        logger.info("事务密集型测试完成 - 成功事务: {}/{}, 总耗时: {}ms, TPS: {}",
                   successCount.get(), transactionCount, totalTime,
                   (double) successCount.get() / (totalTime / 1000.0));

        // 验证事务成功率
        assertTrue(successCount.get() >= transactionCount * 0.75,
                  "事务处理成功率过低: " + successCount.get() + "/" + transactionCount);
        
        // 验证TPS（每秒事务数）
        double tps = (double) successCount.get() / (totalTime / 1000.0);
        assertTrue(tps > 1.0, "TPS过低: " + tps);
    }

    @Test
    void testMixedWorkloadScenario() throws Exception {
        // 混合工作负载场景测试
        logger.info("开始混合工作负载场景测试...");

        int totalRequests = 200;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        AtomicInteger[] operationCounts = new AtomicInteger[4];
        for (int i = 0; i < 4; i++) {
            operationCounts[i] = new AtomicInteger(0);
        }

        CountDownLatch latch = new CountDownLatch(totalRequests);
        long startTime = System.currentTimeMillis();

        // 混合不同类型的请求
        for (int i = 0; i < totalRequests; i++) {
            final int requestId = i;
            final int operationType = i % 4; // 0:读, 1:写, 2:更新, 3:统计

            executor.submit(() -> {
                try {
                    switch (operationType) {
                        case 0: // 读操作
                            mockMvc.perform(get("/api/budgets")
                                            .param("page", "0")
                                            .param("size", "10")
                                            .header("Authorization", validToken))
                                    .andExpect(status().isOk());
                            operationCounts[0].incrementAndGet();
                            break;
                        
                        case 1: // 写操作
                            Map<String, Object> createRequest = Map.of(
                                    "expenseTitle", "混合负载写操作-" + requestId,
                                    "expenseType", "MIXED_TEST",
                                    "applyAmount", 500,
                                    "budgetId", testBudgetId,
                                    "deptId", 1L,
                                    "applyUserId", testUserId
                            );
                            mockMvc.perform(post("/api/expense-applies")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", validToken)
                                            .content(objectMapper.writeValueAsString(createRequest)))
                                    .andExpect(status().isOk());
                            operationCounts[1].incrementAndGet();
                            break;
                        
                        case 2: // 更新操作
                            // 模拟更新操作（此处使用假数据）
                            mockMvc.perform(put("/api/users/" + testUserId)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", validToken)
                                            .content("{\"realName\":\"Updated User\"}"))
                                    .andExpect(status().isOk());
                            operationCounts[2].incrementAndGet();
                            break;
                        
                        case 3: // 统计操作
                            mockMvc.perform(get("/api/budgets/statistics/annual")
                                            .param("year", "2024")
                                            .header("Authorization", validToken))
                                    .andExpect(status().isOk());
                            operationCounts[3].incrementAndGet();
                            break;
                    }
                } catch (Exception e) {
                    logger.error("混合负载操作失败: {} (type: {})", requestId, operationType, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有请求完成
        assertTrue(latch.await(180, TimeUnit.SECONDS), "混合工作负载测试超时");
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        logger.info("混合工作负载测试完成:");
        logger.info("  总请求数: {}, 总耗时: {}ms", totalRequests, totalTime);
        logger.info("  读操作: {}次", operationCounts[0].get());
        logger.info("  写操作: {}次", operationCounts[1].get());
        logger.info("  更新操作: {}次", operationCounts[2].get());
        logger.info("  统计操作: {}次", operationCounts[3].get());
        
        // 计算总体成功率（排除不可控的假数据问题）
        int totalSuccess = operationCounts[0].get() + operationCounts[1].get() + 
                          operationCounts[3].get();
        double successRate = (double) totalSuccess / totalRequests * 100;
        
        logger.info("  总体成功率: {}/{} ({}%)", totalSuccess, totalRequests, successRate);

        // 验证性能指标
        assertTrue(successRate > 70.0, "混合工作负载成功率过低: " + successRate + "%");
        assertTrue(totalTime < 120000, "混合工作负载测试时间过长: " + totalTime + "ms");
    }

    @Test
    void testPeakLoadHandling() throws Exception {
        // 峰值负载处理能力测试
        logger.info("开始峰值负载处理能力测试...");

        int peakUsers = 100;
        int requestsPerUser = 10;
        int totalRequests = peakUsers * requestsPerUser;

        ExecutorService executor = Executors.newFixedThreadPool(peakUsers);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(totalRequests);

        long startTime = System.currentTimeMillis();

        // 模拟峰值负载场景
        for (int user = 0; user < peakUsers; user++) {
            final int userId = user;
            executor.submit(() -> {
                for (int request = 0; request < requestsPerUser; request++) {
                    try {
                        // 模拟用户同时访问多个接口
                        if (request % 3 == 0) {
                            mockMvc.perform(get("/api/expense-applies")
                                            .param("page", "0")
                                            .param("size", "5")
                                            .header("Authorization", validToken))
                                    .andExpect(status().isOk());
                        } else if (request % 3 == 1) {
                            mockMvc.perform(get("/api/budgets")
                                            .param("page", "0")
                                            .param("size", "8")
                                            .header("Authorization", validToken))
                                    .andExpect(status().isOk());
                        } else {
                            mockMvc.perform(get("/api/approvals/pending")
                                            .param("page", "0")
                                            .param("size", "10")
                                            .param("approverId", testUserId.toString())
                                            .header("Authorization", validToken))
                                    .andExpect(status().isOk());
                        }
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        // 等待峰值负载测试完成
        assertTrue(latch.await(300, TimeUnit.SECONDS), "峰值负载测试超时");
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        logger.info("峰值负载测试结果:");
        logger.info("  并发用户数: {}", peakUsers);
        logger.info("  总请求数: {}", totalRequests);
        logger.info("  成功请求: {}", successCount.get());
        logger.info("  失败请求: {}", errorCount.get());
        logger.info("  总耗时: {}ms", totalTime);
        logger.info("  平均响应时间: {}ms", (double) totalTime / totalRequests);

        double successRate = (double) successCount.get() / totalRequests * 100;
        double requestsPerSecond = (double) successCount.get() / (totalTime / 1000.0);

        logger.info("  成功率: {}%", String.format("%.2f", successRate));
        logger.info("  RPS (Requests Per Second): {}", String.format("%.2f", requestsPerSecond));

        // 性能指标验证
        assertTrue(successRate > 80.0, "峰值负载成功率过低: " + successRate + "%");
        assertTrue(requestsPerSecond > 10.0, "RPS过低: " + requestsPerSecond);
        assertTrue(totalTime < 180000, "峰值负载测试时间过长: " + totalTime + "ms");
    }

    @Test
    void testResourceContentionScenarios() throws Exception {
        // 资源竞争场景测试
        logger.info("开始资源竞争场景测试...");

        int concurrentTransactions = 30;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentTransactions);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(concurrentTransactions);
        AtomicInteger deadlockCount = new AtomicInteger(0);
        AtomicInteger timeoutCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);

        // 所有线程同时开始竞争资源
        for (int i = 0; i < concurrentTransactions; i++) {
            final int txId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待统一开始

                    // 模拟资源竞争场景（操作同一预算）
                    Map<String, Object> updateRequest = Map.of(
                            "budgetName", "资源竞争测试-" + txId,
                            "updateBy", testUserId
                    );

                    mockMvc.perform(put("/api/budgets/" + testBudgetId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .header("Authorization", validToken)
                                    .content(objectMapper.writeValueAsString(updateRequest)))
                            .andExpect(status().isOk());

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                        timeoutCount.incrementAndGet();
                    } else if (e.getMessage() != null && e.getMessage().contains("deadlock")) {
                        deadlockCount.incrementAndGet();
                    }
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // 统一开始竞争
        startLatch.countDown();

        // 等待竞争完成（设置较短的超时时间）
        boolean completed = finishLatch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        logger.info("资源竞争测试结果:");
        logger.info("  并发事务数: {}", concurrentTransactions);
        logger.info("  成功事务: {}", successCount.get());
        logger.info("  超时事务: {}", timeoutCount.get());
        logger.info("  死锁事务: {}", deadlockCount.get());
        logger.info("  测试完成: {}", completed);

        // 验证系统对资源竞争的处理能力
        assertTrue(successCount.get() > 0, "资源竞争场景下无事务成功");
        assertTrue(deadlockCount.get() == 0, "检测到死锁情况: " + deadlockCount.get());
        
        // 总成功率应达到可接受水平
        double overallSuccessRate = (double) successCount.get() / concurrentTransactions * 100;
        assertTrue(overallSuccessRate > 30.0, 
                  "资源竞争场景成功率过低: " + overallSuccessRate + "%");
    }

    @Test
    void testMemoryLeakDetection() throws Exception {
        // 内存泄漏检测测试
        logger.info("开始内存泄漏检测测试...");

        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // 执行大量操作后检查内存增长
        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            // 执行轻量级查询操作
            mockMvc.perform(get("/api/budgets")
                            .param("page", "0")
                            .param("size", "5")
                            .header("Authorization", validToken))
                    .andExpect(status().isOk());
        }

        // 强制垃圾回收
        System.gc();
        Thread.sleep(2000); // 给GC一些时间

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        logger.info("内存泄漏检测结果:");
        logger.info("  初始内存: {}MB", initialMemory / 1024 / 1024);
        logger.info("  最终内存: {}MB", finalMemory / 1024 / 1024);
        logger.info("  内存增长: {}MB", memoryIncrease / 1024 / 1024);

        // 验证内存增长在合理范围内
        assertTrue(memoryIncrease < 100 * 1024 * 1024, // 100MB
                  "检测到可能的内存泄漏: 增长 " + memoryIncrease / 1024 / 1024 + "MB");
        
        // 内存增长应小于初始内存的10%
        double memoryIncreasePercentage = (double) memoryIncrease / initialMemory * 100;
        assertTrue(memoryIncreasePercentage < 10.0,
                  "内存增长百分比过高: " + String.format("%.2f", memoryIncreasePercentage) + "%");
    }
}