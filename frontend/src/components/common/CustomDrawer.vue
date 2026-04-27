<template>
  <el-drawer
    v-model="visible"
    :title="title"
    :size="size"
    :direction="direction"
    :modal="modal"
    :append-to-body="appendToBody"
    :before-close="handleBeforeClose"
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleClose"
    @closed="handleClosed"
  >
    <!-- 抽屉内容 -->
    <div class="drawer-content">
      <slot></slot>
    </div>

    <!-- 自定义底部区域 -->
    <div v-if="!hideFooter" class="drawer-footer">
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
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

// Props
interface Props {
  modelValue: boolean
  title?: string
  size?: string | number
  direction?: 'rtl' | 'ltr' | 'ttb' | 'btt'
  modal?: boolean
  appendToBody?: boolean
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
  title: '抽屉',
  size: '30%',
  direction: 'rtl',
  modal: true,
  appendToBody: false,
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

// 打开抽屉
const open = () => {
  visible.value = true
}

// 关闭抽屉
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
.drawer-content {
  height: calc(100% - 60px);
  overflow-y: auto;
  padding: 20px;
}

.drawer-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-drawer__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
  margin-bottom: 0;
}

:deep(.el-drawer__body) {
  padding: 0;
}

@media (max-width: 768px) {
  :deep(.el-drawer) {
    width: 100% !important;
  }
  
  .drawer-content {
    padding: 15px;
  }
  
  .drawer-footer {
    padding: 15px;
  }
}
</style>