package com.ccms.controller.system;

import com.ccms.entity.system.DataDict;
import com.ccms.service.system.DataDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据字典管理控制器
 */
@RestController
@RequestMapping("/api/system/dict")
public class DataDictController {

    @Autowired
    private DataDictService dataDictService;

    /**
     * 获取所有字典类型
     */
    @GetMapping("/types")
    public ResponseEntity<List<String>> getAllDictTypes() {
        List<String> dictTypes = dataDictService.getAllDictTypes();
        return ResponseEntity.ok(dictTypes);
    }

    /**
     * 根据字典类型获取字典项
     */
    @GetMapping("/{dictType}")
    public ResponseEntity<List<DataDict>> getDictsByType(@PathVariable String dictType) {
        List<DataDict> dicts = dataDictService.getDictsByType(dictType);
        return ResponseEntity.ok(dicts);
    }

    /**
     * 根据字典类型和字典码获取字典项详细信息
     */
    @GetMapping("/{dictType}/{dictCode}")
    public ResponseEntity<DataDict> getDictDetail(@PathVariable String dictType, @PathVariable String dictCode) {
        DataDict dict = dataDictService.getDictByTypeAndCode(dictType, dictCode);
        return dict != null ? ResponseEntity.ok(dict) : ResponseEntity.notFound().build();
    }

    /**
     * 获取字典项名称
     */
    @GetMapping("/{dictType}/{dictCode}/name")
    public ResponseEntity<String> getDictName(@PathVariable String dictType, @PathVariable String dictCode) {
        String dictName = dataDictService.getDictName(dictType, dictCode);
        return dictName != null ? ResponseEntity.ok(dictName) : ResponseEntity.notFound().build();
    }

    /**
     * 获取字典项值
     */
    @GetMapping("/{dictType}/{dictCode}/value")
    public ResponseEntity<String> getDictValue(@PathVariable String dictType, @PathVariable String dictCode) {
        String dictValue = dataDictService.getDictValue(dictType, dictCode);
        return dictValue != null ? ResponseEntity.ok(dictValue) : ResponseEntity.notFound().build();
    }

    /**
     * 获取字典项的代码映射（code -> name）
     */
    @GetMapping("/{dictType}/code-name")
    public ResponseEntity<Map<String, String>> getDictCodeNameMap(@PathVariable String dictType) {
        Map<String, String> codeNameMap = dataDictService.getDictCodeNameMap(dictType);
        return ResponseEntity.ok(codeNameMap);
    }

    /**
     * 获取字典项的代码映射（code -> value）
     */
    @GetMapping("/{dictType}/code-value")
    public ResponseEntity<Map<String, String>> getDictCodeValueMap(@PathVariable String dictType) {
        Map<String, String> codeValueMap = dataDictService.getDictCodeValueMap(dictType);
        return ResponseEntity.ok(codeValueMap);
    }

    /**
     * 创建新的字典项
     */
    @PostMapping
    public ResponseEntity<DataDict> createDict(@RequestBody DataDict dict) {
        try {
            DataDict savedDict = dataDictService.saveDict(dict);
            return ResponseEntity.ok(savedDict);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新字典项
     */
    @PutMapping("/{dictId}")
    public ResponseEntity<DataDict> updateDict(@PathVariable Long dictId, @RequestBody DataDict dict) {
        if (!dictId.equals(dict.getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            DataDict savedDict = dataDictService.saveDict(dict);
            return ResponseEntity.ok(savedDict);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新字典项状态
     */
    @PutMapping("/{dictId}/status")
    public ResponseEntity<Void> updateDictStatus(@PathVariable Long dictId, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        
        dataDictService.updateDictStatus(dictId, status);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量更新字典项排序
     */
    @PutMapping("/batch/sort")
    public ResponseEntity<Void> batchUpdateSortOrder(@RequestBody Map<Long, Integer> sortOrderMap) {
        try {
            dataDictService.batchUpdateSortOrder(sortOrderMap);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除字典项
     */
    @DeleteMapping("/{dictId}")
    public ResponseEntity<Void> deleteDict(@PathVariable Long dictId) {
        try {
            dataDictService.deleteDict(dictId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取字典树形结构
     */
    @GetMapping("/{dictType}/tree")
    public ResponseEntity<List<DataDict>> getDictTree(@PathVariable String dictType) {
        List<DataDict> dictTree = dataDictService.getDictTree(dictType);
        return ResponseEntity.ok(dictTree);
    }

    /**
     * 根据父级ID获取子字典项
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<DataDict>> getChildrenByParentId(@PathVariable Long parentId) {
        List<DataDict> children = dataDictService.getChildrenByParentId(parentId);
        return ResponseEntity.ok(children);
    }

    /**
     * 获取顶层字典项
     */
    @GetMapping("/top-level")
    public ResponseEntity<List<DataDict>> getTopLevelDicts() {
        List<DataDict> topLevelDicts = dataDictService.getTopLevelDicts();
        return ResponseEntity.ok(topLevelDicts);
    }

    /**
     * 获取内置字典项
     */
    @GetMapping("/built-in")
    public ResponseEntity<List<DataDict>> getBuiltInDicts() {
        List<DataDict> builtInDicts = dataDictService.getBuiltInDicts();
        return ResponseEntity.ok(builtInDicts);
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
     * 检查字典项是否存在
     */
    @GetMapping("/{dictType}/{dictCode}/exists")
    public ResponseEntity<Boolean> checkDictExists(@PathVariable String dictType, @PathVariable String dictCode) {
        boolean exists = dataDictService.dictExists(dictType, dictCode);
        return ResponseEntity.ok(exists);
    }

    /**
     * 导入字典数据
     */
    @PostMapping("/import")
    public ResponseEntity<Void> importDictData(@RequestBody List<DataDict> dictData) {
        try {
            dataDictService.importDictData(dictData);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 导出字典数据
     */
    @GetMapping("/{dictType}/export")
    public ResponseEntity<List<DataDict>> exportDictData(@PathVariable String dictType) {
        List<DataDict> dictData = dataDictService.exportDictData(dictType);
        return ResponseEntity.ok(dictData);
    }
}