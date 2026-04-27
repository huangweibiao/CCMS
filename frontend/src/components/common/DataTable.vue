<template>
  <div class="data-table-container">
    <!-- 表格筛选区域 -->
    <div v-if="showFilter" class="table-filter">
      <slot name="filter">
        <el-form :model="filterModel" :inline="true" class="filter-form">
          <el-form-item 
            v-for="filter in filterConfig" 
            :key="filter.prop"
            :label="filter.label"
          >
            <el-input
              v-if="filter.type === 'input'"
              v-model="filterModel[filter.prop]"
              :placeholder="filter.placeholder || `请输入${filter.label}`"
              clearable
            />
            <el-select
              v-else-if="filter.type === 'select'"
              v-model="filterModel[filter.prop]"
              :placeholder="filter.placeholder || `请选择${filter.label}`"
              clearable
            >
              <el-option
                v-for="option in filter.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
            <el-date-picker
              v-else-if="filter.type === 'date'"
              v-model="filterModel[filter.prop]"
              type="date"
              :placeholder="filter.placeholder || `请选择${filter.label}`"
              value-format="YYYY-MM-DD"
            />
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
      </slot>
    </div>

    <!-- 表格操作区域 -->
    <div v-if="showToolbar" class="table-toolbar">
      <slot name="toolbar">
        <el-button 
          v-if="showAdd" 
          type="primary" 
          @click="handleAdd"
        >
          <el-icon><plus /></el-icon>
          新增
        </el-button>
        
        <el-button 
          v-if="multipleSelection.length > 0 && showBatchDelete"
          type="danger" 
          @click="handleBatchDelete"
        >
          <el-icon><delete /></el-icon>
          批量删除
        </el-button>
        
        <el-button-group>
          <el-button @click="handleRefresh">
            <el-icon><refresh /></el-icon>
          </el-button>
          <el-button @click="handleExport">
            <el-icon><download /></el-icon>
          </el-button>
        </el-button-group>
      </slot>
    </div>

    <!-- 表格主体 -->
    <div class="table-content">
      <el-table
        v-loading="loading"
        :data="tableData"
        :border="border"
        :stripe="stripe"
        :height="height"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <!-- 选择列 -->
        <el-table-column 
          v-if="showSelection" 
          type="selection" 
          width="55"
          align="center"
        />
        
        <!-- 序号列 -->
        <el-table-column 
          v-if="showIndex" 
          type="index" 
          label="序号" 
          width="80"
          align="center"
        />
        
        <!-- 数据列 -->
        <el-table-column
          v-for="column in columns"
          :key="column.prop"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :align="column.align || 'center'"
          :sortable="column.sortable"
          :fixed="column.fixed"
        >
          <template #default="scope">
            <!-- 自定义列内容 -->
            <slot v-if="column.slot" :name="column.slot" :row="scope.row">
              {{ scope.row[column.prop] }}
            </slot>
            <!-- 格式化内容 -->
            <span v-else-if="column.formatter">
              {{ column.formatter(scope.row, column) }}
            </span>
            <!-- 状态标签 -->
            <el-tag 
              v-else-if="column.type === 'tag'" 
              :type="getTagType(scope.row[column.prop])"
            >
              {{ getTagText(scope.row[column.prop], column) }}
            </el-tag>
            <!-- 默认显示 -->
            <span v-else>
              {{ scope.row[column.prop] }}
            </span>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column 
          v-if="showActions" 
          label="操作" 
          :width="actionWidth"
          align="center"
          fixed="right"
        >
          <template #default="scope">
            <slot name="actions" :row="scope.row">
              <el-button
                v-if="showEdit"
                type="primary"
                size="small"
                @click="handleEdit(scope.row)"
              >
                <el-icon><edit /></el-icon>
                编辑
              </el-button>
              
              <el-button
                v-if="showDelete"
                type="danger"
                size="small"
                @click="handleDelete(scope.row)"
              >
                <el-icon><delete /></el-icon>
                删除
              </el-button>
            </slot>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页区域 -->
    <div v-if="showPagination" class="table-pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Delete,
  Download,
  Edit
} from '@element-plus/icons-vue'

// Props
interface TableColumn {
  prop: string
  label: string
  width?: string | number
  minWidth?: string | number
  align?: 'left' | 'center' | 'right'
  sortable?: boolean | 'custom'
  fixed?: boolean | 'left' | 'right'
  slot?: string
  type?: 'tag' | 'text' | 'date'
  formatter?: (row: any, column: TableColumn) => string
  tagMapping?: Record<string, { type: string; text: string }>
}

interface Props {
  // 表格配置
  columns: TableColumn[]
  data: any[]
  loading?: boolean
  border?: boolean
  stripe?: boolean
  height?: string | number
  
  // 分页配置
  showPagination?: boolean
  total?: number
  pageSize?: number
  currentPage?: number
  
  // 功能配置
  showFilter?: boolean
  showToolbar?: boolean
  showSelection?: boolean
  showIndex?: boolean
  showActions?: boolean
  showAdd?: boolean
  showEdit?: boolean
  showDelete?: boolean
  showBatchDelete?: boolean
  actionWidth?: string | number
  
  // 筛选配置
  filterConfig?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  loading: false,
  border: true,
  stripe: true,
  showPagination: true,
  total: 0,
  pageSize: 10,
  currentPage: 1,
  showFilter: true,
  showToolbar: true,
  showSelection: false,
  showIndex: true,
  showActions: true,
  showAdd: true,
  showEdit: true,
  showDelete: true,
  showBatchDelete: true,
  actionWidth: '200',
  filterConfig: () => []
})

// Emits
const emit = defineEmits<{
  'search': [filter: any]
  'add': []
  'edit': [row: any]
  'delete': [row: any]
  'batch-delete': [rows: any[]]
  'refresh': []
  'export': []
  'size-change': [size: number]
  'current-change': [page: number]
  'selection-change': [selection: any[]]
  'sort-change': [sort: any]
}>()

// 响应式数据
const tableData = ref<any[]>(props.data)
const multipleSelection = ref<any[]>([])
const filterModel = reactive({})

// 监听数据变化
watch(() => props.data, (newData) => {
  tableData.value = newData
})

// 计算属性
const computedColumns = computed(() => props.columns)

// 方法
const handleSearch = () => {
  emit('search', filterModel)
}

const handleReset = () => {
  Object.keys(filterModel).forEach(key => {
    filterModel[key] = ''
  })
  emit('search', filterModel)
}

const handleAdd = () => {
  emit('add')
}

const handleEdit = (row: any) => {
  emit('edit', row)
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这条数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    emit('delete', row)
  } catch (error) {
    // 用户取消删除
  }
}

const handleBatchDelete = async () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择要删除的数据')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${multipleSelection.value.length} 条数据吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    emit('batch-delete', multipleSelection.value)
    multipleSelection.value = []
  } catch (error) {
    // 用户取消删除
  }
}

const handleRefresh = () => {
  emit('refresh')
}

const handleExport = () => {
  emit('export')
}

const handleSelectionChange = (selection: any[]) => {
  multipleSelection.value = selection
  emit('selection-change', selection)
}

const handleSortChange = (sort: any) => {
  emit('sort-change', sort)
}

const handleSizeChange = (size: number) => {
  emit('size-change', size)
}

const handleCurrentChange = (page: number) => {
  emit('current-change', page)
}

// 标签处理
const getTagType = (value: any) => {
  // 可以根据value返回不同的tag类型
  if (value === 1 || value === '1' || value === 'success') return 'success'
  if (value === 0 || value === '0' || value === 'danger') return 'danger'
  if (value === 2 || value === '2' || value === 'warning') return 'warning'
  return 'info'
}

const getTagText = (value: any, column: TableColumn) => {
  if (column.tagMapping && column.tagMapping[value]) {
    return column.tagMapping[value].text
  }
  return value
}

// 暴露方法供父组件调用
defineExpose({
  clearSelection: () => {
    multipleSelection.value = []
  },
  setFilter: (filter: any) => {
    Object.assign(filterModel, filter)
  }
})
</script>

<style scoped lang="css">
.data-table-container {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
}

.table-filter {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.filter-form {
  margin-bottom: 0;
}

.table-toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-content {
  margin-bottom: 20px;
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .data-table-container {
    padding: 10px;
  }
  
  .table-toolbar {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}
</style>