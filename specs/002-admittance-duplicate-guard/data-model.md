# Data Model: 入厂申请区域并发判重

## Existing entities

| Entity | Relevant fields | Rule |
|--------|-----------------|------|
| 入厂申请 | `STATUS`, `START_TIME`, `END_TIME`, `AREA_TYPE` | 仅有效状态参与；开区间时间重叠；区域有交集才冲突，区域未知按全区域冲突。 |
| 入厂随行人员 | `VISITOR_ID`, `CERT_NO`, `IS_MAIN` | 主申请人与所有随行人员都参与；不再按 `IS_MAIN` 排除。 |

## New coordination entity

| Field | Purpose | Constraint |
|-------|---------|------------|
| `CERT_NO_HASH` | 证件号的 SHA-256 摘要，用作互斥键 | 主键；不存明文证件号 |
| `CREATE_TIME` | 协调记录的创建时间 | 非空；仅用于审计与运维定位 |

`SMT_ADMITTANCE_CERT_LOCK` 不是申请业务数据，也不按申请生命周期删除。它存在的唯一目的是为首次出现的证件号提供可锁定行；事务提交或回滚后行锁自动释放。

## Release schema prerequisite

下列定义是发布评审材料，**不得由本功能代码或本工作区直接执行**：

```sql
CREATE TABLE SMT_ADMITTANCE_CERT_LOCK (
    CERT_NO_HASH VARCHAR2(64 CHAR) NOT NULL,
    CREATE_TIME TIMESTAMP(6) DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_SMT_ADMITTANCE_CERT_LOCK PRIMARY KEY (CERT_NO_HASH)
);
```

发布前需由数据库负责人以目标 Oracle 版本、表空间规范、权限与回滚方案复核该定义；本次不包含生产 DDL、DML、索引或统计信息操作。
