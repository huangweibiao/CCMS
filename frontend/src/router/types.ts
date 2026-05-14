import type { RouteRecordRaw } from 'vue-router'
import type { MenuItem, MenuTree } from '@/types/permission'

/**
 * 路由元数据扩展
 */
export interface AppRouteMeta {
  /** 路由标题 */
  title: string
  /** 路由图标 */
  icon?: string
  /** 是否需要认证 */
  requiresAuth?: boolean
  /** 需要的权限代码 */
  permissionCode?: string
  /** 是否需要管理员权限 */
  requiresAdmin?: boolean
  /** 是否在菜单中隐藏 */
  hidden?: boolean
  /** 缓存路由 */
  keepAlive?: boolean
  /** 面包屑导航中显示 */
  breadcrumb?: boolean
  /** 菜单排序 */
  menuSort?: number
}

/**
 * 应用路由定义
 */
export interface AppRouteRecordRaw extends RouteRecordRaw {
  meta?: AppRouteMeta
  children?: AppRouteRecordRaw[]
}

/**
 * 路由转换配置
 */
export interface RouteTransformConfig {
  /** 组件映射配置 */
  componentMapping: Record<string, any>
  /** 默认图标 */
  defaultIcon?: string
  /** 路由前缀 */
  pathPrefix?: string
}

/**
 * 动态路由配置
 */
export interface DynamicRouteConfig {
  /** 静态路由 */
  staticRoutes: AppRouteRecordRaw[]
  /** 动态路由根路径 */
  dynamicRootPath: string
  /** 404 路由路径 */
  notFoundPath: string
  /** 登录路由路径 */
  loginPath: string
}

/**
 * 路由权限验证结果
 */
export interface RouteAccessResult {
  /** 是否允许访问 */
  access: boolean
  /** 重定向路径（如果访问被拒绝） */
  redirect?: string
  /** 错误消息 */
  message?: string
}

/**
 * 菜单项到路由项的转换配置
 */
export interface MenuToRouteConfig {
  /** 组件名称映射 */
  componentMap: Record<string, string>
  /** 默认组件 */
  defaultComponent?: any
  /** 是否为嵌套路由 */
  nested?: boolean
}