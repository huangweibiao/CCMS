package com.ccms.repository.message;

import com.ccms.entity.message.Message;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends BaseRepository<Message, Long> {
    
    List<Message> findByReceiverId(Long receiverId);
    
    List<Message> findBySenderId(Long senderId);
    
    List<Message> findByReceiverIdAndReadFalse(Long receiverId);
    
    List<Message> findByMessageType(String messageType);
    
    List<Message> findByBusinessTypeAndBusinessId(String businessType, Long businessId);
    
    List<Message> findByReceiverIdAndStatus(Long receiverId, String status);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiverId = :receiverId AND m.read = false")
    Long countUnreadMessages(@Param("receiverId") Long receiverId);
    
    @Query("SELECT m FROM Message m WHERE m.expireTime < :now AND m.status = 'active'")
    List<Message> findExpiredMessages(@Param("now") LocalDateTime now);
}