import request from '@/utils/request'
import type { UserInfo, DeptInfo, RoleInfo } from '@/types/user'

/**
 * 用户管理相关API
 */
export const userApi = {
  /**
   * 获取用户列表
   */
  getUserList: (params: {
    page: number
    size: number
    userName?: string
    deptId?: number
    status?: number
  }) => {
    return request.get<{
      total: number
      list: UserInfo[]
    }>('/user/list', { params })
  },

  /**
   * 创建用户
   */
  createUser: (data: {
    userCode: string
    userName: string
    deptId: number
    role: string
    mobile?: string
    email?: string
    password: string
  }) => {
    return request.post('/user/create', data)
  },

  /**
   * 更新用户信息
   */
  updateUser: (id: number, data: Partial<UserInfo>) => {
    return request.put(`/user/${id}`, data)
  },

  /**
   * 删除用户
   */
  deleteUser: (id: number) => {
    return request.delete(`/user/${id}`)
  },

  /**
   * 重置用户密码
   */
  resetPassword: (id: number) => {
    return request.post(`/user/${id}/resetPassword`)
  },

  /**
   * 获取部门列表
   */
  getDeptList: () => {
    return request.get<DeptInfo[]>('/user/depts')
  },

  /**
   * 获取角色列表
   */
  getRoleList: () => {
    return request.get<RoleInfo[]>('/user/roles')
  }
}