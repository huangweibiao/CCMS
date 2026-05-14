import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'

// 权限系统相关导入
import { installPermissionDirectives } from '@/directives/permission'

// 全局样式
import './assets/css/index.css'

/**
 * Vue应用启动函数
 */
const startApp = () => {
  const app = createApp(App)

  try {
    // 注册Element Plus图标
    for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
      app.component(key, component)
    }

    // 使用插件
    app.use(createPinia())
    app.use(router)
    app.use(ElementPlus, {
      locale: zhCn,
    })

    // 注册权限指令（条件性注册，避免依赖错误）
    try {
      installPermissionDirectives(app)
      console.log('权限指令注册成功')
    } catch (directiveError) {
      console.warn('权限指令注册失败，但应用继续启动:', directiveError)
    }

    // 挂载应用
    app.mount('#app')
    
    console.log('CCMS应用启动成功')
  } catch (error) {
    console.error('CCMS应用启动失败:', error)
    // 显示友好的错误提示
    const appContainer = document.getElementById('app')
    if (appContainer) {
      appContainer.innerHTML = `
        <div style="padding: 20px; text-align: center; font-family: Arial, sans-serif;">
          <h2>应用启动失败</h2>
          <p>请刷新页面重试或联系技术支持</p>
          <p style="color: #666; font-size: 14px;">错误信息: ${error}</p>
        </div>
      `
    }
  }
}

// 应用启动
document.addEventListener('DOMContentLoaded', () => {
  startApp()
})