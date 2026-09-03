# 验证指南：手动下发权限有效期

## 前置条件

- 使用隔离的本地或集成环境，准备有照片、在职且可授权的测试员工。
- 准备至少一个通行权限组、一个门禁通关权限组和一个考勤通关权限组。
- 不连接生产数据库，也不执行生产 DDL。

## 自动化验证

1. 在 `smart-module/smart-platform` 运行新增的服务和任务测试，确认自定义窗口被传入任务、缺省窗口被补齐、倒置日期无写入。
2. 在 `smart-ui` 运行新增的组件/静态契约测试，确认两个弹窗的默认值、日期校验和请求负载。
3. 运行受影响模块的既有测试，确认未携带日期的请求仍兼容。

## 集成环境人工验证

1. 在员工信息中选择员工和权限组，确认“有效期”在追加/覆盖上方，默认显示当天至 2030-12-31。
2. 以自定义日期提交，查询对应 ISC 下发任务，确认 `START_TIME` 和 `OVER_TIME` 与日期窗口一致。
3. 在三种权限组人员页分别批量粘贴工号并设置日期，确认每次均生成同一窗口的任务。
4. 尝试倒置日期，确认页面拒绝且服务端无新增关联或任务。
5. 在可控 ISC 环境检查设备侧 `auth_config` 的窗口；到期自然失效即可，不验证或创建本地延迟删除任务。

## 本轮自动化结果

- Maven：`PermissionValidityWindowTest`、`SmtDeviceTaskServiceImplTest` 共 14 项通过；`SmtStaffDeviceAuthServiceImplTest`、`SmtDeviceAuthorityServiceImplTest` 共 23 项通过。
- Vitest：全量 79 个测试文件、399 项测试通过。
- 管理端生产构建：在只对本次命令设置临时 `VUE_APP_PLATFORM_URL`、`VUE_APP_BASE_URL` 的前提下通过；未创建或修改 `.env`。
- `pnpm lint` 无 error；仓库已有大量历史 warning，本次未将其作为通过门槛。
