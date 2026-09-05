# 数据模型

复用 `SmtAdmittanceApply`、`SmtAdmittanceFellow`、`SmtDeviceAuthority`、`SmtDeviceAuthorityRelation` 与 ISC 设备任务表，无表结构改动。

- 请求：applyId 为正数 ID；fellowId 必填；vehicleId 当前明确拒绝；authIds 必选、最多100个，去重。
- 选项：applyId、startTime、endTime；fellows(id,name)、固定为空的 vehicles、authorities(id,authorityName,type,areaType)。所有长 ID 以字符串传到浏览器。
- 任务：人员 cardNo=fellow.id；仅使用 ISC 人员任务；applyId=本单；batchId=本次操作生成；startTime=申请开始减提前小时数；overTime=申请截止；action=DOWN。
- 事务：条件更新取得申请行锁后重读；只允许 status 0/3，endTime 晚于当前时间；失败回滚，不更新原审批 batch 指针。
