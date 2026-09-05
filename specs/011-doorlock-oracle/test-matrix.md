# 门锁统一接入验收矩阵

状态：v1 任务输入；测试编号供 `tasks.md` 和实现任务引用。本矩阵描述应验证的行为，不表示任何场景已经执行通过。

## 1. 证据等级与统一断言

| 标记 | 可证明范围 | 不能替代的证据 |
|---|---|---|
| `MOCK` | 事件排序、幂等、状态机、API DTO、敏感字段和前端状态可离线验证 | 不能证明真实 Oracle、网关、协议或物理门锁。 |
| `INTEGRATION` | Smart gateway/platform/lock/bridge 的隔离环境服务契约和恢复行为 | 不能证明生产通信域、真实固件和现场操作。 |
| `ORACLE` | 在批准的指定版本、Schema 的隔离 Oracle 上验证真实事务、类型、空值、时间、约束、稳定排序、分页、并发及执行计划 | 不能用 MySQL、本地 mock 或 Mapper 推断替代；所需目标 Schema/计划缺失时相应验收项标 `UNVERIFIED`，不代表生产数据规模、正式环境或设备能力。 |
| `FIELD` | 指定型号/固件/网关组合的真实报文、设备确认和运维步骤 | 只能覆盖记录中的 profile 和通信域，不自动推广到其他设备。 |
| `UNVERIFIED` | 所需硬件/运行态证据尚未取得，必须保持门禁或待办 | 不能标记为通过，也不能作为上线依据。 |

所有测试还应保存 `traceId`、事件/命令/尝试 ID、与场景相关的 `membershipId`/`aggregateVersion` 或 `targetKind`/设备资产 `expectedVersion`/管理员 scope、脱敏状态时间线和执行模式；资产配置、网关设置和管理员危险动作不强制伪造人员住宿字段。证据不得包含真实密码、动态码、token、人脸/指纹、完整卡号、网关密钥或原始可重放报文。命令成功的统一断言是：必须有匹配 `commandId + attemptNo + wireTaskId` 的明确 `DEVICE_ACK`，且 `deviceConfirmed=true`、新增/修改授权的 membership/聚合版本仍有效；撤权/删钥可以针对已关闭 membership，但必须有匹配的历史 grant/credential/slot 和当前其他引用校验。HTTP 2xx、`transportAccepted=true`、连接写出或“无错误日志”均不算成功。

## 2. 正常与故障场景

| 编号 | 类型/等级 | 场景与操作 | 预期关键断言 | 覆盖需求/用户故事 |
|---|---|---|---|---|
| TEST-001 | 正常 / MOCK+INTEGRATION | 有效员工完成入住事务并提交 Outbox。 | 住宿事实和事件在同一事务成功；事件含稳定 `aggregateId`、单调 `aggregateVersion`、字符串 `staffId/roomId/parkId` 和新 `membershipId`；授权进入 `PENDING_PROVISION`，产生可恢复命令。 | FR-003、FR-004、FR-006、FR-007；US1 |
| TEST-002 | 故障 / MOCK | 入住事务在提交前回滚或 Outbox 写入失败。 | 不产生可投递生命周期事件、授权或设备命令；失败可审计且不会静默丢失住宿事实。 | FR-004；US1、US5 |
| TEST-003 | 故障 / MOCK+INTEGRATION+ORACLE | 重发同一 `eventId`；Web 命令/H5 请求重用同一 key，改变 requestId、callerService、subject、operation、目标或载荷，并在提交后重启重投；同一请求向两个设备下发并部分确认。 | 精确事件重投返回原处理结果。同服务端范围和 key、同载荷复用同一请求/命令，即使 requestId 改变也不重复；不同载荷报 `DUPLICATE_REQUEST`，不能因摘要变化另建行；其他调用范围不碰撞也不能读取原主体结果。Oracle 并发插入只能有一个有效幂等记录；同请求的不同子目标可落多条命令，重投返回原集合、部分成功不代表整体完成；敏感摘要不暴露低熵秘密。 | FR-006、FR-007、FR-008；US1、US3 |
| TEST-004 | 故障 / MOCK+INTEGRATION+ORACLE | 先收到 `aggregateVersion=9`，再收到缺失的 8；另提交不同 `eventId` 的已处理/更旧版本事件，以及同 ID 不同 payload，再重启查询证据。 | 高版本缺口持久待处理，补齐后按顺序处理；只有同 ID 且同不可变载荷才复用原结果。不同 ID 的旧版本必须落本事件的 `STALE_EVENT`/迟到结果，事实冲突或无法核实则进入 `RECONCILIATION_REQUIRED`；同 ID 异载荷保持原 Inbox 不变，隔离审计可独立追溯第二份载荷；不冒用别的事件成功，不回退状态或恢复权限。 | FR-006、FR-008、FR-022；US1、US4 |
| TEST-005 | 正常+边界 / MOCK+INTEGRATION | 同房换床、跨房调宿、定时退宿和批量入住分别触发住宿变更。 | 同一房间换床不重复发放；跨房必须等待旧目标撤权明确 `DEVICE_ACK`/`REVOKED` 后才发新房授权；旧目标离线/无回执进入人工应急，不静默放宽；定时/批量入口都走同一 `membershipId`/版本规则。 | FR-005、FR-008、FR-009；US1、US5 |
| TEST-006 | 故障 / MOCK+INTEGRATION | 退宿后，旧入住产生迟到的“新增凭据成功”回执；或设备槽位已复用。 | 旧记录保持 `PENDING_REVOKE`/`REVOKED`/`RECONCILIATION_REQUIRED`，绝不回到 `ACTIVE`；生成补偿撤权或人工核验，不能误删新 membership 的凭据。 | FR-006、FR-008、FR-009、FR-016；US1、US4 |
| TEST-007 | 正常+故障 / MOCK+INTEGRATION+ORACLE | 离职、冻结、解冻、房间解绑、人员身份转正、身份映射变更；绑定事务提交后崩溃、重算领取后崩溃、部分住户处理后重启、连续两次换锁，以及范围分页中途入住/退宿、快照失效和收集租约超时。 | 身份映射由 platform Outbox 产生。lock 的 `bindingId/bindingVersion`、旧/新目标与持久重算任务同事务落库；本地已知范围原子落账，范围收集阶段可领取/恢复分页，但只有完整快照或稳定水位校验后的 `SEALED` 范围才可执行授权，不能混入不同水位页面。分项进度和领取/重试状态可恢复，较旧版本不能覆盖新绑定；不得因某个住户完成就遗漏其他住户。冻结不新增，解绑/换锁保留旧设备撤权快照并按先撤后发处理。 | FR-005、FR-006、FR-009、FR-016、FR-020；US1、US2、US4 |
| TEST-008 | 正常+故障 / MOCK+FIELD | 桥接回传 `TRANSPORT_ACK` 后暂不回传设备结果，再回传明确成功/失败。 | 只有传输确认时命令为 `WAITING_ACK`、授权仍非 `ACTIVE`；明确设备成功才进入 `SUCCEEDED`/`ACTIVE`，明确设备失败进入 `FAILED` 或受控重试。 | FR-007、FR-008；US1、US2 |
| TEST-009 | 故障 / MOCK+INTEGRATION | 命令写出后网关断线、进程重启、回执丢失或关联号无法确认；另设有持久证据证明尚未出线的对照场景。 | 持久化上下文可恢复；已出线但无明确结果、是否出线未知或关联号不确定，一律进入 `RECONCILIATION_REQUIRED` 并断言新增物理发送次数为零。只有明确未出线且 operation/profile 批准安全重试、有效期/当前授权/版本仍满足的对照场景才允许 `RETRY_PENDING`；远程开门等短时动作不得延迟自动重放。 | FR-008、FR-020、FR-022；US1、US5 |
| TEST-010 | 正常+故障 / MOCK+INTEGRATION | 对可安全重试的授权/撤权命令重试；同一命令网络重发；桥接重复收到尝试。 | `commandId` 不变、`attemptNo` 单调递增；每次尝试有独立 `wireTaskId` 映射；`commandId + attemptNo` 去重；不产生并行设备发送或第二个业务授权。 | FR-006、FR-007、FR-008、FR-018；US1、US5 |
| TEST-011 | 故障 / INTEGRATION+UNVERIFIED→FIELD | 目标缺少分帧、关联位、ACK 或撤权能力证据；对已验证组合分别替换 gateway、device、model、firmware、protocolProfileId，或收到能力/权限错误。 | 能力只匹配完整五元组及对应操作证据；任一维变化必须重新匹配/核验，不能借同型号另一设备的结果放行。缺证据返回 `CAPABILITY_UNVERIFIED`/受控失败，不发送未知协议，不把源码分支当硬件能力；明确设备失败进入 `FAILED`。 | FR-010、FR-022；US2、US3、US5 |
| TEST-012 | 故障 / INTEGRATION+FIELD | 同一物理锁存在两个 gateway 候选，或旧/新执行方同时尝试发送。 | 只有持有设备执行租约的单一桥接可发送；其他候选被 `EXECUTOR_FENCED`；租约丢失/身份冲突进入对账；过渡期不允许新旧双发。 | FR-017、FR-018、FR-021；US5 |
| TEST-013 | 正常+故障 / MOCK+INTEGRATION | Web 对 WEB-L-001～004（锁/设备/网关型号/人员凭据）完成查询、筛选、详情、编辑和受控不可用动作。 | 八组旧操作的字段、分页、空态、确认和权限差异可逐项对版；园区 scope 服务端裁定；能力未知入口显式禁用并说明原因。 | FR-002、FR-010、FR-011、FR-012、FR-014；US2 |
| TEST-014 | 正常+故障 / MOCK+INTEGRATION | Web 对 WEB-L-005～008（授权、下发结果、记录日志、告警设置）执行新增/撤权/重试/导出/批量处理。 | 结果可关联命令和设备确认；批量部分失败、无选择、时间/整数校验可见；密码/通信日志脱敏；告警处置不掩盖设备异常。 | FR-007、FR-012、FR-020；US2 |
| TEST-015 | 故障 / INTEGRATION | 管理员篡改 `parkId`、猜测其他园区/设备/人员 ID，或直接调用导出/内部接口。 | `smart-platform` 拒绝越权并不泄露资源存在性；`smart-lock` 再验内部身份和 scope；bridge 端点对用户 token 不可达。 | FR-002、FR-011、FR-012、FR-020；US2 |
| TEST-016 | 故障 / MOCK+INTEGRATION | Web 在 HTTP 202、`transportAccepted=true`、页面刷新、超时和迟到回执后查看结果。 | 永远显示“已受理/等待设备确认/失败/需人工核验”等真实状态；不以按钮 loading 或 200 响应冒充“已开门/已授权/已撤权”。 | FR-007、FR-008、FR-012、FR-022；US2 |
| TEST-017 | 故障 / MOCK+INTEGRATION | H5 修改 query、路由、缓存、请求体中的 `badge`/`staffId`/`deviceId`，尝试读取或修改他人门锁。 | 服务端只以 token subject 和当前 membership 决定本人；篡改返回本人范围错误/拒绝，不读取他人或按 badge 换人。 | FR-002、FR-013、FR-020；US3 |
| TEST-018 | 正常+故障 / MOCK+INTEGRATION | H5 本人密码重复提交、登出、身份切换、调宿、退宿后检查页面与网络/浏览器存储。 | 重复请求返回同一 `commandId`；密码不出现在 URL、日志、持久化浏览器缓存或修改请求响应；受控 reveal 单独按 TEST-027 验证；状态显示 `PENDING_PROVISION`/确认中，失效事件清除短时结果。 | FR-007、FR-013、FR-014；US3 |
| TEST-019 | 故障 / MOCK+INTEGRATION+UNVERIFIED | H5 未入住、退宿、冻结、设备离线、能力未验证、动态码限频、人脸校验失败/超时。 | 返回真实限制、`CAPABILITY_UNVERIFIED`/限频/人工核验帮助；不创建设备命令、不生成伪动态码、不显示他人设备细节。 | FR-009、FR-010、FR-013、FR-014；US3 |
| TEST-020 | 正常+故障 / MOCK+INTEGRATION | 将旧资产/授权/任务导入两次，中途停止后续跑；包含重复工号、空工号、跨园区和旧未完成任务。 | 批次、来源键、水位、校验和失败清单可追溯；重跑不重复激活或发送；异常不自动挑第一条人员，不把旧任务状态直接当物理事实。 | FR-001、FR-015、FR-016、FR-017；US4 |
| TEST-021 | 故障 / MOCK+INTEGRATION | 迁移数据包含历史密码、卡、指纹或原始报文，检查日志、导出和异常清单。 | 按批准策略最小化/排除敏感原文；不因数据库缺少凭据就声称锁内已失效；差异进入待处置并有责任人。 | FR-014、FR-015、FR-016、FR-020；US4 |
| TEST-022 | 故障 / INTEGRATION+FIELD | `SHADOW`、`CUTOVER_FREEZE`、旧执行方停机和 `NEW_ONLY` 演练；输入生产镜像入住事件。 | 影子不发真实命令；冻结时可靠排队已验证才受理，否则明确暂停；切换前后任意通信域只有一个真实执行方；所有网关改址回读。 | FR-017、FR-018、FR-021、FR-022；US5 |
| TEST-023 | 故障 / INTEGRATION+FIELD | 新系统已经改变部分设备权限后触发回退评估；包含新增、撤权和在途命令。 | 回退先盘点并处理物理权限/业务增量差异，再切换执行方；不能只恢复旧数据库或让旧执行方与新执行方并发。 | FR-008、FR-019、FR-021；US5 |
| TEST-024 | 正常+故障 / MOCK+INTEGRATION+ORACLE | 查询写操作、补偿、异常和切换审计；同一来源设备事件以不同 eventId、相同 deduplicationKey 并发重投，再构造同去重范围/key 不同内容并重启。 | traceId 关联事实与操作者/服务范围；数据库保证同来源去重键只有一个 canonical，同内容只产生一次通信投影/告警，新的 eventId 有可追溯别名记录；同键不同内容保留原事件并隔离冲突证据，不覆盖原内容或推进授权/命令。原始敏感报文不出普通日志，缺失证据明确未验证。 | FR-020、FR-022；US1、US2、US3、US4、US5 |
| TEST-025 | 正常+故障 / ORACLE+INTEGRATION | 在隔离 Oracle 上用代表性数据执行生命周期、授权、命令和迁移账本查询，覆盖空字符串/NULL、带时区时间、数值精度、稳定排序分页、唯一约束、`membershipId + grantRevision` 和重复来源键。 | 以真实 Oracle 语义和可取得计划验证，不把 MySQL 空值/时间/分页行为当作结论；重复/乱序输入由约束或服务状态明确拒绝/对账；无真实 schema/计划时保持 `UNVERIFIED`，不宣称通过。 | FR-001、FR-003、FR-015、FR-016、FR-017、FR-020、FR-022；US1、US4、US5 |
| TEST-026 | 正常+故障 / MOCK+INTEGRATION+ORACLE+FIELD | 两个有效 grant 共用设备凭据并逐个撤销；同一父 `grantId + grantRevision + membershipId` 需要两个设备、每设备多个凭据，部分确认后重启，再注入旧 revision 的迟到成功。 | 非最后 grant 撤销不物理删钥并记录 `RETAINED_BY_OTHER_ACTIVE_GRANT`，最后资格才删除；多设备目标共享父身份，只有当前 revision 全部必需目标/凭据确认才可 `ACTIVE`。目标、凭据及版本关系持久化可恢复；旧 revision 成功不补齐新 revision，重授不复活已撤旧版本。 | FR-007、FR-008、FR-009、FR-016；US1、US2 |
| TEST-027 | 正常+故障 / MOCK+INTEGRATION | H5 以当前 token 申请本人密码 reveal，分别注入错误 subject、错误用途、其他 membership、过期和重复使用的 `verificationRef`；再检查浏览器、日志和修改请求响应。 | 仅 subject、固定用途、当前 membership、短 TTL、单次使用全部满足才返回短时结果；响应 `Cache-Control: no-store`，只在页面内存显示，服务端只留消费摘要；不得进入 URL/日志/持久缓存，`password-commands` 永不回显秘密；沿用既有认证态，不新增 token 存储。 | FR-013、FR-014、FR-020、FR-022；US3 |
| TEST-028 | 正常+故障 / INTEGRATION | 验证 platform 网关将管理 `/platform/lock/v1/*` 与本人 `/platform/lock/me/v1/*` 路由到兼容门面，再由门面调用 lock；尝试旧 `/app/lock/me/v1/*`、内部 `/internal/lock/v1/*`、bridge/TCP 和旧服务 URL。 | 新路径可由现有平台认证/园区范围进入；本人专例不另设 app 服务前缀；内部接口和 bridge 对用户 token 不可达；旧/错误前缀不能绕过 scope、状态机、单活或切换门禁。 | FR-002、FR-012、FR-013、FR-018、FR-021、FR-022；US2、US3、US5 |
| TEST-029 | 故障 / MOCK+INTEGRATION+ORACLE | 提交四类 targetKind 命令并注入缺字段/伪造身份，包含只指向网关、不关联门锁的合法配置命令；持久化后重启，再更改当前 grant revision、slot、资产版本、管理员范围或让授权到期，尝试重发/处理迟到回执。 | 按分类校验并持久保留不可变的原目标/授权快照，网关目标无需伪造 deviceId，门锁目标不可漏真实 deviceId。重启后不能靠当前表重建并换人换范围；历史 grant/membership/slot、expectedVersion 与短时管理员授权可核验，不存可重放 token。当前权限、版本或期限失效拒绝新发送，原快照不能被改写或延长；迟到回执只作旧尝试证据。资产动作不伪造入住。 | FR-007、FR-011、FR-012、FR-020、FR-022；US1、US2、US5 |
| TEST-030 | 故障 / MOCK+INTEGRATION | 对创建、修改、解绑、配置、撤权和管理员远程开门并发提交缺失、过期或错误的 `expectedVersion`。 | 创建可不带版本；其他修改/解绑/配置/危险动作缺失或版本冲突均返回 `EXPECTED_VERSION_REQUIRED`/`VERSION_CONFLICT`，不覆盖并发变更、不出线；人员 `targetAggregateVersion` 不能替代资产版本。 | FR-006、FR-007、FR-008、FR-012、FR-018、FR-022；US1、US2、US5 |
| TEST-031 | 正常+故障 / MOCK+INTEGRATION+FIELD | 退宿后用已关闭 membership 的历史 grant 发起撤权；同时构造同一 credential/slot 仍被另一有效 grant 引用、slot 已复用和引用关系冲突。 | 撤权不因 membership 已关闭而被错误拒绝；按历史 grant/credential/slot 与当前其他引用判断，非最后有效资格只撤关系并保留物理钥匙，最后资格才删钥；slot 复用/冲突进入 `RECONCILIATION_REQUIRED`，不盲删或恢复旧授权。 | FR-006、FR-008、FR-009、FR-016、FR-022；US1、US2、US4 |

## 3. FR 与测试覆盖索引

| 需求 | 测试编号 |
|---|---|
| FR-001 | TEST-020、TEST-025 |
| FR-002 | TEST-013、TEST-015、TEST-017、TEST-028 |
| FR-003 | TEST-001、TEST-025 |
| FR-004 | TEST-001、TEST-002 |
| FR-005 | TEST-005、TEST-007 |
| FR-006 | TEST-001、TEST-003、TEST-004、TEST-006、TEST-007、TEST-010、TEST-030、TEST-031 |
| FR-007 | TEST-001、TEST-003、TEST-008、TEST-010、TEST-014、TEST-016、TEST-018、TEST-026、TEST-029、TEST-030 |
| FR-008 | TEST-003、TEST-004、TEST-005、TEST-006、TEST-008、TEST-009、TEST-010、TEST-016、TEST-023、TEST-026、TEST-030、TEST-031 |
| FR-009 | TEST-005、TEST-006、TEST-007、TEST-019、TEST-026、TEST-031 |
| FR-010 | TEST-011、TEST-013、TEST-019 |
| FR-011 | TEST-013、TEST-015、TEST-029 |
| FR-012 | TEST-013、TEST-014、TEST-015、TEST-016、TEST-028、TEST-029、TEST-030 |
| FR-013 | TEST-017、TEST-018、TEST-019、TEST-027、TEST-028 |
| FR-014 | TEST-013、TEST-018、TEST-019、TEST-021、TEST-027 |
| FR-015 | TEST-020、TEST-021、TEST-025 |
| FR-016 | TEST-006、TEST-007、TEST-020、TEST-021、TEST-025、TEST-026、TEST-031 |
| FR-017 | TEST-012、TEST-020、TEST-022、TEST-025 |
| FR-018 | TEST-010、TEST-012、TEST-022、TEST-028、TEST-030 |
| FR-019 | TEST-023 |
| FR-020 | TEST-007、TEST-009、TEST-014、TEST-015、TEST-017、TEST-021、TEST-024、TEST-025、TEST-027、TEST-029 |
| FR-021 | TEST-012、TEST-022、TEST-023、TEST-028 |
| FR-022 | TEST-004、TEST-009、TEST-011、TEST-016、TEST-022、TEST-024、TEST-025、TEST-027、TEST-028、TEST-029、TEST-030、TEST-031 |

## 4. 放行门槛

1. `MOCK` 通过只能放行离线状态机/API/敏感字段任务，不能标为生产可用。
2. `INTEGRATION` 通过只能证明隔离服务和契约；Oracle 真实计划、迁移数据、网关地址和生产切换仍需独立证据。
3. 任何 `UNVERIFIED` 的硬件能力、协议字段、网关身份或地址改写都必须保持受控不可用，并在任务中列证据责任人、样本、环境和关闭条件。
4. `FIELD` 证据必须绑定具体型号/固件/profile、通信域、执行时间和脱敏样本；一个设备通过不代表全域通过。
5. 发生 `RECONCILIATION_REQUIRED`、迟到回执、执行方冲突或敏感字段泄漏时，相关场景失败；不得用重跑、前端隐藏、数据库回滚或旧系统状态覆盖问题。
