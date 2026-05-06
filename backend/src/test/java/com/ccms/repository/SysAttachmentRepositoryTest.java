package com.ccms.repository;

import com.ccms.entity.SysAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 附件存储层单元测试
 */
class SysAttachmentRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SysAttachmentRepository attachmentRepository;

    @Test
    void shouldSaveAttachmentSuccessfully() {
        // Given
        SysAttachment attachment = createTestAttachment();

        // When
        SysAttachment saved = attachmentRepository.save(attachment);

        // Then
        assertNotNull(saved.getId());
        assertEquals("test.txt", saved.getFileName());
    }

    @Test
    void shouldFindByIdSuccessfully() {
        // Given
        SysAttachment saved = attachmentRepository.save(createTestAttachment());

        // When
        Optional<SysAttachment> found = attachmentRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getFileName(), found.get().getFileName());
    }

    @Test
    void shouldFindByBizTypeAndBizIdSuccessfully() {
        // Given
        SysAttachment attachment = createTestAttachment();
        attachment.setBizType(1);
        attachment.setBizId(100L);
        attachmentRepository.save(attachment);

        // When
        List<SysAttachment> results = attachmentRepository.findByBizTypeAndBizId(1, 100L);

        // Then
        assertFalse(results.isEmpty());
        assertEquals(1, results.get(0).getBizType());
    }

    @Test
    void shouldDeleteAttachmentSuccessfully() {
        // Given
        SysAttachment saved = attachmentRepository.save(createTestAttachment());

        // When
        attachmentRepository.deleteById(saved.getId());
        Optional<SysAttachment> found = attachmentRepository.findById(saved.getId());

        // Then
        assertFalse(found.isPresent());
    }

    private SysAttachment createTestAttachment() {
        SysAttachment attachment = new SysAttachment();
        attachment.setFileName("test.txt");
        attachment.setFileSize(1024L);
        attachment.setFileType("text/plain");
        attachment.setFilePath("/uploads/test.txt");
        attachment.setUploadUserId(1L);
        attachment.setUploadTime(LocalDateTime.now());
        attachment.setStatus(1);
        return attachment;
    }
}
