# Existing Supplier Read Contract

本功能不增加或修改 HTTP 路径、方法、鉴权或请求参数。以下已有读取结果的共同变化是：不再返回已逻辑删除的供应商或人员。

| 读取用途 | 既有路径/调用 | 结果约束 |
|---|---|---|
| 供应商树与人员导入级联选择器 | `GET /platform/securityarea/supplier/list` | 仅有效供应商。 |
| 供应商分页（现有前端封装） | `GET /platform/securityarea/supplier/page` | 仅有效供应商。 |
| 供应商导出 | `GET /platform/securityarea/supplier/excel` | 仅有效供应商。 |
| 到期通知候选 | `POST /platform/securityarea/supplier/notify/list` | 仅有效供应商。 |
| 供应商人员分页 | `GET /platform/securityarea/supplier/person/page` | 仅有效人员且所属供应商有效。 |
| 保密区订单人员名单与访客校验 | 既有服务读取 | 使用人员与有效供应商的单条关联查询，不返回或认可失效供应商名下人员。 |
| 保密区订单详情 | 既有 Mapper 调用 | 订单保留；不提供已删除供应商的当前展示字段。 |

PDA/扫码失效反馈不属于本次契约。
