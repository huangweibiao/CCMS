package com.ccms.service.impl.system;

import com.ccms.entity.system.config.DataDict;
import com.ccms.repository.system.config.DataDictRepository;
import com.ccms.service.system.DataDictService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据字典服务实现类
 */
@Service
@Transactional
public class DataDictServiceImpl implements DataDictService {

    private static final Logger logger = LoggerFactory.getLogger(DataDictServiceImpl.class);

    @Autowired
    private DataDictRepository dataDictRepository;

    // 字典缓存（按字典类型缓存）
    private Map<String, List<DataDict>> dictCache = new ConcurrentHashMap<>();
    private Map<String, Map<String, String>> codeNameCache = new ConcurrentHashMap<>();
    private Map<String, Map<String, String>> codeValueCache = new ConcurrentHashMap<>();
    private volatile boolean cacheInitialized = false;

    @Override
    public DataDict getDictByTypeAndCode(String dictType, String dictCode) {
        initializeCacheIfNeeded();
        
        List<DataDict> dicts = dictCache.get(dictType);
        if (dicts != null) {
            return dicts.stream()
                    .filter(dict -> dict.getDictCode().equals(dictCode) && dict.isEnabled())
                    .findFirst()
                    .orElse(null);
        }
        
        return dataDictRepository.findByDictTypeAndDictCode(dictType, dictCode).orElse(null);
    }

    @Override
    public List<DataDict> getDictsByType(String dictType) {
        initializeCacheIfNeeded();
        
        List<DataDict> cachedDicts = dictCache.get(dictType);
        if (cachedDicts != null) {
            return new ArrayList<>(cachedDicts);
        }
        
                        List<DataDict> dicts = dataDictRepository.findByDictTypeAndStatusOrderBySortOrderAsc(dictType, 1);
        dictCache.put(dictType, dicts);
        updateCodeMaps(dictType, dicts);
        return dicts;
    }

    @Override
    public List<DataDict> getAllDictsByType(String dictType) {
        return dataDictRepository.findByDictTypeOrderBySortOrderAsc(dictType);
    }

    @Override
    public String getDictName(String dictType, String dictCode) {
        return getDictName(dictType, dictCode, null);
    }

    @Override
    public String getDictName(String dictType, String dictCode, String defaultValue) {
        initializeCacheIfNeeded();
        
        Map<String, String> nameMap = codeNameCache.get(dictType);
        if (nameMap != null) {
            String name = nameMap.get(dictCode);
            return name != null ? name : defaultValue;
        }
        
        DataDict dict = getDictByTypeAndCode(dictType, dictCode);
        return dict != null ? dict.getDictName() : defaultValue;
    }

    @Override
    public String getDictValue(String dictType, String dictCode) {
        initializeCacheIfNeeded();
        
        Map<String, String> valueMap = codeValueCache.get(dictType);
        if (valueMap != null) {
            return valueMap.get(dictCode);
        }
        
        DataDict dict = getDictByTypeAndCode(dictType, dictCode);
        return dict != null ? dict.getDictValue() : null;
    }

    @Override
    public DataDict saveDict(DataDict dict) {
        // 验证数据
        if (!validateDictData(dict)) {
            throw new IllegalArgumentException("字典数据格式不正确");
        }

        // 检查唯一性
        if (dict.getId() == null) {
            if (dataDictRepository.existsByDictTypeAndDictCode(dict.getDictType(), dict.getDictCode())) {
                throw new IllegalArgumentException("字典项已存在: " + dict.getDictType() + "-" + dict.getDictCode());
            }
        } else {
            Optional<DataDict> existing = dataDictRepository.findByDictTypeAndDictCode(dict.getDictType(), dict.getDictCode());
            if (existing.isPresent() && !existing.get().getId().equals(dict.getId())) {
                throw new IllegalArgumentException("字典项已存在: " + dict.getDictType() + "-" + dict.getDictCode());
            }
        }

        DataDict savedDict = dataDictRepository.save(dict);
        
        // 清除相关缓存
        clearCacheForType(dict.getDictType());
        logger.info("保存数据字典: {}-{}", dict.getDictType(), dict.getDictCode());
        
        return savedDict;
    }

    @Override
    public void updateDictStatus(Long dictId, Integer status) {
        Optional<DataDict> dictOpt = dataDictRepository.findById(dictId);
        if (dictOpt.isPresent()) {
            DataDict dict = dictOpt.get();
            dict.setStatus(status);
            dataDictRepository.save(dict);
            
            // 清除相关缓存
            clearCacheForType(dict.getDictType());
            logger.info("更新字典状态: {}-{} -> {}", dict.getDictType(), dict.getDictCode(), status);
        }
    }

    @Override
    public void batchUpdateSortOrder(Map<Long, Integer> sortOrderMap) {
        for (Map.Entry<Long, Integer> entry : sortOrderMap.entrySet()) {
            Optional<DataDict> dictOpt = dataDictRepository.findById(entry.getKey());
            if (dictOpt.isPresent()) {
                DataDict dict = dictOpt.get();
                dict.setSortOrder(entry.getValue());
                dataDictRepository.save(dict);
            }
        }
        
        // 清除所有缓存
        clearAllCache();
        logger.info("批量更新字典排序: {} 条记录", sortOrderMap.size());
    }

    @Override
    public void deleteDict(Long dictId) {
        Optional<DataDict> dictOpt = dataDictRepository.findById(dictId);
        if (dictOpt.isPresent()) {
            DataDict dict = dictOpt.get();
            String dictType = dict.getDictType();
            dataDictRepository.deleteById(dictId);
            
            // 清除相关缓存
            clearCacheForType(dictType);
            logger.info("删除字典项: {}-{}", dictType, dict.getDictCode());
        }
    }

    @Override
    public boolean dictExists(String dictType, String dictCode) {
        return dataDictRepository.existsByDictTypeAndDictCode(dictType, dictCode);
    }

    @Override
    public List<String> getAllDictTypes() {
        return dataDictRepository.findAllDistinctDictTypes();
    }

    @Override
    public List<DataDict> getDictTree(String dictType) {
        List<DataDict> allDicts = getDictsByType(dictType);
        return buildDictTree(allDicts);
    }

    @Override
    public List<DataDict> getChildrenByParentId(Long parentId) {
        return dataDictRepository.findChildrenByParentId(parentId);
    }

    @Override
    public List<DataDict> getTopLevelDicts() {
        return dataDictRepository.findByParentIsNullOrderByDictTypeAsc();
    }

    @Override
    public boolean validateDictData(DataDict dict) {
        if (!StringUtils.hasText(dict.getDictType()) || !StringUtils.hasText(dict.getDictCode()) || 
            !StringUtils.hasText(dict.getDictName())) {
            return false;
        }
        
        if (dict.getDictType().length() > 50 || dict.getDictCode().length() > 50 || 
            dict.getDictName().length() > 100) {
            return false;
        }
        
        return true;
    }

    @Override
    public Map<String, String> getDictCodeNameMap(String dictType) {
        initializeCacheIfNeeded();
        
        Map<String, String> nameMap = codeNameCache.get(dictType);
        if (nameMap != null) {
            return new HashMap<>(nameMap);
        }
        
        // 如果缓存中没有，则从数据库加载
        List<DataDict> dicts = getDictsByType(dictType);
        Map<String, String> newMap = dicts.stream()
                .collect(Collectors.toMap(DataDict::getDictCode, DataDict::getDictName));
        codeNameCache.put(dictType, newMap);
        return new HashMap<>(newMap);
    }

    @Override
    public Map<String, String> getDictCodeValueMap(String dictType) {
        initializeCacheIfNeeded();
        
        Map<String, String> valueMap = codeValueCache.get(dictType);
        if (valueMap != null) {
            return new HashMap<>(valueMap);
        }
        
        // 如果缓存中没有，则从数据库加载
        List<DataDict> dicts = getDictsByType(dictType);
        Map<String, String> newMap = dicts.stream()
                .filter(dict -> dict.getDictValue() != null)
                .collect(Collectors.toMap(DataDict::getDictCode, DataDict::getDictValue));
        codeValueCache.put(dictType, newMap);
        return new HashMap<>(newMap);
    }

    @Override
    public void reloadDictCache() {
        dictCache.clear();
        codeNameCache.clear();
        codeValueCache.clear();
        cacheInitialized = false;
        initializeCacheIfNeeded();
        logger.info("重新加载数据字典缓存");
    }

    @Override
    public List<DataDict> getBuiltInDicts() {
        return dataDictRepository.findByBuiltInTrueOrderByDictTypeAscSortOrderAsc();
    }

    @Override
    public void importDictData(List<DataDict> dictData) {
        for (DataDict dict : dictData) {
            if (validateDictData(dict)) {
                try {
                    saveDict(dict);
                } catch (Exception e) {
                    logger.warn("导入字典数据失败: {}-{}", dict.getDictType(), dict.getDictCode(), e);
                }
            }
        }
        logger.info("导入字典数据完成: {} 条记录", dictData.size());
    }

    @Override
    public List<DataDict> exportDictData(String dictType) {
        return getAllDictsByType(dictType);
    }

    /**
     * 构建字典树形结构
     */
    private List<DataDict> buildDictTree(List<DataDict> dicts) {
        Map<Long, DataDict> dictMap = dicts.stream()
                .collect(Collectors.toMap(DataDict::getId, dict -> dict));
        
        List<DataDict> rootDicts = new ArrayList<>();
        for (DataDict dict : dicts) {
            if (dict.getParent() == null) {
                rootDicts.add(dict);
            } else {
                DataDict parent = dictMap.get(dict.getParent().getId());
                if (parent != null) {
                    // 这里可以添加children集合，但实体类中没有定义getChildren方法
                    // 实际使用中可以考虑使用DTO来构建树形结构
                }
            }
        }
        
        return rootDicts.stream()
                .sorted(Comparator.comparing(DataDict::getSortOrder))
                .collect(Collectors.toList());
    }

    /**
     * 更新代码映射缓存
     */
    private void updateCodeMaps(String dictType, List<DataDict> dicts) {
        Map<String, String> nameMap = dicts.stream()
                .collect(Collectors.toMap(DataDict::getDictCode, DataDict::getDictName));
        codeNameCache.put(dictType, nameMap);
        
        Map<String, String> valueMap = dicts.stream()
                .filter(dict -> dict.getDictValue() != null)
                .collect(Collectors.toMap(DataDict::getDictCode, DataDict::getDictValue));
        codeValueCache.put(dictType, valueMap);
    }

    /**
     * 清除指定字典类型的缓存
     */
    private void clearCacheForType(String dictType) {
        dictCache.remove(dictType);
        codeNameCache.remove(dictType);
        codeValueCache.remove(dictType);
    }

    /**
     * 清除所有缓存
     */
    private void clearAllCache() {
        dictCache.clear();
        codeNameCache.clear();
        codeValueCache.clear();
        cacheInitialized = false;
    }

    /**
     * 初始化字典缓存
     */
    private void initializeCacheIfNeeded() {
        if (!cacheInitialized) {
            synchronized (this) {
                if (!cacheInitialized) {
                    List<String> dictTypes = getAllDictTypes();
                    for (String dictType : dictTypes) {
        List<DataDict> dicts = dataDictRepository.findByDictTypeAndStatusOrderBySortOrderAsc(dictType, 1);
                        dictCache.put(dictType, dicts);
                        updateCodeMaps(dictType, dicts);
                    }
                    cacheInitialized = true;
                    logger.info("数据字典缓存初始化完成，加载 {} 个字典类型", dictTypes.size());
                }
            }
        }
    }
}