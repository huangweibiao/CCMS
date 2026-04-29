package com.ccms.repository;

import com.ccms.entity.SysMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统消息数据访问接口
 * 
 * @author 系统生成
 */
@Repository
public interface SysMessageRepository extends JpaRepository<SysMessage, Long>, 
                                               JpaSpecificationExecutor<SysMessage> {
    
    /**
     * 根据接收者ID查找消息
     * 
     * @param receiverId 接收者ID
     * @return 消息列表
     */
    List<SysMessage> findByReceiverId(Long receiverId);
    
    /**
     * 根据接收者ID和阅读状态查找消息
     * 
     * @param receiverId 接收者ID
     * @param readStatus 阅读状态
     * @return 消息列表
     */
    List<SysMessage> findByReceiverIdAndReadStatus(Long receiverId, String readStatus);
    
    /**
     * 根据接收者ID和阅读状态查找消息（按时间倒序）
     * 
     * @param receiverId 接收者ID
     * @param readStatus 阅读状态
     * @return 消息列表
     */
    List<SysMessage> findByReceiverIdAndReadStatusOrderByCreatedTimeDesc(Long receiverId, String readStatus);
    
    /**
     * 根据消息类型查找消息
     * 
     * @param msgType 消息类型
     * @return 消息列表
     */
    List<SysMessage> findByMsgType(String msgType);
    
    /**
     * 根据消息级别查找消息
     * 
     * @param msgLevel 消息级别
     * @return 消息列表
     */
    List<SysMessage> findByMsgLevel(String msgLevel);
    
    /**
     * 根据发送状态查找消息
     * 
     * @param sendStatus 发送状态
     * @return 消息列表
     */
    List<SysMessage> findBySendStatus(String sendStatus);
    
    /**
     * 根据发送方式和状态查找消息
     * 
     * @param sendMethod 发送方式
     * @param sendStatus 发送状态
     * @return 消息列表
     */
    List<SysMessage> findBySendMethodAndSendStatus(String sendMethod, String sendStatus);
    
    /**
     * 根据业务类型和业务ID查找消息
     * 
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @return 消息列表
     */
    List<SysMessage> findByBizTypeAndBizId(String bizType, Long bizId);
    
    /**
     * 统计接收者未读消息数量
     * 
     * @param receiverId 接收者ID
     * @param readStatus 阅读状态
     * @return 未读消息数量
     */
    int countByReceiverIdAndReadStatus(Long receiverId, String readStatus);
    
    /**
     * 统计接收者特定级别的未读消息数量
     * 
     * @param receiverId 接收者ID
     * @param readStatus 阅读状态
     * @param msgLevel 消息级别
     * @return 未读消息数量
     */
    int countByReceiverIdAndReadStatusAndMsgLevel(Long receiverId, String readStatus, String msgLevel);
    
    /**
     * 统计接收者特定类型的未读消息数量
     * 
     * @param receiverId 接收者ID
     * @param readStatus 阅读状态
     * @param msgType 消息类型
     * @return 未读消息数量
     */
    int countByReceiverIdAndReadStatusAndMsgType(Long receiverId, String readStatus, String msgType);
    
    /**
     * 按消息类型分组统计未读消息数量
     * 
     * @param receiverId 接收者ID
     * @return 分组统计结果
     */
    @Query("SELECT m.msgType, COUNT(m) FROM SysMessage m WHERE m.receiverId = :receiverId AND m.readStatus = 'UNREAD' AND m.deleted = 0 GROUP BY m.msgType")
    List<Object[]> countUnreadByType(@Param("receiverId") Long receiverId);
    
    /**
     * 查找需要重试的消息
     * 
     * @param sendStatus 发送状态
     * @param retryCount 重试次数上限
     * @return 需要重试的消息列表
     */
    List<SysMessage> findBySendStatusAndRetryCountLessThan(String sendStatus, Integer retryCount);
    
    /**
     * 查找计划发送时间已到的消息
     * 
     * @param sendStatus 发送状态
     * @param planSendTime 计划发送时间
     * @return 待发送消息列表
     */
    List<SysMessage> findBySendStatusAndPlanSendTimeBefore(String sendStatus, LocalDateTime planSendTime);
    
    /**
     * 查找已过期的消息
     * 
     * @param expireTime 过期时间
     * @param msgStatus 消息状态
     * @return 过期消息列表
     */
    List<SysMessage> findByExpireTimeBeforeAndMsgStatusNot(LocalDateTime expireTime, String msgStatus);
    
    /**
     * 根据创建时间范围查找消息
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息列表
     */
    List<SysMessage> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据发送时间范围查找消息
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消息列表
     */
    List<SysMessage> findBySendTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找需要确认的消息
     * 
     * @param isConfirmed 是否确认
     * @param expireTime 过期时间
     * @return 需要确认的消息列表
     */
    List<SysMessage> findByIsConfirmedAndExpireTimeAfter(Boolean isConfirmed, LocalDateTime expireTime);
    
    /**
     * 根据删除状态查找消息
     * 
     * @param deleted 删除标识
     * @return 消息列表
     */
    List<SysMessage> findByDeleted(Integer deleted);
    
    /**
     * 查找用户最新的N条消息
     * 
     * @param receiverId 接收者ID
     * @param limit 限制条数
     * @return 最新消息列表
     */
    @Query("SELECT m FROM SysMessage m WHERE m.receiverId = :receiverId AND m.deleted = 0 ORDER BY m.createdTime DESC LIMIT :limit")
    List<SysMessage> findRecentMessages(@Param("receiverId") Long receiverId, @Param("limit") int limit);
    
    /**
     * 查找特定业务相关的最新消息
     * 
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param limit 限制条数
     * @return 业务相关消息
     */
    @Query("SELECT m FROM SysMessage m WHERE m.bizType = :bizType AND m.bizId = :bizId AND m.deleted = 0 ORDER BY m.createdTime DESC LIMIT :limit")
    List<SysMessage> findBizRelatedMessages(@Param("bizType") String bizType, @Param("bizId") Long bizId, @Param("limit") int limit);
    
    /**
     * 统计每日消息发送量
     * 
     * @param date 日期
     * @return 发送量统计
     */
    @Query("SELECT COUNT(m) FROM SysMessage m WHERE DATE(m.sendTime) = :date AND m.sendStatus = 'SENT'")
    long countDailySentMessages(@Param("date") java.time.LocalDate date);
    
    /**
     * 统计消息发送成功率
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成功率统计
     */
    @Query("SELECT COUNT(CASE WHEN m.sendStatus = 'SENT' THEN 1 END), COUNT(m) FROM SysMessage m WHERE m.sendTime BETWEEN :startTime AND :endTime")
    List<Object[]> countSendSuccessRate(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 按发送方式统计消息数量
     * 
     * @return 发送方式统计
     */
    @Query("SELECT m.sendMethod, COUNT(m) FROM SysMessage m WHERE m.deleted = 0 GROUP BY m.sendMethod")
    List<Object[]> countBySendMethod();
    
    /**
     * 查找未发送的重要消息
     * 
     * @param msgLevels 消息级别列表
     * @param sendStatus 发送状态
     * @return 重要消息列表
     */
    List<SysMessage> findByMsgLevelInAndSendStatus(List<String> msgLevels, String sendStatus);
    
    /**
     * 根据模板ID查找消息
     * 
     * @param templateId 模板ID
     * @return 消息列表
     */
    List<SysMessage> findByTemplateId(Long templateId);
    
    /**
     * 查找源相关消息
     * 
     * @param sourceType 源类型
     * @param sourceId 源ID
     * @return 源相关消息
     */
    List<SysMessage> findBySourceTypeAndSourceId(String sourceType, Long sourceId);
}