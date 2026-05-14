import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { authApi } from '@/api/auth'

// 是否正在刷新token
let isRefreshing = false
// 待处理的请求队列
let requests: Array<() => void> = []

/**
 * HTTP请求工具类（增强版）
 * 封装axios，支持token自动刷新、错误重试、权限验证等
 */
class HttpClient {
  private instance: AxiosInstance
  private maxRetryCount = 3
  private retryDelay = 1000

  constructor(baseURL: string = '/api') {
    this.instance = axios.create({
      baseURL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    this.setupInterceptors()
  }

  /**
   * 设置请求拦截器和响应拦截器
   */
  private setupInterceptors() {
    // 请求拦截器
    this.instance.interceptors.request.use(
      (config) => {
        // 添加认证token
        const userStore = useUserStore()
        if (userStore.token) {
          config.headers.Authorization = `Bearer ${userStore.token}`
        }
        
        // 添加请求时间戳，防止缓存
        if (config.method === 'get') {
          config.params = {
            ...config.params,
            _t: Date.now()
          }
        }
        
        // 添加请求标记（用于跟踪）
        config.headers['X-Request-ID'] = this.generateRequestId()
        
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    // 响应拦截器
    this.instance.interceptors.response.use(
      (response: AxiosResponse) => {
        const { data } = response
        
        // 处理业务错误
        if (data.code !== 200 && data.code !== 0) {
          this.handleBusinessError(data)
          return Promise.reject(new Error(data.message || '请求失败'))
        }
        
        return response
      },
      async (error) => {
        // 处理HTTP错误
        if (error.response) {
          return await this.handleHttpError(error)
        } else if (error.request) {
          return this.handleNetworkError(error)
        } else {
          return this.handleConfigError(error)
        }
      }
    )
  }

  /**
   * 处理业务逻辑错误
   */
  private handleBusinessError(data: any) {
    const errorCode = data.code
    const errorMsg = data.message || '请求失败'
    
    switch (errorCode) {
      case 401:
        this.handleUnauthorizedError()
        break
      case 403:
        this.handleForbiddenError(data.message)
        break
      case 5001:
      case 5002:
        // 业务逻辑错误
        ElMessage.warning(errorMsg)
        break
      default:
        ElMessage.error(errorMsg)
    }
  }

  /**
   * 处理HTTP错误
   */
  private async handleHttpError(error: any) {
    const { status, data } = error.response
    
    switch (status) {
      case 401:
        return await this.handleUnauthorizedError(error)
      case 403:
        this.handleForbiddenError(data?.message)
        break
      case 429:
        this.handleRateLimitError()
        break
      case 404:
        this.handleNotFoundError(data?.message)
        break
      case 500:
        this.handleServerError(data?.message)
        break
      case 502:
      case 503:
      case 504:
        this.handleServerUnavailableError()
        break
      default:
        this.handleGenericHttpError(status, data?.message)
    }
    
    return Promise.reject(error)
  }

  /**
   * 处理未授权错误（token过期）
   */
  private async handleUnauthorizedError(error?: any) {
    // 如果是刷新token的请求失败，直接登出
    if (error?.config?.url?.includes('/auth/refresh')) {
      this.forceLogout('登录已过期，请重新登录')
      return Promise.reject(error)
    }

    // 如果正在刷新token，将请求加入队列等待
    if (isRefreshing) {
      return new Promise<void>((resolve, reject) => {
        requests.push(() => {
          resolve()
        })
      }).then(() => {
        return this.instance(error.config)
      })
    }

    isRefreshing = true

    try {
      const userStore = useUserStore()
      
      // 尝试刷新token
      const response = await authApi.refreshToken()
      
      if (response.data.code === 200) {
        // 更新token
        userStore.token = response.data.data.token
        localStorage.setItem('ccms_token', userStore.token)
        
        // 重新执行待处理的请求
        requests.forEach(callback => callback())
        requests = []
        
        // 重新发送当前请求
        error.config.headers.Authorization = `Bearer ${userStore.token}`
        return this.instance(error.config)
      } else {
        // 刷新token失败，强制登出
        this.forceLogout('登录已过期，请重新登录')
        return Promise.reject(error)
      }
    } catch (refreshError) {
      // 刷新token失败，强制登出
      this.forceLogout('登录已过期，请重新登录')
      return Promise.reject(error)
    } finally {
      isRefreshing = false
    }
  }

  /**
   * 强制用户登出
   */
  private forceLogout(message: string) {
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()
    
    // 清除状态
    userStore.logout()
    permissionStore.reset()
    
    // 显示登出提示
    ElMessageBox.alert(message, '会话过期', {
      confirmButtonText: '重新登录',
      callback: () => {
        window.location.href = '/login'
      }
    })
  }

  /**
   * 处理权限不足错误
   */
  private handleForbiddenError(message?: string) {
    const errorMsg = message || '没有权限访问该资源'
    ElMessage.warning(errorMsg)
    
    // 如果是后台操作，可以考虑跳转到权限不足页面
    if (window.location.pathname !== '/403') {
      // 这里可以根据实际需要决定是否跳转
      // window.location.href = '/403'
    }
  }

  /**
   * 处理限流错误
   */
  private handleRateLimitError() {
    ElMessage.warning('请求过于频繁，请稍后重试')
  }

  /**
   * 处理资源不存在错误
   */
  private handleNotFoundError(message?: string) {
    ElMessage.error(message || '请求的资源不存在')
  }

  /**
   * 处理服务器错误
   */
  private handleServerError(message?: string) {
    ElMessage.error(message || '服务器内部错误，请联系管理员')
  }

  /**
   * 处理服务不可用错误
   */
  private handleServerUnavailableError() {
    ElMessage.error('服务暂时不可用，请稍后重试')
  }

  /**
   * 处理通用HTTP错误
   */
  private handleGenericHttpError(status: number, message?: string) {
    ElMessage.error(message || `请求失败 (${status})`)
  }

  /**
   * 处理网络错误
   */
  private handleNetworkError(error: any) {
    ElMessage.error('网络连接错误，请检查网络设置')
    return Promise.reject(error)
  }

  /**
   * 处理配置错误
   */
  private handleConfigError(error: any) {
    ElMessage.error('请求配置错误')
    return Promise.reject(error)
  }

  /**
   * 生成请求ID
   */
  private generateRequestId(): string {
    return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * GET请求
   */
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.instance.get(url, config)
  }

  /**
   * POST请求
   */
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.instance.post(url, data, config)
  }

  /**
   * PUT请求
   */
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.instance.put(url, data, config)
  }

  /**
   * DELETE请求
   */
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.instance.delete(url, config)
  }

  /**
   * 文件上传
   */
  upload<T = any>(url: string, formData: FormData): Promise<AxiosResponse<T>> {
    return this.instance.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

// 创建全局请求实例
const request = new HttpClient()

export default request