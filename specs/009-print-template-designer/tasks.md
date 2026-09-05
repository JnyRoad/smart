# Tasks: 厂牌与访客模板设计器

**Input**: [spec.md](spec.md)、[plan.md](plan.md)、[data-model.md](data-model.md)、[契约](contracts/print-api.md)、[research.md](research.md)、[quickstart.md](quickstart.md)

**Status**: 批次1设计器与批次2绑定、真实人员预览、冻结任务、Windows 客户端、人工恢复及切换代码已实现，软件组独立复审与整体验证已完成，最终整分支代理复审未返回有效报告，需在PR中继续审查。自动化与模拟驱动结果不等于真实环境验收。T005/T006/T012/T037/T039/T051/T025/T031/T036/T043/T045 保留目标 Oracle、登录读回、全入口权限联调、DHR 映射或实机验收缺口；功能默认关闭，供应商实体卡来源未确认时拒绝创建。实际证据见 [quickstart.md](quickstart.md)。

**测试先行**：每组行为实现必须先运行对应测试并观察预期失败，完成最小实现后再转绿。`[P]` 仅表示同阶段、依赖满足且文件不重叠时可并行。已勾选任务对应源码已创建，其余源码路径为规划路径；新增模块首次创建时同步根 README/AGENTS。

## Phase 1: Setup

- [x] T001 核实并锁定 pdfme 同版本组件及可商用中文字体，记录来源/许可证/构建环境到 `specs/009-print-template-designer/research.md`；核对现有 Oracle 测试环境与发布迁移机制，不执行真实 DDL。（FR-017/FR-018，SC-008）
- [x] T002 编写 DOM 挂载/卸载、中文及固定一页/两页的兼容行为测试并先失败：`smart-ui/src/components/print/PdfmeCompatibility.test.js`、`smart-print-renderer/test/compatibility.test.mjs`。（FR-002/FR-004）
- [x] T003 按 T002 建立最小集成与锁文件，验证现有管理端构建；必要时采用 plan 指定的局部预构建方式：`smart-ui/src/components/print/PdfmeHost.vue`、`smart-ui/package.json`、`smart-print-renderer/package.json`；在 `README.md`、`AGENTS.md` 登记渲染模块并记录测量值到 `specs/009-print-template-designer/research.md`。（FR-002/FR-004/FR-017）

## Phase 1a: 单面独立模板修订（先于 T004 执行）

T001—T003 是前次兼容验证历史；本次改变模板语义，不把原“两页模板”规则带入新实现。

- [x] T047 [US1] 先更新单面宿主、独立保存和打印组合行为测试并观察断言失败：`smart-ui/src/components/print/PdfmeCompatibility.test.js`、`smart-ui/src/components/print/SingleTemplateWorkspace.test.js`、`smart-print-renderer/test/compatibility.test.mjs`；覆盖厂牌模板也仅一页、正背面独立保存、两面同名字段不互相覆盖、缺背面/错尺寸/访客背面拒绝。（FR-001/FR-003/FR-004/FR-021）
- [x] T048 [P] [US1] 实现 T047 的单面宿主和合成模板工作台：`smart-ui/src/components/print/template-shape.js`、`smart-ui/src/components/print/PdfmeHost.vue`、`smart-ui/src/components/print/SingleTemplateWorkspace.vue`、`smart-ui/scripts/print-compatibility/index.html`；提供单模板切换、内存保存/重开、正反面选择、组合关联/恢复，不创建业务数据或真实打印任务。（FR-001/FR-003/FR-021）
- [x] T049 [P] [US1] 实现 T047 的独立单页渲染及打印组合：`smart-print-renderer/src/render.mjs`、`smart-print-renderer/scripts/render-sample.mjs`；厂牌组合两面各自渲染后合并，锁定每面版本标识并校验模式/面数/尺寸，访客单页。（FR-003/FR-004/FR-010/FR-021）
- [x] T050 [US1] 运行本次前端/渲染器测试和兼容构建，在真实浏览器验证只有一张编辑画布、切换模板不互相覆盖及组合恢复；同步 `specs/009-print-template-designer/quickstart.md`、`research.md`、`docs/yuhui-prototype/yuhui-blueprint.html` 与模块 README 的实际状态。（FR-001/FR-003/FR-021，SC-001/SC-002）

## Phase 2: Foundational

- [x] T004 编写园区/操作权限、资源白名单、越权照片及受控文件访问失败测试：`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/print/PrintAccessPolicyTest.java`、`smart-print-renderer/test/resource-boundary.test.mjs`。（FR-014/FR-020，SC-006）
- [ ] T005 实现 T004 的服务端授权、字段/资源校验及最小内部渲染边界：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/print/PrintAccessPolicy.java`、`smart-print-renderer/src/resource-policy.mjs`；在 `specs/009-print-template-designer/data-model.md` 记录 Oracle 核对与迁移归属，持久化实现使用项目实际发布机制，不恢复人工脚本目录。（FR-014/FR-020）

**Checkpoint**: 批次1仅依赖软件基础；Windows/实机前置工作在 US3/US4/US5，不能阻塞设计器独立开发，也不能据此跳过最终实机验收。

## Phase 3: US1 模板管理、版本和基础设计器（批次1）

**Goal**: 后勤可独立维护独立单面模板及正反面组合并预览；线上打印不变。

**Independent Test**: SC-001/SC-002 的合成数据场景，不连接打印机。

- [ ] T006 [P] [US1] 编写每次仅编辑一份单面模板、系统独立组合管理页及刷新/重新登录后读回关联、访客无背面、全部基础组件保存还原、编辑冲突提示及预览测试：`smart-ui/src/components/print/PdfmeDesigner.test.js`、`smart-ui/src/views/platform/print/templates/index.test.js`、`smart-ui/src/views/platform/print/pairs/index.test.js`。（FR-022/FR-001/FR-002/FR-004，SC-001）
- [x] T007 [P] [US1] 编写草稿并发、单面独立版本及组合引用不可变版本、组合并发/引用校验与服务重启后持久读回、发布/回滚原子性、被引用历史不可覆盖、越权及面数/尺寸/必填校验失败测试：`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/print/PrintTemplateServiceTest.java`。（FR-001/FR-003/FR-004/FR-014/FR-015，SC-002）
- [x] T008 [US1] 实现 T006 的设计器业务壳和模板页面/API封装：`smart-ui/src/components/print/PdfmeDesigner.vue`、`smart-ui/src/views/platform/print/templates/index.vue`、`smart-ui/src/api/platform/print/templates.js`；组合独立放在 `smart-ui/src/views/platform/print/pairs/index.vue` 及 `smart-ui/src/api/platform/print/pairs.js`，画布不承担关联配置。（FR-022/FR-001/FR-002/FR-003/FR-004）
- [x] T009 [US1] 实现 T007 的单面模板与版本及 PrintTemplatePair 持久化、模板CRUD/发布/回滚、组合CRUD及审计：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/print/PrintTemplateController.java`、`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/print/PrintTemplateService.java`；entity/mapper/DTO按 plan 与数据模型落在现有分层，不新增业务微服务。（FR-001/FR-003/FR-004/FR-014/FR-015） 系统组合须持久保存，关闭设计器及服务重启后读回原修订。（FR-022）
- [x] T010 [US1] 编写服务端预览鉴权契约测试 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/print/PrintPreviewServiceTest.java`、`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/print/PrintObjectStoreTest.java`，以及真实 PDF 渲染的页数、纸张尺寸、中文缺字、超长内容、码区/资源/模板篡改失败测试：`smart-print-renderer/test/render.test.mjs`。（FR-002/FR-004/FR-020，SC-001/SC-002）
- [x] T011 [US1] 实现 T010 的组件注册、受控字体、固定业务面映射、渲染后校验与私有接口：`smart-print-renderer/src/render.mjs`、`smart-print-renderer/src/server.mjs`；预览接口接入 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/print/PrintPreviewController.java`。（FR-002/FR-004/FR-014/FR-020）
- [ ] T012 [US1] 执行批次1独立验收，补齐菜单权限配置方案及保存重开/发布回滚证据：`specs/009-print-template-designer/quickstart.md`；验证既有 `smart-ui/src/views/platform/visitor/qrCode_new/index.vue` 未被新链路接管。（SC-001/SC-002/SC-006） 验收系统组合经刷新、重新登录和服务重启后读回同一关联，明确区分内存演示与持久化结果。（FR-022）

### 前次批次1收口说明（历史证据，批次2状态见页首）

- T004 的静态资源及人员照片边界已实现；人员照片必须通过可信来源的必填 personPhoto 绑定冻结，不能使用静态上传接口绕过。
- T005 的服务端授权、私有对象存储与字段/资源校验已实现；保持未勾选以保留独立 Oracle/schema、容量/清理与发布迁移执行器的未决项。
- T006 的自动化组件和管理页测试已通过，且真实浏览器已验证单面画布及保存等待期间的编辑冻结；保持未勾选以保留真实登录后端的重新登录读回验证。
- T007/T009 的并发、不可变版本、组合持久化和重启读回以隔离 H2 文件库运行真实 Mapper/事务验证，不能证明 Oracle 兼容；真实环境统一归 T012 验收。
- T010 的预览服务及 MockMvc 授权用例落在上述 service 测试目录；真实 Java→Node PDF 另见 `PrintRendererLiveTest.java`。Node 尺寸容差的真实 PDF 用例另见 `smart-print-renderer/test/page-size-compatibility.test.mjs`。
- T012 已交付权限和环境配置说明，旧访客入口未改；Oracle 迁移、登录后端浏览器保存重开/发布回滚/组合重启读回仍未执行。
- 本轮验证：UI 94 文件/481 项、Java 40 项（无跳过）、Node 39 项通过；UI 正式构建成功，打印范围 lint 为 0 错误、145 警告。详细边界与证据见 [quickstart.md](quickstart.md)。

## Phase 4: US2 职级/单位绑定与唯一模板解析（批次2）

**Goal**: 按真实业务关系及正式员工职级自动选唯一合法模板。

**Independent Test**: 绑定优先级、默认模板、保密带码和歧义反例。

- [ ] T051 [US2] 在实现正式员工解析前核实员工级/职员级/经理级等名称对应的 DHR 字典代码和同步来源（用户已确认 EHR 停用），记录到 `specs/009-print-template-designer/research.md`；候选为现有 `SmtStaff.jcheId/jcheName`，不可未经验证替换成岗位、福利层次、HR DTO 或放行人级别枚举。确认来源、园区适用和缺失行为后交给 T013/T015，未确认时保持该入口不可用，不读写未经授权的真实库。（FR-023）
- [x] T013 [P] [US2] 编写厂牌绑定组合/访客绑定单模板的互斥目标、员工级/职员级/经理级不同组合、多职级共用组合、未知/缺职级拒绝、职级集合重叠与跨职级默认拒绝、新发布未关联模板可手选、EXPLICIT 无需预存组合、自动未命中/歧义可手选、已有推荐可改选且仍校验人员资料/分类/权限、单位/供应商精确匹配、有效期、普通默认、保密带码、并发冲突及跨园区测试：`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/print/PrintTemplateResolverTest.java`。（FR-005/FR-014）
- [x] T014 [P] [US2] 编写绑定界面的动态职级选项、多选适用职级、系统关联保存读回、自动匹配依据及解析失败提示契约测试：`smart-ui/src/views/platform/print/bindings/index.test.js`。（FR-005）
- [x] T015 [US2] 实现 T013 的绑定CRUD/按职级唯一解析、职级选项接口和服务端分类/单位关联（按园区已确认映射才启用，实际字典 T051 尚待现场确认）：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/print/PrintTemplateResolver.java`、`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/print/PrintBindingController.java`。（FR-005/FR-014/FR-023）
- [x] T016 [US2] 实现 T014 的系统适用规则管理、职级配置、冲突和未匹配提示：`smart-ui/src/views/platform/print/bindings/index.vue`、`smart-ui/src/api/platform/print/bindings.js`。（FR-005）

## Phase 5: US3 手动双面、任务和客户端基础（批次2）

**Goal**: 一人一卡、一次一面，人工翻面后继续，异常可恢复。

**Independent Test**: 模拟驱动状态/重复/断线 + 实卡10人手动双面。

- [x] T017 [P] [US3] 编写不调用卡号登记/写卡/编码/权限变更接口、已登记卡号缺失/关系越权拒绝、打印机档案手动配置、两份模板版本及组合修订号、手选无绑定可建任务/缺确认拒绝/确认后组合修订变化拒绝、实际选择及确认人审计且不自动改绑定、正式员工职级和匹配依据冻结、晋升后仅新任务重新解析、翻面等待禁改背面、快照冻结、请求幂等、模式冻结、设备独占、前后面转移、结果不明及人工核对续打证据/旧命令迟到事件测试：`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/print/PrintJobServiceTest.java`。（FR-006/FR-007/FR-008/FR-010/FR-011/FR-015/FR-019）
- [x] T018 [P] [US3] 编写客户端命令意图落盘、重启去重、提交结果不明不重放、制品hash/设备身份校验测试：`smart-print-client/tests/PrintCommandRecoveryTests.cs`。（FR-010/FR-011/FR-014/FR-019/FR-020）
- [x] T019 [P] [US3] 编写打印页自动匹配/手动选择、未关联模板候选可见、无匹配或有推荐时均可手选、改选需重新核对预览、批量逐人展示职级和差异、当前人员与双面预览、出卡确认/翻面按钮、防双击、刷新恢复、预览后数据/模板/设备变更拒绝提交并重看预览、逐人批量测试：`smart-ui/src/views/platform/print/jobs/Workbench.test.js`。（FR-008/FR-009/FR-011）
- [x] T020 [US3] 实现 T017 的任务、面进度、attempt/event、设备占用、状态机、领取/回执和人工恢复API：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/print/PrintJobService.java`、`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/print/PrintJobController.java`、`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/print/PrintClientController.java`、`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/print/PrintPrinterController.java`（先交付手动所需档案API）。（FR-006/FR-007/FR-008/FR-010/FR-011/FR-015/FR-019）
- [x] T021 [US3] 在 `specs/009-print-template-designer/research.md` 核实并锁定 .NET/Windows、PDF页面渲染器及再分发许可，确认本地日志目录ACL和设备身份配置；未知许可组件不引入。（FR-017，SC-008）
- [x] T022 [US3] 实现 T018 的 Windows 主程序、受控领取、持久去重日志及页面渲染接口：`smart-print-client/src/Program.cs`、`smart-print-client/src/PrintCommandJournal.cs`、`smart-print-client/src/TaskPoller.cs`；新增 `smart-print-client/README.md` 并同步根 `README.md`、`AGENTS.md`。（FR-010/FR-011/FR-014/FR-019/FR-020）
- [x] T023 [US3] 编写并运行手动模式每次只提交一面、无写卡/编码调用、驱动页选择、打印机身份/介质校验失败测试：`smart-print-client/tests/PrintAdapterTests.cs`。（FR-006/FR-008/FR-009）
- [x] T024 [US3] 实现 T023 的 HiTi 官方驱动单面适配和校准档案：`smart-print-client/src/Adapters/HiTiPrintAdapter.cs`；实现 T019 的模板自动/手选入口和手动翻面工作台 `smart-ui/src/views/platform/print/jobs/manual.vue` 和任务API封装 `smart-ui/src/api/platform/print/jobs.js`。（FR-008/FR-009/FR-011）
- [ ] T025 [US3] 完成模拟异常矩阵及获准的手动实机验收，记录型号/Windows/驱动/介质/放卡方向和10位样本：`specs/009-print-template-designer/quickstart.md`。（SC-003/SC-005）

## Phase 6: US5 访客单面（批次2）

**Goal**: 覆盖现有访客与随行人员的单面凭条。

**Independent Test**: 10张以上实机凭条，分类、照片、二维码正确。

- [x] T026 [P] [US5] 编写普通/VIP、保密/非保密、随行人员数据冻结、未关联单面模板可手选且保密缺码仍拒绝、缺字段拒绝测试：`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/print/PrintSubjectSourceTest.java`。（FR-005/FR-010/FR-013）
- [x] T027 [P] [US5] 编写 Brother 只打印一面、58mm区域/介质/黑红能力校验、回执丢失和缺纸测试：`smart-print-client/tests/BrotherPrintAdapterTests.cs`；编写自动匹配/未关联单面模板手选/预览确认及入口不出现翻面流程测试 `smart-ui/src/views/platform/print/jobs/Workbench.test.js`。（FR-004/FR-006/FR-011/FR-013）
- [x] T028 [US5] 实现 T026 的访客/随行人员解析与快照：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/print/SqlPrintSubjectSource.java`。（FR-005/FR-010/FR-013）
- [x] T029 [US5] 实现 T027 的受控PDF页面图像→固定介质LBX→b-PAC通道：`smart-print-client/src/Adapters/BrotherPrintAdapter.cs`、`smart-print-client/resources/brother/visitor-image.lbx`；客户端组件安装遵循官方许可。（FR-004/FR-006/FR-011/FR-013/FR-017）
- [x] T030 [US5] 实现支持自动匹配/手动选择未关联模板及预览确认的单面访客工作台与现有入口的新链路调用接口，暂由关闭的切换开关隔离：`smart-ui/src/views/platform/print/jobs/visitor.vue`、`smart-ui/src/views/platform/visitor/qrCode_new/index.vue`。（FR-013/FR-016）
- [ ] T031 [US5] 获准后完成QL-800真实样本、裁切/黑红/照片/扫码及缺纸恢复，记录到 `specs/009-print-template-designer/quickstart.md`。（SC-004/SC-005）

## Phase 7: US4 自动双面（批次2）

**Goal**: 同一组正面、背面模板通过翻面模块自动完成同卡两面。

**Independent Test**: 带模块实机10人双面，缺模块反例在提交前拒绝。

- [x] T032 [P] [US4] 编写模式能力校验、活动任务配置及正式员工职级/匹配依据冻结、自动双面可用未预关联的手选两份版本、两页同任务及未知结果测试：`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/print/PrintJobServiceTest.java`、`smart-print-client/tests/PrintAdapterTests.cs`。（FR-006/FR-007/FR-010/FR-012）
- [x] T033 [P] [US4] 编写打印机配置/手动自动模式选择、不可用提示及任务中禁改模式测试：`smart-ui/src/views/platform/print/printers/index.test.js`。（FR-006/FR-007） 同时验证自动翻面页面可自动匹配或手选未关联的两份已发布模板，改选后重新核对预览。（FR-024）
- [x] T034 [US4] 实现 T032 的驱动自动双面/页序/方向与完成证据处理：`smart-print-client/src/Adapters/HiTiPrintAdapter.cs`、`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/print/PrintJobService.java`；明确未知结果不能当两面完成。（FR-007/FR-012）
- [x] T035 [US4] 实现 T033 的打印机档案API与配置页：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/print/PrintPrinterController.java`、`smart-ui/src/views/platform/print/printers/index.vue`、`smart-ui/src/api/platform/print/printers.js`。（FR-006/FR-007）
- [ ] T036 [US4] 获准后完成带翻面模块实卡验收，证明两页实际落在同一卡上，记录10位样本和方向/页序：`specs/009-print-template-designer/quickstart.md`。（SC-003/SC-005）

## Phase 8: US6 权限、审计、联合切换和回退（批次2）

**Goal**: 全链路受控，全部验收后统一切换。

**Independent Test**: 越权反例、活动任务回退和新旧入口演练。

- [ ] T037 [P] [US6] 补齐模板/绑定/预览/制品/设备/任务/恢复的端到端ACL、伪造设备、篡改制品及脱敏审计测试：`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/print/PrintAccessPolicyTest.java`、`smart-print-client/tests/RuntimeOptionsTests.cs`。（FR-014/FR-015/FR-020，SC-006）
- [x] T038 [P] [US6] 编写切换默认关闭、旧访客路径、回退停止创建、在途任务不重放、新厂牌无旧通道停用的测试：`smart-ui/src/api/platform/print/cutover.test.js`、`smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/print/PrintCutoverServiceTest.java`。（FR-016，SC-007）
- [ ] T039 [US6] 完成 T037 覆盖的全入口权限、设备绑定、审计脱敏与安全错误语义：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/print/PrintAccessPolicy.java`、`smart-print-client/src/TaskPoller.cs`。（FR-014/FR-015/FR-020）
- [x] T040 [US6] 实现 T038 的运行时切换状态和任务创建门禁、新旧入口分流：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/print/PrintCutoverService.java`、`smart-ui/src/api/platform/print/cutover.js`、`smart-ui/src/views/platform/visitor/qrCode_new/index.vue`。（FR-016）
- [x] T041 [US6] 编写逐卡工作台审计/恢复/取消结果展示行为测试并先失败：`smart-ui/src/views/platform/print/jobs/JobHistory.test.js`。（FR-011/FR-015）
- [x] T042 [US6] 实现 T041 的任务历史与人工恢复界面：`smart-ui/src/views/platform/print/jobs/JobHistory.vue`，遵循契约可执行动作和实卡确认，不自动重打。（FR-011/FR-015）
- [ ] T043 [US6] 在授权测试环境演练统一切换/回退，核对蓝图 6.1 等独立规格的外部验收证据和每种打印类型覆盖，记录到 `specs/009-print-template-designer/quickstart.md`；独立规格未交付、缺任一实机模式或关联验收不得标通过。（FR-016，SC-006/SC-007）

## Phase 9: Polish & Cross-Cutting Concerns

- [x] T044 整理组件/字体/SDK与客户端安装分发清单、设备校准步骤和运维文档：`smart-print-client/README.md`、`smart-print-renderer/README.md`、`specs/009-print-template-designer/research.md`；核实所有商用及再分发条款。（FR-017，SC-008）
- [ ] T045 运行影响范围内的前后端/渲染器/Windows测试与构建，核对所有SC、FR及真实Oracle/实机证据，回写 `specs/009-print-template-designer/quickstart.md` 和本任务清单；未验证项保持未完成。（SC-001/SC-002/SC-003/SC-004/SC-005/SC-006/SC-007/SC-008）
- [ ] T046 根据真实交付状态更新 `docs/yuhui-prototype/yuhui-blueprint.html`，审查新模块入口和迁移/回退资料；提交、PR、生产切换仅按当时已有授权执行，不因任务完成自动部署。（FR-016/FR-018）

## Dependencies & Execution Order

- 本次修订：更新规格并分析通过后，T047 测试先失败，T048/T049 可按 UI/Node 边界并行，T050 收口；先完成本段再继续 T004。
- 批次1：T001→T002→T003→T047—T050→T004→T005→US1。T006/T007 可并行；T008 依赖 T006，T009 依赖 T007，T011 依赖 T010，T012 收口验收。
- US2 依赖 US1 发布契约；T051 先确认职级来源，再执行 T013 的正式员工分支；T013/T014 可并行，后端 T015、前端 T016 各自等待对应测试。
- US3 依赖 US1/US2；T017/T018/T019 可并行。T020 等待 T017；T022 等待 T018/T021；T024 等待 T019/T023 及 T020/T022；T025 等待手动链路完备。
- US5 依赖 US3 共用任务/客户端基础。T026/T027 可并行；T028/T029/T030 均须对应测试先失败，T031 实机收口。
- US4 依赖 US3 的单面适配/任务基础；可与 US5 并行，但不允许同时改共享 `PrintJobService.java` 或客户端公共文件。T034 在 T024 完成后修改同一适配器，等待 T032；T035 等待 T033。
- US6 端到端审查依赖全部通道。T037/T038 可并行；T039/T040 各自依赖对应测试；T042 等待 T041 先失败；两者分步执行。
- 同一任务文件由主执行者维护。`[P]` 不表示可以跳过服务/契约依赖。

## Requirement Coverage

| 需求 | 任务 |
| --- | --- |
| FR-001 | T006—T009 |
| FR-002 | T002/T003/T006/T008/T010/T011 |
| FR-003 | T007—T009 |
| FR-004 | T002/T003/T006—T011/T027/T029 |
| FR-005 | T013—T016/T026/T028 |
| FR-006 | T017/T020/T023/T027/T032—T035 |
| FR-007 | T017/T020/T032—T035 |
| FR-008 | T017/T019/T020/T023/T024 |
| FR-009 | T019/T023/T024 |
| FR-010 | T017/T018/T020/T022/T026/T028/T032 |
| FR-011 | T017—T020/T022/T024/T027/T029/T041/T042 |
| FR-012 | T032/T034/T036 |
| FR-013 | T026—T031 |
| FR-014 | T004/T005/T007/T009/T013/T015/T018/T022/T037/T039 |
| FR-015 | T007/T009/T017/T020/T037/T039/T041/T042 |
| FR-016 | T030/T038/T040/T043/T046 |
| FR-017 | T001/T003/T021/T029/T044 |
| FR-018 | T001/T017/T023/T046（只读卡号及禁止写卡/编码调用的边界断言） |
| FR-019 | T017/T018/T020/T022 |
| FR-020 | T004/T005/T010/T011/T018/T022/T037/T039 |
| FR-021 | T047—T050/T006—T009/T013—T017/T020/T032/T034 |
| FR-022 | T006—T009/T014/T016/T012 |
| FR-023 | T051/T013—T017/T020/T032 |
| FR-024 | T013/T015/T017/T019/T020/T024/T026—T028/T030/T032—T035 |

## Implementation Strategy

首个可演示增量为批次1（US1），只验证设计器及模板管理，不改变现场打印。批次2按任务/手动→访客与自动→联合验收推进，所有模式通过后统一切换。测试环境、数据库和真实设备需先确认归属及授权；没有硬件的任务可以完成模拟测试，不能勾选实机验收任务。禁止调用 speckit-implement；使用通过一致性分析的本清单交给 superpowers 执行。
