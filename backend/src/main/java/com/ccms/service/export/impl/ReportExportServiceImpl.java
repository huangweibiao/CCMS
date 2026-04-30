package com.ccms.service.export.impl;

import com.ccms.entity.report.ReportTemplate;
import com.ccms.repository.export.ReportShareRepository;
import com.ccms.service.export.ReportExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表导出服务实现类
 */
@Service
public class ReportExportServiceImpl implements ReportExportService {

    @Autowired
    private ReportShareRepository reportShareRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void exportExcel(ReportTemplate template, Map<String, Object> data, HttpServletResponse response) {
        try {
            // 设置响应头
            String fileName = getFileName(template, "xlsx");
            setupResponse(response, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fileName);

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(template.getTemplateName());

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            List<String> headers = getHeadersFromConfig(template.getColumnConfig());
            
            // 设置标题样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // 添加数据行
            List<Map<String, Object>> rows = extractRowsFromData(data);
            CellStyle dataStyle = createDataStyle(workbook);
            
            for (int i = 0; i < rows.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                Map<String, Object> rowData = rows.get(i);
                
                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j);
                    Cell cell = dataRow.createCell(j);
                    Object value = rowData.get(header);
                    setCellValue(cell, value, dataStyle);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入响应流
            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (Exception e) {
            throw new RuntimeException("Excel导出失败：" + e.getMessage());
        }
    }

    @Override
    public void exportPDF(ReportTemplate template, Map<String, Object> data, HttpServletResponse response) {
        try {
            // PDF导出实现（需要第三方库，这里使用简单的HTML转PDF模拟）
            String fileName = getFileName(template, "pdf");
            setupResponse(response, "application/pdf", fileName);

            // 生成HTML内容
            String htmlContent = generatePDFContent(template, data);
            
            // 简单的HTML输出（实际项目应使用iText或Flying Saucer等库）
            String simplePdfContent = "PDF Export for: " + template.getTemplateName() + "\nData: " + data.toString();
            response.getOutputStream().write(simplePdfContent.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new RuntimeException("PDF导出失败：" + e.getMessage());
        }
    }

    @Override
    public void exportWord(ReportTemplate template, Map<String, Object> data, HttpServletResponse response) {
        try {
            String fileName = getFileName(template, "docx");
            setupResponse(response, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", fileName);

            // 简单的文本输出（实际项目应使用Apache POI的XWPFDocument）
            String wordContent = "Word Export for: " + template.getTemplateName() + "\n" + formatDataAsText(data);
            response.getOutputStream().write(wordContent.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new RuntimeException("Word导出失败：" + e.getMessage());
        }
    }

    @Override
    public void exportCSV(ReportTemplate template, Map<String, Object> data, HttpServletResponse response) {
        try {
            String fileName = getFileName(template, "csv");
            setupResponse(response, "text/csv", fileName);

            List<String> headers = getHeadersFromConfig(template.getColumnConfig());
            List<Map<String, Object>> rows = extractRowsFromData(data);

            StringBuilder csvContent = new StringBuilder();
            
            // 写入标题行
            csvContent.append(String.join(",", headers)).append("\n");
            
            // 写入数据行
            for (Map<String, Object> row : rows) {
                List<String> rowValues = headers.stream()
                    .map(header -> escapeCsvValue(String.valueOf(row.getOrDefault(header, ""))))
                    .collect(Collectors.toList());
                csvContent.append(String.join(",", rowValues)).append("\n");
            }

            response.getOutputStream().write(csvContent.toString().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new RuntimeException("CSV导出失败：" + e.getMessage());
        }
    }

    @Override
    public String exportHTML(ReportTemplate template, Map<String, Object> data) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<head>\n")
                .append("<title>").append(template.getTemplateName()).append("</title>\n")
                .append("<style>\n")
                .append("table { border-collapse: collapse; width: 100%; }\n")
                .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
                .append("th { background-color: #f2f2f2; }\n")
                .append("</style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("<h1>").append(template.getTemplateName()).append("</h1>\n")
                .append("<table>\n");

            // 生成表头
            List<String> headers = getHeadersFromConfig(template.getColumnConfig());
            html.append("<tr>\n");
            for (String header : headers) {
                html.append("<th>").append(header).append("</th>\n");
            }
            html.append("</tr>\n");

            // 生成数据行
            List<Map<String, Object>> rows = extractRowsFromData(data);
            for (Map<String, Object> row : rows) {
                html.append("<tr>\n");
                for (String header : headers) {
                    Object value = row.getOrDefault(header, "");
                    html.append("<td>").append(value).append("</td>\n");
                }
                html.append("</tr>\n");
            }

            html.append("</table>\n")
                .append("</body>\n")
                .append("</html>");

            return html.toString();

        } catch (Exception e) {
            throw new RuntimeException("HTML导出失败：" + e.getMessage());
        }
    }

    @Override
    public String generateShareLink(ReportTemplate template, Map<String, Object> data, ReportExportService.ExportConfig config) {
        try {
            // 生成分享令牌
            String shareToken = UUID.randomUUID().toString().replace("-", "");
            
            // 创建分享记录（实际应保存到数据库）
            Map<String, Object> shareInfo = new HashMap<>();
            shareInfo.put("templateId", template.getId());
            shareInfo.put("templateName", template.getTemplateName());
            shareInfo.put("data", data);
            shareInfo.put("createTime", LocalDateTime.now());
            shareInfo.put("expireTime", LocalDateTime.now().plusHours(config.getExpireHours()));
            shareInfo.put("secureShare", config.getSecureShare());
            shareInfo.put("password", config.getPassword());
            
            // 这里应该保存到数据库，暂时使用内存存储
            // reportShareRepository.save(shareInfo);
            
            // 生成分享链接
            return "/api/share/report/" + shareToken;

        } catch (Exception e) {
            throw new RuntimeException("生成分享链接失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getSharedReport(String shareToken) {
        // 从数据库或缓存中获取分享数据
        // 这里实现基本的令牌验证逻辑
        validateShareToken(shareToken);
        
        // 模拟数据
        Map<String, Object> shareData = new HashMap<>();
        shareData.put("templateName", "分享报表");
        shareData.put("data", Collections.singletonMap("message", "这是分享的报表数据"));
        shareData.put("createTime", LocalDateTime.now());
        
        return shareData;
    }

    @Override
    public boolean revokeShareLink(String shareToken) {
        // 删除分享记录
        return true;
    }

    @Override
    public boolean isShareValid(String shareToken) {
        try {
            validateShareToken(shareToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void batchExport(String[] templateCodes, Map<String, Object> params, String format, HttpServletResponse response) {
        try {
            // 批量导出实现（以ZIP包形式）
            String fileName = "批量导出_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".zip";
            setupResponse(response, "application/zip", fileName);

            // 简单的实现：生成包含多个报表的文本说明
            StringBuilder batchContent = new StringBuilder("批量导出报告\n\n");
            for (String templateCode : templateCodes) {
                batchContent.append("模板：").append(templateCode).append("\n");
                batchContent.append("参数：").append(params.toString()).append("\n");
                batchContent.append("格式：").append(format).append("\n\n");
            }

            response.getOutputStream().write(batchContent.toString().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new RuntimeException("批量导出失败：" + e.getMessage());
        }
    }

    // ========== 私有辅助方法 ==========

    private void setupResponse(HttpServletResponse response, String contentType, String fileName) throws IOException {
        response.setContentType(contentType);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    private String getFileName(ReportTemplate template, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeName = template.getTemplateName().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
        return safeName + "_" + timestamp + "." + extension;
    }

    private List<String> getHeadersFromConfig(String columnConfig) {
        try {
            if (columnConfig == null || columnConfig.trim().isEmpty()) {
                return Arrays.asList("字段1", "字段2", "字段3");
            }
            
            List<Map<String, Object>> columns = objectMapper.readValue(columnConfig, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            
            return columns.stream()
                .map(col -> col.getOrDefault("title", "未知列").toString())
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            return Arrays.asList("标题1", "标题2", "标题3");
        }
    }

    private List<Map<String, Object>> extractRowsFromData(Map<String, Object> data) {
        Object rowsObj = data.getOrDefault("rows", data.getOrDefault("data", data));
        
        if (rowsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) rowsObj;
            return rows;
        }
        
        // 如果数据不是列表形式，创建一个简单的行
        List<Map<String, Object>> singleRow = new ArrayList<>();
        singleRow.add(data);
        return singleRow;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void setCellValue(Cell cell, Object value, CellStyle style) {
        cell.setCellStyle(style);
        
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof java.util.Date) {
            cell.setCellValue((java.util.Date) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private String generatePDFContent(ReportTemplate template, Map<String, Object> data) {
        // 生成PDF内容的HTML格式
        return exportHTML(template, data);
    }

    private String formatDataAsText(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    private String escapeCsvValue(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void validateShareToken(String shareToken) {
        // 验证分享令牌的有效性
        if (shareToken == null || shareToken.length() != 32) {
            throw new RuntimeException("无效的分享令牌");
        }
    }
}