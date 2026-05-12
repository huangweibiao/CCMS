package com.ccms.factory;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 审批服务工厂单元测试
 */
@ExtendWith(MockitoExtension.class)
class ApprovalServiceFactoryTest {

    @Mock
    private ApprovalService approvalService;

    @Mock
    private ExpenseApplyService expenseApplyService;

    @Mock
    private ExpenseReimburseService expenseReimburseService;

    @Mock
    private LoanService loanService;

    private ApprovalServiceFactory approvalServiceFactory;

    @BeforeEach
    void setUp() {
        approvalServiceFactory = new ApprovalServiceFactory(
                approvalService,
                expenseApplyService,
                expenseReimburseService,
                loanService
        );
    }

    @Test
    void testSubmitBusinessApproval_Success() {
        // 准备测试数据
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
        request.setBusinessId("TEST_001");
        request.setApplicantId(1L);
        request.setTitle("费用报销审批");
        request.setAmount(new BigDecimal("500.00"));
        request.setDepartId(1L);

        ApprovalInstance instance = new ApprovalInstance();
        instance.setId(1L);
        instance.setBusinessType("EXPENSE_REIMBURSE");
        instance.setStatus(ApprovalStatus.DRAFT);

        // 模拟服务调用
        when(approvalService.createApprovalInstance(
                eq(BusinessTypeEnum.EXPENSE_REIMBURSE),
                eq("TEST_001"),
                eq(1L),
                eq("费用报销审批"),
                eq(new BigDecimal("500.00")),
                eq(1L)
        )).thenReturn(instance);

        when(approvalService.submitApproval(1L)).thenReturn(instance);

        // 执行测试
        ApprovalInstance result = approvalServiceFactory.submitBusinessApproval(
                BusinessTypeEnum.EXPENSE_REIMBURSE,
                "TEST_001",
                1L,
                "费用报销审批",
                Map.of("amount", new BigDecimal("500.00"), "departId", 1L)
        );

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("EXPENSE_REIMBURSE", result.getBusinessType());
        
        // 验证服务调用
        verify(approvalService, times(1)).createApprovalInstance(
                BusinessTypeEnum.EXPENSE_REIMBURSE, "TEST_001", 1L, "费用报销审批", 
                new BigDecimal("500.00"), 1L
        );
        verify(approvalService, times(1)).submitApproval(1L);
    }

    @Test
    void testSubmitBusinessApproval_WithMissingAmount() {
        // 测试缺失金额的情况
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessType(BusinessTypeEnum.LOAN);
        request.setBusinessId("TEST_002");
        request.setApplicantId(2L);
        request.setTitle("借款申请审批");

        ApprovalInstance instance = new ApprovalInstance();
        instance.setId(2L);
        instance.setBusinessType("LOAN");

        // 模拟服务调用
        when(approvalService.createApprovalInstance(
                eq(BusinessTypeEnum.LOAN),
                eq("TEST_002"),
                eq(2L),
                eq("借款申请审批"),
                isNull(),
                eq(2L)
        )).thenReturn(instance);

        when(approvalService.submitApproval(2L)).thenReturn(instance);

        // 执行测试（context中不包含amount）
        ApprovalInstance result = approvalServiceFactory.submitBusinessApproval(
                BusinessTypeEnum.LOAN,
                "TEST_002",
                2L,
                "借款申请审批",
                Map.of("departId", 2L)
        );

        assertNotNull(result);
        assertEquals(2L, result.getId());
        
        verify(approvalService).createApprovalInstance(
                any(BusinessTypeEnum.class), anyString(), anyLong(), anyString(), 
                isNull(), anyLong()
        );
    }

    @Test
    void testProcessApprovalResult_Approved() {
        // 测试审批通过结果处理
        ApprovalInstance instance = new ApprovalInstance();
        instance.setId(1L);
        instance.setBusinessType("EXPENSE_REIMBURSE");
        instance.setBusinessId("TEST_001");
        instance.setStatus(ApprovalStatus.APPROVED);

        // 模拟服务调用
        doNothing().when(expenseReimburseService).updateApprovalResult("TEST_001", ApprovalStatus.APPROVED);

        // 执行测试
        boolean result = approvalServiceFactory.processApprovalResult(instance);

        // 验证结果
        assertTrue(result);
        
        // 验证服务调用
        verify(expenseReimburseService, times(1)).updateApprovalResult("TEST_001", ApprovalStatus.APPROVED);
        verify(expenseApplyService, never()).updateApprovalResult(anyString(), any());
        verify(loanService, never()).updateApprovalResult(anyString(), any());
    }

    @Test
    void testProcessApprovalResult_Rejected() {
        // 测试审批驳回结果处理
        ApprovalInstance instance = new ApprovalInstance();
        instance.setId(2L);
        instance.setBusinessType("LOAN");
        instance.setBusinessId("TEST_002");
        instance.setStatus(ApprovalStatus.REJECTED);

        // 模拟服务调用
        doNothing().when(loanService).updateApprovalResult("TEST_002", ApprovalStatus.REJECTED);

        // 执行测试
        boolean result = approvalServiceFactory.processApprovalResult(instance);

        // 验证结果
        assertTrue(result);
        
        // 验证服务调用
        verify(loanService, times(1)).updateApprovalResult("TEST_002", ApprovalStatus.REJECTED);
        verify(expenseApplyService, never()).updateApprovalResult(anyString(), any());
        verify(expenseReimburseService, never()).updateApprovalResult(anyString(), any());
    }

    @Test
    void testProcessApprovalResult_UnknownBusinessType() {
        // 测试未知业务类型
        ApprovalInstance instance = new ApprovalInstance();
        instance.setId(3L);
        instance.setBusinessType("UNKNOWN_TYPE");
        instance.setBusinessId("TEST_003");
        instance.setStatus(ApprovalStatus.APPROVED);

        // 执行测试
        boolean result = approvalServiceFactory.processApprovalResult(instance);

        // 验证结果（未知业务类型应该返回false）
        assertFalse(result);
        
        // 验证没有调用任何业务服务
        verify(expenseApplyService, never()).updateApprovalResult(anyString(), any());
        verify(expenseReimburseService, never()).updateApprovalResult(anyString(), any());
        verify(loanService, never()).updateApprovalResult(anyString(), any());
    }

    @Test
    void testCheckBusinessStatus() {
        // 测试检查业务状态
        when(expenseReimburseService.checkApprovalStatus("TEST_001"))
                .thenReturn(ApprovalStatus.APPROVED);

        ApprovalStatus status = approvalServiceFactory.checkBusinessStatus(
                BusinessTypeEnum.EXPENSE_REIMBURSE, "TEST_001");

        assertEquals(ApprovalStatus.APPROVED, status);
        
        verify(expenseReimburseService, times(1)).checkApprovalStatus("TEST_001");
    }

    @Test
    void testCheckBusinessStatus_UnknownBusinessType() {
        // 测试未知业务类型的状态检查
        // 使用一个未在工厂中映射的业务类型
        AssertionError error = assertThrows(
                AssertionError.class,
                () -> approvalServiceFactory.checkBusinessStatus(
                        BusinessTypeEnum.valueOf("EXPENSE_APPLY"), "TEST_005")
                )
        );

        // 验证没有调用任何业务服务
        verify(expenseApplyService, never()).checkApprovalStatus(anyString());
        verify(expenseReimburseService, never()).checkApprovalStatus(anyString());
        verify(loanService, never()).checkApprovalStatus(anyString());
    }

    @Test
    void testGetServiceByBusinessType() {
        // 测试根据业务类型获取服务
        Object expenseService = approvalServiceFactory.getServiceByBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
        Object loanService = approvalServiceFactory.getServiceByBusinessType(BusinessTypeEnum.LOAN);

        assertEquals(expenseReimburseService, expenseService);
        assertEquals(this.loanService, loanService);
    }

    @Test
    void testGetServiceByBusinessType_UnsupportedType() {
        // 测试不支持的未知业务类型
        // 使用一个未在工厂中映射的业务类型
        Object unknownService = approvalServiceFactory.getServiceByBusinessType(
                BusinessTypeEnum.valueOf("EXPENSE_APPLY")
        );

        assertNull(unknownService);
    }

    @Test
    void testIsBusinessTypeSupported() {
        // 测试业务类型支持检查
        assertTrue(approvalServiceFactory.isBusinessTypeSupported(BusinessTypeEnum.EXPENSE_REIMBURSE));
        assertTrue(approvalServiceFactory.isBusinessTypeSupported(BusinessTypeEnum.LOAN));
        
        // 使用一个未在工厂中映射的业务类型
        assertFalse(approvalServiceFactory.isBusinessTypeSupported(
                BusinessTypeEnum.valueOf("EXPENSE_APPLY")
        ));
    }

    @Test
    void testGetSupportedBusinessTypes() {
        // 测试获取支持的业务类型列表
        var supportedTypes = approvalServiceFactory.getSupportedBusinessTypes();

        assertNotNull(supportedTypes);
        assertTrue(supportedTypes.contains(BusinessTypeEnum.EXPENSE_REIMBURSE));
        assertTrue(supportedTypes.contains(BusinessTypeEnum.LOAN));
        
        // 验证EXPENSE_APPLY不被支持
        assertFalse(supportedTypes.contains(BusinessTypeEnum.EXPENSE_APPLY));
    }
}