# Task 2 补救：管理员详情与旧员工实体路由收口

## 范围

- 新增 `GET /staff/admin/{staffId}`：要求 `platform_staff_lookup` 权限，认证主体为空时拒绝，并以其园区范围过滤员工所属 BU。
- 新增 `GET /staff/admin/temporary/by-badges`：用于批量离职确认，忽略前端 BU 参数，只查询认证主体园区内临时员工。
- 删除公开实体路由：`/staff/{id}`、`/staff/simple/get`、`/staff/simple/get-list`、`/staff/query/{mobile}`、`/staff/face/search/login`、`/staff/staffByBadge`、`/staff/staffByBadges`、`/staff/getFullByBadge/{badge}`、`/staff/baseInfo/{badge}`、`/staff/updatePhone`。
- Smart UI 简历任职关系原本使用 `getFullByBadge`，已迁移为 `/staff/lookup`（精确工号取主键）再访问 `/staff/admin/{staffId}`，并仅适配姓名、性别和组织展示字段；没有保留旧路由兼容层。
- Schedule 的 6 条按员工 ID 查询迁移到服务令牌内部路径；旧 Feign 实体查询契约删除。

## 最小字段与用途

| 契约 | 字段 | 用途 |
| --- | --- | --- |
| `AdminStaffDetailRespDTO` | `staffId`、`badge`、`name`、`sex`、`companyName`、`departmentName`、`jobName`、`status` | Smart UI 员工识别与组织展示 |
| `AdminTemporaryStaffRespDTO` | `staffId`、`badge`、`name` | Smart UI 批量离职前确认与提交 ID |
| `InternalScheduleIscPersonRespDTO` | `badge`、`name`、`sex`、`birth`、`certno` | ISC 创建人员档案；完整证件号仅在服务端 ISC 调用中使用 |
| `InternalScheduleStaffIdentityRespDTO` | `badge`、`certno`、`status` | ISC 查询、删除复查与卡片下发 |

Schedule 的创建人员路径不再发送历史 `phoneNo`、`email`，也不再记录员工 DTO 内容；日志仅记录任务和成功状态。

## TDD 与验证

- RED：删除旧 `getSimpleSttaffById` Feign 契约后，Schedule 测试编译产生 15 处旧方法符号错误。
- GREEN：`mvn -q -f smart-module/pom.xml -pl smart-schedule -am -Dtest=ISCDeviceTaskServiceImplTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test` 通过（62 tests）。
- `mvn -q -f smart-module/pom.xml -pl smart-platform/smart-platform-biz -am -Dtest=SmtStaffControllerPrivacyContractTest,InternalStaffControllerTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test` 通过。
- `pnpm test -- src/api/platform/basic/personnel_manage.staff-lookup.test.js src/api/platform/basic/staff_info_detail.admin-detail.test.js src/api/platform/resume/index.staff-lookup.test.js src/views/platform/basic/staff_info/detail.privacy.test.js` 通过。
- `git diff --check` 通过。

## 发布依赖

Schedule 新 Feign 路径依赖同分支的 `InternalStaffController` 服务令牌端点先于或同时部署；生产 Nacos、部署、推送和 PR 均未执行。
