package com.nexterp.business.sales.application.service;

import com.nexterp.business.sales.domain.model.SdCreditCheckLog;
import com.nexterp.business.sales.domain.model.SdCreditMaster;
import com.nexterp.business.sales.domain.model.SdSalesOrderHdr;
import com.nexterp.business.sales.domain.repository.SdCreditCheckLogRepository;
import com.nexterp.business.sales.domain.repository.SdCreditMasterRepository;
import com.nexterp.business.sales.domain.repository.SdSalesOrderHdrRepository;
import com.nexterp.business.sales.dto.CreditCheckRequest;
import com.nexterp.business.sales.dto.CreditCheckResult;
import com.nexterp.business.sales.dto.CreditMasterDTO;
import com.nexterp.business.sales.event.CreditCheckPerformedEvent;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 信用管理服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SdCreditService {

    private final SdCreditMasterRepository creditMasterRepository;
    private final SdCreditCheckLogRepository creditCheckLogRepository;
    private final SdSalesOrderHdrRepository salesOrderHdrRepository;
    private final ApplicationEventPublisher eventPublisher;

    /** 信用检查结果: OK-通过 */
    private static final String CHECK_RESULT_OK = "OK";
    /** 信用检查结果: WA-警告 */
    private static final String CHECK_RESULT_WA = "WA";
    /** 信用检查结果: BL-阻止 */
    private static final String CHECK_RESULT_BL = "BL";

    /**
     * 获取客户信用主数据
     *
     * @param customerId 客户ID
     * @param companyId  公司代码ID
     * @return 信用主数据DTO
     */
    public CreditMasterDTO getCreditMaster(Long customerId, Long companyId) {
        SdCreditMaster master = creditMasterRepository
                .findByCustomerIdAndCompanyIdAndIsDeletedFalse(customerId, companyId)
                .orElseThrow(() -> new BusinessException("客户信用主数据不存在, customerId=" + customerId + ", companyId=" + companyId));

        return convertToMasterDTO(master);
    }

    /**
     * 更新客户信用主数据
     *
     * @param customerId  客户ID
     * @param companyId   公司代码ID
     * @param creditLimit 信用额度
     * @param riskClass   风险类别
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCreditMaster(Long customerId, Long companyId, BigDecimal creditLimit, String riskClass) {
        SdCreditMaster master = creditMasterRepository
                .findByCustomerIdAndCompanyIdAndIsDeletedFalse(customerId, companyId)
                .orElseThrow(() -> new BusinessException("客户信用主数据不存在, customerId=" + customerId + ", companyId=" + companyId));

        if (creditLimit != null) {
            master.setCreditLimit(creditLimit);
            master.setAvailableLimit(creditLimit.subtract(master.getUsedLimit()));
        }
        if (riskClass != null) {
            master.setRiskClass(riskClass);
        }
        master.setLastCheckDate(LocalDate.now());

        creditMasterRepository.save(master);
        log.info("更新客户信用主数据: customerId={}, creditLimit={}, riskClass={}", customerId, creditLimit, riskClass);
    }

    /**
     * 执行信用检查
     * 检查已用额度+待检金额 是否超过信用额度, 返回 OK/WA/BL
     *
     * @param request 信用检查请求
     * @return 信用检查结果
     */
    @Transactional(rollbackFor = Exception.class)
    public CreditCheckResult performCreditCheck(CreditCheckRequest request) {
        // 获取信用主数据
        SdCreditMaster master = creditMasterRepository
                .findByCustomerIdAndCompanyIdAndIsDeletedFalse(request.getCustomerId(), request.getCompanyId())
                .orElseThrow(() -> new BusinessException("客户信用主数据不存在, 请先创建信用主数据"));

        BigDecimal creditLimit = master.getCreditLimit();
        BigDecimal usedBefore = master.getUsedLimit();
        BigDecimal checkAmount = request.getCheckAmount() != null ? request.getCheckAmount() : BigDecimal.ZERO;

        // 计算检查后额度
        BigDecimal usedAfter = usedBefore.add(checkAmount);
        BigDecimal availableAfter = creditLimit.subtract(usedAfter);

        // 计算使用率
        BigDecimal usageRate = BigDecimal.ZERO;
        if (creditLimit.compareTo(BigDecimal.ZERO) > 0) {
            usageRate = usedAfter.multiply(new BigDecimal("100"))
                    .divide(creditLimit, 2, RoundingMode.HALF_UP);
        }

        // 判定检查结果
        String checkResult;
        String message;
        String riskClass = master.getRiskClass();

        if (usedAfter.compareTo(creditLimit) <= 0) {
            // 未超出额度
            if (usageRate.compareTo(new BigDecimal("80")) >= 0) {
                checkResult = CHECK_RESULT_WA;
                message = "信用额度使用率已达" + usageRate + "%, 接近上限";
            } else {
                checkResult = CHECK_RESULT_OK;
                message = "信用检查通过";
            }
        } else {
            // 超出额度
            if ("1".equals(riskClass)) {
                // 低风险客户: 警告
                checkResult = CHECK_RESULT_WA;
                message = "信用额度已超出, 但客户为低风险类别, 给予警告";
            } else {
                // 中高风险客户: 阻止
                checkResult = CHECK_RESULT_BL;
                message = "信用额度已超出, 订单被信用冻结";
            }
        }

        // 更新信用主数据
        master.setUsedLimit(usedAfter);
        master.setAvailableLimit(availableAfter);
        master.setLastCheckDate(LocalDate.now());

        // 更新信用状态
        if (CHECK_RESULT_BL.equals(checkResult)) {
            master.setCreditStatus("03"); // 冻结
        } else if (CHECK_RESULT_WA.equals(checkResult)) {
            master.setCreditStatus("02"); // 预警
        } else {
            master.setCreditStatus("01"); // 正常
        }

        creditMasterRepository.save(master);

        // 保存信用检查日志
        SdCreditCheckLog checkLog = SdCreditCheckLog.builder()
                .tenantId(request.getTenantId())
                .customerId(request.getCustomerId())
                .companyId(request.getCompanyId())
                .checkType(request.getCheckType())
                .documentType(request.getDocumentType())
                .documentId(request.getDocumentId())
                .documentNumber(request.getDocumentNumber())
                .checkAmount(checkAmount)
                .usedBefore(usedBefore)
                .usedAfter(usedAfter)
                .creditLimit(creditLimit)
                .usageRate(usageRate)
                .checkResult(checkResult)
                .resultMessage(message)
                .checkTime(LocalDateTime.now())
                .build();
        creditCheckLogRepository.save(checkLog);

        log.info("信用检查完成: customerId={}, checkResult={}, usageRate={}%",
                request.getCustomerId(), checkResult, usageRate);

        // 发布信用检查事件
        eventPublisher.publishEvent(new CreditCheckPerformedEvent(
                request.getCustomerId(),
                request.getCompanyId(),
                checkResult,
                checkAmount,
                request.getTenantId()
        ));

        return CreditCheckResult.builder()
                .checkResult(checkResult)
                .creditLimit(creditLimit)
                .usedBefore(usedBefore)
                .usedAfter(usedAfter)
                .availableLimit(availableAfter)
                .usageRate(usageRate)
                .message(message)
                .build();
    }

    /**
     * 分页查询信用检查日志
     *
     * @param tenantId   租户ID
     * @param customerId 客户ID
     * @param current    当前页
     * @param size       每页大小
     * @return 分页结果
     */
    public PageResult<SdCreditCheckLog> getCreditLogs(Long tenantId, Long customerId, int current, int size) {
        PageRequest pageRequest = PageRequest.of(current - 1, size);
        Page<SdCreditCheckLog> page = creditCheckLogRepository
                .findByTenantIdAndCustomerIdOrderByCheckTimeDesc(tenantId, customerId, pageRequest);

        return PageResult.<SdCreditCheckLog>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .current(page.getNumber() + 1)
                .size(page.getSize())
                .build();
    }

    /**
     * 获取信用冻结的订单列表
     * 查找由于信用检查被阻止的订单 (简化实现: 查找信用状态为"03"冻结的客户关联订单)
     *
     * @param tenantId 租户ID
     * @return 被冻结的订单列表
     */
    public List<Map<String, Object>> getBlockedOrders(Long tenantId) {
        // 查找信用状态为"03"冻结的客户
        List<SdCreditMaster> blockedMasters = creditMasterRepository
                .findByTenantIdAndCreditStatusAndIsDeletedFalse(tenantId, "03");

        List<Map<String, Object>> blockedOrders = new java.util.ArrayList<>();
        for (SdCreditMaster master : blockedMasters) {
            // 查找该客户在创建或审批状态的订单
            List<SdSalesOrderHdr> orders = salesOrderHdrRepository
                    .findByTenantIdAndIsDeletedFalse(tenantId, PageRequest.of(0, 100))
                    .getContent()
                    .stream()
                    .filter(o -> o.getSoldToParty().equals(master.getCustomerId())
                            && ("01".equals(o.getOrderStatus()) || "02".equals(o.getOrderStatus())))
                    .collect(Collectors.toList());

            for (SdSalesOrderHdr order : orders) {
                Map<String, Object> blockedOrder = new HashMap<>();
                blockedOrder.put("orderId", order.getId());
                blockedOrder.put("orderNumber", order.getOrderNumber());
                blockedOrder.put("customerId", master.getCustomerId());
                blockedOrder.put("orderStatus", order.getOrderStatus());
                blockedOrder.put("orderAmount", order.getGrossValue());
                blockedOrder.put("creditLimit", master.getCreditLimit());
                blockedOrder.put("usedLimit", master.getUsedLimit());
                blockedOrders.add(blockedOrder);
            }
        }

        return blockedOrders;
    }

    /**
     * 释放信用冻结的订单
     *
     * @param orderId    订单ID
     * @param releasedBy 释放人
     */
    @Transactional(rollbackFor = Exception.class)
    public void releaseBlockedOrder(Long orderId, String releasedBy) {
        SdSalesOrderHdr order = salesOrderHdrRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("销售订单不存在"));

        // 获取客户信用主数据
        SdCreditMaster master = creditMasterRepository
                .findByCustomerIdAndCompanyIdAndIsDeletedFalse(order.getSoldToParty(), order.getSalesOrgId())
                .orElseThrow(() -> new BusinessException("客户信用主数据不存在"));

        if (!"03".equals(master.getCreditStatus())) {
            throw new BusinessException("该客户信用状态非冻结, 无需释放");
        }

        // 释放信用冻结
        master.setCreditStatus("01"); // 恢复正常
        master.setLastCheckDate(LocalDate.now());
        creditMasterRepository.save(master);

        // 记录信用检查日志
        SdCreditCheckLog releaseLog = SdCreditCheckLog.builder()
                .tenantId(order.getTenantId())
                .customerId(order.getSoldToParty())
                .companyId(order.getSalesOrgId())
                .checkType("01")
                .documentType("SO")
                .documentId(orderId)
                .documentNumber(order.getOrderNumber())
                .checkAmount(BigDecimal.ZERO)
                .usedBefore(master.getUsedLimit())
                .usedAfter(master.getUsedLimit())
                .creditLimit(master.getCreditLimit())
                .usageRate(master.getCreditLimit().compareTo(BigDecimal.ZERO) > 0
                        ? master.getUsedLimit().multiply(new BigDecimal("100"))
                        .divide(master.getCreditLimit(), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO)
                .checkResult("OK")
                .resultMessage("信用冻结已由" + releasedBy + "手动释放")
                .checkTime(LocalDateTime.now())
                .build();
        creditCheckLogRepository.save(releaseLog);

        log.info("释放信用冻结订单: orderId={}, releasedBy={}", orderId, releasedBy);
    }

    /**
     * 转换为信用主数据DTO
     *
     * @param master 信用主数据实体
     * @return 信用主数据DTO
     */
    private CreditMasterDTO convertToMasterDTO(SdCreditMaster master) {
        return CreditMasterDTO.builder()
                .id(master.getId())
                .customerId(master.getCustomerId())
                .companyId(master.getCompanyId())
                .creditLimit(master.getCreditLimit())
                .usedLimit(master.getUsedLimit())
                .availableLimit(master.getAvailableLimit())
                .riskClass(master.getRiskClass())
                .creditGroup(master.getCreditGroup())
                .creditStatus(master.getCreditStatus())
                .checkRule(master.getCheckRule())
                .lastCheckDate(master.getLastCheckDate())
                .nextCheckDate(master.getNextCheckDate())
                .build();
    }
}
