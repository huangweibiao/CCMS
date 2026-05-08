package com.ccms.controller.budget;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetMain;
import com.ccms.repository.budget.BudgetDetailRepository;
import com.ccms.repository.budget.BudgetMainRepository;
import com.ccms.service.BudgetControlService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 预算控制控制器单元测试
 */
@WebMvcTest(BudgetControlController.class)
class BudgetControlControllerTest extends ControllerTestBase {

    @MockBean
    private BudgetControlService budgetControlService;

    @MockBean
    private BudgetMainRepository budgetMainRepository;

    @MockBean
    private BudgetDetailRepository budgetDetailRepository;

    private BudgetMain createTestBudgetMain(Long id) {
        BudgetMain main = new BudgetMain();
        main.setId(id);
        main.setBudgetNo("B2025-001");
        main.setBudgetName("测试预算");
        return main;
    }

    private BudgetDetail createTestBudgetDetail(Long id) {
        BudgetDetail detail = new BudgetDetail();
        detail.setId(id);
        detail.setBudgetMainId(1L);
        detail.setBudgetAmount(new BigDecimal("10000.00"));
        detail.setUsedAmount(new BigDecimal("3000.00"));
        detail.setFrozenAmount(new BigDecimal("2000.00"));
        return detail;
    }

    @Test
    void shouldCheckBudgetAvailabilitySuccess() throws Exception {
        BudgetMain main = createTestBudgetMain(1L);
        BudgetDetail detail = createTestBudgetDetail(1L);

        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(main));
        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(budgetControlService.checkBudgetAvailability(any(BudgetMain.class), any(BudgetDetail.class), any(BigDecimal.class)))
                .thenReturn(true);

        performPost("/api/budget/control/check-availability", createAmountRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void shouldReturnBadRequestWhenBudgetNotExists() throws Exception {
        when(budgetMainRepository.findById(1L)).thenReturn(Optional.empty());

        performPost("/api/budget/control/check-availability", createAmountRequest())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldFreezeBudgetAmountSuccess() throws Exception {
        BudgetDetail detail = createTestBudgetDetail(1L);
        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(budgetControlService.freezeBudgetAmount(any(BudgetDetail.class), any(BigDecimal.class)))
                .thenReturn(true);

        performPost("/api/budget/control/freeze", createAmountRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("预算金额冻结成功"));
    }

    @Test
    void shouldUnfreezeBudgetAmountSuccess() throws Exception {
        BudgetDetail detail = createTestBudgetDetail(1L);
        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(budgetControlService.unfreezeBudgetAmount(any(BudgetDetail.class), any(BigDecimal.class)))
                .thenReturn(true);

        performPost("/api/budget/control/unfreeze", createAmountRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("预算金额解冻成功"));
    }

    @Test
    void shouldDeductBudgetAmountSuccess() throws Exception {
        BudgetDetail detail = createTestBudgetDetail(1L);
        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(budgetControlService.deductBudgetAmount(any(BudgetDetail.class), any(BigDecimal.class)))
                .thenReturn(true);

        performPost("/api/budget/control/deduct", createAmountRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("预算金额扣减成功"));
    }

    @Test
    void shouldReleaseBudgetAmountSuccess() throws Exception {
        BudgetDetail detail = createTestBudgetDetail(1L);
        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(detail));
        when(budgetControlService.releaseBudgetAmount(any(BudgetDetail.class), any(BigDecimal.class)))
                .thenReturn(true);

        performPost("/api/budget/control/release", createAmountRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("预算金额释放成功"));
    }

    @Test
    void shouldReturnBudgetDetailInfo() throws Exception {
        BudgetDetail detail = createTestBudgetDetail(1L);
        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(detail));

        performGet("/api/budget/control/detail/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.availableAmount").value(5000.00));
    }

    @Test
    void shouldReturnBudgetMainStatistics() throws Exception {
        BudgetMain main = createTestBudgetMain(1L);
        BudgetDetail detail = createTestBudgetDetail(1L);

        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(main));
        when(budgetDetailRepository.findByBudgetMainId(1L)).thenReturn(Collections.singletonList(detail));

        performGet("/api/budget/control/main/1/statistics")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalBudgetAmount").value(10000.00))
                .andExpect(jsonPath("$.totalUsedAmount").value(3000.00))
                .andExpect(jsonPath("$.totalFrozenAmount").value(2000.00))
                .andExpect(jsonPath("$.availableAmount").value(5000.00))
                .andExpect(jsonPath("$.detailsCount").value(1));
    }

    private java.util.Map<String, Object> createAmountRequest() {
        java.util.Map<String, Object> request = new java.util.HashMap<>();
        request.put("budgetMainId", 1L);
        request.put("budgetDetailId", 1L);
        request.put("amount", "1000.00");
        return request;
    }
}
