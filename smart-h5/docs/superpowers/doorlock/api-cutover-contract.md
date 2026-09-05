# H5 到 smart-lock 的接口切换契约

## 当前调用事实

当前 smart-h5 的 dorm feature 已封装以下“本人”语义调用，定义位于 smart-h5/src/features/dorm/api.ts：

| 语义 | 当前路径表达 | 前端边界 |
|---|---|---|
| 获取本人动态密码/门锁结果 | `GET /dormitory/staff/get/pwd`（query：`badge`） | 页面应使用当前员工的 `badge`；结果需区分空值未入住。 |
| 修改本人门锁密码 | `POST /dormitory/staff/update/lock/pwd`（body：`badge`、`newPwd`） | 仅提交受控密码输入；结果需区分已受理与设备确认。 |
| 刷新本人动态密码 | `POST /dormitory/staff/update/pwd`（body：`badge`、`facePic`） | 人脸或其他服务端校验结果必须由后端裁定。 |

以上仅确认客户端调用事实；携带 `badge` 不能证明后端已将其与 token 身份绑定，服务端鉴权需单独验证。这些当前路径是现有 Smart 平台接口表达，不是未来 smart-lock 的最终路径。切换前不得仅改前端 URL；必须先冻结版本化 DTO、错误码、鉴权、幂等、状态和回退兼容策略。

## 新接口必须保证的语义

| 合同项 | 要求 |
|---|---|
| 身份 | 服务端从 token 获得本人、租户和数据范围；拒绝 caller 指定 personId。 |
| 状态 | 返回明确的业务状态：可用、未入住/无权限、已受理待确认、已确认、需人工核验、设备不支持、冻结、限频、失败。 |
| 动态结果 | 不写入 URL、日志、持久化浏览器缓存或分享参数；明确到期与使用范围。 |
| 密码 | 不返回明文旧密码；输入受服务端频率、复杂度、设备能力、有效住宿和审计约束。 |
| 幂等 | 写请求带 requestId/idempotencyKey；重复点击不应创建多次设备命令。 |
| 错误 | 使用版本化业务错误码和安全文案；前端不得根据 HTTP 200 推断设备已完成。 |
| 切换模式 | LEGACY_ONLY、SHADOW、CUTOVER_FREEZE、NEW_ONLY 都必须有可显示的受控结果；H5 不得绕过模式直连旧服务。 |

## 前端实施顺序

1. 在 smart-h5/src/features/dorm/api.ts 建立新旧合同的受控适配层，调用方继续只使用“本人”语义函数；
2. 用 mock/契约测试覆盖所有状态，而非用生产旧系统做开发依赖；
3. 在 SHADOW 期间只显示安全的只读或受控不可用状态，绝不触发新旧双下发；
4. 正式 NEW_ONLY 前完成旧路径移除或由网关统一兼容，前端不得保留可切换到旧系统的 URL、开关或人员参数。
