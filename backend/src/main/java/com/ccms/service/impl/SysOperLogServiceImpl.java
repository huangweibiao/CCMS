package com.ccms.service.impl;

import com.ccms.entity.system.SysOperLog;
import com.ccms.repository.SysOperLogRepository;
import com.ccms.service.SysOperLogService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 系统操作日志服务实现类
 */
@Service
@Transactional
public class SysOperLogServiceImpl implements SysOperLogService {

    private final SysOperLogRepository operLogRepository;
    
    public SysOperLogServiceImpl(SysOperLogRepository operLogRepository) {
        this.operLogRepository = operLogRepository;
    }

    @Override
    public void logOperation(OperLogInfo logInfo) {
        try {
            SysOperLog operLog = convertToOperLog(logInfo);
            operLogRepository.save(operLog);
        } catch (Exception e) {
            // 记录操作日志时出错，记录到系统日志
            System.err.println("记录操作日志失败: " + e.getMessage());
        }
    }

    @Override
    public void batchLogOperations(List<OperLogInfo> logInfos) {
        try {
            List<SysOperLog> operLogs = logInfos.stream()
                    .map(this::convertToOperLog)
                    .collect(Collectors.toList());
            operLogRepository.saveAll(operLogs);
        } catch (Exception e) {
            System.err.println("批量记录操作日志失败: " + e.getMessage());
        }
    }

    @Override
    public SysOperLog getOperLogById(Long logId) {
        return operLogRepository.findById(logId).orElse(null);
    }

    @Override
    public Page<SysOperLog> getOperLogs(OperLogQuery query, Pageable pageable) {
        Specification<SysOperLog> spec = buildQuerySpecification(query);
        return operLogRepository.findAll(spec, pageable);
    }

    @Override
    public List<SysOperLog> getOperLogsByBusiness(Long businessId, String businessModule) {
        return operLogRepository.findByBusinessIdAndBusinessModuleOrderByOperTimeDesc(
                businessId.toString(), businessModule);
    }

    @Override
    public Page<SysOperLog> getOperLogsByUser(Long userId, Pageable pageable) {
        return operLogRepository.findByOperUserIdOrderByOperTimeDesc(
                userId.toString(), pageable);
    }

    @Override
    public OperLogStatistics getOperLogStatistics(StatisticsQuery query) {
        OperLogStatistics statistics = new OperLogStatistics();
        LocalDateTime startTime = query.getStartTime() != null ? query.getStartTime() : LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = query.getEndTime() != null ? query.getEndTime() : LocalDateTime.now();
        
        // 总操作数统计
        long totalCount = operLogRepository.countByOperTimeBetween(startTime, endTime);
        statistics.setTotalCount(totalCount);
        
        // 成功/错误操作数统计
        long successCount = operLogRepository.countByOperTimeBetweenAndStatus(startTime, endTime, 0);
        long errorCount = operLogRepository.countByOperTimeBetweenAndStatus(startTime, endTime, 1);
        statistics.setSuccessCount(successCount);
        statistics.setErrorCount(errorCount);
        statistics.setSuccessRate(totalCount > 0 ? (double) successCount / totalCount * 100 : 0);
        
        // 平均耗时统计
        Long avgCostTime = operLogRepository.findAvgCostTimeByOperTimeBetween(startTime, endTime);
        statistics.setAvgCostTime(avgCostTime != null ? avgCostTime : 0);
        
        // 业务类型统计
        if ("businessType".equals(query.getGroupBy())) {
            List<Object[]> businessTypeStats = operLogRepository.countByOperTimeBetweenGroupByBusinessType(startTime, endTime);
            Map<String, Long> statsMap = businessTypeStats.stream()
                    .collect(Collectors.toMap(
                            obj -> obj[0].toString(),
                            obj -> (Long) obj[1]
                    ));
            statistics.setBusinessTypeStats(statsMap);
        }
        
        // 用户操作统计
        if ("user".equals(query.getGroupBy())) {
            List<Object[]> userStats = operLogRepository.countByOperTimeBetweenGroupByUser(startTime, endTime);
            Map<String, Long> userStatsMap = userStats.stream()
                    .collect(Collectors.toMap(
                            obj -> obj[0].toString(),
                            obj -> (Long) obj[1]
                    ));
            statistics.setUserStats(userStatsMap);
        }
        
        // 模块操作统计
        if ("module".equals(query.getGroupBy())) {
            List<Object[]> moduleStats = operLogRepository.countByOperTimeBetweenGroupByModule(startTime, endTime);
            Map<String, Long> moduleStatsMap = moduleStats.stream()
                    .collect(Collectors.toMap(
                            obj -> obj[0].toString(),
                            obj -> (Long) obj[1]
                    ));
            statistics.setModuleStats(moduleStatsMap);
        }
        
        return statistics;
    }

    @Override
    public List<OperTrend> getOperTrendAnalysis(TrendQuery query) {
        LocalDateTime startTime = query.getStartTime() != null ? query.getStartTime() : LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = query.getEndTime() != null ? query.getEndTime() : LocalDateTime.now();
        String period = query.getPeriod() != null ? query.getPeriod() : "day";
        
        List<Object[]> trendData;
        switch (period) {
            case "hour":
                trendData = operLogRepository.findTrendByHour(startTime, endTime);
                break;
            case "month":
                trendData = operLogRepository.findTrendByMonth(startTime, endTime);
                break;
            case "day":
            default:
                trendData = operLogRepository.findTrendByDay(startTime, endTime);
                break;
        }
        
        return trendData.stream()
                .map(obj -> {
                    OperTrend trend = new OperTrend();
                    trend.setDate(obj[0].toString());
                    trend.setOperCount((Long) obj[1]);
                    trend.setSuccessCount((Long) obj[2]);
                    trend.setErrorCount((Long) obj[3]);
                    trend.setAvgCostTime(obj[4] != null ? (Double) obj[4] : 0);
                    return trend;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CleanupResult cleanupExpiredLogs(LocalDateTime beforeDate) {
        CleanupResult result = new CleanupResult();
        try {
            int deletedCount = operLogRepository.deleteByOperTimeBefore(beforeDate);
            result.setDeletedCount(deletedCount);
            result.setMessage(String.format("成功清理 %d 条过期操作日志", deletedCount));
        } catch (Exception e) {
            result.setDeletedCount(0);
            result.setMessage("清理过期操作日志失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public ArchiveResult archiveOperLogs(LocalDateTime beforeDate) {
        ArchiveResult result = new ArchiveResult();
        try {
            // 查询要归档的日志
            List<SysOperLog> logsToArchive = operLogRepository.findByOperTimeBefore(beforeDate);
            
            if (logsToArchive.isEmpty()) {
                result.setMessage("没有需要归档的操作日志");
                return result;
            }
            
            // 创建归档目录
            String archiveDir = "/logs/archive/";
            Path archivePath = Paths.get(archiveDir);
            if (!Files.exists(archivePath)) {
                Files.createDirectories(archivePath);
            }
            
            // 生成归档文件名
            String archiveFileName = "oper_log_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".zip";
            Path archiveFilePath = archivePath.resolve(archiveFileName);
            
            // 创建ZIP归档文件
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archiveFilePath.toFile()))) {
                String logContent = convertLogsToCsv(logsToArchive);
                ZipEntry entry = new ZipEntry("operation_logs.csv");
                zos.putNextEntry(entry);
                zos.write(logContent.getBytes());
                zos.closeEntry();
            }
            
            // 删除已归档的日志
            operLogRepository.deleteAll(logsToArchive);
            
            result.setArchivedCount(logsToArchive.size());
            result.setArchivePath(archiveFilePath.toString());
            result.setMessage(String.format("成功归档 %d 条操作日志", logsToArchive.size()));
            
        } catch (Exception e) {
            result.setArchivedCount(0);
            result.setMessage("归档操作日志失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public ExportResult exportOperLogs(OperLogQuery query) {
        ExportResult result = new ExportResult();
        try {
            Specification<SysOperLog> spec = buildQuerySpecification(query);
            List<SysOperLog> operLogs = operLogRepository.findAll(spec);
            
            if (operLogs.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("没有找到符合条件的操作日志");
                return result;
            }
            
            // 创建导出目录
            String exportDir = "/logs/export/";
            Path exportPath = Paths.get(exportDir);
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }
            
            // 生成导出文件名
            String exportFileName = "oper_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            Path exportFilePath = exportPath.resolve(exportFileName);
            
            // 转换为CSV格式
            String csvContent = convertLogsToCsv(operLogs);
            Files.write(exportFilePath, csvContent.getBytes());
            
            result.setSuccess(true);
            result.setFilePath(exportFilePath.toString());
            result.setFileName(exportFileName);
            result.setRecordCount(operLogs.size());
            result.setMessage(String.format("成功导出 %d 条操作日志", operLogs.size()));
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("导出操作日志失败: " + e.getMessage());
        }
        return result;
    }

    // ========== 私有方法 ==========

    private SysOperLog convertToOperLog(OperLogInfo logInfo) {
        SysOperLog operLog = new SysOperLog();
        BeanUtils.copyProperties(logInfo, operLog);
        operLog.setOperTime(LocalDateTime.now());
        return operLog;
    }

    private Specification<SysOperLog> buildQuerySpecification(OperLogQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (query.getTitle() != null && !query.getTitle().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("operModule"), "%" + query.getTitle() + "%"));
            }
            
            if (query.getBusinessType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessType"), query.getBusinessType()));
            }
            
            if (query.getOperUserId() != null && !query.getOperUserId().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("operUserId"), query.getOperUserId()));
            }
            
            if (query.getOperUserName() != null && !query.getOperUserName().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("operName"), "%" + query.getOperUserName() + "%"));
            }
            
            if (query.getOperIp() != null && !query.getOperIp().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("operIp"), "%" + query.getOperIp() + "%"));
            }
            
            if (query.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.getStatus()));
            }
            
            if (query.getOperTimeStart() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("operTime"), query.getOperTimeStart()));
            }
            
            if (query.getOperTimeEnd() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("operTime"), query.getOperTimeEnd()));
            }
            
            if (query.getBusinessModule() != null && !query.getBusinessModule().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("businessModule"), query.getBusinessModule()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String convertLogsToCsv(List<SysOperLog> operLogs) {
        StringBuilder csvBuilder = new StringBuilder();
        // CSV头部
        csvBuilder.append("操作时间,操作人员,操作模块,业务类型,操作内容,IP地址,操作状态,执行耗时(ms)\n");
        
        // CSV数据行
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (SysOperLog log : operLogs) {
            csvBuilder.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%d\"\n",
                    log.getOperTime().format(formatter),
                    log.getOperName() != null ? log.getOperName() : "",
                    log.getOperModule() != null ? log.getOperModule() : "",
                    getBusinessTypeName(log.getBusinessType()),
                    log.getOperContent() != null ? log.getOperContent().replace("\"", "\"\"") : "",
                    log.getOperIp() != null ? log.getOperIp() : "",
                    log.getStatus() == 0 ? "成功" : "失败",
                    log.getCostTime() != null ? log.getCostTime() : 0));
        }
        
        return csvBuilder.toString();
    }

    private String getBusinessTypeName(Integer businessType) {
        if (businessType == null) return "未知";
        switch (businessType) {
            case 0: return "新增";
            case 1: return "修改";
            case 2: return "删除";
            case 3: return "查询";
            case 4: return "导入";
            case 5: return "导出";
            case 9: return "其他";
            default: return "未知";
        }
    }
}