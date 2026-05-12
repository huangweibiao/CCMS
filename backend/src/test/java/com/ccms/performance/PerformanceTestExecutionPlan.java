package com.ccms.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 性能测试执行计划
 * 提供自动化的性能测试执行和报告生成
 */
@Component
public class PerformanceTestExecutionPlan implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTestExecutionPlan.class);
    
    private final ApprovalProcessPerformanceTest approvalTest;
    
    public PerformanceTestExecutionPlan(ApprovalProcessPerformanceTest approvalTest) {
        this.approvalTest = approvalTest;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "performance-test".equals(args[0])) {
            logger.info("开始执行性能测试计划");
            executePerformanceTestSuite();
        }
    }

    /**
     * 执行完整的性能测试套件
     */
    public void executePerformanceTestSuite() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<TestResult>> futures = new ArrayList<>();
        
        try {
            // 并发执行多个性能测试
            futures.add(executor.submit(this::testBatchPerformance));
            futures.add(executor.submit(this::testConcurrencyPerformance));
            futures.add(executor.submit(this::testMemoryPerformance));
            futures.add(executor.submit(this::testQueryPerformance));
            
            // 等待所有测试完成
            List<TestResult> results = new ArrayList<>();
            for (Future<TestResult> future : futures) {
                try {
                    results.add(future.get(10, TimeUnit.MINUTES));
                } catch (Exception e) {
                    logger.error("性能测试执行失败", e);
                    results.add(new TestResult("ERROR", e.getMessage(), 0, 0));
                }
            }
            
            // 生成综合报告
            generateComprehensiveReport(results);
            
        } finally {
            executor.shutdown();
        }
    }

    private TestResult testBatchPerformance() {
        logger.info("执行批量处理性能测试");
        long startTime = System.currentTimeMillis();
        
        try {
            approvalTest.testBatchCreateFlowConfigPerformance();
            long duration = System.currentTimeMillis() - startTime;
            return new TestResult("BATCH_PERFORMANCE", "批量处理测试", duration, 1);
        } catch (Exception e) {
            return new TestResult("BATCH_PERFORMANCE", "失败: " + e.getMessage(), 0, 0);
        }
    }

    private TestResult testConcurrencyPerformance() {
        logger.info("执行高并发性能测试");
        long startTime = System.currentTimeMillis();
        
        try {
            approvalTest.testHighConcurrencyApprovalPerformance();
            long duration = System.currentTimeMillis() - startTime;
            return new TestResult("CONCURRENCY_PERFORMANCE", "高并发测试", duration, 1);
        } catch (Exception e) {
            return new TestResult("CONCURRENCY_PERFORMANCE", "失败: " + e.getMessage(), 0, 0);
        }
    }

    private TestResult testMemoryPerformance() {
        logger.info("执行内存性能测试");
        long startTime = System.currentTimeMillis();
        
        try {
            approvalTest.testMemoryAndResponseTime();
            long duration = System.currentTimeMillis() - startTime;
            return new TestResult("MEMORY_PERFORMANCE", "内存性能测试", duration, 1);
        } catch (Exception e) {
            return new TestResult("MEMORY_PERFORMANCE", "失败: " + e.getMessage(), 0, 0);
        }
    }

    private TestResult testQueryPerformance() {
        logger.info("执行查询性能测试");
        long startTime = System.currentTimeMillis();
        
        try {
            approvalTest.testMockDataGenerationPerformance();
            long duration = System.currentTimeMillis() - startTime;
            return new TestResult("QUERY_PERFORMANCE", "查询性能测试", duration, 1);
        } catch (Exception e) {
            return new TestResult("QUERY_PERFORMANCE", "失败: " + e.getMessage(), 0, 0);
        }
    }

    private void generateComprehensiveReport(List<TestResult> results) {
        logger.info("\n" + "=".repeat(80));
        logger.info("性能测试综合报告");
        logger.info("=".repeat(80));
        
        int totalTests = results.size();
        int passedTests = 0;
        long totalDuration = 0;
        
        for (TestResult result : results) {
            logger.info("测试类型: {}", result.getTestType());
            logger.info("测试描述: {}", result.getDescription());
            logger.info("执行时间: {}ms", result.getDuration());
            logger.info("测试结果: {}", result.getSuccessCount() > 0 ? "PASSED" : "FAILED");
            logger.info("-".repeat(40));
            
            if (result.getSuccessCount() > 0) {
                passedTests++;
            }
            totalDuration += result.getDuration();
        }
        
        logger.info("测试总结:");
        logger.info("总体测试数: {}", totalTests);
        logger.info("通过测试数: {}", passedTests);
        logger.info("测试成功率: {:.2f}%", (passedTests * 100.0 / totalTests));
        logger.info("总执行时间: {}ms", totalDuration);
        logger.info("=".repeat(80) + "\n");
        
        // 生成性能建议
        generatePerformanceRecommendations(results);
    }

    private void generatePerformanceRecommendations(List<TestResult> results) {
        logger.info("性能优化建议:");
        
        // 分析测试结果并生成建议
        for (TestResult result : results) {
            switch (result.getTestType()) {
                case "BATCH_PERFORMANCE":
                    if (result.getDuration() > 10000) {
                        logger.info("批量处理性能建议: 考虑优化数据库批处理配置，增加连接池大小");
                    }
                    break;
                case "CONCURRENCY_PERFORMANCE":
                    if (result.getDuration() > 15000) {
                        logger.info("高并发性能建议: 增加异步处理能力，优化锁策略");
                    }
                    break;
                case "MEMORY_PERFORMANCE":
                    if (result.getDuration() > 5000) {
                        logger.info("内存性能建议: 检查内存泄漏，优化对象创建策略");
                    }
                    break;
                case "QUERY_PERFORMANCE":
                    if (result.getDuration() > 3000) {
                        logger.info("查询性能建议: 添加适当的数据库索引，优化查询语句");
                    }
                    break;
            }
        }
        
        // 通用建议
        logger.info("通用优化建议：");
        logger.info("- 定期监控缓存命中率，调整缓存策略");
        logger.info("- 使用连接池监控，优化数据库连接使用");
        logger.info("- 启用慢查询日志，识别性能瓶颈");
        logger.info("- 考虑使用消息队列解耦耗时操作");
    }

    /**
     * 测试结果类
     */
    static class TestResult {
        private final String testType;
        private final String description;
        private final long duration;
        private final int successCount;

        public TestResult(String testType, String description, long duration, int successCount) {
            this.testType = testType;
            this.description = description;
            this.duration = duration;
            this.successCount = successCount;
        }

        public String getTestType() { return testType; }
        public String getDescription() { return description; }
        public long getDuration() { return duration; }
        public int getSuccessCount() { return successCount; }
    }
}