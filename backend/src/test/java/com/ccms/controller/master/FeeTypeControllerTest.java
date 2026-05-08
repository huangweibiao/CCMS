package com.ccms.controller.master;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.expense.ExpenseType;
import com.ccms.repository.expense.ExpenseTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 费用类型控制器单元测试
 */
@WebMvcTest(FeeTypeController.class)
class FeeTypeControllerTest extends ControllerTestBase {

    @MockBean
    private ExpenseTypeRepository expenseTypeRepository;

    private ExpenseType createTestFeeType(Long id, String typeCode, String typeName, Boolean enabled) {
        ExpenseType type = new ExpenseType();
        type.setId(id);
        type.setTypeCode(typeCode);
        type.setTypeName(typeName);
        type.setEnabled(enabled);
        type.setTypeLevel(1);
        type.setSortOrder(1);
        type.setNeedApproval(false);
        type.setApprovalThreshold(new BigDecimal("0"));
        type.setSystem(false);
        return type;
    }

    @Test
    void shouldReturnFeeTypeListWhenQuerySuccess() throws Exception {
        ExpenseType type = createTestFeeType(1L, "TRAVEL", "差旅费", true);
        Page<ExpenseType> page = new PageImpl<>(
                Collections.singletonList(type),
                PageRequest.of(0, 10),
                1
        );
        when(expenseTypeRepository.findAll(any(PageRequest.class))).thenReturn(page);

        performGet("/api/fee-types")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].typeCode").value("TRAVEL"));
    }

    @Test
    void shouldReturnFeeTypeWhenGetByIdSuccess() throws Exception {
        ExpenseType type = createTestFeeType(1L, "TRAVEL", "差旅费", true);
        when(expenseTypeRepository.findById(1L)).thenReturn(Optional.of(type));

        performGet("/api/fee-types/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.typeCode").value("TRAVEL"));
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdNotExist() throws Exception {
        when(expenseTypeRepository.findById(999L)).thenReturn(Optional.empty());

        performGet("/api/fee-types/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnFeeTypeWhenGetByCodeSuccess() throws Exception {
        ExpenseType type = createTestFeeType(1L, "TRAVEL", "差旅费", true);
        when(expenseTypeRepository.findByTypeCode("TRAVEL")).thenReturn(type);

        performGet("/api/fee-types/code/TRAVEL")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeCode").value("TRAVEL"));
    }

    @Test
    void shouldCreateFeeTypeSuccess() throws Exception {
        ExpenseType type = createTestFeeType(1L, "OFFICE", "办公费", true);
        when(expenseTypeRepository.findByTypeCode("OFFICE")).thenReturn(null);
        when(expenseTypeRepository.save(any(ExpenseType.class))).thenReturn(type);

        performPost("/api/fee-types", type)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenCreateWithDuplicateCode() throws Exception {
        ExpenseType type = createTestFeeType(1L, "TRAVEL", "差旅费", true);
        when(expenseTypeRepository.findByTypeCode("TRAVEL")).thenReturn(type);

        performPost("/api/fee-types", type)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateFeeTypeSuccess() throws Exception {
        ExpenseType existingType = createTestFeeType(1L, "TRAVEL", "差旅费", true);
        ExpenseType updatedType = createTestFeeType(1L, "TRAVEL", "差旅费用", true);
        when(expenseTypeRepository.findById(1L)).thenReturn(Optional.of(existingType));
        when(expenseTypeRepository.findByTypeCode("TRAVEL")).thenReturn(existingType);
        when(expenseTypeRepository.save(any(ExpenseType.class))).thenReturn(updatedType);

        performPut("/api/fee-types/1", updatedType)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeName").value("差旅费用"));
    }

    @Test
    void shouldDeleteFeeTypeSuccess() throws Exception {
        ExpenseType type = createTestFeeType(1L, "TEMP", "临时类型", true);
        when(expenseTypeRepository.findById(1L)).thenReturn(Optional.of(type));
        when(expenseTypeRepository.countByParentId(1L)).thenReturn(0L);

        performDelete("/api/fee-types/1")
                .andExpect(status().isOk());

        verify(expenseTypeRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldReturnBadRequestWhenDeleteWithChildren() throws Exception {
        ExpenseType type = createTestFeeType(1L, "PARENT", "父类型", true);
        when(expenseTypeRepository.findById(1L)).thenReturn(Optional.of(type));
        when(expenseTypeRepository.countByParentId(1L)).thenReturn(2L);

        performDelete("/api/fee-types/1")
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateFeeTypeStatusSuccess() throws Exception {
        ExpenseType type = createTestFeeType(1L, "TRAVEL", "差旅费", false);
        when(expenseTypeRepository.findById(1L)).thenReturn(Optional.of(type));
        when(expenseTypeRepository.save(any(ExpenseType.class))).thenReturn(type);

        performPut("/api/fee-types/1/status?enabled=true")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void shouldReturnFeeTypeTree() throws Exception {
        when(expenseTypeRepository.findAll()).thenReturn(Collections.emptyList());

        performGet("/api/fee-types/tree")
                .andExpect(status().isOk());
    }
}

