package com.nexterp.business.hrm.controller;

import com.nexterp.business.hrm.application.service.HrmOmJobDetailService;
import com.nexterp.business.hrm.domain.model.HrmOmJobDetail;
import com.nexterp.shared.core.result.PageResult;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 职务详情控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hrm/jobs")
@RequiredArgsConstructor
public class HrmOmJobDetailController {

    private final HrmOmJobDetailService jobService;

    /**
     * 创建职务
     */
    @PostMapping
    @PreAuthorize("hasAuthority('hrm:job:add')")
    public Result<Long> createJob(@Valid @RequestBody HrmOmJobDetail detail) {
        Long id = jobService.createJobDetail(detail);
        return Result.success(id);
    }

    /**
     * 更新职务
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:job:edit')")
    public Result<HrmOmJobDetail> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody HrmOmJobDetail detail) {
        HrmOmJobDetail updated = jobService.updateJobDetail(id, detail);
        return Result.success(updated);
    }

    /**
     * 删除职务
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:job:delete')")
    public Result<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJobDetail(id);
        return Result.success();
    }

    /**
     * 获取职务详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:job:view')")
    public Result<HrmOmJobDetail> getById(@PathVariable Long id) {
        HrmOmJobDetail detail = jobService.getById(id);
        return Result.success(detail);
    }

    /**
     * 根据 OM 对象内码获取
     */
    @GetMapping("/by-object-pk/{objectPk}")
    @PreAuthorize("hasAuthority('hrm:job:view')")
    public Result<HrmOmJobDetail> getByObjectPk(@PathVariable Long objectPk) {
        HrmOmJobDetail detail = jobService.getByObjectPk(objectPk);
        return Result.success(detail);
    }

    /**
     * 根据职务编码获取
     */
    @GetMapping("/by-code/{jobCode}")
    @PreAuthorize("hasAuthority('hrm:job:view')")
    public Result<HrmOmJobDetail> getByJobCode(
            @PathVariable String jobCode,
            @RequestParam Long tenantId) {
        HrmOmJobDetail detail = jobService.getByJobCode(jobCode, tenantId);
        return Result.success(detail);
    }

    /**
     * 获取指定日期有效的职务
     */
    @GetMapping("/valid-on-date")
    @PreAuthorize("hasAuthority('hrm:job:view')")
    public Result<HrmOmJobDetail> getValidOnDate(
            @RequestParam Long objectPk,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        HrmOmJobDetail detail = jobService.getValidOnDate(objectPk, keyDate);
        return Result.success(detail);
    }

    /**
     * 根据职务族查询
     */
    @GetMapping("/by-family/{jobFamilyId}")
    @PreAuthorize("hasAuthority('hrm:job:view')")
    public Result<List<HrmOmJobDetail>> getByJobFamilyId(
            @PathVariable Long jobFamilyId,
            @RequestParam Long tenantId) {
        List<HrmOmJobDetail> jobs = jobService.getByJobFamilyId(jobFamilyId, tenantId);
        return Result.success(jobs);
    }

    /**
     * 根据职能分类查询
     */
    @GetMapping("/by-function/{jobFunction}")
    @PreAuthorize("hasAuthority('hrm:job:view')")
    public Result<List<HrmOmJobDetail>> getByJobFunction(
            @PathVariable String jobFunction,
            @RequestParam Long tenantId) {
        List<HrmOmJobDetail> jobs = jobService.getByJobFunction(jobFunction, tenantId);
        return Result.success(jobs);
    }

    /**
     * 根据职级查询
     */
    @GetMapping("/by-grade/{grade}")
    @PreAuthorize("hasAuthority('hrm:job:view')")
    public Result<List<HrmOmJobDetail>> getByGrade(
            @PathVariable String grade,
            @RequestParam Long tenantId) {
        List<HrmOmJobDetail> jobs = jobService.getByGrade(grade, tenantId);
        return Result.success(jobs);
    }

    /**
     * 根据职等查询
     */
    @GetMapping("/by-level/{level}")
    @PreAuthorize("hasAuthority('hrm:job:view')")
    public Result<List<HrmOmJobDetail>> getByLevel(
            @PathVariable Integer level,
            @RequestParam Long tenantId) {
        List<HrmOmJobDetail> jobs = jobService.getByLevel(level, tenantId);
        return Result.success(jobs);
    }

    /**
     * 获取根职务
     */
    @GetMapping("/roots")
    @PreAuthorize("hasAuthority('hrm:job:view')")
    public Result<List<HrmOmJobDetail>> getRootJobs(@RequestParam Long tenantId) {
        List<HrmOmJobDetail> jobs = jobService.getRootJobs(tenantId);
        return Result.success(jobs);
    }

    /**
     * 更新关联职位数
     */
    @PutMapping("/{objectPk}/position-count")
    @PreAuthorize("hasAuthority('hrm:job:edit')")
    public Result<Void> updatePositionCount(
            @PathVariable Long objectPk,
            @RequestParam Integer count) {
        jobService.updatePositionCount(objectPk, count);
        return Result.success();
    }

    /**
     * 增加职位数
     */
    @PostMapping("/{objectPk}/increment-position-count")
    @PreAuthorize("hasAuthority('hrm:job:edit')")
    public Result<Void> incrementPositionCount(@PathVariable Long objectPk) {
        jobService.incrementPositionCount(objectPk);
        return Result.success();
    }

    /**
     * 减少职位数
     */
    @PostMapping("/{objectPk}/decrement-position-count")
    @PreAuthorize("hasAuthority('hrm:job:edit')")
    public Result<Void> decrementPositionCount(@PathVariable Long objectPk) {
        jobService.decrementPositionCount(objectPk);
        return Result.success();
    }

    /**
     * 分页查询
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('hrm:job:view')")
    public Result<PageResult<HrmOmJobDetail>> listJobs(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<HrmOmJobDetail> page = jobService.listJobDetails(
                tenantId, PageRequest.of(current - 1, size));

        PageResult<HrmOmJobDetail> result = PageResult.<HrmOmJobDetail>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
