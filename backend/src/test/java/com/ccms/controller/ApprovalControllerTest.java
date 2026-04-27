package com.ccms.controller;

import com.ccms.BaseTest;
import com.ccms.entity.Approval;
import com.ccms.service.ApprovalService;
import com.ccms.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ApprovalController单元测试
 */
class ApprovalControllerTest extends BaseTest {

    @Mock
    private ApprovalService approvalService;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private ApprovalController approvalController;

    @BeforeEach
    void setUpForController() {
        super.setUp();
    }

    @Test
    @DisplayName("获取审批列表 - 成功")
    void testGetApprovalList_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Integer page = 1;
        Integer size = 10;
        Long approverId = 1L;
        Long applicantId = 2L;
        Integer status = 1;
        String businessType = "EXPENSE_APPLY";

        List<Approval> approvalList = Arrays.asList(
            createTestApprovalWithId(1L),
            createTestApprovalWithId(2L)
        );
        Page<Approval> approvalPage = new PageImpl<>(approvalList, PageRequest.of(page - 1, size), approvalList.size());

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        when(approvalService.getApprovalList(any(Pageable.class), eq(approverId), eq(applicantId), eq(status), eq(businessType)))
            .thenReturn(approvalPage);

        // 执行测试
        ResponseEntity<Page<Approval>> response = approvalController.getApprovalList(
            page, size, approverId, applicantId, status, businessType, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "approval:read");
        verify(approvalService, times(1)).getApprovalList(
            any(Pageable.class), eq(approverId), eq(applicantId), eq(status), eq(businessType));
    }

    @Test
    @DisplayName("获取审批列表 - 权限不足")
    void testGetApprovalList_PermissionDenied() {
        // 准备测试数据
        String token = "Bearer invalid_token";

        // Mock权限验证失败
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(false);

        // 执行测试
        ResponseEntity<Page<Approval>> response = approvalController.getApprovalList(
            1, 10, null, null, null, null, token);

        // 验证结果
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        // 验证服务层没有被调用
        verify(approvalService, never()).getApprovalList(any(Pageable.class), any(), any(), any(), any());
    }

    @Test
    @DisplayName("获取待审批列表 - 成功")
    void testGetPendingApprovals_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Integer page = 1;
        Integer size = 10;
        Long approverId = 1L;
        String businessType = "EXPENSE_APPLY";

        List<Approval> pendingApprovals = Arrays.asList(
            createTestApprovalWithId(1L),
            createTestApprovalWithId(2L)
        );
        Page<Approval> pendingPage = new PageImpl<>(pendingApprovals, PageRequest.of(page - 1, size), pendingApprovals.size());

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        when(approvalService.getPendingApprovals(any(Pageable.class), eq(approverId), eq(businessType)))
            .thenReturn(pendingPage);

        // 执行测试
        ResponseEntity<Page<Approval>> response = approvalController.getPendingApprovals(
            page, size, approverId, businessType, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "approval:read");
        verify(approvalService, times(1)).getPendingApprovals(
            any(Pageable.class), eq(approverId), eq(businessType));
    }

    @Test
    @DisplayName("执行审批操作 - 批准")
    void testApprove_Success_Approved() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long approvalId = 1L;
        Map<String, Object> approveInfo = new HashMap<>();
        approveInfo.put("action", "approve");
        approveInfo.put("comment", "同意预算合理");

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "审批操作执行成功");
        when(approvalService.approve(approvalId, approveInfo)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = approvalController.approve(approvalId, approveInfo, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("审批操作执行成功", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "approval:action");
        verify(approvalService, times(1)).approve(approvalId, approveInfo);
    }

    @Test
    @DisplayName("执行审批操作 - 拒绝")
    void testApprove_Success_Rejected() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long approvalId = 1L;
        Map<String, Object> rejectInfo = new HashMap<>();
        rejectInfo.put("action", "reject");
        rejectInfo.put("comment", "预算超出范围");

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "审批操作执行成功");
        when(approvalService.approve(approvalId, rejectInfo)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = approvalController.approve(approvalId, rejectInfo, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("审批操作执行成功", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "approval:action");
        verify(approvalService, times(1)).approve(approvalId, rejectInfo);
    }

    @Test
    @DisplayName("执行审批操作 - 审批记录不存在")
    void testApprove_NotFound() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long approvalId = 999L;
        Map<String, Object> approveInfo = new HashMap<>();

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层抛出异常
        when(approvalService.approve(approvalId, approveInfo)).thenThrow(
            new RuntimeException("审批记录不存在")
        );

        // 执行测试
        ResponseEntity<Map<String, String>> response = approvalController.approve(approvalId, approveInfo, token);

        // 验证结果
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        // 验证Mock调用
        verify(approvalService, times(1)).approve(approvalId, approveInfo);
    }

    @Test
    @DisplayName("执行审批操作 - 状态不合法")
    void testApprove_InvalidStatus() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long approvalId = 1L;
        Map<String, Object> approveInfo = new HashMap<>();

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层抛出状态异常
        when(approvalService.approve(approvalId, approveInfo)).thenThrow(
            new RuntimeException("审批记录状态不合法，无法执行操作")
        );

        // 执行测试
        ResponseEntity<Map<String, String>> response = approvalController.approve(approvalId, approveInfo, token);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("状态不合法"));
        
        // 验证Mock调用
        verify(approvalService, times(1)).approve(approvalId, approveInfo);
    }

    @Test
    @DisplayName("审批委托 - 成功")
    void testDelegateApproval_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long approvalId = 1L;
        Map<String, Object> delegateInfo = new HashMap<>();
        delegateInfo.put("delegateTo", "user2");
        delegateInfo.put("reason", "出差期间");

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "审批委托成功");
        when(approvalService.delegateApproval(approvalId, delegateInfo)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = approvalController.delegateApproval(approvalId, delegateInfo, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("审批委托成功", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "approval:delegate");
        verify(approvalService, times(1)).delegateApproval(approvalId, delegateInfo);
    }

    @Test
    @DisplayName("设置加急审批 - 成功")
    void testSetApprovalUrgent_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long approvalId = 1L;
        Map<String, Object> urgentInfo = new HashMap<>();
        urgentInfo.put("urgent", true);
        urgentInfo.put("reason", "紧急业务需求");

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "加急设置成功");
        when(approvalService.setApprovalUrgent(approvalId, urgentInfo)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = approvalController.setApprovalUrgent(approvalId, urgentInfo, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("加急设置成功", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "approval:urgent");
        verify(approvalService, times(1)).setApprovalUrgent(approvalId, urgentInfo);
    }

    @Test
    @DisplayName("设置加急审批 - 审批已完成")
    void testSetApprovalUrgent_AlreadyCompleted() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long approvalId = 1L;
        Map<String, Object> urgentInfo = new HashMap<>();

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层抛出现状异常
        when(approvalService.setApprovalUrgent(approvalId, urgentInfo)).thenThrow(
            new RuntimeException("审批已完成，无法设置加急")
        );

        // 执行测试
        ResponseEntity<Map<String, String>> response = approvalController.setApprovalUrgent(approvalId, urgentInfo, token);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("已完成"));
        
        // 验证Mock调用
        verify(approvalService, times(1)).setApprovalUrgent(approvalId, urgentInfo);
    }

    /**
     * 创建测试审批数据
     */
    private Approval createTestApproval() {
        Approval approval = new Approval();
        approval.setId(1L);
        approval.setBusinessType("EXPENSE_APPLY");
        approval.setBusinessId(1L);
        approval.setApplicantId(1L);
        approval.setApproverId(2L);
        approval.setStatus(1);
        approval.setPriority(1);
        return approval;
    }

    /**
     * 创建带ID的测试审批
     */
    private Approval createTestApprovalWithId(Long id) {
        Approval approval = createTestApproval();
        approval.setId(id);
        return approval;
    }
}