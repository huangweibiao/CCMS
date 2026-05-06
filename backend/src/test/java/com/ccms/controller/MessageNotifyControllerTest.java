package com.ccms.controller;

import com.ccms.entity.SysMessage;
import com.ccms.repository.SysMessageRepository;
import com.ccms.service.MessageNotifyService;
import com.ccms.vo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 消息通知控制器单元测试
 */
@WebMvcTest(MessageNotifyController.class)
class MessageNotifyControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageNotifyService messageNotifyService;

    @MockBean
    private SysMessageRepository sysMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private SysMessage testMessage;
    private Page<SysMessage> testPage;

    @BeforeEach
    void setUp() {
        testMessage = createTestMessage();
        testPage = new PageImpl<>(
                Collections.singletonList(testMessage),
                PageRequest.of(0, 20),
                1
        );
    }

    @Test
    void shouldReturnUserMessages_whenGetListSuccess() throws Exception {
        // Given
        when(sysMessageRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(testPage);

        // When & Then
        mockMvc.perform(get("/api/messages/list")
                        .param("userId", "1001")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void shouldReturnMessagesWithFilters_whenQueryWithParameters() throws Exception {
        // Given
        when(sysMessageRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(testPage);

        // When & Then
        mockMvc.perform(get("/api/messages/list")
                        .param("userId", "1001")
                        .param("page", "0")
                        .param("size", "20")
                        .param("readStatus", "UNREAD")
                        .param("msgType", "SYSTEM"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldReturnUnreadStats_whenGetUnreadStatistics() throws Exception {
        // Given
        MessageNotifyService.UnreadStats stats = new MessageNotifyService.UnreadStats();
        stats.setTotalUnread(10);
        stats.setSystemUnread(3);
        stats.setTodoUnread(5);
        stats.setNoticeUnread(2);
        
        when(messageNotifyService.getUnreadStatistics(eq(1001L))).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/messages/unread-stats")
                        .param("userId", "1001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalUnread").value(10))
                .andExpect(jsonPath("$.data.systemUnread").value(3));
    }

    @Test
    void shouldMarkMessagesAsReadSuccessfully() throws Exception {
        // Given
        MessageNotifyService.MarkResult markResult = new MessageNotifyService.MarkResult();
        markResult.setSuccess(true);
        markResult.setMessage("标记成功");
        markResult.setMarkedCount(3);
        
        when(messageNotifyService.markMessagesAsRead(anyList(), eq(1001L))).thenReturn(markResult);

        Map<String, Object> request = new HashMap<>();
        request.put("messageIds", Arrays.asList(1L, 2L, 3L));
        request.put("userId", 1001L);

        // When & Then
        mockMvc.perform(post("/api/messages/mark-read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.markedCount").value(3));
    }

    @Test
    void shouldSendInnerMessageSuccessfully() throws Exception {
        // Given
        MessageNotifyService.NotifyResult notifyResult = new MessageNotifyService.NotifyResult();
        notifyResult.setSuccess(true);
        notifyResult.setMessage("发送成功");
        notifyResult.setSentCount(1);
        
        when(messageNotifyService.sendInnerMessage(any(MessageNotifyService.InnerMessage.class)))
                .thenReturn(notifyResult);

        Map<String, Object> request = new HashMap<>();
        request.put("title", "测试站内消息");
        request.put("content", "测试内容");
        request.put("receivers", Collections.singletonList(createTestReceiver()));
        request.put("msgType", "NOTICE");

        // When & Then
        mockMvc.perform(post("/api/messages/send-inner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldSendTodoReminderSuccessfully() throws Exception {
        // Given
        MessageNotifyService.NotifyResult notifyResult = new MessageNotifyService.NotifyResult();
        notifyResult.setSuccess(true);
        notifyResult.setMessage("待办发送成功");
        
        when(messageNotifyService.sendTodoReminder(any(MessageNotifyService.TodoMessage.class)))
                .thenReturn(notifyResult);

        Map<String, Object> request = new HashMap<>();
        request.put("title", "待办提醒");
        request.put("content", "您有一个待办事项需要处理");
        request.put("receivers", Collections.singletonList(createTestReceiver()));
        request.put("todoType", "APPROVAL");
        request.put("priority", "HIGH");

        // When & Then
        mockMvc.perform(post("/api/messages/send-todo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldSendTemplateMessageSuccessfully() throws Exception {
        // Given
        MessageNotifyService.BatchNotifyResult batchResult = new MessageNotifyService.BatchNotifyResult();
        batchResult.setSuccessCount(5);
        batchResult.setFailedCount(0);
        
        when(messageNotifyService.sendTemplateMessage(anyString(), anyMap(), anyList()))
                .thenReturn(batchResult);

        Map<String, Object> request = new HashMap<>();
        request.put("templateCode", "TEMP001");
        request.put("params", Collections.singletonMap("userName", "张三"));
        request.put("receivers", Collections.singletonList(createTestReceiver()));

        // When & Then
        mockMvc.perform(post("/api/messages/send-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldGetMessageDetailSuccessfully() throws Exception {
        // Given
        when(sysMessageRepository.findById(eq(1L))).thenReturn(Optional.of(testMessage));
        when(sysMessageRepository.save(any(SysMessage.class))).thenReturn(testMessage);

        // When & Then
        mockMvc.perform(get("/api/messages/detail/{messageId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldReturnError_whenMessageNotFound() throws Exception {
        // Given
        when(sysMessageRepository.findById(eq(999L))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/messages/detail/{messageId}", 999L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("获取消息详情失败: 消息不存在: 999"));
    }

    @Test
    void shouldDeleteMessageSuccessfully() throws Exception {
        // Given
        when(sysMessageRepository.findById(eq(1L))).thenReturn(Optional.of(testMessage));
        when(sysMessageRepository.save(any(SysMessage.class))).thenReturn(testMessage);

        // When & Then
        mockMvc.perform(delete("/api/messages/{messageId}", 1L)
                        .param("userId", "1001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除消息成功"));
    }

    @Test
    void shouldReturnForbidden_whenDeleteOthersMessage() throws Exception {
        // Given
        SysMessage othersMessage = createTestMessage();
        othersMessage.setReceiverId(1002L);
        when(sysMessageRepository.findById(eq(1L))).thenReturn(Optional.of(othersMessage));

        // When & Then
        mockMvc.perform(delete("/api/messages/{messageId}", 1L)
                        .param("userId", "1001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权删除此消息"));
    }

    @Test
    void shouldBatchDeleteMessagesSuccessfully() throws Exception {
        // Given
        when(sysMessageRepository.findById(eq(1L))).thenReturn(Optional.of(testMessage));
        when(sysMessageRepository.findById(eq(2L))).thenReturn(Optional.of(createTestMessage(2L)));
        when(sysMessageRepository.save(any(SysMessage.class))).thenReturn(testMessage);

        Map<String, Object> request = new HashMap<>();
        request.put("messageIds", Arrays.asList(1L, 2L));
        request.put("userId", 1001L);

        // When & Then
        mockMvc.perform(post("/api/messages/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功删除2条消息"));
    }

    @Test
    void shouldConfirmMessageSuccessfully() throws Exception {
        // Given
        SysMessage confirmMessage = createTestMessage();
        confirmMessage.setRequireConfirm(true);
        when(sysMessageRepository.findById(eq(1L))).thenReturn(Optional.of(confirmMessage));
        when(sysMessageRepository.save(any(SysMessage.class))).thenReturn(confirmMessage);

        // When & Then
        mockMvc.perform(post("/api/messages/confirm/{messageId}", 1L)
                        .param("userId", "1001")
                        .param("method", "MANUAL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("消息确认成功"));
    }

    @Test
    void shouldGetMessageStatisticsSuccessfully() throws Exception {
        // Given
        List<SysMessage> messages = Arrays.asList(testMessage, createTestMessage(2L));
        when(sysMessageRepository.findBySendTimeBetween(any(), any())).thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/api/messages/statistics")
                        .param("startTime", "2025-01-01T00:00:00")
                        .param("endTime", "2025-12-31T23:59:59"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalSent").value(2));
    }

    private SysMessage createTestMessage() {
        return createTestMessage(1L);
    }

    private SysMessage createTestMessage(Long id) {
        SysMessage message = new SysMessage();
        message.setId(id);
        message.setReceiverId(1001L);
        message.setTitle("测试消息");
        message.setContent("测试内容");
        message.setMsgType("SYSTEM");
        message.setReadStatus("UNREAD");
        message.setDeleted(0);
        message.setSendStatus("SENT");
        message.setSendMethod("INNER");
        message.setCreatedTime(LocalDateTime.now());
        return message;
    }

    private MessageNotifyService.Receiver createTestReceiver() {
        MessageNotifyService.Receiver receiver = new MessageNotifyService.Receiver();
        receiver.setUserId(1001L);
        receiver.setUserName("张三");
        return receiver;
    }
}