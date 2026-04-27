<template>
  <div class="department-container">
    <!-- 页面标题和操作区域 -->
    <div class="page-header">
      <div class="page-title">
        <h2>部门管理</h2>
        <p>管理企业组织架构和部门信息</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" @click="handleAddDepartment">
          <el-icon><plus /></el-icon>
          新增部门
        </el-button>
        <el-button @click="handleExpandAll">
          <el-icon><expand /></el-icon>
          全部展开
        </el-button>
        <el-button @click="handleCollapseAll">
          <el-icon><fold /></el-icon>
          全部收起
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <div class="search-form">
      <el-form :model="searchForm" size="default">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="部门名称">
              <el-input 
                v-model="searchForm.name" 
                placeholder="请输入部门名称"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="部门编码">
              <el-input 
                v-model="searchForm.code" 
                placeholder="请输入部门编码"
                clearable
              />
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
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><search /></el-icon>
                查询
              </el-button>
              <el-button @click="handleReset">
                <el-icon><refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 部门树表格 -->
    <div class="department-table">
      <el-table
        :data="filteredDepartments"
        row-key="id"
        :default-expand-all="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="name" label="部门名称" min-width="200">
          <template #default="{ row }">
            <div class="department-name">
              <el-icon v-if="row.children && row.children.length > 0">
                <folder-opened v-if="row.expanded" />
                <folder v-else />
              </el-icon>
              <el-icon v-else>
                <office-building />
              </el-icon>
              <span>{{ row.name }}</span>
              <span v-if="row.code" class="department-code">({{ row.code }})</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="managerName" label="部门负责人" width="120" />
        
        <el-table-column prop="userCount" label="员工数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.userCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="orderNum" label="排序" width="80" align="center" />
        
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="160" />
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleAddChild(row)">
              <el-icon><plus /></el-icon>
              添加子部门
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 部门编辑对话框 -->
    <CustomModal
      v-model="departmentModalVisible"
      :title="departmentModalTitle"
      width="500px"
      @closed="handleModalClosed"
    >
      <el-form
        ref="departmentFormRef"
        :model="departmentForm"
        :rules="departmentFormRules"
        label-width="100px"
      >
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="departmentForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        
        <el-form-item label="部门编码" prop="code">
          <el-input v-model="departmentForm.code" placeholder="请输入部门编码" />
        </el-form-item>
        
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="departmentForm.parentId"
            :data="departmentTreeData"
            :props="{ label: 'name', children: 'children', value: 'id' }"
            placeholder="请选择上级部门"
            check-strictly
            style="width: 100%"
            :disabled="!!currentDepartment?.parentId && isEditingChild"
          />
        </el-form-item>
        
        <el-form-item label="负责人" prop="managerId">
          <el-select
            v-model="departmentForm.managerId"
            placeholder="请选择部门负责人"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="排序" prop="orderNum">
          <el-input-number 
            v-model="departmentForm.orderNum" 
            :min="0" 
            :max="999" 
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="departmentForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="departmentForm.remark"
            type="textarea"
            placeholder="请输入部门描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="departmentModalVisible = false">取消</el-button>
        <el-button type="primary" @click="handleDepartmentSubmit" :loading="formLoading">
          保存
        </el-button>
      </template>
    </CustomModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Expand,
  Fold,
  Refresh,
  Search,
  Edit,
  Delete,
  FolderOpened,
  Folder,
  OfficeBuilding
} from '@element-plus/icons-vue'
import CustomModal from '@/components/common/CustomModal.vue'
import { showSuccess, showError, showConfirm } from '@/components/common/Message.vue'

// 响应式数据
const loading = ref(false)
const departmentModalVisible = ref(false)
const departmentModalTitle = ref('')
const formLoading = ref(false)
const isEditingChild = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  status: ''
})

// 部门数据
const departmentList = ref([])
const currentDepartment = ref(null)

// 部门表单
const departmentForm = reactive({
  id: '',
  name: '',
  code: '',
  parentId: '',
  managerId: '',
  orderNum: 0,
  status: 1,
  remark: ''
})

const departmentFormRef = ref<FormInstance>()

// 表单验证规则
const departmentFormRules: FormRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '部门名称长度在 2-50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入部门编码', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_-]+$/, message: '部门编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  parentId: [
    { required: true, message: '请选择上级部门', trigger: 'change' }
  ],
  orderNum: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ]
}

// 用户选项（用于负责人选择）
const userOptions = ref([])

// 计算属性
const departmentTreeData = computed(() => {
  // 将部门列表转换为树形结构
  return buildTree(departmentList.value)
})

const filteredDepartments = computed(() => {
  if (!searchForm.name && !searchForm.code && !searchForm.status) {
    return buildTree(departmentList.value)
  }
  
  // 搜索过滤逻辑
  return departmentList.value.filter(dept => {
    const nameMatch = !searchForm.name || dept.name.includes(searchForm.name)
    const codeMatch = !searchForm.code || dept.code.includes(searchForm.code)
    const statusMatch = !searchForm.status || dept.status === parseInt(searchForm.status)
    
    return nameMatch && codeMatch && statusMatch
  })
})

// 生命周期
onMounted(() => {
  loadDepartments()
  loadUserOptions()
})

// 方法
const buildTree = (list: any[], parentId: string = '') => {
  return list
    .filter(item => item.parentId === parentId)
    .map(item => ({
      ...item,
      children: buildTree(list, item.id)
    }))
}

const loadDepartments = async () => {
  try {
    loading.value = true
    // 模拟API调用
    departmentList.value = [
      {
        id: '1',
        name: '技术部',
        code: 'TECH',
        parentId: '',
        managerId: '1',
        managerName: '张工',
        userCount: 15,
        orderNum: 1,
        status: 1,
        createTime: '2024-01-01 10:00:00',
        remark: '技术研发部门'
      },
      {
        id: '2',
        name: '前端开发组',
        code: 'TECH_FRONTEND',
        parentId: '1',
        managerId: '2',
        managerName: '李工',
        userCount: 6,
        orderNum: 1,
        status: 1,
        createTime: '2024-01-01 10:00:00',
        remark: '前端开发团队'
      },
      {
        id: '3',
        name: '财务部',
        code: 'FINANCE',
        parentId: '',
        managerId: '3',
        managerName: '王会计',
        userCount: 8,
        orderNum: 2,
        status: 1,
        createTime: '2024-01-01 10:00:00',
        remark: '财务管理部门'
      }
    ]
  } catch (error) {
    showError('获取部门列表失败')
    console.error('获取部门列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadUserOptions = async () => {
  // 模拟加载用户列表
  userOptions.value = [
    { id: '1', realName: '张工' },
    { id: '2', realName: '李工' },
    { id: '3', realName: '王会计' },
    { id: '4', realName: '赵经理' }
  ]
}

const handleSearch = () => {
  // 搜索逻辑
  loadDepartments()
}

const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    code: '',
    status: ''
  })
  loadDepartments()
}

const handleAddDepartment = () => {
  departmentModalTitle.value = '新增部门'
  currentDepartment.value = null
  isEditingChild.value = false
  resetDepartmentForm()
  departmentModalVisible.value = true
}

const handleAddChild = (row: any) => {
  departmentModalTitle.value = '添加子部门'
  currentDepartment.value = row
  isEditingChild.value = true
  resetDepartmentForm()
  departmentForm.parentId = row.id
  departmentModalVisible.value = true
}

const handleEdit = (row: any) => {
  departmentModalTitle.value = '编辑部门'
  currentDepartment.value = row
  isEditingChild.value = false
  Object.assign(departmentForm, {
    id: row.id,
    name: row.name,
    code: row.code,
    parentId: row.parentId,
    managerId: row.managerId,
    orderNum: row.orderNum,
    status: row.status,
    remark: row.remark
  })
  departmentModalVisible.value = true
}

const handleDelete = async (row: any) => {
  if (row.userCount && row.userCount > 0) {
    showError('该部门下有员工，无法删除')
    return
  }
  
  const hasChildren = departmentList.value.some(dept => dept.parentId === row.id)
  if (hasChildren) {
    showError('该部门下有子部门，无法删除')
    return
  }
  
  try {
    const confirm = await showConfirm({
      message: `确定要删除部门 "${row.name}" 吗？此操作不可恢复。`,
      type: 'warning'
    })
    
    if (confirm) {
      // TODO: 调用删除API
      showSuccess('部门删除成功')
      loadDepartments()
    }
  } catch (error) {
    console.error('删除部门失败:', error)
  }
}

const handleDepartmentSubmit = async () => {
  if (!departmentFormRef.value) return

  try {
    const valid = await departmentFormRef.value.validate()
    if (!valid) return

    formLoading.value = true
    
    // TODO: 调用保存API
    showSuccess(departmentForm.id ? '部门修改成功' : '部门添加成功')
    departmentModalVisible.value = false
    loadDepartments()
  } catch (error) {
    console.error('保存部门失败:', error)
  } finally {
    formLoading.value = false
  }
}

const handleModalClosed = () => {
  currentDepartment.value = null
  departmentFormRef.value?.clearValidate()
}

const handleExpandAll = () => {
  // 展开所有节点逻辑
  showSuccess('展开全部')
}

const handleCollapseAll = () => {
  // 收起所有节点逻辑
  showSuccess('收起全部')
}

const handleRefresh = () => {
  loadDepartments()
}

const resetDepartmentForm = () => {
  Object.assign(departmentForm, {
    id: '',
    name: '',
    code: '',
    parentId: '',
    managerId: '',
    orderNum: 0,
    status: 1,
    remark: ''
  })
  departmentFormRef.value?.clearValidate()
}
</script>

<style scoped lang="css">
.department-container {
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

.department-table {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.department-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.department-code {
  color: #909399;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .department-container {
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
  .department-table {
    padding: 15px;
  }
  
  .department-name {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>