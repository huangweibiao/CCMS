import request from '@/utils/request'
import type { LoginData, LoginResponse, UserInfo } from '@/types/user'
import type { MenuTree } from '@/types/permission'

/**
 * 扩展的用户信息类型（包含权限信息）
 */
export interface UserInfoWithPermissions extends UserInfo {
  permissions?: string[]
  roles?: string[]
  menuTree?: MenuTree[]
}

/**
 * 扩展的登录响应类型
 */
export interface LoginResponseWithPermissions extends LoginResponse {
  user?: any // 用户基本信息
  permissions?: {
    permissionCodes?: string[]
    menuTree?: MenuTree[]
    menuList?: any[]
  }
}

/**
 * 权限验证请求
 */
export interface PermissionCheckRequest {
  resource: string
  operation?: string
  data?: Record<string, any>
}

/**
 * 认证相关API（扩展版本）
 */
export const authApi = {
  /**
   * 用户登录（增强版，返回权限信息）
   */
  login: (data: LoginData): Promise<ApiResponse<LoginResponseWithPermissions>> => {
    return request.post('/auth/login', data)
  },

  /**
   * 用户登出
   */
  logout: (): Promise<ApiResponse<void>> => {
    return request.post('/auth/logout')
  },

  /**
   * 获取当前用户信息（包含权限）
   */
  getUserInfo: (): Promise<ApiResponse<UserInfoWithPermissions>> => {
    return request.get('/auth/profile')
  },

  /**
   * 仅获取用户基本信息（不包含权限）
   */
  getUserBasicInfo: (): Promise<ApiResponse<UserInfo>> => {
    return request.get('/auth/profile')
  },

  /**
   * 刷新token
   */
  refreshToken: (): Promise<ApiResponse<{ token: string }>> => {
    return request.post('/auth/refresh')
  },

  /**
   * 修改密码
   */
  changePassword: (data: { 
    oldPassword: string; 
    newPassword: string; 
    confirmPassword?: string 
  }): Promise<ApiResponse<void>> => {
    return request.post('/auth/changePassword', data)
  },

  /**
   * 验证token是否有效
   */
  validateToken: (): Promise<ApiResponse<{ valid: boolean }>> => {
    return request.get('/auth/validate')
  },

  /**
   * 获取用户的权限菜单树
   */
  getUserMenuTree: (): Promise<ApiResponse<MenuTree[]>> => {
    return request.get('/auth/menu-tree')
  },

  /**
   * 获取用户权限集合
   */
  getUserPermissions: (): Promise<ApiResponse<any>> => {
    return request.get('/auth/permissions')
  },

  /**
   * 获取用户所有权限标识
   */
  getUserPermissionCodes: (): Promise<ApiResponse<string[]>> => {
    return request.get('/auth/permission-codes')
  },

  /**
   * 校验具体权限
   */
  checkPermission: (data: { permission: string }): Promise<ApiResponse<{ hasPermission: boolean }>> => {
    return request.post('/auth/check-permission', data)
  },

  /**
   * 校验菜单权限
   */
  checkMenuPermission: (data: { menuCode: string }): Promise<ApiResponse<{ hasPermission: boolean }>> => {
    return request.post('/auth/check-menu-permission', data)
  },

  /**
   * 重置密码（管理员用）
   */
  resetPassword: (data: { 
    userId: number; 
    newPassword: string 
  }): Promise<ApiResponse<void>> => {
    return request.post('/auth/resetPassword', data)
  }
}