import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'
import AuthGuard from '@/components/auth/AuthGuard.vue'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'

// Mock 路由组件
const LoginPage = { template: '<div>登录页面</div>' }
const DashboardPage = { template: '<div>仪表板</div>' }
const NotFoundPage = { template: '<div>404页面</div>' }

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
        component: DashboardPage,
        meta: { requiresAuth: true, permissions: ['admin:access'] }
      },
      {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: NotFoundPage
      }
    ]
  })
  
  // Mock push 和 replace 方法
  const originalPush = router.push
  const originalReplace = router.replace
  
  router.push = vi.fn(originalPush)
  router.replace = vi.fn(originalReplace)
  
  return router
}

describe('AuthGuard认证守卫', () => {
  let router: ReturnType<typeof createTestRouter>
  let authStore: ReturnType<typeof useAuthStore>
  let permissionStore: ReturnType<typeof usePermissionStore>
  
  beforeEach(() => {
    setActivePinia(createPinia())
    router = createTestRouter()
    authStore = useAuthStore()
    permissionStore = usePermissionStore()
    vi.clearAllMocks()
  })
  
  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('认证状态检查', () => {
    it('应该允许访问公开路由', async () => {
      authStore.isAuthenticated = false
      
      await router.push('/login')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      expect(wrapper.html()).toContain('登录页面')
    })

    it('应该重定向未认证用户到登录页', async () => {
      authStore.isAuthenticated = false
      
      await router.push('/dashboard')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      expect(router.push).toHaveBeenCalledWith('/login')
    })

    it('应该允许认证用户访问受保护路由', async () => {
      authStore.isAuthenticated = true
      authStore.userInfo = { username: 'testuser', id: 1 }
      
      await router.push('/dashboard')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      expect(wrapper.html()).toContain('仪表板')
      expect(router.push).not.toHaveBeenCalled()
    })
  })

  describe('权限验证', () => {
    it('应该验证用户权限', async () => {
      authStore.isAuthenticated = true
      permissionStore.permissionCodes = ['admin:access', 'user:read']
      
      await router.push('/admin')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      expect(wrapper.html()).toContain('仪表板')
      expect(router.push).not.toHaveBeenCalled()
    })

    it('应该拒绝无权限访问', async () => {
      authStore.isAuthenticated = true
      permissionStore.permissionCodes = ['user:read'] // 没有admin:access权限
      
      await router.push('/admin')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      expect(router.push).toHaveBeenCalledWith('/403')
    })

    it('应该允许超级管理员绕过权限检查', async () => {
      authStore.isAuthenticated = true
      permissionStore.roles = ['super_admin']
      permissionStore.permissionCodes = [] // 即使没有具体权限
      
      await router.push('/admin')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      expect(wrapper.html()).toContain('仪表板')
      expect(router.push).not.toHaveBeenCalled()
    })
  })

  describe('用户信息加载', () => {
    it('应该等待用户信息加载完成', async () => {
      authStore.isAuthenticated = true
      authStore.userInfo = { username: 'testuser', id: 1 }
      
      // Mock login action
      authStore.login = vi.fn().mockResolvedValue({
        success: true,
        data: {
          user: { id: 1, username: 'testuser' },
          token: 'mock-token-123'
        }
      })
      
      await router.push('/dashboard')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      expect(authStore.loadUserInfo).toHaveBeenCalled()
      expect(wrapper.html()).toContain('仪表板')
    })

    it('应该处理用户信息加载失败', async () => {
      authStore.isAuthenticated = true
      authStore.loadUserInfo = vi.fn().mockRejectedValue(new Error('加载失败'))
      
      await router.push('/dashboard')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      expect(router.push).toHaveBeenCalledWith('/login')
    })
  })

  describe('登录重定向', () => {
    it('应该记录原始访问路径', async () => {
      authStore.isAuthenticated = false
      
      await router.push('/dashboard?param=value')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      expect(router.push).toHaveBeenCalledWith('/login')
      expect(localStorage.getItem).toHaveBeenCalledWith('redirectUrl')
    })

    it('应该登录后重定向到原始路径', async () => {
      const redirectUrl = '/dashboard?param=value'
      localStorage.getItem = vi.fn().mockReturnValue(redirectUrl)
      
      authStore.isAuthenticated = true
      
      await router.push('/login')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      expect(router.replace).toHaveBeenCalledWith(redirectUrl)
      expect(localStorage.removeItem).toHaveBeenCalledWith('redirectUrl')
    })
  })

  describe('异常处理', () => {
    it('应该处理路由元数据缺失情况', async () => {
      // 添加一个没有meta的路由
      router.addRoute({
        path: '/public',
        name: 'Public',
        component: { template: '<div>公开页面</div>' }
      })
      
      await router.push('/public')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      // 应该默认允许访问（没有meta或meta为空的路由）
      expect(wrapper.html()).toContain('公开页面')
    })

    it('应该处理token过期情况', async () => {
      authStore.isAuthenticated = true
      // Mock token过期
      authStore.checkTokenExpiry = vi.fn().mockReturnValue(true)
      
      await router.push('/dashboard')
      
      const wrapper = mount(AuthGuard, {
        global: {
          plugins: [router]
        }
      })
      
      await flushPromises()
      
      expect(authStore.logout).toHaveBeenCalled()
      expect(router.push).toHaveBeenCalledWith('/login')
    })
  })
})