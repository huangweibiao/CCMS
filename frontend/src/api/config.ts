import request from '@/utils/request'
import type { SystemConfig, ConfigType } from '@/types/system'

/**
 * 系统配置API
 */
export const configApi = {
  /**
   * 根据配置键获取配置值
   */
  getConfigValue: (configKey: string) => {
    return request.get<string>(`/system/config/${configKey}`)
  },

  /**
   * 根据配置键获取配置值（带默认值）
   */
  getConfigValueWithDefault: (configKey: string, defaultValue: string) => {
    return request.get<string>(`/system/config/${configKey}/default`, {
      params: { defaultValue }
    })
  },

  /**
   * 根据配置键获取整型配置值
   */
  getIntConfigValue: (configKey: string) => {
    return request.get<number>(`/system/config/${configKey}/int`)
  },

  /**
   * 根据配置键获取布尔型配置值
   */
  getBooleanConfigValue: (configKey: string) => {
    return request.get<boolean>(`/system/config/${configKey}/boolean`)
  },

  /**
   * 根据配置键获取配置对象
   */
  getConfigByKey: (configKey: string) => {
    return request.get<SystemConfig>(`/system/config/${configKey}/detail`)
  },

  /**
   * 获取所有启用的配置
   */
  getAllEnabledConfigs: () => {
    return request.get<SystemConfig[]>('/system/config/all-enabled')
  },

  /**
   * 根据配置类型获取配置
   */
  getConfigsByType: (configType: ConfigType) => {
    return request.get<SystemConfig[]>(`/system/config/type/${configType}`)
  },

  /**
   * 获取所有配置键值对
   */
  getAllConfigsAsMap: () => {
    return request.get.get<Record<string, string>>('/system/config/all-map')
  },

  /**
   * 根据前缀获取配置
   */
  getConfigsByPrefix: (prefix: string) => {
    return request.get<SystemConfig[]>(`/system/config/prefix/${prefix}`)
  },

  /**
   * 创建或更新配置
   */
  saveConfig: (data: Partial<SystemConfig>) => {
    return request.post<SystemConfig>('/system/config', data)
  },

  /**
   * 根据配置键更新配置值
   */
  updateConfigValue: (configKey: string, configValue: string) => {
    return request.put<SystemConfig>(`/system/config/${configKey}/value`, null, {
      params: { configValue }
    })
  },

  /**
   * 根据配置键删除配置
   */
  deleteConfigByKey: (configKey: string) => {
    return request.delete(`/system/config/${configKey}`)
  },

  /**
   * 检查配置键是否存在
   */
  checkConfigKeyExists: (configKey: string) => {
    return request.get<{ exists: boolean }>(`/system/config/${configKey}/exists`)
  },

  /**
   * 批量更新配置
   */
  batchUpdateConfigs: (configUpdates: Record<string, string>) => {
    return request.post('/system/config/batch-update', configUpdates)
  },

  /**
   * 验证配置值格式
   */
  validateConfigValue: (configType: ConfigType, value: string) => {
    return request.post<{ valid: boolean }>('/system/config/validate', null, {
      params: { configType, value }
    })
  },

  /**
   * 重新加载配置缓存
   */
  reloadConfigCache: () => {
    return request.post('/system/config/reload-cache')
  }
}
