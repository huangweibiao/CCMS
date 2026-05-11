package com.ccms.controller.archive;

import com.ccms.controller.ControllerTestBase;
import com.ccms.service.archive.DataArchiveService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 数据归档控制器单元测试
 */
@WebMvcTest(DataArchiveController.class)
class DataArchiveControllerTest extends ControllerTestBase {

    @MockBean
    private DataArchiveService dataArchiveService;

    @Test
    void shouldExecuteArchiveSuccess() throws Exception {
        DataArchiveService.ArchiveResult result = new DataArchiveService.ArchiveResult(
            100, 20, 30, 50, LocalDate.of(2024, 1, 1)
        );
        when(dataArchiveService.executeArchive(any(LocalDate.class))).thenReturn(result);

        performPost("/api/archive/execute?archiveDate=2024-01-01")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalArchivedCount").value(100));
    }

    @Test
    void shouldQueryArchivedDataSuccess() throws Exception {
        DataArchiveService.ArchiveRecord record = new DataArchiveService.ArchiveRecord();
        record.setArchiveId(1L);
        when(dataArchiveService.queryArchivedData(any(DataArchiveService.ArchiveQuery.class)))
                .thenReturn(Collections.singletonList(record));

        performPost("/api/archive/query", new DataArchiveService.ArchiveQuery(
            "ccms_expense_apply",
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 12, 31)
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void shouldRestoreArchivedDataSuccess() throws Exception {
        when(dataArchiveService.restoreArchivedData(1L)).thenReturn(true);

        performPost("/api/archive/restore/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldGetArchivePoliciesSuccess() throws Exception {
        DataArchiveService.ArchivePolicy policy1 = new DataArchiveService.ArchivePolicy(
            "ccms_expense_apply", 3
        );
        DataArchiveService.ArchivePolicy policy2 = new DataArchiveService.ArchivePolicy(
            "ccms_reimburse", 5
        );
        when(dataArchiveService.getArchivePolicies()).thenReturn(Arrays.asList(policy1, policy2));

        performGet("/api/archive/policies")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void shouldUpdateArchivePolicySuccess() throws Exception {
        when(dataArchiveService.updateArchivePolicy(any(DataArchiveService.ArchivePolicy.class))).thenReturn(true);

        performPut("/api/archive/policy", new DataArchiveService.ArchivePolicy(
            "ccms_expense_apply", 3
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldArchiveBusinessDataSuccess() throws Exception {
        when(dataArchiveService.archiveBusinessData("expense_apply", LocalDate.of(2024, 1, 1))).thenReturn(50);

        performPost("/api/archive/business?tableName=expense_apply&archiveDate=2024-01-01")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(50));
    }

    @Test
    void shouldArchiveOperationLogsSuccess() throws Exception {
        when(dataArchiveService.archiveOperationLogs(any(LocalDate.class))).thenReturn(200);

        performPost("/api/archive/operation-logs?archiveDate=2024-01-01")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(200));
    }

    @Test
    void shouldArchiveSystemLogsSuccess() throws Exception {
        when(dataArchiveService.archiveSystemLogs(any(LocalDate.class))).thenReturn(500);

        performPost("/api/archive/system-logs?archiveDate=2024-01-01")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(500));
    }

    @Test
    void shouldGetArchiveStatisticsSuccess() throws Exception {
        performGet("/api/archive/statistics")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalArchivedRecords").exists())
                .andExpect(jsonPath("$.data.lastArchiveDate").exists())
                .andExpect(jsonPath("$.data.archiveFileSize").exists())
                .andExpect(jsonPath("$.data.successArchiveOperations").exists());
    }
}
