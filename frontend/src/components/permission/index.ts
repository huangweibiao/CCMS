// 权限组件模块索引文件

export { default as PermissionContainer } from './PermissionContainer.vue'
export { default as PermissionButton } from './PermissionButton.vue'
export { default as PermissionMenu } from './PermissionMenu.vue'
export { default as PermissionMenuItem } from './PermissionMenuItem.vue'

// 组件安装函数
export function installPermissionComponents(app: any) {
  // 这里可以添加组件的全局注册逻辑
  // app.component('PermissionContainer', PermissionContainer)
  // app.component('PermissionButton', PermissionButton)
  // app.component('PermissionMenu', PermissionMenu)
  // app.component('PermissionMenuItem', PermissionMenuItem)
}

// 默认导出所有组件
export default {
  install: installPermissionComponents
}