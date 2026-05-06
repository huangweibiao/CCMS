package com.ccms.repository;

import com.ccms.entity.SysMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统消息存储层单元测试
 */
class SysMessageRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private SysMessageRepository messageRepository;

    @Test
    void shouldSaveMessageSuccessfully() {
        // Given
        SysMessage message = createTestMessage();

        // When
        SysMessage saved = messageRepository.save(message);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Test Title", saved.getTitle());
    }

    @Test
    void shouldFindByIdSuccessfully() {
        // Given
        SysMessage saved = messageRepository.save(createTestMessage());

        // When
        Optional<SysMessage> found = messageRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getTitle(), found.get().getTitle());
    }

    @Test
    void shouldFindByReceiverIdSuccessfully() {
        // Given
        SysMessage message = createTestMessage();
        message.setReceiverId(2L);
        messageRepository.save(message);

        // When
        List<SysMessage> results = messageRepository.findByReceiverId(2L);

        // Then
        assertFalse(results.isEmpty());
        assertEquals(2L, results.get(0).getReceiverId());
    }

    @Test
    void shouldFindByReceiverIdAndReadStatusSuccessfully() {
        // Given
        SysMessage message = createTestMessage();
        message.setReceiverId(2L);
        message.setReadStatus("UNREAD");
        messageRepository.save(message);

        // When
        List<SysMessage> results = messageRepository.findByReceiverIdAndReadStatus(2L, "UNREAD");

        // Then
        assertFalse(results.isEmpty());
        assertEquals("UNREAD", results.get(0).getReadStatus());
    }

    @Test
    void shouldCountByReceiverIdAndReadStatusSuccessfully() {
        // Given
        SysMessage message = createTestMessage();
        message.setReceiverId(2L);
        message.setReadStatus("UNREAD");
        messageRepository.save(message);

        // When
        Long count = messageRepository.countByReceiverIdAndReadStatus(2L, "UNREAD");

        // Then
        assertTrue(count >= 1);
    }

    @Test
    void shouldDeleteMessageSuccessfully() {
        // Given
        SysMessage saved = messageRepository.save(createTestMessage());

        // When
        messageRepository.deleteById(saved.getId());
        Optional<SysMessage> found = messageRepository.findById(saved.getId());

        // Then
        assertFalse(found.isPresent());
    }

    private SysMessage createTestMessage() {
        SysMessage message = new SysMessage();
        message.setTitle("Test Title");
        message.setContent("Test Content");
        message.setMsgType("SYSTEM");
        message.setReceiverId(2L);
        message.setReadStatus("UNREAD");
        message.setSendStatus("SENT");
        message.setCreatedTime(LocalDateTime.now());
        return message;
    }
}
