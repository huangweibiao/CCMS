package com.ccms.controller;

import com.ccms.BaseTest;
import com.ccms.entity.ExpenseApply;
import com.ccms.service.ExpenseApplyService;
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
 * ExpenseApplyController单元测试
 */
class ExpenseApplyControllerTest extends BaseTest {

    @Mock
    private ExpenseApplyService expenseApplyService;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private ExpenseApplyController expenseApplyController;

    @BeforeEach
    void setUpForController() {
        super.setUp();
    }

    @Test
    @DisplayName("获取费用申请列表 - 成功")
    void testGetExpenseApplyList_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Integer page = 1;
        Integer size = 10;
        Long applicantId = 1L;
        Long departmentId = 1L;
        Integer status = 1;

        List<ExpenseApply> applyList = Arrays.asList(
            createTestExpenseApplyWithId(1L),
            createTestExpenseApplyWithId(2L)
        );
        Page<ExpenseApply> applyPage = new PageImpl<>(applyList, PageRequest.of(page - 1, size), applyList.size());

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        when(expenseApplyService.getExpenseApplyList(any(Pageable.class), eq(applicantId), eq(departmentId), eq(status)))
            .thenReturn(applyPage);

        // 执行测试
        ResponseEntity<Page<ExpenseApply>> response = expenseApplyController.getExpenseApplyList(
            page, size, applicantId, departmentId, status, null, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "expense_apply:read");
        verify(expenseApplyService, times(1)).getExpenseApplyList(
            any(Pageable.class), eq(applicantId), eq(departmentId), eq(status));
    }

    @Test
    @DisplayName("获取费用申请列表 - 权限不足")
    void testGetExpenseApplyList_PermissionDenied() {
        // 准备测试数据
        String token = "Bearer invalid_token";

        // Mock权限验证失败
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(false);

        // 执行测试
        ResponseEntity<Page<ExpenseApply>> response = expenseApplyController.getExpenseApplyList(
            1, 10, null, null, null, null, token);

        // 验证结果
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        // 验证服务层没有被调用
        verify(expenseApplyService, never()).getExpenseApplyList(any(Pageable.class), any(), any(), any());
    }

    @Test
    @DisplayName("获取费用申请详情 - 成功")
    void testGetExpenseApplyDetail_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long applyId = 1L;
        ExpenseApply expenseApply = createTestExpenseApplyWithId(applyId);

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        when(expenseApplyService.getExpenseApplyDetail(applyId)).thenReturn(expenseApply);

        // 执行测试
        ResponseEntity<ExpenseApply> response = expenseApplyController.getExpenseApplyDetail(applyId, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(applyId, response.getBody().getId());
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "expense_apply:read");
        verify(expenseApplyService, times(1)).getExpenseApplyDetail(applyId);
    }

    @Test
    @DisplayName("获取费用申请详情 - 申请不存在")
    void testGetExpenseApplyDetail_NotFound() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long applyId = 999L;

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层抛出异常
        when(expenseApplyService.getExpenseApplyDetail(applyId)).thenThrow(
            new RuntimeException("费用申请不存在")
        );

        // 执行测试
        ResponseEntity<ExpenseApply> response = expenseApplyController.getExpenseApplyDetail(applyId, token);

        // 验证结果
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        // 验证Mock调用
        verify(expenseApplyService, times(1)).getExpenseApplyDetail(applyId);
    }

    @Test
    @DisplayName("创建费用申请 - 成功")
    void testCreateExpenseApply_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        ExpenseApply expenseApply = createTestExpenseApply();

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "费用申请创建成功");
        when(expenseApplyService.createExpenseApply(expenseApply)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = expenseApplyController.createExpenseApply(expenseApply, token);

        // 验证结果
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("费用申请创建成功", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "expense_apply:create");
        verify(expenseApplyService, times(1)).createExpenseApply(expenseApply);
    }

    @Test
    @DisplayName("创建费用申请 - 预算不足")
    void testCreateExpenseApply_InsufficientBudget() {
        // 准备测试数据
        String token = "Bearer valid_token";
        ExpenseApply expenseApply = createTestExpenseApply();
        expenseApply.setAmount(100000.0); // 超额金额

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层抛出预算不足异常
        when(expenseApplyService.createExpenseApply(expenseApply)).thenThrow(
            new RuntimeException("部门预算不足")
        );

        // 执行测试
        ResponseEntity<Map<String, String>> response = expenseApplyController.createExpenseApply(expenseApply, token);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("部门预算不足"));
        
        // 验证Mock调用
        verify(expenseApplyService, times(1)).createExpenseApply(expenseApply);
    }

    @Test
    @DisplayName("更新费用申请 - 成功")
    void testUpdateExpenseApply_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long applyId = 1L;
        ExpenseApply expenseApply = createTestExpenseApplyWithId(applyId);

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "费用申请更新成功");
        when(expenseApplyService.updateExpenseApply(applyId, expenseApply)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = expenseApplyController.updateExpenseApply(applyId, expenseApply, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("费用申请更新成功", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "expense_apply:update");
        verify(expenseApplyService, times(1)).updateExpenseApply(applyId, expenseApply);
    }

    @Test
    @DisplayName("提交费用申请审批 - 成功")
    void testSubmitExpenseApply_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long applyId = 1L;

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "费用申请提交成功，等待审批");
        when(expenseApplyService.submitExpenseApply(applyId)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = expenseApplyController.submitExpenseApply(applyId, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("费用申请提交成功，等待审批", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "expense_apply:submit");
        verify(expenseApplyService, times(1)).submitExpenseApply(applyId);
    }

    @Test
    @DisplayName("提交费用申请审批 - 状态不合法")
    void testSubmitExpenseApply_InvalidStatus() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long applyId = 1L;

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层抛出状态异常
        when(expenseApplyService.submitExpenseApply(applyId)).thenThrow(
            new RuntimeException("费用申请状态不合法，无法提交审批")
        );

        // 执行测试
        ResponseEntity<Map<String, String>> response = expenseApplyController.submitExpenseApply(applyId, token);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("状态不合法"));
        
        // 验证Mock调用
        verify(expenseApplyService, times(1)).submitExpenseApply(applyId);
    }

    /**
     * 创建带ID的测试费用申请
     */
    private ExpenseApply createTestExpenseApplyWithId(Long id) {
        ExpenseApply expenseApply = createTestExpenseApply();
        expenseApply.setId(id);
        return expenseApply;
    }
}