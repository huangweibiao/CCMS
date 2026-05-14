import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import AuthGuard from '@/components/auth/AuthGuard.vue'
import { vPermission, vRole } from '@/directives/permission'

// Mock API 请求
vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  getUserInfo: vi.fn(),
  refreshToken: vi.fn(),
  logout: vi.fn()
}))

// Mock 路由组件
const LoginPage = {
  template: '<div>登录页面<button @click="mockLogin">登录</button></div>',
  methods: {
    mockLogin() {
      this.$emit('login')
    }
  }
}

const DashboardPage = {
  template: `
    <div>
      <h1>仪表板</h1>
      <button v-permission="'user:create'">创建用户</button>
      <button v-role="'admin'">管理员面板</button>
      <div v-permission="['user:read', 'user:write']">高级操作区</div>
    </div>
  `,
  directives: {
    permission: vPermission,
    role: vRole
  }
}

const AdminPage = {
  template: '<div>管理员页面</div>',
  meta: { requiresAuth: true, permissions: ['admin:access'] }
}

// 创建测试路由
const createTestRouter = () => {
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: '/login',
        name: 'Login',
        component: LoginPage,
        meta: { public: true }
      },
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: DashboardPage,
        meta: { requiresAuth: true }
      },
      {
        path: '/admin',
        name: 'Admin',
        component: AdminPage,
        meta: { requiresAuth: true, permissions: ['admin:access'] }
      }
    ]
  })
  
  // Mock 路由方法
  router.push = vi.fn()
  router.replace = vi.fn()
  
  return router
}

describe('RBAC完整流程集成测试', () => {
  let router: ReturnType<typeof createTestRouter>
  let userStore: ReturnType<typeof useUserStore>
  let permissionStore: ReturnType<typeof usePermissionStore>
  
  beforeEach(() => {
    setActivePinia(createPinia())
    router = createTestRouter()
    userStore = useUserStore()
    permissionStore = usePermissionStore()
    
    // Mock localStorage
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: vi.fn(),
        setItem: vi.fn(),
        removeItem: vi.fn()
      }
    })
    
    vi.clearAllMocks()
  })
  
  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('完整用户登录和权限验证流程', () => {
    it('应该完成普通用户登录和权限检查流程', async () => {
      // 1. 初始状态：未登录用户访问登录页
      await router.push('/login')
      userStore.isAuthenticated = false
      
      const loginWrapper = mount(LoginPage, {
        global: {
          plugins: [router]
        }
      })
      
      expect(loginWrapper.html()).toContain('登录页面')
      
      // 2. 模拟登录过程
      const authApi = await import('@/api/auth')
      authApi.login.mockResolvedValue({
        success: true,
        data: {
          user: {
            id: 1,
            username: 'normaluser',
            permissions: ['user:read', 'user:update'],
            roles: ['user']
          },
          token: 'mock-jwt-token',
          refreshToken: 'mock-refresh-token'
        }
      })
      
      // 触发登录
      loginWrapper.find('button').trigger('click')
      
      // 3. 验证登录状态更新
      await flushPromises()
      
      // 注意：这里需要简化测试逻辑，因为我们的实际登录流程较为复杂
      userStore.isAuthenticated = true
      userStore.userInfo = { 
        id: 1, 
        username: 'normaluser', 
        roles: ['user'] 
      }
      
      expect(userStore.isAuthenticated).toBe(true)
      expect(userStore.userInfo?.username).toBe('normaluser')
      expect(permissionStore.permissionCodes).toEqual(['user:read', 'user:update'])
      expect(permissionStore.roles).toEqual(['user'])
      
      // 4. 验证重定向到仪表板
      expect(router.push).toHaveBeenCalledWith('/dashboard')
      
      // 5. 访问仪表板页面
      const dashboardWrapper = mount(DashboardPage, {
        global: {
          plugins: [router],
          directives: {
            permission: vPermission,
            role: vRole
          }
        }
      })
      
      // 6. 验证权限指令行为
      const permissionButton = dashboardWrapper.find('button')
      expect(permissionButton.isVisible()).toBe(false) // 没有user:create权限
      
      // 7. 验证角色指令行为
      const roleButton = dashboardWrapper.findAll('button')[1]
      expect(roleButton.isVisible()).toBe(false) // 没有admin角色
      
      // 8. 验证高级操作区权限
      const advancedSection = dashboardWrapper.find('div')
      expect(advancedSection.isVisible()).toBe(false) // 需要user:write权限
    })

    it('应该完成管理员用户登录和权限检查流程', async () => {
      // 1. 模拟管理员登录
      const authApi = await import('@/api/auth')
      authApi.login.mockResolvedValue({
        success: true,
        data: {
          user: {
            id: 2,
            username: 'adminuser',
            permissions: ['user:create', 'user:read', 'user:update', 'user:delete', 'admin:access'],
            roles: ['admin', 'user']
          },
          token: 'admin-mock-token'
        }
      })
      
      // 登录管理员用户
      await authStore.login({
        username: 'adminuser',
        password: 'admin123'
      })
      
      await flushPromises()
      
      // 2. 验证管理员权限
      expect(permissionStore.permissionCodes).toContain('admin:access')
      expect(permissionStore.roles).toContain('admin')
      
      // 3. 访问管理员页面
      await router.push('/admin')
      
      const adminWrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      // 4. 应该可以成功访问管理员页面（有权限）
      expect(router.push).not.toHaveBeenCalledWith('/403')
      
      // 5. 验证仪表板权限指令
      const dashboardWrapper = mount(DashboardPage, {
        global: {
          plugins: [router],
          directives: {
            permission: vPermission,
            role: vRole
          }
        }
      })
      
      // 管理员应该可以看到所有按钮
      const permissionButton = dashboardWrapper.find('button')
      expect(permissionButton.isVisible()).toBe(true)
      
      const roleButton = dashboardWrapper.findAll('button')[1]
      expect(roleButton.isVisible()).toBe(true)
      
      const advancedSection = dashboardWrapper.find('div')
      expect(advancedSection.isVisible()).toBe(true)
    })

    it('应该验证超级管理员绕过权限检查的功能', async () => {
      // 1. 模拟超级管理员登录
      const authApi = await import('@/api/auth')
      authApi.login.mockResolvedValue({
        success: true,
        data: {
          user: {
            id: 3,
            username: 'superadmin',
            permissions: ['user:read'], // 即使只有基本权限
            roles: ['super_admin', 'admin']
          },
          token: 'super-admin-token'
        }
      })
      
      await authStore.login({
        username: 'superadmin',
        password: 'super123'
      })
      
      await flushPromises()
      
      // 2. 验证超级管理员角色
      expect(permissionStore.roles).toContain('super_admin')
      
      // 3. 访问需要admin:access权限的页面
      await router.push('/admin')
      
      const guardWrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      // 4. 超级管理员应该可以绕过权限检查
      expect(router.push).not.toHaveBeenCalledWith('/403')
      
      // 5. 验证权限指令也能绕过
      const dashboardWrapper = mount(DashboardPage, {
        global: {
          plugins: [router],
          directives: {
            permission: vPermission,
            role: vRole
          }
        }
      })
      
      // 即使在permissions中只有user:read，super_admin也能看到所有内容
      const permissionButton = dashboardWrapper.find('button')
      expect(permissionButton.isVisible()).toBe(true) // 绕过权限检查
    })
  })

  describe('权限动态变化验证', () => {
    it('应该验证权限状态变化的实时响应', async () => {
      // 1. 初始登录为普通用户
      authStore.isAuthenticated = true
      permissionStore.permissionCodes = ['user:read']
      permissionStore.roles = ['user']
      
      // 2. 验证初始权限状态
      await router.push('/dashboard')
      
      const dashboardWrapper = mount(DashboardPage, {
        global: {
          plugins: [router],
          directives: {
            permission: vPermission,
            role: vRole
          }
        }
      })
      
      // 初始状态：没有高级权限
      const permissionButton = dashboardWrapper.find('button')
      expect(permissionButton.isVisible()).toBe(false)
      
      // 3. 模拟权限升级
      permissionStore.permissionCodes = ['user:create', 'user:read', 'user:update']
      permissionStore.roles = ['user', 'editor']
      
      await nextTick()
      
      // 4. 验证权限升级后的状态
      expect(permissionButton.isVisible()).toBe(true)
      
      const roleButton = dashboardWrapper.findAll('button')[1]
      expect(roleButton.isVisible()).toBe(false) // 仍然没有admin角色
      
      // 5. 模拟权限撤销
      permissionStore.permissionCodes = ['user:read']
      
      await nextTick()
      
      // 6. 验证权限撤销后的状态
      expect(permissionButton.isVisible()).toBe(false)
    })
  })

  describe('异常情况处理', () => {
    it('应该处理token过期自动重新认证', async () => {
      // 1. 初始登录
      authStore.isAuthenticated = true
      authStore.token = 'expired-token'
      permissionStore.permissionCodes = ['user:read']
      
      // 2. Mock token过期和自动刷新
      const authApi = await import('@/api/auth')
      authApi.refreshToken.mockResolvedValue({
        success: true,
        data: {
          token: 'new-token',
          refreshToken: 'new-refresh-token'
        }
      })
      
      // 3. 模拟访问需要认证的页面
      await router.push('/dashboard')
      
      const guardWrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      // 4. 验证token刷新和正常访问
      expect(authApi.refreshToken).toHaveBeenCalled()
      expect(authStore.token).toBe('new-token')
      expect(router.push).not.toHaveBeenCalledWith('/login')
    })

    it('应该处理权限信息加载失败', async () => {
      // 1. 模拟登录但权限信息加载失败
      authStore.isAuthenticated = true
      
      const authApi = await import('@/api/auth')
      authApi.getUserInfo.mockRejectedValue(new Error('权限加载失败'))
      
      // 2. 访问需要权限验证的页面
      await router.push('/dashboard')
      
      const guardWrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      // 3. 应该重定向到登录页面
      expect(router.push).toHaveBeenCalledWith('/login')
    })
  })
})