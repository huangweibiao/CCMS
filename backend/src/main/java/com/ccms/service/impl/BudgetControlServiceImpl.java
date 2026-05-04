package com.ccms.service.impl;

import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetMain;
import com.ccms.repository.budget.BudgetDetailRepository;
import com.ccms.repository.budget.BudgetMainRepository;
import com.ccms.service.BudgetControlService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 预算控制服务实现类
 * 实现预算扣减、释放、冻结、解冻等核心控制功能，包含乐观锁并发控制
 * 
 * @author 系统生成
 */
@Service
public class BudgetControlServiceImpl implements BudgetControlService {
    
    private static final Logger logger = LoggerFactory.getLogger(BudgetControlServiceImpl.class);
    
    @Autowired
    private BudgetMainRepository budgetMainRepository;
    
    @Autowired
    private BudgetDetailRepository budgetDetailRepository;
    
    @Override
    public boolean checkBudgetAvailability(BudgetMain budgetMain, BudgetDetail budgetDetail, BigDecimal amount) {
        if (budgetMain == null || budgetDetail == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("预算可用性检查参数无效：budgetMain={}, budgetDetail={}, amount={}", 
                     budgetMain, budgetDetail, amount);
            return false;
        }
        
        // 检查预算状态是否为已生效
        if (budgetMain.getBudgetStatus() != 3) { // 3表示已生效
            logger.warn("预算状态异常，不允许使用：budgetStatus={}", budgetMain.getBudgetStatus());
            return false;
        }
        
        // 检查可用余额是否充足
        BigDecimal availableAmount = budgetDetail.getBudgetAmount()
                .subtract(budgetDetail.getUsedAmount())
                .subtract(budgetDetail.getFrozenAmount());
        
        boolean available = availableAmount.compareTo(amount) >= 0;
        logger.info("预算可用性检查结果：available={}, availableAmount={}, requiredAmount={}", 
                   available, availableAmount, amount);
        
        return available;
    }
    
    @Override
    @Transactional
    public boolean freezeBudgetAmount(BudgetDetail budgetDetail, BigDecimal amount) {
        try {
            // 重新加载最新的预算明细数据，避免脏读
            BudgetDetail freshDetail = budgetDetailRepository.findById(budgetDetail.getId())
                    .orElseThrow(() -> new RuntimeException("预算明细不存在"));
            
            // 检查余额是否充足
            BigDecimal availableAmount = freshDetail.getBudgetAmount()
                    .subtract(freshDetail.getUsedAmount())
                    .subtract(freshDetail.getFrozenAmount());
            
            if (availableAmount.compareTo(amount) < 0) {
                logger.warn("预算余额不足，无法冻结：availableAmount={}, freezeAmount={}", 
                          availableAmount, amount);
                return false;
            }
            
            // 更新冻结金额
            BigDecimal newFrozenAmount = freshDetail.getFrozenAmount().add(amount);
            freshDetail.setFrozenAmount(newFrozenAmount);
            
            // 乐观锁控制 - 保存时自动检查版本
            BudgetDetail savedDetail = budgetDetailRepository.save(freshDetail);
            
            logger.info("预算冻结成功：detailId={}, amount={}, newFrozenAmount={}", 
                      savedDetail.getId(), amount, savedDetail.getFrozenAmount());
            
            return true;
            
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("预算冻结并发冲突：detailId={}, amount={}", budgetDetail.getId(), amount, e);
            throw new RuntimeException("预算冻结操作发生并发冲突，请重试", e);
        } catch (Exception e) {
            logger.error("预算冻结失败：detailId={}, amount={}", budgetDetail.getId(), amount, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean unfreezeBudgetAmount(BudgetDetail budgetDetail, BigDecimal amount) {
        try {
            // 重新加载最新的预算明细数据
            BudgetDetail freshDetail = budgetDetailRepository.findById(budgetDetail.getId())
                    .orElseThrow(() -> new RuntimeException("预算明细不存在"));
            
            // 检查冻结金额是否足够
            if (freshDetail.getFrozenAmount().compareTo(amount) < 0) {
                logger.warn("冻结金额不足，无法解冻：frozenAmount={}, unfreezeAmount={}", 
                          freshDetail.getFrozenAmount(), amount);
                return false;
            }
            
            // 更新冻结金额
            BigDecimal newFrozenAmount = freshDetail.getFrozenAmount().subtract(amount);
            freshDetail.setFrozenAmount(newFrozenAmount);
            
            // 乐观锁控制 - 保存时自动检查版本
            BudgetDetail savedDetail = budgetDetailRepository.save(freshDetail);
            
            logger.info("预算解冻成功：detailId={}, amount={}, newFrozenAmount={}", 
                      savedDetail.getId(), amount, savedDetail.getFrozenAmount());
            
            return true;
            
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("预算解冻并发冲突：detailId={}, amount={}", budgetDetail.getId(), amount, e);
            throw new RuntimeException("预算解冻操作发生并发冲突，请重试", e);
        } catch (Exception e) {
            logger.error("预算解冻失败：detailId={}, amount={}", budgetDetail.getId(), amount, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deductBudgetAmount(BudgetDetail budgetDetail, BigDecimal amount) {
        try {
            // 重新加载最新的预算明细数据
            BudgetDetail freshDetail = budgetDetailRepository.findById(budgetDetail.getId())
                    .orElseThrow(() -> new RuntimeException("预算明细不存在"));
            
            // 检查冻结金额是否足够
            if (freshDetail.getFrozenAmount().compareTo(amount) < 0) {
                logger.warn("冻结金额不足，无法扣减：frozenAmount={}, deductAmount={}", 
                          freshDetail.getFrozenAmount(), amount);
                return false;
            }
            
            // 扣减冻结金额并添加到已用金额
            BigDecimal newFrozenAmount = freshDetail.getFrozenAmount().subtract(amount);
            BigDecimal newUsedAmount = freshDetail.getUsedAmount().add(amount);
            
            freshDetail.setFrozenAmount(newFrozenAmount);
            freshDetail.setUsedAmount(newUsedAmount);
            
            // 乐观锁控制 - 保存时自动检查版本
            BudgetDetail savedDetail = budgetDetailRepository.save(freshDetail);
            
            logger.info("预算扣减成功：detailId={}, amount={}, newUsedAmount={}, newFrozenAmount={}", 
                      savedDetail.getId(), amount, savedDetail.getUsedAmount(), savedDetail.getFrozenAmount());
            
            return true;
            
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("预算扣减并发冲突：detailId={}, amount={}", budgetDetail.getId(), amount, e);
            throw new RuntimeException("预算扣减操作发生并发冲突，请重试", e);
        } catch (Exception e) {
            logger.error("预算扣减失败：detailId={}, amount={}", budgetDetail.getId(), amount, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean releaseBudgetAmount(BudgetDetail budgetDetail, BigDecimal amount) {
        try {
            // 重新加载最新的预算明细数据
            BudgetDetail freshDetail = budgetDetailRepository.findById(budgetDetail.getId())
                    .orElseThrow(() -> new RuntimeException("预算明细不存在"));
            
            // 检查已用金额是否足够
            if (freshDetail.getUsedAmount().compareTo(amount) < 0) {
                logger.warn("已用金额不足，无法释放：usedAmount={}, releaseAmount={}", 
                          freshDetail.getUsedAmount(), amount);
                return false;
            }
            
            // 释放已用金额
            BigDecimal newUsedAmount = freshDetail.getUsedAmount().subtract(amount);
            freshDetail.setUsedAmount(newUsedAmount);
            
            // 乐观锁控制 - 保存时自动检查版本
            BudgetDetail savedDetail = budgetDetailRepository.save(freshDetail);
            
            logger.info("预算释放成功：detailId={}, amount={}, newUsedAmount={}", 
                      savedDetail.getId(), amount, savedDetail.getUsedAmount());
            
            return true;
            
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("预算释放并发冲突：detailId={}, amount={}", budgetDetail.getId(), amount, e);
            throw new RuntimeException("预算释放操作发生并发冲突，请重试", e);
        } catch (Exception e) {
            logger.error("预算释放失败：detailId={}, amount={}", budgetDetail.getId(), amount, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean adjustBudgetBalance(BudgetDetail budgetDetail, BigDecimal carryOverAmount) {
        try {
            // 重新加载最新的预算明细数据
            BudgetDetail freshDetail = budgetDetailRepository.findById(budgetDetail.getId())
                    .orElseThrow(() -> new RuntimeException("预算明细不存在"));
            
            // 执行余额调整（月末结转等场景）
            if (carryOverAmount.compareTo(BigDecimal.ZERO) > 0) {
                // 结转金额为正，增加预算
                BigDecimal newBudgetAmount = freshDetail.getBudgetAmount().add(carryOverAmount);
                freshDetail.setBudgetAmount(newBudgetAmount);
                freshDetail.setCarryOver(carryOverAmount);
            } else {
                // 结转金额为负或零，设置结转记录
                freshDetail.setCarryOver(carryOverAmount);
            }
            
            // 乐观锁控制 - 保存时自动检查版本
            BudgetDetail savedDetail = budgetDetailRepository.save(freshDetail);
            
            logger.info("预算余额调整成功：detailId={}, carryOverAmount={}, newBudgetAmount={}", 
                      savedDetail.getId(), carryOverAmount, savedDetail.getBudgetAmount());
            
            return true;
            
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("预算余额调整并发冲突：detailId={}, carryOverAmount={}", 
                       budgetDetail.getId(), carryOverAmount, e);
            throw new RuntimeException("预算余额调整操作发生并发冲突，请重试", e);
        } catch (Exception e) {
            logger.error("预算余额调整失败：detailId={}, carryOverAmount={}", 
                       budgetDetail.getId(), carryOverAmount, e);
            return false;
        }
    }
    
    /**
     * 预算验证结果内部类
     */
    public static class BudgetValidationResult {
        private final boolean valid;
        private final String message;
        
        public BudgetValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}