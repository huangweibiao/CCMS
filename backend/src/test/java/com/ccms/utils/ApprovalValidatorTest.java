package com.ccms.utils;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.BusinessTypeEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 审批验证工具类单元测试
 */
class ApprovalValidatorTest {

    @Test
    void testValidateApprovalRequest_Success() {
        // 测试合法审批请求
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
        request.setBusinessId("TEST_001");
        request.setApplicantId(1L);
        request.setTitle("测试审批请求");
        request.setAmount(new BigDecimal("1000.00"));

        // 应该不会抛出异常
        assertDoesNotThrow(() -> ApprovalValidator.validateApprovalRequest(request));
    }

    @Test
    void testValidateApprovalRequest_NullRequest() {
        // 测试空请求
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApprovalValidator.validateApprovalRequest(null)
        );
        assertEquals("审批请求不能为空", exception.getMessage());
    }

    @Test
    void testValidateApprovalRequest_MissingFields() {
        // 测试缺失必填字段
        ApprovalRequest request = new ApprovalRequest();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApprovalValidator.validateApprovalRequest(request)
        );
        // 由于字段级验证，这里可能会抛出多个验证错误
        assertTrue(exception.getMessage().contains("不能为空"));
    }

    @Test
    void testValidateApprovalRequest_NegativeAmount() {
        // 测试负金额
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
        request.setBusinessId("TEST_002");
        request.setApplicantId(1L);
        request.setTitle("测试负金额");
        request.setAmount(new BigDecimal("-100.00"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApprovalValidator.validateApprovalRequest(request)
        );
        assertEquals("金额不能为负数", exception.getMessage());
    }

    @Test
    void testIsValidStatusTransition() {
        // 测试状态转换验证
        assertTrue(ApprovalValidator.isValidStatusTransition(
                ApprovalStatus.DRAFT, ApprovalStatus.APPROVING, ApprovalAction.SUBMIT));
        
        assertTrue(ApprovalValidator.isValidStatusTransition(
                ApprovalStatus.APPROVING, ApprovalStatus.APPROVED, ApprovalAction.APPROVE));
        
        assertTrue(ApprovalValidator.isValidStatusTransition(
                ApprovalStatus.APPROVING, ApprovalStatus.REJECTED, ApprovalAction.REJECT));
        
        // 无效转换
        assertFalse(ApprovalValidator.isValidStatusTransition(
                ApprovalStatus.APPROVED, ApprovalStatus.APPROVING, ApprovalAction.SUBMIT));
    }

    @Test
    void testValidateStatusTransition_Success() {
        // 测试合法的状态转换
        assertDoesNotThrow(() -> ApprovalValidator.validateStatusTransition(
                ApprovalStatus.DRAFT, ApprovalStatus.APPROVING, ApprovalAction.SUBMIT));
    }

    @Test
    void testValidateStatusTransition_Invalid() {
        // 测试不合法的状态转换
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApprovalValidator.validateStatusTransition(
                        ApprovalStatus.APPROVED, ApprovalStatus.APPROVING, ApprovalAction.SUBMIT)
        );
        assertTrue(exception.getMessage().contains("状态转换不合法"));
    }

    @Test
    void testGetAllowedActions() {
        // 测试获取允许的操作
        List<ApprovalAction> allowedActions = ApprovalValidator.getAllowedActions(ApprovalStatus.DRAFT);
        
        assertNotNull(allowedActions);
        assertTrue(allowedActions.contains(ApprovalAction.SUBMIT));
        assertTrue(allowedActions.contains(ApprovalAction.CANCEL));
    }

    @Test
    void testGetTargetStatuses() {
        // 测试获取目标状态
        List<ApprovalStatus> targetStatuses = ApprovalValidator.getTargetStatuses(ApprovalStatus.DRAFT);
        
        assertNotNull(targetStatuses);
        assertTrue(targetStatuses.contains(ApprovalStatus.APPROVING));
        assertTrue(targetStatuses.contains(ApprovalStatus.CANCELED));
    }

    @Test
    void testIsActionAllowed() {
        // 测试检查操作是否允许
        assertTrue(ApprovalValidator.isActionAllowed(ApprovalStatus.DRAFT, ApprovalAction.SUBMIT));
        assertTrue(ApprovalValidator.isActionAllowed(ApprovalStatus.DRAFT, ApprovalAction.CANCEL));
        
        assertFalse(ApprovalValidator.isActionAllowed(ApprovalStatus.DRAFT, ApprovalAction.APPROVE));
    }

    @Test
    void testValidateBusinessType_Success() {
        // 测试合法的业务类型
        assertDoesNotThrow(() -> ApprovalValidator.validateBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE));
        assertDoesNotThrow(() -> ApprovalValidator.validateBusinessType(BusinessTypeEnum.LOAN));
        assertDoesNotThrow(() -> ApprovalValidator.validateBusinessType(BusinessTypeEnum.EXPENSE_APPLY));
    }

    @Test
    void testValidateBusinessType_Invalid() {
        // 测试不支持的业务类型
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApprovalValidator.validateBusinessType(null)
        );
        assertEquals("业务类型不能为空", exception.getMessage());
    }

    @Test
    void testValidateFlowConfig_Success() {
        // 测试合法的流程配置
        ApprovalFlowConfig config = new ApprovalFlowConfig();
        config.setBusinessType("EXPENSE_REIMBURSE");
        config.setFlowName("费用报销审批流程");
        config.setMinAmount(new BigDecimal("0"));
        config.setMaxAmount(new BigDecimal("10000"));

        assertDoesNotThrow(() -> ApprovalValidator.validateFlowConfig(config));
    }

    @Test
    void testValidateNodeOrder_Success() {
        // 测试合法的节点顺序
        ApprovalNode node1 = new ApprovalNode();
        node1.setNodeOrder(1);
        ApprovalNode node2 = new ApprovalNode();
        node2.setNodeOrder(2);

        assertDoesNotThrow(() -> ApprovalValidator.validateNodeOrder(Arrays.asList(node1, node2)));
    }

    @Test
    void testValidateNodeOrder_Invalid() {
        // 测试非连续的节点顺序
        ApprovalNode node1 = new ApprovalNode();
        node1.setNodeOrder(1);
        ApprovalNode node2 = new ApprovalNode();
        node2.setNodeOrder(3); // 缺少顺序2

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ApprovalValidator.validateNodeOrder(Arrays.asList(node1, node2))
        );
        assertTrue(exception.getMessage().contains("节点顺序不连续"));
    }

    @Test
    void testIsAmountInRange() {
        // 测试金额范围验证
        BigDecimal amount = new BigDecimal("500.00");
        BigDecimal minAmount = new BigDecimal("100.00");
        BigDecimal maxAmount = new BigDecimal("1000.00");

        assertTrue(ApprovalValidator.isAmountInRange(amount, minAmount, maxAmount));
        assertFalse(ApprovalValidator.isAmountInRange(new BigDecimal("50.00"), minAmount, maxAmount));
        assertFalse(ApprovalValidator.isAmountInRange(new BigDecimal("1500.00"), minAmount, maxAmount));
    }
}