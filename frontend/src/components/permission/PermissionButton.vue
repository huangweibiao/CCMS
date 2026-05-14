<template>
  <el-button
    v-bind="buttonProps"
    :disabled="checkAccess ? undefined : true"
    :title="tooltipMessage"
    @click="handleClick"
  >
    <slot />
  </el-button>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import { usePermissionStore } from '@/stores/permission'
import { ElMessage } from 'element-plus'
import type { ButtonProps } from 'element-plus'

interface Props {
  /** 需要的权限标识 */
  permission?: string | string[]
  /** 需要的角色标识 */
  role?: string | string[]
  /** 需要的菜单权限 */
  menu?: string
  /** 验证模式: any(任一权限) 或 all(所有权限) */
  mode?: 'any' | 'all'
  /** 无权限时的提示信息 */
  fallbackMessage?: string
  /** 是否显示无权限提示 */
  showMessage?: boolean
  /** 是否为管理员跳过权限验证 */
  skipIfAdmin?: boolean
  /** 无权限时的按钮行为：disable(禁用)或hide(隐藏) */
  behavior?: 'disable' | 'hide'
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'any',
  showMessage: true,
  skipIfAdmin: true,
  behavior: 'disable'
})

const emit = defineEmits<{
  click: [event: MouseEvent]
  noPermission: []
}>()

const permissionStore = usePermissionStore()
const attrs = useAttrs()

/**
 * 合并按钮属性
 */
const buttonProps = computed(() => {
  const baseProps: ButtonProps = {
    type: 'primary',
    size: 'default',
    ...attrs
  }

  // 如果没有权限且行为是隐藏，则不渲染按钮
  if (!checkAccess.value && props.behavior === 'hide') {
    return null
  }

  return baseProps
})

/**
 * 检查访问权限
 */
const checkAccess = computed(() => {
  // 如果没有权限要求，则允许访问
  if (!props.permission && !props.role && !props.menu) {
    return true
  }

  // 如果是管理员且有跳过选项，则允许访问
  if (props.skipIfAdmin && permissionStore.isSuperAdmin()) {
    return true
  }

  // 检查权限
  if (props.permission) {
    const hasPerm = permissionStore.hasPermission(props.permission, props.mode)
    if (!hasPerm) return false
  }

  // 检查角色
  if (props.role) {
    const hasRole = permissionStore.hasRole(props.role)
    if (!hasRole) return false
  }

  // 检查菜单权限
  if (props.menu) {
    const hasMenu = permissionStore.hasMenuPermission(props.menu)
    if (!hasMenu) return false
  }

  return true
})

/**
 * 权限提示信息
 */
const tooltipMessage = computed(() => {
  if (checkAccess.value) {
    return ''
  }
  
  if (props.fallbackMessage) {
    return props.fallbackMessage
  }
  
  return '您没有操作权限'
})

/**
 * 点击事件处理
 */
function handleClick(event: MouseEvent) {
  if (!checkAccess.value) {
    if (props.showMessage) {
      ElMessage.warning(tooltipMessage.value)
    }
    emit('noPermission')
    return
  }

  emit('click', event)
}
</script>

<style scoped>
.permission-button {
  position: relative;
}

/* 无权限时的禁用样式 */
.permission-button--disabled::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.5);
  cursor: not-allowed;
  z-index: 1;
}
</style>