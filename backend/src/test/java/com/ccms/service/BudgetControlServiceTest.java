package com.ccms.service;

import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetMain;
import com.ccms.repository.budget.BudgetDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 预算控制服务单元测试
 */
class BudgetControlServiceTest extends BaseServiceTest {

    @Mock
    private BudgetDetailRepository budgetDetailRepository;

    @InjectMocks
    private BudgetControlService budgetControlService;

    private BudgetMain testBudgetMain;
    private BudgetDetail testBudgetDetail;

    @BeforeEach
    void setUp() {
        testBudgetMain = createTestBudgetMain();
        testBudgetDetail = createTestBudgetDetail();
    }

    @Test
    void shouldCheckBudgetAvailabilitySuccessfully() {
        // Given
        testBudgetDetail.setAvailableAmount(new BigDecimal("5000.00"));

        // When
        boolean result = budgetControlService.checkBudgetAvailability(
                testBudgetMain, testBudgetDetail, new BigDecimal("3000.00"));

        // Then
        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenBudgetInsufficient() {
        // Given
        testBudgetDetail.setAvailableAmount(new BigDecimal("1000.00"));

        // When
        boolean result = budgetControlService.checkBudgetAvailability(
                testBudgetMain, testBudgetDetail, new BigDecimal("3000.00"));

        // Then
        assertFalse(result);
    }

    @Test
    void shouldFreezeBudgetAmountSuccessfully() {
        // Given
        testBudgetDetail.setAvailableAmount(new BigDecimal("5000.00"));
        testBudgetDetail.setFrozenAmount(new BigDecimal("0.00"));
        when(budgetDetailRepository.save(any(BudgetDetail.class))).thenReturn(testBudgetDetail);

        // When
        boolean result = budgetControlService.freezeBudgetAmount(testBudgetDetail, new BigDecimal("2000.00"));

        // Then
        assertTrue(result);
    }

    @Test
    void shouldUnfreezeBudgetAmountSuccessfully() {
        // Given
        testBudgetDetail.setAvailableAmount(new BigDecimal("3000.00"));
        testBudgetDetail.setFrozenAmount(new BigDecimal("2000.00"));
        when(budgetDetailRepository.save(any(BudgetDetail.class))).thenReturn(testBudgetDetail);

        // When
        boolean result = budgetControlService.unfreezeBudgetAmount(testBudgetDetail, new BigDecimal("1000.00"));

        // Then
        assertTrue(result);
    }

    @Test
    void shouldDeductBudgetAmountSuccessfully() {
        // Given
        testBudgetDetail.setFrozenAmount(new BigDecimal("2000.00"));
        testBudgetDetail.setUsedAmount(new BigDecimal("1000.00"));
        when(budgetDetailRepository.save(any(BudgetDetail.class))).thenReturn(testBudgetDetail);

        // When
        boolean result = budgetControlService.deductBudgetAmount(testBudgetDetail, new BigDecimal("1000.00"));

        // Then
        assertTrue(result);
    }

    @Test
    void shouldReleaseBudgetAmountSuccessfully() {
        // Given
        testBudgetDetail.setUsedAmount(new BigDecimal("3000.00"));
        testBudgetDetail.setAvailableAmount(new BigDecimal("2000.00"));
        when(budgetDetailRepository.save(any(BudgetDetail.class))).thenReturn(testBudgetDetail);

        // When
        boolean result = budgetControlService.releaseBudgetAmount(testBudgetDetail, new BigDecimal("1000.00"));

        // Then
        assertTrue(result);
    }

    @Test
    void shouldAdjustBudgetBalanceSuccessfully() {
        // Given
        testBudgetDetail.setAvailableAmount(new BigDecimal("5000.00"));
        when(budgetDetailRepository.save(any(BudgetDetail.class))).thenReturn(testBudgetDetail);

        // When
        boolean result = budgetControlService.adjustBudgetBalance(testBudgetDetail, new BigDecimal("1000.00"));

        // Then
        assertTrue(result);
    }

    private BudgetMain createTestBudgetMain() {
        BudgetMain main = new BudgetMain();
        main.setId(1L);
        main.setBudgetNo("BUD20250001");
        main.setTotalAmount(new BigDecimal("100000.00"));
        return main;
    }

    private BudgetDetail createTestBudgetDetail() {
        BudgetDetail detail = new BudgetDetail();
        detail.setId(1L);
        detail.setBudgetId(1L);
        detail.setTotalAmount(new BigDecimal("50000.00"));
        detail.setUsedAmount(new BigDecimal("10000.00"));
        detail.setFrozenAmount(new BigDecimal("5000.00"));
        detail.setAvailableAmount(new BigDecimal("35000.00"));
        return detail;
    }
}
