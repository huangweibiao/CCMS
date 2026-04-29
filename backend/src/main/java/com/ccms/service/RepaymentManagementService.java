package com.ccms.service;

import com.ccms.dto.RepaymentRequest;
import com.ccms.dto.RepaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 还款管理服务接口 - 为Controller提供业务逻辑
 */
public interface RepaymentManagementService {

    /**
     * 创建还款记录
     */
    RepaymentResponse createRepayment(RepaymentRequest request);

    /**
     * 获取还款详情
     */
    RepaymentResponse getRepaymentById(Long id);

    /**
     * 更新还款记录
     */
    RepaymentResponse updateRepayment(RepaymentRequest request);

    /**
     * 删除还款记录
     */
    void deleteRepayment(Long id);

    /**
     * 获取借款的还款记录
     */
    List<RepaymentResponse> getRepaymentsByLoanId(Long loanId);

    /**
     * 分页查询还款记录
     */
    Page<RepaymentResponse> findRepayments(Pageable pageable);

    /**
     * 根据状态查询还款记录
     */
    List<RepaymentResponse> findRepaymentsByStatus(Integer status);

    /**
     * 还款确认
     */
    RepaymentResponse confirmRepayment(Long id);

    /**
     * 还款取消
     */
    RepaymentResponse cancelRepayment(Long id);

    /**
     * 批量确认还款
     */
    void batchConfirmRepayments(List<Long> repaymentIds);

    /**
     * 获取用户还款记录
     */
    List<RepaymentResponse> findRepaymentsByUserId(Long userId);

    /**
     * 自动还款核销
     */
    RepaymentResponse autoWriteOff(Long loanId);

    /**
     * 借款未还金额统计
     */
    Object getLoanRepaymentStats(Long loanId);

    /**
     * 用户未还金额统计
     */
    Object getUserRepaymentStats(Long userId);
}