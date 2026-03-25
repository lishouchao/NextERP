package com.nexterp.business.hrm.controller;

import com.nexterp.business.hrm.application.service.HrmOmPositionDetailService;
import com.nexterp.business.hrm.domain.model.HrmOmPositionDetail;
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
import java.util.Map;

/**
 * 职位详情控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hrm/positions")
@RequiredArgsConstructor
public class HrmOmPositionDetailController {

    private final HrmOmPositionDetailService positionService;

    /**
     * 创建职位
     */
    @PostMapping
    @PreAuthorize("hasAuthority('hrm:position:add')")
    public Result<Long> createPosition(@Valid @RequestBody HrmOmPositionDetail detail) {
        Long id = positionService.createPositionDetail(detail);
        return Result.success(id);
    }

    /**
     * 更新职位
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:position:edit')")
    public Result<HrmOmPositionDetail> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody HrmOmPositionDetail detail) {
        HrmOmPositionDetail updated = positionService.updatePositionDetail(id, detail);
        return Result.success(updated);
    }

    /**
     * 删除职位
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:position:delete')")
    public Result<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePositionDetail(id);
        return Result.success();
    }

    /**
     * 获取职位详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<HrmOmPositionDetail> getById(@PathVariable Long id) {
        HrmOmPositionDetail detail = positionService.getById(id);
        return Result.success(detail);
    }

    /**
     * 根据 OM 对象内码获取
     */
    @GetMapping("/by-object-pk/{objectPk}")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<HrmOmPositionDetail> getByObjectPk(@PathVariable Long objectPk) {
        HrmOmPositionDetail detail = positionService.getByObjectPk(objectPk);
        return Result.success(detail);
    }

    /**
     * 根据职位编码获取
     */
    @GetMapping("/by-code/{positionCode}")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<HrmOmPositionDetail> getByPositionCode(
            @PathVariable String positionCode,
            @RequestParam Long tenantId) {
        HrmOmPositionDetail detail = positionService.getByPositionCode(positionCode, tenantId);
        return Result.success(detail);
    }

    /**
     * 获取指定日期有效的职位
     */
    @GetMapping("/valid-on-date")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<HrmOmPositionDetail> getValidOnDate(
            @RequestParam Long objectPk,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        HrmOmPositionDetail detail = positionService.getValidOnDate(objectPk, keyDate);
        return Result.success(detail);
    }

    /**
     * 根据组织查询职位
     */
    @GetMapping("/by-org/{orgObjectPk}")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<List<HrmOmPositionDetail>> getByOrgObjectPk(
            @PathVariable Long orgObjectPk,
            @RequestParam Long tenantId) {
        List<HrmOmPositionDetail> positions = positionService.getByOrgObjectPk(orgObjectPk, tenantId);
        return Result.success(positions);
    }

    /**
     * 根据职务查询职位
     */
    @GetMapping("/by-job/{jobObjectPk}")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<List<HrmOmPositionDetail>> getByJobObjectPk(
            @PathVariable Long jobObjectPk,
            @RequestParam Long tenantId) {
        List<HrmOmPositionDetail> positions = positionService.getByJobObjectPk(jobObjectPk, tenantId);
        return Result.success(positions);
    }

    /**
     * 根据职位状态查询
     */
    @GetMapping("/by-status/{positionStatus}")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<List<HrmOmPositionDetail>> getByPositionStatus(
            @PathVariable String positionStatus,
            @RequestParam Long tenantId) {
        List<HrmOmPositionDetail> positions = positionService.getByPositionStatus(positionStatus, tenantId);
        return Result.success(positions);
    }

    /**
     * 获取空缺职位
     */
    @GetMapping("/vacant")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<List<HrmOmPositionDetail>> getVacantPositions(
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        List<HrmOmPositionDetail> positions = positionService.getVacantPositions(tenantId, keyDate);
        return Result.success(positions);
    }

    /**
     * 获取有编制空缺的职位
     */
    @GetMapping("/with-vacancy")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<List<HrmOmPositionDetail>> getPositionsWithVacancy(
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        List<HrmOmPositionDetail> positions = positionService.getPositionsWitVacancy(tenantId, keyDate);
        return Result.success(positions);
    }

    /**
     * 获取关键岗位
     */
    @GetMapping("/key-positions")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<List<HrmOmPositionDetail>> getKeyPositions(
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        List<HrmOmPositionDetail> positions = positionService.getKeyPositions(tenantId, keyDate);
        return Result.success(positions);
    }

    /**
     * 获取经理岗位
     */
    @GetMapping("/manager-positions")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<List<HrmOmPositionDetail>> getManagerPositions(
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        List<HrmOmPositionDetail> positions = positionService.getManagerPositions(tenantId, keyDate);
        return Result.success(positions);
    }

    /**
     * 分配任职者
     */
    @PostMapping("/{objectPk}/assign-holder")
    @PreAuthorize("hasAuthority('hrm:position:edit')")
    public Result<Void> assignHolder(
            @PathVariable Long objectPk,
            @RequestParam Long holderObjectPk,
            @RequestParam String employeeNo,
            @RequestParam String holderName) {
        positionService.assignHolder(objectPk, holderObjectPk, employeeNo, holderName);
        return Result.success();
    }

    /**
     * 移除任职者
     */
    @DeleteMapping("/{objectPk}/remove-holder")
    @PreAuthorize("hasAuthority('hrm:position:edit')")
    public Result<Void> removeHolder(@PathVariable Long objectPk) {
        positionService.removeHolder(objectPk);
        return Result.success();
    }

    /**
     * 冻结职位
     */
    @PutMapping("/{objectPk}/freeze")
    @PreAuthorize("hasAuthority('hrm:position:edit')")
    public Result<Void> freezePosition(@PathVariable Long objectPk) {
        positionService.freezePosition(objectPk);
        return Result.success();
    }

    /**
     * 解冻职位
     */
    @PutMapping("/{objectPk}/unfreeze")
    @PreAuthorize("hasAuthority('hrm:position:edit')")
    public Result<Void> unfreezePosition(@PathVariable Long objectPk) {
        positionService.unfreezePosition(objectPk);
        return Result.success();
    }

    /**
     * 废除职位
     */
    @PutMapping("/{objectPk}/abolish")
    @PreAuthorize("hasAuthority('hrm:position:edit')")
    public Result<Void> abolishPosition(@PathVariable Long objectPk) {
        positionService.abolishPosition(objectPk);
        return Result.success();
    }

    /**
     * 按组织统计职位数
     */
    @GetMapping("/count-by-org")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<Map<Long, Long>> countByOrg(
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        Map<Long, Long> counts = positionService.countByOrg(tenantId, keyDate);
        return Result.success(counts);
    }

    /**
     * 按职务统计职位数
     */
    @GetMapping("/count-by-job")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<Map<Long, Long>> countByJob(
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        Map<Long, Long> counts = positionService.countByJob(tenantId, keyDate);
        return Result.success(counts);
    }

    /**
     * 分页查询
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('hrm:position:view')")
    public Result<PageResult<HrmOmPositionDetail>> listPositions(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<HrmOmPositionDetail> page = positionService.listPositionDetails(
                tenantId, PageRequest.of(current - 1, size));

        PageResult<HrmOmPositionDetail> result = PageResult.<HrmOmPositionDetail>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
