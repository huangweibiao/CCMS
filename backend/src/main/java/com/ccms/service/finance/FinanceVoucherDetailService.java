package com.ccms.service.finance;

import com.ccms.entity.finance.FinanceVoucherDetail;
import com.ccms.repository.finance.FinanceVoucherDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 财务凭证明细管理服务
 */
@Service
public class FinanceVoucherDetailService {

    @Autowired
    private FinanceVoucherDetailRepository voucherDetailRepository;

    /**
     * 根据凭证ID查询明细
     */
    public List<FinanceVoucherDetail> getVoucherDetailsByVoucherId(Long voucherId) {
        return voucherDetailRepository.findByVoucherId(voucherId);
    }

    /**
     * 创建凭证明细
     */
    @Transactional
    public FinanceVoucherDetail createVoucherDetail(FinanceVoucherDetail detail) {
        return voucherDetailRepository.save(detail);
    }

    /**
     * 批量创建凭证明细
     */
    @Transactional
    public List<FinanceVoucherDetail> batchCreateVoucherDetails(List<FinanceVoucherDetail> details) {
        return voucherDetailRepository.saveAll(details);
    }

    /**
     * 更新凭证明细
     */
    @Transactional
    public FinanceVoucherDetail updateVoucherDetail(FinanceVoucherDetail detail) {
        FinanceVoucherDetail existingDetail = voucherDetailRepository.findById(detail.getId())
                .orElseThrow(() -> new RuntimeException("凭证明细不存在"));

        existingDetail.setAccountCode(detail.getAccountCode());
        existingDetail.setAccountName(detail.getAccountName());
        existingDetail.setDebitAmount(detail.getDebitAmount());
        existingDetail.setCreditAmount(detail.getCreditAmount());
        existingDetail.setDescription(detail.getDescription());
        existingDetail.setCostCenter(detail.getCostCenter());
        existingDetail.setDepartment(detail.getDepartment());

        return voucherDetailRepository.save(existingDetail);
    }

    /**
     * 删除凭证明细
     */
    @Transactional
    public void deleteVoucherDetail(Long id) {
        voucherDetailRepository.deleteById(id);
    }

    /**
     * 根据凭证ID查询明细数量
     */
    public Long countByVoucherId(Long voucherId) {
        return voucherDetailRepository.findByVoucherId(voucherId).stream().count();
    }
}