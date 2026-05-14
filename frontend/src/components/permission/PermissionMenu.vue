<template>
  <div class="permission-menu">
    <!-- 菜单头部插槽 -->
    <slot name="header" />
    
    <!-- 菜单容器 -->
    <div class="menu-container">
      <!-- 动态菜单模式 -->
      <template v-if="menuSource === 'dynamic'">
        <el-menu
          :default-active="activeMenu"
          :mode="menuMode"
          :collapse="collapse"
          :router="true"
          @select="handleMenuSelect"
        >
          <template v-for="menu in filteredMenuTree" :key="menu.id">
            <permission-menu-item 
              :menu="menu"
              :level="0"
              :show-icon="showIcon"
              :max-level="maxLevel"
            />
          </template>
        </el-menu>
      </template>
      
      <!-- 静态菜单模式 -->
      <template v-else-if="menuSource === 'static'">
        <el-menu
          :default-active="activeMenu"
          :mode="menuMode"
          :collapse="collapse"
          :router="true"
          @select="handleMenuSelect"
        >
          <slot name="menu-items" />
        </el-menu>
      </template>
    </div>
    
    <!-- 菜单底部插槽 -->
    <slot name="footer" />
    
    <!-- 空菜单提示 -->
    <div v-if="filteredMenuTree.length === 0 && menuSource === 'dynamic'" class="empty-menu">
      <el-empty description="暂无菜单权限" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { usePermissionStore } from '@/stores/permission'
import { useRoute, useRouter } from 'vue-router'
import type { MenuMode } from 'element-plus'
import PermissionMenuItem from './PermissionMenuItem.vue'

interface Props {
  /** 菜单数据来源：dynamic(动态权限菜单) 或 static(静态菜单) */
  menuSource?: 'dynamic' | 'static'
  /** 菜单显示模式 */
  menuMode?: MenuMode
  /** 是否折叠菜单 */
  collapse?: boolean
  /** 是否显示菜单图标 */
  showIcon?: boolean
  /** 最大菜单层级 */
  maxLevel?: number
  /** 是否自动加载菜单 */
  autoLoad?: boolean
  /** 是否过滤隐藏菜单 */
  filterHidden?: boolean
  /** 默认激活的菜单路径 */
  defaultActive?: string
}

const props = withDefaults(defineProps<Props>(), {
  menuSource: 'dynamic',
  menuMode: 'vertical',
  collapse: false,
  showIcon: true,
  maxLevel: 3,
  autoLoad: true,
  filterHidden: true,
  defaultActive: ''
})

const emit = defineEmits<{
  menuSelect: [menu: any]
  menuLoaded: [menus: any[]]
}>()

const permissionStore = usePermissionStore()
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const activeMenu = ref(props.defaultActive)

/**
 * 过滤后的菜单树
 */
const filteredMenuTree = computed(() => {
  if (props.menuSource !== 'dynamic') {
    return []
  }

  let menus = permissionStore.menuTree
  
  // 过滤隐藏菜单
  if (props.filterHidden) {
    menus = filterHiddenMenus(menus)
  }
  
  // 限制最大层级
  if (props.maxLevel > 0) {
    menus = limitMenuLevel(menus, props.maxLevel)
  }
  
  return menus
})

/**
 * 加载用户菜单
 */
async function loadUserMenu() {
  if (props.menuSource !== 'dynamic') {
    return
  }

  loading.value = true
  try {
    await permissionStore.loadPermissions()
    emit('menuLoaded', filteredMenuTree.value)
  } catch (error) {
    console.error('加载菜单失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 过滤隐藏菜单
 */
function filterHiddenMenus(menus: any[]): any[] {
  return menus.filter(menu => {
    if (menu.visible === false) {
      return false
    }
    
    if (menu.children && menu.children.length > 0) {
      menu.children = filterHiddenMenus(menu.children)
    }
    
    return true
  })
}

/**
 * 限制菜单层级
 */
function limitMenuLevel(menus: any[], maxLevel: number, currentLevel = 0): any[] {
  if (currentLevel >= maxLevel) {
    return []
  }

  return menus.map(menu => {
    const newMenu = { ...menu }
    
    if (newMenu.children && newMenu.children.length > 0) {
      newMenu.children = limitMenuLevel(newMenu.children, maxLevel, currentLevel + 1)
    }
    
    return newMenu
  })
}

/**
 * 菜单选择事件
 */
function handleMenuSelect(index: string, indexPath: string[], item: any) {
  emit('menuSelect', item)
}

/**
 * 根据当前路由设置激活菜单
 */
function setActiveMenuFromRoute() {
  if (props.defaultActive) {
    activeMenu.value = props.defaultActive
    return
  }

  const currentPath = route.path
  const findActiveMenu = (menus: any[]): string | null => {
    for (const menu of menus) {
      if (menu.path === currentPath) {
        return menu.path
      }
      
      if (menu.children && menu.children.length > 0) {
        const childActive = findActiveMenu(menu.children)
        if (childActive) {
          return childActive
        }
      }
    }
    return null
  }

  const activePath = findActiveMenu(filteredMenuTree.value)
  if (activePath) {
    activeMenu.value = activePath
  }
}

// 监视路由变化
watch(
  () => route.path,
  () => {
    setActiveMenuFromRoute()
  }
)

// 监视菜单数据变化
watch(
  () => permissionStore.menuTree,
  () => {
    setActiveMenuFromRoute()
  }
)

// 初始化
onMounted(async () => {
  if (props.autoLoad) {
    await loadUserMenu()
  }
  setActiveMenuFromRoute()
})
</script>

<style scoped>
.permission-menu {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.menu-container {
  flex: 1;
  overflow: auto;
}

.empty-menu {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  text-align: center;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
</style>