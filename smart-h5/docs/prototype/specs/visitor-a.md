# 访客模块（标准/许昌流程）前半部分功能规格（5 页）

> 来源：旧仓库 `smart-h5/src/views-mobile/pages/visitor/`（只读参考，仅提取功能事实）。
> 访客全流程免登录；三步流程公共组件「步骤栏」：①被访信息 → ②访客信息 → ③提交信息。
> 数据在页面间通过 localStorage 传递（hostInfor / visitorInfo / fellowInfo / visitorPhone 等）。

## 1. 入厂申请（被访者信息） `/xuchang/visitor`

- 旧组件：`visitor/index.vue`（+ `components/visitor-steps.vue`、`page3-bottom`）
- 用途：访客填写被访人信息，作为预约流程第 1 步。

### 页面初始化
- 进入页面先做微信 OAuth：URL 无 `code` 参数时跳转微信授权（snsapi_base）回跳本页；有 `code` 时：
  - 调 GET `/common/config/admittance/notice`（parkId）获取温馨提示，`isNeedNotice===1` 时弹「知道了」告知弹窗（内容支持 HTML）。
  - 调 GET `/admittance/apply/get/openId`（code）换取 openId/unionId，存入 hostInfor。
- 园区固定：parkId=许昌园区 ID，parkName=「裕同科技许昌园区」。
- 从 localStorage `hostInfor` 回填已填数据。

### UI 元素
| 字段 | 类型 | 必填 | 校验/说明 |
|---|---|---|---|
| 步骤栏 | 步骤指示 | - | 当前第 1 步「被访信息」 |
| 所选园区 parkName | 文本（只读） | 否 | 固定「裕同科技许昌园区」 |
| 姓名 receptionistName | 输入框 | 是 | 空时提示「请输入被访人姓名」 |
| 手机号 receptionistPhone | 电话输入 | 是 | 空时提示「请输入被访人手机号」；手机号格式 |
| 下一步 | 主按钮 | - | 见交互 |

### 交互与跳转
- 「下一步」：表单校验通过后调 POST `/admittance/apply/app/searchReceptionist`（parkId+姓名+手机号）查询被访人；成功（code=0）将返回的 receptionistBadge/Name/Phone 存 hostInfor → localStorage，跳 `/xuchang/visitor/visitorInfo`；失败 toast 后端 message（如查无此人）。

### 接口
1. GET `/common/config/admittance/notice`（platform）温馨提示
2. GET `/admittance/apply/get/openId`（platform）OAuth code 换 openId
3. POST `/admittance/apply/app/searchReceptionist`（platform）查询被访人

### 状态
默认表单态、温馨提示弹窗态、被访人查询失败 toast。

## 2. 访客信息 `/xuchang/visitor/visitorInfo`

- 旧组件：`visitor/visitorInfo.vue`
- 用途：流程第 2 步，填写访客本人信息、授权区域、来访/离开时间、随行人员入口。

### 页面初始化
- 加载枚举：人员证件类型 GET `/admittance/apply/enum/person/cert`、来访事由 GET `/admittance/apply/enum/cause`、区域类别 GET `/admittance/area/type/list?type=2`。
- 加载授权区域配置：GET `/admittance/apply/app/area-options?parkId=`（带 localStorage 缓存；接口失败时回退旧接口 GET `/admittance/apply/enum/factory/type?flag=1/0` 组装「新工厂/老工厂」两个厂区）。
- 从 localStorage 回填 visitorInfo / hostInfor / fellowInfo / 各厂区已选区域。

### UI 元素
| 字段 | 类型 | 必填 | 校验/说明 |
|---|---|---|---|
| 步骤栏 | 步骤指示 | - | 当前第 2 步「访客信息」 |
| 访客姓名 visitorName | 输入框 | 是 | 空提示「请输入访客姓名」 |
| 访客照片 visitorPhotoId | 人脸照片上传（单张） | 是 | 上传后做人脸检测；POST `/file/upload/face`（multipart） |
| 证件号码 certNo | 输入框 | 是 | 普通来访时按身份证规则校验（格式/校验位） |
| 来访单位 company | 输入框 | 是 | 空提示「请输入来访单位」 |
| 来访事由 cause | 下拉选择 | 是 | 选项来自事由枚举（desc/code） |
| 区域类型 permitFactoryType | 单选卡片组 | 是 | 厂区列表（如新工厂/老工厂），每卡显示厂名+「已选 N」；切厂区清空其他厂区选择 |
| 授权区域 areaType | 多选 chip + 「更多区域（已选 N）」按钮 | 是 | 内联最多显示 inlineAreaLimit（默认 4）个常用区域；更多区域跳 `/xuchang/visitor/addAreaType?type=&factoryType=&parkId=`；为空提示「授权区域不能为空！」 |
| 来访时间 startTime | 日期时间选择 | 是 | 改动后清空离开时间 |
| 离开时间 endTime | 日期时间选择 | 是 | 必须晚于来访时间（否则「离开时间应大于来访时间!」）；跨度 ≤365 天（否则「申请时间最多不允许超过365天!」） |
| 随行人员 | 入口行（显示「N位」或空） | 否 | 点击跳 `/xuchang/visitor/addPersonList` |
| 下一步 | 主按钮 | - | 提交中显示「正在验证」并禁用 |

（旧代码中「来访类型 personType」「携带物品 thing」「车辆信息」已注释停用：personType 固定普通来访 3、thing 固定 4=无。）

### 交互与跳转
- 「下一步」：表单校验 → 授权区域非空 → 时间区间校验 → 身份证校验 → 调 POST `/admittance/apply/equal/check`（被访人 badge + 访客与随行人员列表 + 时间/区域等）做重复申请/一致性验证；通过跳 `/xuchang/visitor/tel`，失败 toast message。
- 所有输入实时存 localStorage（visitorInfo、各厂区区域选择）。

### 接口
1. GET `/admittance/apply/enum/person/cert`（platform）证件类型枚举
2. GET `/admittance/apply/enum/cause`（platform）来访事由枚举
3. GET `/admittance/area/type/list?type=2`（platform）区域类别
4. GET `/admittance/apply/app/area-options`（platform）授权区域配置（新）
5. GET `/admittance/apply/enum/factory/type?flag=0|1`（platform）授权区域（旧回退）
6. POST `/admittance/apply/equal/check`（platform）访客身份/重复验证
7. POST `/file/upload/face`（visitor/file 模块）人脸照片上传（照片组件内）

### 状态
默认表单态、提交中（正在验证）、区域配置不可用提示「授权区域配置不可用，请联系管理员」、各类校验 toast。

## 3. 随行人员列表 `/xuchang/visitor/addPersonList`

- 旧组件：`visitor/add-person-list.vue`
- 用途：查看/管理已添加随行人员（数据仅存 localStorage `fellowInfo`，无接口）。

### UI 元素
- 顶部提示「已添加随行人员（N人）」。
- 列表项：人脸照片缩略图 + 姓名 + 证件号码 + 编辑图标 + 删除图标。
- 空态：「暂无随行人员信息 / 请点击下方按钮添加随行人员」。
- 底部双按钮：「新增随行人员」（次按钮）→ `/xuchang/visitor/addPerson`；「确 定」（主按钮）→ 返回 `/xuchang/visitor/visitorInfo`。

### 交互
- 编辑：携带 `itemInfo/itemIndex/isEdit=true` 跳 `/xuchang/visitor/addPerson`。
- 删除：直接从列表移除并保存 localStorage（无二次确认——旧版行为）。
- （旧页还有合肥分支 idHefei，路由不同，本批不含合肥页。）

### 接口
无（纯本地数据）。

## 4. 添加随行人员 `/xuchang/visitor/addPerson`

- 旧组件：`visitor/add-person.vue`
- 用途：新增或编辑单个随行人员。

### 页面初始化
- 调 GET `/admittance/apply/enum/person/cert` 获取证件类型枚举（旧页加载但表单未实际使用证件类型选择）。
- query 携带 `itemInfo/itemIndex/isEdit` 时进入编辑模式并回填。

### UI 元素
| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| 访客姓名 fellowName | 输入框 | 是 | 空提示「请输入」 |
| 访客照片 fellowPhotoId | 人脸照片上传（单张） | 是 | 人脸检测上传 POST `/file/upload/face` |
| 证件号码 certNo | 输入框 | 是 | 身份证规则校验（格式/校验位错误 toast） |
| 提交按钮 | 主按钮 | - | 新增模式文案「确认添加随行人员」，编辑模式「确认修改随行人员」 |

### 交互与跳转
- 提交：身份证校验 → 表单校验 → 新增 push / 编辑替换到 fellowInfo → 存 localStorage → 跳回 `/xuchang/visitor/addPersonList`。

### 接口
1. GET `/admittance/apply/enum/person/cert`（platform）
2. POST `/file/upload/face`（照片组件内）

## 5. 手机验证（提交信息） `/xuchang/visitor/tel`

- 旧组件：`visitor/tel.vue`（+ `identifying-code` 验证码组件）
- 用途：流程第 3 步，访客本人手机号短信验证并最终提交入厂申请。

### UI 元素
| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| 步骤栏 | 步骤指示 | - | 当前第 3 步「提交信息」 |
| 手机号 visitorPhone | tel 输入，maxlength=11 | 是 | 空「请输入手机号」；格式「手机号格式不正确」 |
| 验证码 smsCode | 数字输入，maxlength=6 | 是 | 空「请输入验证码」；超 6 位截断 |
| 获取验证码 | 行内按钮（输入框右侧） | - | 点击校验手机号后发送 GET `/sms/send/getCode/{mobile}`，成功 toast「发送成功」并 120s 倒计时（「120s」递减），期间禁用 |
| 下一步 | 主按钮 | - | 提交中文案「正在提交」并禁用 |

### 交互与提交链路
1. 「下一步」→ 本地校验手机号/验证码 → GET `/sms/verify?mobile=&smsCode=` 校验验证码，失败 toast。
2. 验证通过后，重新拉取授权区域配置（GET `/admittance/apply/app/area-options`，失败回退缓存/旧接口）剪除已失效区域；若剪完为空 toast「请选择授权区域」并跳回 visitorInfo。
3. 非贵宾（personType≠2）先调 POST `/wechat/visit/checkBlackVisitor`（app 模块）黑名单校验，`data===false` 时 toast「抱歉，你已被加入访客黑名单，不能进行入厂申请!」并终止。
4. 组装完整申请对象（被访人信息、访客信息、unionId、起止时间补 `:00` 秒、授权区域、事由、随行人员列表 fellowList——主访客 isMain=1 + 随行 isMain=0、车辆列表 vehicleList——许昌流程当前为空）调 POST `/admittance/apply/save/apply`。
5. 成功 toast「申请成功」并跳 `/xuchang/visitor/result`；失败 toast message。

### 接口
1. GET `/sms/send/getCode/{mobile}`（app）发送短信验证码
2. GET `/sms/verify`（app）校验短信验证码
3. GET `/admittance/apply/app/area-options`（platform）提交前区域复核
4. GET `/admittance/apply/enum/factory/type`（platform）区域回退
5. POST `/wechat/visit/checkBlackVisitor`（app）黑名单校验
6. POST `/admittance/apply/save/apply`（platform）提交入厂申请

### 状态
默认态、验证码倒计时态、提交中（正在提交）、黑名单拦截 toast、区域失效回跳、申请成功跳转。

---

## 补遗（2026-06-11 接口契约核对）

1. **访客照片上传接口记录有误**：本规格此前记录的 `POST /file/upload/face`（visitor 模块）实际是旧仓库 `services/uploadImage.js` 中的死代码——其调用方 `components/tce-form/supplement/form-img.vue` 未在任何页面注册使用，且 `visitor` 模块未在 `conf.js PROXYS` 中注册（调用会直接抛错）。
2. **实际生效的照片上传链路**（`form-upload-image-single-face.vue`，用于 visitorInfo 访客照片、add-person 随行人员照片）：
   - POST `algorithm:/out/face/cut`（人脸检测+裁剪，body: `{imageData: base64}`，拍照组件 uploadServer）
   - POST `app:/wechat/visit/checkFace`（上传/校验，body: `{visitorPhoto: base64}`，返回照片 id 作为 visitorPhotoId/fellowPhotoId）
3. 开发时以真实抓包为准固化请求/响应结构（见架构设计 §3）。
