package com.ccms.service.impl;

import com.ccms.entity.budget.BudgetMain;
import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetAdjust;
import com.ccms.repository.budget.BudgetMainRepository;
import com.ccms.repository.budget.BudgetDetailRepository;
import com.ccms.repository.budget.BudgetAdjustRepository;
import com.ccms.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * 预算管理服务实现类
 * 
 * @author 系统生成
 */
@Service
@Transactional
public class BudgetServiceImpl implements BudgetService {

    private final BudgetMainRepository budgetMainRepository;
    private final BudgetDetailRepository budgetDetailRepository;
    private final BudgetAdjustRepository budgetAdjustRepository;

    @Autowired
    public BudgetServiceImpl(BudgetMainRepository budgetMainRepository, 
                           BudgetDetailRepository budgetDetailRepository,
                           BudgetAdjustRepository budgetAdjustRepository) {
        this.budgetMainRepository = budgetMainRepository;
        this.budgetDetailRepository = budgetDetailRepository;
        this.budgetAdjustRepository = budgetAdjustRepository;
    }

    @Override
    public BudgetMain createBudget(BudgetMain budget) {
        // 设置默认状态
        if (budget.getStatus() == null) {
            budget.setStatus(0); // 草稿状态
        }
        if (budget.getApprovalStatus() == null) {
            budget.setApprovalStatus(0); // 待提交
        }
        
        // 生成预算编码
        if (budget.getBudgetCode() == null || budget.getBudgetCode().isEmpty()) {
            budget.setBudgetCode(generateBudgetCode());
        }
        
        return budgetMainRepository.save(budget);
    }

    @Override
    public BudgetMain updateBudget(BudgetMain budget) {
        Optional<BudgetMain> existingOpt = budgetMainRepository.findById(budget.getId());
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("预算不存在");
        }
        
        BudgetMain existing = existingOpt.get();
        // 检查是否允许修改（草稿状态才允许修改）
        if (existing.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的预算不允许修改");
        }
        
        // 更新可修改的字段
        existing.setBudgetName(budget.getBudgetName());
        existing.setBudgetCycle(budget.getBudgetCycle());
        existing.setBudgetYear(budget.getBudgetYear());
        existing.setDescription(budget.getDescription());
        
        return budgetMainRepository.save(existing);
    }

    @Override
    public void submitBudgetForApproval(Long budgetId) {
        Optional<BudgetMain> budgetOpt = budgetMainRepository.findById(budgetId);
        if (budgetOpt.isEmpty()) {
            throw new RuntimeException("预算不存在");
        }
        
        BudgetMain budget = budgetOpt.get();
        // 只有草稿状态才能提交审批
        if (budget.getStatus() != 0) {
            throw new RuntimeException("预算状态不允许提交审批");
        }
        
        // 检查预算明细是否完整
        List<BudgetDetail> details = budgetDetailRepository.findByBudgetMainId(budgetId);
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("预算明细不能为空");
        }
        
        // 更新状态为待审批
        budget.setStatus(1); // 审批中
        budget.setApprovalStatus(1); // 审批中
        budgetMainRepository.save(budget);
    }

    @Override
    public BudgetMain getBudgetById(Long budgetId) {
        return budgetMainRepository.findById(budgetId).orElse(null);
    }

    @Override
    public List<BudgetMain> getBudgetsByDeptAndYear(Long deptId, Integer year) {
        return budgetMainRepository.findByDeptIdAndBudgetYear(deptId, year);
    }

    @Override
    public BudgetDetail addBudgetDetail(BudgetDetail detail) {
        // 验证预算主表存在
        Optional<BudgetMain> budgetOpt = budgetMainRepository.findById(detail.getBudgetMainId());
        if (budgetOpt.isEmpty()) {
            throw new RuntimeException("预算主表不存在");
        }
        
        BudgetMain budget = budgetOpt.get();
        // 只有在草稿状态才能添加明细
        if (budget.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的预算不允许添加明细");
        }
        
        // 设置默认值
        if (detail.getUsedAmount() == null) {
            detail.setUsedAmount(BigDecimal.ZERO);
        }
        if (detail.getRemainingAmount() == null && detail.getBudgetAmount() != null) {
            detail.setRemainingAmount(detail.getBudgetAmount().subtract(detail.getUsedAmount()));
        }
        
        return budgetDetailRepository.save(detail);
    }

    @Override
    public BudgetDetail updateBudgetDetail(BudgetDetail detail) {
        Optional<BudgetDetail> existingOpt = budgetDetailRepository.findById(detail.getId());
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("预算明细不存在");
        }
        
        // 验证预算主表状态
        Optional<BudgetMain> budgetOpt = budgetMainRepository.findById(detail.getBudgetMainId());
        if (budgetOpt.isEmpty()) {
            throw new RuntimeException("预算主表不存在");
        }
        
        BudgetMain budget = budgetOpt.get();
        if (budget.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的预算不允许修改明细");
        }
        
        BudgetDetail existing = existingOpt.get();
        // 更新可修改字段
        existing.setBudgetAmount(detail.getBudgetAmount());
        existing.setDescription(detail.getDescription());
        
        return budgetDetailRepository.save(existing);
    }

    @Override
    public List<BudgetDetail> getBudgetDetails(Long budgetId) {
        return budgetDetailRepository.findByBudgetMainId(budgetId);
    }

    @Override
    public BudgetAdjust applyBudgetAdjust(BudgetAdjust adjust) {
        // 验证预算存在
        Optional<BudgetMain> budgetOpt = budgetMainRepository.findById(adjust.getBudgetMainId());
        if (budgetOpt.isEmpty()) {
            throw new RuntimeException("预算不存在");
        }
        
        // 设置默认状态
        if (adjust.getStatus() == null) {
            adjust.setStatus(0); // 草稿
        }
        if (adjust.getAdjustStatus() == null) {
            adjust.setAdjustStatus(0); // 待提交
        }
        
        adjust.setAdjustDate(java.time.LocalDate.now());
        
        return budgetAdjustRepository.save(adjust);
    }

    @Override
    public void approveBudgetAdjust(Long adjustId, boolean approved, String comment) {
        Optional<BudgetAdjust> adjustOpt = budgetAdjustRepository.findById(adjustId);
        if (adjustOpt.isEmpty()) {
            throw new RuntimeException("预算调整申请不存在");
        }
        
        BudgetAdjust adjust = adjustOpt.get();
        if (approved) {
            adjust.setStatus(3); // 已生效
            adjust.setAdjustStatus(2); // 已通过
            
            // 实际生效预算调整逻辑
            applyBudgetAdjustment(adjust);
        } else {
            adjust.setStatus(4); // 已拒绝
            adjust.setAdjustStatus(3); // 已拒绝
        }
        
        adjust.setApprovalComment(comment);
        adjust.setApprovalTime(java.time.LocalDateTime.now());
        
        budgetAdjustRepository.save(adjust);
    }

    @Override
    public List<BudgetAdjust> getBudgetAdjustments(Long budgetId) {
        return budgetAdjustRepository.findByBudgetMainId(budgetId);
    }

    @Override
    public BudgetStatistics getBudgetStatistics(Long deptId, Integer year) {
        BigDecimal totalBudget = budgetMainRepository.calculateTotalBudgetAmountByDeptAndYear(deptId, year);
        BigDecimal usedBudget = calculateUsedBudget(deptId, year);
        BigDecimal remainingBudget = totalBudget.subtract(usedBudget);
        
        BigDecimal usageRate = BigDecimal.ZERO;
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            usageRate = usedBudget.divide(totalBudget, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        return new BudgetStatistics(totalBudget, usedBudget, remainingBudget, usageRate);
    }

    @Override
    public boolean checkBudgetAvailability(Long deptId, Integer expenseType, BigDecimal amount, Integer year) {
        // 查询部门的预算明细
        List<BudgetDetail> details = budgetDetailRepository.findByDeptIdAndYearAndExpenseType(deptId, year, expenseType);
        
        if (details.isEmpty()) {
            return false; // 没有设置该费用类型的预算
        }
        
        // 计算可用预算
        BigDecimal availableBudget = details.stream()
                .map(detail -> detail.getRemainingAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return availableBudget.compareTo(amount) >= 0;
    }

    @Override
    public boolean deductBudget(Long deptId, Integer expenseType, BigDecimal amount, Integer year) {
        if (!checkBudgetAvailability(deptId, expenseType, amount, year)) {
            return false;
        }
        
        // 查找可用的预算明细
        List<BudgetDetail> details = budgetDetailRepository.findByDeptIdAndYearAndExpenseType(deptId, year, expenseType);
        
        for (BudgetDetail detail : details) {
            if (detail.getRemainingAmount().compareTo(amount) >= 0) {
                // 扣减预算
                BigDecimal remaining = detail.getRemainingAmount().subtract(amount);
                detail.setUsedAmount(detail.getUsedAmount().add(amount));
                detail.setRemainingAmount(remaining);
                budgetDetailRepository.save(detail);
                return true;
            }
        }
        
        // 如果单个明细不足，需要拆分扣减（简化处理）
        return false;
    }

    @Override
    public boolean returnBudget(Long deptId, Integer expenseType, BigDecimal amount, Integer year) {
        // 查找对应的预算明细
        List<BudgetDetail> details = budgetDetailRepository.findByDeptIdAndYearAndExpenseType(deptId, year, expenseType);
        
        for (BudgetDetail detail : details) {
            // 退回预算
            detail.setUsedAmount(detail.getUsedAmount().subtract(amount));
            detail.setRemainingAmount(detail.getRemainingAmount().add(amount));
            budgetDetailRepository.save(detail);
            return true;
        }
        
        return false;
    }
    
    /**
     * 生成预算编码
     */
    private String generateBudgetCode() {
        return "BUDGET_" + System.currentTimeMillis();
    }
    
    /**
     * 计算已使用的预算
     */
    private BigDecimal calculateUsedBudget(Long deptId, Integer year) {
        // 简化实现，实际应从费用申请统计
        // 这里使用预算明细中记录的使用金额
        List<BudgetMain> budgets = budgetMainRepository.findByDeptIdAndBudgetYear(deptId, year);
        
        return budgets.stream()
                .map(budget -> budgetDetailRepository.calculateTotalUsedAmount(budget.getId()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 应用预算调整
     */
    private void applyBudgetAdjustment(BudgetAdjust adjust) {
        // 实现预算调整生效逻辑
        // 这里简化处理，实际应根据调整类型更新预算明细
        
        // 更新预算主表总额（简化处理）
        Optional<BudgetMain> budgetOpt = budgetMainRepository.findById(adjust.getBudgetMainId());
        if (budgetOpt.isEmpty()) {
            return;
        }
        
        BudgetMain budget = budgetOpt.get();
        BigDecimal newTotalAmount = adjustBudgetAmount(budget.getTotalAmount(), adjust);
        budget.setTotalAmount(newTotalAmount);
        budgetMainRepository.save(budget);
    }
    
    /**
     * 根据调整类型调整预算金额
     */
    private BigDecimal adjustBudgetAmount(BigDecimal currentAmount, BudgetAdjust adjust) {
        if (adjust.getAdjustType() == 1) { // 追加
            return currentAmount.add(adjust.getAdjustAmount());
        } else if (adjust.getAdjustType() == 2) { // 削减
            return currentAmount.subtract(adjust.getAdjustAmount());
        } else { // 调整（直接设置新值）
            return adjust.getAdjustAmount();
        }
    }
}