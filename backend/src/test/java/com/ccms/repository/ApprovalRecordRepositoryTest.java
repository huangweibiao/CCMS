package com.ccms.repository;

import com.ccms.entity.ApprovalRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ApprovalRecordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ApprovalRecordRepository approvalRecordRepository;

    @Test
    public void testSaveAndFindById() {
        // 准备测试数据
        ApprovalRecord record = createTestRecord(1001L, 1, 2001L, "PENDING");
        
        // 保存审批记录
        ApprovalRecord savedRecord = approvalRecordRepository.save(record);
        
        // 验证保存成功
        assertThat(savedRecord.getId()).isNotNull();
        assertThat(savedRecord.getBusinessId()).isEqualTo(1001L);
        
        // 根据ID查询
        Optional<ApprovalRecord> foundRecord = approvalRecordRepository.findById(savedRecord.getId());
        assertThat(foundRecord).isPresent();
        assertThat(foundRecord.get().getApprovalStatus()).isEqualTo("PENDING");
    }

    @Test
    public void testFindByBusinessIdAndBusinessType() {
        // 准备多个测试审批记录
        ApprovalRecord record1 = createTestRecord(1001L, 1, 2001L, "PENDING");
        ApprovalRecord record2 = createTestRecord(1001L, 1, 2002L, "APPROVED");
        ApprovalRecord record3 = createTestRecord(1002L, 1, 2001L, "PENDING");
        ApprovalRecord record4 = createTestRecord(1001L, 2, 2001L, "REJECTED"); // 不同业务类型
        
        entityManager.persist(record1);
        entityManager.persist(record2);
        entityManager.persist(record3);
        entityManager.persist(record4);
        entityManager.flush();
        
        // 根据业务ID和业务类型查询
        List<ApprovalRecord> businessRecords = approvalRecordRepository.findByBusinessIdAndBusinessType(1001L, 1);
        assertThat(businessRecords).hasSize(2);
        assertThat(businessRecords).extracting("approvalUserId").containsExactlyInAnyOrder(2001L, 2002L);
    }

    @Test
    public void testFindLatestRecordByBusiness() {
        // 准备时间戳不同的审批记录
        LocalDateTime earlierTime = LocalDateTime.of(2023, 12, 1, 10, 0);
        LocalDateTime laterTime = LocalDateTime.of(2023, 12, 1, 11, 0);
        
        ApprovalRecord olderRecord = createTestRecord(1001L, 1, 2001L, "PENDING");
        olderRecord.setCreateTime(earlierTime);
        
        ApprovalRecord newerRecord = createTestRecord(1001L, 1, 2002L, "APPROVED");
        newerRecord.setCreateTime(laterTime);
        
        entityManager.persist(olderRecord);
        entityManager.persist(newerRecord);
        entityManager.flush();
        
        // 查询最新的审批记录
        Optional<ApprovalRecord> latestRecord = approvalRecordRepository.findLatestRecordByBusiness(1001L, 1);
        assertThat(latestRecord).isPresent();
        assertThat(latestRecord.get().getApprovalUserId()).isEqualTo(2002L);
        assertThat(latestRecord.get().getApprovalStatus()).isEqualTo("APPROVED");
    }

    @Test
    public void testCountPendingApprovalsByUserId() {
        // 准备不同审批人和状态的记录
        ApprovalRecord pending1 = createTestRecord(1001L, 1, 2001L, "PENDING");
        ApprovalRecord pending2 = createTestRecord(1002L, 1, 2001L, "PENDING");
        ApprovalRecord approved = createTestRecord(1003L, 1, 2001L, "APPROVED");
        ApprovalRecord rejected = createTestRecord(1004L, 1, 2001L, "REJECTED");
        ApprovalRecord otherUserPending = createTestRecord(1005L, 1, 2002L, "PENDING");
        
        entityManager.persist(pending1);
        entityManager.persist(pending2);
        entityManager.persist(approved);
        entityManager.persist(rejected);
        entityManager.persist(otherUserPending);
        entityManager.flush();
        
        // 统计待审批数量
        long pendingCount = approvalRecordRepository.countPendingApprovalsByUserId(2001L);
        assertThat(pendingCount).isEqualTo(2);
        
        // 验证其他用户的待审批数量
        long otherUserPendingCount = approvalRecordRepository.countPendingApprovalsByUserId(2002L);
        assertThat(otherUserPendingCount).isEqualTo(1);
    }

    @Test
    public void testUpdateApprovalStatus() {
        ApprovalRecord record = createTestRecord(1001L, 1, 2001L, "PENDING");
        record.setApprovalComment(null);
        
        ApprovalRecord savedRecord = approvalRecordRepository.save(record);
        entityManager.flush();
        
        // 更新审批状态和审批意见
        String newComment = "费用合理，同意审批";
        int updatedCount = approvalRecordRepository.updateApprovalStatus(savedRecord.getId(), 2, newComment);
        assertThat(updatedCount).isEqualTo(1);
        
        // 验证状态已更新
        Optional<ApprovalRecord> updatedRecord = approvalRecordRepository.findById(savedRecord.getId());
        assertThat(updatedRecord).isPresent();
        assertThat(updatedRecord.get().getApprovalStatusNum()).isEqualTo(2);
        assertThat(updatedRecord.get().getApprovalComment()).isEqualTo(newComment);
    }

    @Test
    public void testFindByApprovalUserId() {
        ApprovalRecord user1Record1 = createTestRecord(1001L, 1, 2001L, "PENDING");
        ApprovalRecord user1Record2 = createTestRecord(1002L, 1, 2001L, "APPROVED");
        ApprovalRecord user2Record = createTestRecord(1003L, 1, 2002L, "PENDING");
        
        entityManager.persist(user1Record1);
        entityManager.persist(user1Record2);
        entityManager.persist(user2Record);
        entityManager.flush();
        
        // 根据审批人ID查询
        List<ApprovalRecord> user1Records = approvalRecordRepository.findByApprovalUserId(2001L);
        assertThat(user1Records).hasSize(2);
        assertThat(user1Records).extracting("businessId").containsExactlyInAnyOrder(1001L, 1002L);
        
        List<ApprovalRecord> user2Records = approvalRecordRepository.findByApprovalUserId(2002L);
        assertThat(user2Records).hasSize(1);
        assertThat(user2Records.get(0).getBusinessId()).isEqualTo(1003L);
    }

    @Test
    public void testFindByApprovalStatus() {
        ApprovalRecord pending1 = createTestRecord(1001L, 1, 2001L, "PENDING");
        ApprovalRecord pending2 = createTestRecord(1002L, 1, 2002L, "PENDING");
        ApprovalRecord approved = createTestRecord(1003L, 1, 2001L, "APPROVED");
        ApprovalRecord rejected = createTestRecord(1004L, 1, 2001L, "REJECTED");
        
        entityManager.persist(pending1);
        entityManager.persist(pending2);
        entityManager.persist(approved);
        entityManager.persist(rejected);
        entityManager.flush();
        
        // 根据审批状态查询
        List<ApprovalRecord> pendingRecords = approvalRecordRepository.findByApprovalStatus("PENDING");
        assertThat(pendingRecords).hasSize(2);
        assertThat(pendingRecords).extracting("approvalStatus").containsOnly("PENDING");
        
        List<ApprovalRecord> approvedRecords = approvalRecordRepository.findByApprovalStatus("APPROVED");
        assertThat(approvedRecords).hasSize(1);
        assertThat(approvedRecords.get(0).getBusinessId()).isEqualTo(1003L);
        
        List<ApprovalRecord> rejectedRecords = approvalRecordRepository.findByApprovalStatus("REJECTED");
        assertThat(rejectedRecords).hasSize(1);
        assertThat(rejectedRecords.get(0).getBusinessId()).isEqualTo(1004L);
    }

    @Test
    public void testFindByBusinessType() {
        ApprovalRecord type1Record1 = createTestRecord(1001L, 1, 2001L, "PENDING");
        ApprovalRecord type1Record2 = createTestRecord(1002L, 1, 2002L, "APPROVED");
        ApprovalRecord type2Record = createTestRecord(1003L, 2, 2001L, "PENDING");
        
        entityManager.persist(type1Record1);
        entityManager.persist(type1Record2);
        entityManager.persist(type2Record);
        entityManager.flush();
        
        // 根据业务类型查询
        List<ApprovalRecord> type1Records = approvalRecordRepository.findByBusinessType(1);
        assertThat(type1Records).hasSize(2);
        assertThat(type1Records).extracting("businessId").containsExactlyInAnyOrder(1001L, 1002L);
        
        List<ApprovalRecord> type2Records = approvalRecordRepository.findByBusinessType(2);
        assertThat(type2Records).hasSize(1);
        assertThat(type2Records.get(0).getBusinessId()).isEqualTo(1003L);
    }

    @Test
    public void testSoftDeleteFunctionality() {
        ApprovalRecord record = createTestRecord(1001L, 1, 2001L, "PENDING");
        
        ApprovalRecord savedRecord = approvalRecordRepository.save(record);
        
        // 软删除审批记录
        approvalRecordRepository.softDelete(savedRecord);
        entityManager.flush();
        
        // 验证软删除成功
        Optional<ApprovalRecord> deletedRecord = approvalRecordRepository.findById(savedRecord.getId());
        assertThat(deletedRecord).isPresent();
        assertThat(deletedRecord.get().getDelFlag()).isEqualTo(1);
        
        // 验证active查询不会返回已删除的审批记录
        Optional<ApprovalRecord> activeRecord = approvalRecordRepository.findActiveById(savedRecord.getId());
        assertThat(activeRecord).isEmpty();
    }

    @Test
    public void testFindAllActive() {
        // 创建活跃和已删除的审批记录
        ApprovalRecord activeRecord1 = createTestRecord(1001L, 1, 2001L, "PENDING");
        activeRecord1.setDelFlag(0);
        
        ApprovalRecord activeRecord2 = createTestRecord(1002L, 1, 2002L, "APPROVED");
        activeRecord2.setDelFlag(0);
        
        ApprovalRecord deletedRecord = createTestRecord(1003L, 1, 2001L, "REJECTED");
        deletedRecord.setDelFlag(1);
        
        entityManager.persist(activeRecord1);
        entityManager.persist(activeRecord2);
        entityManager.persist(deletedRecord);
        entityManager.flush();
        
        // 测试只返回活跃审批记录
        List<ApprovalRecord> activeRecords = approvalRecordRepository.findAllActive();
        assertThat(activeRecords).hasSize(2);
        assertThat(activeRecords).extracting("businessId").containsExactlyInAnyOrder(1001L, 1002L);
    }

    @Test
    public void testApprovalWorkflowScenario() {
        // 模拟一个完整的审批流程场景
        // 第一步：提交申请，创建第一个审批节点
        ApprovalRecord step1 = createTestRecord(1001L, 1, 2001L, "PENDING");
        step1.setApprovalStep(1);
        step1.setApprovalNode("部门经理审批");
        
        // 第二步：部门经理审批通过，创建第二个审批节点
        ApprovalRecord step2 = createTestRecord(1001L, 1, 2002L, "PENDING");
        step2.setApprovalStep(2);
        step2.setApprovalNode("财务总监审批");
        
        entityManager.persist(step1);
        entityManager.persist(step2);
        entityManager.flush();
        
        // 查询特定业务的所有审批步骤
        List<ApprovalRecord> workflowRecords = approvalRecordRepository.findByBusinessIdAndBusinessType(1001L, 1);
        assertThat(workflowRecords).hasSize(2);
        assertThat(workflowRecords).extracting("approvalStep").containsExactlyInAnyOrder(1, 2);
        
        // 验证步骤排序
        assertThat(workflowRecords).isSortedAccordingTo((r1, r2) -> 
            Integer.compare(r1.getApprovalStep(), r2.getApprovalStep()));
    }

    private ApprovalRecord createTestRecord(Long businessId, Integer businessType, Long approvalUserId, String approvalStatus) {
        ApprovalRecord record = new ApprovalRecord();
        record.setBusinessId(businessId);
        record.setBusinessType(businessType);
        record.setApprovalUserId(approvalUserId);
        record.setApprovalStatus(approvalStatus);
        record.setApprovalStatusNum(approvalStatus.equals("PENDING") ? 0 : approvalStatus.equals("APPROVED") ? 2 : 3);
        record.setApprovalStep(1);
        record.setApprovalNode("初始审批");
        record.setDelFlag(0);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        return record;
    }
}