package com.nexterp.platform.report.controller;

import com.nexterp.platform.report.service.ReportExportService;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 报表控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportExportService reportExportService;

    /**
     * 导出报表为Excel
     *
     * @param reportCode 报表编码
     * @param tenantId   租户ID
     * @param params     查询参数
     * @return Excel文件
     */
    @PostMapping("/{reportCode}/export")
    @PreAuthorize("hasAuthority('system:report:export')")
    public ResponseEntity<byte[]> exportToExcel(
            @PathVariable String reportCode,
            @RequestParam Long tenantId,
            @RequestBody(required = false) Map<String, Object> params) {

        byte[] excelBytes = reportExportService.exportToExcel(reportCode, tenantId, params);

        // 生成文件名
        String filename = reportCode + "_" + System.currentTimeMillis() + ".xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }
}
