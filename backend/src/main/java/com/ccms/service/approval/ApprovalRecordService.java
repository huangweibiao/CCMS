package com.ccms.service.approval;

import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.repository.ApprovalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批记录服务类
 */
@Service
public class ApprovalRecordService {

    @Autowired
    private ApprovalRecordRepository approvalRecordRepository;

    /**
     * 根据业务单据ID和类型查找审批记录
     */
    public List<ApprovalRecord> findByBusinessIdAndType(Long businessId, String businessType) {
        return approvalRecordRepository.findByBusinessIdAndBusinessTypeOrderByApprovalStep(businessId, businessType);
    }

    /**
     * 保存审批记录
     */
    public ApprovalRecord save(ApprovalRecord record) {
        return approvalRecordRepository.save(record);
    }

    /**
     * 根据ID查找审批记录
     */
    public ApprovalRecord findById(Long id) {
        return approvalRecordRepository.findById(id).orElse(null);
    }

    /**
     * 更新审批记录状态
     */
    public void updateRecordStatus(Long recordId, String status, String comment) {
        ApprovalRecord record = findById(recordId);
        if (record != null) {
            record.setApprovalResult(status);
            record.setApprovalComment(comment);
            save(record);
        }
    }

    /**
     * 更新审批记录
     */
    public void update(ApprovalRecord record) {
        approvalRecordRepository.save(record);
    }

    /**
     * 查找超时审批记录
     */
    public List<ApprovalRecord> findTimeoutRecords(LocalDateTime deadline) {
        return approvalRecordRepository.findTimeoutRecords(deadline);
    }
}