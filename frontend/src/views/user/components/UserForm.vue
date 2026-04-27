<template>
  <div class="user-form-container">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      :disabled="loading"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="formData.username"
          placeholder="请输入用户名"
          :maxlength="20"
          show-word-limit
          :disabled="!!userData"
        />
      </el-form-item>

      <el-form-item label="姓名" prop="realName">
        <el-input
          v-model="formData.realName"
          placeholder="请输入真实姓名"
          :maxlength="20"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="formData.email"
          placeholder="请输入邮箱地址"
          type="email"
        />
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="formData.phone"
          placeholder="请输入手机号"
          :maxlength="11"
        />
      </el-form-item>

      <el-form-item label="部门" prop="departmentId">
        <el-select
          v-model="formData.departmentId"
          placeholder="请选择部门"
          style="width: 100%"
        >
          <el-option
            v-for="dept in departmentOptions"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="角色" prop="roleIds">
        <el-select
          v-model="formData.roleIds"
          placeholder="请选择角色"
          multiple
          style="width: 100%"
        >
          <el-option
            v-for="role in roleOptions"
            :key="role.id"
            :label="role.name"
            :value="role.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="formData.remark"
          type="textarea"
          placeholder="请输入备注信息"
          :rows="3"
          :maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <div v-if="!userData" class="password-section">
        <el-alert
          title="新用户设置"
          type="info"
          :closable="false"
          description="新用户需要设置初始密码"
          show-icon
        />
        
        <el-form-item label="初始密码" prop="password" class="mt-4">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入初始密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请确认初始密码"
            show-password
          />
        </el-form-item>
      </div>

      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ loading ? '保存中...' : '保存' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

// Props
interface Props {
  userData?: any
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

// Emits
const emit = defineEmits<{
  submit: [formData: any]
  cancel: []
}>()

// 表单引用
const formRef = ref<FormInstance>()

// 响应式数据
const departmentOptions = ref([
  { id: '1', name: '技术部' },
  { id: '2', name: '财务部' },
  { id: '3', name: '人事部' },
  { id: '4', name: '市场部' },
  { id: '5', name: '行政部' },
  { id: '6', name: '销售部' }
])

const roleOptions = ref([
  { id: '1', name: '系统管理员' },
  { id: '2', name: '财务人员' },
  { id: '3', name: '部门经理' },
  { id: '4', name: '普通员工' }
])

// 表单数据
const formData = reactive({
  id: '',
  username: '',
  realName: '',
  email: '',
  phone: '',
  departmentId: '',
  roleIds: [],
  status: 1,
  remark: '',
  password: '',
  confirmPassword: ''
})

// 表单验证规则
const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3-20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2-20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择部门', trigger: 'change' }
  ],
  roleIds: [
    { required: true, message: '请选择角色', trigger: 'change' },
    { type: 'array', min: 1, message: '至少选择一个角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  password: [
    { 
      required: () => !props.userData, 
      message: '请输入初始密码', 
      trigger: 'blur' 
    },
    { 
      min: 6, 
      max: 20, 
      message: '密码长度在 6-20 个字符', 
      trigger: 'blur' 
    }
  ],
  confirmPassword: [
    { 
      required: () => !props.userData,
      message: '请确认密码',
      trigger: 'blur'
    },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (!props.userData && value !== formData.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 生命周期
onMounted(() => {
  if (props.userData) {
    initFormData()
  }
})

// 监听userData变化
watch(() => props.userData, () => {
  if (props.userData) {
    initFormData()
  }
}, { immediate: true })

// 方法
const initFormData = () => {
  if (!props.userData) return

  Object.assign(formData, {
    id: props.userData.id || '',
    username: props.userData.username || '',
    realName: props.userData.realName || '',
    email: props.userData.email || '',
    phone: props.userData.phone || '',
    departmentId: props.userData.departmentId || '',
    roleIds: props.userData.roleIds || props.userData.roles?.map((role: any) => role.id) || [],
    status: props.userData.status ?? 1,
    remark: props.userData.remark || '',
    password: '',
    confirmPassword: ''
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    // 准备提交数据
    const submitData = {
      ...formData
    }

    // 编辑时移除密码字段（如果不是新设密码）
    if (props.userData && !formData.password) {
      delete submitData.password
      delete submitData.confirmPassword
    }

    emit('submit', submitData)
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleCancel = () => {
  emit('cancel')
  formRef.value?.resetFields()
}

// 暴露方法
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    id: '',
    username: '',
    realName: '',
    email: '',
    phone: '',
    departmentId: '',
    roleIds: [],
    status: 1,
    remark: '',
    password: '',
    confirmPassword: ''
  })
}

const validateForm = () => {
  return formRef.value?.validate()
}

// 暴露方法给父组件
defineExpose({
  resetForm,
  validateForm
})
</script>

<style scoped lang="css">
.user-form-container {
  padding: 20px 0;
}

.form-actions {
  text-align: right;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.password-section {
  margin-bottom: 20px;
}

.mt-4 {
  margin-top: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-form-container {
    padding: 0;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.el-form-item__label) {
    text-align: left;
  }
}
</style>