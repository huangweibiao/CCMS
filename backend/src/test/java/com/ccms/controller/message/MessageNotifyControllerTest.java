package com.ccms.controller.message;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.message.Message;
import com.ccms.entity.message.MessageTemplate;
import com.ccms.service.MessageService;
import com.ccms.service.MessageTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 消息通知控制器单元测试
 */
@WebMvcTest(MessageNotifyController.class)
class MessageNotifyControllerTest extends ControllerTestBase {

    @MockBean
    private MessageTemplateService messageTemplateService;

    @MockBean
    private MessageService messageService;

    private MessageTemplate createTestTemplate(Long id, String code, String name) {
        MessageTemplate template = new MessageTemplate();
        template.setId(id);
        template.setTemplateCode(code);
        template.setTemplateName(name);
        template.setTemplateType("SYSTEM");
        template.setSubjectTemplate("测试标题");
        template.setTemplateContent("测试内容");
        template.setCreateTime(LocalDateTime.now());
        return template;
    }

    private Message createTestMessage(Long id, String title) {
        Message message = new Message();
        message.setId(id);
        message.setTitle(title);
        message.setContent("消息内容");
        message.setMessageType("SYSTEM");
        message.setSenderId(1L);
        message.setReceiverId(2L);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }

    @Test
    void shouldReturnTemplateList() throws Exception {
        MessageTemplate template = createTestTemplate(1L, "T001", "测试模板");
        when(messageTemplateService.getAllTemplates())
                .thenReturn(Collections.singletonList(template));

        performGet("/api/message/templates")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].templateCode").value("T001"));
    }

    @Test
    void shouldReturnTemplateByIdWhenExists() throws Exception {
        MessageTemplate template = createTestTemplate(1L, "T001", "测试模板");
        when(messageTemplateService.getAllTemplates())
                .thenReturn(Collections.singletonList(template));

        performGet("/api/message/templates/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateCode").value("T001"));
    }

    @Test
    void shouldReturnNotFoundWhenTemplateNotExists() throws Exception {
        when(messageTemplateService.getAllTemplates()).thenReturn(Collections.emptyList());

        performGet("/api/message/templates/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateTemplateSuccess() throws Exception {
        MessageTemplateService.TemplateRequest request = new MessageTemplateService.TemplateRequest();
        request.setTemplateCode("T002");
        request.setTemplateName("新模板");

        MessageTemplateService.TemplateOperationResult result = 
                new MessageTemplateService.TemplateOperationResult(true, "创建成功");
        when(messageTemplateService.createTemplate(any(MessageTemplateService.TemplateRequest.class)))
                .thenReturn(result);

        performPost("/api/message/templates", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldUpdateTemplateSuccess() throws Exception {
        MessageTemplate template = createTestTemplate(1L, "T001", "测试模板");
        MessageTemplateService.TemplateRequest request = new MessageTemplateService.TemplateRequest();
        request.setTemplateName("更新模板");

        MessageTemplateService.TemplateOperationResult result = 
                new MessageTemplateService.TemplateOperationResult(true, "更新成功");
        when(messageTemplateService.updateTemplate(anyLong(), any(MessageTemplateService.TemplateRequest.class)))
                .thenReturn(result);
        when(messageTemplateService.getAllTemplates()).thenReturn(Collections.singletonList(template));

        performPut("/api/message/templates/1", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldDeleteTemplateSuccess() throws Exception {
        MessageTemplate template = createTestTemplate(1L, "T001", "测试模板");
        when(messageTemplateService.getAllTemplates()).thenReturn(Collections.singletonList(template));
        when(messageTemplateService.deleteTemplate("T001")).thenReturn(true);

        performDelete("/api/message/templates/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldSendMessageByTemplateSuccess() throws Exception {
        Message message = createTestMessage(1L, "测试消息");
        when(messageService.sendMessageByTemplate(anyString(), anyLong(), anyMap(), anyInt(), anyLong()))
                .thenReturn(message);

        Map<String, Object> request = new HashMap<>();
        request.put("templateCode", "T001");
        request.put("receiverId", 2L);
        request.put("parameters", new HashMap<String, Object>());
        request.put("businessType", 1);
        request.put("businessId", 1L);

        performPost("/api/message/send", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldSendMessageDirectlySuccess() throws Exception {
        Message message = createTestMessage(1L, "直接消息");
        when(messageService.sendMessage(anyInt(), anyLong(), anyLong(), anyString(), anyString(), anyInt(), anyLong()))
                .thenReturn(message);

        Map<String, Object> request = new HashMap<>();
        request.put("messageType", 1);
        request.put("senderId", 1L);
        request.put("receiverId", 2L);
        request.put("title", "测试标题");
        request.put("content", "测试内容");
        request.put("businessType", 1);
        request.put("businessId", 1L);

        performPost("/api/message/send", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldSendBatchMessageSuccess() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("templateCode", "T001");
        request.put("senderId", 1L);
        request.put("receiverIds", Arrays.asList(2L, 3L));
        request.put("parameters", new HashMap<String, Object>());
        request.put("businessType", 1);
        request.put("businessId", 1L);

        Message message = createTestMessage(1L, "批量消息");
        when(messageService.sendMessageByTemplate(anyString(), anyLong(), anyMap(), anyInt(), anyLong()))
                .thenReturn(message);

        performPost("/api/message/send-batch", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalCount").value(2));
    }

    @Test
    void shouldPreviewMessageSuccess() throws Exception {
        MessageTemplate template = createTestTemplate(1L, "T001", "测试模板");
        when(messageService.getMessageTemplate("T001")).thenReturn(template);

        Map<String, Object> request = new HashMap<>();
        request.put("templateCode", "T001");
        request.put("parameters", new HashMap<String, Object>());

        performPost("/api/message/preview", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.templateCode").value("T001"));
    }

    @Test
    void shouldReturnErrorWhenPreviewTemplateNotExists() throws Exception {
        when(messageService.getMessageTemplate("T999")).thenReturn(null);

        Map<String, Object> request = new HashMap<>();
        request.put("templateCode", "T999");
        request.put("parameters", new HashMap<String, Object>());

        performPost("/api/message/preview", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
