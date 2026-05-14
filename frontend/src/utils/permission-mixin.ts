import { usePermissionStore } from '@/stores/permission'

export const permissionMixin = {
  methods: {
    /**
     * 检查是否有权限
     * @param permission 权限标识或权限标识数组
     * @param mode 验证模式：'any'（任一权限）或 'all'（所有权限）
     */
    hasPermission(permission: string | string[], mode: 'any' | 'all' = 'any'): boolean {
      const permissionStore = usePermissionStore()
      return permissionStore.hasPermission(permission, mode)
    },

    /**
     * 检查是否有角色
     * @param role 角色标识或角色标识数组
     */
    hasRole(role: string | string[]): boolean {
      const permissionStore = usePermissionStore()
      return permissionStore.hasRole(role)
    },

    /**
     * 检查是否有菜单访问权限
     * @param menuCode 菜单编码
     */
    hasMenuPermission(menuCode: string): boolean {
      const permissionStore = usePermissionStore()
      return permissionStore.hasMenuPermission(menuCode)
    },

    /**
     * 获取当前用户的所有权限标识
     */
    getPermissionCodes(): string[] {
      const permissionStore = usePermissionStore()
      return permissionStore.permissionCodes
    },

    /**
     * 获取当前用户的所有角色标识
     */
    getRoles(): string[] {
      const permissionStore = usePermissionStore()
      return permissionStore.roles
    },

    /**
     * 获取用户菜单树
     */
    getMenuTree() {
      const permissionStore = usePermissionStore()
      return permissionStore.menuTree
    },

    /**
     * 获取用户菜单列表（平铺格式）
     */
    getMenuList() {
      const permissionStore = usePermissionStore()
      return permissionStore.menuList
    },

    /**
     * 刷新权限信息
     */
    async refreshPermissions(): Promise<void> {
      const permissionStore = usePermissionStore()
      await permissionStore.loadPermissions()
    },

    /**
     * 检查是否超级管理员
     */
    isSuperAdmin(): boolean {
      const permissionStore = usePermissionStore()
      return permissionStore.isSuperAdmin()
    },

    /**
     * 权限验证装饰器（用于方法）
     * @param permission 需要的权限
     * @param fallback 无权限时的回调
     */
    withPermission(permission: string | string[], fallback?: () => void): () => boolean {
      return (): boolean => {
        const hasPerm = this.hasPermission(permission)
        if (!hasPerm && fallback) {
          fallback()
        }
        return hasPerm
      }
    }
  }
}

export const roleMixin = {
  methods: {
    /**
     * 检查是否是特定角色
     * @param role 角色标识
     */
    isRole(role: string): boolean {
      const permissionStore = usePermissionStore()
      return permissionStore.hasRole(role)
    },

    /**
     * 检查是否有任一角色
     * @param roles 角色标识数组
     */
    hasAnyRole(roles: string[]): boolean {
      const permissionStore = usePermissionStore()
      return permissionStore.hasRole(roles)
    },

    /**
     * 检查是否有所有角色
     * @param roles 角色标识数组
     */
    hasAllRoles(roles: string[]): boolean {
      const permissionStore = usePermissionStore()
      return roles.every(role => permissionStore.hasRole(role))
    }
  }
}