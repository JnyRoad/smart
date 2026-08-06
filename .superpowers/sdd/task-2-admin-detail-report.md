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

## 复审阻塞项整改

- 已实现 `GET /internal/staff/schedule/isc-person/{staffId}` 与
  `GET /internal/staff/schedule/identity/{staffId}`。两个路径均使用 `@Inner`、
  `@OpenApi("server")`，并额外校验受管的 Schedule `client_id`；未认证、非
  Schedule 服务令牌和未配置客户端均拒绝。路由参数保持 Feign 已发布的字符串契约，
  服务端仅按主键读取并投影 Schedule 专用最小 DTO。
- 删除 `/staff/getTempList` 与 `/staff/getTempById`，替换为
  `/staff/admin/temporary/page`、`/staff/admin/temporary/{staffId}`。二者均要求
  `platform_staff_lookup`，范围来自认证管理员园区；新的临时人员响应没有身份证、
  手机号、人脸或图片链接。Smart UI 已同步迁移并在客户端再次丢弃任何意外附带的
  敏感字段。
- 删除接收完整 `SmtStaff` 的 `/staff/updateStaff` 与 `/staff/outDormitory`；手机号
  修改与基础资料修改替换为 `/staff/admin/update-phone`、`/staff/admin/update`，要求
  `platform_staff_manage`，只接收最小请求且在服务端验证目标员工园区。基础资料修改
  不接受证件号、电话、地址、人脸、组织归属字段。
- Smart Data 许昌 C6 三个员工 Feign 调用（入职、离职、复职）已追加
  `INTERNAL_SERVICE_AUTH_REQUIRED`，与新的内部服务令牌契约一致。
