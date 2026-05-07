<template>
  <div class="config-container">
    <div class="page-header">
      <div class="page-title">
        <h2>系统配置</h2>
        <p>管理系统参数配置，支持多种配置类型</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><plus /></el-icon>
          新增配置
        </el-button>
        <el-button @click="handleReloadCache">
          <el-icon><refresh /></el-icon>
          刷新缓存
        </el-button>
      </div>
    </div>

    <div class="search-form">
      <el-form :model="searchForm" inline>
        <el-form-item label="配置键">
          <el-input v-model="searchForm.configKey" placeholder="请输入配置键" clearable />
        </el-form-item>
        <el-form-item label="配置类型">
          <el-select v-model="searchForm.configType" placeholder="请选择" clearable>
            <el-option label="字符串" value="STRING" />
            <el-option label="数字" value="NUMBER" />
            <el-option label="布尔值" value="BOOLEAN" />
            <el-option label="JSON" value="JSON" />
            <el-option label="列表" value="LIST" />
          </el-select>
        </el-form-item>
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
      </el-form>
    </div>

    <el-table :data="configList" v-loading="loading" stripe>
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="configKey" label="配置键" min-width="200" />
      <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip />
      <el-table-column prop="configName" label="配置名称" min-width="150" />
      <el-table-column prop="configType" label="配置类型" width="100">
        <template #default="{ row }">
          <el-tag>{{ row.configType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'danger'">
            {{ row.enabled ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="success" link size="small" @click="handleToggleStatus(row)">
            {{ row.enabled ? '禁用' : '启用' }}
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="formData.configKey" placeholder="请输入配置键" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="formData.configValue" placeholder="请输入配置值" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="formData.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="formData.configType" placeholder="请选择配置类型" style="width: 100%">
            <el-option label="字符串" value="STRING" />
            <el-option label="数字" value="NUMBER" />
            <el-option label="布尔值" value="BOOLEAN" />
            <el-option label="JSON" value="JSON" />
            <el-option label="列表" value="LIST" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-radio-group v-model="formData.enabled">
            <el-radio :label="true">启用</el-radio>
            <el-radio :label="false">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" rows="3" placeholder="请输入配置描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { configApi } from '@/api/config'
import type { SystemConfig } from '@/types/system'

const loading = ref(false)
const configList = ref<SystemConfig[]>([])
const allConfigs = ref<SystemConfig[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref()

const searchForm = reactive({
  configKey: '',
  configType: ''
})

const formData = reactive<Partial<SystemConfig>>({
  configKey: '',
  configValue: '',
  configName: '',
  configType: 'STRING',
  enabled: true,
  description: ''
})

const formRules = {
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
  configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }]
}

onMounted(() => {
  loadConfigList()
})

const loadConfigList = async () => {
  loading.value = true
  try {
    const res = await configApi.getAllEnabledConfigs()
    allConfigs.value = res.data
    filterConfigs()
  } catch (error) {
    ElMessage.error('获取配置列表失败')
  } finally {
    loading.value = false
  }
}

const filterConfigs = () => {
  configList.value = allConfigs.value.filter(config => {
    const matchKey = !searchForm.configKey || config.configKey.toLowerCase().includes(searchForm.configKey.toLowerCase())
    const matchType = !searchForm.configType || config.configType === searchForm.configType
    return matchKey && matchType
  })
}

const handleSearch = () => {
  filterConfigs()
}

const handleReset = () => {
  searchForm.configKey = ''
  searchForm.configType = ''
  filterConfigs()
}

const handleAdd = () => {
  dialogTitle.value = '新增配置'
  Object.assign(formData, {
    id: undefined,
    configKey: '',
    configValue: '',
    configName: '',
    configType: 'STRING',
    enabled: true,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: SystemConfig) => {
  dialogTitle.value = '编辑配置'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    await configApi.saveConfig(formData)
    ElMessage.success(formData.id ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadConfigList()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = async (row: SystemConfig) => {
  try {
    const newEnabled = !row.enabled
    await configApi.saveConfig({ ...row, enabled: newEnabled })
    ElMessage.success('状态更新成功')
    loadConfigList()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const handleDelete = async (row: SystemConfig) => {
  try {
    await ElMessageBox.confirm('确定要删除该配置吗？', '提示', { type: 'warning' })
    await configApi.deleteConfigByKey(row.configKey)
    ElMessage.success('删除成功')
    loadConfigList()
  } catch (error) {
    // 取消删除
  }
}

const handleReloadCache = async () => {
  try {
    await configApi.reloadConfigCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    ElMessage.error('缓存刷新失败')
  }
}
</script>

<style scoped lang="scss">
.config-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
