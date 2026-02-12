package com.nexterp.platform.report.controller;

import com.nexterp.platform.report.dto.request.ReportCreateRequest;
import com.nexterp.platform.report.dto.request.ReportQueryRequest;
import com.nexterp.platform.report.dto.response.ReportResponse;
import com.nexterp.platform.report.service.ReportService;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 报表管理控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportManagementController {

    private final ReportService reportService;

    /**
     * 创建报表
     *
     * @param request 创建请求
     * @return 报表响应
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:report:add')")
    public Result<ReportResponse> createReport(@Valid @RequestBody ReportCreateRequest request) {
        ReportResponse response = reportService.createReport(request);
        return Result.success(response);
    }

    /**
     * 更新报表
     *
     * @param id 报表ID
     * @param request 更新请求
     * @return 报表响应
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:report:edit')")
    public Result<ReportResponse> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportCreateRequest request) {
        ReportResponse response = reportService.updateReport(id, request);
        return Result.success(response);
    }

    /**
     * 删除报表
     *
     * @param id 报表ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:report:delete')")
    public Result<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return Result.success();
    }

    /**
     * 获取报表详情
     *
     * @param id 报表ID
     * @return 报表响应
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:report:view')")
    public Result<ReportResponse> getReportById(@PathVariable Long id) {
        ReportResponse response = reportService.getReportById(id);
        return Result.success(response);
    }

    /**
     * 分页查询报表
     *
     * @param request 查询请求
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('system:report:view')")
    public Result<PageResult<ReportResponse>> listReports(
            @RequestBody ReportQueryRequest request,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<ReportResponse> response = reportService.listReports(request, current, size);
        return Result.success(response);
    }

    /**
     * 查询报表数据
     *
     * @param reportCode 报表编码
     * @param tenantId 租户ID
     * @param params 查询参数
     * @return 数据列表
     */
    @PostMapping("/{reportCode}/data")
    @PreAuthorize("hasAuthority('system:report:query')")
    public Result<List<Map<String, Object>>> queryReportData(
            @PathVariable String reportCode,
            @RequestParam Long tenantId,
            @RequestBody(required = false) Map<String, Object> params) {
        List<Map<String, Object>> data = reportService.queryReportData(reportCode, tenantId, params);
        return Result.success(data);
    }
}
