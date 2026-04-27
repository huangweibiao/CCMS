<template>
  <div class="user-list-container">
    <!-- 页面标题和操作区域 -->
    <div class="page-header">
      <div class="page-title">
        <h2>用户管理</h2>
        <p>查询、管理企业用户信息</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><plus /></el-icon>
          新增用户
        </el-button>
        <el-button @click="handleExport">
          <el-icon><download /></el-icon>
          导出数据
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <div class="search-form">
      <el-form :model="searchForm" size="default">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="用户名">
              <el-input 
                v-model="searchForm.username" 
                placeholder="请输入用户名"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="姓名">
              <el-input 
                v-model="searchForm.realName" 
                placeholder="请输入姓名"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="部门">
              <el-select v-model="searchForm.departmentId" placeholder="请选择部门" clearable>
                <el-option
                  v-for="dept in departmentOptions"
                  :key="dept.id"
                  :label="dept.name"
                  :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
                <el-option label="启用" value="1" />
                <el-option label="禁用" value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: right;">
            <el-button type="primary" @click="handleSearch">
              <el-icon><search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><refresh /></el-icon>
              重置
            </el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <DataTable
        ref="dataTableRef"
        :columns="tableColumns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        @pagination-change="handlePaginationChange"
      >
        <template #actions="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">
            <el-icon><edit /></el-icon>
            编辑
          </el-button>
          <el-button 
            type="success" 
            link 
            size="small" 
            @click="handleResetPassword(row)"
          >
            <el-icon><key /></el-icon>
            重置密码
          </el-button>
          <el-button 
            type="warning" 
            link 
            size="small" 
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button 
            type="danger" 
            link 
            size="small" 
            @click="handleDelete(row)"
          >
            <el-icon><delete /></el-icon>
            删除
          </el-button>
        </template>
      </DataTable>
    </div>

    <!-- 用户编辑对话框 -->
    <CustomDrawer
      v-model="editDrawerVisible"
      :title="editDrawerTitle"
      size="40%"
      @closed="handleEditClosed"
    >
      <UserForm
        v-if="editDrawerVisible"
        :user-data="currentUser"
        :loading="formLoading"
        @submit="handleFormSubmit"
        @cancel="handleFormCancel"
      />
    </CustomDrawer>

    <!-- 重置密码对话框 -->
    <CustomModal
      v-model="resetPasswordModalVisible"
      title="重置密码"
      width="400px"
      @closed="handleResetPasswordClosed"
    >
      <el-form :model="resetPasswordForm" :rules="resetPasswordRules" ref="resetPasswordFormRef">
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordModalVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPasswordSubmit" :loading="resetPasswordLoading">
          确定
        </el-button>
      </template>
    </CustomModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Download,
  Refresh,
  Search,
  Edit,
  Key,
  Delete
} from '@element-plus/icons-vue'
import DataTable from '@/components/common/DataTable.vue'
import CustomModal from '@/components/common/CustomModal.vue'
import CustomDrawer from '@/components/common/CustomDrawer.vue'
import UserForm from './components/UserForm.vue'
import { getUserList, updateUser, deleteUser, resetPassword } from '@/api/user'
import { showSuccess, showError, showConfirm } from '@/components/common/Message.vue'

// 表格列配置
const tableColumns = [
  {
    prop: 'username',
    label: '用户名',
    width: '120px',
    sortable: true
  },
  {
    prop: 'realName',
    label: '姓名',
    width: '100px',
    sortable: true
  },
  {
    prop: 'departmentName',
    label: '部门',
    width: '120px'
  },
  {
    prop: 'email',
    label: '邮箱',
    width: '180px'
  },
  {
    prop: 'phone',
    label: '手机号',
    width: '130px'
  },
  {
    prop: 'roleNames',
    label: '角色',
    showOverflowTooltip: true
  },
  {
    prop: 'status',
    label: '状态',
    width: '80px',
    type: 'tag',
    tagType: (row: any) => row.status === 1 ? 'success' : 'danger',
    formatter: (row: any) => row.status === 1 ? '启用' : '禁用'
  },
  {
    prop: 'createTime',
    label: '创建时间',
    width: '160px',
    type: 'date',
    sortable: true
  },
  {
    prop: 'actions',
    label: '操作',
    width: '200px',
    slot: true
  }
]

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const dataTableRef = ref()
const departmentOptions = ref([])

// 搜索表单
const searchForm = reactive({
  username: '',
  realName: '',
  departmentId: '',
  status: ''
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 编辑相关
const editDrawerVisible = ref(false)
const editDrawerTitle = ref('')
const currentUser = ref(null)
const formLoading = ref(false)

// 重置密码相关
const resetPasswordModalVisible = ref(false)
const resetPasswordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})
const resetPasswordFormRef = ref<FormInstance>()
const resetPasswordLoading = ref(false)
const currentResetUserId = ref('')

// 重置密码验证规则
const resetPasswordRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== resetPasswordForm.newPassword) {
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
  loadTableData()
  loadDepartmentOptions()
})

// 方法
const loadTableData = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    }
    
    const response = await getUserList(params)
    tableData.value = response.data.list
    pagination.total = response.data.total
  } catch (error) {
    showError('获取用户列表失败')
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadDepartmentOptions = async () => {
  // 这里调用部门API获取部门列表
  departmentOptions.value = [
    { id: '1', name: '技术部' },
    { id: '2', name: '财务部' },
    { id: '3', name: '人事部' },
    { id: '4', name: '市场部' }
  ]
}

const handleSearch = () => {
  pagination.current = 1
  loadTableData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    realName: '',
    departmentId: '',
    status: ''
  })
  pagination.current = 1
  loadTableData()
}

const handlePaginationChange = (page: number, pageSize: number) => {
  pagination.current = page
  pagination.pageSize = pageSize
  loadTableData()
}

const handleAdd = () => {
  editDrawerTitle.value = '新增用户'
  currentUser.value = null
  editDrawerVisible.value = true
}

const handleEdit = (row: any) => {
  editDrawerTitle.value = '编辑用户'
  currentUser.value = { ...row }
  editDrawerVisible.value = true
}

const handleEditClosed = () => {
  currentUser.value = null
}

const handleFormSubmit = async (formData: any) => {
  try {
    formLoading.value = true
    await updateUser(formData)
    showSuccess(currentUser.value ? '用户修改成功' : '用户添加成功')
    editDrawerVisible.value = false
    loadTableData()
  } catch (error) {
    console.error('保存用户失败:', error)
  } finally {
    formLoading.value = false
  }
}

const handleFormCancel = () => {
  editDrawerVisible.value = false
}

const handleDelete = async (row: any) => {
  try {
    const confirm = await showConfirm({
      message: `确定要删除用户 "${row.realName || row.username}" 吗？此操作不可恢复。`,
      type: 'warning'
    })
    
    if (confirm) {
      await deleteUser(row.id)
      showSuccess('用户删除成功')
      loadTableData()
    }
  } catch (error) {
    console.error('删除用户失败:', error)
  }
}

const handleResetPassword = (row: any) => {
  currentResetUserId.value = row.id
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordFormRef.value?.clearValidate()
  resetPasswordModalVisible.value = true
}

const handleResetPasswordSubmit = async () => {
  if (!resetPasswordFormRef.value) return

  try {
    const valid = await resetPasswordFormRef.value.validate()
    if (!valid) return

    resetPasswordLoading.value = true
    await resetPassword({
      userId: currentResetUserId.value,
      newPassword: resetPasswordForm.newPassword
    })
    
    showSuccess('密码重置成功')
    resetPasswordModalVisible.value = false
  } catch (error) {
    console.error('重置密码失败:', error)
  } finally {
    resetPasswordLoading.value = false
  }
}

const handleResetPasswordClosed = () => {
  currentResetUserId.value = ''
  resetPasswordFormRef.value?.clearValidate()
}

const handleToggleStatus = async (row: any) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const action = row.status === 1 ? '禁用' : '启用'
    
    const confirm = await showConfirm({
      message: `确定要${action}用户 "${row.realName || row.username}" 吗？`,
      type: 'warning'
    })
    
    if (confirm) {
      await updateUser({
        ...row,
        status: newStatus
      })
      showSuccess(`用户${action}成功`)
      loadTableData()
    }
  } catch (error) {
    console.error('切换用户状态失败:', error)
  }
}

const handleExport = () => {
  showSuccess('导出功能开发中...')
}

const handleRefresh = () => {
  loadTableData()
}
</script>

<style scoped lang="css">
.user-list-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-title h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.page-title p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.page-actions {
  display: flex;
  gap: 10px;
}

.search-form {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.table-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-list-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    padding: 15px;
  }
  
  .page-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .search-form,
  .table-container {
    padding: 15px;
  }
}
</style>