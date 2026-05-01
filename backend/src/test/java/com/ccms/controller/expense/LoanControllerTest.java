package com.ccms.controller.expense;

import com.ccms.dto.LoanApplyRequest;
import com.ccms.dto.LoanResponse;
import com.ccms.entity.expense.LoanMain;
import com.ccms.repository.expense.LoanMainRepository;
import com.ccms.service.LoanService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 借款管理控制器集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanService loanService;

    @MockBean
    private LoanMainRepository loanMainRepository;

    private LoanApplyRequest loanRequest;
    private LoanResponse loanResponse;
    private LoanMain loanMain;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        loanRequest = new LoanApplyRequest();
        loanRequest.setLoanUserId(1L);
        loanRequest.setLoanDeptId(1L);
        loanRequest.setLoanAmount(new BigDecimal("1000.00"));
        loanRequest.setExpectRepayDate(LocalDate.now().plusDays(30));
        loanRequest.setPurpose("业务差旅借款");
        loanRequest.setBankName("中国银行");
        loanRequest.setBankAccount("1234567890");

        loanResponse = new LoanResponse();
        loanResponse.setId(1L);
        loanResponse.setLoanUserId(1L);
        loanResponse.setLoanDeptId(1L);
        loanResponse.setLoanAmount(new BigDecimal("1000.00"));
        loanResponse.setExpectRepayDate(LocalDate.now().plusDays(30));
        loanResponse.setPurpose("业务差旅借款");
        loanResponse.setStatus(0);

        loanMain = new LoanMain();
        loanMain.setId(1L);
        loanMain.setLoanUserId(1L);
        loanMain.setLoanDeptId(1L);
        loanMain.setLoanAmount(new BigDecimal("1000.00"));
        loanMain.setExpectRepayDate(LocalDate.now().plusDays(30));
        loanMain.setPurpose("业务差旅借款");
        loanMain.setStatus(0);
    }

    @Test
    void createLoan_Success() throws Exception {
        when(loanService.applyLoan(any(LoanMain.class))).thenReturn(loanMain);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void getLoan_Success() throws Exception {
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(loanMain));

        mockMvc.perform(get("/api/loans/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getLoan_NotFound() throws Exception {
        when(loanMainRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/loans/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateLoan_Success() throws Exception {
        loanRequest.setId(1L);
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(loanMain));
        when(loanService.updateLoan(any(LoanMain.class))).thenReturn(loanMain);

        mockMvc.perform(put("/api/loans/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteLoan_Success() throws Exception {
        loanMain.setStatus(0); // 草稿状态可以删除
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(loanMain));

        mockMvc.perform(delete("/api/loans/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserLoans_Success() throws Exception {
        List<LoanMain> loans = Arrays.asList(loanMain);
        when(loanMainRepository.findByLoanUserId(1L)).thenReturn(loans);

        mockMvc.perform(get("/api/loans/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getLoans_Success() throws Exception {
        Page<LoanMain> loanPage = new PageImpl<>(Arrays.asList(loanMain));
        when(loanMainRepository.findAll(any(PageRequest.class))).thenReturn(loanPage);

        mockMvc.perform(get("/api/loans")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getLoansByStatus_Success() throws Exception {
        List<LoanMain> loans = Arrays.asList(loanMain);
        when(loanMainRepository.findByStatus(0)).thenReturn(loans);

        mockMvc.perform(get("/api/loans/status/0"))
                .andExpect(status().isOk());
    }

    @Test
    void submitForApproval_Success() throws Exception {
        loanMain.setStatus(0); // 草稿状态可以提交
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(loanMain));

        mockMvc.perform(post("/api/loans/1/submit"))
                .andExpect(status().isOk());
    }

    @Test
    void approveLoan_Success() throws Exception {
        loanMain.setStatus(1); // 审批中状态可以批准
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(loanMain));
        when(loanService.approveLoan(anyLong(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/loans/1/approve")
                        .param("remark", "同意申请"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectLoan_Success() throws Exception {
        loanMain.setStatus(1); // 审批中状态可以驳回
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(loanMain));
        when(loanService.rejectLoan(anyLong(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/loans/1/reject")
                        .param("remark", "申请材料不全"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelLoan_Success() throws Exception {
        loanMain.setStatus(0); // 草稿状态可以取消
        when(loanMainRepository.findById(1L)).thenReturn(Optional.of(loanMain));

        mockMvc.perform(post("/api/loans/1/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    void checkBorrowEligibility_Success() throws Exception {
        when(loanService.canUserBorrow(1L, new BigDecimal("1000.00"))).thenReturn(true);

        mockMvc.perform(get("/api/loans/check-borrow-eligibility/1")
                        .param("amount", "1000.00"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void getUserTotalLoan_Success() throws Exception {
        when(loanService.getUserTotalLoanAmount(1L)).thenReturn(new BigDecimal("5000.00"));

        mockMvc.perform(get("/api/loans/user/1/total-loan"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserUnpaidBalance_Success() throws Exception {
        when(loanService.getUserUnpaidBalance(1L)).thenReturn(new BigDecimal("2500.00"));

        mockMvc.perform(get("/api/loans/user/1/unpaid-balance"))
                .andExpect(status().isOk());
    }

    @Test
    void getPendingRepaymentLoans_Success() throws Exception {
        List<LoanMain> loans = Arrays.asList(loanMain);
        when(loanMainRepository.findPendingRepaymentLoans()).thenReturn(loans);

        mockMvc.perform(get("/api/loans/pending-repayment"))
                .andExpect(status().isOk());
    }

    @Test
    void getOverdueLoans_Success() throws Exception {
        List<LoanMain> loans = Arrays.asList(loanMain);
        when(loanMainRepository.findOverdueLoans()).thenReturn(loans);

        mockMvc.perform(get("/api/loans/overdue"))
                .andExpect(status().isOk());
    }

    @Test
    void createLoan_InvalidRequest() throws Exception {
        // 设置无效的请求数据
        loanRequest.setLoanAmount(new BigDecimal("-100.00")); // 金额为负数

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateLoan_NotFound() throws Exception {
        loanRequest.setId(999L);
        when(loanMainRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/loans/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isBadRequest());
    }
}