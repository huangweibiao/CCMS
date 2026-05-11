package com.ccms.service.finance;

import com.ccms.entity.finance.FinancePayment;
import com.ccms.entity.finance.FinancePaymentMethod;
import com.ccms.repository.finance.FinancePaymentRepository;
import com.ccms.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务支付业务服务类
 * 核心功能：支付单据管理、支付审批流程、支付凭证生成
 */
@Service
public class FinancePaymentService {

    @Autowired
    private FinancePaymentRepository paymentRepository;

    /**
     * 支付单据生成结果类
     */
    public static class PaymentGenerationResult {
        private boolean success;
        private String message;
        private Long paymentId;
        private String paymentNo;

        public PaymentGenerationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public PaymentGenerationResult(boolean success, String message, Long paymentId, String paymentNo) {
            this.success = success;
            this.message = message;
            this.paymentId = paymentId;
            this.paymentNo = paymentNo;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Long getPaymentId() { return paymentId; }
        public String getPaymentNo() { return paymentNo; }
    }

    /**
     * 创建财务支付单据
     * @param businessType 业务类型
     * @param businessId 业务单据ID
     * @param businessNo 业务单据编号
     * @param amount 支付金额
     * @return 支付单据信息
     */
    @Transactional
    public PaymentGenerationResult createPayment(String businessType, Long businessId, String businessNo,
                                                        BigDecimal amount, String paymentMethod) {
        try {
            FinancePayment payment = new FinancePayment();
            payment.setPaymentNo(generatePaymentNo(businessType));
            payment.setBusinessType(businessType);
            payment.setBusinessId(businessId);
            payment.setBusinessNo(businessNo);
            payment.setApplyDepartmentId(getCurrentUserId());
            payment.setApplyDepartmentName(getCurrentDepartmentName());
            payment.setApplyEmployeeId(getCurrentUserId());
            payment.setApplyEmployeeName(getCurrentUserName());
            payment.setAmount(amount);
            payment.setPaymentMethod(FinancePaymentMethod.valueOf(paymentMethod));
            payment.setPaymentDate(LocalDate.now());
            payment.setPaymentStatus(1); // 待审批
            payment.setAccountingStatus(0); // 未核算

            payment.setCreateBy(getCurrentUserId());
            payment.setCreateTime(LocalDateTime.now());

            FinancePayment savedPayment = paymentRepository.save(payment);

            return new PaymentGenerationResult(true, "支付单创建成功",
                    savedPayment.getPaymentId(), savedPayment.getPaymentNo());

        } catch (Exception e) {
            return new PaymentGenerationResult(false, "支付单创建失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询支付单据
     */
    public FinancePayment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("支付单据不存在"));
    }

    /**
     * 查询待审批的支付单据
     */
    public List<FinancePayment> findPendingApprovalPayments() {
        return paymentRepository.findPendingApprovalPayments();
    }

    /**
     * 查询已审批待支付的支付单据
     */
    public List<FinancePayment> findApprovedPendingPayments() {
        return paymentRepository.findApprovedPendingPayments();
    }

    /**
     * 查询支付统计信息
     */
    public Map<String, Object> getPaymentStatistics(LocalDate startDate, LocalDate endDate, Integer paymentStatus) {
        Map<String, Object> result = new HashMap<>();

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        BigDecimal approvedAmount = BigDecimal.ZERO;
        Long totalCount = 0L;

        if (paymentStatus != null) {
            totalAmount = paymentRepository.sumAmountByPaymentStatusAndPaymentDateBetween(paymentStatus, startDate, endDate);
            totalCount = paymentRepository.countByPaymentDateBetween(startDate, endDate);
        } else {
            // 统计所有支付
            List<FinancePayment> allPayments = paymentRepository.findAll();
            totalCount = Long.valueOf(allPayments.size());
            totalAmount = allPayments.stream()
                    .map(FinancePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        result.put("totalAmount", totalAmount);
        result.put("totalCount", totalCount);
        result.put("startDate", startDate);
        result.put("endDate", endDate);

        if (paymentStatus != null) {
            result.put("pendingAmount", paymentRepository.sumAmountByPaymentStatusAndPaymentDateBetween(1, startDate, endDate));
            result.put("approvedAmount", paymentRepository.sumAmountByPaymentStatusAndPaymentDateBetween(2, startDate, endDate));
        }

        return result;
    }

    /**
     * 生成支付编号
     */
    private String generatePaymentNo(String businessType) {
        return businessType.toUpperCase() + "-" + System.currentTimeMillis() % 10000;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        return 1L;
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUserName() {
        return "系统管理员";
    }

    /**
     * 获取当前部门名称
     */
    private String getCurrentDepartmentName() {
        return "财务部";
    }
}

    /**
     * 查询支付单据列表
     */
    public Map<String, Object> getPaymentList(String businessType, String businessNo, Integer paymentStatus,
                                                 LocalDate startDate, LocalDate endDate, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        Pageable pageable = PageRequest.of(page, size);

        if (businessType != null && !businessType.isEmpty()) {
            Page<FinancePayment> paymentPage = paymentRepository.findByBusinessType(businessType, pageable);
            result.put("payments", paymentPage.getContent());
            result.put("total", paymentPage.getTotalElements());
            result.put("pages", paymentPage.getTotalPages());
        } else if (businessNo != null && !businessNo.isEmpty()) {
            Page<FinancePayment> paymentPage = paymentRepository.findByBusinessNoLike(businessNo, pageable);
            result.put("payments", paymentPage.getContent());
            result.put("total", paymentPage.getTotalElements());
            result.put("pages", paymentPage.getTotalPages());
        } else if (paymentStatus != null) {
            Page<FinancePayment> paymentPage = paymentRepository.findByPaymentStatus(paymentStatus, pageable);
            result.put("payments", paymentPage.getContent());
            result.put("total", paymentPage.getTotalElements());
            result.put("pages", paymentPage.getTotalPages());
        } else if (startDate != null && endDate != null) {
            Page<FinancePayment> paymentPage = paymentRepository.findByPaymentDateBetween(startDate, endDate, pageable);
            result.put("payments", paymentPage.getContent());
            result.put("total", paymentPage.getTotalElements());
            result.put("pages", paymentPage.getTotalPages());
        } else {
            Page<FinancePayment> allPayments = paymentRepository.findAll(pageable);
            result.put("payments", allPayments.getContent());
            result.put("total", allPayments.getTotalElements());
            result.put("pages", allPayments.getTotalPages());
        }

        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", result.getOrDefault("pages", 1));

        return result;
    }

    /**
     * 审批财务支付
     */
    @Transactional
    public PaymentGenerationResult approvePayment(Long paymentId, Integer approve, String comment) {
        try {
            FinancePayment payment = getPaymentById(paymentId);

            if (!payment.getPaymentStatus().equals(1)) { // 只有待审批才能审批
                return new PaymentGenerationResult(false, "支付状态不正确，无法审批");
            }

            payment.setPaymentStatus(approve == 1 ? 2 : 4); // 已批准或已拒绝
            payment.setApprovalUserId(getCurrentUserId());
            payment.setApprovalUserName(getCurrentUserName());
            payment.setApprovalTime(LocalDateTime.now());
            payment.setApprovalComment(comment);
            payment.setUpdateBy(getCurrentUserId());
            payment.setUpdateTime(LocalDateTime.now());

            paymentRepository.save(payment);

            return new PaymentGenerationResult(true, approve == 1 ? "支付审批成功" : "支付审批拒绝");

        } catch (Exception e) {
            return new PaymentGenerationResult(false, "支付审批失败: " + e.getMessage());
        }
    }

    /**
     * 批量审批支付
     */
    @Transactional
    public Map<String, Object> batchApprovePayments(List<Long> paymentIds, Integer approve, String comment) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (Long paymentId : paymentIds) {
            PaymentGenerationResult approveResult = approvePayment(paymentId, approve, comment);
            if (approveResult.isSuccess()) {
                successCount++;
            } else {
                failCount++;
            }
        }

        result.put("totalCount", paymentIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("message", String.format("批量支付审批完成：成功%d个，失败%d个", successCount, failCount));

        return result;
    }

    /**
     * 执行支付
     */
    @Transactional
    public PaymentGenerationResult executePayment(Long paymentId) {
        try {
            FinancePayment payment = getPaymentById(paymentId);

            if (!payment.getPaymentStatus().equals(2)) { // 只有已批准才能执行
                return new PaymentGenerationResult(false, "支付状态不正确，无法执行支付");
            }

            payment.setPaymentStatus(3); // 已支付
            payment.setActualPaymentDate(LocalDate.now());
            payment.setUpdateBy(getCurrentUserId());
            payment.setUpdateTime(LocalDateTime.now());

            // TODO: 集成银行支付接口
            // TODO: 发送支付通知

            paymentRepository.save(payment);

            return new PaymentGenerationResult(true, "支付执行成功");

        } catch (Exception e) {
            return new PaymentGenerationResult(false, "支付执行失败: " + e.getMessage());
        }
    }

    /**
     * 取消支付
     */
    @Transactional
    public PaymentGenerationResult cancelPayment(Long paymentId, String cancelReason) {
        try {
            FinancePayment payment = getPaymentById(paymentId);

            if (payment.getPaymentStatus().equals(3)) {
                return new PaymentGenerationResult(false, "已支付的支付无法取消");
            }

            payment.setPaymentStatus(4); // 已取消
            payment.setPaymentReason(cancelReason);
            payment.setUpdateBy(getCurrentUserId());
            payment.setUpdateTime(LocalDateTime.now());

            paymentRepository.save(payment);

            return new PaymentGenerationResult(true, "支付取消成功");

        } catch (Exception e) {
            return new PaymentGenerationResult(false, "支付取消失败: " + e.getMessage());
        }
    }

    /**
     * 生成支付编号
     */
    private String generatePaymentNo(String businessType) {
        LocalDate now = LocalDate.now();
        String dateStr = now.getYear() + String.format("%02d", now.getMonthValue() + 1) + String.format("%02d", now.getDayOfMonth());
        return businessType.toUpperCase() + "-" + dateStr + "-" + System.currentTimeMillis() % 10000;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        return 1L; // 暂时返回固定ID，实际应从安全上下文获取
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUserName() {
        return "系统管理员"; // 暂时返回固定名称，实际应从安全上下文获取
    }

    /**
     * 获取当前部门名称
     */
    private String getCurrentDepartmentName() {
        return "财务部"; // 暂时返回固定名称，实际应从系统配置获取
    }
}