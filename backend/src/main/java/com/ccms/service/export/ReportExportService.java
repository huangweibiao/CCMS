package com.ccms.service.export;

import com.ccms.entity.report.ReportTemplate;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 报表导出服务接口
 */
public interface ReportExportService {

    /**
     * 导出Excel报表
     */
    void exportExcel(ReportTemplate template, Map<String, Object> data, HttpServletResponse response);

    /**
     * 导出PDF报表
     */
    void exportPDF(ReportTemplate template, Map<String, Object> data, HttpServletResponse response);

    /**
     * 导出Word报表
     */
    void exportWord(ReportTemplate template, Map<String, Object> data, HttpServletResponse response);

    /**
     * 导出CSV报表
     */
    void exportCSV(ReportTemplate template, Map<String, Object> data, HttpServletResponse response);

    /**
     * 导出HTML报表
     */
    String exportHTML(ReportTemplate template, Map<String, Object> data);

    /**
     * 生成报表分享链接
     */
    String generateShareLink(ReportTemplate template, Map<String, Object> data, ExportConfig config);

    /**
     * 获取分享的报表数据
     */
    Map<String, Object> getSharedReport(String shareToken);

    /**
     * 撤销分享链接
     */
    boolean revokeShareLink(String shareToken);

    /**
     * 检查分享链接有效性
     */
    boolean isShareValid(String shareToken);

    /**
     * 批量导出报表
     */
    void batchExport(String[] templateCodes, Map<String, Object> params, String format, HttpServletResponse response);
    
    /**
     * 导出配置
     */
    class ExportConfig {
    private Integer expireHours = 24; // 过期时间（小时）
    private Boolean allowDownload = true; // 是否允许下载
    private Boolean secureShare = false; // 是否加密分享
    private String password; // 分享密码

    public Integer getExpireHours() {
        return expireHours;
    }

    public void setExpireHours(Integer expireHours) {
        this.expireHours = expireHours;
    }

    public Boolean getAllowDownload() {
        return allowDownload;
    }

    public void setAllowDownload(Boolean allowDownload) {
        this.allowDownload = allowDownload;
    }

    public Boolean getSecureShare() {
        return secureShare;
    }

    public void setSecureShare(Boolean secureShare) {
        this.secureShare = secureShare;
    }

    public String getPassword() {
        return password;
    }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}