# 数据模型
- Identity：subjectId稳定内部ID；staffNo显示工号；displayName姓名；employmentType人员来源；organization单位；permissions权限码；posts允许执行岗位。
- Module：id、title、description、category、permission、route；本地注册且默认拒绝未知模块。
- Application：id、kind(item-pass)、title、reason、applicantId/name、fromPostId/toPostId、supplierName、visitorName、materials、seals、status、timeline。
- 放行状态：pending → approved/rejected；approved → transporting → completed。
- 供应商以入厂申请签发的厂牌核验，进出状态由历史事件及服务端资格共同裁定，不进入 Application 状态机。
- 演示只允许本账号申请、拥有审批权限且非本人审批、执行权限与对应岗位；固定演示授权不代表实际生产关系。
- ScanResult：value string、source(hardware/camera)。不转换数字，不解析执行任意URL。

## 当前有效修订：供应商进出不使用 Application
Application 仅处理保密物品放行；历史设计中的 supplier 申请及 pending/approved/inside 状态不再适用。
SupplierVerification 包含核验标识、厂牌标识、人员、单位、入厂申请引用、岗位与区域、授权有效期、核验失效时间、是否通过、拒绝原因、当前在内/在外状态及允许的 enter/leave 方向。凭证原文仅用于核验请求，不作为客户端授权依据。
SupplierPassage 为不可变进出事件：id、verificationId、badgeId、visitorName、supplierName、admissionId、postId、areaName、direction、operatorName、occurredAt。查询采用方向与人员/单位/厂牌条件。
核验结果只在当前身份和岗位上下文中有效；切换或过期必须重扫。真实服务提交时重新验证资格并以幂等键返回唯一事件。演示仅使用预置虚构厂牌，不给任意输入授予资格。
外包与派遣保留来源值、统一展示分类，不由 employmentType 自动授予业务权限。

## 连续扫码与押运核验模型
Application.transport 记录出发时 mode（escort/lock）及 lockNo，不允许到达改方式或换锁。现场执行输入 execution 包含 mode、escortProof、lockNo，互斥校验；刷卡证明绑定会话、岗位、单据和动作，演示证明不能用于真实模式。
SupplierVerification 增加 photoUrl、visitorPhone、hostName、hostPhone、authorizedAreas；响应/副本都保留这些字段，缺失照片显示暂无照片。照片与联系方式仅用于当前授权核验，不写入设备持久设置。


## 申请候选与执行授权分离
`ApplicationOptions={posts:Post[]}` 是当前申请人可以填写的起终点候选，只用于申请表，不写入 `Identity.posts`。`Identity.posts` 始终是当前人员可执行的安检岗位。候选只在内存保存，随会话清空，不授予审批或执行权限。现场查询上下文包含 `scope=execute` 与当前授权 `postId`。

`Application.fromPostName` / `toPostName` 为可选的点位名称快照，随查询和动作结果保留，仅用于展示。它们不属于 `ApplicationDraft`，应从可信点位目录生成；演示模式从独立候选生成。普通员工及审批人无需拥有执行岗位就能查看名称。

## 供应商服务端规则快照（Phase11）
- 资格快照：人员、单位、厂牌、入厂申请标识与有效状态，审批是否通过，有效时间窗口、授权区域及人员展示资料；由未来真实查询适配提供，不从App正文直接绑定为可信对象。
- 操作上下文：已认证工作人员、权限、授权岗位及可信岗位区域关系。供应商不是操作账号。
- 在内状态：以人员与区域为主体，含unknown/inside/outside及版本；新核验只有在资格和岗位检查通过后可建立unknown，首个明确进出动作决定实际状态，不能用换厂牌绕过重复进出校验。
- 短期核验：绑定操作人、岗位、区域、人员、厂牌、入厂申请、在内状态版本、核验时间与失效时间；失效不得晚于资格到期或核验后5分钟。
- 通行结果：新的人员区域状态和单次不可变事件。事件保留核验ID、服务端事件ID、操作人、方向、时间与资格引用；当前资格必须在登记时重验。数据库层后续需原子更新状态、消耗核验并记录幂等结果。

## 物品放行持久化（Phase12）
- 当前单据表保存单号、版本、状态、申请人/审批人、起终点及完整JSON快照；快照由显式codec恢复不可变值对象。
- 事件表只追加，单号/版本唯一；每次业务变更恰好追加一条领域事件。
- 命令表按服务端作用域、操作者、幂等键唯一，保存请求摘要和当次结果快照；状态前进后旧命令重试仍返回原结果，不伪装成当前状态。
- 三者在一个JDBC事务内更新，CAS版本冲突或事件写入失败不得遗留部分状态。当前权限检查仍先于幂等原回复读取。

## 供应商持久化（Phase12）
- 人员区域状态表以personId/areaId为主键，不以厂牌或入厂申请为主键；旧严格 `verify` 路径找不到基线必须拒绝。App HTTP 使用 `verifyOrInitialize`：仅在当前资格和岗位均已可信校验后，才可创建 `UNKNOWN`、版本为0的首次基线；无效资格仍必须拒绝。测试自行建立的合成基线不代表生产人员当前位置。
- 核验表保存领域核验的完整绑定、状态版本、半开有效期与一次性消耗状态；人员换厂牌也不能绕过当前状态。
- 事件表保留不可变通行事件，核验ID唯一、人员区域版本唯一；命令表保存按作用域/操作者/键归属的请求摘要及原结果。
- 状态CAS、核验条件消耗、插事件与命令结果必须原子提交。登记仍使用当前可信资格和岗位映射，原核验的成功标志不能代替重验。
