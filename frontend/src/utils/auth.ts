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
   */
  static isTokenValid(): boolean {
    const userStore = useUserStore()
    if (!userStore.token) return false
    
    // 简单检查token格式（实际应该解析JWT检查过期时间）
    const tokenParts = userStore.token.split('.')
    return tokenParts.length === 3
  }
}