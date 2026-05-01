package com.ccms.service.system;

import com.ccms.entity.system.DataDict;

import java.util.List;
import java.util.Map;

/**
 * 数据字典服务接口
 */
public interface DataDictService {

    /**
     * 根据字典类型和字典码获取字典项
     */
    DataDict getDictByTypeAndCode(String dictType, String dictCode);

    /**
     * 根据字典类型获取所有启用的字典项
     */
    List<DataDict> getDictsByType(String dictType);

    /**
     * 根据字典类型获取所有字典项（包括未启用）
     */
    List<DataDict> getAllDictsByType(String dictType);

    /**
     * 获取字典项名称
     */
    String getDictName(String dictType, String dictCode);

    /**
     * 获取字典项名称，带默认值
     */
    String getDictName(String dictType, String dictCode, String defaultValue);

    /**
     * 获取字典项值
     */
    String getDictValue(String dictType, String dictCode);

    /**
     * 保存字典项
     */
    DataDict saveDict(DataDict dict);

    /**
     * 更新字典项状态
     */
    void updateDictStatus(Long dictId, Integer status);

    /**
     * 批量更新字典项排序
     */
    void batchUpdateSortOrder(Map<Long, Integer> sortOrderMap);

    /**
     * 删除字典项
     */
    void deleteDict(Long dictId);

    /**
     * 检查字典项是否已存在
     */
    boolean dictExists(String dictType, String dictCode);

    /**
     * 获取所有字典类型
     */
    List<String> getAllDictTypes();

    /**
     * 获取字典项树形结构
     */
    List<DataDict> getDictTree(String dictType);

    /**
     * 根据父级ID获取子字典项
     */
    List<DataDict> getChildrenByParentId(Long parentId);

    /**
     * 获取顶层字典项（无父级）
     */
    List<DataDict> getTopLevelDicts();

    /**
     * 验证字典数据格式
     */
    boolean validateDictData(DataDict dict);

    /**
     * 获取字典项映射关系（code -> name）
     */
    Map<String, String> getDictCodeNameMap(String dictType);

    /**
     * 获取字典项映射关系（code -> value）
     */
    Map<String, String> getDictCodeValueMap(String dictType);

    /**
     * 重新加载字典缓存
     */
    void reloadDictCache();

    /**
     * 获取内置字典项
     */
    List<DataDict> getBuiltInDicts();

    /**
     * 导入字典数据
     */
    void importDictData(List<DataDict> dictData);

    /**
     * 导出字典数据
     */
    List<DataDict> exportDictData(String dictType);
}