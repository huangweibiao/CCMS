package com.ccms.service;

import com.ccms.entity.system.log.OperateLog;

/**
 * 操作日志服务接口
 */
public interface OperateLogService {
    
    /**
     * 保存操作日志
     */
    void saveOperateLog(OperateLog operateLog);
    
    /**
     * 记录成功的操作日志
     */
    void recordSuccessLog(String businessType, Long businessId, Integer operateType, 
                         Long operateUserId, String operateContent, String operateIp);
    
    /**
     * 记录失败的操作日志
     */
    void recordFailedLog(String businessType, Long businessId, Integer operateType, 
                        Long operateUserId, String operateContent, String errorMessage, String operateIp);
}