# 研究记录：厂牌与访客模板设计器

- **规格**：`specs/009-print-template-designer/spec.md`
- **核查日期**：2026-09-04
- **研究范围**：模板设计器、可信 PDF 渲染、Brother QL-800 标签打印、HiTi/呈妍证卡打印及商用许可边界。
- **实际进度**：已从兼容演示进入批次1模板/版本/组合、受控图片与合成预览实现；离线测试、真实Java→Node渲染及未验收边界见 [quickstart.md](quickstart.md)。T001—T003与T047—T050保留为历史证据。
- **本轮仍未执行**：真实业务数据和 Oracle 连接、模板表 schema bootstrap、实际迁移执行器、SDK/驱动下载、Windows 代理联机、两款打印机实机打印及生产切换。

## 结论

建议以 **pdfme 作为模板模型、浏览器设计器和 PDF 生成基础**，配合项目自有的业务版本服务、私有渲染服务和 Windows 打印代理。该结论是“满足许可和联调门禁后的推荐”，不是对所有外围依赖的无条件商用许可承诺。

选择理由如下：

1. pdfme 官方仓库代码采用 MIT，适合作为商业闭源系统的基础；模板 JSON、设计器预览和正式 PDF 可以围绕同一模型组织。
2. 当前需求同时包含双面厂牌、单面凭条、字段绑定、版本回滚、二维码/条码和严格的数据快照。直接使用通用画布库会额外承担模板协议、分页/面数校验、字体嵌入和 PDF 生成，工作量和验收面更大。
3. 业务权限、模板发布和打印任务冻结应留在 Java 服务；正式打印 PDF 由受控 Node 服务生成，避免浏览器预览结果成为生产凭据，也避免 Java 直接执行 JavaScript 版 pdfme。
4. 本地设备必须由 Windows 代理处理：Brother 使用 b-PAC COM，HiTi 使用 Windows 系统驱动或经过许可确认的 SDK。浏览器不直接承担设备状态、独占和重试语义。

## 官方证据与状态

状态含义：**已验证**表示本轮或此前已打开的官方页面明确支持该事实；**推断**表示由证据形成的架构建议；**UNVERIFIED**表示仍需在实现、法务、构建或实机阶段确认。

| 主题 | 官方来源（本轮核查日期：2026-09-04） | 已核实事实 | 状态与边界 |
| --- | --- | --- | --- |
| pdfme 许可 | [pdfme 官方仓库](https://github.com/pdfme/pdfme)、[LICENSE](https://github.com/pdfme/pdfme/blob/main/LICENSE.md) | 仓库许可证为 MIT；项目包含 Designer/Generator 相关能力。 | **已验证**。MIT 只覆盖对应项目代码；字体、图片资源、PDF 栅格化组件和其他 npm 依赖仍须单独登记许可。 |
| pdfme 版本与宿主 | [pdfme 官方文档](https://pdfme.com/)、[@pdfme/ui npm 页面](https://www.npmjs.com/package/@pdfme/ui) | `@pdfme/ui`、`@pdfme/common`、`@pdfme/schemas`、`@pdfme/generator` 均锁定为 6.1.12；当前宿主是 Vue 2.7.16，锁文件中的 Vue CLI 为 3.12.1（Webpack 4）。直接把 pdfme library 引入 Vue CLI lib 构建在 `@pdfme/ui/dist/index.js:432:23` 因 `params?.Parent ?? Object` 失败。 | **兼容性已有局部验证，生产接入仍受门禁约束**。局部 esbuild ESM 运行时和显式预构建后的 Vue CLI UMD 兼容构建已通过；这不是 pdfme 官方 Vue2 插件，正式链路仍需完成剩余断言和集成验收。 |
| 中文字体 | [Noto CJK 官方源码](https://raw.githubusercontent.com/notofonts/noto-cjk/Sans2.004/Sans/OTF/SimplifiedChinese/NotoSansCJKsc-Regular.otf) | 已固定 `NotoSansCJKsc-Regular.otf`（Sans2.004，OFL 1.1），16,437,364 bytes，SHA-256 为 `2c76254f6fc379fddfce0a7e84fb5385bb135d3e399294f6eeb6680d0365b74b`；设计器字体别名为 `NotoSansSC`。 | **资源和许可已核对；最终字形一致性 UNVERIFIED**。浏览器合成数据中文显示、Node PDF 中文嵌入及文本提取已验证；嵌入后实际打印效果和完整缺字检测仍待验证。 |
| Brother QL-800 | [QL-800 官方产品页](https://www.brother-usa.com/products/ql800) | 已核实 QL-800 的 Windows USB 连接、最大有效打印宽度 58 mm及黑/红双色介质能力边界。 | **已验证硬件规格；图片效果 UNVERIFIED**。红黑输出受指定介质限制，图片缩放、裁切、抖动和小字可读性必须试打。 |
| Brother b-PAC | [Brother b-PAC 官方开发页](https://support.brother.com/g/s/es/dev/en/bpac/) | b-PAC 提供 Windows COM/SDK 方式控制 Brother 标签打印；现有访客链路已有 b-PAC 依赖。 | **已验证可作为集成方向**。具体 SDK/Runtime 版本、部署包、商用分发和运行时条款仍须在许可清单中锁定，不能仅凭能调用 COM 宣称全链路无约束。 |
| HiTi 型号 | [HiTi CS-220e 官方产品页](https://www.hiti.com/product/Product.aspx?MenuID=6&lang=EN)、[官方下载服务](https://www.hiti.com/support/download.aspx?lang=EN) | 官方页面使用 `CS-220e`；用户现场称 `CS220`，两者是否为同一实机型号需看铭牌、驱动和序列信息。官方页面列有 Windows 兼容信息，但未形成本项目 Windows 11 实机证据。 | **CS-220 与 CS-220e 对应关系、Windows 11 行为 UNVERIFIED**。不得把名称相似当成型号确认。 |
| HiTi 双面硬件 | [CS-200e/CS-220e 官方手册](https://www.hiti.com/files/Product/dm/CS-200e%26CS-220e%20manual.pdf)、[官方功能 FAQ](https://www.hiti.com/Support/FAQ_detail.aspx?ID=6&QID=146&lang=EN) | 官方资料把 Flipper 列为双面打印可选模块；手册驱动设置含 `Front & back image` 和 `Page order`，并写明双面打印适用卡厚约 0.5–1.0 mm。FAQ 说明双向 Flipper 可从出卡侧重新送卡。 | **双面能力方向已验证；两页同卡映射 UNVERIFIED**。资料未明确证明一个包含正反两页的 OS 打印任务必然在同一张卡的正反面完成，必须试打。 |
| HiTi 第三方打印 | [HiTi 第三方软件打印 FAQ](https://www.hiti.com/Support/FAQ_detail.aspx?ID=6&QID=154&lang=EN) | 官方 FAQ 说明 CardDésirée CS 以及 CardFive/CorelDraw 等软件可通过 Windows 打印驱动打印，并可在打印首选项设置卡型和打印区域。 | **已验证可优先走系统驱动**。这支持“打印代理调用驱动”的路线，但不证明自动双面页序、状态回执或同卡映射。 |
| HiTi SDK | [HiTi SDK 申请页](https://www.hiti.com/support/SDKForm.aspx?MenuID=102&lang=EN) | 官方提供需填写公司、申请人、用途及联系方式的 SDK 申请表。 | **SDK 包、API 能力、免费/商业授权、再分发条款 UNVERIFIED**。未取 SDK 不等于没有 SDK；打印-only 路径先不依赖未确认的 SDK。 |
| .NET 10 LTS | [Microsoft .NET 官方支持策略](https://dotnet.microsoft.com/en-us/platform/support/policy) | 页面在本轮访问时列出 .NET 10 为 Active/LTS，支持结束日期为 2028-11-14。 | **官方支持期已核实**。实现时仍需锁定具体 patch、Windows x64 支持矩阵及 b-PAC/HiTi 驱动的 COM 兼容性；微软运行时支持不等于设备驱动支持。 |

## 单面独立模板修订

- **决定**：每份模板及每次设计器编辑固定一页。正面、背面独立版本，通过 `PrintTemplatePair` 预关联或打印前显式选择组成厂牌；访客仅一份单面模板。两份版本号无需相同。
- **理由**：后勤可以专注一张画布，单独维护背面，并让兼容组合复用同一背面。设计层的页数和打印任务的输出面数分开管理。
- **版本规则**：组合绑定具体已发布版本；单面新版发布不自动替换组合。任务开始前冻结两份版本、各面输入及资源，手动翻面期间不能改选。组合只需修订号和审计，不增加独立的组合发布/回滚流程。
- **输出规则**：单模板预览输出一页，厂牌任务独立渲染两份单页后按正面/背面合并。禁止直接拼接两份 schemas 并共用一份字段值，避免独立模板中的同名字段相互覆盖。
- **适用范围**：显式选择和已绑定组合都接受同样的园区、用途、分类、尺寸/方向、介质和保密带码校验。
- **替代方案**：保留一个双页模板、仅把两面拆成标签页，无法实现独立版本与背面复用，因此不采用。

下方 T001—T003 的双页设计器记录是前次已完成验证，不代表本次继续使用双页模板。T047—T050 的执行结果在 quickstart 追加记录。

## 本轮 DHR 来源核查（2026-09-05）

用户已明确：目前全部使用 DHR，EHR 已彻底停用。后续打印只接 DHR 形成的正式员工资料；旧 EHR 分支只用于理解历史代码，不作为可启用选项。

- 正式调度主链为 `smart-schedule/.../EhrToStaffTaskService.java:351-511`（类名沿用历史 EHR 命名）。它在 370–376 行把 DHR `Jchen` 去掉“层”、将“课长”改成“科长”，再通过 `EvwHortationsAllJchenEnum` 转换；代码中的经理/职员/员工分别为 `5/7/9`。随后经 `/staff/sync` 和 `StaffUtil.buildStaff:132-133` 写入 `SmtStaff.jcheId/jcheName`。这证明当前源码转换规则，尚不代表厂牌业务等级与之等价。
- `DHREmpJChenEnum` 中的 `105/108/109` 被另一条 DTO wrapper 路径使用，不能拿来替代正式调度链；`job_level` UPMS 字典在本轮核查位置用于临时人员，不足以证明正式员工打印应以它为权威字典。
- DHR `Jchen=null` 时当前调度器会回填员工代码 `9`，但名称仍为空；非空未知名称可能得到 Java `null`，随后形成字符串 `"null"`。打印资料校验须同时核对代码、名称和来源，不能仅判代码非空后把缺失人员当员工级。
- 待确认项缩小为：厂牌等级与 DHR 原始名称及平台职层的映射、园区/BU启用范围和脱敏样例。用户尚未明确“厂牌等级就是档案职层”，T051保持未完成，不能用这份源码推导替代业务确认。

## 系统关联与正式员工职级修订

- **业务已确认**：用户要求关联在系统中管理；正式员工按“员工级、职员级、经理级”等职级使用不同厂牌。系统组合管理负责关联两份单面版本，适用规则负责把职级及单位范围关联到组合；多个职级可共用组合，兼容的组合可复用背面。设计器只编辑一份单面模板。
- **持久化决定**：`PrintTemplatePair` 和 `BindingRule` 由平台保存、鉴权和审计，刷新、重新登录或服务重启后关联仍有效。当前兼容页只有合成数据及内存版本，不能代替正式系统关联。
- **匹配决定**：正式员工先由服务端取得已验证的人事职级代码，筛出包含该职级的规则，再按所属公司精确绑定、同职级明确默认规则解析。缺失/未知职级须先补齐资料；无配置或歧义时停止自动匹配，不自动跨职级兜底。按用户最新补充，打印页同时允许手选未关联的已发布模板，EXPLICIT 不要求预存组合；展示职级、实际模板/版本和预览，核对确认后可打印，硬性权限/分类/尺寸/带码校验仍执行。
- **快照决定**：任务保存职级代码、名称、来源和读取时间，同时冻结规则修订、命中依据及正反面版本。职级调整只影响之后新建的任务。

本轮核对的是当前源码结构，未查询真实人事字典或 Oracle 数据。名称到稳定代码的映射仍为 **UNVERIFIED**，由待实施任务 T051 确认；不能把代码中存在某个字段当成已确认业务含义或实际同步成功。

| 当前源码证据 | 能确认的内容 | 使用边界 |
| --- | --- | --- |
| [SmtStaff.java](../../smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/SmtStaff.java) 的 `jobId/jobName`、`jcheId/jcheName`、`welfareLevel` | 实体分别定义岗位、职层、福利层次。 | `jcheId/jcheName` 是首选核查候选，尚不能证明其实际值就是用户所说的厂牌等级。 |
| [StaffUtil.java](../../smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/utils/StaffUtil.java) 的 `buildStaff` | 将 HR `JchenID/JchenName` 赋给员工职层，同时分别映射岗位和福利层次。 | 证明赋值路径，不能证明当前同步数据质量或等级字典内容。 |
| [EhrSyncPersonReqDTO.java](../../smart-module/smart-data/smart-data-api/src/main/java/com/tce/smart/data/api/dto/intergration/req/EhrSyncPersonReqDTO.java) 的 `empGrade/empGradeName` | 同步 DTO 定义职级 ID、职级描述。 | 未验证它与上述职层的对应关系，也不能据此认定已经写入平台员工表。 |
| [StaffRespDTO.java](../../smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/staffmange/StaffRespDTO.java) 的 `jobName` | 响应字段注释称“职级名称”，但员工实体同名字段定义为“岗位名称”。 | 注释存在口径差异，不据此把岗位当职级。 |
| [staff_info.js](../../smart-ui/src/const/crud/platform/basic/staff_info.js) | 员工管理界面分别显示“岗位”和“职层”。 | 只证明界面字段划分，不证明职层字典值。 |
| [ApplyPersonLevelEnum.java](../../smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/emun/ApplyPersonLevelEnum.java) | 放行人级别中出现“周边职员级以下”“经理级”。 | 属于另一项业务枚举，不复用为员工厂牌等级字典。 |

## 手动选择作为自动匹配的补充

用户明确新模板可能已发布但忘记关联，因此取消上一版“手选必须找到有效职级/公司绑定且对应已保存组合”的限制。自动绑定决定推荐结果，手选决定本次实际使用版本；两者都由服务端校验。未关联和与推荐不同作为核对提示，不成为手选门槛；手选无需新增审批流程，不要求先补关联，也不自动修改长期规则。操作员确认与实际选择写入任务快照，人员资料缺失或权限/模板/介质不合法仍拒绝。此修订仅更新文档，正式业务界面和接口未实现。

## 前次最小兼容验证（T001—T003 历史证据）

下列结果只证明 T001–T003 的最小接入路径可继续推进，不能替代真实业务、正式模板管理、PDF 生产验收或设备试打。

| 检查 | 结果 | 边界 |
| --- | --- | --- |
| Node/pnpm 与前端宿主 | Node `24.15.0`、pnpm `11.3.0`；Vue `2.7.16`；锁文件中的 Vue CLI `3.12.1`（Webpack 4） | 已核对本地工具链；Windows .NET 10 代理尚未创建或验证。 |
| pdfme 依赖许可 | `@pdfme/ui`、`@pdfme/common`、`@pdfme/schemas`、`@pdfme/generator` 均为 `6.1.12`、MIT；`esbuild` `0.27.7`、MIT | 依赖版本和许可证已记录，间接依赖及最终发行包清单仍需按 SC-008 收口。 |
| PDF 辅助依赖 | `clawpdf` `0.3.1`、MIT；随包的 PDFium 需要保留其第三方 notice | MIT 结论不能覆盖 PDFium notice 以外的发行义务；HiTi Windows 位图链路尚未锁定。 |
| 中文字体 | `NotoSansCJKsc-Regular.otf` 已按 Sans2.004/OFL 1.1 固定，UI 别名 `NotoSansSC`；大小和 SHA-256 已在上表记录 | 浏览器中文显示、真实 PDF 中文提取已验证；完整缺字检测与实机字形效果待 T010 及设备验收。 |
| 浏览器局部运行时 | esbuild ESM runtime 通过，产物 `7,629,469` bytes、`2855 inputs` | Worker/WASM 及 notice 已随包复制；固定空白底板画布测试未独立覆盖浏览器 PDF 背景 Worker 分支。 |
| Vue CLI 兼容构建 | 同源复制 Worker/WASM/字体/notice 后，`build:print-compatibility` 的 Vue CLI UMD 构建通过，初次耗时 `2036 ms`，修复后增量 `658 ms` | 直接 library import 仍会在 `@pdfme/ui/dist/index.js:432:23` 遇到 `params?.Parent ?? Object`；当前通过显式预构建兼容路径规避，不能宣称旧构建链已完全兼容。 |
| `smart-ui` 回归 | `pnpm test`：85 files、433 tests passed；`pnpm build`：退出码 0；定向 lint：0 errors、2 warnings | 两项 warning 来自测试夹具中 pdfme 协议字段 x/y 的短名称；构建有大静态资源体积提醒，尚无业务 API、权限或打印机验收。 |
| 浏览器合成数据 | 在 `127.0.0.1:18763` 验证中文厂牌正/反两面和二维码；字段改为 `12` 后等待编辑事件，保存/重开仍为 `12`；访客保存为 1 页；卸载无画布、重新挂载为 1 个画布 | 只使用合成数据和内存保存，没有真实业务持久化。已补上游 100 ms 延迟提交的 200 ms 输入保护期：快速保存明确拒绝，稳定后保存新值。非法访客增页后销毁重建合法模板，页游标和内容均恢复，仍可保存一面。 |
| `smart-print-renderer` 基础测试 | 6 项通过；真实 pdfme 生成 PDF，clawpdf 提取并断言“姓名：张三”；页数/尺寸、远程图片与未知组件反例通过 | 完整缺字、超长内容、码区与授权资源策略仍待后续任务；不能标记正式渲染器验收通过。 |

## Oracle、本地持久化与迁移边界

- `docker-compose.dev.yml:53-73` 定义了本地 `gvenzl/oracle-xe:21-slim` 和健康检查，`docker-compose.dev.yml:220-242` 让 `smart-platform` 依赖健康的 Oracle；这只能证明本地容器入口存在，不能证明模板表已经 bootstrap。
- `docker/.env.local.example:52-56` 只有本地示例配置。本轮不读取真实 `.env`、不连接真实库、不执行 DDL，因此模板表的 schema bootstrap 为 **UNVERIFIED**。
- 当前仓库没有已确认的 Flyway、Liquibase 或同等通用迁移框架；`smart/docs/12-risks/tech-debt.md:14-16` 也记录了该缺口。`smart-module/database/README.md` 的手工 SQL 说明与模块开发规则要求按发布记录/线上版本落地存在口径差异，实际迁移执行器和发布记录路径为 **UNVERIFIED**。
- 现有 `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/SmtIscStaffCardSchemaTest.java:10-49`、`SmtEnergyProjectionSchemaTest.java:15-18,20-46,120-160` 属于离线 SQL/实体/Mapper 契约检查，不连接 Oracle。模板实体/Mapper 后续可沿用 `smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/PrintTemplateSchemaTest.java` 的规划位置，业务事务测试可沿用 `smart-platform-biz/src/test/java/com/tce/smart/platform/service/print/PrintTemplateServiceTest.java` 的规划位置；两者都不能冒充真实数据库集成测试。
- 新增模板表前须确认隔离 schema 的 bootstrap、清理、回滚、发布归属与执行记录，再补 Oracle 集成测试；当前不写 DDL，也不在起步清单中增加未经确认的迁移命令。

## 候选方案比较

| 方案 | 适合度 | 主要问题 | 决策 |
| --- | --- | --- | --- |
| **pdfme + 自有业务/渲染/打印边界** | 能同时覆盖 JSON 模板、浏览器设计、正反面和 PDF 输出；与当前规格一致 | Vue2/旧 Webpack、CJK 字体、第三方栅格化依赖和设备联调仍有门禁 | **推荐** |
| `vue-plugin-hiprint/hiprint` | 面向打印排版，可能减少部分画布工作 | 外围 Vue wrapper 的许可证不能代表上游核心、插件、打印客户端和依赖的全链路商用许可；双面证卡、正式 PDF 和本项目任务冻结语义仍需补建 | **不作为当前主方案**。只有拿到完整上游许可证和发行包清单后再复评 |
| ReportBro | 报表设计/渲染模型可作为备选参考 | 卡片正反面、QL-800/HiTi 设备通道和闭源集成边界需分别验证；不能把某个演示仓库或发行版的许可推导成商用许可 | **备选，当前 UNVERIFIED** |
| GrapesJS | 通用可视化编辑能力强 | 偏网页内容编辑，不直接提供本项目所需的卡片面数、字段校验、可信 PDF 和设备任务语义 | **不选作打印引擎** |
| Fabric.js/Konva 或完全自研画布 | 自由度最高，可完全控制交互和数据模型 | 需要自行实现设计器、JSON 协议、正反面、字体、二维码/条码、PDF、版本校验和打印适配；交付与验收范围显著扩大 | **仅作为 pdfme 许可或渲染能力失败时的后备** |

任何候选若包含 GPL/AGPL、商业版限制、仅 SaaS 授权、设计器与渲染器分开授权或独立打印客户端许可，必须在进入分发包前单独取得许可证据；`UNVERIFIED` 不能作为上线依据。

## 拟定架构与责任边界

以下目录是规划边界；本轮已建立并验证 `smart-print-renderer/` 的最小兼容运行时，Windows 客户端和 Java 业务落点仍待后续批次实现：

```text
smart-ui/                         Vue2 + ElementUI 设计器与管理交互
smart-module/smart-platform/      Java 业务权限、模板版本与打印任务
smart-print-renderer/             Node24 私有可信 PDF 渲染服务（已完成最小兼容验证）
smart-print-client/               Windows .NET 10 LTS x64 本地打印代理（尚未创建）
```

### Vue2 设计器封装

在 `smart-ui` 中封装 pdfme Designer，负责三栏画布、组件属性编辑、单模板切换、字段绑定和 JSON 编辑态保存，正反面组合在画布外选择。所有单面模板都固定一页；人员厂牌组合固定两个输出面，访客凭条固定一个输出面；设计器不直接决定园区权限、模板发布或设备能力。浏览器预览可以用于编辑反馈，正式打印前必须由私有渲染服务按冻结快照重新生成 PDF。

### Java `smart-platform`

Java 服务负责园区与操作权限、模板 CRUD、版本发布/回滚、系统持久组合、职级/公司/供应商绑定、唯一匹配、保密访客缺码拒打、设备档案、任务创建和快照冻结。冻结内容至少包括人员数据、模板版本、设备配置、手动/自动模式、正反面输出和资源引用。Java 不直接执行 pdfme JavaScript，不把客户端传来的预览图当成正式打印凭据。

### `smart-print-renderer/`（Node24）

这是私有网络内的小型渲染服务，使用锁定版本的 pdfme 生成正式 PDF，并执行面数、尺寸、必填字段、越界、字体和码区校验。它只接收 Java 已授权的模板版本和数据快照，不访问打印机，也不负责业务权限。Java 不能原生执行 pdfme，因此用独立 Node 服务承接正式 PDF 生成；该边界是架构推断。

### `smart-print-client/`（Windows .NET 10 LTS x64）

本地代理负责设备发现/绑定、任务串行、设备独占、打印指令、状态回执和可恢复进度。Brother 通道调用 b-PAC COM；HiTi 通道优先调用 Windows 系统驱动，是否使用 SDK 要等申请、能力和许可确认。代理实现手动双面时逐面发送，并在人工翻面等待期间保持设备占用；自动双面只有在 Flipper、页序、方向和同卡行为通过实机验收后才开放。

HiTi 若必须把 PDF 页面转换为位图，所用 PDF 栅格化依赖（具体组件待定）必须先完成商用许可、Windows x64 和字体一致性核查；在此之前不锁定具体库、不放入分发包。

## 打印语义与关键风险

- 正反面来自独立的单面模板版本，任务创建时一并冻结到同一张厂牌任务。手动模式明确拆成“正面单面任务 → 出卡确认 → 人工翻面确认 → 背面单面任务”，任何一面结果不明都先核对实卡，不自动重印。
- 自动模式不应仅根据驱动存在 `Front & back image` 就启用；必须证明设备存在 Flipper、卡厚符合范围、页序和方向正确、两面确实落在同卡，并保留实机证据。
- 访客凭条始终只生成一面，不进入翻面流程，不因厂牌模板有背面而产生空白背面任务。
- IC 厂牌本功能只打印卡面；卡号读取、登记和门禁权限沿用既有链路，不写卡、不编码、不改变卡内数据。
- 设计器、Java、Node、Windows 代理和厂商组件的许可证必须分别入清单。pdfme MIT 已确认，不足以覆盖字体、图片、b-PAC、HiTi SDK/驱动和栅格化依赖。

## 未验证项不视为选型失败

以下事项是后续门禁，不是当前否决 pdfme 或打印代理方案的依据：

1. 直接 library import 在 `@pdfme/ui/dist/index.js:432:23` 的旧 Webpack 兼容问题，以及显式预构建路径在完整业务集成中的稳定性。
2. CJK 字体嵌入、缺字检测、Node24 输出与浏览器预览一致性；当前固定字体中文显示和 PDF 文本提取断言已通过，完整缺字与越界仍待 T010。
3. Brother QL-800 的 b-PAC Runtime 商用分发条款、双色介质、图片适配和目标电脑权限。
4. 现场“CS220”是否为 `CS-220e`、Windows 11 驱动状态、Flipper 是否安装以及自动双面页序/方向。
5. HiTi SDK 的实际获取、API 能力和授权；没有 SDK 公开下载证据不能推断“没有 SDK”。
6. PDF 栅格化组件的商业许可、性能和中文字形一致性。
7. Oracle 模板表的 schema bootstrap、正式迁移执行器、发布记录和隔离集成测试环境。
8. 真实业务持久化、权限链路及其保存时序；兼容页对 pdfme 属性延迟的保存/切换保护已通过本地验证。

### 2026-09-05：打印人员来源的补充核实

- DHR 为唯一在用来源；正式员工等级仍须按园区确认 `jcheId/jcheName` 的代码名称映射，未确认时拒绝该人员解析，不猜测员工级/职员级/经理级编码。
- COMPANY 按人员类型固定命名空间：正式员工使用 `SMT_PARK_BU.COMP_ID/PARK_ID`；外包/派遣使用 `SMT_ORGANIZE_RELATION.ID/PARK_ID/COMP_TYPE=1或2`。长期供应商人员的 `SUPPLIER_ID` 对应 `SMT_SECURITYAREA_SUPPLIER.ID`，需同园区且未删除；不能替换为普通 `SMT_SUPPLIER`。证据：`SmtStaffMapper.xml` 员工分页/卡导入、`SmtStaffServiceImpl` 外部组织保存、`SmtSupplierPersonMapper.xml` 的供应商 join。
- 新旧申请 ID 可能重复，主体显式分为 `VISITOR/VISITOR_COMPANION`（旧 `SMT_VISITOR/SMT_FELLOW_VISITOR`）及 `ADMITTANCE/ADMITTANCE_COMPANION`（新 `SMT_ADMITTANCE_APPLY/SMT_ADMITTANCE_FELLOW`）。不能依次查表碰运气。新主人员按 `VISITOR_ID=申请ID,IS_MAIN=1` 唯一读取，随行人员按自身 ID 且 `IS_MAIN=0` 读取。
- 原 `getAreaType` 返回 OA 区域枚举，不证明公共或保密；`isCommon` 仅表示常用区域。新申请可显式配置按 `AREA_TYPE → SMT_ADMITTANCE_AREA_TYPE_AUTH(PARK_ID,AUTH_TYPE=1) → SMT_DEVICE_AUTHORITY(PARK_ID,TYPE=1).AREA_TYPE` 推导，必须完整映射且性质仅0/1，任一1为保密；缺项或跨园区拒绝。旧访客须单独配置明确分类策略，VIP 与保密为不同维度。
- 照片引用是 `SMT_IMAGE.IMAGE_CODE`。员工 DHR 人脸代码来自 `FACE_PIC_ID`，保存来源为 `PARK_ID=0,IMAGE_TYPE=11`。当前 H5 `checkFace` 默认保存 `IMAGE_TYPE=12` 且请求 `PARK_ID=null`；真实数据库默认值是否替换 null 未验证，不能假定新访客必为801或本园区存储。照片适配必须先核人员归属，再按配置的明确存储域（可为 NULL）与类型唯一取图；不允许跨存储域回退。
- 上述图覆盖报告对工作树路径为 `excluded/not_tracked`，已直接核对当前源码；未读取或修改真实人员库。
