package com.ccms.integration;

import com.ccms.BaseTest;
import com.ccms.entity.budget.BudgetMain;
import com.ccms.entity.expense.ExpenseApplyMain;
import com.ccms.entity.expense.ExpenseApplyDetail;
import com.ccms.service.BudgetService;
import com.ccms.service.ExpenseApplyService;
import com.ccms.service.ApprovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 预算-费用申请集成测试
 * 验证预算控制与费用申请审批的端到端流程
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(locations = "classpath:application-test.yml")
@DisplayName("预算与费用申请集成测试")
class BudgetExpenseIntegrationTest extends BaseTest {

    @Autowired
    private BudgetService budgetService;
    
    @Autowired
    private ExpenseApplyService expenseApplyService;
    
    @Autowired
    private ApprovalService approvalService;

    private BudgetMain testBudget;
    private ExpenseApplyMain testExpenseApply;
    private ExpenseApplyDetail testExpenseDetail;

    @BeforeEach
    public void setUp() {
        super.setUp();
        
        // 创建测试预算数据
        testBudget = new BudgetMain();
        testBudget.setBudgetYear(2024);
        testBudget.setDepartment("技术部");
        testBudget.setTotalAmount(new BigDecimal("100000.00"));
        testBudget.setUsedAmount(BigDecimal.ZERO);
        testBudget.setAvailableAmount(new BigDecimal("100000.00"));
        testBudget.setStatus("ACTIVE");
        testBudget.setPlanName("2024年度技术部预算");
        testBudget.setCreateTime(LocalDateTime.now());
        testBudget.setUpdateTime(LocalDateTime.now());

        // 创建测试费用申请数据
        testExpenseApply = new ExpenseApplyMain();
        testExpenseApply.setTitle("差旅费申请");
        testExpenseApply.setDepartment("技术部");
        testExpenseApply.setApplicant("张三");
        testExpenseApply.setTotalAmount(new BigDecimal("5000.00"));
        testExpenseApply.setStatus("DRAFT");
        testExpenseApply.setApplyTime(LocalDateTime.now());
        testExpenseApply.setCreateTime(LocalDateTime.now());
        testExpenseApply.setUpdateTime(LocalDateTime.now());

        // 创建测试费用明细数据
        testExpenseDetail = new ExpenseApplyDetail();
        testExpenseDetail.setFeeType("差旅费");
        testExpenseDetail.setAmount(new BigDecimal("5000.00"));
        testExpenseDetail.setRemarks("往返车票费用");
        testExpenseDetail.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("预算控制与费用申请完整流程测试")
    void budgetControlAndExpenseApplyWorkflow() {
        // 阶段1：预算检查
        boolean budgetCheck = budgetService.checkBudgetLimit("技术部", 2024, new BigDecimal("5000.00"));
        assertThat(budgetCheck).isTrue();

        // 阶段2：创建费用申请
        ExpenseApplyMain createdApply = expenseApplyService.createExpenseApply(
            testExpenseApply, 
            Arrays.asList(testExpenseDetail)
        );
        assertThat(createdApply).isNotNull();
        assertThat(createdApply.getApplyNo()).isNotNull();
        assertThat(createdApply.getStatus()).isEqualTo("DRAFT");

        // 阶段3：提交审批
        boolean submissionResult = expenseApplyService.submitForApproval(createdApply.getId());
        assertThat(submissionResult).isTrue();

        // 验证申请状态更新
        ExpenseApplyMain submittedApply = expenseApplyService.getExpenseApplyById(createdApply.getId())
            .orElseThrow(() -> new RuntimeException("申请不存在"));
        assertThat(submittedApply.getStatus()).isEqualTo("PENDING");

        // TODO：阶段4：审批流程（需要审批服务集成）
        // ApprovalInstance approvalInstance = approvalService.createApprovalInstance(
        //     createdApply.getApplyNo(),
        //     "EXPENSE_APPLY",
        //     "张三"
        // );
        // assertThat(approvalInstance).isNotNull();

        logger.info("预算控制与费用申请集成测试完成");
    }

    @Test
    @DisplayName("预算不足时的费用申请拦截测试")
    void budgetInsufficientAndExpenseApplyBlock() {
        // 设置较小的可用预算
        testBudget.setAvailableAmount(new BigDecimal("1000.00"));

        // 预算检查应该失败
        boolean budgetCheck = budgetService.checkBudgetLimit("技术部", 2024, new BigDecimal("5000.00"));
        assertThat(budgetCheck).isFalse();

        // 尝试创建费用申请应该失败
        try {
            expenseApplyService.createExpenseApply(testExpenseApply, Arrays.asList(testExpenseDetail));
            // 如果到达这里，说明异常没有被正确抛出
            assertThat(false).isTrue();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("预算");
        }

        logger.info("预算不足拦截测试完成");
    }

    @Test
    @DisplayName("预算扣减与恢复测试")
    void budgetDeductionAndRestorationTest() {
        // 初始预算检查
        boolean initialCheck = budgetService.checkBudgetLimit("技术部", 2024, new BigDecimal("5000.00"));
        assertThat(initialCheck).isTrue();

        // 扣减预算
        boolean deductionResult = budgetService.deductBudget(testBudget.getId(), new BigDecimal("5000.00"));
        assertThat(deductionResult).isTrue();

        // 验证预算已更新
        BudgetMain updatedBudget = budgetService.getBudgetMainById(testBudget.getId())
            .orElseThrow(() -> new RuntimeException("预算不存在"));
        assertThat(updatedBudget.getUsedAmount()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(updatedBudget.getAvailableAmount()).isEqualTo(new BigDecimal("95000.00"));

        // 返还预算
        boolean returnResult = budgetService.returnBudget(testBudget.getId(), new BigDecimal("3000.00"));
        assertThat(returnResult).isTrue();

        // 验证预算恢复
        BudgetMain restoredBudget = budgetService.getBudgetMainById(testBudget.getId())
            .orElseThrow(() -> new RuntimeException("预算不存在"));
        assertThat(restoredBudget.getUsedAmount()).isEqualTo(new BigDecimal("2000.00"));
        assertThat(restoredBudget.getAvailableAmount()).isEqualTo(new BigDecimal("98000.00"));

        logger.info("预算扣减与恢复测试完成");
    }

    @Test
    @DisplayName("费用申请状态流转测试")
    void expenseApplyStatusTransitionTest() {
        // 创建费用申请
        ExpenseApplyMain createdApply = expenseApplyService.createExpenseApply(
            testExpenseApply, 
            Arrays.asList(testExpenseDetail)
        );
        
        // 状态：DRAFT → PENDING
        boolean submitResult = expenseApplyService.submitForApproval(createdApply.getId());
        assertThat(submitResult).isTrue();

        ExpenseApplyMain submittedApply = expenseApplyService.getExpenseApplyById(createdApply.getId())
            .orElseThrow(() -> new RuntimeException("申请不存在"));
        assertThat(submittedApply.getStatus()).isEqualTo("PENDING");

        // 状态：PENDING → APPROVED
        boolean approveResult = expenseApplyService.updateExpenseApplyStatus(createdApply.getId(), "APPROVED");
        assertThat(approveResult).isTrue();

        ExpenseApplyMain approvedApply = expenseApplyService.getExpenseApplyById(createdApply.getId())
            .orElseThrow(() -> new RuntimeException("申请不存在"));
        assertThat(approvedApply.getStatus()).isEqualTo("APPROVED");

        // 状态：APPROVED → REJECTED
        boolean rejectResult = expenseApplyService.updateExpenseApplyStatus(createdApply.getId(), "REJECTED");
        assertThat(rejectResult).isTrue();

        ExpenseApplyMain rejectedApply = expenseApplyService.getExpenseApplyById(createdApply.getId())
            .orElseThrow(() -> new RuntimeException("申请不存在"));
        assertThat(rejectedApply.getStatus()).isEqualTo("REJECTED");

        logger.info("费用申请状态流转测试完成");
    }

    @Test
    @DisplayName("费用申请明细管理测试")
    void expenseApplyDetailManagementTest() {
        // 创建费用申请
        ExpenseApplyMain createdApply = expenseApplyService.createExpenseApply(
            testExpenseApply, 
            Arrays.asList(testExpenseDetail)
        );

        // 获取申请明细
        assertThat(createdApply.getTotalAmount()).isEqualTo(new BigDecimal("5000.00"));

        // 可以添加更多的明细管理测试
        // 如：明细修改、明细删除等操作

        logger.info("费用申请明细管理测试完成");
    }
}