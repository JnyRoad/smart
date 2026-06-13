# 访客模块设计（标准/许昌流程）

日期：2026-06-12
状态：设计已经旅途批准（含分支评审节奏修订）；本文档为落档 spec，待旅途审阅。
前置：[2026-06-11-nextjs-rewrite-architecture.md](./2026-06-11-nextjs-rewrite-architecture.md)、
功能事实来源 [visitor-a.md](../../prototype/specs/visitor-a.md)、[visitor-b.md](../../prototype/specs/visitor-b.md)（旧仓库只读净室分析）。

## 0. 范围与决策（旅途拍板）

- **本期只做标准（许昌）流程 12 页；合肥变体暂缓**（页面内不写死许昌逻辑，parkId/parkName 取租户配置，为 `flows.visitor==='hefei'` 字段级分支留口子但不实现）。
- 健康码/行程码为合肥流程字段，本期不涉及；将来实现时跟随后端 `/common/config/visitor/health` 开关。
- **AES 加密本期不需要**：已核对旧仓库，加密仅用于 check-in/lock/激活模块，访客全部接口无加密。
- 访客全流程**免登录**（所有接口 `auth: 'none'`），与员工登录态互不相干。
- 模块内分 3 个分支交付，每个分支独立评审合并（见 §6）。

## 1. 路由与页面（12 页）

| 新路由 | 旧页 | 要点 |
|---|---|---|
| `/visitor` | index | 步骤① 被访人姓名+手机号；进入即访客 OAuth（code 换 openId）；温馨提示弹窗（isNeedNotice=1，富文本）；下一步调 searchReceptionist，成功存被访人并跳 info |
| `/visitor/info` | visitorInfo | 步骤② 访客姓名、人脸照片、证件号（身份证校验）、来访单位、来访事由（枚举）、厂区单选卡片+授权区域 chips（内联最多 4 个 + 更多区域入口）、来访/离开时间（离开>来访、跨度≤365 天）、随行人员入口；下一步调 equal/check 通过后跳 tel |
| `/visitor/area` | addAreaType | 区域多选：搜索过滤、全选/取消全选、已选 n/总数、详细位置、确定/重置；query 携带 type/factoryType/parkId |
| `/visitor/persons` | addPersonList | 随行人员列表（纯本地）：照片+姓名+证件号+编辑/删除（删除无二次确认，复刻旧行为）；空态；新增/确定双按钮 |
| `/visitor/persons/add` | addPerson | 新增/编辑随行人员：姓名、人脸照片、证件号（身份证校验）；编辑模式回填 |
| `/visitor/cars` | addCarList | 车辆列表（纯本地）：车牌+司机姓名+编辑/删除；空态；新增/确定双按钮。**注意**：旧版 info 页的车辆入口整段被注释（实际不可达，已批准 mockup 同样不放入口），车辆页仅保留直达路由；tel 提交仍映射 carList（与旧版行为完全一致） |
| `/visitor/cars/add` | addCar | 新增/编辑车辆：车牌（专用输入）、司机姓名（默认带访客姓名）、证件类型（枚举，默认身份证复印件 code=2）、证件照片上传 |
| `/visitor/tel` | tel | 步骤③ 手机号+验证码（120s 倒计时）→ sms/verify → 区域复核剪除（剪空 toast 并回 info）→ 黑名单校验 → save/apply（主访客 isMain=1 + 随行 isMain=0、时间补 `:00`、vehicleList 来自 carList）→ 成功清草稿跳 result |
| `/visitor/result` | result | 「已发送成功，等待被访对象审批」+ 再预约一次 → /visitor |
| `/visitor/truck` | truck | 货车独立预约：车牌、事由（货车枚举）、姓名、出发地、预约时间、备注 + 手机验证；提交 save/car/apply |
| `/visitor/truck/result` | resultTruck | 「等待系统审批」+ 再预约一次 → /visitor/truck |
| `/visitor/code` | code | 通行二维码页（query id）：delFlag 三态（0 有效=二维码+预约码+预约信息卡+使用指引；1 失效=失效图+预约信息卡；2 过期=仅失效图）；区域 code→名称用 factory/type 两个枚举映射 |

## 2. 流程状态管理（方案 A，已批准）

`features/visitor/flow-store.ts`：单一 Zustand store + persist 中间件（localStorage，独立 key `visitor-flow`，与旧版键不互通——访客草稿是临时数据，无新旧共享需求）。

```
state: {
  host:    { openId?, unionId?, receptionistBadge?, receptionistName?, receptionistPhone? }
  visitor: { visitorName, visitorPhotoId, certNo, company, cause?, startTime, endTime,
             permitFactoryType?, areasByFactory: Record<factoryType, {list: string[], custom: string}> }
  fellows: FellowPerson[]    // { fellowName, fellowPhotoId, certNo }
  cars:    VisitorCar[]      // { plate, name, certType, certImg }
  phone:   string
}
actions: patchHost / patchVisitor / setFellows(增删改) / setCars(增删改) / setAreas / reset
```

为什么必须持久化：`/visitor` 入口做微信 OAuth 整页跳转，回跳后内存全丢；旧版靠 localStorage 存活，新版以 persist 等价实现。提交成功后 `reset()` 清草稿。

## 3. API 层（features/visitor/api.ts，全部 auth:'none'）

| 接口 | Method/模块 | 用途 |
|---|---|---|
| `/common/config/admittance/notice?parkId=` | GET platform | 温馨提示 |
| `/admittance/apply/get/openId?code=` | GET platform | 访客 OAuth code 换 openId |
| `/admittance/apply/app/searchReceptionist` | POST platform | 查询被访人 |
| `/admittance/apply/enum/person/cert` `/enum/cause` `/enum/vehicle/cert` `/enum/car/cause` | GET platform | 枚举 |
| `/admittance/area/type/list?type=2` | GET platform | 区域类别 |
| `/admittance/apply/app/area-options?parkId=` | GET platform | 授权区域配置（新） |
| `/admittance/apply/enum/factory/type?flag=0\|1` | GET platform | 区域配置回退 + code→名称映射 |
| `/admittance/apply/equal/check` | POST platform | 重复申请/一致性验证 |
| `/sms/send/getCode/{mobile}`、`/sms/verify` | GET app | 短信发送/校验 |
| `/wechat/visit/checkBlackVisitor` | POST app | 黑名单（data===false 拦截） |
| `/admittance/apply/save/apply`、`/save/car/apply` | POST platform | 提交申请/货车预约 |
| `/admittance/apply/search/Detail/{id}` | GET platform | 二维码详情 |
| `/out/face/cut` | POST algorithm | 人脸检测+裁剪（base64） |
| `/wechat/visit/checkFace` | POST app | 照片上传换 photoId（人脸照与车辆证件照共用） |

响应结构以真实抓包为准（架构 §3）；开发期类型按规格 + 旧组件字段名定义，宽松可选 + 快速失败。

## 4. 公共组件与纯函数

- `components/visitor-steps.tsx`：三步步骤条（①被访信息 ②访客信息 ③提交信息）。
- `components/face-upload.tsx`：拍照/相册 → base64 → `algorithm:/out/face/cut` → `app:/wechat/visit/checkFace` → photoId；失败 toast；上传中 loading。车辆证件照复用（跳过 face/cut，仅 checkFace——与旧版 form-upload-image-single 一致）。
- `components/plate-input.tsx`：车牌输入（省份简称选择 + 号码）。
- `components/sms-code-field.tsx`：手机号+验证码+120s 倒计时（从登录页抽取复用）。
- `features/visitor/id-card.ts`：身份证 18 位格式+校验位校验（纯函数）。
- `features/visitor/area-options.ts`：区域配置加载链（新接口 → localStorage 缓存 → 旧枚举回退；匹配不到厂区时清缓存 toast「授权区域配置不可用，请联系管理员」）+ 提交前剪除失效区域（剪空报错回 info）。

## 5. 错误处理与边界

- 后端失败一律快速失败 toast `message`；提交按钮提交中禁用防重复（「正在验证」/「正在提交」）。
- 访客 OAuth 失败不阻塞表单填写（openId 缺失在提交链路由后端报错暴露）。
- 时间校验：离开>来访（否则 toast 并清离开时间）、跨度≤365 天。
- 身份证校验失败 toast 具体原因（格式/校验位）。
- 删除随行人员/车辆无二次确认（复刻旧行为，功能不增不减）。

## 6. 分支与质量门禁

| 分支 | 内容 |
|---|---|
| `feat/visitor-flow` | flow-store + api.ts + 访客 OAuth + visitor-steps/face-upload/sms-code-field + `/visitor` `/visitor/info` `/visitor/tel` `/visitor/result` |
| `feat/visitor-extras` | `/visitor/area` + 随行人员 2 页 + 车辆 2 页 + plate-input |
| `feat/visitor-truck-code` | `/visitor/truck` + `/visitor/truck/result` + `/visitor/code` |

**每个分支完成定义**：`pnpm check/test/e2e` 全绿 → 独立子 agent 代码评审并修复至无明确问题 → PR → 合并（仓库已开自动删分支）。

## 7. 测试策略

- 单测（vitest）：id-card 校验、area-options 加载链与剪除逻辑、flow-store（含 persist 恢复与 reset）。
- E2E（Playwright，网络层 mock）：
  1. 完整申请主链：OAuth stub → 被访人查询 → 访客信息（含 mock 人脸上传）→ 区域选择 → 随行人员增删 → 短信验证 → 提交成功 → result；断言 save/apply 请求体（isMain 结构、时间秒、区域字段）。
  2. 黑名单拦截 toast 终止。
  3. 区域复核剪空 → toast + 回跳 info。
  4. 货车预约链 + 断言 save/car/apply 请求体。
  5. 二维码页 delFlag=0/1/2 三态渲染。
- 人脸上传链路以接口契约 mock 验证；真实摄像头/人脸检测效果留真机验证清单。

## 8. 明确不做（YAGNI）

- 合肥 4 页变体（含健康码/行程码、携带物品、证件类型 picker）——下期。
- 旧代码已注释停用的字段（司机籍贯/驾驶证号/紧急联系人/车型/颜色、来访类型 personType 选择、携带物品 thing）——personType 固定 3、thing 固定 4，与旧版现行为一致。
- AES 加密（访客接口无加密，已核实）。
- **访客申请记录 2 页（records / record-detail）**：原型中的新增功能（旧版无此页面）；短信验证码复用访客申请既有 `app:/sms/send/getCode/{mobile}`，列表/详情/queryToken 为独立记录查询契约。
