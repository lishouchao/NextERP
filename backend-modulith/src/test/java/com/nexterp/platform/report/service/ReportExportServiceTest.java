package com.nexterp.platform.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.platform.report.domain.model.SysReport;
import com.nexterp.platform.report.domain.repository.SysReportRepository;
import com.nexterp.shared.core.exception.BusinessException;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 报表导出服务测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
class ReportExportServiceTest {

    @Mock
    private SysReportRepository reportRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReportExportService exportService;

    private SysReport testReport;

    @BeforeEach
    void setUp() {
        testReport = SysReport.builder()
                .id(1L)
                .tenantId(1L)
                .reportCode("RPT_TEST_001")
                .reportName("测试报表")
                .reportType("table")
                .datasourceType("sql")
                .datasourceConfig("{\"sql\":\"SELECT id, name FROM test_table\"}")
                .reportConfig("{\"columns\":[{\"field\":\"id\",\"title\":\"ID\"},{\"field\":\"name\",\"title\":\"名称\"}]}")
                .status(1)
                .build();
    }

    @Test
    @DisplayName("导出Excel - 成功")
    void testExportToExcel_Success() {
        // Given
        List<Map<String, Object>> mockData = new ArrayList<>();
        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("id", 1);
        row1.put("name", "测试1");
        mockData.add(row1);

        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("id", 2);
        row2.put("name", "测试2");
        mockData.add(row2);

        when(reportRepository.findByReportCode("RPT_TEST_001", 1L))
                .thenReturn(Optional.of(testReport));
        when(jdbcTemplate.queryForList(anyString())).thenReturn(mockData);

        // When
        byte[] result = exportService.exportToExcel("RPT_TEST_001", 1L, Map.of());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("导出Excel - 报表不存在")
    void testExportToExcel_ReportNotFound() {
        // Given
        when(reportRepository.findByReportCode("NOT_EXIST", 1L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> exportService.exportToExcel("NOT_EXIST", 1L, Map.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("报表不存在");
    }

    @Test
    @DisplayName("导出Excel - 带参数")
    void testExportToExcel_WithParams() {
        // Given
        List<Map<String, Object>> mockData = List.of(
                Map.of("id", 1, "name", "测试1")
        );

        when(reportRepository.findByReportCode("RPT_TEST_001", 1L))
                .thenReturn(Optional.of(testReport));
        when(jdbcTemplate.queryForList(anyString())).thenReturn(mockData);

        Map<String, Object> params = Map.of("tenantId", "1", "period", "2024-01");

        // When
        byte[] result = exportService.exportToExcel("RPT_TEST_001", 1L, params);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("导出Excel - 空数据")
    void testExportToExcel_EmptyData() {
        // Given
        when(reportRepository.findByReportCode("RPT_TEST_001", 1L))
                .thenReturn(Optional.of(testReport));
        when(jdbcTemplate.queryForList(anyString())).thenReturn(Collections.emptyList());

        // When
        byte[] result = exportService.exportToExcel("RPT_TEST_001", 1L, Map.of());

        // Then
        assertThat(result).isNotNull();
        // 空数据也应该生成Excel文件（只有表头）
    }
}
