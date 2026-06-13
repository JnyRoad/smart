# 第 3 批模块设计：物品放行域（good-release-live / good-release-work / return-factory，16 页）

> 事实来源：docs/prototype/specs/{good-release-live-return-factory,good-release-work-a,good-release-work-b}.md（旧仓库净室分析）；接口与字段以旧版 services/goodRreleaseLive.js、goodRreleaseOffice.js、returnFactory.js 及对应 vue 页面为准。实现时每页 E2E mock 形状必须再对照旧 Vue 源码（批2 教训）。

## 1. 分支与交付顺序

| 分支 | 内容 |
|---|---|
| 1 `feat/good-release-live` | 生活区 3 页 + 域公共层（api/dicts/状态规则） |
| 2 `feat/return-factory` | 返厂确认 2 页（复用 live 的详情接口与时间线） |
| 3 `feat/good-release-work` | 办公区 11 页（主表单 + 列表/详情 + 人员/物品 增删改 4 页 + 只读详情 4 页） |

每分支独立：TDD → 全绿（check/test/e2e/build）→ 子 agent 评审 → 修复 → 复评 → PR → 合并。home 宫格映射已预留 `/releaseGoods→/good-release/live`、`/articlesrelease→/good-release/work`、`/returnFactory→/return-factory`，每分支补对应死链回归。

## 2. 路由表（16 页）

- 生活区：`/good-release/live`（发起）、`/good-release/live/list`、`/good-release/live/detail?id=`
- 返厂：`/return-factory`（双 Tab：待确认/我确认，`?tab=confirmed` 直达第二 Tab）、`/return-factory/detail?id=`
- 办公区：`/good-release/work`（主表单）、`/good-release/work/list`、`/good-release/work/detail?id=`
  - 申请编辑链：`/good-release/work/persons`（人员列表）、`/good-release/work/persons/edit`（新增/编辑，`?index=` 进入编辑）、`/good-release/work/goods`、`/good-release/work/goods/edit`
  - 详情只读链：`/good-release/work/detail/persons`、`/good-release/work/detail/persons/item?i=`、`/good-release/work/detail/goods`、`/good-release/work/detail/goods/item?i=`

## 3. 域公共层 `src/features/good-release/`

- `api.ts`：
  - GET `app:/appdormitory/roomList/{badge}`（双层 data 信封同 dorm-exit；live 选项已对照旧 index.vue:151-152 核实：value=`${dormitoryId}/${floorId}/${roomId}/${id}` 四段、label=`${dormitoryName}/${floorName}层/${roomName}号房/${bedNumber}床`——床位 id 字段名是 `id`。与退宿两段拼装不同，单独建 `getLiveRooms` 类型含 floorId/floorName/bedNumber/id）
  - POST `platform:/articlesrelease/living/save`；GET `platform:/articlesrelease/page`（badge、type=3、current、size=10）
  - GET `platform:/articlesrelease/detail/{id}`（生活区/办公区/返厂详情共用）
  - POST `platform:/articlesrelease/office/save`；GET `platform:/articlesrelease/office/page`（badge、type=5、current、size=10）
  - GET `platform:/articlesrelease/oa/staff/info/{badge}`（工号查人，返回 name、id）
  - GET `platform:/articlesrelease/back/page`（approvalStatus 0/1、分页、可附 badge/name/startTime/endTime/licensePlate）；POST `platform:/articlesrelease/back/confirm/{releaseId}`
- `dicts.ts`（纯数据 + 单测）：fxqc 厂内0/厂外1；sffc 是0/否1；fxdd=dddd 共用 12 项（A栋0…其它10、市场开发部/生活区11）；sqrjb 5 项；fxsx 9 项（人员放行0、出差人员放行7、非保密1、保密4、电脑6、固资5、空车3、自动化8、废品出售10）；wpfxlb 3 项；ysfs 人工0/货车1/叉车2/三轮3。`isPersonRelease(fxsx) = fxsx===0||fxsx===7`。
- `release-status.ts`（纯函数 + 单测）：详情放行码三态（expire&&status<4→过期；status===4→已出厂；status===2&&qrCodePic&&!expire→出码；否则不显示）；列表状态配色（status 2 绿/3 红/其余默认）；放行信息块显示条件 status===4||status===5；返厂按钮条件 status===4 && !backTime。
- `work-draft.ts`：办公区跨页草稿 Zustand persist（key `goods-work-draft`，对齐访客模块方案 A 先例），结构 `{applyMain 表单字段, persons: WorkPerson[], goods: WorkGood[]}`；动作 set/addPerson/updatePerson/removePerson/addGood/updateGood/removeGood/clearAll；提交成功 clearAll。
- 详情只读链数据传递：**sessionStorage 快照** key `good-release-detail-items`（`{persons, goods}`，详情页写入，子页按 `?i=` 读单条）——旧版用超长 query JSON，会话级快照更干净且刷新可活；快照缺失（直接刷子页）→ 返回 `/good-release/work` 域内安全页，不崩。

## 4. good-release-live（3 页）

### `/good-release/live`（发起）
- SegmentTabs(submit)。进入按工号拉房间列表；查不到（空/失败）→ toast「没有查询到您的房间信息，不能进行物品放行（生活区）申请」，申请时同样拦截。
- 表单：物品类型 picker（唯一选项「宿舍生活物品」value=3 默认选中）；物品名称（必填）；携带人（只读=当前用户姓名）；房间 picker（value/label 拼装见 §3，拆装为纯函数+单测）；预计离厂日期（必填）；车牌号 PlateInput（选填）；备注（选填）；物品照片 ImageListUpload base64 模式最多 3 张（**必填**——空图拦截不可提交，对齐旧版 `required field="imgs"`；删除前 Dialog 确认「是否移除此图片」，组件加 `confirmRemove` 可选 prop，默认关闭不影响报修页）。
- 提交体（已对照旧 index.vue:97-116 核实，平铺无嵌套）：articlesDesc、articlesType=3、badge、parkId、dormitoryId/floorId/roomId/bedId（房间 value 按 `/` 四段拆）、carrier、licensePlate、name（=当前用户姓名）、plannedDepartureTime、remarks、status=1、oneImg/twoImg/threeImg（base64 按序铺开，不足为空串 `''`）。成功条件 code===0 && data，跳 list；失败 toast message。

### `/good-release/live/list`
- SegmentTabs(list) + useListPager（GET /articlesrelease/page，badge+type=3+size=10）。卡片：「{name}提交的物品放行」+ 状态标签 oaNode（status 2 绿/3 红）；行：物品类型 articlesTypeName、物品名称 articlesDesc、携带人 carrier、申请时间 createTime。点卡片 → detail?id=。空态/下拉刷新/分页。

### `/good-release/live/detail?id=`
- 放行码三态区（release-status 规则；qrCodePic 为 base64 图）+「【温馨提示】在门卫处出示放行码」。
- 申请信息：携带人大字 + 人脸照（无则占位）；物品类型/名称、房间信息（宿舍名+房间名）、离厂时间、车牌号（空「无」）、备注（空「无」）、物品照片≤3 张（点击 antd-mobile ImageViewer 预览）。
- ApprovalTimeline；放行信息块（status 4/5：statusName、securityStaff、departureTime、remark）。

## 5. return-factory（2 页）

### `/return-factory`
- 双 Tab 待确认（approvalStatus=0）/我确认（=1），`?tab=confirmed` 直达——**新路由约定**：旧版 `curTabIndex=1` 不复用（站内入口都是新链接，无旧链接兼容需求），home 宫格映射只指向无参根路径，确认成功跳转由新详情页发起。各自 useListPager（GET /articlesrelease/back/page）。
- 卡片：「{name}提交的放行条」+ backStatus 标签；行：申请部门 deptName、放行事项 releaseItemDesc、申请时间 createTime、OA节点 oaNode（空「-」）。点卡片 → detail?id=。
- 底部「搜索」按钮（列表非空时显示）→ 员工信息搜索弹层（工号/携带人姓名/申请开始/结束时间，确定按条件重查当前 Tab）。**车牌搜索与扫一扫旧版已注释停用，不实现**（YAGNI，规格仅记录）。

### `/return-factory/detail?id=`
- GET /articlesrelease/detail/{id}（共用）。头部：姓名 | deptName + 标签（applyMain.sffcDesc==='是'→「返厂」否则「不返厂」）。
- 信息字段（applyMain）：fxqcDesc、fxddDesc、ddddDesc、fxsxDesc、wpfxlbDesc、sqrjbDesc、附件 fjsc（图，空「-」）。
- 条件块：isPersonRelease(fxsx) → 「放行人员」标签云（personDetailList，显示 xm）点击 → `/good-release/work/detail/persons`；否则「放行物品」标签云（thingDetailList，「{wpbm}-{wpmc}{wpsl}{wpdw}」）→ `/good-release/work/detail/goods`（经 sessionStorage 快照传递）。**依赖 work 分支的只读详情页**——故 work 只读 4 页若未先行，本分支先以快照+占位链接交付、work 分支补页（分支顺序已把 return-factory 放 work 前，实现时此两页的跳转在 work 合并前 e2e 仅断言快照写入与 URL，不断言落地页内容）。
- ApprovalTimeline；底部「确认返厂」按钮（status===4 && !backTime）→ POST back/confirm/{id} → 成功跳 `/return-factory?tab=confirmed`；失败 toast。

## 6. good-release-work（11 页）

### `/good-release/work`（主表单）
- SegmentTabs(submit)。草稿来自 work-draft store（进入回填，任何跳出前已实时写 store——Zustand 每次 set 即持久化，无需「跳出前保存」时机）。
- 表单组一（全必填）：fxqc/sffc/fxdd/dddd/sqrjb 五个 picker + 出发备注 fxddxq、到达备注 ddddxq 文本。
- 表单组二：fxsx picker（联动分支）、wpfxlb picker、附件 fjsc 单图 base64（选填，**ImageListUpload max=1**——FaceUpload 产物是 photoId 不适用）。
- 条件入口行：isPersonRelease → 「放行人员」人员姓名标签 +「请添加」空态 → `/good-release/work/persons`；否则「放行物品」标签（「{wpbm}-{wpmc}{wpsl}{wpdw}」）→ `/good-release/work/goods`。
- 提交（已对照旧 index.vue:213-272 核实）：顶层 `{applyMain: {fxqc,sffc,fxdd,fxddxq,dddd,ddddxq,sqrjb,fxsx,wpfxlb,fjsc}, badge, parkId, status:1, personList, thingList}` —— **applyMain 为嵌套对象**；personList 仅 isPersonRelease 时组装（gh、xm、name、lcsy、lcrq=lcDate 日期段、lcsj=lcDate 时间段），否则组 thingList（wpbm、wpmc、wpdw、wpsl、jsdw、fxrq 取日期段、bz、ysfs、xm、name、cph）；两数组始终都在体内（另一分支为空数组）。成功（code===0 && data）clearAll → `/good-release/work/list`；失败 toast message。组装为纯函数 `buildWorkSubmitBody` + 单测。
- 切换 fxsx 人员↔物品分支：不清空另一分支草稿（旧版同——提交时只按当前分支取数）。

### `/good-release/work/list`
- SegmentTabs(list) + useListPager（office/page，badge+type=5+size=10）。卡片：「{name}提交的放行条」+ oaNode 标签；行：申请部门 compName、放行事项 releaseItemDesc、申请时间 createTime、确认状态 backStatus。点卡片 → detail?id=。

### `/good-release/work/detail?id=`
- 同生活区详情骨架：放行码三态区 → 申请人行（name | deptName + 返厂/不返厂标签）→ 申请信息（fxqcDesc/fxddDesc/ddddDesc/fxsxDesc/wpfxlbDesc/sqrjbDesc/fjsc 预览）→ 条件标签云（人员 xm / 物品拼串，点击写 sessionStorage 快照 → 只读链）→ ApprovalTimeline → 放行信息区（status 4/5）。

### 申请编辑链 4 页
- `/good-release/work/persons`：「已添加放行人员（N人）」+ 卡片（{name} {gh}，编辑/删除图标；行：lcsy、lcDate）；空态插画文案；底部「新增人员」/「确 定」（回主表单）。删除直接移除（旧版无确认）。
- `/good-release/work/persons/edit`：工号/姓名只读入口（点击弹 search-by-staff 弹窗：工号输入+确定 → GET oa/staff/info/{badge} 回填 name 与 xm=id；失败 toast）；lcsy 必填；lcDate 日期+时间选择必填。底部「确认添加/修改放行人员」→ 写 store 回 persons 列表页。
- `/good-release/work/goods`：同构（「已添加放行物品（N项）」，卡片标题 wpmc+wpbm，行 wpdw、fxrq）。
- `/good-release/work/goods/edit`：物品信息（wpbm/wpmc/wpdw/wpsl 必填）+ 放行信息（jsdw 必填、fxrq 日期必填、bz 选填、ysfs picker 必填）+ 经手人（search-by-staff 选人 name/xm、cph PlateInput 选填）。
- search-by-staff 做成 work 域内组件 `staff-search-popup.tsx`（live/return 不用）。

### 详情只读链 4 页
- `/good-release/work/detail/persons`：「放行人员（N人）」卡片（姓名+工号，行 lcsy、lcrq+lcsj 拼接）→ item?i=。
- `/good-release/work/detail/persons/item?i=`：只读行 gh/xm/lcsy/lcrq+lcsj。
- `/good-release/work/detail/goods`：「放行物品（N项）」卡片（wpmc+wpbm，行 wpdw、fxrq）→ item?i=。
- `/good-release/work/detail/goods/item?i=`：只读 10 行（wpbm/wpmc/wpdw/wpsl/jsdw/fxrq/bz/ysfsDesc 字典映射/xm/cph）。
- 快照缺失或 i 越界 → 跳回 `/good-release/work`（不崩白屏）。

## 7. 测试策略

- 单测：dicts 映射与 isPersonRelease；release-status 三态/配色/按钮条件；live 房间选项 value 拆装；work 提交体组装（personList/thingList 分支 + lcDate 拆分）；work-draft store 增删改。
- E2E（mock 形状摘自旧 Vue 源码）：
  - live：房间为空拦截 → **空图拦截（照片必填）** → 正常提交体断言（含 oneImg/twoImg/threeImg 与拆分的房间四 id）→ 列表状态配色 → 详情三态（过期/已出厂/出码）+ 放行信息区。
  - return-factory（分支 2 断言边界：work 只读页未合并，**详情人员/物品分支只断言 sessionStorage 快照写入 + 目标 URL，不断言落地页内容**；work 分支合并后在 work 的 e2e 里补只读链完整断言）：双 Tab 切换与 `?tab=confirmed` 直达 → 搜索条件重查断言 → 确认返厂（status=4 无 backTime 显示按钮，确认后跳我确认 Tab；status≠4 无按钮）。
  - work：主表单草稿跨页持久（加人员→回填→刷新仍在）→ fxsx 分支切换入口变化 → 人员/物品增删改 → 提交体断言（personList 分支与 thingList 分支各一条）→ 详情只读链快照传递 → 回退三件套：**快照缺失 / `i` 越界 / 快照损坏（非 JSON）** 均安全跳回 `/good-release/work`。
  - 死链回归：home 宫格 `/good-release/live`、`/good-release/work`、`/return-factory` 三入口。
- 不做：扫一扫、车牌搜索（旧版停用）；合肥变体。

## 8. 防御与边界决策

1. 详情接口三方共用（live/work/return），Envelope 类型一份，放 api.ts 共用段。
2. status=5 含义旧码未注明（推测异常终态），仅按「与 4 同样显示放行信息区」处理，不臆造文案。
3. 添加链 xm=人员 id、详情链 xm=姓名（新旧语义不一致，旧版事实）——类型上分开 `WorkPerson.xm: string|number`（提交用 id）与详情 `PersonDetail.xm: string`（展示），注释记录原因。
4. 图片链路（已核旧码 form-upload-image-base64.vue:69-70）：旧版照片经拍照组件上传 `checkFace` 后取 `resultData.base64` 存入表单，最终随 save 提交字符串；新版沿用 dorm-repairs 先例——**本地 FileReader 直出 base64**，不过 checkFace（后端只收字符串，行为等价；若真机联调发现后端依赖 checkFace 压缩/尺寸，列入集成清单回补，与 dorm-repairs 一并处理）。
5. work 主表单字段较多，校验失败 toast 第一条缺失项（对齐旧版逐项校验提示文案，实现时摘自旧码）。
6. **已接受的「严于旧版」偏差**（修旧版的洞，非回归）：① 主表单当前分支列表为空时拦截提交（toast「请添加放行人员/物品」）——旧版可提交空数组；② 添加人员页选人必填（旧版工号/姓名可空提交）；③ 详情只读单条页缺 `i` 参数按非法处理回退（旧版必带 itemObj，无此入口）。
