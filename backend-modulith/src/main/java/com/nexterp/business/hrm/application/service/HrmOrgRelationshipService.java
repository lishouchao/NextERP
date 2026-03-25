package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrmOrgRelationship;
import com.nexterp.business.hrm.domain.repository.HrmOrgRelationshipRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * OM 对象关系服务
 * 对标 SAP HRP1001
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrmOrgRelationshipService {

    private final HrmOrgRelationshipRepository relationshipRepository;

    /**
     * 创建关系
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRelationship(HrmOrgRelationship relationship) {
        // 检查关系是否已存在
        Optional<HrmOrgRelationship> existing = relationshipRepository
                .findByObjectTypeAAndObjectIdAAndObjectTypeBAndObjectIdBAndRelationTypeAndIsDeletedFalse(
                        relationship.getObjectTypeA(), relationship.getObjectIdA(),
                        relationship.getObjectTypeB(), relationship.getObjectIdB(),
                        relationship.getRelationType());

        if (existing.isPresent()) {
            throw new BusinessException("关系已存在: " + relationship.getRelationType());
        }

        // 设置默认值
        if (relationship.getValidFrom() == null) {
            relationship.setValidFrom(LocalDate.now());
        }
        if (relationship.getValidTo() == null) {
            relationship.setValidTo(LocalDate.of(9999, 12, 31));
        }
        if (relationship.getIsPrimary() == null) {
            relationship.setIsPrimary(false);
        }

        HrmOrgRelationship saved = relationshipRepository.save(relationship);
        log.info("创建关系成功: type={}, A={}{}, B={}{]",
                saved.getRelationType(),
                saved.getObjectTypeA(), saved.getObjectIdA(),
                saved.getObjectTypeB(), saved.getObjectIdB());
        return saved.getId();
    }

    /**
     * 更新关系
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmOrgRelationship updateRelationship(Long pk, HrmOrgRelationship relationship) {
        HrmOrgRelationship existing = relationshipRepository.findById(pk)
                .orElseThrow(() -> new BusinessException("关系不存在"));

        existing.setPercentage(relationship.getPercentage());
        existing.setPriority(relationship.getPriority());
        existing.setIsPrimary(relationship.getIsPrimary());

        return relationshipRepository.save(existing);
    }

    /**
     * 删除关系
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelationship(Long pk) {
        HrmOrgRelationship relationship = relationshipRepository.findById(pk)
                .orElseThrow(() -> new BusinessException("关系不存在"));

        relationship.setIsDeleted(true);
        relationshipRepository.save(relationship);
        log.info("删除关系成功: pk={}", pk);
    }

    /**
     * 获取关系详情
     */
    public HrmOrgRelationship getRelationshipByPk(Long pk) {
        return relationshipRepository.findById(pk)
                .orElseThrow(() -> new BusinessException("关系不存在"));
    }

    /**
     * 获取对象的 A 端关系
     */
    public List<HrmOrgRelationship> getRelationshipsByObjectA(String objectTypeA, String objectIdA, Long tenantId) {
        return relationshipRepository.findByObjectTypeAAndObjectIdAAndTenantIdAndIsDeletedFalse(
                objectTypeA, objectIdA, tenantId);
    }

    /**
     * 获取对象的 B 端关系
     */
    public List<HrmOrgRelationship> getRelationshipsByObjectB(String objectTypeB, String objectIdB, Long tenantId) {
        return relationshipRepository.findByObjectTypeBAndObjectIdBAndTenantIdAndIsDeletedFalse(
                objectTypeB, objectIdB, tenantId);
    }

    /**
     * 获取指定日期有效的关系
     */
    public List<HrmOrgRelationship> getValidOnDate(String objectTypeA, String objectIdA, Long tenantId, LocalDate keyDate) {
        return relationshipRepository.findValidOnDate(objectTypeA, objectIdA, tenantId, keyDate);
    }

    // ==================== 组织架构专用方法 ====================

    /**
     * 获取组织的下级组织 (002 关系)
     */
    public List<HrmOrgRelationship> getSubOrgUnits(String orgId, Long tenantId, LocalDate keyDate) {
        return relationshipRepository.findSubOrgUnits(orgId, tenantId, keyDate);
    }

    /**
     * 获取组织的职位 (003 关系)
     */
    public List<HrmOrgRelationship> getPositionsByOrg(String orgId, Long tenantId, LocalDate keyDate) {
        return relationshipRepository.findPositionsByOrg(orgId, tenantId, keyDate);
    }

    /**
     * 获取职位的职务 (007 关系)
     */
    public Optional<HrmOrgRelationship> getJobByPosition(String positionId, Long tenantId, LocalDate keyDate) {
        return relationshipRepository.findJobByPosition(positionId, tenantId, keyDate);
    }

    /**
     * 获取职位的任职者 (008 关系)
     */
    public List<HrmOrgRelationship> getHoldersByPosition(String positionId, Long tenantId, LocalDate keyDate) {
        return relationshipRepository.findHoldersByPosition(positionId, tenantId, keyDate);
    }

    /**
     * 获取职位的主要任职者 (009 关系，isPrimary=true)
     */
    public Optional<HrmOrgRelationship> getPrimaryHolder(String positionId, Long tenantId, LocalDate keyDate) {
        return relationshipRepository.findPrimaryHolder(positionId, tenantId, keyDate);
    }

    /**
     * 获取组织的负责人 (009 关系)
     */
    public List<HrmOrgRelationship> getManagersByOrg(String orgId, Long tenantId, LocalDate keyDate) {
        return relationshipRepository.findManagersByOrg(orgId, tenantId, keyDate);
    }

    /**
     * 分配人员到职位
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmOrgRelationship assignHolder(String positionId, String personId,
                                           Long tenantId, LocalDate validFrom) {
        // 检查是否已有主要任职者
        Optional<HrmOrgRelationship> current = relationshipRepository
                .findPrimaryHolder(positionId, tenantId, validFrom);

        if (current.isPresent()) {
            throw new BusinessException("职位已有任职者: " + positionId);
        }

        HrmOrgRelationship relationship = HrmOrgRelationship.builder()
                .relationType("008")
                .objectTypeA("S")
                .objectIdA(positionId)
                .objectTypeB("P")
                .objectIdB(personId)
                .isPrimary(true)
                .validFrom(validFrom)
                .validTo(LocalDate.of(9999, 12, 31))
                .tenantId(tenantId)
                .build();

        return relationshipRepository.save(relationship);
    }

    /**
     * 解除人员职位分配
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeHolder(String positionId, String personId, Long tenantId, LocalDate validTo) {
        Optional<HrmOrgRelationship> relationship = relationshipRepository
                .findByObjectTypeAAndObjectIdAAndObjectTypeBAndObjectIdBAndRelationTypeAndIsDeletedFalse(
                        "S", positionId, "P", personId, "008");

        if (relationship.isPresent()) {
            relationship.get().setValidTo(validTo);
            relationshipRepository.save(relationship.get());
            log.info("解除职位分配: position={}, person={}", positionId, personId);
        }
    }

    /**
     * 限制定位到指定日期
     */
    @Transactional(rollbackFor = Exception.class)
    public void delimit(Long pk, LocalDate validTo) {
        HrmOrgRelationship existing = relationshipRepository.findById(pk)
                .orElseThrow(() -> new BusinessException("关系不存在"));

        if (validTo.isBefore(existing.getValidFrom())) {
            throw new BusinessException("截止日期不能早于生效日期");
        }

        existing.setValidTo(validTo);
        relationshipRepository.save(existing);
        log.info("限制关系有效期: pk={}, validTo={}", pk, validTo);
    }
}
