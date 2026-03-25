package com.nexterp.business.controlling.application.service;

import com.nexterp.business.controlling.domain.model.CoProfitabilitySegment;
import com.nexterp.business.controlling.domain.repository.CoProfitabilitySegmentRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CO-PA 盈利分析服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoProfitabilityService {

    private final CoProfitabilitySegmentRepository segmentRepository;

    /**
     * 创建盈利段
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createSegment(CoProfitabilitySegment segment) {
        // 设置默认值
        if (segment.getRevenue() == null) {
            segment.setRevenue(BigDecimal.ZERO);
        }
        if (segment.getSalesDiscount() == null) {
            segment.setSalesDiscount(BigDecimal.ZERO);
        }
        if (segment.getNetRevenue() == null) {
            segment.setNetRevenue(BigDecimal.ZERO);
        }
        if (segment.getCostOfGoodsSold() == null) {
            segment.setCostOfGoodsSold(BigDecimal.ZERO);
        }
        if (segment.getGrossMargin() == null) {
            segment.setGrossMargin(BigDecimal.ZERO);
        }
        if (segment.getSalesExpense() == null) {
            segment.setSalesExpense(BigDecimal.ZERO);
        }
        if (segment.getAdminExpense() == null) {
            segment.setAdminExpense(BigDecimal.ZERO);
        }
        if (segment.getContributionMargin1() == null) {
            segment.setContributionMargin1(BigDecimal.ZERO);
        }
        if (segment.getContributionMargin2() == null) {
            segment.setContributionMargin2(BigDecimal.ZERO);
        }

        // 计算贡献边际
        segment.calculateContributionMargin();

        CoProfitabilitySegment saved = segmentRepository.save(segment);
        log.info("创建盈利段成功: segmentNumber={}", segment.getSegmentNumber());
        return saved.getId();
    }

    /**
     * 批量创建盈利段
     */
    @Transactional(rollbackFor = Exception.class)
    public void createSegmentsBatch(List<CoProfitabilitySegment> segments) {
        for (CoProfitabilitySegment segment : segments) {
            segment.calculateContributionMargin();
        }
        segmentRepository.saveAll(segments);
        log.info("批量创建盈利段成功: count={}", segments.size());
    }

    /**
     * 更新盈利段
     */
    @Transactional(rollbackFor = Exception.class)
    public CoProfitabilitySegment updateSegment(Long id, CoProfitabilitySegment segment) {
        CoProfitabilitySegment existing = segmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("盈利段不存在"));

        existing.setRevenue(segment.getRevenue());
        existing.setSalesDiscount(segment.getSalesDiscount());
        existing.setCostOfGoodsSold(segment.getCostOfGoodsSold());
        existing.setSalesExpense(segment.getSalesExpense());
        existing.setAdminExpense(segment.getAdminExpense());

        existing.calculateContributionMargin();

        return segmentRepository.save(existing);
    }

    /**
     * 删除盈利段
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSegment(Long id) {
        CoProfitabilitySegment segment = segmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("盈利段不存在"));

        segment.setIsDeleted(true);
        segmentRepository.save(segment);
        log.info("删除盈利段成功: id={}", id);
    }

    /**
     * 获取盈利段详情
     */
    public CoProfitabilitySegment getSegmentById(Long id) {
        return segmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("盈利段不存在"));
    }

    /**
     * 按会计年度查询
     */
    public List<CoProfitabilitySegment> listByFiscalYear(String fiscalYear, Long tenantId) {
        return segmentRepository.findByFiscalYearAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(fiscalYear, tenantId);
    }

    /**
     * 按会计期间查询
     */
    public List<CoProfitabilitySegment> listByFiscalPeriod(String fiscalYear, String fiscalPeriod, Long tenantId) {
        return segmentRepository.findByFiscalYearAndFiscalPeriodAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(
                fiscalYear, fiscalPeriod, tenantId);
    }

    /**
     * 按日期范围查询
     */
    public List<CoProfitabilitySegment> listByDateRange(LocalDate startDate, LocalDate endDate, Long tenantId) {
        return segmentRepository.findByPeriodDateBetweenAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(
                startDate, endDate, tenantId);
    }

    /**
     * 按利润中心查询
     */
    public List<CoProfitabilitySegment> listByProfitCenter(String profitCenter, Long tenantId) {
        return segmentRepository.findByProfitCenterAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(profitCenter, tenantId);
    }

    /**
     * 按客户查询
     */
    public List<CoProfitabilitySegment> listByCustomer(Long customerId, Long tenantId) {
        return segmentRepository.findByCustomerIdAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(customerId, tenantId);
    }

    /**
     * 按物料查询
     */
    public List<CoProfitabilitySegment> listByMaterial(Long materialId, Long tenantId) {
        return segmentRepository.findByMaterialIdAndTenantIdAndIsDeletedFalseOrderByPeriodDateAsc(materialId, tenantId);
    }

    /**
     * 分页查询
     */
    public Page<CoProfitabilitySegment> listSegments(Long tenantId, Pageable pageable) {
        return segmentRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("isDeleted"), false)
                ),
                pageable);
    }

    /**
     * 计算期间汇总
     */
    public PeriodSummary calculatePeriodSummary(String fiscalYear, String fiscalPeriod, Long tenantId) {
        List<CoProfitabilitySegment> segments = listByFiscalPeriod(fiscalYear, fiscalPeriod, tenantId);

        PeriodSummary summary = new PeriodSummary();
        for (CoProfitabilitySegment segment : segments) {
            summary.revenue = summary.revenue.add(segment.getRevenue() != null ? segment.getRevenue() : BigDecimal.ZERO);
            summary.costOfGoodsSold = summary.costOfGoodsSold.add(segment.getCostOfGoodsSold() != null ? segment.getCostOfGoodsSold() : BigDecimal.ZERO);
            summary.grossMargin = summary.grossMargin.add(segment.getGrossMargin() != null ? segment.getGrossMargin() : BigDecimal.ZERO);
            summary.contributionMargin = summary.contributionMargin.add(segment.getContributionMargin2() != null ? segment.getContributionMargin2() : BigDecimal.ZERO);
        }

        if (summary.revenue.compareTo(BigDecimal.ZERO) > 0) {
            summary.grossMarginRate = summary.grossMargin.multiply(BigDecimal.valueOf(100))
                    .divide(summary.revenue, 2, BigDecimal.ROUND_HALF_UP);
        }

        return summary;
    }

    /**
     * 期间汇总数据
     */
    public static class PeriodSummary {
        public BigDecimal revenue = BigDecimal.ZERO;
        public BigDecimal costOfGoodsSold = BigDecimal.ZERO;
        public BigDecimal grossMargin = BigDecimal.ZERO;
        public BigDecimal contributionMargin = BigDecimal.ZERO;
        public BigDecimal grossMarginRate = BigDecimal.ZERO;
    }
}
