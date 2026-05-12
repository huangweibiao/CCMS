package com.ccms.integration;

import com.ccms.dto.approval.ExpenseApprovalDto;
import com.ccms.entity.approval.*;
import com.ccms.enums.approval.ApprovalStatus;
import com.ccms.repository.approval.*;
import com.ccms.service.ApprovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 审批流程性能测试
 * 测试系统在高并发和大数据量下的表现
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ApprovalPerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalPerformanceTest.class);

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private ApprovalFlowConfigRepository flowConfigRepository;

    @Autowired
    private ApprovalInstanceRepository instanceRepository;

    @Autowired
    private ApprovalRecordRepository recordRepository;

    @Autowired
    private ApprovalAuditLogRepository auditLogRepository;

    @Autowired
    private ApprovalNodeRepository nodeRepository;

    private Long testUser = 1001L;
    private Long testApprover = 1002L;
    private ApprovalFlowConfig testConfig;

    @BeforeEach
    void setUp() {
        // 创建测试配置
        testConfig = new ApprovalFlowConfig();
        testConfig.setFlowName("性能测试流程");
        testConfig.setBusinessType("PERFORMANCE_TEST");
        testConfig.setCategory("测试");
        testConfig.setMinAmount(BigDecimal.ZERO);
        testConfig.setMaxAmount(new BigDecimal("100000"));
        testConfig.setActive(true);
        flowConfigRepository.save(testConfig);
    }

    /**
     * 测试批量创建审批实例的性能
     */
    @Test
    void testBatchCreateInstancesPerformance() throws Exception {
        int batchSize = 1000;
        logger.info("开始测试批量创建 {} 个审批实例性能", batchSize);

        LocalDateTime startTime = LocalDateTime.now();

        // 单线程批量创建
        List<ApprovalInstance> instances = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            ApprovalInstance instance = new ApprovalInstance();
            instance.setBusinessType("PERFORMANCE_TEST");
            instance.setBusinessTitle("性能测试申请-" + i);
            instance.setApplicantId(testUser);
            instance.setDepartmentId(101L);
            instance.setStatus(ApprovalStatus.DRAFT);
            instance.setAmount(new BigDecimal("100.00"));
            instance.setDescription("批量性能测试");
            instance.setApplyTime(LocalDateTime.now());
            instance.setFlowConfigId(testConfig.getId());
            
            instances.add(instance);
        }

        List<ApprovalInstance> savedInstances = instanceRepository.saveAll(instances);
        LocalDateTime endTime = LocalDateTime.now();

        Duration duration = Duration.between(startTime, endTime);
        double opsPerSecond = (double) batchSize / duration.getSeconds();

        logger.info("批量创建 {} 个实例耗时: {} 秒", batchSize, duration.getSeconds());
        logger.info("平均操作速率: {:.2f} 实例/秒", opsPerSecond);

        assertThat(savedInstances).hasSize(batchSize);
        assertThat(duration.getSeconds()).isLessThan(30); // 期望30秒内完成
    }

    /**
     * 测试并发创建审批实例的性能
     */
    @Test
    void testConcurrentCreateInstancesPerformance() throws Exception {
        int threadCount = 10;
        int instancesPerThread = 100;
        int totalInstances = threadCount * instancesPerThread;

        logger.info("开始测试并发创建性能: {} 线程, 每线程 {} 实例, 总计 {} 实例", 
            threadCount, instancesPerThread, totalInstances);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        LocalDateTime startTime = LocalDateTime.now();

        for (int thread = 0; thread < threadCount; thread++) {
            final int threadId = thread;
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                int successfulCreates = 0;
                for (int i = 0; i < instancesPerThread; i++) {
                    try {
                        ApprovalInstance instance = new ApprovalInstance();
                        instance.setBusinessType("PERFORMANCE_TEST");
                        instance.setBusinessTitle("并发测试-" + threadId + "-" + i);
                        instance.setApplicantId(testUser + threadId);
                        instance.setDepartmentId(101L);
                        instance.setStatus(ApprovalStatus.DRAFT);
                        instance.setAmount(new BigDecimal("50.00"));
                        instance.setDescription("并发性能测试");
                        instance.setApplyTime(LocalDateTime.now());
                        instance.setFlowConfigId(testConfig.getId());

                        instanceRepository.save(instance);
                        successfulCreates++;
                    } catch (Exception e) {
                        logger.error("线程 {} 创建实例失败: {}", threadId, e.getMessage());
                    }
                }
                return successfulCreates;
            }, executor);
            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        allFutures.get(60, TimeUnit.SECONDS); // 设置超时时间

        int totalSuccessful = futures.stream()
            .map(CompletableFuture::join)
            .mapToInt(Integer::intValue)
            .sum();

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);

        logger.info("并发创建完成: 成功 {} / 总计 {} 实例", totalSuccessful, totalInstances);
        logger.info("并发创建耗时: {} 秒", duration.getSeconds());
        if (duration.getSeconds() > 0) {
            double opsPerSecond = (double) totalSuccessful / duration.getSeconds();
            logger.info("并发操作速率: {:.2f} 实例/秒", opsPerSecond);
        }

        executor.shutdown();

        assertThat(totalSuccessful).isGreaterThan(totalInstances * 0.95); // 成功率95%以上
        assertThat(duration.getSeconds()).isLessThan(45); // 期望45秒内完成
    }

    /**
     * 测试审批流程完整生命周期的性能
     */
    @Test
    void testCompleteApprovalFlowPerformance() {
        int testCaseCount = 100;
        logger.info("开始测试完整审批流程性能: {} 个测试用例", testCaseCount);

        LocalDateTime startTime = LocalDateTime.now();
        int successfulFlows = 0;

        for (int i = 0; i < testCaseCount; i++) {
            try {
                // 1. 创建审批实例
                ApprovalInstance instance = new ApprovalInstance();
                instance.setBusinessType("PERFORMANCE_TEST");
                instance.setBusinessTitle("完整流程测试-" + i);
                instance.setApplicantId(testUser);
                instance.setDepartmentId(101L);
                instance.setStatus(ApprovalStatus.DRAFT);
                instance.setAmount(new BigDecimal("200.00"));
                instance.setDescription("完整流程性能测试");
                instance.setApplyTime(LocalDateTime.now());
                instance.setFlowConfigId(testConfig.getId());
                instance = instanceRepository.save(instance);

                // 2. 模拟提交
                instance.setStatus(ApprovalStatus.APPROVING);
                instance.setSubmitTime(LocalDateTime.now());
                instance = instanceRepository.save(instance);

                // 3. 模拟审批
                ApprovalRecord record = new ApprovalRecord();
                record.setInstanceId(instance.getId());
                record.setApproverId(testApprover);
                record.setApprovalResult("APPROVED");
                record.setComments("性能测试审批");
                record.setApprovalTime(LocalDateTime.now());
                recordRepository.save(record);

                // 4. 标记完成
                instance.setStatus(ApprovalStatus.COMPLETED);
                instance.setCompleteTime(LocalDateTime.now());
                instanceRepository.save(instance);

                successfulFlows++;

            } catch (Exception e) {
                logger.error("测试用例 {} 执行失败: {}", i, e.getMessage());
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);

        logger.info("完整流程测试完成: 成功 {} / 总计 {} 个流程", successfulFlows, testCaseCount);
        logger.info("完整流程测试耗时: {} 秒", duration.getSeconds());
        if (successfulFlows > 0 && duration.getSeconds() > 0) {
            double opsPerSecond = (double) successfulFlows / duration.getSeconds();
            logger.info("完整流程操作速率: {:.2f} 流程/秒", opsPerSecond);
        }

        assertThat(successfulFlows).isEqualTo(testCaseCount); // 所有流程都应成功
        assertThat(duration.getSeconds()).isLessThan(60); // 期望60秒内完成
    }

    /**
     * 测试大数据量查询性能
     */
    @Test
    void testLargeDatasetQueryPerformance() {
        // 先创建大量测试数据
        int largeDatasetSize = 5000;
        logger.info("准备创建 {} 条测试数据用于查询性能测试", largeDatasetSize);

        List<ApprovalInstance> largeDataset = new ArrayList<>();
        for (int i = 0; i < largeDatasetSize; i++) {
            ApprovalInstance instance = new ApprovalInstance();
            instance.setBusinessType("PERFORMANCE_TEST");
            instance.setBusinessTitle("查询测试-" + i);
            instance.setApplicantId(testUser + (i % 100)); // 模拟多个申请人
            instance.setDepartmentId(101L + (i % 10)); // 模拟多个部门
            instance.setStatus(ApprovalStatus.values()[i % ApprovalStatus.values().length]);
            instance.setAmount(new BigDecimal(i % 1000 + 100));
            instance.setDescription("大数据量查询性能测试");
            instance.setApplyTime(LocalDateTime.now().minusDays(i % 30));
            instance.setFlowConfigId(testConfig.getId());
            
            largeDataset.add(instance);
        }

        instanceRepository.saveAll(largeDataset);
        logger.info("测试数据创建完成，开始查询性能测试");

        // 测试各种查询场景
        testQueryByStatus();
        testQueryByApplicant();
        testQueryByDateRange();
        testComplexQuery();
    }

    /**
     * 测试按状态查询性能
     */
    private void testQueryByStatus() {
        LocalDateTime startTime = LocalDateTime.now();
        
        for (ApprovalStatus status : ApprovalStatus.values()) {
            List<ApprovalInstance> results = instanceRepository.findByStatus(status);
            logger.debug("状态 {} 查询结果: {} 条", status, results.size());
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        
        logger.info("按状态分类查询总耗时: {} 毫秒", duration.toMillis());
        assertThat(duration.toMillis()).isLessThan(5000); // 期望5秒内完成所有状态查询
    }

    /**
     * 测试按申请人查询性能
     */
    private void testQueryByApplicant() {
        // 测试特定申请人的查询
        LocalDateTime startTime = LocalDateTime.now();
        
        for (int i = 0; i < 10; i++) {
            Long applicantId = testUser + i;
            List<ApprovalInstance> results = instanceRepository.findByApplicantId(applicantId);
            logger.debug("申请人 {} 查询结果: {} 条", applicantId, results.size());
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        
        logger.info("按申请人查询总耗时: {} 毫秒", duration.toMillis());
        assertThat(duration.toMillis()).isLessThan(3000); // 期望3秒内完成
    }

    /**
     * 测试按时间范围查询性能
     */
    private void testQueryByDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(15);
        LocalDateTime endDate = LocalDateTime.now();
        
        LocalDateTime startTime = LocalDateTime.now();
        
        // 模拟按时间范围查询
        List<ApprovalInstance> recentInstances = instanceRepository
            .findByApplyTimeBetween(startDate, endDate);
        
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        
        logger.info("按时间范围查询结果: {} 条，耗时: {} 毫秒", 
            recentInstances.size(), duration.toMillis());
        
        assertThat(duration.toMillis()).isLessThan(2000); // 期望2秒内完成
    }

    /**
     * 测试复杂组合查询性能
     */
    private void testComplexQuery() {
        LocalDateTime startTime = LocalDateTime.now();
        
        // 模拟复杂查询：特定状态 + 时间范围 + 金额范围
        List<ApprovalInstance> complexResults = instanceRepository
            .findComplexQuery(ApprovalStatus.APPROVING, 
                            LocalDateTime.now().minusDays(7), 
                            LocalDateTime.now(),
                            new BigDecimal("100"), 
                            new BigDecimal("1000"));
        
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        
        logger.info("复杂组合查询结果: {} 条，耗时: {} 毫秒", 
            complexResults.size(), duration.toMillis());
        
        assertThat(duration.toMillis()).isLessThan(3000); // 期望3秒内完成
    }

    /**
     * 测试内存使用和垃圾回收表现
     */
    @Test
    void testMemoryAndGCPerformance() {
        logger.info("开始内存使用和GC性能测试");
        
        // 获取初始内存状态
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 创建大量对象测试内存管理
        int objectCount = 10000;
        List<ApprovalInstance> tempObjects = new ArrayList<>();
        
        for (int i = 0; i < objectCount; i++) {
            ApprovalInstance instance = new ApprovalInstance();
            instance.setBusinessType("MEMORY_TEST");
            instance.setBusinessTitle("内存测试-" + i);
            // 设置其他属性...
            tempObjects.add(instance);
        }
        
        // 强制GC并检查内存
        System.gc();
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        logger.info("内存测试 - 初始: {} MB, 结束: {} MB, 增加: {} MB", 
            initialMemory / (1024 * 1024), 
            finalMemory / (1024 * 1024), 
            memoryIncrease / (1024 * 1024));
        
        // 合理的对象在GC后应该有显著的内存回收
        assertThat(memoryIncrease).isLessThan(50 * 1024 * 1024); // 期望增加不超过50MB
    }

    /**
     * 测试数据库连接池性能
     */
    @Test
    void testConnectionPoolPerformance() throws Exception {
        int concurrentConnections = 20;
        int operationsPerConnection = 50;
        
        logger.info("测试数据库连接池性能: {} 并发连接, 每连接 {} 操作", 
            concurrentConnections, operationsPerConnection);
        
        ExecutorService executor = Executors.newFixedThreadPool(concurrentConnections);
        List<CompletableFuture<Integer>> futures = new ArrayList<>();
        
        LocalDateTime startTime = LocalDateTime.now();
        
        for (int i = 0; i < concurrentConnections; i++) {
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                int successfulOps = 0;
                for (int j = 0; j < operationsPerConnection; j++) {
                    try {
                        // 执行简单的数据库操作
                        instanceRepository.count();
                        successfulOps++;
                        Thread.sleep(10); // 模拟操作间隔
                    } catch (Exception e) {
                        logger.error("数据库操作失败: {}", e.getMessage());
                    }
                }
                return successfulOps;
            }, executor);
            futures.add(future);
        }
        
        // 等待所有操作完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        allFutures.get(120, TimeUnit.SECONDS); // 给予充分的超时时间
        
        int totalSuccessful = futures.stream()
            .map(CompletableFuture::join)
            .mapToInt(Integer::intValue)
            .sum();
        
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        
        logger.info("连接池测试完成: 成功 {} / 总计 {} 操作", 
            totalSuccessful, concurrentConnections * operationsPerConnection);
        logger.info("连接池测试耗时: {} 秒", duration.getSeconds());
        
        executor.shutdown();
        
        assertThat(totalSuccessful).isEqualTo(concurrentConnections * operationsPerConnection);
        assertThat(duration.getSeconds()).isLessThan(90); // 期望90秒内完成
    }

    /**
     * 性能测试结果报告
     */
    public static class PerformanceReport {
        private final String testName;
        private final int totalOperations;
        private final int successfulOperations;
        private final Duration duration;
        private final double operationsPerSecond;
        
        public PerformanceReport(String testName, int totalOperations, int successfulOperations, Duration duration) {
            this.testName = testName;
            this.totalOperations = totalOperations;
            this.successfulOperations = successfulOperations;
            this.duration = duration;
            this.operationsPerSecond = duration.getSeconds() > 0 ? 
                (double) successfulOperations / duration.getSeconds() : 0;
        }
        
        public void logReport() {
            logger.info("=== 性能测试报告: {} ===", testName);
            logger.info("总操作数: {}", totalOperations);
            logger.info("成功操作数: {}", successfulOperations);
            logger.info("成功率: {:.2f}%", (successfulOperations * 100.0) / totalOperations);
            logger.info("总耗时: {} 秒", duration.getSeconds());
            logger.info("操作速率: {:.2f} 操作/秒", operationsPerSecond);
            logger.info("================================");
        }
    }
}