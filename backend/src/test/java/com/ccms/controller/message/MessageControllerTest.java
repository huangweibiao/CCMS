package com.ccms.controller.message;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.message.SysMessage;
import com.ccms.repository.message.SysMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 消息管理控制器单元测试
 */
@WebMvcTest(MessageController.class)
class MessageControllerTest extends ControllerTestBase {

    @MockBean
    private SysMessageRepository sysMessageRepository;

    private SysMessage createTestMessage(Long id, String title, Integer messageType, Integer isRead) {
        SysMessage message = new SysMessage();
        message.setId(id);
        message.setTitle(title);
        message.setContent("测试消息内容");
        message.setMessageType(messageType);
        message.setIsRead(isRead);
        message.setReceiverId(1L);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }

    @Test
    void shouldReturnMessageListWhenQuerySuccess() throws Exception {
        SysMessage message = createTestMessage(1L, "测试消息", 1, 0);
        Page<SysMessage> page = new PageImpl<>(
                Collections.singletonList(message),
                PageRequest.of(0, 10),
                1
        );
        when(sysMessageRepository.findAll(any(PageRequest.class))).thenReturn(page);

        performGet("/api/messages")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("测试消息"));
    }

    @Test
    void shouldReturnMessageWhenGetByIdSuccess() throws Exception {
        SysMessage message = createTestMessage(1L, "测试消息", 1, 0);
        when(sysMessageRepository.findById(1L)).thenReturn(Optional.of(message));

        performGet("/api/messages/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("测试消息"));
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdNotExist() throws Exception {
        when(sysMessageRepository.findById(999L)).thenReturn(Optional.empty());

        performGet("/api/messages/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUserMessagesSuccess() throws Exception {
        SysMessage message = createTestMessage(1L, "测试消息", 1, 0);
        when(sysMessageRepository.findByReceiverId(1L)).thenReturn(Collections.singletonList(message));

        performGet("/api/messages/user/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldReturnUserUnreadMessagesSuccess() throws Exception {
        SysMessage message = createTestMessage(1L, "未读消息", 1, 0);
        when(sysMessageRepository.findByReceiverIdAndIsRead(1L, 0)).thenReturn(Collections.singletonList(message));

        performGet("/api/messages/user/1/unread")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldCreateMessageSuccess() throws Exception {
        SysMessage message = createTestMessage(1L, "测试消息", 1, 0);
        when(sysMessageRepository.save(any(SysMessage.class))).thenReturn(message);

        performPost("/api/messages", message)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldMarkMessageAsReadSuccess() throws Exception {
        SysMessage message = createTestMessage(1L, "测试消息", 1, 0);
        when(sysMessageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(sysMessageRepository.save(any(SysMessage.class))).thenReturn(message);

        performPut("/api/messages/1/read")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("消息已标记为已读"));
    }

    @Test
    void shouldMarkAllMessagesAsReadSuccess() throws Exception {
        SysMessage message = createTestMessage(1L, "测试消息", 1, 0);
        when(sysMessageRepository.findByReceiverIdAndIsRead(1L, 0)).thenReturn(Collections.singletonList(message));

        performPut("/api/messages/user/1/read-all")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void shouldDeleteMessageSuccess() throws Exception {
        when(sysMessageRepository.existsById(1L)).thenReturn(true);

        performDelete("/api/messages/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sysMessageRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldReturnMessageStatistics() throws Exception {
        when(sysMessageRepository.findByReceiverId(1L)).thenReturn(Collections.emptyList());
        when(sysMessageRepository.countByReceiverIdAndIsRead(1L, 0)).thenReturn(5L);
        when(sysMessageRepository.countByReceiverIdAndIsRead(1L, 1)).thenReturn(10L);

        performGet("/api/messages/statistics/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unread").value(5))
                .andExpect(jsonPath("$.read").value(10));
    }
}

