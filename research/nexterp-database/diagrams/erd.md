# NextERP 数据库 ER 图

## 核心模块关系图

```mermaid
erDiagram
    %% 租户和组织
    sys_tenant ||--o{ sys_company : "owns"
    sys_tenant ||--o{ bp_partner : "owns"
    sys_tenant ||--o{ mm_material : "owns"
    sys_tenant ||--o{ hr_employee : "owns"

    sys_company ||--o{ sys_plant : "has"
    sys_company ||--o{ sys_sales_organization : "has"
    sys_company ||--o{ sys_purchasing_organization : "has"

    sys_plant ||--o{ sys_storage_location : "has"

    %% 业务伙伴
    bp_partner ||--o{ bp_partner_role : "has"
    bp_partner ||--o{ bp_address : "has"
    bp_partner ||--o{ bp_bank_account : "has"
    bp_partner ||--o{ bp_customer_company : "customer"
    bp_partner ||--o{ bp_supplier_company : "supplier"

    %% 财务
    fi_chart_of_accounts ||--o{ fi_gl_account : "contains"
    fi_gl_account ||--o{ fi_account_balance : "has"

    sys_company ||--o{ fi_journal_entry_hdr : "posts"
    fi_journal_entry_hdr ||--o{ fi_journal_entry_itm : "contains"
    fi_journal_entry_itm }o--|| fi_gl_account : "posts to"
    fi_journal_entry_itm }o--o| bp_partner : "references"

    bp_partner ||--o{ fi_partner_balance : "has"

    %% 物料管理
    mm_material ||--o{ mm_material_plant : "extends"
    mm_material ||--o{ mm_material_valuation : "valued"
    mm_material ||--o{ mm_material_storage : "stored"

    mm_purchase_order_hdr ||--o{ mm_purchase_order_itm : "contains"
    mm_purchase_order_itm }o--o| mm_material : "orders"
    mm_purchase_order_hdr }o--|| bp_partner : "from supplier"

    mm_material_document_hdr ||--o{ mm_material_document_itm : "contains"
    mm_material_document_itm }o--|| mm_material : "moves"

    %% HR
    hr_org_unit ||--o{ hr_org_unit : "parent of"
    hr_org_unit ||--o{ hr_position : "has"
    hr_job ||--o{ hr_position : "defines"
    hr_position ||--o| hr_employee : "held by"

    hr_employee ||--o{ hr_it0001_org_assignment : "IT0001"
    hr_employee ||--o{ hr_it0002_personal_data : "IT0002"
    hr_employee ||--o{ hr_it0008_basic_pay : "IT0008"
    hr_employee ||--o{ hr_it2001_absence : "IT2001"
    hr_employee ||--o{ hr_payroll_result : "paid"
```

## 业务伙伴模型

```mermaid
erDiagram
    bp_partner {
        UUID id PK
        UUID tenant_id FK
        VARCHAR partner_number
        VARCHAR partner_type "1=Org,2=Person"
        VARCHAR organization_name
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR full_name
        VARCHAR tax_id
        DATE valid_from
        DATE valid_to
    }

    bp_partner_role {
        UUID id PK
        UUID partner_id FK
        VARCHAR role_type "FLCU00=Customer,FLVN00=Supplier"
        DATE valid_from
        DATE valid_to
    }

    bp_address {
        UUID id PK
        UUID partner_id FK
        VARCHAR address_type
        VARCHAR street
        VARCHAR city
        VARCHAR postal_code
        VARCHAR country_id
        BOOLEAN is_default
    }

    bp_customer_company {
        UUID id PK
        UUID partner_id FK
        UUID company_id FK
        VARCHAR payment_terms
        DECIMAL credit_limit
    }

    bp_customer_sales {
        UUID id PK
        UUID partner_id FK
        UUID sales_area_id FK
        VARCHAR customer_group
        VARCHAR price_group
    }

    bp_supplier_company {
        UUID id PK
        UUID partner_id FK
        UUID company_id FK
        VARCHAR payment_terms
    }

    bp_supplier_purchasing {
        UUID id PK
        UUID partner_id FK
        UUID purchasing_org_id FK
        VARCHAR supplier_group
        DECIMAL quality_score
    }

    bp_partner ||--o{ bp_partner_role : ""
    bp_partner ||--o{ bp_address : ""
    bp_partner ||--o{ bp_customer_company : ""
    bp_partner ||--o{ bp_customer_sales : ""
    bp_partner ||--o{ bp_supplier_company : ""
    bp_partner ||--o{ bp_supplier_purchasing : ""
```

## 财务会计模型

```mermaid
erDiagram
    fi_gl_account {
        UUID id PK
        UUID tenant_id FK
        UUID chart_of_accounts_id FK
        UUID company_id FK
        VARCHAR account_number
        VARCHAR name
        VARCHAR account_type "A,L,E,I,X"
        BOOLEAN is_pl_account
        BOOLEAN is_postable
        DATE valid_from
        DATE valid_to
    }

    fi_journal_entry_hdr {
        UUID id PK
        UUID tenant_id FK
        UUID company_id FK
        VARCHAR document_number
        INTEGER fiscal_year
        DATE document_date
        DATE posting_date
        UUID currency_id FK
        VARCHAR status
    }

    fi_journal_entry_itm {
        UUID id PK
        UUID header_id FK
        INTEGER line_item
        UUID account_id FK
        UUID partner_id FK
        VARCHAR debit_credit "D,C"
        DECIMAL amount
        DECIMAL amount_dc
        UUID cost_center_id FK
        VARCHAR item_text
    }

    fi_account_balance {
        UUID id PK
        UUID company_id FK
        UUID account_id FK
        INTEGER fiscal_year
        UUID currency_id FK
        DECIMAL period_balance
        DECIMAL year_balance
    }

    fi_journal_entry_hdr ||--o{ fi_journal_entry_itm : "contains"
    fi_gl_account ||--o{ fi_journal_entry_itm : "debited/credited"
    fi_gl_account ||--o{ fi_account_balance : "balance"
```

## HR 信息类型模型

```mermaid
erDiagram
    hr_employee {
        UUID id PK
        UUID tenant_id FK
        VARCHAR employee_number
        VARCHAR full_name
        VARCHAR gender
        DATE birth_date
        DATE hire_date
        UUID org_unit_id FK
        UUID position_id FK
        UUID job_id FK
    }

    hr_it0001_org_assignment {
        UUID employee_id PK
        DATE valid_from PK
        UUID org_unit_id FK
        UUID position_id FK
        UUID job_id FK
        UUID cost_center_id FK
        UUID manager_id FK
    }

    hr_it0002_personal_data {
        UUID employee_id PK
        DATE valid_from PK
        VARCHAR last_name
        VARCHAR first_name
        VARCHAR gender
        DATE birth_date
        VARCHAR id_number
    }

    hr_it0008_basic_pay {
        UUID employee_id PK
        DATE valid_from PK
        VARCHAR pay_grade
        UUID currency_id FK
        JSONB wage_items
        DECIMAL total_amount
    }

    hr_it2001_absence {
        UUID employee_id PK
        VARCHAR subtype PK
        DATE valid_from PK
        UUID leave_type_id FK
        DECIMAL days
        DECIMAL hours
        VARCHAR approval_status
    }

    hr_payroll_result {
        UUID id PK
        UUID employee_id FK
        VARCHAR payroll_period
        JSONB payroll_items
        DECIMAL gross_pay
        DECIMAL net_pay
    }

    hr_employee ||--o{ hr_it0001_org_assignment : "IT0001"
    hr_employee ||--o{ hr_it0002_personal_data : "IT0002"
    hr_employee ||--o{ hr_it0008_basic_pay : "IT0008"
    hr_employee ||--o{ hr_it2001_absence : "IT2001"
    hr_employee ||--o{ hr_payroll_result : "payroll"
```

## 组织架构模型

```mermaid
erDiagram
    hr_org_unit {
        UUID id PK
        UUID tenant_id FK
        VARCHAR org_code
        VARCHAR name
        VARCHAR org_type
        UUID parent_id FK
        UUID manager_id FK
        UUID cost_center_id FK
        INTEGER headcount
    }

    hr_job {
        UUID id PK
        UUID tenant_id FK
        VARCHAR job_code
        VARCHAR name
        VARCHAR job_category "M,P,S,O"
        VARCHAR job_grade
    }

    hr_position {
        UUID id PK
        UUID tenant_id FK
        VARCHAR position_code
        VARCHAR name
        UUID job_id FK
        UUID org_unit_id FK
        UUID holder_id FK
        UUID cost_center_id FK
        VARCHAR position_status "VA,FI,FR,AB"
    }

    hr_org_unit ||--o{ hr_org_unit : "parent"
    hr_org_unit ||--o{ hr_position : "contains"
    hr_job ||--o{ hr_position : "defines"
    hr_position ||--o| hr_employee : "held by"
```

## 采购模型

```mermaid
erDiagram
    mm_material {
        UUID id PK
        UUID tenant_id FK
        VARCHAR material_number
        VARCHAR name
        UUID material_type_id FK
        UUID material_group_id FK
        UUID base_uom_id FK
    }

    mm_purchase_order_hdr {
        UUID id PK
        UUID tenant_id FK
        UUID company_id FK
        VARCHAR po_number
        UUID supplier_id FK
        DATE document_date
        UUID currency_id FK
        DECIMAL total_amount
    }

    mm_purchase_order_itm {
        UUID id PK
        UUID header_id FK
        INTEGER line_item
        UUID material_id FK
        UUID plant_id FK
        DECIMAL quantity
        UUID uom_id FK
        DECIMAL price
        DECIMAL net_amount
    }

    mm_material_document_hdr {
        UUID id PK
        UUID tenant_id FK
        VARCHAR document_number
        DATE posting_date
    }

    mm_material_document_itm {
        UUID id PK
        UUID header_id FK
        INTEGER line_item
        UUID material_id FK
        UUID movement_type_id FK
        DECIMAL quantity
        VARCHAR debit_credit
    }

    mm_purchase_order_hdr ||--o{ mm_purchase_order_itm : "contains"
    mm_purchase_order_itm }o--o| mm_material : "orders"
    mm_material_document_hdr ||--o{ mm_material_document_itm : "contains"
    mm_material_document_itm }o--|| mm_material : "moves"
    mm_material_document_itm }o--o| mm_purchase_order_itm : "references"
```

## 表统计

| 模块 | 表数量 | 主要表 |
|------|--------|--------|
| Core | 8 | core_currency, core_uom, core_country |
| Tenant | 12 | sys_tenant, sys_company, sys_plant |
| BP | 10 | bp_partner, bp_address, bp_customer_company |
| FI/CO | 10 | fi_gl_account, fi_journal_entry_hdr |
| MM | 14 | mm_material, mm_purchase_order_hdr |
| HR | 18 | hr_employee, hr_it0001, hr_payroll_result |
| **总计** | **72** | |
