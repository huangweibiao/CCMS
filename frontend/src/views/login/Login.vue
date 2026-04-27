<template>
  <div class="login-container">
    <div class="login-form">
      <div class="login-header">
        <h2>企业级费控管理系统</h2>
        <p>CCMS - Corporate Cost Management System</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form-content"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>© 2025 CCMS - 企业级费控管理系统 v1.0.0</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 用户登录组件
 * 负责处理用户认证和登录流程
 * 
 * @file Login.vue
 * @description 企业级费控管理系统登录页面
 * @author CCMS开发团队
 * @version 1.0.0
 */

import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import type { LoginData } from '@/types/user'

// Vue路由实例
const router = useRouter()
// 用户状态管理
const userStore = useUserStore()

/**
 * 登录表单引用
 * 用于表单验证和重置操作
 */
const loginFormRef = ref<FormInstance>()

/**
 * 登录按钮加载状态
 * 控制登录过程中的加载动画和禁用状态
 */
const loading = ref(false)

/**
 * 登录表单数据
 * 包含用户名和密码字段
 */
const loginForm = reactive<LoginData>({
  username: '',
  password: ''
})

/**
 * 登录表单验证规则
 * 定义表单字段的验证要求和错误提示
 */
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ]
}

/**
 * 处理用户登录操作
 * 
 * 主要流程：
 * 1. 表单验证
 * 2. 调用登录API
 * 3. 登录成功跳转
 * 4. 错误处理
 * 
 * @throws {Error} 登录过程中出现的错误
 * @returns {Promise<void>}
 */
const handleLogin = async () => {
  // 表单引用安全检查
  if (!loginFormRef.value) return
  
  // 表单验证
  const valid = await loginFormRef.value.validate()
  if (!valid) return
  
  // 设置加载状态
  loading.value = true
  
  try {
    // 调用用户登录状态管理
    await userStore.login(loginForm)
    
    // 登录成功提示
    ElMessage.success('登录成功')
    
    // 跳转到仪表板页面
    router.push('/dashboard')
  } catch (error) {
    // 登录失败处理
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    // 重置加载状态
    loading.value = false
  }
}
</script>

<style lang="css" scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: cover;
}

.login-form {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #303133;
  margin-bottom: 8px;
  font-size: 24px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-form-content {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.login-footer p {
  color: #909399;
  font-size: 12px;
}
</style>