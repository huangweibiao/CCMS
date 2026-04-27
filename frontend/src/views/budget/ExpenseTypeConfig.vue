<template>
  <div class="expense-type-config-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>费用类型配置</h2>
      <p>管理系统中的费用类型和科目设置</p>
    </div>

    <div class="expense-type-content">
      <!-- 操作工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAddCategory">
            <el-icon><plus /></el-icon>
            新增费用大类
          </el-button>
          <el-button type="success" @click="handleAddExpenseType">
            <el-icon><plus /></el-icon>
            新增费用类型
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索费用类型" 
            style="width: 200px"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 费用类型树形结构 -->
      <div class="type-tree-container">
        <div class="tree-panel">
          <div class="tree-header">
            <span>费用类型结构</span>
            <div class="tree-actions">
              <el-button type="text" size="small" @click="expandAll">
                <el-icon><expand /></el-icon>
                展开全部
              </el-button>
              <el-button type="text" size="small" @click="collapseAll">
                <el-icon><fold /></el-icon>
                折叠全部
              </el-button>
            </div>
          </div>
          <el-tree
            ref="typeTreeRef"
            :data="expenseTypeTree"
            :props="treeProps"
            :expand-on-click-node="false"
            :highlight-current="true"
            node-key="id"
            default-expand-all
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <div class="node-content">
                  <div class="node-info">
                    <span class="node-label" :class="{ 'category-node': data.type === 'CATEGORY' }">
                      {{ node.label }}
                    </span>
                    <span v-if="data.type === 'EXPENSE_TYPE'" class="type-code">
                      {{ data.typeCode }}
                    </span>
                  </div>
                  <div class="node-actions">
                    <el-button v-if="data.type === 'CATEGORY'" type="text" size="small" @click.stop="handleAddChildCategory(data)">
                      <el-icon><folder-add /></el-icon>
                    </el-button>
                    <el-button type="text" size="small" @click.stop="handleEdit(data)">
                      <el-icon><edit /></el-icon>
                    </el-button>
                    <el-button v-if="!data.children || data.children.length === 0" type="text" size="small" @click.stop="handleDelete(data)">
                      <el-icon><delete /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
            </template>
          </el-tree>
        </div>

        <!-- 详情面板 -->
        <div class="detail-panel">
          <div v-if="currentNode" class="detail-content">
            <div class="detail-header">
              <h3>{{ currentNode.name }}</h3>
              <div class="detail-actions">
                <el-button type="primary" size="small" @click="handleEdit(currentNode)">
                  <el-icon><edit /></el-icon>
                  编辑
                </el-button>
                <el-button v-if="!currentNode.children || currentNode.children.length === 0" type="danger" size="small" @click="handleDelete(currentNode)">
                  <el-icon><delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
            
            <div class="detail-info">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="类型代码" v-if="currentNode.typeCode">
                  {{ currentNode.typeCode }}
                </el-descriptions-item>
                <el-descriptions-item label="类型标识">
                  <el-tag :type="currentNode.type === 'CATEGORY' ? 'primary' : 'success'">
                    {{ currentNode.type === 'CATEGORY' ? '费用大类' : '费用类型' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="currentNode.status === 'ENABLED' ? 'success' : 'danger'">
                    {{ currentNode.status === 'ENABLED' ? '启用' : '禁用' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ currentNode.createTime }}
                </el-descriptions-item>
                <el-descriptions-item label="备注" v-if="currentNode.remark">
                  {{ currentNode.remark }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
          
          <div v-else class="empty-detail">
            <el-empty description="请选择一个费用类型查看详情" />
          </div>
        </div>
      </div>

      <!-- 新增/编辑弹窗 -->
      <CustomModal
        v-model:visible="dialogVisible"
        :title="dialogTitle"
        width="600px"
        @confirm="handleDialogConfirm"
        @cancel="handleDialogCancel"
      >
        <el-form 
          ref="formRef" 
          :model="formData" 
          :rules="formRules" 
          label-width="100px"
          class="expense-type-form"
        >
          <el-form-item label="父级类型" v-if="formData.type === 'EXPENSE_TYPE' || formData.type === 'CHILD_CATEGORY'">
            <el-cascader
              v-model="formData.parentId"
              :options="parentOptions"
              :props="{ value: 'id', label: 'name', children: 'children' }"
              placeholder="选择父级费用大类"
              style="width: 100%"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入费用类型名称" />
          </el-form-item>
          
          <el-form-item label="类型代码" prop="typeCode" v-if="formData.type === 'EXPENSE_TYPE'">
            <el-input v-model="formData.typeCode" placeholder="请输入费用类型代码" />
          </el-form-item>
          
          <el-form-item label="类型级别" v-if="formData.type === 'CATEGORY'">
            <el-radio-group v-model="formData.level">
              <el-radio :label="1">一级大类</el-radio>
              <el-radio :label="2">二级子类</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio label="ENABLED">启用</el-radio>
              <el-radio label="DISABLED">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="排序" prop="sortOrder">
            <el-input-number v-model="formData.sortOrder" :min="1" :max="999" />
          </el-form-item>
          
          <el-form-item label="备注">
            <el-input 
              v-model="formData.remark" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入备注信息" 
            />
          </el-form-item>
        </el-form>
      </CustomModal>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Plus, Search, Expand, Fold, Edit, Delete, FolderAdd } from '@element-plus/icons-vue'
import CustomModal from '@/components/common/CustomModal.vue'
import { showSuccess, showWarning, showError } from '@/components/common/Message.vue'

// 响应式数据
const typeTreeRef = ref()
const formRef = ref()
const searchKeyword = ref('')

// 当前选中的节点
const currentNode = ref<any>(null)

// 弹窗控制
const dialogVisible = ref(false)
const dialogType = ref('') // 'category' | 'expenseType' | 'edit'
const dialogTitle = ref('')

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  type: '', // 'CATEGORY' | 'EXPENSE_TYPE'
  typeCode: '',
  parentId: [],
  level: 1,
  status: 'ENABLED',
  sortOrder: 1,
  remark: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  typeCode: [
    { required: true, message: '请输入类型代码', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]+$/, message: '只能包含大写字母、数字和下划线', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 费用类型树数据
const expenseTypeTree = ref([
  {
    id: '1',
    name: '行政费用',
    type: 'CATEGORY',
    status: 'ENABLED',
    sortOrder: 1,
    createTime: '2024-01-01 10:00:00',
    children: [
      {
        id: '101',
        name: '办公室租赁',
        type: 'EXPENSE_TYPE',
        typeCode: 'OFFICE_RENT',
        status: 'ENABLED',
        sortOrder: 1,
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: '102',
        name: '物业管理费',
        type: 'EXPENSE_TYPE',
        typeCode: 'PROPERTY_MANAGEMENT',
        status: 'ENABLED',
        sortOrder: 2,
        createTime: '2024-01-01 10:00:00'
      }
    ]
  },
  {
    id: '2',
    name: '差旅费用',
    type: 'CATEGORY',
    status: 'ENABLED',
    sortOrder: 2,
    createTime: '2024-01-01 10:00:00',
    children: [
      {
        id: '201',
        name: '交通费用',
        type: 'EXPENSE_TYPE',
        typeCode: 'TRAVEL_TRANSPORT',
        status: 'ENABLED',
        sortOrder: 1,
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: '202',
        name: '住宿费用',
        type: 'EXPENSE_TYPE',
        typeCode: 'TRAVEL_HOTEL',
        status: 'ENABLED',
        sortOrder: 2,
        createTime: '2024-01-01 10:00:00'
      }
    ]
  },
  {
    id: '3',
    name: '研发费用',
    type: 'CATEGORY',
    status: 'ENABLED',
    sortOrder: 3,
    createTime: '2024-01-01 10:00:00',
    children: [
      {
        id: '301',
        name: '软件开发工具',
        type: 'EXPENSE_TYPE',
        typeCode: 'DEV_TOOLS',
        status: 'ENABLED',
        sortOrder: 1,
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: '302',
        name: '硬件设备采购',
        type: 'EXPENSE_TYPE',
        typeCode: 'DEV_HARDWARE',
        status: 'ENABLED',
        sortOrder: 2,
        createTime: '2024-01-01 10:00:00'
      }
    ]
  }
])

// 树形配置
const treeProps = {
  label: 'name',
  children: 'children'
}

// 父级选项（用于表单级联选择）
const parentOptions = ref(expenseTypeTree.value.filter(item => item.type === 'CATEGORY'))

// 方法定义
const handleAddCategory = () => {
  dialogType.value = 'category'
  dialogTitle.value = '新增费用大类'
  resetForm()
  formData.type = 'CATEGORY'
  formData.level = 1
  dialogVisible.value = true
}

const handleAddExpenseType = () => {
  dialogType.value = 'expenseType'
  dialogTitle.value = '新增费用类型'
  resetForm()
  formData.type = 'EXPENSE_TYPE'
  dialogVisible.value = true
}

const handleAddChildCategory = (parent: any) => {
  dialogType.value = 'category'
  dialogTitle.value = '新增子费用大类'
  resetForm()
  formData.type = 'CATEGORY'
  formData.level = parent.level + 1
  formData.parentId = [parent.id]
  dialogVisible.value = true
}

const handleEdit = (node: any) => {
  dialogType.value = 'edit'
  dialogTitle.value = '编辑费用类型'
  
  // 填充表单数据
  Object.assign(formData, {
    id: node.id,
    name: node.name,
    type: node.type,
    typeCode: node.typeCode || '',
    parentId: node.parentId ? [node.parentId] : [],
    status: node.status,
    sortOrder: node.sortOrder,
    remark: node.remark || ''
  })
  
  dialogVisible.value = true
}

const handleDelete = async (node: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${node.name}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // TODO: 调用删除API
    showSuccess(`费用类型"${node.name}"已删除`)
    await loadExpenseTypes()
    currentNode.value = null
  } catch (error) {
    // 用户取消删除
  }
}

const handleNodeClick = (data: any) => {
  currentNode.value = data
}

const handleSearch = () => {
  // TODO: 实现搜索功能
  console.log('搜索关键词:', searchKeyword.value)
}

const expandAll = () => {
  expenseTypeTree.value.forEach((node: any) => {
    typeTreeRef.value.store.nodesMap[node.id].expanded = true
  })
}

const collapseAll = () => {
  expenseTypeTree.value.forEach((node: any) => {
    typeTreeRef.value.store.nodesMap[node.id].expanded = false
  })
}

const resetForm = () => {
  Object.assign(formData, {
    id: '',
    name: '',
    type: '',
    typeCode: '',
    parentId: [],
    level: 1,
    status: 'ENABLED',
    sortOrder: 1,
    remark: ''
  })
}

const handleDialogConfirm = async () => {
  try {
    await formRef.value.validate()
    
    // TODO: 调用API保存数据
    const isEdit = dialogType.value === 'edit'
    await new Promise(resolve => setTimeout(resolve, 500))
    
    showSuccess(isEdit ? '费用类型更新成功' : '费用类型创建成功')
    dialogVisible.value = false
    await loadExpenseTypes()
  } catch (error) {
    showError('表单验证失败，请检查输入')
  }
}

const handleDialogCancel = () => {
  dialogVisible.value = false
  resetForm()
}

const loadExpenseTypes = async () => {
  // TODO: 调用API加载费用类型数据
  // 暂时使用模拟数据
  console.log('加载费用类型数据')
}

onMounted(() => {
  loadExpenseTypes()
})
</script>

<style scoped lang="css">
.expense-type-config-container {
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

.expense-type-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
}

.type-tree-container {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  height: 600px;
}

.tree-panel {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #f8f9fa;
}

.tree-header span {
  font-weight: 600;
  color: #303133;
}

.tree-actions {
  display: flex;
  gap: 8px;
}

.tree-node {
  width: 100%;
  padding: 4px 0;
}

.node-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.node-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.node-label {
  font-size: 14px;
}

.category-node {
  font-weight: 600;
  color: #409eff;
}

.type-code {
  font-size: 12px;
  color: #909399;
  background: #f4f4f5;
  padding: 1px 6px;
  border-radius: 4px;
}

.node-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

.detail-panel {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.detail-content {
  padding: 20px;
  height: 100%;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.detail-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.detail-actions {
  display: flex;
  gap: 10px;
}

.detail-info {
  margin-top: 20px;
}

.empty-detail {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.expense-type-form {
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .type-tree-container {
    grid-template-columns: 1fr;
    height: auto;
  }
  
  .tree-panel {
    height: 400px;
  }
  
  .detail-panel {
    height: 300px;
  }
}

@media (max-width: 768px) {
  .expense-type-config-container {
    padding: 10px;
  }
  
  .toolbar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .toolbar-left {
    justify-content: center;
  }
  
  .toolbar-right {
    justify-content: center;
  }
}
</style>