<template>
  <!-- 消息通知组件 - 通过调用方法使用，无需模板渲染 -->
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import type { ElMessageBoxOptions } from 'element-plus'

// 消息类型
type MessageType = 'success' | 'warning' | 'info' | 'error'

// 通知位置
type NotificationPosition = 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left'

// 确认框选项
interface ConfirmOptions {
  title?: string
  message: string
  type?: 'success' | 'warning' | 'info' | 'error'
  confirmButtonText?: string
  cancelButtonText?: string
  showCancelButton?: boolean
  showClose?: boolean
  beforeClose?: (action: string, instance: any, done: Function) => void
}

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
  // ElMessage 配置
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

// 创建消息API对象用于组件内部使用
const messageApi = {
  // 基础消息
  showMessage,
  showSuccess,
  showWarning,
  showError,
  showInfo,
  
  // 通知
  showNotification,
  
  // 对话框
  showConfirm,
  showPrompt,
  
  // 特殊功能
  showLoading,
  
  // 队列消息
  queuedMessage,
  
  // 配置
  configureGlobalMessages
}

// 使用defineExpose将消息API暴露给组件模板
defineExpose({
  showMessage,
  showSuccess,
  showWarning,
  showError,
  showInfo,
  showNotification,
  showConfirm,
  showPrompt,
  showLoading,
  queuedMessage,
  configureGlobalMessages,
  messageApi
})

// 全局注册消息API（使其可用作全局插件）
if (typeof window !== 'undefined') {
  Object.defineProperty(window, '$message', {
    value: messageApi,
    writable: false,
    configurable: false
  })
}
</script>

<style scoped lang="css">
/* 消息通知组件样式（主要处理消息队列的视觉优化） */
:deep(.el-message) {
  z-index: 9999;
}

:deep(.el-message--group) {
  margin-bottom: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  :deep(.el-message) {
    min-width: 80vw;
    max-width: 95vw;
  }
  
  :deep(.el-notification) {
    width: 80vw;
    max-width: 95vw;
  }
}
</style>