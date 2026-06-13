# database

`database/` 保存 `smart-module` 相关数据库资料。

当前平台没有自动迁移框架；需要人工执行的正向/回滚 SQL 放在 `manual/`。根目录下的 `*.db` 是本地 SQLite/临时数据库文件，应由 `.gitignore` 忽略，不作为迁移脚本提交。

## 目录结构

```text
database/
├── README.md
├── smart_bridge.db        # 本地数据库文件，忽略提交
└── manual/
    ├── README.md
    ├── 20260602_add_smt_isc_park_config.sql
    ├── 20260602_add_smt_isc_staff_card.sql
    ├── 20260602_add_smt_isc_card_task.sql
    ├── 20260602_rollback_smt_isc_park_config.sql
    ├── 20260602_rollback_smt_isc_staff_card.sql
    ├── 20260602_rollback_smt_isc_card_task.sql
    └── ...
```

## 维护规则

- 新增数据库变更脚本放入 `manual/`，并同步更新 [manual/README.md](manual/README.md)。
- 正向脚本和回滚脚本要能说明执行条件、影响表、是否可重复执行。
- 不提交真实数据库 dump、SQLite 本地文件、生产导出数据或含敏感信息的样例。
