import type { AppRouteRecordRaw, MenuToRouteConfig, MenuTree } from '@/router/types'
import type { MenuItem } from '@/types/permission'
import { PermissionUtil } from '@/utils/permission'
import Layout from '@/views/layout/Layout.vue'

/**
 * 路由工具类
 * 提供路由转换、权限验证等工具方法
 */
export class RouterUtils {
  
  /**
   * 将菜单树转换为路由配置
   */
  static async convertMenuToRoutes(
    menus: MenuTree[],
    config: MenuToRouteConfig
  ): Promise<AppRouteRecordRaw[]> {
    const routes: AppRouteRecordRaw[] = []
    
    for (const menu of menus) {
      const route = this.menuToRoute(menu, config)
      if (route) {
        routes.push(route)
      }
    }
    
    return routes
  }
  
  /**
   * 将单个菜单项转换为路由项
   */
  private static menuToRoute(
    menu: MenuItem,
    config: MenuToRouteConfig
  ): AppRouteRecordRaw | null {
    // 过滤不可见或禁用的菜单
    if (!menu.visible || menu.status !== 1) {
      return null
    }
    
    // 目录类型的菜单，需要处理子菜单
    if (menu.menuType === 0) {
      const children: AppRouteRecordRaw[] = []
      
      if (menu.children && menu.children.length > 0) {
        for (const child of menu.children) {
          const childRoute = this.menuToRoute(child, config)
          if (childRoute) {
            children.push(childRoute)
          }
        }
      }
      
      // 如果目录没有有效的子路由，则不生成路由
      if (children.length === 0) {
        return null
      }
      
      return {
        path: menu.path || `/${menu.menuCode}`,
        name: menu.menuCode,
        component: config.nested ? undefined : Layout,
        meta: {
          title: menu.menuName,
          icon: menu.icon,
          menuSort: menu.sort,
          breadcrumb: true
        },
        children
      }
    }
    
    // 菜单类型，生成实际的路由
    if (menu.menuType === 1) {
      const componentPath = menu.component || menu.menuCode
      const component = config.componentMap[componentPath] || config.defaultComponent
      
      if (!component) {
        console.warn(`未找到组件映射: ${componentPath}`)
        return null
      }
      
      return {
        path: menu.path || `/${menu.menuCode}`,
        name: menu.menuCode,
        component: component,
        meta: {
          title: menu.menuName,
          icon: menu.icon,
          permissionCode: menu.perms,
          menuSort: menu.sort,
          breadcrumb: true,
          keepAlive: true
        }
      }
    }
    
    // 按钮类型，不生成路由
    return null
  }
  
  /**
   * 验证路由访问权限
   */
  static async checkRouteAccess(route: AppRouteRecordRaw): Promise<{
    access: boolean
    redirect?: string
    message?: string
  }> {
    const meta = route.meta || {}
    
    // 检查认证要求
    if (meta.requiresAuth || meta.permissionCode) {
      const isAuthenticated = PermissionUtil.isAuthenticated()
      if (!isAuthenticated) {
        return {
          access: false,
          redirect: '/login',
          message: '请先登录'
        }
      }
    }
    
    // 检查管理员权限
    if (meta.requiresAdmin) {
      const isAdmin = PermissionUtil.isAdmin()
      if (!isAdmin) {
        return {
          access: false,
          redirect: '/dashboard',
          message: '需要管理员权限'
        }
      }
    }
    
    // 检查特定权限代码
    if (meta.permissionCode) {
      const hasPermission = await PermissionUtil.hasPermission(meta.permissionCode)
      if (!hasPermission) {
        return {
          access: false,
          redirect: '/dashboard',
          message: '权限不足'
        }
      }
    }
    
    return { access: true }
  }
  
  /**
   * 扁平化路由配置
   */
  static flattenRoutes(routes: AppRouteRecordRaw[]): AppRouteRecordRaw[] {
    const flatRoutes: AppRouteRecordRaw[] = []
    
    const flatten = (routeList: AppRouteRecordRaw[]) => {
      for (const route of routeList) {
        flatRoutes.push(route)
        if (route.children && route.children.length > 0) {
          flatten(route.children)
        }
      }
    }
    
    flatten(routes)
    return flatRoutes
  }
  
  /**
   * 根据路径查找路由
   */
  static findRouteByPath(
    routes: AppRouteRecordRaw[],
    path: string
  ): AppRouteRecordRaw | null {
    const find = (routeList: AppRouteRecordRaw[]): AppRouteRecordRaw | null => {
      for (const route of routeList) {
        if (route.path === path) {
          return route
        }
        if (route.children && route.children.length > 0) {
          const found = find(route.children)
          if (found) return found
        }
      }
      return null
    }
    
    return find(routes)
  }
  
  /**
   * 生成面包屑导航数据
   */
  static generateBreadcrumbs(
    routes: AppRouteRecordRaw[],
    currentPath: string
  ): Array<{ title: string; path?: string }> {
    const breadcrumbs: Array<{ title: string; path?: string }> = []
    
    const findBreadcrumb = (
      routeList: AppRouteRecordRaw[],
      path: string,
      parentPath: string = ''
    ): boolean => {
      for (const route of routeList) {
        const fullPath = parentPath + route.path
        
        if (fullPath === path) {
          if (route.meta?.breadcrumb !== false && route.meta?.title) {
            breadcrumbs.unshift({
              title: route.meta.title,
              path: fullPath
            })
          }
          return true
        }
        
        if (route.children && route.children.length > 0) {
          if (findBreadcrumb(route.children, path, fullPath + '/')) {
            if (route.meta?.breadcrumb !== false && route.meta?.title) {
              breadcrumbs.unshift({
                title: route.meta.title,
                path: fullPath
              })
            }
            return true
          }
        }
      }
      return false
    }
    
    findBreadcrumb(routes, currentPath)
    return breadcrumbs
  }
  
  /**
   * 获取可访问的路由菜单
   */
  static async getAccessibleMenuRoutes(
    routes: AppRouteRecordRaw[]
  ): Promise<AppRouteRecordRaw[]> {
    const accessibleRoutes: AppRouteRecordRaw[] = []
    
    for (const route of routes) {
      // 检查当前路由的访问权限
      const canAccess = await this.checkRouteAccess(route)
      
      if (canAccess.access) {
        // 如果有子路由，递归检查
        if (route.children && route.children.length > 0) {
          const accessibleChildren = await this.getAccessibleMenuRoutes(route.children)
          if (accessibleChildren.length > 0) {
            accessibleRoutes.push({
              ...route,
              children: accessibleChildren
            })
          }
        } else {
          accessibleRoutes.push(route)
        }
      }
    }
    
    return accessibleRoutes
  }
  
  /**
   * 路由排序
   */
  static sortRoutes(routes: AppRouteRecordRaw[]): AppRouteRecordRaw[] {
    return routes.sort((a, b) => {
      const sortA = a.meta?.menuSort || 0
      const sortB = b.meta?.menuSort || 0
      return sortA - sortB
    })
  }
}