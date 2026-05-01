<template>
  <div class="approval-config-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>审批流配置</h2>
      <p>管理系统各类申请的审批流程配置</p>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><plus /></el-icon>
          新建审批流
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" inline>
        <el-form-item label="审批类型">
          <el-select v-model="filterForm.approvalType" placeholder="选择审批类型" clearable>
            <el-option 
              v-for="type in approvalTypes" 
              :key="type.value" 
              :label="type.label" 
              :value="type.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="filterForm.keyword"
            placeholder="输入审批流名称或描述"
            clearable
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 审批流列表 -->
    <div class="config-list-section">
      <el-table 
        :data="configList" 
        v-loading="loading"
        row-key="id"
        style="width: 100%"
      >
        <el-table-column label="审批流名称" min-width="200" prop="name">
          <template #default="{ row }">
            <div class="config-name">
              <span class="name-text">{{ row.name }}</span>
              <el-tag v-if="row.isDefault" size="small" type="success">默认</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="审批类型" width="120" prop="type">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批节点" width="120" prop="nodeCount">
          <template #default="{ row }">
            <span>{{ row.nodeCount }} 个节点</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" prop="status">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="'ENABLED'"
              :inactive-value="'DISABLED'"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="条件规则" width="150" prop="conditionCount">
          <template #default="{ row }">
            <span>{{ row.conditionCount || 0 }} 条规则</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" prop="createTime">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="160" prop="updateTime">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><edit /></el-icon>
              编辑
            </el-button>
            <el-button type="info" link size="small" @click="handlePreview(row)">
              <el-icon><view /></el-icon>
              预览
            </el-button>
            <el-button 
              type="success" 
              link 
              size="small" 
              @click="handleSetDefault(row)"
              v-if="!row.isDefault"
            >
              <el-icon><check /></el-icon>
              设为默认
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 流程图预览模态框 -->
      <el-dialog v-model="previewVisible" title="审批流预览" width="80%">
        <div class="flow-preview">
          <div class="flow-chart">
            <!-- 简单的流程图展示 -->
            <div class="flow-item start">
              <div class="flow-node">
                <el-icon><play /></el-icon>
                <span>提交申请</span>
              </div>
              <div class="flow-arrow"></div>
            </div>
            
            <div v-for="node in previewNodes" :key="node.id" class="flow-item">
              <div class="flow-node" :class="{ 'current': node.isCurrent }">
                <el-icon><user /></el-icon>
                <div class="node-content">
                  <div class="node-title">{{ node.name }}</div>
                  <div class="node-desc">{{ node.role }} ({{ node.type }})</div>
                </div>
                <div class="node-actions">
                  <el-tag size="small" v-if="node.multiSign">会签</el-tag>
                </div>
              </div>
              <div class="flow-arrow" v-if="!node.isLast"></div>
            </div>
            
            <div class="flow-item end">
              <div class="flow-node">
                <el-icon><check /></el-icon>
                <span>审批完成</span>
              </div>
            </div>
          </div>
          
          <div class="flow-info">
            <h4>流程信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">审批类型：</span>
                <span class="value">{{ getTypeText(previewFlow.type) }}</span>
              </div>
              <div class="info-item">
                <span class="label">节点总数：</span>
                <span class="value">{{ previewNodes.length }}</span>
              </div>
              <div class="info-item">
                <span class="label">会签节点：</span>
                <span class="value">{{ previewNodes.filter(n => n.multiSign).length }}</span>
              </div>
              <div class="info-item">
                <span class="label">审批模式：</span>
                <span class="value">顺序审批</span>
              </div>
            </div>
          </div>
        </div>
        
        <template #footer>
          <el-button @click="previewVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleEdit(previewFlow)">编辑此流程</el-button>
        </template>
      </el-dialog>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, Refresh, Edit, View, Check, Delete, Play, User } from '@element-plus/icons-vue'
import { showSuccess, showError, showConfirm } from '@/utils/message''

// 路由管理
const router = useRouter()

// 筛选表单
const filterForm = reactive({
  approvalType: '',
  status: '',
  keyword: ''
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 审批类型列表
const approvalTypes = [
  { label: '费用申请', value: 'EXPENSE_APPLY' },
  { label: '费用报销', value: 'EXPENSE_REIMBURSE' },
  { label: '预算申请', value: 'BUDGET_APPLY' },
  { label: '请假申请', value: 'LEAVE_APPLY' },
  { label: '加班申请', value: 'OVERTIME_APPLY' },
  { label: '采购申请', value: 'PURCHASE_APPLY' }
]

// 审批流列表数据
const configList = ref<any[]>([])
const loading = ref(false)

// 预览相关
const previewVisible = ref(false)
const previewFlow = ref<any>({})
const previewNodes = ref<any[]>([])

// 类型映射
const typeMap = {
  EXPENSE_APPLY: { text: '费用申请', color: 'primary' },
  EXPENSE_REIMBURSE: { text: '费用报销', color: 'success' },
  BUDGET_APPLY: { text: '预算申请', color: 'warning' },
  LEAVE_APPLY: { text: '请假申请', color: 'info' },
  OVERTIME_APPLY: { text: '加班申请', color: 'warning' },
  PURCHASE_APPLY: { text: '采购申请', color: 'danger' }
}

// 生命周期
onMounted(() => {
  loadConfigList()
})

// 方法定义
const loadConfigList = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取审批流配置列表
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 模拟数据
    configList.value = [
      {
        id: '1',
        name: '普通费用申请审批流程',
        type: 'EXPENSE_APPLY',
        description: '适用于单笔金额5000元以下的费用申请',
        nodeCount: 3,
        status: 'ENABLED',
        isDefault: true,
        conditionCount: 2,
        createTime: '2024-01-10 09:00:00',
        updateTime: '2024-01-15 14:30:00'
      },
      {
        id: '2',
        name: '大额费用申请审批流程',
        type: 'EXPENSE_APPLY',
        description: '适用于单笔金额5000元以上的费用申请',
        nodeCount: 5,
        status: 'ENABLED',
        isDefault: false,
        conditionCount: 3,
        createTime: '2024-01-12 10:15:00',
        updateTime: '2024-01-16 16:45:00'
      },
      {
        id: '3',
        name: '标准费用报销流程',
        type: 'EXPENSE_REIMBURSE',
        description: '适用于普通费用报销和借款核销',
        nodeCount: 4,
        status: 'ENABLED',
        isDefault: true,
        conditionCount: 1,
        createTime: '2024-01-08 14:20:00',
        updateTime: '2024-01-14 11:10:00'
      },
      {
        id: '4',
        name: '预算申请审批流程',
        type: 'BUDGET_APPLY',
        description: '适用于部门预算申请和调整',
        nodeCount: 4,
        status: 'DISABLED',
        isDefault: true,
        conditionCount: 0,
        createTime: '2024-01-05 15:30:00',
        updateTime: '2024-01-18 09:25:00'
      },
      {
        id: '5',
        name: '紧急请假审批流程',
        type: 'LEAVE_APPLY',
        description: '适用于紧急请假申请的快速审批通道',
        nodeCount: 2,
        status: 'ENABLED',
        isDefault: false,
        conditionCount: 1,
        createTime: '2024-01-15 16:40:00',
        updateTime: '2024-01-17 13:20:00'
      }
    ]
    
    pagination.total = configList.value.length
    
  } catch (error) {
    console.error('加载审批流配置失败:', error)
    showError('加载审批流配置失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadConfigList()
}

const handleReset = () => {
  Object.assign(filterForm, {
    approvalType: '',
    status: '',
    keyword: ''
  })
  pagination.current = 1
  loadConfigList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadConfigList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadConfigList()
}

const handleCreate = () => {
  console.log('创建新审批流')
  // TODO: 跳转到编辑页面
  showSuccess('新建审批流功能开发中')
}

const handleEdit = (row: any) => {
  console.log('编辑审批流:', row)
  // TODO: 跳转到编辑页面
  router.push(`/approval/config/edit/${row.id}`)
}

const handlePreview = (row: any) => {
  console.log('预览审批流:', row)
  previewFlow.value = row
  
  // 模拟审批节点数据
  previewNodes.value = [
    { id: '1', name: '部门主管审批', role: '部门主管', type: '必审', multiSign: false, isCurrent: false },
    { id: '2', name: '财务审批', role: '财务人员', type: '会签', multiSign: true, isCurrent: true },
    { id: '3', name: '总经理审批', role: '总经理', type: '或审', multiSign: false, isCurrent: false, isLast: row.nodeCount === 3 }
  ]
  
  if (row.nodeCount > 3) {
    previewNodes.value.push(
      { id: '4', name: '董事长审批', role: '董事长', type: '必审', multiSign: false, isCurrent: false, isLast: true }
    )
  }
  
  previewVisible.value = true
}

const handleSetDefault = async (row: any) => {
  try {
    await showConfirm(`确定要将"${row.name}"设为默认审批流吗？`)
    
    // TODO: 调用API设置为默认
    await new Promise(resolve => setTimeout(resolve, 300))
    
    showSuccess('已设置为默认审批流')
    loadConfigList()
    
  } catch (error) {
    // 用户取消操作
  }
}

const handleDelete = async (row: any) => {
  try {
    await showConfirm(`确定要删除审批流"${row.name}"吗？此操作不可恢复。`)
    
    // TODO: 调用删除API
    await new Promise(resolve => setTimeout(resolve, 300))
    
    showSuccess('审批流已删除')
    loadConfigList()
    
  } catch (error) {
    // 用户取消操作
  }
}

const handleStatusChange = async (row: any) => {
  const newStatus = row.status === 'ENABLED' ? '启用' : '禁用'
  
  try {
    await showConfirm(`确定要${newStatus}"${row.name}"审批流吗？`)
    
    // TODO: 调用状态更新API
    await new Promise(resolve => setTimeout(resolve, 300))
    
    showSuccess(`审批流已${newStatus}`)
    
  } catch (error) {
    // 操作取消，恢复原来的状态
    row.status = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  }
}

const getTypeText = (type: string) => {
  return typeMap[type]?.text || '未知类型'
}

const getTypeColor = (type: string) => {
  return typeMap[type]?.color || 'info'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 16)
}
</script>

<style scoped lang="css">
.approval-config-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.filter-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.config-list-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.config-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-weight: 500;
  color: #303133;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

/* 流程图预览样式 */
.flow-preview {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.flow-chart {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.flow-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.flow-node {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  background: white;
  min-width: 280px;
  transition: all 0.3s ease;
}

.flow-node.start {
  border-color: #67c23a;
  background: #f0f9ff;
}

.flow-node.end {
  border-color: #409eff;
  background: #f0f9ff;
}

.flow-node.current {
  border-color: #e6a23c;
  background: #fdf6ec;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.2);
}

.flow-node .el-icon {
  font-size: 20px;
  color: #409eff;
}

.flow-node.start .el-icon {
  color: #67c23a;
}

.flow-node.end .el-icon {
  color: #409eff;
}

.flow-node.current .el-icon {
  color: #e6a23c;
}

.node-content {
  flex: 1;
}

.node-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.node-desc {
  font-size: 12px;
  color: #909399;
}

.node-actions {
  margin-left: auto;
}

.flow-arrow {
  width: 2px;
  height: 20px;
  background: #e4e7ed;
  position: relative;
}

.flow-arrow::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: -4px;
  width: 10px;
  height: 10px;
  border-right: 2px solid #e4e7ed;
  border-bottom: 2px solid #e4e7ed;
  transform: rotate(45deg);
}

.flow-info {
  border-left: 1px solid #e4e7ed;
  padding-left: 20px;
}

.flow-info h4 {
  margin: 0 0 16px 0;
  color: #303133;
}

.info-grid {
  display: grid;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item .label {
  color: #909399;
  font-size: 14px;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .approval-config-container {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .filter-section,
  .config-list-section {
    padding: 15px;
  }
  
  .flow-preview {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  
  .flow-info {
    border-left: none;
    border-top: 1px solid #e4e7ed;
    padding-left: 0;
    padding-top: 20px;
  }
}

@media (max-width: 480px) {
  .filter-section .el-form-item {
    margin-bottom: 12px;
    width: 100%;
  }
  
  .filter-section .el-form-item__content {
    width: 100%;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table .el-button) {
    font-size: 12px;
    padding: 4px 8px;
  }
}
</style>