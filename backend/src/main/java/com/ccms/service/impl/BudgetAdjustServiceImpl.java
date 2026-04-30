package com.ccms.service.impl;

import com.ccms.entity.budget.BudgetAdjust;
import com.ccms.entity.budget.BudgetDetail;
import com.ccms.repository.budget.BudgetAdjustRepository;
import com.ccms.repository.budget.BudgetDetailRepository;
import com.ccms.service.BudgetAdjustService;
import com.ccms.service.ApprovalService;
import com.ccms.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 预算调整服务实现类
 * 
 * @author 系统生成
 */
@Service
public class BudgetAdjustServiceImpl implements BudgetAdjustService {
    
    @Autowired
    private BudgetAdjustRepository budgetAdjustRepository;
    
    @Autowired
    private BudgetDetailRepository budgetDetailRepository;
    
    @Autowired
    private ApprovalService approvalService;
    
    @Override
    @Transactional
    public ResultVO<BudgetAdjust> createAdjustApply(Long budgetId, Long budgetDetailId, 
                                                   Integer adjustType, BigDecimal adjustAmount,
                                                   String reason, Long applyUserId, String applyUserName) {
        
        try {
            // 验证预算明细是否存在
            BudgetDetail budgetDetail = budgetDetailRepository.findById(budgetDetailId)
                .orElse(null);
            
            if (budgetDetail == null) {
                return ResultVO.error("预算明细不存在");
            }
            
            // 创建预算调整记录
            BudgetAdjust adjust = new BudgetAdjust();
            adjust.setBudgetId(budgetId);
            adjust.setBudgetDetailId(budgetDetailId);
            adjust.setAdjustType(adjustType);
            adjust.setAdjustAmount(adjustAmount);
            adjust.setReason(reason);
            adjust.setAdjustBy(applyUserId);
            adjust.setApprovalStatus(0); // 待提交
            adjust.setExecuteStatus(0); // 未执行
            
            // 生成调整单号
            String adjustNo = "ADJ" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
                UUID.randomUUID().toString().substring(0, 4);
            adjust.setAdjustNo(adjustNo);
            
            // 设置原始金额
            adjust.setOriAmount(budgetDetail.getBudgetAmount());
            
            // 计算调整后金额
            BigDecimal afterAmount = calculateAfterAmount(
                budgetDetail.getBudgetAmount(), adjustAmount, adjustType);
            adjust.setAfterAmount(afterAmount);
            
            // 保存调整记录
            budgetAdjustRepository.save(adjust);
            
            return ResultVO.success("预算调整申请创建成功", adjust);
            
        } catch (Exception e) {
            return ResultVO.error("创建预算调整申请失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultVO<BudgetAdjust> submitToApproval(Long adjustId, Long userId) {
        try {
            BudgetAdjust adjust = budgetAdjustRepository.findById(adjustId)
                .orElse(null);
            
            if (adjust == null) {
                return ResultVO.error("预算调整记录不存在");
            }
            
            if (!adjust.getApprovalStatus().equals(0)) {
                return ResultVO.error("该调整申请已提交，状态不可修改");
            }
            
            // 验证预算调整可行性
            ResultVO<Boolean> validateResult = validateAdjustment(
                adjust.getBudgetId(), adjust.getBudgetDetailId(), 
                adjust.getAdjustType(), adjust.getAdjustAmount());
            
            if (!validateResult.isSuccess()) {
                return ResultVO.error(validateResult.getMsg());
            }
            
            // 更新状态为审批中
            adjust.setApprovalStatus(1);
            adjust.setUpdateTime(LocalDateTime.now());
            
            // 启动审批流程
            // 这里暂时模拟审批流程，实际应调用ApprovalService
            adjust.setCurrentApproverId(1001L); // 模拟审批人ID
            adjust.setCurrentApproverName("预算管理员"); // 模拟审批人姓名
            
            budgetAdjustRepository.save(adjust);
            
            return ResultVO.success("预算调整申请已提交审批", adjust);
            
        } catch (Exception e) {
            return ResultVO.error("提交预算调整申请失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultVO<BudgetAdjust> approveAdjust(Long adjustId, Boolean approveResult, 
                                               String approvalComment, Long approverId, String approverName) {
        try {
            BudgetAdjust adjust = budgetAdjustRepository.findById(adjustId)
                .orElse(null);
            
            if (adjust == null) {
                return ResultVO.error("预算调整记录不存在");
            }
            
            if (!adjust.getApprovalStatus().equals(1)) {
                return ResultVO.error("该调整申请不在审批中状态");
            }
            
            // 更新审批状态
            adjust.setApprovalStatus(approveResult ? 2 : 3); // 2-通过 3-驳回
            adjust.setApprovalTime(LocalDateTime.now());
            adjust.setReason(approvalComment != null ? approvalComment : adjust.getReason());
            adjust.setUpdateTime(LocalDateTime.now());
            
            budgetAdjustRepository.save(adjust);
            
            String resultMsg = approveResult ? "预算调整申请已审批通过" : "预算调整申请已审批驳回";
            return ResultVO.success(resultMsg, adjust);
            
        } catch (Exception e) {
            return ResultVO.error("审批预算调整申请失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultVO<BudgetAdjust> executeAdjust(Long adjustId) {
        try {
            BudgetAdjust adjust = budgetAdjustRepository.findById(adjustId)
                .orElse(null);
            
            if (adjust == null) {
                return ResultVO.error("预算调整记录不存在");
            }
            
            if (!adjust.getApprovalStatus().equals(2)) {
                return ResultVO.error("只有已审批通过的调整申请才能执行");
            }
            
            if (!adjust.getExecuteStatus().equals(0)) {
                return ResultVO.error("该调整申请已执行，状态不可修改");
            }
            
            // 获取预算明细
            BudgetDetail budgetDetail = budgetDetailRepository.findById(adjust.getBudgetDetailId())
                .orElse(null);
            
            if (budgetDetail == null) {
                return ResultVO.error("预算明细不存在");
            }
            
            // 执行预算调整
            adjust.setExecuteStatus(1); // 执行中
            adjust.setUpdateTime(LocalDateTime.now());
            
            try {
                // 更新预算明细金额
                BigDecimal newAmount = adjust.getAfterAmount();
                budgetDetail.setBudgetAmount(newAmount);
                budgetDetail.setUpdateTime(LocalDateTime.now());
                
                budgetDetailRepository.save(budgetDetail);
                
                // 更新调整记录状态
                adjust.setExecuteStatus(2); // 执行成功
                adjust.setExecuteTime(LocalDateTime.now());
                adjust.setExecuteMsg("预算调整执行成功");
                
            } catch (Exception e) {
                adjust.setExecuteStatus(3); // 执行失败
                adjust.setExecuteMsg("预算调整执行失败: " + e.getMessage());
                throw e;
            }
            
            budgetAdjustRepository.save(adjust);
            
            return ResultVO.success("预算调整执行成功", adjust);
            
        } catch (Exception e) {
            return ResultVO.error("执行预算调整失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ResultVO<BudgetAdjust> cancelAdjust(Long adjustId, Long userId, String reason) {
        try {
            BudgetAdjust adjust = budgetAdjustRepository.findById(adjustId)
                .orElse(null);
            
            if (adjust == null) {
                return ResultVO.error("预算调整记录不存在");
            }
            
            if (!adjust.getApprovalStatus().equals(0)) {
                return ResultVO.error("只有待提交状态的调整申请才能撤销");
            }
            
            // 更新状态为已撤销
            adjust.setApprovalStatus(4); // 已撤销
            adjust.setReason(reason);
            adjust.setUpdateTime(LocalDateTime.now());
            
            budgetAdjustRepository.save(adjust);
            
            return ResultVO.success("预算调整申请已撤销", adjust);
            
        } catch (Exception e) {
            return ResultVO.error("撤销预算调整申请失败: " + e.getMessage());
        }
    }
    
    @Override
    public BudgetAdjust getAdjustDetail(Long adjustId) {
        return budgetAdjustRepository.findById(adjustId).orElse(null);
    }
    
    @Override
    public ResultVO<Boolean> validateAdjustment(Long budgetId, Long budgetDetailId, 
                                               Integer adjustType, BigDecimal adjustAmount) {
        try {
            BudgetDetail budgetDetail = budgetDetailRepository.findById(budgetDetailId)
                .orElse(null);
            
            if (budgetDetail == null) {
                return ResultVO.error("预算明细不存在");
            }
            
            // 验证金额合理性
            if (adjustAmount == null || adjustAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResultVO.error("调整金额必须大于0");
            }
            
            // 验证调整类型
            if (adjustType == null || adjustType < 1 || adjustType > 3) {
                return ResultVO.error("调整类型不合法");
            }
            
            // 验证调整后金额的合理性
            BigDecimal originalAmount = budgetDetail.getBudgetAmount();
            BigDecimal afterAmount = calculateAfterAmount(originalAmount, adjustAmount, adjustType);
            
            // 调整后金额不能为负
            if (afterAmount.compareTo(BigDecimal.ZERO) < 0) {
                return ResultVO.error("调整后预算金额不能为负数");
            }
            
            return ResultVO.success("验证通过", true);
            
        } catch (Exception e) {
            return ResultVO.error("预算调整验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算调整后金额
     */
    private BigDecimal calculateAfterAmount(BigDecimal originalAmount, BigDecimal adjustAmount, Integer adjustType) {
        switch (adjustType) {
            case 1: // 追加
                return originalAmount.add(adjustAmount);
            case 2: // 调减
                return originalAmount.subtract(adjustAmount);
            case 3: // 转移（简化处理，实际需要更复杂的逻辑）
                return originalAmount; // 转移不改变金额，只是从一个明细转到另一个
            default:
                throw new IllegalArgumentException("不支持的调整类型: " + adjustType);
        }
    }
}