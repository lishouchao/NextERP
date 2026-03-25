package com.nexterp.business.hrm.application.service;

import com.nexterp.business.hrm.domain.model.HrmOrgObject;
import com.nexterp.business.hrm.domain.repository.HrmOrgObjectRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * OM 对象服务
 * 对标 SAP HRP1000
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrmOrgObjectService {

    private final HrmOrgObjectRepository orgObjectRepository;

    /**
     * 创建 OM 对象
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrgObject(HrmOrgObject orgObject) {
        // 检查对象ID是否已存在
        if (orgObjectRepository.findByObjectTypeAndObjectIdAndIsDeletedFalse(
                orgObject.getObjectType(), orgObject.getObjectId()).isPresent()) {
            throw new BusinessException("对象ID已存在: " + orgObject.getObjectType() + "-" + orgObject.getObjectId());
        }

        // 设置默认值
        if (orgObject.getValidFrom() == null) {
            orgObject.setValidFrom(LocalDate.now());
        }
        if (orgObject.getValidTo() == null) {
            orgObject.setValidTo(LocalDate.of(9999, 12, 31));
        }
        if (orgObject.getObjStatus() == null) {
            orgObject.setObjStatus("ACTIVE");
        }

        HrmOrgObject saved = orgObjectRepository.save(orgObject);
        log.info("创建OM对象成功: type={}, id={}", saved.getObjectType(), saved.getObjectId());
        return saved.getId();
    }

    /**
     * 更新 OM 对象
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmOrgObject updateOrgObject(Long pk, HrmOrgObject orgObject) {
        HrmOrgObject existing = orgObjectRepository.findById(pk)
                .orElseThrow(() -> new BusinessException("OM对象不存在"));

        existing.setShortText(orgObject.getShortText());
        existing.setLongText(orgObject.getLongText());
        existing.setObjStatus(orgObject.getObjStatus());
        existing.setSortOrder(orgObject.getSortOrder());

        return orgObjectRepository.save(existing);
    }

    /**
     * 删除 OM 对象 (软删除)
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrgObject(Long pk) {
        HrmOrgObject orgObject = orgObjectRepository.findById(pk)
                .orElseThrow(() -> new BusinessException("OM对象不存在"));

        orgObject.setIsDeleted(true);
        orgObjectRepository.save(orgObject);
        log.info("删除OM对象成功: pk={}", pk);
    }

    /**
     * 获取 OM 对象详情
     */
    public HrmOrgObject getOrgObjectByPk(Long pk) {
        return orgObjectRepository.findById(pk)
                .orElseThrow(() -> new BusinessException("OM对象不存在"));
    }

    /**
     * 根据对象类型和ID获取
     */
    public HrmOrgObject getByObjectTypeAndId(String objectType, String objectId) {
        return orgObjectRepository.findByObjectTypeAndObjectIdAndIsDeletedFalse(objectType, objectId)
                .orElseThrow(() -> new BusinessException("OM对象不存在: " + objectType + "-" + objectId));
    }

    /**
     * 获取指定类型的所有对象
     */
    public List<HrmOrgObject> getByObjectType(String objectType, Long tenantId) {
        return orgObjectRepository.findByObjectTypeAndTenantIdAndIsDeletedFalse(objectType, tenantId);
    }

    /**
     * 获取指定日期有效的对象
     */
    public List<HrmOrgObject> getValidOnDate(String objectType, Long tenantId, LocalDate keyDate) {
        return orgObjectRepository.findValidOnDate(objectType, tenantId, keyDate);
    }

    /**
     * 获取指定日期活跃的对象
     */
    public List<HrmOrgObject> getActiveOnDate(String objectType, Long tenantId, LocalDate keyDate) {
        return orgObjectRepository.findActiveOnDate(objectType, tenantId, keyDate);
    }

    /**
     * 获取对象历史记录
     */
    public List<HrmOrgObject> getHistory(String objectType, String objectId, Long tenantId) {
        return orgObjectRepository.findHistoryByObjectId(objectType, objectId, tenantId);
    }

    /**
     * 分页查询
     */
    public Page<HrmOrgObject> listOrgObjects(Long tenantId, String objectType, Pageable pageable) {
        return orgObjectRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("objectType"), objectType),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }

    /**
     * 时间片分割 - 创建新版本
     */
    @Transactional(rollbackFor = Exception.class)
    public HrmOrgObject createNewVersion(Long pk, LocalDate validFrom, HrmOrgObject newData) {
        HrmOrgObject existing = orgObjectRepository.findById(pk)
                .orElseThrow(() -> new BusinessException("OM对象不存在"));

        // 将现有记录的有效期截止到新版本开始前一天
        existing.setValidTo(validFrom.minusDays(1));
        orgObjectRepository.save(existing);

        // 创建新版本
        newData.setValidFrom(validFrom);
        newData.setValidTo(LocalDate.of(9999, 12, 31));
        newData.setObjectType(existing.getObjectType());
        newData.setObjectId(existing.getObjectId());

        return orgObjectRepository.save(newData);
    }

    /**
     * 限制定位到指定日期
     */
    @Transactional(rollbackFor = Exception.class)
    public void delimit(Long pk, LocalDate validTo) {
        HrmOrgObject existing = orgObjectRepository.findById(pk)
                .orElseThrow(() -> new BusinessException("OM对象不存在"));

        if (validTo.isBefore(existing.getValidFrom())) {
            throw new BusinessException("截止日期不能早于生效日期");
        }

        existing.setValidTo(validTo);
        orgObjectRepository.save(existing);
        log.info("限制OM对象有效期: pk={}, validTo={}", pk, validTo);
    }
}
