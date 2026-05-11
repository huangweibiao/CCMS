package com.ccms.controller.expense;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.expense.ExpenseSettle;
import com.ccms.repository.expense.ExpenseSettleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 借款结算控制器单元测试
 */
@WebMvcTest(LoanSettlementController.class)
class LoanSettlementControllerTest extends ControllerTestBase {

    @MockBean
    private ExpenseSettleRepository expenseSettleRepository;

    private ExpenseSettle createTestSettlement(Long id, String settleNo, Integer status) {
        ExpenseSettle settle = new ExpenseSettle();
        settle.setId(id);
        settle.setSettleNo(settleNo);
        settle.setExpenseApplyId(1L);
        settle.setApplyUserId(1L);
        settle.setSettleAmount(new BigDecimal("5000.00"));
        settle.setSettleDate(LocalDate.now());
        settle.setStatus(status);
        return settle;
    }

    @Test
    void shouldReturnSettlementListWhenQuerySuccess() throws Exception {
        ExpenseSettle settle = createTestSettlement(1L, "ST202501010001", 0);
        Page<ExpenseSettle> page = new PageImpl<>(
                Collections.singletonList(settle),
                PageRequest.of(0, 10),
                1
        );
        when(expenseSettleRepository.findAll(any(PageRequest.class))).thenReturn(page);

        performGet("/api/loan/settlement")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].settleNo").value("ST202501010001"));
    }

    @Test
    void shouldReturnSettlementListByUserId() throws Exception {
        ExpenseSettle settle = createTestSettlement(1L, "ST202501010001", 0);
        when(expenseSettleRepository.findByApplyUserId(1L)).thenReturn(Collections.singletonList(settle));

        performGet("/api/loan/settlement?applyUserId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].applyUserId").value(1));
    }

    @Test
    void shouldReturnSettlementListByStatus() throws Exception {
        ExpenseSettle settle = createTestSettlement(1L, "ST202501010001", 3);
        when(expenseSettleRepository.findByStatus(3)).thenReturn(Collections.singletonList(settle));

        performGet("/api/loan/settlement?status=3")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value(3));
    }

    @Test
    void shouldReturnSettlementWhenGetByIdSuccess() throws Exception {
        ExpenseSettle settle = createTestSettlement(1L, "ST202501010001", 0);
        when(expenseSettleRepository.findById(1L)).thenReturn(Optional.of(settle));

        performGet("/api/loan/settlement/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.settleNo").value("ST202501010001"));
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdNotExist() throws Exception {
        when(expenseSettleRepository.findById(999L)).thenReturn(Optional.empty());

        performGet("/api/loan/settlement/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateSettlementSuccess() throws Exception {
        ExpenseSettle settle = createTestSettlement(1L, "ST202501010001", 0);
        when(expenseSettleRepository.save(any(ExpenseSettle.class))).thenReturn(settle);

        performPost("/api/loan/settlement", settle)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value(0));
    }

    @Test
    void shouldUpdateSettlementSuccess() throws Exception {
        ExpenseSettle existing = createTestSettlement(1L, "ST202501010001", 0);
        ExpenseSettle updated = createTestSettlement(1L, "ST202501010001", 1);
        
        when(expenseSettleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseSettleRepository.save(any(ExpenseSettle.class))).thenReturn(updated);

        performPut("/api/loan/settlement/1", updated)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenUpdateNonExistent() throws Exception {
        ExpenseSettle settle = createTestSettlement(1L, "ST202501010001", 0);
        when(expenseSettleRepository.findById(999L)).thenReturn(Optional.empty());

        performPut("/api/loan/settlement/999", settle)
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteSettlementSuccess() throws Exception {
        when(expenseSettleRepository.existsById(1L)).thenReturn(true);

        performDelete("/api/loan/settlement/1")
                .andExpect(status().isOk());

        verify(expenseSettleRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeleteNonExistent() throws Exception {
        when(expenseSettleRepository.existsById(999L)).thenReturn(false);

        performDelete("/api/loan/settlement/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldConfirmSettlementSuccess() throws Exception {
        ExpenseSettle settle = createTestSettlement(1L, "ST202501010001", 2);
        when(expenseSettleRepository.findById(1L)).thenReturn(Optional.of(settle));
        when(expenseSettleRepository.save(any(ExpenseSettle.class))).thenReturn(settle);

        performPost("/api/loan/settlement/1/confirm")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("结算已确认"));
    }

    @Test
    void shouldReturnNotFoundWhenConfirmNonExistent() throws Exception {
        when(expenseSettleRepository.findById(999L)).thenReturn(Optional.empty());

        performPost("/api/loan/settlement/999/confirm")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnSettlementsByLoanId() throws Exception {
        ExpenseSettle settle1 = createTestSettlement(1L, "ST202501010001", 3);
        ExpenseSettle settle2 = createTestSettlement(2L, "ST202501010002", 3);
        when(expenseSettleRepository.findByExpenseApplyId(1L)).thenReturn(Arrays.asList(settle1, settle2));

        performGet("/api/loan/settlement/loan/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturnStatisticsByLoanId() throws Exception {
        when(expenseSettleRepository.calculateTotalSettleAmount(1L)).thenReturn(new BigDecimal("10000.00"));
        when(expenseSettleRepository.findByExpenseApplyId(1L)).thenReturn(Arrays.asList(
                createTestSettlement(1L, "ST001", 3),
                createTestSettlement(2L, "ST002", 3)
        ));

        performGet("/api/loan/settlement/statistics?loanId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSettledAmount").value(10000.00))
                .andExpect(jsonPath("$.settlementCount").value(2));
    }

    @Test
    void shouldReturnStatisticsByYear() throws Exception {
        ExpenseSettle settle = createTestSettlement(1L, "ST202501010001", 3);
        settle.setSettleDate(LocalDate.of(2025, 1, 15));
        when(expenseSettleRepository.findAll()).thenReturn(Collections.singletonList(settle));

        performGet("/api/loan/settlement/statistics?year=2025")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearSettlementCount").value(1))
                .andExpect(jsonPath("$.yearSettlementAmount").value(5000.00));
    }

    @Test
    void shouldReturnOverallStatistics() throws Exception {
        ExpenseSettle draft = createTestSettlement(1L, "ST001", 0);
        ExpenseSettle submitted = createTestSettlement(2L, "ST002", 1);
        ExpenseSettle processing = createTestSettlement(3L, "ST003", 2);
        ExpenseSettle completed = createTestSettlement(4L, "ST004", 3);
        ExpenseSettle cancelled = createTestSettlement(5L, "ST005", 4);
        
        when(expenseSettleRepository.count()).thenReturn(5L);
        when(expenseSettleRepository.findByStatus(0)).thenReturn(Collections.singletonList(draft));
        when(expenseSettleRepository.findByStatus(1)).thenReturn(Collections.singletonList(submitted));
        when(expenseSettleRepository.findByStatus(2)).thenReturn(Collections.singletonList(processing));
        when(expenseSettleRepository.findByStatus(3)).thenReturn(Collections.singletonList(completed));
        when(expenseSettleRepository.findByStatus(4)).thenReturn(Collections.singletonList(cancelled));
        when(expenseSettleRepository.findAll()).thenReturn(Arrays.asList(draft, submitted, processing, completed, cancelled));

        performGet("/api/loan/settlement/statistics")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(5))
                .andExpect(jsonPath("$.draftCount").value(1))
                .andExpect(jsonPath("$.submittedCount").value(1))
                .andExpect(jsonPath("$.processingCount").value(1))
                .andExpect(jsonPath("$.completedCount").value(1))
                .andExpect(jsonPath("$.cancelledCount").value(1))
                .andExpect(jsonPath("$.totalSettlementAmount").value(25000.00));
    }
}
