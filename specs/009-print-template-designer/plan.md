# Implementation Plan: 厂牌与访客模板设计器

**Branch**: `feat/pdfme-template-designer` | **Date**: 2026-09-05 | **Spec**: [spec.md](spec.md)

**Input**: `specs/009-print-template-designer/spec.md`

**Status**: 批次1模板、版本、系统组合和私有资源已实现；批次2已实现职级/单位绑定、可信人员来源、照片冻结、任务/设备状态机、Windows 两款适配器及打印工作台，预览确认绑定已完成，软件组复审及本地测试通过；最终整分支代理复审未取得有效报告，需在PR中继续审查。实际 Oracle、DHR等级映射、供应商实体卡来源和打印机验收未完成，运行开关保持默认关闭。

## Summary

采用 pdfme 作为设计与 PDF 生成引擎，业务系统管理模板、版本、绑定及任务；所有模板各含一页；人员厂牌由独立的正面、背面模板组合输出两面，访客使用一份单面模板。翻面方式属于打印机档案及任务快照，同一组选择用于手动、自动双面。

复用 Java 平台服务处理业务规则；浏览器只负责编辑展示。正式输出由受控 Node 渲染服务生成并冻结，Windows 打印客户端主动领取任务，经 Brother b-PAC 或 HiTi 官方驱动执行。厂牌以一人一张卡为执行单元，结果不确定时停止并等待实卡核对。

## Technical Context

- **Language/Version**: 既有 Java 8、Vue 2.7；规划 Node.js 24 LTS 渲染服务、Windows .NET 10 LTS x64 打印客户端，正式版本与锁文件在实施前置任务中核实。
- **Primary Dependencies**: pdfme `ui/common/generator/schemas` 使用同一精确版本；Element UI；MyBatis/MyBatis-Plus；b-PAC COM；HiTi Windows 驱动。中文字体和 PDF 页面渲染依赖须先核许可并锁定。
- **Storage**: 既有 Oracle 存模板与任务；布局/快照使用 CLOB，不依赖未验证 Oracle JSON 特性；受控文件存储保存字体、图片及冻结输出；批次1模板图片和预览采用打印专属 Oracle 私有对象表适配，详见 data-model 第15节，不复用新闻公开下载或已停止维护的 smart-file-biz。客户端本地持久日志防止重复提交，不承担业务主数据。
- **Testing**: 前端现有 Vitest 与 Vue Test Utils；Java 模块现有测试设施；新增渲染服务行为测试；Windows 客户端状态/驱动适配测试及实机验收。
- **Target Platform**: 现有管理后台、Linux 服务环境、Windows x64 打印工作站。具体 Windows/设备/驱动组合是硬件验收条件。
- **Project Type**: 现有 Web 平台扩展，增加两个职责明确的设备支撑组件。
- **Performance Goals**: 当前不承诺吞吐 SLA；编辑器按路由懒加载，首次实施测量冷启动/字体体积；打印按设备串行，手动模式优先保证配对。验收规模见 SC-003/004。
- **Constraints**: 不重做通用画布、不升级整个管理后台；不让网页上传任意最终 PDF 作为正式厂牌；不将队列接受视为物理出卡；不写 IC 卡；新打印链路默认关闭。
- **Scale/Scope**: 独立单面模板、正反面组合、模板版本、单位绑定、两款设备适配、三种打印模式、逐卡批量与恢复。免检证业务页/PDA/保密考试等另有规格。

## Constitution Check

| 原则 | 设计前与设计后检查 |
| --- | --- |
| I 展示与业务查询边界 | 通过：园区、分类、单位关系、照片授权、模板解析及正式数据快照均由 smart-platform 决定；Vue/pdfme 不决策权限。 |
| II Oracle 以实证为准 | 通过：本文仅逻辑模型；实施前核目标 Oracle 字段/约束/版本及本地环境，未承诺索引性能。 |
| III 真实数据与 DDL 分离 | 通过：当前不执行数据库操作；迁移与回滚随发布机制管理，不复建人工脚本目录。 |
| IV 中文可维护性 | 通过：规格、业务解释及计划中的新增代码注释均要求中文。 |
| V 分层行为验证 | 通过：tasks 的每组行为实现有先行失败测试；文档只检查链接、结构及一致性，实机另列。 |
| VI 隔离与资料 | 通过：本任务 linked worktree，非 main；显式绑定 specs/009-print-template-designer，保留资料，忽略本机指针。 |

设备尚未验证是发布门槛，不是允许绕过的条件。无宪法例外。

## Project Structure

### Documentation

```text
specs/009-print-template-designer/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── contracts/print-api.md
├── quickstart.md
├── tasks.md
└── checklists/requirements.md
```

蓝图仅保留面向产品的摘要，并链接上述唯一规格来源。

### Source Code（当前实现结构，环境验收状态见 quickstart）

```text
smart-ui/src/api/platform/print/
smart-ui/src/views/platform/print/
smart-ui/src/components/print/                 # pdfme 生命周期、单面编辑及正反面组合
smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/{req,resp}/print/
smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/print/
smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/mapper/
smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/
smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/{controller,service}/print/
smart-print-renderer/                         # 私有渲染，Node.js，不承载业务查询
smart-print-client/                           # Windows 执行、持久日志、驱动适配
```

后端实体使用领域子包；Mapper Java 与 XML 沿用 core 的现有目录，不放入 biz。对应测试置于同模块 test 树。`smart-print-renderer` 已登记根 README 与 AGENTS；Windows 子项目仍待后续任务创建。

## Architecture and Decisions

### 1. 设计与版本

管理端使用 pdfme Designer 的 DOM 挂载接口包在 Vue 组件中，进入/退出页面管理实例生命周期；框架内部依赖不进入业务交互。优先直接集成并做现有 Vue CLI/Webpack 构建验证；若确有语法或样式冲突，采用同源局部预构建设计器资源，保留同一字段/模板契约，禁止趁机重构全站。

业务设计器只接收一份单面模板，`STAFF_CARD` 和 `VISITOR_SLIP` 的 `schemas` 都必须恰好一页；厂牌模板声明 FRONT/BACK 用途，访客仅 FRONT。单面模板版本独立发布/回滚，界面不再同时编辑或分页切换正反面。

新增轻量 `PrintTemplatePair` 关联两个已发布版本，记录名称、园区、适用分类和并发修订号；不增加第二套组合版本发布流程。组合保存具体版本 ID，正背面版本号可以不同。单面新版发布不自动升级组合，显式更新组合才影响未来任务；兼容组合可以复用背面模板。正式系统在模板管理的独立组合页面维护关联，服务端保存到 Oracle；设计器仅编辑一份单面模板，离开设计器或重启服务不会丢失已保存关联。

任务创建可使用绑定结果、选已有组合，或显式选择已发布正背面版本。服务端按同样的适用范围和权限校验，不能通过临时选择绕过保密带码等规则。手动和自动均在任务创建时冻结完整两面，翻面等待中不接受另一份背面。访客只选择一份单面模板。

当前兼容修订的 Node 内存接口为 `renderSinglePageTemplate({printType, template, input, fontBytes})` 和 `renderPrintTemplates({printType, printMode, front, back, fontBytes})`；`front/back` 各含 `templateVersionId`、单页 `template` 和该面的 `input`。先独立渲染每面，再按 FRONT/BACK 顺序合并 PDF，避免两个独立模板使用同名字段时相互覆盖。单模板预览固定一页；厂牌组合的 PDF 固定两页，访客输出固定一页。持久化和 HTTP 仍在后续任务。

兼容验证页只提供合成模板库、单页编辑、内存保存重开、正反面下拉选择及组合保存/恢复；不冒充正式模板管理或实际打印。

正式员工的模板适用规则增加职级条件：后勤从经确认的人事字典选择员工级、职员级、经理级等代码，一条规则可选择多个职级并指向一个 pair。职级名称不作为匹配键，不假设代码具有大小顺序。职级缺失或未知时先补齐人员资料；无对应规则或歧义时只停止自动匹配，允许操作员手选已发布模板，不自动使用其他职级模板。公司精确规则优先于园区默认，但两层都必须先满足职级条件；默认只在其显式列出的职级内生效。

系统业务页分为模板库/单面编辑、正反面组合（`smart-ui/src/views/platform/print/pairs/index.vue`）和适用规则（`bindings/index.vue`）。组合 API 封装在 `smart-ui/src/api/platform/print/pairs.js`；不把兼容验证页的内存关联直接当成正式业务管理。职级规则复用 BindingRule，存放去重代码集合并由服务端匹配；配置及人员数据由服务端验证，浏览器不能自报职级。候选字段为 SmtStaff 的职层 `jcheId/jcheName`，实际来源和字典对应关系先执行 T051；HR 同步 DTO 的 `empGrade/empGradeName` 仅证明接口存在，不能证明平台已经使用它。

### 2. 服务端业务与可信渲染

smart-platform 负责匹配绑定、园区与操作权限、读取人员/申请/照片、发布校验、冻结任务和审计。员工实体本身不直接提供园区归属，须通过已验证的员工园区关系（包括合法卡片关系等）验证，不能只凭工号/staffId取数；人员厂牌沿用蓝图的已登记卡号前置条件，缺失时拒绝，不在打印流程发卡或写卡；外包与供应商分类也必须使用服务端已核对关系。既有补领价格配置和 ISC 权限同步任务不复用为模板/物理打印任务。普通类型优先按公司/供应商精确绑定匹配，未命中时才使用同园区/打印物/分类的明确默认模板；正式员工两层候选均必须匹配其已验证职级，不能跨职级兜底；保密访客两种选择都必须使用保密带码模板；零命中或同优先级多命中时停止自动匹配，可手选符合该分类及带码要求的已发布模板。排序规则和字段来源见契约。

Node 渲染服务新增的必要性：Java 无法原生运行 pdfme generator，而信任浏览器上传的最终成品会绕开人员和模板校验。它只接受平台经过验证的模板与快照、受控资源内容，不查人员数据库，不对公网提供渲染入口，不接受任意 URL/脚本/本地路径。服务间认证、请求大小、超时及并发上限在配置中固定；输出 PDF 及页数/尺寸/hash，由平台保存为只读任务制品。

草稿预览可使用浏览器内合成数据；涉及真实人员的预览及正式打印须经服务端授权和渲染。正式员工任务额外冻结职级代码/名称/来源、命中规则及匹配依据；员工晋升后的新任务重新解析，已创建任务不改版。手选 PAIR 或 EXPLICIT 不要求存在当前职级/公司的适用绑定，EXPLICIT 也不要求事先保存组合。打印页始终显示“自动匹配/手动选择”，手选列表按园区、打印物、人员分类、发布状态和权限加载全部可用模板，不能通过 BindingRule 内连接过滤掉未关联模板。展示人员职级、模板名称/版本、两面预览及“未关联/与自动推荐不同”等提示，操作员核对确认后才能提交；这只是当前打印选择，不自动更新长期关联。同任务各面共享一次冻结的版本/数据/资源，重启或恢复不能重新解析最新人员/模板替换原制品。渲染失败不进入可执行状态。

手选请求仍由服务端校验人员资料及模板发布/分类/权限/资源、面数和介质，不把“无绑定”当成非法模板。自动解析诊断中的未命中/歧义可作为手选提示；权限失败、人员资料错误或服务故障不能伪装成无匹配后放行。PAIR 固定确认过的 pairRevision，EXPLICIT 固定两份版本；改选后需重新核对预览，批量选择逐人验证并展示预览和差异。任务快照保存自动解析结果、实际选择、确认人/时间，未使用的绑定规则不能伪造为命中。

### 3. Windows 打印客户端

客户端主动经 HTTPS 向平台领取任务，网页仅调用平台业务 API，避免浏览器直接操控本地裸打印端口。客户端采用独立设备身份，限定园区和打印机，不能借用管理员会话获得全园区人员接口权限。

收到命令后校验设备身份、任务/配置版本、制品 hash 与页面限制；将命令意图持久化后再交给驱动。命令重复到达时复用本地记录；一旦发生“可能已提交但回执不明”，报告 RESULT_UNKNOWN，不自动重新发送。状态证据区分队列接受、设备完成及人工确认。

- Brother：将冻结 PDF 的一面按校准 DPI 渲染为图像，放入固定介质尺寸 LBX 的图片对象，经 b-PAC 打印；不要求把 pdfme 的每个组件翻译成 LBX 对象。图片格式、黑红转换、二维码锐度及切刀都以试打结果为准。
- HiTi 手动：每个命令只输出 FRONT 或 BACK，一次单面；按设备校准的进卡/翻转方向提示操作员。
- HiTi 自动：一张卡的 FRONT/BACK 组成一次设备执行单元，通过经验证的驱动正反面/页序设置提交。不能假设任意两页 PDF 自动等于同卡双面。
- PDF 页面渲染器：优先采用能保留字体/条码清晰度、可商用的成熟组件；选定具体依赖和再分发方式是客户端实现前置任务，不自研 PDF 解释器，不默认接受未知许可 SDK。

### 4. 三种模式与进度

`SINGLE` 仅用于访客凭条；厂牌允许 `MANUAL_DUPLEX` 或 `AUTO_DUPLEX`。模板不存翻面模式；任务冻结所选模式及打印机档案版本。自动模式未验收时不能发送，允许操作员在任务执行前选择手动模式后创建任务。

一张卡一个 job，自动两面为一个 attempt，手动 FRONT/BACK 各有独立 attempt。持久化面进度，FRONT 确认出卡后才能进入 AWAITING_FLIP；点击已翻面只为当前任务创建一次 BACK 命令。批量人员逐卡执行，不先印整批正面再批量印背面。

同一打印机保留 activeJobId；等待翻面、失联或结果不明都不自动释放。客户端连接租约和物理设备占用分别管理：连接过期不得导致另一客户端接管后重印。人工核对、清空设备并记录原因后才能释放/恢复。状态与幂等细则见 [data-model.md](data-model.md)。

### 5. 安全、资源及审计

所有列表、详情、文件下载和动作均校验园区+权限；打印机所属园区、人员/申请园区、模板/绑定园区必须一致。资源用内部标识和内容 hash 解析，拒绝任意远程 URL、路径穿越、脚本与未批准插件。照片沿用当前受限下载链路，不使用可跨园区下载的宽权限回退。

页面和默认日志只暴露任务摘要；完整快照/制品需要单独授权。部署清单记录依赖/字体/SDK许可与来源；厂商包优先从官方安装，是否随安装包分发按具体条款决定。

## Delivery and Gates

| 阶段 | 交付内容 | 退出条件 |
| --- | --- | --- |
| G0 设计器兼容验证 | pdfme 构建/中文/固定面数及相关依赖许可 | 兼容问题有通过证据或确定的局部集成方式，未知授权依赖不打包 |
| 批次1 | US1、所需权限/资源基础、模板/版本/基础设计器、合成数据预览 | 保存还原/独立单面版本/组合冻结/页数校验通过；旧打印入口保持不变 |
| 批次2软件 | US2—US6、可信渲染、客户端、两种翻面、访客单面、审计恢复；先核 Windows/SDK/驱动/PDF栅格化器许可 | 行为与适配测试通过，真实业务数据预览鉴权有效，客户端依赖许可已核实 |
| G1 硬件验收 | 两款设备、厂牌手动/自动、访客单面、异常恢复 | SC-003/004/005真实样本全部通过；操作系统/驱动/耗材档案完整 |
| G2 联合切换 | 蓝图打印族及关联业务联合验收、切换/回退演练 | SC-006/007/008通过，执行发布时再核目标和授权 |

设计器可独立开发演示，缺少某种实机不允许把完整硬件验收标完成。回退停止新任务，已在设备内的卡先核对/处理，旧访客链路继续可用；新增无旧实现的厂牌入口暂时停用。回退不删除模板历史，也不重复播放失败任务。

蓝图 6.1 免检证等关联业务由独立规格交付，属于 G2 的外部验收依赖，不计入本规格的软件实现任务；T043 必须登记其交付与验收证据。关联规格尚未完成时，本功能可完成开发和独立验收，统一生产切换继续等待，不能用本任务通过代替整个打印族通过。

## Validation Strategy

测试先于对应行为实现，任务编号与 FR/SC 映射见 [tasks.md](tasks.md)。已完成的兼容构建和测试，以及后续 Oracle 集成与实机流程见 [quickstart.md](quickstart.md)，不能用模拟结果替代硬件完成证据。

## Complexity Tracking

无宪法例外。保留两个必要组件：删除 Node 渲染服务会失去服务端可信 pdfme 输出；删除 Windows 客户端会失去本地 COM/驱动控制、设备串行和断线恢复。当前不增加消息中间件、通用插件平台、跨园区共享或底层打印协议自研。
