package com.ccms.repository;

import com.ccms.entity.BudgetMain;
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
public class BudgetMainRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BudgetMainRepository budgetMainRepository;

    @Test
    public void testSaveAndFindById() {
        // 准备测试数据
        BudgetMain budget = createTestBudget("BUD202312001", 2023, 1001L, new BigDecimal("500000.00"));
        
        // 保存预算
        BudgetMain savedBudget = budgetMainRepository.save(budget);
        
        // 验证保存成功
        assertThat(savedBudget.getId()).isNotNull();
        assertThat(savedBudget.getBudgetCode()).isEqualTo("BUD202312001");
        
        // 根据ID查询
        Optional<BudgetMain> foundBudget = budgetMainRepository.findById(savedBudget.getId());
        assertThat(foundBudget).isPresent();
        assertThat(foundBudget.get().getAmount()).isEqualTo(new BigDecimal("500000.00"));
    }

    @Test
    public void testFindByBudgetCode() {
        // 准备多个测试预算
        BudgetMain budget1 = createTestBudget("BUD202312001", 2023, 1001L, new BigDecimal("500000.00"));
        BudgetMain budget2 = createTestBudget("BUD202312002", 2023, 1002L, new BigDecimal("300000.00"));
        
        entityManager.persist(budget1);
        entityManager.persist(budget2);
        entityManager.flush();
        
        // 根据预算编码查询
        Optional<BudgetMain> foundBudget = budgetMainRepository.findByBudgetCode("BUD202312001");
        assertThat(foundBudget).isPresent();
        assertThat(foundBudget.get().getAmount()).isEqualTo(new BigDecimal("500000.00"));
    }

    @Test
    public void testFindByDeptId() {
        BudgetMain budget1 = createTestBudget("BUD202312001", 2023, 1001L, new BigDecimal("500000.00"));
        BudgetMain budget2 = createTestBudget("BUD202312002", 2023, 1001L, new BigDecimal("300000.00"));
        BudgetMain budget3 = createTestBudget("BUD202312003", 2023, 1002L, new BigDecimal("200000.00"));
        
        entityManager.persist(budget1);
        entityManager.persist(budget2);
        entityManager.persist(budget3);
        entityManager.flush();
        
        // 根据部门ID查询
        List<BudgetMain> deptBudgets = budgetMainRepository.findByDeptId(1001L);
        assertThat(deptBudgets).hasSize(2);
        assertThat(deptBudgets).extracting("budgetCode").containsExactlyInAnyOrder("BUD202312001", "BUD202312002");
    }

    @Test
    public void testFindByBudgetYearAndDeptId() {
        BudgetMain budget1 = createTestBudget("BUD202312001", 2023, 1001L, new BigDecimal("500000.00"));
        BudgetMain budget2 = createTestBudget("BUD202312002", 2023, 1001L, new BigDecimal("300000.00"));
        BudgetMain budget3 = createTestBudget("BUD202412001", 2024, 1001L, new BigDecimal("400000.00"));
        
        entityManager.persist(budget1);
        entityManager.persist(budget2);
        entityManager.persist(budget3);
        entityManager.flush();
        
        // 根据年份和部门ID查询
        List<BudgetMain> yearDeptBudgets = budgetMainRepository.findByBudgetYearAndDeptId(2023, 1001L);
        assertThat(yearDeptBudgets).hasSize(2);
        assertThat(yearDeptBudgets).extracting("budgetCode").containsExactlyInAnyOrder("BUD202312001", "BUD202312002");
    }

    @Test
    public void testCalculateTotalBudgetAmountByDeptAndYear() {
        BudgetMain budget1 = createTestBudget("BUD202312001", 2023, 1001L, new BigDecimal("500000.00"));
        budget1.setStatus(1); // 已审核状态
        
        BudgetMain budget2 = createTestBudget("BUD202312002", 2023, 1001L, new BigDecimal("300000.00"));
        budget2.setStatus(1); // 已审核状态
        
        BudgetMain budget3 = createTestBudget("BUD202312003", 2023, 1001L, new BigDecimal("200000.00"));
        budget3.setStatus(0); // 待审核状态，不应计入总额
        
        entityManager.persist(budget1);
        entityManager.persist(budget2);
        entityManager.persist(budget3);
        entityManager.flush();
        
        // 计算部门年度预算总额
        BigDecimal totalAmount = budgetMainRepository.calculateTotalBudgetAmountByDeptAndYear(1001L, 2023);
        assertThat(totalAmount).isEqualTo(new BigDecimal("800000.00"));
    }

    @Test
    public void testUpdateStatus() {
        BudgetMain budget = createTestBudget("BUD202312001", 2023, 1001L, new BigDecimal("500000.00"));
        budget.setStatus(0); // 初始状态：待审核
        
        BudgetMain savedBudget = budgetMainRepository.save(budget);
        entityManager.flush();
        
        // 更新预算状态
        int updatedCount = budgetMainRepository.updateStatus(savedBudget.getId(), 1, 0);
        assertThat(updatedCount).isEqualTo(1);
        
        // 验证状态已更新
        Optional<BudgetMain> updatedBudget = budgetMainRepository.findById(savedBudget.getId());
        assertThat(updatedBudget).isPresent();
        assertThat(updatedBudget.get().getStatus()).isEqualTo(1);
    }

    @Test
    public void testSoftDeleteFunctionality() {
        BudgetMain budget = createTestBudget("BUD202312001", 2023, 1001L, new BigDecimal("500000.00"));
        
        BudgetMain savedBudget = budgetMainRepository.save(budget);
        
        // 软删除预算
        budgetMainRepository.softDelete(savedBudget);
        entityManager.flush();
        
        // 验证软删除成功
        Optional<BudgetMain> deletedBudget = budgetMainRepository.findById(savedBudget.getId());
        assertThat(deletedBudget).isPresent();
        assertThat(deletedBudget.get().getDelFlag()).isEqualTo(1);
        
        // 验证active查询不会返回已删除的预算
        Optional<BudgetMain> activeBudget = budgetMainRepository.findActiveById(savedBudget.getId());
        assertThat(activeBudget).isEmpty();
    }

    @Test
    public void testFindAllActive() {
        // 创建活跃和已删除的预算
        BudgetMain activeBudget1 = createTestBudget("BUD202312001", 2023, 1001L, new BigDecimal("500000.00"));
        activeBudget1.setDelFlag(0);
        
        BudgetMain activeBudget2 = createTestBudget("BUD202312002", 2023, 1002L, new BigDecimal("300000.00"));
        activeBudget2.setDelFlag(0);
        
        BudgetMain deletedBudget = createTestBudget("BUD202312003", 2023, 1003L, new BigDecimal("200000.00"));
        deletedBudget.setDelFlag(1);
        
        entityManager.persist(activeBudget1);
        entityManager.persist(activeBudget2);
        entityManager.persist(deletedBudget);
        entityManager.flush();
        
        // 测试只返回活跃预算
        List<BudgetMain> activeBudgets = budgetMainRepository.findAllActive();
        assertThat(activeBudgets).hasSize(2);
        assertThat(activeBudgets).extracting("budgetCode").containsExactlyInAnyOrder("BUD202312001", "BUD202312002");
    }

    private BudgetMain createTestBudget(String budgetCode, int budgetYear, Long deptId, BigDecimal amount) {
        BudgetMain budget = new BudgetMain();
        budget.setBudgetCode(budgetCode);
        budget.setBudgetYear(budgetYear);
        budget.setDeptId(deptId);
        budget.setAmount(amount);
        budget.setStatus(0);
        budget.setDelFlag(0);
        budget.setCreateTime(LocalDateTime.now());
        budget.setUpdateTime(LocalDateTime.now());
        return budget;
    }
}