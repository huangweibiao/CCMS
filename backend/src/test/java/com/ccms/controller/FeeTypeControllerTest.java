package com.ccms.controller;

import com.ccms.entity.fee.FeeType;
import com.ccms.service.FeeTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 费用类型控制器测试类
 */
@WebMvcTest(FeeTypeController.class)
class FeeTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeeTypeService feeTypeService;

    @Test
    void testGetAllFeeTypes_Success() throws Exception {
        // 准备
        List<FeeType> feeTypes = Arrays.asList(
                createFeeType(1L, "TRAVEL", "差旅费", 1),
                createFeeType(2L, "MEAL", "餐饮费", 1)
        );
        when(feeTypeService.getAllActiveFeeTypes()).thenReturn(feeTypes);

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].typeCode").value("TRAVEL"))
                .andExpect(jsonPath("$[1].typeCode").value("MEAL"));

        verify(feeTypeService).getAllActiveFeeTypes();
    }

    @Test
    void testGetAllFeeTypes_Empty() throws Exception {
        // 准备
        when(feeTypeService.getAllActiveFeeTypes()).thenReturn(Arrays.asList());

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(feeTypeService).getAllActiveFeeTypes();
    }

    @Test
    void testGetFeeTypeById_Success() throws Exception {
        // 准备
        FeeType feeType = createFeeType(1L, "TRAVEL", "差旅费", 1);
        when(feeTypeService.getFeeTypeById(1L)).thenReturn(Optional.of(feeType));

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.typeCode").value("TRAVEL"))
                .andExpect(jsonPath("$.typeName").value("差旅费"));

        verify(feeTypeService).getFeeTypeById(1L);
    }

    @Test
    void testGetFeeTypeById_NotFound() throws Exception {
        // 准备
        when(feeTypeService.getFeeTypeById(99L)).thenReturn(Optional.empty());

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(feeTypeService).getFeeTypeById(99L);
    }

    @Test
    void testGetFeeTypeByCode_Success() throws Exception {
        // 准备
        FeeType feeType = createFeeType(1L, "TRAVEL", "差旅费", 1);
        when(feeTypeService.findByTypeCode("TRAVEL")).thenReturn(Optional.of(feeType));

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/code/{code}", "TRAVEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.typeCode").value("TRAVEL"))
                .andExpect(jsonPath("$.typeName").value("差旅费"));

        verify(feeTypeService).findByTypeCode("TRAVEL");
    }

    @Test
    void testGetFeeTypeByCode_NotFound() throws Exception {
        // 准备
        when(feeTypeService.findByTypeCode("NOT_EXIST")).thenReturn(Optional.empty());

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/code/{code}", "NOT_EXIST"))
                .andExpect(status().isNotFound());

        verify(feeTypeService).findByTypeCode("NOT_EXIST");
    }

    @Test
    void testGetBudgetControlledFeeTypes_Success() throws Exception {
        // 准备
        List<FeeType> feeTypes = Arrays.asList(
                createFeeType(1L, "TRAVEL", "差旅费", 1),
                createFeeType(2L, "ACCOMMODATION", "住宿费", 1)
        );
        when(feeTypeService.getBudgetControlledFeeTypes()).thenReturn(feeTypes);

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/budget-controlled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(feeTypeService).getBudgetControlledFeeTypes();
    }

    @Test
    void testGetInvoiceRequiredFeeTypes_Success() throws Exception {
        // 准备
        List<FeeType> feeTypes = Arrays.asList(
                createFeeType(1L, "TRAVEL", "差旅费", 1),
                createFeeType(2L, "MEAL", "餐饮费", 1)
        );
        when(feeTypeService.getInvoiceRequiredFeeTypes()).thenReturn(feeTypes);

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/invoice-required"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(feeTypeService).getInvoiceRequiredFeeTypes();
    }

    @Test
    void testCreateFeeType_Success() throws Exception {
        // 准备
        FeeType newFeeType = createFeeType(null, "NEW_TYPE", "新费用类型", 1);
        FeeType createdFeeType = createFeeType(5L, "NEW_TYPE", "新费用类型", 1);
        
        when(feeTypeService.createFeeType(any(FeeType.class))).thenReturn(createdFeeType);

        // 执行和验证
        mockMvc.perform(post("/api/fee-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newFeeType)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.typeCode").value("NEW_TYPE"));

        verify(feeTypeService).createFeeType(any(FeeType.class));
    }

    @Test
    void testCreateFeeType_ValidationError() throws Exception {
        // 准备
        FeeType invalidFeeType = new FeeType();
        invalidFeeType.setTypeCode(""); // 无效的类型代码

        // 执行和验证
        mockMvc.perform(post("/api/fee-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidFeeType)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateFeeType_Success() throws Exception {
        // 准备
        FeeType updateFeeType = createFeeType(1L, "TRAVEL_UPDATED", "更新差旅费", 1);
        when(feeTypeService.updateFeeType(any(FeeType.class))).thenReturn(updateFeeType);

        // 执行和验证
        mockMvc.perform(put("/api/fee-types/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateFeeType)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.typeCode").value("TRAVEL_UPDATED"));

        verify(feeTypeService).updateFeeType(any(FeeType.class));
    }

    @Test
    void testUpdateFeeType_MismatchId() throws Exception {
        // 准备
        FeeType updateFeeType = createFeeType(1L, "TRAVEL", "差旅费", 1);

        // 执行和验证（URL ID与请求体ID不匹配）
        mockMvc.perform(put("/api/fee-types/{id}", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateFeeType)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteFeeType_Success() throws Exception {
        // 准备
        doNothing().when(feeTypeService).deleteFeeType(1L);

        // 执行和验证
        mockMvc.perform(delete("/api/fee-types/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(feeTypeService).deleteFeeType(1L);
    }

    @Test
    void testToggleFeeTypeStatus_Enable() throws Exception {
        // 准备
        FeeType disabledFeeType = createFeeType(1L, "TRAVEL", "差旅费", 0);
        FeeType enabledFeeType = createFeeType(1L, "TRAVEL", "差旅费", 1);
        
        when(feeTypeService.toggleFeeTypeStatus(1L)).thenReturn(enabledFeeType);

        // 执行和验证
        mockMvc.perform(patch("/api/fee-types/{id}/toggle-status", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));

        verify(feeTypeService).toggleFeeTypeStatus(1L);
    }

    @Test
    void testToggleFeeTypeStatus_Disable() throws Exception {
        // 准备
        FeeType enabledFeeType = createFeeType(1L, "TRAVEL", "差旅费", 1);
        FeeType disabledFeeType = createFeeType(1L, "TRAVEL", "差旅费", 0);
        
        when(feeTypeService.toggleFeeTypeStatus(1L)).thenReturn(disabledFeeType);

        // 执行和验证
        mockMvc.perform(patch("/api/fee-types/{id}/toggle-status", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(0));

        verify(feeTypeService).toggleFeeTypeStatus(1L);
    }

    @Test
    void testIsValidFeeType_Valid() throws Exception {
        // 准备
        when(feeTypeService.isValidFeeType("TRAVEL")).thenReturn(true);

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/{code}/validate", "TRAVEL"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(feeTypeService).isValidFeeType("TRAVEL");
    }

    @Test
    void testIsValidFeeType_Invalid() throws Exception {
        // 准备
        when(feeTypeService.isValidFeeType("NOT_EXIST")).thenReturn(false);

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/{code}/validate", "NOT_EXIST"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(feeTypeService).isValidFeeType("NOT_EXIST");
    }

    @Test
    void testIsBudgetControlRequired_Required() throws Exception {
        // 准备
        when(feeTypeService.isBudgetControlRequired("TRAVEL")).thenReturn(true);

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/{code}/budget-control-required", "TRAVEL"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(feeTypeService).isBudgetControlRequired("TRAVEL");
    }

    @Test
    void testIsInvoiceRequired_Required() throws Exception {
        // 准备
        when(feeTypeService.isInvoiceRequired("TRAVEL")).thenReturn(true);

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/{code}/invoice-required", "TRAVEL"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(feeTypeService).isInvoiceRequired("TRAVEL");
    }

    @Test
    void testCanDeleteFeeType_CanDelete() throws Exception {
        // 准备
        when(feeTypeService.canDeleteFeeType(1L)).thenReturn(true);

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/{id}/can-delete", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(feeTypeService).canDeleteFeeType(1L);
    }

    @Test
    void testCanDeleteFeeType_CannotDelete() throws Exception {
        // 准备
        when(feeTypeService.canDeleteFeeType(1L)).thenReturn(false);

        // 执行和验证
        mockMvc.perform(get("/api/fee-types/{id}/can-delete", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(feeTypeService).canDeleteFeeType(1L);
    }

    // 辅助方法
    private FeeType createFeeType(Long id, String code, String name, Integer status) {
        FeeType feeType = new FeeType();
        feeType.setId(id);
        feeType.setTypeCode(code);
        feeType.setTypeName(name);
        feeType.setStatus(status);
        feeType.setCategory(1);
        feeType.setBudgetControlFlag(1);
        feeType.setInvoiceRequireFlag(1);
        feeType.setSortNo(1);
        return feeType;
    }
}