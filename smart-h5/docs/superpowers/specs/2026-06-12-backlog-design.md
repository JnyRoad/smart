# 第 4 批模块设计：backLog 审批域（10 页 + JSSDK 扫码 Spike）

> 事实来源：docs/prototype/specs/backlog-a.md、backlog-b.md（旧仓库净室分析）；旧服务层 services/backLog.js。实现时每页 E2E mock 形状对照旧 Vue 源码（views-mobile/pages/backLog/）。

## 1. 范围裁剪与分支

- **不实现 2 个 result 页**（goodReleaseLive/result、goodReleaseWork/result）：旧版为纯静态占位（数据写死 test、无接口），且全仓无任何跳转指向它们——判定已废弃，YAGNI 跳过；规格留档，如需恢复成本极低。其余 10 页全做。
- 旧版已注释停用的「扫一扫」按钮与车牌搜索弹层入口不实现（与批 3 同口径）；**接口层各列表查询类型保留可选 `licensePlate` 字段（契约完整性），UI 不做任何入口**——单列为 YAGNI 裁剪项。JSSDK 扫码作为 **Spike 文档**交付（见 §6）。

| 分支 | 内容 |
|---|---|
| 1 `feat/backlog-live` | 待办首页 + 生活区审批 列表/详情 + 免登录放行条 /code（4 页）+ 域公共层（审批 Tab 列表骨架、approve api） |
| 2 `feat/backlog-approvals` | 办公区审批 列表/详情 + 报修审批 列表/详情 + 退宿审批 列表/详情（6 页）+ Spike 文档 |

每分支：TDD → 全绿 → 子 agent 评审 → 修复 → 复评 → PR → 合并。home 宫格 `/approve→/backlog` 已映射，补死链回归。

## 2. 路由表（10 页）

- `/backlog`（待办首页，3 入口卡）
- `/backlog/release-live`、`/backlog/release-live/detail?id=&sort=&tab=`（生活区审批）
- `/code?id=`（免登录放行条——顶层路由对齐旧 `/xuchang/backLog/code` 的免登录语义；页面不挂 useRequireAuth，详情接口带 token 时自动携带；**真实网关是否允许匿名访问列入集成清单**）
- `/backlog/release-work`、`/backlog/release-work/detail?id=&tab=`
- `/backlog/repairs`、`/backlog/repairs/detail?id=&tab=`
- `/backlog/dorm-exit`、`/backlog/dorm-exit/detail?id=&tab=`

Tab 直达统一新约定：`?tab=done` 直达「我审批的」，无参/其他值默认「待我审批的」（旧 curTabIndex 不复用，批 3 先例）；详情接收 `?tab=done` 表示只读。**成功跳转**：审批通过/拒绝类 → 列表 `?tab=done`；报修接单/不接单 → 无参列表（默认待我审批 Tab，等价旧 curTabIndex=0），不引入 `tab=todo` 枚举。

## 3. 域公共层 `src/features/backlog/`

- `api.ts`：
  - GET `platform:/approve/list/new/page`（recordType=3、recordState 0/1、分页、可选 badge/name/startTime/endTime）→ 生活区审批列表（条目：approveId、approveName、approveNodeDesc、approveDesc、approveState（'1' 绿 '2' 红）、sort、articlesTypeDesc、articleName、carrier、roomInfo、createTime）
  - GET `platform:/articlesrelease/status/update`（approveBadge、id、status 2/3、remark）室友/宿管审批
  - POST `platform:/articlesrelease/status/security/update`（guardOneImg/Two/Three、id、parkId、status 4/5、badge、remark）保安放行
  - GET `platform:/approve/list/repairs/list`（recordType=5、recordState、分页）报修审批列表（approveId、approveName、statusDesc、rangeTypeDesc、repairTypeDesc、dormitoryName、faultDesc、createTime）
  - GET `platform:/dormitory/repair/status/update`（approveBadge、id、remark、status 1/2）接单/不接单；POST `platform:/dormitory/repair/reply`（approveBadge、id、result、status 3/4）维修结果
  - 退宿审批列表复用 dorm-services 的 `getDormExitPage`？**不复用**——审批侧是 POST `platform:/dor/quit/list/approval`（isSecurityGuard、parkId、status 0/1、分页、可选 badge/name），单独建；GET `platform:/dor/quit/status/update`（id、remark、status、approveBadge）审批
  - 办公区审批列表复用批 3 `getWorkReleasePage`？**不复用**——审批侧带 approvalStatus 与搜索条件且无 badge/type 固定参数，按旧 backLog 用法单独建 `getWorkApprovalPage`（GET `/articlesrelease/office/page`，approvalStatus、分页、可选 badge/name/releaseItem/startTime/endTime）（实现时核对旧 backLog/good-release-work/list.vue 的实参）
  - 详情/保安放行接口与批 3 共用处直接 import `features/good-release/api`（getReleaseDetail）与 `dorm-services/api`（getRepairDetail、getDormExitDetail）。
- `approval-tabs.tsx`：双 Tab「待我审批的/我审批的」页内组件（参数化 label 与计数语义，return-factory 同构样式）；`?tab=done` 直达。
- `useIsSecurityGuard`：baseinfo 的 `isSecurityGuard === 0` 为保安（**0 是 / 1 否**，旧注释事实）；搜索按钮仅保安显示（生活区/办公区/退宿三个审批列表）。
- 搜索弹层复用 return-factory 的字段集（badge/name/startTime/endTime）；办公区多一个 releaseItem 下拉（FXSX_OPTIONS 复用批 3 字典）；退宿只有 badge/name。做成 `staff-filter-popup.tsx`（字段可配）。

## 4. 分支 1：backlog-live（4 页）

### `/backlog`
- 标题「待办事项」+ 3 入口卡（物品放行（生活区）→ /backlog/release-live、退宿审批 → /backlog/dorm-exit、园区报修审批 → /backlog/repairs）。纯静态导航。旧版注释禁用的 4 入口不做。
- 注意：home 审批角标已有三接口（批 1 实现），本页不重复拉数。

### `/backlog/release-live`（审批列表）
- ApprovalTabs（recordState 0/1）+ useListPager；卡片：标题 approveName；右上 Tab0 显示 approveNodeDesc 灰、Tab1 显示 approveDesc（approveState '1' 绿/'2' 红）；行：物品类型/物品名称/携带人/房间信息/申请时间。
- 保安搜索（badge/name/startTime/endTime）。点卡 → `detail?id={approveId}&sort={sort}&tab={当前}`。

### `/backlog/release-live/detail`
- GET /articlesrelease/detail/{id}（复用批 3 类型）；加载失败 toast 后 `router.replace('/home')`（旧行为）。
- 申请信息卡（同批 3 live 详情骨架：carrier+facePic、类型/名称/房间/离厂时间/车牌/备注/三图预览）。
- 「上传物品照片」：仅 sort=3 且 detail.isUploadImg===0 且 tab≠done 且 status<4，ImageListUpload base64 ≤3。
- ApprovalTimeline；「审批意见（选填）」输入：tab≠done 且 status<4。
- 操作按钮：sort 1/2 且 tab≠done →「拒 绝」(status update 3)/「通 过」(2)，GET status/update（approveBadge=当前工号、remark）；sort=3 且 tab≠done 且 status<4 →「拒绝放行」(5)/「确认放行」(4)，POST security/update（guard 三图按序、parkId、badge、remark）；保安需图未传 → toast「请至少上传一张照片」。
- 成功跳 `/backlog/release-live?tab=done`（扫码场景 isScan 本版无入口，不实现）。

### `/code?id=`（免登录）
- 不挂 useRequireAuth。GET /articlesrelease/detail/{id}。
- 放行码五态互斥（**与批 3 qrPanelState 不同**，单独纯函数 `codePanelState` + 单测）：expire→「放行码已过期」；status 1→「放行码仍在审批中，请稍后」；2→二维码 +「扫描放行码以识别备案物进行放行」；3→「审核失败」；4→「已出厂」；5→「拒绝放行」。
- 携带人卡（carrier+facePic）；单据卡：parkName、articlesTypeName 标签、宿舍位置 `dormitoryName-floorName层-roomName号房-bedName床`（有宿舍才显示）、plannedDepartureTime、remarks、三图；状态卡：statusName||'-'、拒绝原因 remark（仅 5）、securityStaff||'-'（4/5）、departureTime||'-'（仅 4）；流转记录（approvalProcess 非空）+「电话联系」`tel:{phone}`。
- 旧 code.vue 双层 `res.data.data` 解包疑为历史差异，按单层 data 实现并记录（与详情页一致）。

## 5. 分支 2：backlog-approvals（6 页）

### `/backlog/release-work` + detail
- 列表：ApprovalTabs（approvalStatus 0/1）+ 卡片（{name}提交的放行条 + backStatus 徽章；申请部门 deptName/放行事项 releaseItemDesc/申请时间/OA节点 oaNode||'-'）；保安搜索（badge/name/releaseItem 下拉/startTime/endTime）。
- 详情：复用批 3 work 详情骨架（申请人卡+applyMain 字段+人员/物品标签云→只读链快照）+ isUploadImg===0 时「上传物品照片」≤3 + ApprovalTimeline；操作仅 status===2 且 tab≠done →「拒 绝」(5)/「通 过」(4) POST security/update（remark 恒不渲染输入框，旧事实）；需图未传 toast「请至少上传一张照片」；成功跳 `/backlog/release-work?tab=done`（**修正旧版误跳生活区列表的 bug，spec 记录偏差**）；失败 toast 后回 /home。

### `/backlog/repairs` + detail
- 列表：ApprovalTabs（recordState 0/1，GET approve/list/repairs/list recordType=5）+ 卡片（approveName + statusDesc 徽章；维修区域/类别/楼栋/故障描述/申请时间）。无搜索。点卡 → detail?id={approveId}&tab=。
- 详情：状态卡（statusDesc、createTime）+ 报修信息卡（姓名/工号/BU/部门/区域/类别/维修位置 dormitoryName#roomName/园区/故障描述/三图预览，复用批 2 getRepairDetail）+ ApprovalTimeline + tab≠done 时「描述(非必填)」TextArea(maxLength 100) + tab=done 且 repairReplyList 非空时维修结果列表（批 2 RepairReply 渲染复用）。
- 操作（按 status）：0 →「不接单」(2)/「接单」(1) GET repair/status/update → 成功跳无参列表（默认待我审批 Tab，旧 curTabIndex=0）；1 →「无法维修」(4)/「已安排维修」(3) POST repair/reply（result=描述）→ 成功跳 `?tab=done`。维修结果列表渲染**先把批 2 报修详情页的回复记录块抽成可复用组件**（RepairReplyList），勿直接 import 页面局部实现。

### `/backlog/dorm-exit` + detail
- 列表：ApprovalTabs（status 0/1，POST dor/quit/list/approval：isSecurityGuard、parkId、分页）+ 卡片（{name}提交的退宿申请 + statusDesc（Tab1 下 status 1 绿/3 红？旧事实：已审批 Tab status=1 绿、3 红——以旧 list.vue 配色为准实现时核对）；房间 dorDetailStr 多行/退宿原因/预计离开时间/申请时间）；保安搜索（badge/name）。
- 详情：申请人卡（name+faceId 头像——**先把批 2 dorm-exit 详情页的 FaceAvatar 抽成可复用组件**）+ 字段（dorDetailStr/quitReasonDesc/applyLeaveTime/remark/三图）+ ApprovalTimeline。**时间线字段归一化**：审批侧详情返回 `processRecord`（旧 detail.vue:63-67），与用户侧 `approvalProcess` 字段名不同——加 normalizer（`processRecord ?? approvalProcess ?? []`）+ 单测，防空白时间线。tab≠done 时「描述(非必填)」(maxLength 50)。
- 操作：**审批模式 = `detail.isApprove === true`（且 tab≠done）；isApprove 缺失/false 一律只读不显示按钮**（旧版默认查看模式，按钮仅 isApprove 开放——旧 detail.vue:115-141）。status===1 →「拒绝」(3)/「通过」(2)；status===2 →「拒绝」(5)/「保安通过」(4)；GET dor/quit/status/update（id、remark、status、approveBadge）→ 成功跳 `?tab=done`；失败 toast 回 /home。

## 6. JSSDK 扫码 Spike（文档交付 docs/superpowers/specs/2026-06-12-jssdk-scan-spike.md）

- 事实：旧版扫一扫全部被注释停用；逻辑为 GET `app:/wechat/sign?url=` 取签名 → wx.config(scanQRCode) → wx.scanQRCode 得二维码文本（放行单 id）→ GET /articlesrelease/detail/{id} 按 articlesType 3/4 分流到生活区/办公区审批详情；退宿扫码走 GET /dor/quit/list/check/{id}。
- **本批只交付 Spike 文档与风险清单，不写任何代码**（入口在旧版已停用，无行为基线可对照）；文档内的「未来落地形态建议」（wechat-scan.ts 封装 + feature flag）仅作为后续实施参考，不在本批落地。

## 7. 测试策略

- 单测：codePanelState 五态；api 层无新纯逻辑不强行单测。
- E2E（mock 摘旧 Vue）：
  - backlog 首页 3 入口导航 + home 宫格 `/approve` 死链回归。
  - 生活区审批：列表双 Tab 字段/配色（approveState '1'/'2'）→ 详情 sort=1 通过（GET status/update 参数断言）→ sort=3 保安 isUploadImg=0 未传图拦截 → 传图后确认放行（POST security/update 三图+parkId 断言）→ tab=done 只读无按钮。
  - /code：五态互斥渲染 + tel: 链接 + 免登录（不 seed token 直接访问）。
  - 办公区审批：status=2 显示按钮、通过后跳 `?tab=done`（修正项回归）；isUploadImg 拦截。
  - 报修审批：status 0 接单（GET 参数断言）→ status 1 已安排维修（POST reply 断言）；维修结果列表渲染。
  - 退宿审批：status 1 主管通过 → status 2 保安通过（status/update 参数断言）；保安搜索条件重查；**isApprove 缺失/false 时无操作按钮**（高风险回归点）；processRecord 归一化单测。
- 保安身份 mock：baseinfo 返回 isSecurityGuard 0/1 两态断言搜索按钮显隐。

## 8. 防御与边界决策

1. 详情加载失败统一 toast 后 `router.replace('/home')`（旧行为，三个详情一致）。
2. 办公区审批成功跳转修正为本模块列表（旧版跳生活区为 bug，已记录偏差）。
3. result 两页不做（§1）；扫一扫不做（Spike 文档）。
4. /code 免登录与网关匿名访问的矛盾列入集成清单（页面不强制登录，接口有 token 则带）。
5. 退宿审批模式以 `isApprove === true` 为准，字段缺失默认**只读**（不显示操作按钮）——与旧版默认查看模式一致。
6. 车牌搜索：接口类型保留可选 licensePlate，UI 无入口；E2E 断言列表请求不携带 licensePlate 参数。
7. **已接受的「严于旧版」偏差**：生活区审批 sort 1/2 按钮额外要求 status<4（旧版只看 curTabIndex，已出厂单据理论上仍可点）；提交人 badge 未就绪时 toast 拦截（旧版会发出 undefined）。
8. **过渡期死链**：分支 1 合并后、分支 2 合并前，home 砖「园区报修审批」与 /backlog 入口「退宿审批/园区报修审批」指向的 `/backlog/repairs`、`/backlog/dorm-exit` 暂为 404（与批 3 return-factory→work 只读链先例同口径）；**分支 2 必须紧随合并**。
9. **联调核对项**：① 报修「无法维修/已安排维修」依赖已接单（status=1）记录仍出现在 recordState=0 列表（旧版行为暗示如此），若后端归入 recordState=1 则该操作不可达——真实数据验证；② `isApprove` 严格 `=== true` 判等，若后端返回 1/'true' 按钮会全灭——联调冒烟必验一条真实待审批退宿单；③ faultImgs 字段格式（原始 base64 vs data URL vs URL）已用 toImageSrc 防御，真实响应核对一次。
10. **时间线载荷归一化**：旧网关真实节点形状是 `{recordNode, statusName, staffInfos[{staffName,result,resultDesc,remark,recordDate,createDate}]}`（recordNode===0 为提交节点）；ApprovalTimeline 经 `normalizeProcess` 同时接受该形状与内部 `{statusName, approvers[]}` 形状——**批 2/3 的 e2e mock 用的是内部形状，真实载荷字段核对列入集成清单**。
