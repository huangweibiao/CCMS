<template>
  <div class="custom-form">
    <el-form
      ref="formRef"
      :model="formModel"
      :rules="formRules"
      :label-width="labelWidth"
      :size="size"
    >
      <el-row :gutter="gutter">
        <el-col 
          v-for="item in formItems" 
          :key="item.prop"
          :span="item.span || defaultSpan"
          :xs="item.xs || 24"
          :sm="item.sm || 12"
          :md="item.md || 8"
        >
          <el-form-item 
            :label="item.label" 
            :prop="item.prop"
            :required="item.required"
          >
            <!-- 输入框 -->
            <el-input
              v-if="item.type === 'input'"
              v-model="formModel[item.prop]"
              :placeholder="item.placeholder || `请输入${item.label}`"
              :disabled="item.disabled"
              :clearable="item.clearable !== false"
              :type="item.inputType || 'text'"
              :maxlength="item.maxlength"
              :show-word-limit="item.showWordLimit"
            />
            
            <!-- 文本域 -->
            <el-input
              v-else-if="item.type === 'textarea'"
              v-model="formModel[item.prop]"
              type="textarea"
              :rows="item.rows || 3"
              :placeholder="item.placeholder || `请输入${item.label}`"
              :disabled="item.disabled"
              :maxlength="item.maxlength"
              :show-word-limit="item.showWordLimit"
            />
            
            <!-- 数字输入框 -->
            <el-input-number
              v-else-if="item.type === 'number'"
              v-model="formModel[item.prop]"
              :min="item.min"
              :max="item.max"
              :step="item.step || 1"
              :precision="item.precision"
              :controls-position="item.controlsPosition"
              :disabled="item.disabled"
            />
            
            <!-- 选择器 -->
            <el-select
              v-else-if="item.type === 'select'"
              v-model="formModel[item.prop]"
              :placeholder="item.placeholder || `请选择${item.label}`"
              :multiple="item.multiple"
              :filterable="item.filterable"
              :clearable="item.clearable !== false"
              :disabled="item.disabled"
              @change="item.onChange && item.onChange(formModel[item.prop], formModel)"
            >
              <el-option
                v-for="option in item.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
                :disabled="option.disabled"
              />
            </el-select>
            
            <!-- 日期选择器 -->
            <el-date-picker
              v-else-if="item.type === 'date'"
              v-model="formModel[item.prop]"
              :type="item.dateType || 'date'"
              :placeholder="item.placeholder || `请选择${item.label}`"
              :format="item.format"
              :value-format="item.valueFormat || 'YYYY-MM-DD'"
              :disabled="item.disabled"
              :clearable="item.clearable !== false"
            />
            
            <!-- 时间选择器 -->
            <el-time-picker
              v-else-if="item.type === 'time'"
              v-model="formModel[item.prop]"
              :placeholder="item.placeholder || `请选择${item.label}`"
              :format="item.format || 'HH:mm:ss'"
              :value-format="item.valueFormat || 'HH:mm:ss'"
              :disabled="item.disabled"
              :clearable="item.clearable !== false"
            />
            
            <!-- 日期时间选择器 -->
            <el-date-picker
              v-else-if="item.type === 'datetime'"
              v-model="formModel[item.prop]"
              type="datetime"
              :placeholder="item.placeholder || `请选择${item.label}`"
              :format="item.format || 'YYYY-MM-DD HH:mm:ss'"
              :value-format="item.valueFormat || 'YYYY-MM-DD HH:mm:ss'"
              :disabled="item.disabled"
              :clearable="item.clearable !== false"
            />
            
            <!-- 单选按钮 -->
            <el-radio-group
              v-else-if="item.type === 'radio'"
              v-model="formModel[item.prop]"
              :disabled="item.disabled"
            >
              <el-radio
                v-for="option in item.options"
                :key="option.value"
                :label="option.value"
              >
                {{ option.label }}
              </el-radio>
            </el-radio-group>
            
            <!-- 复选框 -->
            <el-checkbox-group
              v-else-if="item.type === 'checkbox'"
              v-model="formModel[item.prop]"
              :disabled="item.disabled"
            >
              <el-checkbox
                v-for="option in item.options"
                :key="option.value"
                :label="option.value"
              >
                {{ option.label }}
              </el-checkbox>
            </el-checkbox-group>
            
            <!-- 开关 -->
            <el-switch
              v-else-if="item.type === 'switch'"
              v-model="formModel[item.prop]"
              :active-value="item.activeValue !== undefined ? item.activeValue : 1"
              :inactive-value="item.inactiveValue !== undefined ? item.inactiveValue : 0"
              :active-text="item.activeText"
              :inactive-text="item.inactiveText"
              :disabled="item.disabled"
            />
            
            <!-- 文件上传 -->
            <el-upload
              v-else-if="item.type === 'upload'"
              :action="item.action"
              :multiple="item.multiple"
              :limit="item.limit"
              :file-list="formModel[item.prop]"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :on-remove="handleUploadRemove"
              :before-upload="item.beforeUpload"
            >
              <el-button type="primary">
                <el-icon><upload /></el-icon>
                点击上传
              </el-button>
              <template #tip>
                <div class="el-upload__tip" v-if="item.tip">
                  {{ item.tip }}
                </div>
              </template>
            </el-upload>
            
            <!-- 自定义插槽 -->
            <slot
              v-else-if="item.type === 'slot'"
              :name="item.slotName || item.prop"
              :model="formModel"
              :item="item"
            />
            
            <!-- 默认文本显示 -->
            <span v-else-if="item.type === 'text'">
              {{ formModel[item.prop] }}
            </span>
          </el-form-item>
        </el-col>
      </el-row>
      
      <!-- 表单操作按钮 -->
      <div v-if="showActions" class="form-actions">
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ submitText }}
        </el-button>
        <el-button @click="handleReset" :disabled="loading">
          重置
        </el-button>
        <el-button v-if="showCancel" @click="handleCancel" :disabled="loading">
          取消
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

// Props
interface FormItem {
  prop: string
  label: string
  type: 'input' | 'textarea' | 'number' | 'select' | 'date' | 'time' | 'datetime' | 
        'radio' | 'checkbox' | 'switch' | 'upload' | 'slot' | 'text'
  span?: number
  xs?: number
  sm?: number
  md?: number
  required?: boolean
  disabled?: boolean
  placeholder?: string
  clearable?: boolean
  maxlength?: number
  showWordLimit?: boolean
  inputType?: string
  rows?: number
  min?: number
  max?: number
  step?: number
  precision?: number
  controlsPosition?: 'right'
  multiple?: boolean
  filterable?: boolean
  options?: any[]
  dateType?: string
  format?: string
  valueFormat?: string
  activeValue?: any
  inactiveValue?: any
  activeText?: string
  inactiveText?: string
  action?: string
  limit?: number
  tip?: string
  slotName?: string
  onChange?: (value: any, form: any) => void
  beforeUpload?: (file: File) => boolean | Promise<boolean>
}

interface Props {
  modelValue?: any
  formItems: FormItem[]
  rules?: any
  labelWidth?: string
  size?: 'large' | 'default' | 'small'
  gutter?: number
  defaultSpan?: number
  showActions?: boolean
  submitText?: string
  showCancel?: boolean
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({}),
  rules: () => ({}),
  labelWidth: '100px',
  size: 'default',
  gutter: 20,
  defaultSpan: 24,
  showActions: true,
  submitText: '提交',
  showCancel: true,
  loading: false
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: any]
  'submit': [form: any]
  'reset': []
  'cancel': []
  'validate': [valid: boolean]
}>()

// 响应式数据
const formRef = ref()
const formModel = reactive({ ...props.modelValue })

// 表单验证规则
const formRules = reactive(props.rules)

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
  Object.keys(formModel).forEach(key => {
    delete formModel[key]
  })
  Object.assign(formModel, newValue)
}, { deep: true })

// 监听formModel变化
watch(formModel, (newValue) => {
  emit('update:modelValue', { ...newValue })
}, { deep: true })

// 方法
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (valid) {
      emit('submit', { ...formModel })
      emit('validate', true)
    }
  } catch (error) {
    emit('validate', false)
    ElMessage.error('表单验证失败，请检查输入')
  }
}

const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  emit('reset')
}

const handleCancel = () => {
  emit('cancel')
}

const handleUploadSuccess = (response: any, file: any, fileList: any[]) => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error: any, file: any, fileList: any[]) => {
  ElMessage.error('文件上传失败')
}

const handleUploadRemove = (file: any, fileList: any[]) => {
  // 处理文件删除
}

// 验证表单
const validate = () => {
  return formRef.value?.validate()
}

// 重置表单
const resetFields = () => {
  formRef.value?.resetFields()
}

// 清除验证
const clearValidate = () => {
  formRef.value?.clearValidate()
}

// 设置表单值
const setFieldsValue = (values: any) => {
  Object.assign(formModel, values)
}

// 获取表单值
const getFieldsValue = () => {
  return { ...formModel }
}

// 暴露方法给父组件
defineExpose({
  validate,
  resetFields,
  clearValidate,
  setFieldsValue,
  getFieldsValue
})
</script>

<style scoped lang="css">
.custom-form {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

.form-actions {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-date-editor) {
  width: 100%;
}

@media (max-width: 768px) {
  .custom-form {
    padding: 15px;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}
</style>