<template>
  <div class="app-sidebar">
    <el-scrollbar class="sidebar-scrollbar">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="false"
        background-color="#001529"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <template v-for="route in accessibleRoutes" :key="route.path">
          <!-- 有子菜单的情况 -->
          <el-sub-menu 
            v-if="route.children && route.children.length > 0" 
            :index="route.path"
          >
            <template #title>
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            
            <el-menu-item 
              v-for="child in route.children" 
              :key="child.path" 
              :index="route.path + '/' + child.path"
              v-show="!child.meta?.hidden"
            >
              <template #title>
                <el-icon v-if="child.meta?.icon">
                  <component :is="child.meta.icon" />
                </el-icon>
                <span>{{ child.meta?.title }}</span>
              </template>
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 没有子菜单的情况 -->
          <el-menu-item 
            v-else 
            :index="route.path"
            v-show="!route.meta?.hidden"
          >
            <el-icon v-if="route.meta?.icon">
              <component :is="route.meta.icon" />
            </el-icon>
            <span>{{ route.meta?.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>
    
    <!-- 侧边栏折叠按钮 -->
    <div class="sidebar-footer">
      <el-button 
        type="text" 
        @click="toggleCollapse" 
        class="collapse-btn"
      >
        <el-icon v-if="isCollapse">
          <expand />
        </el-icon>
        <el-icon v-else>
          <fold />
        </el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { AuthUtil } from '@/utils/auth'
import { 
  House, 
  User, 
  OfficeBuilding, 
  Money, 
  DocumentAdd,
  Bell,
  Finished,
  DataAnalysis,
  Expand,
  Fold
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 获取路由配置
const routes = computed(() => {
  return router.options.routes
    .filter(route => route.path === '/')[0]?.children || []
})

// 获取用户可访问的路由
const accessibleRoutes = computed(() => {
  return routes.value.filter(route => {
    return AuthUtil.canAccessRoute(route.meta || {})
  })
})

// 当前激活的菜单
const activeMenu = computed(() => {
  const { path } = route
  return path
})

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 监听窗口大小变化，自动折叠侧边栏
const handleResize = () => {
  if (window.innerWidth < 768) {
    isCollapse.value = true
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  handleResize() // 初始化检查
})
</script>

<style scoped lang="css">
.app-sidebar {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
  background-color: #001529;
  transition: width 0.3s;
}

.sidebar-scrollbar {
  flex: 1;
}

:deep(.el-menu) {
  border: none;
}

:deep(.el-sub-menu .el-menu-item) {
  background-color: #0c2135 !important;
}

:deep(.el-sub-menu .el-menu-item:hover) {
  background-color: #001528 !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
}

.sidebar-footer {
  border-top: 1px solid #2d3740;
  padding: 10px;
  text-align: center;
}

.collapse-btn {
  width: 100%;
  color: #bfcbd9;
  font-size: 16px;
}

.collapse-btn:hover {
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.1);
}

@media (max-width: 768px) {
  .app-sidebar {
    position: fixed;
    top: 60px;
    left: 0;
    z-index: 1000;
    height: calc(100vh - 60px);
  }
  
  .app-sidebar:not(:hover) {
    width: 64px !important;
  }
}
</style>