# 旧系统接口全量枚举与原型规格覆盖率比对报告

> 日期：2026-06-11
> 旧系统接口定义层：`smart-h5/src/services/`（20 个文件，1333 行）
> 比对对象：`docs/prototype/specs/` 下 12 份模块规格
> 性质：静态代码分析（局限性见文末）

## 0. 关键背景事实

1. **module 前缀注册表**（`smart-h5/src/conf.js` `PROXYS`）只注册了 4 个模块：`platform`、`app`、`auth`、`algorithm`。services 中引用 `admin` / `visitor` / `workbench` / `file` 模块的接口，运行时会直接抛 `module在PROXYS不存在` —— 这些接口**必然是死代码**（无论是否被 import）。
2. `services/login.js` 的 `loginByPwd` 不带 module，URL 直接拼 `BASEURL`（`/auth/oauth/token`），全仓库无调用方。
3. `views-mobile/` 中**没有绕过 services 层的直接 HTTP 请求**。仅 `login/code_wechat.vue`、`login/components/msgCode.vue` 调用 `http.refreshAuthorizationHeader()`（设置请求头，非发请求）。
4. `components/tce-form/supplement/form-img.vue`、`form-img-remarks.vue` 未在 `chunks/components.js` 全局注册，也未被任何页面 import → `services/uploadImage.js` 的全部 4 个接口为死代码。
5. 部分 service 函数路径相同（如 `backLog.getBackLogListGoodReleaseWork` 与 `goodRreleaseOffice.getPageApi` 同为 `/articlesrelease/office/page`），按**唯一 module+method+path** 去重计数。

## 1. 旧系统接口全量清单（87 条，去重后）

图例：✅ 已收录（标注规格文件）｜⚠️ 收录但有误/标注为死代码｜❌ 未收录。
规格文件名省略 `.md` 后缀；「页面」列为旧仓库使用方（services 文件 → 页面）。

### 1.1 platform 模块（56 条）

| # | Method | Path | 用途 | 被哪些页面使用 | 覆盖 |
|---|---|---|---|---|---|
| 1 | GET | /approve/list/new/page | 待审批列表（物品放行生活区/通用） | home/index、backLog/good-release-live/list | ✅ backlog-a、home-mine-help |
| 2 | GET | /articlesrelease/office/page | 物品放行(办公区)列表/审批列表 | home/index、backLog/good-release-work/list、good-release-work/list | ✅ backlog-b、good-release-work-a |
| 3 | POST | /articlesrelease/status/security/update | 保安放行 | backLog live/work detail | ✅ backlog-a、backlog-b |
| 4 | GET | /articlesrelease/status/update | 物品放行审批（生活区，室友/宿管） | backLog/good-release-live/detail | ✅ backlog-a |
| 5 | GET | /approve/list/repairs/list | 园区报修待审批列表 | home/index、backLog/dorm-repairs/list | ✅ backlog-b、home-mine-help |
| 6 | GET | /dormitory/repair/status/update | 报修接单/不接单 | backLog/dorm-repairs/detail | ✅ backlog-b |
| 7 | POST | /dormitory/repair/reply | 报修维修结果回复 | backLog/dorm-repairs/detail | ✅ backlog-b |
| 8 | POST | /dor/quit/list/approval | 退宿待审批列表 | home/index、backLog/dorm-exit/list | ✅ backlog-b、home-mine-help |
| 9 | GET | /dor/quit/status/update | 退宿审批 | backLog/dorm-exit/detail | ✅ backlog-b |
| 10 | POST | /dormitory/queryDormitory | 按园区查宿舍楼 | check-in/index | ✅ dorm-lock-checkin |
| 11 | GET | /dormitory/type/by/park-and-dormitory | 房型枚举 | check-in/index | ✅ dorm-lock-checkin |
| 12 | GET | /park/tree/condition | 楼层树 | check-in/select-room | ✅ dorm-lock-checkin |
| 13 | GET | /dormitory/room/search/condition | 按条件查房间 | check-in/select-room | ✅ dorm-lock-checkin |
| 14 | POST | /dormitory/room/bedDetail/{roomId} | 房间床位详情 | check-in/select-room | ✅ dorm-lock-checkin |
| 15 | GET | /staff/define/badge | 按工号查员工 | check-in/index | ✅ dorm-lock-checkin |
| 16 | POST | /dormitory/room/autoallot | 宿舍申请提交（自动/手动分床） | check-in/index | ✅ dorm-lock-checkin |
| 17 | GET | /dormitory/staff/roomList/{staffBadge} | 本人入住宿舍列表 | check-in/list | ✅ dorm-lock-checkin |
| 18 | GET | /dormitory/staff/statementdetail/record | 水电扣费明细 | dorm/water-elec | ✅ dorm-lock-checkin |
| 19 | POST | /dormitory/staff/face/compare | 人脸比对（getStaffFace） | **无调用方** | ❌ 死代码 |
| 20 | POST | /dormitory/staff/update/pwd | 人脸刷新门锁动态码 | lock/get-code | ✅ dorm-lock-checkin |
| 21 | GET | /dormitory/staff/get/pwd | 获取门锁动态码 | lock/index、check-in/list | ✅ dorm-lock-checkin |
| 22 | POST | /dormitory/staff/update/lock/pwd | 修改门锁动态码 | lock/edit-code | ✅ dorm-lock-checkin |
| 23 | POST | /dor/quit/apply | 提交退宿申请 | dorm-exit/index | ✅ dorm-exit-repairs |
| 24 | GET | /dor/quit/page | 退宿申请分页列表 | dorm-exit/list | ✅ dorm-exit-repairs |
| 25 | GET | /dor/quit/detail/{id} | 退宿申请详情 | dorm-exit/detail、backLog/dorm-exit/detail | ✅ dorm-exit-repairs、backlog-b |
| 26 | GET | /dor/quit/list/check/{id} | 退宿门卫扫码核验 | backLog/dorm-exit/detail | ✅ dorm-exit-repairs、backlog-b |
| 27 | POST | /dormitory/repair/add | 提交报修 | dorm-repairs/index | ✅ dorm-exit-repairs |
| 28 | GET | /dormitory/repair/query/record | 报修分页列表 | dorm-repairs/list | ✅ dorm-exit-repairs |
| 29 | GET | /dormitory/repair/query/detail/{id} | 报修详情 | dorm-repairs/detail、backLog/dorm-repairs/detail | ✅ dorm-exit-repairs、backlog-b |
| 30 | POST | /articlesrelease/living/save | 物品放行(生活区)提交 | good-release-live/index | ✅ good-release-live-return-factory |
| 31 | GET | /articlesrelease/page | 物品放行(生活区)列表 | good-release-live/list | ✅ good-release-live-return-factory |
| 32 | GET | /articlesrelease/detail/{id} | 物品放行详情（生活/办公/退宿码共用） | live/work detail、backLog 各 detail/code、return-factory/detail | ✅ backlog-a、backlog-b、good-release-live-return-factory、good-release-work-a |
| 33 | POST | /articlesrelease/office/save | 物品放行(办公区)提交 | good-release-work/index | ✅ good-release-work-a、good-release-work-b |
| 34 | GET | /articlesrelease/oa/staff/info/{badge} | 按工号查 OA 员工信息 | good-release-work/add-person、add-goods | ✅ good-release-work-a、good-release-work-b |
| 35 | GET | /articlesrelease/back/page | 返厂待确认列表 | return-factory/list | ✅ good-release-live-return-factory |
| 36 | POST | /articlesrelease/back/confirm/{releaseId} | 确认返厂 | return-factory/detail | ✅ good-release-live-return-factory |
| 37 | GET | /common/config/admittance/notice | 入厂预约温馨提示 | visitor/index、indexHefei | ✅ visitor-a、visitor-hefei |
| 38 | GET | /admittance/apply/get/openId | OAuth code 换 openId | visitor/index、indexHefei | ✅ visitor-a、visitor-hefei |
| 39 | POST | /admittance/apply/app/searchReceptionist | 查询被访人 | visitor/index、indexHefei | ✅ visitor-a、visitor-hefei |
| 40 | POST | /admittance/apply/save/apply | 提交入厂申请 | visitor/tel、telHefei | ✅ visitor-a、visitor-hefei |
| 41 | POST | /admittance/apply/save/car/apply | 货车入厂预约提交 | visitor/truck | ✅ visitor-b |
| 42 | GET | /admittance/apply/enum/cause | 来访事由枚举（标准） | visitor/visitorInfo | ✅ visitor-a |
| 43 | GET | /visitor/enum/cause/type | 来访事由枚举（合肥） | visitor/visitorInfoHefei | ✅ visitor-hefei |
| 44 | GET | /admittance/apply/enum/car/cause | 货车入厂事由枚举 | visitor/truck | ✅ visitor-b |
| 45 | GET | /admittance/apply/enum/person/cert | 人员证件类型枚举 | visitorInfo(s)、add-person(s) | ✅ visitor-a、visitor-hefei |
| 46 | GET | /admittance/apply/enum/vehicle/cert | 车辆证件类型枚举 | visitor/add-car | ✅ visitor-b |
| 47 | GET | /admittance/apply/enum/vehicle/type | 车辆类型枚举 | visitor/add-car（UI 已停用） | ✅ visitor-b |
| 48 | GET | /admittance/apply/enum/vehicle/color | 车辆颜色枚举 | visitor/add-car（UI 已停用） | ✅ visitor-b |
| 49 | GET | /admittance/area/type/list | OA 区域类别 | visitor/visitorInfo | ✅ visitor-a |
| 50 | GET | /admittance/apply/enum/carry | 携带物品枚举 | visitorInfoHefei（标准版已注释停用） | ✅ visitor-hefei |
| 51 | GET | /common/config/visitor/health | 健康码/行程码开关 | visitor/visitorInfoHefei | ✅ visitor-hefei |
| 52 | GET | /admittance/apply/search/Detail/{id} | 访客二维码/申请详情 | visitor/code | ✅ visitor-b |
| 53 | POST | /admittance/apply/equal/check | 访客身份/重复申请校验 | visitor/visitorInfo | ✅ visitor-a |
| 54 | GET | /admittance/area/type/admittance/factory/list | 厂区列表（getFactoryList） | **无调用方**（code.vue 中已注释） | ❌ 死代码 |
| 55 | GET | /admittance/apply/enum/factory/type | 厂区区域枚举（旧回退） | visitorInfo、tel、addAreaType、code | ✅ visitor-a、visitor-b |
| 56 | GET | /admittance/apply/app/area-options | 授权区域配置（新） | visitorInfo、tel、addAreaType | ✅ visitor-a、visitor-b |

### 1.2 app 模块（18 条）

| # | Method | Path | 用途 | 被哪些页面使用 | 覆盖 |
|---|---|---|---|---|---|
| 57 | GET | /wechat/sign | 微信 JS-SDK 签名（扫一扫） | home/nav-list、backLog 各 list | ✅ backlog-a、backlog-b、home-mine-help |
| 58 | GET | /dormitory/repair/enum/range | 维修区域枚举 | dorm-repairs/index | ✅ dorm-exit-repairs |
| 59 | GET | /appdormitory/roomList/{staffBadge} | 按工号查本人宿舍房间 | good-release-live/index、dorm-exit/index | ✅ good-release-live-return-factory、dorm-exit-repairs |
| 60 | GET | /guide/help/question/list | 帮助中心问题列表 | help/index | ✅ home-mine-help |
| 61 | GET | /guide/help/question/answer/{id} | 帮助文档详情 | help/detail | ✅ home-mine-help |
| 62 | GET | /service/module/list | 首页服务模块宫格 | home/index | ✅ home-mine-help |
| 63 | GET | /home/bbs/list | 公告列表 | home/index、home-msg、bbs/list | ✅ home-mine-help |
| 64 | GET | /home/bbs/detail/{id} | 公告详情 | bbs/detail | ✅ home-mine-help |
| 65 | GET | /common/weather | 天气 | home/home-top | ✅ home-mine-help |
| 66 | POST | /wechat/getBadge | 微信 code 换工号 | **无调用方** | ⚠️ login-root（已注明本批页面未调用） |
| 67 | POST | /wechat/xc/banging/badge | 绑定微信 openId 与工号 | login/logon_badge | ✅ login-root |
| 68 | GET | /employee/fullinfo | 用户全量信息 | home/index | ✅ home-mine-help |
| 69 | GET | /employee/baseinfo | 用户基本信息（离职判断） | home/index | ✅ home-mine-help |
| 70 | POST | /wechat/xc/unbind | 解除微信绑定 | mine/menu-list | ✅ home-mine-help |
| 71 | GET | /sms/send/getCode/{mobile} | 发送短信验证码 | identifying-code 组件 → visitor tel/telHefei/truck、login/msgCode | ✅ login-root、visitor-a、visitor-b、visitor-hefei |
| 72 | GET | /sms/verify | 校验短信验证码 | visitor tel/telHefei/truck | ✅ visitor-a、visitor-b、visitor-hefei |
| 73 | POST | /wechat/visit/checkFace | 图片上传/人脸校验（所有 tce-form 上传组件共用） | visitor 各页照片/证件照、good-release-live 照片、lock/get-code | ✅ dorm-lock-checkin、good-release-live-return-factory；visitor-a/b/hefei 经本次补遗收录 |
| 74 | POST | /wechat/visit/checkBlackVisitor | 访客黑名单校验 | visitor tel/telHefei | ✅ visitor-a、visitor-hefei |

### 1.3 auth / algorithm / 无模块（3 条）

| # | Module | Method | Path | 用途 | 被哪些页面使用 | 覆盖 |
|---|---|---|---|---|---|---|
| 75 | auth | POST | /wx/public/token | 微信 code 换 token | login/code_wechat | ✅ login-root |
| 76 | （无，直拼 BASEURL） | POST | /auth/oauth/token | 账号密码登录 | **无调用方** | ⚠️ login-root（已注明旧页未接线） |
| 77 | algorithm | POST | /out/face/cut | 人脸检测+裁剪（拍照组件 uploadServer） | form-upload-image-single-face → visitorInfo(s)、add-person(s) | ⚠️ 原 12 份规格均未收录，**本次已补遗**至 visitor-a、visitor-hefei |

### 1.4 死模块接口（module 未注册于 PROXYS，调用即抛错，共 10 条）

| # | Module | Method | Path | 用途（按函数名推断） | 被哪些页面使用 | 覆盖 |
|---|---|---|---|---|---|---|
| 78 | admin | GET | /mobile/{mobile} | 手机号发验证码（账号激活） | 无（1_activation.js 无调用方） | ❌ 死代码 |
| 79 | admin | GET | /user/password/validUrl | 激活链接校验 | 无 | ❌ 死代码 |
| 80 | admin | POST | /mobile/captcha/valid | 验证码校验 | 无 | ❌ 死代码 |
| 81 | admin | POST | /user/password/reset | 设置/重置密码 | 无 | ❌ 死代码 |
| 82 | visitor | GET | /access/article/type/list | 接口测试（test.js getTest） | 无 | ❌ 死代码 |
| 83 | workbench | POST | /employee/image/import | 人脸采集导入 | 无（upload.js postFaceImageSave 无调用方） | ❌ 死代码 |
| 84 | file | POST | /file/upload | 通用文件上传 | 无（uploadImage.js postImageSave 无调用方） | ❌ 死代码 |
| 85 | visitor | POST | /file/upload/face | 人脸照上传 | 仅未注册的 supplement/form-img.vue | ⚠️ visitor-a 曾**误记**为访客照片上传接口，本次补遗已更正 |
| 86 | visitor | POST | /file/upload/file | 其他图片上传 | 仅未注册的 supplement 组件 | ❌ 死代码 |
| 87 | visitor | POST | /file/upload/card | 证件图片上传 | 仅未注册的 supplement 组件 | ❌ 死代码 |

> 唯一接口总数 **87**（platform 56 + app 18 + auth/algorithm/无模块 3 + 死模块 10；platform 表内 #19/#54、app 表内 #66 等死代码已计入各自模块小计）。

## 2. 统计

| 指标 | 数量 |
|---|---|
| 旧系统唯一接口总数 N（module+method+path 去重） | **87** |
| 其中实际在用（有可达调用链） | 73 |
| 其中死代码（无调用方 / 模块未注册 / 调用组件未挂载） | 14 |
| 核对前规格已收录 | 75（含 2 条已注明死代码的 #66/#76，及 1 条误记 #86） |
| 在用接口的规格覆盖（核对前） | 72 / 73 |
| 在用接口的规格覆盖（**本次补遗后**） | **73 / 73 = 100%** |

### 2.1 未收录清单（12 条，全部为死代码，原因推测）

| Path | Module | 原因推测 |
|---|---|---|
| /mobile/{mobile} | admin | 账号激活流程残留：`1_activation.js` 整文件无调用方，且 admin 模块未注册于 PROXYS，疑为从管理后台工程复制的死代码 |
| /user/password/validUrl | admin | 同上 |
| /mobile/captcha/valid | admin | 同上 |
| /user/password/reset | admin | 同上 |
| /dormitory/staff/face/compare | platform | `dorm.getStaffFace` 无调用方；门锁人脸刷新实际改走 `/wechat/visit/checkFace`+`/dormitory/staff/update/pwd`，疑为旧方案残留 |
| /access/article/type/list | visitor | `test.js` 脚手架测试接口，且 visitor 模块未注册 |
| /employee/image/import | workbench | `upload.postFaceImageSave` 无调用方，workbench 模块未注册；疑为工作台工程复制残留 |
| /file/upload | file | `uploadImage.postImageSave` 无调用方（页面用的是同名的 `upload.postImageSave`→checkFace），file 模块未注册 |
| /file/upload/face | visitor | 调用方 supplement/form-img.vue 未注册到任何页面，visitor 模块未注册；visitor-a 此前误记，已补遗更正 |
| /file/upload/file | visitor | 同上（form-img / form-img-remarks 均不可达） |
| /file/upload/card | visitor | 同上 |
| /admittance/area/type/admittance/factory/list | platform | 唯一调用处 code.vue 中已整段注释，被 `/admittance/apply/enum/factory/type` 取代 |

### 2.2 核对前的实质缺漏（已通过补遗修复）

| 接口 | 实际用途 | 修复动作 |
|---|---|---|
| algorithm `POST /out/face/cut` | 访客照片人脸检测+裁剪（form-upload-image-single-face 的拍照组件 uploadServer），visitorInfo / add-person / visitorInfoHefei / add-person-hefei 均依赖 | 补遗至 visitor-a.md、visitor-hefei.md |
| app `POST /wechat/visit/checkFace`（访客侧） | 访客照片、随行人员照片、证件照片（add-car certImg、合肥 certPic）的真实上传接口 | 补遗至 visitor-a.md（更正 `/file/upload/face` 误记）、visitor-b.md、visitor-hefei.md |

另有 2 条死接口被规格如实收录并标注（不算缺漏）：`/auth/oauth/token`（login-root 注明"旧页未接线"）、`/wechat/getBadge`（login-root 注明"未调用"）。新版实现时这 2 条是否保留由登录方案决定。

## 3. 局限性（必读）

1. **本报告是纯静态代码分析结果**：接口清单、method、路径、参数名均提取自旧仓库前端代码（services 定义 + 页面调用处），未与任何后端文档或线上流量核对。
2. **请求/响应字段结构未在本报告固化**。按架构设计（`docs/superpowers/specs/2026-06-11-nextjs-rewrite-architecture.md` §3）的约定：**88+ 接口的请求/响应类型必须在开发阶段通过真实抓包逐个固化**（zod schema），供应商代码只能当线索、不能当规范。本报告的 87 条枚举即为抓包工作的清单底稿。
3. 静态分析无法发现：网关层改写（如 POST body 中 size/current 的提升行为，架构 §3 已列为需抓包确认项）、服务端按 header/租户分流的差异、已废弃但仍被旧后端兼容的字段。
4. 「死代码」判定基于当前 git 工作区代码；若线上版本与仓库不一致（如热修），结论可能失效。
5. 计数口径为唯一 `module+method+path`；同一路径被多个 service 函数封装（如 `/articlesrelease/office/page`）只计 1 条。
