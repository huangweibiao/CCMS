import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import ExpenseApplicationPage from '../../pages/ExpenseApplicationPage.vue';
import { useAuthStore } from '../../stores/auth';
import { useExpenseStore } from '../../stores/expense';
import type { ExpenseApplication } from '../../types/expense';

// Mock the stores
vi.mock('../../stores/auth');
vi.mock('../../stores/expense');

describe('ExpenseApplicationPage', () => {
  let mockAuthStore: any;
  let mockExpenseStore: any;

  beforeEach(() => {
    setActivePinia(createPinia());
    
    // Reset mocks
    mockAuthStore = {
      isAuthenticated: true,
      currentUser: {
        id: 1,
        username: 'testuser',
        role: 'USER'
      }
    };
    
    mockExpenseStore = {
      expenseApplications: [],
      loading: false,
      error: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loadExpenseApplications: vi.fn().mockResolvedValue({ success: true }),
      createExpenseApplication: vi.fn().mockResolvedValue({ success: true }),
      updateExpenseApplication: vi.fn().mockResolvedValue({ success: true }),
      deleteExpenseApplication: vi.fn().mockResolvedValue({ success: true }),
      getPendingApplications: vi.fn(() => []),
      getApprovedApplications: vi.fn(() => []),
      getRejectedApplications: vi.fn(() => [])
    };
    
    (useAuthStore as any).mockReturnValue(mockAuthStore);
    (useExpenseStore as any).mockReturnValue(mockExpenseStore);
  });

  describe('Initialization', () => {
    it('should load expense applications on mount when authenticated', async () => {
      const wrapper = mount(ExpenseApplicationPage);
      
      expect(mockExpenseStore.loadExpenseApplications).toHaveBeenCalledWith({
        page: 1,
        pageSize: 10
      });
    });

    it('should display loading state', async () => {
      mockExpenseStore.loading = true;
      const wrapper = mount(ExpenseApplicationPage);
      
      expect(wrapper.find('[data-test="loading"]').exists()).toBe(true);
      expect(wrapper.text()).toContain('加载中');
    });
  });

  describe('Expense Application List', () => {
    it('should display empty state when no applications exist', () => {
      mockExpenseStore.expenseApplications = [];
      const wrapper = mount(ExpenseApplicationPage);
      
      expect(wrapper.find('[data-test="empty-state"]').exists()).toBe(true);
      expect(wrapper.text()).toContain('暂无费用申请');
    });

    it('should display list of expense applications', () => {
      const mockApplications: ExpenseApplication[] = [
        {
          id: 1,
          title: '项目开发费用',
          amount: 5000,
          description: '项目相关费用',
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        },
        {
          id: 2,
          title: '办公用品采购',
          amount: 3000,
          description: '日常办公用品',
          department: '行政部',
          applicant: '李四',
          status: 'APPROVED',
          createdAt: '2024-01-10',
          updatedAt: '2024-01-12'
        }
      ];
      
      mockExpenseStore.expenseApplications = mockApplications;
      const wrapper = mount(ExpenseApplicationPage);
      
      const applicationElements = wrapper.findAll('[data-test="application-item"]');
      expect(applicationElements).toHaveLength(2);
      
      expect(wrapper.text()).toContain('项目开发费用');
      expect(wrapper.text()).toContain('5000');
      expect(wrapper.text()).toContain('办公用品采购');
      expect(wrapper.text()).toContain('3000');
    });

    it('should display application status correctly', () => {
      const mockApplications: ExpenseApplication[] = [
        {
          id: 1,
          title: '测试申请',
          amount: 1000,
          description: '测试',
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
      
      mockExpenseStore.expenseApplications = mockApplications;
      const wrapper = mount(ExpenseApplicationPage);
      
      expect(wrapper.find('[data-test="status-badge"]').text()).toContain('待审批');
    });
  });

  describe('Create Expense Application', () => {
    it('should show create form when create button is clicked', async () => {
      const wrapper = mount(ExpenseApplicationPage);
      
      const createButton = wrapper.find('[data-test="create-button"]');
      await createButton.trigger('click');
      
      expect(wrapper.find('[data-test="create-form"]').exists()).toBe(true);
    });

    it('should create new expense application successfully', async () => {
      const wrapper = mount(ExpenseApplicationPage);
      
      // Open create form
      await wrapper.find('[data-test="create-button"]').trigger('click');
      
      // Fill form
      await wrapper.find('[data-test="title-input"]').setValue('新费用申请');
      await wrapper.find('[data-test="amount-input"]').setValue('3000');
      await wrapper.find('[data-test="description-input"]').setValue('新申请描述');
      await wrapper.find('[data-test="department-select"]').setValue('技术部');
      
      // Submit form
      await wrapper.find('[data-test="submit-button"]').trigger('click');
      
      expect(mockExpenseStore.createExpenseApplication).toHaveBeenCalledWith({
        title: '新费用申请',
        amount: 3000,
        description: '新申请描述',
        department: '技术部'
      });
    });

    it('should validate form inputs before submission', async () => {
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="create-button"]').trigger('click');
      
      // Try to submit without filling required fields
      await wrapper.find('[data-test="submit-button"]').trigger('click');
      
      // Should show validation errors
      expect(wrapper.text()).toContain('请输入标题');
      expect(wrapper.text()).toContain('请输入金额');
      
      // Create function should not be called
      expect(mockExpenseStore.createExpenseApplication).not.toHaveBeenCalled();
    });

    it('should handle create failure gracefully', async () => {
      mockExpenseStore.createExpenseApplication = vi.fn().mockResolvedValue({
        success: false,
        error: '创建失败'
      });
      
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="create-button"]').trigger('click');
      await wrapper.find('[data-test="title-input"]').setValue('测试申请');
      await wrapper.find('[data-test="amount-input"]').setValue('1000');
      await wrapper.find('[data-test="submit-button"]').trigger('click');
      
      await nextTick();
      
      expect(wrapper.text()).toContain('创建失败');
    });
  });

  describe('Edit Expense Application', () => {
    it('should show edit form when edit button is clicked', async () => {
      const mockApplications: ExpenseApplication[] = [
        {
          id: 1,
          title: '原始申请',
          amount: 2000,
          description: '原始描述',
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
      
      mockExpenseStore.expenseApplications = mockApplications;
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="edit-button"]').trigger('click');
      
      expect(wrapper.find('[data-test="edit-form"]').exists()).toBe(true);
      expect(wrapper.find('[data-test="title-input"]').element.value).toBe('原始申请');
    });

    it('should update expense application successfully', async () => {
      const mockApplications: ExpenseApplication[] = [
        {
          id: 1,
          title: '原始申请',
          amount: 2000,
          description: '原始描述',
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
      
      mockExpenseStore.expenseApplications = mockApplications;
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="edit-button"]').trigger('click');
      await wrapper.find('[data-test="title-input"]').setValue('更新后的申请');
      await wrapper.find('[data-test="submit-button"]').trigger('click');
      
      expect(mockExpenseStore.updateExpenseApplication).toHaveBeenCalledWith(1, {
        title: '更新后的申请'
      });
    });

    it('should not allow editing approved or rejected applications', () => {
      const mockApplications: ExpenseApplication[] = [
        {
          id: 1,
          title: '已批准申请',
          amount: 2000,
          description: '描述',
          department: '技术部',
          applicant: '张三',
          status: 'APPROVED',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        },
        {
          id: 2,
          title: '已拒绝申请',
          amount: 1000,
          description: '描述',
          department: '技术部',
          applicant: '李四',
          status: 'REJECTED',
          createdAt: '2024-01-10',
          updatedAt: '2024-01-10'
        }
      ];
      
      mockExpenseStore.expenseApplications = mockApplications;
      const wrapper = mount(ExpenseApplicationPage);
      
      // Should not show edit button for approved/rejected applications
      const editButtons = wrapper.findAll('[data-test="edit-button"]');
      expect(editButtons).toHaveLength(0);
    });
  });

  describe('Delete Expense Application', () => {
    it('should show delete confirmation dialog', async () => {
      const mockApplications: ExpenseApplication[] = [
        {
          id: 1,
          title: '待删除申请',
          amount: 1000,
          description: '描述',
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
      
      mockExpenseStore.expenseApplications = mockApplications;
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="delete-button"]').trigger('click');
      
      expect(wrapper.find('[data-test="delete-dialog"]').exists()).toBe(true);
      expect(wrapper.text()).toContain('确认删除');
    });

    it('should delete expense application when confirmed', async () => {
      const mockApplications: ExpenseApplication[] = [
        {
          id: 1,
          title: '待删除申请',
          amount: 1000,
          description: '描述',
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
      
      mockExpenseStore.expenseApplications = mockApplications;
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="delete-button"]').trigger('click');
      await wrapper.find('[data-test="confirm-delete"]').trigger('click');
      
      expect(mockExpenseStore.deleteExpenseApplication).toHaveBeenCalledWith(1);
    });

    it('should cancel delete when user cancels', async () => {
      const mockApplications: ExpenseApplication[] = [
        {
          id: 1,
          title: '待删除申请',
          amount: 1000,
          description: '描述',
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
      
      mockExpenseStore.expenseApplications = mockApplications;
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="delete-button"]').trigger('click');
      await wrapper.find('[data-test="cancel-delete"]').trigger('click');
      
      expect(mockExpenseStore.deleteExpenseApplication).not.toHaveBeenCalled();
      expect(wrapper.find('[data-test="delete-dialog"]').exists()).toBe(false);
    });
  });

  describe('Pagination', () => {
    it('should handle pagination correctly', async () => {
      mockExpenseStore.expenseApplications = Array.from({ length: 25 }, (_, i) => ({
        id: i + 1,
        title: `申请 ${i + 1}`,
        amount: 1000 + i * 100,
        description: '描述',
        department: '技术部',
        applicant: '张三',
        status: 'PENDING',
        createdAt: '2024-01-15',
        updatedAt: '2024-01-15'
      }));
      
      mockExpenseStore.total = 25;
      const wrapper = mount(ExpenseApplicationPage);
      
      // Should show pagination
      expect(wrapper.find('[data-test="pagination"]').exists()).toBe(true);
      
      // Click next page
      await wrapper.find('[data-test="next-page"]').trigger('click');
      
      expect(mockExpenseStore.loadExpenseApplications).toHaveBeenCalledWith({
        page: 2,
        pageSize: 10
      });
    });

    it('should change page size', async () => {
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="page-size-select"]').setValue('20');
      
      expect(mockExpenseStore.loadExpenseApplications).toHaveBeenCalledWith({
        page: 1,
        pageSize: 20
      });
    });
  });

  describe('Filtering and Search', () => {
    it('should filter applications by status', async () => {
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="status-filter"]').setValue('PENDING');
      
      expect(mockExpenseStore.loadExpenseApplications).toHaveBeenCalledWith({
        page: 1,
        pageSize: 10,
        status: 'PENDING'
      });
    });

    it('should search applications by title', async () => {
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="search-input"]').setValue('项目');
      // Simulate debounced search
      await new Promise(resolve => setTimeout(resolve, 300));
      
      expect(mockExpenseStore.loadExpenseApplications).toHaveBeenCalledWith({
        page: 1,
        pageSize: 10,
        search: '项目'
      });
    });

    it('should reset filters', async () => {
      const wrapper = mount(ExpenseApplicationPage);
      
      // Set some filters first
      await wrapper.find('[data-test="status-filter"]').setValue('PENDING');
      await wrapper.find('[data-test="search-input"]').setValue('项目');
      
      // Reset filters
      await wrapper.find('[data-test="reset-filters"]').trigger('click');
      
      expect(wrapper.find('[data-test="status-filter"]').element.value).toBe('');
      expect(wrapper.find('[data-test="search-input"]').element.value).toBe('');
    });
  });

  describe('Error Handling', () => {
    it('should display error message when loading fails', () => {
      mockExpenseStore.error = '加载失败';
      const wrapper = mount(ExpenseApplicationPage);
      
      expect(wrapper.text()).toContain('加载失败');
    });

    it('should retry loading on retry button click', async () => {
      mockExpenseStore.error = '加载失败';
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="retry-button"]').trigger('click');
      
      expect(mockExpenseStore.loadExpenseApplications).toHaveBeenCalledWith({
        page: 1,
        pageSize: 10
      });
    });
  });

  describe('Permissions', () => {
    it('should show create button for authenticated users', () => {
      mockAuthStore.isAuthenticated = true;
      const wrapper = mount(ExpenseApplicationPage);
      
      expect(wrapper.find('[data-test="create-button"]').exists()).toBe(true);
    });

    it('should hide create button for unauthenticated users', () => {
      mockAuthStore.isAuthenticated = false;
      const wrapper = mount(ExpenseApplicationPage);
      
      expect(wrapper.find('[data-test="create-button"]').exists()).toBe(false);
    });

    it('should show edit/delete buttons only for pending applications', () => {
      const mockApplications: ExpenseApplication[] = [
        {
          id: 1,
          title: '待审批申请',
          amount: 1000,
          description: '描述',
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
      
      mockExpenseStore.expenseApplications = mockApplications;
      mockAuthStore.currentUser.id = 1; // Same as applicant
      const wrapper = mount(ExpenseApplicationPage);
      
      expect(wrapper.find('[data-test="edit-button"]').exists()).toBe(true);
      expect(wrapper.find('[data-test="delete-button"]').exists()).toBe(true);
    });

    it('should hide edit/delete buttons for other users', () => {
      const mockApplications: ExpenseApplication[] = [
        {
          id: 1,
          title: '他人申请',
          amount: 1000,
          description: '描述',
          department: '技术部',
          applicant: '李四', // Different applicant
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
      
      mockExpenseStore.expenseApplications = mockApplications;
      mockAuthStore.currentUser.id = 1; // Current user is different from applicant
      const wrapper = mount(ExpenseApplicationPage);
      
      expect(wrapper.find('[data-test="edit-button"]').exists()).toBe(false);
      expect(wrapper.find('[data-test="delete-button"]').exists()).toBe(false);
    });
  });

  describe('Edge Cases', () => {
    it('should handle very large amounts correctly', async () => {
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="create-button"]').trigger('click');
      await wrapper.find('[data-test="amount-input"]').setValue('1000000000');
      await wrapper.find('[data-test="submit-button"]').trigger('click');
      
      expect(mockExpenseStore.createExpenseApplication).toHaveBeenCalledWith(
        expect.objectContaining({
          amount: 1000000000
        })
      );
    });

    it('should handle network timeout during create', async () => {
      mockExpenseStore.createExpenseApplication = vi.fn().mockImplementation(() => 
        new Promise((_, reject) => setTimeout(() => reject(new Error('Timeout')), 5000))
      );
      
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="create-button"]').trigger('click');
      await wrapper.find('[data-test="title-input"]').setValue('测试申请');
      await wrapper.find('[data-test="amount-input"]').setValue('1000');
      
      // Mock a timeout by fast-forwarding time
      vi.useFakeTimers();
      wrapper.find('[data-test="submit-button"]').trigger('click');
      await vi.runAllTimersAsync();
      vi.useRealTimers();
      
      // Should show timeout error
      expect(wrapper.text()).toContain('请求超时');
    });

    it('should handle concurrent operations gracefully', async () => {
      const wrapper = mount(ExpenseApplicationPage);
      
      // Start multiple operations
      const createPromises = Array(3).fill(null).map(async () => {
        await wrapper.find('[data-test="create-button"]').trigger('click');
        await wrapper.find('[data-test="title-input"]').setValue('并发申请');
        await wrapper.find('[data-test="submit-button"]').trigger('click');
      });
      
      await Promise.all(createPromises);
      
      // Should handle all requests without crashing
      expect(mockExpenseStore.createExpenseApplication).toHaveBeenCalledTimes(3);
    });

    it('should handle empty form submissions gracefully', async () => {
      const wrapper = mount(ExpenseApplicationPage);
      
      await wrapper.find('[data-test="create-button"]').trigger('click');
      
      // Try to submit empty form
      await wrapper.find('[data-test="submit-button"]').trigger('click');
      
      // Should show validation errors, not crash
      expect(wrapper.text()).toContain('请输入标题');
      expect(mockExpenseStore.createExpenseApplication).not.toHaveBeenCalled();
    });
  });
});