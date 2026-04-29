package com.ccms.service.impl;

import com.ccms.dto.LoanApplyRequest;
import com.ccms.dto.LoanResponse;
import com.ccms.entity.expense.LoanMain;
import com.ccms.service.LoanManagementService;
import com.ccms.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanManagementServiceImpl implements LoanManagementService {

    @Autowired
    private LoanService loanService;

    @Override
    public LoanResponse createLoan(LoanApplyRequest request) {
        LoanMain loan = new LoanMain();
        loan.setLoanUserId(request.getLoanUserId());
        loan.setLoanDeptId(request.getLoanDeptId());
        loan.setLoanAmount(request.getLoanAmount());
        loan.setExpectRepayDate(request.getExpectRepayDate());
        loan.setPurpose(request.getPurpose());
        
        LoanMain savedLoan = loanService.applyLoan(loan);
        return convertToResponse(savedLoan);
    }

    @Override
    public LoanResponse getLoanById(Long id) {
        LoanMain loan = loanService.getLoanById(id);
        return convertToResponse(loan);
    }

    @Override
    public LoanResponse updateLoan(LoanApplyRequest request) {
        LoanMain loan = loanService.getLoanById(request.getId());
        loan.setLoanAmount(request.getLoanAmount());
        loan.setPurpose(request.getPurpose());
        loan.setExpectRepayDate(request.getExpectRepayDate());
        
        LoanMain updatedLoan = loanService.updateLoan(loan);
        return convertToResponse(updatedLoan);
    }

    @Override
    public void deleteLoan(Long id) {
        LoanMain loan = loanService.getLoanById(id);
        if (loan.getStatus() != 0) {
            throw new RuntimeException("只有草稿状态的借款单可以删除");
        }
        // delete logic will be implemented in LoanService
        // Adding delete method to LoanService would be needed
    }

    @Override
    public List<LoanResponse> getLoansByUserId(Long userId) {
        List<LoanMain> loans = loanService.getLoansByUserId(userId);
        return loans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<LoanResponse> findLoans(Pageable pageable) {
        // This method needs to be implemented in LoanService first
        // For now, returning empty page
        return Page.empty();
    }

    @Override
    public List<LoanResponse> findLoansByStatus(Integer status) {
        List<LoanMain> loans = loanService.getLoansByStatus(status);
        return loans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanResponse> findLoansByDepartmentId(Long deptId) {
        // This method needs to be implemented in LoanService
        return List.of();
    }

    @Override
    public LoanResponse submitForApproval(Long id) {
        LoanMain loan = loanService.getLoanById(id);
        if (loan.getStatus() != 0) {
            throw new RuntimeException("只有草稿状态的借款单可以提交审批");
        }
        
        // Update status to "审批中"
        LoanMain updatedLoan = loanService.updateLoan(loan);
        return convertToResponse(updatedLoan);
    }

    @Override
    public LoanResponse cancelLoan(Long id) {
        // Implement cancel logic
        return null; // Placeholder
    }

    @Override
    public LoanResponse disburseLoan(Long id) {
        // Implement disburse logic
        return null; // Placeholder
    }

    @Override
    public List<LoanResponse> findPendingRepaymentLoans() {
        // Implement pending repayment logic
        return List.of();
    }

    @Override
    public List<LoanResponse> findOverdueLoans() {
        List<LoanMain> overdueLoans = loanService.getOverdueLoans();
        return overdueLoans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private LoanResponse convertToResponse(LoanMain loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setApplyId(loan.getApplyId());
        response.setLoanNo(loan.getLoanNo());
        response.setLoanUserId(loan.getLoanUserId());
        response.setLoanDeptId(loan.getLoanDeptId());
        response.setLoanAmount(loan.getLoanAmount());
        response.setExpectRepayDate(loan.getExpectRepayDate());
        response.setPurpose(loan.getPurpose());
        response.setStatus(loan.getStatus());
        response.setRepaidAmount(loan.getRepaidAmount());
        response.setBalanceAmount(loan.getBalanceAmount());
        response.setCreateTime(loan.getCreateTime());
        response.setUpdateTime(loan.getUpdateTime());
        return response;
    }
}