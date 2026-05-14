/**
 * 权限相关类型定义
 */

export interface MenuItem {
  id: number
  parentId?: number
  menuName: string
  menuCode: string
  menuType: number // 0: 目录, 1: 菜单, 2: 按钮
  path?: string
  component?: string
  icon?: string
  sort: number
  status: number // 0: 禁用, 1: 启用
  visible: boolean
  perms?: string
  remark?: string
  createTime?: string
  updateTime?: string
  children?: MenuItem[]
}

export interface MenuTree extends MenuItem {
  children?: MenuTree[]
}

export interface PermissionCheck {
  permissionCode: string
  resource?: string
  operation?: string
}

export interface PermissionResult {
  permissionCode: string
  hasPermission: boolean
  message?: string
}

export interface UserPermission {
  userId: number
  userName: string
  permissions: string[]
  menuPermissions: MenuTree[]
  roleNames: string[]
  dataScopes?: DataScope[]
}

export interface DataScope {
  resource: string
  scopeType: 'ALL' | 'DEPT' | 'CUSTOM'
  deptIds?: number[]
  conditions?: Record<string, any>
}

export interface RoleInfo {
  id: number
  roleName: string
  roleCode: string
  description?: string
  status: number
  createTime?: string
  updateTime?: string
  permissionCount?: number
}

export interface PermissionState {
  // 权限数据
  menuTree: MenuTree[]
  permissionSet: Set<string>
  hasLoadedPermissions: boolean
  
  // 缓存数据
  menuCache: Map<string, MenuItem>
  permissionCache: Map<string, PermissionResult>
  
  // 加载状态
  isLoading: boolean
  loadError: string | null
}