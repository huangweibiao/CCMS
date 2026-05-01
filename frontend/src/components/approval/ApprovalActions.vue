<template>
  <div class="approval-actions">
    <!-- 基础操作按钮 -->
    <el-button-group>
      <el-button 
        type="success" 
        @click="showApproveDialog" 
        :disabled="!canApprove"
      >
        <el-icon><check /></el-icon>
        同意
      </el-button>
      <el-button 
        type="danger" 
        @click="showRejectDialog" 
        :disabled="!canReject"
      >
        <el-icon><close /></el-icon>
        驳回
      </el-button>
      <el-button 
        type="warning" 
        @click="showTransferDialog" 
        :disabled="!canTransfer"
      >
        <el-icon><share /></el-icon>
        转审
      </el-button>
    </el-button-group>

    <!-- 更多操作下拉菜单 -->
    <el-dropdown @command="handleMoreCommand" style="margin-left: 8px;">
      <el-button type="primary">
        更多操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="remind" :disabled="!canRemind">
            <el-icon><bell /></el-icon>
            催办申请人
          </el-dropdown-item>
          <el-dropdown-item command="follow" :disabled="!canFollow">
            <el-icon><star /></el-icon>
            {{ isFollowing ? '取消关注' : '关注此单' }}
          </el-dropdown-item>
          <el-dropdown-item command="print" divided>
            <el-icon><printer /></el-icon>
            打印申请单
          </el-dropdown-item>
          <el-dropdown-item command="export">
            <el-icon><download /></el-icon>
            导出详情
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <!-- 同意审批对话框 -->
    <el-dialog 
      v-model="approveDialogVisible" 
      title="同意审批" 
      width="500px"
      :before-close="closeApproveDialog"
    >
      <div class="approval-dialog">
        <div class="dialog-header">
          <div class="approval-info">
            <h4>{{ approval.title }}</h4>
            <p>申请金额：<span class="amount">¥{{ formatAmount(approval.amount) }}</span></p>
          </div>
        </div>
        
        <el-form :model="approveForm" :rules="approveRules" ref="approveFormRef">
          <el-form-item label="审批意见" prop="comment">
            <el-input
              v-model="approveForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入审批意见（可选）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="附件上传">
            <el-upload
              class="upload-demo"
              action="#"
              :on-preview="handlePreview"
              :on-remove="handleRemove"
              :before-remove="beforeRemove"
              :limit="3"
              :on-exceed="handleExceed"
              :file-list="approveForm.files"
            >
              <el-button type="primary">
                <el-icon><upload /></el-icon>
                点击上传
              </el-button>
              <template #tip>
                <div class="el-upload__tip">
                  可选上传审批相关附件，支持jpg/png/pdf/doc格式，单个文件不超过5MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
          
          <el-form-item label="审批模式">
            <el-radio-group v-model="approveForm.approvalMode">
              <el-radio label="NORMAL">普通审批</el-radio>
              <el-radio label="FAST">快速审批</el-radio>
            </el-radio-group>
            <div class="mode-tip">
              {{ approveForm.approvalMode === 'FAST' ? '快速审批将跳过部分验证，谨慎使用' : '按照标准流程进行审批' }}
            </div>
          </el-form-item>
        </el-form>
        
        <div class="dialog-footer">
          <el-button @click="closeApproveDialog">取消</el-button>
          <el-button type="success" @click="submitApprove" :loading="approveLoading">
            <el-icon><check /></el-icon>
            确认同意
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 驳回审批对话框 -->
    <el-dialog 
      v-model="rejectDialogVisible" 
      title="驳回审批" 
      width="500px"
      :before-close="closeRejectDialog"
    >
      <div class="approval-dialog">
        <div class="dialog-header">
          <div class="approval-info">
            <h4>{{ approval.title }}</h4>
            <p class="reject-warning">
              <el-icon><warning /></el-icon>
              驳回后申请人需要重新提交申请
            </p>
          </div>
        </div>
        
        <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef">
          <el-form-item label="驳回原因" prop="reason" required>
            <el-select v-model="rejectForm.reason" placeholder="请选择驳回原因">
              <el-option label="信息不完整" value="INCOMPLETE" />
              <el-option label="金额不合理" value="AMOUNT_INVALID" />
              <el-option label="不符合规定" value="RULE_VIOLATION" />
              <el-option label="预算不足" value="BUDGET_LIMIT" />
              <el-option label="其他原因" value="OTHER" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="详细说明" prop="comment" required>
            <el-input
              v-model="rejectForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请详细说明驳回理由，便于申请人理解并修正"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="驳回类型">
            <el-radio-group v-model="rejectForm.rejectType">
              <el-radio label="RETURN">返回修改</el-radio>
              <el-radio label="TERMINATE">直接终止</el-radio>
            </el-radio-group>
            <div class="type-tip">
              {{
                rejectForm.rejectType === 'RETURN' 
                  ? '申请人可以修改后重新提交' 
                  : '申请将被终止，无法再次提交'
              }}
            </div>
          </el-form-item>
          
          <el-form-item label="通知申请人">
            <el-switch v-model="rejectForm.notifyApplicant" />
            <span class="notify-tip">通过系统消息和邮件通知申请人</span>
          </el-form-item>
        </el-form>
        
        <div class="dialog-footer">
          <el-button @click="closeRejectDialog">取消</el-button>
          <el-button type="danger" @click="submitReject" :loading="rejectLoading">
            <el-icon><close /></el-icon>
            确认驳回
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 转审对话框 -->
    <el-dialog 
      v-model="transferDialogVisible" 
      title="转审处理" 
      width="600px"
      :before-close="closeTransferDialog"
    >
      <div class="approval-dialog">
        <div class="dialog-header">
          <div class="approval-info">
            <h4>{{ approval.title }}</h4>
            <p class="transfer-info">
              原审批人：<span class="current-approver">{{ currentApprover }}</span>
            </p>
          </div>
        </div>
        
        <el-form :model="transferForm" :rules="transferRules" ref="transferFormRef">
          <el-form-item label="转审对象" prop="targetUser" required>
            <el-select 
              v-model="transferForm.targetUser" 
              placeholder="请选择转审人员"
              filterable
              remote
              :remote-method="searchUsers"
              :loading="userSearchLoading"
            >
              <el-option 
                v-for="user in userList" 
                :key="user.id" 
                :label="user.name" 
                :value="user.id"
              >
                <div class="user-option">
                  <span class="user-name">{{ user.name }}</span>
                  <span class="user-dept">{{ user.department }}</span>
                  <span class="user-role">{{ user.role }}</span>
                </div>
              </el-option>
            </el-select>
            <div class="transfer-tip">请选择具有相应审批权限的人员进行转审</div>
          </el-form-item>
          
          <el-form-item label="转审原因" prop="reason" required>
            <el-input
              v-model="transferForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请输入转审原因"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="处理方式">
            <el-radio-group v-model="transferForm.transferType">
              <el-radio label="DELEGATE">委托转审</el-radio>
              <el-radio label="REDIRECT">重定向转审</el-radio>
            </el-radio-group>
            <div class="transfer-type-tip">
              {{
                transferForm.transferType === 'DELEGATE' 
                  ? '您仍然可以查看审批进度，但不再参与审批' 
                  : '完全将该任务转移给指定人员'
              }}
            </div>
          </el-form-item>
          
          <el-form-item label="紧急程度">
            <el-select v-model="transferForm.urgency">
              <el-option label="普通" value="LOW" />
              <el-option label="较急" value="MEDIUM" />
              <el-option label="紧急" value="HIGH" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="截止时间">
            <el-date-picker
              v-model="transferForm.deadline"
              type="datetime"
              placeholder="选择截止时间"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-form>
        
        <div class="dialog-footer">
          <el-button @click="closeTransferDialog">取消</el-button>
          <el-button type="warning" @click="submitTransfer" :loading="transferLoading">
            <el-icon><share /></el-icon>
            确认转审
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 催办对话框 -->
    <el-dialog 
      v-model="remindDialogVisible" 
      title="催办通知" 
      width="400px"
    >
      <div class="remind-dialog">
        <p>确定要向申请人 <strong>{{ approval.applicant }}</strong> 发送催办通知吗？</p>
        <el-input
          v-model="remindMessage"
          type="textarea"
          :rows="3"
          placeholder="可输入催办消息内容（可选）"
          maxlength="200"
          show-word-limit
        />
      </div>
      
      <template #footer>
        <el-button @click="remindDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRemind">确认发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { 
  Check, Close, Share, ArrowDown, Bell, Star, Printer, Download, Upload, Warning 
} from '@element-plus/icons-vue'
import { showSuccess, showError, showConfirm } from '@/utils/message''
import type { FormInstance, FormRules, UploadFile } from 'element-plus'

// 组件属性
interface Props {
  approval: any
}

const props = defineProps<Props>()

// 组件事件
const emit = defineEmits<{
  approve: [data: any]
  reject: [data: any, reason: string]
  transfer: [data: any, target: string, reason: string]
}>()

// 响应式数据
const approveDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const transferDialogVisible = ref(false)
const remindDialogVisible = ref(false)

const approveLoading = ref(false)
const rejectLoading = ref(false)
const transferLoading = ref(false)
const userSearchLoading = ref(false)

const isFollowing = ref(false)
const remindMessage = ref('')

// 表单引用
const approveFormRef = ref<FormInstance>()
const rejectFormRef = ref<FormInstance>()
const transferFormRef = ref<FormInstance>()

// 表单数据
const approveForm = reactive({
  comment: '',
  files: [],
  approvalMode: 'NORMAL'
})

const rejectForm = reactive({
  reason: '',
  comment: '',
  rejectType: 'RETURN',
  notifyApplicant: true
})

const transferForm = reactive({
  targetUser: '',
  reason: '',
  transferType: 'DELEGATE',
  urgency: 'MEDIUM',
  deadline: ''
})

// 用户列表（模拟数据）
const userList = ref([
  { id: '1', name: '张主管', department: '技术部', role: '部门主管' },
  { id: '2', name: '李经理', department: '财务部', role: '财务经理' },
  { id: '3', name: '王总', department: '总经办', role: '总经理' },
  { id: '4', name: '赵总监', department: '市场部', role: '市场总监' },
  { id: '5', name: '钱科长', department: '行政部', role: '行政科长' }
])

// 验证规则
const approveRules: FormRules = {
  comment: [
    { max: 500, message: '审批意见不能超过500字', trigger: 'blur' }
  ]
}

const rejectRules: FormRules = {
  reason: [
    { required: true, message: '请选择驳回原因', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入驳回说明', trigger: 'blur' },
    { min: 10, message: '驳回说明至少10字', trigger: 'blur' }
  ]
}

const transferRules: FormRules = {
  targetUser: [
    { required: true, message: '请选择转审对象', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入转审原因', trigger: 'blur' }
  ]
}

// 计算属性
const canApprove = computed(() => {
  return props.approval?.status === 'PENDING'
})

const canReject = computed(() => {
  return props.approval?.status === 'PENDING'
})

const canTransfer = computed(() => {
  return props.approval?.status === 'PENDING'
})

const canRemind = computed(() => {
  return true // 只要有申请人都可以催办
})

const canFollow = computed(() => {
  return true // 都可以关注
})

const currentApprover = computed(() => {
  return props.approval?.currentApprover || '待分配'
})

// 方法定义
const showApproveDialog = () => {
  approveForm.comment = ''
  approveForm.files = []
  approveForm.approvalMode = 'NORMAL'
  approveDialogVisible.value = true
}

const closeApproveDialog = () => {
  approveDialogVisible.value = false
  approveFormRef.value?.clearValidate()
}

const showRejectDialog = () => {
  rejectForm.reason = ''
  rejectForm.comment = ''
  rejectForm.rejectType = 'RETURN'
  rejectForm.notifyApplicant = true
  rejectDialogVisible.value = true
}

const closeRejectDialog = () => {
  rejectDialogVisible.value = false
  rejectFormRef.value?.clearValidate()
}

const showTransferDialog = () => {
  transferForm.targetUser = ''
  transferForm.reason = ''
  transferForm.transferType = 'DELEGATE'
  transferForm.urgency = 'MEDIUM'
  transferForm.deadline = ''
  transferDialogVisible.value = true
}

const closeTransferDialog = () => {
  transferDialogVisible.value = false
  transferFormRef.value?.clearValidate()
}

const submitApprove = async () => {
  if (!approveFormRef.value) return
  
  const valid = await approveFormRef.value.validate()
  if (!valid) return
  
  approveLoading.value = true
  try {
    // TODO: 调用API提交同意审批
    await new Promise(resolve => setTimeout(resolve, 800))
    
    emit('approve', {
      ...props.approval,
      comment: approveForm.comment,
      files: approveForm.files,
      approvalMode: approveForm.approvalMode
    })
    
    closeApproveDialog()
    showSuccess('审批已同意')
    
  } catch (error) {
    console.error('同意审批失败:', error)
    showError('同意审批失败')
  } finally {
    approveLoading.value = false
  }
}

const submitReject = async () => {
  if (!rejectFormRef.value) return
  
  const valid = await rejectFormRef.value.validate()
  if (!valid) return
  
  rejectLoading.value = true
  try {
    // TODO: 调用API提交驳回审批
    await new Promise(resolve => setTimeout(resolve, 800))
    
    emit('reject', props.approval, rejectForm.comment)
    
    closeRejectDialog()
    showSuccess('审批已驳回')
    
  } catch (error) {
    console.error('驳回审批失败:', error)
    showError('驳回审批失败')
  } finally {
    rejectLoading.value = false
  }
}

const submitTransfer = async () => {
  if (!transferFormRef.value) return
  
  const valid = await transferFormRef.value.validate()
  if (!valid) return
  
  transferLoading.value = true
  try {
    // TODO: 调用API提交转审
    await new Promise(resolve => setTimeout(resolve, 800))
    
    emit('transfer', props.approval, transferForm.targetUser, transferForm.reason)
    
    closeTransferDialog()
    showSuccess('转审已提交')
    
  } catch (error) {
    console.error('转审失败:', error)
    showError('转审失败')
  } finally {
    transferLoading.value = false
  }
}

const handleMoreCommand = (command: string) => {
  switch (command) {
    case 'remind':
      remindDialogVisible.value = true
      break
    case 'follow':
      toggleFollow()
      break
    case 'print':
      handlePrint()
      break
    case 'export':
      handleExport()
      break
  }
}

const submitRemind = async () => {
  try {
    // TODO: 调用催办API
    await new Promise(resolve => setTimeout(resolve, 300))
    
    remindDialogVisible.value = false
    showSuccess('催办通知已发送')
    
  } catch (error) {
    console.error('催办失败:', error)
    showError('催办失败')
  }
}

const toggleFollow = async () => {
  try {
    // TODO: 调用关注API
    await new Promise(resolve => setTimeout(resolve, 200))
    
    isFollowing.value = !isFollowing.value
    showSuccess(isFollowing.value ? '已关注此申请单' : '已取消关注')
    
  } catch (error) {
    console.error('关注操作失败:', error)
    showError('操作失败')
  }
}

const handlePrint = () => {
  // TODO: 实现打印功能
  showSuccess('打印功能开发中')
}

const handleExport = () => {
  // TODO: 实现导出功能
  showSuccess('导出功能开发中')
}

const searchUsers = (query: string) => {
  if (query) {
    userSearchLoading.value = true
    setTimeout(() => {
      userList.value = userList.value.filter(user => 
        user.name.includes(query) || 
        user.department.includes(query) ||
        user.role.includes(query)
      )
      userSearchLoading.value = false
    }, 300)
  } else {
    userList.value = []
  }
}

// 文件上传相关方法
const handlePreview = (file: UploadFile) => {
  console.log('预览文件:', file)
  // TODO: 实现文件预览
  showSuccess('文件预览功能开发中')
}

const handleRemove = (file: UploadFile) => {
  console.log('移除文件:', file)
  approveForm.files = approveForm.files.filter((f: any) => f.uid !== file.uid)
}

const beforeRemove = () => {
  return showConfirm('确定要移除这个文件吗？')
}

const handleExceed = () => {
  showError('最多只能上传3个文件')
}

// 辅助方法
const formatAmount = (amount: number) => {
  return amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'
}
</script>

<style scoped lang="css">
.approval-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.approval-dialog {
  padding: 0 8px;
}

.dialog-header {
  margin-bottom: 20px;
}

.approval-info h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
}

.approval-info p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
}

.reject-warning {
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 4px;
}

.transfer-info {
  color: #606266;
}

.current-approver {
  font-weight: 600;
  color: #409eff;
}

.mode-tip,
.type-tip,
.notify-tip,
.transfer-tip,
.transfer-type-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.user-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.user-dept,
.user-role {
  font-size: 12px;
  color: #909399;
}

.remind-dialog {
  text-align: center;
}

.remind-dialog p {
  margin-bottom: 16px;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .approval-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .approval-actions .el-button-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .approval-actions .el-dropdown {
    margin-left: 0;
    margin-top: 8px;
  }
}

@media (max-width: 480px) {
  .dialog-footer {
    flex-direction: column;
  }
  
  .dialog-footer .el-button {
    width: 100%;
    margin-bottom: 8px;
  }
}
</style>