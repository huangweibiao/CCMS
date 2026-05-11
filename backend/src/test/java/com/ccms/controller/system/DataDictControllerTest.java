package com.ccms.controller.system;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.system.config.DataDict;
import com.ccms.service.system.DataDictService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 数据字典控制器单元测试
 */
@WebMvcTest(DataDictController.class)
class DataDictControllerTest extends ControllerTestBase {

    @MockBean
    private DataDictService dataDictService;

    private DataDict createTestDict(Long id, String dictType, String dictCode, String dictName) {
        DataDict dict = new DataDict();
        dict.setId(id);
        dict.setDictType(dictType);
        dict.setDictCode(dictCode);
        dict.setDictName(dictName);
        dict.setDictValue(dictCode);
        dict.setSortOrder(1);
        dict.setStatus(1);
        return dict;
    }

    @Test
    void shouldReturnDictsByType() throws Exception {
        DataDict dict = createTestDict(1L, "STATUS", "ACTIVE", "启用");
        when(dataDictService.getDictsByType("STATUS")).thenReturn(Collections.singletonList(dict));

        performGet("/api/system/dict/type/STATUS")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dictCode").value("ACTIVE"));
    }

    @Test
    void shouldReturnDictByTypeAndCode() throws Exception {
        DataDict dict = createTestDict(1L, "STATUS", "ACTIVE", "启用");
        when(dataDictService.getDictByTypeAndCode("STATUS", "ACTIVE")).thenReturn(dict);

        performGet("/api/system/dict/type/STATUS/code/ACTIVE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dictName").value("启用"));
    }

    @Test
    void shouldReturnNotFoundWhenDictNotExist() throws Exception {
        when(dataDictService.getDictByTypeAndCode("STATUS", "NOTEXIST")).thenReturn(null);

        performGet("/api/system/dict/type/STATUS/code/NOTEXIST")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnDictName() throws Exception {
        when(dataDictService.getDictName("STATUS", "ACTIVE")).thenReturn("启用");

        performGet("/api/system/dict/type/STATUS/code/ACTIVE/name")
                .andExpect(status().isOk())
                .andExpect(content().string("启用"));
    }

    @Test
    void shouldReturnAllDictTypes() throws Exception {
        when(dataDictService.getAllDictTypes()).thenReturn(Arrays.asList("STATUS", "TYPE", "CATEGORY"));

        performGet("/api/system/dict/types")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void shouldReturnDictTree() throws Exception {
        when(dataDictService.getDictTree("STATUS")).thenReturn(Collections.emptyList());

        performGet("/api/system/dict/type/STATUS/tree")
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateDictSuccess() throws Exception {
        DataDict dict = createTestDict(1L, "STATUS", "NEW", "新建");
        when(dataDictService.saveDict(any(DataDict.class))).thenReturn(dict);

        performPost("/api/system/dict", dict)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldUpdateDictSuccess() throws Exception {
        DataDict dict = createTestDict(1L, "STATUS", "ACTIVE", "已启用");
        when(dataDictService.saveDict(any(DataDict.class))).thenReturn(dict);

        performPut("/api/system/dict/1", dict)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dictName").value("已启用"));
    }

    @Test
    void shouldUpdateDictStatusSuccess() throws Exception {
        doNothing().when(dataDictService).updateDictStatus(1L, 0);

        performPut("/api/system/dict/1/status?status=0")
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteDictSuccess() throws Exception {
        doNothing().when(dataDictService).deleteDict(1L);

        performDelete("/api/system/dict/1")
                .andExpect(status().isOk());

        verify(dataDictService, times(1)).deleteDict(1L);
    }

    @Test
    void shouldCheckDictExists() throws Exception {
        when(dataDictService.dictExists("STATUS", "ACTIVE")).thenReturn(true);

        performGet("/api/system/dict/check-exists?dictType=STATUS&dictCode=ACTIVE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void shouldReloadDictCache() throws Exception {
        doNothing().when(dataDictService).reloadDictCache();

        performPost("/api/system/dict/reload-cache")
                .andExpect(status().isOk());
    }

    /**
     * Task 11.1: 添加字典项缓存失效测试
     */

    @Test
    void shouldInvalidateDictCache() throws Exception {
        // given
        DataDict dict = createTestDict(1L, "STATUS", "ACTIVE", "启用");
        when(dataDictService.saveDict(any(DataDict.class))).thenReturn(dict);
        doNothing().when(dataDictService).reloadDictCache();

        // when & then
        performPut("/api/system/dict/1", dict)
                .andExpect(status().isOk());
    }

    @Test
    void shouldInvalidateDictCacheAfterDelete() throws Exception {
        // given
        doNothing().when(dataDictService).deleteDict(1L);
        doNothing().when(dataDictService).reloadDictCache();

        // when & then
        performDelete("/api/system/dict/1")
                .andExpect(status().isOk());
    }

    /**
     * Task 11.3: 添加字典项数据验证测试
     */

    @Test
    void shouldValidateEmptyDictCode() throws Exception {
        // given
        DataDict dict = createTestDict(1L, "STATUS", "", "启用");
        when(dataDictService.saveDict(any(DataDict.class))).thenThrow(new IllegalArgumentException("字典编码不能为空"));

        // when & then
        performPost("/api/system/dict", dict)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateDuplicateDictCode() throws Exception {
        // given
        DataDict dict = createTestDict(1L, "STATUS", "ACTIVE", "启用");
        when(dataDictService.dictExists("STATUS", "ACTIVE")).thenReturn(true);

        // when & then
        performPost("/api/system/dict", dict)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void shouldValidateEmptyDictName() throws Exception {
        // given
        DataDict dict = createTestDict(1L, "STATUS", "CODE", "");
        when(dataDictService.saveDict(any(DataDict.class))).thenThrow(new IllegalArgumentException("字典名称不能为空"));

        // when & then
        performPost("/api/system/dict", dict)
                .andExpect(status().isBadRequest());
    }

    /**
     * Task 11.4: 添加字典项父子关系测试
     */

    @Test
    void shouldReturnDictTreeWithChildren() throws Exception {
        // given
        DataDict parent = createTestDict(1L, "EXPENSE_TYPE", "OFFICE", "办公费用");
        
        DataDict child1 = createTestDict(2L, "EXPENSE_TYPE", "SUPPLIES", "办公用品");
        
        DataDict child2 = createTestDict(3L, "EXPENSE_TYPE", "PRINT", "打印费");
        
        List<DataDict> allDicts = Arrays.asList(parent, child1, child2);
        when(dataDictService.getDictTree("EXPENSE_TYPE")).thenReturn(allDicts);

        // when & then
        performGet("/api/system/dict/type/EXPENSE_TYPE/tree")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void shouldGetDictChildren() throws Exception {
        // given
        DataDict child1 = createTestDict(2L, "EXPENSE_TYPE", "SUPPLIES", "办公用品");
        
        DataDict child2 = createTestDict(3L, "EXPENSE_TYPE", "PRINT", "打印费");
        
        when(dataDictService.getDictsByType("EXPENSE_TYPE")).thenReturn(Arrays.asList(child1, child2));

        // when & then
        performGet("/api/system/dict/type/EXPENSE_TYPE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    /**
     * Task 11.5: 添加字典项排序功能测试
     */

    @Test
    void shouldReturnDictsInOrder() throws Exception {
        // given
        DataDict dict1 = createTestDict(1L, "STATUS", "ACTIVE", "启用");
        dict1.setSortOrder(1);
        
        DataDict dict2 = createTestDict(2L, "STATUS", "INACTIVE", "禁用");
        dict2.setSortOrder(2);
        
        DataDict dict3 = createTestDict(3L, "STATUS", "PENDING", "待处理");
        dict3.setSortOrder(3);
        
        when(dataDictService.getDictsByType("STATUS")).thenReturn(Arrays.asList(dict1, dict2, dict3));

        // when & then
        performGet("/api/system/dict/type/STATUS")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sortOrder").value(1))
                .andExpect(jsonPath("$[1].sortOrder").value(2))
                .andExpect(jsonPath("$[2].sortOrder").value(3));
    }

    @Test
    void shouldUpdateDictSortOrder() throws Exception {
        // given
        DataDict dict = createTestDict(1L, "STATUS", "ACTIVE", "启用");
        dict.setSortOrder(5);
        when(dataDictService.saveDict(any(DataDict.class))).thenReturn(dict);

        // when & then
        performPut("/api/system/dict/1", dict)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortOrder").value(5));
    }
}
