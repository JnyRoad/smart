# 访客手动下发接口

网关前缀 `/platform`；以下路径均要求登录及 `platform_visitor_incoming_auth`，并校验用户园区范围。

## GET /manage/admittance/apply/device/auth/options?applyId=...

`Result.data`：

```json
{"applyId":"101","startTime":"2026-09-05 08:00:00","endTime":"2026-09-06 18:00:00","fellows":[{"id":"201","name":"测试访客"}],"vehicles":[],"authorities":[{"id":401,"authorityName":"园区公共权限","type":1,"areaType":0}]}
```

startTime 为申请开始减现有 putOffsetHour；endTime 固定，均只读。type 与现有权限类型保持一致，人员支持 1；车辆 3 当前不开放；后端为最后校验权威。只返回当前申请中照片 ID 非空且非空白的人员和当前园区人员权限组；无可用人员时 fellows 返回空数组，不返回证件号等无关信息。提交时再次校验照片，防止查询后数据变化。

## POST /manage/admittance/apply/device/auth

```json
{"applyId":"101","fellowId":"201","authIds":[401]}
```

当前 fellowId 必填且必须为正数；vehicleId 输入明确拒绝，二者同时传也拒绝。无日期、无覆盖/追加参数。返回 `Result.data` 批次号字符串。业务校验失败使用现有 Result 异常约定；非零 code 前端显示 msg，网络异常展示错误。提交成功只表示任务入队。

## 部署前条件

在现有权限管理中为入厂申请记录增加/授予按钮权限码 `platform_visitor_incoming_auth`。未授予者拒绝操作；本任务不写真实权限库。涉密校验依赖 5.1，当前拒绝并说明原因。

## 设备限制

仅可向支持申请单/批次追溯的 ISC 人员设备下发。车辆和非 ISC 设备当前不可用；包含不支持设备的请求整体拒绝，不会跳过后返回成功。vehicles保留空数组以明确当前无可选车辆。
