<template>
  <div class="role-container">
    <!-- 页面标题和操作区域 -->
    <div class="page-header">
      <div class="page-title">
        <h2>角色权限管理</h2>
        <p>管理系统角色和权限分配</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" @click="handleAddRole">
          <el-icon><plus /></el-icon>
          新增角色
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 角色列表 -->
    <div class="role-list">
      <div class="search-panel">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索角色名称或编码"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="role-cards">
        <div 
          v-for="role in filteredRoles" 
          :key="role.id" 
          class="role-card"
          :class="{ 'role-card-active': currentRole?.id === role.id }"
          @click="handleSelectRole(role)"
        >
          <div class="role-header">
            <div class="role-name">{{ role.name }}</div>
            <div class="role-code">{{ role.code }}</div>
            <el-tag 
              :type="role.status === 1 ? 'success' : 'danger'" 
              size="small"
            >
              {{ role.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </div>
          
          <div class="role-info">
            <div class="role-stat">
              <span class="stat-label">用户数量:</span>
              <span class="stat-value">{{ role.userCount || 0 }}</span>
            </div>
            <div class="role-stat">
              <span class="stat-label">权限数量:</span>
              <span class="stat-value">{{ role.permissionCount || 0 }}</span>
            </div>
          </div>
          
          <div class="role-desc">{{ role.remark || '暂无描述' }}</div>
          
          <div class="role-actions">
            <el-button type="primary" link size="small" @click.stop="handleEditRole(role)">
              编辑
            </el-button>
            <el-button type="warning" link size="small" @click.stop="handleToggleStatus(role)">
              {{ role.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link size="small" @click.stop="handleDeleteRole(role)">
              删除
            </el-button>
          </div>
        </div>
        
        <div v-if="filteredRoles.length === 0" class="empty-state">
          <el-empty description="暂无角色数据" />
        </div>
      </div>
    </div>

    <!-- 权限分配面板 -->
    <div class="permission-panel" v-if="currentRole">
      <div class="panel-header">
        <h3>权限分配 - {{ currentRole?.name }}</h3>
        <div class="panel-actions">
          <el-button type="primary" @click="handleSavePermissions" :loading="permissionLoading">
            保存权限
          </el-button>
          <el-button @click="handleSelectAll">全选</el-button>
          <el-button @click="handleClearAll">清空</el-button>
        </div>
      </div>
      
      <div class="permission-tree">
        <el-tree
          ref="permissionTreeRef"
          :data="permissionTreeData"
          show-checkbox
          node-key="id"
          :default-expand-all="true"
          :props="{ label: 'name', children: 'children' }"
          :check-strictly="false"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <span class="node-label">{{ data.name }}</span>
              <span v-if="data.code" class="node-code">{{ data.code }}</span>
              <el-tag v-if="data.type === 'button'" size="small" type="info">按钮</el-tag>
              <span v-if="data.api" class="node-api">{{ data.api }}</span>
            </div>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- 角色编辑对话框 -->
    <CustomModal
      v-model="roleModalVisible"
      :title="roleModalTitle"
      width="500px"
      @closed="handleRoleModalClosed"
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="roleFormRules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="roleForm.code" placeholder="请输入角色编码" />
        </el-form-item>
        
        <el-form-item label="数据权限" prop="dataScope">
          <el-select v-model="roleForm.dataScope" placeholder="请选择数据权限" style="width: 100%">
            <el-option label="全部数据" value="1" />
            <el-option label="本部门数据" value="2" />
            <el-option label="本部门及以下数据" value="3" />
            <el-option label="仅本人数据" value="4" />
            <el-option label="自定义数据" value="5" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="排序" prop="orderNum">
          <el-input-number 
            v-model="roleForm.orderNum" 
            :min="0" 
            :max="999" 
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="roleForm.remark"
            type="textarea"
            placeholder="请输入角色描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="roleModalVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit" :loading="formLoading">
          保存
        </el-button>
      </template>
    </CustomModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import CustomModal from '@/components/common/CustomModal.vue'
import { showSuccess, showError, showConfirm } from '@/components/common/Message.vue'

// 响应式数据
const loading = ref(false)
const roleModalVisible = ref(false)
const roleModalTitle = ref('')
const formLoading = ref(false)
const permissionLoading = ref(false)

// 搜索和列表
const searchKeyword = ref('')
const roleList = ref([])
const currentRole = ref(null)

// 角色表单
const roleForm = reactive({
  id: '',
  name: '',
  code: '',
  dataScope: '4',
  orderNum: 0,
  status: 1,
  remark: ''
})

const roleFormRef = ref<FormInstance>()

// 表单验证规则
const roleFormRules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '角色名称长度在 2-50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
  ],
  dataScope: [
    { required: true, message: '请选择数据权限', trigger: 'change' }
  ],
  orderNum: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ]
}

// 权限树
const permissionTreeRef = ref()
const permissionTreeData = ref([])

// 计算属性
const filteredRoles = computed(() => {
  if (!searchKeyword.value) {
    return roleList.value
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  return roleList.value.filter(role => 
    role.name.toLowerCase().includes(keyword) || 
    role.code.toLowerCase().includes(keyword)
  )
})

// 生命周期
onMounted(() => {
  loadRoles()
  loadPermissions()
})

// 方法
const loadRoles = async () => {
  try {
    loading.value = true
    // 模拟API调用
    roleList.value = [
      {
        id: '1',
        name: '系统管理员',
        code: 'ADMIN',
        dataScope: '1',
        userCount: 1,
        permissionCount: 50,
        orderNum: 1,
        status: 1,
        remark: '拥有系统所有权限',
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: '2',
        name: '财务人员',
        code: 'FINANCE',
        dataScope: '2',
        userCount: 5,
        permissionCount: 30,
        orderNum: 2,
        status: 1,
        remark: '负责财务报表和预算管理',
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: '3',
        name: '部门经理',
        code: 'MANAGER',
        dataScope: '3',
        userCount: 3,
        permissionCount: 40,
        orderNum: 3,
        status: 1,
        remark: '管理部门预算和审批',
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: '4',
        name: '普通员工',
        code: 'USER',
        dataScope: '4',
        userCount: 100,
        permissionCount: 20,
        orderNum: 4,
        status: 1,
        remark: '普通员工权限',
        createTime: '2024-01-01 10:00:00'
      }
    ]
  } catch (error) {
    showError('获取角色列表失败')
    console.error('获取角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadPermissions = async () => {
  try {
    // 模拟权限树数据
    permissionTreeData.value = [
      {
        id: '100',
        name: '系统管理',
        code: 'SYSTEM',
        type: 'menu',
        children: [
          {
            id: '101',
            name: '用户管理',
            code: 'USER_MANAGE',
            type: 'menu',
            api: '/api/user/**',
            children: [
              { id: '10101', name: '用户查询', code: 'USER_QUERY', type: 'button', api: '/api/user/list' },
              { id: '10102', name: '用户新增', code: 'USER_ADD', type: 'button', api: '/api/user/add' },
              { id: '10103', name: '用户修改', code: 'USER_EDIT', type: 'button', api: '/api/user/update' },
              { id: '10104', name: '用户删除', code: 'USER_DELETE', type: 'button', api: '/api/user/delete' }
            ]
          },
          {
            id: '102',
            name: '角色管理',
            code: 'ROLE_MANAGE',
            type: 'menu',
            api: '/api/role/**',
            children: [
              { id: '10201', name: '角色查询', code: 'ROLE_QUERY', type: 'button', api: '/api/role/list' },
              { id: '10202', name: '角色新增', code: 'ROLE_ADD', type: 'button', api: '/api/role/add' },
              { id: '10203', name: '角色修改', code: 'ROLE_EDIT', type: 'button', api: '/api/role/update' },
              { id: '10204', name: '角色删除', code: 'ROLE_DELETE', type: 'button', api: '/api/role/delete' }
            ]
          },
          {
            id: '103',
            name: '部门管理',
            code: 'DEPT_MANAGE',
            type: 'menu',
            api: '/api/dept/**',
            children: [
              { id: '10301', name: '部门查询', code: 'DEPT_QUERY', type: 'button', api: '/api/dept/list' },
              { id: '10302', name: '部门新增', code: 'DEPT_ADD', type: 'button', api: '/api/dept/add' },
              { id: '10303', name: '部门修改', code: 'DEPT_EDIT', type: 'button', api: '/api/dept/update' },
              { id: '10304', name: '部门删除', code: 'DEPT_DELETE', type: 'button', api: '/api/dept/delete' }
            ]
          }
        ]
      },
      {
        id: '200',
        name: '预算管理',
        code: 'BUDGET',
        type: 'menu',
        children: [
          { id: '201', name: '预算查询', code: 'BUDGET_QUERY', type: 'button', api: '/api/budget/list' },
          { id: '202', name: '预算新增', code: 'BUDGET_ADD', type: 'button', api: '/api/budget/add' },
          { id: '203', name: '预算修改', code: 'BUDGET_EDIT', type: 'button', api: '/api/budget/update' },
          { id: '204', name: '预算审核', code: 'BUDGET_APPROVE', type: 'button', api: '/api/budget/approve' }
        ]
      }
    ]
  } catch (error) {
    showError('获取权限列表失败')
    console.error('获取权限列表失败:', error)
  }
}

const handleAddRole = () => {
  roleModalTitle.value = '新增角色'
  resetRoleForm()
  roleModalVisible.value = true
}

const handleEditRole = (role: any) => {
  roleModalTitle.value = '编辑角色'
  Object.assign(roleForm, {
    id: role.id,
    name: role.name,
    code: role.code,
    dataScope: role.dataScope,
    orderNum: role.orderNum,
    status: role.status,
    remark: role.remark
  })
  roleModalVisible.value = true
}

const handleDeleteRole = async (role: any) => {
  if (role.userCount && role.userCount > 0) {
    showError('该角色已分配给用户，无法删除')
    return
  }
  
  try {
    const confirm = await showConfirm({
      message: `确定要删除角色 "${role.name}" 吗？此操作不可恢复。`,
      type: 'warning'
    })
    
    if (confirm) {
      // TODO: 调用删除API
      showSuccess('角色删除成功')
      loadRoles()
      if (currentRole.value?.id === role.id) {
        currentRole.value = null
      }
    }
  } catch (error) {
    console.error('删除角色失败:', error)
  }
}

const handleToggleStatus = async (role: any) => {
  const newStatus = role.status === 1 ? 0 : 1
  const action = role.status === 1 ? '禁用' : '启用'
  
  try {
    const confirm = await showConfirm({
      message: `确定要${action}角色 "${role.name}" 吗？`,
      type: 'warning'
    })
    
    if (confirm) {
      // TODO: 调用状态切换API
      showSuccess(`角色${action}成功`)
      loadRoles()
    }
  } catch (error) {
    console.error('切换角色状态失败:', error)
  }
}

const handleSelectRole = (role: any) => {
  currentRole.value = role
  // 加载该角色的权限设置
  loadRolePermissions(role.id)
}

const loadRolePermissions = async (roleId: string) => {
  // 模拟加载角色权限
  await nextTick()
  if (permissionTreeRef.value) {
    // 模拟设置选中的权限（这里应该从API获取）
    const checkedKeys = ['10101', '10102', '10201', '201']
    permissionTreeRef.value.setCheckedKeys(checkedKeys)
  }
}

const handleSavePermissions = async () => {
  if (!currentRole.value || !permissionTreeRef.value) return
  
  try {
    permissionLoading.value = true
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
    
    const permissionIds = [...checkedKeys, ...halfCheckedKeys]
    
    // TODO: 调用保存权限API
    showSuccess('权限保存成功')
  } catch (error) {
    console.error('保存权限失败:', error)
  } finally {
    permissionLoading.value = false
  }
}

const handleSelectAll = () => {
  if (permissionTreeRef.value) {
    permissionTreeRef.value.setCheckedKeys(permissionTreeData.value.map(getAllKeys).flat())
  }
}

const handleClearAll = () => {
  if (permissionTreeRef.value) {
    permissionTreeRef.value.setCheckedKeys([])
  }
}

const getAllKeys = (node: any): string[] => {
  const keys = [node.id]
  if (node.children) {
    node.children.forEach((child: any) => {
      keys.push(...getAllKeys(child))
    })
  }
  return keys
}

const handleRoleSubmit = async () => {
  if (!roleFormRef.value) return

  try {
    const valid = await roleFormRef.value.validate()
    if (!valid) return

    formLoading.value = true
    
    // TODO: 调用保存角色API
    showSuccess(roleForm.id ? '角色修改成功' : '角色添加成功')
    roleModalVisible.value = false
    loadRoles()
  } catch (error) {
    console.error('保存角色失败:', error)
  } finally {
    formLoading.value = false
  }
}

const handleRoleModalClosed = () => {
  roleFormRef.value?.clearValidate()
}

const handleSearch = () => {
  // 搜索逻辑已通过计算属性实现
}

const handleRefresh = () => {
  loadRoles()
  loadPermissions()
}

const resetRoleForm = () => {
  Object.assign(roleForm, {
    id: '',
    name: '',
    code: '',
    dataScope: '4',
    orderNum: 0,
    status: 1,
    remark: ''
  })
  roleFormRef.value?.clearValidate()
}
</script>

<style scoped lang="css">
.role-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.role-list {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex: 1;
}

.search-panel {
  margin-bottom: 20px;
}

.role-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.role-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}

.role-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.role-card-active {
  border-color: #409eff;
  background: #ecf5ff;
}

.role-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.role-name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.role-code {
  font-size: 12px;
  color: #909399;
}

.role-info {
  margin-bottom: 12px;
}

.role-stat {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 14px;
}

.stat-label {
  color: #606266;
}

.stat-value {
  color: #409eff;
  font-weight: 500;
}

.role-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
  line-height: 1.4;
}

.role-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px 0;
}

.permission-panel {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex: 2;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.panel-header h3 {
  margin: 0;
  color: #303133;
}

.panel-actions {
  display: flex;
  gap: 10px;
}

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.node-label {
  font-weight: 500;
}

.node-code {
  font-size: 12px;
  color: #909399;
}

.node-api {
  font-size: 12px;
  color: #67c23a;
  font-family: monospace;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .role-container {
    padding: 10px;
    gap: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    padding: 15px;
  }
  
  .page-actions,
  .panel-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .role-list,
  .permission-panel {
    padding: 15px;
  }
  
  .role-cards {
    grid-template-columns: 1fr;
  }
  
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>