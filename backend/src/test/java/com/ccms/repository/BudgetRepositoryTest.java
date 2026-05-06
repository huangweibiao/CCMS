package com.ccms.repository;

import com.ccms.entity.budget.BudgetMain;
import com.ccms.repository.budget.BudgetMainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 预算存储层单元测试
 */
class BudgetRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private BudgetMainRepository budgetMainRepository;

    @Test
    void shouldSaveBudgetSuccessfully() {
        // Given
        BudgetMain budget = createTestBudget();

        // When
        BudgetMain saved = budgetMainRepository.save(budget);

        // Then
        assertNotNull(saved.getId());
        assertEquals("BUD20250001", saved.getBudgetNo());
    }

    @Test
    void shouldFindByIdSuccessfully() {
        // Given
        BudgetMain saved = budgetMainRepository.save(createTestBudget());

        // When
        Optional<BudgetMain> found = budgetMainRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getBudgetNo(), found.get().getBudgetNo());
    }

    @Test
    void shouldFindByBudgetYearSuccessfully() {
        // Given
        BudgetMain budget = createTestBudget();
        budget.setBudgetYear(2025);
        budgetMainRepository.save(budget);

        // When
        List<BudgetMain> results = budgetMainRepository.findByBudgetYear(2025);

        // Then
        assertFalse(results.isEmpty());
    }

    @Test
    void shouldFindByDeptIdSuccessfully() {
        // Given
        BudgetMain budget = createTestBudget();
        budget.setDeptId(1L);
        budgetMainRepository.save(budget);

        // When
        List<BudgetMain> results = budgetMainRepository.findByDeptId(1L);

        // Then
        assertFalse(results.isEmpty());
    }

    @Test
    void shouldDeleteBudgetSuccessfully() {
        // Given
        BudgetMain saved = budgetMainRepository.save(createTestBudget());

        // When
        budgetMainRepository.deleteById(saved.getId());
        Optional<BudgetMain> found = budgetMainRepository.findById(saved.getId());

        // Then
        assertFalse(found.isPresent());
    }

    private BudgetMain createTestBudget() {
        BudgetMain budget = new BudgetMain();
        budget.setBudgetNo("BUD20250001");
        budget.setBudgetName("2025年度预算");
        budget.setBudgetYear(2025);
        budget.setTotalAmount(new BigDecimal("100000.00"));
        budget.setDeptId(1L);
        budget.setStatus(1);
        budget.setCreateTime(LocalDate.now());
        return budget;
    }
}
