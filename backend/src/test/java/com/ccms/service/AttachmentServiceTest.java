package com.ccms.service;

import com.ccms.entity.SysAttachment;
import com.ccms.repository.SysAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 附件服务单元测试
 */
class AttachmentServiceTest extends BaseServiceTest {

    @Mock
    private SysAttachmentRepository attachmentRepository;

    @InjectMocks
    private AttachmentService attachmentService;

    private SysAttachment testAttachment;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testAttachment = createTestAttachment();
        testFile = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
    }

    @Test
    void shouldUploadFileSuccessfully() throws IOException {
        // Given
        when(attachmentRepository.save(any(SysAttachment.class))).thenReturn(testAttachment);

        // When
        AttachmentService.UploadResult result = attachmentService.uploadFile(testFile, 1, 100L, 1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void shouldUploadFilesSuccessfully() throws IOException {
        // Given
        List<MultipartFile> files = Arrays.asList(testFile, testFile);
        when(attachmentRepository.save(any(SysAttachment.class))).thenReturn(testAttachment);

        // When
        AttachmentService.BatchUploadResult result = attachmentService.uploadFiles(files, 1, 100L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalCount());
    }

    @Test
    void shouldGetAttachmentsByBizSuccessfully() {
        // Given
        List<SysAttachment> attachments = Arrays.asList(testAttachment);
        when(attachmentRepository.findByBizTypeAndBizId(eq(1), eq(100L))).thenReturn(attachments);

        // When
        List<SysAttachment> result = attachmentService.getAttachmentsByBiz(1, 100L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetAttachmentByIdSuccessfully() {
        // Given
        when(attachmentRepository.findById(eq(1L))).thenReturn(Optional.of(testAttachment));

        // When
        SysAttachment result = attachmentService.getAttachmentById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testAttachment.getId(), result.getId());
    }

    @Test
    void shouldDeleteAttachmentSuccessfully() {
        // Given
        when(attachmentRepository.findById(eq(1L))).thenReturn(Optional.of(testAttachment));
        when(attachmentRepository.save(any(SysAttachment.class))).thenReturn(testAttachment);

        // When
        boolean result = attachmentService.deleteAttachment(1L);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldDownloadFileSuccessfully() {
        // Given
        when(attachmentRepository.findById(eq(1L))).thenReturn(Optional.of(testAttachment));

        // When
        AttachmentService.DownloadResult result = attachmentService.downloadFile(1L, null);

        // Then
        assertNotNull(result);
    }

    @Test
    void shouldGetPreviewUrlSuccessfully() {
        // Given
        when(attachmentRepository.findById(eq(1L))).thenReturn(Optional.of(testAttachment));

        // When
        AttachmentService.PreviewResult result = attachmentService.getPreviewUrl(1L);

        // Then
        assertNotNull(result);
    }

    @Test
    void shouldValidateFileSuccessfully() {
        // When
        AttachmentService.FileValidationResult result = attachmentService.validateFile(testFile);

        // Then
        assertNotNull(result);
    }

    @Test
    void shouldGetStorageStatisticsSuccessfully() {
        // Given
        List<SysAttachment> attachments = Arrays.asList(testAttachment);
        when(attachmentRepository.findAll()).thenReturn(attachments);

        // When
        AttachmentService.StorageStatistics result = attachmentService.getStorageStatistics();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalFiles());
    }

    private SysAttachment createTestAttachment() {
        SysAttachment attachment = new SysAttachment();
        attachment.setId(1L);
        attachment.setFileName("test.txt");
        attachment.setFileSize(1024L);
        attachment.setFileType("text/plain");
        attachment.setBizType(1);
        attachment.setBizId(100L);
        attachment.setStatus(1);
        return attachment;
    }
}
