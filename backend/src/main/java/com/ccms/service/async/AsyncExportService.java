package com.ccms.service.async;

import com.ccms.entity.export.ExportTask;
import com.ccms.repository.export.ExportTaskRepository;
import com.ccms.service.export.ReportExportService;
import com.ccms.service.report.ReportTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 异步导出服务
 */
@Service
public class AsyncExportService {

    @Autowired
    private ExportTaskRepository exportTaskRepository;

    @Autowired
    private ReportTemplateService reportTemplateService;

    @Autowired
    private ReportExportService reportExportService;

    /**
     * 创建异步导出任务
     */
    public String createExportTask(String templateCode, Map<String, Object> params, String format, String createBy) {
        ExportTask task = new ExportTask();
        task.setTaskId(UUID.randomUUID().toString());
        task.setTemplateCode(templateCode);
        task.setExportFormat(format);
        task.setExportParams(params.toString());
        task.setCreateBy(createBy);
        task.setStatus(0); // 待处理
        task.setCreateTime(LocalDateTime.now());
        
        exportTaskRepository.save(task);
        return task.getTaskId();
    }

    /**
     * 异步执行导出任务
     */
    @Async("exportTaskExecutor")
    @Transactional
    public void executeExportTask(String taskId) {
        try {
            ExportTask task = exportTaskRepository.findByTaskId(taskId)
                    .orElseThrow(() -> new RuntimeException("导出任务不存在"));
            
            // 更新任务状态为处理中
            task.setStatus(1);
            task.setStartTime(LocalDateTime.now());
            exportTaskRepository.save(task);
            
            // 执行导出逻辑
            // 这里可以实现具体的导出逻辑
            // 例如：生成文件并保存到指定位置
            
            // 模拟处理时间
            Thread.sleep(5000);
            
            // 任务完成
            task.setStatus(2);
            task.setEndTime(LocalDateTime.now());
            task.setResultPath("/export/files/" + taskId + "." + task.getExportFormat());
            exportTaskRepository.save(task);
            
        } catch (Exception e) {
            // 处理失败
            ExportTask task = exportTaskRepository.findByTaskId(taskId).orElse(null);
            if (task != null) {
                task.setStatus(3);
                task.setErrorMessage(e.getMessage());
                task.setEndTime(LocalDateTime.now());
                exportTaskRepository.save(task);
            }
        }
    }

    /**
     * 获取任务状态
     */
    public Map<String, Object> getTaskStatus(String taskId) {
        return exportTaskRepository.findByTaskId(taskId)
                .map(task -> {
                    Map<String, Object> status = new java.util.HashMap<>();
                    status.put("taskId", task.getTaskId());
                    status.put("status", task.getStatus());
                    status.put("statusText", getStatusText(task.getStatus()));
                    status.put("createTime", task.getCreateTime());
                    status.put("startTime", task.getStartTime());
                    status.put("endTime", task.getEndTime());
                    status.put("resultPath", task.getResultPath());
                    status.put("errorMessage", task.getErrorMessage());
                    return status;
                })
                .orElseThrow(() -> new RuntimeException("任务不存在"));
    }

    /**
     * 取消导出任务
     */
    public boolean cancelTask(String taskId) {
        ExportTask task = exportTaskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        if (task.getStatus() == 0 || task.getStatus() == 1) {
            task.setStatus(4); // 已取消
            task.setEndTime(LocalDateTime.now());
            exportTaskRepository.save(task);
            return true;
        }
        
        return false;
    }

    /**
     * 清理过期的任务记录
     */
    @Async
    @Transactional
    public void cleanExpiredTasks() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        exportTaskRepository.deleteByCreateTimeBeforeAndStatusIn(
                threshold, java.util.Arrays.asList(2, 3, 4));
    }

    private String getStatusText(int status) {
        switch (status) {
            case 0: return "待处理";
            case 1: return "处理中";
            case 2: return "已完成";
            case 3: return "失败";
            case 4: return "已取消";
            default: return "未知";
        }
    }
}