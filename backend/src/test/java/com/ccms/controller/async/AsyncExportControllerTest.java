package com.ccms.controller.async;

import com.ccms.controller.BaseControllerTest;
import com.ccms.service.async.AsyncExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 异步导出控制器单元测试
 */
@WebMvcTest(AsyncExportController.class)
class AsyncExportControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AsyncExportService asyncExportService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldCreateExportTaskSuccessfully() throws Exception {
        // Given
        when(asyncExportService.createExportTask(anyString(), anyMap(), anyString(), anyString()))
                .thenReturn("task-001");
        doNothing().when(asyncExportService).executeExportTask(anyString());

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", "2025-01-01");
        params.put("endDate", "2025-12-31");

        // When & Then
        mockMvc.perform(post("/api/async/export/create")
                        .header("X-User-Id", "1001")
                        .param("templateCode", "EXPENSE_REPORT")
                        .param("format", "excel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskId").value("task-001"));
    }

    @Test
    void shouldGetTaskStatusSuccessfully() throws Exception {
        // Given
        Map<String, Object> status = new HashMap<>();
        status.put("taskId", "task-001");
        status.put("status", "COMPLETED");
        status.put("progress", 100);
        when(asyncExportService.getTaskStatus(eq("task-001"))).thenReturn(status);

        // When & Then
        mockMvc.perform(get("/api/async/export/status/{taskId}", "task-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void shouldCancelTaskSuccessfully() throws Exception {
        // Given
        when(asyncExportService.cancelTask(eq("task-001"))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/async/export/cancel/{taskId}", "task-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("任务取消成功"));
    }

    @Test
    void shouldReturnBadRequest_whenCancelCompletedTask() throws Exception {
        // Given
        when(asyncExportService.cancelTask(eq("task-001"))).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/async/export/cancel/{taskId}", "task-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldGetUserTasksSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/async/export/list")
                        .header("X-User-Id", "1001")
                        .param("page", "1")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.data").isArray());
    }

    @Test
    void shouldGetTaskStatsSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/async/export/stats")
                        .header("X-User-Id", "1001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalTasks").value(15))
                .andExpect(jsonPath("$.data.completedTasks").value(10));
    }

    @Test
    void shouldCleanupTasksSuccessfully() throws Exception {
        // Given
        doNothing().when(asyncExportService).cleanExpiredTasks();

        // When & Then
        mockMvc.perform(post("/api/async/export/cleanup")
                        .header("X-User-Id", "1001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldRetryTaskSuccessfully() throws Exception {
        // Given
        doNothing().when(asyncExportService).executeExportTask(eq("task-001"));

        // When & Then
        mockMvc.perform(post("/api/async/export/retry/{taskId}", "task-001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskId").value("task-001"));
    }

    @Test
    void shouldBatchSubmitTasksSuccessfully() throws Exception {
        // Given
        when(asyncExportService.createExportTask(anyString(), anyMap(), anyString(), anyString()))
                .thenReturn("task-001", "task-002");
        doNothing().when(asyncExportService).executeExportTask(anyString());

        List<Map<String, Object>> tasks = Arrays.asList(
                Map.of("templateCode", "REPORT1", "format", "excel", "params", new HashMap<>()),
                Map.of("templateCode", "REPORT2", "format", "pdf", "params", new HashMap<>())
        );
        Map<String, Object> request = Map.of("tasks", tasks);

        // When & Then
        mockMvc.perform(post("/api/async/export/batch")
                        .header("X-User-Id", "1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskIds").isArray());
    }
}
