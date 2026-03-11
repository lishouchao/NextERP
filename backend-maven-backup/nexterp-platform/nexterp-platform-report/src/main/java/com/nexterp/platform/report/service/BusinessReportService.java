package com.nexterp.platform.report.service;

import com.nexterp.platform.report.domain.repository.SysReportRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 业务报表服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessReportService {

    private final SysReportRepository reportRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 生成采购分析报表
     *
     * @param tenantId 租户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 采购分析数据
     */
    public Map<String, Object> generatePurchaseAnalysisReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        // 按供应商统计采购金额
        String supplierSql = """
            SELECT
                s.id as supplier_id,
                s.supplier_code,
                s.supplier_name,
                COUNT(DISTINCT po.id) as order_count,
                COALESCE(SUM(po.total_amount), 0) as total_amount
            FROM sup_supplier s
            LEFT JOIN pur_purchase_order po ON s.id = po.supplier_id
                AND po.tenant_id = s.tenant_id
                AND po.is_deleted = false
                AND po.order_date BETWEEN ? AND ?
            WHERE s.tenant_id = ?
                AND s.is_deleted = false
                AND s.status = 1
            GROUP BY s.id, s.supplier_code, s.supplier_name
            ORDER BY total_amount DESC
            """;

        List<Map<String, Object>> supplierData = jdbcTemplate.queryForList(
            supplierSql, startDate, endDate, tenantId);

        // 按物料统计采购数量
        String materialSql = """
            SELECT
                m.id as material_id,
                m.material_code,
                m.material_name,
                COUNT(DISTINCT po.id) as order_count,
                COALESCE(SUM(pod.quantity), 0) as total_quantity
            FROM inv_material m
            LEFT JOIN pur_purchase_order_detail pod ON m.id = pod.material_id
            LEFT JOIN pur_purchase_order po ON pod.purchase_order_id = po.id
                AND po.tenant_id = m.tenant_id
                AND po.is_deleted = false
                AND po.order_date BETWEEN ? AND ?
            WHERE m.tenant_id = ?
                AND m.is_deleted = false
                AND m.status = 1
            GROUP BY m.id, m.material_code, m.material_name
            ORDER BY total_quantity DESC
            """;

        List<Map<String, Object>> materialData = jdbcTemplate.queryForList(
            materialSql, startDate, endDate, tenantId);

        // 汇总统计
        BigDecimal totalAmount = supplierData.stream()
                .map(row -> new BigDecimal(row.get("total_amount").toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("supplierData", supplierData);
        result.put("materialData", materialData);
        result.put("totalAmount", totalAmount);
        result.put("supplierCount", supplierData.size());

        return result;
    }

    /**
     * 生成销售分析报表
     *
     * @param tenantId 租户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 销售分析数据
     */
    public Map<String, Object> generateSalesAnalysisReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        // 按客户统计销售金额
        String customerSql = """
            SELECT
                c.id as customer_id,
                c.customer_code,
                c.customer_name,
                COUNT(DISTINCT so.id) as order_count,
                COALESCE(SUM(so.total_amount), 0) as total_amount
            FROM sal_customer c
            LEFT JOIN sal_sales_order so ON c.id = so.customer_id
                AND so.tenant_id = c.tenant_id
                AND so.is_deleted = false
                AND so.order_date BETWEEN ? AND ?
            WHERE c.tenant_id = ?
                AND c.is_deleted = false
                AND c.status = 1
            GROUP BY c.id, c.customer_code, c.customer_name
            ORDER BY total_amount DESC
            """;

        List<Map<String, Object>> customerData = jdbcTemplate.queryForList(
            customerSql, startDate, endDate, tenantId);

        // 按产品统计销售数量
        String productSql = """
            SELECT
                p.id as product_id,
                p.product_code,
                p.product_name,
                COUNT(DISTINCT so.id) as order_count,
                COALESCE(SUM(sod.quantity), 0) as total_quantity,
                COALESCE(SUM(sod.amount), 0) as total_amount
            FROM inv_material p
            LEFT JOIN sal_sales_order_detail sod ON p.id = sod.product_id
            LEFT JOIN sal_sales_order so ON sod.sales_order_id = so.id
                AND so.tenant_id = p.tenant_id
                AND so.is_deleted = false
                AND so.order_date BETWEEN ? AND ?
            WHERE p.tenant_id = ?
                AND p.is_deleted = false
                AND p.status = 1
            GROUP BY p.id, p.product_code, p.product_name
            ORDER BY total_amount DESC
            """;

        List<Map<String, Object>> productData = jdbcTemplate.queryForList(
            productSql, startDate, endDate, tenantId);

        // 汇总统计
        BigDecimal totalAmount = customerData.stream()
                .map(row -> new BigDecimal(row.get("total_amount").toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("customerData", customerData);
        result.put("productData", productData);
        result.put("totalAmount", totalAmount);
        result.put("customerCount", customerData.size());

        return result;
    }

    /**
     * 生成库存分析报表
     *
     * @param tenantId 租户ID
     * @return 库存分析数据
     */
    public Map<String, Object> generateInventoryAnalysisReport(Long tenantId) {
        // 库存汇总
        String summarySql = """
            SELECT
                m.id as material_id,
                m.material_code,
                m.material_name,
                m.specification,
                m.unit,
                m.min_stock,
                m.max_stock,
                m.safety_stock,
                COALESCE(inv.current_qty, 0) as current_qty,
                COALESCE(inv.available_qty, 0) as available_qty
            FROM inv_material m
            LEFT JOIN inv_inventory inv ON m.id = inv.material_id
                AND inv.tenant_id = m.tenant_id
                AND inv.is_deleted = false
            WHERE m.tenant_id = ?
                AND m.is_deleted = false
                AND m.status = 1
            ORDER BY m.material_code
            """;

        List<Map<String, Object>> inventoryData = jdbcTemplate.queryForList(summarySql, tenantId);

        // 库存预警分析
        List<Map<String, Object>> warnings = new ArrayList<>();
        List<Map<String, Object>> shortages = new ArrayList<>();
        List<Map<String, Object>> overstocks = new ArrayList<>();

        for (Map<String, Object> row : inventoryData) {
            BigDecimal currentQty = new BigDecimal(row.get("current_qty").toString());
            BigDecimal minStock = row.get("min_stock") != null ?
                new BigDecimal(row.get("min_stock").toString()) : BigDecimal.ZERO;
            BigDecimal maxStock = row.get("max_stock") != null ?
                new BigDecimal(row.get("max_stock").toString()) : BigDecimal.ZERO;
            BigDecimal safetyStock = row.get("safety_stock") != null ?
                new BigDecimal(row.get("safety_stock").toString()) : BigDecimal.ZERO;

            if (minStock.compareTo(BigDecimal.ZERO) > 0 && currentQty.compareTo(minStock) < 0) {
                row.put("warning_type", "低于最小库存");
                warnings.add(row);
                shortages.add(row);
            } else if (maxStock.compareTo(BigDecimal.ZERO) > 0 && currentQty.compareTo(maxStock) > 0) {
                row.put("warning_type", "高于最大库存");
                warnings.add(row);
                overstocks.add(row);
            } else if (safetyStock.compareTo(BigDecimal.ZERO) > 0 && currentQty.compareTo(safetyStock) < 0) {
                row.put("warning_type", "低于安全库存");
                warnings.add(row);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reportDate", LocalDate.now());
        result.put("inventoryData", inventoryData);
        result.put("warnings", warnings);
        result.put("shortages", shortages);
        result.put("overstocks", overstocks);
        result.put("totalMaterials", inventoryData.size());
        result.put("warningCount", warnings.size());

        return result;
    }

    /**
     * 生成生产分析报表
     *
     * @param tenantId 租户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 生产分析数据
     */
    public Map<String, Object> generateProductionAnalysisReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        // 按产品统计生产数量
        String productSql = """
            SELECT
                p.product_id,
                p.product_code,
                p.product_name,
                COUNT(DISTINCT po.id) as order_count,
                COALESCE(SUM(po.planned_qty), 0) as planned_qty,
                COALESCE(SUM(po.completed_qty), 0) as completed_qty
            FROM pro_production_order po
            LEFT JOIN inv_material p ON po.product_id = p.id
                AND p.tenant_id = po.tenant_id
                AND p.is_deleted = false
                AND p.plan_start_date BETWEEN ? AND ?
            WHERE po.tenant_id = ?
                AND po.is_deleted = false
                AND po.status IN (2, 3)
            GROUP BY p.product_id, p.product_code, p.product_name
            ORDER BY completed_qty DESC
            """;

        List<Map<String, Object>> productData = jdbcTemplate.queryForList(
            productSql, startDate, endDate, tenantId);

        // 工序完成率统计
        String operationSql = """
            SELECT
                op.process_id,
                op.process_code,
                op.process_name,
                COUNT(*) as record_count,
                COALESCE(AVG(op.actual_man_hours), 0) as avg_man_hours,
                COALESCE(AVG(op.actual_machine_hours), 0) as avg_machine_hours
            FROM pro_operation_record op
            WHERE op.tenant_id = ?
                AND op.actual_end_time BETWEEN ? AND ?
                AND op.is_deleted = false
            GROUP BY op.process_id, op.process_code, op.process_name
            ORDER BY record_count DESC
            """;

        List<Map<String, Object>> operationData = jdbcTemplate.queryForList(
            operationSql, tenantId, startDate, endDate);

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("productData", productData);
        result.put("operationData", operationData);

        return result;
    }

    /**
     * 生成销售业绩报表
     *
     * @param tenantId 租户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 销售业绩数据
     */
    public Map<String, Object> generateSalesPerformanceReport(Long tenantId, LocalDate startDate, LocalDate endDate) {
        // 按销售人员统计
        String sql = """
            SELECT
                e.id as employee_id,
                e.employee_no,
                e.employee_name,
                d.department_name,
                COUNT(DISTINCT so.id) as order_count,
                COALESCE(SUM(so.total_amount), 0) as total_amount,
                COALESCE(SUM(so.profit_amount), 0) as total_profit
            FROM hrm_employee e
            LEFT JOIN sal_sales_order so ON e.id = so.sales_person_id
                AND so.tenant_id = e.tenant_id
                AND so.is_deleted = false
                AND so.order_date BETWEEN ? AND ?
            LEFT JOIN hrm_department d ON e.department_id = d.id
            WHERE e.tenant_id = ?
                AND e.is_deleted = false
                AND e.work_status = 1
            GROUP BY e.id, e.employee_no, e.employee_name, d.department_name
            ORDER BY total_amount DESC
            """;

        List<Map<String, Object>> employeeData = jdbcTemplate.queryForList(
                sql, startDate, endDate, tenantId);

        // 按部门统计
        String deptSql = """
            SELECT
                d.id as department_id,
                d.dept_code,
                d.dept_name,
                COUNT(DISTINCT so.id) as order_count,
                COALESCE(SUM(so.total_amount), 0) as total_amount
            FROM hrm_department d
            LEFT JOIN hrm_employee e ON d.id = e.department_id
            LEFT JOIN sal_sales_order so ON e.id = so.sales_person_id
                AND so.tenant_id = d.tenant_id
                AND so.is_deleted = false
                AND so.order_date BETWEEN ? AND ?
            WHERE d.tenant_id = ?
                AND d.is_deleted = false
                AND d.status = 1
            GROUP BY d.id, d.dept_code, d.dept_name
            ORDER BY total_amount DESC
            """;

        List<Map<String, Object>> deptData = jdbcTemplate.queryForList(
                deptSql, startDate, endDate, tenantId);

        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("employeeData", employeeData);
        result.put("deptData", deptData);

        return result;
    }
}
