import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginData } from '@/types/user'
import { authApi } from '@/api/auth'

/**
 * 用户状态管理
 * 管理用户认证状态、用户信息和权限
 */
export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)
  
  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')
  const userName = computed(() => userInfo.value?.userName || '')
  const deptName = computed(() => userInfo.value?.deptName || '')
  
  // Actions
  /**
   * 用户登录
   */
  const login = async (loginData: LoginData) => {
    try {
      const response = await authApi.login(loginData)
      token.value = response.data.token
      userInfo.value = response.data.userInfo
      
      // 存储到localStorage
      localStorage.setItem('ccms_token', token.value)
      localStorage.setItem('ccms_user', JSON.stringify(userInfo.value))
      
      return response
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }
  
  /**
   * 用户登出
   */
  const logout = async () => {
    try {
      await authApi.logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    } finally {
      // 清除本地存储
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('ccms_token')
      localStorage.removeItem('ccms_user')
    }
  }
  
  /**
   * 从本地存储初始化用户状态
   */
  const initFromStorage = () => {
    const storedToken = localStorage.getItem('ccms_token')
    const storedUser = localStorage.getItem('ccms_user')
    
    if (storedToken) {
      token.value = storedToken
    }
    
    if (storedUser) {
      try {
        userInfo.value = JSON.parse(storedUser)
      } catch (error) {
        console.error('解析用户信息失败:', error)
        localStorage.removeItem('ccms_user')
      }
    }
  }
  
  /**
   * 刷新用户信息
   */
  const refreshUserInfo = async () => {
    try {
      const response = await authApi.getUserInfo()
      userInfo.value = response.data
      localStorage.setItem('ccms_user', JSON.stringify(response.data))
    } catch (error) {
      console.error('刷新用户信息失败:', error)
      logout()
    }
  }
  
  return {
    // State
    token,
    userInfo,
    
    // Getters
    isAuthenticated,
    isAdmin,
    userName,
    deptName,
    
    // Actions
    login,
    logout,
    initFromStorage,
    refreshUserInfo
  }
})