package com.ccms.service.impl;

import com.ccms.dto.RepaymentRequest;
import com.ccms.dto.RepaymentResponse;
import com.ccms.entity.expense.LoanMain;
import com.ccms.entity.expense.LoanRepayment;
import com.ccms.repository.expense.LoanMainRepository;
import com.ccms.repository.expense.LoanRepaymentRepository;
import com.ccms.service.LoanRepaymentService;
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
import java.util.stream.Collectors;

/**
 * 借款还款服务实现类
 */
@Service
@Transactional
public class LoanRepaymentServiceImpl implements LoanRepaymentService {

    private static final Logger logger = LoggerFactory.getLogger(LoanRepaymentServiceImpl.class);
    
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final LoanMainRepository loanMainRepository;
    private final MessageService messageService;

    @Autowired
    public LoanRepaymentServiceImpl(LoanRepaymentRepository loanRepaymentRepository,
                                    LoanMainRepository loanMainRepository,
                                    MessageService messageService) {
        this.loanRepaymentRepository = loanRepaymentRepository;
        this.loanMainRepository = loanMainRepository;
        this.messageService = messageService;
    }

    @Override
    public LoanRepayment createRepayment(LoanRepayment repayment) {
        // 参数校验
        validateRepayment(repayment);
        
        // 关联借款验证
        LoanMain loan = loanMainRepository.findById(repayment.getLoanId())
                .orElseThrow(() -> new RuntimeException("借款单不存在"));
        
        // 验证借款状态
        if (loan.getStatus() != 2) { // 不是已放款状态
            throw new RuntimeException("只有已放款的借款单可以进行还款");
        }
        
        // 验证还款金额不超过剩余应还金额
        BigDecimal remainingBalance = loan.getLoanAmount().subtract(loan.getRepaidAmount() != null ? loan.getRepaidAmount() : BigDecimal.ZERO);
        if (repayment.getRepaymentAmount().compareTo(remainingBalance) > 0) {
            throw new RuntimeException("还款金额不能超过剩余应还金额");
        }
        
        // 生成还款单号
        String repaymentNo = generateRepaymentNo();
        repayment.setRepaymentNo(repaymentNo);
        
        // 设置默认值
        repayment.setStatus(0); // 待还款
        repayment.setCreateTime(LocalDateTime.now());
        
        // 保存还款记录
        LoanRepayment savedRepayment = loanRepaymentRepository.save(repayment);
        
        logger.info("还款记录创建成功，还款单号：{}", repaymentNo);
        
        return savedRepayment;
    }

    @Override
    public LoanRepayment executeRepayment(Long repaymentId) {
        // 获取还款记录
        LoanRepayment repayment = getRepaymentById(repaymentId);
        
        // 只有待还款状态的记录可以执行
        if (repayment.getStatus() != 0) {
            throw new RuntimeException("只有待还款状态可以执行还款");
        }
        
        // 获取关联借款
        LoanMain loan = loanMainRepository.findById(repayment.getLoanId())
                .orElseThrow(() -> new RuntimeException("借款单不存在"));
        
        // 执行还款
        repayment.setStatus(1); // 已还款
        repayment.setRepaymentDate(LocalDate.now());
        repayment.setUpdateTime(LocalDateTime.now());
        
        // 更新借款还款信息
        BigDecimal currentRepaid = loan.getRepaidAmount() != null ? loan.getRepaidAmount() : BigDecimal.ZERO;
        loan.setRepaidAmount(currentRepaid.add(repayment.getRepaymentAmount()));
        
        // 如果已全额还款，更新借款状态
        BigDecimal remainingBalance = loan.getLoanAmount().subtract(loan.getRepaidAmount());
        if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(3); // 已还清
        }
        
        loanRepaymentRepository.save(repayment);
        loanMainRepository.save(loan);
        
        // 发送还款成功消息
        messageService.sendApprovalMessage(loan.getLoanUserId(), 
            "还款成功", "您的借款单号：" + loan.getLoanNo() + " 还款成功，还款金额：" + repayment.getRepaymentAmount());
        
        logger.info("还款执行成功，还款单号：{}", repayment.getRepaymentNo());
        
        return repayment;
    }

    @Override
    public boolean cancelRepayment(Long repaymentId) {
        LoanRepayment repayment = getRepaymentById(repaymentId);
        
        // 只有待还款状态的记录可以取消
        if (repayment.getStatus() != 0) {
            throw new RuntimeException("只有待还款状态可以取消");
        }
        
        repayment.setStatus(2); // 已取消
        repayment.setUpdateTime(LocalDateTime.now());
        loanRepaymentRepository.save(repayment);
        
        logger.info("还款取消成功，还款单号：{}", repayment.getRepaymentNo());
        
        return true;
    }

    @Override
    public LoanRepayment getRepaymentById(Long id) {
        return loanRepaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("还款记录不存在"));
    }

    @Override
    public LoanRepayment getRepaymentByRepaymentNo(String repaymentNo) {
        return loanRepaymentRepository.findByRepaymentNo(repaymentNo)
                .orElseThrow(() -> new RuntimeException("还款记录不存在"));
    }

    @Override
    public List<LoanRepayment> getRepaymentsByLoanId(Long loanId) {
        return loanRepaymentRepository.findByLoanId(loanId);
    }

    @Override
    public Page<LoanRepayment> getRepaymentsByLoanId(Long loanId, Pageable pageable) {
        return loanRepaymentRepository.findByLoanId(loanId, pageable);
    }

    @Override
    public List<LoanRepayment> getOverdueRepayments() {
        return loanRepaymentRepository.findOverdueRepayments(LocalDate.now());
    }

    @Override
    public List<LoanRepayment> getPendingRepayments() {
        return loanRepaymentRepository.findPendingRepayments(LocalDate.now());
    }

    @Override
    public BigDecimal getTotalRepaymentAmountByLoanId(Long loanId) {
        return loanRepaymentRepository.sumRepaymentAmountByLoanId(loanId);
    }

    @Override
    public BigDecimal getTotalRepaymentAmountByUserId(Long userId) {
        return loanRepaymentRepository.sumRepaymentAmountByUserId(userId);
    }

    @Override
    public String generateRepaymentNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return "RP" + dateStr + timestamp;
    }

    @Override
    public LoanRepayment autoRepaymentByReimbursement(Long loanId, BigDecimal amount, String reimbursementNo) {
        LoanMain loan = loanMainRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("借款单不存在"));
        
        // 创建自动还款记录
        LoanRepayment repayment = new LoanRepayment();
        repayment.setLoanId(loanId);
        repayment.setRepaymentAmount(amount);
        repayment.setRepaymentType(2); // 报销抵扣
        repayment.setRemark("报销单号：" + reimbursementNo + " 自动核销");
        repayment.setStatus(1); // 直接标记为已还款
        repayment.setRepaymentDate(LocalDate.now());
        repayment.setRepaymentNo(generateRepaymentNo());
        repayment.setCreateTime(LocalDateTime.now());
        
        LoanRepayment savedRepayment = createRepayment(repayment);
        
        // 自动执行还款
        return executeRepayment(savedRepayment.getId());
    }

    @Override
    public LoanRepayment forceRepayment(Long loanId, BigDecimal amount, String remark) {
        LoanMain loan = loanMainRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("借款单不存在"));
        
        // 创建强制还款记录
        LoanRepayment repayment = new LoanRepayment();
        repayment.setLoanId(loanId);
        repayment.setRepaymentAmount(amount);
        repayment.setRepaymentType(3); // 系统强制还款
        repayment.setRemark("系统强制还款：" + remark);
        repayment.setStatus(1); // 直接标记为已还款
        repayment.setRepaymentDate(LocalDate.now());
        repayment.setRepaymentNo(generateRepaymentNo());
        repayment.setCreateTime(LocalDateTime.now());
        
        LoanRepayment savedRepayment = createRepayment(repayment);
        
        // 自动执行还款
        return executeRepayment(savedRepayment.getId());
    }

    @Override
    public RepaymentResponse createRepayment(RepaymentRequest request) {
        LoanRepayment repayment = new LoanRepayment();
        copyProperties(request, repayment);
        LoanRepayment savedRepayment = createRepayment(repayment);
        return convertToResponse(savedRepayment);
    }

    @Override
    public List<RepaymentResponse> getRepaymentsByUserId(Long userId) {
        List<LoanRepayment> repayments = loanRepaymentRepository.findByUserId(userId);
        return repayments.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public Page<RepaymentResponse> findRepayments(Pageable pageable) {
        Page<LoanRepayment> repayments = loanRepaymentRepository.findAll(pageable);
        return repayments.map(this::convertToResponse);
    }

    @Override
    public List<RepaymentResponse> autoSettleByReimbursement(Long reimbursementId) {
        // 这里需要获取报销单信息并自动核销相关借款
        // 简化实现：根据报销单中的借款信息进行核销
        // 实际项目中需要更复杂的逻辑
        
        // 返回自动核销的还款记录
        return List.of();
    }

    /**
     * 验证还款参数
     */
    private void validateRepayment(LoanRepayment repayment) {
        if (repayment.getLoanId() == null) {
            throw new RuntimeException("借款ID不能为空");
        }
        
        if (repayment.getRepaymentAmount() == null || repayment.getRepaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("还款金额必须大于0");
        }
        
        if (repayment.getDueDate() == null || repayment.getDueDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("还款到期日不能早于当前日期");
        }
    }

    /**
     * DTO转Entity
     */
    private void copyProperties(RepaymentRequest request, LoanRepayment repayment) {
        repayment.setLoanId(request.getLoanId());
        repayment.setRepaymentAmount(request.getRepayAmount());
        repayment.setRepaymentType(request.getRepayType());
        repayment.setDueDate(request.getRepayDate());
        repayment.setRemark(request.getRemark());
    }

    /**
     * Entity转DTO
     */
    private RepaymentResponse convertToResponse(LoanRepayment repayment) {
        RepaymentResponse response = new RepaymentResponse();
        response.setId(repayment.getId());
        response.setRepayNo(repayment.getRepaymentNo());
        response.setLoanId(repayment.getLoanId());
        response.setRepayAmount(repayment.getRepaymentAmount());
        response.setRepayType(repayment.getRepaymentType());
        response.setRepayDate(repayment.getRepaymentDate());
        response.setStatus(repayment.getStatus());
        response.setRemark(repayment.getRemark());
        response.setCreateTime(repayment.getCreateTime());
        response.setUpdateTime(repayment.getUpdateTime());
        
        // 设置用户和借款信息（需要查询）
        try {
            LoanMain loan = loanMainRepository.findById(repayment.getLoanId()).orElse(null);
            if (loan != null) {
                response.setLoanNo(loan.getLoanNo());
                response.setUserId(loan.getLoanUserId());
                // TODO: 获取用户名，这里需要用户服务
            }
        } catch (Exception e) {
            logger.warn("获取借款信息失败: {}", e.getMessage());
        }
        
        return response;
    }
}