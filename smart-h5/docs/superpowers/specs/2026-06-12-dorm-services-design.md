# 第 2 批模块设计：宿舍域三件套（check-in / dorm-repairs / dorm-exit，9 页）

日期：2026-06-12
状态：设计已经旅途批准（含三分支顺序）；按旅途授权，spec 经 Codex 一致性确认后即开工（不再等用户审阅）。
功能事实来源：docs/prototype/specs/dorm-lock-checkin.md §5-7、dorm-exit-repairs.md（旧仓库只读净室分析）。
关键实锤（2026-06-12 核对旧代码）：退宿「上传物品照片」组件（tce-form upload-image → services/upload.postImageSave）实际走 `app:/wechat/visit/checkFace` 换 photoId；报修 faultImgs 为 base64 直接进提交体（无独立上传接口）。

## 0. 范围与分支

| 顺序 | 分支 | 内容 |
|---|---|---|
| 1 | `feat/dorm-repairs` | 报修 3 页 + 本批公共组件（segment-tabs / approval-timeline / image-list-upload）+ README 补记（含上批欠账） |
| 2 | `feat/dorm-exit` | 退宿 3 页（含放行二维码分态） |
| 3 | `feat/check-in` | 宿舍申请 3 页（选房联动 + 实名信息 + 动态码解密展示） |

每分支：check/test/e2e 全绿 → 子 agent 评审修复至无明确问题 → PR → 合并。全部登录态（useRequireAuth）；badge/姓名取 `GET app:/employee/baseinfo`。

## 1. 本批公共组件

- `components/segment-tabs.tsx`：「发起提交｜查看数据」双段切换条。props `{ active: 'submit' | 'list', listHref, submitHref }`；修正旧版退宿页「发起提交」误跳报修页的 bug（按原型注释：停留本模块）。
- `components/approval-timeline.tsx`：审批流程时间线。props `{ submitter?: { name, time }, nodes: ProcessNode[] }`；ProcessNode `{ statusName, approvers: { name, result, opinion?, time? }[] }`；result 配色：0 待审批蓝 / 1 通过绿 / 2 拒绝、3 关闭橙红 / 4 等待灰；意见行「意见: xxx」。节点 0 为提交节点「{姓名} - 提交申请」+ 提交时间。
- `components/image-list-upload.tsx`：多图上传（max 张数、可删、点击 ImageViewer 预览）。两种输出：
  - `mode='photoId'`：每张走 checkFace（plain，复用 FaceUpload 内部链路逻辑）→ photoId[]（退宿用）
  - `mode='base64'`：本地 FileReader → base64[]（报修用，提交体直传）

## 2. dorm-repairs（3 页）

### `/dorm-repairs`（发起）
- SegmentTabs(submit)。表单：
  - 维修区域：`GET app:/dormitory/repair/enum/range` 枚举；失败兜底固定 4 项 宿舍(1)/办公室(2)/车间(3)/园区周边(4)
  - 所在楼栋：随区域联动（宿舍：老工厂1/2/3号宿舍、新工厂宿舍楼；办公室：餐厅三楼、北门岗、东门岗、辅房；车间：一楼、二楼、三楼；园区周边：园区周边）；切区域默认选首项。联动表为纯函数 + 单测。
  - 维修类别：固定 14 项枚举 灯(1)插座(2)水龙头(3)水管(4)门窗(5)锁(6)空调(7)其他(8)床(9)柜子(10)玻璃(11)洗手台(12)桌椅(13)地漏(14)
  - 所在房间（文本，必填）、故障描述（textarea，必填）、物品照片（base64 多图，选填）
- 提交 `POST platform:/dormitory/repair/add` `{rangeType, repairType, dormitoryName, roomName, faultDesc, faultImgs[], parkId}`；成功**直接跳列表（无 toast，旧版行为）**；失败 toast message。

### `/dorm-repairs/list`
- SegmentTabs(list)。卡片：「{姓名}提交的园区报修」+ statusDesc（status=3 绿 / 4、6 红 / 其余灰）；字段：维修区域/类别/楼栋/故障描述（单行省略）/申请时间。分页 `GET platform:/dormitory/repair/query/record`（current,size=10，不带 badge）+ 下拉刷新；点卡片 → 详情；空态。

### `/dorm-repairs/detail?id=`
- `GET platform:/dormitory/repair/query/detail/{id}`。状态卡（statusDesc+createTime）；信息卡（姓名/工号/BU compName/部门 depName/区域/类别/位置 `楼栋#房间`/园区/故障描述/照片≤3 可预览）；ApprovalTimeline(approvalProcess)；**维修结果区**（repairReplyList 非空才显示，多条：replyStatusDesc 维修成功绿/无法维修红 + replyTime + replyName + replyDesc）。

## 3. dorm-exit（3 页）

### `/dorm-exit`（发起）
- SegmentTabs(submit)。表单：
  - 房间信息：`GET app:/appdormitory/roomList/{badge}`（**注意响应双层 data：列表在 `res.data.data`**）；选项 `value=\`${dormitoryId}/${roomId}\``、`label=\`${dormitoryName}/${roomName}号房\``（旧 index.vue:140-142 实锤）；picker 多次选择、去重、已选列表可删；接口查不到 toast「没有查询到您的房间信息，不能进行退宿申请申请」（保留原文）
  - 退宿原因：离职(3)/自离(5)/外宿(2)
  - 预计离开日期（DatePicker，提交补 `:00`）、备注（选填）、物品照片（photoId 多图，选填，详情最多展示 3 张）
- 校验：未选房间 toast「请选择退宿房间！」。提交 `POST platform:/dor/quit/apply` `{dormitoryIds[], roomIds[]（value 按 '/' 拆分：[0]=dormitoryId,[1]=roomId）, quitReason, applyLeaveTime+':00', remark, imgs[]（photoId）, parkId, badge, name}`；**成功判断 `code===0 && data`（旧版实锤）** toast「申请成功」跳列表。房间值拆分为纯函数 + 单测。

### `/dorm-exit/list`
- 卡片：「{姓名}提交的退宿申请」+ statusDesc（status=2 绿 / 3 红 / 其余灰）；房间信息多行（dorDetailStr[]）/退宿原因/预计离开时间/申请时间。分页 `GET platform:/dor/quit/page`（current,size=10,badge）。

### `/dorm-exit/detail?id=`
- `GET platform:/dor/quit/detail/{id}`。**status 分态**：
  - 2（审批通过）：放行二维码（base64 JPG `<img>`）+「在门卫处出示放行码」
  - 4：「已出厂」占位 + 「已同意出厂」
  - 5：「已拒绝出厂」
  - 其余：无码区
- 信息卡：姓名 + 人脸照片（**URL = `/platform/image/view/{faceId}`，conf.js:11 实锤**；缺省默认头像占位）、房间多行、原因、预计离开时间、备注、照片≤3 可预览（imgs 直接作 src，旧版同）；ApprovalTimeline。
- 门卫扫码核验接口（/dor/quit/list/check/{id}）属 backLog 批次，不在本批。

## 4. check-in（3 页）

### `/check-in`（发起）
- SegmentTabs(submit)。进入并行拉：楼栋 `POST platform:/dormitory/queryDormitory`（parkId, isAccount=true，条目 `{id, dormitoryName}`）、实名信息 `GET platform:/staff/define/badge`（badge → `{name, sex, nation, certno, birth, homeAddress, validDate, validDateFm}`）；**实名获取失败：隐藏申请按钮，原位红字「获取用户信息失败！」**（实现偏差：旧版为 toast，红字常驻信息更持久，已确认保留）。
- 表单：分配方式（自选房间默认/系统分配按钮组）→ 楼栋 picker → 房间类型 picker（`GET platform:/dormitory/type/by/park-and-dormitory`，条目 `{id, typeName}`，**切楼栋清空房型**；**切房型同时清房间草稿**——旧版只在切楼栋时清，属潜伏 bug，本版修复）→ 自选模式显示 房间号/床位 入口（先校验楼栋房型已选）→ `/check-in/select-room?dormitoryId=&roomType=`（均传 id）。
- 选房回填：sessionStorage 草稿 `check-in-room`（对齐旧版 localStorage roomInfo 模式，会话级更干净），结构 `{floorId, roomId, roomName, bedId, bedNumber}`；另存表单草稿 `check-in-form`（`{dormitoryId, dormitoryName, roomTypeCode, roomTypeDesc}`）——App Router 跳选房页会卸载本页组件态，返回时由草稿恢复楼栋/房型。
- 提交 `POST platform:/dormitory/room/autoallot`：实名字段**显式映射**（`certno` 原样、`birth→birthday`、`homeAddress→address`、`validDate→validDateStart`、`validDateFm→validDateEnd`、补 `signOrg:null`，映射为纯函数 `identityToSubmitFields` + 单测）+ dormitoryId + roomType（均为 id）+ badge + parkId（自选加 floorId/roomId/bedId）；自选未选房间 toast「请选择房间号」；成功清两份草稿跳 `/check-in/detail`。

### `/check-in/select-room`
- 楼层数据 `GET platform:/park/tree/condition` 返回 园区→楼栋→楼层 三层树，**楼层列表取 `data[0].children[0].children`**（纯函数 `floorsFromConditionTree` + 单测），显示 `{label}层`，默认第 1 层；树缺层级 toast「无房间信息！」回 /check-in，接口失败 toast「获取房间信息失败！」回 /check-in。
- 右房间网格 `GET platform:/dormitory/room/search/condition`，条目 `{roomId, roomName, roomSex, freeBedNum, bedTotal}`，每格「X房」+「{男|女} {freeBedNum}/{bedTotal}」（roomSex 0 男 1 女）；查询失败网格内红字展示错误（旧版 toast res.message）。
- 点房间：freeBedNum>0 才拉床位 `POST platform:/dormitory/room/bedDetail/{roomId}`，否则 toast「该房间无可选用的床位！」；床位条目 `{id, bedNumber, staffBadge, delFlag}`，剔除已占用（staffBadge≠null，空串也算占用）或停用（delFlag=1），剔空 toast「无可选用的床位！」；选定写 sessionStorage 草稿回 /check-in。床位过滤为纯函数 + 单测。

### `/check-in/detail`
- SegmentTabs(list)。`GET platform:/dormitory/staff/roomList/{badge}` 分配记录卡：楼栋/房间（roomName+「房间」）/床位（bedNumber+「号床」）+「身份特征检查」框——**特征码嵌套在 `item.lockPwd` 下**（指纹 `lockPwd.fingerprintDesc`、动态码 `lockPwd.dynamicCode===3` 显示「已录入」否则 `lockPwd.dynamicDesc`；code 1/3 成功图标，0/2/4 失败图标）；卡底 `lockPwd.fingerprintCode≠0` 且 `GET /dormitory/staff/get/pwd` 有值时展示 **decryptFromHex 解密后的动态码**（复用批 1 AES 层与防御逻辑）。下拉刷新；空态。

## 5. 测试策略

- 单测：报修楼栋联动表、退宿房间去重/value 拆分、床位过滤规则、时间线 result→配色映射。
- E2E（网络 mock，沿用既有模式）：
  - 报修：区域→楼栋联动断言 → 提交体断言 → 列表状态配色 → 详情含维修结果区。
  - 退宿：房间多选去重/删除 → 提交体断言（dormitoryIds/roomIds 拆分、:00 秒）→ 详情 2/4/5 三态渲染。
  - check-in：实名失败隐藏按钮；楼栋→房型联动清空；选房页楼层切换/满房拦截/床位过滤 → 草稿回填 → 提交体断言（自选含 floorId/roomId/bedId）；detail 动态码解密展示（测试 key 真密文）。
  - 死链闭合回归：主页服务宫格 `/dorm-repairs`、`/check-in`、`/dorm-exit` 三入口落到真实页面。
- SegmentTabs 跳转双向用例（含退宿页修正后的行为）。

## 6. 防御与边界决策

1. 报修枚举接口失败 → 固定兜底 4 项（旧版同）；楼栋联动表前端写死（旧版同）。
2. 退宿详情 faceId 图片 URL 加载失败 → 默认头像占位，不破版。
3. 列表均复用「月份切换竞态守卫」同款模式（分页 token 防过期响应污染）——实现为本批列表页通用 hook `useListPager`（含 InfiniteScroll/PullToRefresh 状态机），三模块共用，消除批 1 中 help/water-elec 的重复样板（顺带把这两页迁移到 hook 作为回归保护）。
4. 上传图片大小：旧版无压缩限制，本批沿用（不加压缩，YAGNI；真机如发现过大再议）。

## 6.5 Codex 确认补充（2026-06-12，结论：方案一致可执行）

- parkId 一律取 `getTenantConfig().parkId`（与 home/visitor 同模式）。
- `useListPager` 列入 feat/dorm-repairs 交付清单，单测须覆盖「过期响应不污染列表」竞态；help/water-elec 迁移由既有 E2E 保护。
- 主页宫格死链回归补「退宿入口」（现有 home E2E 未覆盖）。
- check-in 选房 sessionStorage 草稿需覆盖 返回/刷新/重进 场景测试。

## 7. 不做（YAGNI）

- 门卫扫码核验（backLog 批次）；报修催单/撤销（旧版无）；图片压缩。
