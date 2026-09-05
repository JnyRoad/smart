# 实施计划：访客手动下发权限

**分支**：`feat/visitor-manual-auth` | **日期**：2026-09-05 | **规格**：[spec.md](spec.md)

## 摘要

在管理端访客申请记录增加单选和双栏权限弹窗，通过受保护的访客管理接口查询可授权对象、提交本单人员 ISC 任务。沿用已有设备任务管线、申请单有效期与作废串行机制。5.1 未落地期间，涉密权限明确拒绝。

## 技术上下文

- Java 8、Spring Boot、MyBatis-Plus、Oracle；Vue 2、Element UI；无新依赖或数据库迁移。
- 测试：JUnit/Mockito、Vitest/Vue Test Utils；测试只使用本地隔离数据和 Mock 外部边界。
- 范围：`smart-platform-api` 请求/响应，`smart-platform-biz` 管理 Controller/业务服务，`smart-ui` 访客列表及弹窗。
- 每次一个人员对象、1–100 个权限组；同一设备去重，校验全部成功后才保存任务。

## 宪法门禁

初始与设计后均通过：业务权限在后端；不修改 SQL/数据库；中文说明；TDD；独立 worktree；规格显式绑定。真实 Oracle/ISC 验收单独保留。

## 结构与职责

- `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/req/admittance/VisitorManualAuthReqDTO.java`：申请、对象和权限组请求；不接收日期。
- `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/admittance/VisitorManualAuthOptionsRespDTO.java`：申请固定有效期、候选对象、权限组。
- `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/admittance/SmtAdmittanceApplyManageController.java`：新查询/下发端点，统一操作权限 `platform_visitor_incoming_auth`。
- `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/admittance/`：校验与创建任务。使用独立 `VisitorManualAuthService` / `impl/VisitorManualAuthServiceImpl` 处理本次授权；依赖现有 Mapper、人员/权限/设备服务和任务保存契约，不反向依赖原申请服务。
- `smart-ui/src/views/platform/visitor/incoming_record/`：列表单选、`manualAuth.vue` 弹窗、`_service.js` 接口封装及行为测试；双栏结构沿用员工页样式与交互，不改员工功能。视觉依据为 `docs/yuhui-prototype/yuhui-web-proto.html` 的访客授权/员工授权弹窗及 `staff_info/index.vue`：700px 弹窗、12px 外圆角、8px 面板圆角、系统橙色主按钮、灰色对象信息区；只读有效期放在双栏之后、页脚按钮之前。

## 关键流程

1. 查询和下发均检查当前用户权限和申请所属园区。
2. 下发先按现有条件更新方式取得申请单行锁；状态只允许已通过/已到达，重读有效期，拒绝过期或倒置。
3. 对象 ID 必须来自当前申请；人员照片必须有效；车辆当前不支持，提交即拒绝；权限组同园区、类型匹配、公共权限，所有设备有效、同园区且为 ISC 人员设备。
4. 生成新批次，任务统一写入申请和批次，复用现有任务保存链；任一步失败整体回滚。
5. 返回批次字符串，前端提示任务提交成功，关闭弹窗并刷新列表。

## 全局约束

不调用历史 H5 放行路由执行新写入，不产生员工关联，不改申请状态，不更新既有审批完整批次快照，不新建延迟删除，不把任务提交当设备成功。无考试依据时禁止涉密下发。上线需通过权限管理授予新按钮权限码，未授予默认不可操作。

## 验证与交接

任务见 [tasks.md](tasks.md)，契约见 [contracts/api.md](contracts/api.md)，验证见 [quickstart.md](quickstart.md)。前后端按契约独立 TDD，最终合并审查请求字段、类型、ACL、时间与任务字段。

## 已核验能力限制

ISC服务明确拒绝车辆，标准任务表缺少applyId/batchId，因此首版仅开放ISC人员下发。请求保留vehicleId用于明确拒绝；选项vehicles返回空列表，前端不开放车辆选项并说明限制。权限组只呈现人员组；服务端仍对所有入参全量校验，不允许混入不支持设备。
