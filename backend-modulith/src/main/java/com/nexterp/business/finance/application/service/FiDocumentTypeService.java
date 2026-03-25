package com.nexterp.business.finance.application.service;

import com.nexterp.business.finance.domain.model.FiDocumentType;
import com.nexterp.business.finance.domain.repository.FiDocumentTypeRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 凭证类型服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FiDocumentTypeService {

    private final FiDocumentTypeRepository documentTypeRepository;

    /**
     * 创建凭证类型
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createDocumentType(FiDocumentType documentType) {
        // 检查代码是否已存在
        if (documentTypeRepository.existsByDocTypeCodeAndTenantIdAndIsDeletedFalse(
                documentType.getDocTypeCode(), documentType.getTenantId())) {
            throw new BusinessException("凭证类型代码已存在: " + documentType.getDocTypeCode());
        }

        // 设置默认值
        if (documentType.getStatus() == null) {
            documentType.setStatus(1);
        }

        FiDocumentType saved = documentTypeRepository.save(documentType);
        log.info("创建凭证类型成功: docTypeCode={}, name={}", saved.getDocTypeCode(), saved.getDocTypeName());
        return saved.getId();
    }

    /**
     * 更新凭证类型
     */
    @Transactional(rollbackFor = Exception.class)
    public FiDocumentType updateDocumentType(Long id, FiDocumentType documentType) {
        FiDocumentType existing = documentTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("凭证类型不存在"));

        existing.setDocTypeName(documentType.getDocTypeName());
        existing.setDocTypeNameEn(documentType.getDocTypeNameEn());
        existing.setVoucherWord(documentType.getVoucherWord());
        existing.setRemark(documentType.getRemark());

        return documentTypeRepository.save(existing);
    }

    /**
     * 删除凭证类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocumentType(Long id) {
        FiDocumentType documentType = documentTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("凭证类型不存在"));

        documentType.setIsDeleted(true);
        documentTypeRepository.save(documentType);
        log.info("删除凭证类型成功: id={}", id);
    }

    /**
     * 获取凭证类型详情
     */
    public FiDocumentType getDocumentTypeById(Long id) {
        return documentTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("凭证类型不存在"));
    }

    /**
     * 根据代码获取
     */
    public FiDocumentType getDocumentTypeByCode(String docTypeCode, Long tenantId) {
        return documentTypeRepository.findByDocTypeCodeAndTenantIdAndIsDeletedFalse(docTypeCode, tenantId)
                .orElseThrow(() -> new BusinessException("凭证类型不存在: " + docTypeCode));
    }

    /**
     * 获取所有启用的凭证类型
     */
    public List<FiDocumentType> listEnabledDocumentTypes(Long tenantId) {
        return documentTypeRepository.findByStatusAndTenantIdAndIsDeletedFalseOrderBySortOrder(1, tenantId);
    }

    /**
     * 根据分类查询
     */
    public List<FiDocumentType> listByClass(String docTypeClass, Long tenantId) {
        return documentTypeRepository.findByDocTypeClassAndTenantIdAndIsDeletedFalseOrderBySortOrder(docTypeClass, tenantId);
    }

    /**
     * 分页查询
     */
    public Page<FiDocumentType> listDocumentTypes(Long tenantId, Pageable pageable) {
        return documentTypeRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }
}
