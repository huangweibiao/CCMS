import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePermissionStore } from '@/stores/permission'
import { authApi } from '@/api/auth'

// Mock API
vi.mock('@/api/auth', () => ({
  authApi: {
    getUserPermissionCodes: vi.fn(),
    getUserMenuTree: vi.fn()
  }
}))

describe('Permission Store', () => {
  let permissionStore: ReturnType<typeof usePermissionStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    permissionStore = usePermissionStore()
    vi.clearAllMocks()
  })

  describe('权限验证功能', () => {
    it('应该支持单个权限验证', () => {
      permissionStore.permissionCodes = ['user:create', 'user:read', 'user:update']
      
      expect(permissionStore.hasPermission('user:create')).toBe(true)
      expect(permissionStore.hasPermission('user:delete')).toBe(false)
    })

    it('应该支持多个权限的any模式验证', () => {
      permissionStore.permissionCodes = ['user:create', 'user:read']
      
      expect(permissionStore.hasPermission(['user:create', 'user:delete'], 'any')).toBe(true)
      expect(permissionStore.hasPermission(['user:delete', 'user:export'], 'any')).toBe(false)
    })

    it('应该支持多个权限的all模式验证', () => {
      permissionStore.permissionCodes = ['user:create', 'user:read', 'user:update']
      
      expect(permissionStore.hasPermission(['user:create', 'user:read'], 'all')).toBe(true)
      expect(permissionStore.hasPermission(['user:create', 'user:delete'], 'all')).toBe(false)
    })
  })

  describe('角色验证功能', () => {
    it('应该支持单个角色验证', () => {
      permissionStore.roles = ['admin', 'editor']
      
      expect(permissionStore.hasRole('admin')).toBe(true)
      expect(permissionStore.hasRole('viewer')).toBe(false)
    })

    it('应该支持多个角色验证', () => {
      permissionStore.roles = ['admin', 'editor']
      
      expect(permissionStore.hasRole(['admin', 'viewer'])).toBe(true)
      expect(permissionStore.hasRole(['viewer', 'guest'])).toBe(false)
    })
  })

  describe('超级管理员验证', () => {
    it('应该正确识别超级管理员', () => {
      permissionStore.roles = ['admin', 'super_admin']
      expect(permissionStore.isSuperAdmin()).toBe(true)

      permissionStore.roles = ['admin', 'editor']
      expect(permissionStore.isSuperAdmin()).toBe(false)
    })
  })

  describe('菜单权限功能', () => {
    it('应该支持菜单权限验证', () => {
      permissionStore.permissionCodes = ['menu:user', 'menu:system']
      
      expect(permissionStore.hasMenuPermission('menu:user')).toBe(true)
      expect(permissionStore.hasMenuPermission('menu:report')).toBe(false)
    })
  })

  describe('权限加载功能', () => {
    it('应该成功加载权限数据', async () => {
      // 模拟API响应
      const mockPermissions = ['user:create', 'user:read', 'menu:system']
      const mockMenuTree = [
        { id: 1, menuName: '系统管理', path: '/system', children: [] }
      ]

      vi.mocked(authApi.getUserPermissionCodes).mockResolvedValue({
        data: mockPermissions,
        code: 200,
        message: 'success'
      })

      vi.mocked(authApi.getUserMenuTree).mockResolvedValue({
        data: mockMenuTree,
        code: 200,
        message: 'success'
      })

      await permissionStore.loadPermissions()

      expect(permissionStore.permissionCodes).toEqual(mockPermissions)
      expect(permissionStore.menuTree).toEqual(mockMenuTree)
      expect(authApi.getUserPermissionCodes).toHaveBeenCalledOnce()
      expect(authApi.getUserMenuTree).toHaveBeenCalledOnce()
    })

    it('应该处理权限加载失败的情况', async () => {
      vi.mocked(authApi.getUserPermissionCodes).mockRejectedValue(new Error('Network error'))

      await expect(permissionStore.loadPermissions()).rejects.toThrow('Network error')
      
      // 权限数据应该保持为空
      expect(permissionStore.permissionCodes).toEqual([])
      expect(permissionStore.menuTree).toEqual([])
    })
  })

  describe('权限缓存功能', () => {
    it('应该支持权限数据的缓存', () => {
      const mockPermissions = ['user:create', 'user:read']
      
      permissionStore.updatePermissions(mockPermissions)
      
      expect(permissionStore.permissionCodes).toEqual(mockPermissions)
      
      // 验证权限操作
      expect(permissionStore.hasPermission('user:create')).toBe(true)
      expect(permissionStore.hasPermission('user:delete')).toBe(false)
    })
  })
})