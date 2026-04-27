import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useBudgetStore } from '../../stores/budget';
import { fetchBudgets, createBudget, updateBudget, deleteBudget, approveBudget } from '../../api/budget';
import type { Budget, BudgetForm } from '../../types/budget';

// Mock the budget API
vi.mock('../../api/budget', () => ({
  fetchBudgets: vi.fn(),
  createBudget: vi.fn(),
  updateBudget: vi.fn(),
  deleteBudget: vi.fn(),
  approveBudget: vi.fn(),
}));

describe('Budget Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  describe('Initial State', () => {
    it('should initialize with default values', () => {
      const budgetStore = useBudgetStore();
      
      expect(budgetStore.budgets).toEqual([]);
      expect(budgetStore.loading).toBe(false);
      expect(budgetStore.error).toBe('');
      expect(budgetStore.currentPage).toBe(1);
      expect(budgetStore.pageSize).toBe(10);
      expect(budgetStore.total).toBe(0);
    });
  });

  describe('Load Budgets Action', () => {
    it('should load budgets successfully', async () => {
      const mockBudgets: Budget[] = [
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
      ];
      
      const mockResponse = {
        data: mockBudgets,
        total: 2,
        page: 1,
        pageSize: 10
      };
      
      (fetchBudgets as any).mockResolvedValue(mockResponse);
      
      const budgetStore = useBudgetStore();
      const result = await budgetStore.loadBudgets({ page: 1, pageSize: 10 });
      
      expect(fetchBudgets).toHaveBeenCalledWith({ page: 1, pageSize: 10 });
      expect(budgetStore.budgets).toEqual(mockBudgets);
      expect(budgetStore.total).toBe(2);
      expect(budgetStore.currentPage).toBe(1);
      expect(budgetStore.pageSize).toBe(10);
      expect(budgetStore.loading).toBe(false);
      expect(budgetStore.error).toBe('');
      expect(result.success).toBe(true);
    });

    it('should handle load budgets failure', async () => {
      const errorMessage = '获取预算数据失败';
      (fetchBudgets as any).mockRejectedValue(new Error(errorMessage));
      
      const budgetStore = useBudgetStore();
      const result = await budgetStore.loadBudgets({ page: 1 });
      
      expect(fetchBudgets).toHaveBeenCalledWith({ page: 1 });
      expect(budgetStore.budgets).toEqual([]);
      expect(budgetStore.total).toBe(0);
      expect(budgetStore.loading).toBe(false);
      expect(budgetStore.error).toBe(errorMessage);
      expect(result.success).toBe(false);
      expect(result.message).toBe(errorMessage);
    });

    it('should set loading state during API call', async () => {
      let resolvePromise: (value: any) => void;
      const promise = new Promise(resolve => {
        resolvePromise = resolve;
      });
      
      (fetchBudgets as any).mockReturnValue(promise);
      
      const budgetStore = useBudgetStore();
      const loadPromise = budgetStore.loadBudgets({ page: 1 });
      
      // Should be loading during API call
      expect(budgetStore.loading).toBe(true);
      
      // Resolve the promise
      resolvePromise!({ data: [], total: 0 });
      await loadPromise;
      
      // Loading should be false after API call
      expect(budgetStore.loading).toBe(false);
    });
  });

  describe('Create Budget Action', () => {
    it('should create budget successfully', async () => {
      const budgetForm: BudgetForm = {
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 50000
      };
      
      const mockBudget: Budget = {
        id: 1,
        ...budgetForm,
        usedAmount: 0,
        remainingAmount: 50000,
        status: 'PENDING',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      };
      
      (createBudget as any).mockResolvedValue(mockBudget);
      
      const budgetStore = useBudgetStore();
      budgetStore.budgets = []; // Ensure initial state is empty
      
      const result = await budgetStore.createBudget(budgetForm);
      
      expect(createBudget).toHaveBeenCalledWith(budgetForm);
      expect(budgetStore.budgets).toContainEqual(mockBudget);
      expect(result.success).toBe(true);
      expect(result.data).toEqual(mockBudget);
    });

    it('should handle create budget failure', async () => {
      const budgetForm: BudgetForm = {
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 50000
      };
      
      const errorMessage = '创建预算失败';
      (createBudget as any).mockRejectedValue(new Error(errorMessage));
      
      const budgetStore = useBudgetStore();
      budgetStore.budgets = [];
      
      const result = await budgetStore.createBudget(budgetForm);
      
      expect(createBudget).toHaveBeenCalledWith(budgetForm);
      expect(budgetStore.budgets).toEqual([]); // Should not add to list
      expect(result.success).toBe(false);
      expect(result.message).toBe(errorMessage);
    });

    it('should validate budget form before creating', async () => {
      const budgetForm: BudgetForm = {
        department: '',
        year: 0,
        month: 0,
        amount: -100
      };
      
      const budgetStore = useBudgetStore();
      const result = await budgetStore.createBudget(budgetForm);
      
      expect(createBudget).not.toHaveBeenCalled();
      expect(result.success).toBe(false);
      expect(result.message).toContain('部门不能为空');
    });
  });

  describe('Update Budget Action', () => {
    it('should update budget successfully', async () => {
      const existingBudget: Budget = {
        id: 1,
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 50000,
        usedAmount: 25000,
        remainingAmount: 25000,
        status: 'PENDING',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      };
      
      const updateData = { amount: 60000 };
      const updatedBudget = { ...existingBudget, ...updateData };
      
      (updateBudget as any).mockResolvedValue(updatedBudget);
      
      const budgetStore = useBudgetStore();
      budgetStore.budgets = [existingBudget];
      
      const result = await budgetStore.updateBudget(1, updateData);
      
      expect(updateBudget).toHaveBeenCalledWith(1, updateData);
      expect(budgetStore.budgets).toContainEqual(updatedBudget);
      expect(result.success).toBe(true);
      expect(result.data).toEqual(updatedBudget);
    });

    it('should handle update budget failure', async () => {
      const existingBudget: Budget = {
        id: 1,
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 50000,
        usedAmount: 25000,
        remainingAmount: 25000,
        status: 'PENDING',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      };
      
      const errorMessage = '更新预算失败';
      (updateBudget as any).mockRejectedValue(new Error(errorMessage));
      
      const budgetStore = useBudgetStore();
      budgetStore.budgets = [existingBudget];
      
      const result = await budgetStore.updateBudget(1, { amount: 60000 });
      
      expect(updateBudget).toHaveBeenCalledWith(1, { amount: 60000 });
      expect(budgetStore.budgets).toContainEqual(existingBudget); // Should not update
      expect(result.success).toBe(false);
      expect(result.message).toBe(errorMessage);
    });

    it('should handle update non-existent budget', async () => {
      const budgetStore = useBudgetStore();
      budgetStore.budgets = []; // Empty list
      
      const result = await budgetStore.updateBudget(999, { amount: 60000 });
      
      expect(updateBudget).toHaveBeenCalledWith(999, { amount: 60000 });
      expect(result.success).toBe(false);
      expect(result.message).toBe('预算不存在');
    });
  });

  describe('Delete Budget Action', () => {
    it('should delete budget successfully', async () => {
      const budgetToDelete: Budget = {
        id: 1,
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 50000,
        usedAmount: 25000,
        remainingAmount: 25000,
        status: 'PENDING',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      };
      
      const otherBudget: Budget = {
        id: 2,
        department: '销售部',
        year: 2024,
        month: 1,
        amount: 30000,
        usedAmount: 15000,
        remainingAmount: 15000,
        status: 'APPROVED',
        createdAt: '2024-01-02',
        updatedAt: '2024-01-02'
      };
      
      (deleteBudget as any).mockResolvedValue({ success: true });
      
      const budgetStore = useBudgetStore();
      budgetStore.budgets = [budgetToDelete, otherBudget];
      
      const result = await budgetStore.deleteBudget(1);
      
      expect(deleteBudget).toHaveBeenCalledWith(1);
      expect(budgetStore.budgets).not.toContainEqual(budgetToDelete);
      expect(budgetStore.budgets).toContainEqual(otherBudget);
      expect(result.success).toBe(true);
    });

    it('should handle delete budget failure', async () => {
      const budgetToDelete: Budget = {
        id: 1,
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 50000,
        usedAmount: 25000,
        remainingAmount: 25000,
        status: 'PENDING',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      };
      
      const errorMessage = '删除预算失败';
      (deleteBudget as any).mockRejectedValue(new Error(errorMessage));
      
      const budgetStore = useBudgetStore();
      budgetStore.budgets = [budgetToDelete];
      
      const result = await budgetStore.deleteBudget(1);
      
      expect(deleteBudget).toHaveBeenCalledWith(1);
      expect(budgetStore.budgets).toContainEqual(budgetToDelete); // Should not delete
      expect(result.success).toBe(false);
      expect(result.message).toBe(errorMessage);
    });
  });

  describe('Approve Budget Action', () => {
    it('should approve budget successfully', async () => {
      const budgetToApprove: Budget = {
        id: 1,
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 50000,
        usedAmount: 0,
        remainingAmount: 50000,
        status: 'PENDING',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      };
      
      const approvedBudget = { ...budgetToApprove, status: 'APPROVED' };
      (approveBudget as any).mockResolvedValue(approvedBudget);
      
      const budgetStore = useBudgetStore();
      budgetStore.budgets = [budgetToApprove];
      
      const result = await budgetStore.approveBudget(1);
      
      expect(approveBudget).toHaveBeenCalledWith(1);
      expect(budgetStore.budgets).toContainEqual(approvedBudget);
      expect(result.success).toBe(true);
      expect(result.data).toEqual(approvedBudget);
    });

    it('should handle approve budget failure', async () => {
      const budgetToApprove: Budget = {
        id: 1,
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 50000,
        usedAmount: 0,
        remainingAmount: 50000,
        status: 'PENDING',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      };
      
      const errorMessage = '审批预算失败';
      (approveBudget as any).mockRejectedValue(new Error(errorMessage));
      
      const budgetStore = useBudgetStore();
      budgetStore.budgets = [budgetToApprove];
      
      const result = await budgetStore.approveBudget(1);
      
      expect(approveBudget).toHaveBeenCalledWith(1);
      expect(budgetStore.budgets).toContainEqual(budgetToApprove); // Should not update
      expect(result.success).toBe(false);
      expect(result.message).toBe(errorMessage);
    });
  });

  describe('Getters', () => {
    beforeEach(() => {
      const budgetStore = useBudgetStore();
      budgetStore.budgets = [
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
        },
        {
          id: 3,
          department: '技术部',
          year: 2024,
          month: 2,
          amount: 40000,
          usedAmount: 20000,
          remainingAmount: 20000,
          status: 'REJECTED',
          createdAt: '2024-02-01',
          updatedAt: '2024-02-01'
        }
      ];
    });

    describe('approvedBudgets', () => {
      it('should return only approved budgets', () => {
        const budgetStore = useBudgetStore();
        const approved = budgetStore.approvedBudgets;
        
        expect(approved).toHaveLength(1);
        expect(approved[0].status).toBe('APPROVED');
        expect(approved[0].id).toBe(1);
      });
    });

    describe('pendingBudgets', () => {
      it('should return only pending budgets', () => {
        const budgetStore = useBudgetStore();
        const pending = budgetStore.pendingBudgets;
        
        expect(pending).toHaveLength(1);
        expect(pending[0].status).toBe('PENDING');
        expect(pending[0].id).toBe(2);
      });
    });

    describe('rejectedBudgets', () => {
      it('should return only rejected budgets', () => {
        const budgetStore = useBudgetStore();
        const rejected = budgetStore.rejectedBudgets;
        
        expect(rejected).toHaveLength(1);
        expect(rejected[0].status).toBe('REJECTED');
        expect(rejected[0].id).toBe(3);
      });
    });

    describe('getBudgetByDepartment', () => {
      it('should return budgets for specific department', () => {
        const budgetStore = useBudgetStore();
        const techBudgets = budgetStore.getBudgetByDepartment('技术部');
        
        expect(techBudgets).toHaveLength(2);
        techBudgets.forEach(budget => {
          expect(budget.department).toBe('技术部');
        });
      });

      it('should return empty array for non-existent department', () => {
        const budgetStore = useBudgetStore();
        const hrBudgets = budgetStore.getBudgetByDepartment('人事部');
        
        expect(hrBudgets).toEqual([]);
      });
    });

    describe('getBudgetByYearMonth', () => {
      it('should return budget for specific year and month', () => {
        const budgetStore = useBudgetStore();
        const janBudget = budgetStore.getBudgetByYearMonth(2024, 1);
        
        expect(janBudget).toHaveLength(2);
        janBudget.forEach(budget => {
          expect(budget.year).toBe(2024);
          expect(budget.month).toBe(1);
        });
      });

      it('should return empty array for non-existent period', () => {
        const budgetStore = useBudgetStore();
        const marBudget = budgetStore.getBudgetByYearMonth(2024, 3);
        
        expect(marBudget).toEqual([]);
      });
    });

    describe('totalBudgetAmount', () => {
      it('should calculate total budget amount', () => {
        const budgetStore = useBudgetStore();
        const total = budgetStore.totalBudgetAmount;
        
        expect(total).toBe(50000 + 30000 + 40000);
      });
    });

    describe('totalUsedAmount', () => {
      it('should calculate total used amount', () => {
        const budgetStore = useBudgetStore();
        const total = budgetStore.totalUsedAmount;
        
        expect(total).toBe(25000 + 15000 + 20000);
      });
    });

    describe('totalRemainingAmount', () => {
      it('should calculate total remaining amount', () => {
        const budgetStore = useBudgetStore();
        const total = budgetStore.totalRemainingAmount;
        
        expect(total).toBe(25000 + 15000 + 20000);
      });
    });
  });

  describe('Edge Cases', () => {
    it('should handle very large budget amounts', async () => {
      const budgetForm: BudgetForm = {
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 1000000000 // 10亿
      };
      
      const mockBudget: Budget = {
        id: 1,
        ...budgetForm,
        usedAmount: 0,
        remainingAmount: 1000000000,
        status: 'PENDING',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      };
      
      (createBudget as any).mockResolvedValue(mockBudget);
      
      const budgetStore = useBudgetStore();
      const result = await budgetStore.createBudget(budgetForm);
      
      expect(result.success).toBe(true);
      expect(result.data.amount).toBe(1000000000);
    });

    it('should handle pagination with large data sets', async () => {
      // Mock a large dataset response
      const largeData = Array.from({ length: 1000 }, (_, i) => ({
        id: i + 1,
        department: `部门${i % 5}`,
        year: 2024,
        month: (i % 12) + 1,
        amount: 10000 + i * 100,
        usedAmount: 5000 + i * 50,
        remainingAmount: 5000 + i * 50,
        status: i % 3 === 0 ? 'APPROVED' : i % 3 === 1 ? 'PENDING' : 'REJECTED',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      }));
      
      (fetchBudgets as any).mockResolvedValue({
        data: largeData.slice(0, 10), // Return first page
        total: 1000,
        page: 1,
        pageSize: 10
      });
      
      const budgetStore = useBudgetStore();
      await budgetStore.loadBudgets({ page: 1, pageSize: 10 });
      
      expect(budgetStore.budgets).toHaveLength(10);
      expect(budgetStore.total).toBe(1000);
      expect(budgetStore.currentPage).toBe(1);
      expect(budgetStore.pageSize).toBe(10);
    });

    it('should handle concurrent operations gracefully', async () => {
      const budgetForm: BudgetForm = {
        department: '技术部',
        year: 2024,
        month: 1,
        amount: 50000
      };
      
      (createBudget as any).mockResolvedValue({
        id: 1,
        ...budgetForm,
        usedAmount: 0,
        remainingAmount: 50000,
        status: 'PENDING',
        createdAt: '2024-01-01',
        updatedAt: '2024-01-01'
      });
      
      const budgetStore = useBudgetStore();
      
      // Start multiple create operations concurrently
      const promises = [
        budgetStore.createBudget(budgetForm),
        budgetStore.createBudget(budgetForm),
        budgetStore.createBudget(budgetForm)
      ];
      
      const results = await Promise.all(promises);
      
      // All should succeed
      results.forEach(result => {
        expect(result.success).toBe(true);
      });
      
      // Should have created 3 budgets
      expect(budgetStore.budgets).toHaveLength(3);
    });
  });
});