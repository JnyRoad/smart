# WEB-L-003 网关与设备型号

## 网关管理

旧入口为 views/platform/facility/gateway/index.vue，建议目标为 smart-ui/src/views/platform/facility/gateway/。

| 区域 | 第一版操作基线 |
|---|---|
| 筛选 | 设备 ID、名称、连接状态。 |
| 表格 | ID、名称、IP、服务端口、状态、备注。IP 与端口仅向具备运维权限的角色展示。 |
| 行操作 | 允许入网、禁止入网、编辑、查看连接设备、删除。 |
| 弹窗 | 删除必须确认；“连接设备”显示关联设备表，不能在弹窗中隐式更改绑定。 |

旧包可见 GET/POST/PUT/DELETE /gateway、GET /gateway/allow/{id}、GET /gateway/close/{id}、GET /device/list/{gatewayId}。新接口不得把网关服务地址、密钥或认证材料交给普通后台用户；允许/禁止入网和删除必须由新 bridge/控制面的实际状态确认。

## 设备型号管理

旧入口为 views/platform/facility/type/index.vue，建议目标为 smart-ui/src/views/platform/facility/type/。

| 项目 | 第一版操作基线 |
|---|---|
| 筛选 | 型号名称、型号标识。 |
| 表格/表单 | 型号名称、标识、是否支持刷卡、指纹、密码、备注。 |
| 动作 | 添加、编辑、删除。 |
| 校验 | 名称/标识唯一性由服务端复核；能力未知不可默认勾选为支持。 |

旧包可见 GET /device/model/page、POST/PUT/DELETE /device/model。新页面中的能力勾选只编辑经过协议矩阵批准的能力，禁止前端猜测或把 UI 选择直接当作设备实际可用。

## 验收

网关与型号均需覆盖搜索、空态、编辑取消、校验错误、确认删除、无权限和关联设备弹窗。因安全做脱敏或限制的 IP、端口、能力字段要在安全差异表中说明。

