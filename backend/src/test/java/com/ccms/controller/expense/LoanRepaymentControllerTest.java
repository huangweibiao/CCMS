package com.ccms.controller.expense;

import com.ccms.dto.RepaymentRequest;
import com.ccms.dto.RepaymentResponse;
import com.ccms.entity.expense.LoanMain;
import com.ccms.entity.expense.LoanRepayment;
import com.ccms.repository.expense.LoanMainRepository;
import com.ccms.repository.expense.LoanRepaymentRepository;
import com.ccms.service.LoanRepaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 借款还款管理控制器集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoanRepaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanRepaymentService loanRepaymentService;

    @MockBean
    private LoanRepaymentRepository loanRepaymentRepository;

    @MockBean
    private LoanMainRepository loanMainRepository;

    private RepaymentRequest repaymentRequest;
    private RepaymentResponse repaymentResponse;
    private LoanRepayment loanRepayment;
    private LoanMain loanMain;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        repaymentRequest = new RepaymentRequest();
        repaymentRequest.setLoanId(1L);
        repaymentRequest.setRepayAmount(new BigDecimal("500.00"));
        repaymentRequest.setRepayDate(LocalDate.now());
        repaymentRequest.setRepayType(1); // 主动还款
        repaymentRequest.setRemark("测试还款");

        repaymentResponse = new RepaymentResponse();
        repaymentResponse.setId(1L);
        repaymentResponse.setLoanId(1L);
        repaymentResponse.setRepayAmount(new BigDecimal("500.00"));
        repaymentResponse.setRepayDate(LocalDate.now());
        repaymentResponse.setRepayType(1);
        repaymentResponse.setStatus(0);
        repaymentResponse.setRemark("测试还款");

        loanRepayment = new LoanRepayment();
        loanRepayment.setId(1L);
        loanRepayment.setLoanId(1L);
        loanRepayment.setRepaymentAmount(new BigDecimal("500.00"));
        loanRepayment.setRepaymentDate(LocalDate.now());
        loanRepayment.setRepaymentType(1);
        loanRepayment.setDueDate(LocalDate.now().plusDays(30));
        loanRepayment.setStatus(0);
        loanRepayment.setRemark("测试还款");
        loanRepayment.setCreateTime(LocalDateTime.now());

        loanMain = new LoanMain();
        loanMain.setId(1L);
        loanMain.setLoanUserId(1L);
        loanMain.setLoanDeptId(1L);
        loanMain.setLoanAmount(new BigDecimal("1000.00"));
        loanMain.setRepaidAmount(new BigDecimal("0.00"));
        loanMain.setBalanceAmount(new BigDecimal("1000.00"));
        loanMain.setStatus(2); // 已放款
    }

    @Test
    void createRepayment_Success() throws Exception {
        when(loanRepaymentService.createRepayment(any(RepaymentRequest.class))).thenReturn(repaymentResponse);

        mockMvc.perform(post("/api/loan-repayments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repaymentRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void executeRepayment_Success() throws Exception {
        when(loanRepaymentService.getRepaymentsByUserId(1L)).thenReturn(Arrays.asList(repaymentResponse));

        mockMvc.perform(post("/api/loan-repayments/1/execute"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelRepayment_Success() throws Exception {
        when(loanRepaymentService.cancelRepayment(1L)).thenReturn(true);

        mockMvc.perform(post("/api/loan-repayments/1/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    void getRepayment_Success() throws Exception {
        when(loanRepaymentService.getRepaymentsByUserId(1L)).thenReturn(Arrays.asList(repaymentResponse));

        mockMvc.perform(get("/api/loan-repayments/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getRepayment_NotFound() throws Exception {
        when(loanRepaymentService.getRepaymentsByUserId(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/loan-repayments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserRepayments_Success() throws Exception {
        when(loanRepaymentService.getRepaymentsByUserId(1L)).thenReturn(Arrays.asList(repaymentResponse));

        mockMvc.perform(get("/api/loan-repayments/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getRepaymentsByLoan_Success() throws Exception {
        when(loanRepaymentService.getRepaymentsByUserId(1L)).thenReturn(Arrays.asList(repaymentResponse));

        mockMvc.perform(get("/api/loan-repayments/loan/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getRepayments_Success() throws Exception {
        Page<RepaymentResponse> page = new PageImpl<>(Arrays.asList(repaymentResponse));
        when(loanRepaymentService.findRepayments(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/loan-repayments")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getPendingRepayments_Success() throws Exception {
        when(loanRepaymentService.getRepaymentsByUserId(1L)).thenReturn(Arrays.asList(repaymentResponse));

        mockMvc.perform(get("/api/loan-repayments/pending"))
                .andExpect(status().isOk());
    }

    @Test
    void autoRepayment_Success() throws Exception {
        when(loanRepaymentService.createRepayment(any(RepaymentRequest.class))).thenReturn(repaymentResponse);

        mockMvc.perform(post("/api/loan-repayments/auto-repayment")
                        .param("loanId", "1")
                        .param("amount", "500.00")
                        .param("reimbursementNo", "BX2024010001"))
                .andExpect(status().isOk());
    }

    @Test
    void forceRepayment_Success() throws Exception {
        when(loanRepaymentService.createRepayment(any(RepaymentRequest.class))).thenReturn(repaymentResponse);

        mockMvc.perform(post("/api/loan-repayments/force-repayment")
                        .param("loanId", "1")
                        .param("amount", "500.00")
                        .param("remark", "系统强制还款"))
                .andExpect(status().isOk());
    }

    @Test
    void getTotalRepaymentByLoan_Success() throws Exception {
        when(loanRepaymentService.getTotalRepaymentAmountByLoanId(1L)).thenReturn(new BigDecimal("1000.00"));

        mockMvc.perform(get("/api/loan-repayments/stats/loan/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getTotalRepaymentByUser_Success() throws Exception {
        when(loanRepaymentService.getTotalRepaymentAmountByUserId(1L)).thenReturn(new BigDecimal("5000.00"));

        mockMvc.perform(get("/api/loan-repayments/stats/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    void autoSettleByReimbursement_Success() throws Exception {
        when(loanRepaymentService.autoSettleByReimbursement(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(post("/api/loan-repayments/auto-settle/1"))
                .andExpect(status().isOk());
    }

    @Test
    void createRepayment_InvalidRequest() throws Exception {
        // 设置无效的请求数据
        repaymentRequest.setRepayAmount(new BigDecimal("-100.00")); // 金额为负数

        mockMvc.perform(post("/api/loan-repayments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repaymentRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void executeRepayment_NotFound() throws Exception {
        when(loanRepaymentService.getRepaymentsByUserId(1L)).thenReturn(Arrays.asList());

        mockMvc.perform(post("/api/loan-repayments/999/execute"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void autoRepayment_InvalidParameters() throws Exception {
        mockMvc.perform(post("/api/loan-repayments/auto-repayment")
                        .param("loanId", "1")
                        .param("amount", "-100.00") // 金额为负数
                        .param("reimbursementNo", "BX2024010001"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOverdueRepayments_Success() throws Exception {
        when(loanRepaymentService.getRepaymentsByUserId(1L)).thenReturn(Arrays.asList(repaymentResponse));

        mockMvc.perform(get("/api/loan-repayments/overdue"))
                .andExpect(status().isOk());
    }
}