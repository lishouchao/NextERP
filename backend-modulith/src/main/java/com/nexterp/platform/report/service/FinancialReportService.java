package com.nexterp.platform.report.service;

import com.nexterp.platform.report.domain.model.SysReport;
import com.nexterp.platform.report.domain.repository.SysReportRepository;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 财务报表服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialReportService {

    private final SysReportRepository reportRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 生成试算平衡表
     *
     * @param tenantId 租户ID
     * @param period   会计期间
     * @return 试算平衡表数据
     */
    public Map<String, Object> generateTrialBalance(Long tenantId, String period) {
        // 查询科目余额
        String sql = """
            SELECT
                account_code,
                account_name,
                account_type,
                account_direction,
                opening_balance,
                current_debit,
                current_credit,
                ending_balance
            FROM fin_account
            WHERE tenant_id = ?
            AND status = 1
            AND is_deleted = false
            ORDER BY account_code
            """;

        List<Map<String, Object>> accounts = jdbcTemplate.queryForList(sql, tenantId);

        // 计算借方合计和贷方合计
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebitEnding = BigDecimal.ZERO;
        BigDecimal totalCreditEnding = BigDecimal.ZERO;

        for (Map<String, Object> account : accounts) {
            BigDecimal debit = account.get("current_debit") != null ?
                new BigDecimal(account.get("current_debit").toString()) : BigDecimal.ZERO;
            BigDecimal credit = account.get("current_credit") != null ?
                new BigDecimal(account.get("current_credit").toString()) : BigDecimal.ZERO;
            BigDecimal endingBalance = account.get("ending_balance") != null ?
                new BigDecimal(account.get("ending_balance").toString()) : BigDecimal.ZERO;
            Integer accountType = account.get("account_type") != null ?
                Integer.parseInt(account.get("account_type").toString()) : 0;

            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);

            if (accountType == 1 || accountType == 4) {
                // 资产、成本类科目
                totalDebitEnding = totalDebitEnding.add(endingBalance);
            } else {
                // 负债、所有者权益、损益类科目
                totalCreditEnding = totalCreditEnding.add(endingBalance);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reportDate", LocalDate.now());
        result.put("period", period);
        result.put("accounts", accounts);
        result.put("totalDebit", totalDebit);
        result.put("totalCredit", totalCredit);
        result.put("totalDebitEnding", totalDebitEnding);
        result.put("totalCreditEnding", totalCreditEnding);
        result.put("isBalanced", totalDebit.compareTo(totalCredit) == 0);

        return result;
    }

    /**
     * 生成资产负债表
     *
     * @param tenantId 租户ID
     * @param period   会计期间
     * @return 资产负债表数据
     */
    public Map<String, Object> generateBalanceSheet(Long tenantId, String period) {
        // 查询科目余额并分类汇总
        String sql = """
            SELECT
                account_type,
                account_code,
                account_name,
                account_direction,
                ending_balance
            FROM fin_account
            WHERE tenant_id = ?
            AND status = 1
            AND is_deleted = false
            ORDER BY account_code
            """;

        List<Map<String, Object>> accounts = jdbcTemplate.queryForList(sql, tenantId);

        // 按科目类型分组汇总
        Map<Integer, BigDecimal> typeBalances = new HashMap<>();
        for (Map<String, Object> account : accounts) {
            Integer accountType = Integer.parseInt(account.get("account_type").toString());
            BigDecimal balance = account.get("ending_balance") != null ?
                new BigDecimal(account.get("ending_balance").toString()) : BigDecimal.ZERO;
            typeBalances.merge(accountType, balance, BigDecimal::add);
        }

        // 计算资产、负债、所有者权益
        BigDecimal assets = typeBalances.getOrDefault(1, BigDecimal.ZERO); // 资产
        BigDecimal liabilities = typeBalances.getOrDefault(2, BigDecimal.ZERO); // 负债
        BigDecimal equity = typeBalances.getOrDefault(3, BigDecimal.ZERO); // 所有者权益

        Map<String, Object> result = new HashMap<>();
        result.put("reportDate", LocalDate.now());
        result.put("period", period);
        result.put("assets", assets);
        result.put("liabilities", liabilities);
        result.put("equity", equity);
        result.put("liabilitiesAndEquity", liabilities.add(equity));
        result.put("isBalanced", assets.compareTo(liabilities.add(equity)) == 0);

        // 明细数据
        result.put("assetDetails", filterByType(accounts, 1));
        result.put("liabilityDetails", filterByType(accounts, 2));
        result.put("equityDetails", filterByType(accounts, 3));

        return result;
    }

    /**
     * 生成利润表
     *
     * @param tenantId 租户ID
     * @param period   会计期间
     * @return 利润表数据
     */
    public Map<String, Object> generateIncomeStatement(Long tenantId, String period) {
        // TODO: 从凭证表汇总收入和费用
        Map<String, Object> result = new HashMap<>();
        result.put("reportDate", LocalDate.now());
        result.put("period", period);
        result.put("revenue", BigDecimal.ZERO);
        result.put("cost", BigDecimal.ZERO);
        result.put("expense", BigDecimal.ZERO);
        result.put("operatingProfit", BigDecimal.ZERO);
        result.put("netProfit", BigDecimal.ZERO);

        return result;
    }

    /**
     * 生成现金流量表
     *
     * @param tenantId 租户ID
     * @param period   会计期间
     * @return 现金流量表数据
     */
    public Map<String, Object> generateCashFlowStatement(Long tenantId, String period) {
        // TODO: 实现现金流量表计算逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("reportDate", LocalDate.now());
        result.put("period", period);
        result.put("operatingCashFlow", BigDecimal.ZERO);
        result.put("investingCashFlow", BigDecimal.ZERO);
        result.put("financingCashFlow", BigDecimal.ZERO);
        result.put("netCashFlow", BigDecimal.ZERO);

        return result;
    }

    /**
     * 生成科目余额表
     *
     * @param tenantId 租户ID
     * @param period   会计期间
     * @return 科目余额表数据
     */
    public Map<String, Object> generateAccountBalanceSheet(Long tenantId, String period) {
        String sql = """
            SELECT
                account_type,
                account_code,
                account_name,
                account_direction,
                opening_balance,
                current_debit,
                current_credit,
                ending_balance
            FROM fin_account
            WHERE tenant_id = ?
            AND status = 1
            AND is_deleted = false
            ORDER BY account_code
            """;

        List<Map<String, Object>> accounts = jdbcTemplate.queryForList(sql, tenantId);

        // 按科目类型分组
        Map<Integer, List<Map<String, Object>>> groupedAccounts = new LinkedHashMap<>();
        for (Map<String, Object> account : accounts) {
            Integer accountType = Integer.parseInt(account.get("account_type").toString());
            groupedAccounts.computeIfAbsent(accountType, k -> new ArrayList<>()).add(account);
        }

        // 计算每个类型的合计
        Map<Integer, BigDecimal> typeTotals = new HashMap<>();
        for (Map<String, Object> account : accounts) {
            Integer accountType = Integer.parseInt(account.get("account_type").toString());
            BigDecimal balance = account.get("ending_balance") != null ?
                new BigDecimal(account.get("ending_balance").toString()) : BigDecimal.ZERO;
            typeTotals.merge(accountType, balance, BigDecimal::add);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reportDate", LocalDate.now());
        result.put("period", period);
        result.put("groupedAccounts", groupedAccounts);
        result.put("typeTotals", typeTotals);
        result.put("totalAssets", typeTotals.getOrDefault(1, BigDecimal.ZERO));
        result.put("totalLiabilities", typeTotals.getOrDefault(2, BigDecimal.ZERO));
        result.put("totalEquity", typeTotals.getOrDefault(3, BigDecimal.ZERO));
        result.put("totalCost", typeTotals.getOrDefault(4, BigDecimal.ZERO));
        result.put("totalProfit", typeTotals.getOrDefault(5, BigDecimal.ZERO));

        return result;
    }

    /**
     * 按科目类型筛选
     *
     * @param accounts 科目列表
     * @param type     类型
     * @return 筛选后的列表
     */
    private List<Map<String, Object>> filterByType(List<Map<String, Object>> accounts, Integer type) {
        return accounts.stream()
                .filter(account -> type.equals(account.get("account_type")))
                .toList();
    }

    /**
     * 导出财务报表为Excel
     *
     * @param reportType 报表类型
     * @param tenantId   租户ID
     * @param period     会计期间
     * @return Excel字节数组
     */
    public byte[] exportFinancialReportToExcel(String reportType, Long tenantId, String period) {
        Map<String, Object> data;

        switch (reportType.toLowerCase()) {
            case "trial_balance":
                data = generateTrialBalance(tenantId, period);
                break;
            case "balance_sheet":
                data = generateBalanceSheet(tenantId, period);
                break;
            case "income_statement":
                data = generateIncomeStatement(tenantId, period);
                break;
            case "cash_flow":
                data = generateCashFlowStatement(tenantId, period);
                break;
            case "account_balance":
                data = generateAccountBalanceSheet(tenantId, period);
                break;
            default:
                throw new BusinessException("不支持的报表类型: " + reportType);
        }

        // 使用 ReportExportService 导出
        return exportDataToExcel(data, reportType);
    }

    /**
     * 导出数据为Excel
     *
     * @param data     报表数据
     * @param fileName 文件名
     * @return Excel字节数组
     */
    private byte[] exportDataToExcel(Map<String, Object> data, String fileName) {
        // TODO: 调用 ReportExportService
        log.info("导出报表为Excel: fileName={}", fileName);
        return new byte[0];
    }
}
