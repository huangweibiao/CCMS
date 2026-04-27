<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :fullscreen="fullscreen"
    :top="top"
    :modal="modal"
    :append-to-body="appendToBody"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    :show-close="showClose"
    :before-close="handleBeforeClose"
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleClose"
    @closed="handleClosed"
  >
    <!-- 模态框内容 -->
    <div class="modal-content">
      <slot></slot>
    </div>

    <!-- 自定义底部区域 -->
    <template v-if="!hideFooter" #footer>
      <span class="dialog-footer">
        <slot name="footer">
          <el-button @click="handleCancel" :loading="cancelLoading">
            {{ cancelText }}
          </el-button>
          <el-button 
            type="primary" 
            @click="handleConfirm" 
            :loading="confirmLoading"
          >
            {{ confirmText }}
          </el-button>
        </slot>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

// Props
interface Props {
  modelValue: boolean
  title?: string
  width?: string
  fullscreen?: boolean
  top?: string
  modal?: boolean
  appendToBody?: boolean
  closeOnClickModal?: boolean
  closeOnPressEscape?: boolean
  showClose?: boolean
  beforeClose?: (done: () => void) => void
  hideFooter?: boolean
  cancelText?: string
  confirmText?: string
  cancelLoading?: boolean
  confirmLoading?: boolean
  destroyOnClose?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  title: '提示',
  width: '50%',
  top: '15vh',
  modal: true,
  appendToBody: false,
  closeOnClickModal: true,
  closeOnPressEscape: true,
  showClose: true,
  hideFooter: false,
  cancelText: '取消',
  confirmText: '确定',
  cancelLoading: false,
  confirmLoading: false,
  destroyOnClose: false
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'open': []
  'opened': []
  'close': []
  'closed': []
  'confirm': []
  'cancel': []
  'before-close': [done: () => void]
}>()

// 响应式数据
const visible = ref(props.modelValue)

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

// 监听visible变化
watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

// 方法
const handleOpen = () => {
  emit('open')
}

const handleOpened = () => {
  emit('opened')
}

const handleClose = () => {
  emit('close')
}

const handleClosed = () => {
  emit('closed')
}

const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

const handleBeforeClose = (done: () => void) => {
  if (props.beforeClose) {
    props.beforeClose(done)
  } else {
    emit('before-close', done)
    done()
  }
}

// 打开模态框
const open = () => {
  visible.value = true
}

// 关闭模态框
const close = () => {
  visible.value = false
}

// 暴露方法给父组件
defineExpose({
  open,
  close
})
</script>

<style scoped lang="css">
.modal-content {
  max-height: 70vh;
  overflow-y: auto;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
  margin-right: 0;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #ebeef5;
  padding: 15px 20px;
}

@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 5vh auto;
  }
  
  :deep(.el-dialog__body) {
    padding: 15px;
  }
}
</style>