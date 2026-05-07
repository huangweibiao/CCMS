package com.ccms.controller.message;

import com.ccms.entity.message.SysMessage;
import com.ccms.repository.message.SysMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息管理控制器
 * 对应设计文档：4.8节 消息通知相关表
 *
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final SysMessageRepository sysMessageRepository;

    @Autowired
    public MessageController(SysMessageRepository sysMessageRepository) {
        this.sysMessageRepository = sysMessageRepository;
    }

    /**
     * 获取消息列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<SysMessage>> getMessageList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer messageType,
            @RequestParam(required = false) Integer isRead) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<SysMessage> messagePage;

        if (messageType != null && isRead != null) {
            List<SysMessage> all = sysMessageRepository.findByReceiverIdAndReadStatus(null, isRead);
            List<SysMessage> filtered = all.stream()
                    .filter(m -> messageType.equals(m.getMessageType()))
                    .toList();
            int start = Math.min(page * size, filtered.size());
            int end = Math.min(start + size, filtered.size());
            messagePage = new PageImpl<>(filtered.subList(start, end), pageRequest, filtered.size());
        } else if (messageType != null) {
            List<SysMessage> list = sysMessageRepository.findByMessageType(messageType);
            int start = Math.min(page * size, list.size());
            int end = Math.min(start + size, list.size());
            messagePage = new PageImpl<>(list.subList(start, end), pageRequest, list.size());
        } else if (isRead != null) {
            List<SysMessage> list = sysMessageRepository.findByReadStatus(isRead);
            int start = Math.min(page * size, list.size());
            int end = Math.min(start + size, list.size());
            messagePage = new PageImpl<>(list.subList(start, end), pageRequest, list.size());
        } else {
            messagePage = sysMessageRepository.findAll(pageRequest);
        }
        return ResponseEntity.ok(messagePage);
    }

    /**
     * 根据ID获取消息
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<SysMessage> getMessageById(@PathVariable Long messageId) {
        return sysMessageRepository.findById(messageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取用户的消息列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<SysMessage>> getUserMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer messageType) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        List<SysMessage> allMessages = sysMessageRepository.findByReceiverId(userId);

        if (messageType != null) {
            allMessages = allMessages.stream()
                    .filter(m -> messageType.equals(m.getMessageType()))
                    .toList();
        }

        int start = Math.min(page * size, allMessages.size());
        int end = Math.min(start + size, allMessages.size());
        Page<SysMessage> messagePage = new PageImpl<>(allMessages.subList(start, end), pageRequest, allMessages.size());
        return ResponseEntity.ok(messagePage);
    }

    /**
     * 获取用户的未读消息
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<Page<SysMessage>> getUserUnreadMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        List<SysMessage> unreadMessages = sysMessageRepository.findByReceiverIdAndReadStatus(userId, 0);

        int start = Math.min(page * size, unreadMessages.size());
        int end = Math.min(start + size, unreadMessages.size());
        Page<SysMessage> messagePage = new PageImpl<>(unreadMessages.subList(start, end), pageRequest, unreadMessages.size());
        return ResponseEntity.ok(messagePage);
    }

    /**
     * 创建消息
     */
    @PostMapping
    public ResponseEntity<SysMessage> createMessage(@RequestBody SysMessage message) {
        if (message.getIsRead() == null) {
            message.setIsRead(0);
        }
        SysMessage saved = sysMessageRepository.save(message);
        return ResponseEntity.ok(saved);
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/{messageId}/read")
    @Transactional
    public ResponseEntity<Map<String, Object>> markMessageAsRead(@PathVariable Long messageId) {
        var messageOpt = sysMessageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SysMessage message = messageOpt.get();
        message.setIsRead(1);
        message.setReadTime(LocalDateTime.now());
        sysMessageRepository.save(message);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "消息已标记为已读");
        return ResponseEntity.ok(result);
    }

    /**
     * 标记用户所有消息为已读
     */
    @PutMapping("/user/{userId}/read-all")
    @Transactional
    public ResponseEntity<Map<String, Object>> markAllMessagesAsRead(@PathVariable Long userId) {
        List<SysMessage> unreadMessages = sysMessageRepository.findByReceiverIdAndReadStatus(userId, 0);
        LocalDateTime now = LocalDateTime.now();
        for (SysMessage message : unreadMessages) {
            message.setIsRead(1);
            message.setReadTime(now);
        }
        sysMessageRepository.saveAll(unreadMessages);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "所有消息已标记为已读");
        result.put("count", unreadMessages.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteMessage(@PathVariable Long messageId) {
        if (!sysMessageRepository.existsById(messageId)) {
            return ResponseEntity.notFound().build();
        }
        sysMessageRepository.deleteById(messageId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "消息已删除");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取消息统计
     */
    @GetMapping("/statistics/{userId}")
    public ResponseEntity<Map<String, Object>> getMessageStatistics(@PathVariable Long userId) {
        Map<String, Object> stats = new HashMap<>();

        List<SysMessage> userMessages = sysMessageRepository.findByReceiverId(userId);
        long totalCount = userMessages.size();
        long unreadCount = sysMessageRepository.countByReceiverIdAndReadStatus(userId, 0);
        long readCount = sysMessageRepository.countByReceiverIdAndReadStatus(userId, 1);

        long todoCount = userMessages.stream().filter(m -> Integer.valueOf(1).equals(m.getMessageType())).count();
        long reminderCount = userMessages.stream().filter(m -> Integer.valueOf(2).equals(m.getMessageType())).count();
        long notificationCount = userMessages.stream().filter(m -> Integer.valueOf(3).equals(m.getMessageType())).count();

        stats.put("total", totalCount);
        stats.put("unread", unreadCount);
        stats.put("read", readCount);
        stats.put("todo", todoCount);
        stats.put("reminder", reminderCount);
        stats.put("notification", notificationCount);

        return ResponseEntity.ok(stats);
    }
}
