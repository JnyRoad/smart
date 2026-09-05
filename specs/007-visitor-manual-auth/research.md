# 研究记录

- 决策：独立 `/manage/admittance/apply/device/auth` 管理端点。原因：历史 `/admittance/**` 放行，作废已采用管理端路由。否决把写接口放在匿名访客路径。
- 决策：单申请内明确选择一名人员，避免默认为所有同行人授权。蓝图中的车辆分支因底层不支持可追溯任务而暂不开放，提交 vehicleId 明确拒绝。
- 决策：复用员工 `index.vue` 的双栏交互，访客专属弹窗固定有效期。当前 `staff_info/issueAuth.vue` 实际为任务进度页，与蓝图旧路径不符。
- 决策：新批次只追溯本次手动任务，不覆盖申请自动审批批次指针，防止缩小审批进度统计范围。
- 决策：涉密依赖缺失时明确拒绝。当前平台源码未发现考试表或证件号查询能力；不采用蓝图提及的临时跳过。
- 图谱：Tier 2，项目 `Users-lvtu-source-YUTO-smart`，初始代次 `2026-09-05T02:08:14Z`；主要 Java/Vue 文件无记录缺口，HTML 部分解析失败已直接读 5.2/5.1 源文。图关系中 getter 命中有启发式误连，实际源码优先。
- 基线：c7e8532f；已有 001–005 与并行任务 006，认领 007；主 checkout 无关改动保持不动。

- 追加源码证据：`SmtIscDeviceTaskServiceImpl.saveTask` 87起始拒绝车辆；`SmtDeviceTask` 无申请/批次字段，`SmtIscDeviceTask` 有。因此不扩底层/数据库，本次仅实现可追溯的ISC人员链路，所有不支持范围明确拒绝。
- 视觉约束：用户补充要求与 docs 原型及系统保持一致，时间在最下方。实际 Web 原型 `grantDlg`、`staffAuthDlg` 和现有员工弹窗均将有效期放在权限区之后；本次沿用该顺序及 `theme-yutong`，不引入第二套主题。
