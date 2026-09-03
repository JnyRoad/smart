# 入厂申请区域并发判重契约

## Existing endpoints

| Endpoint | Contract after change |
|----------|-----------------------|
| `POST /platform/admittance/apply/save/apply` | 请求和响应字段不变；提交时执行最终原子判重。若命中重复、区域未知的保守冲突或锁等待失败，返回现有业务错误格式，不保存申请。 |
| `POST /platform/admittance/apply/equal/check` | 请求和响应字段不变；只提供当前时刻的重复提示，不保留锁、不会保证随后提交仍可成功。 |

## Decision rules

1. 主申请人与所有随行人员都使用证件号参与判重；任一人员命中即拒绝整单。
2. 已拒绝、已作废申请不参与。
3. `existing.start < incoming.end` 且 `existing.end > incoming.start` 才构成时间重叠。
4. 两边区域有至少一个完整相同的区域 ID 才构成区域重叠；区域未知时按全区域冲突。
5. 正式提交遇到并发竞争时，共享证件号的请求在获得协调锁后重新执行完整判重；仅在命中重复规则时返回重复申请结果并不保存。区域不重叠或前序事务回滚时，后续请求继续保存；首次锁行创建的 JDBC 等待超时时返回“申请处理中”业务错误。
