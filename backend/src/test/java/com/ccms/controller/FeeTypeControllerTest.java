package com.ccms.controller;

import com.ccms.entity.fee.FeeType;
import com.ccms.service.FeeTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 费用类型控制器单元测试
 */
@WebMvcTest(FeeTypeController.class)
class FeeTypeControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeeTypeService feeTypeService;

    @Autowired
    private ObjectMapper objectMapper;

    private FeeType testFeeType;
    private List List<FeeType> testFeeTypeList;

    @BeforeEach
    void setUp() {
        testFeeType = createTestFeeType();
        testFeeTypeList = Arrays.asList(
                testFeeType,
                createTransportFeeType(),
                createMealFeeType()
        );
    }

    @Test
    void shouldReturnAllActiveFeeTypes_whenGetAllSuccess() throws Exception {
        // Given
        when(feeTypeService.getAllActiveFeeTypes()).thenReturn(testFeeTypeList);

        // When & Then
        mockMvc.perform(get("/api/fee-types"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].typeCode").value("TRAVEL"));
    }

    @Test
    void shouldReturnEmptyList_whenNoActiveFeeTypes() throws Exception {
        // Given
        when(feeTypeService.getAllActiveFeeTypes()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/fee-types"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnFeeTypeDetail_whenGetByIdSuccess() throws Exception {
        // Given
        when(feeTypeService.getFeeTypeById(eq(1L))).thenReturn(Optional.of(testFeeType));

        // When & Then
        mockMvc.perform(get("/api/fee-types/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.typeCode").value("TRAVEL"))
                .andExpect(jsonPath("$.typeName").value("差旅费"));
    }

    @Test
    void shouldReturnNotFound_whenFeeTypeNotExists() throws Exception {
        // Given
        when(feeTypeService.getFeeTypeById(eq(999L))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/fee-types/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateFeeTypeSuccessfully() throws Exception {
        // Given
        FeeType newFeeType = createTestFeeType();
        newFeeType.setId(null);
        when(feeTypeService.createFeeType(any(FeeType.class))).thenReturn(testFeeType);

        // When & Then
        mockMvc.perform(post("/api/fee-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFeeType)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.typeCode").value("TRAVEL"));
    }

    @Test
    void shouldReturnBadRequest_whenCreateFeeTypeFails() throws Exception {
        // Given
        FeeType invalidFeeType = new FeeType();
        when(feeTypeService.createFeeType(any(FeeType.class)))
                .thenThrow(new RuntimeException("费用类型编码已存在"));

        // When & Then
        mockMvc.perform(post("/api/fee-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFeeType)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateFeeTypeSuccessfully() throws Exception {
        // Given
        FeeType updateFeeType = createTestFeeType();
        updateFeeType.setTypeName("差旅费用");
        when(feeTypeService.updateFeeType(any(FeeType.class))).thenReturn(updateFeeType);

        // When & Then
        mockMvc.perform(put("/api/fee-types/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateFeeType)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeName").value("差旅费用"));
    }

    @Test
    void shouldReturnBadRequest_whenUpdateFeeTypeFails() throws Exception {
        // Given
        when(feeTypeService.updateFeeType(any(FeeType.class)))
                .thenThrow(new RuntimeException("费用类型不存在"));

        // When & Then
        mockMvc.perform(put("/api/fee-types/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFeeType)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteFeeTypeSuccessfully() throws Exception {
        // Given
        doNothing().when(feeTypeService).deleteFeeType(eq(1L));

        // When & Then
        mockMvc.perform(delete("/api/fee-types/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequest_whenDeleteFeeTypeFails() throws Exception {
        // Given
        doThrow(new RuntimeException("费用类型已被使用，无法删除"))
                .when(feeTypeService).deleteFeeType(eq(1L));

        // When & Then
        mockMvc.perform(delete("/api/fee-types/{id}", 1L))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBudgetControlledFeeTypes() throws Exception {
        // Given
        List List<FeeType> budgetControlledTypes = Arrays.asList(testFeeType, createTransportFeeType());
        when(feeTypeService.getBudgetControlledFeeTypes()).thenReturn(budgetControlledTypes);

        // When & Then
        mockMvc.perform(get("/api/fee-types/budget-controlled"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnInvoiceRequiredFeeTypes() throws Exception {
        // Given
        List List<FeeType> invoiceRequiredTypes = Collections.singletonList(testFeeType);
        when(feeTypeService.getInvoiceRequiredFeeTypes()).thenReturn(invoiceRequiredTypes);

        // When & Then
        mockMvc.perform(get("/api/fee-types/invoice-required"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturnTrue_whenFeeTypeIsValid() throws Exception {
        // Given
        when(feeTypeService.isValidFeeType(eq("TRAVEL"))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/fee-types/validate/{typeCode}", "TRAVEL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnFalse_whenFeeTypeIsInvalid() throws Exception {
        // Given
        when(feeTypeService.isValidFeeType(eq("INVALID"))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/fee-types/validate/{typeCode}", "INVALID"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldReturnTrue_whenBudgetControlRequired() throws Exception {
        // Given
        when(feeTypeService.isBudgetControlRequired(eq("TRAVEL"))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/fee-types/budget-control/{typeCode}", "TRAVEL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnFalse_whenBudgetControlNotRequired() throws Exception {
        // Given
        when(feeTypeService.isBudgetControlRequired(eq("MEAL"))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/fee-types/budget-control/{typeCode}", "MEAL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldReturnTrue_whenInvoiceRequired() throws Exception {
        // Given
        when(feeTypeService.isInvoiceRequired(eq("TRAVEL"))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/fee-types/invoice-require/{typeCode}", "TRAVEL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnFalse_whenInvoiceNotRequired() throws Exception {
        // Given
        when(feeTypeService.isInvoiceRequired(eq("ALLOWANCE"))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/fee-types/invoice-require/{typeCode}", "ALLOWANCE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldReturnFeeTypeHierarchy() throws Exception {
        // Given
        when(feeTypeService.getFeeTypeHierarchy()).thenReturn(testFeeTypeList);

        // When & Then
        mockMvc.perform(get("/api/fee-types/hierarchy"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldReturnFeeTypesByCategory() throws Exception {
        // Given
        List List<FeeType> categoryTypes = Arrays.asList(testFeeType, createTransportFeeType());
        when(feeTypeService.getFeeTypesByCategory(eq(1))).thenReturn(categoryTypes);

        // When & Then
        mockMvc.perform(get("/api/fee-types/category/{category}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldBatchUpdateBudgetControlFlagSuccessfully() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        doNothing().when(feeTypeService).batchUpdateBudgetControlFlag(eq(ids), eq(1));

        // When & Then
        mockMvc.perform(put("/api/fee-types/batch-update/budget-control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids))
                        .param("budgetControlFlag", "1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldBatchUpdateInvoiceRequireFlagSuccessfully() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L);
        doNothing().when(feeTypeService).batchUpdateInvoiceRequireFlag(eq(ids), eq(1));

        // When & Then
        mockMvc.perform(put("/api/fee-types/batch-update/invoice-require")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids))
                        .param("invoiceRequireFlag", "1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequest_whenBatchUpdateFails() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1L, 2L);
        doThrow(new RuntimeException("批量更新失败"))
                .when(feeTypeService).batchUpdateBudgetControlFlag(anyList(), anyInt());

        // When & Then
        mockMvc.perform(put("/api/fee-types/batch-update/budget-control")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids))
                        .param("budgetControlFlag", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * 创建测试费用类型
     */
    private FeeType createTestFeeType() {
        FeeType feeType = new FeeType();
        feeType.setId(1L);
        feeType.setTypeCode("TRAVEL");
        feeType.setTypeName("差旅费");
        feeType.setCategory(1);
        feeType.setBudgetControlFlag(1);
        feeType.setInvoiceRequireFlag(1);
        feeType.setStatus(1);
        return feeType;
    }

    private FeeType createTransportFeeType() {
        FeeType feeType = new FeeType();
        feeType.setId(2L);
        feeType.setTypeCode("TRANSPORT");
        feeType.setTypeName("交通费");
        feeType.setCategory(1);
        feeType.setBudgetControlFlag(1);
        feeType.setInvoiceRequireFlag(1);
        feeType.setStatus(1);
        return feeType;
    }

    private FeeType createMealFeeType() {
        FeeType feeType = new FeeType();
        feeType.setId(3L);
        feeType.setTypeCode("MEAL");
        feeType.setTypeName("餐费");
        feeType.setCategory(2);
        feeType.setBudgetControlFlag(0);
        feeType.setInvoiceRequireFlag(0);
        feeType.setStatus(1);
        return feeType;
    }
}
