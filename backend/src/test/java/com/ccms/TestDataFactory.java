package com.ccms;

import com.ccms.dto.*;
import com.ccms.entity.system.SysAttachment;
import com.ccms.entity.system.User;
import com.ccms.entity.budget.Budget;
import com.ccms.entity.expense.ExpenseApply;
import com.ccms.entity.expense.ExpenseApplyItem;
import com.ccms.entity.fee.FeeType;
import com.ccms.entity.loan.Loan;
import com.ccms.entity.loan.LoanRepayment;
import com.ccms.entity.message.MessageTemplate;
import com.ccms.service.TemplateRequest;
import com.ccms.service.TemplateOperationResult;
import com.ccms.service.AttachmentInfo;
import com.ccms.service.UploadResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 测试数据工厂类
 * 提供测试数据的创建和配置方法
 */
public class TestDataFactory {
    
    private TestDataFactory() {
        // 私有构造器，防止实例化
    }
    
    // 基础测试常量
    public static final String TEST_USER_ID = "test-user-001";
    public static final String TEST_USER_NAME = "测试用户";
    public static final String TEST_USER_DEPT = "技术部";
    public static final String TEST_BIZ_TYPE = "EXPENSE_APPLY";
    public static final Long TEST_BIZ_ID = 10001L;
    public static final String TEST_FILE_NAME = "test-document.pdf";
    public static final String TEST_TEMPLATE_CODE = "APPROVAL_NOTIFICATION";
    
    /**
     * 创建测试用户对象
     */
    public static User createTestUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USER_NAME);
        user.setDepartment(TEST_USER_DEPT);
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        return user;
    }
    
    /**
     * 创建测试预算对象
     */
    public static Budget createTestBudget() {
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setBudgetName("技术部年度预算");
        budget.setBudgetAmount(new BigDecimal("500000.00"));
        budget.setUsedAmount(BigDecimal.ZERO);
        budget.setRemainingAmount(new BigDecimal("500000.00"));
        budget.setDepartment(TEST_USER_DEPT);
        budget.setBudgetYear(2024);
        budget.setStatus(1);
        return budget;
    }
    
    /**
     * 创建测试费用申请对象
     */
    public static ExpenseApply createTestExpenseApply() {
        ExpenseApply apply = new ExpenseApply();
        apply.setId(1001L);
        apply.setApplyNo(generateApplyNo());
        apply.setApplyUser(TEST_USER_ID);
        apply.setApplyUserName(TEST_USER_NAME);
        apply.setDepartment(TEST_USER_DEPT);
        apply.setApplyAmount(new BigDecimal("5000.00"));
        apply.setApplyReason("项目开发费用支出");
        apply.setApplyStatus(0); // 待审批
        apply.setCreateTime(LocalDateTime.now());
        return apply;
    }
    
    /**
     * 创建测试费用申请项目
     */
    public static ExpenseApplyItem createTestExpenseApplyItem() {
        ExpenseApplyItem item = new ExpenseApplyItem();
        item.setId(1L);
        item.setApplyId(1001L);
        item.setFeeTypeId(1L);
        item.setFeeAmount(new BigDecimal("3000.00"));
        item.setDescription("服务器租赁费用");
        return item;
    }
    
    /**
     * 创建测试费用类型
     */
    public static FeeType createTestFeeType() {
        FeeType feeType = new FeeType();
        feeType.setId(1L);
        feeType.setFeeTypeCode("SERVER_COST");
        feeType.setFeeTypeName("服务器费用");
        feeType.setIsActive(true);
        return feeType;
    }
    
    /**
     * 创建测试贷款对象
     */
    public static Loan createTestLoan() {
        Loan loan = new Loan();
        loan.setId(2001L);
        loan.setLoanNo(generateLoanNo());
        loan.setUserId(TEST_USER_ID);
        loan.setAmount(new BigDecimal("100000.00"));
        loan.setInterestRate(new BigDecimal("0.05"));
        loan.setStatus(1); // 正常
        loan.setCreateTime(LocalDateTime.now());
        return loan;
    }
    
    /**
     * 创建测试还款记录
     */
    public static LoanRepayment createTestLoanRepayment() {
        LoanRepayment repayment = new LoanRepayment();
        repayment.setId(1L);
        repayment.setLoanId(2001L);
        repayment.setRepaymentAmount(new BigDecimal("2000.00"));
        repayment.setRepaymentDate(LocalDateTime.now());
        repayment.setStatus(1); // 已还款
        return repayment;
    }
    
    /**
     * 创建测试附件对象
     */
    public static SysAttachment createTestAttachment() {
        SysAttachment attachment = new SysAttachment();
        attachment.setId(3001L);
        attachment.setFileName(TEST_FILE_NAME);
        attachment.setFileSize(1024000L);
        attachment.setMimeType("application/pdf");
        attachment.setFilePath("/uploads/2024/01/" + TEST_FILE_NAME);
        attachment.setBizType(TEST_BIZ_TYPE);
        attachment.setBizId(TEST_BIZ_ID);
        attachment.setUploadUser(TEST_USER_ID);
        attachment.setUploadTime(LocalDateTime.now());
        return attachment;
    }
    
    /**
     * 创建测试消息模板
     */
    public static MessageTemplate createTestMessageTemplate() {
        MessageTemplate template = new MessageTemplate();
        template.setId(1L);
        template.setTemplateCode(TEST_TEMPLATE_CODE);
        template.setTemplateName("审批通知模板");
        template.setTemplateContent("您有一个新的审批请求，请及时处理。");
        template.setTemplateType("NOTIFICATION");
        template.setDescription("审批流程通知模板");
        template.setCreatedTime(LocalDateTime.now());
        return template;
    }
    
    /**
     * 创建测试模板请求
     */
    public static TemplateRequest createTestTemplateRequest() {
        TemplateRequest request = new TemplateRequest();
        request.setTemplateCode(TEST_TEMPLATE_CODE);
        request.setTemplateName("测试模板");
        request.setContent("测试模板内容");
        request.setTemplateType("NOTIFICATION");
        request.setDescription("测试模板描述");
        request.setCreatedBy(TEST_USER_ID);
        return request;
    }
    
    /**
     * 创建测试模板操作结果
     */
    public static TemplateOperationResult createTestTemplateOperationResult() {
        TemplateOperationResult result = new TemplateOperationResult();
        result.setSuccess(true);
        result.setMessage("操作成功");
        result.setTemplateId(1L);
        return result;
    }
    
    /**
     * 创建测试附件信息
     */
    public static AttachmentInfo createTestAttachmentInfo() {
        AttachmentInfo info = new AttachmentInfo();
        info.setBizType(TEST_BIZ_TYPE);
        info.setBizId(TEST_BIZ_ID);
        info.setDescription("测试附件");
        info.setIsPublic(false);
        return info;
    }
    
    /**
     * 创建测试上传结果
     */
    public static UploadResult createTestUploadResult() {
        UploadResult result = new UploadResult();
        result.setSuccess(true);
        result.setMessage("上传成功");
        result.setAttachmentId(3001L);
        result.setFileName(TEST_FILE_NAME);
        return result;
    }
    
    /**
     * 生成唯一的申请编号
     */
    private static String generateApplyNo() {
        return "AP" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + 
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }
    
    /**
     * 生成唯一的贷款编号
     */
    private static String generateLoanNo() {
        return "LN" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + 
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }
}