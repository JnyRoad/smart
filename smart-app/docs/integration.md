# 统一客户端集成说明

本文记录 `smart-app` 与统一身份、权限和业务后端之间的边界。供应商核验、登记和记录接口已接入现有入厂申请/人员 Mapper 与 Oracle 事务仓储，厂牌采用逐人记录 ID；App 会话、身份读取和物品放行也已在统一 `/api/v1/**` 路径实现。隔离 Docker 本机环境已完成网关、认证、UPMS、平台和业务闭环验证；预览仍保留显式演示模式。真实人员、DHR 与现场设备联调另行验收。

后续新平台的长期技术与 API 标准见[项目级技术基线](../../docs/architecture/new-platform-technical-baseline.md)。当前 App 已直接使用该标准；现有 Java 服务只增加 `/api/v1/**` 控制器和网关精确路由，旧接口保持不变。源码实现不表示已迁移或上线。

## 当前状态

`config/runtime.uts` 中的 `apiBaseUrl` 默认为空。空地址时 `services/client-api.uts` 不发送任何真实请求，页面只能显式进入演示模式。演示模式的数据来自进程内的固定身份和虚构物品申请、预置厂牌与进出事件，不会写入后端。

现有 App 使用网关 `/auth/oauth/token` 和 `/app/**`。新 App 不复用这一路径：系统建档的外包／派遣人员由 UPMS 显式凭据校验，正式员工只委托 `EmployeeCredentialAdapter`。本机 profile 提供虚构 DHR 适配器以验证接口边界；生产接入仅替换该适配器，未知、停用或来源不完整的人员均失败关闭。真实 DHR 的同步覆盖、停用和统一授权结果仍待验证。

认证改造中的新增内部Feign接缝采用POST JSON；保留的UPMS第三方认证仍使用历史URL协议，尚不能宣称密码传递全链路已改造。正式接通前需要确认上游支持的安全正文协议。真实人员、状态、权限和岗位来源缺失时不得签发不完整会话，也不能用本机固定身份或Token替代。

特别需要协调网关协议：历史文档写 RSA，而当前 `PasswordDecoderFilter` 实际从 query 取密码并按 AES 解密；现有客户端还携带 OAuth Basic 凭据。新客户端不复制这组密码参数和共享密钥，采用 HTTPS JSON 正文的统一会话契约。仅填写旧网关地址不会让新接口自动可用，后端需新增适配接口并完成鉴权联调。

## 客户端契约及接入状态

下表是 `client-api.uts` 的实际请求边界。所有列出的端点均已实现，并已通过隔离 Docker 网关以及 App UTS 适配器验证。默认客户端仍只允许 HTTPS；本机验收通过显式、受限的 `127.0.0.1` 回环配置临时开启 HTTP，不能用于生产。

| 方法 | 路径 | 请求 | 客户端当前要求 |
| --- | --- | --- | --- |
| POST | `/api/v1/sessions` | JSON `{ "staffNo": "...", "password": "..." }` | 仅 HTTPS；认证服务返回 token 与过期时间 |
| GET | `/api/v1/me` | Bearer token | 平台服务按当前员工主数据返回 `identity`；App 收到两步结果后才建立真实会话 |
| GET | `/api/v1/item-passes/posts` | Bearer 会话与申请权限 | 返回 `{posts: Post[]}`；只作为申请起终点候选，不授予安检岗位权限 |
| GET | `/api/v1/item-passes?scope=execute&postId=...` | Bearer token、当前已选岗位 | 服务端核执行权限、岗位归属和可执行状态；客户端仅在加载后实时筛选 |
| GET | `/api/v1/item-passes` | Bearer token | 返回当前身份可见的申请单数组；服务端按 token 决定数据范围 |
| POST | `/api/v1/item-passes` | 申请草稿 + `Idempotency-Key` | 服务端从 token 取得申请人，不接受客户端自称的申请人或权限 |
| POST | `/api/v1/item-passes/{id}/actions` | `{ "action", "postId", "comment", "execution" }` + `Idempotency-Key` | 服务端重新核验权限、岗位、状态和现场强制核验 |
| POST | `/api/v1/visitor-checks` | `{ "credentialCode", "postId" }` | 逐人FELLOW.ID字符串；查当前资格，必要时建立UNKNOWN状态，保存短时核验与允许方向，不产生通行事件 |
| POST | `/api/v1/visitor-passes` | `{ "verificationId", "postId", "direction" }` + `Idempotency-Key` | 服务端重新验证当前资格、岗位与状态，原子登记单次进入/离开 |
| GET | `/api/v1/visitor-passes` | Bearer token | 当前账号有权查看的进出事件，服务端负责园区与人员数据范围 |

工作台应用目录目前由 `core/catalog.uts` 固定注册，尚未调用 `/api/v1/me/apps`。未来若后端提供应用目录接口，也只能作为已知模块的元数据来源，必须经过本地模块白名单、权限码和页面路由校验，不能直接执行远程 URL。

工作台/待办分开读取本人申请、授权审批和当前岗位执行任务；执行待办要求当前岗位接口实际返回该单据，并通过客户端的状态/权限检查。最近申请按只读入口打开。服务端仍须约束每个查询的数据范围。

单据响应可附带字符串 `fromPostName`、`toPostName` 展示快照，供没有执行岗位的员工和主管阅读。名称由服务端根据点位生成，不信任申请草稿提供的名称；展示名称不等于岗位授权。旧响应缺少名称仍兼容，非字符串名称会被客户端拒绝。

## 登录和人员统一

登录表单不让用户选择“正式员工”“外包”或“派遣”身份。服务端需要在同一个认证入口完成以下工作：

1. 依据工号和密码找到统一身份主体，识别 DHR 同步的正式员工与系统维护的外包／派遣人员；后两者按同类人员展示和授权，保留来源值用于数据兼容。
2. 拒绝重复工号、离职或过期人员、停用账号和不完整的身份映射。
3. 返回稳定的 `subjectId`，以及 `staffNo`、`displayName`、`employmentType`、`organization`、`permissions` 和 `posts`。
4. 将权限码和现场岗位绑定到当前身份、园区和有效期，而不是让客户端根据人员来源自行推断。

认证服务先返回下列 token 响应，`expiresAt` 语义为 Unix 毫秒；当前 Smart 网关对 Java `long` 使用 13 位十进制字符串传输，App 同时兼容该字符串与未来可能的 JSON 数字，随后平台服务返回下方 `identity` 对象：

```json
{
  "token": "<memory-only-token>",
  "expiresAt": 0
}
```

```json
{
  "subjectId": "stable-subject-id",
  "staffNo": "000001",
  "displayName": "示例人员",
  "employmentType": "employee",
  "organization": "示例单位",
  "permissions": ["item-pass:apply"],
  "posts": []
}
```

密码只允许出现在 HTTPS 的 POST 正文，不应出现在 URL、查询参数、日志、错误消息、设备存储或业务请求中。当前会话控制器只将 token 放在内存，退出和过期会清空会话；刷新 token、服务端注销和设备风控接口尚未纳入契约。任何 token 持久化、自动刷新或旧接口兼容都需要单独评审。

服务端必须把客户端显隐当作体验层提示，不能把它当作授权。即使用户手工构造页面请求，后端也必须按 token、权限码、岗位、园区、单据状态和当前身份重新判断。

## 两项业务的状态边界

保密物品放行的客户端流程为“申请 → 审批 → 出发 → 到达 → 完成”。供应商通行资格在入厂申请时办理；客户端流程为“扫描已打印厂牌 → 资格核验 → 明确选择进入或离开 → 生成单次通行事件”，每次进出都需重新核验。审批权限和现场执行权限分开，岗位只能从服务端返回的 `posts` 集合中选择。

真实写操作带 `Idempotency-Key`，客户端不会在超时后自动重放。同一会话最后一份尚未确认的申请草稿，或同一单据、动作、岗位和意见的状态操作，再次人工提交时沿用原键；草稿改变、会话退出或进程重启后不会跨会话恢复该键，未知结果仍应先查记录，后端必须另有业务重复申请约束。服务端应保证幂等键、状态版本和并发冲突处理；客户端收到 409 时提示刷新核对，不能假定操作失败或成功。

物品放行出发时必须二选一登记押运人或定位锁：押运人提交的工牌号由服务端重新核验当前有效工作人员，定位锁要求完整编号。到达沿用出发方式重新核验，并核对同一定位锁编号。扫码、相机结果、演示按钮和本地岗位选择都不能替代读卡器级的可信证明；首版服务端复核员工主数据，读卡设备接入另行验收。供应商仅通过厂牌二维码核验，进入/离开由后端重新核验资格及当前状态并原子记录人员、单位、区域、岗位、方向、操作人、时间和核验凭据；不复用物品的 NFC 模拟开关。

## 扫码适配

PDA 实体扫码头按键扫描后通常以键盘事件写入聚焦的编辑框，客户端只负责收集完整文本并统一调用 `normalizeScan`。处理规则如下：

- 只去除末尾 CR/LF，保留前导零和原始字符串语义。
- 对无结束符的结果要求用户明确确认。
- 拒绝空白、控制字符和超过 4096 字符的输入。
- 不把扫码文本解析成 URL 或执行命令。

手机和 PDA 摄像头通过独立适配器接入，设置中的 `hardware`/`camera` 只是设备偏好，不等于操作系统一定具备对应能力。当前没有真实 PDA、手机摄像头或微信小程序相机的验收记录；适配器完成后需分别验证权限拒绝、取消、重复扫描、异常结果和平台 API 差异。

## 配置和联调要求

`apiBaseUrl` 非空时必须是不带用户名、密码和查询参数的 HTTPS 地址。不要把账号、token、加密密钥或小程序密钥写入源码、`manifest.json`、文档示例或提交记录；联调时可在本地修改 `config/runtime.uts` 的非秘密 HTTPS 地址；本机修改不得夹带提交。当前尚未实现构建环境自动注入。

后端开始联调前至少需要提供：统一登录响应 schema、三类人员的身份来源和去重规则、权限码与岗位清单、申请单字段和状态机、审批人与申请人关系、现场核验凭据接口、错误码、幂等键语义、过期/注销策略，以及各端 HTTPS、CORS 和小程序合法域名配置。没有这些信息时，客户端只能做本地演示和契约校验，不能宣称已接入 DHR 或生产业务。

桌面打印不属于当前移动客户端契约。后续若访客打印需要桌面端，应单独设计打印服务和设备桥接接口，由桌面容器消费受后端授权的任务，避免把本地打印权限放到移动端前端。

## 供应商厂牌核验边界
厂牌二维码不能由客户端自行解析为可信身份或区域授权，原文仅送服务端查询对应入厂申请及厂牌资格。核验结果包含人员、供应商、厂牌、入厂申请、区域、岗位、授权期限、短时核验期限、是否通过、拒绝原因、当前在内/在外与允许方向。身份或岗位变更即失效，失败和过期结果不能继续登记。

进出记录为独立事件，页面没有供应商申请、审批或“待审批”状态。演示当前限定东门有效厂牌，未知、过期、无区域资格均拒绝，重复进入/离开拒绝；真实进出规则及异常离场处理由服务端根据现场规则决定。客户端不自动重试写入；超时后同一核验与方向的人工重试须使用同一幂等键，并提示先核对记录。退出会话或重新核验不能替代服务端的业务去重约束。

## 现有二维码链路核对（Phase10历史调查）

本轮只读检查发现多种不同凭证，不能将它们默认等同于现场供应商厂牌二维码：

| 当前源码定位 | 已确认含义 | 迁移边界 |
| --- | --- | --- |
| `AdmittanceCodeDetailWrapper.java:83-87`、`SmtAdmittanceApplyController.java:203-206` | 入厂预约二维码以 `smsCode` 生成，提供 `/admittance/apply/searchVisitorByCode/{code}` 查询 | 该查询不能代替完整审批、有效期和区域资格核验 |
| `smart-ui/src/views/platform/visitor/qrCode_new/index.vue:393-542` 及其 Brother `red/visitor.lbx` 模板 | 本仓库这一个历史打印分支写入人员、单位、区域、期限等字段，所核模板没有 QR 对象 | 不能据此否认现场已有二维码厂牌；实际厂牌可能来自其他模板或服务，其编码仍需联调确认 |
| `SmtStaffServiceImpl.java:1031-1061` | 员工二维码内容为 `SmtStaff.id`，存在 1800 秒缓存 | 不能作为供应商人员或厂牌标识 |
| `SmtSecurityAreaSupplier.java:43-150`、`SmtSupplierPersonMapper.xml:54-73` | 有供应商授权时间、区域和人员关系；现有 active 人员查询仅检查删除标记 | 后端核验需组合当前有效期、授权区域、入厂审批及厂牌状态 |
| `SmtSecurityAreaOrderController.java:48-95` | 现有保密区订单接口处理创建、查询及 OA 流程 | 本次有界核查未证实可直接接入的供应商厂牌核验/幂等进出事件 API |

以上保留初次调查结论，不是生产厂牌格式或全部外部服务的完整性结论。随后用户明确逐人记录ID规则，Phase13已实现新的供应商接口与二维码输出；旧预约码查询仍不用于安检核验。

## 设备设置与现场岗位
扫码方式在“我的 → 设置”选择并显式保存，使用 uni.setStorageSync 的设备持久存储；不需要新数据库。所有扫码页响应式读取已保存配置，不再提供临时切换按钮；存储失败保持原配置并明确报错。该设备偏好在换人登录和重启后继续有效，不保存密码或权限。

工作台及“我的”移除岗位选择。岗位只在设置中编辑并保存；物品现场执行列表、扫码定位、执行详情和供应商厂牌核验顶部只读显示当前岗位。旧选岗链接跳转设置，设置岗位项和状态写入均要求 item-pass:execute 或 supplier:execute。未选岗的执行列表、查询、扫码和执行直链均不能办理，普通申请字段不受影响。人员来源不授予或阻止安检角色。普通申请人只在申请表内填写单据出发/到达字段。执行岗位只在当前会话保留，换人登录后清空，操作界面始终提示核对岗位与设备位置。


## 第一版服务端实施核对（Phase10）

用户确认本轮目标为源码和测试环境业务闭环，真机包随后进行。当前界面继续作为基线；以下是本 worktree 源码核对结果，不代表接口已经上线。

| 能力 | 当前源码证据 | 实施方向 |
| --- | --- | --- |
| 工号密码 | `SysUserService.simpleLogin(username,password)` 已接收显式参数；上层 `SmartUserDetailsServiceImpl` 与强密码工具读取 OAuth 请求，旧 Feign 通过GET query传密码 | 增加不依赖Servlet的显式认证及内部POST JSON，后续再接专用Provider与客户端会话；不把旧共享密钥或query密码复制到新App |
| DHR 人员 | `YutoDhrPsndo` 和 `SmtStaffServiceImpl` 保存人员类型、派遣公司与人员状态 | 映射 DHR 正式工及已有劳务/派遣同步记录；不能据此推断所有外包工作人员都在 DHR，供应商人员不在登录范围 |
| 系统内外包／派遣工作人员 | `SmtOutSrcApplyServiceImpl` 导入既有工号，审批后 `SmtStaffExtServiceImpl.saveBatchTemporaryStaff` 写入 `smt_staff` 临时人员；`SysUserServiceImpl.simpleLogin` 已有临时人员认证分支 | 复用工作人员既有认证链路，单独核验状态、有效期和权限；不从供应商台账建立 App 账号，详见下方来源核对 |
| App 权限 | `SmtAppStaffAuthServiceImpl.initLoginAuth` 会初始化旧 App 默认权限 | 不直接作为新客户端的登录副作用，不将默认模块权限等同于安检岗位授权 |
| 安检岗位 | 已核对的保密区人员/策略关系表达区域资格，尚未证实为安检操作岗位关系 | 安检执行岗位需要明确来源；申请候选由独立接口提供，Identity.posts 仅存本人授权执行岗位 |
| 物品放行 | `SmtArticlesReleaseController` / `SmtArticlesReleaseServiceImpl` 对应旧 OA 出厂等流程，缺少新域字段 | 在 smart-platform 新建独立保密物品域，并以 `/api/v1/item-passes` 适配入口承载，不沿用旧 OA 状态写接口 |
| 供应商预约 | `SmtSecurityAreaOrderController` / `SmtSecurityAreaOrderServiceImpl` 为供应商预约 OA 申请 | 保留入厂申请授权来源，逐次进出使用独立事件，不用订单审批状态表示进入/离开 |
| 厂牌与人员资料 | 入厂申请含照片、被访人和期限；旧 supplier person 查询不足以给出完整当前资格；实际厂牌编码仍 UNVERIFIED | 先核真实厂牌打印来源与脱敏扫码原文，再组合人员、公司、入厂审批、期限和区域，事件提交时重验 |

### 后端边界
保密物品纯领域规则已新增于 smart-platform-core 的 `core/client/release`，并通过23项JUnit测试。该规则只接收服务端内部可信上下文、持久单据快照和已验证卡证，负责状态、指派审批、岗位及押运方式校验；不能将 HTTP 中的身份、权限或“已刷卡”字段直接传为可信上下文。签发会话、卡证查询、数据库事务与幂等记录都必须由后续适配实现，纯规则测试不算真实业务闭环。

蓝图要求安检人员在 A 点出发、B 点到达也刷卡；当前 App execution 只有押运证明/锁号字段，所以真实现场动作继续阻断。后续需补安检本人卡证的可信接口及客户端交互，不能仅因为锁号分支无需押运人刷卡就解除全部保护。

### 当前待联调条件
- 用户已选择新建本任务独立本机环境，采用专用编排和 `.env.client-local`；初始化和健康验证以Phase11验证记录为准。现有 `smart-auth-012-oracle` 属于另一任务，不用于本次联调。
- 测试身份来源、工号冲突/停用口径、指定审批人及安检岗位授权来源。外包人员不能仅凭类别默认授予角色。
- 测试 Oracle schema 基线与已有发布记录；持久化设计需核对实际字段、约束及唯一性，迁移不在未确定目标时执行。
- 脱敏厂牌二维码原文及打印来源；卡证/读卡设备的可信校验链。

用户已授权新建本任务的本机测试环境。专用Oracle现已健康，初始空schema已核实：`127.0.0.1:15218/FREEPDB1`、`SMART_CLIENT_008`，实际VERSION_FULL为23.26.3.0.0（本机ARM）。生成与恢复参见 [专用环境说明](../../docker/client-integration/README.md)，已有卷时不能重新生成不同密码。Phase12继续接入真实持久化事务；第三方DHR、真实厂牌与卡证来源仍需实际接入资料。本地合成测试数据不作为真实人员或厂牌验收，不启动连接默认外部配置的服务。


### 登录人员范围最终澄清
用户明确：供应商人员不登录 App，他们通过 H5 的入厂申请办理入厂；App 由安检工作人员扫描供应商厂牌核验和登记进出。供应商人员/供应商员工台账不作为本次 App 账号来源，不为其新增工号账号或密码机制。

App 登录仍支持正式员工和实际使用 App 的外包／派遣工作人员。既有 `platform/outsourcing` 外包单位/在岗人员模块已核实：

- `SmtOutSrcApplyServiceImpl` 使用导入数据提供的工号；审批通过后 `SmtStaffExtServiceImpl.saveBatchTemporaryStaff` 写入 `smt_staff`，状态 `STAFF_STATUS_TEMPORARY=4`。
- 单位 `compType` 的外协/派遣分类，与 DHR 人员类型是两套来源值；统一身份需按来源映射，不能把 `status=4` 当成离职或未知状态直接拒绝。
- `SysUserServiceImpl.simpleLogin` 已包含临时人员的服务端密码验证及首次登录用户初始化。新 JSON 会话入口应提取可接收显式工号密码的服务逻辑，不能复用旧请求 URL 取密码方式，更不能在客户端保存或推导默认密码规则。
- 外包单位管理员由 `SmtOrganizeRelationServiceImpl` 创建 UPMS 企业管理员账号，用于单位 H5 业务；该角色不能自动赋予所有外包人员。
- 临时 App 默认权限和门禁设备授权不等于安检操作岗位。新返回的 Identity.posts 必须使用独立明确的执行授权来源。

因此无需另建供应商或外包密码体系的推断已撤回；后续复用工作人员现有认证链路，再核实测试环境中的停用、有效期和权限。

## Phase11 供应商后端规则
`smart-platform-core/core/client/supplier` 已实现供应商资格核验与明确进出登记的纯领域规则，使用人员与区域维度的在内状态，换厂牌不会重置该状态。核验及登记都检查当前岗位授权、人员/单位/厂牌/入厂申请是否有效、审批是否通过、授权区域和有效时间；登记还重新检查最新资格，并绑定原核验的操作人、岗位、区域、各主体与状态版本。

核验最长5分钟且不超过资格有效期，两个窗口均到期即失效。登记拒绝重复方向和旧状态版本，返回新的状态与单次不可变事件。18项新测试和23项物品回归通过，独立复核通过。Phase11仅包含纯领域规则；后续Phase12已增加下述Oracle仓储，厂牌解析、真实查询provider与HTTP仍未接入。

## Phase11 显式凭据认证
已新增 `SmartUserDetailsService.authenticate(username,password)` 与内部POST JSON `/api/user/simple`。新方法不依赖Servlet/query或旧user_details缓存；UPMS新路径每次校验工作人员凭据，已有外包/派遣账号先核存量哈希，错误密码不能先触发权限初始化。普通测试分支也不能凭合成成功放行未知账号。common-security对工作人员和平台账号再核服务端存量哈希，并执行锁定、账号状态和强密码检查。

新接口继续受 `@Inner` 保护，必须提供JSON正文与内部头，密码不会进入新增内部Feign的URL；DTO仅在传输时序列化密码，toString不输出密码。正文解析失败由Controller局部返回固定400泛化错误，避免既有全局异常处理回显/记录畸形JSON中的凭据。原GET和OAuth兼容入口保留。

定向33项、UPMS回归108项和旧OAuth配置2项通过，两项认证复核发现已修复并通过有界独立复核。新调用方需要配套新版UPMS POST处理器，不允许失败后降级到GET。保留的第三方历史URL认证协议仍需后续改造；本项没有启用客户端会话、Token签发或统一人员/岗位来源，不能据此声称App已能真实工号登录。

## Phase12 Oracle事务仓储
`JdbcConfidentialReleaseStore` 在方法内开启独立JDBC事务，调用原领域规则创建、审批、驳回、出发及到达；当前快照、逐版本事件与幂等原回复一起提交。版本条件更新防止同一版本被并发处理两次；旧键重试返回当次原快照，仍核当前权限与岗位，不重做业务动作。持久化查询、摘要、审计采用一致的单号和主体规范化规则。

`JdbcSupplierAccessStore` 持久化核验、人员区域状态、通行事件和幂等结果。状态按人员/区域建立，换厂牌不重置；缺少明确初始状态时拒绝办理。登记重验当前资格与岗位，并在同一事务内更新状态、消耗核验、追加事件和保存原回复；不同幂等键也不能重复使用核验。真实初始状态迁移及当前资格provider仍需接入，测试中的合成基线不授予真实人员资格。

版本化资源位于 `smart-platform-core/src/main/resources/db/client008/`，V001创建物品3表、V002创建供应商4表。生产仓储不自动执行DDL，也不参与外层Spring事务；调用方如需组合事务，需先统一事务边界。显式本机集成测试仅对已确认的 `SMART_CLIENT_008@FREEPDB1` 初始化，执行前验证目标及表结构，不调用第三方或建立其数据库替身。61项领域/真实Oracle测试通过，运行命令见 [验证说明](validation.md)。这仍不是App→HTTP→身份/现场设备的完整闭环。

## Phase13：人员记录ID到供应商进出事件

厂牌二维码内容为每名访客的 `SMT_ADMITTANCE_FELLOW.ID` 十进制字符串，主申请通过 `VISITOR_ID` 关联 `SMT_ADMITTANCE_APPLY`。不查询、不降级到6位短信预约码；不把主申请ID分给多人。合法短人员ID仍按主键处理，不用位数猜测凭证来源。API和打印始终保留字符串，避免19位Long经过JavaScript数字后失真。

服务器复用H5已落库数据，要求人员入厂 `applyType=1`、申请已批准0或已到达3、当前岗位园区一致、处于授权窗口、岗位映射的 `areaType` 编码精确匹配。区域按CSV整数匹配，0是有效编码，1不匹配11；`permitArea` 自由文字不作为权限。返回姓名、照片地址、公司、被访人、联系方式、授权期限与区域；随行人没有独立电话时返回空值，只有主人员证件/姓名匹配时使用申请主访客电话。照片沿用现有ImageService，真实照片服务加载仍需环境验收。

同一人员不同申请或厂牌共用人员/区域状态，内部以规范化证件类别与号码的摘要关联，不向App返回证件或摘要。维护中H5按身份证规则录入，提交显式类型0；历史空类型仅对通过同样18位校验位规则的号码按0规范化，未知证件类型拒绝，避免从null补为0后换出新身份。公司是入厂申请中的名称快照，未虚构长期供应商主数据启停状态。没有历史状态时，在当前资格和岗位检查通过后建立 `UNKNOWN`；页面显示“首次登记，请核对进出方向”，安检明确选择进入或离开。已知在内/在外时继续拒绝重复方向。V003仅扩展本任务两个状态约束，旧严格verify入口的缺基线拒绝行为保留。

服务实现位于 `smart-platform-biz/client/supplier`，复用现有资源服务器身份，不新增匿名通道：

- `POST /api/v1/visitor-checks`：扫描人员ID，保存短时核验，不产生进出事件。
- `POST /api/v1/visitor-passes`：提交核验ID、岗位、明确方向和 `Idempotency-Key`；重读当前入厂资格，事务内完成状态、消耗核验、事件和幂等回复。
- `GET /api/v1/visitor-passes`：只返回有权岗位最近100条事件。历史操作人当前返回稳定用户ID，不冒充查看者姓名；姓名解析可后续接入人员目录。

部署配置 `smart.client.supplier.enabled` 默认false；启用时必须明确配置 `business-timezone: Asia/Shanghai` 和 `posts`。每个岗位需要id、name、park-id、park-name、area-id、area-name、admittance-area-type-code。配置是岗位与入厂区域的映射，不是给所有用户授权。用户须同时具备 `supplier:execute`（核验/登记）或 `supplier:read`（查记录）、`supplier:post:<岗位ID>` 和匹配的 `SmartUser.parkIdList`；不能按正式/外包身份类别直接授予权限。实际网关 `/platform` 前缀及客户端会话返回的posts需后续对齐，没有修改现有网关规则。登记期间来源行锁与仓储事务会同时占用两条同库连接，数据源必须与SqlSessionFactory一致；启用前需按实际连接池和并发量做容量验收，本机测试不代表生产容量。

逐人打印资料新增 `recordQrCode` PNG Base64，内容即人员ID。管理端现有打印页新增“二维码厂牌”浏览器预览/手动打印入口。旧短信查预约、H5申请及Brother b-PAC模板保持原流程；本次没有直接改写没有二维码对象的.lbx模板。浏览器/PDF版与现场Brother打印机输出仍是两个验收项。

隔离 `docker/app-demo` 已启动完整本机服务组合，并已从网关验证三类人员登录、物品申请至完成、供应商厂牌进出、预约码拒绝和未知账号拒绝。`smart-app` 的 UTS 适配器也完成三类人员登录与模块目录验证。默认配置不会连接该环境；完整真实 DHR、现场读卡器、真实厂牌与真机验收仍未完成。登记通过同一 Oracle 的申请/人员行锁覆盖资格重读和通行提交；锁等待后按实际时间校验，撤销和资格修改须在行锁释放后继续。

## Phase14：App 专用、版本化后端接口

本阶段采用“新增标准接口，不改旧接口”的实现。旧 `articlesrelease`、H5 入厂申请、后台管理和 `/oauth/token` 保持原契约；新 App 只调用 `/api/v1/**`。接口默认关闭：未启用时请求会在服务端统一返回不可用，不创建业务数据，也不会改变既有系统行为。

| 服务 | 新增路径 | 责任 |
| --- | --- | --- |
| `smart-auth` | `POST /api/v1/sessions` | 只接收 JSON 工号和密码；调用既有显式凭据校验，再使用服务端登记的 OAuth client 签发 token。响应只含 `token` 与 `expiresAt`，App 不持有 client secret。 |
| `smart-platform` | `GET /api/v1/me` | 根据 Bearer token、当前 `smt_staff` 与组织关系输出身份。外协组织映射为 `outsourced`，派遣组织或 `empType=9` 映射为 `dispatched`，`empType=1` 映射为 `employee`；未知类型、离职和停用人员失败关闭。 |
| `smart-platform` | `/api/v1/item-passes/**` | 新物品放行申请、读取、候选岗位和动作接口，调用独立 Oracle 表和幂等仓储，不读写旧 OA 单据。 |
| `smart-platform` | `/api/v1/visitor-checks`、`/api/v1/visitor-passes` | 继续使用逐人 `SMT_ADMITTANCE_FELLOW.ID` 厂牌码，读取现有 H5 入厂数据、返回人员资料并登记独立进出事件。供应商本人没有 App 登录接口。 |

真实客户端只配置统一 HTTPS 网关 `runtimeConfig.apiBaseUrl`，依次调用 `POST /api/v1/sessions` 和 `GET /api/v1/me`。网关将会话路径精确路由到认证服务，并将身份和业务资源精确路由到平台服务；`SmartRequestGlobalFilter` 保留 `/api/**` 原路径，历史服务前缀继续移除首段。隔离 Docker 已验证 Nacos 服务发现、路由和完整网关启动；真实网络地址、证书与生产 Nacos 配置仍未验证。

部署侧最小配置如下，示例中的岗位、审批人、OAuth client ID 都是占位符，不可照抄为生产授权：

```yaml
# smart-auth；OAuth client 的密钥只保留在服务端已有登记中
smart:
  client:
    session:
      enabled: true
      client-id: <server-registered-app-client>

# smart-platform；每项业务显式启用，各岗位须与实际园区、区域及权限登记匹配
smart:
  client:
    item-pass:
      enabled: true
      posts:
        - id: <post-id>
          name: <post-name>
          park-id: <park-id>
          park-name: <park-name>
      applicant-approvers:
        <staff-no>: <approver-staff-no>
    supplier:
      enabled: true
      business-timezone: Asia/Shanghai
      posts:
        - id: <post-id>
          name: <post-name>
          park-id: <park-id>
          park-name: <park-name>
          area-id: <area-id>
          area-name: <area-name>
          admittance-area-type-code: <area-type-code>
```

每个实际安检账号还需要由既有授权系统授予准确的 `item-pass:*` 或 `supplier:*` 权限、`item-pass:post:<post-id>` 或 `supplier:post:<post-id>`，并在 `parkIdList` 中拥有对应园区。人员类别不自动授予任何岗位。供应商接口在每次办理时也重新读取当前人员目录，令牌尚未过期但员工已经离职、停用或失去可识别用工类别时同样拒绝。

物品出发和到达的安检操作由服务端根据 token 确认安检人员；押运人模式把 PDA 键盘输入或相机扫码得到的工牌号交给服务端重新查询有效员工，禁止安检员自任押运人。定位锁模式只接受合法锁号，且到达必须沿用出发登记方式和锁号。首版的工牌扫描不是读卡器签名证明；将来接入读卡设备时替换 `ReleaseCardEvidenceVerifier`，不改变 App HTTP 契约或旧业务接口。

### 已验证与未验证

- 已验证：99 条客户端 Node 测试、认证／UPMS／平台定向 JUnit、四端源码编译，以及 `docker/app-demo` 的完整微服务运行。网关自动验证外包、派遣、正式员工三类登录，物品申请／审批／双岗执行、供应商厂牌核验／进出／记录、预约码拒绝和未知账号拒绝；App UTS 适配器也完成网关登录及模块目录验收。Oracle 只监听本机回环，数据均为本任务虚构 schema。
- 未验证：真实 DHR、真实 OAuth client 登记、真实工号和实际岗位授权、真实厂牌照片加载、PDA/手机读码及真机安装包。隔离 Docker 的 DHR、人员、访客和厂牌不能替代真实测试环境。
