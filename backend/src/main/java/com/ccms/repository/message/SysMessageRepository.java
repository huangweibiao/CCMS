package com.ccms.repository.message;

import com.ccms.entity.message.SysMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统消息表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface SysMessageRepository extends JpaRepository<SysMessage, Long> {

    /**
     * 根据接收人ID查询消息
     * 
     * @param receiverId 接收人ID
     * @return 消息列表
     */
    List<SysMessage> findByReceiverId(Long receiverId);

    /**
     * 根据消息类型查询消息
     * 
     * @param messageType 消息类型：1-系统通知，2-审批提醒，3-费用提醒，4-预算提醒，5-其他
     * @return 消息列表
     */
    List<SysMessage> findByMessageType(Integer messageType);

    /**
     * 根据阅读状态查询消息
     * 
     * @param isRead 阅读状态：0-未读，1-已读
     * @return 消息列表
     */
    List<SysMessage> findByIsRead(Integer isRead);

    /**
     * 查询用户的未读消息
     * 
     * @param receiverId 接收人ID
     * @param isRead 阅读状态
     * @return 消息列表
     */
    List<SysMessage> findByReceiverIdAndIsRead(Long receiverId, Integer isRead);

    /**
     * 查询用户的未读消息数量
     * 
     * @param receiverId 接收人ID
     * @param isRead 阅读状态
     * @return 消息数量
     */
    Long countByReceiverIdAndIsRead(Long receiverId, Integer isRead);

    /**
     * 根据业务ID和业务类型查询消息
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 消息列表
     */
    List<SysMessage> findByBusinessIdAndBusinessType(Long businessId, String businessType);

    /**
     * 更新消息阅读状态
     * 
     * @param id 消息ID
     * @param isRead 阅读状态
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE SysMessage sm SET sm.isRead = :isRead, sm.readTime = CURRENT_TIMESTAMP WHERE sm.id = :id")
    void updateIsRead(@Param("id") Long id, @Param("isRead") Integer isRead);

    /**
     * 批量更新消息阅读状态
     * 
     * @param receiverId 接收人ID
     * @param isRead 新阅读状态
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE SysMessage sm SET sm.isRead = :isRead, sm.readTime = CURRENT_TIMESTAMP WHERE sm.receiverId = :receiverId AND sm.isRead = 0")
    void batchUpdateIsRead(@Param("receiverId") Long receiverId, @Param("isRead") Integer isRead);

    /**
     * 删除过期消息
     * 
     * @param expireDays 过期天数
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM SysMessage sm WHERE sm.createTime < FUNCTION('DATE_SUB', CURRENT_TIMESTAMP, :expireDays, 'DAY')")
    void deleteExpiredMessages(@Param("expireDays") Integer expireDays);

    /**
     * 查询最近一周的消息
     * 
     * @param receiverId 接收人ID
     * @return 消息列表
     */
    @Query("SELECT sm FROM SysMessage sm WHERE sm.receiverId = :receiverId AND sm.createTime >= FUNCTION('DATE_SUB', CURRENT_TIMESTAMP, 7, 'DAY') ORDER BY sm.createTime DESC")
    List<SysMessage> findRecentMessagesByUserId(@Param("receiverId") Long receiverId);
}