package com.nexterp.platform.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.platform.report.domain.model.SysReport;
import com.nexterp.platform.report.domain.repository.SysReportRepository;
import com.nexterp.platform.report.dto.request.ReportCreateRequest;
import com.nexterp.platform.report.dto.request.ReportQueryRequest;
import com.nexterp.platform.report.dto.response.ReportResponse;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 报表服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final SysReportRepository reportRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 创建报表
     *
     * @param request 创建请求
     * @return 报表响应
     */
    @Transactional(rollbackFor = Exception.class)
    public ReportResponse createReport(ReportCreateRequest request) {
        // 检查报表编码是否存在
        if (reportRepository.existsByReportCode(request.getReportCode(), request.getTenantId())) {
            throw new BusinessException("报表编码已存在");
        }

        SysReport report = SysReport.builder()
                .tenantId(request.getTenantId())
                .reportCode(request.getReportCode())
                .reportName(request.getReportName())
                .reportType(request.getReportType())
                .datasourceType(request.getDatasourceType() != null ? request.getDatasourceType() : "sql")
                .datasourceConfig(convertMapToJson(request.getDatasourceConfig()))
                .reportConfig(convertMapToJson(request.getReportConfig()))
                .reportGroup(request.getReportGroup())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .status(1)
                .remark(request.getRemark())
                .build();

        SysReport saved = reportRepository.save(report);
        log.info("创建报表成功: reportCode={}", request.getReportCode());
        return toResponse(saved);
    }

    /**
     * 更新报表
     *
     * @param id 报表ID
     * @param request 更新请求
     * @return 报表响应
     */
    @Transactional(rollbackFor = Exception.class)
    public ReportResponse updateReport(Long id, ReportCreateRequest request) {
        SysReport report = reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报表不存在"));

        // 检查报表编码是否被其他报表使用
        if (!report.getReportCode().equals(request.getReportCode()) &&
            reportRepository.existsByReportCode(request.getReportCode(), request.getTenantId())) {
            throw new BusinessException("报表编码已被使用");
        }

        report.setReportCode(request.getReportCode());
        report.setReportName(request.getReportName());
        report.setReportType(request.getReportType());
        report.setDatasourceType(request.getDatasourceType());
        report.setDatasourceConfig(convertMapToJson(request.getDatasourceConfig()));
        report.setReportConfig(convertMapToJson(request.getReportConfig()));
        report.setReportGroup(request.getReportGroup());
        report.setSortOrder(request.getSortOrder());
        report.setRemark(request.getRemark());

        SysReport updated = reportRepository.save(report);
        log.info("更新报表成功: id={}", id);
        return toResponse(updated);
    }

    /**
     * 删除报表
     *
     * @param id 报表ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteReport(Long id) {
        SysReport report = reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报表不存在"));

        report.setIsDeleted(true);
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);

        log.info("删除报表成功: id={}", id);
    }

    /**
     * 获取报表详情
     *
     * @param id 报表ID
     * @return 报表响应
     */
    public ReportResponse getReportById(Long id) {
        SysReport report = reportRepository.findById(id)
                .orElseThrow(() -> new BusinessException("报表不存在"));
        return toResponse(report);
    }

    /**
     * 分页查询报表
     *
     * @param request 查询请求
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    public PageResult<ReportResponse> listReports(ReportQueryRequest request, Integer current, Integer size) {
        Specification<SysReport> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getTenantId() != null) {
                predicates.add(cb.equal(root.get("tenantId"), request.getTenantId()));
            }

            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (request.getReportCode() != null && !request.getReportCode().isEmpty()) {
                predicates.add(cb.like(root.get("reportCode"), "%" + request.getReportCode() + "%"));
            }

            if (request.getReportName() != null && !request.getReportName().isEmpty()) {
                predicates.add(cb.like(root.get("reportName"), "%" + request.getReportName() + "%"));
            }

            if (request.getReportType() != null && !request.getReportType().isEmpty()) {
                predicates.add(cb.equal(root.get("reportType"), request.getReportType()));
            }

            if (request.getReportGroup() != null && !request.getReportGroup().isEmpty()) {
                predicates.add(cb.equal(root.get("reportGroup"), request.getReportGroup()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder", "createdAt");
        Pageable pageable = PageRequest.of(current - 1, size, sort);
        Page<SysReport> page = reportRepository.findAll(spec, pageable);

        return PageResult.<ReportResponse>builder()
                .records(page.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()))
                .total(page.getTotalElements())
                .current(current)
                .size(size)
                .build();
    }

    /**
     * 查询报表数据
     *
     * @param reportCode 报表编码
     * @param tenantId 租户ID
     * @param params 查询参数
     * @return 数据列表
     */
    public List<Map<String, Object>> queryReportData(String reportCode, Long tenantId, Map<String, Object> params) {
        SysReport report = reportRepository.findByReportCode(reportCode, tenantId)
                .orElseThrow(() -> new BusinessException("报表不存在"));

        if ("sql".equals(report.getDatasourceType())) {
            return executeSqlQuery(report, params);
        } else if ("api".equals(report.getDatasourceType())) {
            // TODO: 实现API数据源查询
            throw new BusinessException("暂不支持API数据源");
        } else {
            throw new BusinessException("不支持的数据源类型");
        }
    }

    /**
     * 执行SQL查询
     *
     * @param report 报表
     * @param params 查询参数
     * @return 查询结果
     */
    private List<Map<String, Object>> executeSqlQuery(SysReport report, Map<String, Object> params) {
        try {
            Map<String, Object> configMap = objectMapper.readValue(report.getDatasourceConfig(), Map.class);
            String sql = (String) configMap.get("sql");

            // 参数替换
            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String placeholder = "${" + entry.getKey() + "}";
                    if (sql.contains(placeholder)) {
                        sql = sql.replace(placeholder, String.valueOf(entry.getValue()));
                    }
                }
            }

            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            log.error("执行SQL查询失败", e);
            throw new BusinessException("查询报表数据失败: " + e.getMessage());
        }
    }

    /**
     * 转换Map为JSON字符串
     *
     * @param map Map对象
     * @return JSON字符串
     */
    private String convertMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("转换Map为JSON失败", e);
            return null;
        }
    }

    /**
     * 转换为响应对象
     *
     * @param report 报表实体
     * @return 报表响应
     */
    private ReportResponse toResponse(SysReport report) {
        return ReportResponse.builder()
                .id(report.getId())
                .tenantId(report.getTenantId())
                .reportCode(report.getReportCode())
                .reportName(report.getReportName())
                .reportType(report.getReportType())
                .datasourceType(report.getDatasourceType())
                .datasourceConfig(parseJsonToMap(report.getDatasourceConfig()))
                .reportConfig(parseJsonToMap(report.getReportConfig()))
                .reportGroup(report.getReportGroup())
                .sortOrder(report.getSortOrder())
                .status(report.getStatus())
                .remark(report.getRemark())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    /**
     * 转换JSON字符串为Map
     *
     * @param json JSON字符串
     * @return Map对象
     */
    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("转换JSON为Map失败", e);
            return new HashMap<>();
        }
    }
}
