# SAP ERP 数据库设计研究项目

本项目旨在研究和复现 SAP ERP 系统的数据库设计，涵盖 ECC 6.0 和 S/4HANA 两个主要版本。

## 项目目标

1. **复现 SAP 数据库模式** - 研究并文档化 SAP 核心模块的表结构
2. **版本对比** - 分析 ECC 和 S/4HANA 之间的数据库变化
3. **最佳实践参考** - 为 NextERP 提供企业级 ERP 数据建模参考

## 研究方法

### 数据来源
- SAP 官方文档 (SAP Help Portal)
- SAP 表结构参考网站 (LeanX.eu, SAPStack.com)
- SAP S/4HANA 迁移指南
- SAP ABAP 数据字典参考

### 研究范围
- 核心业务表结构
- 主数据表 (Master Data)
- 交易数据表 (Transactional Data)
- 配置表 (Customizing Tables)
- 索引和数据库对象

## 目录结构

```
sap-database/
├── common/                    # 通用概念和跨模块表
│   ├── README.md
│   ├── client-dependent.md    # 集团相关概念
│   └── number-ranges.md       # 号码范围
├── ecc/                       # SAP ECC 6.0 数据库设计
│   ├── fi-co/                # 财务会计/控制
│   ├── mm/                   # 物料管理
│   ├── sd/                   # 销售分销
│   ├── hr/                   # 人力资源 (HCM)
│   ├── pp/                   # 生产计划
│   ├── ps/                   # 项目系统
│   ├── pm/                   # 工厂维护
│   ├── qm/                   # 质量管理
│   └── cs/                   # 客户服务
├── s4hana/                    # SAP S/4HANA 数据库设计
│   ├── fi-co/
│   ├── mm/
│   ├── sd/
│   ├── hr/
│   ├── pp/
│   ├── ps/
│   ├── pm/
│   ├── qm/
│   └── cs/
└── migration/                 # ECC 到 S/4HANA 迁移变化
    ├── table-changes.md
    ├── deprecated-tables.md
    └── new-tables.md
```

## SAP 模块对照表

| 模块代码 | 英文名称 | 中文名称 | 主要功能 |
|---------|---------|---------|---------|
| FI | Financial Accounting | 财务会计 | 总账、应收、应付、资产会计 |
| CO | Controlling | 管理会计 | 成本控制、利润分析 |
| MM | Material Management | 物料管理 | 采购、库存、发票校验 |
| SD | Sales & Distribution | 销售分销 | 销售、发货、开票 |
| HR/HCM | Human Capital Management | 人力资本管理 | 人事、薪酬、时间管理 |
| PP | Production Planning | 生产计划 | MRP、生产订单、BOM |
| PS | Project System | 项目系统 | 项目计划、预算、结算 |
| PM | Plant Maintenance | 工厂维护 | 设备维护、维修订单 |
| QM | Quality Management | 质量管理 | 质检计划、检验批 |
| CS | Customer Service | 客户服务 | 服务订单、服务合同 |

## 参考资源

- [SAP Help Portal](https://help.sap.com/)
- [LeanX - SAP Tables](https://www.leanx.eu/en/sap-tables)
- [SAPStack](https://sapstack.com/)
- [SAP S/4HANA Cloud API Hub](https://api.sap.com/)

## 贡献指南

本研究项目采用 Markdown 格式记录，每个模块包含：
1. 核心表清单
2. 表关系图 (Mermaid ERD)
3. 字段详细说明
4. 与 NextERP 的对照

## 许可

本研究项目仅供学习和研究目的。SAP 是 SAP SE 公司的注册商标。
