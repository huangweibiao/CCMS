package com.ccms.repository.finance;

import com.ccms.entity.finance.VoucherTemplate;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 凭证模板Repository接口
 * 对应表名：finance_voucher_template
 */
@Repository
public interface FinanceVoucherTemplateRepository extends BaseRepository<VoucherTemplate, Long> {

    /**
     * 根据模板编码查询
     * @param templateCode 模板编码
     * @return 凭证模板信息
     */
    @Query("SELECT t FROM VoucherTemplate t WHERE t.templateCode = :templateCode")
    VoucherTemplate findByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 根据模板类型查询
     * @param voucherType 凭证类型
     * @return 凭证模板列表
     */
    @Query("SELECT t FROM VoucherTemplate t WHERE t.voucherType = :voucherType")
    List<VoucherTemplate> findByVoucherType(@Param("voucherType") String voucherType);

    /**
     * 根据业务类型查询
     * @param businessType 业务类型
     * @return 凭证模板列表
     */
    @Query("SELECT t FROM VoucherTemplate t WHERE t.businessType = :businessType")
    List<VoucherTemplate> findByBusinessType(@Param("businessType") String businessType);

    /**
     * 根据凭证类型和业务类型联合查询
     * @param voucherType 凭证类型
     * @param businessType 业务类型
     * @return 凭证模板信息
     */
    @Query("SELECT t FROM VoucherTemplate t WHERE t.voucherType = :voucherType AND t.businessType = :businessType")
    VoucherTemplate findByVoucherTypeAndBusinessType(@Param("voucherType") String voucherType, @Param("businessType") String businessType);

    /**
     * 根据状态查询
     * @param status 状态
     * @return 凭证模板列表
     */
    @Query("SELECT t FROM VoucherTemplate t WHERE t.status = :status")
    List<VoucherTemplate> findByStatus(@Param("status") Integer status);

    /**
     * 查询启用的模板
     * @return 启用的凭证模板列表
     */
    @Query("SELECT t FROM VoucherTemplate t WHERE t.status = 1 ORDER BY t.createTime DESC")
    List<VoucherTemplate> findActiveTemplates();
}