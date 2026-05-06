package com.ccms.controller.system;

import com.ccms.controller.BaseControllerTest;
import com.ccms.entity.system.DataDict;
import com.ccms.service.system.DataDictService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 数据字典控制器单元测试
 */
@WebMvcTest(DataDictController.class)
class DataDictControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataDictService dataDictService;

    @Autowired
    private ObjectMapper objectMapper;

    private DataDict testDict;

    @BeforeEach
    void setUp() {
        testDict = createTestDict();
    }

    @Test
    void shouldGetAllDictTypesSuccessfully() throws Exception {
        // Given
        List<String> types = Arrays.asList("expense_type", "approval_status", "user_role");
        when(dataDictService.getAllDictTypes()).thenReturn(types);

        // When & Then
        mockMvc.perform(get("/api/system/dict/types"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldGetDictsByTypeSuccessfully() throws Exception {
        // Given
        List<DataDict> dicts = Arrays.asList(testDict, createTestDict(2L, "TRANSPORT", "交通费"));
        when(dataDictService.getDictsByType(eq("expense_type"))).thenReturn(dicts);

        // When & Then
        mockMvc.perform(get("/api/system/dict/{dictType}", "expense_type"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetDictDetailSuccessfully() throws Exception {
        // Given
        when(dataDictService.getDictByTypeAndCode(eq("expense_type"), eq("TRAVEL"))).thenReturn(testDict);

        // When & Then
        mockMvc.perform(get("/api/system/dict/{dictType}/{dictCode}", "expense_type", "TRAVEL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dictCode").value("TRAVEL"));
    }

    @Test
    void shouldReturnNotFound_whenDictNotExists() throws Exception {
        // Given
        when(dataDictService.getDictByTypeAndCode(anyString(), anyString())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/system/dict/{dictType}/{dictCode}", "expense_type", "NONEXISTENT"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetDictNameSuccessfully() throws Exception {
        // Given
        when(dataDictService.getDictName(eq("expense_type"), eq("TRAVEL"))).thenReturn("差旅费");

        // When & Then
        mockMvc.perform(get("/api/system/dict/{dictType}/{dictCode}/name", "expense_type", "TRAVEL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("差旅费"));
    }

    @Test
    void shouldGetDictValueSuccessfully() throws Exception {
        // Given
        when(dataDictService.getDictValue(eq("expense_type"), eq("TRAVEL"))).thenReturn("1");

        // When & Then
        mockMvc.perform(get("/api/system/dict/{dictType}/{dictCode}/value", "expense_type", "TRAVEL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void shouldGetDictCodeNameMapSuccessfully() throws Exception {
        // Given
        Map<String, String> map = new HashMap<>();
        map.put("TRAVEL", "差旅费");
        map.put("MEAL", "餐费");
        when(dataDictService.getDictCodeNameMap(eq("expense_type"))).thenReturn(map);

        // When & Then
        mockMvc.perform(get("/api/system/dict/{dictType}/code-name", "expense_type"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.TRAVEL").value("差旅费"));
    }

    @Test
    void shouldGetDictCodeValueMapSuccessfully() throws Exception {
        // Given
        Map<String, String> map = new HashMap<>();
        map.put("TRAVEL", "1");
        map.put("MEAL", "2");
        when(dataDictService.getDictCodeValueMap(eq("expense_type"))).thenReturn(map);

        // When & Then
        mockMvc.perform(get("/api/system/dict/{dictType}/code-value", "expense_type"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.TRAVEL").value("1"));
    }

    @Test
    void shouldCreateDictSuccessfully() throws Exception {
        // Given
        when(dataDictService.saveDict(any(DataDict.class))).thenReturn(testDict);

        // When & Then
        mockMvc.perform(post("/api/system/dict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDict)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dictCode").value("TRAVEL"));
    }

    @Test
    void shouldUpdateDictSuccessfully() throws Exception {
        // Given
        DataDict updated = createTestDict();
        updated.setDictName("Updated Name");
        when(dataDictService.saveDict(any(DataDict.class))).thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/system/dict/{dictId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateDictStatusSuccessfully() throws Exception {
        // Given
        doNothing().when(dataDictService).updateDictStatus(eq(1L), eq(0));

        // When & Then
        mockMvc.perform(put("/api/system/dict/{dictId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": 0}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldBatchUpdateSortOrderSuccessfully() throws Exception {
        // Given
        doNothing().when(dataDictService).batchUpdateSortOrder(anyMap());

        Map<Long, Integer> sortOrderMap = new HashMap<>();
        sortOrderMap.put(1L, 1);
        sortOrderMap.put(2L, 2);

        // When & Then
        mockMvc.perform(put("/api/system/dict/batch/sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sortOrderMap)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteDictSuccessfully() throws Exception {
        // Given
        doNothing().when(dataDictService).deleteDict(eq(1L));

        // When & Then
        mockMvc.perform(delete("/api/system/dict/{dictId}", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDictTreeSuccessfully() throws Exception {
        // Given
        List<DataDict> tree = Arrays.asList(testDict);
        when(dataDictService.getDictTree(eq("expense_type"))).thenReturn(tree);

        // When & Then
        mockMvc.perform(get("/api/system/dict/{dictType}/tree", "expense_type"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetChildrenByParentIdSuccessfully() throws Exception {
        // Given
        List<DataDict> children = Arrays.asList(createTestDict(2L, "SUB1", "子项1"));
        when(dataDictService.getChildrenByParentId(eq(1L))).thenReturn(children);

        // When & Then
        mockMvc.perform(get("/api/system/dict/parent/{parentId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetTopLevelDictsSuccessfully() throws Exception {
        // Given
        List<DataDict> topLevel = Arrays.asList(testDict);
        when(dataDictService.getTopLevelDicts()).thenReturn(topLevel);

        // When & Then
        mockMvc.perform(get("/api/system/dict/top-level"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetBuiltInDictsSuccessfully() throws Exception {
        // Given
        List<DataDict> builtIn = Arrays.asList(testDict);
        when(dataDictService.getBuiltInDicts()).thenReturn(builtIn);

        // When & Then
        mockMvc.perform(get("/api/system/dict/built-in"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReloadDictCacheSuccessfully() throws Exception {
        // Given
        doNothing().when(dataDictService).reloadDictCache();

        // When & Then
        mockMvc.perform(post("/api/system/dict/reload-cache"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldCheckDictExistsSuccessfully() throws Exception {
        // Given
        when(dataDictService.dictExists(eq("expense_type"), eq("TRAVEL"))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/system/dict/{dictType}/{dictCode}/exists", "expense_type", "TRAVEL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldImportDictDataSuccessfully() throws Exception {
        // Given
        doNothing().when(dataDictService).importDictData(anyList());

        List<DataDict> dictData = Arrays.asList(testDict);

        // When & Then
        mockMvc.perform(post("/api/system/dict/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dictData)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldExportDictDataSuccessfully() throws Exception {
        // Given
        List<DataDict> dictData = Arrays.asList(testDict);
        when(dataDictService.exportDictData(eq("expense_type"))).thenReturn(dictData);

        // When & Then
        mockMvc.perform(get("/api/system/dict/{dictType}/export", "expense_type"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private DataDict createTestDict() {
        return createTestDict(1L, "TRAVEL", "差旅费");
    }

    private DataDict createTestDict(Long id, String code, String name) {
        DataDict dict = new DataDict();
        dict.setId(id);
        dict.setDictType("expense_type");
        dict.setDictCode(code);
        dict.setDictName(name);
        dict.setDictValue("1");
        dict.setStatus(1);
        dict.setSortOrder(1);
        return dict;
    }
}
