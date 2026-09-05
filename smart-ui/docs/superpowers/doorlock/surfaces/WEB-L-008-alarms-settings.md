# WEB-L-008 告警记录与告警设置

## 目标与落点

旧页面为 views/platform/lock/alarm_record/index.vue 与 views/platform/setting/alarm/index.vue。建议目标分别为 smart-ui/src/views/platform/lock/alarm_record/ 与 smart-ui/src/views/platform/setting/alarm/。

## 告警记录操作基线

| 项目 | 第一版要求 |
|---|---|
| 筛选 | 时间、处理状态、报警类别（错误、防撬、低电量）、来源设备。 |
| 列表 | 告警类型、来源、时间、状态和详情入口。 |
| 批量操作 | 多选“忽略报警”“已处理”；未选择对象时给出显式提示，不能静默成功。 |
| 详情 | 显示处理结论与必要的设备/事件摘要，不暴露原始敏感报文。 |

## 告警设置操作基线

按园区维护低电量阈值、低电量开关、离线分钟数和邮件收件人。离线分钟数必须是整数；保存、取消、加载失败和无园区权限均要有可辨识反馈。

旧包可见 GET /message/record/page、PUT /message/record/batch/status、GET/POST/PUT /warn/config、GET /warn/config/{parkId}。

## 验收

覆盖多选/未选、批量部分失败、告警详情、园区切换、整数校验、保存取消、越权与告警状态刷新。忽略或已处理只代表工单处置状态，不得隐藏设备仍异常的事实。

