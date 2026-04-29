package com.ccms.service;

import com.ccms.entity.message.Message;
import com.ccms.entity.message.MessageTemplate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知服务接口
 * 
 * @author 系统生成
 */
public interface MessageService {
    
    /**
     * 发送消息
     * 
     * @param messageType 消息类型
     * @param senderId 发送人ID
     * @param receiverId 接收人ID
     * @param title 消息标题
     * @param content 消息内容
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 发送的消息
     */
    Message sendMessage(Integer messageType, Long senderId, Long receiverId, 
                       String title, String content, Integer businessType, Long businessId);
    
    /**
     * 发送系统消息
     * 
     * @param receiverId 接收人ID
     * @param title 消息标题
     * @param content 消息内容
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 发送的消息
     */
    Message sendSystemMessage(Long receiverId, String title, String content, 
                             Integer businessType, Long businessId);
    
    /**
     * 批量发送消息
     * 
     * @param messageType 消息类型
     * @param senderId 发送人ID
     * @param receiverIds 接收人ID列表
     * @param title 消息标题
     * @param content 消息内容
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 发送的消息列表
     */
    List<Message> sendMessages(Integer messageType, Long senderId, List<Long> receiverIds,
                              String title, String content, Integer businessType, Long businessId);
    
    /**
     * 获取用户的消息列表
     * 
     * @param userId 用户ID
     * @param messageType 消息类型（可选）
     * @param isRead 是否已读（可选）
     * @param startDate 开始时间（可选）
     * @param endDate 结束时间（可选）
     * @return 消息列表
     */
    List<Message> getUserMessages(Long userId, Integer messageType, Boolean isRead, 
                                 LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取未读消息数量
     * 
     * @param userId 用户ID
     * @return 未读消息数量
     */
    long getUnreadMessageCount(Long userId);
    
    /**
     * 标记消息为已读
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void markMessageAsRead(Long messageId, Long userId);
    
    /**
     * 批量标记消息为已读
     * 
     * @param messageIds 消息ID列表
     * @param userId 用户ID
     */
    void markMessagesAsRead(List<Long> messageIds, Long userId);
    
    /**
     * 标记所有消息为已读
     * 
     * @param userId 用户ID
     */
    void markAllMessagesAsRead(Long userId);
    
    /**
     * 删除消息
     * 
     * @param messageId 消息ID
     * @param userId 用户ID
     */
    void deleteMessage(Long messageId, Long userId);
    
    /**
     * 批量删除消息
     * 
     * @param messageIds 消息ID列表
     * @param userId 用户ID
     */
    void deleteMessages(List<Long> messageIds, Long userId);
    
    /**
     * 保存消息模板
     * 
     * @param template 消息模板
     * @return 保存的模板
     */
    MessageTemplate saveMessageTemplate(MessageTemplate template);
    
    /**
     * 获取消息模板
     * 
     * @param templateCode 模板编码
     * @return 消息模板
     */
    MessageTemplate getMessageTemplate(String templateCode);
    
    /**
     * 根据模板发送消息
     * 
     * @param templateCode 模板编码
     * @param receiverId 接收人ID
     * @param parameters 模板参数
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 发送的消息
     */
    Message sendMessageByTemplate(String templateCode, Long receiverId, 
                                 java.util.Map<String, Object> parameters,
                                 Integer businessType, Long businessId);
    
    /**
     * 获取消息统计信息
     * 
     * @param userId 用户ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 消息统计
     */
    MessageStatistics getMessageStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 创建消息订阅
     * 
     * @param userId 用户ID
     * @param messageType 消息类型
     * @return 是否创建成功
     */
    boolean subscribeToMessageType(Long userId, Integer messageType);
    
    /**
     * 取消消息订阅
     * 
     * @param userId 用户ID
     * @param messageType 消息类型
     * @return 是否取消成功
     */
    boolean unsubscribeFromMessageType(Long userId, Integer messageType);
    
    /**
     * 检查用户是否订阅了某类消息
     * 
     * @param userId 用户ID
     * @param messageType 消息类型
     * @return 是否订阅
     */
    boolean isUserSubscribed(Long userId, Integer messageType);
    
    /**
     * 发送审批消息
     * 
     * @param userId 用户ID
     * @param title 消息标题
     * @param content 消息内容
     */
    void sendApprovalMessage(Long userId, String title, String content);
    
    /**
     * 消息统计信息
     */
    class MessageStatistics {
        private final Long totalMessages;
        private final Long readMessages;
        private final Long unreadMessages;
        private final Long systemMessages;
        private final Long workflowMessages;
        
        public MessageStatistics(Long totalMessages, Long readMessages, 
                               Long unreadMessages, Long systemMessages, 
                               Long workflowMessages) {
            this.totalMessages = totalMessages;
            this.readMessages = readMessages;
            this.unreadMessages = unreadMessages;
            this.systemMessages = systemMessages;
            this.workflowMessages = workflowMessages;
        }
        
        public Long getTotalMessages() {
            return totalMessages;
        }
        
        public Long getReadMessages() {
            return readMessages;
        }
        
        public Long getUnreadMessages() {
            return unreadMessages;
        }
        
        public Long getSystemMessages() {
            return systemMessages;
        }
        
        public Long getWorkflowMessages() {
            return workflowMessages;
        }
    }
}