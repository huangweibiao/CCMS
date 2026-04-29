package com.ccms.service.impl;

import com.ccms.dto.RepaymentRequest;
import com.ccms.dto.RepaymentResponse;
import com.ccms.entity.expense.RepaymentRecord;
import com.ccms.service.LoanService;
import com.ccms.service.RepaymentManagementService;
import com.ccms.service.RepaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RepaymentManagementServiceImpl implements RepaymentManagementService {

    @Autowired
    private RepaymentService repaymentService;
    
    @Autowired
    private LoanService loanService;

    @Override
    public RepaymentResponse createRepayment(RepaymentRequest request) {
        RepaymentRecord repayment = new RepaymentRecord();
        repayment.setLoanId(request.getLoanId());
        repayment.setRepayNo(request.getRepayNo());
        repayment.setRepayAmount(request.getRepayAmount());
        repayment.setRepayType(request.getRepayType());
        repayment.setRepayDate(request.getRepayDate());
        repayment.setRepayBy(request.getRepayBy());
        repayment.setBankName(request.getBankName());
        repayment.setBankAccount(request.getBankAccount());
        repayment.setRemark(request.getRemark());
        
        RepaymentRecord savedRepayment = repaymentService.createRepayment(repayment);
        return convertToResponse(savedRepayment);
    }

    @Override
    public RepaymentResponse getRepaymentById(Long id) {
        RepaymentRecord repayment = repaymentService.getRepaymentById(id);
        return convertToResponse(repayment);
    }

    @Override
    public RepaymentResponse updateRepayment(RepaymentRequest request) {
        RepaymentRecord repayment = repaymentService.getRepaymentById(request.getId());
        repayment.setRepayAmount(request.getRepayAmount());
        repayment.setRepayDate(request.getRepayDate());
        repayment.setRepayBy(request.getRepayBy());
        repayment.setRemark(request.getRemark());
        
        RepaymentRecord updatedRepayment = repaymentService.updateRepayment(repayment);
        return convertToResponse(updatedRepayment);
    }

    @Override
    public void deleteRepayment(Long id) {
        RepaymentRecord repayment = repaymentService.getRepaymentById(id);
        if (repayment == null) {
            throw new RuntimeException("还款记录不存在");
        }
        repaymentService.deleteRepayment(id);
    }

    @Override
    public List<RepaymentResponse> getRepaymentsByLoanId(Long loanId) {
        List<RepaymentRecord> repayments = repaymentService.getRepaymentsByLoanId(loanId);
        return repayments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RepaymentResponse> findRepayments(Pageable pageable) {
        Page<RepaymentRecord> repayments = repaymentService.findRepayments(pageable);
        return repayments.map(this::convertToResponse);
    }

    @Override
    public List<RepaymentResponse> findRepaymentsByStatus(Integer status) {
        List<RepaymentRecord> repayments = repaymentService.findRepaymentsByStatus(status);
        return repayments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RepaymentResponse confirmRepayment(Long id) {
        RepaymentRecord repayment = repaymentService.confirmRepayment(id);
        return convertToResponse(repayment);
    }

    @Override
    public RepaymentResponse cancelRepayment(Long id) {
        repaymentService.cancelRepayment(id);
        return getRepaymentById(id);
    }

    @Override
    public void batchConfirmRepayments(List<Long> repaymentIds) {
        repaymentService.batchConfirmRepayments(repaymentIds);
    }

    @Override
    public List<RepaymentResponse> findRepaymentsByUserId(Long userId) {
        List<RepaymentRecord> repayments = repaymentService.findRepaymentsByUserId(userId);
        return repayments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RepaymentResponse autoWriteOff(Long loanId) {
        RepaymentRecord repayment = repaymentService.autoWriteOff(loanId);
        return convertToResponse(repayment);
    }

    @Override
    public Object getLoanRepaymentStats(Long loanId) {
        return repaymentService.getLoanRepaymentStats(loanId);
    }

    @Override
    public Object getUserRepaymentStats(Long userId) {
        return repaymentService.getUserRepaymentStats(userId);
    }

    private RepaymentResponse convertToResponse(RepaymentRecord repayment) {
        RepaymentResponse response = new RepaymentResponse();
        response.setId(repayment.getId());
        response.setLoanId(repayment.getLoanId());
        response.setRepayNo(repayment.getRepayNo());
        response.setRepayAmount(repayment.getRepayAmount());
        response.setRepayType(repayment.getRepayType());
        response.setRepayDate(repayment.getRepayDate());
        response.setBankName(repayment.getBankName());
        response.setBankAccount(repayment.getBankAccount());
        response.setRemark(repayment.getRemark());
        response.setCreateTime(repayment.getCreateTime());
        response.setUpdateTime(repayment.getUpdateTime());
        return response;
    }
}