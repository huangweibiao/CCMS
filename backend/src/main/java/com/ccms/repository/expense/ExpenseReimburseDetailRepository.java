package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseReimburseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 报销单明细表数据访问接口
 * 
 * @author 系统生成
 */
@Repository
public interface ExpenseReimburseDetailRepository extends JpaRepository<ExpenseReimburseDetail, Long> {
    
    /**
     * 根据报销单ID查询明细列表
     * 
     * @param reimburseId 报销单ID
     * @return 明细列表
     */
    List<ExpenseReimburseDetail> findByReimburseId(Long reimburseId);
    
    /**
     * 根据报销单ID和费用类型查询明细
     * 
     * @param reimburseId 报销单ID
     * @param feeTypeId 费用类型ID
     * @return 明细列表
     */
    List<ExpenseReimburseDetail> findByReimburseIdAndFeeTypeId(Long reimburseId, Long feeTypeId);
    
    /**
     * 统计报销单的总金额
     * 
     * @param reimburseId 报销单ID
     * @return 总金额
     */
    @Query("SELECT SUM(d.amount) FROM ExpenseReimburseDetail d WHERE d.reimburseId = :reimburseId")
    Double calculateTotalAmountByReimburse(@Param("reimburseId") Long reimburseId);
    
    /**
     * 删除报销单的所有明细
     * 
     * @param reimburseId 报销单ID
     */
    void deleteByReimburseId(Long reimburseId);
}
