# WEB-L-005 权限管理

## 目标与落点

旧页面为 views/platform/key/auth/index.vue，建议目标为 smart-ui/src/views/platform/key/auth/。当前 Smart 的 dormitory/grant_auth 可以作为局部组件或测试参考，但不能替代旧页面的字段、条件和操作对版。

## 查询与列表

| 项目 | 第一版操作基线 |
|---|---|
| 筛选 | 园区、工号、姓名、手机号、授权设备、状态、有效时间。 |
| 列 | 工号、姓名、手机号、设备、有效期、状态、钥匙类型-状态描述、创建时间。 |
| 顶部操作 | 查询、清空、添加、导出。 |
| 行操作 | 取消授权、重新授权、编辑、删除；当 isAvailable=1 时旧页面禁用这些动作，具体状态含义须以运行态证据和新领域状态机对齐。 |

## 新增与变更

1. 按姓名或工号检索候选人员；
2. 密码、卡、指纹均为空时，不能选择人员进行授权；
3. 选择设备，并选择指定有效期或长期有效；
4. 取消授权必须显示明确确认；
5. 重新授权仅可调整有效期，开始日期不得早于当天；
6. 编辑、删除、取消、重新授权的结果必须回到同一授权记录并显示真实命令状态。

新页面要保持这些表单步骤、校验和确认，但必须以 smart-lock 的授权意图、批次和设备确认模型执行。提交不是“已授权”；待投递、待设备确认、失败和人工核验均需要可查询、可理解的状态。

## 历史接口证据

旧包可见 GET /permissions/page、POST /permissions/add/device、POST /permissions/batch、PUT /permissions、DELETE /permissions/{id}、PUT /permissions/cancelAuth/{id}、POST /permissions/reAuth、GET /person/search/{keyword}、GET /device/page、GET /permissions/export/{parkId}。

## 验收

每种写操作至少测试：有权限成功受理、无权限拒绝、无凭据不可选择、非法日期、设备离线/不支持、重复提交、设备未确认、部分失败和重新授权。测试需证明取消授权没有在设备确认前被显示为物理撤销完成。

