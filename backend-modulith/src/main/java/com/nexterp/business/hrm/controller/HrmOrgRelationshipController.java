package com.nexterp.business.hrm.controller;

import com.nexterp.business.hrm.application.service.HrmOrgRelationshipService;
import com.nexterp.business.hrm.domain.model.HrmOrgRelationship;
import com.nexterp.shared.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * OM 对象关系控制器
 * 对标 SAP HRP1001
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/hrm/org-relationships")
@RequiredArgsConstructor
public class HrmOrgRelationshipController {

    private final HrmOrgRelationshipService relationshipService;

    /**
     * 创建关系
     */
    @PostMapping
    @PreAuthorize("hasAuthority('hrm:org:add')")
    public Result<Long> createRelationship(@Valid @RequestBody HrmOrgRelationship relationship) {
        Long id = relationshipService.createRelationship(relationship);
        return Result.success(id);
    }

    /**
     * 更新关系
     */
    @PutMapping("/{pk}")
    @PreAuthorize("hasAuthority('hrm:org:edit')")
    public Result<HrmOrgRelationship> updateRelationship(
            @PathVariable Long pk,
            @Valid @RequestBody HrmOrgRelationship relationship) {
        HrmOrgRelationship updated = relationshipService.updateRelationship(pk, relationship);
        return Result.success(updated);
    }

    /**
     * 删除关系
     */
    @DeleteMapping("/{pk}")
    @PreAuthorize("hasAuthority('hrm:org:delete')")
    public Result<Void> deleteRelationship(@PathVariable Long pk) {
        relationshipService.deleteRelationship(pk);
        return Result.success();
    }

    /**
     * 获取关系详情
     */
    @GetMapping("/{pk}")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<HrmOrgRelationship> getByPk(@PathVariable Long pk) {
        HrmOrgRelationship relationship = relationshipService.getRelationshipByPk(pk);
        return Result.success(relationship);
    }

    /**
     * 获取对象的 A 端关系
     */
    @GetMapping("/by-object-a")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOrgRelationship>> getByObjectA(
            @RequestParam String objectTypeA,
            @RequestParam String objectIdA,
            @RequestParam Long tenantId) {
        List<HrmOrgRelationship> relationships = relationshipService
                .getRelationshipsByObjectA(objectTypeA, objectIdA, tenantId);
        return Result.success(relationships);
    }

    /**
     * 获取对象的 B 端关系
     */
    @GetMapping("/by-object-b")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOrgRelationship>> getByObjectB(
            @RequestParam String objectTypeB,
            @RequestParam String objectIdB,
            @RequestParam Long tenantId) {
        List<HrmOrgRelationship> relationships = relationshipService
                .getRelationshipsByObjectB(objectTypeB, objectIdB, tenantId);
        return Result.success(relationships);
    }

    /**
     * 获取指定日期有效的关系
     */
    @GetMapping("/valid-on-date")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOrgRelationship>> getValidOnDate(
            @RequestParam String objectTypeA,
            @RequestParam String objectIdA,
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        List<HrmOrgRelationship> relationships = relationshipService
                .getValidOnDate(objectTypeA, objectIdA, tenantId, keyDate);
        return Result.success(relationships);
    }

    // ==================== 组织架构专用接口 ====================

    /**
     * 获取组织的下级组织 (002 关系)
     */
    @GetMapping("/sub-orgs")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOrgRelationship>> getSubOrgUnits(
            @RequestParam String orgId,
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        List<HrmOrgRelationship> relationships = relationshipService
                .getSubOrgUnits(orgId, tenantId, keyDate);
        return Result.success(relationships);
    }

    /**
     * 获取组织的职位 (003 关系)
     */
    @GetMapping("/positions-by-org")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOrgRelationship>> getPositionsByOrg(
            @RequestParam String orgId,
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        List<HrmOrgRelationship> relationships = relationshipService
                .getPositionsByOrg(orgId, tenantId, keyDate);
        return Result.success(relationships);
    }

    /**
     * 获取职位的职务 (007 关系)
     */
    @GetMapping("/job-by-position")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<HrmOrgRelationship> getJobByPosition(
            @RequestParam String positionId,
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        Optional<HrmOrgRelationship> relationship = relationshipService
                .getJobByPosition(positionId, tenantId, keyDate);
        return Result.success(relationship.orElse(null));
    }

    /**
     * 获取职位的任职者 (008 关系)
     */
    @GetMapping("/holders-by-position")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOrgRelationship>> getHoldersByPosition(
            @RequestParam String positionId,
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        List<HrmOrgRelationship> relationships = relationshipService
                .getHoldersByPosition(positionId, tenantId, keyDate);
        return Result.success(relationships);
    }

    /**
     * 获取职位的主要任职者
     */
    @GetMapping("/primary-holder")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<HrmOrgRelationship> getPrimaryHolder(
            @RequestParam String positionId,
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        Optional<HrmOrgRelationship> relationship = relationshipService
                .getPrimaryHolder(positionId, tenantId, keyDate);
        return Result.success(relationship.orElse(null));
    }

    /**
     * 获取组织的负责人 (009 关系)
     */
    @GetMapping("/managers-by-org")
    @PreAuthorize("hasAuthority('hrm:org:view')")
    public Result<List<HrmOrgRelationship>> getManagersByOrg(
            @RequestParam String orgId,
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate keyDate) {
        List<HrmOrgRelationship> relationships = relationshipService
                .getManagersByOrg(orgId, tenantId, keyDate);
        return Result.success(relationships);
    }

    /**
     * 分配人员到职位
     */
    @PostMapping("/assign-holder")
    @PreAuthorize("hasAuthority('hrm:org:edit')")
    public Result<HrmOrgRelationship> assignHolder(
            @RequestParam String positionId,
            @RequestParam String personId,
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom) {
        HrmOrgRelationship relationship = relationshipService
                .assignHolder(positionId, personId, tenantId, validFrom);
        return Result.success(relationship);
    }

    /**
     * 解除人员职位分配
     */
    @DeleteMapping("/remove-holder")
    @PreAuthorize("hasAuthority('hrm:org:edit')")
    public Result<Void> removeHolder(
            @RequestParam String positionId,
            @RequestParam String personId,
            @RequestParam Long tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validTo) {
        relationshipService.removeHolder(positionId, personId, tenantId, validTo);
        return Result.success();
    }

    /**
     * 限制定位到指定日期
     */
    @PutMapping("/{pk}/delimit")
    @PreAuthorize("hasAuthority('hrm:org:edit')")
    public Result<Void> delimit(
            @PathVariable Long pk,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validTo) {
        relationshipService.delimit(pk, validTo);
        return Result.success();
    }
}
