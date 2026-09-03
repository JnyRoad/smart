# 任务：手动下发权限增加起止日期

**输入**：[spec.md](spec.md)、[plan.md](plan.md)、[data-model.md](data-model.md)、[契约](contracts/manual-permission-window.md)

## 阶段 1：准备

- [X] T001 核对并保留当前工作区未提交的规格产物与已有改动边界，确认不执行真实数据库操作：`specs/004-permission-date/`

## 阶段 2：基础数据与后端契约

- [X] T002 [P] 为两种请求的日期字段、默认值和倒置日期校验写失败测试：`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtStaffDeviceAuthServiceImplTest.java`、`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityServiceImplTest.java`
- [X] T003 [P] 为员工授权服务写任务时间窗口透传、缺省窗口和“不生成延迟删除任务”的失败测试：`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtStaffDeviceAuthServiceImplTest.java`
- [X] T004 [P] 为权限组批量授权服务写相同窗口透传和非法日期零写入的失败测试：`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityServiceImplTest.java`
- [X] T005 为 `SMT_STAFF_DEVICE_AUTH` 增加可展示的起止时间字段，并提供非破坏性回滚前置检查：`smart-module/database/manual/20260903_add_staff_device_auth_window.sql`、`smart-module/database/manual/20260903_rollback_staff_device_auth_window.sql`、`smart-module/database/manual/README.md`
- [X] T006 为关联实体、请求/响应 DTO 与相关 Mapper 追加起止时间映射：`smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/SmtStaffDeviceAuth.java`、`smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/req/DeviceAuthRelationAddReqDTO.java`、`smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/`
- [X] T007 在员工授权和权限组批量授权服务中归一化日期、校验顺序并将窗口传入设备任务服务：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtStaffDeviceAuthServiceImpl.java`、`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityServiceImpl.java`
- [X] T008 修改设备任务服务契约与实现，将请求窗口写入 ISC 下发任务而不创建延迟删除任务：`smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/service/SmtDeviceTaskService.java`、`smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/service/impl/SmtDeviceTaskServiceImpl.java`

**检查点**：未携带日期的旧请求兼容；合法窗口进入下发任务；倒置日期零写入。

## 阶段 3：用户故事 1 - 员工信息授权有效期（P1）

**目标**：员工信息的通关权限分配可选择并下发有效期。

**独立测试**：提交默认和自定义区间，检查请求负载；倒置区间在客户端不能提交。

- [X] T009 [P] [US1] 编写员工通关权限弹窗默认日期、校验和请求负载的失败测试：`smart-ui/src/views/platform/basic/staff_info/permission-window.test.js`
- [X] T010 [US1] 在员工通关权限分配弹窗的追加/覆盖选项上方实现有效期选择、默认值和客户端校验：`smart-ui/src/views/platform/basic/staff_info/index.vue`
- [X] T011 [US1] 已确认员工 API 包装器直接透传请求对象，无需改动：`smart-ui/src/api/platform/basic/staff_info.js`

**检查点**：员工信息入口独立可用。

## 阶段 4：用户故事 2 - 权限组人员授权有效期（P2）

**目标**：三类权限组人员批量粘贴窗口共享有效期行为。

**独立测试**：以每种权限组路由打开同一窗口，日期处理和请求字段一致。

- [X] T012 [P] [US2] 编写权限组批量粘贴窗口默认日期、校验和请求负载的失败测试：`smart-ui/src/views/platform/area/limit/doPaste.test.js`
- [X] T013 [US2] 在共享权限组人员批量粘贴窗口实现有效期选择、默认值和客户端校验：`smart-ui/src/views/platform/area/limit/doPaste.vue`
- [X] T014 [US2] 在权限组人员明细展示关联记录的起止日期：`smart-ui/src/const/crud/platform/area/limit_person.js`、`smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/AuthDetailRespDTO.java`、`smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtDeviceAuthorityMapper.xml`

**检查点**：通行权限、门禁通关权限、考勤通关权限的共用批量入口一致可用。

## 阶段 5：收尾与验证

- [X] T015 跑新增后端测试及既有受影响测试，并记录无法本地复核的 ISC 设备侧边界：`smart-module/smart-platform/`
- [X] T016 跑新增前端 Vitest、lint 和受影响构建检查：`smart-ui/`
- [X] T017 对照 [quickstart.md](quickstart.md) 复查请求、任务、展示和非范围，回写本任务清单：`specs/004-permission-date/quickstart.md`、`specs/004-permission-date/tasks.md`

## 依赖与执行顺序

- T002、T003、T004 必须先失败，再开始 T005-T008 的实现。
- T005-T008 是两个用户故事的共同基础。
- T009 必须先失败，再实施 T010-T011；T012 必须先失败，再实施 T013-T014。
- T015-T017 在两个用户故事完成后执行。

## 并行机会

- T002、T003、T004 可在不同测试类并行。
- T009 与 T012 可在不同前端测试文件并行。
- 数据库迁移、后端实现和前端实现按依赖分阶段，不让多个修改者写同一文件。

## 实施策略

先完成后端数据和任务窗口这一最小闭环，再完成员工信息入口，最后接入共享权限组窗口。每项测试必须先观察到预期失败，再写最小代码使其通过。
