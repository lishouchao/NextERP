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
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
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

    // PDFBox 3.x 字体常量
    private static final PDFont FONT_HELVETICA = new PDType1Font(FontName.HELVETICA);
    private static final PDFont FONT_HELVETICA_BOLD = new PDType1Font(FontName.HELVETICA_BOLD);

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

            // 获取列定义
            List<ColumnDefinition> columns = parseColumnConfig(report.getColumnConfig());

            // 设置边距
            float margin = config.getMargin();
            float yPosition = PDRectangle.A4.getHeight() - margin;
            float xPosition = margin;
            float tableWidth = PDRectangle.A4.getWidth() - 2 * margin;

            // 创建第一页
            PDPage currentPage = new PDPage(getPageSize(config.getPageSize()));
            document.addPage(currentPage);
            PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

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
            PDFont font = FONT_HELVETICA;
            int fontSize = config.getFontSize();
            float currentY = yPosition;

            for (Map<String, Object> row : data) {
                // 检查是否需要新页面
                if (currentY < margin + config.getRowHeight()) {
                    // 绘制当前页页脚
                    if (config.isShowFooter() && config.isShowPageNumber()) {
                        String pageNumber = String.format("第 %d 页", document.getNumberOfPages());
                        drawText(contentStream, pageNumber, xPosition, margin + 20, 10, false);
                    }
                    contentStream.close();

                    // 创建新页面
                    currentPage = new PDPage(getPageSize(config.getPageSize()));
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                    currentY = PDRectangle.A4.getHeight() - margin;

                    // 重新绘制表头
                    if (config.isShowTableHeader()) {
                        drawTableHeader(contentStream, columns, xPosition, currentY, columnWidths);
                        currentY -= config.getRowHeight();
                    }
                }

                drawTableRow(contentStream, columns, row, xPosition, currentY, columnWidths);
                currentY -= config.getRowHeight();
            }

            // 绘制最后一页的页脚
            if (config.isShowFooter()) {
                float footerY = margin + 20;
                if (config.isShowPageNumber()) {
                    String pageNumber = String.format("第 %d 页", document.getNumberOfPages());
                    drawText(contentStream, pageNumber, xPosition, footerY, 10, false);
                }
            }

            contentStream.close();
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
        PDFont font = bold ? FONT_HELVETICA_BOLD : FONT_HELVETICA;
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
        contentStream.setFont(FONT_HELVETICA_BOLD, 12);
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
        contentStream.setFont(FONT_HELVETICA, 10);

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
