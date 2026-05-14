import { usePermissionStore } from '@/stores/permission'
import { useUserStore } from '@/stores/user'
import type { MenuItem } from '@/types/permission'

/**
 * 权限工具类
 * 提供权限检查的便捷方法和验证逻辑
 */
export class PermissionUtil {
  
  /**
   * 获取权限store实例
   */
  private static getPermissionStore() {
    return usePermissionStore()
  }

  /**
   * 获取用户store实例
   */
  private static getUserStore() {
    return useUserStore()
  }

  /**
   * 验证用户是否有权限
   * @param permissionCode 权限代码
   * @param needLoad 如果未加载权限是否自动加载
   */
  static async hasPermission(permissionCode: string, needLoad: boolean = true): Promise<boolean> {
    const userStore = this.getUserStore()
    const permissionStore = this.getPermissionStore()

    // 未登录用户无权限
    if (!userStore.isAuthenticated) {
      return false
    }

    // 管理员拥有所有权限
    if (userStore.isAdmin) {
      return true
    }

    // 尝试从缓存检查
    if (permissionStore.hasLoadedPermissions) {
      return permissionStore.hasPermission(permissionCode)
    }

    // 需要加载权限但未加载
    if (needLoad) {
      try {
        await permissionStore.loadUserPermissions()
        return permissionStore.hasPermission(permissionCode)
      } catch (error) {
        console.error('权限加载失败:', error)
        return false
      }
    }

    return false
  }

  /**
   * 批量检查权限
   */
  static async hasPermissions(permissionCodes: string[], needLoad: boolean = true): Promise<boolean> {
    if (permissionCodes.length === 0) return true

    const userStore = this.getUserStore()
    if (!userStore.isAuthenticated) return false

    // 管理员拥有所有权限
    if (userStore.isAdmin) return true

    const permissionStore = this.getPermissionStore()

    if (!permissionStore.hasLoadedPermissions && needLoad) {
      try {
        await permissionStore.loadUserPermissions()
      } catch (error) {
        console.error('权限加载失败:', error)
        return false
      }
    }

    return permissionCodes.every(code => permissionStore.hasPermission(code))
  }

  /**
   * 检查菜单权限
   */
  static async hasMenuPermission(menuCode: string, needLoad: boolean = true): Promise<boolean> {
    const userStore = this.getUserStore()
    if (!userStore.isAuthenticated) return false

    // 管理员拥有所有权限
    if (userStore.isAdmin) return true

    const permissionStore = this.getPermissionStore()

    if (!permissionStore.hasLoadedPermissions && needLoad) {
      try {
        await permissionStore.loadUserPermissions()
      } catch (error) {
        console.error('权限加载失败:', error)
        return false
      }
    }

    return permissionStore.hasMenuPermission(menuCode)
  }

  /**
   * 检查路径权限
   */
  static async hasPathPermission(path: string, needLoad: boolean = true): Promise<boolean> {
    const userStore = this.getUserStore()
    if (!userStore.isAuthenticated) return false

    // 管理员可以访问所有路径
    if (userStore.isAdmin) return true

    const permissionStore = this.getPermissionStore()

    if (!permissionStore.hasLoadedPermissions && needLoad) {
      try {
        await permissionStore.loadUserPermissions()
      } catch (error) {
        console.error('权限加载失败:', error)
        return false
      }
    }

    return permissionStore.hasPathPermission(path)
  }

  /**
   * 获取用户可访问的菜单项
   */
  static async getUserMenuTree(): Promise<MenuItem[]> {
    const userStore = this.getUserStore()
    const permissionStore = this.getPermissionStore()

    if (!userStore.isAuthenticated) {
      return []
    }

    if (!permissionStore.hasLoadedPermissions) {
      try {
        await permissionStore.loadUserPermissions()
      } catch (error) {
        console.error('菜单加载失败:', error)
        return []
      }
    }

    // 过滤可见的菜单项
    const filterVisibleMenus = (menus: MenuItem[]): MenuItem[] => {
      return menus
        .filter(menu => menu.visible && menu.status === 1)
        .map(menu => ({
          ...menu,
          children: menu.children ? filterVisibleMenus(menu.children) : undefined
        }))
        .filter(menu => {
          // 确保有孩子的目录不会被过滤掉
          return menu.children && menu.children.length > 0 ? true : !!menu.path
        })
    }

    return filterVisibleMenus(permissionStore.flattedMenuList)
  }

  /**
   * 根据路径获取菜单信息
   */
  static async getMenuByPath(path: string): Promise<MenuItem | undefined> {
    const userStore = this.getUserStore()
    const permissionStore = this.getPermissionStore()

    if (!userStore.isAuthenticated) return undefined

    if (!permissionStore.hasLoadedPermissions) {
      try {
        await permissionStore.loadUserPermissions()
      } catch (error) {
        console.error('菜单加载失败:', error)
        return undefined
      }
    }

    return permissionStore.getMenuByPath(path)
  }

  /**
   * 根据菜单代码获取菜单信息
   */
  static async getMenuByCode(menuCode: string): Promise<MenuItem | undefined> {
    const userStore = this.getUserStore()
    const permissionStore = this.getPermissionStore()

    if (!userStore.isAuthenticated) return undefined

    if (!permissionStore.hasLoadedPermissions) {
      try {
        await permissionStore.loadUserPermissions()
      } catch (error) {
        console.error('菜单加载失败:', error)
        return undefined
      }
    }

    return permissionStore.getMenuByCode(menuCode)
  }

  /**
   * 清除权限缓存
   */
  static clearCache(): void {
    const permissionStore = this.getPermissionStore()
    permissionStore.clearCache()
  }

  /**
   * 重置权限状态
   */
  static reset(): void {
    const permissionStore = this.getPermissionStore()
    permissionStore.reset()
  }

  /**
   * 检查是否为管理员
   */
  static isAdmin(): boolean {
    const userStore = this.getUserStore()
    return userStore.isAdmin
  }

  /**
   * 检查用户是否已登录
   */
  static isAuthenticated(): boolean {
    const userStore = this.getUserStore()
    return userStore.isAuthenticated
  }
}