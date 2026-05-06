package com.ccms.controller.archive;

import com.ccms.controller.BaseControllerTest;
import com.ccms.service.archive.DataArchiveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 数据归档控制器单元测试
 */
@WebMvcTest(DataArchiveController.class)
class DataArchiveControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataArchiveService dataArchiveService;

    @Autowired
    private ObjectMapper objectMapper;

    private DataArchiveService.ArchiveResult testArchiveResult;
    private DataArchiveService.ArchiveRecord testArchiveRecord;

    @BeforeEach
    void setUp() {
        testArchiveResult = createTestArchiveResult();
        testArchiveRecord = createTestArchiveRecord();
    }

    @Test
    void shouldExecuteArchiveSuccessfully() throws Exception {
        // Given
        when(dataArchiveService.executeArchive(any(LocalDate.class)))
                .thenReturn(testArchiveResult);

        // When & Then
        mockMvc.perform(post("/api/archive/execute")
                        .param("archiveDate", "2025-01-01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.archivedCount").value(1000));
    }

    @Test
    void shouldQueryArchivedDataSuccessfully() throws Exception {
        // Given
        List<DataArchiveService.ArchiveRecord> records = Arrays.asList(
                testArchiveRecord,
                createTestArchiveRecord(2L)
        );
        when(dataArchiveService.queryArchivedData(any(DataArchiveService.ArchiveQuery.class)))
                .thenReturn(records);

        DataArchiveService.ArchiveQuery query = new DataArchiveService.ArchiveQuery();
        query.setTableName("expense_apply");
        query.setStartDate(LocalDate.of(2025, 1, 1));
        query.setEndDate(LocalDate.of(2025, 12, 31));

        // When & Then
        mockMvc.perform(post("/api/archive/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldRestoreArchivedDataSuccessfully() throws Exception {
        // Given
        when(dataArchiveService.restoreArchivedData(eq(1L)))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/archive/restore/{archiveId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldReturnArchivePoliciesSuccessfully() throws Exception {
        // Given
        List<DataArchiveService.ArchivePolicy> policies = Arrays.asList(
                createTestArchivePolicy("expense_apply", 180),
                createTestArchivePolicy("operation_log", 90)
        );
        when(dataArchiveService.getArchivePolicies())
                .thenReturn(policies);

        // When & Then
        mockMvc.perform(get("/api/archive/policies"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldUpdateArchivePolicySuccessfully() throws Exception {
        // Given
        DataArchiveService.ArchivePolicy policy = createTestArchivePolicy("expense_apply", 365);
        when(dataArchiveService.updateArchivePolicy(any(DataArchiveService.ArchivePolicy.class)))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(put("/api/archive/policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policy)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldArchiveBusinessDataSuccessfully() throws Exception {
        // Given
        when(dataArchiveService.archiveBusinessData(eq("expense_apply"), any(LocalDate.class)))
                .thenReturn(500);

        // When & Then
        mockMvc.perform(post("/api/archive/business")
                        .param("tableName", "expense_apply")
                        .param("archiveDate", "2025-01-01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(500));
    }

    @Test
    void shouldArchiveOperationLogsSuccessfully() throws Exception {
        // Given
        when(dataArchiveService.archiveOperationLogs(any(LocalDate.class)))
                .thenReturn(10000);

        // When & Then
        mockMvc.perform(post("/api/archive/operation-logs")
                        .param("archiveDate", "2025-01-01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(10000));
    }

    @Test
    void shouldArchiveSystemLogsSuccessfully() throws Exception {
        // Given
        when(dataArchiveService.archiveSystemLogs(any(LocalDate.class)))
                .thenReturn(5000);

        // When & Then
        mockMvc.perform(post("/api/archive/system-logs")
                        .param("archiveDate", "2025-01-01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5000));
    }

    @Test
    void shouldGetArchiveStatisticsSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/archive/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalArchivedRecords").value(25000))
                .andExpect(jsonPath("$.data.archiveFileSize").value("2.5GB"))
                .andExpect(jsonPath("$.data.successArchiveOperations").value(45));
    }

    private DataArchiveService.ArchiveResult createTestArchiveResult() {
        DataArchiveService.ArchiveResult result = new DataArchiveService.ArchiveResult();
        result.setSuccess(true);
        result.setArchivedCount(1000);
        result.setMessage("归档成功");
        return result;
    }

    private DataArchiveService.ArchiveRecord createTestArchiveRecord() {
        return createTestArchiveRecord(1L);
    }

    private DataArchiveService.ArchiveRecord createTestArchiveRecord(Long id) {
        DataArchiveService.ArchiveRecord record = new DataArchiveService.ArchiveRecord();
        record.setId(id);
        record.setTableName("expense_apply");
        record.setRecordCount(100);
        record.setArchiveDate(LocalDate.now());
        return record;
    }

    private DataArchiveService.ArchivePolicy createTestArchivePolicy(String tableName, int retentionDays) {
        DataArchiveService.ArchivePolicy policy = new DataArchiveService.ArchivePolicy();
        policy.setTableName(tableName);
        policy.setRetentionDays(retentionDays);
        policy.setEnabled(true);
        return policy;
    }
}
