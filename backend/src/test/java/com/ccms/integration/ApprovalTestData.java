package com.ccms.integration;

import com.ccms.dto.approval.ExpenseApprovalDto;
import com.ccms.entity.approval.*;
import com.ccms.entity.user.User;
import com.ccms.enums.approval.ApprovalStatus;
import com.ccms.enums.approval.ApprovalType;
import com.ccms.repository.approval.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 审批流程测试数据生成器
 * 为集成测试和演示提供预设的测试数据
 */
@Component
@Profile("test")
public class ApprovalTestData {

    private final ApprovalFlowConfigRepository flowConfigRepository;
    private final ApprovalInstanceRepository instanceRepository;
    private final ApprovalNodeRepository nodeRepository;
    private final ApprovalRecordRepository recordRepository;
    private final ApprovalAuditLogRepository auditLogRepository;

    public ApprovalTestData(ApprovalFlowConfigRepository flowConfigRepository,
                          ApprovalInstanceRepository instanceRepository,
                          ApprovalNodeRepository nodeRepository,
                          ApprovalRecordRepository recordRepository,
                          ApprovalAuditLogRepository auditLogRepository) {
        this.flowConfigRepository = flowConfigRepository;
        this.instanceRepository = instanceRepository;
        this.nodeRepository = nodeRepository;
        this.recordRepository = recordRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 创建完整的测试数据集合
     */
    public void createCompleteTestData() {
        // 创建测试用户
        List<User> users = createTestUsers();
        
        // 创建各类审批流程配置
        createExpenseFlowConfigs(users);
        createLoanFlowConfigs(users);
        createLeaveFlowConfigs(users);
        createPurchaseFlowConfigs(users);
        
        // 创建不同状态的审批实例
        createDraftInstances(users);
        createApprovingInstances(users);
        createCompletedInstances(users);
        createRejectedInstances(users);
        createWithdrawnInstances(users);
        
        // 创建审批记录和审计日志
        createApprovalRecords(users);
        createAuditLogs();
    }

    /**
     * 创建报销相关的测试数据
     */
    public void createExpenseTestData() {
        List<User> users = createTestUsers();
        createExpenseFlowConfigs(users);
        createExpenseTestInstances(users);
    }

    /**
     * 创建借款相关的测试数据
     */
    public void createLoanTestData() {
        List<User> users = createTestUsers();
        createLoanFlowConfigs(users);
        createLoanTestInstances(users);
    }

    /**
     * 创建测试用户
     */
    private List<User> createTestUsers() {
        User employee1 = new User();
        employee1.setId(1001L);
        employee1.setUsername("employee1");
        employee1.setRealName("张三");
        employee1.setDepartmentId(101L);
        employee1.setEmail("zhangsan@company.com");

        User employee2 = new User();
        employee2.setId(1002L);
        employee2.setUsername("employee2");
        employee2.setRealName("李四");
        employee2.setDepartmentId(102L);
        employee2.setEmail("lisi@company.com");

        User manager1 = new User();
        manager1.setId(2001L);
        manager1.setUsername("manager1");
        manager1.setRealName("王经理");
        manager1.setDepartmentId(101L);
        manager1.setEmail("wang@company.com");

        User manager2 = new User();
        manager2.setId(2002L);
        manager2.setUsername("manager2");
        manager2.setRealName("李总监");
        manager2.setDepartmentId(102L);
        manager2.setEmail("limanager@company.com");

        User finance1 = new User();
        finance1.setId(3001L);
        finance1.setUsername("finance1");
        finance1.setRealName("赵财务");
        finance1.setDepartmentId(201L);
        finance1.setEmail("zhao@company.com");

        User finance2 = new User();
        finance2.setId(3002L);
        finance2.setUsername("finance2");
        finance2.setRealName("钱财务");
        finance2.setDepartmentId(201L);
        finance2.setEmail("qian@company.com");

        return Arrays.asList(employee1, employee2, manager1, manager2, finance1, finance2);
    }

    /**
     * 创建报销审批流程配置
     */
    private void createExpenseFlowConfigs(List<User> users) {
        User manager1 = users.get(2);
        User manager2 = users.get(3);
        User finance1 = users.get(4);

        // 小额报销审批流程（直接审批）
        ApprovalFlowConfig smallExpenseFlow = new ApprovalFlowConfig();
        smallExpenseFlow.setFlowName("小额报销审批流程");
        smallExpenseFlow.setBusinessType("EXPENSE_APPROVAL");
        smallExpenseFlow.setApprovalType(ApprovalType.SEQUENTIAL);
        smallExpenseFlow.setCategory("报销");
        smallExpenseFlow.setMinAmount(BigDecimal.ZERO);
        smallExpenseFlow.setMaxAmount(new BigDecimal("500"));
        smallExpenseFlow.setDescription("小额日常报销，一级审批");
        smallExpenseFlow.setActive(true);
        flowConfigRepository.save(smallExpenseFlow);

        // 大额报销审批流程（多级审批）
        ApprovalFlowConfig largeExpenseFlow = new ApprovalFlowConfig();
        largeExpenseFlow.setFlowName("大额报销审批流程");
        largeExpenseFlow.setBusinessType("EXPENSE_APPROVAL");
        largeExpenseFlow.setApprovalType(ApprovalType.SEQUENTIAL);
        largeExpenseFlow.setCategory("报销");
        largeExpenseFlow.setMinAmount(new BigDecimal("500"));
        largeExpenseFlow.setMaxAmount(new BigDecimal("5000"));
        largeExpenseFlow.setDescription("大额报销，需要主管和财务双重审批");
        largeExpenseFlow.setActive(true);
        flowConfigRepository.save(largeExpenseFlow);

        // 创建审批节点
        // 小额流程 - 直接审批节点
        ApprovalNode smallNode = new ApprovalNode();
        smallNode.setFlowConfigId(smallExpenseFlow.getId());
        smallNode.setNodeName("直接审批");
        smallNode.setNodeType("APPROVAL");
        smallNode.setApproverIds(manager1.getId().toString());
        smallNode.setApprovalStrategy("OR");
        smallNode.setNodeOrder(1);
        nodeRepository.save(smallNode);

        // 大额流程 - 主管审批节点
        ApprovalNode managerNode = new ApprovalNode();
        managerNode.setFlowConfigId(largeExpenseFlow.getId());
        managerNode.setNodeName("主管审批");
        managerNode.setNodeType("APPROVAL");
        managerNode.setApproverIds(manager1.getId() + "," + manager2.getId());
        managerNode.setApprovalStrategy("OR");
        managerNode.setNodeOrder(1);
        managerNode.setConditions("amount >= 500 && amount < 5000");
        nodeRepository.save(managerNode);

        // 大额流程 - 财务审批节点
        ApprovalNode financeNode = new ApprovalNode();
        financeNode.setFlowConfigId(largeExpenseFlow.getId());
        financeNode.setNodeName("财务审批");
        financeNode.setNodeType("APPROVAL");
        financeNode.setApproverIds(finance1.getId().toString());
        financeNode.setApprovalStrategy("AND");
        financeNode.setNodeOrder(2);
        financeNode.setConditions("amount >= 2000 && amount < 5000");
        nodeRepository.save(financeNode);
    }

    /**
     * 创建借款审批流程配置
     */
    private void createLoanFlowConfigs(List<User> users) {
        User manager1 = users.get(2);
        User manager2 = users.get(3);
        User finance1 = users.get(4);

        // 小额借款审批流程
        ApprovalFlowConfig smallLoanFlow = new ApprovalFlowConfig();
        smallLoanFlow.setFlowName("小额借款审批流程");
        smallLoanFlow.setBusinessType("LOAN_APPROVAL");
        smallLoanFlow.setApprovalType(ApprovalType.SEQUENTIAL);
        smallLoanFlow.setCategory("借款");
        smallLoanFlow.setMinAmount(BigDecimal.ZERO);
        smallLoanFlow.setMaxAmount(new BigDecimal("1000"));
        smallLoanFlow.setDescription("小额借款，一级审批");
        smallLoanFlow.setActive(true);
        flowConfigRepository.save(smallLoanFlow);

        // 大额借款审批流程
        ApprovalFlowConfig largeLoanFlow = new ApprovalFlowConfig();
        largeLoanFlow.setFlowName("大额借款审批流程");
        largeLoanFlow.setBusinessType("LOAN_APPROVAL");
        largeLoanFlow.setApprovalType(ApprovalType.SEQUENTIAL);
        largeLoanFlow.setCategory("借款");
        largeLoanFlow.setMinAmount(new BigDecimal("1000"));
        largeLoanFlow.setMaxAmount(new BigDecimal("10000"));
        largeLoanFlow.setDescription("大额借款，需要多重审批");
        largeLoanFlow.setActive(true);
        flowConfigRepository.save(largeLoanFlow);

        // 创建审批节点
        ApprovalNode loanNode1 = new ApprovalNode();
        loanNode1.setFlowConfigId(smallLoanFlow.getId());
        loanNode1.setNodeName("主管审批");
        loanNode1.setNodeType("APPROVAL");
        loanNode1.setApproverIds(manager1.getId().toString());
        loanNode1.setApprovalStrategy("OR");
        loanNode1.setNodeOrder(1);
        nodeRepository.save(loanNode1);

        ApprovalNode loanNode2 = new ApprovalNode();
        loanNode2.setFlowConfigId(largeLoanFlow.getId());
        loanNode2.setNodeName("财务审批");
        loanNode2.setNodeType("APPROVAL");
        loanNode2.setApproverIds(finance1.getId().toString());
        loanNode2.setApprovalStrategy("AND");
        loanNode2.setNodeOrder(2);
        nodeRepository.save(loanNode2);
    }

    /**
     * 创建请假审批流程配置
     */
    private void createLeaveFlowConfigs(List<User> users) {
        User manager1 = users.get(2);
        User manager2 = users.get(3);

        ApprovalFlowConfig leaveFlow = new ApprovalFlowConfig();
        leaveFlow.setFlowName("请假审批流程");
        leaveFlow.setBusinessType("LEAVE_APPROVAL");
        leaveFlow.setApprovalType(ApprovalType.SEQUENTIAL);
        leaveFlow.setCategory("请假");
        leaveFlow.setDescription("员工请假审批流程");
        leaveFlow.setActive(true);
        flowConfigRepository.save(leaveFlow);

        ApprovalNode leaveNode = new ApprovalNode();
        leaveNode.setFlowConfigId(leaveFlow.getId());
        leaveNode.setNodeName("主管审批");
        leaveNode.setNodeType("APPROVAL");
        leaveNode.setApproverIds(manager1.getId() + "," + manager2.getId());
        leaveNode.setApprovalStrategy("OR");
        leaveNode.setNodeOrder(1);
        nodeRepository.save(leaveNode);
    }

    /**
     * 创建采购审批流程配置
     */
    private void createPurchaseFlowConfigs(List<User> users) {
        User manager1 = users.get(2);
        User finance1 = users.get(4);

        ApprovalFlowConfig purchaseFlow = new ApprovalFlowConfig();
        purchaseFlow.setFlowName("采购审批流程");
        purchaseFlow.setBusinessType("PURCHASE_APPROVAL");
        purchaseFlow.setApprovalType(ApprovalType.SEQUENTIAL);
        purchaseFlow.setCategory("采购");
        purchaseFlow.setDescription("采购申请审批流程");
        purchaseFlow.setActive(true);
        flowConfigRepository.save(purchaseFlow);

        ApprovalNode purchaseNode1 = new ApprovalNode();
        purchaseNode1.setFlowConfigId(purchaseFlow.getId());
        purchaseNode1.setNodeName("部门审批");
        purchaseNode1.setNodeType("APPROVAL");
        purchaseNode1.setApproverIds(manager1.getId().toString());
        purchaseNode1.setApprovalStrategy("OR");
        purchaseNode1.setNodeOrder(1);
        nodeRepository.save(purchaseNode1);

        ApprovalNode purchaseNode2 = new ApprovalNode();
        purchaseNode2.setFlowConfigId(purchaseFlow.getId());
        purchaseNode2.setNodeName("财务审批");
        purchaseNode2.setNodeType("APPROVAL");
        purchaseNode2.setApproverIds(finance1.getId().toString());
        purchaseNode2.setApprovalStrategy("AND");
        purchaseNode2.setNodeOrder(2);
        nodeRepository.save(purchaseNode2);
    }

    /**
     * 创建草稿状态的审批实例
     */
    private void createDraftInstances(List<User> users) {
        User employee1 = users.get(0);
        
        // 报销草稿
        ApprovalInstance draftExpense = new ApprovalInstance();
        draftExpense.setBusinessType("EXPENSE_APPROVAL");
        draftExpense.setBusinessTitle("交通费报销申请");
        draftExpense.setApplicantId(employee1.getId());
        draftExpense.setDepartmentId(101L);
        draftExpense.setStatus(ApprovalStatus.DRAFT);
        draftExpense.setAmount(new BigDecimal("150.50"));
        draftExpense.setDescription("本月交通费用报销");
        draftExpense.setApplyTime(LocalDateTime.now());
        instanceRepository.save(draftExpense);

        // 借款草稿
        ApprovalInstance draftLoan = new ApprovalInstance();
        draftLoan.setBusinessType("LOAN_APPROVAL");
        draftLoan.setBusinessTitle("出差借款申请");
        draftLoan.setApplicantId(employee1.getId());
        draftLoan.setDepartmentId(101L);
        draftLoan.setStatus(ApprovalStatus.DRAFT);
        draftLoan.setAmount(new BigDecimal("800.00"));
        draftLoan.setDescription("下周出差费用预支");
        draftLoan.setApplyTime(LocalDateTime.now().minusDays(1));
        instanceRepository.save(draftLoan);
    }

    /**
     * 创建审批中的实例
     */
    private void createApprovingInstances(List<User> users) {
        User employee1 = users.get(0);
        User employee2 = users.get(1);

        // 小额报销审批中
        ApprovalInstance approvingExpense = new ApprovalInstance();
        approvingExpense.setBusinessType("EXPENSE_APPROVAL");
        approvingExpense.setBusinessTitle("办公用品采购报销");
        approvingExpense.setApplicantId(employee1.getId());
        approvingExpense.setDepartmentId(101L);
        approvingExpense.setStatus(ApprovalStatus.APPROVING);
        approvingExpense.setAmount(new BigDecimal("300.00"));
        approvingExpense.setDescription("购买办公用品费用");
        approvingExpense.setApplyTime(LocalDateTime.now());
        approvingExpense.setSubmitTime(LocalDateTime.now());
        instanceRepository.save(approvingExpense);

        // 大额借款审批中
        ApprovalInstance approvingLoan = new ApprovalInstance();
        approvingLoan.setBusinessType("LOAN_APPROVAL");
        approvingLoan.setBusinessTitle("设备采购借款");
        approvingLoan.setApplicantId(employee2.getId());
        approvingLoan.setDepartmentId(102L);
        approvingLoan.setStatus(ApprovalStatus.APPROVING);
        approvingLoan.setAmount(new BigDecimal("3500.00"));
        approvingLoan.setDescription("采购新电脑设备");
        approvingLoan.setApplyTime(LocalDateTime.now().minusHours(2));
        approvingLoan.setSubmitTime(LocalDateTime.now().minusHours(1));
        instanceRepository.save(approvingLoan);
    }

    /**
     * 创建已完成的实例
     */
    private void createCompletedInstances(List<User> users) {
        User employee1 = users.get(0);
        User manager1 = users.get(2);

        // 已完成报销
        ApprovalInstance completedExpense = new ApprovalInstance();
        completedExpense.setBusinessType("EXPENSE_APPROVAL");
        completedExpense.setBusinessTitle("项目差旅报销");
        completedExpense.setApplicantId(employee1.getId());
        completedExpense.setDepartmentId(101L);
        completedExpense.setStatus(ApprovalStatus.COMPLETED);
        completedExpense.setAmount(new BigDecimal("1200.00"));
        completedExpense.setDescription("客户项目差旅费用");
        completedExpense.setApplyTime(LocalDateTime.now().minusDays(5));
        completedExpense.setSubmitTime(LocalDateTime.now().minusDays(4));
        completedExpense.setCompleteTime(LocalDateTime.now().minusDays(3));
        instanceRepository.save(completedExpense);

        // 创建对应的审批记录
        ApprovalRecord approvalRecord = new ApprovalRecord();
        approvalRecord.setInstanceId(completedExpense.getId());
        approvalRecord.setApproverId(manager1.getId());
        approvalRecord.setApprovalResult("APPROVED");
        approvalRecord.setComments("批准，费用合理");
        approvalRecord.setApprovalTime(LocalDateTime.now().minusDays(3));
        recordRepository.save(approvalRecord);
    }

    /**
     * 创建被驳回的实例
     */
    private void createRejectedInstances(List<User> users) {
        User employee2 = users.get(1);
        User manager2 = users.get(3);

        // 被驳回报销
        ApprovalInstance rejectedExpense = new ApprovalInstance();
        rejectedExpense.setBusinessType("EXPENSE_APPROVAL");
        rejectedExpense.setBusinessTitle("设备维护费用报销");
        rejectedExpense.setApplicantId(employee2.getId());
        rejectedExpense.setDepartmentId(102L);
        rejectedExpense.setStatus(ApprovalStatus.REJECTED);
        rejectedExpense.setAmount(new BigDecimal("800.00"));
        rejectedExpense.setDescription("服务器维护费用");
        rejectedExpense.setApplyTime(LocalDateTime.now().minusDays(2));
        rejectedExpense.setSubmitTime(LocalDateTime.now().minusDays(1));
        rejectedExpense.setRejectTime(LocalDateTime.now().minusHours(3));
        instanceRepository.save(rejectedExpense);

        // 创建驳回记录
        ApprovalRecord rejectRecord = new ApprovalRecord();
        rejectRecord.setInstanceId(rejectedExpense.getId());
        rejectRecord.setApproverId(manager2.getId());
        rejectRecord.setApprovalResult("REJECTED");
        rejectRecord.setComments("维护费用过高，需提供详细报价单");
        rejectRecord.setApprovalTime(LocalDateTime.now().minusHours(3));
        recordRepository.save(rejectRecord);
    }

    /**
     * 创建已撤销的实例
     */
    private void createWithdrawnInstances(List<User> users) {
        User employee1 = users.get(0);

        // 已撤销申请
        ApprovalInstance withdrawnExpense = new ApprovalInstance();
        withdrawnExpense.setBusinessType("EXPENSE_APPROVAL");
        withdrawnExpense.setBusinessTitle("培训费用报销");
        withdrawnExpense.setApplicantId(employee1.getId());
        withdrawnExpense.setDepartmentId(101L);
        withdrawnExpense.setStatus(ApprovalStatus.WITHDRAWN);
        withdrawnExpense.setAmount(new BigDecimal("600.00"));
        withdrawnExpense.setDescription("技术培训费用");
        withdrawnExpense.setApplyTime(LocalDateTime.now().minusDays(3));
        withdrawnExpense.setSubmitTime(LocalDateTime.now().minusDays(2));
        withdrawnExpense.setWithdrawTime(LocalDateTime.now().minusDays(1));
        withdrawnExpense.setWithdrawReason("培训计划变更");
        instanceRepository.save(withdrawnExpense);
    }

    /**
     * 创建报销测试实例
     */
    private void createExpenseTestInstances(List<User> users) {
        User employee1 = users.get(0);
        User employee2 = users.get(1);

        // 创建不同金额的报销实例
        ExpenseTestScenario[] scenarios = {
            new ExpenseTestScenario("交通费", new BigDecimal("85.60"), "日常通勤费用"),
            new ExpenseTestScenario("餐费补贴", new BigDecimal("200.00"), "客户招待餐费"),
            new ExpenseTestScenario("住宿费", new BigDecimal("350.00"), "出差住宿费用"),
            new ExpenseTestScenario("培训费", new BigDecimal("1200.00"), "技能提升培训"),
            new ExpenseTestScenario("设备采购", new BigDecimal("4500.00"), "办公设备采购")
        };

        for (int i = 0; i < scenarios.length; i++) {
            ExpenseTestScenario scenario = scenarios[i];
            User applicant = i % 2 == 0 ? employee1 : employee2;
            
            ApprovalInstance instance = new ApprovalInstance();
            instance.setBusinessType("EXPENSE_APPROVAL");
            instance.setBusinessTitle(scenario.title);
            instance.setApplicantId(applicant.getId());
            instance.setDepartmentId(applicant.getDepartmentId());
            instance.setAmount(scenario.amount);
            instance.setDescription(scenario.description);
            instance.setApplyTime(LocalDateTime.now().minusDays(i));
            
            // 设置不同状态
            if (i == 0) instance.setStatus(ApprovalStatus.DRAFT);
            else if (i == 1) instance.setStatus(ApprovalStatus.APPROVING);
            else if (i == 2) instance.setStatus(ApprovalStatus.COMPLETED);
            else if (i == 3) instance.setStatus(ApprovalStatus.REJECTED);
            else instance.setStatus(ApprovalStatus.WITHDRAWN);
            
            if (i > 0) instance.setSubmitTime(LocalDateTime.now().minusDays(i));
            if (i == 2) instance.setCompleteTime(LocalDateTime.now().minusDays(i-1));
            if (i == 3) instance.setRejectTime(LocalDateTime.now().minusDays(i-1));
            if (i == 4) instance.setWithdrawTime(LocalDateTime.now().minusDays(i-1));
            
            instanceRepository.save(instance);
        }
    }

    /**
     * 创建借款测试实例
     */
    private void createLoanTestInstances(List<User> users) {
        User employee1 = users.get(0);
        User employee2 = users.get(1);

        LoanTestScenario[] scenarios = {
            new LoanTestScenario("差旅借款", new BigDecimal("800.00"), "短期出差费用"),
            new LoanTestScenario("设备借款", new BigDecimal("2800.00"), "采购办公设备"),
            new LoanTestScenario("项目借款", new BigDecimal("5000.00"), "项目启动资金")
        };

        for (int i = 0; i < scenarios.length; i++) {
            LoanTestScenario scenario = scenarios[i];
            User applicant = i % 2 == 0 ? employee1 : employee2;
            
            ApprovalInstance instance = new ApprovalInstance();
            instance.setBusinessType("LOAN_APPROVAL");
            instance.setBusinessTitle(scenario.title);
            instance.setApplicantId(applicant.getId());
            instance.setDepartmentId(applicant.getDepartmentId());
            instance.setAmount(scenario.amount);
            instance.setDescription(scenario.description);
            instance.setApplyTime(LocalDateTime.now().minusDays(i * 2));
            
            // 设置不同状态
            if (i == 0) instance.setStatus(ApprovalStatus.DRAFT);
            else if (i == 1) instance.setStatus(ApprovalStatus.APPROVING);
            else instance.setStatus(ApprovalStatus.COMPLETED);
            
            if (i > 0) instance.setSubmitTime(LocalDateTime.now().minusDays(i * 2));
            if (i == 2) instance.setCompleteTime(LocalDateTime.now().minusDays(i * 2 - 1));
            
            instanceRepository.save(instance);
        }
    }

    /**
     * 创建审批记录
     */
    private void createApprovalRecords(List<User> users) {
        User manager1 = users.get(2);
        User manager2 = users.get(3);
        User finance1 = users.get(4);

        // 获取所有审批中的实例
        List<ApprovalInstance> approvingInstances = instanceRepository.findByStatus(ApprovalStatus.APPROVING);
        
        for (ApprovalInstance instance : approvingInstances) {
            // 为每个实例创建审批记录
            if (instance.getAmount().compareTo(new BigDecimal("1000")) < 0) {
                // 小额审批由经理处理
                ApprovalRecord record = new ApprovalRecord();
                record.setInstanceId(instance.getId());
                record.setApproverId(manager1.getId());
                record.setApprovalResult("APPROVED");
                record.setComments("符合公司政策，批准申请");
                record.setApprovalTime(LocalDateTime.now());
                recordRepository.save(record);
            } else {
                // 大额审批创建多个记录
                ApprovalRecord mgrRecord = new ApprovalRecord();
                mgrRecord.setInstanceId(instance.getId());
                mgrRecord.setApproverId(manager2.getId());
                mgrRecord.setApprovalResult("APPROVED");
                mgrRecord.setComments("项目需要，同意申请");
                mgrRecord.setApprovalTime(LocalDateTime.now().minusHours(1));
                recordRepository.save(mgrRecord);

                ApprovalRecord finRecord = new ApprovalRecord();
                finRecord.setInstanceId(instance.getId());
                finRecord.setApproverId(finance1.getId());
                finRecord.setApprovalResult("APPROVED");
                finRecord.setComments("预算充足，财务审核通过");
                finRecord.setApprovalTime(LocalDateTime.now());
                recordRepository.save(finRecord);
            }
        }
    }

    /**
     * 创建审计日志
     */
    private void createAuditLogs() {
        List<ApprovalInstance> instances = instanceRepository.findAll();
        
        for (ApprovalInstance instance : instances) {
            // 为每个实例创建初始审计日志
            ApprovalAuditLog createLog = new ApprovalAuditLog();
            createLog.setInstanceId(instance.getId());
            createLog.setActionType(AuditActionType.CREATE);
            createLog.setDescription("创建审批申请");
            createLog.setOperatorId(instance.getApplicantId());
            createLog.setOperatorName("申请人");
            createLog.setOldStatus(null);
            createLog.setNewStatus(instance.getStatus());
            createLog.setLogTime(instance.getApplyTime());
            auditLogRepository.save(createLog);

            // 如果已提交，创建提交日志
            if (instance.getSubmitTime() != null) {
                ApprovalAuditLog submitLog = new ApprovalAuditLog();
                submitLog.setInstanceId(instance.getId());
                submitLog.setActionType(AuditActionType.SUBMIT);
                submitLog.setDescription("提交审批申请");
                submitLog.setOperatorId(instance.getApplicantId());
                submitLog.setOperatorName("申请人");
                submitLog.setOldStatus(ApprovalStatus.DRAFT);
                submitLog.setNewStatus(ApprovalStatus.APPROVING);
                submitLog.setLogTime(instance.getSubmitTime());
                auditLogRepository.save(submitLog);
            }

            // 根据最终状态创建相应日志
            if (instance.getCompleteTime() != null) {
                ApprovalAuditLog completeLog = new ApprovalAuditLog();
                completeLog.setInstanceId(instance.getId());
                completeLog.setActionType(AuditActionType.COMPLETE);
                completeLog.setDescription("审批完成");
                completeLog.setOperatorId(1000L); // 系统操作
                completeLog.setOperatorName("系统");
                completeLog.setOldStatus(ApprovalStatus.APPROVING);
                completeLog.setNewStatus(ApprovalStatus.COMPLETED);
                completeLog.setLogTime(instance.getCompleteTime());
                auditLogRepository.save(completeLog);
            }
        }
    }

    /**
     * 清理测试数据
     */
    public void cleanupTestData() {
        auditLogRepository.deleteAll();
        recordRepository.deleteAll();
        nodeRepository.deleteAll();
        instanceRepository.deleteAll();
        flowConfigRepository.deleteAll();
    }

    /**
     * 测试数据生成器
     */
    public static final class TestDataGenerator {
        
        // 私有构造函数防止实例化
        private TestDataGenerator() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }
        
        public static ExpenseApprovalDto createExpenseDto(String title, BigDecimal amount, String description, Long applicantId) {
            ExpenseApprovalDto dto = new ExpenseApprovalDto();
            dto.setBusinessType("EXPENSE_APPROVAL");
            dto.setBusinessTitle(title);
            dto.setApplicantId(applicantId);
            dto.setDepartmentId(101L);
            dto.setAmount(amount);
            dto.setDescription(description);
            dto.setExpenseDate(LocalDateTime.now());
            return dto;
        }

        public static ApprovalFlowConfig createFlowConfig(String name, String businessType, ApprovalType type, 
                                                         BigDecimal minAmount, BigDecimal maxAmount) {
            ApprovalFlowConfig config = new ApprovalFlowConfig();
            config.setFlowName(name);
            config.setBusinessType(businessType);
            config.setApprovalType(type);
            config.setCategory("通用");
            config.setMinAmount(minAmount);
            config.setMaxAmount(maxAmount);
            config.setDescription("测试流程配置");
            config.setActive(true);
            return config;
        }
    }

    private static class ExpenseTestScenario {
        private final String title;
        private final BigDecimal amount;
        private final String description;
        
        public ExpenseTestScenario(String title, BigDecimal amount, String description) {
            this.title = title;
            this.amount = amount;
            this.description = description;
        }
        
        public String getTitle() { return title; }
        public BigDecimal getAmount() { return amount; }
        public String getDescription() { return description; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ExpenseTestScenario)) return false;
            ExpenseTestScenario that = (ExpenseTestScenario) o;
            return Objects.equals(title, that.title) &&
                   Objects.equals(amount, that.amount) &&
                   Objects.equals(description, that.description);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(title, amount, description);
        }
        
        @Override
        public String toString() {
            return "ExpenseTestScenario{" +
                   "title='" + title + '\'' +
                   ", amount=" + amount +
                   ", description='" + description + '\'' +
                   '}';
        }
    }

    private static class LoanTestScenario {
        private final String title;
        private final BigDecimal amount;
        private final String description;
        
        public LoanTestScenario(String title, BigDecimal amount, String description) {
            this.title = title;
            this.amount = amount;
            this.description = description;
        }
        
        public String getTitle() { return title; }
        public BigDecimal getAmount() { return amount; }
        public String getDescription() { return description; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LoanTestScenario)) return false;
            LoanTestScenario that = (LoanTestScenario) o;
            return Objects.equals(title, that.title) &&
                   Objects.equals(amount, that.amount) &&
                   Objects.equals(description, that.description);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(title, amount, description);
        }
        
        @Override
        public String toString() {
            return "LoanTestScenario{" +
                   "title='" + title + '\'' +
                   ", amount=" + amount +
                   ", description='" + description + '\'' +
                   '}';
        }
    }
}