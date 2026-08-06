# 待办事项（backLog，审批侧）模块功能规格 · 前半部分（A）

> 来源：旧仓库 `smart-h5/src/views-mobile/pages/backLog/`（只读净室分析，仅提取功能事实）。
> 覆盖页面：待办首页、物品放行（生活区）审批列表 / 详情 / 结果、二维码放行条。

## 通用背景

- 物品放行（生活区）审批流为三级节点：**室友（sort=1）→ 宿管（sort=2）→ 保安（sort=3）**。
- 单据状态 `status`：1 审批中、2 审批通过、3 审批未通过（拒绝）、4 已出厂（保安确认放行）、5 拒绝放行（保安扫码拒绝）；另有 `expire`（放行码过期）布尔位。
- 流程节点内人员结果 `result`：0 待审批（蓝）、1 通过（绿）、2 拒绝（红）、3 关闭（红）、4 等待（灰）。
- 用户信息来自登录态 store：`employeeBadge`（工号）、`isSecurityGuard`（0 是保安 / 1 否）、`parkInfo.id`（园区 id）。

---

## 1. 待办事项首页 `/xuchang/backLog`

旧文件：`backLog/index.vue`（组件 `page2-title`、`page2-list`）

### 用途
审批侧的功能入口聚合页。

### UI 元素
- 页头：标题「待办事项」+ 右侧装饰插图（bg.png）。
- 入口列表（卡片行：左图标 + 标题 + 右箭头），当前启用 3 项：
  1. 物品放行（生活区）→ `/xuchang/backLog/goodReleaseLive`
  2. 退宿审批 → `/xuchang/backLog/dormExit`
  3. 园区报修审批 → `/xuchang/backLog/dormRepairs`
- 每项支持可选副文案 `tip`（当前均为空）。
- 旧代码中被注释禁用的入口（不实现）：访客审批、物品放行（办公区）、离职交接审批、员工申诉回复。

### 交互
- 点击入口行 → router.push 对应路由。

### 接口
无（纯静态导航页）。

---

## 2. 物品放行（生活区）审批列表 `/xuchang/backLog/goodReleaseLive`

旧文件：`backLog/good-release-live/list.vue`（组件 `page3-tab`、`page3-bottom`、`search-by-staff`、`plateNumber`）

### 用途
审批人查看「待我审批的」与「我审批的」生活区物品放行申请，并进入详情处理。

### UI 元素
- 顶部双 Tab：「待我审批的」(recordState=0) /「我审批的」(recordState=1)，可由路由 query `curTabIndex` 预选。
- 申请卡片列表，每卡字段：
  - 标题：`approveName`（申请名称/申请人）
  - 右上状态：Tab0 显示 `approveNodeDesc`（当前节点描述，灰色）；Tab1 显示 `approveDesc`，按 `approveState` 着色（'1' 绿=通过、'2' 红=拒绝）
  - 行字段：物品类型 `articlesTypeDesc`、物品名称 `articleName`、携带人名称 `carrier`、房间信息 `roomInfo`、申请时间 `createTime`
- 下拉刷新（「更新成功」提示）+ 上拉分页加载（size=10，超过总页数停止）。
- 空态组件（无数据时）。
- 底部「搜索」按钮：**仅当 `isSecurityGuard === 0`（保安）显示**。
- 搜索弹层（按员工信息查找）：工号 `badge`、携带人姓名 `name`、申请开始时间 `startTime`（时间选择）、申请结束时间 `endTime`（时间选择）+ 确定按钮。
- 旧代码保留但入口已注释的能力（原型以演示说明保留、不作主入口）：按车牌号搜索弹层（`licensePlate` 过滤）、微信扫一扫（扫码直达详情）。

### 交互与流转
- 切 Tab → 重置分页、清列表、重新请求。
- 点击卡片 → `/xuchang/backLog/goodReleaseLive/detail?id={approveId}&curTabIndex={当前Tab}&sort={item.sort}`。
- 搜索确定 → 以筛选条件重查列表。
- 请求失败 → toast `message || '网络错误'`。

### 接口
| 用途 | Method | Path | 关键参数 |
|---|---|---|---|
| 审批列表 | GET | platform `/approve/list/new/page` | recordType=3, recordState(0/1), current, size, badge, name, startTime, endTime, licensePlate |
| 扫码取详情（跳详情用） | GET | platform `/articlesrelease/detail/{id}` | articlesType=3 生活区 / 4 办公区 决定跳转目标 |
| 微信 JS-SDK 签名 | GET | app `/wechat/sign` | url（扫一扫配置，现注释未启用） |

---

## 3. 物品放行（生活区）审批详情 `/xuchang/backLog/goodReleaseLive/detail`

旧文件：`backLog/good-release-live/detail.vue`（组件 `process` 审批流程时间线、`page3-bottom`）

### 用途
审批人查看申请详情并执行同意/拒绝；保安节点执行确认放行/拒绝放行（可要求拍照）。

路由 query：`id`（单据）、`sort`（1 室友 / 2 宿管 / 3 保安）、`curTabIndex`（0 待审批=可操作 / 1 已审批=只读）、`isScan`（是否扫码进入）。

### UI 元素
- 申请信息卡：
  - 携带人 `carrier` + 人脸照片 `facePic`（无则占位头像）
  - 物品类型 `articlesTypeName`、物品名称 `articlesDesc`、房间信息 `dormitoryName + roomName`、离厂时间 `plannedDepartureTime`、车牌号 `licensePlate || '无'`、备注信息 `remarks || '无'`
  - 物品照片：`oneImg / twoImg / threeImg` 最多 3 张，可点击预览
- 「上传物品照片」表单：**仅当 sort=3 且 `isUploadImg === 0`（园区配置需上传）且 curTabIndex=0 且 status<4** 显示，base64 多图上传。
- 审批流程时间线（process 组件）：
  - 提交节点（recordNode=0）：`staffInfos[0].staffName` - `resultDesc` + `createDate`
  - 审批节点：节点名 `statusName`，节点内每人 `staffName` - `resultDesc`（按 result 0/1/2·3/4 蓝/绿/红/灰着色）、`意见: remark`（有则显示）、时间 `recordDate || createDate`
- 「审批意见（选填）」输入框：仅 curTabIndex=0 且 status<4 显示。
- 底部操作按钮：
  - sort=1/2 且 curTabIndex=0：「拒 绝」(status=3) /「通 过」(status=2)
  - sort=3 且 curTabIndex=0 且 status<4：「拒绝放行」(status=5) /「确认放行」(status=4)
  - 其余情况（已审批 Tab、status≥4）只读无按钮。

### 校验与流转
- 保安提交时若 `isUploadImg === 0` 且未传图 → toast「请至少上传一张照片」并阻断。
- 室友/宿管审批成功 → 跳列表页 `?curTabIndex=1`。
- 保安审批成功 → 扫码进入(isScan)跳 `/xuchang/home`，否则跳列表 `?curTabIndex=1`。
- 详情加载失败 → toast 后跳 `/xuchang/home`。

### 接口
| 用途 | Method | Path | 关键参数 |
|---|---|---|---|
| 详情 | GET | platform `/articlesrelease/detail/{id}` | — |
| 室友/宿管审批 | GET | platform `/articlesrelease/status/update` | approveBadge, id, status(2/3), remark |
| 保安放行 | POST | platform `/articlesrelease/status/security/update` | guardOneImg/guardTwoImg/guardThreeImg, id, parkId, status(4/5), badge, remark |

---

## 4. 物品放行-审批结果 `/xuchang/backLog/goodReleaseLive/result`

旧文件：`backLog/good-release-live/result.vue`

### 用途
审批完成后的结果回执页。

> 事实说明：旧页面是一个未接接口的静态占位实现（写死「张三｜研发部」「test」等假数据，图片框为空），无任何脚本逻辑与跳转。原型按其字段结构呈现并用可信 mock 数据替换占位文案。

### UI 元素
- 顶部结果图位 + 提示文案「您已通过物品放行审批」。
- 人员卡：姓名 ｜ 部门 + 右侧状态标签「不返厂」。
- 信息卡 1：放行事项。
- 信息卡 2：放行时间、OA节点。

### 接口
无（旧页未调用接口）。

---

## 5. 二维码放行条 `/xuchang/backLog/code`（免登录）

旧文件：`backLog/good-release-live/code.vue`

### 用途
凭链接（query `id`）免登录查看物品放行条：放行码、单据信息、流转记录；供门岗扫码/核验。

### UI 元素（自上而下）
- 放行码区（按状态互斥展示）：
  - `expire=true`：过期占位图 +「放行码已过期」
  - status=1：审批中占位图 +「放行码仍在审批中，请稍后」
  - status=2：二维码图片（`data:image/jpg;base64,` + `qrCodePic`）+「扫描放行码以识别备案物进行放行」
  - status=3：失败占位图 +「审核失败」
  - status=4：出厂占位图 +「已出厂」
  - status=5：失败占位图 +「拒绝放行」
- 携带人卡：`carrier` + 人脸照 `facePic`（有则显示）。
- 单据信息卡：园区名 `parkName`、物品类型标签 `articlesTypeName`、宿舍位置 `dormitoryName-floorName层-roomName号房-bedName床`（有宿舍才显示）、预计离厂日期 `plannedDepartureTime`、物品描述 `remarks`、物品照片（最多 3 张）。
- 状态卡：状态 `statusName || '-'`；拒绝原因 `remark`（仅 status=5）；放行人员 `securityStaff || '-'`（status=4/5）；离厂时间 `departureTime || '-'`（仅 status=4）。
- 流转记录卡（`approvalProcess` 非空时）：与详情页时间线同构（提交节点 / 审批节点、result 着色）+ 右上角「电话联系」`tel:` 链接（`detailData.phone`）。
- 页底品牌 logo。

### 接口
| 用途 | Method | Path |
|---|---|---|
| 放行条详情 | GET | platform `/articlesrelease/detail/{id}` |

---

## 不确定点

1. **result.vue 为静态占位页**：未接接口、数据写死，且全仓未发现指向该路由的跳转；真实业务中可能已废弃。原型按字段结构忠实重现，数据用 mock。
2. 列表页「按车牌号搜索」与「微信扫一扫」入口在旧代码中被注释（逻辑保留）：原型在搜索弹层中保留车牌搜索为说明性次入口，扫一扫不做主入口，仅在文件头注释中记录。
3. 列表接口 `approve/list/new/page` 返回字段（approveName/approveNodeDesc/approveDesc/approveState/sort 等）语义由模板用法反推，无后端文档佐证。
4. `isUploadImg`（0 需上传 / 1 不需）为园区级配置，取自详情接口返回。
5. code 页旧代码对响应做了双层 `res.data.data` 解包，与详情页 `res.data` 不一致，疑为历史接口包装差异，原型不体现。
