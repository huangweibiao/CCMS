package com.ccms.controller;

import com.ccms.entity.system.SysAttachment;
import com.ccms.service.SysAttachmentService;
import com.ccms.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SysAttachmentController.class)
class SysAttachmentControllerTest extends BaseControllerTest {

    @MockBean
    private SysAttachmentService attachmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private SysAttachment testAttachment;
    private List<SysAttachment> testAttachmentList;
    private Page<SysAttachment> testAttachmentPage;

    @BeforeEach
    void setUp() {
        testAttachment = TestDataFactory.createSysAttachment();
        testAttachmentList = List.of(
                TestDataFactory.createSysAttachment(),
                TestDataFactory.createSysAttachment(),
                TestDataFactory.createSysAttachment()
        );
        testAttachmentPage = new PageImpl<>(testAttachmentList, PageRequest.of(0, 20), testAttachmentList.size());
    }

    @Test
    void shouldReturnUploadResult_whenUploadFileSuccess() throws Exception {
        // Given
        SysAttachmentService.UploadResult uploadResult = new SysAttachmentService.UploadResult();
        uploadResult.setSuccess(true);
        uploadResult.setAttachmentId(1L);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", 
                MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(attachmentService.uploadFile(any(), any())).thenReturn(uploadResult);

        // When
        ResultActions result = mockMvc.perform(multipart("/api/attachments/upload")
                .file(file)
                .param("bizType", "expense")
                .param("bizId", "123")
                .param("description", "测试附件")
                .param("isPublic", "false")
                .header("X-User-Id", "user123")
                .header("X-User-Name", "测试用户"));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.attachmentId").value(1L));

        verify(attachmentService, times(1)).uploadFile(any(), any());
    }

    @Test
    void shouldReturnBadRequest_whenUploadFileFailed() throws Exception {
        // Given
        SysAttachmentService.UploadResult uploadResult = new SysAttachmentService.UploadResult();
        uploadResult.setSuccess(false);
        uploadResult.setErrorMessage("文件上传失败");

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", 
                MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(attachmentService.uploadFile(any(), any())).thenReturn(uploadResult);

        // When
        ResultActions result = mockMvc.perform(multipart("/api/attachments/upload")
                .file(file)
                .param("bizType", "expense")
                .param("bizId", "123")
                .header("X-User-Id", "user123")
                .header("X-User-Name", "测试用户"));

        // Then
        result.andExpect(status().isBadRequest());

        verify(attachmentService, times(1)).uploadFile(any(), any());
    }

    @Test
    void shouldReturnUploadResults_whenBatchUploadFilesSuccess() throws Exception {
        // Given
        SysAttachmentService.UploadResult result1 = new SysAttachmentService.UploadResult();
        result1.setSuccess(true);
        result1.setAttachmentId(1L);

        SysAttachmentService.UploadResult result2 = new SysAttachmentService.UploadResult();
        result2.setSuccess(true);
        result2.setAttachmentId(2L);

        List<SysAttachmentService.UploadResult> results = List.of(result1, result2);

        MockMultipartFile file1 = new MockMultipartFile("files", "file1.txt", 
                MediaType.TEXT_PLAIN_VALUE, "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "file2.txt", 
                MediaType.TEXT_PLAIN_VALUE, "content2".getBytes());

        when(attachmentService.uploadFiles(any(), any())).thenReturn(results);

        // When
        ResultActions result = mockMvc.perform(multipart("/api/attachments/upload/batch")
                .file(file1)
                .file(file2)
                .param("bizType", "expense")
                .param("bizId", "123")
                .header("X-User-Id", "user123")
                .header("X-User-Name", "测试用户"));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(attachmentService, times(1)).uploadFiles(any(), any());
    }

    @Test
    void shouldReturnDownloadFile_whenDownloadFileSuccess() throws Exception {
        // Given
        SysAttachmentService.DownloadResult downloadResult = new SysAttachmentService.DownloadResult();
        downloadResult.setSuccess(true);
        downloadResult.setFileContent("file content".getBytes());
        downloadResult.setFileName("test.txt");
        downloadResult.setMimeType("text/plain");

        Long attachmentId = 1L;
        when(attachmentService.downloadFile(attachmentId, "user123")).thenReturn(downloadResult);

        // When
        ResultActions result = mockMvc.perform(get("/api/attachments/download/{attachmentId}", attachmentId)
                .header("X-User-Id", "user123"));

        // Then
        result.andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/plain"))
                .andExpect(header().string("Content-Disposition", containsString("test.txt")));

        verify(attachmentService, times(1)).downloadFile(attachmentId, "user123");
    }

    @Test
    void shouldReturnNotFound_whenDownloadFileNotFound() throws Exception {
        // Given
        SysAttachmentService.DownloadResult downloadResult = new SysAttachmentService.DownloadResult();
        downloadResult.setSuccess(false);

        Long attachmentId = 999L;
        when(attachmentService.downloadFile(attachmentId, "user123")).thenReturn(downloadResult);

        // When
        ResultActions result = mockMvc.perform(get("/api/attachments/download/{attachmentId}", attachmentId)
                .header("X-User-Id", "user123"));

        // Then
        result.andExpect(status().isNotFound());

        verify(attachmentService, times(1)).downloadFile(attachmentId, "user123");
    }

    @Test
    void shouldReturnPreviewResult_whenPreviewFileSuccess() throws Exception {
        // Given
        SysAttachmentService.PreviewResult previewResult = new SysAttachmentService.PreviewResult();
        previewResult.setSuccess(true);
        previewResult.setPreviewUrl("/preview/1");

        Long attachmentId = 1L;
        when(attachmentService.previewFile(attachmentId, "user123")).thenReturn(previewResult);

        // When
        ResultActions result = mockMvc.perform(get("/api/attachments/preview/{attachmentId}", attachmentId)
                .header("X-User-Id", "user123"));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.previewUrl").value("/preview/1"));

        verify(attachmentService, times(1)).previewFile(attachmentId, "user123");
    }

    @Test
    void shouldReturnAttachmentList_whenGetAttachmentsByBizSuccess() throws Exception {
        // Given
        String bizType = "expense";
        Long bizId = 123L;
        when(attachmentService.getAttachmentsByBiz(bizType, bizId)).thenReturn(testAttachmentList);

        // When
        ResultActions result = mockMvc.perform(get("/api/attachments/biz/{bizType}/{bizId}", bizType, bizId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(attachmentService, times(1)).getAttachmentsByBiz(bizType, bizId);
    }

    @Test
    void shouldReturnAttachmentPage_whenGetAttachmentsWithPagination() throws Exception {
        // Given
        when(attachmentService.getAttachments(any(), any())).thenReturn(testAttachmentPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/attachments/list")
                .param("bizType", "expense")
                .param("bizId", "123")
                .param("fileName", "test")
                .param("fileType", "txt")
                .param("isDeleted", "false")
                .param("page", "0")
                .param("size", "20")
                .param("sort", "createTime")
                .param("direction", "desc"));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));

        verify(attachmentService, times(1)).getAttachments(any(), any());
    }

    @Test
    void shouldReturnOk_whenDeleteAttachmentSuccess() throws Exception {
        // Given
        Long attachmentId = 1L;
        doNothing().when(attachmentService).deleteAttachment(attachmentId, "user123");

        // When
        ResultActions result = mockMvc.perform(delete("/api/attachments/{attachmentId}", attachmentId)
                .header("X-User-Id", "user123"));

        // Then
        result.andExpect(status().isOk());

        verify(attachmentService, times(1)).deleteAttachment(attachmentId, "user123");
    }

    @Test
    void shouldReturnOk_whenBatchDeleteAttachmentsSuccess() throws Exception {
        // Given
        List<Long> attachmentIds = List.of(1L, 2L, 3L);
        doNothing().when(attachmentService).batchDeleteAttachments(attachmentIds, "user123");

        // When
        ResultActions result = mockMvc.perform(delete("/api/attachments/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attachmentIds))
                .header("X-User-Id", "user123"));

        // Then
        result.andExpect(status().isOk());

        verify(attachmentService, times(1)).batchDeleteAttachments(attachmentIds, "user123");
    }

    @Test
    void shouldReturnOk_whenRestoreAttachmentSuccess() throws Exception {
        // Given
        Long attachmentId = 1L;
        doNothing().when(attachmentService).restoreAttachment(attachmentId, "user123");

        // When
        ResultActions result = mockMvc.perform(put("/api/attachments/{attachmentId}/restore", attachmentId)
                .header("X-User-Id", "user123"));

        // Then
        result.andExpect(status().isOk());

        verify(attachmentService, times(1)).restoreAttachment(attachmentId, "user123");
    }

    @Test
    void shouldReturnStatistics_whenGetAttachmentStatistics() throws Exception {
        // Given
        SysAttachmentService.AttachmentStatistics statistics = new SysAttachmentService.AttachmentStatistics();
        statistics.setTotalCount(100L);
        statistics.setTotalSize(1024000L);

        when(attachmentService.getStatistics()).thenReturn(statistics);

        // When
        ResultActions result = mockMvc.perform(get("/api/attachments/statistics"));

        // Then
        result.andExpect(status().isOk());

        verify(attachmentService, times(1)).getStatistics();
    }

    @Test
    void shouldReturnDuplicateCheckResult_whenCheckFileDuplicate() throws Exception {
        // Given
        SysAttachmentService.DuplicateCheckResult duplicateResult = new SysAttachmentService.DuplicateCheckResult();
        duplicateResult.setIsDuplicate(false);

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", 
                MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        when(attachmentService.checkDuplicate(file)).thenReturn(duplicateResult);

        // When
        ResultActions result = mockMvc.perform(multipart("/api/attachments/check-duplicate")
                .file(file));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isDuplicate").value(false));

        verify(attachmentService, times(1)).checkDuplicate(file);
    }

    @Test
    void shouldReturnCleanupResult_whenCleanupExpiredAttachments() throws Exception {
        // Given
        SysAttachmentService.CleanupResult cleanupResult = new SysAttachmentService.CleanupResult();
        cleanupResult.setDeletedCount(5);
        cleanupResult.setSuccess(true);

        when(attachmentService.cleanupExpiredAttachments()).thenReturn(cleanupResult);

        // When
        ResultActions result = mockMvc.perform(post("/api/attachments/cleanup")
                .header("X-User-Id", "user123"));

        // Then
        result.andExpect(status().isOk());

        verify(attachmentService, times(1)).cleanupExpiredAttachments();
    }

    @Test
    void shouldReturnEmptyList_whenGetDuplicateSuggestions() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/api/attachments/duplicate-suggestions")
                .param("fileMd5", "abc123def456"));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldHandleLargeFileUpload_whenUploadFileSuccess() throws Exception {
        // Given
        SysAttachmentService.UploadResult uploadResult = new SysAttachmentService.UploadResult();
        uploadResult.setSuccess(true);
        uploadResult.setAttachmentId(1L);

        // 模拟大文件内容
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        MockMultipartFile file = new MockMultipartFile("file", "large.txt", 
                MediaType.TEXT_PLAIN_VALUE, largeContent);

        when(attachmentService.uploadFile(any(), any())).thenReturn(uploadResult);

        // When
        ResultActions result = mockMvc.perform(multipart("/api/attachments/upload")
                .file(file)
                .param("bizType", "expense")
                .param("bizId", "123")
                .header("X-User-Id", "user123")
                .header("X-User-Name", "测试用户"));

        // Then
        result.andExpect(status().isOk());

        verify(attachmentService, times(1)).uploadFile(any(), any());
    }

    @Test
    void shouldHandleSpecialCharacters_whenUploadFileWithSpecialChars() throws Exception {
        // Given
        SysAttachmentService.UploadResult uploadResult = new SysAttachmentService.UploadResult();
        uploadResult.setSuccess(true);
        uploadResult.setAttachmentId(1L);

        MockMultipartFile file = new MockMultipartFile("file", "特殊文件@测试.pdf", 
                "application/pdf", "test content".getBytes());

        when(attachmentService.uploadFile(any(), any())).thenReturn(uploadResult);

        // When
        ResultActions result = mockMvc.perform(multipart("/api/attachments/upload")
                .file(file)
                .param("bizType", "特殊业务-类型")
                .param("bizId", "123")
                .param("description", "包含特殊字符的描述@测试")
                .header("X-User-Id", "user123")
                .header("X-User-Name", "测试用户"));

        // Then
        result.andExpect(status().isOk());

        verify(attachmentService, times(1)).uploadFile(any(), any());
    }

    @Test
    void shouldHandleEmptyAttachmentList_whenGetAttachmentsByBizEmpty() throws Exception {
        // Given
        String bizType = "expense";
        Long bizId = 999L;
        when(attachmentService.getAttachmentsByBiz(bizType, bizId)).thenReturn(List.of());

        // When
        ResultActions result = mockMvc.perform(get("/api/attachments/biz/{bizType}/{bizId}", bizType, bizId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(attachmentService, times(1)).getAttachmentsByBiz(bizType, bizId);
    }

    @Test
    void shouldHandleNullParameters_whenGetAttachmentsWithoutOptionalParams() throws Exception {
        // Given
        when(attachmentService.getAttachments(any(), any())).thenReturn(testAttachmentPage);

        // When - 只传递必需参数
        ResultActions result = mockMvc.perform(get("/api/attachments/list")
                .param("page", "0")
                .param("size", "20"));

        // Then
        result.andExpect(status().isOk());

        verify(attachmentService, times(1)).getAttachments(any(), any());
    }

    @Test
    void shouldHandleLargePageSize_whenGetAttachmentsWithLargeSize() throws Exception {
        // Given
        List<SysAttachment> largeList = TestDataFactory.createSysAttachments(100);
        Page<SysAttachment> largePage = new PageImpl<>(largeList, PageRequest.of(0, 100), 100);

        when(attachmentService.getAttachments(any(), any())).thenReturn(largePage);

        // When
        ResultActions result = mockMvc.perform(get("/api/attachments/list")
                .param("page", "0")
                .param("size", "100"));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(100));

        verify(attachmentService, times(1)).getAttachments(any(), any());
    }

    @Test
    void shouldHandleEmptyBatchDelete_whenBatchDeleteWithEmptyList() throws Exception {
        // Given
        List<Long> emptyList = List.of();
        doNothing().when(attachmentService).batchDeleteAttachments(emptyList, "user123");

        // When
        ResultActions result = mockMvc.perform(delete("/api/attachments/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyList))
                .header("X-User-Id", "user123"));

        // Then
        result.andExpect(status().isOk());

        verify(attachmentService, times(1)).batchDeleteAttachments(emptyList, "user123");
    }

    @Test
    void shouldHandleInvalidFile_whenUploadFileWithUnsupportedType() throws Exception {
        // Given
        SysAttachmentService.UploadResult uploadResult = new SysAttachmentService.UploadResult();
        uploadResult.setSuccess(false);
        uploadResult.setErrorMessage("不支持的文件类型");

        MockMultipartFile file = new MockMultipartFile("file", "test.exe", 
                "application/x-msdownload", "test content".getBytes());

        when(attachmentService.uploadFile(any(), any())).thenReturn(uploadResult);

        // When
        ResultActions result = mockMvc.perform(multipart("/api/attachments/upload")
                .file(file)
                .param("bizType", "expense")
                .param("bizId", "123")
                .header("X-User-Id", "user123")
                .header("X-User-Name", "测试用户"));

        // Then
        result.andExpect(status().isBadRequest());

        verify(attachmentService, times(1)).uploadFile(any(), any());
    }

    @Test
    void shouldHandleMissingHeaders_whenUploadFileWithoutAuthHeaders() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", 
                MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

        // When
        ResultActions result = mockMvc.perform(multipart("/api/attachments/upload")
                .file(file)
                .param("bizType", "expense")
                .param("bizId", "123"));

        // Then - 由于缺少认证头信息，预期会返回适当的错误状态
        result.andExpect(status().is4xxClientError());
    }
}