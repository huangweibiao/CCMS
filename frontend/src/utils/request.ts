import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/user'

/**
 * HTTP请求工具类
 * 封装axios，提供统一的请求拦截、响应处理和错误处理
 */
class HttpClient {
  private instance: AxiosInstance

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
          ElMessage.error(data.message || '请求失败')
          return Promise.reject(new Error(data.message || '请求失败'))
        }
        
        return response
      },
      (error) => {
        // 处理HTTP错误
        if (error.response) {
          const { status, data } = error.response
          
          switch (status) {
            case 401:
              ElMessage.error('登录已过期，请重新登录')
              const userStore = useUserStore()
              userStore.logout()
              window.location.href = '/login'
              break
            case 403:
              ElMessage.error('没有权限访问该资源')
              break
            case 404:
              ElMessage.error('请求的资源不存在')
              break
            case 500:
              ElMessage.error('服务器内部错误')
              break
            default:
              ElMessage.error(data?.message || `请求失败 (${status})`)
          }
        } else if (error.request) {
          ElMessage.error('网络连接错误，请检查网络设置')
        } else {
          ElMessage.error('请求配置错误')
        }
        
        return Promise.reject(error)
      }
    )
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