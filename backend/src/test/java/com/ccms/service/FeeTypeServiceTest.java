package com.ccms.service;

import com.ccms.entity.fee.FeeType;
import com.ccms.repository.FeeTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 费用类型服务测试类
 */
@ExtendWith(MockitoExtension.class)
class FeeTypeServiceTest {

    @Mock
    private FeeTypeRepository feeTypeRepository;

    @InjectMocks
    private FeeTypeService feeTypeService;

    private FeeType mockFeeType;
    private FeeType mockParentFeeType;

    @BeforeEach
    void setUp() {
        // 创建模拟父级费用类型
        mockParentFeeType = new FeeType();
        mockParentFeeType.setId(1L);
        mockParentFeeType.setTypeCode("TRAVEL");
        mockParentFeeType.setTypeName("差旅费");
        mockParentFeeType.setStatus(1);
        mockParentFeeType.setCategory(2);
        mockParentFeeType.setBudgetControlFlag(1);
        mockParentFeeType.setInvoiceRequireFlag(1);

        // 创建模拟费用类型
        mockFeeType = new FeeType();
        mockFeeType.setId(2L);
        mockFeeType.setTypeCode("TRAVEL_TRANSPORT");
        mockFeeType.setTypeName("交通费");
        mockFeeType.setTypeDesc("出差交通费用");
        mockFeeType.setStatus(1);
        mockFeeType.setCategory(2);
        mockFeeType.setBudgetControlFlag(1);
        mockFeeType.setInvoiceRequireFlag(1);
        mockFeeType.setSortNo(1);
        mockFeeType.setParentId(1L);
        mockFeeType.setIsSystemPreset(1);
    }

    @Test
    void testGetAllActiveFeeTypes() {
        // 准备
        List<FeeType> expectedList = Arrays.asList(mockFeeType);
        when(feeTypeRepository.findByStatusOrderBySortNoAsc(1)).thenReturn(expectedList);

        // 执行
        List<FeeType> result = feeTypeService.getAllActiveFeeTypes();

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TRAVEL_TRANSPORT", result.get(0).getTypeCode());
        verify(feeTypeRepository).findByStatusOrderBySortNoAsc(1);
    }

    @Test
    void testGetBudgetControlledFeeTypes() {
        // 准备
        List<FeeType> expectedList = Arrays.asList(mockFeeType);
        when(feeTypeRepository.findByBudgetControlFlag(1)).thenReturn(expectedList);

        // 执行
        List<FeeType> result = feeTypeService.getBudgetControlledFeeTypes();

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isBudgetControlRequired());
        verify(feeTypeRepository).findByBudgetControlFlag(1);
    }

    @Test
    void testGetInvoiceRequiredFeeTypes() {
        // 准备
        List<FeeType> expectedList = Arrays.asList(mockFeeType);
        when(feeTypeRepository.findByInvoiceRequireFlag(1)).thenReturn(expectedList);

        // 执行
        List<FeeType> result = feeTypeService.getInvoiceRequiredFeeTypes();

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isInvoiceRequired());
        verify(feeTypeRepository).findByInvoiceRequireFlag(1);
    }

    @Test
    void testFindByTypeCode_Success() {
        // 准备
        when(feeTypeRepository.findByTypeCode("TRAVEL_TRANSPORT")).thenReturn(Optional.of(mockFeeType));

        // 执行
        Optional<FeeType> result = feeTypeService.findByTypeCode("TRAVEL_TRANSPORT");

        // 验证
        assertTrue(result.isPresent());
        assertEquals("TRAVEL_TRANSPORT", result.get().getTypeCode());
        verify(feeTypeRepository).findByTypeCode("TRAVEL_TRANSPORT");
    }

    @Test
    void testFindByTypeCode_NotFound() {
        // 准备
        when(feeTypeRepository.findByTypeCode("NOT_EXIST")).thenReturn(Optional.empty());

        // 执行
        Optional<FeeType> result = feeTypeService.findByTypeCode("NOT_EXIST");

        // 验证
        assertFalse(result.isPresent());
        verify(feeTypeRepository).findByTypeCode("NOT_EXIST");
    }

    @Test
    void testIsValidFeeType_Valid() {
        // 准备
        when(feeTypeRepository.findByTypeCode("TRAVEL_TRANSPORT")).thenReturn(Optional.of(mockFeeType));

        // 执行
        boolean result = feeTypeService.isValidFeeType("TRAVEL_TRANSPORT");

        // 验证
        assertTrue(result);
    }

    @Test
    void testIsValidFeeType_Invalid_NotFound() {
        // 准备
        when(feeTypeRepository.findByTypeCode("NOT_EXIST")).thenReturn(Optional.empty());

        // 执行
        boolean result = feeTypeService.isValidFeeType("NOT_EXIST");

        // 验证
        assertFalse(result);
    }

    @Test
    void testIsValidFeeType_Invalid_Disabled() {
        // 准备
        mockFeeType.setStatus(0); // 禁用状态
        when(feeTypeRepository.findByTypeCode("TRAVEL_TRANSPORT")).thenReturn(Optional.of(mockFeeType));

        // 执行
        boolean result = feeTypeService.isValidFeeType("TRAVEL_TRANSPORT");

        // 验证
        assertFalse(result);
    }

    @Test
    void testIsBudgetControlRequired_Required() {
        // 准备
        when(feeTypeRepository.findByTypeCode("TRAVEL_TRANSPORT")).thenReturn(Optional.of(mockFeeType));

        // 执行
        boolean result = feeTypeService.isBudgetControlRequired("TRAVEL_TRANSPORT");

        // 验证
        assertTrue(result);
    }

    @Test
    void testIsInvoiceRequired_Required() {
        // 准备
        when(feeTypeRepository.findByTypeCode("TRAVEL_TRANSPORT")).thenReturn(Optional.of(mockFeeType));

        // 执行
        boolean result = feeTypeService.isInvoiceRequired("TRAVEL_TRANSPORT");

        // 验证
        assertTrue(result);
    }

    @Test
    void testCreateFeeType_Success() {
        // 准备
        FeeType newFeeType = new FeeType();
        newFeeType.setTypeCode("NEW_TYPE");
        newFeeType.setTypeName("新费用类型");
        newFeeType.setCategory(1);
        
        when(feeTypeRepository.findByTypeCode("NEW_TYPE")).thenReturn(Optional.empty());
        when(feeTypeRepository.save(any(FeeType.class))).thenReturn(newFeeType);

        // 执行
        FeeType result = feeTypeService.createFeeType(newFeeType);

        // 验证
        assertNotNull(result);
        assertEquals("NEW_TYPE", result.getTypeCode());
        verify(feeTypeRepository).findByTypeCode("NEW_TYPE");
        verify(feeTypeRepository).save(newFeeType);
    }

    @Test
    void testCreateFeeType_Failure_CodeExists() {
        // 准备
        FeeType newFeeType = new FeeType();
        newFeeType.setTypeCode("EXISTING_TYPE");
        
        when(feeTypeRepository.findByTypeCode("EXISTING_TYPE")).thenReturn(Optional.of(mockFeeType));

        // 执行和验证
        assertThrows(RuntimeException.class, () -> feeTypeService.createFeeType(newFeeType));
        verify(feeTypeRepository).findByTypeCode("EXISTING_TYPE");
        verify(feeTypeRepository, never()).save(any());
    }

    @Test
    void testUpdateFeeType_Success() {
        // 准备
        FeeType updatedFeeType = new FeeType();
        updatedFeeType.setId(2L);
        updatedFeeType.setTypeCode("UPDATED_TYPE");
        updatedFeeType.setTypeName("更新后的费用类型");
        updatedFeeType.setCategory(1);
        
        when(feeTypeRepository.findById(2L)).thenReturn(Optional.of(mockFeeType));
        when(feeTypeRepository.findByTypeCode("UPDATED_TYPE")).thenReturn(Optional.empty());
        when(feeTypeRepository.save(any(FeeType.class))).thenReturn(updatedFeeType);

        // 执行
        FeeType result = feeTypeService.updateFeeType(updatedFeeType);

        // 验证
        assertNotNull(result);
        assertEquals("UPDATED_TYPE", result.getTypeCode());
        verify(feeTypeRepository).findById(2L);
        verify(feeTypeRepository).findByTypeCode("UPDATED_TYPE");
        verify(feeTypeRepository).save(any(FeeType.class));
    }

    @Test
    void testDeleteFeeType_Success() {
        // 准备
        mockFeeType.setIsSystemPreset(0); // 非系统预设，允许删除
        when(feeTypeRepository.findById(2L)).thenReturn(Optional.of(mockFeeType));
        when(feeTypeRepository.save(any(FeeType.class))).thenReturn(mockFeeType);

        // 执行
        feeTypeService.deleteFeeType(2L);

        // 验证
        assertEquals(0, mockFeeType.getStatus().intValue());
        verify(feeTypeRepository).findById(2L);
        verify(feeTypeRepository).save(mockFeeType);
    }

    @Test
    void testDeleteFeeType_Failure_SystemPreset() {
        // 准备
        mockFeeType.setIsSystemPreset(1); // 系统预设，不允许删除
        when(feeTypeRepository.findById(2L)).thenReturn(Optional.of(mockFeeType));

        // 执行和验证
        assertThrows(RuntimeException.class, () -> feeTypeService.deleteFeeType(2L));
        verify(feeTypeRepository).findById(2L);
        verify(feeTypeRepository, never()).save(any());
    }

    @Test
    void testGetFeeTypeById_Success() {
        // 准备
        when(feeTypeRepository.findById(2L)).thenReturn(Optional.of(mockFeeType));

        // 执行
        Optional<FeeType> result = feeTypeService.getFeeTypeById(2L);

        // 验证
        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getId().longValue());
        verify(feeTypeRepository).findById(2L);
    }

    @Test
    void testGetFeeTypeById_NotFound() {
        // 准备
        when(feeTypeRepository.findById(99L)).thenReturn(Optional.empty());

        // 执行
        Optional<FeeType> result = feeTypeService.getFeeTypeById(99L);

        // 验证
        assertFalse(result.isPresent());
        verify(feeTypeRepository).findById(99L);
    }

    @Test
    void testCanDeleteFeeType_CanDelete() {
        // 准备
        mockFeeType.setIsSystemPreset(0); // 非系统预设
        when(feeTypeRepository.findById(2L)).thenReturn(Optional.of(mockFeeType));

        // 执行
        boolean result = feeTypeService.canDeleteFeeType(2L);

        // 验证
        assertTrue(result);
    }

    @Test
    void testCanDeleteFeeType_CannotDelete_SystemPreset() {
        // 准备
        mockFeeType.setIsSystemPreset(1); // 系统预设
        when(feeTypeRepository.findById(2L)).thenReturn(Optional.of(mockFeeType));

        // 执行
        boolean result = feeTypeService.canDeleteFeeType(2L);

        // 验证
        assertFalse(result);
    }

    @Test
    void testCanDeleteFeeType_CannotDelete_NotFound() {
        // 准备
        when(feeTypeRepository.findById(99L)).thenReturn(Optional.empty());

        // 执行
        boolean result = feeTypeService.canDeleteFeeType(99L);

        // 验证
        assertFalse(result);
    }

    @Test
    void testValidateFeeType_Valid() {
        // 准备
        FeeType validFeeType = new FeeType();
        validFeeType.setTypeCode("VALID_CODE123");
        validFeeType.setTypeName("有效费用类型");
        validFeeType.setCategory(1);

        // 执行和验证（不应该抛出异常）
        assertDoesNotThrow(() -> feeTypeService.validateFeeType(validFeeType));
    }

    @Test
    void testValidateFeeType_Invalid_MissingCode() {
        // 准备
        FeeType invalidFeeType = new FeeType();
        invalidFeeType.setTypeCode("");
        invalidFeeType.setTypeName("无效费用类型");

        // 执行和验证
        assertThrows(RuntimeException.class, () -> feeTypeService.validateFeeType(invalidFeeType));
    }

    @Test
    void testValidateFeeType_Invalid_InvalidCodeFormat() {
        // 准备
        FeeType invalidFeeType = new FeeType();
        invalidFeeType.setTypeCode("invalid-code!");
        invalidFeeType.setTypeName("无效费用类型");

        // 执行和验证
        assertThrows(RuntimeException.class, () -> feeTypeService.validateFeeType(invalidFeeType));
    }
}