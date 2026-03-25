package com.nexterp.business.hrm.controller;

import com.nexterp.business.hrm.application.service.HrmOrgObjectService;
import com.nexterp.business.hrm.domain.model.HrmOrgObject;
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
 * OM 对象控制器
 * 对标 SAP HRP1000
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hrm/org-objects")
@RequiredArgsConstructor
public class HrmOrgObjectController {

    private final HrmOrgObjectService orgObjectService;

    /**
     * 创建 OM 对象
     */
    @PostMapping
    @PreAuthorize("hasAuthority('hrm:org:add')")
    public Result<Long> createOrgObject(@Valid @RequestBody HrmOrgObject orgObject) {
        Long id = orgObjectService.createOrgObject(orgObject);
        return Result.success(id);
    }

    /**
     * 更新 OM 对象
     */
    @PutMapping("/{pk}")
    @PreAuthorize("hasAuthority('hrm:org:edit')")
    public Result<HrmOrgObject> updateOrgObject(
            @PathVariable Long pk,
            @Valid @RequestBody HrmOrgObject orgObject) {
        HrmOrgObject updated = orgObjectService.updateOrgObject(pk, orgObject);
        return Result.success(updated);
    }

    /**
     * 删除 OM 对象
     */
    @DeleteMapping("/{pk}")
    @PreAuthorize("hasAuthority('hrm:org:delete')")
    public Result<Void> deleteOrgObject(@PathVariable Long pk) {
        orgObjectService.deleteOrgObject(pk);
        return Result.success();
    }

    /**
     * 获取 OM 对象详情
     */
    @GetMapping("/{pk}")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<HrmOrgObject> getByPk(@PathVariable Long pk) {
        HrmOrgObject orgObject = orgObjectService.getOrgObjectByPk(pk);
        return Result.success(orgObject);
    }

    /**
     * 根据对象类型和ID获取
     */
    @GetMapping("/by-type-id")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<HrmOrgObject> getByTypeAndId(
            @RequestParam String objectType,
            @RequestParam String objectId) {
        HrmOrgObject orgObject = orgObjectService.getByObjectTypeAndId(objectType, objectId);
        return Result.success(orgObject);
    }

    /**
     * 获取指定类型的所有对象
     */
    @GetMapping("/by-type/{objectType}")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOrgObject>> getByObjectType(
            @PathVariable String objectType,
            @RequestParam Long tenantId) {
        List<HrmOrgObject> objects = orgObjectService.getByObjectType(objectType, tenantId);
        return Result.success(objects);
    }

    /**
     * 获取指定日期有效的对象
     */
    @GetMapping("/valid-on-date")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOrgObject>> getValidOnDate(
            @RequestParam String objectType,
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        List<HrmOrgObject> objects = orgObjectService.getValidOnDate(objectType, tenantId, keyDate);
        return Result.success(objects);
    }

    /**
     * 获取指定日期活跃的对象
     */
    @GetMapping("/active-on-date")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOrgObject>> getActiveOnDate(
            @RequestParam String objectType,
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        List<HrmOrgObject> objects = orgObjectService.getActiveOnDate(objectType, tenantId, keyDate);
        return Result.success(objects);
    }

    /**
     * 获取对象历史记录
     */
    @GetMapping("/history")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOrgObject>> getHistory(
            @RequestParam String objectType,
            @RequestParam String objectId,
            @RequestParam Long tenantId) {
        List<HrmOrgObject> history = orgObjectService.getHistory(objectType, objectId, tenantId);
        return Result.success(history);
    }

    /**
     * 创建新版本 (时间片分割)
     */
    @PostMapping("/{pk}/new-version")
    @PreAuthorize("hasAuthority('hrm:org:edit')")
    public Result<HrmOrgObject> createNewVersion(
            @PathVariable Long pk,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom,
            @Valid @RequestBody HrmOrgObject newData) {
        HrmOrgObject newVersion = orgObjectService.createNewVersion(pk, validFrom, newData);
        return Result.success(newVersion);
    }

    /**
     * 限制定位到指定日期
     */
    @PutMapping("/{pk}/delimit")
    @PreAuthorize("hasAuthority('hrm:org:edit')")
    public Result<Void> delimit(
            @PathVariable Long pk,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validTo) {
        orgObjectService.delimit(pk, validTo);
        return Result.success();
    }

    /**
     * 分页查询
     */
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<PageResult<HrmOrgObject>> listOrgObjects(
            @RequestParam Long tenantId,
            @RequestParam String objectType,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<HrmOrgObject> page = orgObjectService.listOrgObjects(
                tenantId, objectType, PageRequest.of(current - 1, size));

        PageResult<HrmOrgObject> result = PageResult.<HrmOrgObject>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();

        return Result.success(result);
    }
}
