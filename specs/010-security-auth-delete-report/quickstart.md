# 验证指南

## 本地

从 `smart-module/` 执行本次涉及的服务与控制器回归：

```bash
mvn -pl smart-platform/smart-platform-biz -am \
  -Dtest=SmtSecurityAuthDeleteServiceImplTest,SmtStaffServiceImplTest,SmtSecurityAuthDeleteLogServiceImplTest,SmtSecurityAuthDeleteLogControllerTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

从 `smart-ui/` 执行 `pnpm test`、`pnpm lint`。以下构建变量只是编译占位，不连接服务，也不是部署配置：

```bash
VUE_APP_PLATFORM_URL=http://127.0.0.1:9990 \
VUE_APP_BASE_URL=http://127.0.0.1:9990 \
NODE_OPTIONS=--openssl-legacy-provider pnpm build
```

本次后端测试使用 Mockito / MockMvc，未启动应用、定时任务或真实设备调用。

## 本次验证结果（2026-09-05 UTC）

- Maven reactor 编译及上述 4 个测试类共 45 项通过，0 失败、0 错误、0 跳过。覆盖实际审计快照、逐条事务回滚顺序、审计失败抛出、同次普通/ISC 任务引用、旧 void 入口兼容、演练配置、园区隔离、导出边界与真实 HTTP 分页参数。
- 前端全量 Vitest：86 个文件、436 项通过。覆盖组合筛选、请求竞态、任务详情、导出实际 ArrayBuffer 错误体、401 处理和真实 Element UI 演练开关。
- 前端全量 lint：退出码 0，无 error，仍有仓库 warning；生产构建退出码 0。
- 读取实际 Mapper 主查询及结果筛选 SQL，在内存 SQLite 中执行 15 种任务状态组合与演练/白名单样例，另核对同号不同来源、园区过滤、空范围拒绝及排序，断言通过。该检查只证明 SQL 结果语义，不证明 Oracle 方言、驱动、分页插件或执行计划。
- 后端报表、自动删权链路与前端分别独立评审，确认的问题修复后已复核关闭；`git diff --check` 通过。

Oracle DDL、真实数据库事务/分页、首页 3 秒性能、目标表格软件显示、完整登录权限链路及真实设备状态联调均未验收。数据库、菜单、生产开关及部署未执行。

## 升级交付验证

- 已提供 [版本化迁移目录](../../smart-module/database/manual/20260905_security_auth_delete_report/README.md) 和 [升级说明](../../smart-module/docs/releases/security-auth-delete-report.md)，包含显式目标 schema、前置/后置校验、兼容对象重跑与应用回退边界。Oracle 脚本仅做源级校验，尚未执行。
- 迁移脚本与发布流程已分别独立源级复核，发现的 schema、Oracle 元数据类型、可空性和约束比较问题修复后复核关闭；文档链接及 SQLPlus 共享文件依赖检查通过。
- 对齐 `main` 基线 `8c6e9f6c` 后，Maven 使用上述同组 45 项测试执行 `package`，全部通过并生成可执行 `smart-platform-biz.jar`。
- 按本版本单服务 manifest 调用 `scripts/build-release-jars.sh --skip-build` 已成功收集 1 个平台 Jar 和 3 个 runtime 脚本，4 项 SHA-256 校验均通过；未启动服务。发布产物位于被忽略的 `release-artifacts/`，不纳入提交。

## 发布前置与现场验收

按 [发布说明](../../smart-module/docs/releases/security-auth-delete-report.md) 核验 Oracle schema 后应用新结构并配置菜单权限；只在明确授权的目标环境执行。

1. 使用两个不同园区管理员与一个无园区账号，验证列表、导出及直接任务详情访问范围。
2. 在测试园区准备白名单、未到期、无设备、演练命中数据；执行现有任务入口，核对报表记录与原权限保持情况。
3. 正式任务各准备执行中、失败、全部成功、关联任务缺失；核对列表/筛选/导出/详情一致。
4. 模拟一条删除失败，验证该条原权限仍在、失败审计存在、后续记录继续处理。
5. 核对历史快照不随员工/权限组更名而变化；导出超 10000 条明确拒绝。
6. 使用目标规模数据查看 Oracle 实际执行计划并测量首页耗时，小于 3 秒才通过性能验收。

本地通过不能替代以上现场验证。本次未授权生产开关、DDL/DML、设备调用或部署。
