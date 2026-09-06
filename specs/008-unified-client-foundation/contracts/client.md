# 客户端内部契约
核心UTS模块不含平台API：
- catalog.uts：MODULES、visibleModules(identity, query, category)、hasPermission(identity, permission)、resolveModule(id)、favoriteModules(identity, ids)，不认通配符权限。
- scan.uts：normalizeScan(value)仅去除末尾CR/LF且保留前导零；拒绝空值、嵌入控制字符、超长(4096字符)，不按扫码文本执行URL。
- models.uts：Identity、Post、ModuleEntry、Application、TimelineEntry、ApplicationDraft、BusinessKind、ActionKind。
- workflow.uts：validateDraft、canAct、transitionApplication；演示数据遵守基础状态/身份/岗位边界，真实服务端另行终审。
- demo.uts：DEMO_IDENTITIES三类人员+主管+安检；createDemoApplications提供显式演示种子，无真实个人数据。
页面依赖state/session.uts集中操作，禁止页面直接调用生产HTTP写接口。
真实登录采用核实后的现有认证入口或新统一身份适配契约；缺失集成不允许伪造已登录状态。完整现状与差距在smart-app/docs/integration.md记录。

## 当前契约修订：供应商厂牌通行（待后端实现）
供应商不再调用 applications 或申请审批 actions。新增拟议接口：
- POST /api/v1/visitor-checks：credentialCode、postId；返回短时核验结果，含人员/单位/入厂授权/区域/有效期/当前状态/允许方向。验证失败明确返回拒绝状态或业务错误，不能登记。
- POST /api/v1/visitor-passes：verificationId、postId、direction；Idempotency-Key 必填。后端重新校验人员、岗位、当前资格及状态，原子写入一次事件。同一键重试返回同一结果。
- GET /api/v1/visitor-passes：读取当前身份有权查看的事件，服务端负责数据范围；客户端筛选方向与人员/单位/厂牌。
二维码原文作为不透明凭证传入，只查验现有入厂申请及厂牌授权，不由客户端生成资格。旧厂牌编码格式、真实授权来源与状态规则仍需后端联调确认。现场核验按用户明确指示仅扫描厂牌二维码；不新增 App 内申请或审批流程。

## 现场执行与完整人员核验补充
- actions 契约新增 execution={mode:'escort'|'lock',escortProof:string,lockNo:string}。押运人与定位锁必须二选一，出发登记transport，到达沿用模式且重新核验。真实服务必须重新验证人员刷卡证明/锁号及状态；客户端当前仍阻断未接入的真实执行。
- supplier verification 增加 photoUrl、visitorPhone、hostName、hostPhone、authorizedAreas:string[]；空照片/联系方式由页面明确展示缺失，禁止制造人员资料。
- loadExecutionApplications、findScannedApplication 须先有当前授权岗位，且只定位该岗位当前可执行单据；一般申请及审批读取保持独立。


## Phase 10：申请选项和现场查询（待服务端接入）
- GET `/api/v1/item-passes/posts`：Bearer 会话与 `item-pass:apply` 权限；返回 `{posts:[{id,name,parkId,parkName}]}`。候选不是执行授权，普通申请人可以没有 Identity.posts。
- GET `/api/v1/item-passes?scope=execute&postId=...`：服务端必须同时核当前身份、`item-pass:execute` 权限、岗位归属和可执行状态。空岗位在客户端请求前拒绝。未提供 scope 的列表继续只返回当前用户有权查询的记录。
- 申请提交起终点须在当前候选集合内，后端仍须再次验证最新路线和数据权限；申请候选请求失败、未加载完成、账号切换均不得使用缓存候选提交。
- 工作台与待办分别取得本人/审批记录和当前岗位执行记录，再按单号合并；执行待办只采用岗位接口实际返回且仍可执行的单据，普通列表返回的本人记录不转成执行任务。最近申请始终以 `view=read` 打开。
- `Application` 响应可包含字符串 `fromPostName`、`toPostName`，供无执行岗位的申请人和审批人显示起终点；名称由服务端根据单据点位生成，不接受草稿自称的名称，不授予岗位权限。可选名称类型错误时拒绝响应，旧响应缺省仍兼容，详情找不到可见名称时回退到标识。

## Phase13供应商接口实现

上述供应商三个路径已在smart-platform-biz的client/supplier实现，默认关闭。credentialCode必须是SMT_ADMITTANCE_FELLOW.ID的1至19位正十进制字符串，拒绝JSON数字，且不查询或回退短信预约码；badgeId/admissionId始终为字符串。首次presence=unknown且allowedDirections=[enter,leave]；inside只允许leave，outside只允许enter。记录为最新100条有权岗位事件。错误使用400/401/403/404/409/503与固定泛化正文，新增接口不记录扫码正文或人员快照。记录ID仅定位人员，资格在核验和提交时重新读取；真实认证与网关仍单独验收。
