# 物品放行（办公区）模块功能规格 — 后半部分（B）

> 来源：旧仓库 `smart-h5/src/views-mobile/pages/good-release-work/`（只提取功能事实，净室重写）。
> 覆盖 6 页：添加物品、添加物品-列表、人员详情、人员详情-列表、物品详情、物品详情-列表。
> 总体差异：`add-*` 是**申请编辑链路**（数据走 localStorage `releaseGoodsInfo`，可增删改）；`detail-*` 是**详情只读链路**（数据由上级详情页通过路由 query 传入，纯展示、无任何编辑入口、无接口调用）。

## 接口清单（模块 service：`services/goodRreleaseOffice.js`）

| method | path | 用途 | 本批使用页面 |
|---|---|---|---|
| POST | `/articlesrelease/office/draft` | 创建当前认证用户的办公区草稿（返回 releaseId） | 进入申请子流程前 |
| GET | `/articlesrelease/{releaseId}/staff/lookup?badge={badge}` | 在受权草稿中按工号查人员（仅返回 name、id） | 添加物品（姓名选择） |
| POST | `/articlesrelease/office/save` | 提交放行申请 | 不在本批（申请主页 index 使用） |
| GET | `/articlesrelease/office/page` | 申请分页列表 | 不在本批（列表页使用） |

运输方式字典（`const.js` transportTypeOption）：人工=0、货车=1、叉车=2、三轮=3。

---

## 1. 添加物品 `/xuchang/goodReleaseWork/addGoods`

- **旧组件**：`add-goods.vue`（含子组件 `components/search-by-staff.vue`）
- **用途**：在放行申请流程中新增或编辑一条放行物品记录（新增/编辑双模式）。

### UI 元素
表单分 3 组：
1. 物品信息：资产编码 `wpbm`（必填，文本）、名称 `wpmc`（必填，文本）、单位 `wpdw`（必填，文本）、数量 `wpsl`（必填，文本）
2. 放行信息：接收单位 `jsdw`（必填，文本）、放行日期 `fxrq`（必填，日期选择器）、备注(原因) `bz`（选填，文本）、运输方式 `ysfs`（必填，选择器：人工/货车/叉车/三轮）
3. 经手人信息：姓名（点击弹出「输入员工工号查询」弹窗，未选时显示「请输入」占位）、车牌号 `cph`（选填，车牌专用输入）

底部固定按钮：新增模式「确认添加放行物品」/ 编辑模式「确认修改放行物品」。

工号查询弹窗（search-by-staff）：头部左「输入员工工号查询」、右「确定」；内容为工号输入框（占位「请输入」）。

### 交互与校验
- 必填校验信息：请输入资产编码 / 请输入名称 / 请输入单位 / 请输入数量 / 请输入接收单位；运输方式必选。
- 弹窗确定后调 `GET /articlesrelease/{releaseId}/staff/lookup?badge={工号}`：`releaseId` 必须来自已创建的服务端草稿；成功（code=0 且有 data）回填姓名 `name` 与人员 id `xm`；失败 toast 后端 message。查询期间全局 loading。
- 提交：校验通过后，新增模式 push 进 localStorage `releaseGoodsInfo` 数组；编辑模式按 `itemIndex` 原位替换；随后跳转添加物品-列表页。

### 页面状态
- 新增模式（空表单）/ 编辑模式（由 query `itemInfo`+`itemIndex`+`isEdit` 回填）。
- 弹窗打开态；人员查询 loading / 失败 toast。

### 跳转
- 提交成功 → `/xuchang/goodReleaseWork/addGoodsList`。

---

## 2. 添加物品-列表 `/xuchang/goodReleaseWork/addGoodsList`

- **旧组件**：`add-goods-list.vue`
- **用途**：展示已暂存（localStorage）的放行物品，可增删改后确认返回申请主页。

### UI 元素
- 顶部提示：「已添加放行物品（N项）」。
- 物品卡片：标题 = 名称 + 资产编码；右侧编辑、删除两个图标按钮；信息行：单位 `wpdw`、放行日期 `fxrq`。
- 空态：插画 + 文案「暂无放行物品信息」「请点击下方按钮添加放行物品」。
- 底部固定双按钮：「新增物品」（次按钮）、「确 定」（主按钮）。

### 交互
- 编辑 → 跳添加物品页并带 `itemInfo`/`itemIndex`/`isEdit=true`。
- 删除 → 直接从数组移除并回写 localStorage（旧版无二次确认）。
- 新增物品 → `/xuchang/goodReleaseWork/addGoods`；确定 → `/xuchang/goodReleaseWork`（申请主页）。

### 页面状态
- 有数据列表态 / 空态。

### 接口
- 无（纯 localStorage）。

---

## 3. 人员详情 `/xuchang/goodReleaseWork/detailPerson`

- **旧组件**：`detail-person.vue`
- **用途**：详情链路中查看单个放行人员的只读信息；数据由列表页通过 query `itemObj`（JSON）传入。

### UI 元素（只读字段行）
工号 `gh`、姓名 `xm`、离厂事由 `lcsy`、离厂日期 = `lcrq` + `lcsj`（日期+时间拼接）。

### 交互 / 状态 / 接口
- 无交互、无编辑入口、单一展示态、无接口。
- 与添加链路差异：添加人员页（A 批）为可编辑表单，本页为同字段的只读视图。

---

## 4. 人员详情-列表 `/xuchang/goodReleaseWork/detailPersonList`

- **旧组件**：`detail-person-list.vue`
- **用途**：详情链路中查看放行人员清单；数据由详情页 query `list`（JSON 数组）传入。

### UI 元素
- 顶部提示：「放行人员（N人）」。
- 人员卡片（整卡可点）：标题 = 姓名 + 工号；信息行：离厂事由 `lcsy`、离厂时间 = `lcrq` + `lcsj`。
- 旧版无空态分支（空数组时页面留白）、无底部按钮、无增删改入口。

### 跳转
- 点卡片 → `/xuchang/goodReleaseWork/detailPerson?itemObj=...`。

### 接口
- 无。

---

## 5. 物品详情 `/xuchang/goodReleaseWork/detailGoods`

- **旧组件**：`detail-goods.vue`
- **用途**：详情链路中查看单条放行物品的只读信息；数据由列表页 query `itemObj` 传入。

### UI 元素（只读字段行，共 10 行）
资产编码 `wpbm`、名称 `wpmc`、单位 `wpdw`、数量 `wpsl`、接收单位 `jsdw`、放行日期 `fxrq`、备注(原因) `bz`、运输方式 `ysfsDesc`（由 `ysfs` 值经字典映射为中文：人工/货车/叉车/三轮）、姓名 `xm`、车牌号 `cph`。

### 交互 / 状态 / 接口
- 无交互、单一展示态、无接口。
- 与添加链路差异：字段与添加物品页一致但只读；运输方式展示映射后的中文文案。注意详情页姓名取 `xm` 字段（详情数据里 `xm` 为姓名文本，与添加页暂存结构中 `xm`=人员 id 不同源）。

---

## 6. 物品详情-列表 `/xuchang/goodReleaseWork/detailGoodsList`

- **旧组件**：`detail-goods-list.vue`
- **用途**：详情链路中查看放行物品清单；数据由详情页 query `list` 传入。

### UI 元素
- 顶部提示：「放行物品（N项）」。
- 物品卡片（整卡可点）：标题 = 名称 + 资产编码；信息行：单位 `wpdw`、放行日期 `fxrq`。
- 旧版无空态分支、无底部按钮、无编辑/删除图标（与添加物品-列表的差异点）。

### 跳转
- 点卡片 → `/xuchang/goodReleaseWork/detailGoods?itemObj=...`。

### 接口
- 无。
