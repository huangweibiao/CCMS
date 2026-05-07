package com.ccms.controller.system;

import com.ccms.entity.system.config.DataDict;
import com.ccms.service.system.DataDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据字典控制器
 * 对应设计文档：4.9 系统通用表 - 数据字典相关接口
 */
@RestController
@RequestMapping("/api/system/dict")
public class DataDictController {

    private final DataDictService dataDictService;

    @Autowired
    public DataDictController(DataDictService dataDictService) {
        this.dataDictService = dataDictService;
    }

    /**
     * 根据字典类型获取字典列表
     */
    @GetMapping("/type/{dictType}")
    public ResponseEntity<List<DataDict>> getDictsByType(@PathVariable String dictType) {
        List<DataDict> dicts = dataDictService.getDictsByType(dictType);
        return ResponseEntity.ok(dicts);
    }

    /**
     * 根据字典类型和编码获取字典项
     */
    @GetMapping("/type/{dictType}/code/{dictCode}")
    public ResponseEntity<DataDict> getDictByTypeAndCode(
            @PathVariable String dictType,
            @PathVariable String dictCode) {
        DataDict dict = dataDictService.getDictByTypeAndCode(dictType, dictCode);
        if (dict != null) {
            return ResponseEntity.ok(dict);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据字典类型获取字典项名称
     */
    @GetMapping("/type/{dictType}/code/{dictCode}/name")
    public ResponseEntity<String> getDictName(
            @PathVariable String dictType,
            @PathVariable String dictCode) {
        String name = dataDictService.getDictName(dictType, dictCode);
        return ResponseEntity.ok(name);
    }

    /**
     * 根据字典类型获取字典项值
     */
    @GetMapping("/type/{dictType}/code/{dictCode}/value")
    public ResponseEntity<String> getDictValue(
            @PathVariable String dictType,
            @PathVariable String dictCode) {
        String value = dataDictService.getDictValue(dictType, dictCode);
        return ResponseEntity.ok(value);
    }

    /**
     * 获取所有字典类型
     */
    @GetMapping("/types")
    public ResponseEntity<List<String>> getAllDictTypes() {
        List<String> types = dataDictService.getAllDictTypes();
        return ResponseEntity.ok(types);
    }

    /**
     * 获取字典项树形结构
     */
    @GetMapping("/type/{dictType}/tree")
    public ResponseEntity<List<DataDict>> getDictTree(@PathVariable String dictType) {
        List<DataDict> tree = dataDictService.getDictTree(dictType);
        return ResponseEntity.ok(tree);
    }

    /**
     * 根据父级ID获取子字典项
     */
    @GetMapping("/parent/{parentId}/children")
    public ResponseEntity<List<DataDict>> getChildrenByParentId(@PathVariable Long parentId) {
        List<DataDict> children = dataDictService.getChildrenByParentId(parentId);
        return ResponseEntity.ok(children);
    }

    /**
     * 获取顶层字典项
     */
    @GetMapping("/top-level")
    public ResponseEntity<List<DataDict>> getTopLevelDicts() {
        List<DataDict> dicts = dataDictService.getTopLevelDicts();
        return ResponseEntity.ok(dicts);
    }

    /**
     * 创建字典项
     */
    @PostMapping
    public ResponseEntity<DataDict> createDict(@RequestBody DataDict dict) {
        DataDict created = dataDictService.saveDict(dict);
        return ResponseEntity.ok(created);
    }

    /**
     * 更新字典项
     */
    @PutMapping("/{dictId}")
    public ResponseEntity<DataDict> updateDict(
            @PathVariable Long dictId,
            @RequestBody DataDict dict) {
        dict.setId(dictId);
        DataDict updated = dataDictService.saveDict(dict);
        return ResponseEntity.ok(updated);
    }

    /**
     * 更新字典项状态
     */
    @PutMapping("/{dictId}/status")
    public ResponseEntity<Void> updateDictStatus(
            @PathVariable Long dictId,
            @RequestParam Integer status) {
        dataDictService.updateDictStatus(dictId, status);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除字典项
     */
    @DeleteMapping("/{dictId}")
    public ResponseEntity<Void> deleteDict(@PathVariable Long dictId) {
        dataDictService.deleteDict(dictId);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量更新字典项排序
     */
    @PostMapping("/batch-update-sort")
    public ResponseEntity<Void> batchUpdateSortOrder(@RequestBody Map<Long, Integer> sortOrderMap) {
        dataDictService.batchUpdateSortOrder(sortOrderMap);
        return ResponseEntity.ok().build();
    }

    /**
     * 检查字典项是否存在
     */
    @GetMapping("/check-exists")
    public ResponseEntity<Map<String, Boolean>> checkDictExists(
            @RequestParam String dictType,
            @RequestParam String dictCode) {
        boolean exists = dataDictService.dictExists(dictType, dictCode);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * 获取字典项映射（code -> name）
     */
    @GetMapping("/type/{dictType}/code-name-map")
    public ResponseEntity<Map<String, String>> getDictCodeNameMap(@PathVariable String dictType) {
        Map<String, String> map = dataDictService.getDictCodeNameMap(dictType);
        return ResponseEntity.ok(map);
    }

    /**
     * 获取字典项映射（code -> value）
     */
    @GetMapping("/type/{dictType}/code-value-map")
    public ResponseEntity<Map<String, String>> getDictCodeValueMap(@PathVariable String dictType) {
        Map<String, String> map = dataDictService.getDictCodeValueMap(dictType);
        return ResponseEntity.ok(map);
    }

    /**
     * 重新加载字典缓存
     */
    @PostMapping("/reload-cache")
    public ResponseEntity<Void> reloadDictCache() {
        dataDictService.reloadDictCache();
        return ResponseEntity.ok().build();
    }

    /**
     * 获取内置字典项
     */
    @GetMapping("/built-in")
    public ResponseEntity<List<DataDict>> getBuiltInDicts() {
        List<DataDict> dicts = dataDictService.getBuiltInDicts();
        return ResponseEntity.ok(dicts);
    }

    /**
     * 导入字典数据
     */
    @PostMapping("/import")
    public ResponseEntity<Void> importDictData(@RequestBody List<DataDict> dictData) {
        dataDictService.importDictData(dictData);
        return ResponseEntity.ok().build();
    }

    /**
     * 导出字典数据
     */
    @GetMapping("/export")
    public ResponseEntity<List<DataDict>> exportDictData(@RequestParam(required = false) String dictType) {
        List<DataDict> dicts = dataDictService.exportDictData(dictType);
        return ResponseEntity.ok(dicts);
    }
}
