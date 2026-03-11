package com.nexterp.platform.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.platform.report.domain.model.SysReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * PDF导出服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final ObjectMapper objectMapper;

    /**
     * 导出数据为PDF
     *
     * @param report 报表定义
     * @param data   数据列表
     * @return PDF字节数组
     */
    public byte[] exportToPdf(SysReport report, List<Map<String, Object>> data) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 解析导出配置
            PdfExportConfig config = parseExportConfig(report.getExportConfig());

            // 创建页面
            PDPage page = new PDPage(getPageSize(config.getPageSize()));
            document.addPage(page);

            // 获取列定义
            List<ColumnDefinition> columns = parseColumnConfig(report.getColumnConfig());

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                // 设置边距
                float margin = config.getMargin();
                float yPosition = PDRectangle.A4.getHeight() - margin;
                float xPosition = margin;
                float tableWidth = PDRectangle.A4.getWidth() - 2 * margin;

                // 绘制标题
                if (config.isShowHeader() && config.getHeaderText() != null) {
                    drawText(contentStream, config.getHeaderText(), xPosition, yPosition,
                            config.getHeaderFontSize(), true);
                    yPosition -= config.getHeaderFontSize() + 10;
                }

                // 计算列宽
                float[] columnWidths = calculateColumnWidths(columns, tableWidth);

                // 绘制表头
                if (config.isShowTableHeader()) {
                    drawTableHeader(contentStream, columns, xPosition, yPosition, columnWidths);
                    yPosition -= config.getRowHeight();
                }

                // 绘制数据行
                PDFont font = PDType1Font.HELVETICA;
                int fontSize = config.getFontSize();

                for (Map<String, Object> row : data) {
                    // 检查是否需要新页面
                    if (yPosition < margin + config.getRowHeight()) {
                        contentStream.close();
                        PDPage newPage = new PDPage(getPageSize(config.getPageSize()));
                        document.addPage(newPage);
                        PDPageContentStream newContentStream = new PDPageContentStream(document, newPage);
                        newContentStream.setFont(font, fontSize);
                        yPosition = PDRectangle.A4.getHeight() - margin;
                        newContentStream.close();
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.setFont(font, fontSize);
                    }

                    drawTableRow(contentStream, columns, row, xPosition, yPosition, columnWidths);
                    yPosition -= config.getRowHeight();
                }

                // 绘制页脚
                if (config.isShowFooter()) {
                    float footerY = margin + 20;
                    if (config.isShowPageNumber()) {
                        String pageNumber = String.format("第 %d 页", document.getNumberOfPages());
                        drawText(contentStream, pageNumber, xPosition, footerY, 10, false);
                    }
                }
            }

            document.save(outputStream);
            log.info("PDF导出成功: report={}, rows={}", report.getReportCode(), data.size());
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("PDF导出失败", e);
            throw new BusinessException("PDF导出失败: " + e.getMessage());
        }
    }

    /**
     * 绘制文本
     */
    private void drawText(PDPageContentStream contentStream, String text,
                         float x, float y, int fontSize, boolean bold) throws IOException {
        PDFont font = bold ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA;
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text != null ? text : "");
        contentStream.endText();
    }

    /**
     * 绘制表头
     */
    private void drawTableHeader(PDPageContentStream contentStream, List<ColumnDefinition> columns,
                                 float x, float y, float[] columnWidths) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.setNonStrokingColor(200, 200, 200);

        // 绘制背景
        float totalWidth = 0;
        for (float width : columnWidths) {
            totalWidth += width;
        }
        contentStream.addRect(x, y - 15, totalWidth, 15);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0);

        // 绘制文本
        float currentX = x;
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition column = columns.get(i);
            contentStream.beginText();
            contentStream.newLineAtOffset(currentX + 5, y - 5);
            contentStream.showText(column.getLabel());
            contentStream.endText();
            currentX += columnWidths[i];
        }
    }

    /**
     * 绘制数据行
     */
    private void drawTableRow(PDPageContentStream contentStream, List<ColumnDefinition> columns,
                              Map<String, Object> row, float x, float y, float[] columnWidths) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, 10);

        float currentX = x;
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition column = columns.get(i);
            Object value = row.get(column.getField());
            String text = value != null ? value.toString() : "";

            // 截断过长的文本
            if (text.length() > 50) {
                text = text.substring(0, 47) + "...";
            }

            contentStream.beginText();
            contentStream.newLineAtOffset(currentX + 5, y - 5);
            contentStream.showText(text);
            contentStream.endText();
            currentX += columnWidths[i];
        }
    }

    /**
     * 计算列宽
     */
    private float[] calculateColumnWidths(List<ColumnDefinition> columns, float tableWidth) {
        float[] widths = new float[columns.size()];
        float totalWidth = 0;

        // 首先使用配置的宽度
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getWidth() != null && columns.get(i).getWidth() > 0) {
                widths[i] = columns.get(i).getWidth();
                totalWidth += widths[i];
            }
        }

        // 剩余宽度平均分配
        int remainingColumns = 0;
        for (int i = 0; i < columns.size(); i++) {
            if (widths[i] == 0) {
                remainingColumns++;
            }
        }

        if (remainingColumns > 0) {
            float remainingWidth = tableWidth - totalWidth;
            float avgWidth = remainingWidth / remainingColumns;
            for (int i = 0; i < columns.size(); i++) {
                if (widths[i] == 0) {
                    widths[i] = avgWidth;
                }
            }
        }

        return widths;
    }

    /**
     * 解析导出配置
     */
    private PdfExportConfig parseExportConfig(String exportConfig) {
        try {
            if (exportConfig != null && !exportConfig.isEmpty()) {
                return objectMapper.readValue(exportConfig, PdfExportConfig.class);
            }
        } catch (Exception e) {
            log.warn("解析导出配置失败，使用默认配置", e);
        }
        return new PdfExportConfig();
    }

    /**
     * 解析列配置
     */
    @SuppressWarnings("unchecked")
    private List<ColumnDefinition> parseColumnConfig(String columnConfig) {
        try {
            if (columnConfig != null && !columnConfig.isEmpty()) {
                return objectMapper.readValue(columnConfig, List.class);
            }
        } catch (Exception e) {
            log.warn("解析列配置失败", e);
        }
        return List.of();
    }

    /**
     * 获取页面大小
     */
    private PDRectangle getPageSize(String pageSize) {
        if ("A4".equalsIgnoreCase(pageSize)) {
            return PDRectangle.A4;
        } else if ("A3".equalsIgnoreCase(pageSize)) {
            return PDRectangle.A3;
        } else if ("LETTER".equalsIgnoreCase(pageSize)) {
            return PDRectangle.LETTER;
        }
        return PDRectangle.A4;
    }

    /**
     * PDF导出配置
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PdfExportConfig {
        private String pageSize = "A4";
        private String orientation = "portrait";
        private float margin = 50;
        private boolean showHeader = true;
        private String headerText;
        private int headerFontSize = 16;
        private boolean showTableHeader = true;
        private boolean showFooter = true;
        private boolean showPageNumber = true;
        private int fontSize = 10;
        private int rowHeight = 20;
    }

    /**
     * 列定义
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ColumnDefinition {
        private String field;
        private String label;
        private Integer width;
        private String align = "left";
    }
}
