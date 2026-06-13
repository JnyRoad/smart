# 功能规格：首页 / 公告 / 个人中心 / 帮助中心（home-mine-help）

> 来源：旧仓库 `smart-h5/src/views-mobile/pages/{home,mine,help}` 只读分析，仅提取功能事实。
> 日期：2026-06-11

---

## 1. 首页 `/xuchang/home`

旧组件：`home/index.vue` + 子组件 `components/home-top.vue`、`components/home-msg.vue`、`components/nav-list.vue`、`components/park-list.vue`

### 用途
登录后落地页：展示园区/天气/用户信息、最新公告入口、审批待办宫格（带角标）、园区服务宫格（后端动态配置）。

### UI 元素
- **顶部区（home-top）**
  - 园区名称：固定「裕同科技许昌园区」（前端写死 parkInfo：id=PARKID，地址「许昌数字经济产业园」存 store）。
  - 天气条：`{city} {天气类型} {温度}°C {风向}`，仅在天气接口返回 city 时显示。
  - 员工信息条（橙色胶囊）：工号 `employeeId` + 姓名 `employeeName` + 右箭头；点击 → `/xuchang/mine`。
  - 园区切换弹层（park-list）：底部弹出，标题「选择园区」+「确定」按钮，选项：许昌园区 / 石岩园区 / 大岭山园区，选中高亮，点遮罩关闭。**注意：旧版触发入口已被注释，弹层组件保留但实际不可达**——原型中作为演示状态保留并注明。
- **公告条（home-msg）**：喇叭图标 + 最新一条公告标题（单行省略）+ 右箭头；点击 → `/xuchang/home/bbs/list`；无公告时显示「暂无公告」。
- **审批宫格（naveList，前端固定 3 项，4 列网格，带红色角标，>99 显示 99+）**
  | 入口 | 目标路由 | 角标来源 |
  |---|---|---|
  | 宿舍物品放行审批 | `/xuchang/backLog/goodReleaseLive` | GET `/approve/list/new/page`（recordType=3, recordState=0）total |
  | 园区报修审批 | `/xuchang/backLog/dormRepairs` | GET `/approve/list/repairs/list`（recordType=5, recordState=0）total |
  | 退宿审批 | `/xuchang/backLog/dormExit` | POST `/dor/quit/list/approval`（status=0, isSecurityGuard, parkId）total |
- **园区服务宫格（serviceModule，后端动态：GET `/service/module/list` 返回 moduleIcon/moduleName/moduleUrl）**，标题「园区服务」（橙色下划线 tab 样式）。moduleUrl → 前端路由映射（全部入口）：
  | 后端 moduleUrl | 模块名 | 前端路由 |
  |---|---|---|
  | `/dormRepairs` | 园区报修 | `/xuchang/dormRepairs` |
  | `/xuchang/checkIn` | 宿舍申请 | `/xuchang/checkIn` |
  | `/xuchang/dormExit` | 退宿申请 | `/xuchang/dormExit` |
  | `/approve` | 待审批（待办事项） | `/xuchang/backLog` |
  | `/releaseGoods` | 物品放行（生活区） | `/xuchang/goodReleaseLive` |
  | `/articlesrelease` | 物品放行（办公区） | `/xuchang/goodReleaseWork` |
  | `/returnFactory` | 返厂确认 | `/xuchang/returnFactory` |
  | `/dorm` | 我的宿舍 | `/xuchang/dorm` |
  - 特殊项「扫码放行」：不跳路由，调微信 JS-SDK 扫一扫（先 GET `/wechat/sign?url=` 取签名 → wx.config → wx.scanQRCode）。扫码结果 JSON `{id, type}`：type=`'6'` → `/xuchang/backLog/dormExit/detail?id=&curTabIndex=0&isScan=true`；type=`'3-5'` → `/xuchang/backLog/goodReleaseWork/detail?id=&curTabIndex=0&isScan=true`；其它 → `/xuchang/backLog/goodReleaseLive/detail?id=&curTabIndex=0&sort=3&isScan=true`。扫码失败 toast「扫码失败」；签名失败 toast「微信配置失败」。

### 交互与校验
- 进入页面并行加载：用户全量信息、基本信息、服务模块、园区信息（本地写死）、3 个待审批数；全程 loading，任一失败 toast「数据加载失败」。
- 基本信息 `status===0`（已离职）：alert 弹窗「该用户已离职，已为你自动退出登录」，确定 → 清 token → 跳微信 OAuth 重新授权（工牌登录页 `/xuchang/login/logon_badge`）。
- 待审批数接口失败 toast `message || '网络错误'`。

### 页面状态
正常 / 加载中 / 数据加载失败 / 已离职弹窗 / 公告为空（暂无公告）/ 角标 0（不显示）与 99+。

### 接口
- GET `/service/module/list` 服务模块
- GET `/home/bbs/list`（current=1,size=1,parkId）最新公告
- GET `/common/weather`（city=许昌）天气
- GET `/employee/fullinfo` 用户全量信息（工号/姓名）
- GET `/employee/baseinfo` 用户基本信息（离职判断）
- GET `/approve/list/new/page` 待审批分页（物品放行角标）
- GET `/approve/list/repairs/list` 报修待审批
- POST `/dor/quit/list/approval` 退宿待审批
- GET `/wechat/sign` 微信 JS-SDK 签名（扫码放行用）

---

## 2. 公告列表 `/xuchang/home/bbs/list`

旧组件：`home/bbs/list.vue`

### 用途
园区公告列表。

### UI 元素
- 列表项卡片：左侧公告图片 `bbsImg`（60×60）+ 右侧公告标题 `bbsTitle`（单行省略，垂直居中）。
- 空态：通用空组件。

### 交互
- 点击项按 `contentLinkType` 分流：
  - `1` 外部链接：`window.location.href = bbsUrl`（页面直接跳外链）。
  - `2` 富文本：→ `/xuchang/home/bbs/detail?id={bbsId}&isPdf=false`。
  - `4` PDF：→ `/xuchang/home/bbs/detail?id={bbsId}&isPdf=true`。
- 加载失败 toast `message || '网络错误'`；全程 loading。

### 页面状态
正常列表 / 加载中 / 空态 / 加载失败。

### 接口
- GET `/home/bbs/list`（badge=parkId, current=1, size=100）

---

## 3. 公告详情 `/xuchang/home/bbs/detail`

旧组件：`home/bbs/detail.vue`

### 用途
展示单条公告内容，支持富文本与 PDF 两种形态。

### UI 元素
- `isPdf=false`：富文本 `bbsContent`（v-html 渲染，可含图片）。
- `isPdf=true`：PDF 预览（`previewUrl`，vue-pdf 内嵌渲染）。

### 交互
- 富文本内图片点击 → 全屏图片预览器（收集页面内所有 img，支持多图索引切换）。
- 加载失败 toast；全程 loading。

### 页面状态
富文本态 / PDF 态 / 加载中 / 加载失败。

### 接口
- GET `/home/bbs/detail/{id}`

---

## 4. 个人中心 `/xuchang/mine`

旧组件：`mine/index.vue` + `mine/components/menu-list.vue`（数据来自 store 缓存的 baseinfo/parkInfo，本页无新请求；解绑有接口）

### 用途
个人信息概览 + 功能菜单 + 微信解绑。

### UI 元素
- 头部：圆形头像 `employeePhoto` + 姓名 `employeeName` + 性别图标（`employeeSex` 0=男 / 1=女）+ 手机号 `mobile` + 右箭头；点击 → `/xuchang/mine/detail`。
- 园区横幅卡片（背景图）：园区名 `parkName`。
- 三列信息卡：所属部门 `deptName` / 担任职务 `jobName` / 人员状态 `statusDes`（空值显示 `-`）。
- 菜单列表（图标 + 标题 + 右箭头）：
  | 菜单 | 行为 |
  |---|---|
  | 我的宿舍 | → `/xuchang/dorm` |
  | 帮助中心 | → `/xuchang/help` |
  | 微信解绑 | confirm 弹窗「是否确认解除微信绑定？」（确定/取消） |
- 底部「返回」按钮（描边圆角主色）→ `/xuchang/home`。

### 交互与校验
- 解绑确认后 POST `/wechat/xc/unbind`：成功 toast「解绑成功」→ 清 token → 跳微信 OAuth（回到工牌登录页）；失败 toast `message || '网络错误'`。

### 页面状态
正常 / 字段缺省（`-`）/ 解绑确认弹窗 / 解绑成功（toast）/ 解绑失败。

### 接口
- POST `/wechat/xc/unbind` 解除微信绑定

---

## 5. 个人信息 `/xuchang/mine/detail`

旧组件：`mine/detail.vue`（纯展示，数据来自 store 缓存 baseinfo，无接口请求）

### 用途
员工档案明细只读页。

### UI 元素（3 个分组，行式 label/value，空值显示 `-`）
- 分组一：姓名 `employeeName`、工号 `employeeBadge`、公司 `buName`
- 分组二：部门 `deptName`、职务 `jobName`、职层 `jcheName`、福利层次 `welfareLevel`
- 分组三：人员状态 `statusDes`、人员类型 `empTypeDes`、员工性质 `empAttribute`

### 交互 / 状态
无交互；正常 / 字段缺省两态。

### 接口
无（数据来自首页拉取的 GET `/employee/baseinfo` 缓存）。

---

## 6. 帮助中心 `/xuchang/help`

旧组件：`help/index.vue`

### 用途
常见问题列表，分页加载。

### UI 元素
- 头部横幅：标题「帮助中心」+ 右侧插画。
- 问题列表项：问题标题 `questionTitle`（单行省略）+ 右箭头。
- 空态：通用空组件。

### 交互
- 点击项 → `/xuchang/help/detail?id={questionId}`。
- 下拉刷新（重置第 1 页，提示「更新成功」）；上拉加载下一页（current < pages 时翻页，否则提示无更多）。
- 加载失败 toast；全程 loading。

### 页面状态
正常 / 加载中 / 空态 / 无更多 / 加载失败。

### 接口
- GET `/guide/help/question/list`（current, size=10）分页

---

## 7. 帮助文档详情 `/xuchang/help/detail`

旧组件：`help/detail.vue`

### 用途
单个问题的答案富文本页。

### UI 元素
- 顶部标题条：「问题详情」（居中）。
- 答案富文本 `answerContent`（v-html，可含图片）。

### 交互
- 富文本内图片点击 → 全屏图片预览器（多图索引）。
- 加载失败 toast；全程 loading。

### 页面状态
正常 / 加载中 / 加载失败。

### 接口
- GET `/guide/help/question/answer/{id}`

---

## 不确定点
1. 首页园区切换弹层（park-list）在旧版中触发入口已注释，组件不可达；原型按「保留演示」处理，新版是否要恢复需产品确认。
2. 「扫码放行」并非 naveList/serviceList 静态配置项，依赖后端 `/service/module/list` 下发同名模块才会出现；其 moduleUrl 不在映射表内（pageSrc 为空），仅靠标题命中扫码逻辑。
3. 首页 `getDormExitList` 写入 `naveList[2].num`，而 `getBackLogNumGoodReleaseWork`（已停用）也写 `naveList[1].num`，与报修角标同位，存在旧代码冲突痕迹；按现行启用逻辑（报修=index1，退宿=index2）记录。
4. 公告列表请求参数用 `badge: parkId`（首页公告条用 `parkId`），字段名不一致疑为旧版笔误，原样记录。
5. 帮助列表接口 GET 却把分页参数放 `data`（body）而非 `params`，疑为旧版缺陷，契约按 query 理解。
6. 个人中心/个人信息页数据完全依赖首页写入 store 的缓存，直接刷新进入会空白；新版建议页内自取 `/employee/baseinfo`。
