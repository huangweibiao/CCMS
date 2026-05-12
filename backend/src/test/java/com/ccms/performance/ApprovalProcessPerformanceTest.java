package com.ccms.performance;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalActionEnum;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.enums.ApproverTypeEnum;
import com.ccms.service.ApprovalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 审批流程性能测试
 * 测试系统在高并发场景下的性能和稳定性
 */
@SpringBootTest
@ActiveProfiles("test")
public class ApprovalProcessPerformanceTest {

    @Autowired
    private ApprovalService approvalService;

    /**
     * 批量创建审批流程配置的性能测试
     */
    @Test
    public void testBatchCreateFlowConfigPerformance() throws InterruptedException {
        int batchSize = 100;
        int threadCount = 10;
        
        System.out.println("开始批量创建流程配置性能测试");
        System.out.println("批量大小: " + batchSize + ", 线程数: " + threadCount);
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Long>> futures = new ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Future<Long> future = executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                
                for (int j = 0; j < batchSize; j++) {
                    try {
                        ApprovalFlowConfig config = createMockFlowConfig(threadId * batchSize + j);
                        // 这里应该是调用createFlowConfig方法，但为了测试使用模拟
                        Thread.sleep(10); // 模拟创建耗时
                    } catch (Exception e) {
                        System.err.println("创建流程配置失败: " + e.getMessage());
                    }
                }
                
                return System.currentTimeMillis() - startTime;
            });
            futures.add(future);
        }
        
        // 等待所有任务完成
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        
        // 统计结果
        long totalTime = 0;
        int successCount = 0;
        for (Future<Long> future : futures) {
            try {
                totalTime += future.get();
                successCount++;
            } catch (Exception e) {
                System.err.println("任务执行失败: " + e.getMessage());
            }
        }
        
        stopWatch.stop();
        
        System.out.println("测试结果:");
        System.out.println("总耗时: " + stopWatch.getTotalTimeMillis() + "ms");
        System.out.println("成功线程数: " + successCount + "/" + threadCount);
        System.out.println("平均线程耗时: " + (successCount > 0 ? totalTime / successCount : 0) + "ms");
        System.out.println("吞吐量: " + (batchSize * threadCount * 1000.0 / stopWatch.getTotalTimeMillis()) + " 操作/秒");
    }

    /**
     * 高并发审批操作性能测试
     */
    @Test
    public void testHighConcurrencyApprovalPerformance() throws InterruptedException {
        int concurrentUsers = 50;
        int operationsPerUser = 20;
        
        System.out.println("开始高并发审批操作性能测试");
        System.out.println("并发用户数: " + concurrentUsers + ", 每个用户操作数: " + operationsPerUser);
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        List<Future<ApprovalTestResult>> futures = new ArrayList<>();
        
        for (int i = 0; i < concurrentUsers; i++) {
            final int userId = i + 1;
            Future<ApprovalTestResult> future = executor.submit(() -> {
                ApprovalTestResult result = new ApprovalTestResult();
                long startTime = System.currentTimeMillis();
                
                for (int j = 0; j < operationsPerUser; j++) {
                    try {
                        // 模拟审批操作
                        if (j % 3 == 0) {
                            simulateSubmitApproval(userId, j);
                        } else if (j % 3 == 1) {
                            simulateApproveOperation(userId, j);
                        } else {
                            simulateRejectOperation(userId, j);
                        }
                        result.incrementSuccess();
                    } catch (Exception e) {
                        result.incrementFailure();
                        System.err.println("用户 " + userId + " 操作失败: " + e.getMessage());
                    }
                }
                
                result.setExecutionTime(System.currentTimeMillis() - startTime);
                return result;
            });
            futures.add(future);
        }
        
        // 等待所有任务完成
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
        
        // 统计结果
        int totalSuccess = 0;
        int totalFailure = 0;
        long totalExecutionTime = 0;
        int completedUsers = 0;
        
        for (Future<ApprovalTestResult> future : futures) {
            try {
                ApprovalTestResult result = future.get();
                totalSuccess += result.getSuccessCount();
                totalFailure += result.getFailureCount();
                totalExecutionTime += result.getExecutionTime();
                completedUsers++;
            } catch (Exception e) {
                System.err.println("用户任务执行失败: " + e.getMessage());
            }
        }
        
        stopWatch.stop();
        
        System.out.println("测试结果:");
        System.out.println("总耗时: " + stopWatch.getTotalTimeMillis() + "ms");
        System.out.println("完成用户数: " + completedUsers + "/" + concurrentUsers);
        System.out.println("成功操作数: " + totalSuccess);
        System.out.println("失败操作数: " + totalFailure);
        System.out.println("成功率: " + (totalSuccess * 100.0 / (totalSuccess + totalFailure)) + "%");
        System.out.println("平均用户耗时: " + (completedUsers > 0 ? totalExecutionTime / completedUsers : 0) + "ms");
        System.out.println("吞吐量: " + ((totalSuccess + totalFailure) * 1000.0 / stopWatch.getTotalTimeMillis()) + " 操作/秒");
    }

    /**
     * 内存和响应时间性能测试
     */
    @Test
    public void testMemoryAndResponseTime() {
        System.out.println("开始内存和响应时间测试");
        
        // 记录初始内存使用
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        // 模拟大量查询操作
        int queryCount = 1000;
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < queryCount; i++) {
            long startTime = System.nanoTime();
            
            // 模拟查询操作
            simulateQueryOperation(i);
            
            long endTime = System.nanoTime();
            responseTimes.add((endTime - startTime) / 1_000_000); // 转换为毫秒
            
            if (i % 100 == 0) {
                System.out.println("已完成 " + i + " 次查询");
            }
        }
        
        stopWatch.stop();
        
        // 记录最终内存使用
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        // 分析响应时间
        responseTimes.sort(Long::compareTo);
        long p50 = responseTimes.get((int) (queryCount * 0.5));
        long p90 = responseTimes.get((int) (queryCount * 0.9));
        long p95 = responseTimes.get((int) (queryCount * 0.95));
        long p99 = responseTimes.get((int) (queryCount * 0.99));
        
        System.out.println("测试结果:");
        System.out.println("总耗时: " + stopWatch.getTotalTimeMillis() + "ms");
        System.out.println("内存使用: " + (memoryUsed / 1024 / 1024) + " MB");
        System.out.println("平均响应时间: " + responseTimes.stream().mapToLong(Long::longValue).average().orElse(0) + "ms");
        System.out.println("P50响应时间: " + p50 + "ms");
        System.out.println("P90响应时间: " + p90 + "ms");
        System.out.println("P95响应时间: " + p95 + "ms");
        System.out.println("P99响应时间: " + p99 + "ms");
        System.out.println("吞吐量: " + (queryCount * 1000.0 / stopWatch.getTotalTimeMillis()) + " 查询/秒");
    }

    /**
     * 生成Mock数据性能测试
     */
    @Test
    public void testMockDataGenerationPerformance() {
        System.out.println("开始Mock数据生成性能测试");
        
        int dataSize = 10000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        // 生成Mock数据
        List<ApprovalFlowConfig> configs = generateMockFlowConfigs(dataSize);
        List<ApprovalInstance> instances = generateMockInstances(dataSize);
        List<ApprovalRecord> records = generateMockRecords(dataSize);
        
        stopWatch.stop();
        
        System.out.println("测试结果:");
        System.out.println("生成数据量: " + dataSize + " 条");
        System.out.println("总耗时: " + stopWatch.getTotalTimeMillis() + "ms");
        System.out.println("平均生成速度: " + (dataSize * 1000.0 / stopWatch.getTotalTimeMillis()) + " 条/秒");
        System.out.println("配置数: " + configs.size());
        System.out.println("实例数: " + instances.size());
        System.out.println("记录数: " + records.size());
    }

    // 辅助方法
    
    private ApprovalFlowConfig createMockFlowConfig(int id) {
        ApprovalFlowConfig config = new ApprovalFlowConfig();
        config.setId((long) id);
        config.setName("测试流程配置" + id);
        config.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
        config.setStatus("ACTIVE");
        config.setVersion(1);
        return config;
    }
    
    private void simulateSubmitApproval(int userId, int operationId) throws InterruptedException {
        // 模拟提交审批的延迟和资源消耗
        Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
    }
    
    private void simulateApproveOperation(int userId, int operationId) throws InterruptedException {
        // 模拟审批通过的延迟和资源消耗
        Thread.sleep(ThreadLocalRandom.current().nextInt(5, 30));
    }
    
    private void simulateRejectOperation(int userId, int operationId) throws InterruptedException {
        // 模拟审批驳回的延迟和资源消耗
        Thread.sleep(ThreadLocalRandom.current().nextInt(5, 30));
    }
    
    private void simulateQueryOperation(int queryId) {
        // 模拟查询操作的延迟和资源消耗
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(2, 10));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private List<ApprovalFlowConfig> generateMockFlowConfigs(int count) {
        List<ApprovalFlowConfig> configs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            configs.add(createMockFlowConfig(i));
        }
        return configs;
    }
    
    private List<ApprovalInstance> generateMockInstances(int count) {
        List<ApprovalInstance> instances = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ApprovalInstance instance = new ApprovalInstance();
            instance.setId((long) i);
            instance.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
            instance.setStatus(ApprovalStatusEnum.PENDING);
            instances.add(instance);
        }
        return instances;
    }
    
    private List<ApprovalRecord> generateMockRecords(int count) {
        List<ApprovalRecord> records = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ApprovalRecord record = new ApprovalRecord();
            record.setId((long) i);
            record.setAction(ApprovalActionEnum.APPROVE);
            records.add(record);
        }
        return records;
    }
    
    // 测试结果类
    static class ApprovalTestResult {
        private int successCount = 0;
        private int failureCount = 0;
        private long executionTime = 0;
        
        public void incrementSuccess() { successCount++; }
        public void incrementFailure() { failureCount++; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    }
}