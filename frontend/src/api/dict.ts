import request from '@/utils/request'
import type { DataDict, DictType } from '@/types/system'

/**
 * 数据字典API
 */
export const dictApi = {
  /**
   * 根据字典类型获取字典列表
   */
  getDictsByType: (dictType: string) => {
    return request.get<DataDict[]>(`/system/dict/type/${dictType}`)
  },

  /**
   * 根据字典类型和编码获取字典项
   */
  getDictByTypeAndCode: (dictType: string, dictCode: string) => {
    return request.get<DataDict>(`/system/dict/type/${dictType}/code/${dictCode}`)
  },

  /**
   * 根据字典类型获取字典项名称
   */
  getDictName: (dictType: string, dictCode: string) => {
    return request.get<string>(`/system/dict/type/${dictType}/code/${dictCode}/name`)
  },

  /**
   * 根据字典类型获取字典项值
   */
  getDictValue: (dictType: string, dictCode: string) => {
    return request.get<string>(`/system/dict/type/${dictType}/code/${dictCode}/value`)
  },

  /**
   * 获取所有字典类型
   */
  getAllDictTypes: () => {
    return request.get<string[]>('/system/dict/types')
  },

  /**
   * 获取字典项树形结构
   */
  getDictTree: (dictType: string) => {
    return request.get<DataDict[]>(`/system/dict/type/${dictType}/tree`)
  },

  /**
   * 根据父级ID获取子字典项
   */
  getChildrenByParentId: (parentId: number) => {
    return request.get<DataDict[]>(`/system/dict/parent/${parentId}/children`)
  },

  /**
   * 获取顶层字典项
   */
  getTopLevelDicts: () => {
    return request.get<DataDict[]>('/system/dict/top-level')
  },

  /**
   * 创建字典项
   */
  createDict: (data: Partial<DataDict>) => {
    return request.post<DataDict>('/system/dict', data)
  },

  /**
   * 更新字典项
   */
  updateDict: (dictId: number, data: Partial<DataDict>) => {
    return request.put<DataDict>(`/system/dict/${dictId}`, data)
  },

  /**
   * 更新字典项状态
   */
  updateDictStatus: (dictId: number, status: number) => {
    return request.put(`/system/dict/${dictId}/status`, null, { params: { status } })
  },

  /**
   * 删除字典项
   */
  deleteDict: (dictId: number) => {
    return request.delete(`/system/dict/${dictId}`)
  },

  /**
   * 批量更新字典项排序
   */
  batchUpdateSortOrder: (sortOrderMap: Record<number, number>) => {
    return request.post('/system/dict/batch-update-sort', sortOrderMap)
  },

  /**
   * 检查字典项是否存在
   */
  checkDictExists: (dictType: string, dictCode: string) => {
    return request.get<{ exists: boolean }>('/system/dict/check-exists', {
      params: { dictType, dictCode }
    })
  },

  /**
   * 获取字典项映射（code -> name）
   */
  getDictCodeNameMap: (dictType: string) => {
    return request.get.get<Record<string, string>>(`/system/dict/type/${dictType}/code-name-map`)
  },

  /**
   * 获取字典项映射（code -> value）
   */
  getDictCodeValueMap: (dictType: string) => {
    return request.get.get<Record<string, string>>(`/system/dict/type/${dictType}/code-value-map`)
  },

  /**
   * 重新加载字典缓存
   */
  reloadDictCache: () => {
    return request.post('/system/dict/reload-cache')
  },

  /**
   * 获取内置字典项
   */
  getBuiltInDicts: () => {
    return request.get<DataDict[]>('/system/dict/built-in')
  },

  /**
   * 导入字典数据
   */
  importDictData: (dictData: DataDict[]) => {
    return request.post('/system/dict/import', dictData)
  },

  /**
   * 导出字典数据
   */
  exportDictData: (dictType?: string) => {
    return request.get<DataDict[]>('/system/dict/export', { params: { dictType } })
  }
}
