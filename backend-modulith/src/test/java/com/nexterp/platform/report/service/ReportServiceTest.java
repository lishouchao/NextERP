package com.nexterp.platform.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.platform.report.domain.model.SysReport;
import com.nexterp.platform.report.domain.repository.SysReportRepository;
import com.nexterp.platform.report.dto.request.ReportCreateRequest;
import com.nexterp.platform.report.dto.request.ReportQueryRequest;
import com.nexterp.platform.report.dto.response.ReportResponse;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 报表服务测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private SysReportRepository reportRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReportService reportService;

    private ObjectMapper objectMapper;
    private SysReport testReport;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        testReport = SysReport.builder()
                .id(1L)
                .tenantId(1L)
                .reportCode("RPT_TEST_001")
                .reportName("测试报表")
                .reportType("table")
                .datasourceType("sql")
                .datasourceConfig("{\"sql\":\"SELECT * FROM test\"}")
                .reportConfig("{\"columns\":[]}")
                .status(1)
                .isDeleted(false)
                .build();
    }

    @Test
    @DisplayName("创建报表 - 成功")
    void testCreateReport_Success() {
        // Given
        ReportCreateRequest request = new ReportCreateRequest();
        request.setTenantId(1L);
        request.setReportCode("RPT_TEST_001");
        request.setReportName("测试报表");
        request.setReportType("table");
        request.setDatasourceType("sql");
        request.setDatasourceConfig(Map.of("sql", "SELECT * FROM test"));

        when(reportRepository.existsByReportCode("RPT_TEST_001", 1L)).thenReturn(false);
        when(reportRepository.save(any(SysReport.class))).thenReturn(testReport);

        // When
        ReportResponse response = reportService.createReport(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getReportCode()).isEqualTo("RPT_TEST_001");
        assertThat(response.getReportName()).isEqualTo("测试报表");
        verify(reportRepository).save(any(SysReport.class));
    }

    @Test
    @DisplayName("创建报表 - 编码已存在")
    void testCreateReport_CodeExists() {
        // Given
        ReportCreateRequest request = new ReportCreateRequest();
        request.setTenantId(1L);
        request.setReportCode("RPT_TEST_001");
        request.setReportName("测试报表");
        request.setReportType("table");

        when(reportRepository.existsByReportCode("RPT_TEST_001", 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> reportService.createReport(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("报表编码已存在");
    }

    @Test
    @DisplayName("获取报表详情 - 成功")
    void testGetReportById_Success() {
        // Given
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));

        // When
        ReportResponse response = reportService.getReportById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getReportCode()).isEqualTo("RPT_TEST_001");
    }

    @Test
    @DisplayName("获取报表详情 - 不存在")
    void testGetReportById_NotFound() {
        // Given
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reportService.getReportById(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("报表不存在");
    }

    @Test
    @DisplayName("删除报表 - 成功")
    void testDeleteReport_Success() {
        // Given
        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(SysReport.class))).thenReturn(testReport);

        // When
        reportService.deleteReport(1L);

        // Then
        verify(reportRepository).save(argThat(report -> report.getIsDeleted()));
    }

    @Test
    @DisplayName("分页查询报表")
    void testListReports() {
        // Given
        ReportQueryRequest request = new ReportQueryRequest();
        request.setTenantId(1L);

        List<SysReport> reports = List.of(testReport);
        Page<SysReport> page = new PageImpl<>(reports);

        when(reportRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        PageResult<ReportResponse> result = reportService.listReports(request, 1, 10);

        // Then
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("查询报表数据 - SQL数据源")
    void testQueryReportData_Sql() {
        // Given
        List<Map<String, Object>> mockData = List.of(
                Map.of("id", 1, "name", "test1"),
                Map.of("id", 2, "name", "test2")
        );

        when(reportRepository.findByReportCode("RPT_TEST_001", 1L))
                .thenReturn(Optional.of(testReport));
        when(jdbcTemplate.queryForList(anyString())).thenReturn(mockData);

        // When
        List<Map<String, Object>> result = reportService.queryReportData("RPT_TEST_001", 1L, Map.of());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("name")).isEqualTo("test1");
    }

    @Test
    @DisplayName("更新报表 - 成功")
    void testUpdateReport_Success() {
        // Given
        ReportCreateRequest request = new ReportCreateRequest();
        request.setTenantId(1L);
        request.setReportCode("RPT_TEST_001");
        request.setReportName("更新后的报表");
        request.setReportType("chart");

        when(reportRepository.findById(1L)).thenReturn(Optional.of(testReport));
        when(reportRepository.save(any(SysReport.class))).thenReturn(testReport);

        // When
        ReportResponse response = reportService.updateReport(1L, request);

        // Then
        assertThat(response).isNotNull();
        verify(reportRepository).save(any(SysReport.class));
    }
}
