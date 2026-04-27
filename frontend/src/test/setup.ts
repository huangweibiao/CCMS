import { config } from '@vue/test-utils'
import { beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

/**
 * Vitest测试配置文件
 * 配置Vue组件测试环境
 */

// 全局Pinia状态管理配置
beforeEach(() => {
  const pinia = createPinia()
  setActivePinia(pinia)
})

// Vue Test Utils 全局配置
config.global.config = {
  globalProperties: {
    $t: (key: string) => key, // 模拟i18n
    $route: { path: '/test' },
    $router: { push: () => {} },
  }
}