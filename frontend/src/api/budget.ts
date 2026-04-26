import request from '@/utils/request'

/**
 * 预算管理相关API
 */
export const budgetApi = {
  /**
   * 获取预算列表
   */
  getBudgetList: (params: {
    page: number
    size: number
    budgetYear?: number
    deptId?: number
    status?: number
  }) => {
    return request.get<{
      total: number
      list: any[]
    }>('/budget/list', { params })
  },

  /**
   * 创建预算
   */
  createBudget: (data: any) => {
    return request.post('/budget/create', data)
  },

  /**
   * 更新预算信息
   */
  updateBudget: (id: number, data: any) => {
    return request.put(`/budget/${id}`, data)
  }
}