# 功能规格：根工作台 + 登录模块（5 页）

> 来源：旧仓库 `smart-h5/src`（只读净室分析，只提取功能事实）。
> 旧路由注册：`src/router/pages/login.js`（均挂在 `views-mobile/layout.vue` 下，layout 仅为 `<router-view>` 容器，无任何自身 UI）。
> 园区常量：`conf.js` 中 `APPIP = wx5c0d26056102d41e`（微信公众号 appid）、`PARKID = 5000021`（许昌园区）。

---

## 1. 根工作台 `/`（OLD: views-mobile/index.vue → root/index.html）

- **用途**：项目根路径占位主页。旧页功能极简：仅居中展示一行文字「智慧园区许昌园区项目H5-主页」。
- **UI 元素**：
  - 文案：智慧园区许昌园区项目H5-主页（居中）。
- **交互与校验**：无任何按钮、表单、跳转逻辑。
- **页面状态**：仅 1 个静态状态。
- **跳转关系**：无（实际用户主入口是 `/xuchang/home`，由登录成功后跳入，不属于本页）。
- **接口**：无。

## 2. 登录 `/xuchang/login`（OLD: pages/login/index.vue + components/pwd.vue + components/msgCode.vue → login/index.html）

- **用途**：账号登录页，短信验证码登录 + 账号密码登录双 Tab。
- **UI 元素**：
  - 标题：欢迎来到（弱化色）+ 裕慧家园（强调）。
  - Tab 切换：「短信登录」（默认选中）/「密码登录」。
  - 短信登录面板：
    - 手机号输入框，placeholder「点击输入手机号」。
    - 验证码输入框，placeholder「点击输入验证码」；右侧「获取验证码」按钮（identifying-code 组件，倒计时 120 秒，倒计时文案 `Ns`，发送中有 loading）。
    - 协议勾选（radio）：「我已阅读并同意」+《裕慧家园用户协议及保密承诺》，默认勾选。
    - 「登录」主按钮（圆角）。
  - 密码登录面板：
    - 用户名输入框，placeholder「点击输入用户名」。
    - 密码输入框（type=password），placeholder「点击输入密码」。
    - 同上协议勾选。
    - 「登录」主按钮。
- **交互与校验**：
  - 获取验证码：手机号为空 → 提示「请输入手机号」；格式不符 → 提示「手机号格式不正确」；发送成功 → 提示「发送成功」并进入 120s 倒计时（倒计时内禁点）；失败 → toast 后端 message。
  - 短信登录提交：旧实现未调真实登录接口，直接把手机号当 token 存入 store、刷新 Authorization 头并跳转 `/xuchang/home`（待重写时接 `/auth/mobile/token/sms`，旧代码中该接口仅以注释存在）。
  - 密码登录提交：旧实现 `submit()` 为空函数（功能未完成）；服务层已定义 `loginByPwd` → `POST /auth/oauth/token`，未接线。
- **页面状态**：短信 Tab / 密码 Tab；验证码按钮的 可发送 / 倒计时 / 发送中 三态。
- **跳转关系**：登录成功 → `/xuchang/home`。
- **接口**：
  - `GET app:/sms/send/getCode/{mobile}` 发送短信验证码（services/other.js getPhoneMessage）。
  - `POST auth:/auth/oauth/token` 账号密码登录（services/login.js loginByPwd，已定义未接线）。
  - 注释中预留：`/auth/mobile/token/sms`（短信登录）、`/auth/ocr/token/face`（人脸）、`/auth/token/logout`（登出）。

## 3. 微信授权发起 `/xuchang/login/wechat`（OLD: login_wechat.vue → login/wechat.html）

- **用途**：纯跳板页，无可见 UI。created 时立即重定向微信 OAuth。
- **UI 元素**：旧页为空白 div（原型以「正在跳转微信授权」过渡态呈现，元素不增不减原则下仅为状态可视化）。
- **交互**：进入即跳转 `https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx5c0d26056102d41e&redirect_uri={origin}/#/xuchang/login/wechat/code&response_type=code&scope=snsapi_base&state=123#wechat_redirect`。
- **页面状态**：仅「跳转中」一态。
- **跳转关系**：→ 微信授权 → 回调 `/xuchang/login/wechat/code?code=xxx`。
- **接口**：无后端接口（仅微信 OAuth 跳转）。

## 4. 微信授权回调 `/xuchang/login/wechat/code`（OLD: code_wechat.vue → login/wechat-code.html）

- **用途**：接收微信 OAuth code，静默换 token 登录。无可见 UI（旧模板内容全部被注释）。
- **逻辑流程**（created）：
  1. 从 URL 解析 `code` 参数；无 code 则什么都不做。
  2. 若 store 已有有效 token（非 `invalid_token`）→ 刷新 Authorization 头，直接跳 `/xuchang/home`。
  3. 否则以基础凭证（Basic `c21hcnQ6c21hcnQ=`）调 `POST auth:/wx/public/token`，body `{ code, type: 'F' }`。
  4. 返回含 `access_token` → 存 access_token / expires_in / refresh_token，刷新 Authorization 头，跳 `/xuchang/home`。
  5. 返回 `code === 1` 且 `data` 为「账号未绑定工号，请先绑定」或「员工状态异常」→ 重新发起微信 OAuth，redirect 到 `/xuchang/login/logon_badge`。
  6. 其他失败 → toast `res.message`。
- **页面状态**：登录处理中 / 已有 token 直接放行 / 换 token 成功跳首页 / 未绑定工号转绑定 / 失败 toast。
- **跳转关系**：成功 → `/xuchang/home`；未绑定/员工状态异常 → 微信 OAuth → `/xuchang/login/logon_badge`。
- **接口**：`POST auth:/wx/public/token`（services/login.js getTokenByCode，Basic 授权）。

## 5. 绑定员工号 `/xuchang/login/logon_badge`（OLD: logon_badge.vue → login/logon-badge.html）

- **用途**：微信 openId 与员工工号绑定页（带微信回调 code 进入）。
- **UI 元素**：
  - 标题：欢迎来到 + 裕慧家园。
  - 小节标题：「绑定员工号」。
  - 员工号输入框，placeholder「输入员工号」。
  - 身份证后六位输入框，placeholder「输入身份证后六位」。
  - 「绑定」主按钮（圆角）。
- **交互与校验**：
  - 提交前校验：员工号与身份证后六位均已填写（任一为空则不提交，旧页无显式提示）。
  - 提交时显示全局 loading；调绑定接口，body `{ parkId: 5000021, code(URL中的微信code), badge, lastCertNum }`。
  - `code === 0` 绑定成功 → 重新发起微信 OAuth（redirect 到 `/xuchang/login/wechat/code`）完成登录。
  - 失败 → toast `res.message`，2 秒后重新 OAuth 回到 `/xuchang/login/logon_badge`（刷新 code 重试）。
- **页面状态**：表单态 / 提交 loading 态 / 失败 toast（2s 后重试）。
- **跳转关系**：成功 → OAuth → `/xuchang/login/wechat/code` → `/xuchang/home`；失败 → OAuth → 本页。
- **接口**：`POST app:/wechat/xc/banging/badge`（services/login.js wechatBadge）。

---

## 接口汇总

| 接口 | method | 用途 | 使用页 |
|---|---|---|---|
| `app:/sms/send/getCode/{mobile}` | GET | 发送短信验证码 | 登录-短信 |
| `auth:/auth/oauth/token` | POST | 账号密码登录（旧页未接线） | 登录-密码 |
| `auth:/wx/public/token` | POST | 微信 code 换 token（body: code, type:'F'，Basic 授权） | 微信回调 |
| `app:/wechat/xc/banging/badge` | POST | 绑定微信 openId 与工号（parkId/code/badge/lastCertNum） | 绑定员工号 |
| `app:/wechat/getBadge` | POST | 根据 code 获取工号（services 已定义，本 5 页未调用） | — |

## 不确定点

1. 旧密码登录 `submit()` 是空函数，短信登录也未调真实登录接口（直接把手机号写成 token）——旧功能本身未完成；重写时需补 `/auth/oauth/token` 与 `/auth/mobile/token/sms` 真实接线。
2. 协议《裕慧家园用户协议及保密承诺》在旧页只是文本，无链接与未勾选拦截逻辑。
3. 根路径 `/` 在旧仓库只是占位文字页，真正工作台是 `/xuchang/home`（home 模块，不在本批 5 页内）。
4. 微信 OAuth 的 appid/parkId 为旧 conf.js 硬编码值，重写时应转环境配置。
