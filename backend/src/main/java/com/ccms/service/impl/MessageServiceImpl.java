package com.ccms.service.impl;

import com.ccms.entity.message.Message;
import com.ccms.entity.message.MessageTemplate;
import com.ccms.repository.message.MessageRepository;
import com.ccms.repository.message.MessageTemplateRepository;
import com.ccms.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 消息通知服务实现类
 * 
 * @author 系统生成
 */
@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageTemplateRepository messageTemplateRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository,
                            MessageTemplateRepository messageTemplateRepository) {
        this.messageRepository = messageRepository;
        this.messageTemplateRepository = messageTemplateRepository;
    }

    @Override
    public Message sendMessage(Integer messageType, Long senderId, Long receiverId, 
                              String title, String content, Integer businessType, Long businessId) {
        Message message = new Message();
        message.setTitle(title);
        message.setContent(content);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessageType(messageType != null ? messageType.toString() : "0");
        message.setBusinessType(businessType != null ? businessType.toString() : "0");
        message.setBusinessId(businessId);
        message.setRead(false);
        
        // 检查用户是否订阅了该类消息
        if (!isUserSubscribed(receiverId, messageType)) {
            // 如果用户没有订阅，根据业务重要性决定是否发送
            if (!isImportantBusinessType(businessType)) {
                return null; // 不发送非重要消息给未订阅用户
            }
        }
        
        return messageRepository.save(message);
    }

    @Override
    public Message sendSystemMessage(Long receiverId, String title, String content, 
                                    Integer businessType, Long businessId) {
        // 系统消息，发送人ID为0
        return sendMessage(0, 0L, receiverId, title, content, businessType, businessId);
    }

    @Override
    public List<Message> sendMessages(Integer messageType, Long senderId, List<Long> receiverIds,
                                     String title, String content, Integer businessType, Long businessId) {
        List<Message> messages = new ArrayList<>();
        
        for (Long receiverId : receiverIds) {
            Message message = sendMessage(messageType, senderId, receiverId, title, 
                                        content, businessType, businessId);
            if (message != null) {
                messages.add(message);
            }
        }
        
        return messages;
    }

    @Override
    public List<Message> getUserMessages(Long userId, Integer messageType, Boolean isRead, 
                                        LocalDateTime startDate, LocalDateTime endDate) {
        // 简化实现，实际应该根据参数过滤
        if (messageType != null) {
            return messageRepository.findByMessageType(messageType.toString());
        } else {
            return messageRepository.findByReceiverId(userId);
        }
    }

    @Override
    public long getUnreadMessageCount(Long userId) {
        if (messageRepository.countUnreadMessages(userId) == null) {
            return 0;
        }
        return messageRepository.countUnreadMessages(userId);
    }

    @Override
    public void markMessageAsRead(Long messageId, Long userId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            throw new RuntimeException("消息不存在");
        }
        
        Message message = messageOpt.get();
        if (!message.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权操作此消息");
        }
        
        message.setRead(true);
        message.setReadTime(LocalDateTime.now());
        messageRepository.save(message);
    }

    @Override
    public void markMessagesAsRead(List<Long> messageIds, Long userId) {
        for (Long messageId : messageIds) {
            markMessageAsRead(messageId, userId);
        }
    }

    @Override
    public void markAllMessagesAsRead(Long userId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndReadFalse(userId);
        
        for (Message message : unreadMessages) {
            message.setRead(true);
            message.setReadTime(LocalDateTime.now());
            messageRepository.save(message);
        }
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            throw new RuntimeException("消息不存在");
        }
        
        Message message = messageOpt.get();
        if (!message.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权删除此消息");
        }
        
        messageRepository.deleteById(messageId);
    }

    @Override
    public void deleteMessages(List<Long> messageIds, Long userId) {
        for (Long messageId : messageIds) {
            deleteMessage(messageId, userId);
        }
    }

    @Override
    public MessageTemplate saveMessageTemplate(MessageTemplate template) {
        return messageTemplateRepository.save(template);
    }

    @Override
    public MessageTemplate getMessageTemplate(String templateCode) {
        Optional<MessageTemplate> templateOpt = messageTemplateRepository.findByTemplateCode(templateCode);
        return templateOpt.orElse(null);
    }

    @Override
    public Message sendMessageByTemplate(String templateCode, Long receiverId, 
                                        Map<String, Object> parameters,
                                        Integer businessType, Long businessId) {
        MessageTemplate template = getMessageTemplate(templateCode);
        if (template == null) {
            throw new RuntimeException("消息模板不存在");
        }
        
        // 渲染模板内容
        String title = renderTemplate(template.getSubjectTemplate(), parameters);
        String content = renderTemplate(template.getTemplateContent(), parameters);
        
        return sendMessage(Integer.parseInt(template.getTemplateType()), 0L, receiverId, 
                title, content, Integer.parseInt(template.getBusinessType()), businessId);
    }

    @Override
    public MessageStatistics getMessageStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        // 简化实现统计逻辑
        List<Message> userMessages = messageRepository.findByReceiverId(userId);
        long totalMessages = userMessages.size();
        long readMessages = userMessages.stream().filter(m -> Boolean.TRUE.equals(m.getRead())).count();
        long unreadMessages = userMessages.stream().filter(m -> !Boolean.TRUE.equals(m.getRead())).count();
        
        return new MessageStatistics(totalMessages, readMessages, unreadMessages, 0L, 0L);
    }

    @Override
    public boolean subscribeToMessageType(Long userId, Integer messageType) {
        // 实现消息订阅逻辑
        // 这里简化实现，实际应该有订阅表
        return true; // 总是返回成功
    }

    @Override
    public boolean unsubscribeFromMessageType(Long userId, Integer messageType) {
        // 实现取消订阅逻辑
        return true; // 总是返回成功
    }

    @Override
    public boolean isUserSubscribed(Long userId, Integer messageType) {
        // 实现检查订阅逻辑
        // 这里简化实现，默认用户订阅了所有重要类型的消息
        return isImportantMessageType(messageType);
    }
    
    /**
     * 渲染模板内容
     */
    private String renderTemplate(String template, Map<String, Object> parameters) {
        if (template == null || parameters == null) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
    
    /**
     * 判断是否为重要消息类型
     */
    private boolean isImportantMessageType(Integer messageType) {
        // 定义重要的消息类型
        // 0: 系统消息, 1: 审批消息, 2: 预算预警, 3: 费用提醒
        return messageType == 0 || messageType == 1 || messageType == 2 || messageType == 3;
    }
    
    /**
     * 判断是否为重要业务类型
     */
    private boolean isImportantBusinessType(Integer businessType) {
        // 定义重要的业务类型
        // 1: 费用申请, 2: 费用报销, 3: 预算调整, 4: 紧急事务
        return businessType == 1 || businessType == 2 || businessType == 3 || businessType == 4;
    }
    
    @Override
    public void sendApprovalMessage(Long userId, String title, String content) {
        // 审批消息使用消息类型1（审批消息），业务类型1（费用申请）
        sendMessage(1, 0L, userId, title, content, 1, null);
    }
}