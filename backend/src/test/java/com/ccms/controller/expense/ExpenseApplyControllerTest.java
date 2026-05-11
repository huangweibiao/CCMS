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

    @Test
    void shouldRejectApplyWhenBudgetExceeded() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        apply.setApplyAmount(new BigDecimal("100000.00")); // 超出预算的大金额
        apply.setTotalAmount(new BigDecimal("100000.00"));
        
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply", apply)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("费用金额超过可用预算"));
    }

    @Test
    void shouldRejectApplyWhenBudgetThresholdExceeded() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        apply.setApplyAmount(new BigDecimal("9500.00")); // 接近预算阈值
        apply.setTotalAmount(new BigDecimal("9500.00"));
        
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply", apply)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("费用金额超过审批阈值"));
    }

    @Test
    void shouldHandleMultiLevelApprovalFlow() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        
        // 第一级审批
        apply.setStatus(1); // 待审批状态
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply/1/submit")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("申请单已提交"));
        
        // 第二级审批
        apply.setStatus(2); // 审批中
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        
        performPost("/api/expense/apply/1/approve")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("审批已通过"));
        
        // 最终审批完成
        apply.setStatus(3); // 已批准
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        
        performGet("/api/expense/apply/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(3))
                .andExpect(jsonPath("$.statusDescription").value("已批准"));
    }

    @Test
    void shouldHandleApprovalReject() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 1);
        
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply/1/reject")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("审批已拒绝"));
    }

    @Test
    void shouldHandleWithdrawAndResubmit() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 1);
        
        // 第一次提交
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply/1/submit")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        // 撤回申请
        apply.setStatus(0); // 回到草稿状态
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        
        performPost("/api/expense/apply/1/withdraw")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("申请单已撤回"))
                .andExpect(jsonPath("$.data.status").value(0));
        
        // 修改后重新提交
        apply.setReason("修改后的申请理由");
        apply.setStatus(1); // 再次提交
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        
        performPost("/api/expense/apply/1/submit")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("申请单已重新提交"));
    }

    @Test
    void shouldRejectWithdrawWhenApplyInFinalStatus() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 3); // 已批准状态
        
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        
        performPost("/api/expense/apply/1/withdraw")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("已批准的申请单无法撤回"));
    }

    @Test
    void shouldHandleAttachmentUploadFailure() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        // 模拟附件上传失败
        performPost("/api/expense/apply/1/attachment/upload", new byte[0])
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("附件上传失败：文件为空或格式不支持"));
    }

    @Test
    void shouldHandleAttachmentUploadSuccess() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        
        when(applyMainRepository.findById(1L)).thenReturn(Optional.of(apply));
        
        // 模拟成功的附件上传
        performPost("/api/expense/apply/1/attachment/upload", "test file content".getBytes())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("附件上传成功"));
    }

    @Test
    void shouldRejectApplyWhenExpenseTypeDisabled() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        apply.setApplyType(999); // 设置一个禁用的费用类型ID
        
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply", apply)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("选择的费用类型已被禁用，请选择其他费用类型"));
    }

    @Test
    void shouldHandleInvalidExpenseType() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        apply.setApplyType(null); // 费用类型为空
        
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply", apply)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("费用类型不能为空"));
    }

    @Test
    void shouldHandleFirstPagePagination() throws Exception {
        ExpenseApplyMain apply1 = createTestApply(1L, "EA202501010001", 0);
        ExpenseApplyMain apply2 = createTestApply(2L, "EA202501010002", 1);
        ExpenseApplyMain apply3 = createTestApply(3L, "EA202501010003", 2);
        
        Page<ExpenseApplyMain> page = new PageImpl<>(
                Arrays.asList(apply1, apply2, apply3),
                PageRequest.of(0, 10),
                15
        );
        
        when(applyMainRepository.findAll(any(PageRequest.class))).thenReturn(page);
        
        performGet("/api/expense/apply?page=0&size=10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void shouldHandleLastPagePagination() throws Exception {
        ExpenseApplyMain apply1 = createTestApply(1L, "EA202501010011", 0);
        ExpenseApplyMain apply2 = createTestApply(2L, "EA202501010012", 1);
        
        Page<ExpenseApplyMain> page = new PageImpl<>(
                Arrays.asList(apply1, apply2),
                PageRequest.of(1, 10),
                15
        );
        
        when(applyMainRepository.findAll(any(PageRequest.class))).thenReturn(page);
        
        performGet("/api/expense/apply?page=1&size=10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldHandleEmptyPagePagination() throws Exception {
        Page<ExpenseApplyMain> emptyPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(5, 10),
                0
        );
        
        when(applyMainRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);
        
        performGet("/api/expense/apply?page=5&size=10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void shouldHandleNegativePageNumber() throws Exception {
        performGet("/api/expense/apply?page=-1&size=10")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("页码不能为负数"));
    }

    @Test
    void shouldHandleZeroPageSize() throws Exception {
        performGet("/api/expense/apply?page=0&size=0")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("每页大小不能为0"));
    }

    @Test
    void shouldHandleOversizedPageSize() throws Exception {
        performGet("/api/expense/apply?page=0&size=1000")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("每页大小不能超过100"));
    }

    @Test
    void shouldRejectApplyWithMissingRequiredFields() throws Exception {
        ExpenseApplyMain apply = new ExpenseApplyMain();
        // 缺少必填字段：申请类型、申请金额、申请人ID、部门ID、理由、预期日期
        
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply", apply)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldRejectApplyWithNegativeAmount() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        apply.setApplyAmount(new BigDecimal("-100.00")); // 负金额
        apply.setTotalAmount(new BigDecimal("-100.00"));
        
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply", apply)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("申请金额必须大于0"));
    }

    @Test
    void shouldRejectApplyWithInvalidDateFormat() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        apply.setExpectedDate(Date.valueOf("2020-01-01")); // 过期日期
        
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply", apply)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("预期日期不能早于当前日期"));
    }

    @Test
    void shouldRejectApplyWithEmptyReason() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        apply.setReason(""); // 空理由
        
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply", apply)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("申请理由不能为空"));
    }

    @Test
    void shouldRejectApplyWithTooLongReason() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "EA202501010001", 0);
        // 创建超长理由（超过1000字符）
        String longReason = "这是一段非常长的理由".repeat(100);
        apply.setReason(longReason);
        
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply", apply)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("申请理由不能超过1000个字符"));
    }

    @Test
    void shouldRejectApplyWithInvalidApplyNoFormat() throws Exception {
        ExpenseApplyMain apply = createTestApply(1L, "INVALID-FORMAT", 0); // 无效的申请单号格式
        
        when(applyMainRepository.save(any(ExpenseApplyMain.class))).thenReturn(apply);
        
        performPost("/api/expense/apply", apply)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("申请单号格式不正确，应为：EA+YYYYMMDD+4位序号"));
    }
}

