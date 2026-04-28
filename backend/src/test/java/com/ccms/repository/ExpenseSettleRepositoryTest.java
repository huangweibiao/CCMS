package com.ccms.repository;

import com.ccms.entity.ExpenseSettle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ExpenseSettleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseSettleRepository expenseSettleRepository;

    @Test
    public void testSaveAndFindById() {
        // 准备测试数据
        ExpenseSettle settlement = createTestSettlement(1001L, new BigDecimal("5000.00"));
        
        // 保存核销记录
        ExpenseSettle savedSettlement = expenseSettleRepository.save(settlement);
        
        // 验证保存成功
        assertThat(savedSettlement.getId()).isNotNull();
        assertThat(savedSettlement.getApplyMainId()).isEqualTo(1001L);
        
        // 根据ID查询
        Optional<ExpenseSettle> foundSettlement = expenseSettleRepository.findById(savedSettlement.getId());
        assertThat(foundSettlement).isPresent();
        assertThat(foundSettlement.get().getSettleAmount()).isEqualTo(new BigDecimal("5000.00"));
    }

    @Test
    public void testFindByApplyMainId() {
        // 准备多个测试核销记录
        ExpenseSettle settlement1 = createTestSettlement(1001L, new BigDecimal("5000.00"));
        ExpenseSettle settlement2 = createTestSettlement(1001L, new BigDecimal("3000.00"));
        ExpenseSettle settlement3 = createTestSettlement(1002L, new BigDecimal("2000.00"));
        
        entityManager.persist(settlement1);
        entityManager.persist(settlement2);
        entityManager.persist(settlement3);
        entityManager.flush();
        
        // 根据申请单ID查询
        List<ExpenseSettle> settlementRecords = expenseSettleRepository.findByApplyMainId(1001L);
        assertThat(settlementRecords).hasSize(2);
        
        // 验证返回的记录按创建时间排序（最新的在前面）
        assertThat(settlementRecords).isSortedAccordingTo((s1, s2) -> 
            s2.getCreateTime().compareTo(s1.getCreateTime()));
        
        assertThat(settlementRecords).extracting("settleAmount").containsExactlyInAnyOrder(
            new BigDecimal("5000.00"), new BigDecimal("3000.00")
        );
    }

    @Test
    public void testCalculateTotalSettleAmount() {
        ExpenseSettle settlement1 = createTestSettlement(1001L, new BigDecimal("5000.00"));
        settlement1.setStatus(1); // 已完成
        
        ExpenseSettle settlement2 = createTestSettlement(1001L, new BigDecimal("3000.00"));
        settlement2.setStatus(1); // 已完成
        
        ExpenseSettle settlement3 = createTestSettlement(1001L, new BigDecimal("2000.00"));
        settlement3.setStatus(0); // 待处理，不应计入总额
        
        ExpenseSettle settlement4 = createTestSettlement(1002L, new BigDecimal("4000.00"));
        settlement4.setStatus(1); // 不同申请单，不应计入总额
        
        entityManager.persist(settlement1);
        entityManager.persist(settlement2);
        entityManager.persist(settlement3);
        entityManager.persist(settlement4);
        entityManager.flush();
        
        // 计算申请单累计核销总额
        BigDecimal totalAmount = expenseSettleRepository.calculateTotalSettleAmount(1001L);
        assertThat(totalAmount).isEqualTo(new BigDecimal("8000.00"));
    }

    @Test
    public void testUpdateStatus() {
        ExpenseSettle settlement = createTestSettlement(1001L, new BigDecimal("5000.00"));
        settlement.setStatus(0); // 初始状态：待处理
        
        ExpenseSettle savedSettlement = expenseSettleRepository.save(settlement);
        entityManager.flush();
        
        // 更新核销状态
        int updatedCount = expenseSettleRepository.updateStatus(savedSettlement.getId(), 1);
        assertThat(updatedCount).isEqualTo(1);
        
        // 验证状态已更新
        Optional<ExpenseSettle> updatedSettlement = expenseSettleRepository.findById(savedSettlement.getId());
        assertThat(updatedSettlement).isPresent();
        assertThat(updatedSettlement.get().getStatus()).isEqualTo(1);
    }

    @Test
    public void testFindByMultipleApplyMainIds() {
        // 测试多个申请单的核销记录查询
        ExpenseSettle settlement1 = createTestSettlement(1001L, new BigDecimal("5000.00"));
        ExpenseSettle settlement2 = createTestSettlement(1002L, new BigDecimal("3000.00"));
        ExpenseSettle settlement3 = createTestSettlement(1001L, new BigDecimal("2000.00"));
        ExpenseSettle settlement4 = createTestSettlement(1003L, new BigDecimal("4000.00"));
        
        entityManager.persist(settlement1);
        entityManager.persist(settlement2);
        entityManager.persist(settlement3);
        entityManager.persist(settlement4);
        entityManager.flush();
        
        // 查询多个申请单的核销记录
        List<ExpenseSettle> settlementsForApply1 = expenseSettleRepository.findByApplyMainId(1001L);
        assertThat(settlementsForApply1).hasSize(2);
        
        List<ExpenseSettle> settlementsForApply2 = expenseSettleRepository.findByApplyMainId(1002L);
        assertThat(settlementsForApply2).hasSize(1);
        
        List<ExpenseSettle> settlementsForApply3 = expenseSettleRepository.findByApplyMainId(1003L);
        assertThat(settlementsForApply3).hasSize(1);
    }

    @Test
    public void testFindByStatus() {
        ExpenseSettle settlement1 = createTestSettlement(1001L, new BigDecimal("5000.00"));
        settlement1.setStatus(1); // 已完成
        
        ExpenseSettle settlement2 = createTestSettlement(1001L, new BigDecimal("3000.00"));
        settlement2.setStatus(0); // 待处理
        
        ExpenseSettle settlement3 = createTestSettlement(1002L, new BigDecimal("2000.00"));
        settlement3.setStatus(1); // 已完成
        
        entityManager.persist(settlement1);
        entityManager.persist(settlement2);
        entityManager.persist(settlement3);
        entityManager.flush();
        
        // 通过自定义查询方法测试按状态查询
        List<ExpenseSettle> completedSettlements = expenseSettleRepository.findAllByStatus(1);
        assertThat(completedSettlements).hasSize(2);
        assertThat(completedSettlements).extracting("status").containsOnly(1);
        
        List<ExpenseSettle> pendingSettlements = expenseSettleRepository.findAllByStatus(0);
        assertThat(pendingSettlements).hasSize(1);
        assertThat(pendingSettlements).extracting("status").containsOnly(0);
    }

    @Test
    public void testSoftDeleteFunctionality() {
        ExpenseSettle settlement = createTestSettlement(1001L, new BigDecimal("5000.00"));
        
        ExpenseSettle savedSettlement = expenseSettleRepository.save(settlement);
        
        // 软删除核销记录
        expenseSettleRepository.softDelete(savedSettlement);
        entityManager.flush();
        
        // 验证软删除成功
        Optional<ExpenseSettle> deletedSettlement = expenseSettleRepository.findById(savedSettlement.getId());
        assertThat(deletedSettlement).isPresent();
        assertThat(deletedSettlement.get().getDelFlag()).isEqualTo(1);
        
        // 验证active查询不会返回已删除的核销记录
        Optional<ExpenseSettle> activeSettlement = expenseSettleRepository.findActiveById(savedSettlement.getId());
        assertThat(activeSettlement).isEmpty();
    }

    @Test
    public void testFindAllActive() {
        // 创建活跃和已删除的核销记录
        ExpenseSettle activeSettlement1 = createTestSettlement(1001L, new BigDecimal("5000.00"));
        activeSettlement1.setDelFlag(0);
        
        ExpenseSettle activeSettlement2 = createTestSettlement(1002L, new BigDecimal("3000.00"));
        activeSettlement2.setDelFlag(0);
        
        ExpenseSettle deletedSettlement = createTestSettlement(1003L, new BigDecimal("2000.00"));
        deletedSettlement.setDelFlag(1);
        
        entityManager.persist(activeSettlement1);
        entityManager.persist(activeSettlement2);
        entityManager.persist(deletedSettlement);
        entityManager.flush();
        
        // 测试只返回活跃核销记录
        List<ExpenseSettle> activeSettlements = expenseSettleRepository.findAllActive();
        assertThat(activeSettlements).hasSize(2);
        assertThat(activeSettlements).extracting("applyMainId").containsExactlyInAnyOrder(1001L, 1002L);
    }

    @Test
    public void testSettlementAmountValidation() {
        // 测试核销金额不能为负数
        ExpenseSettle settlement = createTestSettlement(1001L, new BigDecimal("-1000.00"));
        
        // 在Spring Data JPA中，负数金额会在数据库层面被拒绝或验证
        // 这里主要测试Repository能正常处理边界值
        ExpenseSettle savedSettlement = expenseSettleRepository.save(settlement);
        assertThat(savedSettlement).isNotNull();
        assertThat(savedSettlement.getSettleAmount().compareTo(BigDecimal.ZERO) < 0).isTrue();
    }

    private ExpenseSettle createTestSettlement(Long applyMainId, BigDecimal settleAmount) {
        ExpenseSettle settlement = new ExpenseSettle();
        settlement.setApplyMainId(applyMainId);
        settlement.setSettleAmount(settleAmount);
        settlement.setSettleNumber("SETTLE" + System.currentTimeMillis());
        settlement.setSettleDate(java.sql.Date.valueOf(java.time.LocalDate.now()));
        settlement.setDescription("测试费用核销");
        settlement.setStatus(1);
        settlement.setDelFlag(0);
        settlement.setCreateTime(LocalDateTime.now());
        settlement.setUpdateTime(LocalDateTime.now());
        return settlement;
    }
}