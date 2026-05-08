package com.ccms.controller.expense;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.expense.ExpenseApplyMain;
import com.ccms.repository.expense.ExpenseApplyMainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 费用申请控制器单元测试
 */
@WebMvcTest(ExpenseApplyController.class)
class ExpenseApplyControllerTest extends ControllerTestBase {

    @MockBean
    private ExpenseApplyMainRepository applyMainRepository;

    private ExpenseApplyMain createTestApply(Long id, String applyNo, Integer status) {
        ExpenseApplyMain apply = new ExpenseApplyMain();
        apply.setId(id);
        apply.setApplyNo(applyNo);
        apply.setApplyType(1);
        apply.setApplyUserId(1L);
        apply.setApplyDeptId(1L);
        apply.setApplyAmount(new BigDecimal("1000.00"));
        apply.setTotalAmount(new BigDecimal("1000.00"));
        apply.setReason("测试申请");
        apply.setExpectedDate(Date.valueOf("2025-12-31"));
        apply.setStatus(status);
        return apply;
    }

    @Test
    void shouldReturnApplyListWhenQuerySuccess() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        Page<ExpenseApplyMain> page = new PageImpl<>(
                Collections.singletonList(apply),
                PageRequest.of(0, 10),
                1
        );
        when(applyMainRepository.findAll(any(PageRequest.class))).thenReturn(page);

        performGet("/api/expense/apply")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].applyNo").value("EA202501010001"));
    }

    @Test
    void shouldReturnApplyWhenGetByIdSuccess() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));

        performGet("/api/expense/apply/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.applyNo").value("EA202501010001"));
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdNotExist() throws Exception {
        when(applyMainRepository.findById(999L)).thenReturn(Optional.empty());

        performGet("/api/expense/apply/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateApplySuccess() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);

        performPost("/api/expense/apply", apply)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldUpdateApplySuccess() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);

        performPut("/api/expense/apply/1", apply)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldDeleteApplySuccess() throws Exception {
        performDelete("/api/expense/apply/1")
                .andExpect(status().isOk());

        verify(applyMainRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldSubmitApplySuccess() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);

        performPost("/api/expense/apply/1/submit")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("申请单已提交"));
    }

    @Test
    void shouldWithdrawApplySuccess() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 1);
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);

        performPost("/api/expense/apply/1/withdraw")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("申请单已撤回"));
    }

    @Test
    void shouldReturnUserAppliesSuccess() throws Exception {
        ExpenseApplyMain apply1 = createTestApply(1L, "EA202501010001", 0);
        ExpenseApplyMain apply2 = createTestApply(2L, "EA202501010002", 1);
        when(applyMainRepository.findByApplyUserIdOrderByCreateTimeDesc(1L))
                .thenReturn(Arrays.asList(apply2, apply1));

        performGet("/api/expense/apply/user/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldReturnDeptAppliesSuccess() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        when(applyMainRepository.findByApplyDeptIdOrderByCreateTimeDesc(1L))
                .thenReturn(Collections.singletonList(apply));

        performGet("/api/expense/apply/dept/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldReturnPendingAppliesSuccess() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 1);
        when(applyMainRepository.findByStatus(1)).thenReturn(Collections.singletonList(apply));

        performGet("/api/expense/apply/pending")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldReturnStatisticsWithUserId() throws Exception {
        when(applyMainRepository.countByApplyUserId(1L)).thenReturn(10L);
        when(applyMainRepository.countByApplyUserIdAndStatus(1L, 0)).thenReturn(3L);
        when(applyMainRepository.countByApplyUserIdAndStatus(1L, 1)).thenReturn(2L);
        when(applyMainRepository.countByApplyUserIdAndStatus(1L, 2)).thenReturn(4L);
        when(applyMainRepository.countByApplyUserIdAndStatus(1L, 3)).thenReturn(1L);

        performGet("/api/expense/apply/statistics?userId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.draft").value(3))
                .andExpect(jsonPath("$.pending").value(2))
                .andExpect(jsonPath("$.approved").value(4))
                .andExpect(jsonPath("$.rejected").value(1));
    }
}

