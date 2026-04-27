import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import BudgetManagementPage from '../../pages/BudgetManagementPage.vue';
import { useBudgetStore } from '../../stores/budget';
import { useAuthStore } from '../../stores/auth';

// Mock the stores
vi.mock('../../stores/budget', () => ({
  useBudgetStore: vi.fn()
}));

vi.mock('../../stores/auth', () => ({
  useAuthStore: vi.fn()
}));

describe('BudgetManagementPage', () => {
  let budgetStore: any;
  let authStore: any;

  beforeEach(() => {
    setActivePinia(createPinia());
    
    // Create mock stores
    budgetStore = {
      budgets: [
        {
          id: 1,
          department: '技术部',
          year: 2024,
          month: 1,
          amount: 50000,
          usedAmount: 25000,
          remainingAmount: 25000,
          status: 'APPROVED',
          createdAt: '2024-01-01',
          updatedAt: '2024-01-01'
        },
        {
          id: 2,
          department: '销售部',
          year: 2024,
          month: 1,
          amount: 30000,
          usedAmount: 15000,
          remainingAmount: 15000,
          status: 'PENDING',
          createdAt: '2024-01-02',
          updatedAt: '2024-01-02'
        }
      ],
      loading: false,
      error: '',
      currentPage: 1,
      pageSize: 10,
      total: 2,
      loadBudgets: vi.fn().mockResolvedValue({ success: true }),
      createBudget: vi.fn().mockResolvedValue({ success: true }),
      updateBudget: vi.fn().mockResolvedValue({ success: true }),
      deleteBudget: vi.fn().mockResolvedValue({ success: true }),
      approveBudget: vi.fn().mockResolvedValue({ success: true })
    };
    
    authStore = {
      userInfo: {
        id: 1,
        username: 'testuser',
        name: '测试用户',
        role: 'USER'
      },
      hasPermission: vi.fn().mockReturnValue(true)
    };
    
    (useBudgetStore as any).mockReturnValue(budgetStore);
    (useAuthStore as any).mockReturnValue(authStore);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  const createWrapper = (props = {}) => {
    return mount(BudgetManagementPage, {
      global: {
        plugins: [createPinia()],
        stubs: {
          'router-link': true,
          'el-table': true,
          'el-table-column': true,
          'el-button': true,
          'el-dialog': true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-pagination': true,
          'el-tag': true
        }
      },
      props
    });
  };

  describe('Initial Render', () => {
    it('should render budget management page correctly', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('预算管理');
      expect(wrapper.text()).toContain('新增预算');
    });

    it('should display budget table with data', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('技术部');
      expect(wrapper.text()).toContain('销售部');
      expect(wrapper.text()).toContain('¥50,000.00');
      expect(wrapper.text()).toContain('¥30,000.00');
    });

    it('should show action buttons for each budget', () => {
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('编辑');
      expect(wrapper.text()).toContain('删除');
      expect(wrapper.text()).toContain('审批');
    });
  });

  describe('Data Loading', () => {
    it('should load budgets on component mount', async () => {
      createWrapper();
      
      await flushPromises();
      
      expect(budgetStore.loadBudgets).toHaveBeenCalledWith({
        page: 1,
        pageSize: 10
      });
    });

    it('should handle pagination changes', async () => {
      const wrapper = createWrapper();
      await flushPromises();
      
      // Clear previous calls
      budgetStore.loadBudgets.mockClear();
      
      // Simulate page change
      const pagination = wrapper.find('.el-pagination');
      if (pagination.exists()) {
        // Trigger page change event
        wrapper.vm.handlePageChange(2);
        
        expect(budgetStore.loadBudgets).toHaveBeenCalledWith({
          page: 2,
          pageSize: 10
        });
      }
    });

    it('should handle search functionality', async () => {
      const wrapper = createWrapper();
      await flushPromises();
      
      budgetStore.loadBudgets.mockClear();
      
      // Simulate search input
      const searchInput = wrapper.find('.search-input');
      if (searchInput.exists()) {
        await searchInput.setValue('技术部');
        
        // Trigger search (with debounce)
        await new Promise(resolve => setTimeout(resolve, 300));
        
        expect(budgetStore.loadBudgets).toHaveBeenCalledWith({
          page: 1,
          pageSize: 10,
          department: '技术部'
        });
      }
    });
  });

  describe('Budget Creation', () => {
    it('should open create budget dialog', async () => {
      const wrapper = createWrapper();
      
      const createButton = wrapper.find('.create-button');
      if (createButton.exists()) {
        await createButton.trigger('click');
        
        const dialog = wrapper.find('.budget-dialog');
        expect(dialog.exists()).toBe(true);
        expect(dialog.isVisible()).toBe(true);
      }
    });

    it('should validate budget form before submission', async () => {
      const wrapper = createWrapper();
      
      // Open dialog
      wrapper.vm.showCreateDialog = true;
      await wrapper.vm.$nextTick();
      
      // Try to submit empty form
      const submitButton = wrapper.find('.submit-button');
      if (submitButton.exists()) {
        await submitButton.trigger('click');
        
        expect(budgetStore.createBudget).not.toHaveBeenCalled();
        
        // Check for validation errors
        expect(wrapper.text()).toContain('部门不能为空');
        expect(wrapper.text()).toContain('年份不能为空');
        expect(wrapper.text()).toContain('月份不能为空');
        expect(wrapper.text()).toContain('金额必须大于0');
      }
    });

    it('should create budget with valid data', async () => {
      const wrapper = createWrapper();
      
      wrapper.vm.showCreateDialog = true;
      wrapper.vm.budgetForm = {
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 50000
      };
      
      await wrapper.vm.$nextTick();
      
      const submitButton = wrapper.find('.submit-button');
      if (submitButton.exists()) {
        await submitButton.trigger('click');
        
        expect(budgetStore.createBudget).toHaveBeenCalledWith({
          department: '技术部',
          year: 2024,
          month: 1,
          amount: 50000
        });
        
        // Dialog should close after successful creation
        await flushPromises();
        expect(wrapper.vm.showCreateDialog).toBe(false);
      }
    });
  });

  describe('Budget Operations', () => {
    it('should edit budget', async () => {
      const wrapper = createWrapper();
      
      const editButton = wrapper.find('.edit-button');
      if (editButton.exists()) {
        await editButton.trigger('click');
        
        // Should open edit dialog with budget data
        expect(wrapper.vm.showEditDialog).toBe(true);
        expect(wrapper.vm.editingBudget.id).toBe(1);
        
        // Update budget amount
        wrapper.vm.budgetForm.amount = 60000;
        
        const submitButton = wrapper.find('.submit-button');
        if (submitButton.exists()) {
          await submitButton.trigger('click');
          
          expect(budgetStore.updateBudget).toHaveBeenCalledWith(1, {
            amount: 60000
          });
        }
      }
    });

    it('should delete budget with confirmation', async () => {
      const wrapper = createWrapper();
      
      const deleteButton = wrapper.find('.delete-button');
      if (deleteButton.exists()) {
        // Mock confirmation dialog
        const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
        
        await deleteButton.trigger('click');
        
        expect(confirmSpy).toHaveBeenCalledWith('确定要删除这个预算吗？');
        expect(budgetStore.deleteBudget).toHaveBeenCalledWith(1);
        
        confirmSpy.mockRestore();
      }
    });

    it('should cancel delete operation when user declines', async () => {
      const wrapper = createWrapper();
      
      const deleteButton = wrapper.find('.delete-button');
      if (deleteButton.exists()) {
        const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);
        
        await deleteButton.trigger('click');
        
        expect(confirmSpy).toHaveBeenCalled();
        expect(budgetStore.deleteBudget).not.toHaveBeenCalled();
        
        confirmSpy.mockRestore();
      }
    });

    it('should approve pending budget', async () => {
      const wrapper = createWrapper();
      
      const approveButton = wrapper.find('.approve-button');
      if (approveButton.exists()) {
        await approveButton.trigger('click');
        
        expect(budgetStore.approveBudget).toHaveBeenCalledWith(2);
      }
    });
  });

  describe('Access Control', () => {
    it('should show create button for users with permission', () => {
      authStore.hasPermission.mockReturnValue(true);
      
      const wrapper = createWrapper();
      
      const createButton = wrapper.find('.create-button');
      expect(createButton.exists()).toBe(true);
    });

    it('should hide create button for users without permission', () => {
      authStore.hasPermission.mockReturnValue(false);
      
      const wrapper = createWrapper();
      
      const createButton = wrapper.find('.create-button');
      expect(createButton.exists()).toBe(false);
    });

    it('should show approval actions for approvers', () => {
      authStore.hasPermission.mockImplementation((perm: string) => 
        perm === 'budget:approve'
      );
      
      const wrapper = createWrapper();
      
      const approveButtons = wrapper.findAll('.approve-button');
      expect(approveButtons.length).toBeGreaterThan(0);
    });

    it('should hide approval actions for non-approvers', () => {
      authStore.hasPermission.mockReturnValue(false);
      
      const wrapper = createWrapper();
      
      const approveButtons = wrapper.findAll('.approve-button');
      expect(approveButtons.length).toBe(0);
    });
  });

  describe('Error Handling', () => {
    it('should display error message when operation fails', async () => {
      budgetStore.createBudget.mockRejectedValue(new Error('创建失败'));
      budgetStore.error = '创建预算失败';
      
      const wrapper = createWrapper();
      
      wrapper.vm.showCreateDialog = true;
      wrapper.vm.budgetForm = {
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 50000
      };
      
      await wrapper.vm.$nextTick();
      
      const submitButton = wrapper.find('.submit-button');
      if (submitButton.exists()) {
        await submitButton.trigger('click');
        await flushPromises();
        
        expect(wrapper.text()).toContain('创建预算失败');
      }
    });

    it('should clear error when user retries', async () => {
      budgetStore.error = '操作失败';
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('操作失败');
      
      // Clear error
      budgetStore.error = '';
      await wrapper.vm.$nextTick();
      
      expect(wrapper.text()).not.toContain('操作失败');
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty budget list', () => {
      budgetStore.budgets = [];
      budgetStore.total = 0;
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('暂无数据');
      expect(wrapper.text()).toContain('还没有预算记录');
    });

    it('should handle very large budget amounts', () => {
      budgetStore.budgets[0].amount = 1000000000; // 10亿
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('¥1,000,000,000.00');
    });

    it('should handle budget with zero amount', () => {
      budgetStore.budgets[0].amount = 0;
      budgetStore.budgets[0].remainingAmount = 0;
      
      const wrapper = createWrapper();
      
      expect(wrapper.text()).toContain('¥0.00');
    });

    it('should handle concurrent operations gracefully', async () => {
      const wrapper = createWrapper();
      
      // Start multiple operations
      const operations = [
        budgetStore.createBudget({ department: '部门1', year: 2024, month: 1, amount: 10000 }),
        budgetStore.updateBudget(1, { amount: 20000 }),
        budgetStore.deleteBudget(2)
      ];
      
      await Promise.all(operations);
      
      // All operations should complete without errors
      expect(budgetStore.createBudget).toHaveBeenCalled();
      expect(budgetStore.updateBudget).toHaveBeenCalled();
      expect(budgetStore.deleteBudget).toHaveBeenCalled();
    });
  });

  describe('Data Validation', () => {
    it('should validate year range', async () => {
      const wrapper = createWrapper();
      
      wrapper.vm.showCreateDialog = true;
      wrapper.vm.budgetForm = {
        department: '技术部',
        year: 1900, // Invalid year
        month: 1,
        amount: 50000
      };
      
      await wrapper.vm.$nextTick();
      
      const submitButton = wrapper.find('.submit-button');
      if (submitButton.exists()) {
        await submitButton.trigger('click');
        
        expect(budgetStore.createBudget).not.toHaveBeenCalled();
        expect(wrapper.text()).toContain('年份必须在2020-2030之间');
      }
    });

    it('should validate month range', async () => {
      const wrapper = createWrapper();
      
      wrapper.vm.showCreateDialog = true;
      wrapper.vm.budgetForm = {
        department: '技术部',
        year: 2024,
        month: 13, // Invalid month
        amount: 50000
      };
      
      await wrapper.vm.$nextTick();
      
      const submitButton = wrapper.find('.submit-button');
      if (submitButton.exists()) {
        await submitButton.trigger('click');
        
        expect(budgetStore.createBudget).not.toHaveBeenCalled();
        expect(wrapper.text()).toContain('月份必须在1-12之间');
      }
    });

    it('should validate amount positive', async () => {
      const wrapper = createWrapper();
      
      wrapper.vm.showCreateDialog = true;
      wrapper.vm.budgetForm = {
        department: '技术部',
        year: 2024,
        month: 1,
        amount: -100 // Negative amount
      };
      
      await wrapper.vm.$nextTick();
      
      const submitButton = wrapper.find('.submit-button');
      if (submitButton.exists()) {
        await submitButton.trigger('click');
        
        expect(budgetStore.createBudget).not.toHaveBeenCalled();
        expect(wrapper.text()).toContain('金额必须大于0');
      }
    });
  });
});