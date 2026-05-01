import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { ElForm, ElFormItem, ElInput, ElSelect, ElDatePicker, ElButton } from 'element-plus';
import ExpenseForm from '@/components/expense/ExpenseForm.vue';

const mockBudgetList = [
  { id: 1, name: '差旅费预算', balance: 10000 },
  { id: 2, name: '办公费预算', balance: 5000 }
];

const mockFeeTypes = [
  { id: 1, name: '差旅费', description: '出差相关费用' },
  { id: 2, name: '接待费', description: '客户接待费用' }
];

describe('ExpenseForm', () => {
  let wrapper;

  beforeEach(() => {
    wrapper = mount(ExpenseForm, {
      props: {
        budgetList: mockBudgetList,
        feeTypes: mockFeeTypes,
        isEditing: false
      },
      global: {
        stubs: {
          'el-form': ElForm,
          'el-form-item': ElFormItem,
          'el-input': ElInput,
          'el-select': ElSelect,
          'el-date-picker': ElDatePicker,
          'el-button': ElButton
        }
      }
    });
  });

  it('renders all form fields correctly', () => {
    expect(wrapper.find('[data-testid="expense-title"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="expense-amount"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="expense-budget"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="expense-fee-type"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="expense-date"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="expense-description"]').exists()).toBe(true);
  });

  it('validates required form fields', async () => {
    const form = wrapper.findComponent(ElForm);
    
    // 提交空表单
    await wrapper.vm.submitForm();
    
    // 应该显示验证错误
    expect(wrapper.vm.$refs.formRef.validate).toHaveBeenCalled();
  });

  it('adds expense detail when clicking add button', async () => {
    const initialDetailCount = wrapper.vm.form.details.length;
    
    await wrapper.find('[data-testid="add-detail-btn"]').trigger('click');
    
    expect(wrapper.vm.form.details.length).toBe(initialDetailCount + 1);
  });

  it('removes expense detail when clicking delete button', async () => {
    // 先添加一个明细
    await wrapper.vm.addExpenseDetail();
    const detailCount = wrapper.vm.form.details.length;
    
    // 删除第一个明细
    await wrapper.find('[data-testid="remove-detail-btn"]').trigger('click');
    
    expect(wrapper.vm.form.details.length).toBe(detailCount - 1);
  });

  it('calculates total amount correctly', async () => {
    wrapper.vm.form.details = [
      { amount: 100, quantity: 2, unitPrice: 50 }, // 总计 100
      { amount: 200, quantity: 1, unitPrice: 200 } // 总计 200
    ];
    
    await wrapper.vm.$nextTick();
    
    expect(wrapper.vm.form.totalAmount).toBe(300);
  });

  it('updates total amount when detail changes', async () => {
    // 设置初始明细
    wrapper.vm.form.details = [{ amount: 100 }];
    
    // 修改金额
    wrapper.vm.form.details[0].amount = 200;
    
    await wrapper.vm.calculateTotalAmount();
    
    expect(wrapper.vm.form.totalAmount).toBe(200);
  });

  it('checks budget availability when amount exceeds balance', async () => {
    wrapper.vm.form.budgetId = 1;
    wrapper.vm.form.totalAmount = 15000; // 超过预算余额10000
    
    await wrapper.vm.checkBudgetAvailability();
    
    expect(wrapper.vm.budgetExceeded).toBe(true);
    expect(wrapper.vm.errorMessage).toBeDefined();
  });

  it('allows submission when amount is within budget', async () => {
    wrapper.vm.form.budgetId = 1;
    wrapper.vm.form.totalAmount = 5000; // 在预算余额10000内
    
    await wrapper.vm.checkBudgetAvailability();
    
    expect(wrapper.vm.budgetExceeded).toBe(false);
    expect(wrapper.vm.submitButtonDisabled).toBe(false);
  });

  it('emits submit event with form data', async () => {
    const mockEmit = vi.fn();
    wrapper.vm.$emit = mockEmit;
    
    // 填写有效的表单数据
    wrapper.vm.form.title = '差旅费申请';
    wrapper.vm.form.totalAmount = 5000;
    wrapper.vm.form.budgetId = 1;
    wrapper.vm.form.feeTypeId = 1;
    wrapper.vm.form.expenseDate = '2024-01-15';
    wrapper.vm.form.description = '出差广州';
    
    wrapper.vm.form.details = [
      { itemName: '交通费', amount: 2000, quantity: 1, unitPrice: 2000, description: '高铁票' },
      { itemName: '住宿费', amount: 3000, quantity: 3, unitPrice: 1000, description: '酒店住宿' }
    ];
    
    await wrapper.vm.submitForm();
    
    expect(mockEmit).toHaveBeenCalledWith('submit', expect.objectContaining({
      title: '差旅费申请',
      totalAmount: 5000,
      budgetId: 1,
      details: expect.any(Array)
    }));
  });

  it('emits cancel event when cancel button is clicked', async () => {
    const mockEmit = vi.fn();
    wrapper.vm.$emit = mockEmit;
    
    await wrapper.find('[data-testid="cancel-btn"]').trigger('click');
    
    expect(mockEmit).toHaveBeenCalledWith('cancel');
  });

  it('loads fee types on mount', () => {
    expect(wrapper.vm.feeTypes.length).toBeGreaterThan(0);
  });

  it('formats currency correctly', () => {
    const formatted = wrapper.vm.formatCurrency(1234.56);
    expect(formatted).toBe('¥1,234.56');
  });

  it('handles date formatting correctly', () => {
    const date = '2024-01-15';
    const formatted = wrapper.vm.formatDate(date);
    expect(formatted).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  it('validates expense detail fields', async () => {
    // 添加明细但不填写必填字段
    await wrapper.vm.addExpenseDetail();
    const detail = wrapper.vm.form.details[0];
    
    // 明细验证应该检查itemName和amount
    expect(detail.itemName).toBe('');
    expect(detail.amount).toBe(0);
    
    // 提交时有验证逻辑
    await wrapper.vm.validateDetail(detail);
    
    // 应该有验证错误
    expect(detail.errors).toBeDefined();
  });

  it('calculates amount from quantity and unit price', async () => {
    const detail = { quantity: 2, unitPrice: 50 };
    const calculated = wrapper.vm.calculateDetailAmount(detail);
    
    expect(calculated).toBe(100);
  });
});