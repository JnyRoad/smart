# 认证（Authentication）

## 协议

OAuth 2.0（基于 Spring Security OAuth 2.3.4.RELEASE，已 EOL，但功能稳定）。

## 授权服务器配置

位于 smart-auth 模块的 `AuthorizationServerConfig`：

- **Token Store**：`RedisTokenStore`，前缀 `smart_oauth:`
- **Token Enhancer**：自定义，向 access_token 注入以下业务字段
  - `user_id` — 用户 ID
  - `username` — 用户名
  - `dept_id` — 当前部门
  - `license` — 业务许可标识
  - `parkList` — **该用户所属的园区 ID 列表**（多园区核心）
  - `isStrongPwd` — 是否已设置强密码
  - `salaryTypeName` — 工资类型（与考勤/工资模块联动，语义待业务方确认）
- **Refresh Token**：不重复使用（每次刷新换新）
- **Token 过期时间**：由 `sys_oauth_client_details.access_token_validity` / `refresh_token_validity` 控制

## 支持的 grant_type

| grant_type | 端点 | 适用 |
|-----------|------|------|
| `password` | `/oauth/token` | Web 后台、原生 App |
| `authorization_code` | `/oauth/authorize` + `/oauth/token` | 三方接入 |
| `client_credentials` | `/oauth/token` | 服务间调用（少用，多用 @Inner） |
| `refresh_token` | `/oauth/token` | Token 续期 |
| 短信码登录 | `/mobile/token/sms` | 移动端 |
| 社交登录 | `/mobile/token/social` | 微信/QQ/YHT 绑定后登录 |
| 微信公众号 | `/wx/public/token` | 公众号场景 |
| 人脸识别 | `/ocr/token/...` | 园区门禁/访客 |
| 友互通登录 | `/yht/token` | 集团内部协同平台 |

## 资源服务器（业务侧 Token 验证）

`smart-common-security` 中的 `ResourceServerConfigurerAdapter` 提供：

- 自动从请求头解析 Bearer Token → 反序列化为 `OAuth2Authentication`；
- 注入 `SecurityContextHolder`，业务代码可通过 `@AuthenticationPrincipal` 或 `SecurityUtils` 获取当前用户；
- 内部调用通过 `from=Y` 请求头 + `@Inner` 注解放行。

## 登录流程（账密示例）

```
1. 终端 POST /auth/oauth/token
   Headers: Authorization: Basic base64(client_id:client_secret)
   Body: grant_type=password
         &username=xxx
         &password=<RSA 公钥加密后的密文>
         &code=<验证码>&randomStr=<验证码 key>

2. Gateway:
   - ValidateCodeGatewayFilter: 用 randomStr 取 Redis 缓存的验证码比对
   - PasswordDecoderFilter: 用私钥解密 password
   - 转发到 smart-auth

3. smart-auth:
   - DaoAuthenticationProvider 校验用户名/密码（MD5 + salt）
   - 触发 TokenEnhancer → 注入业务字段
   - 写 Redis → 返回 access_token + refresh_token

4. 终端后续请求:
   Authorization: Bearer <access_token>
```

## 密码安全

- 存储：`MD5(password + salt)`（salt 随用户生成）
- 传输：前端 RSA 公钥加密 → 网关私钥解密（PasswordDecoderFilter）
- 强度：登录后若 `is_strong_pwd=0`，前端引导调用 `PUT /user/password/update` 强制改强密码

## 多园区与 Token

`parkList` 字段是多园区支持的关键。业务子系统从 Token 解析 `parkList` 后，对查询/写操作按 `park_id ∈ parkList` 进行数据过滤。**中台本身不强制做园区行级数据隔离**，依赖业务方落实。

## 已知问题

- Spring Security OAuth 2.3.x 已停止维护，长期需迁移到 Spring Authorization Server（Spring Boot 3.x）。
- XStream 1.4.14 用于 Spring Security OAuth 内部，存在反序列化 CVE，应跟踪官方升级或自行排除。
