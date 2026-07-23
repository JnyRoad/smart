# 物品放行（办公区）模块功能规格（前半部分 A：5 页）

> 来源：旧仓库 `smart-h5/src/views-mobile/pages/good-release-work/`（只读净室分析，仅提取功能事实）。
> 服务层：`src/services/goodRreleaseOffice.js`、`src/services/goodRreleaseLive.js`（详情接口与生活区共用）。
> 模块共享字典：`const.js`（放行去处 / 是否返厂 / 出发地点 / 到达地点 / 放行人级别 / 放行事项 / 物品放行类别 / 运输方式）。

## 共享数据流转（草稿暂存）

主表单与人员 / 物品子流程之间通过 localStorage 草稿互通：

- `releaseId`：首次进入人员/物品子流程前由 `POST /articlesrelease/office/draft` 创建的服务端草稿标识；本地草稿只缓存该标识，所有权、园区和申请人均以后端持久化记录为准。

- `applyGoodsWorkInfo`：主表单字段草稿（任何跳出前保存，回来后回填）。
- `goodsPersonInfo`：已添加的放行人员数组（添加人员页写入，主表单与人员列表页读取）。
- `releaseGoodsInfo`：已添加的放行物品数组（添加物品页写入，本批不含）。
- 申请提交成功后三个 key 全部清空，跳到放行记录页。

分支规则：放行事项 `fxsx` 取值 0（人员放行）或 7（人员放行-仅限出差使用）时走「人员放行」子列表；其余值走「物品放行」子列表。

## 字典取值（const.js）

- 放行去处 fxqc：厂内(0) / 厂外(1)
- 是否返厂 sffc：是(0) / 否(1)
- 出发地点 fxdd 与 到达地点 dddd（同一组）：A栋(0)、B栋(1)、C栋(2)、D栋(3)、E栋(4)、F栋(5)、福侨4号仓库(6)、厂区加工中心(7)、纸托(8)、万顺(9)、市场开发部/生活区(11)、其它(10)
- 放行人级别 sqrjb：周边职员级以下(0)、生产楼层长以下(4)、生产楼层长(含)以上(3)、课长级(1)、经理级(2)
- 放行事项 fxsx：人员放行(0)、人员放行(仅限出差使用)(7)、非保密物品放行(1)、保密物品放行(4)、电脑放行(6)、固定资产放行(不包含电脑)(5)、空车放行(3)、自动化物品放行(8)、废品出售(10)
- 物品放行类别 wpfxlb：领/退/转料(0)、异动/转卖(2)、其它(3)
- 运输方式 ysfs（物品子表用）：人工(0)、货车(1)、叉车(2)、三轮(3)

---

## 1. 物品放行（办公区）主表单 `/xuchang/goodReleaseWork`

旧文件：`index.vue`（组件：`page3-tab`、`page3-bottom`、`components/person-tag`、`components/goods-tag`）

**用途**：发起物品放行（办公区）申请的主表单页。

**UI 元素**

- 顶部双 Tab：「发起提交」（当前）/「查看数据」（跳列表页）。
- 表单组一（全部必填，picker 类型为选择器）：
  - 放行去处 fxqc（picker，字典见上）
  - 是否返厂 sffc（picker）
  - 出发地点 fxdd（picker）
  - 出发备注 fxddxq（文本输入，必填提示「请输入出发备注」）
  - 到达地点 dddd（picker）
  - 到达备注 ddddxq（文本输入，必填提示「请输入到达备注」）
  - 放行人级别 sqrjb（picker）
- 表单组二：
  - 放行事项 fxsx（picker，必填；选择联动下方子列表分支）
  - 物品放行类别 wpfxlb（picker，必填）
  - 附件上传 fjsc（单图上传 base64，非必填）
- 条件区块：
  - fxsx=0 或 7 → 「人员放行」入口行：展示已添加人员姓名标签列表（person-tag），空时显示灰色「请添加」，右侧箭头，点击跳添加人员-列表页。
  - 其它 fxsx → 「物品放行」入口行：展示已添加物品标签（格式「物品编码-物品名称数量单位」，goods-tag），空时「请添加」，点击跳添加物品-列表页 `/xuchang/goodReleaseWork/addGoodsList`。
- 底部固定栏：主按钮「申请」。

**交互与校验**

- 任何跳出（切 Tab / 进子列表 / 提交前）先把表单草稿写入 `applyGoodsWorkInfo`；进入页面时回填草稿与两个子列表。
- 进入人员/物品子流程：若本地没有 `releaseId`，先调 `POST /articlesrelease/office/draft`，成功后缓存响应的 `releaseId` 再跳转；失败则停留当前页并提示。
- 点「申请」：整表校验 → 按 fxsx 分支组装 personList（gh、xm、name、lcsy、lcrq=离厂日期、lcsj=离厂时间，由 lcDate 拆分）或 thingList（wpbm、wpmc、wpdw、wpsl、jsdw、fxrq、bz、ysfs、xm、name、cph）→ 提交 `applyMain` + `releaseId` + parkId + status=1（固定）+ personList + thingList；申请人工号由认证主体确定，客户端不得提交 `badge`。
- 成功：清空三个草稿 key，跳 `/xuchang/goodReleaseWork/list`；失败：toast 错误信息。

**页面状态**：人员分支（含已添加 / 空）、物品分支（含已添加 / 空）。

**跳转**：→ list（Tab / 提交成功）、→ addPersonList、→ addGoodsList。

**接口**：POST `/articlesrelease/office/save`（module: platform）。

---

## 2. 放行记录（查看数据） `/xuchang/goodReleaseWork/list`

旧文件：`list.vue`（组件：`page3-tab`、cube-scroll）

**用途**：当前用户的办公区放行申请分页列表。

**UI 元素**

- 顶部双 Tab：「发起提交」/「查看数据」（当前）。
- 列表卡片（点击进详情）：
  - 标题：「{name}提交的放行条」+ 右侧状态标签 oaNode（OA 审批节点名）。
  - 信息行：申请部门 compName、放行事项 releaseItemDesc、申请时间 createTime、确认状态 backStatus。
- 空态组件（无数据时）。

**交互**：下拉刷新（重置第 1 页）、上拉加载下一页（current<pages 时翻页，否则提示无更多）、点卡片跳详情携带 id。

**接口**：GET `/articlesrelease/office/page`，参数 badge（当前用户工号）、type=5（固定）、current、size=10；返回 records / pages。失败 toast「网络错误」。

**跳转**：→ index（Tab）、→ detail?id=xx。

---

## 3. 放行详情 `/xuchang/goodReleaseWork/detail?id=`

旧文件：`detail.vue`（组件：`good-release-live/components/process` 审批流程、`components/person-tag-detail`、`components/goods-tag`）

**用途**：单条放行申请详情，含放行二维码、申请信息、人员/物品、审批流程、出厂放行信息。

**UI 元素（按旧页顺序）**

1. 放行码区（三态互斥）：
   - expire=true 且 status<4：占位图 + 「放行码已过期」
   - status=4：占位图 + 「已出厂」
   - status=2 且有 qrCodePic 且未过期：base64 二维码图 + 「【温馨提示】在门卫处出示放行码」
   - 其余状态（如审批中）不显示放行码区。
2. 申请人行：姓名 name | 部门 deptName，右侧标签：sffcDesc=「是」→「返厂」，否则「不返厂」。
3. 申请信息行（applyMain 的描述字段）：放行去处 fxqcDesc、出发地点 fxddDesc、到达地点 ddddDesc、放行事项 fxsxDesc、放行类别 wpfxlbDesc、放行人级别 sqrjbDesc、附件 fjsc（有图可预览，无则「-」）。
4. 条件区块：fxsx=0/7 → 「放行人员」姓名标签（点击跳 `/xuchang/goodReleaseWork/detailPersonList`，query 带人员列表 JSON）；否则「放行物品」物品标签（点击跳 `/xuchang/goodReleaseWork/detailGoodsList`）。
5. 审批流程 approvalProcess：竖向时间线；提交节点显示「{staffName}-{resultDesc}+时间」；审批节点显示 statusName + 每个审批人「{staffName}-{resultDesc}」（result 配色：0 待审批 / 1 通过 / 2 拒绝、3 关闭 / 4 等待）+ 可选「意见: remark」+ 时间。
6. 放行信息区（仅 status=4 或 5 显示）：状态 statusName、放行人员 securityStaff、离场时间 departureTime、备注 remark。

**接口**：GET `/articlesrelease/detail/{id}`（与生活区共用，module: platform）。失败 toast「网络错误」。

**跳转**：→ detailPersonList、→ detailGoodsList。

---

## 4. 添加人员 `/xuchang/goodReleaseWork/addPerson`

旧文件：`add-person.vue`（组件：`page3-bottom`、`components/search-by-staff` 工号查询弹窗）

**用途**：新增或编辑一名放行人员（仅 fxsx=人员放行分支用）。

**UI 元素**

- 表单：
  - 工号（只读入口行，点击弹出「输入员工工号查询」弹窗；空时灰色「请输入」）
  - 姓名（只读入口行，同上，由查询结果回填）
  - 离厂事由 lcsy（文本输入，必填，提示「请输入离厂事由」）
  - 离厂日期 lcDate（日期时间选择器，必填，含日期+时间）
- 工号查询弹窗（search-by-staff）：标题「输入员工工号查询」+ 工号输入框 + 「确定」；仅当已有 `releaseId` 时查询并回填姓名 name、人员 id（存入 xm 字段）、工号 gh；失败 toast。
- 底部按钮：新增模式「确认添加放行人员」/ 编辑模式「确认修改放行人员」。

**交互**：进入时读 `goodsPersonInfo` 草稿；编辑模式由 query 传 itemInfo（JSON）、itemIndex、isEdit=true 并回填。提交：校验 → 新增 push / 编辑按 itemIndex 替换 → 写回 `goodsPersonInfo` → 跳回添加人员-列表页。

**接口**：GET `/articlesrelease/{releaseId}/staff/lookup?badge={badge}`（仅在当前认证用户拥有的服务端草稿上按工号查询，响应仅含 name、id）。

**跳转**：→ addPersonList（提交后）。

---

## 5. 添加人员-列表 `/xuchang/goodReleaseWork/addPersonList`

旧文件：`add-person-list.vue`（组件：`page3-bottom`）

**用途**：管理已添加的放行人员（增删改），确认后回主表单。

**UI 元素**

- 有数据：顶部提示「已添加放行人员（N人）」；人员卡片：标题「{name} {gh}」+ 右侧编辑、删除图标；信息行：离厂事由 lcsy、离厂时间 lcDate。
- 空态：插画 + 「暂无放行人员信息」「请点击下方按钮添加放行人员」。
- 底部双按钮：「新增人员」（次按钮）→ addPerson；「确 定」（主按钮）→ 回主表单。

**交互**：编辑 → 跳 addPerson 携带 itemInfo/itemIndex/isEdit=true；删除 → 从数组移除并立即写回 `goodsPersonInfo`（无二次确认）；数据源全程为 localStorage 草稿，无接口调用。

**接口**：无。

**跳转**：→ addPerson（新增 / 编辑）、→ index（确定）。

---

## 接口汇总

| 接口 | 方法 | 用途 | 页面 |
|---|---|---|---|
| `/articlesrelease/office/save` | POST | 提交放行申请 | 主表单 |
| `/articlesrelease/office/draft` | POST | 创建当前认证用户的办公区草稿，返回 releaseId | 进入人员/物品子流程 |
| `/articlesrelease/office/page` | GET | 分页列表（badge、type=5、current、size=10） | 放行记录 |
| `/articlesrelease/detail/{id}` | GET | 申请详情（与生活区共用） | 放行详情 |
| `/articlesrelease/{releaseId}/staff/lookup?badge={badge}` | GET | 在受权草稿中按工号查询最小人员信息 | 添加人员 |

## 不确定点

- 列表接口 type 注释写「固定 4 办公区」但实际传 `type: 5`，以代码事实 5 为准。
- 详情接口复用生活区 `goodRreleaseLive.getDetailApi`，办公区专用 detail 接口在服务层被注释弃用。
- 添加人员页中 `xm` 字段实际存的是员工信息接口返回的 `id`（人员标识），`name` 才是姓名；详情人员标签却展示 `item.xm`（person-tag-detail），推测后端详情返回的 xm 已是姓名，存在新旧字段语义不一致。
- 物品分支（addGoods/addGoodsList/detailGoodsList/detailPersonList）属后半部分 B，本批不覆盖。
