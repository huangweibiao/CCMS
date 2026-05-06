package com.ccms.service;

import com.ccms.entity.message.Message;
import com.ccms.entity.message.MessageTemplate;
import com.ccms.repository.message.MessageRepository;
import com.ccms.repository.message.MessageTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消息服务单元测试
 */
class MessageServiceTest extends BaseServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageTemplateRepository templateRepository;

    @InjectMocks
    private MessageService messageService;

    private Message testMessage;
    private MessageTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testMessage = createTestMessage();
        testTemplate = createTestTemplate();
    }

    @Test
    void shouldSendMessageSuccessfully() {
        // Given
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // When
        Message result = messageService.sendMessage(1, 1L, 2L, "Test Title", "Test Content", 1, 100L);

        // Then
        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
    }

    @Test
    void shouldSendSystemMessageSuccessfully() {
        // Given
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // When
        Message result = messageService.sendSystemMessage(2L, "System Notice", "System Content", 1, 100L);

        // Then
        assertNotNull(result);
    }

    @Test
    void shouldSendMessagesSuccessfully() {
        // Given
        List<Long> receiverIds = Arrays.asList(2L, 3L, 4L);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // When
        List<Message> results = messageService.sendMessages(1, 1L, receiverIds, "Title", "Content", 1, 100L);

        // Then
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    @Test
    void shouldGetUserMessagesSuccessfully() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        when(messageRepository.findByReceiverIdAndMessageTypeAndIsReadAndCreateTimeBetween(
                anyLong(), any(), any(), any(), any()))
                .thenReturn(messages);

        // When
        List<Message> results = messageService.getUserMessages(2L, 1, false, null, null);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void shouldGetUnreadMessageCountSuccessfully() {
        // Given
        when(messageRepository.countByReceiverIdAndIsRead(eq(2L), eq(false))).thenReturn(5L);

        // When
        long count = messageService.getUnreadMessageCount(2L);

        // Then
        assertEquals(5, count);
    }

    @Test
    void shouldMarkMessageAsReadSuccessfully() {
        // Given
        when(messageRepository.findById(eq(1L))).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> messageService.markMessageAsRead(1L, 2L));
    }

    @Test
    void shouldMarkMessagesAsReadSuccessfully() {
        // Given
        List<Long> messageIds = Arrays.asList(1L, 2L);
        when(messageRepository.findAllById(anyIterable())).thenReturn(Arrays.asList(testMessage));
        when(messageRepository.saveAll(anyList())).thenReturn(Arrays.asList(testMessage));

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> messageService.markMessagesAsRead(messageIds, 2L));
    }

    @Test
    void shouldMarkAllMessagesAsReadSuccessfully() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        when(messageRepository.findByReceiverIdAndIsRead(eq(2L), eq(false))).thenReturn(messages);
        when(messageRepository.saveAll(anyList())).thenReturn(messages);

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> messageService.markAllMessagesAsRead(2L));
    }

    @Test
    void shouldDeleteMessageSuccessfully() {
        // Given
        when(messageRepository.findById(eq(1L))).thenReturn(Optional.of(testMessage));
        doNothing().when(messageRepository).delete(any(Message.class));

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> messageService.deleteMessage(1L, 2L));
    }

    @Test
    void shouldSaveMessageTemplateSuccessfully() {
        // Given
        when(templateRepository.save(any(MessageTemplate.class))).thenReturn(testTemplate);

        // When
        MessageTemplate result = messageService.saveMessageTemplate(testTemplate);

        // Then
        assertNotNull(result);
        assertEquals("WELCOME", result.getTemplateCode());
    }

    @Test
    void shouldGetMessageTemplateSuccessfully() {
        // Given
        when(templateRepository.findByTemplateCode(eq("WELCOME"))).thenReturn(Optional.of(testTemplate));

        // When
        MessageTemplate result = messageService.getMessageTemplate("WELCOME");

        // Then
        assertNotNull(result);
    }

    @Test
    void shouldSendMessageByTemplateSuccessfully() {
        // Given
        when(templateRepository.findByTemplateCode(eq("WELCOME"))).thenReturn(Optional.of(testTemplate));
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        Map<String, Object> params = new HashMap<>();
        params.put("username", "张三");

        // When
        Message result = messageService.sendMessageByTemplate("WELCOME", 2L, params, 1, 100L);

        // Then
        assertNotNull(result);
    }

    @Test
    void shouldGetMessageStatisticsSuccessfully() {
        // Given
        when(messageRepository.countByReceiverIdAndCreateTimeBetween(anyLong(), any(), any())).thenReturn(100L);
        when(messageRepository.countByReceiverIdAndIsReadAndCreateTimeBetween(anyLong(), eq(true), any(), any())).thenReturn(80L);

        // When
        MessageService.MessageStatistics stats = messageService.getMessageStatistics(2L, null, null);

        // Then
        assertNotNull(stats);
        assertEquals(100, stats.getTotalMessages());
    }

    private Message createTestMessage() {
        Message message = new Message();
        message.setId(1L);
        message.setMessageType(1);
        message.setSenderId(1L);
        message.setReceiverId(2L);
        message.setTitle("Test Title");
        message.setContent("Test Content");
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }

    private MessageTemplate createTestTemplate() {
        MessageTemplate template = new MessageTemplate();
        template.setId(1L);
        template.setTemplateCode("WELCOME");
        template.setTemplateName("欢迎消息");
        template.setTitleTemplate("欢迎 {username}");
        template.setContentTemplate("欢迎加入我们的系统！");
        return template;
    }
}
