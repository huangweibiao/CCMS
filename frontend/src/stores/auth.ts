import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import type { UserInfo } from '@/types/user'

interface AuthState {
  token: string
  user: UserInfo | null
  menus: any[]
  permissions: string[]
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('ccms_token') || '',
    user: JSON.parse(localStorage.getItem('ccms_user') || 'null'),
    menus: JSON.parse(localStorage.getItem('ccms_menus') || '[]'),
    permissions: JSON.parse(localStorage.getItem('ccms_permissions') || '[]')
  }),
  
  actions: {
    async login(username: string, password: string) {
      const response = await authApi.login({ username, password })
      this.token = response.data.token
      this.user = response.data.userInfo || response.data.user
      this.menus = response.data.menus || []
      this.permissions = response.data.permissions || []
      this.persist()
    },
    
    async refreshMe() {
      const response = await authApi.getUserInfo()
      this.user = response.data
      this.menus = response.data.menus || []
      this.permissions = response.data.permissions || []
      this.persist()
    },
    
    hasPermission(code: string) {
      return this.permissions.includes(code)
    },
    
    clear() {
      this.token = ''
      this.user = null
      this.menus = []
      this.permissions = []
      localStorage.removeItem('ccms_token')
      localStorage.removeItem('ccms_user')
      localStorage.removeItem('ccms_menus')
      localStorage.removeItem('ccms_permissions')
    },
    
    persist() {
      localStorage.setItem('ccms_token', this.token)
      localStorage.setItem('ccms_user', JSON.stringify(this.user))
      localStorage.setItem('ccms_menus', JSON.stringify(this.menus))
      localStorage.setItem('ccms_permissions', JSON.stringify(this.permissions))
    }
  }
})