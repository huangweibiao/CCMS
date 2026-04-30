package com.ccms.controller;

import com.ccms.common.response.ApiResponse;
import com.ccms.entity.message.Message;
import com.ccms.service.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知控制器
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取用户消息列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Message>> getUserMessages(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer messageType,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        
        List<Message> messages = messageService.getUserMessages(userId, messageType, isRead, startDate, endDate);
        return ApiResponse.success(messages);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/user/{userId}/unread-count")
    public ApiResponse<Long> getUnreadMessageCount(@PathVariable Long userId) {
        long count = messageService.getUnreadMessageCount(userId);
        return ApiResponse.success(count);
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/{messageId}/read")
    public ApiResponse<Void> markMessageAsRead(
            @PathVariable Long messageId,
            @RequestParam @NotNull Long userId) {
        
        messageService.markMessageAsRead(messageId, userId);
        return ApiResponse.success();
    }

    /**
     * 批量标记消息为已读
     */
    @PutMapping("/batch-read")
    public ApiResponse<Void> markMessagesAsRead(
            @RequestBody @Valid List<Long> messageIds,
            @RequestParam @NotNull Long userId) {
        
        messageService.markMessagesAsRead(messageIds, userId);
        return ApiResponse.success();
    }

    /**
     * 标记所有消息为已读
     */
    @PutMapping("/user/{userId}/read-all")
    public ApiResponse<Void> markAllMessagesAsRead(@PathVariable Long userId) {
        messageService.markAllMessagesAsRead(userId);
        return ApiResponse.success();
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    public ApiResponse<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam @NotNull Long userId) {
        
        messageService.deleteMessage(messageId, userId);
        return ApiResponse.success();
    }

    /**
     * 批量删除消息
     */
    @DeleteMapping("/batch-delete")
    public ApiResponse<Void> deleteMessages(
            @RequestBody @Valid List<Long> messageIds,
            @RequestParam @NotNull Long userId) {
        
        messageService.deleteMessages(messageIds, userId);
        return ApiResponse.success();
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ApiResponse<Message> sendMessage(
            @RequestParam Integer messageType,
            @RequestParam(required = false) Long senderId,
            @RequestParam @NotNull Long receiverId,
            @RequestParam @NotNull String title,
            @RequestParam @NotNull String content,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) Long businessId) {
        
        Message message = messageService.sendMessage(messageType, senderId, receiverId, 
                                                     title, content, businessType, businessId);
        return ApiResponse.success(message);
    }

    /**
     * 批量发送消息
     */
    @PostMapping("/send-batch")
    public ApiResponse<List<Message>> sendMessages(
            @RequestParam Integer messageType,
            @RequestParam(required = false) Long senderId,
            @RequestBody @Valid List<Long> receiverIds,
            @RequestParam @NotNull String title,
            @RequestParam @NotNull String content,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) Long businessId) {
        
        List<Message> messages = messageService.sendMessages(messageType, senderId, receiverIds, 
                                                              title, content, businessType, businessId);
        return ApiResponse.success(messages);
    }

    /**
     * 发送系统消息
     */
    @PostMapping("/system/send")
    public ApiResponse<Message> sendSystemMessage(
            @RequestParam @NotNull Long receiverId,
            @RequestParam @NotNull String title,
            @RequestParam @NotNull String content,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) Long businessId) {
        
        Message message = messageService.sendSystemMessage(receiverId, title, content, 
                                                           businessType, businessId);
        return ApiResponse.success(message);
    }

    /**
     * 获取消息统计数据
     */
    @GetMapping("/user/{userId}/statistics")
    public ApiResponse<MessageService.MessageStatistics> getMessageStatistics(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        
        MessageService.MessageStatistics statistics = messageService.getMessageStatistics(userId, startDate, endDate);
        return ApiResponse.success(statistics);
    }
}