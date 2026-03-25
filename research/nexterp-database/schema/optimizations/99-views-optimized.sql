-- ============================================================================
-- NextERP 优化版 Views
-- 优化点：适配分区表、物化视图优化、LATERAL JOIN 优化
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 业务伙伴视图
-- ----------------------------------------------------------------------------

-- 客户视图（使用 LATERAL 优化当前有效记录查询）
CREATE OR REPLACE VIEW v_customer AS
SELECT
    p.id,
    p.tenant_id,
    p.partner_number,
    p.organization_name,
    p.full_name,
    p.tax_id,
    p.status,
    r.role_type,

    -- 公司代码数据（当前有效）
    cc.company_id,
    cc.payment_terms,
    cc.credit_limit,

    -- 销售范围数据（当前有效）
    cs.sales_area_id,
    cs.customer_group,
    cs.price_group,

    -- 默认地址
    a.phone,
    a.email,
    a.city,
    a.country_id

FROM bp_partner p
JOIN bp_partner_role r ON r.partner_id = p.id
    AND r.role_type = 'FLCU00'
    AND r.valid_from <= CURRENT_DATE
    AND r.valid_to >= CURRENT_DATE
-- 使用 LATERAL 获取当前有效的公司代码数据
LEFT JOIN LATERAL (
    SELECT * FROM bp_customer_company
    WHERE partner_id = p.id
      AND valid_from <= CURRENT_DATE
      AND valid_to >= CURRENT_DATE
    LIMIT 1
) cc ON TRUE
-- 使用 LATERAL 获取当前有效的销售范围数据
LEFT JOIN LATERAL (
    SELECT * FROM bp_customer_sales
    WHERE partner_id = p.id
      AND valid_from <= CURRENT_DATE
      AND valid_to >= CURRENT_DATE
    LIMIT 1
) cs ON TRUE
-- 默认地址
LEFT JOIN LATERAL (
    SELECT * FROM bp_address
    WHERE partner_id = p.id
      AND is_default = TRUE
      AND valid_from <= CURRENT_DATE
      AND valid_to >= CURRENT_DATE
    LIMIT 1
) a ON TRUE
WHERE p.status = 'ACTIVE';

COMMENT ON VIEW v_customer IS '客户视图 (参考 SAP CDS View I_Customer) - 优化版';

-- 供应商视图
CREATE OR REPLACE VIEW v_supplier AS
SELECT
    p.id,
    p.tenant_id,
    p.partner_number,
    p.organization_name,
    p.full_name,
    p.tax_id,
    p.status,
    r.role_type,

    -- 公司代码数据（当前有效）
    sc.company_id,
    sc.payment_terms,

    -- 采购组织数据（当前有效）
    sp.purchasing_org_id,
    sp.supplier_group,
    sp.quality_score,
    sp.delivery_score,
    sp.price_score,
    sp.overall_score,

    -- 默认地址
    a.phone,
    a.email,
    a.city

FROM bp_partner p
JOIN bp_partner_role r ON r.partner_id = p.id
    AND r.role_type = 'FLVN00'
    AND r.valid_from <= CURRENT_DATE
    AND r.valid_to >= CURRENT_DATE
LEFT JOIN LATERAL (
    SELECT * FROM bp_supplier_company
    WHERE partner_id = p.id
      AND valid_from <= CURRENT_DATE
      AND valid_to >= CURRENT_DATE
    LIMIT 1
) sc ON TRUE
LEFT JOIN LATERAL (
    SELECT * FROM bp_supplier_purchasing
    WHERE partner_id = p.id
      AND valid_from <= CURRENT_DATE
      AND valid_to >= CURRENT_DATE
    LIMIT 1
) sp ON TRUE
LEFT JOIN LATERAL (
    SELECT * FROM bp_address
    WHERE partner_id = p.id
      AND is_default = TRUE
      AND valid_from <= CURRENT_DATE
      AND valid_to >= CURRENT_DATE
    LIMIT 1
) a ON TRUE
WHERE p.status = 'ACTIVE';

COMMENT ON VIEW v_supplier IS '供应商视图 (参考 SAP CDS View I_Supplier) - 优化版';

-- ----------------------------------------------------------------------------
-- 2. 财务视图
-- ----------------------------------------------------------------------------

-- 科目余额视图（适配独立字段而非数组）
CREATE OR REPLACE VIEW v_account_balance AS
SELECT
    b.id,
    b.tenant_id,
    b.company_id,
    c.code AS company_code,
    c.name AS company_name,
    b.account_id,
    a.account_number,
    a.name AS account_name,
    a.account_type,
    b.fiscal_year,
    b.currency_id,
    cur.code AS currency_code,

    -- 期间余额（独立字段）
    b.period_01_balance, b.period_01_debit, b.period_01_credit,
    b.period_02_balance, b.period_02_debit, b.period_02_credit,
    b.period_03_balance, b.period_03_debit, b.period_03_credit,
    b.period_04_balance, b.period_04_debit, b.period_04_credit,
    b.period_05_balance, b.period_05_debit, b.period_05_credit,
    b.period_06_balance, b.period_06_debit, b.period_06_credit,
    b.period_07_balance, b.period_07_debit, b.period_07_credit,
    b.period_08_balance, b.period_08_debit, b.period_08_credit,
    b.period_09_balance, b.period_09_debit, b.period_09_credit,
    b.period_10_balance, b.period_10_debit, b.period_10_credit,
    b.period_11_balance, b.period_11_debit, b.period_11_credit,
    b.period_12_balance, b.period_12_debit, b.period_12_credit,

    -- 年度累计
    b.year_balance,
    b.year_debit,
    b.year_credit

FROM fi_account_balance b
JOIN sys_company c ON c.id = b.company_id
JOIN fi_gl_account a ON a.id = b.account_id
LEFT JOIN core_currency cur ON cur.id = b.currency_id;

COMMENT ON VIEW v_account_balance IS '科目余额视图 - 适配独立字段';

-- 凭证明细视图（适配分区表）
CREATE OR REPLACE VIEW v_journal_entry AS
SELECT
    h.id,
    h.fiscal_year,
    h.tenant_id,
    h.company_id,
    c.code AS company_code,
    h.document_number,
    h.document_date,
    h.posting_date,
    h.period,
    h.currency_id,
    h.header_text,
    h.status,

    -- 行项目
    i.line_item,
    i.account_number,
    a.name AS account_name,
    i.debit_credit,
    i.amount,
    i.amount_dc,
    i.item_text,

    -- 业务伙伴
    i.partner_id,
    p.full_name AS partner_name,

    -- 成本对象
    i.cost_center_id,
    cc.code AS cost_center_code,
    cc.name AS cost_center_name

FROM fi_journal_entry_hdr h
JOIN fi_journal_entry_itm i ON i.header_id = h.id AND i.fiscal_year = h.fiscal_year
JOIN sys_company c ON c.id = h.company_id
LEFT JOIN fi_gl_account a ON a.account_number = i.account_number
LEFT JOIN bp_partner p ON p.id = i.partner_id
LEFT JOIN sys_cost_center cc ON cc.id = i.cost_center_id;

COMMENT ON VIEW v_journal_entry IS '凭证明细视图 - 适配分区表';

-- ----------------------------------------------------------------------------
-- 3. 物料管理视图
-- ----------------------------------------------------------------------------

-- 物料库存视图（含可用库存）
CREATE OR REPLACE VIEW v_material_stock AS
SELECT
    ms.id,
    ms.tenant_id,
    m.id AS material_id,
    m.material_number,
    m.name AS material_name,
    mt.code AS material_type,
    mg.code AS material_group,
    m.base_uom_id,

    -- 库存地点
    sl.plant_id,
    p.code AS plant_code,
    p.name AS plant_name,
    sl.id AS storage_location_id,
    sl.code AS storage_location_code,
    sl.name AS storage_location_name,

    -- 库存数量
    ms.unrestricted_stock,
    ms.quality_stock,
    ms.blocked_stock,
    ms.in_transit_stock,
    ms.available_stock,
    (ms.unrestricted_stock + ms.quality_stock + ms.blocked_stock + ms.in_transit_stock) AS total_stock,

    -- 价值
    ms.unrestricted_value,
    CASE WHEN ms.unrestricted_stock > 0
        THEN ms.unrestricted_value / ms.unrestricted_stock
        ELSE 0 END AS unit_price

FROM mm_material_storage ms
JOIN mm_material m ON m.id = ms.material_id
JOIN sys_storage_location sl ON sl.id = ms.storage_location_id
JOIN sys_plant p ON p.id = sl.plant_id
LEFT JOIN mm_material_type mt ON mt.id = m.material_type_id
LEFT JOIN mm_material_group mg ON mg.id = m.material_group_id;

COMMENT ON VIEW v_material_stock IS '物料库存视图 - 含可用库存';

-- 采购订单视图（适配分区表）
CREATE OR REPLACE VIEW v_purchase_order AS
SELECT
    h.id,
    h.fiscal_year,
    h.tenant_id,
    h.po_number,
    h.document_date,
    h.supplier_id,
    s.organization_name AS supplier_name,
    h.currency_id,
    h.total_amount,
    h.status,
    h.approval_status,

    -- 行项目
    i.line_item,
    i.material_number,
    i.description,
    i.plant_id,
    p.code AS plant_code,
    i.quantity,
    i.quantity_delivered,
    i.open_quantity,
    i.uom_id,
    i.price,
    i.net_amount

FROM mm_purchase_order_hdr h
LEFT JOIN mm_purchase_order_itm i ON i.header_id = h.id AND i.fiscal_year = h.fiscal_year
LEFT JOIN bp_partner s ON s.id = h.supplier_id
LEFT JOIN sys_plant p ON p.id = i.plant_id;

COMMENT ON VIEW v_purchase_order IS '采购订单视图 - 适配分区表';

-- 库存快照视图
CREATE OR REPLACE VIEW v_inventory_snapshot AS
SELECT
    s.id,
    s.tenant_id,
    s.snapshot_year,
    s.snapshot_month,
    s.snapshot_date,
    m.material_number,
    m.name AS material_name,
    p.code AS plant_code,
    p.name AS plant_name,
    sl.code AS storage_location_code,
    sl.name AS storage_location_name,
    s.unrestricted_stock,
    s.quality_stock,
    s.blocked_stock,
    s.in_transit_stock,
    s.stock_value,
    s.receipt_qty,
    s.issue_qty,
    cur.code AS currency_code
FROM mm_inventory_snapshot s
JOIN mm_material m ON m.id = s.material_id
JOIN sys_plant p ON p.id = s.plant_id
LEFT JOIN sys_storage_location sl ON sl.id = s.storage_location_id
LEFT JOIN core_currency cur ON cur.id = s.currency_id;

COMMENT ON VIEW v_inventory_snapshot IS '库存快照视图';

-- ----------------------------------------------------------------------------
-- 4. HR 视图
-- ----------------------------------------------------------------------------

-- 员工视图（使用优化后的综合视图）
CREATE OR REPLACE VIEW v_employee AS
SELECT
    e.id,
    e.tenant_id,
    e.employee_number,
    e.full_name,
    e.gender,
    e.birth_date,
    e.email,
    e.phone,
    e.employee_status,
    e.hire_date,
    e.seniority,

    -- 组织信息
    e.org_unit_id,
    e.org_unit_name,
    e.position_id,
    e.position_name,
    e.job_id,
    e.job_name,
    e.manager_id,
    e.manager_name,

    -- 薪资
    e.salary_currency,
    e.pay_grade,
    e.pay_level,
    e.current_gross_salary,
    e.current_net_salary

FROM v_hr_employee_full e;

COMMENT ON VIEW v_employee IS '员工视图 (参考 SAP CDS View I_Employee) - 优化版';

-- 组织架构视图
CREATE OR REPLACE VIEW v_org_structure AS
SELECT
    o.id,
    o.tenant_id,
    o.org_code,
    o.name,
    o.org_type,
    o.parent_id,
    p.name AS parent_name,
    o.level,
    o.manager_id,
    e.full_name AS manager_name,
    o.headcount,
    o.max_headcount,
    o.status,

    -- 职位统计
    (SELECT COUNT(*) FROM hr_position pos
     WHERE pos.org_unit_id = o.id
       AND pos.position_status = 'FI'
       AND pos.valid_from <= CURRENT_DATE
       AND pos.valid_to >= CURRENT_DATE) AS filled_positions,
    (SELECT COUNT(*) FROM hr_position pos
     WHERE pos.org_unit_id = o.id
       AND pos.position_status = 'VA'
       AND pos.valid_from <= CURRENT_DATE
       AND pos.valid_to >= CURRENT_DATE) AS vacant_positions

FROM hr_org_unit o
LEFT JOIN hr_org_unit p ON p.id = o.parent_id
LEFT JOIN hr_employee e ON e.id = o.manager_id
WHERE o.valid_from <= CURRENT_DATE
  AND o.valid_to >= CURRENT_DATE;

COMMENT ON VIEW v_org_structure IS '组织架构视图 - 优化版';

-- 薪酬汇总视图（适配分区表和独立工资项）
CREATE OR REPLACE VIEW v_payroll_summary AS
SELECT
    r.id,
    r.fiscal_year AS payroll_year,
    r.tenant_id,
    r.employee_id,
    e.employee_number,
    e.full_name,
    r.payroll_period,
    r.payroll_month,
    r.gross_pay,
    r.total_deduction,
    r.net_pay,
    r.currency_id,
    r.status,

    -- 工资项汇总
    COALESCE(SUM(CASE WHEN wt.code = '4000' THEN pi.amount ELSE 0 END), 0) AS pension_personal,
    COALESCE(SUM(CASE WHEN wt.code = '4001' THEN pi.amount ELSE 0 END), 0) AS medical_personal,
    COALESCE(SUM(CASE WHEN wt.code = '4002' THEN pi.amount ELSE 0 END), 0) AS unemployment_personal,
    COALESCE(SUM(CASE WHEN wt.code = '4003' THEN pi.amount ELSE 0 END), 0) AS housing_fund_personal,
    COALESCE(SUM(CASE WHEN wt.code = '4004' THEN pi.amount ELSE 0 END), 0) AS income_tax

FROM hr_payroll_result r
JOIN hr_employee e ON e.id = r.employee_id
LEFT JOIN hr_payroll_item pi ON pi.payroll_result_id = r.id
LEFT JOIN hr_wage_type wt ON wt.id = pi.wage_type_id
GROUP BY r.id, r.fiscal_year, r.tenant_id, r.employee_id, e.employee_number, e.full_name,
         r.payroll_period, r.payroll_month, r.gross_pay, r.total_deduction, r.net_pay,
         r.currency_id, r.status;

COMMENT ON VIEW v_payroll_summary IS '薪酬汇总视图 - 适配分区表';

-- ----------------------------------------------------------------------------
-- 5. 物化视图（报表优化）
-- ----------------------------------------------------------------------------

-- 客户余额物化视图
CREATE MATERIALIZED VIEW mv_customer_balance AS
SELECT
    p.id AS partner_id,
    p.tenant_id,
    pb.company_id,
    c.code AS company_code,
    p.partner_number,
    p.organization_name,
    pb.fiscal_year,
    pb.currency_id,
    pb.year_balance,
    pb.year_debit,
    pb.year_credit
FROM bp_partner p
JOIN bp_partner_role r ON r.partner_id = p.id
    AND r.role_type = 'FLCU00'
    AND r.valid_from <= CURRENT_DATE
    AND r.valid_to >= CURRENT_DATE
JOIN fi_partner_balance pb ON pb.partner_id = p.id
JOIN sys_company c ON c.id = pb.company_id
WHERE p.status = 'ACTIVE';

CREATE UNIQUE INDEX idx_mv_customer_balance ON mv_customer_balance (partner_id, company_id, fiscal_year);
CREATE INDEX idx_mv_customer_balance_tenant ON mv_customer_balance (tenant_id);

COMMENT ON MATERIALIZED VIEW mv_customer_balance IS '客户余额物化视图 - 优化版';

-- 供应商余额物化视图
CREATE MATERIALIZED VIEW mv_supplier_balance AS
SELECT
    p.id AS partner_id,
    p.tenant_id,
    pb.company_id,
    c.code AS company_code,
    p.partner_number,
    p.organization_name,
    pb.fiscal_year,
    pb.currency_id,
    pb.year_balance,
    pb.year_debit,
    pb.year_credit
FROM bp_partner p
JOIN bp_partner_role r ON r.partner_id = p.id
    AND r.role_type = 'FLVN00'
    AND r.valid_from <= CURRENT_DATE
    AND r.valid_to >= CURRENT_DATE
JOIN fi_partner_balance pb ON pb.partner_id = p.id
JOIN sys_company c ON c.id = pb.company_id
WHERE p.status = 'ACTIVE';

CREATE UNIQUE INDEX idx_mv_supplier_balance ON mv_supplier_balance (partner_id, company_id, fiscal_year);
CREATE INDEX idx_mv_supplier_balance_tenant ON mv_supplier_balance (tenant_id);

COMMENT ON MATERIALIZED VIEW mv_supplier_balance IS '供应商余额物化视图 - 优化版';

-- 库存价值物化视图
CREATE MATERIALIZED VIEW mv_inventory_value AS
SELECT
    ms.tenant_id,
    m.material_id,
    m.material_number,
    m.name AS material_name,
    ms.plant_id,
    p.code AS plant_code,
    p.name AS plant_name,
    SUM(ms.unrestricted_stock) AS total_unrestricted,
    SUM(ms.quality_stock) AS total_quality,
    SUM(ms.blocked_stock) AS total_blocked,
    SUM(ms.unrestricted_value) AS total_value,
    mv.currency_id
FROM mm_material_storage ms
JOIN v_material_stock m ON m.id = ms.material_id
JOIN sys_plant p ON p.id = ms.plant_id
LEFT JOIN mm_material_valuation mv ON mv.material_id = ms.material_id
    AND mv.plant_id = ms.plant_id
    AND mv.valid_from <= CURRENT_DATE
    AND mv.valid_to >= CURRENT_DATE
GROUP BY ms.tenant_id, m.material_id, m.material_number, m.name, ms.plant_id, p.code, p.name, mv.currency_id;

CREATE UNIQUE INDEX idx_mv_inventory_value ON mv_inventory_value (material_id, plant_id);
CREATE INDEX idx_mv_inventory_value_tenant ON mv_inventory_value (tenant_id);

COMMENT ON MATERIALIZED VIEW mv_inventory_value IS '库存价值物化视图';

-- ----------------------------------------------------------------------------
-- 6. 刷新物化视图函数
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION refresh_materialized_views()
RETURNS VOID AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_customer_balance;
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_supplier_balance;
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_inventory_value;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION refresh_materialized_views IS '刷新所有物化视图';

-- 定时刷新物化视图（需要 pg_cron 扩展）
-- SELECT cron.schedule('refresh_mviews', '0 2 * * *', 'SELECT refresh_materialized_views()');
