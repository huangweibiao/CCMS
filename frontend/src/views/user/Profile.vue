<template>
  <div class="profile-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>个人中心</h2>
      <p>管理您的个人信息和账户设置</p>
    </div>

    <div class="profile-content">
      <!-- 侧边栏导航 -->
      <div class="profile-sidebar">
        <div class="user-info-card">
          <div class="user-avatar">
            <el-avatar :size="80" :src="userInfo.avatar" :alt="userInfo.realName">
              {{ userInfo.realName?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="avatar-upload" @click="handleAvatarUpload">
              <el-icon><camera /></el-icon>
            </div>
          </div>
          <div class="user-details">
            <h3>{{ userInfo.realName || userInfo.username }}</h3>
            <p class="user-department">{{ userInfo.departmentName || '暂无部门' }}</p>
            <p class="user-role">{{ userInfo.roleNames?.join(', ') || '暂无角色' }}</p>
          </div>
        </div>

        <div class="sidebar-nav">
          <div 
            v-for="nav in navItems" 
            :key="nav.key"
            class="nav-item"
            :class="{ 'nav-item-active': activeNav === nav.key }"
            @click="handleNavClick(nav.key)"
          >
            <el-icon class="nav-icon">
              <component :is="nav.icon" />
            </el-icon>
            <span class="nav-label">{{ nav.label }}</span>
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="profile-main">
        <!-- 基本信息 -->
        <div v-if="activeNav === 'basic'" class="profile-section">
          <div class="section-header">
            <h3>基本信息</h3>
            <el-button type="primary" @click="handleEditBasic">
              <el-icon><edit /></el-icon>
              编辑信息
            </el-button>
          </div>
          
          <div class="info-grid">
            <div class="info-item">
              <label>用户名：</label>
              <span>{{ userInfo.username }}</span>
            </div>
            <div class="info-item">
              <label>姓名：</label>
              <span>{{ userInfo.realName || '未设置' }}</span>
            </div>
            <div class="info-item">
              <label>邮箱：</label>
              <span>{{ userInfo.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <label>手机号：</label>
              <span>{{ userInfo.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <label>部门：</label>
              <span>{{ userInfo.departmentName || '未分配' }}</span>
            </div>
            <div class="info-item">
              <label>角色：</label>
              <span>{{ userInfo.roleNames?.join(', ') || '未分配' }}</span>
            </div>
            <div class="info-item full-width">
              <label>注册时间：</label>
              <span>{{ userInfo.createTime || '未知' }}</span>
            </div>
          </div>
        </div>

        <!-- 修改密码 -->
        <div v-else-if="activeNav === 'password'" class="profile-section">
          <div class="section-header">
            <h3>修改密码</h3>
            <p>定期修改密码可以提高账户安全性</p>
          </div>
          
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordFormRules"
            label-width="120px"
            class="password-form"
          >
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input
                v-model="passwordForm.currentPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
              <div class="password-tips">
                密码长度6-20位，建议包含字母、数字和特殊字符
              </div>
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
                修改密码
              </el-button>
              <el-button @click="handleResetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 安全设置 -->
        <div v-else-if="activeNav === 'security'" class="profile-section">
          <div class="section-header">
            <h3>安全设置</h3>
            <p>管理您的账户安全选项</p>
          </div>
          
          <div class="security-list">
            <div class="security-item">
              <div class="security-info">
                <h4>登录验证</h4>
                <p>启用双重验证提高账户安全性</p>
              </div>
              <el-switch v-model="securitySettings.twoFactorAuth" />
            </div>
            
            <div class="security-item">
              <div class="security-info">
                <h4>登录提醒</h4>
                <p>新设备登录时发送邮件通知</p>
              </div>
              <el-switch v-model="securitySettings.loginAlert" />
            </div>
            
            <div class="security-item">
              <div class="security-info">
                <h4>会话管理</h4>
                <p>查看和管理您的登录会话</p>
              </div>
              <el-button type="primary" link @click="handleViewSessions">
                查看会话
              </el-button>
            </div>
          </div>
          
          <div class="security-actions">
            <el-button type="primary" @click="handleSaveSecuritySettings">
              保存设置
            </el-button>
          </div>
        </div>

        <!-- 操作日志 -->
        <div v-else-if="activeNav === 'logs'" class="profile-section">
          <div class="section-header">
            <h3>操作日志</h3>
            <p>查看您的最近操作记录</p>
          </div>
          
          <div class="logs-list">
            <div v-for="log in operationLogs" :key="log.id" class="log-item">
              <div class="log-content">
                <div class="log-action">{{ log.action }}</div>
                <div class="log-detail">{{ log.detail }}</div>
              </div>
              <div class="log-meta">
                <div class="log-time">{{ log.time }}</div>
                <div class="log-ip">{{ log.ip }}</div>
              </div>
            </div>
          </div>
          
          <div class="logs-pagination">
            <el-pagination
              :current-page="logsPagination.current"
              :page-size="logsPagination.pageSize"
              :total="logsPagination.total"
              layout="prev, pager, next"
              @current-change="handleLogsPageChange"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑基本信息对话框 -->
    <CustomModal
      v-model="editModalVisible"
      title="编辑基本信息"
      width="500px"
      @closed="handleEditModalClosed"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" type="email" placeholder="请输入邮箱地址" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editModalVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveBasicInfo" :loading="editLoading">
          保存
        </el-button>
      </template>
    </CustomModal>

    <!-- 头像上传组件 -->
    <FileUpload
      v-if="avatarUploadVisible"
      ref="avatarUploadRef"
      action="/api/user/avatar"
      :limit="1"
      :show-file-list="false"
      :auto-upload="true"
      accept="image/*"
      @success="handleAvatarSuccess"
      @error="handleAvatarError"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  User,
  Lock,
  Shield,
  Document,
  Edit,
  Camera
} from '@element-plus/icons-vue'
import CustomModal from '@/components/common/CustomModal.vue'
import FileUpload from '@/components/common/FileUpload.vue'
import { useUserStore } from '@/stores/user'
import { showSuccess, showError } from '@/components/common/Message.vue'

// Store
const userStore = useUserStore()

// 响应式数据
const activeNav = ref('basic')
const editModalVisible = ref(false)
const avatarUploadVisible = ref(false)

// 表单引用
const passwordFormRef = ref<FormInstance>()
const editFormRef = ref<FormInstance>()

// 用户信息
const userInfo = reactive({
  id: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  departmentName: '',
  roleNames: [],
  avatar: '',
  createTime: ''
})

// 导航项
const navItems = [
  { key: 'basic', label: '基本信息', icon: User },
  { key: 'password', label: '修改密码', icon: Lock },
  { key: 'security', label: '安全设置', icon: Shield },
  { key: 'logs', label: '操作日志', icon: Document }
]

// 密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordLoading = ref(false)

// 密码表单验证规则
const passwordFormRules: FormRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6-20 个字符', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*\d)/,
      message: '密码必须包含字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 编辑表单
const editForm = reactive({
  realName: '',
  email: '',
  phone: ''
})

const editLoading = ref(false)

// 编辑表单验证规则
const editFormRules: FormRules = {
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2-20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 安全设置
const securitySettings = reactive({
  twoFactorAuth: false,
  loginAlert: true
})

// 操作日志
const operationLogs = ref([])
const logsPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 生命周期
onMounted(() => {
  loadUserInfo()
  loadSecuritySettings()
  loadOperationLogs()
})

// 方法
const loadUserInfo = () => {
  // 从store获取用户信息
  Object.assign(userInfo, userStore.userInfo || {})
}

const loadSecuritySettings = async () => {
  // 模拟加载安全设置
  Object.assign(securitySettings, {
    twoFactorAuth: false,
    loginAlert: true
  })
}

const loadOperationLogs = async () => {
  try {
    // 模拟加载操作日志
    operationLogs.value = [
      {
        id: '1',
        action: '用户登录',
        detail: '从IP 192.168.1.100登录系统',
        time: '2024-01-15 10:30:25',
        ip: '192.168.1.100'
      },
      {
        id: '2',
        action: '修改个人信息',
        detail: '更新了邮箱地址',
        time: '2024-01-14 16:20:15',
        ip: '192.168.1.100'
      },
      {
        id: '3',
        action: '查看预算报表',
        detail: '查询了2024年1月份预算数据',
        time: '2024-01-14 14:10:08',
        ip: '192.168.1.100'
      },
      {
        id: '4',
        action: '提交费用申请',
        detail: '提交了差旅费用申请单',
        time: '2024-01-13 11:25:30',
        ip: '192.168.1.100'
      }
    ]
    logsPagination.total = operationLogs.value.length
  } catch (error) {
    console.error('加载操作日志失败:', error)
  }
}

const handleNavClick = (navKey: string) => {
  activeNav.value = navKey
}

const handleEditBasic = () => {
  Object.assign(editForm, {
    realName: userInfo.realName,
    email: userInfo.email,
    phone: userInfo.phone
  })
  editModalVisible.value = true
}

const handleSaveBasicInfo = async () => {
  if (!editFormRef.value) return

  try {
    const valid = await editFormRef.value.validate()
    if (!valid) return

    editLoading.value = true
    
    // TODO: 调用更新用户信息API
    Object.assign(userInfo, editForm)
    
    showSuccess('个人信息更新成功')
    editModalVisible.value = false
  } catch (error) {
    console.error('保存个人信息失败:', error)
  } finally {
    editLoading.value = false
  }
}

const handleEditModalClosed = () => {
  editFormRef.value?.clearValidate()
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    const valid = await passwordFormRef.value.validate()
    if (!valid) return

    passwordLoading.value = true
    
    // TODO: 调用修改密码API
    showSuccess('密码修改成功')
    handleResetPasswordForm()
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

const handleResetPasswordForm = () => {
  passwordFormRef.value?.resetFields()
}

const handleSaveSecuritySettings = async () => {
  try {
    // TODO: 调用保存安全设置API
    showSuccess('安全设置已保存')
  } catch (error) {
    console.error('保存安全设置失败:', error)
  }
}

const handleViewSessions = () => {
  showSuccess('会话管理功能开发中...')
}

const handleLogsPageChange = (page: number) => {
  logsPagination.current = page
  loadOperationLogs()
}

const handleAvatarUpload = () => {
  avatarUploadVisible.value = true
}

const handleAvatarSuccess = (response: any, file: any) => {
  // TODO: 处理头像上传成功
  userInfo.avatar = response.data.url
  showSuccess('头像上传成功')
  avatarUploadVisible.value = false
}

const handleAvatarError = (error: Error) => {
  showError('头像上传失败')
  console.error('头像上传失败:', error)
}
</script>

<style scoped lang="css">
.profile-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.profile-content {
  display: flex;
  gap: 20px;
}

.profile-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.user-info-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.avatar-upload {
  position: absolute;
  bottom: 0;
  right: 0;
  background: #409eff;
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
}

.user-details h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.user-department,
.user-role {
  margin: 4px 0;
  color: #909399;
  font-size: 14px;
}

.sidebar-nav {
  background: white;
  border-radius: 8px;
  padding: 16px 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: background-color 0.3s;
  gap: 12px;
}

.nav-item:hover {
  background-color: #f5f7fa;
}

.nav-item-active {
  background-color: #ecf5ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

.nav-icon {
  font-size: 18px;
}

.nav-label {
  font-weight: 500;
}

.profile-main {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.profile-section {
  min-height: 400px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.section-header h3 {
  margin: 0;
  color: #303133;
}

.section-header p {
  margin: 4px 0 0 0;
  color: #909399;
  font-size: 14px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item label {
  min-width: 80px;
  color: #606266;
  font-weight: 500;
}

.info-item span {
  color: #303133;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.password-form {
  max-width: 500px;
}

.password-tips {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.security-list {
  max-width: 600px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.security-info h4 {
  margin: 0 0 4px 0;
  color: #303133;
}

.security-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.security-actions {
  margin-top: 24px;
}

.logs-list {
  max-height: 400px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.log-content {
  flex: 1;
}

.log-action {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.log-detail {
  font-size: 14px;
  color: #606266;
}

.log-meta {
  text-align: right;
  min-width: 200px;
}

.log-time {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.log-ip {
  font-size: 12px;
  color: #c0c4cc;
}

.logs-pagination {
  margin-top: 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    padding: 10px;
  }
  
  .profile-content {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
  
  .profile-main {
    padding: 16px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .log-item {
    flex-direction: column;
    gap: 8px;
  }
  
  .log-meta {
    text-align: left;
    min-width: auto;
  }
}
</style>