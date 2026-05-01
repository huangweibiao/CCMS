import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import type { ElMessageBoxOptions, MessageType } from 'element-plus'

// 提示消息
export const showMessage = (
  message: string,
  type: MessageType = 'info',
  duration: number = 3000
) => {
  return ElMessage({
    message,
    type,
    duration,
    showClose: true,
    grouping: true
  })
}

// 成功消息
export const showSuccess = (message: string, duration: number = 3000) => {
  return showMessage(message, 'success', duration)
}

// 警告消息
export const showWarning = (message: string, duration: number = 5000) => {
  return showMessage(message, 'warning', duration)
}

// 错误消息
export const showError = (message: string, duration: number = 5000) => {
  return showMessage(message, 'error', duration)
}

// 信息消息
export const showInfo = (message: string, duration: number = 3000) => {
  return showMessage(message, 'info', duration)
}

// 通知类型定义
type NotificationPosition = 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left'

// 通知提示
export const showNotification = (
  title: string,
  message: string,
  type: MessageType = 'info',
  duration: number = 4500,
  position: NotificationPosition = 'top-right',
  offset: number = 0
) => {
  return ElNotification({
    title,
    message,
    type,
    duration,
    position,
    offset,
    showClose: true
  })
}

// 确认框选项
interface ConfirmOptions {
  title?: string
  message: string
  type?: MessageType
  confirmButtonText?: string
  cancelButtonText?: string
  showCancelButton?: boolean
  showClose?: boolean
  beforeClose?: (action: string, instance: any, done: Function) => void
}

// 确认对话框
export const showConfirm = (options: ConfirmOptions): Promise<boolean> => {
  return new Promise((resolve) => {
    const {
      title = '提示',
      message,
      type = 'warning',
      confirmButtonText = '确定',
      cancelButtonText = '取消',
      showCancelButton = true,
      showClose = false,
      beforeClose
    } = options

    const messageBoxOptions: ElMessageBoxOptions = {
      title,
      message,
      type,
      confirmButtonText,
      cancelButtonText,
      showCancelButton,
      showClose,
      beforeClose
    }

    ElMessageBox.confirm(message, title, messageBoxOptions)
      .then(() => {
        resolve(true)
      })
      .catch(() => {
        resolve(false)
      })
  })
}

// 输入对话框
export const showPrompt = (
  message: string,
  title: string = '输入',
  inputType: 'text' | 'textarea' = 'text',
  inputValue: string = '',
  inputPlaceholder: string = '请输入内容'
): Promise<string | null> => {
  return new Promise((resolve) => {
    ElMessageBox.prompt(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType,
      inputValue,
      inputPlaceholder,
      showClose: false
    })
      .then(({ value }) => {
        resolve(value)
      })
      .catch(() => {
        resolve(null)
      })
  })
}

// 消息加载
export const showLoading = (message: string = '加载中...'): () => void => {
  const loading = ElMessage({
    message,
    type: 'info',
    duration: 0,
    showClose: false
  })

  return () => {
    loading.close()
  }
}

// 批量消息处理（避免消息重叠）
class MessageQueue {
  private queue: Array<() => void> = []
  private processing: boolean = false

  addMessage(messageFn: () => void): void {
    this.queue.push(messageFn)
    this.processQueue()
  }

  private processQueue(): void {
    if (this.processing || this.queue.length === 0) {
      return
    }

    this.processing = true
    const messageFn = this.queue.shift()!
    
    messageFn()
    
    // 延迟处理下一条消息，避免消息重叠
    setTimeout(() => {
      this.processing = false
      this.processQueue()
    }, 300)
  }
}

// 创建消息队列实例
const messageQueue = new MessageQueue()

// 队列化消息（避免连续消息重叠）
export const queuedMessage = (
  message: string,
  type: MessageType = 'info',
  duration: number = 3000
): void => {
  messageQueue.addMessage(() => {
    showMessage(message, type, duration)
  })
}

// 全局消息配置
export const configureGlobalMessages = (config: {
  maxCount?: number
  zIndex?: number
}): void => {
  if (config.maxCount !== undefined) {
    // @ts-ignore
    ElMessage.config({
      maxCount: config.maxCount
    })
  }
  
  if (config.zIndex !== undefined) {
    // @ts-ignore
    ElMessage.config({
      zIndex: config.zIndex
    })
  }
}

// 默认导出一个对象，包含主要的消息方法
export default {
  showSuccess,
  showError,
  showWarning,
  showInfo,
  showConfirm
}