<template>
  <!-- 有子菜单的情况 - 使用el-sub-menu -->
  <el-sub-menu
    v-if="hasChildren"
    :index="menu.path || menu.id?.toString()"
    :disabled="!hasPermission"
  >
    <template #title>
      <!-- 菜单图标 -->
      <el-icon v-if="showIcon && menu.icon">
        <component :is="menu.icon" />
      </el-icon>
      
      <!-- 菜单标题 -->
      <span>{{ menu.menuName || menu.title }}</span>
      
      <!-- 权限标签 -->
      <el-tag 
        v-if="showPermissionTag" 
        size="small" 
        :type="permissionTagType"
        class="permission-tag"
      >
        {{ permissionTagText }}
      </el-tag>
    </template>
    
    <!-- 递归渲染子菜单 -->
    <template v-for="childMenu in visibleChildren" :key="childMenu.id">
      <permission-menu-item 
        :menu="childMenu"
        :level="level + 1"
        :show-icon="showIcon"
        :max-level="maxLevel"
        :show-permission-tag="showPermissionTag"
      />
    </template>
  </el-sub-menu>
  
  <!-- 无子菜单的情况 - 使用el-menu-item -->
  <el-menu-item
    v-else
    :index="menu.path || menu.id?.toString()"
    :disabled="!hasPermission"
    :route="{ path: menu.path }"
  >
    <!-- 菜单图标 -->
    <el-icon v-if="showIcon && menu.icon">
      <component :is="menu.icon" />
    </el-icon>
    
    <!-- 菜单标题 -->
    <span>{{ menu.menuName || menu.title }}</span>
    
    <!-- 权限标签 -->
    <el-tag 
      v-if="showPermissionTag" 
      size="small" 
      :type="permissionTagType"
      class="permission-tag"
    >
      {{ permissionTagText }}
    </el-tag>
  </el-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { usePermissionStore } from '@/stores/permission'

interface Props {
  /** 菜单数据 */
  menu: any
  /** 当前菜单层级 */
  level: number
  /** 是否显示菜单图标 */
  showIcon?: boolean
  /** 最大菜单层级 */
  maxLevel?: number
  /** 是否显示权限标签 */
  showPermissionTag?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showIcon: true,
  maxLevel: 3,
  showPermissionTag: false
})

const permissionStore = usePermissionStore()

/**
 * 检查是否有访问权限
 */
const hasPermission = computed(() => {
  // 如果没有权限要求，则允许访问
  if (!props.menu.permissionCode) {
    return true
  }
  
  // 检查菜单权限
  return permissionStore.hasMenuPermission(props.menu.permissionCode)
})

/**
 * 是否有子菜单
 */
const hasChildren = computed(() => {
  return props.menu.children && 
         Array.isArray(props.menu.children) && 
         props.menu.children.length > 0 &&
         props.level < (props.maxLevel || 3)
})

/**
 * 可见的子菜单（有权限的子菜单）
 */
const visibleChildren = computed(() => {
  if (!props.menu.children || !Array.isArray(props.menu.children)) {
    return []
  }
  
  return props.menu.children.filter((child: any) => {
    // 检查子菜单是否有权限
    if (!child.permissionCode) {
      return true
    }
    
    return permissionStore.hasMenuPermission(child.permissionCode)
  })
})

/**
 * 权限标签类型
 */
const permissionTagType = computed(() => {
  if (!hasPermission.value) {
    return 'danger'
  }
  
  if (props.menu.permissionType === 'admin') {
    return 'warning'
  }
  
  if (props.menu.permissionType === 'special') {
    return 'success'
  }
  
  return 'info'
})

/**
 * 权限标签文本
 */
const permissionTagText = computed(() => {
  if (!props.menu.permissionCode) {
    return '公开'
  }
  
  if (!hasPermission.value) {
    return '无权限'
  }
  
  return props.menu.permissionType || '权限'
})
</script>

<style scoped>
.permission-tag {
  margin-left: 8px;
  font-size: 10px;
}

/* 禁用菜单项的样式 */
.el-menu-item.is-disabled,
.el-sub-menu.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>