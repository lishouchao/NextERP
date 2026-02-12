package com.nexterp.platform.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.platform.report.domain.model.SysReport;
import com.nexterp.platform.report.domain.repository.SysReportRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * 报表导出服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final SysReportRepository reportRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 导出报表为Excel
     *
     * @param reportCode 报表编码
     * @param tenantId   租户ID
     * @param params     查询参数
     * @return Excel字节数组
     */
    public byte[] exportToExcel(String reportCode, Long tenantId, Map<String, Object> params) {
        // 获取报表配置
        SysReport report = reportRepository.findByReportCode(reportCode, tenantId)
                .orElseThrow(() -> new BusinessException("报表不存在"));

        try {
            // 查询数据
            List<Map<String, Object>> data = executeReportQuery(report, params);

            // 创建Excel工作簿
            try (SXSSFWorkbook workbook = new SXSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                Sheet sheet = workbook.createSheet(report.getReportName());

                // 创建表头样式
                CellStyle headerStyle = createHeaderStyle(workbook);

                // 创建数据样式
                CellStyle dataStyle = createDataStyle(workbook);

                // 获取列配置
                List<ColumnConfig> columns = parseColumnConfig(report);

                // 写入表头
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < columns.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns.get(i).getTitle());
                    cell.setCellStyle(headerStyle);
                }

                // 写入数据
                int rowNum = 1;
                for (Map<String, Object> rowData : data) {
                    Row row = sheet.createRow(rowNum++);
                    for (int i = 0; i < columns.size(); i++) {
                        Cell cell = row.createCell(i);
                        Object value = rowData.get(columns.get(i).getField());
                        setCellValue(cell, value);
                        cell.setCellStyle(dataStyle);
                    }
                }

                // 自动调整列宽
                for (int i = 0; i < columns.size(); i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            log.error("导出Excel失败", e);
            throw new BusinessException("导出Excel失败");
        }
    }

    /**
     * 执行报表查询
     *
     * @param report 报表
     * @param params 查询参数
     * @return 查询结果
     */
    private List<Map<String, Object>> executeReportQuery(SysReport report, Map<String, Object> params) {
        if ("sql".equals(report.getDatasourceType())) {
            // SQL查询
            String sql = parseSqlConfig(report.getDatasourceConfig(), params);
            return jdbcTemplate.queryForList(sql);
        } else if ("api".equals(report.getDatasourceType())) {
            // API接口查询
            // TODO: 实现API接口查询
            throw new BusinessException("暂不支持API数据源");
        } else {
            throw new BusinessException("不支持的数据源类型");
        }
    }

    /**
     * 解析SQL配置
     *
     * @param config 配置JSON
     * @param params 查询参数
     * @return SQL语句
     */
    private String parseSqlConfig(String config, Map<String, Object> params) {
        try {
            Map<String, Object> configMap = objectMapper.readValue(config, Map.class);
            String sql = (String) configMap.get("sql");

            // 简单的参数替换
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String placeholder = "${" + entry.getKey() + "}";
                    if (sql.contains(placeholder)) {
                        sql = sql.replace(placeholder, String.valueOf(entry.getValue()));
                    }
                }
            }

            return sql;
        } catch (Exception e) {
            log.error("解析SQL配置失败", e);
            throw new BusinessException("解析SQL配置失败");
        }
    }

    /**
     * 解析列配置
     *
     * @param report 报表
     * @return 列配置列表
     */
    private List<ColumnConfig> parseColumnConfig(SysReport report) {
        try {
            Map<String, Object> configMap = objectMapper.readValue(report.getReportConfig(), Map.class);
            List<Map<String, Object>> columnsList = (List<Map<String, Object>>) configMap.get("columns");

            List<ColumnConfig> columns = new ArrayList<>();
            if (columnsList != null) {
                for (Map<String, Object> columnMap : columnsList) {
                    ColumnConfig config = new ColumnConfig();
                    config.setField((String) columnMap.get("field"));
                    config.setTitle((String) columnMap.get("title"));
                    columns.add(config);
                }
            }
            return columns;
        } catch (Exception e) {
            log.error("解析列配置失败", e);
            throw new BusinessException("解析列配置失败");
        }
    }

    /**
     * 创建表头样式
     *
     * @param workbook 工作簿
     * @return 样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    /**
     * 创建数据样式
     *
     * @param workbook 工作簿
     * @return 样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 设置单元格值
     *
     * @param cell  单元格
     * @param value 值
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    /**
     * 列配置
     */
    private static class ColumnConfig {
        private String field;
        private String title;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
