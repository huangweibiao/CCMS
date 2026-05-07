<template>
  <div class="dict-container">
    <div class="page-header">
      <div class="page-title">
        <h2>数据字典管理</h2>
        <p>管理系统数据字典项，支持多级字典结构</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><plus /></el-icon>
          新增字典
        </el-button>
        <el-button @click="handleReloadCache">
          <el-icon><refresh /></el-icon>
          刷新缓存
        </el-button>
      </div>
    </div>

    <div class="dict-content">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="dict-type-panel">
            <div class="panel-header">
              <h3>字典类型</h3>
            </div>
            <el-menu
              :default-active="currentDictType"
              class="dict-type-menu"
              @select="handleTypeSelect"
            >
              <el-menu-item v-for="type in dictTypes" :key="type" :index="type">
                <el-icon><document /></el-icon>
                <span>{{ type }}</span>
              </el-menu-item>
            </el-menu>
          </div>
        </el-col>
        <el-col :span="18">
          <div class="dict-list-panel">
            <div class="panel-header">
              <h3>字典项列表 - {{ currentDictType || '请选择字典类型' }}</h3>
            </div>
            <el-table :data="dictList" v-loading="loading" stripe>
              <el-table-column prop="dictCode" label="字典编码" width="150" />
              <el-table-column prop="dictName" label="字典名称" width="150" />
              <el-table-column prop="dictValue" label="字典值" />
              <el-table-column prop="sortOrder" label="排序" width="80" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="handleEdit(row)">
                    编辑
                  </el-button>
                  <el-button type="success" link size="small" @click="handleToggleStatus(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="danger" link size="small" @click="handleDelete(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="formData.dictType" placeholder="请输入字典类型" />
        </el-form-item>
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="formData.dictCode" placeholder="请输入字典编码" />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="formData.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="formData.dictValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" rows="3" />
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
import { Plus, Refresh, Document } from '@element-plus/icons-vue'
import { dictApi } from '@/api/dict'
import type { DataDict } from '@/types/system'

const loading = ref(false)
const dictTypes = ref<string[]>([])
const currentDictType = ref('')
const dictList = ref<DataDict[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref()

const formData = reactive<Partial<DataDict>>({
  dictType: '',
  dictCode: '',
  dictName: '',
  dictValue: '',
  sortOrder: 0,
  status: 1,
  remark: ''
})

const formRules = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }]
}

onMounted(() => {
  loadDictTypes()
})

const loadDictTypes = async () => {
  try {
    const res = await dictApi.getAllDictTypes()
    dictTypes.value = res.data
    if (dictTypes.value.length > 0 && !currentDictType.value) {
      currentDictType.value = dictTypes.value[0]
      loadDictList()
    }
  } catch (error) {
    ElMessage.error('获取字典类型失败')
  }
}

const loadDictList = async () => {
  if (!currentDictType.value) return
  loading.value = true
  try {
    const res = await dictApi.getDictsByType(currentDictType.value)
    dictList.value = res.data
  } catch (error) {
    ElMessage.error('获取字典列表失败')
  } finally {
    loading.value = false
  }
}

const handleTypeSelect = (type: string) => {
  currentDictType.value = type
  loadDictList()
}

const handleAdd = () => {
  dialogTitle.value = '新增字典'
  Object.assign(formData, {
    dictType: currentDictType.value,
    dictCode: '',
    dictName: '',
    dictValue: '',
    sortOrder: 0,
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: DataDict) => {
  dialogTitle.value = '编辑字典'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    if (formData.id) {
      await dictApi.updateDict(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      await dictApi.createDict(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadDictList()
    loadDictTypes()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = async (row: DataDict) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await dictApi.updateDictStatus(row.id!, newStatus)
    ElMessage.success('状态更新成功')
    loadDictList()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const handleDelete = async (row: DataDict) => {
  try {
    await ElMessageBox.confirm('确定要删除该字典项吗？', '提示', { type: 'warning' })
    await dictApi.deleteDict(row.id!)
    ElMessage.success('删除成功')
    loadDictList()
  } catch (error) {
    // 取消删除
  }
}

const handleReloadCache = async () => {
  try {
    await dictApi.reloadDictCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    ElMessage.error('缓存刷新失败')
  }
}
</script>

<style scoped lang="scss">
.dict-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dict-content {
  .dict-type-panel,
  .dict-list-panel {
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  .panel-header {
    padding: 15px;
    border-bottom: 1px solid #ebeef5;
    h3 {
      margin: 0;
      font-size: 16px;
    }
  }

  .dict-type-menu {
    border-right: none;
  }
}
</style>
