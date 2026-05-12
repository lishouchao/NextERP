package com.nexterp.business.production.application.service;

import com.nexterp.business.production.domain.model.ProBom;
import com.nexterp.business.production.domain.model.ProBomDetail;
import com.nexterp.business.production.domain.repository.ProBomRepository;
import com.nexterp.business.production.dto.CreateBomRequest;
import com.nexterp.business.production.dto.CreateBomRequest.BomDetailRequest;
import com.nexterp.business.production.dto.ProBomDTO;
import com.nexterp.business.production.dto.ProBomDetailDTO;
import com.nexterp.business.production.event.BomCreatedEvent;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物料清单(BOM)服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProBomService {

    private final ProBomRepository bomRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建BOM
     *
     * @param request 创建请求
     * @return BOM ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createBom(CreateBomRequest request) {
        // 检查BOM编码是否已存在
        if (bomRepository.existsByBomCodeAndTenantIdAndIsDeletedFalse(request.getBomCode(), request.getTenantId())) {
            throw new BusinessException("BOM编码已存在: " + request.getBomCode());
        }

        // 构建BOM头
        ProBom bom = ProBom.builder()
                .tenantId(request.getTenantId())
                .bomCode(request.getBomCode())
                .bomName(request.getBomName())
                .bomType(request.getBomType())
                .version(request.getVersion())
                .productId(request.getProductId())
                .productCode(request.getProductCode())
                .productName(request.getProductName())
                .specification(request.getSpecification())
                .unit(request.getUnit())
                .bomQty(request.getBomQty() != null ? request.getBomQty() : BigDecimal.ONE)
                .baseType(request.getBaseType())
                .status(0) // 草稿
                .effectiveDate(request.getEffectiveDate())
                .expiryDate(request.getExpiryDate())
                .remark(request.getRemark())
                .attachments(request.getAttachments())
                .isDeleted(false)
                .build();

        // 构建BOM明细
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            List<ProBomDetail> details = request.getDetails().stream()
                    .map(this::buildBomDetail)
                    .collect(Collectors.toList());
            bom.setDetails(details);
        }

        ProBom saved = bomRepository.save(bom);
        log.info("创建BOM成功: code={}, name={}", saved.getBomCode(), saved.getBomName());

        // 发布BOM创建事件
        eventPublisher.publishEvent(new BomCreatedEvent(
                saved.getId(), saved.getBomCode(), saved.getProductId(), saved.getTenantId()));

        return saved.getId();
    }

    /**
     * 更新BOM
     *
     * @param id      BOM ID
     * @param request 更新请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBom(Long id, CreateBomRequest request) {
        ProBom existing = bomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BOM不存在"));

        // 检查BOM编码是否被其他BOM使用
        if (!existing.getBomCode().equals(request.getBomCode()) &&
            bomRepository.existsByBomCodeAndTenantIdAndIsDeletedFalse(request.getBomCode(), existing.getTenantId())) {
            throw new BusinessException("BOM编码已被其他BOM使用: " + request.getBomCode());
        }

        // 更新头信息
        existing.setBomCode(request.getBomCode());
        existing.setBomName(request.getBomName());
        existing.setBomType(request.getBomType());
        existing.setVersion(request.getVersion());
        existing.setProductId(request.getProductId());
        existing.setProductCode(request.getProductCode());
        existing.setProductName(request.getProductName());
        existing.setSpecification(request.getSpecification());
        existing.setUnit(request.getUnit());
        existing.setBomQty(request.getBomQty());
        existing.setBaseType(request.getBaseType());
        existing.setEffectiveDate(request.getEffectiveDate());
        existing.setExpiryDate(request.getExpiryDate());
        existing.setRemark(request.getRemark());
        existing.setAttachments(request.getAttachments());

        // 替换明细：清除旧的，添加新的
        existing.getDetails().clear();
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            List<ProBomDetail> newDetails = request.getDetails().stream()
                    .map(this::buildBomDetail)
                    .collect(Collectors.toList());
            existing.getDetails().addAll(newDetails);
        }

        bomRepository.save(existing);
        log.info("更新BOM成功: id={}", id);
    }

    /**
     * 删除BOM（软删除）
     *
     * @param id BOM ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBom(Long id) {
        ProBom bom = bomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BOM不存在"));

        bom.setIsDeleted(true);
        bomRepository.save(bom);
        log.info("删除BOM成功: id={}", id);
    }

    /**
     * 根据ID获取BOM详情
     *
     * @param id BOM ID
     * @return BOM DTO
     */
    public ProBomDTO getBomById(Long id) {
        ProBom bom = bomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BOM不存在"));
        return convertToDTO(bom);
    }

    /**
     * 分页查询BOM
     *
     * @param tenantId 租户ID
     * @param bomType  BOM类型
     * @param status   状态
     * @param current  当前页（从1开始）
     * @param size     每页大小
     * @return 分页结果
     */
    public PageResult<ProBomDTO> listBoms(Long tenantId, Integer bomType, Integer status, int current, int size) {
        Specification<ProBom> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.equal(root.get("isDeleted"), false));
            if (bomType != null) {
                predicates.add(cb.equal(root.get("bomType"), bomType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<ProBom> page = bomRepository.findAll(spec, PageRequest.of(current - 1, size));

        List<ProBomDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<ProBomDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(current)
                .size(size)
                .build();
    }

    /**
     * 启用BOM
     *
     * @param id BOM ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateBom(Long id) {
        ProBom bom = bomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BOM不存在"));
        bom.setStatus(1);
        bomRepository.save(bom);
        log.info("启用BOM成功: id={}", id);
    }

    /**
     * 停用BOM
     *
     * @param id BOM ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivateBom(Long id) {
        ProBom bom = bomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BOM不存在"));
        bom.setStatus(2);
        bomRepository.save(bom);
        log.info("停用BOM成功: id={}", id);
    }

    // ========== 私有方法 ==========

    /**
     * 构建BOM明细实体
     */
    private ProBomDetail buildBomDetail(BomDetailRequest req) {
        return ProBomDetail.builder()
                .lineNo(req.getLineNo())
                .componentType(req.getComponentType())
                .componentId(req.getComponentId())
                .componentCode(req.getComponentCode())
                .componentName(req.getComponentName())
                .specification(req.getSpecification())
                .unit(req.getUnit())
                .quantity(req.getQuantity())
                .scrapRate(req.getScrapRate())
                .effectiveStartDate(req.getEffectiveStartDate())
                .effectiveEndDate(req.getEffectiveEndDate())
                .isKeyComponent(req.getIsKeyComponent())
                .isReverseSubstitute(req.getIsReverseSubstitute())
                .substituteGroup(req.getSubstituteGroup())
                .supplyType(req.getSupplyType())
                .remark(req.getRemark())
                .build();
    }

    /**
     * 实体转DTO
     */
    private ProBomDTO convertToDTO(ProBom bom) {
        List<ProBomDetailDTO> detailDTOs = bom.getDetails() != null
                ? bom.getDetails().stream().map(this::convertDetailToDTO).collect(Collectors.toList())
                : List.of();

        return ProBomDTO.builder()
                .id(bom.getId())
                .tenantId(bom.getTenantId())
                .bomCode(bom.getBomCode())
                .bomName(bom.getBomName())
                .bomType(bom.getBomType())
                .bomTypeName(bom.getBomTypeName())
                .version(bom.getVersion())
                .productId(bom.getProductId())
                .productCode(bom.getProductCode())
                .productName(bom.getProductName())
                .specification(bom.getSpecification())
                .unit(bom.getUnit())
                .bomQty(bom.getBomQty())
                .baseType(bom.getBaseType())
                .status(bom.getStatus())
                .statusName(getStatusName(bom.getStatus()))
                .effectiveDate(bom.getEffectiveDate())
                .expiryDate(bom.getExpiryDate())
                .remark(bom.getRemark())
                .attachments(bom.getAttachments())
                .createdAt(bom.getCreatedAt())
                .createdBy(bom.getCreatedBy())
                .updatedAt(bom.getUpdatedAt())
                .updatedBy(bom.getUpdatedBy())
                .details(detailDTOs)
                .build();
    }

    /**
     * 明切实体转DTO
     */
    private ProBomDetailDTO convertDetailToDTO(ProBomDetail detail) {
        return ProBomDetailDTO.builder()
                .id(detail.getId())
                .bomId(detail.getBomId())
                .lineNo(detail.getLineNo())
                .componentType(detail.getComponentType())
                .componentId(detail.getComponentId())
                .componentCode(detail.getComponentCode())
                .componentName(detail.getComponentName())
                .specification(detail.getSpecification())
                .unit(detail.getUnit())
                .quantity(detail.getQuantity())
                .scrapRate(detail.getScrapRate())
                .effectiveStartDate(detail.getEffectiveStartDate())
                .effectiveEndDate(detail.getEffectiveEndDate())
                .isKeyComponent(detail.getIsKeyComponent())
                .isReverseSubstitute(detail.getIsReverseSubstitute())
                .substituteGroup(detail.getSubstituteGroup())
                .supplyType(detail.getSupplyType())
                .remark(detail.getRemark())
                .build();
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "启用";
            case 2 -> "停用";
            default -> "未知";
        };
    }
}
