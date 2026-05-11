package com.ccms.controller.expense;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.expense.LoanRepayment;
import com.ccms.repository.expense.LoanRepaymentRepository;
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
 * 借款还款控制器单元测试
 */
@WebMvcTest(LoanRepaymentController.class)
class LoanRepaymentControllerTest extends ControllerTestBase {

    @MockBean
    private LoanRepaymentRepository loanRepaymentRepository;

    private LoanRepayment createTestRepayment(Long id, String repaymentNo, int status) {
        LoanRepayment repayment = new LoanRepayment();
        repayment.setId(id);
        repayment.setRepaymentNo(repaymentNo);
        repayment.setLoanId(1L);
        repayment.setRepaymentAmount(new BigDecimal("1000.00"));
        repayment.setRepaymentDate(LocalDate.now());
        repayment.setStatus(status);
        repayment.setApprovalStatus(0);
        return repayment;
    }

    @Test
    void shouldReturnRepaymentListWhenQuerySuccess() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_PENDING);
        Page<LoanRepayment> page = new PageImpl<>(
                Collections.singletonList(repayment),
                PageRequest.of(0, 10),
                1
        );
        when(loanRepaymentRepository.findAll(any(PageRequest.class))).thenReturn(page);

        performGet("/api/loan/repayment")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].repaymentNo").value("RP202501010001"));
    }

    @Test
    void shouldReturnRepaymentListByLoanId() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_PENDING);
        Page<LoanRepayment> page = new PageImpl<>(
                Collections.singletonList(repayment),
                PageRequest.of(0, 10),
                1
        );
        when(loanRepaymentRepository.findByLoanId(eq(1L), any(PageRequest.class))).thenReturn(page);

        performGet("/api/loan/repayment?loanId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].loanId").value(1));
    }

    @Test
    void shouldReturnRepaymentListByStatus() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_CONFIRMED);
        when(loanRepaymentRepository.findByStatus(LoanRepayment.STATUS_CONFIRMED)).thenReturn(Collections.singletonList(repayment));

        performGet("/api/loan/repayment?status=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value(1));
    }

    @Test
    void shouldReturnRepaymentWhenGetByIdSuccess() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_PENDING);
        when(loanRepaymentRepository.findById(1L)).thenReturn(Optional.of(repayment));

        performGet("/api/loan/repayment/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.repaymentNo").value("RP202501010001"));
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdNotExist() throws Exception {
        when(loanRepaymentRepository.findById(999L)).thenReturn(Optional.empty());

        performGet("/api/loan/repayment/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnRepaymentWhenGetByNoSuccess() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_PENDING);
        when(loanRepaymentRepository.findByRepaymentNo("RP202501010001")).thenReturn(Optional.of(repayment));

        performGet("/api/loan/repayment/no/RP202501010001")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repaymentNo").value("RP202501010001"));
    }

    @Test
    void shouldCreateRepaymentSuccess() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_PENDING);
        when(loanRepaymentRepository.save(any(LoanRepayment.class))).thenReturn(repayment);

        performPost("/api/loan/repayment", repayment)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value(0));
    }

    @Test
    void shouldUpdateRepaymentSuccess() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_PENDING);
        when(loanRepaymentRepository.save(any(LoanRepayment.class))).thenReturn(repayment);

        performPut("/api/loan/repayment/1", repayment)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldDeleteRepaymentSuccess() throws Exception {
        performDelete("/api/loan/repayment/1")
                .andExpect(status().isOk());

        verify(loanRepaymentRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldConfirmRepaymentSuccess() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_PENDING);
        when(loanRepaymentRepository.findById(1L)).thenReturn(Optional.of(repayment));
        when(loanRepaymentRepository.save(any(LoanRepayment.class))).thenReturn(repayment);

        performPost("/api/loan/repayment/1/confirm")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("还款已确认"));
    }

    @Test
    void shouldApproveRepaymentSuccess() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_PENDING);
        when(loanRepaymentRepository.findById(1L)).thenReturn(Optional.of(repayment));
        when(loanRepaymentRepository.save(any(LoanRepayment.class))).thenReturn(repayment);

        performPost("/api/loan/repayment/1/approve?approverId=2&approved=true&comment=同意")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("还款审批通过"));
    }

    @Test
    void shouldRejectRepaymentSuccess() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_PENDING);
        when(loanRepaymentRepository.findById(1L)).thenReturn(Optional.of(repayment));
        when(loanRepaymentRepository.save(any(LoanRepayment.class))).thenReturn(repayment);

        performPost("/api/loan/repayment/1/approve?approverId=2&approved=false&comment=金额有误")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("还款审批驳回"));
    }

    @Test
    void shouldReturnRepaymentsByLoan() throws Exception {
        LoanRepayment repayment1 = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_CONFIRMED);
        LoanRepayment repayment2 = createTestRepayment(2L, "RP202501010002", LoanRepayment.STATUS_CONFIRMED);
        when(loanRepaymentRepository.findByLoanId(1L)).thenReturn(Arrays.asList(repayment1, repayment2));

        performGet("/api/loan/repayment/loan/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturnRepaymentsByUser() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_CONFIRMED);
        when(loanRepaymentRepository.findByUserId(1L)).thenReturn(Collections.singletonList(repayment));

        performGet("/api/loan/repayment/user/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnOverdueRepayments() throws Exception {
        LoanRepayment repayment = createTestRepayment(1L, "RP202501010001", LoanRepayment.STATUS_PENDING);
        when(loanRepaymentRepository.findOverdueRepayments(any(LocalDate.class)))
                .thenReturn(Collections.singletonList(repayment));

        performGet("/api/loan/repayment/overdue")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnStatisticsByLoanId() throws Exception {
        when(loanRepaymentRepository.sumRepaymentAmountByLoanId(1L)).thenReturn(new BigDecimal("5000.00"));

        performGet("/api/loan/repayment/statistics?loanId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value(1))
                .andExpect(jsonPath("$.totalRepaymentAmount").value(5000.00));
    }

    @Test
    void shouldReturnStatisticsByUserId() throws Exception {
        when(loanRepaymentRepository.sumRepaymentAmountByUserId(1L)).thenReturn(new BigDecimal("3000.00"));

        performGet("/api/loan/repayment/statistics?userId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalRepaymentAmount").value(3000.00));
    }
}
