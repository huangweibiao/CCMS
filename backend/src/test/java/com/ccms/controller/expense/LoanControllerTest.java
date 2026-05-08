package com.ccms.controller.expense;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.expense.LoanMain;
import com.ccms.repository.expense.LoanMainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
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
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 借款管理控制器单元测试
 */
@WebMvcTest(controllers = LoanController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class LoanControllerTest extends ControllerTestBase {

    @MockBean
    private LoanMainRepository loanMainRepository;

    private LoanMain createTestLoan(Long id, String loanNo, Integer status) {
        LoanMain loan = new LoanMain();
        loan.setId(id);
        loan.setLoanNo(loanNo);
        loan.setLoanUserId(1L);
        loan.setLoanDeptId(1L);
        loan.setLoanAmount(new BigDecimal("10000.00"));
        loan.setRepaidAmount(new BigDecimal("3000.00"));
        loan.setExpectRepayDate(LocalDate.now().plusMonths(3));
        loan.setStatus(status);
        loan.setPurpose("项目备用金");
        return loan;
    }

    @Test
    void shouldReturnLoanListWhenQuerySuccess() throws Exception {
        // given
        LoanMain loan = createTestLoan(1L, "L202501010001", 0);
        Page<LoanMain> page = new PageImpl<>(
                Collections.singletonList(loan),
                PageRequest.of(0, 10),
                1
        );
        when(loanMainRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // when & then
        performGet("/api/loan")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].loanNo").value("L202501010001"))
                .andExpect(jsonPath("$.content[0].status").value(0));
    }

    @Test
    void shouldReturnLoanListWhenQueryByUserId() throws Exception {
        // given
        LoanMain loan = createTestLoan(1L, "L202501010001", 0);
        Page<LoanMain> page = new PageImpl<>(
                Collections.singletonList(loan),
                PageRequest.of(0, 10),
                1
        );
        when(loanMainRepository.findByLoanUserId(eq(1L), any(PageRequest.class))).thenReturn(page);

        // when & then
        performGet("/api/loan?userId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].loanUserId").value(1));
    }

    @Test
    void shouldReturnLoanListWhenQueryByStatus() throws Exception {
        // given
        LoanMain loan = createTestLoan(1L, "L202501010001", 1);
        Page<LoanMain> page = new PageImpl<>(
                Collections.singletonList(loan),
                PageRequest.of(0, 10),
                1
        );
        when(loanMainRepository.findByStatus(eq(1), any(PageRequest.class))).thenReturn(page);

        // when & then
        performGet("/api/loan?status=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value(1));
    }

    @Test
    void shouldReturnLoanWhenGetByIdSuccess() throws Exception {
        // given
        LoanMain loan = createTestLoan(1L, "L202501010001", 0);
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(loan));

        // when & then
        performGet("/api/loan/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.loanNo").value("L202501010001"));
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdNotExist() throws Exception {
        // given
        when(loanMainRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        performGet("/api/loan/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnLoanWhenGetByNoSuccess() throws Exception {
        // given
        LoanMain loan = createTestLoan(1L, "L202501010001", 0);
        when(loanMainRepository.findByLoanNo("L202501010001")).thenReturn(Optional.of(loan));

        // when & then
        performGet("/api/loan/no/L202501010001")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanNo").value("L202501010001"));
    }

    @Test
    void shouldReturnNotFoundWhenGetByNoNotExist() throws Exception {
        // given
        when(loanMainRepository.findByLoanNo("NOTEXIST")).thenReturn(Optional.empty());

        // when & then
        performGet("/api/loan/no/NOTEXIST")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateLoanSuccess() throws Exception {
        // given
        LoanMain loan = createTestLoan(1L, "L202501010001", 0);
        when(loanMainRepository.save(any(LoanMain.class))).thenReturn(loan);

        // when & then
        performPost("/api/loan", loan)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.loanNo").value("L202501010001"));
    }

    @Test
    void shouldUpdateLoanSuccess() throws Exception {
        // given
        LoanMain loan = createTestLoan(1L, "L202501010001", 0);
        when(loanMainRepository.save(any(LoanMain.class))).thenReturn(loan);

        // when & then
        performPut("/api/loan/1", loan)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldDeleteLoanSuccess() throws Exception {
        // when & then
        performDelete("/api/loan/1")
                .andExpect(status().isOk());

        verify(loanMainRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldSubmitLoanSuccess() throws Exception {
        // given
        LoanMain loan = createTestLoan(1L, "L202501010001", 0);
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanMainRepository.save(any(LoanMain.class))).thenReturn(loan);

        // when & then
        performPost("/api/loan/1/submit")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("借款单已提交"));
    }

    @Test
    void shouldReturnNotFoundWhenSubmitNotExist() throws Exception {
        // given
        when(loanMainRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        performPost("/api/loan/999/submit")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUserLoansSuccess() throws Exception {
        // given
        LoanMain loan1 = createTestLoan(1L, "L202501010001", 0);
        LoanMain loan2 = createTestLoan(2L, "L202501010002", 1);
        when(loanMainRepository.findByLoanUserId(1L))
                .thenReturn(Arrays.asList(loan1, loan2));

        // when & then
        performGet("/api/loan/user/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturnStatisticsWithUserId() throws Exception {
        // given
        LoanMain loan1 = createTestLoan(1L, "L202501010001", 0);
        LoanMain loan2 = createTestLoan(2L, "L202501010002", 1);
        LoanMain loan3 = createTestLoan(3L, "L202501010003", 2);
        when(loanMainRepository.findByLoanUserId(1L))
                .thenReturn(Arrays.asList(loan1, loan2, loan3));

        // when & then
        performGet("/api/loan/statistics?userId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.draft").value(1))
                .andExpect(jsonPath("$.pending").value(1))
                .andExpect(jsonPath("$.loaned").value(1))
                .andExpect(jsonPath("$.totalLoanAmount").value(30000.00));
    }

    @Test
    void shouldReturnStatisticsWithoutUserId() throws Exception {
        // given
        LoanMain loan1 = createTestLoan(1L, "L202501010001", 0);
        LoanMain loan2 = createTestLoan(2L, "L202501010002", 1);
        when(loanMainRepository.count()).thenReturn(2L);
        when(loanMainRepository.findAll()).thenReturn(Arrays.asList(loan1, loan2));

        // when & then
        performGet("/api/loan/statistics")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.totalLoanAmount").value(20000.00));
    }
}






