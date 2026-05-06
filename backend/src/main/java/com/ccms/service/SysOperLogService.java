package com.ccms.service;

import com.ccms.entity.system.log.SysOperLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统操作日志服务接口
 * 提供操作记录、查询统计、审计分析等功能
 */
public interface SysOperLogService {

    /**
     * 记录操作日志
     */
    void logOperation(OperLogInfo logInfo);

    /**
     * 批量记录操作日志
     */
    void batchLogOperations(List<OperLogInfo> logInfos);

    /**
     * 根据ID获取操作日志详情
     */
    SysOperLog getOperLogById(Long logId);

    /**
     * 分页查询操作日志
     */
    Page<SysOperLog> getOperLogs(OperLogQuery query, Pageable pageable);

    /**
     * 根据业务ID查询操作日志
     */
    List<SysOperLog> getOperLogsByBusiness(Long businessId, String businessModule);

    /**
     * 根据用户ID查询操作日志
     */
    Page<SysOperLog> getOperLogsByUser(Long userId, Pageable pageable);

    /**
     * 获取操作日志统计信息
     */
    OperLogStatistics getOperLogStatistics(StatisticsQuery query);

    /**
     * 获取操作趋势分析
     */
    List<OperTrend> getOperTrendAnalysis(TrendQuery query);

    /**
     * 清理过期操作日志
     */
    CleanupResult cleanupExpiredLogs(LocalDateTime beforeDate);

    /**
     * 归档操作日志
     */
    ArchiveResult archiveOperLogs(LocalDateTime beforeDate);

    /**
     * 导出操作日志
     */
    ExportResult exportOperLogs(OperLogQuery query);

    // ========== DTO类定义 ==========

    /**
     * 操作日志信息
     */
    class OperLogInfo {
        private String title;              // 模块标题
        private Integer businessType;      // 业务类型
        private String method;             // 方法名称
        private String requestMethod;      // 请求方式
        private Integer operatorType;      // 操作类别
        private String operUserId;         // 操作人员ID
        private String operUserName;       // 操作人员名称
        private String deptName;           // 部门名称
        private String operUrl;            // 请求URL
        private String operIp;             // 主机地址
        private String operLocation;       // 操作地点
        private String operParam;          // 请求参数
        private String jsonResult;         // 返回参数
        private Integer status;            // 操作状态
        private String errorMsg;           // 错误消息
        private Long costTime;             // 执行耗时
        private String businessModule;     // 业务模块
        private String businessId;         // 业务ID
        private String deviceInfo;         // 操作设备信息
        private String userAgent;          // 用户代理

        // getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public Integer getBusinessType() { return businessType; }
        public void setBusinessType(Integer businessType) { this.businessType = businessType; }
        
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        
        public String getRequestMethod() { return requestMethod; }
        public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
        
        public Integer getOperatorType() { return operatorType; }
        public void setOperatorType(Integer operatorType) { this.operatorType = operatorType; }
        
        public String getOperUserId() { return operUserId; }
        public void setOperUserId(String operUserId) { this.operUserId = operUserId; }
        
        public String getOperUserName() { return operUserName; }
        public void setOperUserName(String operUserName) { this.operUserName = operUserName; }
        
        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }
        
        public String getOperUrl() { return operUrl; }
        public void setOperUrl(String operUrl) { this.operUrl = operUrl; }
        
        public String getOperIp() { return operIp; }
        public void setOperIp(String operIp) { this.operIp = operIp; }
        
        public String getOperLocation() { return operLocation; }
        public void setOperLocation(String operLocation) { this.operLocation = operLocation; }
        
        public String getOperParam() { return operParam; }
        public void setOperParam(String operParam) { this.operParam = operParam; }
        
        public String getJsonResult() { return jsonResult; }
        public void setJsonResult(String jsonResult) { this.jsonResult = jsonResult; }
        
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        
        public String getErrorMsg() { return errorMsg; }
        public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
        
        public Long getCostTime() { return costTime; }
        public void setCostTime(Long costTime) { this.costTime = costTime; }
        
        public String getBusinessModule() { return businessModule; }
        public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
        
        public String getBusinessId() { return businessId; }
        public void setBusinessId(String businessId) { this.businessId = businessId; }
        
        public String getDeviceInfo() { return deviceInfo; }
        public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
        
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    }

    /**
     * 操作日志查询条件
     */
    class OperLogQuery {
        private String title;              // 模块标题
        private Integer businessType;      // 业务类型
        private String operUserId;         // 操作人员ID
        private String operUserName;       // 操作人员名称
        private String operIp;             // 主机地址
        private Integer status;            // 操作状态
        private LocalDateTime operTimeStart; // 操作时间开始
        private LocalDateTime operTimeEnd;   // 操作时间结束
        private String businessModule;     // 业务模块

        // getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public Integer getBusinessType() { return businessType; }
        public void setBusinessType(Integer businessType) { this.businessType = businessType; }
        
        public String getOperUserId() { return operUserId; }
        public void setOperUserId(String operUserId) { this.operUserId = operUserId; }
        
        public String getOperUserName() { return operUserName; }
        public void setOperUserName(String operUserName) { this.operUserName = operUserName; }
        
        public String getOperIp() { return operIp; }
        public void setOperIp(String operIp) { this.operIp = operIp; }
        
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        
        public LocalDateTime getOperTimeStart() { return operTimeStart; }
        public void setOperTimeStart(LocalDateTime operTimeStart) { this.operTimeStart = operTimeStart; }
        
        public LocalDateTime getOperTimeEnd() { return operTimeEnd; }
        public void setOperTimeEnd(LocalDateTime operTimeEnd) { this.operTimeEnd = operTimeEnd; }
        
        public String getBusinessModule() { return businessModule; }
        public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
    }

    /**
     * 操作日志统计信息
     */
    class OperLogStatistics {
        private long totalCount;          // 总操作数
        private long successCount;        // 成功操作数
        private long errorCount;          // 错误操作数
        private double successRate;       // 成功率
        private long avgCostTime;         // 平均耗时
        private Map<String, Long> businessTypeStats; // 业务类型统计
        private Map<String, Long> userStats;         // 用户操作统计
        private Map<String, Long> moduleStats;       // 模块操作统计

        // getters and setters
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        
        public long getSuccessCount() { return successCount; }
        public void setSuccessCount(long successCount) { this.successCount = successCount; }
        
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public long getAvgCostTime() { return avgCostTime; }
        public void setAvgCostTime(long avgCostTime) { this.avgCostTime = avgCostTime; }
        
        public Map<String, Long> getBusinessTypeStats() { return businessTypeStats; }
        public void setBusinessTypeStats(Map<String, Long> businessTypeStats) { this.businessTypeStats = businessTypeStats; }
        
        public Map<String, Long> getUserStats() { return userStats; }
        public void setUserStats(Map<String, Long> userStats) { this.userStats = userStats; }
        
        public Map<String, Long> getModuleStats() { return moduleStats; }
        public void setModuleStats(Map<String, Long> moduleStats) { this.moduleStats = moduleStats; }
    }

    /**
     * 统计查询条件
     */
    class StatisticsQuery {
        private LocalDateTime startTime;  // 开始时间
        private LocalDateTime endTime;    // 结束时间
        private String groupBy;           // 分组方式

        // getters and setters
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public String getGroupBy() { return groupBy; }
        public void setGroupBy(String groupBy) { this.groupBy = groupBy; }
    }

    /**
     * 操作趋势分析结果
     */
    class OperTrend {
        private String date;              // 日期
        private long operCount;           // 操作次数
        private long successCount;        // 成功次数
        private long errorCount;          // 错误次数
        private double avgCostTime;       // 平均耗时

        // getters and setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public long getOperCount() { return operCount; }
        public void setOperCount(long operCount) { this.operCount = operCount; }
        
        public long getSuccessCount() { return successCount; }
        public void setSuccessCount(long successCount) { this.successCount = successCount; }
        
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
        
        public double getAvgCostTime() { return avgCostTime; }
        public void setAvgCostTime(double avgCostTime) { this.avgCostTime = avgCostTime; }
    }

    /**
     * 趋势查询条件
     */
    class TrendQuery {
        private LocalDateTime startTime;  // 开始时间
        private LocalDateTime endTime;    // 结束时间
        private String period;            // 时间周期
        private String businessType;      // 业务类型

        // getters and setters
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
    }

    /**
     * 清理结果
     */
    class CleanupResult {
        private int deletedCount;         // 删除数量
        private String message;           // 消息

        // getters and setters
        public int getDeletedCount() { return deletedCount; }
        public void setDeletedCount(int deletedCount) { this.deletedCount = deletedCount; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 归档结果
     */
    class ArchiveResult {
        private int archivedCount;        // 归档数量
        private String archivePath;       // 归档路径
        private String message;           // 消息

        // getters and setters
        public int getArchivedCount() { return archivedCount; }
        public void setArchivedCount(int archivedCount) { this.archivedCount = archivedCount; }
        
        public String getArchivePath() { return archivePath; }
        public void setArchivePath(String archivePath) { this.archivePath = archivePath; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 导出结果
     */
    class ExportResult {
        private boolean success;          // 是否成功
        private String filePath;          // 文件路径
        private String fileName;          // 文件名
        private long recordCount;         // 记录数量
        private String message;           // 消息

        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public long getRecordCount() { return recordCount; }
        public void setRecordCount(long recordCount) { this.recordCount = recordCount; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}