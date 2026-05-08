package com.ccms.controller.system;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.system.SysAttachment;
import com.ccms.repository.system.SysAttachmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 附件管理控制器单元测试
 */
@WebMvcTest(SysAttachmentController.class)
class SysAttachmentControllerTest extends ControllerTestBase {

    @MockBean
    private SysAttachmentRepository attachmentRepository;

    private SysAttachment createTestAttachment(Long id, String fileName) {
        SysAttachment attachment = new SysAttachment();
        attachment.setId(id);
        attachment.setFileName(fileName);
        attachment.setFilePath("/uploads/test" + id + ".pdf");
        attachment.setFileUrl("/uploads/test" + id + ".pdf");
        attachment.setFileSize(1024L);
        attachment.setFileType(".pdf");
        attachment.setMimeType("application/pdf");
        attachment.setBusinessType(1);
        attachment.setBusinessId(1L);
        attachment.setStorageType(0);
        attachment.setUploadUserId(1L);
        attachment.setIsPublic(false);
        attachment.setDownloadCount(0);
        attachment.setIsDeleted(false);
        return attachment;
    }

    @Test
    void shouldReturnAttachmentList() throws Exception {
        SysAttachment attachment = createTestAttachment(1L, "test.pdf");
        when(attachmentRepository.findByIsDeleted(false))
                .thenReturn(Collections.singletonList(attachment));

        performGet("/api/system/attachments")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fileName").value("test.pdf"));
    }

    @Test
    void shouldReturnAttachmentsByBusinessTypeAndId() throws Exception {
        SysAttachment attachment = createTestAttachment(1L, "test.pdf");
        when(attachmentRepository.findByBusinessTypeAndBusinessId(1, 1L))
                .thenReturn(Collections.singletonList(attachment));

        performGet("/api/system/attachments?businessType=1&businessId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnAttachmentByIdWhenExists() throws Exception {
        SysAttachment attachment = createTestAttachment(1L, "test.pdf");
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));

        performGet("/api/system/attachments/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("test.pdf"));
    }

    @Test
    void shouldReturnNotFoundWhenAttachmentNotExists() throws Exception {
        when(attachmentRepository.findById(999L)).thenReturn(Optional.empty());

        performGet("/api/system/attachments/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUploadAttachmentSuccess() throws Exception {
        SysAttachment attachment = createTestAttachment(1L, "test.pdf");
        when(attachmentRepository.save(any(SysAttachment.class))).thenReturn(attachment);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/system/attachments/upload")
                        .file(file)
                        .param("businessType", "1")
                        .param("businessId", "1")
                        .param("description", "测试附件"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.pdf"));
    }

    @Test
    void shouldDeleteAttachmentSuccess() throws Exception {
        SysAttachment attachment = createTestAttachment(1L, "test.pdf");
        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));
        when(attachmentRepository.save(any(SysAttachment.class))).thenReturn(attachment);

        performDelete("/api/system/attachments/1")
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenDeleteNonExistentAttachment() throws Exception {
        when(attachmentRepository.findById(999L)).thenReturn(Optional.empty());

        performDelete("/api/system/attachments/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateAttachmentSuccess() throws Exception {
        SysAttachment existing = createTestAttachment(1L, "test.pdf");
        SysAttachment updated = createTestAttachment(1L, "updated.pdf");
        updated.setDescription("更新描述");

        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(attachmentRepository.save(any(SysAttachment.class))).thenReturn(updated);

        performPut("/api/system/attachments/1", updated)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("更新描述"));
    }

    @Test
    void shouldReturnAttachmentsByBusiness() throws Exception {
        SysAttachment attachment = createTestAttachment(1L, "test.pdf");
        when(attachmentRepository.findByBusinessTypeAndBusinessId(1, 1L))
                .thenReturn(Collections.singletonList(attachment));

        performGet("/api/system/attachments/business/1/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnAttachmentsByMd5() throws Exception {
        SysAttachment attachment = createTestAttachment(1L, "test.pdf");
        when(attachmentRepository.findByFileMd5("abc123"))
                .thenReturn(Collections.singletonList(attachment));

        performGet("/api/system/attachments/md5/abc123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
