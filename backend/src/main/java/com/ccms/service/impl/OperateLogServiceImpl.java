package com.ccms.service.impl;

import com.ccms.entity.system.log.OperateLog;
import com.ccms.repository.system.log.OperateLogRepository;
import com.ccms.service.OperateLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现类
 */
@Service
public class OperateLogServiceImpl implements OperateLogService {

    private static final Logger log = LoggerFactory.getLogger(OperateLogServiceImpl.class);

    @Autowired
    private OperateLogRepository operateLogRepository;

    @Override
    public void saveOperateLog(OperateLog operateLog) {
        try {
            if (operateLog.getOperTime() == null) {
                operateLog.setOperTime(java.time.LocalDateTime.now());
            }
            operateLogRepository.save(operateLog);
        } catch (Exception e) {
            log.error("保存操作日志失败: {}", e.getMessage(), e);
            // 操作日志保存失败不应该影响主业务流程
        }
    }

    @Override
    public void recordSuccessLog(String businessType, Long businessId, Integer operateType, 
                                Long operateUserId, String operateContent, String operateIp) {
        OperateLog operateLog = new OperateLog();
        operateLog.setBusinessType(businessType);
        operateLog.setBusinessId(businessId);
        operateLog.setOperModule(getModuleByBusinessType(businessType));
        operateLog.setOperType(operateType.toString());
        operateLog.setOperUserId(operateUserId);
        operateLog.setOperContent(operateContent);
        operateLog.setOperIp(operateIp);
        operateLog.setOperTime(java.time.LocalDateTime.now());
        
        saveOperateLog(operateLog);
    }

    @Override
    public void recordFailedLog(String businessType, Long businessId, Integer operateType, 
                               Long operateUserId, String operateContent, String errorMessage, String operateIp) {
        OperateLog operateLog = new OperateLog();
        operateLog.setBusinessType(businessType);
        operateLog.setBusinessId(businessId);
        operateLog.setOperModule(getModuleByBusinessType(businessType));
        operateLog.setOperType(operateType.toString());
        operateLog.setOperUserId(operateUserId);
        operateLog.setOperContent(operateContent + " (失败：" + errorMessage + ")");
        operateLog.setOperIp(operateIp);
        operateLog.setOperTime(java.time.LocalDateTime.now());
        
        saveOperateLog(operateLog);
    }
    
    /**
     * 根据业务类型获取操作模块名称
     */
    private String getModuleByBusinessType(String businessType) {
        switch (businessType) {
            case "expense_apply":
                return "费用申请";
            case "expense_reimburse":
                return "费用报销";
            case "budget":
                return "预算管理";
            case "approval":
                return "审批管理";
            default:
                return "系统管理";
        }
    }
}