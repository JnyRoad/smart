# WEB-L-007 开门记录、密码记录与通信日志

## 入口与落点

旧页面包含：

- views/platform/lock/open_record/index.vue；
- views/platform/lock/pwd_record/index.vue；
- views/platform/lock/report/index.vue。

建议分别落在 smart-ui/src/views/platform/lock/open_record/、pwd_record/、report/，保持旧版三个独立查询入口。

## 开门记录

筛选为园区、设备、姓名、工号、开门方式（密码、指纹、卡片、远程开门）；表格为开门时间、设备名称、设备分组、开门方式、姓名、工号。导出前必须选择园区，且服务端生成、数据范围和审计必须可追溯。

## 密码修改记录

筛选为设备名称、位置、成功/失败、修改时间；表格为密码类型、设备名称、位置、状态、修改密码、创建时间、修改人。为满足安全要求，“修改密码”只能显示掩码或安全摘要；需在对版证据中记录这是受控安全差异，不能泄露旧明文。

## 设备通信日志

筛选为设备名称、日志类型（请求、响应、上报）、备注；表格为流水号、设备名称、日志类型、数据、备注、创建时间；查看动作展示 JSON。新页面仅显示经脱敏的结构化摘要，不展示原始 TCP 载荷、凭据、密钥或可重放报文。

旧包可见 GET /record/page、GET /record/export/{parkId}、GET /pwd/log/page、GET /device/log/page。

## 验收

三个页面均覆盖时间筛选、园区数据权限、空态、分页、导出前置条件、无权限、敏感字段脱敏、JSON 查看与异常内容处理。导出与查看必须保留审计证据。

