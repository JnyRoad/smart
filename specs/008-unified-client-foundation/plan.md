# Implementation Plan: 裕慧家园统一客户端骨架

**Branch**: `feat/uniapp-x-foundation` | **Date**: 2026-09-05 | **Spec**: [spec.md](spec.md)

## Summary
基于用户 HBuilderX 工程，交付可运行的统一客户端基座。物品申请/审批/现场操作与供应商厂牌核验/进出记录分别进入模块化页面；现场强制身份核验保留服务端边界。演示适配器与真实 HTTP 适配器明确分开。

## Technical Context
- Language/Version：HBuilderX 5.24.2026081301，Vue 3 组合式 API，uni-app x Vapor 字节码；核心采用可擦除 TypeScript 语法的 UTS 文件，避免浏览器与原生专用依赖进入核心。
- Primary Dependencies：HBuilderX 内置 Vue 与 uni API。现阶段 Vue reactive 足够，不为骨架单独引入状态管理包。
- Storage：认证凭据仅内存；设备扫码偏好、按subject隔离的常用入口可本地存储；演示记录仅进程内。
- Testing：Node 24+ 内置测试运行器 + UTS加载钩子；HBuilderX内置Node22编译器 Web/Android Vapor/微信/支付宝源码构建；浏览器关键路径测试。仅演示数据参与本地测试。
- Target：Android/iOS客户端，Web，微信；支付宝源码编译已验证，iOS/鸿蒙真机另行核实。桌面打印后续适配。
- Constraints：不使用真实账号、不触碰生产、不改旧客户端、不写数据库；不将前端显隐当作服务端鉴权。
- Scope：工作台、全部应用、待办、消息、我的、登录、设置、选岗、申请、记录、详情、扫码。

## Constitution Check
- I 展示/查询边界：客户端只组合API结果与演示数据，不实现真实授予权限。
- II/III Oracle/真实数据：本次不改SQL，不连接真实数据库。
- IV 中文注释与文案；V 行为测试先行；VI 已处于独立worktree和任务分支。
- 旧管理端Vue2约束继续适用smart-ui；新smart-app采用用户明确确认的uni-app x技术栈，不重写旧管理端。

## Project Structure
```text
smart-app/
  App.uvue / main.uts / pages.json / manifest.json
  core/                 # 类型、权限目录、扫码解析、业务状态和演示角色
  services/             # uni传输、真实接口和演示适配
  state/                # 会话与界面上下文、显式清理
  components/           # 页面壳、模块入口、状态/空态
  pages/                # 各独立页面
  static/               # 真实品牌logo与生成插画
  styles/               # 从YUTO规范提取的原生兼容样式
  tests/ / scripts/     # 行为测试、UTS加载与构建入口
  docs/                 # 集成契约与验证边界
```

## Global Constraints
核心不调用uni、window、fetch。模块使用稳定id、category、title、description、permission、route，不接受服务端任意路由；权限拒绝为默认。Identity包含subjectId/staffNo/displayName/employmentType/organization/permissions/posts；employmentType为employee/outsourced/dispatched。Post包含id/name/parkId/parkName。首次登录不强制选岗，执行时才强制授权岗位。Session认证仅内存，任何身份切换清空岗位与待办状态。新增api endpoint未实现必须在文档和UI明确说明。演示环境进入和操作都显式标注。客户端不保存密码、不把密码放URL、不自动重试业务写入。

## Phases and Validation
T001-T003 规格和工程准备；T004-T006 核心权限/扫码/演示业务与行为测试；T007-T009 请求、会话与页面；T010-T012 编译、交互验收、文档与复核。主agent拥有tasks状态，其他作者不写tasks。工作完成不自动提交或推送。

## 本轮修订：厂牌通行与操作体验
- 物品 Application 状态机只接受 item-pass；拒绝旧 supplier 申请与审批。移除 supplier:apply/approve 模块及权限。
- 新增 core/supplier-access.uts，负责演示厂牌资格、核验结果及进出事件规则。二维码原文不解析为可信身份或授权。真实适配新增 supplier-access 验证及事件接口；现有入厂/厂牌业务不在本次改动范围。
- state/session.uts 统一保护身份、授权岗位、异步代次与写入锁；供应商核验上下文随身份或岗位变更失效，超时重试沿用同一事件幂等键。核验与事件分开，任何扫码不直接写入记录。
- 身份来源保留 employee/outsourced/dispatched，统一人员分类文案。员工、外包与派遣不默认获得安检或审批权限。
- 新页面 supplier-access 与 supplier-records；原申请/详情/记录页只处理物品。原供应商链接显式拒绝或转到新核验/记录页，不允许申请和审批。
- 统一主题、真实图标页签、入口图标、紧凑工作台、48px 主按钮、明确按钮间距；审批双按钮同一行。复用首版生成插画和真实裕同标志，不引入额外 UI 依赖。
- 验证顺序：新增业务失败测试→领域/服务→页面关键路径→四平台源码编译→浏览器行为与一次批量视觉检查→独立复核。真实后端与设备仍另行验收。

## 验证中发现的编译与并发修正
Android 原生 CSS 对 display:block/inline-flex、伪类、属性选择器及后代/相邻选择器不兼容；共用样式使用基础类选择器和 flex，Web 专有焦点样式进入 H5 条件块。HBuilderX 在报告 CSS ERROR/Invalid selector 后仍可能退出0并显示Build complete，因此 scripts/build.mjs 同时检查真实退出码与编译诊断，出现这类错误必须失败。
供应商每次成功登记必须清空短时核验，下一次动作重新扫描；未确认写入才保留人工重试键，不缓存“成功事件”用于后续通行。清除核验后迟到写响应不得恢复上下文，写入成功使先前列表请求失效，避免旧快照覆盖新事件。

HBuilderX Web 输入组件存在100ms同步节流；扫码确认读取 confirm/blur 事件即时值，完整结果后重建输入控件，避免上一次回显覆盖下一次扫码。业务核验通过后保留并锁定扫码组件，登记完成或显式重扫时重置接收窗口。

异常链接回归发现：HBuilderX 在组件挂载阶段调用 onLoad/onShow，守卫同步 switchTab 会在页面缓存建立前修改路由，后续相同页面可能复用未清除的拒绝状态。守卫仍立即拒绝业务；导航延后到 Vue nextTick，并绑定会话代次，避免旧重定向干扰新账号。

## 设置与执行岗位修订
扫码方式继续使用 uni.setStorageSync 持久保存，不新增数据库或配置服务。设置页编辑后明确保存；ScanInput 响应式读取保存值，变更后清除旧输入/接收器并恢复对应焦点，不提供业务页切换按钮。通用页面移除岗位入口，独立岗位页和 selectPost 以执行权限校验。申请表 availablePosts 仍用于单据字段，与执行岗位分开；执行页顶部只读显示当前岗位，修改统一收回设置并显式保存，账号变更仍清空岗位。

完整扫码结果校验失败时保留错误提示，但清空输入并重建接收窗口，保证异常/超长码之后无需手工删除即可继续扫码。此恢复不绕过扫码归一化校验。

## 连续扫码和押运核验修订
使用现有 ScanInput 回车接收与输入重建能力，硬件模式取消确认按钮；申请封条改为输入上方的可删除标签，按扫描顺序追加到末尾并自动显示末尾。设置页合并当前会话岗位草稿与设备扫码草稿，保存前分别验证权限和存储；岗位不跨账号持久化。现场列表和扫码通过 hasExecutionPost / loadExecutionApplications / findScannedApplication 双层岗位与当前动作校验，未选岗不加载执行记录。
物品执行新增 execution 参数（mode、escortProof、lockNo），押运刷卡证明绑定当前会话/岗位/单据/动作且只在显式演示生成；真实模式继续关闭未接入的现场动作。出发登记 transport，抵达读取同一种方式并再次校验押运证明或同一锁号；服务端契约未来需独立验证。供应商核验增加 photoUrl、visitorPhone、hostName、hostPhone、authorizedAreas，演示无真实照片则显示暂无照片，不生成身份照片。

验证修正：办理完成后当前单据会移出本岗位待办，协调层返回本次成功结果快照供详情页展示，列表仍严格按可办理状态过滤；快照随页面恢复、离开或身份/岗位变化清空。封条列表采用有高度上限的滚动区域，按扫描顺序追加到末尾并自动显示末尾，连续扫码后末尾条码与输入框保持接近。

审批与安检上下文按入口意图固定：审批待办传入 approve，安检列表/扫码传入 execute，申请结果与历史记录传入 read，不能因审批后的状态变化切入安检。只有审批权限的身份即使刷新后列表不再包含该单，也保留经过类型、单号和目标状态校验的成功响应快照；物品 API 在请求前拒绝供应商 enter/leave 动作。

## 封条标签与现场搜索合并
申请页采用 flex 横向换行标签，取消标签撑满整行和文本 flex:1，长码受容器宽度限制，保留可触控删除和列表限高。执行记录页将扫码并入搜索框：输入事件即时更新查询，computed 仅筛选已加载列表；硬件模式在页面激活时聚焦，完整回车后保留并选中当前编号以便下一次覆盖。摄像头按钮置于右侧，沿用当前平台支持边界及 normalizeScan；回调绑定页面代次、账号、岗位和方式，结果只更新搜索，不跳详情或写入。旧扫码直链保留守卫兼容。输入焦点及选区参考 DCloud 官方 input 文档：https://doc.dcloud.net.cn/uni-app-x/component/input.html 。

## Phase 10：真实集成与岗位数据边界
沿用 Spec Kit 的本目录作为唯一规格事实源。先执行 FR-024/025，后续服务端适配须以调查和环境证据补齐本计划后再实施。

- GET `/api/v1/item-passes/posts` 返回 `{posts: Post[]}`，只在有 `item-pass:apply` 权限时请求；候选点位不授予执行权限。请求失败清空候选，表单保留已填内容、显示失败并可重试；提交时再次检查起终点均在当前有效候选集合内。
- `state/session.uts` 分开保存申请候选、加载状态与错误；新函数 `loadApplicationOptions` / `applicationPosts`，会话改变清空，序号防止迟到响应覆盖。`availablePosts` 保持仅为执行授权岗位。演示候选从显式演示目录独立提供。
- 现场调用 `api.listExecution(token, postId)`，请求 `/api/v1/item-passes?scope=execute&postId=<encoded>`；普通列表仍调用 `/api/v1/item-passes`。API 层拒绝空岗。服务端实现前只完成可联调契约，不解除真实出发/到达阻断。
- Java 后端保持既有模块架构，不引入新的后端语言或本地数据库替身。目标 schema 和测试网关缺失时，不臆测建表、不启动连接默认外部配置的 Spring 服务。
- 本次客户端改造先补失败测试，后实现；运行 Node 与关键浏览器回归，受影响多端源码编译。后端研究及环境依赖写入 integration.md，未接入项保持明确。


### 可独立实施的后端领域规则
源码已确认旧 ArticlesRelease 为 OA 物品出厂流程，SecurityAreaOrder 为供应商预约；新规则位于 `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/client/release/`，使用 Java 8 普通不可变值对象，避免把请求字段当成已认证身份或已验证卡证。

- `ConfidentialReleaseWorkflow`：申请创建与状态转换，不接数据库、Spring HTTP或外部系统。申请者、可申请点位、指派审批人、执行权限及卡证快照均由未来服务端适配器提供，禁止请求正文直接绑定为可信上下文。
- 输入含服务端时钟、预期版本、持久单据快照和动作，结果返回新单据及审计事件。版本匹配在规则层检查，但持久化必须另行用 Oracle 条件更新/事务保证，领域检查不宣称解决并发。
- 刷卡证明快照应含已验证的持卡人、单据、岗位、动作、操作者绑定和短时有效期；安检证明持卡人与操作者相同，押运证明不可替代安检证明。证明签发/查验由未来真实卡证服务承担，不实现演示证明provider。
- 出发登记押运方式，到达重验模式和锁号；封条保留前导零，拒绝空白、重复及嵌入控制字符。事件使用服务端提供ID和时间，返回对象集合防御性复制。
- JUnit4 定向行为测试先失败后通过。只构建这一 Maven reactor 依赖，不启动服务；无真实仓储不暴露新HTTP写入接口。仓储、幂等事务、真实认证与供应商适配继续属于T041。


### Phase 10 评审修正：工作台/待办也采用当前岗位查询
独立复核 CR-C2-001 发现工作台/待办仍通过普通列表获得执行任务。修正：新增 `loadWorkbenchApplications`，将本人申请/审批数据与通过 `listExecution(token,postId)` 获取的当前岗位执行数据分开请求后组合；执行专用账号未选岗不调用普通列表或执行接口。普通记录页的历史读取仍使用原 `loadApplications`。工作台最近记录仅保留本人申请；审批待办仍无需选岗，执行待办必须 `hasExecutionPost` 与 `canAct`。聚合响应同样受账号、岗位、请求序号约束，旧请求不得恢复其他岗位任务。


申请候选分离后的展示补充：无执行岗位的申请人/主管仍应看到单据起终点名称。Application响应可带 `fromPostName`/`toPostName` 展示快照；字段不授予权限，服务端根据点位生成，不接受草稿自称的名称。演示种子与提交时由演示候选生成名称，详情优先单据名称，其次当前可见目录，缺失才显示岗位标识。接口边界拒绝非字符串名称。不得为显示名称恢复普通人员的Identity.posts。

## Phase 11：供应商服务端规则
新增包 `smart-platform-core/core/client/supplier`，继续使用Java8纯值对象和规则，不启动服务或建立演示后端。真实厂牌原文由未来适配层解析/查询，规则层不猜测编码，不创建供应商账号。

- 可信资格快照包含厂牌/人员/单位/入厂申请标识、三者有效状态及审批通过状态、有效时间窗口、授权区域和必要展示资料；人员的当前在内状态按人员与区域保存，并有版本，不能因换厂牌绕过重复进入。
- 服务端操作上下文包含操作人、权限与已授权岗位；岗位到区域映射来自可信目录，不能由请求正文自称。核验产生绑定上述主体、岗位、区域与状态版本的内部快照，过期时间取服务端5分钟上限与资格到期时间的较早者。
- 登记重新使用当前资格、岗位授权、人员区域状态和服务端时钟，不能只信任上次allowed标记。进入要求在外，离开要求在内，授权到期或撤回时普通登记拒绝；异常离场另有业务流程时再设计，不隐式放宽当前规则。
- 事件保留实际操作人、岗位、方向、人员/单位/厂牌/入厂申请、核验ID和服务端时间，状态版本递增且原快照不可变。规则只检查版本，不宣称解决持久化并发；原子状态更新、核验消耗与幂等写入仍属于T041。
- 先JUnit失败测试，后实现；仅纯领域验证，不进行DB或第三方连接。客户端及已验收物品领域不改动。

### 本任务独立本机依赖
用户已明确选择新建独立本机测试环境，取代此前测试目标未定的阻塞。两个checkout的本地env均缺失，唯一运行Oracle属于其他任务。新增 `docker/client-integration/` 最小编排，固定Compose项目 `smart-app-008`，首先只启用Oracle；后续按接口需要扩展网关等，不整体启动现有backend profile或读取生产env。

- Oracle独立命名数据卷与 `SMART_CLIENT_008` 应用schema；端口仅监听127.0.0.1，启动前检查端口与同名资源归属，冲突明确拒绝。默认XE21/amd64已在本机ARM启动失败，按运行证据改用 `gvenzl/oracle-free:23-slim` / arm64 / FREEPDB1；使用新的 `smart-app-008-oracle-free-data` 卷，保留失败XE卷。该本机版本不等于生产版本兼容性验收。
- `scripts/client-integration-env.mjs` 生成随机本机凭据和 `.env.client-local`，权限600、精确忽略、不打印值；已有配置不覆盖。Compose显式使用该env，不使用默认生产连接。
- 启动后验证容器/卷标签、健康、映射、数据库版本、schema与空表基线；该新容器中的初始化属于已授权本机测试环境建设。不能连接或变更 `smart-auth-012-oracle`。
- 此阶段数据库就绪不代表HTTP接口、工作人员认证、厂牌、卡证或完整业务通过。数据卷保留，停机与恢复命令写入该目录README，不自动删除数据卷。

### 显式凭据认证准备
已核实UPMS `SysUserService.simpleLogin(username,password)` 原本就接收显式参数；耦合Servlet的是common-security `SmartUserDetailsServiceImpl` 与强密码工具。新增 `SmartUserDetailsService.authenticate(username,password)`，保留旧 `loadUserByUsername` 及既有分支，供后续专用AuthenticationProvider调用。

- 新方法只使用传入凭据，校验空值、登录失败锁定，并每次完成真实凭据验证，不读取/写入旧user_details缓存作为认证结果。平台账号按现有字典与服务端密码哈希验证；工作人员经新POST Feign入口调用既有UPMS逻辑，必须success且data=true。
- `smart-upms-api` 增加专用凭据DTO（禁止密码进入toString）；RemoteUserService新增POST `/api/user/simple` JSON方法。UserApiController对应方法沿用现有 `@Inner` 保护，`@Valid @RequestBody`，不增加忽略鉴权或公网白名单，不接受query/form代替body；旧GET不改。
- `SecurityUtils.isStrongPwd(username,password)` 新纯参数重载，旧Servlet版本保持URI语义并委托；新方法构建UserDetails后检查账号启用/锁定状态。新增内部调用的远程失败不把上游包含凭据的消息原样返回；新增入口与Feign接缝不将密码放入日志、URL或持久缓存。
- 单元/MockMvc测试mock内部服务和Redis，不启动Spring外连，不调用DHR。覆盖无Servlet上下文、旧缓存不能绕过新密码、平台/工作人员成功失败、success+false、锁定/停用/弱密码、JSON body及旧GET/旧OAuth兼容。
- `/api/v1/sessions`、`/api/v1/me`、token Provider/失败事件、身份分类、有效期与岗位组装继续属于T041，不能直接序列化含密码的UserInfo给App；本次仅建立安全可调用的服务端适配接缝。

认证实施核对修正：UPMS旧simpleLogin开头也存在 `user_details` 命中即返回true，不能让新POST直接委托它。保持旧方法兼容，新增无缓存凭据校验方法，将既有缓存检查后的工作人员校验/初始化逻辑提取共享；新POST只调用无缓存入口。授权扩展至SysUserService接口/实现和定向测试，其他用工状态/默认角色/认证提供方逻辑保持原状。新增测试必须贯穿UPMS真实服务方法，证明缓存命中不能绕过新路径，不能仅用mock远程success掩盖问题。

认证兼容边界裁定：保留的UPMS第三方认证业务内部仍使用历史URL传参；T052仅改造新增内部Feign接缝，不声称外部认证全链路已满足新客户端密码协议。T041实际接通员工认证前必须核实第三方支持的安全正文协议并替换该历史传递，不能以本次单元测试视为验收，也不能将新Feign降回GET规避兼容部署。

认证独立复核修正：旧UPMS对已有临时账号依赖后续OAuth Provider校验密码，新显式POST必须自行验证，并由显式common-security路径保留最终凭据验证；测试成功分支不能让新路径无凭据放行。补真实服务的已有临时账号与测试分支回归，错误密码不得触发初始化副作用。新Controller还需局部处理正文解析/校验错误，固定400泛化响应，避免既有全局异常处理把畸形JSON中的密码写日志/回显；测试需装载真实全局advice验证该边界。

会话实施在 Phase14 完成：`smart-auth` 新增 JSON Controller，使用显式凭据校验和服务端登记的 OAuth client 通过现有 TokenServices 签发 token；平台再依据当前人员主数据返回人员来源、权限和岗位。App 不持有 OAuth secret，也不接受客户端选择 OAuth client。真实 Redis、OAuth 登记、UPMS/人员数据、网关与授权源仍须按 T074 联调，不能以空 Oracle 或固定账号/Token 替代。

网关路径采用统一 `/api/v1`：新增精确路由把 `/api/v1/sessions` 发送至认证服务，把 `/api/v1/me`、`item-passes`、`visitor-checks` 和 `visitor-passes` 发送至平台服务；SmartRequestGlobalFilter 对 `/api/**` 保留完整路径，对历史服务前缀继续去掉第一段。Nacos 实际路由与完整服务启动仍需联调，不能因源码配置就声称客户端已经可达。JSON 请求不进入旧 PasswordDecoderFilter 的 query 密码解码。

环境实施裁定：本任务XE21首次启动退出186，ORA-00442/ORA-27302指向本机架构问题；已确认另一任务健康Oracle实际为23 ARM。仅替换本任务失败容器并使用新卷，旧失败卷保留；不改变其他任务。凭据文件实际位于 `docker/client-integration/.env.client-local`，由专用脚本生成并精确忽略。

## Phase 12：物品放行Oracle事务持久化
本机实测基线：`smart-app-008-oracle` 健康，仅映射 `127.0.0.1:15218`；FREEPDB1为READ WRITE，SMART_CLIENT_008为非公共OPEN用户且业务表数为0。实际Oracle AI Database 26ai Free / VERSION_FULL 23.26.3.0.0，不能将镜像23-slim标签写成生产版本兼容证明。

- 在现有release包新增 `JdbcConfidentialReleaseStore` 与显式 `ReleasePersistenceCodec`，保留不可变领域对象；对外只有业务create/approve/reject/depart/arrive/find，不公开任意save快照。生产仓储接收DataSource，方法内自管单连接事务，不混用Spring外层事务。
- 三表 `SMT_CLIENT_RELEASE`、`SMT_CLIENT_RELEASE_EVENT`、`SMT_CLIENT_RELEASE_COMMAND` 保存当前快照、逐版本审计、命令原回复。采用VARCHAR2/CLOB/NUMBER/TIMESTAMP及30字符内对象名，校验长度且不截断业务值；不使用Java对象序列化。事件唯一单号/版本，命令唯一服务端作用域/操作者/键。
- 请求摘要包括动作、单号、期望版本和业务参数，不包括服务端重试生成的时间/事件ID。幂等查重仍校验当前操作权限及适当岗位/主体绑定；命中返回保存的原回复。业务更新使用版本条件CAS；占位、领域规则、CAS、事件、回复全在同一事务。唯一冲突只在确认该命令键确实冲突后新事务核验原回复，不吞掉其他约束异常。
- JDBC集成测试调用生产业务仓储，在新连接/新实例间完成全流程；覆盖并发同键、不同键相同版本、旧键原回复、异请求拒绝、真实事件约束失败后的全事务回滚及领域拒绝无残片。用显式opt-in执行，缺配置失败；默认普通测试不连接DB。凭据只在进程内读取本任务被忽略的env，不出现在参数、输出或报告。
- 初始化再次核实schema/PDB/产品与目标表不存在，仅作用于本任务新表；现存结构不匹配必须失败。使用core版本化资源 `src/main/resources/db/client008/V001__release.sql` 和对应说明，应用不自动执行DDL，生产执行另按发布授权。测试只清理自己生成的ID，不drop整库。

实施裁定：历史 `smart-module/database/README.md` 仍建议manual目录，与当前根开发规则“不保留人工脚本目录”冲突。本增量以根规则为准，使用上述版本化资源和发布说明，不扩展旧manual目录，也不借此改写历史资料。供应商持久化可独立增量续作；T041的HTTP、真实身份、权限、厂牌和卡证适配不能由本批Oracle测试代替。

### Phase12 供应商独立持久化增量
新增同supplier包 `JdbcSupplierAccessStore` 与显式JSON codec，调用已验收的SupplierAccessWorkflow。数据分为 `SMT_CLIENT_SUP_PRESENCE`（人员/区域主键）、`SMT_CLIENT_SUP_VERIFY`（核验快照、有效期、消耗标志）、`SMT_CLIENT_SUP_EVENT`（核验ID及人员/区域/版本唯一）、`SMT_CLIENT_SUP_COMMAND`（服务端作用域/操作者/键唯一、摘要及原事件回复）。使用独立V002版本化资源，避免与物品存储共享业务表或文件所有权。

旧严格 `verify` 从DB读取明确已建立的人员区域状态；不存在状态时失败关闭。当前 App HTTP 使用 `verifyOrInitialize`：仅在当前可信资格/岗位校验后，才创建 `UNKNOWN`、版本为0的首次基线并保存短时核验；无效或过期资格仍失败关闭。record从DB取核验与当前状态，调用当前资格来源后重新校验领域；同事务CAS、条件消耗核验、插事件和保存幂等结果，任一失败全部回滚。同键重试先核当前操作者/权限与绑定，再返原结果；不同键不得重用已消耗核验。这里的资格/岗位provider由未来服务端适配提供，不接受App自称可信快照，不新增供应商账号。

新增真实Oracle测试验证显式合成人员基线→核验→进入→新连接重核验→离开，换牌仍共享人员区域状态，并覆盖缺基线、过期/撤回、消耗/并发冲突、幂等、约束故障回滚。测试只清理自有合成ID；不调用第三方，不读取其他任务或生产DB。共享core POM/构建入口由物品仓储作者负责，供应商作者不并行执行同reactor Maven，待入口冻结后协调验证。

## Phase 13：复用入厂人员ID与业务接口

现有H5调用 `/platform/admittance/apply/save/apply`，人员保存到 `SMT_ADMITTANCE_FELLOW`，申请保存到 `SMT_ADMITTANCE_APPLY`。用户已明确新厂牌用记录ID，不使用smsCode；逐人打印时取fellow.id。工号认证暂不联调。旧打印模板没有动态二维码，本增量按明确的新ID契约接入，不将现有6位打印查询接口接为核验。

实现裁定：一张申请可有多人，厂牌ID采用人员行ID；稳定的人员区域状态使用证件类别/号码的规范化摘要（仅服务端，不返回身份证或摘要给App），换申请/换牌不重置状态。公司是入厂申请中的名称快照，不假定有长期供应商公司主数据或停用字段；快照逻辑标识明确为入厂单位来源。状态仅接受已通过0/已到达3，期限按明确业务时区Asia/Shanghai转换，不以短信码存在或设备下发标志授予权限。区域严格匹配服务端岗位配置中的入厂区域选项，不解析permitArea自由文字为权限。

服务端适配位于smart-platform-biz的client/supplier包，复用现有两个Mapper的主键读取与ImageService；不改变H5提交/审批或旧打印前预约码查询。开启配置 `smart.client.supplier.enabled` 默认false，岗位目录配置包含id、name、parkId、parkName、areaId、areaName、admittanceAreaTypeCode；无默认授权。当前SmartUser须拥有supplier:execute（写）/supplier:read（查）、supplier:post:<postId>，以及对应parkIdList，服务端三者相交。它是未来会话目录可复用的配置接缝，不签发替代Token。

HTTP使用 `/api/v1/visitor-checks` 与 POST/GET `/api/v1/visitor-passes` 契约。只接受credentialCode/postId、verificationId/postId/direction，写命令必须有Idempotency-Key；忽略或拒绝请求自称的身份/状态/权限。错误使用明确HTTP状态和泛化消息，不记录扫码正文、证件、人员快照或凭据。网关前缀适配单列待验证，不新增匿名白名单。

仓储增加 verifyOrInitialize（旧verify严格行为不变）、findVerification及按授权岗位集合限量读取事件。首次资格校验后才建立UNKNOWN；V003仅扩展本任务状态CHECK为UNKNOWN/OUTSIDE/INSIDE，保留V002历史。首个明确动作决定状态；不同人员码但同一证件仍共用状态。客户端新增unknown显示“首次登记，请核对进出方向”，允许服务端返回的两个明确动作，已有inside/outside规则保持。

测试：入厂人员主键与关联校验、不同人员/园区/无效状态/过期/未授权区域、6位码拒绝及Long精度；MockMvc仅在测试注入认证主体，不能宣称DHR登录通过；真实Oracle覆盖首次状态、并发、事件读取范围及原61项回归。客户端先补unknown行为失败测试再实现，执行受影响构建。无需改动真实数据库或第三方环境。

Phase13打印实现：AdmittanceFellowRespDTO新增recordQrCode，使用逐人ID字符串算法生成PNG；ZXing解码测试验证19位值不丢精度。现有Brother模板未定义二维码对象，保留旧b-PAC路径，在现有管理端打印页新增二维码厂牌浏览器预览/手动打印，不手工改写未知.lbx结构。实物打印与现场扫码单独验收。

业务集成测试：client/integration以真实MyBatis Mapper读取本任务Oracle中的两张平台入厂合成表，再调用正式HTTP Controller/Service/JDBC仓储；测试上下文注入SmartUser，既不签发Token也不调用DHR。合成表仅test/resources夹具，显式env/端口/schema/PDB/版本门禁后建立，不是生产迁移。初版相邻独立事务存在撤销窗口；评审修正使用同一Oracle的申请→人员行锁，持锁覆盖资格重读至通行仓储事务结束。资格修改按数据库行锁顺序串行，锁等待后重新获取实际时间校验有效期；不涉及第三方分布式事务。

评审修正合同：H5保存显式身份证类型0；历史空类型仅对同一H5校验规则可确认的身份证归一0，其他缺失/未知类型拒绝。对正常Long主键支持1–19位，六位实际人员ID仍只按主键查询；短信码即使恰好相同，也不增加smsCode查找或降级。真实Oracle回归补来源为空类型、短主键、短信无回退、完整资料和撤销/锁等待过期并发。

## Phase 14：专用 App API 实施方案

采用标准化网关接口而不是修改旧接口：认证服务只承载 `POST /api/v1/sessions`，平台服务从 token 和当前员工主数据拼装 `GET /api/v1/me`，再承载放行和厂牌业务。App 仅通过 `apiBaseUrl` 调用统一网关；外部网关路径以 `/api/v1` 固定，底层服务名不进入 App 配置或生产地址。

所有新业务开关默认为 false，审批人、岗位—园区—区域映射以及 OAuth client 都只在服务端登记。物品放行使用新表和幂等仓储；供应商继续复用 H5 入厂人员记录 ID，不新增供应商 App 账号。现场押运人工牌先由扫码得到工号、服务端按当前员工目录重查，后续读卡器接入替换卡证验证器。源代码、MockMvc 和本任务 Docker Oracle 已验证；完整服务启动和真实设备验收仅在获得独立测试 Nacos/Redis/UPMS/DHR、网关及授权配置后执行。

## Phase 15：统一会话与隔离本机演示闭环

用户确认本机首版采用完整但隔离的演示闭环；真实 DHR 认证后续通过同一后端适配端口接入。该阶段不把合成数据写入任何既有开发、测试或生产库，也不把演示通过描述为真实 DHR/PDA 验收。

- 对外 API 保持唯一入口 `POST /api/v1/sessions`。认证链新增人员来源路由：外包／派遣使用系统内账户校验，正式员工委托 `EmployeeCredentialAdapter`。来源由平台人员主数据（含组织关系）裁定，客户端没有来源字段；适配器返回只有通过或拒绝，任何异常、未知来源或人员失效均拒绝。当前旧 OAuth 与 `/api/user/simple` 保持兼容；新 App 专用内部调用使用 JSON。
- `EmployeeCredentialAdapter` 先提供仅本机 profile 可启用的模拟实现，并预留显式 DHR HTTP 实现所需的 URL、客户端认证、超时和响应映射配置。没有已批准的真实 DHR 契约时，非本机的 DHR 实现不得伪造成功、不得回退系统密码或记录凭据。
- 平台新增受 token 保护的 `GET /api/v1/me/apps`，仅返回本地白名单模块元数据；App 继续以本地目录交集决定路由。新增 `GET /api/v1/item-passes/{id}`，复用现有服务端可见范围裁定。两者均写入 OpenAPI 契约并有正常、未授权和越权测试。
- 将现有仅 Oracle 的 `docker/client-integration` 迁移为 `docker/app-demo`；Compose 项目、容器、网络、卷、环境文件和 schema 统一使用 `smart-app-demo` 命名并采用未占用的回环端口。旧任务资源不删除、不复用、不作为验收数据。
- 新本机编排以当前源码编译的 gateway/auth/upms/platform 镜像为输入，显式写入专用 Nacos 配置和 Oracle 初始化。初始化仅建立运行所需最小 UPMS、OAuth、人员、访客和 client 表数据，以及明确标记的虚构演示账号。生产配置仍默认关闭 App 专用端点。
- 验证分层：先做单元/契约失败测试，再构建镜像并启动完整 `smart-app-demo`；脚本以网关执行员工申请→审批→安检出发→到达、供应商核验→进入→重新核验→离开、三类身份登录、无效厂牌和六位预约码拒绝。H5 再对真实网关完成关键页面请求检查。真机、PDA 扫码头和真实 DHR 保持单独未验收。
