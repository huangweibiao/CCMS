package com.ccms.controller.system;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.system.log.SysOperLog;
import com.ccms.repository.system.log.SysOperLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 操作日志控制器单元测试
 */
@WebMvcTest(SysOperLogController.class)
class SysOperLogControllerTest extends ControllerTestBase {

    @MockBean
    private SysOperLogRepository operLogRepository;

    private SysOperLog createTestOperLog(Long id, String module, String type) {
        SysOperLog log = new SysOperLog();
        log.setId(id);
        log.setOperModule(module);
        log.setOperType(type);
        log.setOperIp("192.168.1.1");
        log.setOperTime(LocalDateTime.now());
        log.setOperUserId("user123");
        log.setOperName("测试用户");
        log.setStatus(0);

        return log;
    }

    @Test
    void shouldReturnOperLogList() throws Exception {
        SysOperLog log = createTestOperLog(1L, "用户管理", "新增");
        Page<SysOperLog> page = new PageImpl<>(
                Collections.singletonList(log),
                PageRequest.of(0, 10),
                1
        );
        when(operLogRepository.findAll(any(PageRequest.class))).thenReturn(page);

        performGet("/api/system/oper-logs")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].operModule").value("用户管理"));
    }

    @Test
    void shouldReturnOperLogByIdWhenExists() throws Exception {
        SysOperLog log = createTestOperLog(1L, "用户管理", "新增");
        when(operLogRepository.findById(1L)).thenReturn(Optional.of(log));

        performGet("/api/system/oper-logs/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.operModule").value("用户管理"));
    }

    @Test
    void shouldReturnNotFoundWhenOperLogNotExists() throws Exception {
        when(operLogRepository.findById(999L)).thenReturn(Optional.empty());

        performGet("/api/system/oper-logs/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnOperLogsByUserId() throws Exception {
        SysOperLog log = createTestOperLog(1L, "用户管理", "新增");
        when(operLogRepository.findByOperUserId("user123"))
                .thenReturn(Collections.singletonList(log));

        performGet("/api/system/oper-logs/user/user123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnOperLogsByModule() throws Exception {
        SysOperLog log = createTestOperLog(1L, "用户管理", "新增");
        when(operLogRepository.findByOperModule("用户管理"))
                .thenReturn(Collections.singletonList(log));

        performGet("/api/system/oper-logs/module/用户管理")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnOperLogsByType() throws Exception {
        SysOperLog log = createTestOperLog(1L, "用户管理", "新增");
        when(operLogRepository.findByOperType("新增"))
                .thenReturn(Collections.singletonList(log));

        performGet("/api/system/oper-logs/type/新增")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnOperLogsByBusinessId() throws Exception {
        SysOperLog log = createTestOperLog(1L, "用户管理", "新增");
        when(operLogRepository.findByBusinessId("bus123"))
                .thenReturn(Collections.singletonList(log));

        performGet("/api/system/oper-logs/business/bus123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnOperLogsByTimeRange() throws Exception {
        SysOperLog log = createTestOperLog(1L, "用户管理", "新增");
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        when(operLogRepository.findByOperTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(log));

        performGet("/api/system/oper-logs/time-range?startTime=" + startTime + "&endTime=" + endTime)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnRecentOperLogs() throws Exception {
        SysOperLog log = createTestOperLog(1L, "用户管理", "新增");
        when(operLogRepository.findRecentOperLogs(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(log));

        performGet("/api/system/oper-logs/recent?days=7")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnModuleOperFrequency() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        when(operLogRepository.findModuleOperFrequency(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        performGet("/api/system/oper-logs/module-frequency?startTime=" + startTime + "&endTime=" + endTime)
                .andExpect(status().isOk());
    }

    @Test
    void shouldCountUserOperations() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        when(operLogRepository.countByOperUserIdAndOperTimeBetween(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10L);

        performGet("/api/system/oper-logs/user/user123/count?startTime=" + startTime + "&endTime=" + endTime)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user123"))
                .andExpect(jsonPath("$.count").value(10));
    }

    @Test
    void shouldReturnOperLogsByIp() throws Exception {
        SysOperLog log = createTestOperLog(1L, "用户管理", "新增");
        when(operLogRepository.findByOperIp("192.168.1.1"))
                .thenReturn(Collections.singletonList(log));

        performGet("/api/system/oper-logs/ip/192.168.1.1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldDeleteExpiredLogs() throws Exception {
        performDelete("/api/system/oper-logs/expired?expireDays=90")
                .andExpect(status().isOk());

        verify(operLogRepository, times(1)).deleteExpiredLogs(any(LocalDateTime.class));
    }
}
