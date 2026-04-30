package com.ccms.controller;

import com.ccms.entity.SysMessage;
import com.ccms.repository.SysMessageRepository;
import com.ccms.service.MessageNotifyService;
import com.ccms.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 系统消息通知控制器
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/messages")
public class MessageNotifyController {
    
    @Autowired
    private MessageNotifyService messageNotifyService;
    
    @Autowired
    private SysMessageRepository sysMessageRepository;
    
    /**
     * 获取用户消息列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param readStatus 阅读状态
     * @param msgType 消息类型
     * @return 消息列表
     */
    @GetMapping("/list")
    public Result<Page<SysMessage>> getUserMessages(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String readStatus,
            @RequestParam(required = false) String msgType) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
            
            Specification<SysMessage> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("receiverId"), userId));
                predicates.add(cb.equal(root.get("deleted"), 0));
                
                if (readStatus != null && !readStatus.isEmpty()) {
                    predicates.add(cb.equal(root.get("readStatus"), readStatus));
                }
                
                if (msgType != null && !msgType.isEmpty()) {
                    predicates.add(cb.equal(root.get("msgType"), msgType));
                }
                
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            
            Page<SysMessage> messages = sysMessageRepository.findAll(spec, pageable);
            return Result.success(messages, "获取消息列表成功");
        } catch (Exception e) {
            return Result.error(500, "获取消息列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取未读消息统计
     * 
     * @param userId 用户ID
     * @return 未读消息统计
     */
    @GetMapping("/unread-stats")
    public Result<MessageNotifyService.UnreadStats> getUnreadStatistics(@RequestParam Long userId) {
        try {
            MessageNotifyService.UnreadStats stats = messageNotifyService.getUnreadStatistics(userId);
            return Result.success(stats, "获取未读统计成功");
        } catch (Exception e) {
            return Result.error(500, "获取未读统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 标记消息为已读
     * 
     * @param request 标记请求
     * @return 标记结果
     */
    @PostMapping("/mark-read")
    public Result<MessageNotifyService.MarkResult> markMessagesAsRead(@RequestBody MarkReadRequest request) {
        try {
            MessageNotifyService.MarkResult result = messageNotifyService.markMessagesAsRead(
                request.getMessageIds(), request.getUserId());
            
            if (result.isSuccess()) {
                return Result.success(result, result.getMessage());
            } else {
                return Result.error(400, result.getMessage());
            }
        } catch (Exception e) {
            return Result.error(500, "标记消息为已读失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送站内消息
     * 
     * @param request 发送请求
     * @return 发送结果
     */
    @PostMapping("/send-inner")
    public Result<MessageNotifyService.NotifyResult> sendInnerMessage(@RequestBody SendInnerRequest request) {
        try {
            MessageNotifyService.InnerMessage innerMessage = new MessageNotifyService.InnerMessage(
                request.getTitle(), request.getContent(), request.getReceivers());
            
            innerMessage.setMessageType(request.getMsgType());
            innerMessage.setMsgLevel(request.getMsgLevel());
            innerMessage.setBizType(request.getBizType());
            innerMessage.setBizId(request.getBizId());
            innerMessage.setSourceType(request.getSourceType());
            innerMessage.setSourceId(request.getSourceId());
            innerMessage.setCreatedBy(request.getCreatedBy());
            innerMessage.setRequireConfirm(request.getRequireConfirm());
            innerMessage.setConfirmMethod(request.getConfirmMethod());
            innerMessage.setExpireHours(request.getExpireHours());
            
            MessageNotifyService.NotifyResult result = messageNotifyService.sendInnerMessage(innerMessage);
            
            if (result.isSuccess()) {
                return Result.success(result, result.getMessage());
            } else {
                return Result.error(400, result.getMessage());
            }
        } catch (Exception e) {
            return Result.error(500, "发送站内消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送待办提醒
     * 
     * @param request 待办请求
     * @return 发送结果
     */
    @PostMapping("/send-todo")
    public Result<MessageNotifyService.NotifyResult> sendTodoReminder(@RequestBody SendTodoRequest request) {
        try {
            MessageNotifyService.TodoMessage todoMessage = new MessageNotifyService.TodoMessage(
                request.getTitle(), request.getContent(), request.getReceivers());
            
            todoMessage.setTodoType(request.getTodoType());
            todoMessage.setDeadline(request.getDeadline());
            todoMessage.setActionUrl(request.getActionUrl());
            todoMessage.setPriority(request.getPriority());
            todoMessage.setBizType(request.getBizType());
            todoMessage.setBizId(request.getBizId());
            todoMessage.setSourceType(request.getSourceType());
            todoMessage.setSourceId(request.getSourceId());
            todoMessage.setCreatedBy(request.getCreatedBy());
            
            MessageNotifyService.NotifyResult result = messageNotifyService.sendTodoReminder(todoMessage);
            
            if (result.isSuccess()) {
                return Result.success(result, result.getMessage());
            } else {
                return Result.error(400, result.getMessage());
            }
        } catch (Exception e) {
            return Result.error(500, "发送待办提醒失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据模板发送消息
     * 
     * @param request 模板发送请求
     * @return 发送结果
     */
    @PostMapping("/send-template")
    public Result<MessageNotifyService.BatchNotifyResult> sendTemplateMessage(@RequestBody SendTemplateRequest request) {
        try {
            MessageNotifyService.BatchNotifyResult result = messageNotifyService.sendTemplateMessage(
                request.getTemplateCode(), request.getParams(), request.getReceivers());
            
            return Result.success(result, "模板消息发送完成");
        } catch (Exception e) {
            return Result.error(500, "发送模板消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取消息详情
     * 
     * @param messageId 消息ID
     * @return 消息详情
     */
    @GetMapping("/detail/{messageId}")
    public Result<SysMessage> getMessageDetail(@PathVariable Long messageId) {
        try {
            SysMessage message = sysMessageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("消息不存在: " + messageId));
            
            // 如果消息未读，自动标记为已读
            if ("UNREAD".equals(message.getReadStatus())) {
                message.markAsRead();
                sysMessageRepository.save(message);
            }
            
            return Result.success(message, "获取消息详情成功");
        } catch (Exception e) {
            return Result.error(500, "获取消息详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除消息（逻辑删除）
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{messageId}")
    public Result<Void> deleteMessage(@PathVariable Long messageId, @RequestParam Long userId) {
        try {
            SysMessage message = sysMessageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("消息不存在: " + messageId));
            
            // 只能删除自己的消息
            if (!message.getReceiverId().equals(userId)) {
                return Result.error(403, "无权删除此消息");
            }
            
            message.setDeleted(1);
            message.setUpdatedTime(LocalDateTime.now());
            message.setUpdatedBy(userId);
            sysMessageRepository.save(message);
            
            return Result.<Void>success("删除消息成功", null);
        } catch (Exception e) {
            return Result.error(500, "删除消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除消息
     * 
     * @param request 批量删除请求
     * @return 删除结果
     */
    @PostMapping("/batch-delete")
    public Result<Void> batchDeleteMessages(@RequestBody BatchDeleteRequest request) {
        try {
            int deletedCount = 0;
            
            for (Long messageId : request.getMessageIds()) {
                SysMessage message = sysMessageRepository.findById(messageId).orElse(null);
                if (message != null && message.getReceiverId().equals(request.getUserId())) {
                    message.setDeleted(1);
                    message.setUpdatedTime(LocalDateTime.now());
                    message.setUpdatedBy(request.getUserId());
                    sysMessageRepository.save(message);
                    deletedCount++;
                }
            }
            
            return Result.<Void>success(String.format("成功删除%d条消息", deletedCount), null);
        } catch (Exception e) {
            return Result.error(500, "批量删除消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 确认消息
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     * @param method 确认方式
     * @return 确认结果
     */
    @PostMapping("/confirm/{messageId}")
    public Result<Void> confirmMessage(@PathVariable Long messageId, 
                                      @RequestParam Long userId,
                                      @RequestParam(defaultValue = "MANUAL") String method) {
        try {
            SysMessage message = sysMessageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("消息不存在: " + messageId));
            
            // 只能确认发给自己的消息
            if (!message.getReceiverId().equals(userId)) {
                return Result.error(403, "无权确认此消息");
            }
            
            message.confirm(method);
            sysMessageRepository.save(message);
            
            return Result.<Void>success("消息确认成功", null);
        } catch (Exception e) {
            return Result.error(500, "消息确认失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取消息发送统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 发送统计
     */
    @GetMapping("/statistics")
    public Result<MessageStatistics> getMessageStatistics(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        try {
            // 设置默认时间范围（最近30天）
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(30);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            MessageStatistics stats = new MessageStatistics();
            
            // 统计发送总量
            List<SysMessage> messages = sysMessageRepository.findBySendTimeBetween(startTime, endTime);
            stats.setTotalSent(messages.size());
            
            // 统计发送成功率
            long successCount = messages.stream()
                    .filter(m -> "SENT".equals(m.getSendStatus()))
                    .count();
            stats.setSuccessRate(messages.size() > 0 ? (double) successCount / messages.size() : 0.0);
            
            // 按类型统计
            Map<String, Long> typeCount = messages.stream()
                    .collect(java.util.stream.Collectors.groupingBy(SysMessage::getMsgType, java.util.stream.Collectors.counting()));
            stats.setTypeStatistics(typeCount);
            
            // 按渠道统计
            Map<String, Long> channelCount = messages.stream()
                    .collect(java.util.stream.Collectors.groupingBy(SysMessage::getSendMethod, java.util.stream.Collectors.counting()));
            stats.setChannelStatistics(channelCount);
            
            return Result.success(stats, "获取消息统计成功");
        } catch (Exception e) {
            return Result.error(500, "获取消息统计失败: " + e.getMessage());
        }
    }
    
    // ========== 请求参数类 ==========
    
    /**
     * 标记已读请求
     */
    public static class MarkReadRequest {
        private List<Long> messageIds;
        private Long userId;
        
        public List<Long> getMessageIds() { return messageIds; }
        public void setMessageIds(List<Long> messageIds) { this.messageIds = messageIds; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
    
    /**
     * 发送站内消息请求
     */
    public static class SendInnerRequest {
        private String title;
        private String content;
        private List<MessageNotifyService.Receiver> receivers;
        private String msgType;
        private String msgLevel;
        private String bizType;
        private Long bizId;
        private String sourceType;
        private Long sourceId;
        private Long createdBy;
        private boolean requireConfirm;
        private String confirmMethod;
        private Long expireHours;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public List<MessageNotifyService.Receiver> getReceivers() { return receivers; }
        public void setReceivers(List<MessageNotifyService.Receiver> receivers) { this.receivers = receivers; }
        
        public String getMsgType() { return msgType; }
        public void setMsgType(String msgType) { this.msgType = msgType; }
        
        public String getMsgLevel() { return msgLevel; }
        public void setMsgLevel(String msgLevel) { this.msgLevel = msgLevel; }
        
        public String getBizType() { return bizType; }
        public void setBizType(String bizType) { this.bizType = bizType; }
        
        public Long getBizId() { return bizId; }
        public void setBizId(Long bizId) { this.bizId = bizId; }
        
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        
        public Long getSourceId() { return sourceId; }
        public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
        
        public Long getCreatedBy() { return createdBy; }
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
        
        public boolean getRequireConfirm() { return requireConfirm; }
        public void setRequireConfirm(boolean requireConfirm) { this.requireConfirm = requireConfirm; }
        
        public String getConfirmMethod() { return confirmMethod; }
        public void setConfirmMethod(String confirmMethod) { this.confirmMethod = confirmMethod; }
        
        public Long getExpireHours() { return expireHours; }
        public void setExpireHours(Long expireHours) { this.expireHours = expireHours; }
    }
    
    /**
     * 发送待办提醒请求
     */
    public static class SendTodoRequest {
        private String title;
        private String content;
        private List<MessageNotifyService.Receiver> receivers;
        private String todoType;
        private LocalDateTime deadline;
        private String actionUrl;
        private String priority;
        private String bizType;
        private Long bizId;
        private String sourceType;
        private Long sourceId;
        private Long createdBy;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public List<MessageNotifyService.Receiver> getReceivers() { return receivers; }
        public void setReceivers(List<MessageNotifyService.Receiver> receivers) { this.receivers = receivers; }
        
        public String getTodoType() { return todoType; }
        public void setTodoType(String todoType) { this.todoType = todoType; }
        
        public LocalDateTime getDeadline() { return deadline; }
        public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
        
        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        
        public String getBizType() { return bizType; }
        public void setBizType(String bizType) { this.bizType = bizType; }
        
        public Long getBizId() { return bizId; }
        public void setBizId(Long bizId) { this.bizId = bizId; }
        
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        
        public Long getSourceId() { return sourceId; }
        public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
        
        public Long getCreatedBy() { return createdBy; }
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    }
    
    /**
     * 发送模板消息请求
     */
    public static class SendTemplateRequest {
        private String templateCode;
        private Map<String, Object> params;
        private List<MessageNotifyService.Receiver> receivers;
        
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
        
        public Map<String, Object> getParams() { return params; }
        public void setParams(Map<String, Object> params) { this.params = params; }
        
        public List<MessageNotifyService.Receiver> getReceivers() { return receivers; }
        public void setReceivers(List<MessageNotifyService.Receiver> receivers) { this.receivers = receivers; }
    }
    
    /**
     * 批量删除请求
     */
    public static class BatchDeleteRequest {
        private List<Long> messageIds;
        private Long userId;
        
        public List<Long> getMessageIds() { return messageIds; }
        public void setMessageIds(List<Long> messageIds) { this.messageIds = messageIds; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
    
    /**
     * 消息统计信息
     */
    public static class MessageStatistics {
        private int totalSent;
        private double successRate;
        private Map<String, Long> typeStatistics;
        private Map<String, Long> channelStatistics;
        
        public int getTotalSent() { return totalSent; }
        public void setTotalSent(int totalSent) { this.totalSent = totalSent; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public Map<String, Long> getTypeStatistics() { return typeStatistics; }
        public void setTypeStatistics(Map<String, Long> typeStatistics) { this.typeStatistics = typeStatistics; }
        
        public Map<String, Long> getChannelStatistics() { return channelStatistics; }
        public void setChannelStatistics(Map<String, Long> channelStatistics) { this.channelStatistics = channelStatistics; }
    }
}