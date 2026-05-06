package com.ccms.repository.system.log;

import com.ccms.entity.system.log.OperateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 操作日志数据访问接口
 */
@Repository
public interface OperateLogRepository extends JpaRepository<OperateLog, Long> {
    
    /**
     * 根据业务ID和业务类型查询操作日志
     */
    List<OperateLog> findByBusinessIdAndBusinessType(Long businessId, String businessType);
    
    /**
     * 根据操作人ID查询操作日志
     */
    List<OperateLog> findByOperUserId(Long operUserId);
    
    /**
     * 根据业务类型和操作模块查询操作日志
     */
    @Query("SELECT o FROM OperateLog o WHERE o.businessType = :businessType AND o.operModule = :operModule ORDER BY o.operTime DESC")
    List<OperateLog> findByBusinessTypeAndOperModule(@Param("businessType") String businessType, @Param("operModule") String operModule);
}