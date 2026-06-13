# 访客模块功能规格（后半部分 7 页，标准/许昌流程）

> 来源：旧仓库 `smart-h5/src/views-mobile/pages/visitor/`（只读净室分析，仅提取功能事实）。
> 访客全流程免登录（所有接口 `authorization: false`）。流程内数据通过 localStorage 在页面间传递（`visitorInfo` / `carList` / `newAreaTypeList` / `oldAreaTypeList` / `visitorAreaOptions_{parkId}` / `visitorAreaTypeByFactory_{parkId}` / `hostInfor`）。

---

## 1. 添加车辆 `/xuchang/visitor/addCar`

- 旧组件：`visitor/add-car.vue`
- 用途：入厂申请流程中为访客新增/编辑随行车辆，数据存 localStorage `carList`，不直接调提交接口。

### UI 元素
| 字段 | 类型 | 必填 | 校验/默认 |
|---|---|---|---|
| 车牌号 plate | 车牌专用输入（省份简称+号码） | 是 | 车牌键盘输入 |
| 司机姓名 name | 文本输入 | 是 | 默认带入 `visitorInfo.visitorName`；空时提示「请输入司机姓名」 |
| 证件类型 certType | 选择器（枚举） | 是 | 默认「身份证复印件」(code=2)；选项来自车辆证件枚举接口 |
| 证件照片 certImg | 单图上传 | 是 | 「请上传证件照片」 |

> 历史字段（旧代码中已注释停用，原型不实现）：司机籍贯、驾驶证号、紧急联系人、紧急联络人方式、车型、车辆类型、颜色。

- 底部主按钮：新增模式「确认添加车辆」；编辑模式「确认修改车辆」。
- 编辑模式由路由 query 进入：`itemInfo`（JSON 车辆）、`itemIndex`、`isEdit=true`，表单回填。

### 交互/跳转
1. 提交先做表单校验，全部通过后：编辑模式替换 `carList[itemIndex]`，新增模式 push；写回 localStorage。
2. 跳转 `/xuchang/visitor/addCarList`。

### 接口
| 用途 | Method | Path |
|---|---|---|
| 车辆证件类型枚举 | GET | `/admittance/apply/enum/vehicle/cert` |
| 车辆类型枚举（页面加载但 UI 已停用） | GET | `/admittance/apply/enum/vehicle/type` |
| 车辆颜色枚举（页面加载但 UI 已停用） | GET | `/admittance/apply/enum/vehicle/color` |

---

## 2. 车辆列表 `/xuchang/visitor/addCarList`

- 旧组件：`visitor/add-car-list.vue`
- 用途：展示本次预约已添加的车辆（localStorage `carList`），可增删改后回到访客信息页。

### UI 元素
- 顶部计数提示：「已添加车辆（N辆）」。
- 车辆卡片：车牌号（标题）、编辑图标、删除图标、行「司机姓名: xxx」。
- 空态：插画位 + 「暂无车辆信息」「请点击下方按钮添加车辆」。
- 底部双按钮：「新增车辆」（次）+「确 定」（主）。

### 交互/跳转
- 新增车辆 → `/xuchang/visitor/addCar`。
- 编辑 → `/xuchang/visitor/addCar?itemInfo=...&itemIndex=i&isEdit=true`。
- 删除：直接从列表移除并写回 localStorage（旧版无二次确认）。
- 确定 → `/xuchang/visitor/visitorInfo`。

### 接口
无（纯 localStorage 操作）。

### 页面状态
有车辆列表 / 空态。

---

## 3. 添加授权进入区域 `/xuchang/visitor/addAreaType`

- 旧组件：`visitor/addAreaType.vue`
- 用途：勾选本次来访可进入的厂区区域（新/老工厂按 query 区分），可填详细位置，结果存 localStorage 后返回访客信息页。
- 路由 query：`type`（areaFlag，1=新工厂 / 0=老工厂）、`factoryType`（厂区编码）、`parkId`（缺省取 `hostInfor.parkId` 或全局 PARKID）。

### UI 元素
| 元素 | 类型 | 必填 | 说明 |
|---|---|---|---|
| 搜索框 | 文本输入 | 否 | placeholder「搜索授权区域」，按区域名实时模糊过滤（不区分大小写） |
| 全选按钮 | 按钮 | - | 文案在「全选 / 取消全选」间切换（全部选中时显示取消全选） |
| 已选计数 | 文本 | - | 「已选 n/总数」 |
| 区域复选组 | checkbox 列表 | 否 | 区域来自接口；过滤后为空时显示「暂无匹配区域」 |
| 详细位置 | 文本输入 | 否 | 自由填写补充位置 |

- 底部双按钮：「确定」「重置」（重置清空勾选与详细位置）。

### 交互/规则
1. 优先调 area-options 新接口（带 parkId），按 factoryType（或 areaFlag）匹配本厂区的区域列表；接口结果写入 localStorage 缓存，下次先用缓存渲染。
2. 新接口失败且无 factoryType 时回退老枚举接口 `flag=type`。
3. 新接口返回成功但匹配不到厂区配置：清缓存、清列表，toast「授权区域配置不可用，请联系管理员」。
4. 确定：将 `{list:[areaCode...], custom:详细位置}` 按 type 存 `newAreaTypeList`(type=1) / `oldAreaTypeList`(type=0)，并按 factoryType 存 `visitorAreaTypeByFactory_{parkId}`；跳转 `/xuchang/visitor/visitorInfo`。
5. 进入页面回显历史勾选（优先按 factoryType 缓存，其次按 new/old 存储）。

### 接口
| 用途 | Method | Path |
|---|---|---|
| 园区区域配置（新） | GET | `/admittance/apply/app/area-options?parkId=` |
| 厂区区域枚举（兜底） | GET | `/admittance/apply/enum/factory/type?flag=` |

### 页面状态
区域列表 / 搜索无匹配 / 配置不可用（空 + toast）。

---

## 4. 货车预约 `/xuchang/visitor/truck`

- 旧组件：`visitor/truck.vue`
- 用途：货车司机独立预约入厂（不走标准访客多步流程），需短信验证手机号后直接提交。

### UI 元素 —— 分组「货车预约信息」
| 字段 | 类型 | 必填 | 校验/说明 |
|---|---|---|---|
| 车牌号 plate | 车牌专用输入 | 是 | |
| 来访事由 cause | 选择器（枚举） | 是 | 选项来自货车事由枚举接口 |
| 访客姓名 name | 文本输入 | 是 | 空时提示「请输入访客姓名」 |
| 出发地 company | 文本输入 | 是 | 空时提示「请输入出发地」 |
| 预约时间 startTime | 日期时间选择 | 是 | 提交时拼接秒 `:00` |
| 备注 remark | 文本输入 | 否 | placeholder「请填写内托/原材/成品/其他」 |

### 分组「手机验证」
| 字段 | 类型 | 必填 | 校验/说明 |
|---|---|---|---|
| 手机号 visitorPhone | 数字输入 | 是 | 空→toast「请输入手机号」；非法→「手机号格式不正确」 |
| 验证码 smsCode | 数字输入 | 是 | 最长 6 位（超出截断）；空→toast「请输入验证码」；右侧「获取验证码」按钮，点击校验手机号后发短信，120 秒倒计时 |

- 底部主按钮「提交申请」，提交期间 loading「正在提交...」并防重复提交。

### 交互/跳转
1. 提交顺序：手机号/验证码本地校验 → 表单校验 → 调短信校验接口 → 成功后调货车预约保存接口。
2. 保存入参：`{visitorName, visitorPhone, remark, cause(code), startTime+':00', company, vehicleList:[{name, plate}]}`。
3. 成功 toast「申请成功」→ 跳 `/xuchang/visitor/resultTruck`；失败 toast 接口 message。

### 接口
| 用途 | Method | Path |
|---|---|---|
| 货车入厂事由枚举 | GET | `/admittance/apply/enum/car/cause` |
| 发送短信验证码 | GET | `/sms/send/getCode/{mobile}` |
| 校验短信验证码 | GET | `/sms/verify?mobile=&smsCode=`（2026-06-12 核对旧 services/other.js:29 实为 GET+query，此前误记 POST） |
| 货车入厂预约保存 | POST | `/admittance/apply/save/car/apply` |

---

## 5. 提交成功 `/xuchang/visitor/result`

- 旧组件：`visitor/result.vue`
- 用途：标准访客预约提交成功提示页。
- UI：成功插画、主文案「已发送成功，等待被访对象审批」、副文案「审批通过后，我们会以短信或公众号的方式通知您，请注意查看」、文字按钮「再预约一次」。
- 跳转：再预约一次 → `/xuchang/visitor`（访客首页）。
- 接口：无。单一状态。

---

## 6. 提交成功（货车） `/xuchang/visitor/resultTruck`

- 旧组件：`visitor/resultTruck.vue`
- 与第 5 页同构，差异：
  - 主文案「已发送成功，等待系统审批」（货车为系统自动审批）。
  - 再预约一次 → `/xuchang/visitor/truck`。
- 接口：无。单一状态。

---

## 7. 二维码信息 `/xuchang/visitor/code`

- 旧组件：`visitor/code.vue`
- 用途：审批通过后通过短信/公众号链接打开的访客通行二维码页。路由 query：`id`（申请单 id）。

### UI 元素
- 顶部标语「裕同科技许昌园区欢迎您」。
- 二维码卡片（按 `delFlag` 三态）：
  - `delFlag=0` 有效：二维码图（接口返回 base64）、预约码数字 `smsCode`、橙色标签「首次扫码，打印有效」、提示「请截图保存该页面，入园接受检查时可出此凭证，以便核实」。
  - `delFlag=1` 期限内已删除：失效占位图 + 红字「二维码已失效」，下方预约信息卡仍展示。
  - `delFlag=2` 已过期：失效占位图 + 红字「二维码已失效」，预约信息卡不展示。
- 预约信息卡（delFlag 0/1 显示）：园区名 parkName、事由标签 causeDesc、被访人姓名+电话（标注「（被访人）」）、访客姓名+电话（标注「（访客）」）、区域类型 permitFactoryTypeDesc、新工厂（区域 code 映射名称 + permitArea 拼接，逗号分隔，空则隐藏行）、老工厂（同理，permitOldArea）、预约到访时间 startTime、预约离开时间 endTime。
- 使用指引卡（仅 delFlag=0）：三步图示 01 填写预约码 / 02 打印凭条 / 03 粘贴至胸前。
- 页面底部裕同 logo。

### 接口
| 用途 | Method | Path |
|---|---|---|
| 二维码/申请详情 | GET | `/admittance/apply/search/Detail/{id}` |
| 老工厂区域枚举（code→名称映射） | GET | `/admittance/apply/enum/factory/type?flag=0` |
| 新工厂区域枚举（code→名称映射） | GET | `/admittance/apply/enum/factory/type?flag=1` |

### 页面状态
有效（0）/ 已失效保留信息（1）/ 已过期（2）。

---

## 补遗（2026-06-11 接口契约核对）

1. **证件照片上传接口缺记**：add-car 页「证件照片 certImg」使用 `form-upload-image-single` 组件，实际调用 POST `app:/wechat/visit/checkFace`（body: `{visitorPhoto: base64}`，返回 id 作为 certImg），本规格 API 表此前未收录。
2. 开发时以真实抓包为准固化请求/响应结构（见架构设计 §3）。
