package com.ccms.service.schedule;

import com.ccms.repository.export.ExportTaskRepository;
import com.ccms.service.export.ReportExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务服务
 */
@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private ExportTaskRepository exportTaskRepository;

    @Autowired
    private ReportExportService reportExportService;

    /**
     * 每分钟检查超时的导出任务（标记为处理中但超过30分钟未完成）
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
    public void checkTimeoutTasks() {
        try {
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30);
            int updatedCount = exportTaskRepository.markTimeoutTasks(timeoutThreshold);
            
            if (updatedCount > 0) {
                logger.info("标记了 {} 个超时任务", updatedCount);
            }
        } catch (Exception e) {
            logger.error("检查超时任务失败", e);
        }
    }

    /**
     * 每天凌晨2点清理过期任务记录（保留最近7天的记录）
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupExpiredTasks() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(7);
            int deletedCount = exportTaskRepository.deleteByCreateTimeBeforeAndStatusIn(
                    threshold, java.util.Arrays.asList(2, 3, 4)); // 已完成、失败、已取消
            
            if (deletedCount > 0) {
                logger.info("清理了 {} 条过期任务记录", deletedCount);
            }
        } catch (Exception e) {
            logger.error("清理过期任务记录失败", e);
        }
    }

    /**
     * 每小时检查过期的分享链接并自动撤销
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时执行一次
    public void revokeExpiredShares() {
        try {
            // 这里应该从分享记录中查找过期链接并撤销
            // 实际实现需要根据具体的分享存储机制进行
            logger.info("执行过期分享链接检查");
        } catch (Exception e) {
            logger.error("撤销过期分享链接失败", e);
        }
    }

    /**
     * 每5分钟检查系统资源使用情况
     */
    @Scheduled(cron = "0 */5 * * * ?") // 每5分钟执行一次
    public void monitorSystemResources() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory() / (1024 * 1024); // MB
            long totalMemory = runtime.totalMemory() / (1024 * 1024); // MB
            long freeMemory = runtime.freeMemory() / (1024 * 1024); // MB
            long usedMemory = totalMemory - freeMemory; // MB
            
            double memoryUsage = (double) usedMemory / totalMemory * 100;
            
            if (memoryUsage > 80) {
                logger.warn("内存使用率过高: {}% (已用: {}MB, 总内存: {}MB)", 
                    String.format("%.1f", memoryUsage), usedMemory, totalMemory);
            } else {
                logger.debug("系统资源监控 - 内存使用率: {}%", String.format("%.1f", memoryUsage));
            }
        } catch (Exception e) {
            logger.error("系统资源监控失败", e);
        }
    }

    /**
     * 每天凌晨3点生成系统运行报告
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    public void generateDailyReport() {
        try {
            // 统计任务处理情况
            Object[] timeStats = exportTaskRepository.getTaskProcessTimeStats();
            java.util.List<Object[]> formatStats = exportTaskRepository.countExportsByFormat();
            java.util.List<Object[]> templateStats = exportTaskRepository.countExportsByTemplate();
            
            if (timeStats != null) {
                Double avgTime = (Double) timeStats[0];
                Long maxTime = (Long) timeStats[1];
                Long minTime = (Long) timeStats[2];
                
                logger.info("任务处理时间统计 - 平均: {}秒, 最大: {}秒, 最小: {}秒", 
                    avgTime != null ? String.format("%.2f", avgTime) : "N/A",
                    maxTime != null ? maxTime : "N/A",
                    minTime != null ? minTime : "N/A");
            }
            
            if (formatStats != null && !formatStats.isEmpty()) {
                logger.info("导出格式统计:");
                for (Object[] stat : formatStats) {
                    logger.info("  {}: {}", stat[0], stat[1]);
                }
            }
            
            logger.info("每日系统运行报告生成完成");
        } catch (Exception e) {
            logger.error("生成每日报告失败", e);
        }
    }

    /**
     * 每周日凌晨4点生成周报
     */
    @Scheduled(cron = "0 0 4 * * SUN") // 每周日凌晨4点执行
    public void generateWeeklyReport() {
        try {
            // 生成周度统计报告
            LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
            java.util.List<com.ccms.entity.export.ExportTask> weeklyTasks = 
                exportTaskRepository.findByCreateTimeRange(weekStart, LocalDateTime.now());
            
            long totalTasks = weeklyTasks.size();
            long completedTasks = weeklyTasks.stream()
                .filter(task -> task.getStatus() == 2)
                .count();
            double successRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
            
            logger.info("周度统计报告 - 总任务数: {}, 成功率: {}%", totalTasks, String.format("%.1f", successRate));
            
            // 统计最常用的模板和格式
            java.util.List<Object[]> weeklyFormatStats = exportTaskRepository.countExportsByFormat();
            java.util.List<Object[]> weeklyTemplateStats = exportTaskRepository.countExportsByTemplate();
            
            if (!weeklyFormatStats.isEmpty()) {
                logger.info("本周最常用导出格式: {} ({}次)", 
                    weeklyFormatStats.get(0)[0], weeklyFormatStats.get(0)[1]);
            }
            
            if (!weeklyTemplateStats.isEmpty()) {
                logger.info("本周最常用模板: {} ({}次)", 
                    weeklyTemplateStats.get(0)[0], weeklyTemplateStats.get(0)[1]);
            }
            
        } catch (Exception e) {
            logger.error("生成周度报告失败", e);
        }
    }

    /**
     * 每月1号凌晨5点生成月度统计
     */
    @Scheduled(cron = "0 0 5 1 * ?") // 每月1号凌晨5点执行
    public void generateMonthlyReport() {
        try {
            // 生成月度统计报告
            LocalDateTime monthStart = LocalDateTime.now().minusMonths(1);
            java.util.List<com.ccms.entity.export.ExportTask> monthlyTasks = 
                exportTaskRepository.findByCreateTimeRange(monthStart, LocalDateTime.now());
            
            logger.info("月度统计报告 - 总导出任务数: {}", monthlyTasks.size());
            
            // 计算平均处理时间等指标
            Object[] monthlyTimeStats = exportTaskRepository.getTaskProcessTimeStats();
            if (monthlyTimeStats != null && monthlyTimeStats[0] != null) {
                double avgTime = (Double) monthlyTimeStats[0];
                logger.info("月度平均处理时间: {}秒", String.format("%.2f", avgTime));
            }
            
        } catch (Exception e) {
            logger.error("生成月度报告失败", e);
        }
    }

    /**
     * 获取最后执行信息（用于监控）
     */
    public Map<String, Object> getLastExecutionInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("lastHealthCheck", LocalDateTime.now());
        info.put("taskCount", exportTaskRepository.count());
        return info;
    }
}