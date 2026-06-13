# smart-h5 全量页面清单（原型设计基准）

日期：2026-06-11
来源：逐文件核对旧版仓库 `~/source/YUTO/yuto-smart/smart-h5` 的 `src/router/`（hash 路由，全部带 `/xuchang/` 前缀）与 `src/views-mobile/pages/`。
用途：作为逐页原型设计与后续开发的**权威页面清单**。每一行对应一个独立路由页面，原型必须逐页覆盖。

统计：13 个业务模块 + lock（挂载于 /dorm 路由下）+ 根工作台，共 **69 个业务页面路由**（另有 1 个 test 页面，不计入商用范围）。`.vue` 文件实测 134 个（含组件，非路由页面数）。

## 0. 根路由

| # | 旧路由 | 标题 | 组件 | 鉴权 |
|---|--------|------|------|------|
| 1 | `/` | 工作台 | views-mobile/index | 否 |

## 1. login — 登录（4 页，免鉴权）

| # | 旧路由 | 标题 | 组件 | 鉴权 |
|---|--------|------|------|------|
| 1 | `/xuchang/login` | 登录 | login/index | 否 |
| 2 | `/xuchang/login/wechat` | 微信登录 | login/login_wechat | 否 |
| 3 | `/xuchang/login/wechat/code` | 微信验证码 | login/code_wechat | 否 |
| 4 | `/xuchang/login/logon_badge` | 工牌登录 | login/logon_badge | 否 |

## 2. home — 首页（3 页）

| # | 旧路由 | 标题 | 组件 |
|---|--------|------|------|
| 1 | `/xuchang/home` | 首页 | home/index |
| 2 | `/xuchang/home/bbs/list` | 公告列表 | home/bbs/list |
| 3 | `/xuchang/home/bbs/detail` | 公告详情 | home/bbs/detail |

## 3. mine — 个人中心（2 页）

| # | 旧路由 | 标题 | 组件 |
|---|--------|------|------|
| 1 | `/xuchang/mine` | 个人中心 | mine/index |
| 2 | `/xuchang/mine/detail` | 个人信息 | mine/detail |

## 4. dorm + lock — 我的宿舍 / 门锁动态码（4 页）

| # | 旧路由 | 标题 | 组件 |
|---|--------|------|------|
| 1 | `/xuchang/dorm` | 我的宿舍 | dorm/index |
| 2 | `/xuchang/dorm/waterElec` | 水电扣费明细 | dorm/water-elec |
| 3 | `/xuchang/dorm/lock` | 门锁动态码 | lock/index |
| 4 | `/xuchang/dorm/getCode` | 获取门锁动态码 | lock/get-code |

## 5. check-in — 宿舍签到/申请（3 页）

| # | 旧路由 | 标题 | 组件 |
|---|--------|------|------|
| 1 | `/xuchang/checkIn` | 宿舍申请 | check-in/index |
| 2 | `/xuchang/checkIn/selectRoom` | 手动选择房间 | check-in/select-room |
| 3 | `/xuchang/checkIn/detail` | 宿舍申请详情 | check-in/list |

## 6. dorm-exit — 退宿申请（3 页）

| # | 旧路由 | 标题 | 组件 |
|---|--------|------|------|
| 1 | `/xuchang/dormExit` | 退宿申请 | dorm-exit/index |
| 2 | `/xuchang/dormExit/list` | 退宿申请列表 | dorm-exit/list |
| 3 | `/xuchang/dormExit/detail` | 退宿申请详情 | dorm-exit/detail |

## 7. dorm-repairs — 宿舍报修（3 页）

| # | 旧路由 | 标题 | 组件 |
|---|--------|------|------|
| 1 | `/xuchang/dormRepairs` | 宿舍报修 | dorm-repairs/index |
| 2 | `/xuchang/dormRepairs/list` | 宿舍报修列表 | dorm-repairs/list |
| 3 | `/xuchang/dormRepairs/detail` | 宿舍报修详情 | dorm-repairs/detail |

## 8. good-release-live — 物品放行·生活区（3 页）

| # | 旧路由 | 标题 | 组件 |
|---|--------|------|------|
| 1 | `/xuchang/goodReleaseLive` | 物品放行（生活区） | good-release-live/index |
| 2 | `/xuchang/goodReleaseLive/list` | 物品放行记录（生活区） | good-release-live/list |
| 3 | `/xuchang/goodReleaseLive/detail` | 物品放行详情（生活区） | good-release-live/detail |

## 9. good-release-work — 物品放行·办公区（11 页）

| # | 旧路由 | 标题 | 组件 |
|---|--------|------|------|
| 1 | `/xuchang/goodReleaseWork` | 物品放行（办公区） | good-release-work/index |
| 2 | `/xuchang/goodReleaseWork/list` | 物品放行记录（办公区） | good-release-work/list |
| 3 | `/xuchang/goodReleaseWork/detail` | 物品放行详情（办公区） | good-release-work/detail |
| 4 | `/xuchang/goodReleaseWork/addPerson` | 添加人员 | good-release-work/add-person |
| 5 | `/xuchang/goodReleaseWork/addPersonList` | 添加人员-列表 | good-release-work/add-person-list |
| 6 | `/xuchang/goodReleaseWork/addGoods` | 添加物品 | good-release-work/add-goods |
| 7 | `/xuchang/goodReleaseWork/addGoodsList` | 添加物品-列表 | good-release-work/add-goods-list |
| 8 | `/xuchang/goodReleaseWork/detailPerson` | 人员详情 | good-release-work/detail-person |
| 9 | `/xuchang/goodReleaseWork/detailPersonList` | 人员详情-列表 | good-release-work/detail-person-list |
| 10 | `/xuchang/goodReleaseWork/detailGoods` | 物品详情 | good-release-work/detail-goods |
| 11 | `/xuchang/goodReleaseWork/detailGoodsList` | 物品详情-列表 | good-release-work/detail-goods-list |

## 10. return-factory — 返厂确认（2 页）

| # | 旧路由 | 标题 | 组件 |
|---|--------|------|------|
| 1 | `/xuchang/returnFactory` | 返厂确认 | return-factory/list |
| 2 | `/xuchang/returnFactory/detail` | 返厂确认详情 | return-factory/detail |

## 11. backLog — 待办事项（12 页）

| # | 旧路由 | 标题 | 组件 | 鉴权 |
|---|--------|------|------|------|
| 1 | `/xuchang/backLog` | 待办事项 | backLog/index | 是 |
| 2 | `/xuchang/backLog/goodReleaseLive` | 物品放行(生活区)审批列表 | backLog/good-release-live/list | 是 |
| 3 | `/xuchang/backLog/goodReleaseLive/detail` | 物品放行(生活区)-详情 | backLog/good-release-live/detail | 是 |
| 4 | `/xuchang/backLog/goodReleaseLive/result` | 物品放行-结果 | backLog/good-release-live/result | 是 |
| 5 | `/xuchang/backLog/code` | 二维码信息 | backLog/good-release-live/code | 否 |
| 6 | `/xuchang/backLog/goodReleaseWork` | 物品放行(办公区)审批列表 | backLog/good-release-work/list | 是 |
| 7 | `/xuchang/backLog/goodReleaseWork/detail` | 物品放行(办公区)-详情 | backLog/good-release-work/detail | 是 |
| 8 | `/xuchang/backLog/goodReleaseWork/result` | 物品放行-结果 | backLog/good-release-work/result | 是 |
| 9 | `/xuchang/backLog/dormRepairs` | 园区报修审批列表 | backLog/dorm-repairs/list | 是 |
| 10 | `/xuchang/backLog/dormRepairs/detail` | 园区报修-详情 | backLog/dorm-repairs/detail | 是 |
| 11 | `/xuchang/backLog/dormExit` | 退宿申请审批列表 | backLog/dorm-exit/list | 是 |
| 12 | `/xuchang/backLog/dormExit/detail` | 退宿申请-详情 | backLog/dorm-exit/detail | 是 |

## 12. help — 帮助中心（2 页）

| # | 旧路由 | 标题 | 组件 |
|---|--------|------|------|
| 1 | `/xuchang/help` | 帮助中心 | help/index |
| 2 | `/xuchang/help/detail` | 帮助文档详情 | help/detail |

## 13. visitor — 访客管理（16 页，全部免鉴权）

| # | 旧路由 | 标题 | 组件 | 备注 |
|---|--------|------|------|------|
| 1 | `/xuchang/visitor` | 入厂申请 | visitor/index | 标准流程 |
| 2 | `/xuchang/visitor/tel` | 手机验证 | visitor/tel | 标准流程 |
| 3 | `/xuchang/visitor/telHefei` | 手机验证 | visitor/telHefei | 合肥变体 |
| 4 | `/xuchang/visitor/visitorInfo` | 访客信息 | visitor/visitorInfo | 标准流程 |
| 5 | `/xuchang/visitor/indexHefei` | 入厂申请 | visitor/indexHefei | 合肥变体 |
| 6 | `/xuchang/visitor/visitorInfoHefei` | 访客信息 | visitor/visitorInfoHefei | 合肥变体 |
| 7 | `/xuchang/visitor/addPersonList` | 随行人员列表 | visitor/add-person-list | |
| 8 | `/xuchang/visitor/addPerson` | 添加随行人员 | visitor/add-person | |
| 9 | `/xuchang/visitor/addPersonHefei` | 添加随行人员 | visitor/add-person-hefei | 合肥变体 |
| 10 | `/xuchang/visitor/addCarList` | 车辆列表 | visitor/add-car-list | |
| 11 | `/xuchang/visitor/addCar` | 添加车辆 | visitor/add-car | |
| 12 | `/xuchang/visitor/result` | 提交成功 | visitor/result | |
| 13 | `/xuchang/visitor/resultTruck` | 提交成功（货车） | visitor/resultTruck | |
| 14 | `/xuchang/visitor/code` | 二维码信息 | visitor/code | |
| 15 | `/xuchang/visitor/addAreaType` | 添加授权进入区域 | visitor/addAreaType | |
| 16 | `/xuchang/visitor/truck` | 货车预约 | visitor/truck | |

## 备注

- 合肥变体 4 页（telHefei / indexHefei / visitorInfoHefei / addPersonHefei）在新版架构中收敛为同一路由 + 运行时配置分支（见架构设计 §5），但原型阶段对两套流程分别出稿，保证功能 100% 对齐。
- `lock` 模块无独立顶级路由，由 `/xuchang/dorm/lock`、`/xuchang/dorm/getCode` 挂载。
- `test` 模块（`/xuchang/test`，内容详情）为调试页，不纳入商用重写范围。
- 鉴权列空白处默认需要登录态（路由 meta 未标 `isAuth: false`）。
