# 功能规格：退宿申请 / 宿舍报修（共 6 页）

> 来源：旧仓库 `smart-h5/src/views-mobile/pages/dorm-exit/`、`dorm-repairs/`（只读提取功能事实）。
> 路由前缀 `/xuchang`，旧路由 meta 标题分别为：退宿申请 / 退宿申请列表 / 退宿申请详情 / 宿舍报修 / 宿舍报修列表 / 宿舍报修详情。

## 公共约定

- 两个模块的「申请页 / 列表页」顶部都有同一个双段切换条（旧组件 `page3-tab`）：「发起提交」｜「查看数据」，点击在申请页与列表页之间跳转。
- 列表页支持下拉刷新与上拉分页加载（每页 10 条），空数据时显示空态（旧 `tce-empty`）。
- 详情页都有「审批流程」时间线（旧 `components/process/index.vue`）：
  - 节点 0 为提交节点：`姓名 - 提交申请` + 提交时间。
  - 审批节点：节点名称（statusName）+ 每位审批人 `姓名 - 结果`；result 含义：0 待审批（蓝）、1 通过（绿）、2 拒绝 / 3 关闭（橙红）、4 等待（灰）；有审批意见时显示「意见: xxx」，下方显示审批时间。

---

## 1. 退宿申请（发起页） `/xuchang/dormExit`

- **用途**：员工选择名下宿舍房间，提交退宿申请，进入审批流。
- **UI 元素**：
  - 顶部切换条：发起提交（当前）/ 查看数据。
  - 表单：
    - 房间信息（必填，picker）：选项来自接口按工号查询的「宿舍名/房间号房」；可多次选择，已选房间以可删除列表展示（每行房间名 + 删除图标）；重复选择去重。
    - 退宿原因（必填，picker）：离职(3)、自离(5)、外宿(2)。
    - 预计离开日期（必填，时间选择器；提交时拼 `:00` 秒）。
    - 备注（textarea，非必填）。
    - 上传物品照片（图片上传，非必填，详情页最多展示 3 张）。
  - 底部固定主按钮「申请」。
- **校验与交互**：表单整体校验；未选任何房间时 toast「请选择退宿房间！」；按工号查不到房间时 toast「没有查询到您的房间信息，不能进行退宿申请申请」（原文如此）；提交成功 toast「申请成功」并跳转列表页；失败 toast 接口 message。
- **提交字段**：dormitoryIds[]、roomIds[]（由所选房间 value `dormitoryId/roomId` 拆分）、quitReason、applyLeaveTime、remark、imgs[]（photoId）、parkId、badge（工号）、name（姓名）。
- **跳转**：切换条「查看数据」→ `/xuchang/dormExit/list`；旧代码切换条「发起提交」回调跳到 `/xuchang/dormRepairs`（疑似旧代码 bug，见“不确定点”）。
- **接口**：
  - GET `app:/appdormitory/roomList/{staffBadge}`（查询名下房间，services/goodRreleaseLive.js `getRoomDetail`）
  - POST `platform:/dor/quit/apply`（提交申请）

## 2. 退宿申请列表 `/xuchang/dormExit/list`

- **用途**：查看本人提交的退宿申请记录及审批状态。
- **UI 元素**：卡片列表，每卡：
  - 标题「{姓名}提交的退宿申请」+ 右侧状态文本（statusDesc；status=2 绿色、status=3 红色，其余灰色）。
  - 字段行：房间信息（多行，dorDetailStr 数组）、退宿原因、预计离开时间、申请时间。
- **交互**：下拉刷新、上拉加载（current/size=10/badge）；点击卡片 → 详情页（带 id）；空列表显示空态。
- **跳转**：切换条「发起提交」→ `/xuchang/dormExit`；卡片 → `/xuchang/dormExit/detail?id=xxx`。
- **接口**：GET `platform:/dor/quit/page`（current、size、badge）。

## 3. 退宿申请详情 `/xuchang/dormExit/detail?id=`

- **用途**：查看单条退宿申请详情、放行二维码与审批流程。
- **页面状态（按 status）**：
  - status=2（审批通过）：顶部展示放行二维码（接口返 base64 JPG），下方提示「在门卫处出示放行码」；门卫微信扫码核验（扫码核验接口为 `/dor/quit/list/check/{id}`，由门卫端 backLog 页使用）。
  - status=4（已出厂）：顶部展示「已出厂」占位图 + 文案「已出厂」，并显示提示「已同意出厂」。
  - status=5：提示「已拒绝出厂」。
  - 其他状态（如待审批/已拒绝）不显示码区，仅信息 + 审批流程。
- **UI 元素**：
  - 信息卡：姓名 + 人脸照片（faceId 拼图片服务 URL，无照片用默认头像）；房间信息（多行）；退宿原因；预计离开时间；备注；照片（最多 3 张，可点击预览）。
  - 审批流程时间线（见公共约定）。
- **接口**：GET `platform:/dor/quit/detail/{id}`；（关联：GET `platform:/dor/quit/list/check/{id}` 扫码核验，非本页调用）。

## 4. 宿舍报修（发起页） `/xuchang/dormRepairs`

- **用途**：员工提交园区/宿舍维修工单。
- **UI 元素**：
  - 顶部切换条：发起提交（当前）/ 查看数据。
  - 表单：
    - 维修区域（必填，picker）：选项来自接口枚举（desc/code），默认兜底：宿舍(1)、办公室(2)、车间(3)、园区周边(4)。
    - 所在楼栋（必填，picker）：随维修区域联动——宿舍：老工厂1/2/3号宿舍、新工厂宿舍楼；办公室：餐厅三楼、北门岗、东门岗、辅房；车间：一楼、二楼、三楼；园区周边：园区周边。切换区域后默认选中首项。
    - 维修类别（必填，picker）：灯(1)、插座(2)、水龙头(3)、水管(4)、门窗(5)、锁(6)、空调(7)、其他(8)、床(9)、柜子(10)、玻璃(11)、洗手台(12)、桌椅(13)、地漏(14)。
    - 所在房间（必填，文本输入）。
    - 故障描述（必填，textarea）。
    - 上传物品照片（base64 图片上传，非必填）。
  - 底部固定主按钮「申请」。
- **提交字段**：rangeType、repairType、dormitoryName、roomName、faultDesc、faultImgs（base64）、parkId。提交成功直接跳列表页（无成功 toast）；失败 toast message。
- **接口**：
  - GET `app:/dormitory/repair/enum/range`（维修区域枚举）
  - POST `platform:/dormitory/repair/add`（提交工单）

## 5. 宿舍报修列表 `/xuchang/dormRepairs/list`

- **用途**：查看本人提交的报修工单及处理状态。
- **UI 元素**：卡片列表，每卡：
  - 标题「{姓名}提交的园区报修」+ 状态文本（statusDesc；status=3 绿色、status=4/6 红色，其余灰色）。
  - 字段行：维修区域、维修类别、所在楼栋、故障描述（单行省略）、申请时间。
- **交互**：下拉刷新、上拉加载（current/size=10，不带 badge）；点卡片 → 详情；空列表空态。
- **跳转**：切换条「发起提交」→ `/xuchang/dormRepairs`；卡片 → `/xuchang/dormRepairs/detail?id=xxx`。
- **接口**：GET `platform:/dormitory/repair/query/record`（current、size）。

## 6. 宿舍报修详情 `/xuchang/dormRepairs/detail?id=`

- **用途**：查看工单详情、审批流程与维修结果回复。
- **UI 元素**：
  - 状态卡：状态（statusDesc）、报修时间（createTime）。
  - 报修信息卡：姓名、工号（staffBadge）、BU（compName）、部门（depName）、维修区域、维修类别、维修位置（`楼栋#房间`）、所在园区（parkName）、故障描述、物品照片（最多 3 张，可预览）。
  - 审批流程时间线（approvalProcess，结构同退宿）。
  - 维修结果区（repairReplyList 非 null 才显示，可多条）：每条含维修结果（replyStatusDesc，「维修成功」绿 /「无法维修」红）、维修时间（replyTime）、回复人（replyName）、回复内容（replyDesc）。
- **接口**：GET `platform:/dormitory/repair/query/detail/{id}`。

---

## 接口汇总

| 模块 | Method | Path（module:url） | 用途 |
|---|---|---|---|
| 退宿 | GET | app:`/appdormitory/roomList/{staffBadge}` | 查询本人宿舍房间 |
| 退宿 | POST | platform:`/dor/quit/apply` | 提交退宿申请 |
| 退宿 | GET | platform:`/dor/quit/page` | 退宿申请分页列表 |
| 退宿 | GET | platform:`/dor/quit/detail/{id}` | 退宿申请详情 |
| 退宿 | GET | platform:`/dor/quit/list/check/{id}` | 门卫扫码核验（backLog 页使用，非本 6 页） |
| 报修 | GET | app:`/dormitory/repair/enum/range` | 维修区域枚举 |
| 报修 | POST | platform:`/dormitory/repair/add` | 提交报修 |
| 报修 | GET | platform:`/dormitory/repair/query/record` | 报修分页列表 |
| 报修 | GET | platform:`/dormitory/repair/query/detail/{id}` | 报修详情 |

## 不确定点

1. **退宿发起页切换条 bug**：旧 `dorm-exit/index.vue` 的「发起提交」回调跳 `/xuchang/dormRepairs`（报修页），疑为复制粘贴 bug；原型按「停留在退宿发起页」处理并在注释中标注。
2. **状态枚举不完整**：status 数值→文案映射由后端 statusDesc 决定，旧前端只揭示了部分（退宿 2=通过出放行码、3=拒绝、4=已出厂、5=拒绝出厂；报修 3=绿、4/6=红）。原型 mock 文案（待审批/已通过/已拒绝/已出厂、待处理/维修完成/无法维修等）为合理推断。
3. **上传差异**：退宿用 photoId 上传（`upload-image`），报修用 base64（`upload-image-base64`），对重写实现有影响，但对原型仅呈现为相同的图片上传控件。
4. 退宿原因枚举在旧代码中复用了变量名 `REPAIRTYPEOPTION`（值：离职3/自离5/外宿2），数值不连续，按事实保留。
