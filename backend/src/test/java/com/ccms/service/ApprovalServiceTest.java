package com.ccms.service;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.BusinessTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 审批服务单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
class ApprovalServiceTest {

    @Autowired
    private ApprovalService approvalService;

    @Test
    void testCreateApprovalInstance() {
        // 测试创建审批实例
        ApprovalInstance instance = approvalService.createApprovalInstance(
                BusinessTypeEnum.EXPENSE_REIMBURSE,
                "TEST_001",
                1L,
                "测试审批实例",
                new BigDecimal("1000.00"),
                1L
        );

        assertNotNull(instance);
        assertEquals(BusinessTypeEnum.EXPENSE_REIMBURSE.name(), instance.getBusinessType());
        assertEquals("TEST_001", instance.getBusinessId());
        assertEquals(1L, instance.getApplicantId());
        assertEquals(ApprovalStatus.DRAFT, instance.getStatus());
    }

    @Test
    void testSubmitApproval() {
        // 测试提交审批
        ApprovalInstance instance = approvalService.createApprovalInstance(
                BusinessTypeEnum.EXPENSE_REIMBURSE,
                "TEST_002",
                1L,
                "测试提交审批",
                new BigDecimal("500.00"),
                1L
        );

        approvalService.submitApproval(instance.getId());

        ApprovalInstance updatedInstance = approvalService.findById(instance.getId());
        assertEquals(ApprovalStatus.APPROVING, updatedInstance.getStatus());
    }

    @Test
    void testApproveAction() {
        // 测试审批通过操作
        ApprovalInstance instance = approvalService.createApprovalInstance(
                BusinessTypeEnum.EXPENSE_REIMBURSE,
                "TEST_003",
                1L,
                "测试审批通过",
                new BigDecimal("800.00"),
                1L
        );

        approvalService.submitApproval(instance.getId());
        
        // 模拟审批人操作
        ApprovalRecord record = approvalService.performApprovalAction(
                instance.getId(),
                2L, // 审批人ID
                ApprovalAction.APPROVE,
                "同意审批"
        );

        assertNotNull(record);
        assertEquals(ApprovalAction.APPROVE, record.getAction());
        assertEquals("同意审批", record.getRemarks());
    }

    @Test
    void testRejectAction() {
        // 测试审批驳回操作
        ApprovalInstance instance = approvalService.createApprovalInstance(
                BusinessTypeEnum.EXPENSE_REIMBURSE,
                "TEST_004",
                1L,
                "测试审批驳回",
                new BigDecimal("300.00"),
                1L
        );

        approvalService.submitApproval(instance.getId());
        
        ApprovalRecord record = approvalService.performApprovalAction(
                instance.getId(),
                2L,
                ApprovalAction.REJECT,
                "申请理由不充分"
        );

        assertNotNull(record);
        assertEquals(ApprovalAction.REJECT, record.getAction());
        assertEquals("申请理由不充分", record.getRemarks());
        
        ApprovalInstance finalInstance = approvalService.findById(instance.getId());
        assertEquals(ApprovalStatus.REJECTED, finalInstance.getStatus());
    }

    @Test
    void testCancelAction() {
        // 测试取消操作
        ApprovalInstance instance = approvalService.createApprovalInstance(
                BusinessTypeEnum.EXPENSE_REIMBURSE,
                "TEST_005",
                1L,
                "测试审批取消",
                new BigDecimal("200.00"),
                1L
        );

        approvalService.cancelApproval(instance.getId(), 1L, "不想申请了");

        ApprovalInstance canceledInstance = approvalService.findById(instance.getId());
        assertEquals(ApprovalStatus.CANCELED, canceledInstance.getStatus());
    }

    @Test
    void testWithdrawAction() {
        // 测试撤回操作
        ApprovalInstance instance = approvalService.createApprovalInstance(
                BusinessTypeEnum.EXPENSE_REIMBURSE,
                "TEST_006",
                1L,
                "测试审批撤回",
                new BigDecimal("700.00"),
                1L
        );

        approvalService.submitApproval(instance.getId());
        approvalService.withdrawApproval(instance.getId(), 1L, "需要修改申请内容");

        ApprovalInstance withdrawnInstance = approvalService.findById(instance.getId());
        assertEquals(ApprovalStatus.WITHDRAWN, withdrawnInstance.getStatus());
    }

    @Test
    void testFindByBusiness() {
        // 测试按业务查询
        String businessId = "TEST_007";
        
        ApprovalInstance instance = approvalService.createApprovalInstance(
                BusinessTypeEnum.EXPENSE_REIMBURSE,
                businessId,
                1L,
                "测试业务查询",
                new BigDecimal("900.00"),
                1L
        );

        ApprovalInstance foundInstance = approvalService.findByBusiness(
                BusinessTypeEnum.EXPENSE_REIMBURSE, 
                businessId
        );

        assertNotNull(foundInstance);
        assertEquals(instance.getId(), foundInstance.getId());
        assertEquals(businessId, foundInstance.getBusinessId());
    }

    @Test
    void testApprovalHistory() {
        // 测试审批历史查询
        ApprovalInstance instance = approvalService.createApprovalInstance(
                BusinessTypeEnum.EXPENSE_REIMBURSE,
                "TEST_008",
                1L,
                "测试审批历史",
                new BigDecimal("1200.00"),
                1L
        );

        approvalService.submitApproval(instance.getId());
        approvalService.performApprovalAction(instance.getId(), 2L, ApprovalAction.APPROVE, "同意");

        var history = approvalService.getApprovalHistory(instance.getId());
        
        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertEquals(2, history.size()); // 提交记录 + 审批记录
    }
}