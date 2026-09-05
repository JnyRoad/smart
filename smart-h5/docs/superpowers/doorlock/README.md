# smart-h5 门锁前端开发文档

状态：一期 H5 边界已冻结为员工本人自助能力；不承载 Web 管理后台复刻。

当前实现以 [011 门锁统一规格](../../../../specs/011-doorlock-oracle/spec.md)、[Web/H5 契约](../../../../specs/011-doorlock-oracle/contracts/web-h5.md) 和 [任务清单](../../../../specs/011-doorlock-oracle/tasks.md) 为准。本目录保留页面历史参考；旧接口目标、凭据处置及切换假设的冲突按 [设计修订记录](../../../../specs/011-doorlock-oracle/design-revisions.md) 修正，不再单独驱动实现。

H5 通过平台业务入口取得本人门锁能力，由服务端登录态确定身份；不再信任客户端传入的工号决定凭据目标。HTTP 受理与设备确认分开展示，密码不持久保留在浏览器。本轮仅更新设计，没有改动 H5 代码。

## 目标

smart-h5 继续承载“本人宿舍门锁/动态码”体验。管理员的设备、网关、人员、密码、指纹、授权、告警、日志和迁移操作均属于 smart-ui，不得因旧系统存在 H5 bundle 而移植到员工 H5。

## 导航

| 文档 | 用途 |
|---|---|
| [legacy-parity-and-scope.md](legacy-parity-and-scope.md) | 旧 H5 参考、一期明确范围和待决项。 |
| [page-and-state-contract.md](page-and-state-contract.md) | 当前宿舍门锁页面、状态和身份边界。 |
| [api-cutover-contract.md](api-cutover-contract.md) | 现有 H5 调用迁入 smart-lock 的契约要求。 |
| [test-and-acceptance.md](test-and-acceptance.md) | 单测、E2E、真机与安全验收。 |

## 与 Web 的关系

Web 旧后台操作复刻的权威文档在 [smart-ui 门锁文档](../../../../smart-ui/docs/superpowers/doorlock/README.md)。H5 只消费本人身份范围内的门锁状态或通行能力，不能提供后台权限下发或越权查看入口。
