package com.ccms.service.impl;

import com.ccms.dto.LoanApplyRequest;
import com.ccms.dto.LoanResponse;
import com.ccms.entity.expense.LoanMain;
import com.ccms.repository.expense.LoanMainRepository;
import com.ccms.service.LoanService;
import com.ccms.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

/**
 * 借款服务实现类
 */
@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanServiceImpl.class);
    
    private final LoanMainRepository loanMainRepository;
    private final MessageService messageService;

    @Autowired
    public LoanServiceImpl(LoanMainRepository loanMainRepository, 
                          MessageService messageService) {
        this.loanMainRepository = loanMainRepository;
        this.messageService = messageService;
    }

    @Override
    public LoanMain applyLoan(LoanMain loan) {
        // 参数校验
        validateLoanApplication(loan);
        
        // 生成借款单号
        String loanNo = generateLoanNo();
        loan.setLoanNo(loanNo);
        
        // 设置默认值
        loan.setRepaidAmount(BigDecimal.ZERO);
        loan.setStatus(0); // 草稿状态
        
        // 检查借款限制
        if (!canUserBorrow(loan.getLoanUserId(), loan.getLoanAmount())) {
            throw new RuntimeException("用户已达到借款限制，无法申请新借款");
        }
        
        // 保存借款申请
        LoanMain savedLoan = loanMainRepository.save(loan);
        
        logger.info("借款申请提交成功，借款单号：{}", loanNo);
        
        return savedLoan;
    }

    @Override
    public LoanMain updateLoan(LoanMain loan) {
        // 校验借款单是否存在
        LoanMain existingLoan = getLoanById(loan.getId());
        
        // 只有草稿状态的借款单可以修改
        if (existingLoan.getStatus() != 0) {
            throw new RuntimeException("只有草稿状态的借款单可以修改");
        }
        
        // 更新基本信息
        existingLoan.setLoanAmount(loan.getLoanAmount());
        existingLoan.setPurpose(loan.getPurpose());
        existingLoan.setExpectRepayDate(loan.getExpectRepayDate());
        
        return loanMainRepository.save(existingLoan);
    }

    @Override
    public LoanMain getLoanById(Long id) {
        return loanMainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借款单不存在"));
    }

    @Override
    public LoanMain getLoanByLoanNo(String loanNo) {
        return loanMainRepository.findByLoanNo(loanNo)
                .orElseThrow(() -> new RuntimeException("借款单不存在"));
    }

    @Override
    public List<LoanMain> getLoansByUserId(Long userId) {
        return loanMainRepository.findByLoanUserId(userId);
    }

    @Override
    public Page<LoanMain> getLoansByUserId(Long userId, Pageable pageable) {
        return loanMainRepository.findByLoanUserId(userId, pageable);
    }

    @Override
    public List<LoanMain> getLoansByStatus(Integer status) {
        return loanMainRepository.findByStatus(status);
    }

    @Override
    public List<LoanMain> getOverdueLoans() {
        return loanMainRepository.findOverdueLoans();
    }

    @Override
    public List<LoanMain> getUnpaidLoansByUserId(Long userId) {
        return loanMainRepository.findUnpaidLoansByUserId(userId);
    }

    @Override
    public BigDecimal getUserTotalLoanAmount(Long userId) {
        BigDecimal total = loanMainRepository.sumLoanAmountByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getUserUnpaidBalance(Long userId) {
        BigDecimal balance = loanMainRepository.sumBalanceAmountByUserId(userId);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    @Override
    public boolean approveLoan(Long loanId, String remark) {
        LoanMain loan = getLoanById(loanId);
        
        // 只有审批中状态的借款单可以审批
        if (loan.getStatus() != 1) {
            throw new RuntimeException("只有审批中状态的借款单可以审批");
        }
        
        loan.setStatus(2); // 已批准，等待放款
        loanMainRepository.save(loan);
        
        // 发送审批通过消息
        messageService.sendApprovalMessage(loan.getLoanUserId(), 
            "借款申请已批准", "您的借款申请已通过审批，等待放款");
        
        logger.info("借款单审批通过，借款单ID：{}，审批人备注：{}", loanId, remark);
        
        return true;
    }

    @Override
    public boolean rejectLoan(Long loanId, String remark) {
        LoanMain loan = getLoanById(loanId);
        
        // 只有审批中状态的借款单可以驳回
        if (loan.getStatus() != 1) {
            throw new RuntimeException("只有审批中状态的借款单可以驳回");
        }
        
        loan.setStatus(5); // 已驳回
        loanMainRepository.save(loan);
        
        // 发送驳回消息
        messageService.sendApprovalMessage(loan.getLoanUserId(), 
            "借款申请被驳回", "您的借款申请被驳回，原因：" + remark);
        
        logger.info("借款单审批驳回，借款单ID：{}，驳回原因：{}", loanId, remark);
        
        return true;
    }

    @Override
    public boolean disburseLoan(Long loanId) {
        LoanMain loan = getLoanById(loanId);
        
        // 只有已批准状态的借款单可以放款
        if (loan.getStatus() != 2) {
            throw new RuntimeException("只有已批准状态的借款单可以放款");
        }
        
        loan.setStatus(2); // 已放款
        loanMainRepository.save(loan);
        
        // 发送放款消息
        messageService.sendApprovalMessage(loan.getLoanUserId(), 
            "借款已放款", "您的借款申请已成功放款，请查收");
        
        logger.info("借款单放款成功，借款单ID：{}", loanId);
        
        return true;
    }

    @Override
    public boolean updateLoanStatus(Long loanId, Integer status) {
        LoanMain loan = getLoanById(loanId);
        loan.setStatus(status);
        loanMainRepository.save(loan);
        
        logger.info("借款单状态更新，借款单ID：{}，新状态：{}", loanId, status);
        
        return true;
    }

    @Override
    public boolean canUserBorrow(Long userId, BigDecimal amount) {
        // 检查用户是否有逾期未还借款
        List<LoanMain> overdueLoans = getOverdueLoans();
        if (!overdueLoans.isEmpty()) {
            return false;
        }
        
        // 检查单笔借款限额（示例：10000元）
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            return false;
        }
        
        // 检查月度借款限额（示例：50000元）
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        BigDecimal monthlyLoanAmount = getLoanAmountBetweenDates(startOfMonth, endOfMonth);
        if (monthlyLoanAmount.add(amount).compareTo(new BigDecimal("50000")) > 0) {
            return false;
        }
        
        return true;
    }

    @Override
    public String generateLoanNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return "LN" + dateStr + timestamp;
    }

    @Override
    public BigDecimal getLoanAmountBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<LoanMain> loans = loanMainRepository.findByRepayDateRange(startDate, endDate);
        return loans.stream()
                .map(LoanMain::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 以下是为Controller添加的方法

    /**
     * 创建借款申请（DTO接口）
     */
    public LoanResponse createLoan(LoanApplyRequest request) {
        LoanMain loan = new LoanMain();
        copyProperties(request, loan);
        LoanMain savedLoan = applyLoan(loan);
        return convertToResponse(savedLoan);
    }

    /**
     * 获取借款详情（DTO接口）
     */
    public LoanResponse getLoanResponseById(Long id) {
        LoanMain loan = getLoanById(id);
        return convertToResponse(loan);
    }
    
    /**
     * 内部获取借款详情方法，避免命名冲突
     */
    private LoanMain getLoanById_internal(Long id) {
        return loanMainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借款单不存在"));
    }

    /**
     * 更新借款信息（DTO接口）
     */
    public LoanResponse updateLoan(LoanApplyRequest request) {
        LoanMain loan = getLoanById(request.getId());
        copyProperties(request, loan);
        LoanMain updatedLoan = updateLoan(loan);
        return convertToResponse(updatedLoan);
    }

    /**
     * 删除借款记录
     */
    public void deleteLoan(Long id) {
        LoanMain loan = getLoanById(id);
        if (loan.getStatus() != 0) {
            throw new RuntimeException("只有草稿状态的借款单可以删除");
        }
        loanMainRepository.deleteById(id);
    }

    /**
     * 获取用户借款列表（DTO接口）
     */
    public List<LoanResponse> getLoanResponsesByUserId(Long userId) {
        List<LoanMain> loans = getLoansByUserId(userId);
        return loans.stream().map(this::convertToResponse).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 分页查询借款列表
     */
    public Page<LoanResponse> findLoans(Pageable pageable) {
        Page<LoanMain> loans = loanMainRepository.findAll(pageable);
        return loans.map(this::convertToResponse);
    }

    /**
     * 根据状态查询借款（DTO接口）
     */
    public List<LoanResponse> findLoansByStatus(Integer status) {
        List<LoanMain> loans = getLoansByStatus(status);
        return loans.stream().map(this::convertToResponse).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取部门借款列表
     */
    public List<LoanResponse> findLoansByDepartmentId(Long deptId) {
        List<LoanMain> loans = loanMainRepository.findByLoanDeptId(deptId);
        return loans.stream().map(this::convertToResponse).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 提交借款审批
     */
    public LoanResponse submitForApproval(Long id) {
        LoanMain loan = getLoanById(id);
        if (loan.getStatus() != 0) {
            throw new RuntimeException("只有草稿状态的借款单可以提交审批");
        }
        loan.setStatus(1); // 审批中
        LoanMain updatedLoan = loanMainRepository.save(loan);
        return convertToResponse(updatedLoan);
    }

    /**
     * 取消借款申请
     */
    public LoanResponse cancelLoan(Long id) {
        LoanMain loan = getLoanById(id);
        if (loan.getStatus() >= 2) {
            throw new RuntimeException("已放款的借款单不能取消");
        }
        loan.setStatus(4); // 已取消
        LoanMain updatedLoan = loanMainRepository.save(loan);
        return convertToResponse(updatedLoan);
    }

    /**
     * 借款放款
     */
    public LoanResponse disburseLoanResponse(Long id) {
        disburseLoan(id);
        LoanMain loan = getLoanById(id);
        return convertToResponse(loan);
    }

    /**
     * 获取待还款借款
     */
    public List<LoanResponse> findPendingRepaymentLoans() {
        List<LoanMain> loans = loanMainRepository.findPendingRepaymentLoans();
        return loans.stream().map(this::convertToResponse).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取逾期借款
     */
    public List<LoanResponse> findOverdueLoans() {
        List<LoanMain> loans = getOverdueLoans();
        return loans.stream().map(this::convertToResponse).collect(java.util.stream.Collectors.toList());
    }

    /**
     * DTO转Entity
     */
    private void copyProperties(LoanApplyRequest request, LoanMain loan) {
        // 这里需要手动复制字段，因为字段名不完全匹配
        loan.setLoanUserId(request.getLoanUserId());
        loan.setLoanDeptId(request.getLoanDeptId());
        loan.setLoanAmount(request.getLoanAmount());
        loan.setExpectRepayDate(request.getExpectRepayDate());
        loan.setPurpose(request.getPurpose());
        // 其他字段根据需要设置
    }

    /**
     * Entity转DTO
     */
    private LoanResponse convertToResponse(LoanMain loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setApplyId(loan.getApplyId());
        response.setLoanNo(loan.getLoanNo());
        response.setLoanUserId(loan.getLoanUserId());
        response.setLoanAmount(loan.getLoanAmount());
        response.setExpectRepayDate(loan.getExpectRepayDate());
        response.setPurpose(loan.getPurpose());
        response.setStatus(loan.getStatus());
        response.setRepaidAmount(loan.getRepaidAmount());
        response.setBalanceAmount(loan.getBalanceAmount());
        response.setCreateTime(loan.getCreateTime());
        response.setUpdateTime(loan.getUpdateTime());
        response.setApprovalResult(loan.getApprovalResult());
        // 其他字段根据需要设置
        return response;
    }

    /**
     * 验证借款申请参数
     */
    private void validateLoanApplication(LoanMain loan) {
        if (loan.getLoanAmount() == null || loan.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("借款金额必须大于0");
        }
        
        if (loan.getLoanUserId() == null) {
            throw new RuntimeException("借款人ID不能为空");
        }
        
        if (loan.getLoanDeptId() == null) {
            throw new RuntimeException("借款部门ID不能为空");
        }
        
        if (loan.getExpectRepayDate() == null || 
            loan.getExpectRepayDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("预期还款日期不能早于当前日期");
        }
        
        if (loan.getPurpose() == null || loan.getPurpose().trim().isEmpty()) {
            throw new RuntimeException("借款用途不能为空");
        }
    }
}