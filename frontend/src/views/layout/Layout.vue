<template>
  <div class="layout-container">
    <AppHeader />
    <div class="layout-body">
      <AppSidebar />
      <div class="layout-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import { onMounted } from 'vue'
import { AuthUtil } from '@/utils/auth'

// 初始化认证状态
onMounted(() => {
  AuthUtil.initAuth()
})
</script>

<style scoped lang="css">
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.layout-content {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
  overflow: auto;
}

@media (max-width: 768px) {
  .layout-content {
    padding: 10px;
  }
}
</style>