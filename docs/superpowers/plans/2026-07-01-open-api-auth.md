# 开放 API 鉴权框架实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有 Spring Security OAuth2 栈上交付 App ID/App Secret（client_credentials）开放 API 鉴权：scope 授权、client-only 默认拒绝、园区数据范围绑定、应用管理与吊销。

**Architecture:** 授权侧复用 smart-auth 的授权服务器（client_credentials 已原生支持），为 client token 增强园区绑定 claim；资源侧在 smart-common-security 新增 `@OpenApi` 注解 + 拦截器（scope 校验、client-only deny-by-default、审计日志），对 Spring Security OAuth 私有类型的依赖收敛到单一适配类（迁移缝）；管理侧增强 UPMS 与 admin/client 页面。

**Tech Stack:** Java 8、Spring Boot 2.1、Spring Security OAuth2（RedisTokenStore + RemoteTokenServices/check_token）、MyBatis-Plus、Vue2/Element（smart-ui）。

**依据 spec：** `docs/superpowers/specs/2026-07-01-open-api-auth-design.md`（已三轮 Codex 评审定稿）

## Global Constraints

- 所有用户可见文案一律「App ID / App Secret」，禁止出现「客户端 ID」（spec §3.1 硬性要求）
- scope 命名 `open:<业务域>:<资源>:<动作>`，必须 `open:` 前缀
- `@OpenApi` 接口不进 `PermitAllUrlProperties` 白名单
- 注释一律中文（AGENTS.md）；commit message 英文 Conventional Commits
- 对 `OAuth2Authentication` 等停维栈私有类型的依赖只允许出现在 `OpenApiAuthenticationAdapter` 一个类中（spec §6 迁移缝）
- 数据库脚本放 `smart-module/database/manual/`，含回滚脚本
- 快速失败：鉴权判定失败一律抛异常/403，禁止降级放行

---

### Task 1: 存量 client_secret 编码前缀迁移（DelegatingPasswordEncoder 兼容）

**Files:**
- Create: `smart-module/database/manual/2026-07-01-oauth-client-secret-prefix.sql`
- Create: `smart-module/database/manual/2026-07-01-oauth-client-secret-prefix-rollback.sql`
- Modify: `smart/smart-common/smart-common-core/src/main/java/com/tce/smart/common/core/constant/SecurityConstants.java`（`CLIENT_FIELDS` 处，去掉查询时强制拼接 `{noop}`）

**Interfaces:**
- Produces: 库中 `client_secret` 自带编码前缀（存量 `{noop}`、新增/重置 `{bcrypt}`），`SmartClientDetailsService` 查询结果直接交给 `DelegatingPasswordEncoder` 匹配。

**背景**：现状 `SecurityConstants.CLIENT_FIELDS` 在 SELECT 里对 `client_secret` 拼 `concat('{noop}',client_secret)`，即库中明文、编码方式被硬编码。改为「前缀入库」后才能让 BCrypt 与 noop 并存。

- [ ] **Step 1: 写迁移与回滚 SQL**

```sql
-- 2026-07-01-oauth-client-secret-prefix.sql
-- 目的：client_secret 编码前缀入库，为 BCrypt 共存做准备（存量保持 {noop} 行为不变）
UPDATE sys_oauth_client_details
SET client_secret = CONCAT('{noop}', client_secret)
WHERE client_secret NOT LIKE '{%';
```

```sql
-- 2026-07-01-oauth-client-secret-prefix-rollback.sql
-- 回滚：仅剥离 {noop} 前缀（{bcrypt} 记录不可逆，不处理）
UPDATE sys_oauth_client_details
SET client_secret = SUBSTRING(client_secret, 7)
WHERE client_secret LIKE '{noop}%';
```

- [ ] **Step 2: 修改 `CLIENT_FIELDS`**：去掉 `concat('{noop}', client_secret)`，改为直接 `client_secret`。全仓 grep `{noop}` 确认没有其他拼接点。

- [ ] **Step 3: 确认 auth 侧 PasswordEncoder**：检查 `WebSecurityConfigurer`（smart-auth）当前的 `PasswordEncoder` Bean；若非 `PasswordEncoderFactories.createDelegatingPasswordEncoder()`，改为它（同时确认用户密码存储格式不受影响——用户密码走 `SmartUserDetailsService`，若其密码已带 `{bcrypt}` 前缀则无影响；若无前缀需在本任务中一并评估，**不允许悄悄破坏登录**）。

- [ ] **Step 4: 本地验证**：`mvn -pl smart/smart-auth -am package -DskipTests` 编译通过；在测试库执行迁移 SQL 后，用现有前端 client（如 `smart`）走一次密码模式登录换 token 成功。

- [ ] **Step 5: Commit** `git commit -m "feat(auth): store client_secret with encoder prefix for delegating password encoder"`

---

### Task 2: client token 增强园区绑定 claim

**Files:**
- Modify: `smart/smart-auth/src/main/java/com/tce/smart/auth/config/AuthorizationServerConfig.java`（`tokenEnhancer()`）

**Interfaces:**
- Consumes: `sys_oauth_client_details.additional_information` JSON，约定键 `allowedParkIds`（整数数组），由 Task 6 管理页写入。
- Produces: client_credentials token 的 additionalInformation 含 `app_park_ids`（List<Integer>）与 `license`；经 `/oauth/check_token` 透传给资源服务（Task 3 消费）。

- [ ] **Step 1: 修改 tokenEnhancer**——现状 client_credentials 分支直接 return 不增强，改为：

```java
if (SecurityConstants.CLIENT_CREDENTIALS.equals(authentication.getOAuth2Request().getGrantType())) {
    // 开放应用 token：把应用绑定的园区范围写入 token claim，
    // 资源服务据此做数据范围校验，不信任请求参数
    Map<String, Object> info = new HashMap<>(4);
    ClientDetails client = clientDetailsService.loadClientByClientId(
            authentication.getOAuth2Request().getClientId());
    Object parkIds = client.getAdditionalInformation().get("allowedParkIds");
    if (parkIds != null) {
        info.put("app_park_ids", parkIds);
    }
    info.put("license", SecurityConstants.SMART_LICENSE);
    ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
    return accessToken;
}
```

需要把 Task 1 的 `SmartClientDetailsService` 实例提为可注入字段复用（当前在 `configure(ClientDetailsServiceConfigurer)` 里局部 new，提取为 `@Bean`）。

- [ ] **Step 2: 单测**（`smart/smart-auth/src/test/java/.../AuthorizationServerConfigTest.java`）：mock ClientDetails 带/不带 `allowedParkIds`，断言增强结果含/不含 `app_park_ids`。先写测试跑失败，再实现，再跑过。

- [ ] **Step 3: Commit** `git commit -m "feat(auth): enrich client_credentials tokens with app_park_ids claim"`

---

### Task 3: smart-common-security 开放 API 授权层（@OpenApi + 适配类 + 拦截器）

**Files:**
- Create: `smart/smart-common/smart-common-security/src/main/java/com/tce/smart/common/security/annotation/OpenApi.java`
- Create: `smart/smart-common/smart-common-security/src/main/java/com/tce/smart/common/security/openapi/OpenApiAuthenticationAdapter.java`
- Create: `smart/smart-common/smart-common-security/src/main/java/com/tce/smart/common/security/openapi/OpenApiInterceptor.java`
- Modify: `smart/smart-common/smart-common-security/src/main/java/com/tce/smart/common/security/component/SmartResourceServerAutoConfiguration.java`（注册拦截器）
- Modify: `smart/smart-common/smart-common-security/src/main/java/com/tce/smart/common/security/component/SmartResourceServerConfigurerAdapter.java`（converter 捕获 `app_park_ids` 到扩展）
- Test: `smart/smart-common/smart-common-security/src/test/java/com/tce/smart/common/security/openapi/OpenApiInterceptorTest.java`

**Interfaces:**
- Produces:
  - `@OpenApi(String value)`——value 为必填 scope；
  - `OpenApiAuthenticationAdapter`（唯一允许 import `OAuth2Authentication` 的类）：`boolean isClientOnly(Authentication)`、`String clientId(Authentication)`、`Set<String> scopes(Authentication)`、`List<Integer> appParkIds(Authentication)`；
  - `OpenApiInterceptor implements HandlerInterceptor`：`preHandle` 里统一裁决（规则见下），业务服务零配置生效。

**裁决规则（单一真相，与 spec §3.2 一致）**：
1. handler 带 `@OpenApi` → 必须 `isClientOnly()` 且 scopes 含注解 scope，否则 403；
2. handler 不带 `@OpenApi` 且 `isClientOnly()` → 403（deny-by-default）；
3. 其余（用户 token 调普通接口）→ 放行走既有权限体系；
4. 每次裁决输出结构化审计日志：`open-api-audit clientId={} uri={} result={} costMs={} ip={}`。

- [ ] **Step 1: 写失败单测**——`OpenApiInterceptorTest` 判定矩阵：

```java
// 用 MockHttpServletRequest + 手工构造 HandlerMethod，SecurityContext 注入构造的 Authentication
@Test public void clientToken_withScope_onOpenApi_passes() { ... assertTrue(interceptor.preHandle(req, resp, openApiHandler)); }
@Test public void clientToken_missingScope_onOpenApi_403() { ... assertThrows(AccessDeniedException.class, ...); }
@Test public void userToken_onOpenApi_403() { ... }
@Test public void clientToken_onPlainEndpoint_403() { ... }   // deny-by-default（Codex 阻断项回归）
@Test public void userToken_onPlainEndpoint_passes() { ... }
@Test public void anonymous_onOpenApi_403() { ... }
```

- [ ] **Step 2: 跑测试确认失败**：`mvn -pl smart/smart-common/smart-common-security test -Dtest=OpenApiInterceptorTest`，Expected: 编译失败/断言失败。

- [ ] **Step 3: 实现注解 + 适配类 + 拦截器**（核心代码）：

```java
/** 开放 API 标注：仅允许持对应 scope 的应用（client_credentials）token 访问 */
@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
public @interface OpenApi { String value(); }
```

```java
/** 迁移缝：Spring Security OAuth 私有类型只允许出现在本类（spec §6）*/
public class OpenApiAuthenticationAdapter {
    public boolean isClientOnly(Authentication auth) {
        return auth instanceof OAuth2Authentication && ((OAuth2Authentication) auth).isClientOnly();
    }
    public String clientId(Authentication auth) {
        return ((OAuth2Authentication) auth).getOAuth2Request().getClientId();
    }
    public Set<String> scopes(Authentication auth) {
        return ((OAuth2Authentication) auth).getOAuth2Request().getScope();
    }
    @SuppressWarnings("unchecked")
    public List<Integer> appParkIds(Authentication auth) {
        Object v = ((OAuth2Authentication) auth).getOAuth2Request().getExtensions().get("app_park_ids");
        // 空绑定按空列表处理（边界显式）：调用方据此拒绝一切园区数据
        return v == null ? Collections.emptyList() : (List<Integer>) v;
    }
}
```

拦截器 `preHandle`：非 `HandlerMethod` 直接放行；按裁决规则实现，拒绝时抛 `AccessDeniedException`（走现有异常翻译返回 403），`finally` 打审计日志。

converter 扩展：`SmartResourceServerConfigurerAdapter.configure(ResourceServerSecurityConfigurer)` 中的 `DefaultAccessTokenConverter` 替换为子类，`extractAuthentication` 后把 map 中 `app_park_ids` 放入 `OAuth2Request.getExtensions()`。

- [ ] **Step 4: 注册拦截器**：`SmartResourceServerAutoConfiguration` 增加 `WebMvcConfigurer` Bean，`addInterceptors(new OpenApiInterceptor(adapter)).addPathPatterns("/**")`。

- [ ] **Step 5: 跑测试通过** 同 Step 2 命令，Expected: 全 PASS。

- [ ] **Step 6: Commit** `git commit -m "feat(security): add @OpenApi scope enforcement with client-only deny-by-default"`

---

### Task 4: auth 吊销端点 + UPMS 应用管理增强

**Files:**
- Modify: `smart/smart-auth/src/main/java/com/tce/smart/auth/endpoint/SmartTokenEndpoint.java`（新增按 clientId 批量吊销）
- Modify: `smart/smart-upms/smart-upms-biz/src/main/java/com/tce/smart/admin/controller/OauthClientDetailsController.java`
- Modify: `smart/smart-upms/smart-upms-biz/src/main/java/com/tce/smart/admin/service/impl/SysOauthClientDetailsServiceImpl.java`
- Create: `smart/smart-upms/smart-upms-api/src/main/java/com/tce/smart/admin/api/feign/RemoteTokenService.java`（若已存在则扩展）
- Test: `smart/smart-upms/smart-upms-biz/src/test/java/com/tce/smart/admin/service/impl/SysOauthClientDetailsServiceImplTest.java`

**Interfaces:**
- Produces:
  - auth：`DELETE /token/client/{clientId}`（`@Inner` 内部接口）——`RedisTokenStore.findTokensByClientId(clientId)` 逐个 `removeAccessToken`+`removeRefreshToken`，返回吊销数量；
  - upms：`PUT /client/secret/{clientId}` 重置 secret——生成 32 位随机串，`BCryptPasswordEncoder` 编码入库（`{bcrypt}` 前缀），**明文只在本次响应返回**；重置后 Feign 调 auth 吊销旧 token + 清 `CLIENT_DETAILS_KEY` 缓存；
  - upms：删除应用 `delete` 流程追加：Feign 吊销 + 清缓存（验收：删除后旧 token 立即 401/403）。

- [ ] **Step 1: 失败单测**：重置 secret 返回 32 位明文且库中为 `{bcrypt}` 开头；删除应用时调用了吊销 Feign 与缓存清理（mock 验证）。
- [ ] **Step 2: 实现** auth 吊销端点（注意 `@Inner` 注解 + `SecurityConstants.FROM_IN` 调用）与 upms 两处逻辑。
- [ ] **Step 3: 测试通过后 Commit** `git commit -m "feat(upms): app secret reset with bcrypt and token revocation by client id"`

---

### Task 5: 照片拉取首个应用注册脚本

**Files:**
- Create: `smart-module/database/manual/2026-07-01-register-file-receiver-app.sql`

- [ ] **Step 1: 写注册 SQL**（secret 占位，由运维重置生成正式值）：

```sql
-- 注册许昌 FileReceiver 开放应用（App ID: file-receiver-xc）
-- 部署后必须立即通过管理页「重置 App Secret」生成正式凭证
INSERT INTO sys_oauth_client_details
  (client_id, client_secret, scope, authorized_grant_types,
   access_token_validity, additional_information)
VALUES
  ('file-receiver-xc', '{noop}CHANGE-ME-ON-DEPLOY', 'open:admittance:photo:read',
   'client_credentials', 43200, '{"allowedParkIds":[<许昌园区ID>]}');
```

`<许昌园区ID>` 执行前由运维确认填入（查 `smt_park` 表）。

- [ ] **Step 2: Commit** `git commit -m "chore(db): register file-receiver-xc open api app"`

---

### Task 6: smart-ui 应用管理页增强

**Files:**
- Modify: `smart-ui/src/views/admin/client/index.vue`
- Modify: 对应 API 封装 `smart-ui/src/api/admin/client.js`（如无则创建）

**Interfaces:**
- Consumes: Task 4 的 `PUT /client/secret/{clientId}`。

- [ ] **Step 1: 文案改造**：页面标题「应用管理」，列/表单字段标签 `client_id→App ID`、`client_secret→App Secret`（全局约束，禁止「客户端 ID」字样）。
- [ ] **Step 2: scope 多选**：表单 scope 字段改多选下拉，选项来自前端常量 `OPEN_SCOPES = [{ value: 'open:admittance:photo:read', label: '入厂申请照片-读取' }]`（常量文件 `smart-ui/src/const/openScopes.js`，后续新增 scope 在此登记）。
- [ ] **Step 3: 园区绑定**：表单增加「授权园区」多选（复用现有园区下拉数据源），保存时写入 `additional_information` 的 `allowedParkIds` JSON。
- [ ] **Step 4: 重置 App Secret**：行操作按钮 → 二次确认 → 调重置接口 → 弹窗展示明文（一次性，附「关闭后不可再查看」提示与复制按钮）。
- [ ] **Step 5: 验证** `pnpm lint && pnpm build` 通过；本地起 dev 走一遍新增/编辑/重置流程。
- [ ] **Step 6: Commit** `git commit -m "feat(smart-ui): app management page with App ID/Secret wording, scopes and park binding"`

---

### Task 7: 集成回归

- [ ] **Step 1: 集成用例**（测试环境手工+脚本，逐条留证）：
  1. `POST /oauth/token`（grant_type=client_credentials，file-receiver-xc）→ 200 得 token；
  2. 带 token 调 `@OpenApi` 测试端点（Task 8 于照片计划中落地前，可临时在 upms 建一个 `@OpenApi("open:test:ping")` 端点验证）→ 200；
  3. token 缺 scope → 403；用户 token 调开放端点 → 403；
  4. **client token 调 `/platform/admittance/apply/page`、`/platform/admittance/apply/repeat/auth` → 403**（Codex 阻断项回归）；
  5. 删除应用后旧 token → 401/403；
  6. 现有 smart-ui 登录、菜单权限、`@Inner` 服务间调用全部正常。
- [ ] **Step 2: 回归通过后 Commit 文档记录** `git commit -m "test(auth): record open api integration regression results"`

## Self-Review 结论

- spec §3.1-3.4、§4、§5 均有对应任务（凭证/迁移=T1/T5、claim=T2、授权层=T3、管理与吊销=T4/T6、测试=各任务+T7）；
- 迁移缝约束落在 T3 的适配类边界；App ID 文案落在 T6 与全局约束；
- 类型一致性：`OpenApiAuthenticationAdapter` 方法签名在 T3 定义、照片计划 Task 8 按此消费。
