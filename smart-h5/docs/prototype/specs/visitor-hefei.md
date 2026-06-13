# 访客模块 · 合肥流程变体 功能规格

> 来源：旧仓库 `smart-h5/src/views-mobile/pages/visitor/`（只读参考，仅提取功能事实）。
> 合肥流程是访客「入厂申请」三步流程（被访信息 → 访客信息 → 提交信息）的园区变体，园区固定为 **裕同科技合肥园区（parkId=20381）**，免登录使用。
> 三步流程顶部均有步骤条组件（visitor-steps）：①被访信息 ②访客信息 ③提交信息。

## 公共接口（合肥流程涉及）

| # | Method | Path | 模块 | 用途 |
|---|--------|------|------|------|
| 1 | GET | `/common/config/admittance/notice?parkId=` | platform | 园区温馨提示（isNeedNotice=1 时弹窗，富文本） |
| 2 | POST | `/admittance/apply/app/searchReceptionist` | platform | 按姓名+手机号查询被访人（返回 receptionistBadge/Name/Phone） |
| 3 | GET | `/visitor/enum/cause/type` | platform | 来访事由枚举（**合肥专用**，标准版用 `/admittance/apply/enum/cause`） |
| 4 | GET | `/admittance/apply/enum/person/cert` | platform | 证件类型枚举 |
| 5 | GET | `/admittance/apply/enum/carry` | platform | 携带物品枚举 |
| 6 | GET | `/common/config/visitor/health?parkId=20381` | platform | 是否显示健康码/行程码（isHealthCode、isTripCode） |
| 7 | GET | `/sms/send/getCode/{mobile}` | app | 发送短信验证码（倒计时 120s） |
| 8 | GET | `/sms/verify?mobile=&smsCode=` | app | 校验短信验证码 |
| 9 | POST | `/wechat/visit/checkBlackVisitor` | app | 访客黑名单校验（visitorName/certNo/parkId；data===false 即黑名单） |
| 10 | POST | `/admittance/apply/save/apply` | platform | 提交入厂申请 |

页面间数据通过 localStorage 传递：`hostInfor`（被访人）、`visitorInfo`（访客）、`fellowInfo`（随行人员数组）、`visitorPhone`（访客手机号）。

---

## 1. 入厂申请（合肥） `/xuchang/visitor/indexHefei` → `indexHefei.vue`

**用途**：三步流程第 1 步，填写被访者信息并校验被访人存在。

**UI 元素**
- 步骤条（当前第 1 步）。
- 分组标题「被访者信息」。
- 所选园区：只读输入框，固定值「裕同科技合肥园区」。
- 姓名 *：文本输入（字段 `employeeName`），必填提示「请输入被访人姓名」。
- 手机号 *：电话输入（字段 `employeeMobile`），必填提示「请输入被访人手机号」。
- 底部固定主按钮「下一步」。
- 温馨提示弹窗：进入页面即请求接口 1，`isNeedNotice===1` 时弹出富文本内容 + 「知道了」按钮。

**交互/状态**
- 「下一步」：表单校验 → 全屏 loading → 调接口 2（parkId=20381 + 表单）；成功把 receptionistBadge/Name/Phone 存入 `hostInfor` 并写 localStorage，跳 `/xuchang/visitor/visitorInfoHefei`；失败 toast 后端 message（如查无此人）。
- 进入时从 localStorage 回填 `hostInfor`。

**与标准流程（index.vue）的差异**
- 园区固定 parkId=20381/「裕同科技合肥园区」；标准版用配置 PARKID/「裕同科技许昌园区」。
- 查询字段名为 `employeeName`/`employeeMobile`；标准版为 `receptionistName`/`receptionistPhone`。
- **无微信 OAuth 流程**：标准版进入时若 URL 无 `code` 会重定向微信授权并调 `GET /admittance/apply/get/openId` 换 openId；合肥版该逻辑被注释，直接进页面（免登录免微信授权）。
- 温馨提示：标准版仅在拿到 code 后请求；合肥版进入即请求。

---

## 2. 访客信息（合肥） `/xuchang/visitor/visitorInfoHefei` → `visitorInfoHefei.vue`

**用途**：第 2 步，填写访客本人信息、来访时间与随行人员。

**UI 元素（必填区「访客信息（必填）」）**
1. 访客姓名 *：文本输入。
2. 来访事由 *：picker（接口 3 枚举，desc/code）。选中值记为 `cause`。
3. 访客照片 *：单张上传，**带人脸检测**。
4. 证件类型 *：picker（接口 4 枚举）。
5. 证件号码 *：文本输入。
6. 证件照片 *：单张上传，**仅当 cause==5 或 cause==7 时显示**（占位「请上传证件照片」）。
7. 来访单位 *：文本输入。
8. 携带物品 *：picker（接口 5 枚举）。
9. 行程码 *：单张上传，仅当接口 6 返回 `isTripCode==1` 时显示。
10. 健康码 *：单张上传，仅当 `isHealthCode==1` 时显示。
11. 来访时间 *：日期时间选择。
12. 离开时间 *：日期时间选择。

**选填区「其他信息（选填）」**
13. 车牌号：车牌键盘输入（plate-number）。
14. 随行人员：入口行，显示已添加数量「N位」，点击进入随行人员列表。

**交互/状态**
- 离开时间 ≤ 来访时间：toast「离开时间应大于开始时间!」并清空离开时间；修改来访时间也会清空离开时间。
- 点「随行人员」：若未选来访事由 toast「请选择来访事由!」；否则保存表单到 localStorage 并跳 `/xuchang/visitor/addPersonlist?idHefei=true&cause=<cause>`。
- 「下一步」：保存 localStorage → 表单校验 → 跳 `/xuchang/visitor/telHefei`；按钮 loading 态文案「正在验证」。
- 进入时并发拉取枚举接口 3/4/5/6，并从 localStorage 回填。

**与标准流程（visitorInfo.vue）的差异**
- **多出字段**：证件类型（picker）、证件照片（cause=5/7 条件显示）、携带物品（picker）、行程码/健康码（配置开关）、车牌号（选填）。
- **缺少字段**：标准版的「区域类型（厂区卡片选择）」「授权区域（区域 chips + 更多区域）」整块在合肥版不存在。
- 来访事由枚举接口不同（合肥 `/visitor/enum/cause/type`，标准 `/admittance/apply/enum/cause`）。
- 字段顺序不同（合肥：姓名→事由→照片→证件…；标准：姓名→照片→证件号→单位→事由→区域…）。
- 随行人员入口：合肥版强制先选事由并携带 `idHefei=true&cause` query；标准版直接跳转无 query。
- 标准版证件号在第 3 步前有身份证校验依赖（cardid），合肥版本页不做身份证格式校验（随行人员页做）。

---

## 3. 手机验证（合肥） `/xuchang/visitor/telHefei` → `telHefei.vue`

**用途**：第 3 步，访客本人手机号短信验证并提交整个入厂申请。

**UI 元素**
- 步骤条（当前第 3 步）。
- 分组标题「手机验证」。
- 手机号：tel 输入，maxlength=11。
- 验证码：数字输入，maxlength=6（超长自动截断）；行内「获取验证码」按钮（倒计时 120s，调接口 7）。
- 底部固定主按钮「下一步」；提交中变为禁用态「正在提交」。

**交互/校验顺序**
1. 手机号为空 → toast「请输入手机号」；格式不合法 → toast「手机号格式不正确」。
2. 手机号存 localStorage（`visitorPhone`）。
3. 验证码为空 → toast「请输入验证码」。
4. 调接口 8 校验验证码，失败 toast 后端 message。
5. 验证通过后组装提交对象（来自 hostInfor/visitorInfo/fellowInfo）：parkId、receptionistBadge/Name/Phone、unionId、visitorName、visitorPhone、visitorPhoto、certType、certNo、certPic、startTime/endTime（拼 `:00` 秒）、company、personType、cause、carryThing、vehiclePlate（车牌）、remark、fellowVisitorList（每人 certNo/fellowName/fellowPhotoId/certType/certPic/isMain=0/nativePlace）。
6. personType≠2（非贵宾）时先调接口 9 黑名单校验；`data===false` → toast「抱歉，你已被加入访客黑名单，不能进行入厂申请!」终止。
7. 调接口 10 提交（防重复提交）；成功 toast「申请成功」→ 跳 `/xuchang/visitor/result`；失败 toast message 并恢复按钮。
- 进入时回填 localStorage（hostInfor/visitorInfo/fellowInfo/visitorPhone），并清掉 `receptionistPhone` 缓存键。

**与标准流程（tel.vue）的差异**
- 提交字段不同：合肥用 `visitorPhoto/certType/certNo/certPic/carryThing/vehiclePlate/fellowVisitorList`；标准用 `visitorPhotoId/permitArea/permitOldArea/permitFactoryType/areaType/thing/vehicleList/fellowList`。
- 随行人员列表：标准版把**主访客本人**也作为 `isMain:1` 放进 fellowList；合肥版 fellowVisitorList 只含随行人员（isMain 全为 0），主访客信息走顶层字段。
- 合肥版无区域裁剪逻辑（标准版提交前有 pruneVisitorAreaTypesBeforeSubmit 区域校验）、无车辆列表（标准版组装 vehicleList，合肥只传 `vehiclePlate` 字符串）。
- 【旧代码疑似 bug，记录备查】旧 telHefei 第 139 行 `certNo: this.visitorInfo.certType`——证件号被赋成证件类型值；新版应改为 `certNo: visitorInfo.certNo`。
- 【旧代码隐患】合肥 visitorInfoHefei 已去掉 personType 选择，但 telHefei 仍读取 `visitorInfo.personType.value`，依赖历史缓存，存在 undefined 风险；新版需配置化默认值（普通来访=3）。

---

## 4. 添加随行人员（合肥） `/xuchang/visitor/addPersonHefei` → `add-person-hefei.vue`

**用途**：新增或编辑一名随行人员，结果写入 localStorage `fellowInfo` 后返回随行人员列表。

**UI 元素**
1. 访客姓名 *：文本输入（字段 `fellowName`）。
2. 访客照片 *：单张上传，**带人脸检测**（字段 `fellowPhotoId`）。
3. 证件类型 *：picker（接口 4 枚举，字段 `certType`）。
4. 证件号码 *：文本输入（字段 `certNo`，提交时做身份证号校验，非法 toast 校验信息）。
5. 证件照片 *：单张上传，仅当路由 query `cause==5 || cause==7` 时显示（字段 `certPic`）。
- 底部按钮：新增模式「确认添加随行人员」，编辑模式「确认修改随行人员」（圆角描边样式）。

**交互/状态**
- 路由 query：`itemInfo`（编辑回填 JSON）、`itemIndex`、`isEdit`、`cause`。
- 提交：身份证校验 → 表单必填校验 → 新增 push / 编辑 splice 替换 → 写 localStorage → 跳 `/xuchang/visitor/addPersonList?idHefei=true&cause=<cause>`。

**与标准流程（add-person.vue）的差异**
- **多出字段**：证件类型 picker、证件照片（cause=5/7 条件显示）；标准版只有姓名+照片+证件号码 3 项。
- 多调一个接口：证件类型枚举（接口 4）；标准版虽 import 但同样调用——两版均调 enumPersonCertApi，但标准版无证件类型 UI（仅调用未使用）。
- 返回跳转：合肥版回列表页带 `idHefei=true&cause` query；标准版不带 query。

---

## 分析中的不确定点

1. 旧 telHefei `certNo: this.visitorInfo.certType` 为疑似笔误（证件号传了证件类型 code），原型按「应然」用证件号码展示，新版实现需与后端确认。
2. 合肥流程 personType 无 UI 来源（visitorInfoHefei 已注释掉来访类型选择），telHefei 仍读 `personType.value`；推测合肥默认普通来访（3），需确认。
3. 随行人员列表页路由大小写不一致：visitorInfoHefei 跳 `addPersonlist`，add-person-hefei 跳 `addPersonList`（vue-router 默认大小写不敏感所以都能用），新版统一即可。
4. `unionId: hostInfor.code` 在合肥版恒为空（getOpenId 被注释），推测合肥免微信授权、unionId 可空，需后端确认。
5. 健康码/行程码为疫情期配置项（接口 6 控制），原型保留并默认展示，新版是否下线由配置决定。

---

## 补遗（2026-06-11 接口契约核对）

1. **访客照片上传接口缺记**：visitorInfoHefei「访客照片」、add-person-hefei「随行人员照片」均使用 `form-upload-image-single-face` 组件，实际调用：
   - POST `algorithm:/out/face/cut`（人脸检测+裁剪，body: `{imageData: base64}`）
   - POST `app:/wechat/visit/checkFace`（上传/校验，body: `{visitorPhoto: base64}`，返回照片 id）
2. **证件照片（certPic，cause=5/7 条件显示）**：同样走 `app:/wechat/visit/checkFace` 上传。
3. 开发时以真实抓包为准固化请求/响应结构（见架构设计 §3）。
