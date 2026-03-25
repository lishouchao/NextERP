package com.nexterp.business.hrm.controller;

import com.nexterp.business.hrm.application.service.HrmOmOrgUnitDetailService;
import com.nexterp.business.hrm.domain.model.HrmOmOrgUnitDetail;
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
 * 组织单元详情控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hrm/org-units")
@RequiredArgsConstructor
public class HrmOmOrgUnitDetailController {

    private final HrmOmOrgUnitDetailService orgUnitService;

    /**
     * 创建组织单元
     */
    @PostMapping
    @PreAuthorize("hasAuthority('hrm:org:add')")
    public Result<Long> createOrgUnit(@Valid @RequestBody HrmOmOrgUnitDetail detail) {
        Long id = orgUnitService.createOrgUnitDetail(detail);
        return Result.success(id);
    }

    /**
     * 更新组织单元
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:org:edit')")
    public Result<HrmOmOrgUnitDetail> updateOrgUnit(
            @PathVariable Long id,
            @Valid @RequestBody HrmOmOrgUnitDetail detail) {
        HrmOmOrgUnitDetail updated = orgUnitService.updateOrgUnitDetail(id, detail);
        return Result.success(updated);
    }

    /**
     * 删除组织单元
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:org:delete')")
    public Result<Void> deleteOrgUnit(@PathVariable Long id) {
        orgUnitService.deleteOrgUnitDetail(id);
        return Result.success();
    }

    /**
     * 获取组织单元详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<HrmOmOrgUnitDetail> getById(@PathVariable Long id) {
        HrmOmOrgUnitDetail detail = orgUnitService.getById(id);
        return Result.success(detail);
    }

    /**
     * 根据 OM 对象内码获取
     */
    @GetMapping("/by-object-pk/{objectPk}")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<HrmOmOrgUnitDetail> getByObjectPk(@PathVariable Long objectPk) {
        HrmOmOrgUnitDetail detail = orgUnitService.getByObjectPk(objectPk);
        return Result.success(detail);
    }

    /**
     * 根据组织编码获取
     */
    @GetMapping("/by-code/{orgCode}")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<HrmOmOrgUnitDetail> getByOrgCode(
            @PathVariable String orgCode,
            @RequestParam Long tenantId) {
        HrmOmOrgUnitDetail detail = orgUnitService.getByOrgCode(orgCode, tenantId);
        return Result.success(detail);
    }

    /**
     * 获取子组织
     */
    @GetMapping("/{parentObjectPk}/children")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOmOrgUnitDetail>> getChildren(
            @PathVariable Long parentObjectPk,
            @RequestParam Long tenantId) {
        List<HrmOmOrgUnitDetail> children = orgUnitService.getChildren(parentObjectPk, tenantId);
        return Result.success(children);
    }

    /**
     * 获取根组织
     */
    @GetMapping("/roots")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOmOrgUnitDetail>> getRootOrgs(@RequestParam Long tenantId) {
        List<HrmOmOrgUnitDetail> roots = orgUnitService.getRootOrgs(tenantId);
        return Result.success(roots);
    }

    /**
     * 获取指定日期有效的组织
     */
    @GetMapping("/valid-on-date")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<HrmOmOrgUnitDetail> getValidOnDate(
            @RequestParam Long objectPk,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        HrmOmOrgUnitDetail detail = orgUnitService.getValidOnDate(objectPk, keyDate);
        return Result.success(detail);
    }

    /**
     * 根据公司代码查询
     */
    @GetMapping("/by-company/{companyCode}")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOmOrgUnitDetail>> getByCompanyCode(
            @PathVariable String companyCode,
            @RequestParam Long tenantId) {
        List<HrmOmOrgUnitDetail> orgs = orgUnitService.getByCompanyCode(companyCode, tenantId);
        return Result.success(orgs);
    }

    /**
     * 根据成本中心查询
     */
    @GetMapping("/by-cost-center/{costCenterCode}")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOmOrgUnitDetail>> getByCostCenterCode(
            @PathVariable String costCenterCode,
            @RequestParam Long tenantId) {
        List<HrmOmOrgUnitDetail> orgs = orgUnitService.getByCostCenterCode(costCenterCode, tenantId);
        return Result.success(orgs);
    }

    /**
     * 查询超编组织
     */
    @GetMapping("/overstaffed")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOmOrgUnitDetail>> getOverstaffed(@RequestParam Long tenantId) {
        List<HrmOmOrgUnitDetail> orgs = orgUnitService.getOverstaffed(tenantId);
        return Result.success(orgs);
    }

    /**
     * 更新人数
     */
    @PutMapping("/{objectPk}/headcount")
    @PreAuthorize("hasAuthority('hrm:org:edit')")
    public Result<Void> updateHeadcount(
            @PathVariable Long objectPk,
            @RequestParam Integer headcount) {
        orgUnitService.updateHeadcount(objectPk, headcount);
        return Result.success();
    }

    /**
     * 增加人数
     */
    @PostMapping("/{objectPk}/increment-headcount")
    @PreAuthorize("hasAuthority('hrm:org:edit')")
    public Result<Void> incrementHeadcount(@PathVariable Long objectPk) {
        orgUnitService.incrementHeadcount(objectPk);
        return Result.success();
    }

    /**
     * 减少人数
     */
    @PostMapping("/{objectPk}/decrement-headcount")
    @PreAuthorize("hasAuthority('hrm:org:edit')")
    public Result<Void> decrementHeadcount(@PathVariable Long objectPk) {
        orgUnitService.decrementHeadcount(objectPk);
        return Result.success();
    }

    /**
     * 分页查询
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<PageResult<HrmOmOrgUnitDetail>> listOrgUnits(
            @RequestParam Long tenantId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<HrmOmOrgUnitDetail> page = orgUnitService.listOrgUnits(
                tenantId, PageRequest.of(current - 1, size));

        PageResult<HrmOmOrgUnitDetail> result = PageResult.<HrmOmOrgUnitDetail>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
