package com.ccms.controller;

import com.ccms.common.response.ApiResponse;
import com.ccms.entity.message.Message;
import com.ccms.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 消息控制器单元测试
 */
@WebMvcTest(MessageController.class)
class MessageControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    private Message testMessage;
    private List<Message> testMessages;

    @BeforeEach
    void setUp() {
        testMessage = createTestMessage();
        testMessages = Arrays.asList(testMessage, createTestMessage(2L));
    }

    @Test
    void shouldReturnUserMessages_whenGetMessagesSuccess() throws Exception {
        // Given
        when(messageService.getUserMessages(eq(1001L), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(testMessages);

        // When & Then
        mockMvc.perform(get("/api/messages/user/{userId}", 1001L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldReturnFilteredMessages_whenQueryWithParameters() throws Exception {
        // Given
        List<Message> filteredMessages = Collections.singletonList(testMessage);
        when(messageService.getUserMessages(eq(1001L), eq(1), eq(false), any(), any()))
                .thenReturn(filteredMessages);

        // When & Then
        mockMvc.perform(get("/api/messages/user/{userId}", 1001L)
                        .param("messageType", "1")
                        .param("isRead", "false")
                        .param("startDate", "2025-01-01 00:00:00")
                        .param("endDate", "2025-12-31 23:59:59"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void shouldReturnUnreadCount_whenGetUnreadCount() throws Exception {
        // Given
        when(messageService.getUnreadMessageCount(eq(1001L))).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/messages/user/{userId}/unread-count", 1001L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void shouldMarkMessageAsReadSuccessfully() throws Exception {
        // Given
        doNothing().when(messageService).markMessageAsRead(eq(1L), eq(1001L));

        // When & Then
        mockMvc.perform(put("/api/messages/{messageId}/read", 1L)
                        .param("userId", "1001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldMarkMessagesAsReadSuccessfully() throws Exception {
        // Given
        List<Long> messageIds = Arrays.asList(1L, 2L, 3L);
        doNothing().when(messageService).markMessagesAsRead(eq(messageIds), eq(1001L));

        // When & Then
        mockMvc.perform(put("/api/messages/batch-read")
                        .param("userId", "1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldMarkAllMessagesAsReadSuccessfully() throws Exception {
        // Given
        doNothing().when(messageService).markAllMessagesAsRead(eq(1001L));

        // When & Then
        mockMvc.perform(put("/api/messages/user/{userId}/read-all", 1001L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldDeleteMessageSuccessfully() throws Exception {
        // Given
        doNothing().when(messageService).deleteMessage(eq(1L), eq(1001L));

        // When & Then
        mockMvc.perform(delete("/api/messages/{messageId}", 1L)
                        .param("userId", "1001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldDeleteMessagesSuccessfully() throws Exception {
        // Given
        List<Long> messageIds = Arrays.asList(1L, 2L);
        doNothing().when(messageService).deleteMessages(eq(messageIds), eq(1001L));

        // When & Then
        mockMvc.perform(delete("/api/messages/batch-delete")
                        .param("userId", "1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldSendMessageSuccessfully() throws Exception {
        // Given
        when(messageService.sendMessage(eq(1), eq(1001L), eq(1002L), eq("测试标题"), eq("测试内容"), isNull(), isNull()))
                .thenReturn(testMessage);

        // When & Then
        mockMvc.perform(post("/api/messages/send")
                        .param("messageType", "1")
                        .param("senderId", "1001")
                        .param("receiverId", "1002")
                        .param("title", "测试标题")
                        .param("content", "测试内容"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldSendMessagesSuccessfully() throws Exception {
        // Given
        List<Long> receiverIds = Arrays.asList(1002L, 1003L);
        when(messageService.sendMessages(eq(1), eq(1001L), eq(receiverIds), eq("群发标题"), eq("群发内容"), isNull(), isNull()))
                .thenReturn(testMessages);

        // When & Then
        mockMvc.perform(post("/api/messages/send-batch")
                        .param("messageType", "1")
                        .param("senderId", "1001")
                        .param("title", "群发标题")
                        .param("content", "群发内容")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receiverIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldSendSystemMessageSuccessfully() throws Exception {
        // Given
        when(messageService.sendSystemMessage(eq(1002L), eq("系统通知"), eq("系统消息内容"), isNull(), isNull()))
                .thenReturn(testMessage);

        // When & Then
        mockMvc.perform(post("/api/messages/system/send")
                        .param("receiverId", "1002")
                        .param("title", "系统通知")
                        .param("content", "系统消息内容"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldGetMessageStatisticsSuccessfully() throws Exception {
        // Given
        MessageService.MessageStatistics stats = new MessageService.MessageStatistics();
        stats.setTotalCount(100);
        stats.setUnreadCount(20);
        stats.setReadCount(80);
        
        when(messageService.getMessageStatistics(eq(1001L), isNull(), isNull())).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/messages/user/{userId}/statistics", 1001L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(100))
                .andExpect(jsonPath("$.data.unreadCount").value(20));
    }

    private Message createTestMessage() {
        return createTestMessage(1L);
    }

    private Message createTestMessage(Long id) {
        Message message = new Message();
        message.setId(id);
        message.setMessageType(1);
        message.setSenderId(1001L);
        message.setReceiverId(1002L);
        message.setTitle("测试消息");
        message.setContent("测试消息内容");
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }
}