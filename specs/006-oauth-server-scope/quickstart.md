# 验证指南

前提：Java 8、Maven 和已有依赖缓存；测试仅使用 Mock，无需生产服务。

1. 从 smart/ 执行 common-security、UPMS 的相关 JUnit 测试，包括目录、拦截器、客户端服务与控制器。
2. 将本任务 common-security 构建结果供业务 reactor 使用，再从 smart-module/ 执行照片、能耗控制器、调度 token/provider/task 回归。
3. 从 smart-ui/ 运行客户端目录、表单和 API 的 Vitest 用例。
4. git diff --check；核对蓝图仅标已实现未合并；检查模板默认 scope=server。

真实环境验收待部署授权后执行：新建/编辑 server 应用，获取 client_credentials token，访问六个入口；核对未开放路由仍拒绝。不得将本地 MockMvc 结果写成已上线。

## 本次执行证据

- 基线：common-security/UPMS 34 项、platform/schedule 27 项通过。
- TDD：平台新增行为/契约共 4 个断言失败，调度默认 scope 1 个断言失败；均符合旧实现限制。
- 业务回归：从 smart-module/ 使用隔离 Maven 仓库运行如下命令，退出码 0：

```bash
mvn -B -o -Dmaven.repo.local=/private/tmp/smart-oauth-server-scope-m2-20260905 \
  -pl smart-platform/smart-platform-biz,smart-schedule -am \
  -Dtest=OpenApiServerScopeMockMvcTest,EnergyProjectionControllerTest,AdmittancePhotoOpenControllerTest,EnergyProjectionServerTokenProviderTest,SmartMeterTimerTaskEnergyProjectionTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

隔离仓库中 com.tce 工件使用本任务副本，common-security 从本任务源码构建安装且 SHA-256 与被测依赖一致；第三方依赖复用本机缓存。上述路径仅为本次证据，重跑可替换为当前任务隔离目录。
- 示例 YAML 解析通过，蓝图 HTML 可解析且目标条目 ID 唯一。
- 实际 OAuth 签发、网关、浏览器交互、真实园区/照片/能耗数据与生产运行均未验证；未执行提交、推送、合并、部署或数据库变更。

### server 恢复阶段回归

- 基础鉴权及 UPMS：52 项通过（OpenApiScopeCatalog、Interceptor、MockMvc、AuthenticationAdapter、SmartSecurityInnerAspect、客户端 service/controller）。从 smart/ 执行：

```bash
mvn -B -o -pl smart-common/smart-common-security,smart-upms/smart-upms-biz -am \
  -Dtest=OpenApiScopeCatalogTest,OpenApiInterceptorTest,OpenApiInterceptorMockMvcTest,OpenApiAuthenticationAdapterTest,SmartSecurityInnerAspectTest,SysOauthClientDetailsServiceImplTest,OauthClientDetailsControllerTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

- 平台与调度：36 项通过；合计后端 88 项。
- 管理端：从 smart-ui/ 执行 `pnpm test -- --runInBand`，79 个文件、402 项通过。
- 公共目录 TDD：修改断言后 15 项中 2 项按预期失败（server 目录状态、历史细分兼容）；实现后通过。
- UI TDD：`pnpm exec vitest run src/const/crud/admin/client-scope-options.test.js`，原实现 3 项中 1 项失败（返回客户端未持有的历史照片权限），筛选修复后通过。

### server 恢复阶段独立审查结论

六个入口授权、历史兼容、调度、目录/UPMS/UI、示例和蓝图未发现本次引入的明确问题。
发现一个基线已有风险：`AdmittancePhotoOpenController.download` 调用
`AdmittancePhotoOpenServiceImpl.loadPhoto`，后者直接按 photoId 查询图片，不校验 token 园区。
因此本次不宣称下载具备园区隔离；保留的是 pending 清单已有园区过滤。本次不扩大到新增下载限制。
该风险有源码证据，未对真实数据进行请求验证。


### 用户追加授权：照片下载园区校验

- 用户确认补齐照片下载园区校验，已修复上一阶段记录的下载风险；统一 server 与历史 scope 兼容保持不变。
- 初次写测试时命令误用了 reactor 工作目录作为仓库根，测试文件创建失败；已纠正到 worktree 根写入后重新执行。该空测试构建不计入 TDD 证据。
- T011 红灯：真实控制器 / OAuth 适配器 / 拦截器 / 照片服务组合测试 6 项中 5 项失败，均为期望 404、实际 200。随后补充精确查询绑定、空授权、孤立关联及分批用例，17 项中 9 项失败、0 errors，说明失败来自缺失的业务校验。
- T012 绿灯：平台 37 项、调度 16 项，合计 53 项通过，0 failures / errors / skipped。命令从 smart-module/ reactor 执行：

```bash
mvn -B -o -Dmaven.repo.local=/private/tmp/smart-oauth-server-scope-m2-20260905 \
  -pl smart-platform/smart-platform-biz,smart-schedule -am \
  -Dtest=AdmittancePhotoDownloadAuthorizationTest,AdmittancePhotoOpenServiceImplTest,AdmittancePhotoOpenControllerTest,OpenApiServerScopeMockMvcTest,EnergyProjectionControllerTest,EnergyProjectionServerTokenProviderTest,SmartMeterTimerTaskEnergyProjectionTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

- 测试使用数据库服务 Mock，验证了真实应用层授权流程及 MyBatis-Plus 的参数化查询条件，不等同真实 Oracle 执行。
- FileReceiver 现有 404 → NOT_FOUND → 跳过处理已只读核验，无须修改客户端或新增请求参数。
- 发布前现场只读复核：确认目标 schema 的 SMT_ADMITTANCE_FELLOW(FELLOW_PHOTO_ID, VISITOR_ID) 与 SMT_ADMITTANCE_APPLY(ID, PARK_ID, STATUS, END_TIME, APPLY_TYPE) 字段、约束、现有索引和统计信息；使用实际绑定值取得生成查询的执行计划，核对按照片定位及申请 ID 过滤的成本。不得凭本地 Mock 测试承诺性能；不执行统计刷新或新增索引。
- 真实 Oracle、网关 token 签发链路及生产下载尚未验证；未执行提交、推送、合并、部署或真实数据库写入。
- T013 独立复审：本轮照片增量无阻断问题；认证上下文取园区、有效申请查询、拒绝前不读图、旧一参调用移除及 FileReceiver 404 兼容已核验。`git diff --check` 与蓝图目标条目解析/唯一性检查通过。
- 规格续作一致性分析：8 个功能需求与 4 个验收标准均由 T001–T013 覆盖，追加 FR-007/FR-008/SC-004 对应 T011–T013；无未映射任务或需求冲突，Oracle 现场验证限制保留。

### PR 交付

用户追加授权提交、推送并创建 PR；变更已提交 [PR #183](https://github.com/JnyRoad/smart/pull/183)，目标为 `JnyRoad/smart:main`，分支为 `fix/restore-oauth-server-scope`。蓝图已回填 PR 编号；未合并、未部署。上述测试结果来自创建 PR 前的本地验证，不代表远端 CI 通过。暂存后发现的规格 Markdown 行尾空格已清除，最终整体差异重新通过空白检查。

### PR 注释检查反馈

- 核对 PR #183 反馈对应的 head `1277b9d8c4b7c605a8db020ab51f5e023f5aff2a`：无行内评审线程，CodeRabbit 唯一检查警告为 Docstring Coverage 78.65%，要求 80%。来源：[评审检查说明](https://github.com/JnyRoad/smart/pull/183#issuecomment-5549009317)。
- 对该 PR 已修改的 OpenApiInterceptorTest、OpenApiScopeCatalogTest、OauthClientDetailsControllerTest 共补齐 7 处中文 JavaDoc，说明主授权、历史兼容、未知权限拒绝及目录契约。
- 与修复前 HEAD 比较，3 个 Java 文件去除注释后的全部代码词法内容一致，git diff --check 通过；纯注释修改未重复运行业务测试。
- 本地修复完成后，用户已追加授权提交并推送至 PR #183；不宣称远端 Docstring Coverage 已通过，具体百分比待 CodeRabbit 复检。
