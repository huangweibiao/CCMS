import request from '@/utils/request'
import type {
  MenuItem,
  MenuTree,
  PermissionCheck,
  PermissionResult
} from '@/types/permission'

export const permissionApi = {
  /**
   * 获取用户菜单权限树
   */
  getUserMenuTree: (): Promise<ApiResponse<MenuTree[]>> => {
    return request.get('/api/menus/tree')
  },

  /**
   * 获取菜单列表
   */
  getMenuList: (params?: {
    menuName?: string
    status?: number
    menuType?: number
  }): Promise<ApiResponse<MenuItem[]>> => {
    return request.get('/api/menus', { params })
  },

  /**
   * 检查菜单权限
   */
  checkMenuPermission: (menuCode: string): Promise<ApiResponse<boolean>> => {
    return request.post('/api/menus/check', { menuCode })
  },

  /**
   * 批量检查权限
   */
  batchCheckPermissions: (permissions: string[]): Promise<ApiResponse<PermissionResult[]>> => {
    return request.post('/api/permissions/check-batch', { permissions })
  },

  /**
   * 获取权限集合
   */
  getPermissionSet: (): Promise<ApiResponse<string[]>> => {
    return request.get('/api/permissions/set')
  },

  /**
   * 验证数据权限
   */
  validateDataPermission: (resource: string, operation?: string): Promise<ApiResponse<boolean>> => {
    return request.post('/api/permissions/validate-data', {
      resource,
      operation: operation || 'read'
    })
  }
}