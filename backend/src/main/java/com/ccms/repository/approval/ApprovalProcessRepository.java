package com.ccms.repository.approval;

import com.ccms.entity.approval.ApprovalProcess;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalProcessRepository extends BaseRepository<ApprovalProcess, Long> {
    
    Optional<ApprovalProcess> findByProcessCode(String processCode);
    
    List<ApprovalProcess> findByBusinessType(String businessType);
    
    List<ApprovalProcess> findByActiveTrue();
    
    @Query("SELECT ap FROM ApprovalProcess ap WHERE ap.businessType = :businessType AND ap.active = true")
    List<ApprovalProcess> findActiveByBusinessType(@Param("businessType") String businessType);
}