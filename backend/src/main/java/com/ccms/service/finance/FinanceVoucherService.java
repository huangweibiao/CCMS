package com.ccms.service.finance;

import com.ccms.entity.finance.FinanceVoucher;
import com.ccms.entity.finance.FinanceVoucherDetail;
import com.ccms.repository.finance.FinanceVoucherRepository;
import com.ccms.repository.finance.FinanceVoucherDetailRepository;
import com.ccms.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 财务凭证生成服务
 * 核心功能：凭证生成、审核、记账、红冲
 */
@Service
public class FinanceVoucherService {

    @Autowired
    private FinanceVoucherRepository voucherRepository;

    @Autowired
    private FinanceVoucherDetailRepository voucherDetailRepository;

    /**
     * 凭证生成结果类
     */
    public static class VoucherGenerationResult {
        private boolean success;
        private String message;
        private Long voucherId;
        private String voucherNo;

        public VoucherGenerationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public VoucherGenerationResult(boolean success, String message, Long voucherId, String voucherNo) {
            this.success = success;
            this.message = message;
            this.voucherId = voucherId;
            this.voucherNo = voucherNo;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Long getVoucherId() { return voucherId; }
        public String getVoucherNo() { return voucherNo; }
    }

    /**
     * 凭证模板内部表示类
     */
    public static class VoucherTemplateInfo {
        private Long id;
        private String templateCode;
        private String templateName;
        private String voucherType;
        private String businessType;
        private String templateContent;

        public VoucherTemplateInfo() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        public String getVoucherType() { return voucherType; }
        public void setVoucherType(String voucherType) { this.voucherType = voucherType; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getTemplateContent() { return templateContent; }
        public void setTemplateContent(String templateContent) { this.templateContent = templateContent; }
    }

    /**
     * 根据业务单据自动生成凭证
     */
    @Transactional
    public VoucherGenerationResult generateVoucher(String businessType, Long businessId, String businessNo, BigDecimal businessAmount) {
        try {
            // 创建凭证主记录
            FinanceVoucher voucher = new FinanceVoucher();
            voucher.setVoucherNo(generateVoucherNo(businessType, businessId));
            voucher.setBusinessType(businessType);
            voucher.setBusinessId(businessId);
            voucher.setBusinessNo(businessNo);
            voucher.setVoucherDate(LocalDate.now());
            voucher.setAmount(businessAmount);
            voucher.setStatus(1);
            voucher.setVoucherStatus(1);
            voucher.setAccountingDate(LocalDate.now());
            voucher.setSummary("自动生成凭证");

            voucher.setCreateBy(getCurrentUserId());
            voucher.setCreateTime(LocalDateTime.now());

            FinanceVoucher savedVoucher = voucherRepository.save(voucher);

            return new VoucherGenerationResult(true, "凭证生成成功", savedVoucher.getId(), savedVoucher.getVoucherNo());

        } catch (Exception e) {
            return new VoucherGenerationResult(false, "凭证生成失败: " + e.getMessage());
        }
    }

    /**
     * 审核凭证
     */
    @Transactional
    public VoucherGenerationResult approveVoucher(Long voucherId, Integer approve, String remark) {
        try {
            FinanceVoucher voucher = voucherRepository.findById(voucherId)
                    .orElseThrow(() -> new RuntimeException("凭证不存在"));

            if (!voucher.getStatus().equals(1)) {
                return new VoucherGenerationResult(false, "凭证状态不正确，无法审核");
            }

            voucher.setStatus(approve == 1 ? 2 : 4);
            voucher.setUpdateBy(getCurrentUserId());
            voucher.setUpdateTime(LocalDateTime.now());

            FinanceVoucher savedVoucher = voucherRepository.save(voucher);

            return new VoucherGenerationResult(true, approve == 1 ? "凭证审核通过" : "凭证审核驳回");

        } catch (Exception e) {
            return new VoucherGenerationResult(false, "凭证审核失败: " + e.getMessage());
        }
    }

    /**
     * 凭证记账
     */
    @Transactional
    public VoucherGenerationResult postingVoucher(Long voucherId) {
        try {
            FinanceVoucher voucher = voucherRepository.findById(voucherId)
                    .orElseThrow(() -> new RuntimeException("凭证不存在"));

            if (!voucher.getStatus().equals(2)) {
                return new VoucherGenerationResult(false, "凭证状态不正确，无法记账");
            }

            // 验证凭证明细
            List<FinanceVoucherDetail> details = voucherDetailRepository.findByVoucherId(voucherId);
            if (details.isEmpty()) {
                return new VoucherGenerationResult(false, "凭证明细不存在，无法记账");
            }

            // 验证借贷平衡
            BigDecimal totalDebit = details.stream()
                    .map(FinanceVoucherDetail::getDebitAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCredit = details.stream()
                    .map(FinanceVoucherDetail::getCreditAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalDebit.compareTo(totalCredit) != 0) {
                return new VoucherGenerationResult(false, "凭证借贷不平衡，无法记账");
            }

            voucher.setStatus(3);
            voucher.setVoucherStatus(1);
            voucher.setAccountingDate(LocalDate.now());
            voucher.setPostingUserId(getCurrentUserId());
            voucher.setPostingTime(LocalDateTime.now());
            voucher.setUpdateBy(getCurrentUserId());
            voucher.setUpdateTime(LocalDateTime.now());

            voucherRepository.save(voucher);

            return new VoucherGenerationResult(true, "凭证记账成功");

        } catch (Exception e) {
            return new VoucherGenerationResult(false, "凭证记账失败: " + e.getMessage());
        }
    }

    /**
     * 红冲凭证
     */
    @Transactional
    public VoucherGenerationResult reverseVoucher(Long voucherId, Long reverseVoucherId) {
        try {
            FinanceVoucher originalVoucher = voucherRepository.findById(voucherId)
                    .orElseThrow(() -> new RuntimeException("原凭证不存在"));

            // 创建红冲凭证
            FinanceVoucher reverseVoucher = new FinanceVoucher();
            reverseVoucher.setVoucherNo(generateReverseVoucherNo(originalVoucher.getVoucherNo()));
            reverseVoucher.setTemplateId(originalVoucher.getTemplateId());
            reverseVoucher.setBusinessType("REVERSE");
            reverseVoucher.setBusinessId(originalVoucher.getBusinessId());
            reverseVoucher.setBusinessNo(originalVoucher.getBusinessNo());

            // 科目对调
            reverseVoucher.setDebitAccount(originalVoucher.getCreditAccount());
            reverseVoucher.setDebitAccountName(originalVoucher.getCreditAccountName());
            reverseVoucher.setCreditAccount(originalVoucher.getDebitAccount());
            reverseVoucher.setCreditAccountName(originalVoucher.getDebitAccountName());

            // 金额取反
            reverseVoucher.setAmount(originalVoucher.getAmount().negate());

            reverseVoucher.setVoucherDate(LocalDate.now());
            reverseVoucher.setStatus(1);
            reverseVoucher.setVoucherStatus(2);

            reverseVoucher.setCreateBy(getCurrentUserId());
            reverseVoucher.setCreateTime(LocalDateTime.now());

            FinanceVoucher savedReverseVoucher = voucherRepository.save(reverseVoucher);

            // 更新原凭证状态
            originalVoucher.setVoucherStatus(2);
            originalVoucher.setUpdateBy(getCurrentUserId());
            originalVoucher.setUpdateTime(LocalDateTime.now());

            voucherRepository.save(originalVoucher);

            return new VoucherGenerationResult(true, "凭证红冲成功", savedReverseVoucher.getId(), savedReverseVoucher.getVoucherNo());

        } catch (Exception e) {
            return new VoucherGenerationResult(false, "凭证红冲失败: " + e.getMessage());
        }
    }

    /**
     * 查询凭证详情
     */
    public Map<String, Object> getVoucherDetail(Long id) {
        Map<String, Object> result = new HashMap<>();

        FinanceVoucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("凭证不存在"));

        result.put("voucher", voucher);

        List<FinanceVoucherDetail> details = voucherDetailRepository.findByVoucherId(id);
        result.put("voucherDetails", details);

        return result;
    }

    /**
     * 查询凭证列表
     */
    public Map<String, Object> getVoucherList(String businessType, String businessNo, Integer status,
                                                      LocalDate startDate, LocalDate endDate, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        List<FinanceVoucher> vouchers = new ArrayList<>();

        if (businessType != null && !businessType.isEmpty()) {
            vouchers = voucherRepository.findByBusinessType(businessType);
        } else if (businessNo != null && !businessNo.isEmpty()) {
            vouchers = voucherRepository.findByBusinessNoLike(businessNo);
        } else if (status != null) {
            vouchers = voucherRepository.findByStatus(status);
        } else if (startDate != null && endDate != null) {
            vouchers = voucherRepository.findByAccountingDateBetween(startDate, endDate);
        } else {
            int offset = (page - 1) * size;
            List<FinanceVoucher> allVouchers = voucherRepository.findAll();

            int total = allVouchers.size();
            int toIndex = Math.min(offset + size, total);
            vouchers = allVouchers.subList(offset, toIndex);

            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", (int) Math.ceil((double) total / size));
        }

        result.put("vouchers", vouchers);

        return result;
    }

    /**
     * 获取凭证统计信息
     */
    public Map<String, Object> getVoucherStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();

        List<FinanceVoucher> vouchers = voucherRepository.findByAccountingDateBetween(startDate, endDate);
        Long totalCount = voucherRepository.countByAccountingDateBetween(startDate, endDate);

        // 计算总金额
        BigDecimal totalAmount = vouchers.stream()
                .map(FinanceVoucher::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算已记账金额
        BigDecimal postedAmount = vouchers.stream()
                .filter(v -> v.getStatus().equals(3))
                .map(FinanceVoucher::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("totalCount", totalCount);
        result.put("totalAmount", totalAmount);
        result.put("postedAmount", postedAmount);
        result.put("unPostedAmount", totalAmount.subtract(postedAmount));
        result.put("startDate", startDate);
        result.put("endDate", endDate);

        return result;
    }

    /**
     * 生成凭证编号
     */
    private String generateVoucherNo(String businessType, Long businessId) {
        LocalDate now = LocalDate.now();
        String dateStr = now.getYear() + String.format("%02d", now.getMonthValue() + 1) + String.format("%02d", now.getDayOfMonth());
        return businessType.toUpperCase() + "-" + dateStr + "-" + System.currentTimeMillis() % 1000;
    }

    /**
     * 生成红冲凭证编号
     */
    private String generateReverseVoucherNo(String originalVoucherNo) {
        return "R" + originalVoucherNo;
    }

    /**
     * 获取凭证状态名称
     */
    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "草稿";
            case 1: return "已生成";
            case 2: return "已审核";
            case 3: return "已记账";
            case 4: return "已驳回";
            default: return "未知";
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        return 1L;
    }
}