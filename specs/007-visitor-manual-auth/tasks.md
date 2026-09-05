# 任务：访客手动下发权限

执行输入：[spec.md](spec.md)、[plan.md](plan.md)、[接口契约](contracts/api.md)。按 TDD 先观察失败，再实现；主 Agent 负责更新本清单，子代理禁止并发写任务状态。

## 阶段 1：隔离与契约

- [X] T001 核对蓝图、代码与索引，建立任务 worktree，写入 `specs/007-visitor-manual-auth/spec.md`。
- [X] T002 固化 `specs/007-visitor-manual-auth/plan.md` 与 `contracts/api.md`，核对宪法和跨模块契约。

## 阶段 2：后端下发与边界（US1、US2）

- [X] T003 [P] [US1] 在 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/admittance/` 补充手动下发失败测试：人员字段及不支持车辆拒绝、时间窗口、新批次与重叠设备去重；记录 RED。
- [X] T004 [US1] 在 `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/req/admittance/VisitorManualAuthReqDTO.java`、`smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/admittance/VisitorManualAuthOptionsRespDTO.java` 与 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/admittance/` 实现选项查询及原子建任务；使 T003 通过（FR-003/004/006/007）。
- [X] T005 [US2] 在后端相应服务测试和 `controller/admittance/` 测试中覆盖无登录/权限、跨园区、状态、时间、对象/权限/设备越界、照片缺失、车辆不支持、涉密与保存失败；记录 RED（FR-005/008）。
- [X] T006 [US2] 在 `SmtAdmittanceApplyManageController.java` 与手动授权服务补齐 ACL、全部前置校验及作废并发保护，使 T005 通过。

## 阶段 3：前端入口与反馈（US1、US2）

- [X] T007 [P] [US1] 在 `smart-ui/src/views/platform/visitor/incoming_record/` 补充单选、查询清空选择、对象/权限切换、固定日期、请求载荷、成功/失败和重复点击行为测试；运行观察 RED（2 files / 4 tests failed，首次 GREEN 5 files / 20 tests passed）。
- [X] T008 [US1] 实现 `incoming_record/index.vue`、`manualAuth.vue` 与 `_service.js`，使 T007 通过（FR-001/002/009/010）；员工双栏交互复用，禁改员工业务；按用户补充要求对齐原型及系统统一风格，有效期置底，浏览器核对实际布局。

## 阶段 4：集成与交付

- [X] T009 对前后端定向测试、目标 ESLint 和 `git diff --check` 进行集成验证；独立审查请求契约、ACL、身份/时间、事务及兼容性，修复实证问题。
- [X] T010 在 `specs/007-visitor-manual-auth/quickstart.md` 记录真实验证与未验证边界，在 `docs/yuhui-prototype/yuhui-blueprint.html#item-5-2` 如实回写分支实现状态及涉密依赖。

## 依赖与并行

T001 → T002 → 一致性分析。分析通过后，T003→T004→T005→T006 与 T007→T008 可按后端/前端文件所有权并行；两者完成后 T009→T010。共 10 项任务，US1 4 项、US2 2 项，4 项基础/交付任务。

## 执行策略

先完成普通 ISC 人员任务链与可观察错误，再验证鉴权、并发和涉密拒绝。每项完成即由主 Agent 更新；提交、推送和 PR 按会话授权执行，合并与部署另按授权执行。5.1 未接通时不得把涉密功能标记为已验收。

## 完成证据与边界

前端22项、后端98项定向回归通过，目标ESLint无错误，管理端构建与浏览器布局/交互检查通过，详见quickstart。前后端独立审查发现的本次查询残留、选择状态与布局问题已收尾；后端复核确认无其他本次引入的实质缺陷。旧作废回收按证件号跨园区匹配的风险为基线已有，未在本次扩大修改，已列入quickstart与蓝图的上线前边界。
