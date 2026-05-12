package com.nexterp.business.production.application.service;

import com.nexterp.business.production.domain.model.ProRouting;
import com.nexterp.business.production.domain.model.ProRoutingDetail;
import com.nexterp.business.production.domain.repository.ProRoutingDetailRepository;
import com.nexterp.business.production.domain.repository.ProRoutingRepository;
import com.nexterp.business.production.dto.CreateRoutingRequest;
import com.nexterp.business.production.dto.ProRoutingDTO;
import com.nexterp.business.production.dto.ProRoutingDetailDTO;
import com.nexterp.business.production.dto.RoutingDetailRequest;
import com.nexterp.business.production.event.RoutingCreatedEvent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工艺路线服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProRoutingService {

    private final ProRoutingRepository routingRepository;
    private final ProRoutingDetailRepository routingDetailRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建工艺路线
     *
     * @param request 创建工艺路线请求
     * @return 工艺路线ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRouting(CreateRoutingRequest request) {
        // 检查工艺路线编码唯一性
        routingRepository.findByRoutingCodeAndTenantId(request.getRoutingCode(), request.getTenantId())
                .ifPresent(existing -> {
                    throw new BusinessException("工艺路线编码已存在: " + request.getRoutingCode());
                });

        ProRouting routing = ProRouting.builder()
                .routingCode(request.getRoutingCode())
                .routingName(request.getRoutingName())
                .productId(request.getProductId())
                .productCode(request.getProductCode())
                .productName(request.getProductName())
                .specification(request.getSpecification())
                .routingType(request.getRoutingType())
                .version(request.getVersion())
                .isDefault(request.getIsDefault())
                .status(0) // 草稿
                .effectiveDate(request.getEffectiveDate())
                .expiryDate(request.getExpiryDate())
                .remark(request.getRemark())
                .tenantId(request.getTenantId())
                .build();

        // 构建明细
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            List<ProRoutingDetail> details = new ArrayList<>();
            for (RoutingDetailRequest detailReq : request.getDetails()) {
                ProRoutingDetail detail = ProRoutingDetail.builder()
                        .sequenceNo(detailReq.getSequenceNo())
                        .processId(detailReq.getProcessId())
                        .processCode(detailReq.getProcessCode())
                        .processName(detailReq.getProcessName())
                        .workCenterId(detailReq.getWorkCenterId())
                        .workCenterName(detailReq.getWorkCenterName())
                        .standardManHours(detailReq.getStandardManHours())
                        .standardMachineHours(detailReq.getStandardMachineHours())
                        .setupTime(detailReq.getSetupTime())
                        .waitTime(detailReq.getWaitTime())
                        .moveTime(detailReq.getMoveTime())
                        .laborRate(detailReq.getLaborRate())
                        .machineRate(detailReq.getMachineRate())
                        .variableOverheadRate(detailReq.getVariableOverheadRate())
                        .fixedOverheadRate(detailReq.getFixedOverheadRate())
                        .minBatchQty(detailReq.getMinBatchQty())
                        .maxBatchQty(detailReq.getMaxBatchQty())
                        .isParallel(detailReq.getIsParallel())
                        .isOverlap(detailReq.getIsOverlap())
                        .remark(detailReq.getRemark())
                        .routing(routing)
                        .build();
                details.add(detail);
            }
            routing.setDetails(details);
        }

        ProRouting saved = routingRepository.save(routing);
        log.info("创建工艺路线成功: id={}, routingCode={}", saved.getId(), saved.getRoutingCode());

        // 发布工艺路线创建事件
        eventPublisher.publishEvent(new RoutingCreatedEvent(
                saved.getId(),
                saved.getRoutingCode(),
                saved.getProductId(),
                saved.getTenantId()
        ));

        return saved.getId();
    }

    /**
     * 更新工艺路线
     *
     * @param id      工艺路线ID
     * @param request 更新工艺路线请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRouting(Long id, CreateRoutingRequest request) {
        ProRouting routing = routingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工艺路线不存在"));

        if (routing.getStatus() == 1) {
            throw new BusinessException("已启用的工艺路线不允许修改");
        }

        routing.setRoutingCode(request.getRoutingCode());
        routing.setRoutingName(request.getRoutingName());
        routing.setProductId(request.getProductId());
        routing.setProductCode(request.getProductCode());
        routing.setProductName(request.getProductName());
        routing.setSpecification(request.getSpecification());
        routing.setRoutingType(request.getRoutingType());
        routing.setVersion(request.getVersion());
        routing.setIsDefault(request.getIsDefault());
        routing.setEffectiveDate(request.getEffectiveDate());
        routing.setExpiryDate(request.getExpiryDate());
        routing.setRemark(request.getRemark());

        // 清除旧明细并重建
        routing.getDetails().clear();

        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            for (RoutingDetailRequest detailReq : request.getDetails()) {
                ProRoutingDetail detail = ProRoutingDetail.builder()
                        .sequenceNo(detailReq.getSequenceNo())
                        .processId(detailReq.getProcessId())
                        .processCode(detailReq.getProcessCode())
                        .processName(detailReq.getProcessName())
                        .workCenterId(detailReq.getWorkCenterId())
                        .workCenterName(detailReq.getWorkCenterName())
                        .standardManHours(detailReq.getStandardManHours())
                        .standardMachineHours(detailReq.getStandardMachineHours())
                        .setupTime(detailReq.getSetupTime())
                        .waitTime(detailReq.getWaitTime())
                        .moveTime(detailReq.getMoveTime())
                        .laborRate(detailReq.getLaborRate())
                        .machineRate(detailReq.getMachineRate())
                        .variableOverheadRate(detailReq.getVariableOverheadRate())
                        .fixedOverheadRate(detailReq.getFixedOverheadRate())
                        .minBatchQty(detailReq.getMinBatchQty())
                        .maxBatchQty(detailReq.getMaxBatchQty())
                        .isParallel(detailReq.getIsParallel())
                        .isOverlap(detailReq.getIsOverlap())
                        .remark(detailReq.getRemark())
                        .routing(routing)
                        .build();
                routing.getDetails().add(detail);
            }
        }

        routingRepository.save(routing);
        log.info("更新工艺路线成功: id={}", id);
    }

    /**
     * 删除工艺路线
     *
     * @param id 工艺路线ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouting(Long id) {
        ProRouting routing = routingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工艺路线不存在"));

        if (routing.getStatus() == 1) {
            throw new BusinessException("已启用的工艺路线不允许删除");
        }

        routingRepository.delete(routing);
        log.info("删除工艺路线成功: id={}", id);
    }

    /**
     * 根据ID获取工艺路线
     *
     * @param id 工艺路线ID
     * @return 工艺路线DTO
     */
    public ProRoutingDTO getRoutingById(Long id) {
        ProRouting routing = routingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工艺路线不存在"));
        return convertToDTO(routing);
    }

    /**
     * 分页查询工艺路线
     *
     * @param tenantId    租户ID
     * @param routingType 工艺路线类型 (可选)
     * @param status      状态 (可选)
     * @param current     当前页
     * @param size        每页大小
     * @return 分页结果
     */
    public PageResult<ProRoutingDTO> listRoutings(Long tenantId, Integer routingType,
                                                   Integer status, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);

        Specification<ProRouting> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (routingType != null) {
                predicates.add(cb.equal(root.get("routingType"), routingType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<ProRouting> page = routingRepository.findAll(spec, pageRequest);

        List<ProRoutingDTO> records = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.<ProRoutingDTO>builder()
                .records(records)
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 启用工艺路线
     *
     * @param id 工艺路线ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateRouting(Long id) {
        ProRouting routing = routingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工艺路线不存在"));

        if (routing.getStatus() == 1) {
            throw new BusinessException("工艺路线已处于启用状态");
        }

        routing.setStatus(1);
        routingRepository.save(routing);
        log.info("启用工艺路线成功: id={}", id);
    }

    /**
     * 停用工艺路线
     *
     * @param id 工艺路线ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivateRouting(Long id) {
        ProRouting routing = routingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("工艺路线不存在"));

        if (routing.getStatus() == 2) {
            throw new BusinessException("工艺路线已处于停用状态");
        }

        routing.setStatus(2);
        routingRepository.save(routing);
        log.info("停用工艺路线成功: id={}", id);
    }

    /**
     * 转换为DTO
     *
     * @param routing 工艺路线实体
     * @return 工艺路线DTO
     */
    private ProRoutingDTO convertToDTO(ProRouting routing) {
        List<ProRoutingDetailDTO> detailDTOs = routing.getDetails().stream()
                .map(this::convertDetailToDTO)
                .collect(Collectors.toList());

        return ProRoutingDTO.builder()
                .id(routing.getId())
                .tenantId(routing.getTenantId())
                .routingCode(routing.getRoutingCode())
                .routingName(routing.getRoutingName())
                .productId(routing.getProductId())
                .productCode(routing.getProductCode())
                .productName(routing.getProductName())
                .specification(routing.getSpecification())
                .routingType(routing.getRoutingType())
                .routingTypeName(routing.getRoutingTypeName())
                .version(routing.getVersion())
                .isDefault(routing.getIsDefault())
                .status(routing.getStatus())
                .effectiveDate(routing.getEffectiveDate())
                .expiryDate(routing.getExpiryDate())
                .remark(routing.getRemark())
                .details(detailDTOs)
                .build();
    }

    /**
     * 转换明细为DTO
     *
     * @param detail 工艺路线明细实体
     * @return 工艺路线明细DTO
     */
    private ProRoutingDetailDTO convertDetailToDTO(ProRoutingDetail detail) {
        return ProRoutingDetailDTO.builder()
                .id(detail.getId())
                .routingId(detail.getRoutingId())
                .sequenceNo(detail.getSequenceNo())
                .processId(detail.getProcessId())
                .processCode(detail.getProcessCode())
                .processName(detail.getProcessName())
                .workCenterId(detail.getWorkCenterId())
                .workCenterName(detail.getWorkCenterName())
                .standardManHours(detail.getStandardManHours())
                .standardMachineHours(detail.getStandardMachineHours())
                .setupTime(detail.getSetupTime())
                .waitTime(detail.getWaitTime())
                .moveTime(detail.getMoveTime())
                .laborRate(detail.getLaborRate())
                .machineRate(detail.getMachineRate())
                .variableOverheadRate(detail.getVariableOverheadRate())
                .fixedOverheadRate(detail.getFixedOverheadRate())
                .minBatchQty(detail.getMinBatchQty())
                .maxBatchQty(detail.getMaxBatchQty())
                .isParallel(detail.getIsParallel())
                .isOverlap(detail.getIsOverlap())
                .parallelGroupNo(detail.getParallelGroupNo())
                .nextSequenceNo(detail.getNextSequenceNo())
                .alternativeProcessId(detail.getAlternativeProcessId())
                .checkItems(detail.getCheckItems())
                .remark(detail.getRemark())
                .build();
    }
}
