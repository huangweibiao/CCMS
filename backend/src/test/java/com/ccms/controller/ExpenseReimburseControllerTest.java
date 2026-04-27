package com.ccms.controller;

import com.ccms.BaseTest;
import com.ccms.entity.ExpenseReimburse;
import com.ccms.service.ExpenseReimburseService;
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
 * ExpenseReimburseController单元测试
 */
class ExpenseReimburseControllerTest extends BaseTest {

    @Mock
    private ExpenseReimburseService expenseReimburseService;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private ExpenseReimburseController expenseReimburseController;

    @BeforeEach
    void setUpForController() {
        super.setUp();
    }

    @Test
    @DisplayName("获取费用报销列表 - 成功")
    void testGetExpenseReimburseList_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Integer page = 1;
        Integer size = 10;
        Long applicantId = 1L;
        Long departmentId = 1L;
        Integer status = 1;

        List<ExpenseReimburse> reimburseList = Arrays.asList(
            createTestExpenseReimburseWithId(1L),
            createTestExpenseReimburseWithId(2L)
        );
        Page<ExpenseReimburse> reimbursePage = new PageImpl<>(reimburseList, PageRequest.of(page - 1, size), reimburseList.size());

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        when(expenseReimburseService.getExpenseReimburseList(any(Pageable.class), eq(applicantId), eq(departmentId), eq(status)))
            .thenReturn(reimbursePage);

        // 执行测试
        ResponseEntity<Page<ExpenseReimburse>> response = expenseReimburseController.getExpenseReimburseList(
            page, size, applicantId, departmentId, status, null, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "expense_reimburse:read");
        verify(expenseReimburseService, times(1)).getExpenseReimburseList(
            any(Pageable.class), eq(applicantId), eq(departmentId), eq(status));
    }

    @Test
    @DisplayName("获取费用报销列表 - 权限不足")
    void testGetExpenseReimburseList_PermissionDenied() {
        // 准备测试数据
        String token = "Bearer invalid_token";

        // Mock权限验证失败
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(false);

        // 执行测试
        ResponseEntity<Page<ExpenseReimburse>> response = expenseReimburseController.getExpenseReimburseList(
            1, 10, null, null, null, null, token);

        // 验证结果
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        // 验证服务层没有被调用
        verify(expenseReimburseService, never()).getExpenseReimburseList(any(Pageable.class), any(), any(), any());
    }

    @Test
    @DisplayName("处理报销支付 - 成功")
    void testProcessPayment_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long reimburseId = 1L;
        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("paymentMethod", "银行转账");
        paymentInfo.put("paymentAccount", "12345678");

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "报销支付处理成功");
        when(expenseReimburseService.processPayment(reimburseId, paymentInfo)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = expenseReimburseController.processPayment(reimburseId, paymentInfo, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("报销支付处理成功", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "expense_reimburse:payment");
        verify(expenseReimburseService, times(1)).processPayment(reimburseId, paymentInfo);
    }

    @Test
    @DisplayName("处理报销支付 - 报销记录不存在")
    void testProcessPayment_NotFound() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long reimburseId = 999L;
        Map<String, Object> paymentInfo = new HashMap<>();

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层抛出异常
        when(expenseReimburseService.processPayment(reimburseId, paymentInfo)).thenThrow(
            new RuntimeException("费用报销记录不存在")
        );

        // 执行测试
        ResponseEntity<Map<String, String>> response = expenseReimburseController.processPayment(reimburseId, paymentInfo, token);

        // 验证结果
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        // 验证Mock调用
        verify(expenseReimburseService, times(1)).processPayment(reimburseId, paymentInfo);
    }

    @Test
    @DisplayName("上传报销凭证 - 成功")
    void testUploadVoucher_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long reimburseId = 1L;
        Map<String, Object> voucherInfo = new HashMap<>();
        voucherInfo.put("fileId", "file123");
        voucherInfo.put("fileName", "receipt.jpg");

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "报销凭证上传成功");
        when(expenseReimburseService.uploadVoucher(reimburseId, voucherInfo)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = expenseReimburseController.uploadVoucher(reimburseId, voucherInfo, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("报销凭证上传成功", response.getBody().get("message"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "expense_reimburse:voucher");
        verify(expenseReimburseService, times(1)).uploadVoucher(reimburseId, voucherInfo);
    }

    @Test
    @DisplayName("上传报销凭证 - 文件格式不支持")
    void testUploadVoucher_InvalidFileFormat() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long reimburseId = 1L;
        Map<String, Object> invalidVoucherInfo = new HashMap<>();
        invalidVoucherInfo.put("fileId", "file123");
        invalidVoucherInfo.put("fileName", "document.exe"); // 不支持的文件格式

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层抛出格式异常
        when(expenseReimburseService.uploadVoucher(reimburseId, invalidVoucherInfo)).thenThrow(
            new RuntimeException("不支持的文件格式")
        );

        // 执行测试
        ResponseEntity<Map<String, String>> response = expenseReimburseController.uploadVoucher(reimburseId, invalidVoucherInfo, token);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("不支持的文件格式"));
        
        // 验证Mock调用
        verify(expenseReimburseService, times(1)).uploadVoucher(reimburseId, invalidVoucherInfo);
    }

    @Test
    @DisplayName("下载报销凭证 - 成功")
    void testDownloadVoucher_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long reimburseId = 1L;

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("message", "凭证下载链接已生成");
        successResponse.put("downloadUrl", "/api/files/temp/abc123");
        when(expenseReimburseService.downloadVoucher(reimburseId)).thenReturn(successResponse);

        // 执行测试
        ResponseEntity<Map<String, String>> response = expenseReimburseController.downloadVoucher(reimburseId, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("凭证下载链接已生成", response.getBody().get("message"));
        assertNotNull(response.getBody().get("downloadUrl"));
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "expense_reimburse:voucher");
        verify(expenseReimburseService, times(1)).downloadVoucher(reimburseId);
    }

    @Test
    @DisplayName("下载报销凭证 - 凭证不存在")
    void testDownloadVoucher_VoucherNotFound() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long reimburseId = 999L;

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层抛出异常
        when(expenseReimburseService.downloadVoucher(reimburseId)).thenThrow(
            new RuntimeException("报销凭证不存在")
        );

        // 执行测试
        ResponseEntity<Map<String, String>> response = expenseReimburseController.downloadVoucher(reimburseId, token);

        // 验证结果
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        // 验证Mock调用
        verify(expenseReimburseService, times(1)).downloadVoucher(reimburseId);
    }

    @Test
    @DisplayName("获取费用报销详情 - 成功")
    void testGetExpenseReimburseDetail_Success() {
        // 准备测试数据
        String token = "Bearer valid_token";
        Long reimburseId = 1L;
        ExpenseReimburse expenseReimburse = createTestExpenseReimburseWithId(reimburseId);

        // Mock权限验证
        when(permissionService.checkPermission(eq(token), anyString())).thenReturn(true);
        
        // Mock服务层返回
        when(expenseReimburseService.getExpenseReimburseDetail(reimburseId)).thenReturn(expenseReimburse);

        // 执行测试
        ResponseEntity<ExpenseReimburse> response = expenseReimburseController.getExpenseReimburseDetail(reimburseId, token);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(reimburseId, response.getBody().getId());
        
        // 验证Mock调用
        verify(permissionService, times(1)).checkPermission(token, "expense_reimburse:read");
        verify(expenseReimburseService, times(1)).getExpenseReimburseDetail(reimburseId);
    }

    /**
     * 创建测试费用报销数据
     */
    private ExpenseReimburse createTestExpenseReimburse() {
        ExpenseReimburse reimburse = new ExpenseReimburse();
        reimburse.setId(1L);
        reimburse.setUserId(1L);
        reimburse.setApplyId(1L);
        reimburse.setAmount(800.0);
        reimburse.setReason("测试费用报销");
        reimburse.setStatus(1);
        reimburse.setVoucherFileId("voucher123");
        return reimburse;
    }

    /**
     * 创建带ID的测试费用报销
     */
    private ExpenseReimburse createTestExpenseReimburseWithId(Long id) {
        ExpenseReimburse expenseReimburse = createTestExpenseReimburse();
        expenseReimburse.setId(id);
        return expenseReimburse;
    }
}