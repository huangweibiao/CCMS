package com.ccms.repository.export;

import com.ccms.entity.export.ReportShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 报表分享仓库接口
 */
public interface ReportShareRepository extends JpaRepository<ReportShare, Long>, JpaSpecificationExecutor<ReportShare> {

    /**
     * 根据分享令牌查找
     */
    Optional<ReportShare> findByShareToken(String shareToken);

    /**
     * 根据创建人查询分享列表
     */
    List<ReportShare> findByCreateByOrderByCreateTimeDesc(String createBy);

    /**
     * 根据状态和过期时间查询有效的分享
     */
    @Query("SELECT rs FROM ReportShare rs WHERE rs.status = 1 AND rs.expireTime > :currentTime")
    List<ReportShare> findValidShares(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查找已过期的分享
     */
    @Query("SELECT rs FROM ReportShare rs WHERE rs.status = 1 AND rs.expireTime <= :currentTime")
    List<ReportShare> findExpiredShares(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据模板ID查询分享记录
     */
    List<ReportShare> findByTemplateIdOrderByCreateTimeDesc(Long templateId);

    /**
     * 更新访问次数和最后访问时间
     */
    @Modifying
    @Transactional
    @Query("UPDATE ReportShare rs SET rs.accessCount = rs.accessCount + 1, rs.lastAccessTime = :accessTime " +
           "WHERE rs.shareToken = :shareToken")
    void updateAccessInfo(@Param("shareToken") String shareToken, 
                         @Param("accessTime") LocalDateTime accessTime);

    /**
     * 更新下载次数
     */
    @Modifying
    @Transactional
    @Query("UPDATE ReportShare rs SET rs.downloadCount = rs.downloadCount + 1 " +
           "WHERE rs.shareToken = :shareToken")
    void incrementDownloadCount(@Param("shareToken") String shareToken);

    /**
     * 批量标记分享为无效
     */
    @Modifying
    @Transactional
    @Query("UPDATE ReportShare rs SET rs.status = 0 WHERE rs.id IN :ids")
    void batchInvalidateShares(@Param("ids") List<Long> ids);

    /**
     * 根据分享令牌检查是否存在
     */
    boolean existsByShareToken(String shareToken);

    /**
     * 统计各类分享数量
     */
    @Query("SELECT rs.createBy, COUNT(rs) FROM ReportShare rs WHERE rs.status = 1 GROUP BY rs.createBy")
    List<Object[]> countSharesByUser();

    /**
     * 查找需要自动清理的分享记录
     */
    @Query("SELECT rs FROM ReportShare rs WHERE rs.status = 1 AND " +
           "(rs.expireTime < :currentTime OR " +
           "(rs.maxAccessCount > 0 AND rs.accessCount >= rs.maxAccessCount))")
    List<ReportShare> findSharesForCleanup(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 获取用户的分享统计
     */
    @Query("SELECT COUNT(rs), SUM(rs.accessCount), SUM(rs.downloadCount) FROM ReportShare rs WHERE rs.createBy = :createBy")
    Object[] getUserShareStats(@Param("createBy") String createBy);

    /**
     * 根据创建时间和状态搜索
     */
    @Query("SELECT rs FROM ReportShare rs WHERE rs.createTime BETWEEN :startTime AND :endTime AND rs.status = :status")
    List<ReportShare> findByCreateTimeRangeAndStatus(@Param("startTime") LocalDateTime startTime, 
                                                    @Param("endTime") LocalDateTime endTime,
                                                    @Param("status") Integer status);

    /**
     * 删除过期分享记录
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ReportShare rs WHERE rs.status = 0 AND rs.expireTime < :cutoffTime")
    int deleteExpiredRecords(@Param("cutoffTime") LocalDateTime cutoffTime);
}