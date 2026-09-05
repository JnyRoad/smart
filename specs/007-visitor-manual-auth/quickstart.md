# 验证与交付说明

本次实现位于 `feat/visitor-manual-auth`，规格目录为 `specs/007-visitor-manual-auth`。当前完成 ISC 公共人员手动下发；合并与发布状态以 PR 和目标环境为准，本次未操作真实数据库和设备。

## 操作入口与页面

入厂申请记录单选一张卡片，点击「通关权限」，选择本申请人员，将权限组从待选栏移入已选栏后确认。成功提示仅为「下发任务已提交」。长期供应商使用同一个人员入口。

弹窗对齐 docs Web 原型和员工「通关权限分配」：700px 宽、灰色人员信息区、等宽双栏、现有橙色主题及页脚按钮。有效期位于双栏下方、操作按钮上方，起止时间均只读；后端按申请单和既有提前量计算时间，不接收页面自定义日期。

上线前须通过现有权限管理增加并授予按钮权限码 `platform_visitor_incoming_auth`；未授予时按钮隐藏，GET/POST 均拒绝缺权限调用。本任务未写入真实权限库。

## 本地验证结果

| 检查 | 实际结果 |
| --- | --- |
| 访客列表、弹窗、请求契约及原详情返回测试 | 5 files / 22 tests passed |
| 后端新增服务、ACL 和原申请业务回归 | 5 classes / 98 tests，0 failures / errors / skipped |
| 6 个改动 Vue/JS 文件 ESLint | 0 errors / 193 warnings，含既有格式规则和文件行数等告警 |
| 管理端生产模式构建 | exit 0；使用本地占位 URL 验证编译，未部署构建产物 |
| 差异及新增文件空白检查、蓝图规格链接 | 通过 |
| 本地浏览器，真实 Vue 2 / Element UI / common.scss / theme-yutong | 1280×720 下左右栏均为 302px；有效期在双栏之后；页脚完整可见；主按钮颜色为 rgb(237,109,0) |
| 浏览器交互（合成数据） | 两个时间输入框 readOnly=true；涉密禁选；选择/移动权限后请求只有 applyId、fellowId、authIds；提示任务提交成功 |

后端 98 项包括：新增服务 18 项、新增 Controller ACL 4 项、原管理 Controller ACL 2 项，以及两个包下原 `SmtAdmittanceApplyServiceImplTest` 的 65 和 9 项。新增测试覆盖任务字段、设备去重、车辆/涉密/跨园区/禁用设备拒绝、匿名和缺权限方法代理拦截，以及第二个设备保存失败时事务回滚且不提交。

实现前已观察 RED：前端新增行为测试 4 项失败；后端因待实现 DTO/服务类缺失而 testCompile 失败。实现后再运行上述测试为 GREEN。

## 复现命令

从任务 worktree 的 `smart-ui/` 执行（先按项目 README 准备依赖）：

```bash
node node_modules/vitest/vitest.mjs run src/views/platform/visitor/incoming_record/
node node_modules/eslint/bin/eslint.js src/views/platform/visitor/incoming_record/index.vue src/views/platform/visitor/incoming_record/manualAuth.vue src/views/platform/visitor/incoming_record/_service.js src/views/platform/visitor/incoming_record/index.test.js src/views/platform/visitor/incoming_record/manualAuth.test.js src/views/platform/visitor/incoming_record/_service.test.js
VUE_APP_PLATFORM_URL=http://127.0.0.1:9 VUE_APP_BASE_URL=http://127.0.0.1:9 node node_modules/@vue/cli-service/bin/vue-cli-service.js build
```

从任务 worktree 的 `smart-module/` reactor 执行：

```bash
mvn -q -pl smart-platform/smart-platform-biz -am -Dtest=VisitorManualAuthServiceImplTest,VisitorManualAuthControllerAclTest,SmtAdmittanceApplyManageControllerAclTest,SmtAdmittanceApplyServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

从 worktree 根运行 `git diff --check`；新增文件另外检查空白和内容，避免遗漏未跟踪文件。规格脚本须显式设置 `SPECIFY_FEATURE_DIRECTORY` 指向本任务规格目录。

## 尚未接通及现场验收

- 5.1 尚无考试通过表和证件号校验能力，涉密下发明确拒绝；禁止跳过校验。后续接通 5.1 后才能验收考试通过者的涉密授权。
- ISC 任务服务目前拒绝车辆，标准设备任务无申请/批次追溯字段，所以车辆及非 ISC 设备暂不支持；提交不支持范围会整体失败，不会跳过后返回成功。
- **既有风险，尚未修复**：原 `SmtIscDeviceTaskServiceImpl.buildVisitorDownRecordQuery` / `buildVisitorDowningTaskQuery` 使用 `cardNo=id OR badge IN certNos` 且无园区约束；同证件号跨园区申请可能被旧作废链路一起回收。原 `addCard` 已使用与本次相同的身份字段，此风险不是新接口增加的匹配规则。本次未扩展修改历史回收模型；目标环境上线前须专项验证并收窄回收边界。实际跨园区误回收尚未在真实环境复现。
- **UNVERIFIED**：真实 Oracle 条件 UPDATE 行锁、并发作废与事务回滚；本地测试只验证控制流及事务管理器被正确调用。
- **UNVERIFIED**：真实 ISC 下发回执、设备端到期拒行、长期供应商现场完整流程，以及新权限码在目标环境的配置和授予。
- 经授权的集成验收应覆盖普通访客/长期供应商、多个重叠权限组、跨园区/过期/作废拒绝，并核对任务的 cardNo=fellow.id、applyId、新 batchId、固定结束时间；真实设备生效不能用本地 Mock 替代。

不更新申请的审批自动批次指针 `iscSubmitBatch`，不建立员工权限关系、不新增延迟删除，不扩展批量作废。依赖、缓存、构建产物及本地测试报告不纳入交付源码。
