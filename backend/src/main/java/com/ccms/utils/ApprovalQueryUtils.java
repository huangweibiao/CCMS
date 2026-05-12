package com.ccms.utils;

import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalStatus;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审批查询工具类
 * 提供常用的查询和分析功能
 */
@UtilityClass
public class ApprovalQueryUtils {

    /**
     * 根据状态过滤审批实例
     */
    public static List<ApprovalInstance> filterByStatus(List<ApprovalInstance> instances, ApprovalStatus status) {
        return instances.stream()
                .filter(instance -> instance.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * 根据状态范围过滤审批实例
     */
    public static List<ApprovalInstance> filterByStatusRange(List<ApprovalInstance> instances, List<ApprovalStatus> statuses) {
        return instances.stream()
                .filter(instance -> statuses.contains(instance.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 获取等待审批的实例
     */
    public static List<ApprovalInstance> getPendingApprovals(List<ApprovalInstance> instances) {
        return filterByStatusRange(instances, List.of(
                ApprovalStatus.PENDING,
                ApprovalStatus.IN_PROGRESS
        ));
    }

    /**
     * 获取已完成的审批实例
     */
    public static List<ApprovalInstance> getCompletedApprovals(List<ApprovalInstance> instances) {
        return instances.stream()
                .filter(instance -> instance.getStatus().isFinalStatus())
                .collect(Collectors.toList());
    }

    /**
     * 计算审批成功率
     */
    public static double calculateApprovalRate(List<ApprovalInstance> instances) {
        List<ApprovalInstance> completed = getCompletedApprovals(instances);
        if (completed.isEmpty()) {
            return 0.0;
        }

        long approvedCount = completed.stream()
                .filter(instance -> instance.getStatus() == ApprovalStatus.APPROVED)
                .count();

        return (double) approvedCount / completed.size() * 100;
    }

    /**
     * 计算平均审批时长（小时）
     */
    public static double calculateAverageApprovalTime(List<ApprovalInstance> completedInstances) {
        return completedInstances.stream()
                .mapToDouble(instance -> {
                    LocalDateTime startTime = instance.getCreateTime();
                    LocalDateTime endTime = instance.getUpdateTime();
                    if (startTime == null || endTime == null) {
                        return 0.0;
                    }
                    java.time.Duration duration = java.time.Duration.between(startTime, endTime);
                    return duration.toHours();
                })
                .average()
                .orElse(0.0);
    }

    /**
     * 按审批人统计审批记录
     */
    public static Map<Long, List<ApprovalRecord>> groupRecordsByApprover(List<ApprovalRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(ApprovalRecord::getApproverId));
    }

    /**
     * 按日期范围过滤审批实例
     */
    public static List<ApprovalInstance> filterByDateRange(List<ApprovalInstance> instances, 
                                                          LocalDateTime startDate, 
                                                          LocalDateTime endDate) {
        return instances.stream()
                .filter(instance -> {
                    LocalDateTime createTime = instance.getCreateTime();
                    return createTime != null && 
                           !createTime.isBefore(startDate) && 
                           !createTime.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    /**
     * 按金额范围过滤审批实例
     */
    public static List<ApprovalInstance> filterByAmountRange(List<ApprovalInstance> instances, 
                                                            BigDecimal minAmount, 
                                                            BigDecimal maxAmount) {
        return instances.stream()
                .filter(instance -> {
                    BigDecimal amount = instance.getAmount();
                    if (amount == null) {
                        return false;
                    }
                    
                    boolean valid = true;
                    if (minAmount != null) {
                        valid = valid && amount.compareTo(minAmount) >= 0;
                    }
                    if (maxAmount != null) {
                        valid = valid && amount.compareTo(maxAmount) <= 0;
                    }
                    return valid;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取审批记录的时间线
     */
    public static List<ApprovalRecord> getApprovalTimeline(List<ApprovalRecord> records) {
        return records.stream()
                .sorted((r1, r2) -> {
                    LocalDateTime t1 = r1.getApprovalTime();
                    LocalDateTime t2 = r2.getApprovalTime();
                    if (t1 == null && t2 == null) return 0;
                    if (t1 == null) return 1;
                    if (t2 == null) return -1;
                    return t1.compareTo(t2);
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查审批实例是否超时
     */
    public static boolean isApprovalTimeout(ApprovalInstance instance, long timeoutHours) {
        if (instance == null || instance.getCreateTime() == null) {
            return false;
        }

        if (instance.getStatus().isFinalStatus()) {
            return false; // 已完成的实例不算超时
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createTime = instance.getCreateTime();
        java.time.Duration duration = java.time.Duration.between(createTime, now);
        return duration.toHours() > timeoutHours;
    }

    /**
     * 统计各状态审批实例数量
     */
    public static Map<ApprovalStatus, Long> countByStatus(List<ApprovalInstance> instances) {
        return instances.stream()
                .collect(Collectors.groupingBy(
                        ApprovalInstance::getStatus,
                        Collectors.counting()
                ));
    }

    /**
     * 获取未处理的审批实例
     */
    public static List<ApprovalInstance> getUnprocessedApprovals(List<ApprovalInstance> instances, Long approverId) {
        return instances.stream()
                .filter(instance -> 
                        !instance.getStatus().isFinalStatus() && 
                        approverId.equals(instance.getCurrentApproverId())
                )
                .collect(Collectors.toList());
    }

    /**
     * 验证审批记录数据完整性
     */
    public static boolean validateApprovalRecords(List<ApprovalRecord> records) {
        if (records == null || records.isEmpty()) {
            return false;
        }

        // 检查是否有重复的审批人记录
        long distinctApprovers = records.stream()
                .map(ApprovalRecord::getApproverId)
                .distinct()
                .count();

        if (distinctApprovers != records.size()) {
            return false; // 存在重复审批人
        }

        // 检查时间顺序
        List<ApprovalRecord> sortedReco rds = getApprovalTimeline(records);
        for (int i = 1; i < sortedRecords.size(); i++) {
            ApprovalRecord current = sortedRecords.get(i);
            ApprovalRecord previous = sortedRecords.get(i - 1);
            
            if (current.getApprovalTime() != null && previous.getApprovalTime() != null &&
                current.getApprovalTime().isBefore(previous.getApprovalTime())) {
                return false; // 时间顺序异常
            }
        }

        return true;
    }

    /**
     * 计算审批处理效率
     */
    public static Map<String, Object> calculateApprovalEfficiency(List<ApprovalRecord> records) {
        if (records.isEmpty()) {
            return Map.of(
                    "totalRecords", 0,
                    "averageProcessingTime", 0.0,
                    "totalProcessingHours", 0.0,
                    "efficiencyScore", 0.0
            );
        }

        double totalHours = records.stream()
                .mapToDouble(record -> {
                    if (record.getProcessingTime() != null) {
                        return record.getProcessingTime();
                    }
                    return 0.0;
                })
                .sum();

        double avgHours = totalHours / records.size();
        double efficiencyScore = avgHours > 0 ? 100.0 / avgHours : 100.0;

        return Map.of(
                "totalRecords", records.size(),
                "averageProcessingTime", Math.round(avgHours * 100.0) / 100.0,
                "totalProcessingHours", Math.round(totalHours * 100.0) / 100.0,
                "efficiencyScore", Math.round(efficiencyScore * 100.0) / 100.0
        );
    }
}