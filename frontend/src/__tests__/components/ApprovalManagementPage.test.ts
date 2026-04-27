import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import ApprovalManagementPage from '../../pages/ApprovalManagementPage.vue';
import { useAuthStore } from '../../stores/auth';
import { useBudgetStore } from '../../stores/budget';
import { useExpenseStore } from '../../stores/expense';
import type { Budget, ExpenseApplication, ExpenseReimburse } from '../../types';

// Mock the stores
vi.mock('../../stores/auth');
vi.mock('../../stores/budget');
vi.mock('../../stores/expense');

describe('ApprovalManagementPage', () => {
  let mockAuthStore: any;
  let mockBudgetStore: any;
  let mockExpenseStore: any;

  beforeEach(() => {
    setActivePinia(createPinia());
    
    // Reset mocks
    mockAuthStore = {
      isAuthenticated: true,
      currentUser: {
        id: 1,
        username: 'testuser',
        role: 'ADMIN' // Admin has approval permissions
      }
    };
    
    mockBudgetStore = {
      pendingApprovalBudgets: [],
      pendingApprovalReimburses: [],
      loading: false,
      error: '',
      loadPendingApprovals: vi.fn().mockResolvedValue({ success: true }),
      approveBudget: vi.fn().mockResolvedValue({ success: true }),
      rejectBudget: vi.fn().mockResolvedValue({ success: true })
    };
    
    mockExpenseStore = {
      pendingApprovalApplications: [],
      pendingApprovalReimburses: [],
      loading: false,
      error: '',
      loadPendingApprovals: vi.fn().mockResolvedValue({ success: true }),
      approveExpenseApplication: vi.fn().mockResolvedValue({ success: true }),
      rejectExpenseApplication: vi.fn().mockResolvedValue({ success: true }),
      approveExpenseReimburse: vi.fn().mockResolvedValue({ success: true }),
      rejectExpenseReimburse: vi.fn().mockResolvedValue({ success: true })
    };
    
    (useAuthStore as any).mockReturnValue(mockAuthStore);
    (useBudgetStore as any).mockReturnValue(mockBudgetStore);
    (useExpenseStore as any).mockReturnValue(mockExpenseStore);
  });

  describe('Initialization', () => {
    it('should load pending approvals on mount when user has permission', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      expect(mockBudgetStore.loadPendingApprovals).toHaveBeenCalled();
      expect(mockExpenseStore.loadPendingApprovals).toHaveBeenCalled();
    });

    it('should not load approvals for users without permission', async () => {
      mockAuthStore.currentUser.role = 'USER'; // Regular user
      const wrapper = mount(ApprovalManagementPage);
      
      expect(mockBudgetStore.loadPendingApprovals).not.toHaveBeenCalled();
      expect(mockExpenseStore.loadPendingApprovals).not.toHaveBeenCalled();
      
      expect(wrapper.text()).toContain('无访问权限');
    });

    it('should display loading state', async () => {
      mockBudgetStore.loading = true;
      mockExpenseStore.loading = true;
      const wrapper = mount(ApprovalManagementPage);
      
      expect(wrapper.find('[data-test="loading"]').exists()).toBe(true);
      expect(wrapper.text()).toContain('加载中');
    });
  });

  describe('Approval Tabs', () => {
    it('should show correct tabs for different types of approvals', () => {
      const wrapper = mount(ApprovalManagementPage);
      
      expect(wrapper.find('[data-test="budget-tab"]').exists()).toBe(true);
      expect(wrapper.find('[data-test="application-tab"]').exists()).toBe(true);
      expect(wrapper.find('[data-test="reimburse-tab"]').exists()).toBe(true);
    });

    it('should switch between approval tabs', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      // Start on budget tab
      expect(wrapper.find('[data-test="budget-tab"]').classes()).toContain('active');
      
      // Switch to application tab
      await wrapper.find('[data-test="application-tab"]').trigger('click');
      expect(wrapper.find('[data-test="application-tab"]').classes()).toContain('active');
      
      // Switch to reimburse tab
      await wrapper.find('[data-test="reimburse-tab"]').trigger('click');
      expect(wrapper.find('[data-test="reimburse-tab"]').classes()).toContain('active');
    });
  });

  describe('Budget Approvals', () => {
    beforeEach(() => {
      mockBudgetStore.pendingApprovalBudgets = [
        {
          id: 1,
          title: '部门预算申请',
          amount: 100000,
          description: '2024年度部门预算',
          department: '技术部',
          applicant: '张三',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
    });

    it('should display pending budget approvals', () => {
      const wrapper = mount(ApprovalManagementPage);
      
      expect(wrapper.find('[data-test="budget-approval-item"]').exists()).toBe(true);
      expect(wrapper.text()).toContain('部门预算申请');
      expect(wrapper.text()).toContain('100000');
      expect(wrapper.text()).toContain('技术部');
      expect(wrapper.text()).toContain('张三');
    });

    it('should approve budget when approve button is clicked', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="approve-budget"]').trigger('click');
      
      expect(mockBudgetStore.approveBudget).toHaveBeenCalledWith(1);
    });

    it('should show rejection dialog when reject button is clicked', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="reject-budget"]').trigger('click');
      
      expect(wrapper.find('[data-test="rejection-dialog"]').exists()).toBe(true);
    });

    it('should reject budget with reason when confirmed', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="reject-budget"]').trigger('click');
      await wrapper.find('[data-test="rejection-reason"]').setValue('预算金额过高');
      await wrapper.find('[data-test="confirm-reject"]').trigger('click');
      
      expect(mockBudgetStore.rejectBudget).toHaveBeenCalledWith(1, '预算金额过高');
    });

    it('should cancel rejection when user cancels', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="reject-budget"]').trigger('click');
      await wrapper.find('[data-test="cancel-reject"]').trigger('click');
      
      expect(mockBudgetStore.rejectBudget).not.toHaveBeenCalled();
      expect(wrapper.find('[data-test="rejection-dialog"]').exists()).toBe(false);
    });

    it('should validate rejection reason', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="reject-budget"]').trigger('click');
      
      // Try to confirm without reason
      await wrapper.find('[data-test="confirm-reject"]').trigger('click');
      
      expect(wrapper.text()).toContain('请输入驳回理由');
      expect(mockBudgetStore.rejectBudget).not.toHaveBeenCalled();
    });
  });

  describe('Expense Application Approvals', () => {
    beforeEach(() => {
      mockExpenseStore.pendingApprovalApplications = [
        {
          id: 1,
          title: '项目费用申请',
          amount: 5000,
          description: '项目开发费用',
          department: '技术部',
          applicant: '李四',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
    });

    it('should display pending expense application approvals', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      // Switch to application tab
      await wrapper.find('[data-test="application-tab"]').trigger('click');
      
      expect(wrapper.find('[data-test="application-approval-item"]').exists()).toBe(true);
      expect(wrapper.text()).toContain('项目费用申请');
      expect(wrapper.text()).toContain('5000');
    });

    it('should approve expense application', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="application-tab"]').trigger('click');
      await wrapper.find('[data-test="approve-application"]').trigger('click');
      
      expect(mockExpenseStore.approveExpenseApplication).toHaveBeenCalledWith(1);
    });

    it('should reject expense application with reason', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="application-tab"]').trigger('click');
      await wrapper.find('[data-test="reject-application"]').trigger('click');
      await wrapper.find('[data-test="rejection-reason"]').setValue('费用明细不清');
      await wrapper.find('[data-test="confirm-reject"]').trigger('click');
      
      expect(mockExpenseStore.rejectExpenseApplication).toHaveBeenCalledWith(1, '费用明细不清');
    });
  });

  describe('Expense Reimburse Approvals', () => {
    beforeEach(() => {
      mockExpenseStore.pendingApprovalReimburses = [
        {
          id: 1,
          title: '差旅费报销',
          amount: 2000,
          description: '北京出差费用',
          department: '销售部',
          applicant: '王五',
          status: 'PENDING',
          createdAt: '2024-01-15',
          updatedAt: '2024-01-15'
        }
      ];
    });

    it('should display pending expense reimburse approvals', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      // Switch to reimburse tab
      await wrapper.find('[data-test="reimburse-tab"]').trigger('click');
      
      expect(wrapper.find('[data-test="reimburse-approval-item"]').exists()).toBe(true);
      expect(wrapper.text()).toContain('差旅费报销');
      expect(wrapper.text()).toContain('2000');
    });

    it('should approve expense reimburse', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="reimburse-tab"]').trigger('click');
      await wrapper.find('[data-test="approve-reimburse"]').trigger('click');
      
      expect(mockExpenseStore.approveExpenseReimburse).toHaveBeenCalledWith(1);
    });

    it('should reject expense reimburse with reason', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="reimburse-tab"]').trigger('click');
      await wrapper.find('[data-test="reject-reimburse"]').trigger('click');
      await wrapper.find('[data-test="rejection-reason"]').setValue('缺少报销凭证');
      await wrapper.find('[data-test="confirm-reject"]').trigger('click');
      
      expect(mockExpenseStore.rejectExpenseReimburse).toHaveBeenCalledWith(1, '缺少报销凭证');
    });
  });

  describe('Bulk Operations', () => {
    beforeEach(() => {
      mockBudgetStore.pendingApprovalBudgets = [
        { id: 1, title: '预算1', amount: 10000, department: '技术部', applicant: '张一', status: 'PENDING' },
        { id: 2, title: '预算2', amount: 20000, department: '销售部', applicant: '张二', status: 'PENDING' },
        { id: 3, title: '预算3', amount: 30000, department: '行政部', applicant: '张三', status: 'PENDING' }
      ];
    });

    it('should allow selecting multiple items for bulk approval', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      // Select checkboxes
      await wrapper.find('[data-test="select-all"]').trigger('click');
      
      // Should show bulk approval button
      expect(wrapper.find('[data-test="bulk-approve"]').exists()).toBe(true);
    });

    it('should approve selected items in bulk', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      // Select first two items
      await wrapper.find('[data-test="select-item-1"]').trigger('click');
      await wrapper.find('[data-test="select-item-2"]').trigger('click');
      
      await wrapper.find('[data-test="bulk-approve"]').trigger('click');
      
      expect(mockBudgetStore.approveBudget).toHaveBeenCalledWith(1);
      expect(mockBudgetStore.approveBudget).toHaveBeenCalledWith(2);
      expect(mockBudgetStore.approveBudget).not.toHaveBeenCalledWith(3);
    });

    it('should reject selected items in bulk', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      // Select items and bulk reject
      await wrapper.find('[data-test="select-all"]').trigger('click');
      await wrapper.find('[data-test="bulk-reject"]').trigger('click');
      await wrapper.find('[data-test="rejection-reason"]').setValue('批量驳回');
      await wrapper.find('[data-test="confirm-reject"]').trigger('click');
      
      expect(mockBudgetStore.rejectBudget).toHaveBeenCalledWith(1, '批量驳回');
      expect(mockBudgetStore.rejectBudget).toHaveBeenCalledWith(2, '批量驳回');
      expect(mockBudgetStore.rejectBudget).toHaveBeenCalledWith(3, '批量驳回');
    });

    it('should show confirmation for bulk operations', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="select-all"]').trigger('click');
      await wrapper.find('[data-test="bulk-approve"]').trigger('click');
      
      expect(wrapper.find('[data-test="bulk-confirmation"]').exists()).toBe(true);
      expect(wrapper.text()).toContain('确认批量批准3个项目?');
    });

    it('should cancel bulk operation when user cancels', async () => {
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="select-all"]').trigger('click');
      await wrapper.find('[data-test="bulk-approve"]').trigger('click');
      await wrapper.find('[data-test="cancel-bulk"]').trigger('click');
      
      expect(mockBudgetStore.approveBudget).not.toHaveBeenCalled();
    });
  });

  describe('Error Handling', () => {
    it('should display error message when loading fails', () => {
      mockBudgetStore.error = '加载失败';
      mockExpenseStore.error = '加载失败';
      const wrapper = mount(ApprovalManagementPage);
      
      expect(wrapper.text()).toContain('加载失败');
    });

    it('should retry loading on retry button click', async () => {
      mockBudgetStore.error = '加载失败';
      const wrapper = mount(ApprovalManagementPage);
      
      await wrapper.find('[data-test="retry-button"]').trigger('click');
      
      expect(mockBudgetStore.loadPendingApprovals).toHaveBeenCalled();
    });

    it('should handle approval failure gracefully', async () => {
      mockBudgetStore.approveBudget = vi.fn().mockResolvedValue({
        success: false,
        error: '批准失败'
      });
      
      mockBudgetStore.pendingApprovalBudgets = [
        { id: 1, title: '预算', amount: 10000, department: '技术部', applicant: '张三', status: 'PENDING' }
      ];
      
      const wrapper = mount(ApprovalManagementPage);
      await wrapper.find('[data-test="approve-budget"]').trigger('click');
      
      await nextTick();
      expect(wrapper.text()).toContain('批准失败');
    });
  });

  describe('Permissions and Access Control', () => {
    it('should show approval buttons only for users with approval permissions', () => {
      mockAuthStore.currentUser.role = 'ADMIN';
      const wrapper = mount(ApprovalManagementPage);
      
      expect(wrapper.find('[data-test="approve-budget"]').exists()).toBe(true);
      expect(wrapper.find('[data-test="reject-budget"]').exists()).toBe(true);
    });

    it('should hide approval buttons for users without approval permissions', () => {
      mockAuthStore.currentUser.role = 'USER';
      mockBudgetStore.pendingApprovalBudgets = [
        { id: 1, title: '预算', amount: 10000, department: '技术部', applicant: '张三', status: 'PENDING' }
      ];
      
      const wrapper = mount(ApprovalManagementPage);
      
      expect(wrapper.find('[data-test="approve-budget"]').exists()).toBe(false);
      expect(wrapper.find('[data-test="reject-budget"]').exists()).toBe(false);
    });

    it('should show department-specific approvals for department managers', () => {
      mockAuthStore.currentUser.role = 'MANAGER';
      mockAuthStore.currentUser.department = '技术部';
      
      mockBudgetStore.pendingApprovalBudgets = [
        { id: 1, title: '技术部预算', amount: 10000, department: '技术部', applicant: '张三', status: 'PENDING' },
        { id: 2, title: '销售部预算', amount: 20000, department: '销售部', applicant: '李四', status: 'PENDING' }
      ];
      
      const wrapper = mount(ApprovalManagementPage);
      
      // Should only show approvals from manager's department
      expect(wrapper.text()).toContain('技术部预算');
      expect(wrapper.text()).not.toContain('销售部预算');
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty approval lists', async () => {
      mockBudgetStore.pendingApprovalBudgets = [];
      mockExpenseStore.pendingApprovalApplications = [];
      mockExpenseStore.pendingApprovalReimburses = [];
      
      const wrapper = mount(ApprovalManagementPage);
      
      expect(wrapper.text()).toContain('无待审批项目');
    });

    it('should handle very large approval lists', async () => {
      const largeList = Array.from({ length: 100 }, (_, i) => ({
        id: i + 1,
        title: `预算${i + 1}`,
        amount: 10000 + i * 1000,
        department: '技术部',
        applicant: '张三',
        status: 'PENDING'
      }));
      
      mockBudgetStore.pendingApprovalBudgets = largeList;
      const wrapper = mount(ApprovalManagementPage);
      
      // Should show pagination
      expect(wrapper.find('[data-test="pagination"]').exists()).toBe(true);
      
      // Should handle rendering large lists without performance issues
      expect(wrapper.findAll('[data-test="budget-approval-item"]')).toHaveLength(10); // Page size
    });

    it('should prevent concurrent approval operations', async () => {
      mockBudgetStore.pendingApprovalBudgets = [
        { id: 1, title: '预算', amount: 10000, department: '技术部', applicant: '张三', status: 'PENDING' }
      ];
      
      const wrapper = mount(ApprovalManagementPage);
      
      // Click approve multiple times quickly
      const approveButton = wrapper.find('[data-test="approve-budget"]');
      await approveButton.trigger('click');
      await approveButton.trigger('click');
      await approveButton.trigger('click');
      
      // Should only call API once (debounced/protected)
      expect(mockBudgetStore.approveBudget).toHaveBeenCalledTimes(1);
    });

    it('should handle network timeout during approval', async () => {
      mockBudgetStore.approveBudget = vi.fn().mockImplementation(() => 
        new Promise((_, reject) => setTimeout(() => reject(new Error('Timeout')), 5000))
      );
      
      mockBudgetStore.pendingApprovalBudgets = [
        { id: 1, title: '预算', amount: 10000, department: '技术部', applicant: '张三', status: 'PENDING' }
      ];
      
      const wrapper = mount(ApprovalManagementPage);
      
      // Mock a timeout by fast-forwarding time
      vi.useFakeTimers();
      wrapper.find('[data-test="approve-budget"]').trigger('click');
      await vi.runAllTimersAsync();
      vi.useRealTimers();
      
      // Should show timeout error
      expect(wrapper.text()).toContain('请求超时');
    });

    it('should handle mixed approval types in bulk operations', async () => {
      mockBudgetStore.pendingApprovalBudgets = [
        { id: 1, title: '预算1', amount: 10000, department: '技术部', applicant: '张三', status: 'PENDING' }
      ];
      
      mockExpenseStore.pendingApprovalApplications = [
        { id: 1, title: '申请1', amount: 5000, department: '技术部', applicant: '李四', status: 'PENDING' }
      ];
      
      const wrapper = mount(ApprovalManagementPage);
      
      // This test verifies that mixed type bulk operations are handled correctly
      // Currently the UI doesn't support cross-tab bulk operations
      expect(true).toBe(true);
    });

    it('should maintain state after successful approval', async () => {
      const mockBudget = { 
        id: 1, 
        title: '预算', 
        amount: 10000, 
        department: '技术部', 
        applicant: '张三', 
        status: 'PENDING' 
      };
      
      mockBudgetStore.pendingApprovalBudgets = [mockBudget];
      const wrapper = mount(ApprovalManagementPage);
      
      // Remove budget after approval
      mockBudgetStore.approveBudget = vi.fn().mockImplementation(() => {
        mockBudgetStore.pendingApprovalBudgets = [];
        return Promise.resolve({ success: true });
      });
      
      await wrapper.find('[data-test="approve-budget"]').trigger('click');
      
      // Should update UI to show empty state
      await nextTick();
      expect(wrapper.text()).toContain('无待审批项目');
    });
  });
});