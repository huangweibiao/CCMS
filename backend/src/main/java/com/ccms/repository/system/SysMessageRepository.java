package com.ccms.repository.system;

import com.ccms.entity.system.SysMessage;
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
     * @param readStatus 阅读状态：0-未读，1-已读
     * @return 消息列表
     */
    List<SysMessage> findByReadStatus(Integer readStatus);

    /**
     * 查询用户的未读消息
     * 
     * @param receiverId 接收人ID
     * @return 未读消息列表
     */
    List<SysMessage> findByReceiverIdAndReadStatus(Long receiverId, Integer readStatus);

    /**
     * 查询用户的未读消息数量
     * 
     * @param receiverId 接收人ID
     * @return 未读消息数量
     */
    Long countByReceiverIdAndReadStatus(Long receiverId, Integer readStatus);

    /**
     * 根据业务ID和业务类型查询消息
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 消息列表
     */
    List<SysMessage> findByBusinessIdAndBusinessType(Long businessId, Integer businessType);

    /**
     * 更新消息阅读状态
     * 
     * @param id 消息ID
     * @param readStatus 阅读状态
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE SysMessage sm SET sm.readStatus = :readStatus, sm.readTime = CURRENT_TIMESTAMP WHERE sm.id = :id")
    void updateReadStatus(@Param("id") Long id, @Param("readStatus") Integer readStatus);

    /**
     * 批量更新消息阅读状态
     * 
     * @param receiverId 接收人ID
     * @param readStatus 新阅读状态
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE SysMessage sm SET sm.readStatus = :readStatus, sm.readTime = CURRENT_TIMESTAMP WHERE sm.receiverId = :receiverId AND sm.readStatus = 0")
    void batchUpdateReadStatus(@Param("receiverId") Long receiverId, @Param("readStatus") Integer readStatus);

    /**
     * 删除过期消息
     * 
     * @param expireDays 过期天数
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM SysMessage sm WHERE sm.createTime < CURRENT_DATE - :expireDays")
    void deleteExpiredMessages(@Param("expireDays") Integer expireDays);

    /**
     * 查询最近一周的消息
     * 
     * @param receiverId 接收人ID
     * @return 消息列表
     */
    @Query("SELECT sm FROM SysMessage sm WHERE sm.receiverId = :receiverId AND sm.createTime >= CURRENT_DATE - 7 ORDER BY sm.createTime DESC")
    List<SysMessage> findRecentMessagesByUserId(@Param("receiverId") Long receiverId);
}