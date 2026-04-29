package com.ccms.service;

import com.ccms.dto.LoanApplyRequest;
import com.ccms.dto.LoanResponse;
import com.ccms.dto.RepaymentRequest;
import com.ccms.dto.RepaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 借款管理服务接口 - 为Controller提供业务逻辑
 */
public interface LoanManagementService {

    /**
     * 创建借款申请
     */
    LoanResponse createLoan(LoanApplyRequest request);

    /**
     * 获取借款详情
     */
    LoanResponse getLoanById(Long id);

    /**
     * 更新借款信息
     */
    LoanResponse updateLoan(LoanApplyRequest request);

    /**
     * 删除借款记录
     */
    void deleteLoan(Long id);

    /**
     * 获取用户借款列表
     */
    List<LoanResponse> getLoansByUserId(Long userId);

    /**
     * 分页查询借款列表
     */
    Page<LoanResponse> findLoans(Pageable pageable);

    /**
     * 根据状态查询借款
     */
    List<LoanResponse> findLoansByStatus(Integer status);

    /**
     * 获取部门借款列表
     */
    List<LoanResponse> findLoansByDepartmentId(Long deptId);

    /**
     * 提交借款审批
     */
    LoanResponse submitForApproval(Long id);

    /**
     * 取消借款申请
     */
    LoanResponse cancelLoan(Long id);

    /**
     * 借款放款
     */
    LoanResponse disburseLoan(Long id);

    /**
     * 获取待还款借款
     */
    List<LoanResponse> findPendingRepaymentLoans();

    /**
     * 获取逾期借款
     */
    List<LoanResponse> findOverdueLoans();
}