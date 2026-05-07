import request from '@/utils/request'
import type { OperLog, PageResult } from '@/types/system'

/**
 * 操作日志API
 */
export const logApi = {
  /**
   * 获取操作日志列表（分页）
   */
  getOperLogList: (params: {
    page?: number
    size?: number
    operModule?: string
    operType?: string
    startTime?: string
    endTime?: string
  }) => {
    return request.get.get<PageResultResult<OperLog>>('/system/oper-logs', { params })
  },

  /**
   * 根据ID获取操作日志
   */
  getOperLogById: (logId: number) => {
    return request.get.get<OperLog>(`/system/oper-logs/${logId}`)
  },

  /**
   * 根据操作人ID获取操作日志
   */
  getOperLogsByUserId: (userId: number) => {
    return request.get.get<OperLog[]>(`/system/oper-logs/user/${userId}`)
  },

  /**
   * 根据操作模块获取操作日志
   */
  getOperLogsByModule: (operModule: string) => {
    return request.get.get<OperLog[]>(`/system/oper-logs/module/${operModule}`)
  },

  /**
   * 根据操作类型获取操作日志
   */
  getOperLogsByType: (operType: string) => {
    return request.get.get<OperLog[]>(`/system/oper-logs/type/${operType}`)
  },

  /**
   * 根据业务ID获取操作日志
   */
  getOperLogsByBusinessId: (businessId: number) => {
    return request.get.get<OperLog[]>(`/system/oper-logs/business/${businessId}`)
  },

  /**
   * 根据时间范围获取操作日志
   */
  getOperLogsByTimeRange: (startTime: string, endTime: string) => {
    return request.get.get<OperLog[]>('/system/oper-logs/time-range', {
      params: { startTime, endTime }
    })
  },

  /**
   * 获取最近的操作日志
   */
  getRecentOperLogs: (days: number = 7) => {
    return request.get.get<OperLog[]>('/system/oper-logs/recent', { params: { days } })
  },

  /**
   * 获取模块操作频率统计
   */
  getModuleOperFrequency: (startTime: string, endTime: string) => {
    return request.get<Array<[string, number]>>('/system/oper-logs/module-frequency', {
      params: { startTime, endTime }
    })
  },

  /**
   * 统计用户的操作次数
   */
  countUserOperations: (userId: number, startTime: string, endTime: string) => {
    return request.get.get<{
      userId: number
      count: number
      startTime: string
      endTime: string
    }>(`/system/oper-logs/user/${userId}/count`, {
      params: { startTime, endTime }
    })
  },

  /**
   * 根据操作IP获取操作日志
   */
  getOperLogsByIp: (operIp: string) => {
    return request.get.get<OperLog[]>(`/system/oper-logs/ip/${operIp}`)
  },

  /**
   * 删除过期日志
   */
  deleteExpiredLogs: (expireDays: number = 90) => {
    return request.delete('/system/oper-logs/expired', { params: { expireDays } })
  }
}
