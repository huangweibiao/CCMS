<template>
  <div class="permission-container">
    <!-- 默认插槽 - 内容区域 -->
    <slot v-if="checkAccess" />
    
    <!-- 无权限提示插槽 -->
    <slot v-else name="unauthorized">
      <div class="unauthorized-message">
        <el-icon><Lock /></el-icon>
        <span>{{ fallbackMessage || '您没有权限访问此内容' }}</span>
        
        <el-button 
          v-if="showReturnButton" 
          type="primary" 
          size="small" 
          link
          @click="handleReturn"
        >
          返回
        </el-button>
      </div>
    </slot>
    
    <!-- 加载状态插槽 -->
    <slot v-if="loading" name="loading">
      <div class="loading-container">
        <el-icon class="loading-icon"><Loading /></el-icon>
        <span>权限验证中...</span>
      </div>
    </slot>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { usePermissionStore } from '@/stores/permission'
import { useRouter } from 'vue-router'
import { Lock, Loading } from '@element-plus/icons-vue'

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
  /** 是否显示返回按钮 */
  showReturnButton?: boolean
  /** 是否自动加载权限 */
  autoLoadPermissions?: boolean
  /** 是否为管理员跳过权限验证 */
  skipIfAdmin?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'any',
  showReturnButton: true,
  autoLoadPermissions: false,
  skipIfAdmin: true
})

const permissionStore = usePermissionStore()
const router = useRouter()

const loading = ref(false)
const hasCheckedPermissions = ref(false)

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
 * 自动加载权限
 */
async function loadPermissions() {
  if (!props.autoLoadPermissions || hasCheckedPermissions.value) {
    return
  }

  loading.value = true
  try {
    await permissionStore.loadPermissions()
    hasCheckedPermissions.value = true
  } catch (error) {
    console.error('加载权限失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 返回操作
 */
function handleReturn() {
  router.go(-1)
}

// 监视权限状态变化
watch(
  () => permissionStore.permissionCodes,
  () => {
    // 权限状态变化时重新检查
  }
)

// 初始化时的处理
onMounted(async () => {
  await loadPermissions()
})
</script>

<style scoped>
.permission-container {
  width: 100%;
  height: 100%;
}

.unauthorized-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  
  .el-icon {
    font-size: 48px;
    margin-bottom: 16px;
    color: var(--el-color-warning);
  }
  
  .el-button {
    margin-top: 12px;
  }
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  
  .loading-icon {
    font-size: 24px;
    margin-right: 8px;
    animation: rotating 2s linear infinite;
  }
  
  @keyframes rotating {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }
}
</style>