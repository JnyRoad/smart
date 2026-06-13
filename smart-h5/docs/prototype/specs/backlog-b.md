# 待办事项（backLog，审批侧）后半部分功能规格

> 来源：OLD=/Users/lvtu/source/YUTO/yuto-smart/smart-h5/src（只读，仅提取功能事实）。
> 覆盖 7 页：物品放行(办公区) 列表/详情/结果、园区报修 列表/详情、退宿申请 列表/详情。
> 通用约定：所有列表页带「待我审批的 / 我审批的」双 Tab、下拉刷新、上拉分页（size=10）、空态提示；接口失败 toast「网络错误」。

## 1. 物品放行(办公区) 审批列表 `/xuchang/backLog/goodReleaseWork`

- 旧组件：`views-mobile/pages/backLog/good-release-work/list.vue`（含 `components/search-by-staff.vue`、公共 `components/plateNumber/plateNumber.vue`、`page3-tab`、`page3-bottom`）
- 用途：审批人（含保安）查看待审批 / 已审批的办公区物品放行条，可按员工信息或车牌号筛选。

### UI 元素
- 顶部 Tab：`待我审批的`(approvalStatus=0) / `我审批的`(approvalStatus=1)；进入页可由 query `curTabIndex` 指定。
- 列表卡片（点击进详情）：
  - 标题：`{name}提交的放行条`；右侧状态徽章 `backStatus`（文本，如 待审批/已通过/已拒绝）。
  - 字段行：`申请部门:`(deptName)、`放行事项:`(releaseItemDesc)、`申请时间:`(createTime)、`OA节点:`(oaNode，空显示 `-`)。
- 空态：无数据占位。
- 底部操作条：仅当当前用户是保安（isSecurityGuard===0）显示 `搜 索` 按钮（带搜索图标）。
  - 旧代码中「扫一扫」按钮已注释停用，不实现。

### 搜索（保安专用）
- 默认弹出「按员工信息搜索」弹层，字段：`工号`(badge，文本)、`携带人姓名`(name，文本)、`放行事项`(releaseItem，下拉：人员放行/人员放行(仅限出差使用)/非保密物品放行/保密物品放行/电脑放行/固定资产放行(不包含电脑)/空车放行/自动化物品放行/废品出售)、`申请开始时间`(startTime)、`申请结束时间`(endTime)；头部可切换 `按车牌号搜索` / `确定`。
- 车牌搜索弹层：`普通车牌` / `新能源车牌` 切换 + 车牌键盘输入；车牌格式不合法 toast「车牌号不正确」；车牌值变化即触发按 licensePlate 查询。

### 交互与状态
- 切 Tab / 下拉刷新：重置 current=1 清空列表重新查询；上拉：current<pages 时加载下一页，否则提示无更多。
- 页面状态：加载中、列表有数据、空态、网络错误 toast。

### 跳转
- 点卡片 → `/xuchang/backLog/goodReleaseWork/detail?id={id}&curTabIndex={tab}`。

### 接口
- GET `platform:/articlesrelease/office/page`（approvalStatus, current, size, 可选 badge/name/releaseItem/startTime/endTime/licensePlate）

## 2. 物品放行(办公区) 详情 `/xuchang/backLog/goodReleaseWork/detail`

- 旧组件：`backLog/good-release-work/detail.vue`（引用 `pages/good-release-live/components/process/index.vue` 审批流程、`pages/good-release-work/components/person-tag-detail.vue`、`goods-tag.vue`、`page3-bottom`）
- 用途：审批人（保安）查看放行条详情并执行放行（通过）/拒绝。

### UI 元素
- 申请人卡：`{name} | {deptName}`，右侧徽章 `返厂`(applyMain.sffcDesc==='是') / `不返厂`。
- 字段（均来自 applyMain）：`放行去处:`(fxqcDesc)、`出发地点:`(fxddDesc)、`到达地点:`(ddddDesc)、`放行事项:`(fxsxDesc)、`放行类别:`(wpfxlbDesc)、`放行人级别:`(sqrjbDesc)、`附件:`(fjsc 图片，可预览，空显示 `-`)。
- 放行内容（按 applyMain.fxsx 区分）：
  - fxsx===0 或 7（人员放行）：`放行人员:` 人员标签列表（每项显示姓名 xm），点击 → `/xuchang/goodReleaseWork/detailPersonList`（携带列表）。
  - 其他：`放行物品:` 物品标签列表（`{wpbm}-{wpmc}{wpsl}{wpdw}` 编码-名称数量单位），点击 → `/xuchang/goodReleaseWork/detailGoodsList`。
- 上传区：当 detailInfo.isUploadImg===0 时显示 `上传物品照片`（最多 3 张，base64）。
- 审批流程时间轴（detailInfo.approvalProcess）：提交节点显示 `姓名-结果` + 时间；审批节点显示节点名 statusName + 每个审批人 `姓名-resultDesc`（result：0待审批蓝/1通过绿/2拒绝、3关闭红/4等待灰）+ 可选 `意见: {remark}` + 时间。
- 底部操作条：仅 detailInfo.status===2（待保安处理）显示 `拒 绝`(status=5，次按钮) / `通 过`(status=4，主按钮)。

### 交互与校验
- 需上传照片（isUploadImg===0）而未传任何照片时提交 toast「请至少上传一张照片」。
- 提交参数：guardOneImg/guardTwoImg/guardThreeImg、id、parkId、status、badge(审批人工号)、remark。
- 成功后：扫码进入(isScan)回 `/xuchang/home`；否则跳 `/xuchang/backLog/goodReleaseLive?curTabIndex=1`（旧代码事实，疑似复用生活区跳转）。
- 详情加载失败：toast 后跳 `/xuchang/home`。

### 接口
- GET `platform:/articlesrelease/detail/{id}`（详情，复用生活区接口）
- POST `platform:/articlesrelease/status/security/update`（保安放行/拒绝）

## 3. 物品放行 审批结果 `/xuchang/backLog/goodReleaseWork/result`

- 旧组件：`backLog/good-release-work/result.vue`
- 用途：展示审批完成结果页。旧页为静态占位实现（数据全部写死为 test），无接口调用。

### UI 元素
- 顶部凭证图位（旧页为空图占位，原型用放行凭证二维码占位）+ 提示文案 `您已通过物品放行审批`。
- 申请人卡：`张三 | 研发部` + 徽章 `不返厂`。
- 字段卡：`放行事项:`、`放行时间:`、`OA节点:`。

### 接口
- 无（旧页未接接口，静态展示）。

## 4. 园区报修 审批列表 `/xuchang/backLog/dormRepairs`

- 旧组件：`backLog/dorm-repairs/list.vue`
- 用途：维修负责人查看待接单 / 已处理的园区（宿舍）报修工单。

### UI 元素
- Tab：`待我审批的`(recordState=0) / `我审批的`(recordState=1)；query `curTabIndex` 可指定。
- 列表卡片（点击进详情）：标题 `approveName`，右侧状态徽章 `statusDesc`；字段行：`维修区域:`(rangeTypeDesc)、`维修类别:`(repairTypeDesc)、`所在楼栋:`(dormitoryName)、`故障描述:`(faultDesc)、`申请时间:`(createTime)。
- 空态占位。无底部搜索/扫码。

### 跳转
- 点卡片 → `/xuchang/backLog/dormRepairs/detail?id={approveId}&curTabIndex={tab}`。

### 接口
- GET `platform:/approve/list/repairs/list`（recordType=5, recordState, current, size）

## 5. 园区报修 详情 `/xuchang/backLog/dormRepairs/detail`

- 旧组件：`backLog/dorm-repairs/detail.vue`（含 `components/process/index.vue` 审批流程、`components/process/reply-record.vue` 维修结果）
- 用途：维修负责人接单/拒单，接单后回填维修结果。

### UI 元素
- 状态卡：`状态:`(statusDesc)、`报修时间:`(createTime)。
- 报修信息卡：`姓名:`(name)、`工号:`(staffBadge)、`BU:`(compName)、`部门:`(depName)、`维修区域:`(rangeTypeDesc)、`维修类别:`(repairTypeDesc)、`维修位置:`(dormitoryName#roomName)、`所在园区:`(parkName)、`故障描述:`(faultDesc)、`物品照片:`（最多 3 张可预览）。
- `审批流程` 时间轴（approvalProcess，同模块通用时间轴结构）。
- curTabIndex==0（待我审批）：`描述(非必填)` 多行输入（placeholder 请输入内容，maxlength=100）。
- curTabIndex==1 且 repairReplyList 非空：`维修结果` 回复记录列表，每条：`维修结果:`(replyStatusDesc)、`维修时间:`(replyTime)、`回复人:`(replyName)、`回复内容:`(replyDesc)。
- 底部操作（按 detailInfo.status）：
  - status===0：`不接单`(status=2，次) / `接单`(status=1，主) → 接单接口。
  - status===1：`无法维修`(status=4，次) / `已安排维修`(status=3，主) → 维修结果接口。

### 交互与状态流转
- 接单/不接单参数：approveBadge、id、remark、status；成功跳列表 `?curTabIndex=0`。
- 维修回复参数：approveBadge、id、result(描述内容)、status；成功跳列表 `?curTabIndex=1`。
- 状态流转：0待接单 → 1已接单(待维修) → 3维修成功 / 4无法维修；0 → 2不接单关闭。

### 接口
- GET `platform:/dormitory/repair/query/detail/{id}`（详情）
- GET `platform:/dormitory/repair/status/update`（接单/不接单）
- POST `platform:/dormitory/repair/reply`（维修结果回复）

## 6. 退宿申请 审批列表 `/xuchang/backLog/dormExit`

- 旧组件：`backLog/dorm-exit/list.vue`（含 `components/search-by-staff.vue`）
- 用途：审批人（主管/保安）查看待审批 / 已审批的退宿申请；保安可按员工信息搜索、（旧代码保留）微信扫码直达详情。

### UI 元素
- Tab：`待我审批的`(status=0) / `我审批的`(status=1)；query `curTabIndex` 可指定。
- 列表卡片（点击进详情）：标题 `{name}提交的退宿申请`；右侧徽章 `statusDesc`（已审批 Tab 下 status=1 绿色、status=3 红色）；字段行：`房间信息:`(dorDetailStr 多行)、`退宿原因:`(quitReasonDesc)、`预计离开时间:`(applyLeaveTime)、`申请时间:`(createTime)。
- 空态占位。
- 底部操作条：保安（isSecurityGuard===0）显示 `搜 索`；搜索弹层字段：`工号`(badge)、`姓名`(name)，头部 `按员工信息查找` / `确定`。「扫一扫」按钮在旧代码中已注释停用（其 wx.scanQRCode 逻辑保留：扫物品放行码按 articlesType 3/4 分流到生活区/办公区详情）。

### 跳转
- 点卡片 → `/xuchang/backLog/dormExit/detail?id={id}&curTabIndex={tab}`。

### 接口
- POST `platform:/dor/quit/list/approval`（isSecurityGuard, parkId, status, current, size, 可选 badge/name）
- GET `app:/wechat/sign`（微信 JS-SDK 签名，扫码用，当前入口停用）
- GET `platform:/articlesrelease/detail/{id}`（扫码结果分流查询，当前入口停用）

## 7. 退宿申请 详情 `/xuchang/backLog/dormExit/detail`

- 旧组件：`backLog/dorm-exit/detail.vue`（含 `components/process/index.vue`）
- 用途：主管审批退宿（通过/拒绝），保安二次确认放行（保安通过/拒绝）。

### UI 元素
- 申请人卡：`姓名`(name) + 人脸照片（facePic=GET_IMAGE_URL/faceId，缺省占位头像）。
- 字段：`房间信息:`(dorDetailStr 多行)、`退宿原因:`(quitReasonDesc)、`预计离开时间:`(applyLeaveTime)、`备注:`(remark)、`照片:`（最多 3 张可预览）。
- `审批流程` 时间轴（processRecord，同模块通用时间轴结构）。
- curTabIndex==0（审批模式）：`描述(非必填)` 多行输入（placeholder 请输入内容，maxlength=50）。
- 底部操作（仅审批模式 curTabIndex==0）：
  - detailInfo.status===1（待主管审批）：`拒绝`(status=3，次) / `通过`(status=2，主)。
  - detailInfo.status===2（待保安确认）：`拒绝`(status=5，次) / `保安通过`(status=4，主)。

### 交互与状态流转
- 审批模式判定：扫码进入(isScan) 强制审批模式且改走扫码详情接口；普通进入由详情返回的 isApprove 置审批模式（默认查看模式）。
- 提交参数：id、remark(描述)、status、approveBadge；成功后扫码进入回 `/xuchang/home`，否则跳列表 `?curTabIndex=1`。
- 详情加载失败：toast 后跳 `/xuchang/home`。
- 状态流转：1 待主管审批 → 2 待保安确认（主管通过）/ 3 已拒绝；2 → 4 已放行（保安通过）/ 5 保安拒绝。

### 接口
- GET `platform:/dor/quit/detail/{id}`（详情）
- GET `platform:/dor/quit/list/check/{id}`（扫码详情）
- GET `platform:/dor/quit/status/update`（审批）

## 不确定点

1. 办公区放行详情审批成功后旧代码跳 `/xuchang/backLog/goodReleaseLive?curTabIndex=1`（生活区列表），疑似旧 bug 或刻意复用，按旧行为记录。
2. 办公区放行详情提交参数含 `remark`，但旧页表单未渲染任何 remark 输入项（始终 undefined），原型不增加该输入。
3. result.vue 为纯静态页（数据写死 test、空图占位、无路由跳入逻辑），原型按"审批通过结果凭证页"以 mock 数据呈现。
4. 列表"扫一扫"按钮在两处旧代码中均被注释停用，原型不展示，仅在规格中记录其分流逻辑。
5. 退宿详情 status 与列表 status 枚举（1/2/3/4/5 的中文文案）后端未在前端代码中给全，原型采用推断文案：1待审批、2主管已通过(待保安确认)、3已拒绝、4已放行、5保安拒绝。
