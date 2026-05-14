import type { Directive, DirectiveBinding } from 'vue'
import { usePermissionStore } from '@/stores/permission'

/**
 * 权限指令配置选项
 */
interface PermissionDirectiveOptions {
  /** 权限标识 */
  value: string | string[]
  /** 验证模式：any(任一权限)或all(所有权限) */
  mode?: 'any' | 'all'
  /** 是否启用权限验证 */
  enabled?: boolean
}

/**
 * 权限指令处理函数
 */
function handlePermission(el: HTMLElement, binding: DirectiveBinding<PermissionDirectiveOptions | string | string[]>) {
  const permissionStore = usePermissionStore()
  
  // 解析指令参数
  let options: PermissionDirectiveOptions
  
  if (typeof binding.value === 'string' || Array.isArray(binding.value)) {
    options = {
      value: binding.value,
      mode: 'any',
      enabled: true
    }
  } else {
    options = {
      value: binding.value.value,
      mode: binding.value.mode || 'any',
      enabled: binding.value.enabled !== false
    }
  }
  
  // 如果权限验证禁用，则不处理
  if (!options.enabled) {
    return
  }
  
  // 检查权限
  const hasPermission = permissionStore.hasPermission(options.value, options.mode)
  
  if (!hasPermission) {
    // 隐藏元素
    if (el.parentNode) {
      el.style.display = 'none'
    } else {
      // 如果元素还没有挂载到DOM，设置标记后在mounted时处理
      el.dataset.permissionHide = 'true'
    }
  } else {
    // 显示元素
    el.style.display = ''
    delete el.dataset.permissionHide
  }
}

/**
 * 权限指令定义
 */
export const vPermission: Directive<HTMLElement, PermissionDirectiveOptions | string | string[]> = {
  mounted(el, binding) {
    handlePermission(el, binding)
  },
  
  updated(el, binding) {
    handlePermission(el, binding)
  }
}

/**
 * 角色权限指令
 */
export const vRole: Directive<HTMLElement, string | string[]> = {
  mounted(el, binding) {
    const permissionStore = usePermissionStore()
    const hasRole = permissionStore.hasRole(binding.value)
    
    if (!hasRole) {
      if (el.parentNode) {
        el.style.display = 'none'
      } else {
        el.dataset.roleHide = 'true'
      }
    } else {
      el.style.display = ''
      delete el.dataset.roleHide
    }
  },
  
  updated(el, binding) {
    const permissionStore = usePermissionStore()
    const hasRole = permissionStore.hasRole(binding.value)
    
    if (!hasRole) {
      el.style.display = 'none'
      delete el.dataset.roleHide
    } else {
      el.style.display = ''
      el.dataset.roleHide = 'true'
    }
  }
}

/**
 * 菜单权限指令 - 检查是否有菜单访问权限
 */
export const vMenu: Directive<HTMLElement, string> = {
  mounted(el, binding) {
    const permissionStore = usePermissionStore()
    const hasMenuAccess = permissionStore.hasMenuPermission(binding.value)
    
    if (!hasMenuAccess) {
      if (el.parentNode) {
        el.style.display = 'none'
      } else {
        el.dataset.menuHide = 'true'
      }
    } else {
      el.style.display = ''
      delete el.dataset.menuHide
    }
  },
  
  updated(el, binding) {
    const permissionStore = usePermissionStore()
    const hasMenuAccess = permissionStore.hasMenuPermission(binding.value)
    
    if (!hasMenuAccess) {
      el.style.display = 'none'
      delete el.dataset.menuHide
    } else {
      el.style.display = ''
      el.dataset.menuHide = 'true'
    }
  }
}

/**
 * 安装指令到Vue应用
 */
export function installPermissionDirectives(app: any) {
  app.directive('permission', vPermission)
  app.directive('role', vRole)
  app.directive('menu', vMenu)
}