-- ============================================================================
-- NextERP Views
-- 常用视图定义 (借鉴 SAP CDS View 概念)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 业务伙伴视图
-- ----------------------------------------------------------------------------

-- 客户视图
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

    -- 公司代码数据
    cc.company_id,
    cc.payment_terms,
    cc.credit_limit,

    -- 销售范围数据
    cs.sales_area_id,
    cs.customer_group,
    cs.price_group,

    -- 联系信息
    a.phone,
    a.email,
    a.city,
    a.country_id

FROM bp_partner p
JOIN bp_partner_role r ON r.partner_id = p.id AND r.role_type = 'FLCU00'
LEFT JOIN bp_customer_company cc ON cc.partner_id = p.id
LEFT JOIN bp_customer_sales cs ON cs.partner_id = p.id
LEFT JOIN bp_address a ON a.partner_id = p.id AND a.is_default = TRUE
WHERE p.status = 'ACTIVE';

COMMENT ON VIEW v_customer IS '客户视图 (参考 SAP CDS View I_Customer)';

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

    -- 公司代码数据
    sc.company_id,
    sc.payment_terms,

    -- 采购组织数据
    sp.purchasing_org_id,
    sp.supplier_group,
    sp.quality_score,

    -- 联系信息
    a.phone,
    a.email,
    a.city

FROM bp_partner p
JOIN bp_partner_role r ON r.partner_id = p.id AND r.role_type = 'FLVN00'
LEFT JOIN bp_supplier_company sc ON sc.partner_id = p.id
LEFT JOIN bp_supplier_purchasing sp ON sp.partner_id = p.id
LEFT JOIN bp_address a ON a.partner_id = p.id AND a.is_default = TRUE
WHERE p.status = 'ACTIVE';

COMMENT ON VIEW v_supplier IS '供应商视图 (参考 SAP CDS View I_Supplier)';

-- ----------------------------------------------------------------------------
-- 财务视图
-- ----------------------------------------------------------------------------

-- 科目余额视图
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

    -- 期间余额
    b.period_balance[1] AS period_01,
    b.period_balance[2] AS period_02,
    b.period_balance[3] AS period_03,
    b.period_balance[4] AS period_04,
    b.period_balance[5] AS period_05,
    b.period_balance[6] AS period_06,
    b.period_balance[7] AS period_07,
    b.period_balance[8] AS period_08,
    b.period_balance[9] AS period_09,
    b.period_balance[10] AS period_10,
    b.period_balance[11] AS period_11,
    b.period_balance[12] AS period_12,

    -- 年度累计
    b.year_balance,
    b.year_debit,
    b.year_credit

FROM fi_account_balance b
JOIN sys_company c ON c.id = b.company_id
JOIN fi_gl_account a ON a.id = b.account_id
LEFT JOIN core_currency cur ON cur.id = b.currency_id;

COMMENT ON VIEW v_account_balance IS '科目余额视图';

-- 凭证明细视图
CREATE OR REPLACE VIEW v_journal_entry AS
SELECT
    h.id,
    h.tenant_id,
    h.company_id,
    c.code AS company_code,
    h.document_number,
    h.fiscal_year,
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
JOIN fi_journal_entry_itm i ON i.header_id = h.id
JOIN sys_company c ON c.id = h.company_id
LEFT JOIN fi_gl_account a ON a.account_number = i.account_number
LEFT JOIN bp_partner p ON p.id = i.partner_id
LEFT JOIN sys_cost_center cc ON cc.id = i.cost_center_id;

COMMENT ON VIEW v_journal_entry IS '凭证明细视图';

-- ----------------------------------------------------------------------------
-- 物料管理视图
-- ----------------------------------------------------------------------------

-- 物料库存视图
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

COMMENT ON VIEW v_material_stock IS '物料库存视图';

-- 采购订单视图
CREATE OR REPLACE VIEW v_purchase_order AS
SELECT
    h.id,
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
    i.quantity - i.quantity_delivered AS open_quantity,
    i.uom_id,
    i.price,
    i.net_amount

FROM mm_purchase_order_hdr h
LEFT JOIN mm_purchase_order_itm i ON i.header_id = h.id
LEFT JOIN bp_partner s ON s.id = h.supplier_id
LEFT JOIN sys_plant p ON p.id = i.plant_id;

COMMENT ON VIEW v_purchase_order IS '采购订单视图';

-- ----------------------------------------------------------------------------
-- HR 视图
-- ----------------------------------------------------------------------------

-- 员工视图
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
    o.org_code,
    o.name AS org_unit_name,
    e.position_id,
    pos.position_code,
    pos.name AS position_name,
    e.job_id,
    j.job_code,
    j.name AS job_name,
    j.job_category,

    -- 薪资 (当前有效)
    pay.currency_id,
    pay.total_amount AS current_salary

FROM hr_employee e
LEFT JOIN hr_org_unit o ON o.id = e.org_unit_id
LEFT JOIN hr_position pos ON pos.id = e.position_id
LEFT JOIN hr_job j ON j.id = e.job_id
LEFT JOIN hr_it0008_basic_pay pay ON pay.employee_id = e.id
    AND pay.valid_from <= CURRENT_DATE
    AND pay.valid_to >= CURRENT_DATE;

COMMENT ON VIEW v_employee IS '员工视图 (参考 SAP CDS View I_Employee)';

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
    (SELECT COUNT(*) FROM hr_position pos WHERE pos.org_unit_id = o.id AND pos.position_status = 'FI') AS filled_positions,
    (SELECT COUNT(*) FROM hr_position pos WHERE pos.org_unit_id = o.id AND pos.position_status = 'VA') AS vacant_positions

FROM hr_org_unit o
LEFT JOIN hr_org_unit p ON p.id = o.parent_id
LEFT JOIN hr_employee e ON e.id = o.manager_id;

COMMENT ON VIEW v_org_structure IS '组织架构视图';

-- 薪酬汇总视图
CREATE OR REPLACE VIEW v_payroll_summary AS
SELECT
    r.id,
    r.tenant_id,
    r.employee_id,
    e.employee_number,
    e.full_name,
    r.payroll_period,
    r.payroll_year,
    r.payroll_month,
    r.gross_pay,
    r.total_deduction,
    r.net_pay,
    r.currency_id,
    r.status,

    -- 扣款明细 (从 JSONB 提取)
    r.payroll_items->>'pension_personal' AS pension_personal,
    r.payroll_items->>'medical_personal' AS medical_personal,
    r.payroll_items->>'unemployment_personal' AS unemployment_personal,
    r.payroll_items->>'housing_fund_personal' AS housing_fund_personal,
    r.payroll_items->>'income_tax' AS income_tax

FROM hr_payroll_result r
JOIN hr_employee e ON e.id = r.employee_id;

COMMENT ON VIEW v_payroll_summary IS '薪酬汇总视图';

-- 请假余额视图
CREATE OR REPLACE VIEW v_leave_balance AS
SELECT
    q.id,
    q.tenant_id,
    q.employee_id,
    e.employee_number,
    e.full_name,
    q.leave_type_id,
    lt.code AS leave_type_code,
    lt.name AS leave_type_name,
    q.quota_year,
    q.opening_balance,
    q.accrued,
    q.used,
    q.balance,
    q.expire_date

FROM hr_leave_quota q
JOIN hr_employee e ON e.id = q.employee_id
JOIN hr_leave_type lt ON lt.id = q.leave_type_id;

COMMENT ON VIEW v_leave_balance IS '请假余额视图';

-- ----------------------------------------------------------------------------
-- 报表视图 (物化视图)
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
JOIN bp_partner_role r ON r.partner_id = p.id AND r.role_type = 'FLCU00'
JOIN fi_partner_balance pb ON pb.partner_id = p.id
JOIN sys_company c ON c.id = pb.company_id
WHERE p.status = 'ACTIVE';

CREATE UNIQUE INDEX idx_mv_customer_balance ON mv_customer_balance (partner_id, company_id, fiscal_year);

COMMENT ON MATERIALIZED VIEW mv_customer_balance IS '客户余额物化视图';

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
JOIN bp_partner_role r ON r.partner_id = p.id AND r.role_type = 'FLVN00'
JOIN fi_partner_balance pb ON pb.partner_id = p.id
JOIN sys_company c ON c.id = pb.company_id
WHERE p.status = 'ACTIVE';

CREATE UNIQUE INDEX idx_mv_supplier_balance ON mv_supplier_balance (partner_id, company_id, fiscal_year);

COMMENT ON MATERIALIZED VIEW mv_supplier_balance IS '供应商余额物化视图';

-- 刷新物化视图的函数
CREATE OR REPLACE FUNCTION refresh_materialized_views()
RETURNS VOID AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_customer_balance;
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_supplier_balance;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION refresh_materialized_views IS '刷新所有物化视图';
