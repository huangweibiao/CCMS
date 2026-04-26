import request from '@/utils/request'
import type { LoginData, LoginResponse, UserInfo } from '@/types/user'

/**
 * 认证相关API
 */
export const authApi = {
  /**
   * 用户登录
   */
  login: (data: LoginData) => {
    return request.post<LoginResponse>('/auth/login', data)
  },

  /**
   * 用户登出
   */
  logout: () => {
    return request.post('/auth/logout')
  },

  /**
   * 获取当前用户信息
   */
  getUserInfo: () => {
    return request.get<UserInfo>('/auth/userInfo')
  },

  /**
   * 刷新token
   */
  refreshToken: () => {
    return request.post<{ token: string }>('/auth/refresh')
  },

  /**
   * 修改密码
   */
  changePassword: (data: { oldPassword: string; newPassword: string }) => {
    return request.post('/auth/changePassword', data)
  }
}