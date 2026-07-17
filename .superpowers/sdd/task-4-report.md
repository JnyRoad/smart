# Task 4 前端状态与运维闭环报告

## 状态

完成；已基于 `e4b6b96a fix(platform): redact security failure metadata` 复核。前端不拼接、不推导敏感字段，只显示后端当前批次返回的脱敏文本。

## 实现

- `doSend` 已切换为 `POST /security/auth/apply/{id}/dispatch`，仅 HTTP 202 视为已受理；GET 兼容端点未被前端调用。
- 列表页在命令未返回时禁用同一申请行；202 后显示批次、刷新列表并启动单一当前批次轮询。网络/业务错误、终态和组件销毁都会停止轮询。
- 列表补齐待下发、下发中、成功、部分失败/失败筛选，并展示当前批次、待处理、成功、失败、取消；取消数量独立显示但不与 `failCount` 相加。
- 详情页展示当前批次、待处理、成功、失败、取消及 `failureReasons` 的工号、姓名、设备、终态、原因文本；不新增重置失败人员按钮。
- 新增 runbook：迁移预检、三表关联 SQL、旧任务接管、迟到 ISC 结果隔离、设备离线重发、发布/灰度/回退顺序。

## TDD 与验证

- RED：新增 `dispatch-flow.test.js` 后，旧实现因 GET 端点、缺少行级禁用/轮询方法而失败。
- GREEN：`pnpm test` 通过，72 个测试文件、381 个测试。
- `pnpm lint` 退出 0；仓库既有 25,415 条 warning，本任务四个前端文件单独检查为 0 errors。
- 原始 `pnpm build` 因缺 `VUE_APP_PLATFORM_URL` 与 `VUE_APP_BASE_URL` 失败；使用本地占位 URL 重跑后生产构建通过。

## 自审

- 全局 Axios 保持 30 秒，未提高超时；202 从不被当作设备下发成功。
- 轮询终态使用 `totalCount`、待处理数和 `successCount + failCount`；`canceledCount` 是 `failCount` 子集，绝不重复计数。

## 复审修复：202 与轮询竞态

- Axios 响应拦截器现将全部 HTTP 2xx 视为成功，HTTP 202 不再先弹全局错误；非 2xx 和 `code === 1` 的原错误路径保持不变。
- 列表与详情轮询均加入递增 generation/token、当前批次身份和组件销毁标识；每个 await 返回、错误处理、页面写入和定时器注册前均校验身份，旧请求不能清空、刷新或重启新批次。
- RED：新增 202、组件销毁后返回、旧批次失败不停止新批次三例，旧实现得到三个预期失败。
- GREEN：聚焦回归后 `pnpm test` 通过，72 个测试文件、384 个测试。
