package com.ccms.repository.expense;

import com.ccms.entity.expense.ReimburseAttachment;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReimburseAttachmentRepository extends BaseRepository<ReimburseAttachment, Long> {
    
    List<ReimburseAttachment> findByReimburseMainId(Long reimburseMainId);
    
    @Query("SELECT ra FROM ReimburseAttachment ra WHERE ra.reimburseMainId = :mainId ORDER BY ra.createdTime DESC")
    List<ReimburseAttachment> findByReimburseMainIdOrderByCreatedTimeDesc(@Param("mainId") Long mainId);
    
    @Query("SELECT ra.fileName FROM ReimburseAttachment ra WHERE ra.reimburseMainId = :mainId")
    List<String> findFileNamesByReimburseMainId(@Param("mainId") Long mainId);
    
    @Query("SELECT COUNT(ra) FROM ReimburseAttachment ra WHERE ra.reimburseMainId = :mainId")
    Long countByReimburseMainId(@Param("mainId") Long mainId);
}