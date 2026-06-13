# 功能规格：物品放行（生活区）+ 返厂确认

> 来源：旧仓库 `smart-h5/src/views-mobile/pages/good-release-live/`、`return-factory/` 及其引用组件、`src/services/goodRreleaseLive.js`、`src/services/returnFactory.js`。本文件只记录功能事实，不含旧代码。

## 通用事实

- 接口模块前缀：`app` / `platform` 为旧工程的 http 模块名（对应不同网关前缀）。
- 审批流程组件（process）：纵向时间线，节点分两类——提交节点（recordNode=0，显示提交人姓名+结果+时间）与审批节点（显示节点名 statusName + 每位审批人姓名、结果、意见 remark、时间）。审批人 result 取值：0 待审批（蓝）、1 通过（绿）、2 拒绝 / 3 关闭（红）、4 等待（灰）。
- page3Tab：页面顶部双 Tab 切换条；page3Bottom：底部固定操作栏。

---

## 1. 物品放行（生活区）— 发起提交 `/xuchang/goodReleaseLive`

**用途**：员工为携带宿舍生活物品离厂发起放行申请。

**UI 元素**
- 顶部双 Tab：「发起提交」（当前）/「查看数据」（跳列表页）。
- 表单组 1：
  - 物品类型（picker，必填，选项只有「宿舍生活物品」value=3，默认选中）
  - 物品名称（输入，必填，"请输入物品名称"）
  - 携带人名称（必填，只读，自动填当前用户姓名）
  - 房间信息（picker，必填，选项来自房间接口，label 形如「X栋/X层/X号房/X床」）
  - 预计离厂日期（日期选择，必填，"请选择预计离厂日期"）
  - 车牌号（车牌键盘输入，非必填）
- 表单组 2：备注（输入，非必填）
- 表单组 3：上传物品照片（必填，最多 3 张；拍照/相册选图 → 本地 base64 预览，可删除（删除前确认弹窗「是否移除此图片」））
- 底部固定栏：「申请」主按钮。

**交互与校验**
- 进入页面即按工号查房间列表；查不到房间 → toast「没有查询到您的房间信息，不能进行物品放行（生活区）申请」，且点申请时同样拦截。
- 提交体字段：articlesDesc、articlesType、badge（工号）、parkId、dormitoryId/floorId/roomId/bedId（从房间选项值拆分）、carrier、licensePlate、name、plannedDepartureTime、remarks、status=1（固定）、oneImg/twoImg/threeImg（base64，最多 3 张）。
- 提交成功 → 跳转列表页；失败 → toast 后端 message。

**接口**
- GET `app /appdormitory/roomList/{staffBadge}` 查询员工宿舍房间
- POST `platform /articlesrelease/living/save` 提交申请
- POST `app /wechat/visit/checkFace` 拍照组件的图片上传校验（base64）

**跳转**：Tab→`/xuchang/goodReleaseLive/list`；提交成功→同上。

---

## 2. 物品放行记录（生活区） `/xuchang/goodReleaseLive/list`

**用途**：查看本人提交的生活区物品放行申请记录（分页）。

**UI 元素**
- 顶部双 Tab：「发起提交」/「查看数据」（当前）。
- 记录卡片列表，每张卡：
  - 标题「{姓名}提交的物品放行」+ 右侧状态标签（显示 oaNode 文案；status=2 绿色、status=3 红色、其他默认色）
  - 字段行：物品类型 articlesTypeName、物品名称 articlesDesc、携带人 carrier、申请时间 createTime
- 空数据时显示空态组件。
- 下拉刷新（"更新成功"提示）/ 上拉分页加载（每页 10 条）。

**交互**：点卡片 → 详情页（带 id）。请求失败 toast「网络错误」。

**接口**：GET `platform /articlesrelease/page`（参数 badge、type=3 固定生活区、current、size=10）。

**跳转**：卡片→`/xuchang/goodReleaseLive/detail?id=`；Tab→`/xuchang/goodReleaseLive`。

---

## 3. 物品放行详情（生活区） `/xuchang/goodReleaseLive/detail?id=`

**用途**：查看单条放行申请详情、放行二维码与审批/放行进度。

**UI 元素（自上而下）**
- 放行码区（三种互斥状态）：
  1. `expire=true 且 status<4`：占位图 + 文案「放行码已过期」
  2. `status=4`：占位图 + 文案「已出厂」
  3. `qrCodePic 存在 且 status=2 且未过期`：显示二维码图片（base64）+「【温馨提示】在门卫处出示放行码」——门卫用微信扫码核验放行
- 申请信息块：携带人姓名（大字）+ 人脸照片（无则占位头像）；物品类型、物品名称、房间信息（宿舍名+房间名）、离厂时间 plannedDepartureTime、车牌号（空显示「无」）、备注信息（空显示「无」）、物品照片（最多 3 张缩略图，可点击预览大图）。
- 审批流程块（process 时间线）。
- 放行信息块（仅 status=4 或 5 时显示）：状态 statusName、放行人员 securityStaff、离场时间 departureTime、备注 remark。

**状态事实**：status 1=待审批、2=已通过（出码）、3=已拒绝、4=已出厂、5=放行相关终态（同样显示放行信息）；expire 标记二维码过期。

**接口**：GET `platform /articlesrelease/detail/{id}`。

**跳转**：无主动跳转（返回上级）。

---

## 4. 返厂确认 `/xuchang/returnFactory`

**用途**：门卫/管理员查看放行条并确认人员/物品返厂；分「待确认的」「我确认的」两个 Tab。

**UI 元素**
- 顶部双 Tab：「待确认的」（approvalStatus=0）/「我确认的」（approvalStatus=1）；入口可经 query `curTabIndex=1` 直达第二 Tab。
- 放行条卡片列表，每张卡：
  - 标题「{姓名}提交的放行条」+ 右侧状态标签 backStatus（如 未返厂/已返厂）
  - 字段行：申请部门 deptName、放行事项 releaseItemDesc、申请时间 createTime、OA节点 oaNode（空显示「-」）
- 空态组件；下拉刷新 / 上拉分页（每页 10）。
- 底部固定栏（仅列表非空时显示）：「搜索」按钮（旧代码中「扫一扫」按钮已注释停用）。
- 搜索弹层 1（按员工信息查找）：工号、携带人姓名、申请开始时间、申请结束时间，确定后按条件重查当前 Tab。
- 搜索弹层 2（车牌号键盘）：输入车牌后按 licensePlate 重查。（两弹层互切入口在旧代码中已注释，事实上仅员工信息搜索可达，车牌搜索逻辑保留。）

**接口**：GET `platform /articlesrelease/back/page`（approvalStatus、current、size=10，可附 badge/name/startTime/endTime/licensePlate 等搜索条件）。

**跳转**：卡片→`/xuchang/returnFactory/detail?id=`。

---

## 5. 返厂确认详情 `/xuchang/returnFactory/detail?id=`

**用途**：查看放行条完整信息并执行「确认返厂」。

**UI 元素**
- 头部块：申请人姓名 | 部门 deptName；标签：applyMain.sffcDesc=「是」→「返厂」，否则「不返厂」。
- 信息字段（均来自 applyMain）：放行去处 fxqcDesc、出发地点 fxddDesc、到达地点 ddddDesc、放行事项 fxsxDesc、放行类别 wpfxlbDesc、放行人级别 sqrjbDesc、附件 fjsc（图片，空显示「-」）。
- 条件块：
  - `fxsx=0 或 7`（人员放行）→「放行人员」标签云（personDetailList，显示姓名 xm），点击 → 人员列表页
  - 否则（物品放行）→「放行物品」标签云（thingDetailList，显示「{物品编码wpbm}-{名称wpmc}{数量wpsl}{单位wpdw}」），点击 → 物品列表页
- 审批流程块（process 时间线）。
- 底部固定栏（仅 `status=4 且 backTime 为空` 时显示）：「确认返厂」主按钮。

**交互**：确认返厂 → loading「请稍后」→ 成功跳回 `/xuchang/returnFactory?curTabIndex=1`（我确认的）；失败 toast。

**接口**
- GET `platform /articlesrelease/detail/{id}`（复用物品放行详情接口）
- POST `platform /articlesrelease/back/confirm/{releaseId}` 确认返厂

**跳转**：人员→`/xuchang/goodReleaseWork/detailPersonList`；物品→`/xuchang/goodReleaseWork/detailGoodsList`；确认成功→`/xuchang/returnFactory?curTabIndex=1`。

---

## 不确定点

1. `status=5` 的确切业务含义旧代码未注明（仅与 4 一起展示「放行信息」块），推测为「逾期放行/异常出厂」一类终态。
2. 拍照组件上传到 `app /wechat/visit/checkFace`，接口名与「访客人脸校验」相同，疑为复用的图片校验接口；最终照片以 base64 直接随 save 提交。
3. 返厂列表的「扫一扫」与「按车牌号搜索」入口在旧代码中被注释停用，原型按现状仅保留「搜索（按员工信息）」，车牌搜索在规格中记录但不作为可达入口。
4. `backStatus` 的全部取值后端未在前端枚举，原型按「未返厂/已返厂」演示。
