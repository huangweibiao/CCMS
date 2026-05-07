package com.ccms.controller.message;

import com.ccms.entity.message.Message;
import com.ccms.entity.message.MessageTemplate;
import com.ccms.service.MessageService;
import com.ccms.service.MessageTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 消息通知控制器
 * 对应设计文档：4.8节 消息通知相关表
 *
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/message")
public class MessageNotifyController {

    private final MessageTemplateService messageTemplateService;
    private final MessageService messageService;

    @Autowired
    public MessageNotifyController(MessageTemplateService messageTemplateService,
                                   MessageService messageService) {
        this.messageTemplateService = messageTemplateService;
        this.messageService = messageService;
    }

    /**
     * 获取消息模板列表
     *
     * @param page         页码
     * @param size         每页大小
     * @param templateType 模板类型（可选）
     * @param templateName 模板名称（可选，支持模糊查询）
     * @return 分页模板列表
     */
    @GetMapping("/templates")
    public ResponseEntity<Page<MessageTemplate>> getTemplateList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String templateName) {
        List<MessageTemplate> allTemplates = messageTemplateService.getAllTemplates();

        // 根据条件过滤
        List<MessageTemplate> filteredTemplates = allTemplates.stream()
                .filter(t -> templateType == null || templateType.equals(t.getTemplateType()))
                .filter(t -> templateName == null || t.getTemplateName() == null ||
                        t.getTemplateName().contains(templateName))
                .toList();

        // 手动分页
        int start = page * size;
        int end = Math.min(start + size, filteredTemplates.size());
        List<MessageTemplate> pageContent = start < filteredTemplates.size()
                ? filteredTemplates.subList(start, end)
                : Collections.emptyList();

        Page<MessageTemplate> templatePage = new PageImpl<>(
                pageContent,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime")),
                filteredTemplates.size()
        );

        return ResponseEntity.ok(templatePage);
    }

    /**
     * 根据ID获取模板
     *
     * @param templateId 模板ID
     * @return 模板详情
     */
    @GetMapping("/templates/{templateId}")
    public ResponseEntity<MessageTemplate> getTemplateById(@PathVariable Long templateId) {
        return messageTemplateService.getAllTemplates().stream()
                .filter(t -> templateId.equals(t.getId()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建模板
     *
     * @param request 模板创建请求
     * @return 创建结果
     */
    @PostMapping("/templates")
    public ResponseEntity<Map<String, Object>> createTemplate(
            @RequestBody MessageTemplateService.TemplateRequest request) {
        MessageTemplateService.TemplateOperationResult result =
                messageTemplateService.createTemplate(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        if (result.getTemplateId() != null) {
            response.put("templateId", result.getTemplateId());
        }
        if (result.getTemplate() != null) {
            response.put("template", result.getTemplate());
        }

        return result.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * 更新模板
     *
     * @param templateId 模板ID
     * @param request    模板更新请求
     * @return 更新结果
     */
    @PutMapping("/templates/{templateId}")
    public ResponseEntity<Map<String, Object>> updateTemplate(
            @PathVariable Long templateId,
            @RequestBody MessageTemplateService.TemplateRequest request) {
        MessageTemplateService.TemplateOperationResult result =
                messageTemplateService.updateTemplate(templateId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        if (result.getTemplateId() != null) {
            response.put("templateId", result.getTemplateId());
        }
        if (result.getTemplate() != null) {
            response.put("template", result.getTemplate());
        }

        return result.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * 删除模板
     *
     * @param templateId 模板ID
     * @return 删除结果
     */
    @DeleteMapping("/templates/{templateId}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@PathVariable Long templateId) {
        // 先获取模板编码
        Optional<MessageTemplate> templateOpt = messageTemplateService.getAllTemplates().stream()
                .filter(t -> templateId.equals(t.getId()))
                .findFirst();

        Map<String, Object> response = new HashMap<>();

        if (templateOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "模板不存在");
            return ResponseEntity.badRequest().body(response);
        }

        boolean deleted = messageTemplateService.deleteTemplate(templateOpt.get().getTemplateCode());

        response.put("success", deleted);
        response.put("message", deleted ? "模板删除成功" : "模板删除失败");

        return deleted
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * 发送消息
     *
     * @param request 消息发送请求
     * @return 发送结果
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody SendMessageRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (request.getTemplateCode() != null && !request.getTemplateCode().isEmpty()) {
                // 使用模板发送消息
                Message message = messageService.sendMessageByTemplate(
                        request.getTemplateCode(),
                        request.getReceiverId(),
                        request.getParameters(),
                        request.getBusinessType(),
                        request.getBusinessId()
                );
                response.put("success", true);
                response.put("message", "消息发送成功");
                response.put("data", message);
            } else {
                // 直接发送消息
                var message = messageService.sendMessage(
                        request.getMessageType(),
                        request.getSenderId(),
                        request.getReceiverId(),
                        request.getTitle(),
                        request.getContent(),
                        request.getBusinessType(),
                        request.getBusinessId()
                );
                response.put("success", true);
                response.put("message", "消息发送成功");
                response.put("data", message);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "消息发送失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量发送消息
     *
     * @param request 批量发送请求
     * @return 发送结果
     */
    @PostMapping("/send-batch")
    public ResponseEntity<Map<String, Object>> sendBatchMessage(@RequestBody BatchSendMessageRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Object> results = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (Long receiverId : request.getReceiverIds()) {
                try {
                    if (request.getTemplateCode() != null && !request.getTemplateCode().isEmpty()) {
                        // 使用模板发送
                        var result = messageService.sendMessageByTemplate(
                                request.getTemplateCode(),
                                receiverId,
                                request.getParameters(),
                                request.getBusinessType(),
                                request.getBusinessId()
                        );
                        results.add(result);
                    } else {
                        // 直接发送
                        var result = messageService.sendMessage(
                                request.getMessageType(),
                                request.getSenderId(),
                                receiverId,
                                request.getTitle(),
                                request.getContent(),
                                request.getBusinessType(),
                                request.getBusinessId()
                        );
                        results.add(result);
                    }
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    Map<String, Object> errorInfo = new HashMap<>();
                    errorInfo.put("receiverId", receiverId);
                    errorInfo.put("error", e.getMessage());
                    results.add(errorInfo);
                }
            }

            response.put("success", true);
            response.put("message", "批量发送完成");
            response.put("successCount", successCount);
            response.put("failCount", failCount);
            response.put("totalCount", request.getReceiverIds().size());
            response.put("results", results);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量发送失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 预览消息
     *
     * @param request 预览请求
     * @return 预览结果
     */
    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewMessage(@RequestBody PreviewMessageRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            MessageTemplate template = messageService.getMessageTemplate(request.getTemplateCode());
            if (template == null) {
                response.put("success", false);
                response.put("message", "模板不存在");
                return ResponseEntity.badRequest().body(response);
            }

            // 渲染模板内容
            String renderedTitle = renderTemplate(template.getSubjectTemplate(), request.getParameters());
            String renderedContent = renderTemplate(template.getTemplateContent(), request.getParameters());

            Map<String, Object> previewData = new HashMap<>();
            previewData.put("templateCode", template.getTemplateCode());
            previewData.put("templateName", template.getTemplateName());
            previewData.put("title", renderedTitle);
            previewData.put("content", renderedContent);
            previewData.put("parameters", request.getParameters());

            response.put("success", true);
            response.put("message", "预览生成成功");
            response.put("data", previewData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "预览生成失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
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
     * 发送消息请求
     */
    public static class SendMessageRequest {
        private String templateCode;
        private Integer messageType;
        private Long senderId;
        private Long receiverId;
        private String title;
        private String content;
        private Integer businessType;
        private Long businessId;
        private Map<String, Object> parameters;

        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }

        public Integer getMessageType() {
            return messageType;
        }

        public void setMessageType(Integer messageType) {
            this.messageType = messageType;
        }

        public Long getSenderId() {
            return senderId;
        }

        public void setSenderId(Long senderId) {
            this.senderId = senderId;
        }

        public Long getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(Long receiverId) {
            this.receiverId = receiverId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Integer getBusinessType() {
            return businessType;
        }

        public void setBusinessType(Integer businessType) {
            this.businessType = businessType;
        }

        public Long getBusinessId() {
            return businessId;
        }

        public void setBusinessId(Long businessId) {
            this.businessId = businessId;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }

    /**
     * 批量发送消息请求
     */
    public static class BatchSendMessageRequest {
        private String templateCode;
        private Integer messageType;
        private Long senderId;
        private List<Long> receiverIds;
        private String title;
        private String content;
        private Integer businessType;
        private Long businessId;
        private Map<String, Object> parameters;

        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }

        public Integer getMessageType() {
            return messageType;
        }

        public void setMessageType(Integer messageType) {
            this.messageType = messageType;
        }

        public Long getSenderId() {
            return senderId;
        }

        public void setSenderId(Long senderId) {
            this.senderId = senderId;
        }

        public List<Long> getReceiverIds() {
            return receiverIds;
        }

        public void setReceiverIds(List<Long> receiverIds) {
            this.receiverIds = receiverIds;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Integer getBusinessType() {
            return businessType;
        }

        public void setBusinessType(Integer businessType) {
            this.businessType = businessType;
        }

        public Long getBusinessId() {
            return businessId;
        }

        public void setBusinessId(Long businessId) {
            this.businessId = businessId;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }

    /**
     * 预览消息请求
     */
    public static class PreviewMessageRequest {
        private String templateCode;
        private Map<String, Object> parameters;

        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }
}
