# 保密区权限自动删除记录报表升级说明

对应规格：[010-security-auth-delete-report](../../../specs/010-security-auth-delete-report/spec.md)。数据库变更使用本版本迁移文件；本文给出应用、数据库和菜单的完整升级顺序。当前交付不包含任何目标环境的执行结果。

## 升级范围

| 交付物 | 位置 / 用途 |
| --- | --- |
| Oracle 迁移 | [20260905_security_auth_delete_report](../../database/manual/20260905_security_auth_delete_report/README.md) |
| 平台服务 Jar | `smart-module/smart-platform/smart-platform-biz/target/smart-platform-biz.jar` |
| 管理端静态文件 | `smart-ui/dist/`，须使用目标环境的前端构建变量 |
| 角色菜单 | 保密区下新增“权限自动删除记录”，配置查询与导出权限 |

数据库变化只有新增 `SMT_SECURITY_AUTH_DELETE_LOG`、`SMT_SECURITY_AUTH_DELETE_TASK` 两张审计表，以及 `SMT_SECURITY_AUTH_DELETE.DRY_RUN` 字段和必要约束。ID 由应用生成，无需序列。新表不关联可删除的任务表外键，不清理业务数据，不根据旧任务猜测自动删权来源回填历史。

仅替换平台业务服务和管理端。api/core 随业务 Jar 打包，不单独部署；不需要因本功能升级网关、认证或设备桥接服务。调度服务只有暂停/恢复既有配置的操作，不包含代码升级。

## 1. 准备构建产物

在本次 PR 对应的检出目录，先从 `smart-module/` 运行：

```bash
mvn -pl smart-platform/smart-platform-biz -am \
  -Dtest=SmtSecurityAuthDeleteServiceImplTest,SmtStaffServiceImplTest,SmtSecurityAuthDeleteLogServiceImplTest,SmtSecurityAuthDeleteLogControllerTest \
  -Dsurefire.failIfNoSpecifiedTests=false package
```

从 `smart-ui/` 运行 `pnpm install --frozen-lockfile`、`pnpm test`、`pnpm lint`，按目标环境提供 `VUE_APP_PLATFORM_URL` 和 `VUE_APP_BASE_URL` 后运行 `NODE_OPTIONS=--openssl-legacy-provider pnpm build`。本地验证所用的 `127.0.0.1` 只是编译占位，不能作为正式部署配置。

记录平台 Jar 和整份前端构建的版本与校验和；不要只拷贝一个 Vue 页面或一个静态 chunk。基础构建方式以 [发布 Jar 清单](../../../scripts/release-jars.manifest) 为依据。

若现场采用本仓库的 Linux runtime 发布流程，完成 Maven 构建后可从仓库根目录收集单服务发布包：

```bash
bash scripts/build-release-jars.sh --skip-build \
  --manifest smart-module/docs/releases/security-auth-delete-report.manifest \
  --output release-artifacts/backend/security-auth-delete-report
```

该命令只收集平台 Jar、校验和与 `runtime/` 三个脚本，不会构建其他后端或管理端。输出目录已存在时使用新的版本目录，不用 `--force` 覆盖旧发布包。将本版本完整迁移目录、`smart-ui/dist/` 和本升级说明一并交付；通用 Jar 收集脚本不会自动携带数据库和前端文件。

## 2. 备份和暂停自动删权

发布负责人先记录目标环境、Oracle 服务和 schema、平台全部实例、静态站点目录、Nacos namespace/group/dataId、原自动删权开关、当前 Jar/静态文件版本及菜单角色配置。将旧 Jar 和完整管理端文件留在目标环境已有备份位置，并按该环境数据库备份流程保存恢复点。

自动删权由 `smart-schedule` 的 `PlatformTimerTask.autoDeleteTask()` 每天按服务时区零点触发，受 `task.job.supplier-auto-auth-delete` 控制。升级窗口将该开关设为 `false`，确认所有调度实例已实际应用且上一轮执行结束。是否需要重启调度服务，按目标环境的配置刷新方式确认；仅修改 Nacos 页面不等于运行实例已暂停。不要通过 `/platform/security/auth/delete/task` 做健康检查，该接口会执行删权。

开关只控制定时触发；发布期间还应避免人工或其他调用方触发该任务。记录原值，验收后只能恢复原值，不因新增报表扩大自动删权范围。

## 3. 执行 Oracle 迁移

脚本入口位于：

```text
smart-module/database/manual/20260905_security_auth_delete_report/
```

使用 SQL*Plus 或 SQLcl，以目标 schema 的数据库用户连接。账号口令通过交互连接输入，不写进迁移文件。以下 `TARGET_SCHEMA` 必须替换为本次发布核对过的 schema 名；它与连接用户、当前 schema 必须一致。

```sql
-- 先在已经连接目标数据库的 SQL*Plus / SQLcl 会话中运行，只读核对已有结构。
@smart-module/database/manual/20260905_security_auth_delete_report/precheck.sql TARGET_SCHEMA

-- 仅在核对通过后执行正向迁移。
@smart-module/database/manual/20260905_security_auth_delete_report/upgrade.sql TARGET_SCHEMA

-- 单独保存迁移后校验输出。
@smart-module/database/manual/20260905_security_auth_delete_report/verify.sql TARGET_SCHEMA
```

从其他目录执行时，把路径换成发布包内的完整路径；整个迁移目录要一并交付，共享校验脚本不能漏拷。由发布流程保存脚本版本/校验和、目标、执行人、时间及输出，应用启动时不会自动执行 SQL。

迁移只补缺失的本版本对象；已存在对象必须通过结构检查后才能跳过。遇到不兼容的类型、约束或非法演练值会停止，不能忽略错误继续部署。迁移报错时保持旧应用与调度暂停状态，先检查哪些步骤已完成；兼容的中断状态可重跑同一份升级脚本，结构漂移交 DBA 处理，不重新运行历史迁移。

Oracle DDL 会隐式提交，`ROLLBACK` 不能撤销已经完成的建表/加列；脚本的错误退出用于阻止后续步骤。[Oracle DDL 事务说明](https://docs.oracle.com/en/database/oracle/oracle-database/19/tdddg/data-definition-language-ddl-statements.html)。

候选性能索引为 `(PARK_ID, EXEC_TIME, ID)`，**不随升级脚本自动创建**。新表主键、任务关联唯一键的索引由约束建立；其他索引须核对目标表规模和真实执行计划后另行决定。前置通配符匹配不承诺获得普通 B-tree 加速。

## 4. 升级平台服务和管理端

1. 只有 `verify.sql` 通过后，才按该环境既有发布方式替换全部平台实例的 `smart-platform-biz.jar`。避免新旧平台实例混跑：旧版本会忽略演练标识，不能在混合版本期间恢复自动任务。
2. 核对平台服务启动与健康状态，确认没有缺表、缺列或 SQL 错误。平台进程健康不等于新报表查询已通过，仍需执行第 6 步。
3. 发布整份 `smart-ui/dist/` 到既有管理端静态站点，更新静态资源缓存；平台先于管理端升级，防止新页面请求旧接口。

目标主机、Jar 目录、进程托管方式、反向代理和健康检查端口尚未核实，不能照搬其他项目或本地开发 compose 的启动命令。若现场采用本仓库的 Linux runtime 脚本，必须遵循其受控发布流程与 watchdog 前置检查；不直接手工 `java -jar` 启动第二个实例。

仅当目标环境确认使用上述 runtime 时，按实际路径替换占位符执行：

```bash
<发布包目录>/runtime/verify-release-runtime.sh --app-root '<实际服务目录>' --service smart-platform-biz
SMART_APP_ROOT='<实际服务目录>' <发布包目录>/runtime/restartService.sh status platform
SMART_APP_ROOT='<实际服务目录>' <发布包目录>/runtime/restartService.sh restart platform
SMART_APP_ROOT='<实际服务目录>' <发布包目录>/runtime/restartService.sh status platform
```

`platform` 是 runtime 的服务参数名。以上模板不能直接复制到未知主机；发布前校验失败时停止，由现场修复服务账号、受控脚本或 watchdog 配置。管理端不由该脚本管理，按现场已有静态发布流程执行。

## 5. 配置菜单和角色

在现有“保密区”功能组下创建菜单“权限自动删除记录”。父菜单、菜单 ID 与角色 ID 使用目标环境真实记录，不预设数值或覆盖其他菜单。

| 配置项 | 值 |
| --- | --- |
| 路由 | `/platform/business/security_area/auth_delete_log/index` |
| 组件 | `views/platform/business/security_area/auth_delete_log/index` |
| 查询权限 | `platform_security_auth_delete_log_view` |
| 导出权限 | `platform_security_auth_delete_log_export` |

通过现有菜单管理功能分别配置页面、查询/导出按钮权限，并授予需要的现有角色。查询权限也用于任务详情；导出权限独立控制。刷新登录会话和菜单缓存后验收。不要为了展示菜单扩大令牌的 `parkIdList`。

## 6. 验收后恢复原开关

- 用实际授权管理员进入报表，验证列表、条件查询、导出、任务详情。接口网关前缀为 `/platform/security/auth/delete/log`。
- 使用不同园区账号和无园区范围账号验证隔离；不能只看菜单能否打开。
- 首次上线没有历史数据时，空列表本身正常。在获准的测试园区执行演练，核对有新记录、权限未删除且没有设备任务；不要把手动调用正式删除当作只读验收。
- 正式记录关联任务全为成功时显示“任务执行成功”；失败/取消/过期为失败，离线继续等待，任务丢失/未知状态为未知。该状态不等于 5.8 已完成设备确认闭环。
- 核对已存人员/部门/权限快照不随后续改名漂移；导出超过 10000 条必须明确拒绝。
- 按 [现场验收清单](../../../specs/010-security-auth-delete-report/quickstart.md) 验证数据库事务、实际分页与首页 3 秒目标。

全部平台实例版本一致、验收通过后，按第 2 步记录恢复自动删权开关原值；原来关闭则继续关闭。确认下一轮实际执行能产生审计，保存发布结果。

## 回滚

1. 先暂停自动删权并确认没有在途执行，保持暂停直到回退条件核对完成。
2. 执行只读回退检查：

   ```sql
   @smart-module/database/manual/20260905_security_auth_delete_report/rollback_check.sql TARGET_SCHEMA
   ```

   若任何配置 `dryRun=1`，旧版本忽略该字段，直接恢复调度可能执行真实删除。脚本会阻止按普通回退流程继续；必须保持任务暂停，由发布负责人确认如何处理这些园区，不能批量把演练改成正式来绕过检查。
3. 停用本次新增菜单与权限入口，恢复备份的完整管理端和上一版本平台 Jar，核对所有平台实例及静态文件版本。
4. **保留两张审计表、任务关联和 DRY_RUN 字段，不执行 DROP/TRUNCATE 或删除历史记录。** 增量结构与旧应用兼容；回退不会恢复已经删除的业务权限，也不会撤回或重发已经创建的设备任务。
5. 回退检查、旧版本健康及业务验收通过后，才根据原发布记录决定是否恢复调度。需要恢复权限或处理已发任务时，另行核对具体记录范围。

## 已执行与未执行

本地业务测试、前端检查及构建结果见 [验证记录](../../../specs/010-security-auth-delete-report/quickstart.md)。本次提供可执行迁移和升级流程，但尚未在目标 Oracle 执行；实际 DDL、真实数据库事务/分页与性能、菜单写入、配置开关、应用部署及设备联调均未执行。
