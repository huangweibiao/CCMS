package com.ccms.controller.expense;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.expense.RepaymentRecord;
import com.ccms.repository.expense.RepaymentRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 还款记录控制器单元测试
 */
@WebMvcTest(RepaymentController.class)
class RepaymentControllerTest extends ControllerTestBase {

    @MockBean
    private RepaymentRecordRepository repaymentRecordRepository;

    private RepaymentRecord createTestRepayment(Long id, int repayType) {
        RepaymentRecord repayment = new RepaymentRecord();
        repayment.setId(id);
        repayment.setRepayNo("REP" + System.currentTimeMillis());
        repayment.setLoanId(1L);
        repayment.setRepayType(repayType);
        repayment.setRepayAmount(new BigDecimal("1000.00"));
        repayment.setRepayDate(LocalDate.now());
        repayment.setRepayBy(1L);
        return repayment;
    }

    @Test
    void shouldReturnRepaymentListWhenQuerySuccess() throws Exception {
        RepaymentRecord repayment = createTestRepayment(1L, 1);
        Page<RepaymentRecord> page = new PageImpl<>(
                Collections.singletonList(repayment),
                PageRequest.of(0, 10),
                1
        );
        when(repaymentRecordRepository.findAll(any(PageRequest.class))).thenReturn(page);

        performGet("/api/repayment")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].repayType").value(1));
    }

    @Test
    void shouldReturnRepaymentListByLoanId() throws Exception {
        RepaymentRecord repayment = createTestRepayment(1L, 1);
        Page<RepaymentRecord> page = new PageImpl<>(
                Collections.singletonList(repayment),
                PageRequest.of(0, 10),
                1
        );
        when(repaymentRecordRepository.findByLoanId(eq(1L), any(PageRequest.class))).thenReturn(page);

        performGet("/api/repayment?loanId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].loanId").value(1));
    }

    @Test
    void shouldReturnRepaymentListByRepayType() throws Exception {
        RepaymentRecord repayment = createTestRepayment(1L, 2);
        when(repaymentRecordRepository.findByRepayType(2)).thenReturn(Collections.singletonList(repayment));

        performGet("/api/repayment?repayType=2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].repayType").value(2));
    }

    @Test
    void shouldReturnRepaymentWhenGetByIdSuccess() throws Exception {
        RepaymentRecord repayment = createTestRepayment(1L, 1);
        when(repaymentRecordRepository.findById(1L)).thenReturn(Optional.of(repayment));

        performGet("/api/repayment/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.repayType").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdNotExist() throws Exception {
        when(repaymentRecordRepository.findById(999L)).thenReturn(Optional.empty());

        performGet("/api/repayment/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateRepaymentSuccess() throws Exception {
        RepaymentRecord repayment = createTestRepayment(1L, 1);
        when(repaymentRecordRepository.save(any(RepaymentRecord.class))).thenReturn(repayment);

        performPost("/api/repayment", repayment)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.repayType").value(1));
    }

    @Test
    void shouldUpdateRepaymentSuccess() throws Exception {
        RepaymentRecord existing = createTestRepayment(1L, 1);
        RepaymentRecord updated = createTestRepayment(1L, 2);
        when(repaymentRecordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(repaymentRecordRepository.save(any(RepaymentRecord.class))).thenReturn(updated);

        performPut("/api/repayment/1", updated)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.repayType").value(2));
    }

    @Test
    void shouldReturnNotFoundWhenUpdateNonExistent() throws Exception {
        RepaymentRecord repayment = createTestRepayment(1L, 1);
        when(repaymentRecordRepository.findById(999L)).thenReturn(Optional.empty());

        performPut("/api/repayment/999", repayment)
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteRepaymentSuccess() throws Exception {
        when(repaymentRecordRepository.existsById(1L)).thenReturn(true);

        performDelete("/api/repayment/1")
                .andExpect(status().isOk());

        verify(repaymentRecordRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeleteNonExistent() throws Exception {
        when(repaymentRecordRepository.existsById(999L)).thenReturn(false);

        performDelete("/api/repayment/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnRepaymentsByLoanId() throws Exception {
        RepaymentRecord repayment1 = createTestRepayment(1L, 1);
        RepaymentRecord repayment2 = createTestRepayment(2L, 2);
        when(repaymentRecordRepository.findByLoanId(1L)).thenReturn(Arrays.asList(repayment1, repayment2));

        performGet("/api/repayment/loan/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturnStatisticsByLoanId() throws Exception {
        when(repaymentRecordRepository.sumRepayAmountByLoanId(1L)).thenReturn(new BigDecimal("5000.00"));
        when(repaymentRecordRepository.countRepaymentByLoanId(1L)).thenReturn(5L);
        when(repaymentRecordRepository.sumReimburseDeductionByLoanId(1L)).thenReturn(new BigDecimal("2000.00"));

        performGet("/api/repayment/statistics?loanId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRepaidAmount").value(5000.00))
                .andExpect(jsonPath("$.repaymentCount").value(5))
                .andExpect(jsonPath("$.reimburseDeductionAmount").value(2000.00));
    }

    @Test
    void shouldReturnStatisticsByUserId() throws Exception {
        RepaymentRecord repayment = createTestRepayment(1L, 1);
        when(repaymentRecordRepository.sumReimburseDeductionByUserId(1L)).thenReturn(new BigDecimal("3000.00"));
        when(repaymentRecordRepository.findByUserId(1L)).thenReturn(Collections.singletonList(repayment));

        performGet("/api/repayment/statistics?userId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRepaidAmount").value(3000.00))
                .andExpect(jsonPath("$.repaymentCount").value(1));
    }

    @Test
    void shouldReturnOverallStatistics() throws Exception {
        when(repaymentRecordRepository.count()).thenReturn(100L);
        when(repaymentRecordRepository.findByRepayType(1)).thenReturn(Arrays.asList(createTestRepayment(1L, 1), createTestRepayment(2L, 1)));
        when(repaymentRecordRepository.findByRepayType(2)).thenReturn(Collections.singletonList(createTestRepayment(3L, 2)));
        when(repaymentRecordRepository.findByRepayType(3)).thenReturn(Collections.singletonList(createTestRepayment(4L, 3)));

        performGet("/api/repayment/statistics")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(100))
                .andExpect(jsonPath("$.cashRepaymentCount").value(2))
                .andExpect(jsonPath("$.reimburseRepaymentCount").value(1))
                .andExpect(jsonPath("$.transferRepaymentCount").value(1));
    }
}
