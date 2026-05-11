package com.ccms.controller.expense;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.expense.ExpenseReimburseDetail;
import com.ccms.entity.expense.ExpenseReimburseMain;
import com.ccms.repository.expense.ExpenseReimburseDetailRepository;
import com.ccms.repository.expense.ExpenseReimburseMainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 费用报销控制器单元测试
 */
@WebMvcTest(ExpenseReimburseController.class)
class ExpenseReimburseControllerTest extends ControllerTestBase {

    @MockBean
    private ExpenseReimburseMainRepository reimburseMainRepository;

    @MockBean
    private ExpenseReimburseDetailRepository reimburseDetailRepository;

    // ==================== 分页查询测试 ====================

    /**
     * given: 存在报销单数据
     * when: 调用分页查询接口
     * then: 返回分页结果
     */
    @Test
    void shouldReturnPagedReimburseList() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        Page<ExpenseReimburseMain> page = new PageImpl<>(
                Collections.singletonList(reimburse),
                PageRequest.of(0, 10),
                1
        );
        when(reimburseMainRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // when & then
        performGet("/api/expense/reimburse")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].reimburseNo", is("R2024001")))
                .andExpect(jsonPath("$.content[0].status", is(0)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    /**
     * given: 存在指定用户的报销单
     * when: 按用户ID查询报销单列表
     * then: 返回该用户的报销单列表
     */
    @Test
    void shouldReturnReimburseListByUserId() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        when(reimburseMainRepository.findBySubmitUserId(1L)).thenReturn(Collections.singletonList(reimburse));

        // when & then
        performGet("/api/expense/reimburse?userId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].submitUserId", is(1)));
    }

    /**
     * given: 存在指定状态的报销单
     * when: 按状态查询报销单列表
     * then: 返回该状态的报销单列表
     */
    @Test
    void shouldReturnReimburseListByStatus() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 1);
        when(reimburseMainRepository.findByStatus(1)).thenReturn(Collections.singletonList(reimburse));

        // when & then
        performGet("/api/expense/reimburse?status=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is(1)));
    }

    /**
     * given: 存在指定用户和状态的报销单
     * when: 按用户ID和状态查询报销单列表
     * then: 返回符合条件的报销单列表
     */
    @Test
    void shouldReturnReimburseListByUserIdAndStatus() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        reimburse.setSubmitUserId(1L);
        when(reimburseMainRepository.findBySubmitUserIdAndStatus(1L, 0)).thenReturn(Collections.singletonList(reimburse));

        // when & then
        performGet("/api/expense/reimburse?userId=1&status=0")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is(0)));
    }

    // ==================== 详情查询测试 ====================

    /**
     * given: 存在指定ID的报销单
     * when: 根据ID查询报销单
     * then: 返回该报销单详情
     */
    @Test
    void shouldReturnReimburseById() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));

        // when & then
        performGet("/api/expense/reimburse/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.reimburseNo", is("R2024001")));
    }

    /**
     * given: 不存在指定ID的报销单
     * when: 根据ID查询报销单
     * then: 返回404
     */
    @Test
    void shouldReturnNotFoundWhenReimburseNotExist() throws Exception {
        // given
        when(reimburseMainRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        performGet("/api/expense/reimburse/999")
                .andExpect(status().isNotFound());
    }

    /**
     * given: 存在指定单号的报销单
     * when: 根据单号查询报销单
     * then: 返回该报销单详情
     */
    @Test
    void shouldReturnReimburseByNo() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        when(reimburseMainRepository.findByReimburseNo("R2024001")).thenReturn(Optional.of(reimburse));

        // when & then
        performGet("/api/expense/reimburse/no/R2024001")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reimburseNo", is("R2024001")));
    }

    /**
     * given: 不存在指定单号的报销单
     * when: 根据单号查询报销单
     * then: 返回404
     */
    @Test
    void shouldReturnNotFoundWhenReimburseNoNotExist() throws Exception {
        // given
        when(reimburseMainRepository.findByReimburseNo("R9999999")).thenReturn(Optional.empty());

        // when & then
        performGet("/api/expense/reimburse/no/R9999999")
                .andExpect(status().isNotFound());
    }

    // ==================== 创建测试 ====================

    /**
     * given: 有效的报销单数据
     * when: 创建报销单
     * then: 返回创建的报销单
     */
    @Test
    void shouldCreateReimburse() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(null, "R2024001", null);
        ExpenseReimburseMain savedReimburse = createTestReimburse(1L, "R2024001", 0);
        when(reimburseMainRepository.save(any(ExpenseReimburseMain.class))).thenReturn(savedReimburse);

        // when & then
        performPost("/api/expense/reimburse", reimburse)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is(0)));
    }

    /**
     * given: 带有状态值的报销单数据
     * when: 创建报销单
     * then: 保留原状态值
     */
    @Test
    void shouldCreateReimburseWithStatus() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(null, "R2024001", 1);
        ExpenseReimburseMain savedReimburse = createTestReimburse(1L, "R2024001", 1);
        when(reimburseMainRepository.save(any(ExpenseReimburseMain.class))).thenReturn(savedReimburse);

        // when & then
        performPost("/api/expense/reimburse", reimburse)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(1)));
    }

    // ==================== 更新测试 ====================

    /**
     * given: 存在指定ID的报销单
     * when: 更新报销单
     * then: 返回更新后的报销单
     */
    @Test
    void shouldUpdateReimburse() throws Exception {
        // given
        ExpenseReimburseMain existingReimburse = createTestReimburse(1L, "R2024001", 0);
        ExpenseReimburseMain updatedReimburse = createTestReimburse(1L, "R2024001-UPDATED", 0);
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(existingReimburse));
        when(reimburseMainRepository.save(any(ExpenseReimburseMain.class))).thenReturn(updatedReimburse);

        // when & then
        performPut("/api/expense/reimburse/1", updatedReimburse)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reimburseNo", is("R2024001-UPDATED")));
    }

    /**
     * given: 不存在指定ID的报销单
     * when: 更新报销单
     * then: 返回404
     */
    @Test
    void shouldReturnNotFoundWhenUpdateNonExistReimburse() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(999L, "R2024001", 0);
        when(reimburseMainRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        performPut("/api/expense/reimburse/999", reimburse)
                .andExpect(status().isNotFound());
    }

    // ==================== 删除测试 ====================

    /**
     * given: 存在指定ID的报销单
     * when: 删除报销单
     * then: 返回200并删除明细
     */
    @Test
    void shouldDeleteReimburse() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        doNothing().when(reimburseDetailRepository).deleteByReimburseId(1L);
        doNothing().when(reimburseMainRepository).deleteById(1L);

        // when & then
        performDelete("/api/expense/reimburse/1")
                .andExpect(status().isOk());

        verify(reimburseDetailRepository, times(1)).deleteByReimburseId(1L);
        verify(reimburseMainRepository, times(1)).deleteById(1L);
    }

    /**
     * given: 不存在指定ID的报销单
     * when: 删除报销单
     * then: 返回404
     */
    @Test
    void shouldReturnNotFoundWhenDeleteNonExistReimburse() throws Exception {
        // given
        when(reimburseMainRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        performDelete("/api/expense/reimburse/999")
                .andExpect(status().isNotFound());
    }

    // ==================== 提交审批测试 ====================

    /**
     * given: 存在草稿状态的报销单
     * when: 提交报销单
     * then: 状态变为审批中并返回成功
     */
    @Test
    void shouldSubmitReimburse() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        when(reimburseMainRepository.save(any(ExpenseReimburseMain.class))).thenReturn(reimburse);

        // when & then
        performPost("/api/expense/reimburse/1/submit")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("报销单已提交")));
    }

    /**
     * given: 不存在指定ID的报销单
     * when: 提交报销单
     * then: 返回404
     */
    @Test
    void shouldReturnNotFoundWhenSubmitNonExistReimburse() throws Exception {
        // given
        when(reimburseMainRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        performPost("/api/expense/reimburse/999/submit")
                .andExpect(status().isNotFound());
    }

    /**
     * given: 报销单状态不是草稿
     * when: 提交报销单
     * then: 返回400错误
     */
    @Test
    void shouldReturnBadRequestWhenSubmitNonDraftReimburse() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 1);
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));

        // when & then
        performPost("/api/expense/reimburse/1/submit")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("只有草稿状态的报销单才能提交")));
    }

    // ==================== 用户报销单查询测试 ====================

    /**
     * given: 存在指定报销人的报销单
     * when: 查询用户的报销单
     * then: 返回该用户的报销单列表
     */
    @Test
    void shouldReturnUserReimburses() throws Exception {
        // given
        ExpenseReimburseMain reimburse1 = createTestReimburse(1L, "R2024001", 0);
        ExpenseReimburseMain reimburse2 = createTestReimburse(2L, "R2024002", 1);
        when(reimburseMainRepository.findByReimburseUserId(1L)).thenReturn(Arrays.asList(reimburse1, reimburse2));

        // when & then
        performGet("/api/expense/reimburse/user/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].reimburseNo", is("R2024001")))
                .andExpect(jsonPath("$[1].reimburseNo", is("R2024002")));
    }

    // ==================== 统计测试 ====================

    /**
     * given: 存在报销单数据
     * when: 查询全局统计
     * then: 返回各状态统计数量
     */
    @Test
    void shouldReturnGlobalStatistics() throws Exception {
        // given
        when(reimburseMainRepository.count()).thenReturn(10L);
        when(reimburseMainRepository.findByStatus(0)).thenReturn(Collections.nCopies(2, new ExpenseReimburseMain()));
        when(reimburseMainRepository.findByStatus(1)).thenReturn(Collections.nCopies(3, new ExpenseReimburseMain()));
        when(reimburseMainRepository.findByStatus(2)).thenReturn(Collections.nCopies(2, new ExpenseReimburseMain()));
        when(reimburseMainRepository.findByStatus(3)).thenReturn(Collections.nCopies(1, new ExpenseReimburseMain()));
        when(reimburseMainRepository.findByStatus(4)).thenReturn(Collections.nCopies(1, new ExpenseReimburseMain()));
        when(reimburseMainRepository.findByStatus(5)).thenReturn(Collections.nCopies(1, new ExpenseReimburseMain()));
        when(reimburseMainRepository.findByStatus(6)).thenReturn(Collections.emptyList());

        // when & then
        performGet("/api/expense/reimburse/statistics")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(10)))
                .andExpect(jsonPath("$.draft", is(2)))
                .andExpect(jsonPath("$.pending", is(3)))
                .andExpect(jsonPath("$.approved", is(2)))
                .andExpect(jsonPath("$.rejected", is(1)))
                .andExpect(jsonPath("$.waitPay", is(1)))
                .andExpect(jsonPath("$.paid", is(1)))
                .andExpect(jsonPath("$.cancelled", is(0)));
    }

    /**
     * given: 存在指定用户的报销单数据
     * when: 按用户查询统计
     * then: 返回该用户各状态统计数量
     */
    @Test
    void shouldReturnUserStatistics() throws Exception {
        // given
        when(reimburseMainRepository.findByReimburseUserId(1L)).thenReturn(Collections.nCopies(5, new ExpenseReimburseMain()));
        when(reimburseMainRepository.findBySubmitUserIdAndStatus(1L, 0)).thenReturn(Collections.nCopies(1, new ExpenseReimburseMain()));
        when(reimburseMainRepository.findBySubmitUserIdAndStatus(1L, 1)).thenReturn(Collections.nCopies(2, new ExpenseReimburseMain()));
        when(reimburseMainRepository.findBySubmitUserIdAndStatus(1L, 2)).thenReturn(Collections.nCopies(1, new ExpenseReimburseMain()));
        when(reimburseMainRepository.findBySubmitUserIdAndStatus(1L, 3)).thenReturn(Collections.nCopies(1, new ExpenseReimburseMain()));
        when(reimburseMainRepository.findBySubmitUserIdAndStatus(1L, 4)).thenReturn(Collections.emptyList());
        when(reimburseMainRepository.findBySubmitUserIdAndStatus(1L, 5)).thenReturn(Collections.emptyList());
        when(reimburseMainRepository.findBySubmitUserIdAndStatus(1L, 6)).thenReturn(Collections.emptyList());

        // when & then
        performGet("/api/expense/reimburse/statistics?userId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(5)))
                .andExpect(jsonPath("$.draft", is(1)))
                .andExpect(jsonPath("$.pending", is(2)))
                .andExpect(jsonPath("$.approved", is(1)))
                .andExpect(jsonPath("$.rejected", is(1)))
                .andExpect(jsonPath("$.waitPay", is(0)))
                .andExpect(jsonPath("$.paid", is(0)))
                .andExpect(jsonPath("$.cancelled", is(0)));
    }

    // ==================== 明细查询测试 ====================

    /**
     * given: 存在报销单及其明细
     * when: 查询报销单明细
     * then: 返回明细列表
     */
    @Test
    void shouldReturnReimburseDetails() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        ExpenseReimburseDetail detail1 = createTestDetail(1L, 1L, new BigDecimal("100.00"));
        ExpenseReimburseDetail detail2 = createTestDetail(2L, 1L, new BigDecimal("200.00"));
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        when(reimburseDetailRepository.findByReimburseId(1L)).thenReturn(Arrays.asList(detail1, detail2));

        // when & then
        performGet("/api/expense/reimburse/1/details")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].amount", is(100.00)))
                .andExpect(jsonPath("$[1].amount", is(200.00)));
    }

    /**
     * given: 不存在指定ID的报销单
     * when: 查询报销单明细
     * then: 返回404
     */
    @Test
    void shouldReturnNotFoundWhenGetDetailsOfNonExistReimburse() throws Exception {
        // given
        when(reimburseMainRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        performGet("/api/expense/reimburse/999/details")
                .andExpect(status().isNotFound());
    }

    // ==================== 添加明细测试 ====================

    /**
     * given: 存在报销单
     * when: 添加明细
     * then: 返回保存的明细
     */
    @Test
    void shouldAddReimburseDetail() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        ExpenseReimburseDetail detail = createTestDetail(null, null, new BigDecimal("150.00"));
        ExpenseReimburseDetail savedDetail = createTestDetail(1L, 1L, new BigDecimal("150.00"));
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        when(reimburseDetailRepository.save(any(ExpenseReimburseDetail.class))).thenReturn(savedDetail);

        // when & then
        performPost("/api/expense/reimburse/1/details", detail)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.reimburseId", is(1)))
                .andExpect(jsonPath("$.amount", is(150.00)));
    }

    /**
     * given: 不存在指定ID的报销单
     * when: 添加明细
     * then: 返回404
     */
    @Test
    void shouldReturnNotFoundWhenAddDetailToNonExistReimburse() throws Exception {
        // given
        ExpenseReimburseDetail detail = createTestDetail(null, null, new BigDecimal("150.00"));
        when(reimburseMainRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        performPost("/api/expense/reimburse/999/details", detail)
                .andExpect(status().isNotFound());
    }

    // ==================== 撤回测试 ====================

    /**
     * given: 存在审批中状态的报销单
     * when: 撤回报销单
     * then: 状态变为草稿并返回成功
     */
    @Test
    void shouldWithdrawReimburse() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 1);
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        when(reimburseMainRepository.save(any(ExpenseReimburseMain.class))).thenReturn(reimburse);

        // when & then
        performPost("/api/expense/reimburse/1/withdraw")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("报销单已撤回")));
    }

    /**
     * given: 不存在指定ID的报销单
     * when: 撤回报销单
     * then: 返回404
     */
    @Test
    void shouldReturnNotFoundWhenWithdrawNonExistReimburse() throws Exception {
        // given
        when(reimburseMainRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        performPost("/api/expense/reimburse/999/withdraw")
                .andExpect(status().isNotFound());
    }

    /**
     * given: 报销单状态不是审批中
     * when: 撤回报销单
     * then: 返回400错误
     */
    @Test
    void shouldReturnBadRequestWhenWithdrawNonPendingReimburse() throws Exception {
        // given
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));

        // when & then
        performPost("/api/expense/reimburse/1/withdraw")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("只有审批中的报销单才能撤回")));
    }

    // ==================== 辅助方法 ====================

    private ExpenseReimburseMain createTestReimburse(Long id, String reimburseNo, Integer status) {
        ExpenseReimburseMain reimburse = new ExpenseReimburseMain();
        reimburse.setId(id);
        reimburse.setReimburseNo(reimburseNo);
        reimburse.setReimburseUserId(1L);
        reimburse.setReimburseDeptId(1L);
        reimburse.setTotalAmount(new BigDecimal("1000.00"));
        reimburse.setLoanDeductTotal(BigDecimal.ZERO);
        reimburse.setActualAmount(new BigDecimal("1000.00"));
        reimburse.setReason("测试报销");
        reimburse.setPeriodStart(Date.valueOf("2024-01-01"));
        reimburse.setPeriodEnd(Date.valueOf("2024-01-31"));
        reimburse.setStatus(status);
        reimburse.setInvoiceTotal(new BigDecimal("1000.00"));
        reimburse.setSubmitUserId(1L);
        reimburse.setCreateTime(LocalDateTime.now());
        reimburse.setUpdateTime(LocalDateTime.now());
        return reimburse;
    }

    private ExpenseReimburseDetail createTestDetail(Long id, Long reimburseId, BigDecimal amount) {
        ExpenseReimburseDetail detail = new ExpenseReimburseDetail();
        detail.setId(id);
        detail.setReimburseId(reimburseId);
        detail.setFeeTypeId(1L);
        detail.setExpenseDate(Date.valueOf("2024-01-15"));
        detail.setAmount(amount);
        detail.setQuantity(1);
        detail.setUnitPrice(amount);
        detail.setDescription("测试明细");
        detail.setCreateTime(LocalDateTime.now());
        detail.setUpdateTime(LocalDateTime.now());
        return detail;
    }

    // ==================== 新增：借款核销逻辑测试 ====================
    
    @Test
    void shouldDeductMultipleLoansWhenSubmitReimburse() throws Exception {
        // given: 报销单总金额1000，关联3个借款
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        reimburse.setTotalAmount(new BigDecimal("1000.00"));
        reimburse.setLoanDeductTotal(BigDecimal.ZERO);
        
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        
        // 模拟核销借款后的状态
        ExpenseReimburseMain afterDeduct = createTestReimburse(1L, "R2024001", 1);
        afterDeduct.setLoanDeductTotal(new BigDecimal("1000.00")); // 核销1000
        
        when(reimburseMainRepository.save(any(ExpenseReimburseMain.class))).thenReturn(afterDeduct);
        
        // when & then: 核销成功
        performPost("/api/expense/reimburse/1/deduct-loans")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("借款核销成功")));
    }

    @Test
    void shouldRejectDeductWhenAmountMismatch() throws Exception {
        // given: 报销金额小于借款总金额
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        reimburse.setTotalAmount(new BigDecimal("800.00"));
        
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        
        // when & then: 核销失败
        performPost("/api/expense/reimburse/1/deduct-loans")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("报销金额不能小于已关联的借款总金额")));
    }

    @Test
    void shouldRejectDeductWhenNoLoansLinked() throws Exception {
        // given: 未关联借款的报销单
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        
        // when & then: 核销失败
        performPost("/api/expense/reimburse/1/deduct-loans")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("报销单未关联任何借款，无法执行核销操作")));
    }

    /**
     * given: 报销单被驳回
     * when: 修改并重新提交报销单
     * then: 状态变更成功
     */
    @Test
    void shouldModifyAndResubmitRejectedReimburse() throws Exception {
        // given: 报销单处于被驳回状态
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 4); // 被驳回状态
        reimburse.setReimburseNo("R2024001");
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        
        // 修改驳回的报销单
        ExpenseReimburseMain modified = createTestReimburse(1L, "R2024001", 0); // 修改为草稿状态
        modified.setReason("修改后的报销理由");
        when(reimburseMainRepository.save(any(ExpenseReimburseMain.class))).thenReturn(modified);
        
        // when: 重新提交
        performPost("/api/expense/reimburse/1/submit")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("报销单已重新提交")));
    }

    @Test
    void shouldRejectModificationWhenReimbursePaid() throws Exception {
        // given: 报销单已支付，无法修改
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 3); // 已支付状态
        
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        
        // when: 尝试修改
        performPut("/api/expense/reimburse/1", reimburse)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("已支付的报销单无法修改")));
    }

    /**
     * given: 报销单中存在重复的发票号码
     * when: 校验发票重复性
     * then: 检测到重复发票并返回错误
     */
    @Test
    void shouldDetectDuplicateInvoice() throws Exception {
        // given: 报销单包含重复发票
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        ExpenseReimburseDetail detail1 = createTestDetail(1L, 1L, new BigDecimal("100.00"));
        detail1.setInvoiceNo("INV001");
        ExpenseReimburseDetail detail2 = createTestDetail(2L, 1L, new BigDecimal("200.00"));
        detail2.setInvoiceNo("INV001"); // 相同发票号
        
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        when(reimburseDetailRepository.save(any(ExpenseReimburseDetail.class))).thenReturn(detail1);
        
        // when: 提交时检查发票重复
        performPost("/api/expense/reimburse/1/submit")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("存在重复发票号码：INV001")));
    }

    /**
     * given: 报销单中所有发票号码唯一
     * when: 校验发票重复性
     * then: 校验通过
     */
    @Test
    void shouldAcceptUniqueInvoices() throws Exception {
        // given: 报销单包含唯一发票
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        ExpenseReimburseDetail detail1 = createTestDetail(1L, 1L, new BigDecimal("100.00"));
        detail1.setInvoiceNo("INV001");
        ExpenseReimburseDetail detail2 = createTestDetail(2L, 1L, new BigDecimal("200.00"));
        detail2.setInvoiceNo("INV002"); // 不同发票号
        
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        when(reimburseDetailRepository.save(any(ExpenseReimburseDetail.class))).thenReturn(detail1);
        
        // when: 提交时检查发票
        performPost("/api/expense/reimburse/1/submit")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("报销单已提交")));
    }

    /**
     * given: 报销单包含多个明细
     * when: 计算汇总数据
     * then: 汇总数据准确无误
     */
    @Test
    void shouldCalculateSummaryAccurately() throws Exception {
        // given: 报销单包含3个明细
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        reimburse.setTotalAmount(new BigDecimal("600.00")); // 明细总金额600
        reimburse.setInvoiceTotal(new BigDecimal("600.00")); // 发票总金额600
        
        ExpenseReimburseDetail detail1 = createTestDetail(1L, 1L, new BigDecimal("100.00"));
        ExpenseReimburseDetail detail2 = createTestDetail(2L, 1L, new BigDecimal("200.00"));
        ExpenseReimburseDetail detail3 = createTestDetail(3L, 1L, new BigDecimal("300.00"));
        
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        when(reimburseDetailRepository.findByReimburseId(1L)).thenReturn(Arrays.asList(detail1, detail2, detail3));
        
        // when: 获取汇总数据
        performGet("/api/expense/reimburse/1/summary")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount", is(600.00)))
                .andExpect(jsonPath("$.detailCount", is(3)))
                .andExpect(jsonPath("$.invoiceTotal", is(600.00)))
                .andExpect(jsonPath("$.invoiceMatch", is(true)));
    }

    /**
     * given: 报销单明细金额与汇总不符
     * when: 计算汇总数据
     * then: 检测到金额不匹配
     */
    @Test
    void shouldDetectAmountMismatchInSummary() throws Exception {
        // given: 明细金额500，但汇总设置为600
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        reimburse.setTotalAmount(new BigDecimal("600.00"));
        
        ExpenseReimburseDetail detail1 = createTestDetail(1L, 1L, new BigDecimal("300.00"));
        ExpenseReimburseDetail detail2 = createTestDetail(2L, 1L, new BigDecimal("200.00"));
        
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        when(reimburseDetailRepository.findByReimburseId(1L)).thenReturn(Arrays.asList(detail1, detail2));
        
        // when: 获取汇总数据
        performGet("/api/expense/reimburse/1/summary")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount", is(600.00))) // 汇总金额
                .andExpect(jsonPath("$.actualDetailTotal", is(500.00))) // 实际明细总计
                .andExpect(jsonPath("$.amountMismatch", is(true))) // 金额不匹配
                .andExpect(jsonPath("$.difference", is(100.00))); // 差异
    }

    /**
     * given: 报销金额为0
     * when: 创建或更新报销单
     * then: 返回金额必须大于0的错误
     */
    @Test
    void shouldRejectReimburseWithZeroAmount() throws Exception {
        // given: 报销金额为0
        ExpenseReimburseMain reimburse = createTestReimburse(null, "R2024001", null);
        reimburse.setTotalAmount(BigDecimal.ZERO);
        
        when(reimburseMainRepository.save(any(ExpenseReimburseMain.class))).thenReturn(reimburse);
        
        // when: 创建报销单
        performPost("/api/expense/reimburse", reimburse)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("报销金额必须大于0")));
    }

    /**
     * given: 报销金额为负数
     * when: 创建或更新报销单
     * then: 返回金额必须大于0的错误
     */
    @Test
    void shouldRejectReimburseWithNegativeAmount() throws Exception {
        // given: 报销金额为负数
        ExpenseReimburseMain reimburse = createTestReimburse(null, "R2024001", null);
        reimburse.setTotalAmount(new BigDecimal("-100.00"));
        
        when(reimburseMainRepository.save(any(ExpenseReimburseMain.class))).thenReturn(reimburse);
        
        // when: 创建报销单
        performPost("/api/expense/reimburse", reimburse)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("报销金额必须大于0")));
    }

    /**
     * given: 报销金额为极大值
     * when: 创建或更新报销单
     * then: 超过业务规则限制时返回错误
     */
    @Test
    void shouldRejectReimburseWithExcessiveAmount() throws Exception {
        // given: 报销金额为极大值（超过业务限制）
        ExpenseReimburseMain reimburse = createTestReimburse(null, "R2024001", null);
        reimburse.setTotalAmount(new BigDecimal("999999.99"));
        
        when(reimburseMainRepository.save(any(ExpenseReimburseMain.class))).thenReturn(reimburse);
        
        // when: 创建报销单
        performPost("/api/expense/reimburse", reimburse)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("报销金额超过单笔上限999999.99元")));
    }

    /**
     * given: 报销单违反业务规则（发票号码格式错误）
     * when: 提交报销单
     * then: 返回业务规则验证错误
     */
    @Test
    void shouldRejectReimburseWithInvalidInvoiceFormat() throws Exception {
        // given: 发票号码格式不正确
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        ExpenseReimburseDetail detail = createTestDetail(null, null, new BigDecimal("100.00"));
        detail.setInvoiceNo("INVALID-FORMAT"); // 错误格式
        
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        when(reimburseDetailRepository.save(any(ExpenseReimburseDetail.class))).thenReturn(detail);
        
        // when: 添加明细
        performPost("/api/expense/reimburse/1/details", detail)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("发票号码格式不正确，应为：INV+12位数字")));
    }

    /**
     * given: 报销单数据一致性问题（总计与明细不匹配）
     * when: 提交报销单
     * then: 检测到数据一致性问题
     */
    @Test
    void shouldDetectDataInconsistency() throws Exception {
        // given: 数据存在一致性问题
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        reimburse.setTotalAmount(new BigDecimal("1000.00")); // 主表金额1000
        reimburse.setInvoiceTotal(new BigDecimal("900.00")); // 发票总金额900，不匹配
        
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        
        // when: 检查数据一致性
        performPost("/api/expense/reimburse/1/check-consistency")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("数据一致性检查失败：发票总金额与报销单总金额不匹配")))
                .andExpect(jsonPath("$.data.totalAmount", is(1000.00)))
                .andExpect(jsonPath("$.data.invoiceTotal", is(900.00)))
                .andExpect(jsonPath("$.data.difference", is(100.00)));
    }

    /**
     * given: 报销单金额与借款核销金额不匹配
     * when: 提交报销单
     * then: 返回业务规则违反错误
     */
    @Test
    void shouldRejectInvalidLoanDeduction() throws Exception {
        // given: 借款核销金额不合理
        ExpenseReimburseMain reimburse = createTestReimburse(1L, "R2024001", 0);
        reimburse.setTotalAmount(new BigDecimal("1000.00"));
        reimburse.setLoanDeductTotal(new BigDecimal("1200.00")); // 核销金额大于报销金额
        
        when(reimburseMainRepository.findById(1L)).thenReturn(Optional.of(reimburse));
        when(reimburseMainRepository.save(any(ExpenseReimburseMain.class))).thenReturn(reimburse);
        
        // when: 提交报销单
        performPost("/api/expense/reimburse/1/submit")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("借款核销金额不能大于报销总金额")));
    }
}

