package com.ccms.controller;

import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetMain;
import com.ccms.repository.budget.BudgetDetailRepository;
import com.ccms.repository.budget.BudgetMainRepository;
import com.ccms.service.BudgetControlService;
import com.ccms.service.impl.BudgetControlServiceImpl;
import com.ccms.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetControlController.class)
class BudgetControlControllerTest extends BaseControllerTest {

    @MockBean
    private BudgetControlService budgetControlService;

    @MockBean
    private BudgetMainRepository budgetMainRepository;

    @MockBean
    private BudgetDetailRepository budgetDetailRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private BudgetMain testBudgetMain;
    private BudgetDetail testBudgetDetail;

    @BeforeEach
    void setUp() {
        testBudgetMain = TestDataFactory.createBudgetMain();
        testBudgetDetail = TestDataFactory.createBudgetDetail();
        testBudgetDetail.setBudgetMain(testBudgetMain);
    }

    @Test
    void shouldReturnAvailabilityResult_whenCheckBudgetAvailabilitySuccess() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "budgetMainId", 1L,
                "budgetDetailId", 2L,
                "amount", 1000.00
        );

        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(testBudgetMain));
        when(budgetDetailRepository.findById(2L)).thenReturn(Optional.of(testBudgetDetail));
        when(budgetControlService.checkBudgetAvailability(testBudgetMain, testBudgetDetail, new BigDecimal("1000.00")))
                .thenReturn(true);

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.available").value(true));

        verify(budgetMainRepository, times(1)).findById(1L);
        verify(budgetDetailRepository, times(1)).findById(2L);
        verify(budgetControlService, times(1)).checkBudgetAvailability(testBudgetMain, testBudgetDetail, new BigDecimal("1000.00"));
    }

    @Test
    void shouldReturnBadRequest_whenCheckBudgetAvailabilityBudgetNotFound() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "budgetMainId", 999L,
                "budgetDetailId", 2L,
                "amount", 1000.00
        );

        when(budgetMainRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("预算信息不存在"));

        verify(budgetMainRepository, times(1)).findById(999L);
        verify(budgetDetailRepository, never()).findById(anyLong());
        verify(budgetControlService, never()).checkBudgetAvailability(any(), any(), any());
    }

    @Test
    void shouldReturnBadRequest_whenCheckBudgetAvailabilityInvalidAmountFormat() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "budgetMainId", "1",
                "budgetDetailId", "2",
                "amount", "invalid"
        );

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").contains("参数错误"));
    }

    @Test
    void shouldReturnSuccess_whenFreezeBudgetAmountSuccess() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "budgetDetailId", 1L,
                "amount", 500.00
        );

        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(testBudgetDetail));
        when(budgetControlService.freezeBudgetAmount(testBudgetDetail, new BigDecimal("500.00")))
                .thenReturn(true);

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/freeze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("预算金额冻结成功"));

        verify(budgetDetailRepository, times(1)).findById(1L);
        verify(budgetControlService, times(1)).freezeBudgetAmount(testBudgetDetail, new BigDecimal("500.00"));
    }

    @Test
    void shouldReturnSuccessWithFalse_whenFreezeBudgetAmountFailed() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "budgetDetailId", 1L,
                "amount", 5000.00
        );

        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(testBudgetDetail));
        when(budgetControlService.freezeBudgetAmount(testBudgetDetail, new BigDecimal("5000.00")))
                .thenReturn(false);

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/freeze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("预算金额冻结失败"));

        verify(budgetDetailRepository, times(1)).findById(1L);
        verify(budgetControlService, times(1)).freezeBudgetAmount(testBudgetDetail, new BigDecimal("5000.00"));
    }

    @Test
    void shouldReturnSuccess_whenUnfreezeBudgetAmountSuccess() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "budgetDetailId", 1L,
                "amount", 300.00
        );

        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(testBudgetDetail));
        when(budgetControlService.unfreezeBudgetAmount(testBudgetDetail, new BigDecimal("300.00")))
                .thenReturn(true);

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/unfreeze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("预算金额解冻成功"));

        verify(budgetDetailRepository, times(1)).findById(1L);
        verify(budgetControlService, times(1)).unfreezeBudgetAmount(testBudgetDetail, new BigDecimal("300.00"));
    }

    @Test
    void shouldReturnSuccess_whenDeductBudgetAmountSuccess() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "budgetDetailId", 1L,
                "amount", 200.00
        );

        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(testBudgetDetail));
        when(budgetControlService.deductBudgetAmount(testBudgetDetail, new BigDecimal("200.00")))
                .thenReturn(true);

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("预算金额扣减成功"));

        verify(budgetDetailRepository, times(1)).findById(1L);
        verify(budgetControlService, times(1)).deductBudgetAmount(testBudgetDetail, new BigDecimal("200.00"));
    }

    @Test
    void shouldReturnSuccess_whenReleaseBudgetAmountSuccess() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "budgetDetailId", 1L,
                "amount", 100.00
        );

        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(testBudgetDetail));
        when(budgetControlService.releaseBudgetAmount(testBudgetDetail, new BigDecimal("100.00")))
                .thenReturn(true);

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/release")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("预算金额释放成功"));

        verify(budgetDetailRepository, times(1)).findById(1L);
        verify(budgetControlService, times(1)).releaseBudgetAmount(testBudgetDetail, new BigDecimal("100.00"));
    }

    @Test
    void shouldReturnSuccess_whenBatchValidateBudget() throws Exception {
        // Given
        Map<String, Object> request = Map.of("test", "value");

        when(budgetControlService).thenReturn(new BudgetControlServiceImpl(null, null));

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/batch-validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量验证通过"));
    }

    @Test
    void shouldReturnBadRequest_whenBatchValidateBudgetServiceTypeMismatch() throws Exception {
        // Given
        Map<String, Object> request = Map.of("test", "value");

        // BudgetControlService不是BudgetControlServiceImpl实例

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/batch-validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("服务类型不匹配"));
    }

    @Test
    void shouldReturnBudgetDetailInfo_whenGetBudgetDetailInfoSuccess() throws Exception {
        // Given
        Long detailId = 1L;
        testBudgetDetail.setBudgetAmount(new BigDecimal("1000.00"));
        testBudgetDetail.setUsedAmount(new BigDecimal("300.00"));
        testBudgetDetail.setFrozenAmount(new BigDecimal("100.00"));

        when(budgetDetailRepository.findById(detailId)).thenReturn(Optional.of(testBudgetDetail));

        // When
        ResultActions result = mockMvc.perform(get("/api/budget/control/detail/{id}", detailId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.budgetDetail.id").value(testBudgetDetail.getId()))
                .andExpect(jsonPath("$.availableAmount").value(600.00))
                .andExpect(jsonPath("$.usedPercentage").value(0.3000));

        verify(budgetDetailRepository, times(1)).findById(detailId);
    }

    @Test
    void shouldReturnBadRequest_whenGetBudgetDetailInfoNotFound() throws Exception {
        // Given
        Long detailId = 999L;
        when(budgetDetailRepository.findById(detailId)).thenReturn(Optional.empty());

        // When
        ResultActions result = mockMvc.perform(get("/api/budget/control/detail/{id}", detailId));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("预算明细不存在"));

        verify(budgetDetailRepository, times(1)).findById(detailId);
    }

    @Test
    void shouldReturnBudgetMainStatistics_whenGetBudgetMainStatisticsSuccess() throws Exception {
        // Given
        Long mainId = 1L;
        
        BudgetDetail detail1 = new BudgetDetail();
        detail1.setBudgetAmount(new BigDecimal("500.00"));
        detail1.setUsedAmount(new BigDecimal("200.00"));
        detail1.setFrozenAmount(new BigDecimal("50.00"));
        
        BudgetDetail detail2 = new BudgetDetail();
        detail2.setBudgetAmount(new BigDecimal("800.00"));
        detail2.setUsedAmount(new BigDecimal("100.00"));
        detail2.setFrozenAmount(new BigDecimal("150.00"));
        
        List<BudgetDetail> details = List.of(detail1, detail2);

        when(budgetMainRepository.findById(mainId)).thenReturn(Optional.of(testBudgetMain));
        when(budgetDetailRepository.findByBudgetMainId(mainId)).thenReturn(details);

        // When
        ResultActions result = mockMvc.perform(get("/api/budget/control/main/{id}/statistics", mainId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.budgetMain.id").value(testBudgetMain.getId()))
                .andExpect(jsonPath("$.totalBudgetAmount").value(1300.00))
                .andExpect(jsonPath("$.totalUsedAmount").value(300.00))
                .andExpect(jsonPath("$.totalFrozenAmount").value(200.00))
                .andExpect(jsonPath("$.availableAmount").value(800.00))
                .andExpect(jsonPath("$.usedPercentage").value(23.08))
                .andExpect(jsonPath("$.detailsCount").value(2));

        verify(budgetMainRepository, times(1)).findById(mainId);
        verify(budgetDetailRepository, times(1)).findByBudgetMainId(mainId);
    }

    @Test
    void shouldReturnBadRequest_whenGetBudgetMainStatisticsNotFound() throws Exception {
        // Given
        Long mainId = 999L;
        when(budgetMainRepository.findById(mainId)).thenReturn(Optional.empty());

        // When
        ResultActions result = mockMvc.perform(get("/api/budget/control/main/{id}/statistics", mainId));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("预算主表不存在"));

        verify(budgetMainRepository, times(1)).findById(mainId);
        verify(budgetDetailRepository, never()).findByBudgetMainId(anyLong());
    }

    @Test
    void shouldHandleZeroBudgetAmount_whenGetBudgetDetailInfoWithZeroBudget() throws Exception {
        // Given
        Long detailId = 1L;
        testBudgetDetail.setBudgetAmount(BigDecimal.ZERO);
        testBudgetDetail.setUsedAmount(BigDecimal.ZERO);
        testBudgetDetail.setFrozenAmount(BigDecimal.ZERO);

        when(budgetDetailRepository.findById(detailId)).thenReturn(Optional.of(testBudgetDetail));

        // When
        ResultActions result = mockMvc.perform(get("/api/budget/control/detail/{id}", detailId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.availableAmount").value(0.00))
                .andExpect(jsonPath("$.usedPercentage").value(0.00));

        verify(budgetDetailRepository, times(1)).findById(detailId);
    }

    @Test
    void shouldHandleEmptyDetails_whenGetBudgetMainStatisticsWithNoDetails() throws Exception {
        // Given
        Long mainId = 1L;
        when(budgetMainRepository.findById(mainId)).thenReturn(Optional.of(testBudgetMain));
        when(budgetDetailRepository.findByBudgetMainId(mainId)).thenReturn(List.of());

        // When
        ResultActions result = mockMvc.perform(get("/api/budget/control/main/{id}/statistics", mainId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.detailsCount").value(0))
                .andExpect(jsonPath("$.totalBudgetAmount").value(0.00))
                .andExpect(jsonPath("$.usedPercentage").value(0.00));

        verify(budgetMainRepository, times(1)).findById(mainId);
        verify(budgetDetailRepository, times(1)).findByBudgetMainId(mainId);
    }

    @Test
    void shouldHandleLargeAmount_whenCheckBudgetAvailabilityWithLargeAmount() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "budgetMainId", 1L,
                "budgetDetailId", 2L,
                "amount", 100000000.00
        );

        when(budgetMainRepository.findById(1L)).thenReturn(Optional.of(testBudgetMain));
        when(budgetDetailRepository.findById(2L)).thenReturn(Optional.of(testBudgetDetail));
        when(budgetControlService.checkBudgetAvailability(testBudgetMain, testBudgetDetail, new BigDecimal("100000000.00")))
                .thenReturn(false);

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(budgetMainRepository, times(1)).findById(1L);
        verify(budgetDetailRepository, times(1)).findById(2L);
        verify(budgetControlService, times(1)).checkBudgetAvailability(testBudgetMain, testBudgetDetail, new BigDecimal("100000000.00"));
    }

    @Test
    void shouldHandleNegativeAmount_whenDeductNegativeAmount() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "budgetDetailId", 1L,
                "amount", -100.00
        );

        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(testBudgetDetail));

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(budgetDetailRepository, times(1)).findById(1L);
        verify(budgetControlService, never()).deductBudgetAmount(any(), any());
    }

    @Test
    void shouldHandleDecimalPrecision_whenBudgetAmountsWithPrecision() throws Exception {
        // Given
        Long detailId = 1L;
        testBudgetDetail.setBudgetAmount(new BigDecimal("1234.5678"));
        testBudgetDetail.setUsedAmount(new BigDecimal("123.4567"));
        testBudgetDetail.setFrozenAmount(new BigDecimal("45.6789"));

        when(budgetDetailRepository.findById(detailId)).thenReturn(Optional.of(testBudgetDetail));

        // When
        ResultActions result = mockMvc.perform(get("/api/budget/control/detail/{id}", detailId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.availableAmount").value(1065.4322))
                .andExpect(jsonPath("$.usedPercentage").value(0.1000));

        verify(budgetDetailRepository, times(1)).findById(detailId);
    }

    @Test
    void shouldHandleMissingParameters_whenRequestWithIncompleteData() throws Exception {
        // Given
        Map<String, Object> request = Map.of(
                "budgetDetailId", 1L
                // missing "amount" parameter
        );

        // When
        ResultActions result = mockMvc.perform(post("/api/budget/control/freeze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").contains("参数错误"));

        verify(budgetDetailRepository, never()).findById(anyLong());
    }

    @Test
    void shouldHandleConcurrentOperations_whenMultipleBudgetControls() throws Exception {
        // Given
        Map<String, Object> freezeRequest = Map.of("budgetDetailId", 1L, "amount", 100.00);
        Map<String, Object> deductRequest = Map.of("budgetDetailId", 1L, "amount", 50.00);

        when(budgetDetailRepository.findById(1L)).thenReturn(Optional.of(testBudgetDetail));
        when(budgetControlService.freezeBudgetAmount(testBudgetDetail, new BigDecimal("100.00")))
                .thenReturn(true);
        when(budgetControlService.deductBudgetAmount(testBudgetDetail, new BigDecimal("50.00")))
                .thenReturn(true);

        // When - 模拟并发操作
        ResultActions freezeResult = mockMvc.perform(post("/api/budget/control/freeze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(freezeRequest)));

        ResultActions deductResult = mockMvc.perform(post("/api/budget/control/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deductRequest)));

        // Then
        freezeResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        deductResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(budgetDetailRepository, times(2)).findById(1L);
        verify(budgetControlService, times(1)).freezeBudgetAmount(testBudgetDetail, new BigDecimal("100.00"));
        verify(budgetControlService, times(1)).deductBudgetAmount(testBudgetDetail, new BigDecimal("50.00"));
    }
}