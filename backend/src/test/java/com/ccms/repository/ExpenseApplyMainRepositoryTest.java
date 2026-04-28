package com.ccms.repository;

import com.ccms.entity.ExpenseApplyMain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ExpenseApplyMainRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseApplyMainRepository expenseApplyMainRepository;

    @Test
    public void testSaveAndFindById() {
        // 准备测试数据
        ExpenseApplyMain application = createTestApplication("APP202312001", 1001L, new BigDecimal("5000.00"));
        
        // 保存申请
        ExpenseApplyMain savedApplication = expenseApplyMainRepository.save(application);
        
        // 验证保存成功
        assertThat(savedApplication.getId()).isNotNull();
        assertThat(savedApplication.getApplyNumber()).isEqualTo("APP202312001");
        
        // 根据ID查询
        Optional<ExpenseApplyMain> foundApplication = expenseApplyMainRepository.findById(savedApplication.getId());
        assertThat(foundApplication).isPresent();
        assertThat(foundApplication.get().getApplyAmount()).isEqualTo(new BigDecimal("5000.00"));
    }

    @Test
    public void testFindByApplyNumber() {
        // 准备多个测试申请
        ExpenseApplyMain application1 = createTestApplication("APP202312001", 1001L, new BigDecimal("5000.00"));
        ExpenseApplyMain application2 = createTestApplication("APP202312002", 1002L, new BigDecimal("3000.00"));
        
        entityManager.persist(application1);
        entityManager.persist(application2);
        entityManager.flush();
        
        // 根据申请单号查询
        Optional<ExpenseApplyMain> foundApplication = expenseApplyMainRepository.findByApplyNumber("APP202312001");
        assertThat(foundApplication).isPresent();
        assertThat(foundApplication.get().getApplyAmount()).isEqualTo(new BigDecimal("5000.00"));
    }

    @Test
    public void testFindByApplyUserId() {
        ExpenseApplyMain application1 = createTestApplication("APP202312001", 1001L, new BigDecimal("5000.00"));
        application1.setApplyUserId(2001L);
        
        ExpenseApplyMain application2 = createTestApplication("APP202312002", 1001L, new BigDecimal("3000.00"));
        application2.setApplyUserId(2001L);
        
        ExpenseApplyMain application3 = createTestApplication("APP202312003", 1002L, new BigDecimal("2000.00"));
        application3.setApplyUserId(2002L);
        
        entityManager.persist(application1);
        entityManager.persist(application2);
        entityManager.persist(application3);
        entityManager.flush();
        
        // 根据申请人ID查询
        List<ExpenseApplyMain> userApplications = expenseApplyMainRepository.findByApplyUserId(2001L);
        assertThat(userApplications).hasSize(2);
        assertThat(userApplications).extracting("applyNumber").containsExactlyInAnyOrder("APP202312001", "APP202312002");
    }

    @Test
    public void testCalculateTotalApplyAmountByDeptAndYear() {
        ExpenseApplyMain application1 = createTestApplication("APP202312001", 1001L, new BigDecimal("5000.00"));
        application1.setStatus(1); // 已审核状态
        application1.setApplyDate(java.sql.Date.valueOf(LocalDate.of(2023, 12, 1)));
        
        ExpenseApplyMain application2 = createTestApplication("APP202312002", 1001L, new BigDecimal("3000.00"));
        application2.setStatus(1); // 已审核状态
        application2.setApplyDate(java.sql.Date.valueOf(LocalDate.of(2023, 12, 15)));
        
        ExpenseApplyMain application3 = createTestApplication("APP202412001", 1001L, new BigDecimal("4000.00"));
        application3.setStatus(1); // 已审核状态，不同年份
        application3.setApplyDate(java.sql.Date.valueOf(LocalDate.of(2024, 1, 1)));
        
        ExpenseApplyMain application4 = createTestApplication("APP202312003", 1001L, new BigDecimal("2000.00"));
        application4.setStatus(0); // 待审核状态，不应计入总额
        application4.setApplyDate(java.sql.Date.valueOf(LocalDate.of(2023, 12, 20)));
        
        entityManager.persist(application1);
        entityManager.persist(application2);
        entityManager.persist(application3);
        entityManager.persist(application4);
        entityManager.flush();
        
        // 计算部门年度申请总额
        BigDecimal totalAmount = expenseApplyMainRepository.calculateTotalApplyAmountByDeptAndYear(1001L, 2023);
        assertThat(totalAmount).isEqualTo(new BigDecimal("8000.00"));
    }

    @Test
    public void testFindByApplyDateBetween() {
        Date startDate = java.sql.Date.valueOf(LocalDate.of(2023, 12, 1));
        Date endDate = java.sql.Date.valueOf(LocalDate.of(2023, 12, 31));
        
        ExpenseApplyMain application1 = createTestApplication("APP202312001", 1001L, new BigDecimal("5000.00"));
        application1.setApplyDate(java.sql.Date.valueOf(LocalDate.of(2023, 12, 10)));
        
        ExpenseApplyMain application2 = createTestApplication("APP202312002", 1001L, new BigDecimal("3000.00"));
        application2.setApplyDate(java.sql.Date.valueOf(LocalDate.of(2023, 12, 20)));
        
        ExpenseApplyMain application3 = createTestApplication("APP202312015", 1001L, new BigDecimal("2000.00"));
        application3.setApplyDate(java.sql.Date.valueOf(LocalDate.of(2023, 11, 30))); // 在日期范围之前
        
        ExpenseApplyMain application4 = createTestApplication("APP202401001", 1001L, new BigDecimal("4000.00"));
        application4.setApplyDate(java.sql.Date.valueOf(LocalDate.of(2024, 1, 1))); // 在日期范围之后
        
        entityManager.persist(application1);
        entityManager.persist(application2);
        entityManager.persist(application3);
        entityManager.persist(application4);
        entityManager.flush();
        
        // 根据申请日期范围查询
        List<ExpenseApplyMain> dateRangeApplications = expenseApplyMainRepository.findByApplyDateBetween(startDate, endDate);
        assertThat(dateRangeApplications).hasSize(2);
        assertThat(dateRangeApplications).extracting("applyNumber").containsExactlyInAnyOrder("APP202312001", "APP202312002");
    }

    @Test
    public void testUpdateStatus() {
        ExpenseApplyMain application = createTestApplication("APP202312001", 1001L, new BigDecimal("5000.00"));
        application.setStatus(0); // 初始状态：待审核
        
        ExpenseApplyMain savedApplication = expenseApplyMainRepository.save(application);
        entityManager.flush();
        
        // 更新申请状态
        int updatedCount = expenseApplyMainRepository.updateStatus(savedApplication.getId(), 1, 0);
        assertThat(updatedCount).isEqualTo(1);
        
        // 验证状态已更新
        Optional<ExpenseApplyMain> updatedApplication = expenseApplyMainRepository.findById(savedApplication.getId());
        assertThat(updatedApplication).isPresent();
        assertThat(updatedApplication.get().getStatus()).isEqualTo(1);
    }

    @Test
    public void testSoftDeleteFunctionality() {
        ExpenseApplyMain application = createTestApplication("APP202312001", 1001L, new BigDecimal("5000.00"));
        
        ExpenseApplyMain savedApplication = expenseApplyMainRepository.save(application);
        
        // 软删除申请
        expenseApplyMainRepository.softDelete(savedApplication);
        entityManager.flush();
        
        // 验证软删除成功
        Optional<ExpenseApplyMain> deletedApplication = expenseApplyMainRepository.findById(savedApplication.getId());
        assertThat(deletedApplication).isPresent();
        assertThat(deletedApplication.get().getDelFlag()).isEqualTo(1);
        
        // 验证active查询不会返回已删除的申请
        Optional<ExpenseApplyMain> activeApplication = expenseApplyMainRepository.findActiveById(savedApplication.getId());
        assertThat(activeApplication).isEmpty();
    }

    @Test
    public void testFindAllActive() {
        // 创建活跃和已删除的申请
        ExpenseApplyMain activeApplication1 = createTestApplication("APP202312001", 1001L, new BigDecimal("5000.00"));
        activeApplication1.setDelFlag(0);
        
        ExpenseApplyMain activeApplication2 = createTestApplication("APP202312002", 1002L, new BigDecimal("3000.00"));
        activeApplication2.setDelFlag(0);
        
        ExpenseApplyMain deletedApplication = createTestApplication("APP202312003", 1003L, new BigDecimal("2000.00"));
        deletedApplication.setDelFlag(1);
        
        entityManager.persist(activeApplication1);
        entityManager.persist(activeApplication2);
        entityManager.persist(deletedApplication);
        entityManager.flush();
        
        // 测试只返回活跃申请
        List<ExpenseApplyMain> activeApplications = expenseApplyMainRepository.findAllActive();
        assertThat(activeApplications).hasSize(2);
        assertThat(activeApplications).extracting("applyNumber").containsExactlyInAnyOrder("APP202312001", "APP202312002");
    }

    private ExpenseApplyMain createTestApplication(String applyNumber, Long deptId, BigDecimal applyAmount) {
        ExpenseApplyMain application = new ExpenseApplyMain();
        application.setApplyNumber(applyNumber);
        application.setDeptId(deptId);
        application.setApplyUserId(2001L);
        application.setApplyAmount(applyAmount);
        application.setApplyDate(java.sql.Date.valueOf(LocalDate.now()));
        application.setExpenseType(1);
        application.setDescription("测试费用申请");
        application.setStatus(0);
        application.setDelFlag(0);
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        return application;
    }
}