# 门锁旧设计勘误与替代决策

状态：v1 设计勘误登记。旧 Web/H5/后端资料保留为证据和对版输入，不是当前实现指令；本文件不覆盖 canonical 原件，也不宣称已完成代码、数据库、网关或真机切换。

## 1. 证据分级

| 标记 | 说明 |
|---|---|
| `[SRC-VERIFIED]` | 当前源码/精确路径已读或父任务提供的图谱 Tier 2 证据；只证明代码当前行为，不证明生产运行态。 |
| `[DOC-LEGACY]` | canonical checkout 或旧门锁重建资料中的设计/离线 bundle 还原；用于行为对版和风险识别，不等于现网能力。 |
| `[UNVERIFIED]` | 需要受控运行态、协议抓包、Oracle/网关/真机或产品确认；不得据此放行相应真实数据/设备操作；不阻塞无关离线实现。 |
| `[INFERENCE]` | 由上述证据推导出的设计选择，需在任务验收中以可观察证据验证。 |

已使用的主要资料：

- Web 八组：stable canonical root `/Users/lvtu/source/YUTO/smart` 下 `smart-ui/docs/superpowers/doorlock/` 的 `WEB-L-001`—`WEB-L-008`、`legacy-reference.md`、`parity-acceptance.md`；H5 为同一 root 下 `smart-h5/docs/superpowers/doorlock/` 的 `api-cutover-contract.md`、`page-and-state-contract.md`、`legacy-parity-and-scope.md`、`test-and-acceptance.md`。
- 旧控制面/桥接资料：同一 stable canonical root 下 `smart-module/smart-lock/docs/superpowers/07-api/` 的 API-001—004、事件信封和幂等文档；`smart-module/smart-bridge-lock/docs/superpowers/06-data/`、`07-api/`、协议证据台账。这里的相对路径不能简写成另一个独立仓库的 `smart-lock/docs`。
- 父任务提供的图谱证据：`Users-lvtu-source-YUTO-smart` generation `2026-09-05T04:00:36Z`，以及独立 `doorlock-smart-lock-reconstructed-20260804` generation `2026-09-05T04:34:36Z`。图谱显示 `RemoteSmartLockService`、`ConnectLockServiceImpl`、`SmtDormitoryStaffController`、`SmtDormitoryBedServiceImpl` 等精确路径已读取并有 coverage `metadata_match/noissue`；这仍是源码证据，不是生产 E2E。

## 2. 修订登记

### DR-001：独立门锁 HTTP 依赖不再是最终领域边界

| 项目 | 内容 |
|---|---|
| 旧前提 | `smart-platform` 的业务入口可直接调用独立门锁服务；实现只要异步发送 HTTP 即可，门锁模块更像远程适配器。 |
| 当前证据 | `[SRC-VERIFIED]` stable canonical root `/Users/lvtu/source/YUTO/smart` 下 `smart-module/smart-platform/smart-platform-api/.../RemoteSmartLockService.java` 仍是独立远程服务接口；`smart-module/smart-platform/smart-platform-biz/.../ConnectLockServiceImpl.java` 存在 `@Async`/`sendSave` HTTP 发送路径；`smart-module/smart-platform/.../SmtDormitoryBedServiceImpl.java` 仍有直接 `remove` 类生命周期路径。`[DOC-LEGACY]` 旧 API-001/项目简述也把平台→门锁列为接口耦合。调用链负结果不能仅依赖图谱：父任务确认 trace 实现 method caller=0/eq 误关联，需以 interface 和源码 fallback 为准。 |
| 替代决策 | 平台保留兼容门面，只负责提交住宿事实和园区入口；同事务写 Outbox 后由 `smart-lock` 统一裁定授权、命令、回执和审计；`smart-bridge-lock` 负责设备通道。业务 controller 不再散落“旧服务/新服务”分支，内部命令只走版本化 LC-CMD-001。 |
| 影响/未验证 | 现有调用方清单、旧路径退出时间和部署路由仍需实现任务验证；本修订不授权删除旧接口或执行切换。 |

### DR-002：前端 badge/人员参数不再是 H5 本人身份

| 项目 | 内容 |
|---|---|
| 旧前提 | 旧 H5 以 `GET /dormitory/staff/get/pwd?badge=...`、`POST /dormitory/staff/update/lock/pwd` body 中的 `badge` 等字段表达本人；页面传入什么人员标识，后端就可能按该字段查询。 |
| 当前证据 | `[DOC-LEGACY]` H5 切换契约明确记录这些调用事实，并指出 badge 与 token 绑定尚未由调用事实证明；旧 H5 `/key/myKey` 远程开门还出现硬编码人员标识。`[SRC-VERIFIED]` `SmtDormitoryStaffController.java` 的前端 badge 入口已由父任务图谱读取。 |
| 替代决策 | 新 H5 只访问 platform 门面下的 `/platform/lock/me/v1/*`；服务端从 Smart token subject、租户和有效 `membershipId` 取本人。`badge`、`staffId`、`deviceId`、query、路由和缓存不能换查询对象；不一致时拒绝/忽略并记录安全事件。门锁页面沿用既有认证态，不另建 token 存储或第二套认证。 |
| 影响/未验证 | token subject 与 platform 人员主数据的具体 claim 映射、旧客户端兼容期和错误文案需在实现/契约测试中验证；不复制旧硬编码。 |

### DR-003：HTTP/异步发送成功不等于设备成功

| 项目 | 内容 |
|---|---|
| 旧前提 | `@Async` 调用返回或网关/TCP 写出没有异常即可向页面显示成功；旧实现把发送、等待和业务结果混在一条链路。 |
| 当前证据 | `[SRC-VERIFIED]` `ConnectLockServiceImpl` 有异步 `sendSave` 路径，但源码存在不证明设备收到/执行。独立重建源码的 `LkDeviceTaskServiceImpl` 已有重试、取消、恢复实现，不能把“旧等待不可靠”误写成“失败永不重试”。`[DOC-LEGACY]` 桥接资料仍记录旧内存等待、超时和进程重启丢上下文；Web/H5 旧对版资料要求修正“请求成功即完成”。 `[UNVERIFIED]` 各型号实际传输 ACK/设备 ACK 仍需现场/抓包。 |
| 替代决策 | 命令状态拆为 `QUEUED`、`DISPATCHED`、`WAITING_ACK`、`SUCCEEDED`、`RETRY_PENDING`、`FAILED`、`EXPIRED`、`CANCELLED`、`RECONCILIATION_REQUIRED`；回执明确分 `transportAccepted` 与 `deviceConfirmed`。保留已验证的安全重试/取消/恢复语义，但每次尝试都须有 `attemptNo`、幂等和过期围栏；无设备回执、只有 HTTP 2xx 或传输 ACK 都不推进 `ACTIVE`/`REVOKED`，未知的远程开门不得自动延迟重放。 |
| 影响/未验证 | 必须补齐重启、迟到回执、未知物理结果、取消竞态和前端状态验收；不能把本地 mock 的成功称为真机闭环。 |

### DR-004：`commandId` 与厂商线任务标识分离

| 项目 | 内容 |
|---|---|
| 旧前提 | 可把任意新系统 UUID 直接塞进协议任务字段，或只保存一个“发送任务号”连接业务命令和设备回执。 |
| 当前证据 | `[DOC-LEGACY]` 桥接协议证据台账 FCT-009 记录服务器面向网关的关联扩展为 8 字节/64-bit 槽位，旧实现把业务任务关联写入 `Data` 并从上行回传；厂商 UART 文档的原生 `CMDID` 不等于服务器 JSON 关联。分帧、位宽和回传规则仍有 `[UNVERIFIED]` 矛盾。 |
| 替代决策 | 控制面使用 `commandId + attemptNo`；桥接持久化独立 `wireTaskId` 映射。若 profile 要求 64-bit/16 位十六进制，必须使用可编码的协议值或映射表，绝不把任意 UUID 硬塞老协议；每次尝试的映射、回执和冲突都可审计。 |
| 影响/未验证 | profile 的实际位宽、字符集、分帧、回执位置和复用窗口必须由抓包/真机门禁关闭；未关闭前能力为 `UNVERIFIED`，不发送。 |

### DR-005：人员住宿聚合版本与 membership 成为乱序围栏

| 项目 | 内容 |
|---|---|
| 旧前提 | 只按人员、房间或设备当前值处理事件；入住、退宿、再次入住和迟到回执可以覆盖彼此，重复事件依赖调用方不重试。 |
| 当前证据 | `[DOC-LEGACY]` 事件信封资料已经提出 `eventId`、`aggregateId + aggregateVersion`、`commandId + attemptNo` 分工，但旧路径/设备状态并不能证明所有入口都使用它们。规格明确要求重放、乱序、迟到撤权和再次入住隔离。 |
| 替代决策 | `aggregateId` 固定为稳定人员住宿生命周期，`aggregateVersion` 单调；每次有效住宿生成 `membershipId`。先按 `eventId` 去重，再按聚合版本和 membership 围栏；旧 membership 迟到新增成功只能补偿撤权/对账，不能恢复新人员权限。 |
| 影响/未验证 | 同房换床、批量、定时退宿、离职和床位管理需逐入口接入同一 Outbox 与测试矩阵；历史数据版本/缺口由迁移任务另行核验。 |

### DR-006：多 gateway 候选不意味着可并发发命令

| 项目 | 内容 |
|---|---|
| 旧前提 | 同一锁有多个 gateway 时可让多个连接/服务尝试发送，或按 IP/最后连接者自然覆盖；灰度可以在单锁/单 gateway 上双写。 |
| 当前证据 | `[DOC-LEGACY]` FCT-009 记录旧实现以 IP 作为会话/寻址键、同一 IP 后连接覆盖先连接，稳定网关身份与并发语义仍 `[UNVERIFIED]`；规格明确要求全通信域单执行方。 |
| 替代决策 | `smart-lock` 对每个物理设备维护唯一执行租约；同锁候选只能一个 bridge 发送，其他候选只上报健康/待处理；租约/身份冲突进入 `RECONCILIATION_REQUIRED`。正式切换必须停止旧执行并完成全域改址回读，禁止新旧双发。 |
| 影响/未验证 | 网关身份、租约存储和全通信域成员清单需真实演练；“一个锁验证通过”不能推广为“全域通过”。 |

### DR-007：Web 复刻的是管理员操作契约，不是旧接口和旧缺陷

| 项目 | 内容 |
|---|---|
| 旧前提 | 复制旧 `/device`、`/permissions`、`/record` URL、明文密码/通信 JSON、无权限入口和前端隐藏规则即可“一摸一样”。 |
| 当前证据 | `[DOC-LEGACY]` Web 八组文档明确要求逐项复刻筛选、字段、按钮、确认、分页、导入/导出和反馈，但排除旧接口、明文密码、完整卡号、指纹模板、原始 TCP 和硬编码身份；旧运行态菜单/资源码仍 `[UNVERIFIED]`。 |
| 替代决策 | 使用 `/platform/lock/v1` 管理门面，覆盖 WEB-L-001—008；服务端 platform scope 和 lock 状态机最终裁定；写操作呈现受理/等待/设备确认/失败/人工核验；敏感字段掩码，未知能力受控不可用。 |
| 影响/未验证 | 旧运行环境八组逐项截图/角色/资源码/导出证据仍需补齐；未补证据不能宣称视觉或菜单完全一致。 |

### DR-008：H5 不吸收旧后台和旧 `/key/*` 能力

| 项目 | 内容 |
|---|---|
| 旧前提 | 旧 H5 bundle 里有 `/key/myKey`、`/key/record`、远程开门和凭据/指纹展示，所以一期应全部迁入。 |
| 当前证据 | `[DOC-LEGACY]` H5 范围文件把 `/dorm`、`/dorm/lock`、`/dorm/get-code` 定为本人一期，明确 `/key/myKey`/`/key/record` 不自动复制；旧远程开门有硬编码身份缺陷。 |
| 替代决策 | H5 只提供 `/platform/lock/me/v1/status`、受控密码查看/修改和动态码请求/查询；不提供管理员授权、设备/网关/告警/迁移/切换入口。本人远程开门必须另立需求、协议、限频和真机门禁，未关闭前不注册入口。 |
| 影响/未验证 | 是否纳入本人开门记录/远程开门是后续产品决策，不由旧 bundle 单独决定；不阻塞当前 Web 八组。 |

### DR-009：旧协议源码事实不等于全部硬件能力

| 项目 | 内容 |
|---|---|
| 旧前提 | 旧代码里有密码、卡、指纹、管理员密码、恢复出厂、双重验证等分支，所以所有型号都可上线这些按钮。 |
| 当前证据 | `[SRC-VERIFIED]` 独立重建源码 `LkKeyServiceImpl` 已有 `ADD_FINGER_KEY`/B8 分包、指纹回执处理和 22/24-hex 测试形态；这已经推翻 stable canonical root 下旧 `smart-module/smart-lock/docs/superpowers/14-decisions/ADR-004-credential-migration.md` “没有远程指纹下发命令”的说法。`[DOC-LEGACY]` 旧协议台账仍确认 B6/B8、B2/B3/BD/配置能力和设备 RET 语义，同时记录网关 TCP 分帧、稳定身份、关联位、双重验证组合等缺口/矛盾。 `[UNVERIFIED]` 重建源码和离线测试不能证明每个型号/固件、现场网关链路或生产指纹 profile 均可用。 |
| 替代决策 | 不再把远程指纹一概判定为“不支持”；能力按 `gateway + device + model + firmware + profile` 矩阵冻结，明确登记 `ADD_FINGER_KEY`/B8 分包格式、22/24-hex 变体、回执和撤权语义。缺少对应 profile 证据仍为 `UNVERIFIED`，Web/H5 受控拒绝或禁用，不能以旧 ADR、旧数据库状态或单一重建测试推断全域支持。 |
| 影响/未验证 | 每个 profile 要有抓包、设备 ACK/失败、撤权、状态读取、重连和安全证据；现场门禁关闭前不能进入 `NEW_ONLY`。 |

### DR-010：Oracle 是新在线事实源，旧 MySQL 只读迁移不参与双写

| 项目 | 内容 |
|---|---|
| 旧前提 | 继续在线依赖旧门锁 MySQL/人员副本，迁移可整表复制后直接重放未完成任务；旧状态等于设备事实。 |
| 当前证据 | `[DOC-LEGACY]` 项目简述和当前规格均确定 Oracle 为新在线目标，要求来源键、批次、水位、异常和断点续跑；桥接资料明确旧任务/凭据不能未经核验当作物理事实。 `[UNVERIFIED]` 具体 Oracle schema/容量/生产计划和设备存量尚未现场核对。 |
| 替代决策 | 在线新命令/审计只写 Oracle；旧 MySQL 只读抽取到迁移账本，重复运行不激活在线授权、不发设备；旧未完成任务逐项分类，历史凭据按批准安全策略处理。旧凭据未迁移只表示新库没有该秘密，**不等于锁内凭据已失效或已被撤权**；必须以设备回执/读取能力/人工核验闭环。 |
| 影响/未验证 | 数据模型/映射和 Oracle 只读实证由同组其他设计与后续任务负责；本文件不执行 SQL、DDL、DML 或真实迁移。历史密码、卡和指纹的处置需明确“迁移、重置、保留待核验或批准排除”，不能用缺行推断物理状态。 |

### DR-011：一期不新增消息中间件，但不牺牲消息语义

| 项目 | 内容 |
|---|---|
| 旧前提 | 为了可靠性立即引入一个新的 MQ，或反过来沿用进程内队列/同步 HTTP 并假定一次成功即可。 |
| 当前证据 | `[DOC-LEGACY]` 旧资料用逻辑 `lock.lifecycle.v1`/`lock.command.v1` 等消息描述职责；规格和本轮批准方案明确一期单活桥、无需新增消息中间件。 `[SRC-VERIFIED]` 当前平台已有异步 HTTP 对接路径，但它不提供设备确认。 |
| 替代决策 | 先以 Oracle Outbox/Inbox + 内部 HTTP/Feign（或等价服务调用）承载版本化信封；至少一次投递、持久状态、幂等、恢复和对账必须不依赖 MQ。未来换传输只能保持同一契约和去重键。 |
| 影响/未验证 | Outbox 调度器、重试期限、内部认证和服务发现配置由实现任务验证；不把“没有 MQ”解释为“可以丢事件”。 |

### DR-012：外部 API 前缀与内部 API 明确分离

| 项目 | 内容 |
|---|---|
| 旧前提 | 客户端直接调用旧门锁 URL/服务，或把内部 bridge/协议地址当作 Web/H5 API；版本/资源命名依赖旧接口碰巧可用。 |
| 当前证据 | `[DOC-LEGACY]` 旧 API 索引要求 Smart gateway 暴露北向接口、桥接接口只在服务身份边界内；当前 H5 仍有旧 platform 路径表达但未冻结新 DTO。 |
| 替代决策 | Web 公开 `/platform/lock/v1/*`（逻辑 `/lock`），H5 公开 platform 门面专例 `/platform/lock/me/v1/*`（逻辑 `/lock/me`），内部统一 `/internal/lock/v1/*`。外部 prefix 由 platform 兼容门面适配；不再另设 `/app` 服务前缀，内部路径绝不经用户网关开放，客户端不拼 `wireTaskId`。 |
| 影响/未验证 | 真实部署的网关 host、既有前缀包装、资源码和路由映射需 implementation/运行态验收冻结；不能把建议路径写成已上线 endpoint。 |

### DR-013：至少一次 + 幂等对账取代物理 exactly-once 和数据库回滚幻想

| 项目 | 内容 |
|---|---|
| 旧前提 | 设备命令可以保证物理 exactly-once；切换出问题时恢复旧数据库即可恢复旧物理状态。 |
| 当前证据 | `[DOC-LEGACY]` 桥接资料记录设备协议/连接可能重复或未知，旧内存等待在重启后丢失；当前规格明确迟到回执、槽位复用和回退增量差异。 `[INFERENCE]` 在无法读取设备凭据清单时，纯数据库回滚不能证明锁内状态。 |
| 替代决策 | 业务消息至少一次，控制面/桥接按事件、命令尝试和线任务映射幂等；未知结果进入 `RECONCILIATION_REQUIRED`；回退先处理已产生的业务/物理增量和在途命令，再切执行方，不能只 restore 数据库。 |
| 影响/未验证 | 真机迟到/断线/回退演练和锁内状态读取能力必须关闭风险；设备物理 exactly-once 不作为系统承诺。 |

### DR-014：图谱调用链不足时不作“无调用/全量覆盖”结论

| 项目 | 内容 |
|---|---|
| 旧前提 | 图谱一次 caller 查询无结果即可断言旧门锁调用已不存在或实现已完整收口。 |
| 当前证据 | `[SRC-VERIFIED]` 父任务说明 `trace` 实现 method caller=0/eq 存在误关联；已有正向类/方法和 coverage `metadata_match/noissue`，但负向调用链不能直接采信。 |
| 替代决策 | 设计只引用已读取的接口/实现精确路径和源码 fallback；新实现任务须对 `RemoteSmartLockService`、`ConnectLockServiceImpl`、`SmtDormitoryStaffController`、`SmtDormitoryBedServiceImpl` 相关调用做正向迁移清单和编译/契约验证，不把图谱缺失当删除证明。 |
| 影响/未验证 | 仍需主任务在最新源码/分支上做完整调用方盘点；本勘误不扩大代码修改范围。 |

### DR-015：旧资料不是独立实现指挥源

| 项目 | 内容 |
|---|---|
| 旧前提 | 旧 `docs/superpowers`、离线 bundle 还原或反编译源码只要写过，就可以绕过当前 spec、plan、data model、contracts 和任务门禁直接实现。 |
| 当前证据 | `[DOC-LEGACY]` Web/H5 文档自己已标明部分运行态/协议能力 `UNVERIFIED`；`AGENTS.md` 和 Constitution 要求以当前源码、README、当前配置和批准规格为准，纯设计资料不证明上线。 |
| 替代决策 | 本规格 `spec.md`、已批准 `plan.md`/`tasks.md`、本目录 `contracts/` 下两份契约、`test-matrix.md`、`design-revisions.md` 及数据模型组成当前设计基线；旧资料只提供操作对版、风险和待补证据。冲突时记录新的勘误/决策，不复制旧资料全包，也不让历史文档独立指挥实现。 |
| 影响/未验证 | 任何旧行为要进入实现必须在任务中给出当前证据、替代决策、兼容范围、验收场景和安全差异；无证据项保持 `UNVERIFIED`。 |

### DR-016：身份映射和房间-设备绑定不再隐含在入住五事件

| 项目 | 内容 |
|---|---|
| 旧前提 | 只要重放入住、调宿、退宿、冻结、解冻五种事件，门锁就能推断人员身份变更、房间换锁、网关/设备替换和绑定版本。 |
| 当前证据 | `[INFERENCE]` 人员身份转正、房间解绑/换锁和设备绑定变化可能不伴随住宿状态变化；把它们塞进旧事件会丢失版本、旧目标撤权和影响 membership 清单。当前规格 FR-003/005/011/016 要求人员、房间、设备事实可追溯，不能靠隐含推断。已批准任务边界把 `RoomBindingService` 放在 `smart-lock`，不要求 platform 跨表写或新增 MQ。 |
| 替代决策 | 拆分事实来源：`LC-EVT-003` 只承载 platform Outbox 产生的 `STAFF_IDENTITY_MAPPING_CHANGED_V1`；`ROOM_DEVICE_BINDING_CHANGED_V1` 由 lock 的 `RoomBindingService` 在自身事务中写入独立 `ROOM_DEVICE_BINDING:{roomId}` + `bindingVersion` 和持久化重算任务，再通过住房 API 核对当前住房。lock 不自行猜测人员，也不把 binding fact 当授权成功。 |
| 影响/未验证 | 平台身份映射入口、lock 绑定版本生成、重算任务幂等和住房 API 核对仍需源码/契约测试验证；事件/本地事实缺失时必须对账，不静默发设备命令。 |

### DR-017：授权记录 revision 与共享凭据分离

| 项目 | 内容 |
|---|---|
| 旧前提 | `REVOKED` 后只能等新 membership 才能再次授权；每个 grant 撤销都可直接删除物理钥匙；部分凭据确认即可视为授权完成。 |
| 当前证据 | `[INFERENCE]` 同一次有效住宿中管理员可能撤销后重新授权，且多个有效 grant 可能引用同一设备凭据；若把 grant、membership 和 credential 混成一条记录，会误删仍被其他资格使用的物理钥匙。 |
| 替代决策 | 以 `grantId + grantRevision + membershipId` 区分授权记录；旧 `REVOKED` grant 不复活，同一有效 membership 可新建 revision；新增/修改凭据要求 membership 当前有效，但退宿撤权必须允许引用已关闭 membership 的历史 grant/credential/slot；多 grant 共享 `credentialId` 时只有最后有效资格撤销才发物理删钥；一个 grant 所需全部凭据确认后才为 `ACTIVE`。 |
| 影响/未验证 | 共享凭据关系、历史撤权的 slot/引用计数锁、删除命令和迁移映射需由数据模型及 TEST-026/031 验证；不能把单条 grant 行状态或“membership 当前有效”当作设备钥匙事实。 |

### DR-018：跨房调宿采用“旧撤权确认后新授权”的安全默认

| 项目 | 内容 |
|---|---|
| 旧前提 | 调宿事件到达即可同时向旧房撤权、向新房发权；旧房离线时为保证体验可以静默先开新房。 |
| 当前证据 | `[DOC-LEGACY]` 旧资料和当前规格均要求处理迟到回执、旧设备待撤权、在途命令和同锁单执行方；旧房撤权未知时先发新房会扩大物理权限窗口。 |
| 替代决策 | 先旧目标撤权并取得明确 `DEVICE_ACK`/`REVOKED`，再排队新房 provision；旧目标无回执/离线进入 `RECONCILIATION_REQUIRED`，通过人工应急通行/核验处理，不自动放宽顺序。 |
| 影响/未验证 | 现场应急流程、停留时限和人工审计需要另行批准；TEST-005 只在旧撤权确认后断言新房发送。 |

### DR-019：受控本人密码查看是一次性 reveal，不改变既有认证存储

| 项目 | 内容 |
|---|---|
| 旧前提 | 要么复刻旧接口直接回传密码、把明文放入缓存/日志，要么为避免泄露完全删除规格要求的本人受控查看；同时误把“门锁页不另存 token”扩大为重做 Smart 认证。 |
| 当前证据 | `[DOC-LEGACY]` H5 现有路径有本人密码/结果读取表达，但切换文档要求服务端身份、状态和敏感字段治理；规格 US3 要求受控查看/修改本人密码。 |
| 替代决策 | 使用 `/platform/lock/me/v1/password-reveals`，验证 `verificationRef` 的 subject、固定用途、当前 membership、短 TTL 和单次消费；只返回 `no-store` 的短时结果，页面内存显示，lock 只留消费摘要，修改命令永不回显。门锁页面不新增 token 存储，继续使用既有认证态。 |
| 影响/未验证 | 认证/人脸服务签发 verificationRef、凭据安全存储和当前密码是否可读需单独核验；没有合法 verificationRef 或能力未验证时拒绝 reveal。 |

### DR-020：Oracle 差异必须由真实语义测试关闭

| 项目 | 内容 |
|---|---|
| 旧前提 | 以 MySQL 空值、时间、分页、排序和唯一键行为推断 Oracle 在线门锁模型；本地 mock 通过即可说明迁移/账本安全。 |
| 当前证据 | `[DOC-LEGACY]` 当前规格明确 Oracle 为唯一在线事实源，Constitution 要求核对 schema/约束/索引/统计/计划；具体现场 Oracle 证据仍 `[UNVERIFIED]`。 |
| 替代决策 | 以 `TEST-025` 在批准隔离 Oracle 上验证空值、时区、精度、稳定排序、唯一约束、`membershipId + grantRevision` 和来源键幂等；无目标 schema/计划时保持 `UNVERIFIED`，不执行生产 SQL/DDL/DML。 |
| 影响/未验证 | Oracle 连接、schema、规模、计划和迁移窗口由数据/DB 任务负责；本勘误不把测试计划写成已执行结果。 |

### DR-021：命令信封按目标分类，资产动作不能伪造入住上下文

| 项目 | 内容 |
|---|---|
| 旧前提 | 所有设备命令都强制携带 `staffId`、`roomId`、`membershipId` 和住宿聚合版本；设备配置、网关设置或管理员远程开门也可以沿用人员凭据命令形状。 |
| 当前证据 | `[INFERENCE]` 人员凭据授权必须受 `membershipId`/`grantRevision` 围栏，但设备资产配置、网关设置和管理员危险动作并不产生入住实例；把它们伪装为人员命令会绕过资产并发控制和管理员 scope。当前生命周期与 Web/H5 契约还要求修改、解绑、配置和危险动作防止并发覆盖。 |
| 替代决策 | 命令增加 `targetKind`：`CREDENTIAL`、`DEVICE_ASSET`、`GATEWAY_ASSET`、`DEVICE_ACTION`。`CREDENTIAL` 必须有 `membershipId`、`grantId`、`grantRevision` 和 `credentialId`；资产/危险动作只使用真实设备/网关、`expectedVersion`、平台裁定的管理员 subject/scope 和短时 `operatorAuthorization`，不得伪造入住字段。除创建外的修改、撤权、解绑、配置和危险动作均必须有当前 `expectedVersion`；管理员远程开门不能由 H5 本人入口或 bridge 直接发起。 |
| 影响/未验证 | platform scope、资产版本来源、管理员危险动作权限和短 TTL 需在 TEST-029/030 与实现任务中验证；本修订不授权开放远程开门或新增外部路由。 |

## 3. 当前仍开放的证据门

以下不是本轮文档默认为真的事实，任务完成前应逐项关闭或保留门禁：

1. 当前部署网关的 external prefix、`/platform/lock/v1` 与 `/platform/lock/me/v1` 路由专例、菜单/资源码、真实角色与园区 scope 映射；
2. Oracle 目标 schema、字段/约束/索引/计划、容量和迁移窗口；
3. gateway 稳定身份、认证/密钥生命周期、全通信域成员和改址回读；
4. 每个型号/固件/profile 的 TCP 分帧、`wireTaskId` 位宽/回传、传输 ACK、设备 ACK、撤权、状态读取、断线重连和并发行为；
5. 旧 Web 八组运行态菜单/字段/角色差异，以及敏感字段脱敏审批；
6. H5 token subject 与 platform 人员/住宿事实的 claim 绑定、`verificationRef` 签发/单次消费、密码/动态码和人脸校验的服务端限频与审计；
7. 从旧执行方到新执行方的停止、在途命令、应急通行和回退增量演练。
