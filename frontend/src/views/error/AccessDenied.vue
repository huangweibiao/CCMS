<template>
  <div class="access-denied-container">
    <div class="error-content">
      <div class="error-icon">
        <el-icon size="100" color="#F56C6C">
          <Warning />
        </el-icon>
      </div>
      
      <div class="error-title">
        403
      </div>
      
      <div class="error-subtitle">
        抱歉，您没有权限访问此页面
      </div>
      
      <div class="error-message" v-if="errorMessage">
        {{ errorMessage }}
      </div>
      
      <div class="error-actions">
        <el-button type="primary" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回上一页
        </el-button>
        
        <el-button @click="goHome">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
        
        <el-button @click="goLogin" v-if="showLoginButton">
          <el-icon><User /></el-icon>
          重新登录
        </el-button>
      </div>
      
      <div class="error-details" v-if="showDetails">
        <p><strong>详细信息：</strong></p>
        <p>访问路径：{{ fromPath }}</p>
        <p>当前用户：{{ currentUserName }}</p>
        <p>用户角色：{{ currentUserRole }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Warning, ArrowLeft, House, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 计算属性
const errorMessage = computed(() => route.query.message as string || '您当前的权限级别无法访问此功能')
const fromPath = computed(() => route.query.from as string || '未知页面')
const showLoginButton = computed(() => !userStore.isAuthenticated)
const currentUserName = computed(() => userStore.userInfo?.userName || '未登录')
const currentUserRole = computed(() => userStore.userInfo?.role || '未分配')

// 根据错误信息决定是否显示详细信息
const showDetails = computed(() => {
  return !errorMessage.value.includes('请先登录')
})

// 方法
const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    goHome()
  }
}

const goHome = () => {
  if (userStore.isAuthenticated) {
    router.push('/dashboard')
  } else {
    router.push('/login')
  }
}

const goLogin = () => {
  router.push({
    path: '/login',
    query: { redirect: fromPath.value }
  })
}

onMounted(() => {
  console.warn(`权限不足访问: ${fromPath.value}`, {
    user: currentUserName.value,
    role: currentUserRole.value,
    message: errorMessage.value
  })
})
</script>

<style scoped>
.access-denied-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.error-content {
  text-align: center;
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 90%;
}

.error-icon {
  margin-bottom: 20px;
}

.error-title {
  font-size: 48px;
  font-weight: bold;
  color: #F56C6C;
  margin-bottom: 10px;
}

.error-subtitle {
  font-size: 18px;
  color: #606266;
  margin-bottom: 20px;
}

.error-message {
  color: #909399;
  font-size: 14px;
  margin-bottom: 30px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.error-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.error-actions .el-button {
  min-width: 120px;
}

.error-details {
  border-top: 1px solid #e4e7ed;
  padding-top: 20px;
  text-align: left;
  font-size: 12px;
  color: #909399;
}

.error-details p {
  margin: 5px 0;
}

@media (max-width: 768px) {
  .error-content {
    padding: 20px;
    margin: 20px;
  }
  
  .error-title {
    font-size: 36px;
  }
  
  .error-subtitle {
    font-size: 16px;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>