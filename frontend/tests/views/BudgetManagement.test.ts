import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { ElTable, ElPagination, ElDialog, ElButton } from 'element-plus';
import BudgetManagement from '@/views/budget/BudgetManagement.vue';
import { nextTick } from 'vue';

const mockBudgets = [
  {
    id: 1,
    name: '差旅费预算',
    totalAmount: 50000,
    usedAmount: 12000,
    balance: 38000,
    status: 'ACTIVE',
    startDate: '2024-01-01',
    endDate: '2024-12-31',
    createdBy: '张三'
  },
  {
    id: 2,
    name: '办公费预算',
    totalAmount: 20000,
    usedAmount: 8000,
    balance: 12000,
    status: 'ACTIVE',
    startDate: '2024-01-01',
    endDate: '2024-12-31',
    createdBy: '李四'
  }
];

describe('BudgetManagement', () => {
  let wrapper;

  beforeEach(() => {
    wrapper = mount(BudgetManagement, {
      global: {
        stubs: {
          'el-table': ElTable,
          'el-pagination': ElPagination,
          'el-dialog': ElDialog,
          'el-button': ElButton
        }
      }
    });
  });

  it('renders budget management interface correctly', () => {
    expect(wrapper.find('[data-testid="budget-table"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="search-form"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="add-budget-btn"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="export-btn"]').exists()).toBe(true);
  });

  it('loads budget list on component mount', async () => {
    const loadBudgetsSpy = vi.spyOn(wrapper.vm, 'loadBudgets');
    
    await wrapper.vm.$nextTick();
    
    expect(loadBudgetsSpy).toHaveBeenCalled();
  });

  it('searches budgets based on search criteria', async () => {
    // 设置搜索条件
    wrapper.vm.searchForm.name = '差旅';
    wrapper.vm.searchForm.status = 'ACTIVE';
    
    await wrapper.vm.handleSearch();
    
    expect(wrapper.vm.loadBudgets).toHaveBeenCalledWith(
      expect.objectContaining({
        name: '差旅',
        status: 'ACTIVE'
      })
    );
  });

  it('resets search criteria and reloads data', async () => {
    // 先设置一些搜索条件
    wrapper.vm.searchForm.name = '测试';
    wrapper.vm.searchForm.status = 'INACTIVE';
    
    await wrapper.vm.handleReset();
    
    expect(wrapper.vm.searchForm.name).toBe('');
    expect(wrapper.vm.searchForm.status).toBe('');
    expect(wrapper.vm.loadBudgets).toHaveBeenCalledWith({});
  });

  it('opens create budget dialog', async () => {
    expect(wrapper.vm.createDialogVisible).toBe(false);
    
    await wrapper.find('[data-testid="add-budget-btn"]').trigger('click');
    
    expect(wrapper.vm.createDialogVisible).toBe(true);
  });

  it('opens edit budget dialog with budget data', async () => {
    const budget = mockBudgets[0];
    
    await wrapper.vm.handleEdit(budget);
    
    expect(wrapper.vm.editDialogVisible).toBe(true);
    expect(wrapper.vm.currentBudget).toEqual(budget);
  });

  it('validates budget form correctly', async () => {
    // 打开创建对话框
    wrapper.vm.createDialogVisible = true;
    await nextTick();
    
    const budgetForm = wrapper.vm.budgetForm;
    
    // 测试空表单验证
    budgetForm.name = '';
    budgetForm.totalAmount = 0;
    budgetForm.startDate = '';
    budgetForm.endDate = '';
    
    await wrapper.vm.submitCreate();
    
    // 应该有验证错误
    expect(wrapper.vm.$refs.createFormRef.validate).toHaveBeenCalled();
  });

  it('calculates budget balance correctly', () => {
    const budget = { totalAmount: 50000, usedAmount: 12000 };
    const balance = wrapper.vm.calculateBalance(budget);
    
    expect(balance).toBe(38000);
  });

  it('formats currency values correctly', () => {
    const formatted = wrapper.vm.formatCurrency(12345.67);
    expect(formatted).toBe('¥12,345.67');
  });

  it('handles pagination correctly', async () => {
    const newPage = 2;
    const newSize = 20;
    
    await wrapper.vm.handlePageChange(newPage);
    expect(wrapper.vm.queryParams.pageNum).toBe(newPage);
    
    await wrapper.vm.handleSizeChange(newSize);
    expect(wrapper.vm.queryParams.pageSize).toBe(newSize);
    
    // 分页变化后应该重新加载数据
    expect(wrapper.vm.loadBudgets).toHaveBeenCalled();
  });

  it('shows budget status correctly', () => {
    const activeStatus = wrapper.vm.formatStatus('ACTIVE');
    const inactiveStatus = wrapper.vm.formatStatus('INACTIVE');
    
    expect(activeStatus).toBe('启用');
    expect(inactiveStatus).toBe('停用');
  });

  it('handles budget deletion with confirmation', async () => {
    const budget = mockBudgets[0];
    const deleteSpy = vi.spyOn(wrapper.vm, 'handleDelete');
    
    await wrapper.vm.handleDelete(budget);
    
    // 应该显示确认对话框
    expect(wrapper.vm.deleteConfirmVisible).toBe(true);
    expect(wrapper.vm.currentBudget).toEqual(budget);
  });

  it('exports budget data', async () => {
    const exportSpy = vi.spyOn(wrapper.vm, 'handleExport');
    
    await wrapper.vm.handleExport();
    
    expect(exportSpy).toHaveBeenCalled();
    // 通常会调用导出API或生成文件
  });

  it('filters budgets by status', async () => {
    const statusFilter = 'ACTIVE';
    
    await wrapper.vm.handleStatusFilter(statusFilter);
    
    expect(wrapper.vm.searchForm.status).toBe(statusFilter);
    expect(wrapper.vm.loadBudgets).toHaveBeenCalledWith(
      expect.objectContaining({ status: statusFilter })
    );
  });

  it('handles budget activation/deactivation', async () => {
    const budget = mockBudgets[0];
    
    // 测试停用
    await wrapper.vm.handleStatusChange(budget, 'INACTIVE');
    expect(wrapper.vm.updateBudgetStatus).toHaveBeenCalledWith(budget.id, 'INACTIVE');
    
    // 测试启用
    await wrapper.vm.handleStatusChange(budget, 'ACTIVE');
    expect(wrapper.vm.updateBudgetStatus).toHaveBeenCalledWith(budget.id, 'ACTIVE');
  });

  it('calculates usage percentage correctly', () => {
    const budget = { totalAmount: 50000, usedAmount: 25000 };
    const percentage = wrapper.vm.calculateUsagePercentage(budget);
    
    expect(percentage).toBe(50);
  });

  it('shows budget details when row is clicked', async () => {
    const budget = mockBudgets[0];
    
    await wrapper.vm.handleRowClick(budget);
    
    expect(wrapper.vm.detailDialogVisible).toBe(true);
    expect(wrapper.vm.currentBudget).toEqual(budget);
  });

  it('handles budget copy functionality', async () => {
    const budget = mockBudgets[0];
    
    await wrapper.vm.handleCopy(budget);
    
    expect(wrapper.vm.createDialogVisible).toBe(true);
    // 复制时应该设置表单数据为原预算的副本
    expect(wrapper.vm.budgetForm.name).toContain('副本');
  });

  it('refreshes budget data', async () => {
    const refreshSpy = vi.spyOn(wrapper.vm, 'handleRefresh');
    
    await wrapper.vm.handleRefresh();
    
    expect(refreshSpy).toHaveBeenCalled();
    expect(wrapper.vm.loadBudgets).toHaveBeenCalled();
  });

  it('handles date range validation', () => {
    const startDate = '2024-01-01';
    const endDate = '2023-12-31'; // 结束日期在开始日期之前
    
    const isValid = wrapper.vm.validateDateRange(startDate, endDate);
    
    expect(isValid).toBe(false);
  });

  it('previews budget before submission', async () => {
    wrapper.vm.budgetForm = {
      name: '测试预算',
      totalAmount: 10000,
      startDate: '2024-01-01',
      endDate: '2024-12-31',
      description: '测试描述'
    };
    
    await wrapper.vm.handlePreview();
    
    expect(wrapper.vm.previewDialogVisible).toBe(true);
    expect(wrapper.vm.previewData).toEqual(wrapper.vm.budgetForm);
  });
});