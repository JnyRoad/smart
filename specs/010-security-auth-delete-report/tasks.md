# 任务清单：保密区权限自动删除记录报表

## Phase 1 规格与基础

- [X] T001 核对蓝图、worktree、已有规格与当前源码，建立本目录 `spec.md`、`plan.md`、`research.md`、`data-model.md`、`contracts/report-api.md`。
- [X] T002 完成 `smart-module/docs/releases/security-auth-delete-report.md` 的结构、菜单、发布和回滚说明，不执行真实数据操作。

## Phase 2 US1/US2 后端报表与审计基础

- [X] T003 [US1] 先在 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/securityzone/impl/SmtSecurityAuthDeleteLogServiceImplTest.java` 补状态、园区隔离、筛选、导出边界测试并确认失败。
- [X] T004 [US1] 在 `smart-platform-core` 新增审计实体与Mapper/XML，`smart-platform-api` 新增报表DTO；`smart-platform-biz` 新增 `SmtSecurityAuthDeleteLogServiceImpl`/Controller，完成快照写入、分页、全部任务关联及参数/权限校验。
- [X] T005 [US2] 完成同一后端服务的有界导出、防公式注入和任务明细；验证跨园区主键请求被拒绝。

## Phase 3 US1/US3 自动任务记录

- [X] T006 [P] [US1] 先在 `SmtSecurityAuthDeleteServiceImplTest.java` 及任务创建测试补白名单、未到期、异常回滚/继续、多设备ID返回场景并确认失败。
- [X] T007 [US1] 修改 `SmtSecurityAuthDeleteServiceImpl.java` 与 `SmtStaffService.java`/`SmtStaffServiceImpl.java`，使逐条事务生效并保存完整审计、关联全部任务，保存失败明确抛错。
- [X] T008 [US3] 补演练及配置兼容/校验测试，修改配置entity/ReqDTO/RespDTO和上述服务，演练不产生真实删除或设备任务。

## Phase 4 US2/US3 管理界面

- [X] T009 [P] [US2] 先在 `smart-ui/src/views/platform/business/security_area/auth_delete_log/` 新增组件/API测试，覆盖组合筛选、分页/重置、导出与任务弹窗失败状态并确认失败。
- [X] T010 [US2] 实现同目录 `index.vue` 与 `smart-ui/src/api/platform/securityAuthDeleteLog.js`，按契约展示报表、导出和任务下钻；补保密区路由入口。
- [X] T011 [US3] 修改 `smart-ui/src/views/platform/business/security_area/edit.vue`，增加演练开关，缺失字段按0呈现；验证保存透传。

## Phase 5 集成与交付

- [X] T012 运行相关 Maven、Vitest、lint、前端构建，独立评审并修复实质问题；在 `quickstart.md` 记录实际验证和限制。
- [X] T013 更新 `docs/yuhui-prototype/yuhui-blueprint.html` 第5.6项和本目录交付状态，明确源码实现与未部署/Oracle现场验收的区别。

## 依赖与并行

T001后进行一致性分析，通过后交给superpowers执行。后端T003–T005、自动任务T006–T008、界面T009–T011可依据固定契约并行；后端Agent独占所有新审计实体/DTO/Mapper/服务/Controller，自动任务Agent独占旧自动任务与Staff及配置Java，前端Agent独占UI文件。主Agent独占发布说明、蓝图及任务状态。全部集成后T012，完成后T013。测试任务先于对应实现；实体与常量通过消费者行为测试验证，不增加getter或源码字符串镜像测试。

## 需求覆盖

FR-001 T004/T007；FR-002 T003/T004/T007；FR-003 T003/T004/T009/T010；FR-004 T003/T004/T005；FR-005 T005/T009/T010；FR-006 T006/T007；FR-007 T008/T011；FR-008 T002/T010/T013。SC-003由T002提供现场验收步骤，真实性能未验证。

## Phase 6 升级交付补齐与 PR

- [X] T014 [US1] 新增版本化 Oracle 迁移、前置检查、迁移后验证和回滚检查，复核与实体/Mapper结构一致、重跑保护和schema边界。
- [X] T015 更新发布说明与迁移目录索引，给出构建产物、数据库/后端/前端/菜单的升级顺序及可执行命令；完成独立评审和文件校验。
- [ ] T016 按用户授权仅提交本任务文件，推送功能分支并创建面向 main 的 PR。

FR-009由T014覆盖，FR-010由T015覆盖；T014与升级流程只读调查可并行，T015依赖两者，T016在验证后执行。真实Oracle验证仍由现场验收，不因脚本存在而标记已执行。
