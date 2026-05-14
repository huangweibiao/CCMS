import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createApp } from 'vue'
import { vPermission, vRole } from '@/directives/permission'
import { usePermissionStore } from '@/stores/permission'

// Mock 组件
const TestComponent = {
  template: `
    <div>
      <button v-permission="'user:create'">创建用户</button>
      <button v-role="'admin'">管理员操作</button>
      <div v-permission="['user:read', 'user:write']">高级操作</div>
    </div>
  `,
  directives: {
    permission: vPermission,
    role: vRole
  }
}

describe('权限指令', () => {
  let permissionStore: ReturnType<typeof usePermissionStore>
  
  beforeEach(() => {
    setActivePinia(createPinia())
    permissionStore = usePermissionStore()
    vi.clearAllMocks()
  })

  describe('v-permission指令', () => {
    it('应该在有权限时显示元素', () => {
      permissionStore.permissionCodes = ['user:create', 'user:read']
      
      const wrapper = mount(TestComponent)
      const button = wrapper.find('button')
      
      expect(button.isVisible()).toBe(true)
      expect(button.element.style.display).toBe('')
    })

    it('应该在无权限时隐藏元素', () => {
      permissionStore.permissionCodes = ['user:read']
      
      const wrapper = mount(TestComponent)
      const button = wrapper.find('button')
      
      expect(button.element.style.display).toBe('none')
    })

    it('应该支持字符串数组权限验证', () => {
      permissionStore.permissionCodes = ['user:read', 'user:write']
      
      const wrapper = mount(TestComponent)
      const permissionDiv = wrapper.find('div')
      
      expect(permissionDiv.isVisible()).toBe(true)
      expect(permissionDiv.element.style.display).toBe('')
    })

    it('应该支持任意权限模式', () => {
      const app = createApp({
        template: `<div v-permission="{ value: ['user:read', 'user:delete'], mode: 'any' }">测试</div>`,
        directives: { permission: vPermission }
      })
      
      permissionStore.permissionCodes = ['user:read']
      const container = document.createElement('div')
      document.body.appendChild(container)
      
      app.mount(container)
      const element = container.querySelector('div')
      
      expect(element?.style.display).toBe('')
      app.unmount()
      document.body.removeChild(container)
    })

    it('应该支持全部权限模式', () => {
      const app = createApp({
        template: `<div v-permission="{ value: ['user:read', 'user:delete'], mode: 'all' }">测试</div>`,
        directives: { permission: vPermission }
      })
      
      permissionStore.permissionCodes = ['user:read']
      const container = document.createElement('div')
      document.body.appendChild(container)
      
      app.mount(container)
      const element = container.querySelector('div')
      
      expect(element?.style.display).toBe('none')
      app.unmount()
      document.body.removeChild(container)
    })
  })

  describe('v-role指令', () => {
    it('应该在有角色时显示元素', () => {
      permissionStore.roles = ['admin', 'editor']
      
      const wrapper = mount(TestComponent)
      const roleButton = wrapper.findAll('button')[1]
      
      expect(roleButton.isVisible()).toBe(true)
      expect(roleButton.element.style.display).toBe('')
    })

    it('应该在无角色时隐藏元素', () => {
      permissionStore.roles = ['editor']
      
      const wrapper = mount(TestComponent)
      const roleButton = wrapper.findAll('button')[1]
      
      expect(roleButton.element.style.display).toBe('none')
    })

    it('应该支持管理员自动绕过权限验证', () => {
      permissionStore.roles = ['super_admin']
      
      const wrapper = mount(TestComponent)
      const button = wrapper.find('button')
      
      expect(button.isVisible()).toBe(true)
      expect(button.element.style.display).toBe('')
    })
  })

  describe('指令响应权限变化', () => {
    it('应该响应权限状态变化', async () => {
      permissionStore.permissionCodes = ['user:read']
      
      const wrapper = mount(TestComponent)
      const button = wrapper.find('button')
      
      // 初始状态下按钮应该被隐藏
      expect(button.element.style.display).toBe('none')
      
      // 更新权限状态
      permissionStore.permissionCodes = ['user:create', 'user:read']
      await wrapper.vm.$nextTick()
      
      // 按钮应该显示
      expect(button.element.style.display).toBe('')
    })

    it('应该响应角色状态变化', async () => {
      permissionStore.roles = ['editor']
      
      const wrapper = mount(TestComponent)
      const roleButton = wrapper.findAll('button')[1]
      
      // 初始状态下按钮应该被隐藏
      expect(roleButton.element.style.display).toBe('none')
      
      // 更新角色状态
      permissionStore.roles = ['admin', 'editor']
      await wrapper.vm.$nextTick()
      
      // 按钮应该显示
      expect(roleButton.element.style.display).toBe('')
    })
  })
})