package com.ccms.controller.async;

import com.ccms.controller.ControllerTestBase;
import com.ccms.service.async.AsyncExportService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 异步导出控制器单元测试
 */
@WebMvcTest(AsyncExportController.class)
class AsyncExportControllerTest extends ControllerTestBase {

    @MockBean
    private AsyncExportService asyncExportService;

    @Test
    void shouldCreateExportTaskSuccess() throws Exception {
        when(asyncExportService.createExportTask(any(), any(), any(), any())).thenReturn("task-123");
        doNothing().when(asyncExportService).executeExportTask("task-123");

        Map<String, Object> params = new HashMap<>();
        
        performPost("/api/async/export/create?templateCode=EXPENSE_REPORT&format=excel", params)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("导出任务创建成功"))
                .andExpect(jsonPath("$.data.taskId").value("task-123"))
                .andExpect(jsonPath("$.data.status").value("待处理"));
    }

    @Test
    void shouldGetTaskStatusSuccess() throws Exception {
        Map<String, Object> status = new HashMap<>();
        status.put("taskId", "task-123");
        status.put("status", "已完成");
        status.put("progress", 100);
        when(asyncExportService.getTaskStatus("task-123")).thenReturn(status);

        performGet("/api/async/export/status/task-123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("已完成"))
                .andExpect(jsonPath("$.data.progress").value(100));
    }

    @Test
    void shouldCancelTaskSuccess() throws Exception {
        when(asyncExportService.cancelTask("task-123")).thenReturn(true);

        performPost("/api/async/export/cancel/task-123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("任务取消成功"));
    }

    @Test
    void shouldCancelTaskFailWhenAlreadyCompleted() throws Exception {
        when(asyncExportService.cancelTask("task-123")).thenReturn(false);

        performPost("/api/async/export/cancel/task-123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("任务无法取消，可能已完成或正在处理"));
    }

    @Test
    void shouldGetUserTasksSuccess() throws Exception {
        performGet("/api/async/export/list?page=1&size=10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.data", hasSize(2)));
    }

    @Test
    void shouldGetTaskStatsSuccess() throws Exception {
        performGet("/api/async/export/stats")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalTasks").value(15))
                .andExpect(jsonPath("$.data.completedTasks").value(10))
                .andExpect(jsonPath("$.data.failedTasks").value(2));
    }

    @Test
    void shouldCleanupTasksSuccess() throws Exception {
        doNothing().when(asyncExportService).cleanExpiredTasks();

        performPost("/api/async/export/cleanup")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("清理任务已提交，将在后台执行"));
    }

    @Test
    void shouldRetryTaskSuccess() throws Exception {
        doNothing().when(asyncExportService).executeExportTask("task-123");

        performPost("/api/async/export/retry/task-123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("任务重试已提交"))
                .andExpect(jsonPath("$.data.taskId").value("task-123"));
    }

    @Test
    void shouldBatchSubmitTasksSuccess() throws Exception {
        when(asyncExportService.createExportTask(any(), any(), any(), any())).thenReturn("task-001", "task-002");
        doNothing().when(asyncExportService).executeExportTask(any());

        Map<String, Object> request = new HashMap<>();
        Map<String, Object> task1 = new HashMap<>();
        task1.put("templateCode", "EXPENSE_REPORT");
        task1.put("format", "excel");
        task1.put("params", new HashMap<>());
        
        Map<String, Object> task2 = new HashMap<>();
        task2.put("templateCode", "BUDGET_ANALYSIS");
        task2.put("format", "pdf");
        task2.put("params", new HashMap<>());
        
        request.put("tasks", java.util.Arrays.asList(task1, task2));

        performPost("/api/async/export/batch", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量任务提交成功"))
                .andExpect(jsonPath("$.data.taskIds", hasSize(2)));
    }
}
