package com.ccms.repository.message;

import com.ccms.entity.message.MessageTemplate;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageTemplateRepository extends BaseRepository<MessageTemplate, Long> {
    
    Optional<MessageTemplate> findByTemplateCode(String templateCode);
    
    List<MessageTemplate> findByTemplateType(String templateType);
    
    List<MessageTemplate> findByBusinessType(String businessType);
    
    List<MessageTemplate> findByActiveTrue();
    
    @Query("SELECT mt FROM MessageTemplate mt WHERE mt.businessType = :businessType AND mt.active = true")
    List<MessageTemplate> findActiveTemplatesByBusinessType(@Param("businessType") String businessType);
    
    @Query("SELECT COUNT(mt) FROM MessageTemplate mt WHERE mt.businessType = :businessType")
    Long countByBusinessType(@Param("businessType") String businessType);
}