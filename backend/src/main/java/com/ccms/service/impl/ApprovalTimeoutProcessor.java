package com.ccms.service.impl;

import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.repository.approval.ApprovalInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalTimeoutProcessor {
    
    private final ApprovalInstanceRepository instanceRepository;
    
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean isRunning = false;
    
    // 默认超时时间配置（单位：小时）
    private long defaultTimeoutHours = 24;
    private long monitoringIntervalMinutes = 30;
    
    /**
     * 启动超时处理服务
     */
    public void startTimeoutProcessing() {
        if (isRunning) {
            log.warn("超时处理服务已经在运行中");
            return;
        }
        
        isRunning = true;
        log.info("启动审批流程超时处理服务，检查间隔: {} 分钟", monitoringIntervalMinutes);
        
        scheduler.scheduleAtFixedRate(this::processTimeouts, 0, monitoringIntervalMinutes, TimeUnit.MINUTES);
    }
    
    /**
     * 停止超时处理服务
     */
    public void stopTimeoutProcessing() {
        isRunning = false;
        scheduler.shutdown();
        log.info("停止审批流程超时处理服务");
    }
    
    /**
     * 处理超时的审批实例
     */
    public List<TimeoutResult> processTimeouts() {
        if (!isRunning) {
            log.debug("超时处理服务已停止，跳过处理");
            return new ArrayList<>();
        }
        
        log.info("开始检查超时审批实例");
        List<TimeoutResult> results = new ArrayList<>();
        
        try {
            // 查找运行中的审批实例
            List<ApprovalInstance> runningInstances = instanceRepository.findByStatus(ApprovalStatusEnum.RUNNING);
            
            for (ApprovalInstance instance : runningInstances) {
                TimeoutResult result = checkAndProcessTimeout(instance);
                if (result != null) {
                    results.add(result);
                }
            }
            
            log.info("超时检查完成，处理了 {} 个超时实例", results.size());
            
        } catch (Exception e) {
            log.error("处理超时实例时发生异常", e);
        }
        
        return results;
    }
    
    /**
     * 检查单个实例是否超时并处理
     */
    private TimeoutResult checkAndProcessTimeout(ApprovalInstance instance) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeoutThreshold = getTimeoutThreshold(instance);
        
        if (instance.getCreateTime().isBefore(timeoutThreshold)) {
            log.warn("发现超时审批实例: ID={}, 创建时间={}, 业务类型={}, 业务ID={}", 
                    instance.getId(), instance.getCreateTime(), 
                    instance.getBusinessType(), instance.getBusinessId());
            
            // 标记为超时
            instance.setStatus(ApprovalStatusEnum.TIMEOUT);
            instance.setFinishTime(now);
            instance.setRemarks("审批流程超时自动终止");
            
            try {
                ApprovalInstance savedInstance = instanceRepository.save(instance);
                log.info("超时实例处理完成: ID={}", savedInstance.getId());
                
                return TimeoutResult.builder()
                        .instanceId(instance.getId())
                        .businessType(instance.getBusinessType())
                        .businessId(instance.getBusinessId())
                        .createTime(instance.getCreateTime())
                        .timeoutTime(now)
                        .durationHours(getDurationHours(instance.getCreateTime(), now))
                        .status(ApprovalStatusEnum.TIMEOUT)
                        .success(true)
                        .build();
                        
            } catch (Exception e) {
                log.error("保存超时实例失败: ID={}", instance.getId(), e);
                
                return TimeoutResult.builder()
                        .instanceId(instance.getId())
                        .businessType(instance.getBusinessType())
                        .businessId(instance.getBusinessId())
                        .createTime(instance.getCreateTime())
                        .timeoutTime(now)
                        .durationHours(getDurationHours(instance.getCreateTime(), now))
                        .status(instance.getStatus())
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build();
            }
        }
        
        return null;
    }
    
    /**
     * 获取超时阈值时间
     */
    private LocalDateTime getTimeoutThreshold(ApprovalInstance instance) {
        // 这里可以扩展为根据流程配置、业务类型等设置不同的超时时间
        return LocalDateTime.now().minusHours(getTimeoutHoursForInstance(instance));
    }
    
    /**
     * 获取实例的超时时间（扩展点）
     */
    private long getTimeoutHoursForInstance(ApprovalInstance instance) {
        // 默认实现，可以根据业务需求扩展
        // 例如：根据业务类型、流程配置等返回不同的超时时间
        return defaultTimeoutHours;
    }
    
    /**
     * 计算持续时间（小时）
     */
    private double getDurationHours(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        return duration.toMinutes() / 60.0;
    }
    
    /**
     * 设置默认超时时间
     */
    public void setDefaultTimeoutHours(long hours) {
        this.defaultTimeoutHours = hours;
        log.info("设置默认超时时间为 {} 小时", hours);
    }
    
    /**
     * 设置监控间隔时间
     */
    public void setMonitoringIntervalMinutes(long minutes) {
        this.monitoringIntervalMinutes = minutes;
        log.info("设置监控间隔为 {} 分钟", minutes);
    }
    
    /**
     * 超时处理结果类
     */
    public static class TimeoutResult {
        private Long instanceId;
        private String businessType;
        private String businessId;
        private LocalDateTime createTime;
        private LocalDateTime timeoutTime;
        private Double durationHours;
        private ApprovalStatusEnum status;
        private boolean success;
        private String errorMessage;
        
        // 构建器方法
        public static TimeoutResultBuilder builder() {
            return new TimeoutResultBuilder();
        }
        
        // Getter 和 Setter
        public Long getInstanceId() { return instanceId; }
        public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
        
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        
        public String getBusinessId() { return businessId; }
        public void setBusinessId(String businessId) { this.businessId = businessId; }
        
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
        
        public LocalDateTime getTimeoutTime() { return timeoutTime; }
        public void setTimeoutTime(LocalDateTime timeoutTime) { this.timeoutTime = timeoutTime; }
        
        public Double getDurationHours() { return durationHours; }
        public void setDurationHours(Double durationHours) { this.durationHours = durationHours; }
        
        public ApprovalStatusEnum getStatus() { return status; }
        public void setStatus(ApprovalStatusEnum status) { this.status = status; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
    
    /**
     * 超时结果构建器
     */
    public static class TimeoutResultBuilder {
        private TimeoutResult result = new TimeoutResult();
        
        public TimeoutResultBuilder instanceId(Long instanceId) {
            result.setInstanceId(instanceId);
            return this;
        }
        
        public TimeoutResultBuilder businessType(String businessType) {
            result.setBusinessType(businessType);
            return this;
        }
        
        public TimeoutResultBuilder businessId(String businessId) {
            result.setBusinessId(businessId);
            return this;
        }
        
        public TimeoutResultBuilder createTime(LocalDateTime createTime) {
            result.setCreateTime(createTime);
            return this;
        }
        
        public TimeoutResultBuilder timeoutTime(LocalDateTime timeoutTime) {
            result.setTimeoutTime(timeoutTime);
            return this;
        }
        
        public TimeoutResultBuilder durationHours(Double durationHours) {
            result.setDurationHours(durationHours);
            return this;
        }
        
        public TimeoutResultBuilder status(ApprovalStatusEnum status) {
            result.setStatus(status);
            return this;
        }
        
        public TimeoutResultBuilder success(boolean success) {
            result.setSuccess(success);
            return this;
        }
        
        public TimeoutResultBuilder errorMessage(String errorMessage) {
            result.setErrorMessage(errorMessage);
            return this;
        }
        
        public TimeoutResult build() {
            return result;
        }
    }
    
    /**
     * 获取处理统计信息
     */
    public TimeoutStatistics getStatistics() {
        LocalDateTime startTime = LocalDateTime.now().minusHours(24);
        List<ApprovalInstance> timeoutInstances = instanceRepository
                .findByStatusAndFinishTimeAfter(ApprovalStatusEnum.TIMEOUT, startTime);
        
        long totalCount = timeoutInstances.size();
        double avgDuration = timeoutInstances.stream()
                .mapToDouble(instance -> getDurationHours(instance.getCreateTime(), instance.getFinishTime()))
                .average()
                .orElse(0.0);
                
        return new TimeoutStatistics(totalCount, avgDuration, startTime, LocalDateTime.now());
    }
    
    /**
     * 超时统计信息类
     */
    public static class TimeoutStatistics {
        private final long totalCount;
        private final double averageDurationHours;
        private final LocalDateTime fromTime;
        private final LocalDateTime toTime;
        
        public TimeoutStatistics(long totalCount, double averageDurationHours, 
                                LocalDateTime fromTime, LocalDateTime toTime) {
            this.totalCount = totalCount;
            this.averageDurationHours = averageDurationHours;
            this.fromTime = fromTime;
            this.toTime = toTime;
        }
        
        public long getTotalCount() { return totalCount; }
        public double getAverageDurationHours() { return averageDurationHours; }
        public LocalDateTime getFromTime() { return fromTime; }
        public LocalDateTime getToTime() { return toTime; }
    }
}