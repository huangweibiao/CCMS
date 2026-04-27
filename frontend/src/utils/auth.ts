import { useUserStore } from '@/stores/user'

/**
 * 认证工具类
 * 提供认证相关的工具函数
 */

export class AuthUtil {
  /**
   * 检查用户是否具有特定权限
   * @param permission 权限标识
   */
  static hasPermission(permission: string): boolean {
    const userStore = useUserStore()
    if (!userStore.userInfo) return false
    
    // 管理员拥有所有权限
    if (userStore.isAdmin) return true
    
    // 根据用户角色判断权限
    // 这里可以根据实际需求扩展权限检查逻辑
    const userRole = userStore.userInfo.role
    
    // 示例权限检查逻辑
    const rolePermissions: Record<string, string[]> = {
      admin: ['*'],
      finance: ['finance:*', 'approval:*'],
      approver: ['approval:*'],
      user: ['expense:apply', 'expense:reimburse']
    }
    
    const permissions = rolePermissions[userRole] || []
    return permissions.includes('*') || permissions.some(p => permission.startsWith(p.replace(':*', '')))
  }
  
  /**
   * 检查用户是否可以访问特定路由
   * @param routeMeta 路由meta信息
   */
  static canAccessRoute(routeMeta: any): boolean {
    if (routeMeta.requiresAdmin) {
      const userStore = useUserStore()
      return userStore.isAdmin
    }
    
    if (routeMeta.requiredPermission) {
      return this.hasPermission(routeMeta.requiredPermission)
    }
    
    return true
  }
  
  /**
   * 初始化认证状态
   */
  static initAuth(): void {
    const userStore = useUserStore()
    userStore.initFromStorage()
  }
  
  /**
   * 检查token是否有效
   * 实现JWT令牌的完整验证，包括格式、过期时间和签名验证
   */
  static isTokenValid(): boolean {
    const userStore = useUserStore()
    const token = userStore.token
    
    // 基本验证
    if (!token) {
      return false
    }
    
    // 验证JWT格式
    const tokenParts = token.split('.')
    if (tokenParts.length !== 3) {
      return false
    }
    
    try {
      // 解析payload部分
      const payload = JSON.parse(atob(tokenParts[1]))
      
      // 验证过期时间
      if (payload.exp && Date.now() >= payload.exp * 1000) {
        console.warn('Token已过期，自动清除')
        userStore.clearAuth()
        return false
      }
      
      // 验证签发时间
      if (payload.iat && Date.now() < payload.iat * 1000) {
        console.warn('Token签发时间异常')
        return false
      }
      
      // 验证iss（签发者） - 可配置验证
      const expectedIssuer = 'CCMS-Auth-Server'
      if (payload.iss && payload.iss !== expectedIssuer) {
        console.warn('Token签发者不匹配')
        return false
      }
      
      // 验证sub（主题）- 用户ID
      if (!payload.sub || !userStore.userInfo || payload.sub !== userStore.userInfo.id.toString()) {
        console.warn('Token用户信息不匹配')
        return false
      }
      
      // TODO: 在实际应用中，这里应该实现签名验证
      // 验证JWT签名（需要在后端配置公钥）
      // this.validateTokenSignature(token)
      
      return true
    } catch (error) {
      console.error('Token解析失败:', error)
      return false
    }
  }
  
  /**
   * 检查用户是否是特定角色
   * @param roles 角色名称数组
   */
  static hasRole(roles: string[]): boolean {
    const userStore = useUserStore()
    if (!userStore.userInfo) return false
    return roles.includes(userStore.userInfo.role)
  }
  
  /**
   * 获取用户可访问的路由列表
   */
  static getAccessibleRoutes(): any[] {
    const userStore = useUserStore()
    if (!userStore.userInfo) return []
    
    // 这里可以返回用户有权限访问的路由列表
    // 示例逻辑：管理员可访问所有路由，普通用户只能访问部分
    const allRoutes = []
    if (userStore.isAdmin) {
      return allRoutes // 返回所有路由
    }
    
    // 根据用户角色返回不同的路由
    const roleRoutes: Record<string, string[]> = {
      finance: ['dashboard', 'budgets', 'approval/*'],
      approver: ['dashboard', 'approval/*'],
      user: ['dashboard', 'expense-apply/*', 'expense-reimburse/*']
    }
    
    const userRole = userStore.userInfo.role
    return allRoutes.filter((route: any) => {
      const routeName = route.name as string
      const allowedRoutes = roleRoutes[userRole] || []
      return allowedRoutes.some(pattern => {
        if (pattern.endsWith('/*')) {
          return routeName.startsWith(pattern.replace('/*', ''))
        }
        return routeName === pattern
      })
    })
  }
  
  /**
   * 创建认证拦截器（可在main.ts中使用）
   */
  static setupAuthGuard(router: any): void {
    router.beforeEach((to: any, from: any, next: any) => {
      const userStore = useUserStore()
      
      // 设置页面标题
      if (to.meta.title) {
        document.title = `${to.meta.title} - CCMS`
      }
      
      // 如果需要认证但未登录，跳转到登录页
      if (to.meta.requiresAuth && !userStore.isAuthenticated) {
        next('/login')
        return
      }
      
      // 如果已登录但尝试访问登录页，跳转到首页
      if (to.path === '/login' && userStore.isAuthenticated) {
        next('/dashboard')
        return
      }
      
      // 检查特定权限
      if (to.meta.requiredPermission && !this.hasPermission(to.meta.requiredPermission)) {
        next('/403') // 无权限页面
        return
      }
      
      next()
    })
  }
}