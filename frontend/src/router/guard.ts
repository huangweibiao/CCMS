import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { PermissionUtil } from '@/utils/permission'
import { usePermissionStore } from '@/stores/permission'
import { RouterUtils } from '@/router/utils'
import type { AppRouteRecordRaw } from '@/router/types'

/**
 * 路由守卫管理器
 * 处理路由权限验证、动态路由加载等
 */
export class RouterGuard {
  private static isInitialized = false
  private static permissionStore = usePermissionStore()

  /**
   * 全局前置守卫
   */
  static async beforeEach(
    to: RouteLocationNormalized,
    from: RouteLocationNormalized,
    next: NavigationGuardNext
  ): Promise<void> {
    // 设置页面标题
    this.setDocumentTitle(to)

    // 特殊路径处理
    if (to.path === '/login') {
      await this.handleLoginRoute(to, next)
      return
    }

    if (to.path === '/404' || to.path === '/403') {
      next()
      return
    }

    // 检查认证状态
    const isAuthenticated = PermissionUtil.isAuthenticated()
    if (!isAuthenticated) {
      if (this.requiresAuth(to)) {
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
      }
      next()
      return
    }

    // 已认证用户的路由处理
    await this.handleAuthenticatedRoute(to, from, next)
  }

  /**
   * 设置文档标题
   */
  private static setDocumentTitle(to: RouteLocationNormalized): void {
    const title = to.meta?.title as string
    if (title) {
      document.title = `${title} - CCMS`
    }
  }

  /**
   * 处理登录路由
   */
  private static async handleLoginRoute(
    to: RouteLocationNormalized,
    next: NavigationGuardNext
  ): Promise<void> {
    // 如果已登录，重定向到首页或目标页面
    if (PermissionUtil.isAuthenticated()) {
      const redirect = to.query.redirect as string || '/dashboard'
      next(redirect)
      return
    }
    
    next()
  }

  /**
   * 处理已认证用户的路由
   */
  private static async handleAuthenticatedRoute(
    to: RouteLocationNormalized,
    from: RouteLocationNormalized,
    next: NavigationGuardNext
  ): Promise<void> {
    try {
      // 初始化权限数据（如果尚未初始化）
      if (!this.isInitialized && !this.permissionStore.hasLoadedPermissions) {
        await this.permissionStore.loadUserPermissions()
        this.isInitialized = true
      }

      // 检查路由权限
      const routeConfig = this.getRouteConfig(to)
      const accessResult = await RouterUtils.checkRouteAccess(routeConfig)

      if (!accessResult.access) {
        // 权限不足处理
        this.handleAccessDenied(accessResult, to, next)
        return
      }

      // 检查动态路由是否已加载
      if (this.requiresDynamicRoute(to) && !this.hasRoute(to.path)) {
        // 尝试从菜单权限中查找对应路由
        const menu = await PermissionUtil.getMenuByPath(to.path)
        if (!menu) {
          next('/404')
          return
        }

        // 如果菜单存在但没有对应路由，可能需要重新加载动态路由
        console.warn(`路由未找到但菜单存在: ${to.path}`)
      }

      next()
    } catch (error) {
      console.error('路由守卫处理失败:', error)
      // 权限加载失败时重定向到登录页
      PermissionUtil.reset()
      next({ path: '/login', query: { redirect: to.fullPath } })
    }
  }

  /**
   * 处理权限不足的情况
   */
  private static handleAccessDenied(
    accessResult: { access: boolean; redirect?: string; message?: string },
    to: RouteLocationNormalized,
    next: NavigationGuardNext
  ): void {
    if (accessResult.redirect) {
      // 重定向到指定页面
      next(accessResult.redirect)
    } else {
      // 显示权限不足页面
      next({
        path: '/403',
        query: {
          from: to.fullPath,
          message: accessResult.message || '权限不足'
        }
      })
    }
  }

  /**
   * 判断路由是否需要认证
   */
  private static requiresAuth(to: RouteLocationNormalized): boolean {
    return to.meta?.requiresAuth === true || 
           to.meta?.requiresAdmin === true || 
           to.meta?.permissionCode != null
  }

  /**
   * 判断路由是否需要动态加载
   */
  private static requiresDynamicRoute(to: RouteLocationNormalized): boolean {
    // 排除静态路由和特殊路由
    const staticRoutes = ['/login', '/404', '/403', '/dashboard']
    return !staticRoutes.includes(to.path) && 
           !to.path.startsWith('/layout')
  }

  /**
   * 获取路由配置（模拟实现）
   */
  private static getRouteConfig(to: RouteLocationNormalized): any {
    return {
      meta: to.meta,
      path: to.path
    }
  }

  /**
   * 检查路由是否存在（模拟实现）
   */
  private static hasRoute(path: string): boolean {
    // 在实际实现中，这里需要检查路由表中是否存在该路由
    // 暂时返回true，后续需要根据动态路由加载机制实现
    return true
  }

  /**
   * 重置路由守卫状态
   */
  static reset(): void {
    this.isInitialized = false
    PermissionUtil.reset()
  }
}