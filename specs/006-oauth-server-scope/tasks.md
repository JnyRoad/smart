# Tasks: 内部开放接口恢复 server

**Input**: 本目录 spec.md / plan.md；明确执行 TDD，先看到行为断言失败再实现。

## Phase 1 - 准备
- [X] T001 建立独立 worktree，核对 PR #161 与已有规格，绑定 specs/006-oauth-server-scope/。
- [X] T002 运行 smart/ 中相关公共鉴权与 UPMS 基线测试，并完成 specs/006-oauth-server-scope/ 的需求与计划。

## Phase 2 - US1 简化授权（P1）
独立验收：server 可新增授予，新建界面只推荐 server，历史值可维护。
- [X] T003 [P] [US1] 在 smart/smart-common/smart-common-security/src/test/java/com/tce/smart/common/security/openapi/ 与 smart/smart-upms/smart-upms-biz/src/test/java/com/tce/smart/admin/ 补目录、server 授予和撤销回归，记录预期失败。
- [X] T004 [US1] 更新 smart/smart-common/smart-common-security/src/main/java/com/tce/smart/common/security/openapi/OpenApiScopeCatalog.java、相关注释；复用 UPMS 校验使 server 正常授予、细分历史仅保留。
- [X] T005 [US1] 在 smart-ui/src/const/crud/admin/client-scope-options.test.js 先补表单失败测试，再修改 smart-ui/src/const/crud/admin/client.js 与必要的管理页逻辑。

## Phase 3 - US2 内部调用与 US3 存量兼容（P1/P2）
独立验收：六入口 server 可用，细分只访问原能力，错误身份拒绝，调度默认 server。
- [X] T006 [P] [US2] 在 smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/controller/ 下补真实注解与拦截器行为测试，在 smart-module/smart-schedule/src/test/java/com/tce/smart/schedule/security/EnergyProjectionServerTokenProviderTest.java 补默认 scope 失败测试。
- [X] T007 [US2] 修改 smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/ 中照片、能耗两个控制器，以 server 为主授权；修改 smart-module/smart-schedule/ 中 OAuth 默认 scope 和注释。
- [X] T008 [US3] 执行 smart/smart-common/smart-common-security/、smart/smart-upms/smart-upms-biz/ 与 smart-module/ 负向和存量兼容回归，确保撤销及其他安全边界不退化。

## Phase 4 - 交付核验
- [X] T009 更新 docker/nacos/config/dev/common.yml、smart-schedule.yml 和 docker/.env.production.example 默认值与说明，更新 docs/yuhui-prototype/yuhui-blueprint.html。
- [X] T010 独立审查、git diff --check，记录 specs/006-oauth-server-scope/quickstart.md 中实际验证与未验证边界。

## Dependencies and parallel execution
T001/T002 → 一致性分析 → T003 与 T006 可并行。
T003 → T004，T005 独立于平台文件；T006 → T007；T004/T005/T007 → T008 → T009/T010。
主 Agent 负责 specs 状态与平台/调度/示例/蓝图；子 Agent 负责公共目录、UPMS 与 UI，文件不重叠。

## Implementation strategy
先完成可正常授予 server，再恢复调用和存量兼容，最后整体验证。用户已追加授权提交、推送并创建 PR；不执行合并或部署。

## Phase 5 - US4 照片下载园区隔离（用户已批准）
- [X] T011 [US4] 在 platform-biz/src/test/java/com/tce/smart/platform/controller/admittance/AdmittancePhotoDownloadAuthorizationTest.java 增加真实下载链路授权失败用例并观察红灯；在既有 AdmittancePhotoOpenServiceImplTest.java 补查询条件和分批契约回归。覆盖 FR-007 / FR-008 / SC-004。
- [X] T012 [US4] 更新 platform-biz/src/main/java/com/tce/smart/platform/ 下 AdmittancePhotoOpenController、AdmittancePhotoOpenService 和 AdmittancePhotoOpenServiceImpl，传 token 园区并在读图前校验目标照片的有效申请关联；适配全部测试调用。覆盖 FR-007 / FR-008。
- [X] T013 [US4] 运行照片服务、控制器及 server/历史 scope 回归并独立审查；同步蓝图和本目录契约、验收证据，注明 Oracle 现场复核及未部署边界。覆盖 FR-006 / SC-004。

依赖：续作规格一致性分析 → T011 红灯 → T012 → T013。主 Agent 独占实现及规格文件；评审 Agent 只读核对。

## Phase 6 - PR #183 注释覆盖率反馈
- [X] T014 为本 PR 已修改的 3 个测试文件中 7 个缺少方法说明的位置补充中文 JavaDoc；核对去除注释后的 Java 词法内容一致及 git diff --check。此项不改变行为，不增加测试；本地修复完成后，用户已追加授权提交并推送至 PR #183。
