package com.nexterp.business.finance.application.service;

import com.nexterp.business.finance.domain.model.FinAccountingPeriod;
import com.nexterp.business.finance.domain.repository.FinAccountingPeriodRepository;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 会计期间服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinAccountingPeriodService {

    private final FinAccountingPeriodRepository periodRepository;

    /**
     * 初始化年度期间
     *
     * @param year     会计年度
     * @param tenantId 租户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void initializeYearPeriods(Integer year, Long tenantId) {
        // 检查是否已存在期间
        List<FinAccountingPeriod> existing = periodRepository.findByAccountingYearAndTenantIdAndIsDeletedFalseOrderByAccountingPeriodAsc(
                year, tenantId);
        if (!existing.isEmpty()) {
            throw new BusinessException("该年度期间已存在");
        }

        // 创建12个期间
        List<FinAccountingPeriod> periods = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            FinAccountingPeriod period = FinAccountingPeriod.builder()
                    .tenantId(tenantId)
                    .accountingYear(year)
                    .accountingPeriod(year + "-" + String.format("%02d", month))
                    .periodStartDate(startDate)
                    .periodEndDate(endDate)
                    .periodStatus(0) // 未开启
                    .build();

            periods.add(period);
        }

        periodRepository.saveAll(periods);
        log.info("初始化年度期间成功: year={}, count={}", year, periods.size());
    }

    /**
     * 开启期间
     *
     * @param periodId 期间ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void openPeriod(Long periodId) {
        FinAccountingPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new BusinessException("期间不存在"));

        if (period.getPeriodStatus() != 0) {
            throw new BusinessException("只能开启未开启状态的期间");
        }

        // 检查上一个期间是否已结账
        String currentPeriod = period.getAccountingPeriod();
        int year = Integer.parseInt(currentPeriod.split("-")[0]);
        int month = Integer.parseInt(currentPeriod.split("-")[1]);

        if (month > 1) {
            String prevPeriod = year + "-" + String.format("%02d", month - 1);
            periodRepository.findByAccountingPeriodAndTenantIdAndIsDeletedFalse(prevPeriod, period.getTenantId())
                    .ifPresent(prev -> {
                        if (prev.getPeriodStatus() != 2) {
                            throw new BusinessException("上一个期间未结账，无法开启当前期间");
                        }
                    });
        }

        period.setPeriodStatus(1); // 已开启
        periodRepository.save(period);

        log.info("开启期间成功: period={}", currentPeriod);
    }

    /**
     * 结账
     *
     * @param periodId 期间ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void closePeriod(Long periodId) {
        FinAccountingPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new BusinessException("期间不存在"));

        if (!period.canClose()) {
            throw new BusinessException("期间状态不允许结账");
        }

        // TODO: 检查期间是否有未记账凭证

        // TODO: 计算期间发生额

        // TODO: 结转损益

        Long currentUserId = UserContext.getUserId();
        period.setPeriodStatus(2); // 已结账
        period.setClosedById(currentUserId);
        // TODO: 从用户服务获取用户名
        period.setClosedByName("结账人");
        period.setClosedAt(java.time.LocalDateTime.now());

        periodRepository.save(period);

        log.info("期间结账成功: period={}", period.getAccountingPeriod());
    }

    /**
     * 反结账
     *
     * @param periodId 期间ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void reopenPeriod(Long periodId) {
        FinAccountingPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new BusinessException("期间不存在"));

        if (!period.canReopen()) {
            throw new BusinessException("期间状态不允许反结账");
        }

        // 检查下一个期间是否已开启
        String currentPeriod = period.getAccountingPeriod();
        int year = Integer.parseInt(currentPeriod.split("-")[0]);
        int month = Integer.parseInt(currentPeriod.split("-")[1]);

        if (month < 12) {
            String nextPeriod = year + "-" + String.format("%02d", month + 1);
            periodRepository.findByAccountingPeriodAndTenantIdAndIsDeletedFalse(nextPeriod, period.getTenantId())
                    .ifPresent(next -> {
                        if (next.getPeriodStatus() > 0) {
                            throw new BusinessException("下一个期间已开启，无法反结账当前期间");
                        }
                    });
        }

        Long currentUserId = UserContext.getUserId();
        period.setPeriodStatus(1); // 恢复为已开启状态
        period.setReopenedById(currentUserId);
        // TODO: 从用户服务获取用户名
        period.setReopenedByName("反结账人");
        period.setReopenedAt(java.time.LocalDateTime.now());

        periodRepository.save(period);

        log.info("期间反结账成功: period={}", currentPeriod);
    }

    /**
     * 获取期间详情
     *
     * @param id 期间ID
     * @return 期间
     */
    public FinAccountingPeriod getPeriodById(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new BusinessException("期间不存在"));
    }

    /**
     * 获取当前期间
     *
     * @param tenantId 租户ID
     * @return 当前期间
     */
    public FinAccountingPeriod getCurrentPeriod(Long tenantId) {
        return periodRepository.findCurrentPeriod(tenantId)
                .orElseThrow(() -> new BusinessException("没有开启的期间"));
    }

    /**
     * 根据日期获取期间
     *
     * @param date     日期
     * @param tenantId 租户ID
     * @return 期间
     */
    public FinAccountingPeriod getPeriodByDate(LocalDate date, Long tenantId) {
        return periodRepository.findByDate(date, tenantId)
                .orElseThrow(() -> new BusinessException("日期所在期间不存在或未开启"));
    }

    /**
     * 获取年度期间列表
     *
     * @param year     会计年度
     * @param tenantId 租户ID
     * @return 期间列表
     */
    public List<FinAccountingPeriod> listYearPeriods(Integer year, Long tenantId) {
        return periodRepository.findByAccountingYearAndTenantIdAndIsDeletedFalseOrderByAccountingPeriodAsc(
                year, tenantId);
    }

    /**
     * 获取已开启的期间列表
     *
     * @param tenantId 租户ID
     * @return 期间列表
     */
    public List<FinAccountingPeriod> listOpenedPeriods(Long tenantId) {
        return periodRepository.findOpenedPeriods(tenantId);
    }

    /**
     * 获取未结账的期间列表
     *
     * @param tenantId 租户ID
     * @return 期间列表
     */
    public List<FinAccountingPeriod> listUnclosedPeriods(Long tenantId) {
        return periodRepository.findUnclosedPeriods(tenantId);
    }
}
