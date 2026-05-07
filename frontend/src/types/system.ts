/**
 * 系统管理相关类型定义
 */

/**
 * 数据字典
 */
export interface DataDict {
  id?: number
  dictType: string
  dictCode: string
  dictName: string
  dictValue?: string
  status?: number
  sortOrder?: number
  remark?: string
  builtIn?: boolean
  parentId?: number
  children?: DataDict[]
}

/**
 * 字典类型
 */
export type DictType = 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON' | 'LIST'

/**
 * 系统配置
 */
export interface SystemConfig {
  id?: number
  configKey: string
  configValue: string
  configName?: string
  description?: string
  configType: ConfigType
  enabled?: boolean
  systemConfig?: boolean
  sortOrder?: number
}

/**
 * 配置类型
 */
export type ConfigType = 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON' | 'LIST'

/**
 * 附件
 */
export interface Attachment {
  id?: number
  businessType?: number
  businessId?: number
  fileName: string
  filePath: string
  fileUrl?: string
  fileSize?: number
  fileType?: string
  storageType?: number
  uploadUserId?: number
  fileMd5?: string
  mimeType?: string
  isPublic?: boolean
  downloadCount?: number
  isDeleted?: boolean
  lastDownloadTime?: string
  deleteTime?: string
  deletedBy?: string
  description?: string
  createTime?: string
  updateTime?: string
}

/**
 * 操作日志
 */
export interface OperLog {
  id?: number
  title?: string
  businessType?: number
  method?: string
  requestMethod?: string
  operatorType?: number
  operUserId?: string
  operName?: string
  deptName?: string
  operModule?: string
  operType?: string
  operContent?: string
  operUrl?: string
  operIp?: string
  operLocation?: string
  operParam?: string
  jsonResult?: string
  status?: number
  errorMsg?: string
  costTime?: number
  businessModule?: string
  businessId?: string
  operTime?: string
  deviceInfo?: string
  userAgent?: string
  createTime?: string
  updateTime?: string
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first?: boolean
  last?: boolean
  empty?: boolean
}
