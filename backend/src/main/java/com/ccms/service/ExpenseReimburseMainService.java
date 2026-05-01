package com.ccms.service;

import com.ccms.entity.expense.ExpenseReimburseMain;
import com.ccms.entity.expense.ExpenseReimburseDetail;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 费用报销主表服务接口
 * 对应新版数据模型实现
 * 
 * @author 系统生成
 */
public interface ExpenseReimburseMainService {
    
    /**
     * 创建报销单主表记录
     */
    ExpenseReimburseMain createReimburseMain(ExpenseReimburseMain reimburseMain);
    
    /**
     * 更新报销单主表记录
     */
    ExpenseReimburseMain updateReimburseMain(ExpenseReimburseMain reimburseMain);
    
    /**
     * 根据ID获取报销单主表记录
     */
    ExpenseReimburseMain getReimburseMainById(Long reimburseId);
    
    /**
     * 删除报销单主表记录（草稿状态）
     */
    boolean deleteReimburseMain(Long reimburseId);
    
    /**
     * 提交报销单审批
     */
    ExpenseReimburseMain submitForApproval(Long reimburseId, Long submitUserId);
    
    /**
     * 撤回提交的报销单
     */
    ExpenseReimburseMain withdrawSubmission(Long reimburseId);
    
    /**
     * 审批报销单
     */
    ExpenseReimburseMain approveReimburse(Long reimburseId, Long approverId, Integer action, String comment);
    
    /**
     * 处理报销单支付
     */
    ExpenseReimburseMain processPayment(Long reimburseId, Integer paymentMethod, String paymentDocNumber);
    
    /**
     * 关联费用申请单
     */
    ExpenseReimburseMain linkToExpenseApply(Long reimburseId, Long applyId);
    
    /**
     * 计算报销单总金额
     */
    BigDecimal calculateTotalAmount(Long reimburseId);
    
    /**
     * 计算实际报销金额（考虑借款抵扣）
     */
    BigDecimal calculateActualAmount(Long reimburseId);
    
    /**
     * 计算发票总额
     */
    BigDecimal calculateInvoiceTotal(Long reimburseId);
    
    /**
     * 获取报销单明细列表
     */
    List<ExpenseReimburseDetail> getReimburseDetails(Long reimburseId);
    
    /**
     * 根据状态获取报销单列表
     */
    List<ExpenseReimburseMain> getReimbursesByStatus(Integer status);
    
    /**
     * 根据用户获取报销单列表
     */
    List<ExpenseReimburseMain> getReimbursesByUser(Long userId);
    
    /**
     * 根据部门获取报销单列表
     */
    List<ExpenseReimburseMain> getReimbursesByDepartment(Long deptId);
    
    /**
     * 分页查询报销单列表
     */
    Page<ExpenseReimburseMain> getReimburseList(int pageNum, int pageSize, Map<String, Object> queryParams);
    
    /**
     * 获取报销统计信息
     */
    Map<String, Object> getReimburseStatistics(Long deptId, Integer year, Integer month);
    
    /**
     * 验证报销单完整性
     */
    boolean validateReimburseCompleteness(Long reimburseId);
    
    /**
     * 检查预算可用性
     */
    boolean checkBudgetAvailability(Long reimburseId);
    
    /**
     * 批量审核报销单
     */
    Map<String, Object> batchApproveReimburses(List<Long> reimburseIds, Long approverId);
    
    /**
     * 生成报销单编号
     */
    String generateReimburseNo();
    
    /**
     * 导出报销单数据
     */
    byte[] exportReimburses(Map<String, Object> exportParams);
    
    /**
     * 获取待处理报销单数量
     */
    long getPendingCount(Long approverId);
    
    /**
     * 获取报销单状态跟踪
     */
    List<Map<String, Object>> getStatusTracking(Long reimburseId);
}