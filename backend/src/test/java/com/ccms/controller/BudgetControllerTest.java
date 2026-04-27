package com.ccms.controller;

import com.ccms.BaseTest;
import com.ccms.entity.Budget;
import com.ccms.service.BudgetService;
import com.ccms.service.JwtService;
import com.ccms.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * BudgetController单元测试
 */
class BudgetControllerTest extends BaseTest {

    @Mock
    private BudgetService budgetService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private BudgetController budgetController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUpForController() {
        super.setUp();
    }

    @Test
    @DisplayName("获取预算列表 - 成功")
    void testGetBudgetList_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Integer page = 1;
        Integer size = 10;
        Integer year = 2024;
        Long departmentId = 1L;
        Integer status = 1;

        List<Budget> budgetList = Arrays.asList(
            createTestBudgetWithId(1L),
            createTestBudgetWithId(2L)
        );
        Page<Budget> budgetPage = new PageImpl<>(budgetList, PageRequest.of(page - 1, size), budgetList.size());

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        when(budgetService.getBudgetList(any(Pageable.class), eq(year), eq(departmentId), eq(status)))
            .thenReturn(budgetPage);

        // 执行测试
        ResponseEntity<Page<Budget>> response = budgetController.getBudgetList(page, size, year, departmentId, status, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "budget:read");
        verify(budgetService, times(1)).getBudgetList(any(Pageable.class), eq(year), eq(departmentId), eq(status));
    }

    @Test
    @DisplayName("获取预算列表 - 权限不足")
    void testGetBudgetList_PermissionDenied() {
        // 准备测试数据
        String token = "Bearer invalid_token";

        // Mock权限验证失败
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(false);

        // 执行测试
        ResponseEntity<Page<Budget>> response = budgetController.getBudgetList(1, 10, null, null, null, token);

        // 验证结果
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        // 验证服务层没有被调用
        verify(budgetService, never()).getBudgetList(any(Pageable.class), any(), any(), any());
    }

    @Test
    @DisplayName("获取预算详情 - 成功")
    void testGetBudgetDetail_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long budgetId = 1L;
        Budget budget = createTestBudgetWithId(budgetId);

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        when(budgetService.getBudgetDetail(budgetId)).thenReturn(budget);

        // 执行测试
        ResponseEntity<Budget> response = budgetController.getBudgetDetail(budgetId, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(budgetId, response.getBody().getId());
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "budget:read");
        verify(budgetService, times(1)).getBudgetDetail(budgetId);
    }

    @Test
    @DisplayName("获取预算详情 - 预算不存在")
    void testGetBudgetDetail_NotFound() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long budgetId = 999L;

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层抛出异常
        when(budgetService.getBudgetDetail(budgetId)).thenThrow(
            new RuntimeException("预算不存在")
        );

        // 执行测试
        ResponseEntity<Budget> response = budgetController.getBudgetDetail(budgetId, token);

        // 验证结果
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        // 验证Mock调用
        verify(budgetService, times(1)).getBudgetDetail(budgetId);
    }

    @Test
    @DisplayName("创建预算 - 成功")
    void testCreateBudget_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Budget budget = createTestBudget();

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "预算创建成功");
        when(budgetService.createBudget(budget)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = budgetController.createBudget(budget, token);

        // 验证结果
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("预算创建成功", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "budget:create");
        verify(budgetService, times(1)).createBudget(budget);
    }

    @Test
    @DisplayName("创建预算 - 数据验证失败")
    void testCreateBudget_ValidationFailed() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Budget invalidBudget = createTestBudget();
        invalidBudget.setTotalAmount(-100.0); // 无效金额

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层抛出验证异常
        when(budgetService.createBudget(invalidBudget)).thenThrow(
            new IllegalArgumentException("预算金额不能为负数")
        );

        // 执行测试
        ResponseEntity<Map<String, String>> response = budgetController.createBudget(invalidBudget, token);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("预算金额不能为负数"));
        
        // 验证Mock调用
        verify(budgetService, times(1)).createBudget(invalidBudget);
    }

    @Test
    @DisplayName("更新预算 - 成功")
    void testUpdateBudget_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long budgetId = 1L;
        Budget budget = createTestBudgetWithId(budgetId);

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "预算更新成功");
        when(budgetService.updateBudget(budgetId, budget)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = budgetController.updateBudget(budgetId, budget, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("预算更新成功", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "budget:update");
        verify(budgetService, times(1)).updateBudget(budgetId, budget);
    }

    @Test
    @DisplayName("删除预算 - 成功")
    void testDeleteBudget_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long budgetId = 1L;

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "预算删除成功");
        when(budgetService.deleteBudget(budgetId)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = budgetController.deleteBudget(budgetId, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("预算删除成功", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "budget:delete");
        verify(budgetService, times(1)).deleteBudget(budgetId);
    }

    /**
     * 创建带ID的测试预算
     */
    private Budget createTestBudgetWithId(Long id) {
        Budget budget = createTestBudget();
        budget.setId(id);
        return budget;
    }
}