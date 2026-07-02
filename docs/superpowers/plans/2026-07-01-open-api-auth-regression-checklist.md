# 开放 API 鉴权框架 —— 集成回归清单（Task 7）

> **背景**：本清单是「开放 API 鉴权框架」实施计划（`docs/superpowers/plans/2026-07-01-open-api-auth.md`）
> Task 7 的交付物。真实测试环境（Nacos + Redis + Oracle 全栈微服务）在开发会话中不可用，
> 因此本任务不产出"已执行通过"的回归结果，而是产出一份**可由具备测试环境权限的人直接照抄执行**
> 的回归清单，覆盖数据库前置、登录回归、client_credentials 换 token、开放端点裁决矩阵、吊销、
> 管理页走查、现有功能回归共 7 大类、合计 24 条用例。
>
> 所有 curl 命令用 `<GATEWAY>` 占位实际网关地址（例如 `http://smart-gateway-host:9990`）；
> 命令中不出现任何真实密码/密钥，均用 `<...>` 占位符标注，执行者需自行替换为测试环境真实值。
>
> **前置代码事实核对**（写清单前已逐一核实，避免凭空编造路径）：
> - **网关路由是数据库驱动的动态路由**（`sys_route_conf` 表 + Redis + Nacos 热更新，见
>   `smart/docs/14-decisions/ADR-003-dynamic-gateway-route.md`），仓库里**没有**静态的
>   `spring.cloud.gateway.routes` 配置可以百分百确认前缀映射。本清单沿用文档
>   （`smart/docs/07-api/api-index.md`）和前端既有调用约定推断的前缀：`/auth/**` → smart-auth，
>   `/admin/**` → smart-upms-biz，`/platform/**` → smart-platform-biz（`smart-ui/src/api/login.js`
>   实际调用 `/auth/oauth/token`、`smart-ui/src/api/admin/client.js` 实际调用
>   `/admin/client/...`，均按此前缀约定拼接，佐证力较强）。**执行者在真正跑 curl 前，
>   务必先去测试环境的 `sys_route_conf` 表（或管理页路由管理功能）核对这三个前缀在当前环境
>   是否确实如此配置**，如有出入以环境实际路由为准，不要机械照抄本清单路径。
> - `/oauth/token` 由 `AuthorizationServerConfig`（`smart/smart-auth/src/main/java/com/tce/smart/auth/config/AuthorizationServerConfig.java`）
>   的 `oauthServer.allowFormAuthenticationForClients()` 配置，client 认证走 **HTTP Basic**（`-u client_id:secret`）。
> - `sys_oauth_client_details` 表的主键就是 `client_id` 字符串本身
>   （`SysOauthClientDetails.java` 第 30 行 `@TableId(value = "client_id", type = IdType.INPUT)`），
>   因此 `OauthClientDetailsController` 的 `GET /client/{id}`、`POST /client/{id}`（删除）
>   路径变量传的都是 `client_id` 字符串（如 `file-receiver-xc`），不是数字自增 ID。
> - `OauthClientDetailsController.java`（`smart/smart-upms/smart-upms-biz/.../controller/OauthClientDetailsController.java`）
>   路由：`@RequestMapping("/client")` + `POST /save`、`GET /page`、`GET /{id}`、
>   `POST /{id}`（删除，`@PreAuthorize sys_client_del`）、`POST /update`、
>   `PUT /secret/{clientId}`（重置，`@PreAuthorize sys_client_edit`）。
> - `SmartTokenEndpoint.java`（`smart/smart-auth/.../endpoint/SmartTokenEndpoint.java`）
>   `@RequestMapping("/token")`，吊销端点为 `@Inner @DeleteMapping("/client/{clientId}")`
>   （即 auth 服务内部路径 `/token/client/{clientId}`，标了 `@Inner`，只能服务间调用，
>   **不经网关暴露给外部**，因此本清单第 5 类"吊销"验证走的是触发路径——管理页删除应用/重置
>   secret——而不是直接 curl 这个内部端点）。
> - `/platform/admittance/apply/page`、`/platform/admittance/apply/repeat/auth` 已在
>   `SmtAdmittanceApplyController.java`（`smart-module/smart-platform/smart-platform-biz/.../controller/admittance/SmtAdmittanceApplyController.java`，
>   `@RequestMapping("/admittance/apply")`）第 125、233 行确认存在（`GET /page`、`POST /repeat/auth`），
>   全类及这两个方法均**未**标注 `@OpenApi`，是 deny-by-default 场景的真实回归目标。
> - 全仓库搜索确认：当前**没有**任何生产代码（非测试代码）标注 `@OpenApi` 的真实业务端点。
>   仅 `OpenApiInterceptorMockMvcTest.java` 里有一个纯测试用 `SampleController`
>   （`@OpenApi("open:test:read")` + `GET /open/test`），不随生产代码部署。
>   因此第 4 类"开放端点矩阵"在照片拉取计划 Task 8 落地真实接口前，**必须**临时在
>   smart-platform 或 smart-app 任一业务服务里手工加一个 `@OpenApi("open:admittance:photo:read")`
>   的测试端点（用完即删，不得合并到 main），或等 Task 8 合并后改用真实接口执行——本清单两种
>   方式都写了对应步骤。
> - `SysOauthClientDetailsServiceImpl.resetSecret` 落库前缀为 `{bcrypt}`
>   （`SecurityConstants` 里的 `BCRYPT` 常量），返回 32 位明文一次性展示。
> - `sys_menu` 菜单改名脚本只改 `NAME` 字段（`客户端管理` → `应用管理`），页面路由本身
>   （`/admin/client`）不变，前端代码未硬编码页面 title（title 由后端菜单表驱动）。

---

## 0. 执行前准备

- [ ] 确认测试环境已启动：Nacos、Redis、Oracle、`smart-gateway`、`smart-auth`、`smart-upms-biz`、
      `smart-platform-biz`（若走真实照片接口路径还需已合并 Task 8 的服务）。
- [ ] 确认已部署的代码版本包含本计划 Task 1-6 的全部提交（`git log --oneline` 核对包含
      `feat(auth): store client_secret with encoder prefix...` 到
      `chore(db): rename client management menu to app management` 区间的提交）。
- [ ] 记录网关地址为 `<GATEWAY>`，后续命令直接替换。
- [ ] 准备一个具备 `sys_client_edit`/`sys_client_del` 权限的管理员账号，用于登录 smart-ui 走管理页操作。

---

## 1. 数据库前置：按序执行三个手工 SQL 脚本

> 脚本目录：`smart-module/database/manual/`。按 `smart-module/database/manual/README.md`
> 约定，使用支持 PL/SQL 匿名块整段执行的工具（SQL Developer / DBeaver / DataGrip），
> **不要**用 SQL*Plus/SQLcl 按分号逐句跑，脚本结尾没有 `/` 分隔符。

### 1.1 执行 `2026-07-01-oauth-client-secret-prefix.sql`

- **前置条件**：已连接测试库，具备 `sys_oauth_client_details` 表 UPDATE 权限。
- **步骤**：
  1. 执行前先跑预览 SQL 确认影响范围：
     ```sql
     SELECT client_id, client_secret FROM sys_oauth_client_details WHERE client_secret NOT LIKE '{%';
     ```
  2. 整段执行 `smart-module/database/manual/2026-07-01-oauth-client-secret-prefix.sql`。
- **预期结果**：`DBMS_OUTPUT` 打印 `Prefixed legacy oauth client secrets with {noop}: N`（N = 步骤 1 预览查到的行数）；
  再次执行预览 SQL 应返回 0 行（所有存量 `client_secret` 均已带 `{noop}` 前缀）。
- **失败排查**：若 N 与预览行数不一致，检查是否有并发写入 `sys_oauth_client_details`；
  若报错 `ORA-00900`，说明工具把脚本当 SQL*Plus 脚本跑了，改用整段执行模式。

### 1.2 执行 `2026-07-01-register-file-receiver-app.sql`（需先替换占位符 + 重置 secret）

- **前置条件**：已从 `smt_park` 表查到许昌园区的 `id`。
  ```sql
  SELECT id, name FROM smt_park WHERE name LIKE '%许昌%';
  ```
- **步骤**：
  1. 打开 `smart-module/database/manual/2026-07-01-register-file-receiver-app.sql`，
     将 `V_ADDITIONAL_INFO` 变量里的 `<许昌园区ID>` 替换为上一步查到的真实数字 ID
     （例如 `'{"allowedParkIds":[<许昌园区ID>]}'` → `'{"allowedParkIds":[42]}'`）。
  2. 整段执行替换后的脚本。
  3. **脚本内置的 `client_secret` 是占位符明文 `CHANGE-ME-ON-DEPLOY`（带 `{noop}` 前缀），
     不可用于任何鉴权验证**，必须立即在管理页走一次"重置 App Secret"（见 6.4）拿到正式
     `{bcrypt}` 编码的 secret，本清单第 3 类的 curl 命令都基于重置后的新 secret。
- **预期结果**：首次执行 `DBMS_OUTPUT` 打印 `应用 file-receiver-xc 注册成功`；
  重复执行打印 `应用 file-receiver-xc 已存在，跳过注册`（脚本幂等）。
  ```sql
  SELECT client_id, scope, authorized_grant_types, access_token_validity, additional_information
  FROM sys_oauth_client_details WHERE client_id = 'file-receiver-xc';
  ```
  应返回 1 行，`scope='open:admittance:photo:read'`、`authorized_grant_types='client_credentials'`、
  `additional_information` 含正确园区 ID 的 JSON。
- **失败排查**：若脚本报错 `占位符<许昌园区ID>未替换`（`ORA-20001`），说明忘了替换占位符，
  这是脚本内置的硬校验，不是故障；重新替换后再执行。若插入后 `additional_information` 不是合法
  JSON（例如误替换成非数字），后续 client_credentials 换 token 时 `AuthorizationServerConfig.tokenEnhancer`
  会原样透传脏数据到 `app_park_ids` claim，不会在此处报错，需回头检查本步骤的 JSON 格式。

### 1.3 执行 `2026-07-01-rename-client-menu-to-app-management.sql`

- **前置条件**：无额外前置，脚本自带存在性判断。
- **步骤**：整段执行 `smart-module/database/manual/2026-07-01-rename-client-menu-to-app-management.sql`。
- **预期结果**：
  - 若库中存在 `SYS_MENU.NAME='客户端管理'` 且 `DEL_FLAG='0'` 的记录，打印
    `已更新 N 条菜单记录（客户端管理 -> 应用管理）`（N 通常为 1）。
  - 若未找到，打印 `未找到，可能已改名或菜单名不同，请人工核对 SYS_MENU 中指向 /admin/client 页面的菜单行`
    ——**此时需要人工去 `SYS_MENU` 表按 `URL`/`COMPONENT` 字段找到指向 `/admin/client` 的那一行，
    确认它的 `NAME` 是否已经是"应用管理"或其它需要人工改的值**，不要把这条输出当成脚本失败。
- **失败排查**：脚本可重复执行、幂等，无破坏性；若第二次执行仍打印"已更新"而不是"未找到"，
  说明存在多条同名菜单记录，需人工核实 `SYS_MENU` 是否有脏数据。

---

## 2. 存量登录回归（验证 `{noop}` 前缀迁移无破坏）

- **前置条件**：1.1 已执行完成；准备一个现有的 smart-ui 管理员账号（用户名/密码），密码用占位符 `<PASSWORD>`。
- **步骤**：
  1. 打开 smart-ui 登录页，用现有账号+密码走一次正常登录（密码模式，前端固定用
     `Authorization: Basic c21hcnQ6c21hcnQ=` 即 `smart:smart` 前端 client，`smart-ui/src/api/login.js`
     硬编码此值），或用等价 curl：
     ```bash
     curl -X POST '<GATEWAY>/auth/oauth/token' \
       -H 'Authorization: Basic c21hcnQ6c21hcnQ=' \
       -H 'TENANT_ID: 1' \
       -d 'grant_type=password' \
       -d 'username=<现有账号用户名>' \
       -d 'password=<PASSWORD>' \
       -d 'scope=server'
     ```
  2. 观察响应。
- **预期结果**：HTTP 200，响应体含 `access_token`、`refresh_token`、`token_type=bearer`。
  smart-ui 登录页应正常跳转进首页，不报"用户名或密码错误"。
- **失败排查**：
  - 若返回 `invalid_grant`/密码错误：检查该用户密码在 `sys_user.password` 里是否本身就没有编码前缀
    （Task 1 Step 3 要求"用户密码走 `SmartUserDetailsService`，若无前缀需评估，不允许悄悄破坏登录"）；
    若发现无前缀，说明用户密码存储格式和 client_secret 是两套机制，需要单独排查
    `SmartUserDetailsService` 的查询 SQL 是否也被这次改动波及（预期不应该，因为 Task 1 只改了
    `CLIENT_FIELDS`，不改用户密码查询字段，但仍需现场确认一次，防止误伤）。
  - 若返回 `unauthorized_client`（Basic 校验失败）：说明 1.1 迁移影响到了前端 client（`smart`）
    自身的 `client_secret` 编码格式，需检查 `sys_oauth_client_details` 里 `client_id='smart'` 那行
    是否也被正确加上了 `{noop}` 前缀。

---

## 3. client_credentials 换 token

- **前置条件**：1.2 已执行且已完成"重置 App Secret"拿到正式明文 secret（记为 `<FILE_RECEIVER_SECRET>`）。
- **步骤**：
  ```bash
  curl -i -X POST '<GATEWAY>/auth/oauth/token' \
    -u 'file-receiver-xc:<FILE_RECEIVER_SECRET>' \
    -d 'grant_type=client_credentials'
  ```
- **预期结果**：HTTP 200；响应体形如：
  ```json
  {"access_token":"...", "token_type":"bearer", "expires_in":43199, "scope":"open:admittance:photo:read", "license":"...", "app_park_ids":[<许昌园区ID>], "jti":"..."}
  ```
  重点核对两点：（a）`scope` 字段等于 `open:admittance:photo:read`；（b）响应体里出现
  `app_park_ids` 且值为数组、包含 1.2 步骤里填入的园区 ID——这是 Task 2 `tokenEnhancer` 增强的
  claim，证明园区绑定生效。记录此 `access_token`，记为 `<CLIENT_TOKEN>`，供第 4 类矩阵使用。
- **失败排查**：
  - `401 invalid_client`：Basic 认证失败，检查 secret 是否是"重置"接口返回的最新明文（旧占位符
    `CHANGE-ME-ON-DEPLOY` 早已在 1.2 步骤里失效）。
  - `400 unauthorized_client` / `invalid_scope`：检查 `sys_oauth_client_details.authorized_grant_types`
    是否含 `client_credentials`（1.2 脚本已写入，若被后续管理页误编辑覆盖需重查）。
  - 200 但响应体没有 `app_park_ids`：说明 `additional_information` 里 `allowedParkIds` 键缺失或
    JSON 格式错误，回到 1.2 用
    `SELECT additional_information FROM sys_oauth_client_details WHERE client_id='file-receiver-xc'`
    核对。

---

## 4. 开放端点裁决矩阵（deny-by-default 核心验证）

> 若照片拉取计划 Task 8 尚未合并：先在 smart-platform-biz（或任一已启动的业务服务）临时加一个
> `@OpenApi("open:admittance:photo:read")` 标注的测试端点（例如复用
> `OpenApiInterceptorMockMvcTest` 里的 `SampleController` 思路，新建一个仅用于本次回归、
> 回归完立刻删除且不提交的 `GET /admittance/apply/__open_api_probe`），部署到测试环境。
> 若 Task 8 已合并：直接使用真实的开放照片接口路径替换下面的 `<OPEN_ENDPOINT>`，
> 并把 scope 换成该接口实际标注的 `@OpenApi` 值。

- **前置条件**：已有 3 类拿到的 `<CLIENT_TOKEN>`（scope=`open:admittance:photo:read`）；
  另需一个普通用户 token（走 2 类的密码模式登录拿到，记为 `<USER_TOKEN>`）。

### 4.1 带匹配 scope 的 client token 调开放端点 → 200

```bash
curl -i -X GET '<GATEWAY>/platform<OPEN_ENDPOINT>' \
  -H 'Authorization: Bearer <CLIENT_TOKEN>'
```
- **预期结果**：HTTP 200，业务响应体正常返回（不是权限报错）。
- **失败排查**：403 说明 `OpenApiInterceptor` 判定 `scopes()` 不含注解要求的 scope，
  检查测试端点 `@OpenApi` 里写的 scope 字符串是否与 token 的 `scope` 完全一致（大小写、`open:`前缀）。

### 4.2 缺 scope 的 client token 调开放端点 → 403

- **步骤**：另注册/使用一个 `scope` 不含 `open:admittance:photo:read` 的测试 client（或临时把
  file-receiver-xc 的 `scope` 字段改成任意不相关值后重新换 token，测完记得改回来），重复 4.1 请求。
- **预期结果**：HTTP 403。
- **失败排查**：若返回 200，说明 `OpenApiInterceptor` 的 scope 校验被绕过，属于阻断级缺陷，
  立刻检查 `OpenApiInterceptor.preHandle` 是否走到了裁决规则第 1 条分支。

### 4.3 普通用户 token 调开放端点 → 403

```bash
curl -i -X GET '<GATEWAY>/platform<OPEN_ENDPOINT>' \
  -H 'Authorization: Bearer <USER_TOKEN>'
```
- **预期结果**：HTTP 403（`OpenApiAuthenticationAdapter.isClientOnly()` 对用户 token 返回 false，
  裁决规则第 1 条要求"必须 isClientOnly()"，用户 token 不满足直接拒绝）。
- **失败排查**：若返回 200，说明 `isClientOnly()` 判定逻辑有误，检查该用户 token 对应的
  `OAuth2Authentication.isClientOnly()` 实际返回值（正常密码模式登录应为 false）。

### 4.4 client token 调 `/platform/admittance/apply/page` → 403（deny-by-default 核心验证）

```bash
curl -i -X GET '<GATEWAY>/platform/admittance/apply/page' \
  -H 'Authorization: Bearer <CLIENT_TOKEN>'
```
- **预期结果**：HTTP 403。`SmtAdmittanceApplyController` 的 `/page` 方法未标 `@OpenApi`，
  按裁决规则第 2 条"handler 不带 `@OpenApi` 且 `isClientOnly()` → 403"应被拒绝。
- **失败排查**：若返回 200 或业务数据，这是 **Codex 阻断项回归**，说明 deny-by-default
  规则失效，必须立即停止发布并排查 `OpenApiInterceptor` 是否正确注册在
  `SmartResourceServerAutoConfiguration` 的拦截器链、`addPathPatterns("/**")` 是否被其它配置覆盖。

### 4.5 client token 调 `/platform/admittance/apply/repeat/auth` → 403（同上，POST 方法）

```bash
curl -i -X POST '<GATEWAY>/platform/admittance/apply/repeat/auth' \
  -H 'Authorization: Bearer <CLIENT_TOKEN>' \
  -H 'Content-Type: application/json' \
  -d '{}'
```
- **预期结果**：HTTP 403（同 4.4 理由；用空 JSON body 即可，只验证鉴权层在业务逻辑执行前拦截，
  不关心业务参数是否合法——若返回的是 400 参数校验错误而非 403，说明鉴权判定被绕过，
  请求已经进入了业务方法，同样按阻断级缺陷处理）。
- **失败排查**：同 4.4。

### 4.6 匿名（无 token）调开放端点 → 403

```bash
curl -i -X GET '<GATEWAY>/platform<OPEN_ENDPOINT>'
```
- **预期结果**：HTTP 401 或 403（无 `Authorization` 头，资源服务器在鉴权链更早阶段即拒绝；
  具体状态码以资源服务器 `ExceptionTranslator` 实际返回为准，只要不是 200/业务数据即视为通过）。

> 测试完成后：若使用的是临时探测端点，立刻从代码中删除并确认未被 commit
> （`git status` 确认工作区干净）；若临时修改过 file-receiver-xc 的 `scope` 字段，
> 记得改回 `open:admittance:photo:read`。

---

## 5. 吊销验证

> `DELETE /token/client/{clientId}` 标了 `@Inner`，不经网关直接暴露，因此验证方式是走
> 管理页触发吊销动作，再用旧 token 请求确认失效。

### 5.1 重置 App Secret 后，旧 token 立即失效

- **前置条件**：已有 3 类换到的 `<CLIENT_TOKEN>`（重置前签发的）。
- **步骤**：
  1. 管理员登录 smart-ui 应用管理页（`/admin/client`），找到 `file-receiver-xc` 行，点击
     「重置 App Secret」，二次确认后记录弹窗里展示的新明文（这一步同时验证 6.4 的弹窗行为）。
  2. 立刻用重置前的旧 `<CLIENT_TOKEN>` 调任意接口验证：
     ```bash
     curl -i -X GET '<GATEWAY>/platform<OPEN_ENDPOINT>' \
       -H 'Authorization: Bearer <CLIENT_TOKEN>'
     ```
- **预期结果**：HTTP 401（token 已被 Redis 移除，资源服务器返回"invalid_token"）。
- **失败排查**：若仍返回 200/403（403 也说明 token 还有效，只是被 deny-by-default 拦了），
  检查 `SysOauthClientDetailsServiceImpl.resetSecret` 是否真的调用了
  `RemoteTokenService.removeTokensByClientId`（Feign 调用异常不会被吞掉，若网络不通会直接抛出，
  此时管理页操作本身应报错而不是"成功但没吊销"——先看管理页重置操作是否报错）；
  再检查 auth 端 `SmartTokenEndpoint.revokeTokensByClientId` 是否正确 cast 到 `RedisTokenStore`
  并执行了 `findTokensByClientId` + 逐个移除。

### 5.2 删除应用后，旧 token 立即失效

- **前置条件**：另建一个一次性测试 client（不要用 file-receiver-xc，避免影响其它用例），
  换出一个 client token 记为 `<DISPOSABLE_TOKEN>`。
- **步骤**：
  1. 管理页删除该测试应用（对应 `POST /client/{clientId}`，`sys_client_del` 权限）。
  2. 用 `<DISPOSABLE_TOKEN>` 调任意接口：
     ```bash
     curl -i -X GET '<GATEWAY>/platform<OPEN_ENDPOINT>' \
       -H 'Authorization: Bearer <DISPOSABLE_TOKEN>'
     ```
- **预期结果**：HTTP 401（同 5.1 理由）；且再次用该 client_id/secret 换 token 应返回
  `invalid_client`（应用记录已被删除）。
- **失败排查**：同 5.1；另需确认 `removeClientDetailsById` 的"删除失败不吊销"分支
  （`SysOauthClientDetailsServiceImplTest` 里已单测覆盖）没有被误触发——即删除本身必须先返回成功。

---

## 6. 管理页走查

- **前置条件**：管理员账号登录 smart-ui，进入应用管理页（`/admin/client`）。

### 6.1 页面/表单文案无「客户端」字样

- **步骤**：走查列表页标题、按钮文案（新增/编辑/删除/重置）、表单字段 label、弹窗文案。
- **预期结果**：全部为「App ID」「App Secret」「应用管理」等措辞，不出现「客户端」二字
  （`openScopes.js` 里允许保留一处代码注释引用 OAuth2 规范术语「客户端」，那是工程注释非用户可见文案，不算违规）。
  菜单标题（浏览器左侧导航/面包屑）应显示「应用管理」（依赖 1.3 的菜单改名脚本已执行）。
- **失败排查**：若菜单标题仍是「客户端管理」，检查 1.3 脚本的 `DBMS_OUTPUT` 输出，
  确认是否命中"未找到"分支（说明该菜单行 `NAME` 字段值和脚本预期的 `客户端管理` 不一致，
  需要人工去 `SYS_MENU` 表核对并手工改）。若列表/表单里仍出现「客户端」字样，是前端代码遗漏，
  需回 `smart-ui/src/views/admin/client/index.vue`、`smart-ui/src/const/crud/admin/client.js` 核查。

### 6.2 scope 多选

- **步骤**：新增或编辑一个应用，scope 字段应为多选下拉，选项来自 `OPEN_SCOPES` 常量
  （当前仅一项：`open:admittance:photo:read` → 「入厂申请照片-读取」）。选中后保存。
- **预期结果**：保存成功；重新打开编辑弹窗，之前选中的 scope 应正确回显（前端把数组
  `join(',')` 存库、`split(',')` 回显，参见 `smart-ui/src/views/admin/client/index.vue`
  的 `handleOpenBefore`/`buildSubmitPayload`）。
- **失败排查**：若回显为空或报错，检查库中 `scope` 字段实际存的分隔符是否是英文逗号
  （`SysOauthClientDetails.scope` 是逗号分隔字符串，非 JSON 数组）。

### 6.3 园区绑定保存后 `additional_information` JSON 正确

- **步骤**：编辑应用，「授权园区」多选框选择 1-2 个园区并保存。
- **预期结果**：数据库层核对：
  ```sql
  SELECT additional_information FROM sys_oauth_client_details WHERE client_id = '<刚编辑的应用ID>';
  ```
  应为合法 JSON，形如 `{"allowedParkIds":[<园区ID1>,<园区ID2>]}`；若该应用原本
  `additional_information` 里还有其它键（当前设计里没有其它键，但若未来新增），
  `mergeAllowedParkIds` 逻辑应保留原有键、只覆盖 `allowedParkIds`。
- **失败排查**：若原 `additional_information` 不是合法 JSON 对象（例如手工造脏数据测试），
  前端应弹出黄色警告提示「原内容不是合法 JSON，仅保留授权园区字段」而不是静默覆盖——
  验证这条提示确实出现，这是 Task 6 明确要求的边界处理。

### 6.4 重置 secret 弹窗一次性展示 + 复制

- **步骤**：点击某应用行的「重置 App Secret」，二次确认弹窗需明确提示"旧 App Secret 立即失效"
  "新密钥只会展示一次"；确认后弹窗展示新明文，尝试点击复制按钮。
- **预期结果**：
  1. 二次确认弹窗文案包含上述两点提示；
  2. 结果弹窗用 `el-alert` 强调"关闭后不可再查看，请立即保存"；
  3. 复制按钮点击后能成功复制明文到剪贴板（`vue-clipboard2`，可粘贴到文本框验证）；
  4. 关闭弹窗后，若不重新触发重置接口，页面上不应有任何地方能再次看到这个明文
     （允许的已知风险：明文短暂残留在组件 `newSecret` 内存变量里直到下次赋值/组件销毁，
     不做强制内存擦除，这是 Task 6 记录的已知遗留，非本次验收阻断项，但走查时应确认
     **UI 层面**关闭弹窗后不再展示）。
- **失败排查**：若关闭弹窗后仍在页面某处（如列表、日志）看到明文，属安全缺陷，立即上报。

---

## 7. 回归面：现有功能不受影响

### 7.1 现有用户登录

- 同第 2 类，另外补充：至少用两个不同角色的现有账号各登录一次，确认均能正常登录、
  拿到含 `user_id`/`username`/`dept_id`/`parkList` 等既有 claim 的 token
  （`AuthorizationServerConfig.tokenEnhancer` 密码模式分支未改动，预期不受影响，仅需现场确认）。

### 7.2 菜单权限

- **步骤**：用上述账号登录 smart-ui 后，检查左侧菜单树渲染正常、按钮级权限
  （如 `sys_client_edit`/`sys_client_del` 控制的「编辑」「删除」「重置 App Secret」按钮）
  按角色正确显示/隐藏。
- **预期结果**：无菜单树加载失败、无按钮权限错乱（本次未新增独立权限码，「重置 App Secret」
  复用了 `sys_client_edit`，需确认拥有该权限的角色能看到重置按钮，不拥有的角色看不到）。

### 7.3 `@Inner` 服务间调用正常

- **步骤**：触发至少一个既有的 `@Inner` 服务间调用链路（例如 upms 调 auth 的
  `POST /token/{token}` 校验、或本次新增的 `DELETE /token/client/{clientId}` 之外的其它
  `@Inner` 端点，如业务模块的 Feign 互调），确认调用成功、返回预期数据。
- **预期结果**：无 `403`/`Inner api can not be called by Outer`一类的内部调用被拒绝报错。
- **失败排查**：注意仓库现状——`SmartSecurityInnerAspect` 的 `FROM_IN` header 校验目前是
  **注释状态**（历史遗留，非本次改动引入），实际服务间鉴权由网关层 `SmartRequestGlobalFilter`
  负责；若怀疑 `@Inner` 调用异常，先排查网关层而非这个切面本身。

---

## 执行结果记录模板

回归执行者请在每条用例后追加：`[PASS/FAIL] 执行时间 执行人 备注`，全部通过后再合并
Task 7 收尾 commit（`test(auth): record open api integration regression results`，
写入实际执行记录，而非本清单模板本身）。
