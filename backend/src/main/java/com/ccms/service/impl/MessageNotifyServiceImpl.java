package com.ccms.service.impl;

import com.ccms.entity.SysMessage;
import com.ccms.repository.SysMessageRepository;
import com.ccms.service.MessageNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息通知服务实现类
 * 
 * @author 系统生成
 */
@Service
@Transactional
public class MessageNotifyServiceImpl implements MessageNotifyService {
    
    @Autowired
    private SysMessageRepository sysMessageRepository;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    @Value("${app.notify.sms.enabled:false}")
    private boolean smsEnabled;
    
    @Value("${app.notify.dingtalk.enabled:false}")
    private boolean dingTalkEnabled;
    
    @Value("${app.notify.wechat-work.enabled:false}")
    private boolean wechatWorkEnabled;
    
    // 内置消息模板
    private static final Map<String, MessageTemplate> BUILTIN_TEMPLATES = new HashMap<>();
    
    static {
        // 审批通过模板
        BUILTIN_TEMPLATES.put("APPROVAL_PASSED", new MessageTemplate(
            1L, "APPROVAL_PASSED", "审批通过通知",
            "${bizType}审批通过通知",
            "您提交的${bizType}（${bizNo}）已审批通过。审批意见：${approvalComment}。请及时查收。",
            "APPROVAL", Map.of("bizType", "费用申请")
        ));
        
        // 审批拒绝模板
        BUILTIN_TEMPLATES.put("APPROVAL_REJECTED", new MessageTemplate(
            2L, "APPROVAL_REJECTED", "审批拒绝通知",
            "${bizType}审批结果通知",
            "您提交的${bizType}（${bizNo}）审批未通过。拒绝原因：${rejectReason}。请修改后重新提交。",
            "APPROVAL", Map.of("bizType", "费用申请")
        ));
        
        // 待办提醒模板
        BUILTIN_TEMPLATES.put("TODO_REMINDER", new MessageTemplate(
            3L, "TODO_REMINDER", "待办事项提醒",
            "待办提醒：${todoTitle}",
            "您有新的待办事项：${todoContent}。截止时间：${deadline}。请及时处理。",
            "TODO", Map.of()
        ));
        
        // 借款超期提醒
        BUILTIN_TEMPLATES.put("LOAN_OVERDUE", new MessageTemplate(
            4L, "LOAN_OVERDUE", "借款超期提醒",
            "借款超期提醒",
            "您有借款已超期${overdueDays}天，金额${amount}元。请尽快处理，逾期将影响您的信用记录。",
            "REMINDER", Map.of()
        ));
    }
    
    @Override
    public NotifyResult sendInnerMessage(InnerMessage message) {
        try {
            List<NotifyResult> results = new ArrayList<>();
            
            for (Receiver receiver : message.getReceivers()) {
                SysMessage sysMessage = createSysMessage(message, receiver);
                sysMessage.setSendMethod("INNER");
                sysMessage.setPlanSendTime(LocalDateTime.now());
                
                // 设置确认要求
                if (message.isRequireConfirm()) {
                    sysMessage.setIsConfirmed(false);
                    if (message.getConfirmMethod() != null) {
                        sysMessage.setConfirmMethod(message.getConfirmMethod());
                    }
                }
                
                // 设置过期时间
                if (message.getExpireHours() != null) {
                    sysMessage.setExpireTime(LocalDateTime.now().plusHours(message.getExpireHours()));
                }
                
                SysMessage savedMessage = sysMessageRepository.save(sysMessage);
                
                // 尝试立即发送
                boolean sendSuccess = sendInnerMessageImmediately(savedMessage);
                
                if (sendSuccess) {
                    results.add(new NotifyResult(true, "发送成功", savedMessage.getId(), "INNER"));
                } else {
                    results.add(new NotifyResult(false, "发送失败，已加入延迟队列", savedMessage.getId(), "INNER"));
                }
            }
            
            return new NotifyResult(true, "站内消息发送完成", null, "INNER");
            
        } catch (Exception e) {
            return new NotifyResult(false, "发送站内消息失败: " + e.getMessage(), null, "INNER");
        }
    }
    
    @Override
    @Async
    public NotifyResult sendEmail(EmailMessage emailMessage) {
        try {
            if (fromEmail == null || fromEmail.isEmpty()) {
                return new NotifyResult(false, "邮件服务未配置", null, "EMAIL");
            }
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setSubject(emailMessage.getSubject());
            
            // 使用HTML内容或纯文本内容
            if (emailMessage.getHtmlContent() != null) {
                helper.setText(emailMessage.getHtmlContent(), true);
            } else {
                helper.setText(emailMessage.getContent(), false);
            }
            
            // 添加收件人
            List<String> toEmails = emailMessage.getReceivers().stream()
                    .filter(r -> r.getEmail() != null && !r.getEmail().isEmpty())
                    .map(Receiver::getEmail)
                    .collect(Collectors.toList());
            
            if (toEmails.isEmpty()) {
                return new NotifyResult(false, "没有有效的邮箱地址", null, "EMAIL");
            }
            
            helper.setTo(toEmails.toArray(new String[0]));
            
            // 设置CC和BCC
            if (emailMessage.getCc() != null) {
                helper.setCc(emailMessage.getCc());
            }
            if (emailMessage.getBcc() != null) {
                helper.setBcc(emailMessage.getBcc());
            }
            
            // 设置回复地址
            if (emailMessage.getReplyTo() != null) {
                helper.setReplyTo(emailMessage.getReplyTo());
            }
            
            // 发送邮件
            mailSender.send(mimeMessage);
            
            // 记录邮件发送日志
            recordEmailLog(emailMessage, toEmails, true, null);
            
            return new NotifyResult(true, "邮件发送成功", null, "EMAIL");
            
        } catch (Exception e) {
            recordEmailLog(emailMessage, Collections.emptyList(), false, e.getMessage());
            return new NotifyResult(false, "邮件发送失败: " + e.getMessage(), null, "EMAIL");
        }
    }
    
    @Override
    public NotifyResult sendSms(SmsMessage smsMessage) {
        try {
            if (!smsEnabled) {
                return new NotifyResult(false, "短信服务未启用", null, "SMS");
            }
            
            // 筛选有效的手机号码
            List<String> validPhones = smsMessage.getReceivers().stream()
                    .filter(r -> r.getPhone() != null && isValidPhone(r.getPhone()))
                    .map(Receiver::getPhone)
                    .collect(Collectors.toList());
            
            if (validPhones.isEmpty()) {
                return new NotifyResult(false, "没有有效的手机号码", null, "SMS");
            }
            
            // 调用短信服务API（简化实现）
            boolean sendSuccess = sendSmsViaApi(validPhones, smsMessage);
            
            if (sendSuccess) {
                recordSmsLog(smsMessage, validPhones, true, null);
                return new NotifyResult(true, "短信发送成功", null, "SMS");
            } else {
                recordSmsLog(smsMessage, validPhones, false, "短信服务调用失败");
                return new NotifyResult(false, "短信发送失败", null, "SMS");
            }
            
        } catch (Exception e) {
            recordSmsLog(smsMessage, Collections.emptyList(), false, e.getMessage());
            return new NotifyResult(false, "短信发送失败: " + e.getMessage(), null, "SMS");
        }
    }
    
    @Override
    public NotifyResult sendChatMessage(ChatMessage chatMessage) {
        try {
            String chatType = chatMessage.getChatType();
            
            if ("DINGDING".equals(chatType) && !dingTalkEnabled) {
                return new NotifyResult(false, "钉钉服务未启用", null, chatType);
            }
            if ("WECHAT_WORK".equals(chatType) && !wechatWorkEnabled) {
                return new NotifyResult(false, "企业微信服务未启用", null, chatType);
            }
            
            // 筛选有效的聊天用户ID
            List<String> validUserIds = chatMessage.getReceivers().stream()
                    .filter(r -> r.getChatUserId() != null && !r.getChatUserId().isEmpty())
                    .map(Receiver::getChatUserId)
                    .collect(Collectors.toList());
            
            if (validUserIds.isEmpty()) {
                return new NotifyResult(false, "没有有效的聊天用户ID", null, chatType);
            }
            
            // 调用聊天服务API（简化实现）
            boolean sendSuccess = sendChatViaApi(chatType, validUserIds, chatMessage);
            
            if (sendSuccess) {
                recordChatLog(chatMessage, validUserIds, true, null);
                return new NotifyResult(true, chatType + "消息发送成功", null, chatType);
            } else {
                recordChatLog(chatMessage, validUserIds, false, chatType + "服务调用失败");
                return new NotifyResult(false, chatType + "消息发送失败", null, chatType);
            }
            
        } catch (Exception e) {
            recordChatLog(chatMessage, Collections.emptyList(), false, e.getMessage());
            return new NotifyResult(false, "聊天消息发送失败: " + e.getMessage(), null, chatMessage.getChatType());
        }
    }
    
    @Override
    public BatchNotifyResult batchSendMessages(List<Message> messages) {
        try {
            List<NotifyResult> results = new ArrayList<>();
            
            for (Message message : messages) {
                NotifyResult result;
                
                if (message instanceof InnerMessage) {
                    result = sendInnerMessage((InnerMessage) message);
                } else if (message instanceof EmailMessage) {
                    result = sendEmail((EmailMessage) message);
                } else if (message instanceof SmsMessage) {
                    result = sendSms((SmsMessage) message);
                } else if (message instanceof ChatMessage) {
                    result = sendChatMessage((ChatMessage) message);
                } else {
                    result = new NotifyResult(false, "不支持的消息类型", null, "UNKNOWN");
                }
                
                results.add(result);
            }
            
            int successCount = (int) results.stream().filter(NotifyResult::isSuccess).count();
            int failedCount = results.size() - successCount;
            
            return new BatchNotifyResult(results.size(), successCount, failedCount, results);
            
        } catch (Exception e) {
            return new BatchNotifyResult(messages.size(), 0, messages.size(), 
                Collections.emptyList());
        }
    }
    
    @Override
    public NotifyResult sendTodoReminder(TodoMessage todoMessage) {
        try {
            // 创建系统消息记录
            SysMessage sysMessage = new SysMessage();
            sysMessage.setTitle(todoMessage.getTitle());
            sysMessage.setContent(todoMessage.getContent());
            sysMessage.setMsgType("TODO");
            sysMessage.setMsgLevel(todoMessage.getPriority());
            sysMessage.setReceiverId(todoMessage.getReceivers().get(0).getUserId());
            sysMessage.setReceiverName(todoMessage.getReceivers().get(0).getUserName());
            sysMessage.setBizType(todoMessage.getTodoType());
            sysMessage.setBizData(serializeBizData(todoMessage));
            sysMessage.setSendMethod("INNER");
            sysMessage.setPlanSendTime(LocalDateTime.now());
            
            // 设置截止时间
            if (todoMessage.getDeadline() != null) {
                sysMessage.setExpireTime(todoMessage.getDeadline());
            }
            
            SysMessage savedMessage = sysMessageRepository.save(sysMessage);
            
            // 尝试发送多通道提醒
            boolean innerSuccess = sendInnerMessageImmediately(savedMessage);
            boolean emailSuccess = sendEmailReminder(todoMessage);
            
            String resultMsg = "待办提醒已发送";
            if (innerSuccess) resultMsg += "（站内信）";
            if (emailSuccess) resultMsg += "（邮件）";
            
            return new NotifyResult(true, resultMsg, savedMessage.getId(), "MULTI");
            
        } catch (Exception e) {
            return new NotifyResult(false, "发送待办提醒失败: " + e.getMessage(), null, "MULTI");
        }
    }
    
    @Override
    public UnreadStats getUnreadStatistics(Long userId) {
        try {
            // 查询用户未读消息统计
            int totalUnread = sysMessageRepository.countByReceiverIdAndReadStatus(userId, "UNREAD");
            int urgentUnread = sysMessageRepository.countByReceiverIdAndReadStatusAndMsgLevel(
                userId, "UNREAD", "URGENT");
            int todoCount = sysMessageRepository.countByReceiverIdAndReadStatusAndMsgType(
                userId, "UNREAD", "TODO");
            
            // 按类型统计
            Map<String, Integer> typeUnreadCount = new HashMap<>();
            List<Object[]> typeCounts = sysMessageRepository.countUnreadByType(userId);
            for (Object[] count : typeCounts) {
                String msgType = (String) count[0];
                Long countVal = (Long) count[1];
                typeUnreadCount.put(msgType, countVal.intValue());
            }
            
            return new UnreadStats(userId, totalUnread, urgentUnread, todoCount, typeUnreadCount);
            
        } catch (Exception e) {
            return new UnreadStats(userId, 0, 0, 0, Collections.emptyMap());
        }
    }
    
    @Override
    public MarkResult markMessagesAsRead(List<Long> messageIds, Long userId) {
        try {
            int markedCount = 0;
            
            for (Long messageId : messageIds) {
                Optional<SysMessage> messageOpt = sysMessageRepository.findById(messageId);
                if (messageOpt.isPresent()) {
                    SysMessage message = messageOpt.get();
                    if (message.getReceiverId().equals(userId) && "UNREAD".equals(message.getReadStatus())) {
                        message.markAsRead();
                        sysMessageRepository.save(message);
                        markedCount++;
                    }
                }
            }
            
            String message = markedCount > 0 ? 
                String.format("成功标记%d条消息为已读", markedCount) :
                "没有需要标记为已读的消息";
            
            return new MarkResult(true, markedCount, message);
            
        } catch (Exception e) {
            return new MarkResult(false, 0, "标记消息为已读失败: " + e.getMessage());
        }
    }
    
    @Override
    public MessageTemplate getMessageTemplate(String templateCode) {
        return BUILTIN_TEMPLATES.get(templateCode);
    }
    
    @Override
    public BatchNotifyResult sendTemplateMessage(String templateCode, Map<String, Object> params, 
                                               List<Receiver> receivers) {
        try {
            MessageTemplate template = getMessageTemplate(templateCode);
            if (template == null) {
                return new BatchNotifyResult(0, 0, 0, Collections.emptyList());
            }
            
            // 合并参数
            Map<String, Object> finalParams = new HashMap<>(template.getDefaultParams());
            if (params != null) {
                finalParams.putAll(params);
            }
            
            // 渲染模板
            String title = renderTemplate(template.getTitleTemplate(), finalParams);
            String content = renderTemplate(template.getContentTemplate(), finalParams);
            
            // 创建InnerMessage并发送
            InnerMessage message = new InnerMessage(title, content, receivers);
            message.setMessageType(template.getMessageType());
            
            NotifyResult result = sendInnerMessage(message);
            
            return new BatchNotifyResult(1, result.isSuccess() ? 1 : 0, 
                result.isSuccess() ? 0 : 1, Collections.singletonList(result));
            
        } catch (Exception e) {
            return new BatchNotifyResult(1, 0, 1, Collections.emptyList());
        }
    }
    
    @Override
    @Scheduled(fixedDelay = 30000) // 每30秒执行一次
    public void processDelayedMessages() {
        try {
            // 查找需要重试的消息
            List<SysMessage> retryMessages = sysMessageRepository.findBySendStatusAndRetryCountLessThan(
                "FAILED", 3); // 最多重试3次
            
            for (SysMessage message : retryMessages) {
                if (shouldRetry(message)) {
                    retrySendMessage(message);
                }
            }
            
            // 查找计划发送时间已到的消息
            List<SysMessage> scheduledMessages = sysMessageRepository.findBySendStatusAndPlanSendTimeBefore(
                "PENDING", LocalDateTime.now());
            
            for (SysMessage message : scheduledMessages) {
                sendScheduledMessage(message);
            }
            
        } catch (Exception e) {
            System.err.println("处理延迟消息失败: " + e.getMessage());
        }
    }
    
    // ========== 私有方法 ==========
    
    private SysMessage createSysMessage(Message message, Receiver receiver) {
        SysMessage sysMessage = new SysMessage();
        sysMessage.setTitle(message.getTitle());
        sysMessage.setContent(message.getContent());
        sysMessage.setMsgType(message.getMessageType());
        sysMessage.setMsgLevel(message.getMsgLevel());
        sysMessage.setReceiverId(receiver.getUserId());
        sysMessage.setReceiverName(receiver.getUserName());
        sysMessage.setSenderId(message.getCreatedBy());
        sysMessage.setBizType(message.getBizType());
        sysMessage.setBizId(message.getBizId());
        sysMessage.setBizData(serializeBizData(message));
        sysMessage.setSourceType(message.getSourceType());
        sysMessage.setSourceId(message.getSourceId());
        sysMessage.setCreatedBy(message.getCreatedBy());
        
        return sysMessage;
    }
    
    private boolean sendInnerMessageImmediately(SysMessage message) {
        try {
            message.setSendStatus("SENT");
            message.setSendTime(LocalDateTime.now());
            message.setMsgStatus("SENT");
            sysMessageRepository.save(message);
            return true;
        } catch (Exception e) {
            message.markAsFailed(e.getMessage());
            sysMessageRepository.save(message);
            return false;
        }
    }
    
    private boolean sendSmsViaApi(List<String> phones, SmsMessage smsMessage) {
        // 模拟短信发送API调用
        try {
            Thread.sleep(100); // 模拟网络延迟
            return Math.random() > 0.1; // 90%成功率
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean sendChatViaApi(String chatType, List<String> userIds, ChatMessage chatMessage) {
        // 模拟聊天API调用
        try {
            Thread.sleep(200); // 模拟网络延迟
            return Math.random() > 0.05; // 95%成功率
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean sendEmailReminder(TodoMessage todoMessage) {
        // 简化实现，实际应该创建EmailMessage并发送
        return false;
    }
    
    private String renderTemplate(String template, Map<String, Object> params) {
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(key, value);
        }
        return result;
    }
    
    private String serializeBizData(Message message) {
        // 简化实现，实际应该使用JSON序列化
        Map<String, Object> data = new HashMap<>();
        data.put("bizType", message.getBizType());
        data.put("bizId", message.getBizId());
        data.put("sourceType", message.getSourceType());
        data.put("sourceId", message.getSourceId());
        
        return data.toString(); // 简化处理
    }
    
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }
    
    private boolean shouldRetry(SysMessage message) {
        if (message.getLastRetryTime() == null) {
            return true;
        }
        
        long minutesSinceLastRetry = java.time.Duration.between(
            message.getLastRetryTime(), LocalDateTime.now()).toMinutes();
        
        // 重试间隔策略：第1次等1分钟，第2次等5分钟，第3次等15分钟
        long requiredWaitTime = Math.min(1L << (message.getRetryCount() - 1), 15L);
        
        return minutesSinceLastRetry >= requiredWaitTime;
    }
    
    private void retrySendMessage(SysMessage message) {
        try {
            // 根据发送方式重试
            boolean success = false;
            
            switch (message.getSendMethod()) {
                case "EMAIL":
                    success = retryEmail(message);
                    break;
                case "SMS":
                    success = retrySms(message);
                    break;
                case "DINGDING":
                case "WECHAT_WORK":
                    success = retryChat(message);
                    break;
                default:
                    success = sendInnerMessageImmediately(message);
            }
            
            if (success) {
                message.markAsSent();
            } else {
                message.markAsFailed("重试失败");
            }
            
            sysMessageRepository.save(message);
            
        } catch (Exception e) {
            message.markAsFailed("重试异常: " + e.getMessage());
            sysMessageRepository.save(message);
        }
    }
    
    private void sendScheduledMessage(SysMessage message) {
        try {
            boolean success = sendInnerMessageImmediately(message);
            if (!success) {
                message.markAsFailed("计划发送失败");
                sysMessageRepository.save(message);
            }
        } catch (Exception e) {
            message.markAsFailed("计划发送异常: " + e.getMessage());
            sysMessageRepository.save(message);
        }
    }
    
    private boolean retryEmail(SysMessage message) {
        // 简化重试逻辑
        return Math.random() > 0.3; // 70%成功率
    }
    
    private boolean retrySms(SysMessage message) {
        // 简化重试逻辑
        return Math.random() > 0.2; // 80%成功率
    }
    
    private boolean retryChat(SysMessage message) {
        // 简化重试逻辑
        return Math.random() > 0.1; // 90%成功率
    }
    
    private void recordEmailLog(EmailMessage message, List<String> recipients, boolean success, String error) {
        System.out.println(String.format("[EmailLog] %s - To: %s, Subject: %s, Success: %s, Error: %s",
            LocalDateTime.now(), recipients, message.getSubject(), success, error));
    }
    
    private void recordSmsLog(SmsMessage message, List<String> phones, boolean success, String error) {
        System.out.println(String.format("[SmsLog] %s - Phones: %s, Content: %s, Success: %s, Error: %s",
            LocalDateTime.now(), phones, message.getContent(), success, error));
    }
    
    private void recordChatLog(ChatMessage message, List<String> userIds, boolean success, String error) {
        System.out.println(String.format("[ChatLog] %s - UserIds: %s, Type: %s, Success: %s, Error: %s",
            LocalDateTime.now(), userIds, message.getChatType(), success, error));
    }
}