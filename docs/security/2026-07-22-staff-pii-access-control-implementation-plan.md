# 员工隐私接口分层与 Nacos 精确收口实施计划

> **给执行型 Agent：** 必须使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 逐任务执行。本计划使用复选框追踪进度。

**目标：** 在不打断当前 Smart UI、Smart H5、Smart App、UPMS 与内部任务的前提下，移除外网匿名员工隐私接口，并将 Nacos `yuto_prod / dev` 的业务通配白名单收口为经验证的精确路径。

**架构：** Platform 将员工查询拆成“外部最小 DTO”“当前用户本人接口”“内部用途 DTO”三类。外部请求需要 OAuth 用户令牌和权限/归属校验；内部请求需要服务令牌、`FROM_IN`、`@Inner`、Gateway 路由拒绝和网络隔离。所有调用方先迁移到新契约，再按 Data ID 灰度收紧 Nacos。

**技术栈：** Java 8、Spring Boot 2.1、Spring Security OAuth2、OpenFeign、MyBatis-Plus、Maven、Vue 2、Next.js 16、React 19、TypeScript、Vitest、Playwright、Nacos。

## 全局约束

- 生产配置范围固定为 Nacos `yuto_prod / dev`；不写入、上传或打印任何生产秘密。
- `smart-h5-vue2` 已下线：禁止修改、构建、回归或纳入发布清单。
- 新 Controller 不得返回 `SmtStaff`、`SmtStaffDTO`、`SmtDormitoryStaff` 或任意持久化实体。
- 身份证、手机号、住址、邮箱、微信号、人脸、证件图片禁止进入外部 DTO、日志、测试 fixture 和文档示例。
- 内部接口不加入 `security.oauth2.client.ignore-urls`；`FROM_IN` 不是身份凭据，必须与服务令牌和网络隔离联合使用。
- 任何 Nacos 收口前必须完成调用方迁移、灰度探针、网关日志核验和历史版本备份。
- 任何回滚不得恢复 `/staff/**`、`/articlesrelease/**`、`/api/**` 或 `/**` 匿名开放。

---

## 文件结构与依赖顺序

| 位置 | 责任 | 依赖任务 |
|---|---|---|
| `smart-module/smart-platform/smart-platform-api/.../dto/resp/` | 外部最小 DTO、本人 DTO、内部用途 DTO | 2 |
| `smart-module/smart-platform/smart-platform-biz/.../controller/` | 外部授权、本人归属、内部接口路由 | 2、3、4 |
| `smart-module/smart-platform/smart-platform-biz/.../service/` | 员工资料投影、园区/房间/单据归属校验 | 2、3、4 |
| `smart/smart-common/smart-common-security/` | 服务令牌 Feign、`@Inner` 强制模式与拒绝测试 | 1、5 |
| `smart-module/smart-app/`、`smart/smart-upms/` | 迁移旧 Feign 员工查询及日志脱敏 | 5 |
| `smart-ui/`、`smart-h5/` | 迁移当前前端调用与回归测试 | 3、4 |
| `docker/nacos/config/dev/` | 本地 Nacos 安全基线，生产发布的可审计参照 | 6、7 |
| `docs/security/` | Nacos 清单、灰度记录、验收与回滚材料 | 1、6、7 |

任务 1 至 5 先形成兼容版；任务 6、7 只能在兼容版部署并通过灰度验证后执行。任务 8 是最终发布门禁。

### Task 1：建立配置基线、禁止规则检查与生产发布清单

**文件：**

- Create: `scripts/security/check-nacos-ignore-urls.mjs`
- Create: `scripts/security/check-nacos-ignore-urls.test.mjs`
- Create: `docs/security/2026-07-22-yuto-prod-dev-nacos-access-control-rollout.md`
- Modify: `package.json`（仅添加根级 `check:nacos-access-control` 脚本；若根目录没有 `package.json`，改为 `scripts/README.md` 记录命令）

**消耗：** `docker/nacos/config/dev/*.yml` 中的 `security.oauth2.client.ignore-urls`。

**产出：** 可重复运行的通配规则检测器、Data ID 变更清单和生产人工发布模板。

- [ ] **步骤 1：写出失败测试，定义禁止的匿名规则。**

```js
import assert from 'node:assert/strict'
import { findForbiddenIgnoreUrls } from './check-nacos-ignore-urls.mjs'

assert.deepEqual(
  findForbiddenIgnoreUrls(['/**', '/staff/**', '/api/**', '/actuator/**']),
  ['/**', '/staff/**', '/api/**'],
)
assert.deepEqual(findForbiddenIgnoreUrls(['/actuator/**', '/v2/api-docs']), [])
```

- [ ] **步骤 2：运行失败测试。**

Run: `node scripts/security/check-nacos-ignore-urls.test.mjs`  
Expected: `ERR_MODULE_NOT_FOUND`，因为检测器尚不存在。

- [ ] **步骤 3：实现检测器。**

导出 `findForbiddenIgnoreUrls(urls)` 和 `scanConfigDirectory(directory)`；前者只接受字符串数组，后者读取 YAML 文本中的 `ignore-urls` 列表。禁止项精确为 `/**`、`/staff/**`、`/articlesrelease/**`、`/api/**`。输出必须只包含文件名、Data ID、行号和路径，不读取或打印 OAuth 密钥、数据源或其他配置值。

```js
export const FORBIDDEN_ANONYMOUS_PATTERNS = new Set([
  '/**', '/staff/**', '/articlesrelease/**', '/api/**',
])

export function findForbiddenIgnoreUrls(urls) {
  return urls.filter((url) => FORBIDDEN_ANONYMOUS_PATTERNS.has(url))
}
```

- [ ] **步骤 4：运行通过测试和本地配置扫描。**

Run: `node scripts/security/check-nacos-ignore-urls.test.mjs`  
Expected: exit `0`。

Run: `node scripts/security/check-nacos-ignore-urls.mjs docker/nacos/config/dev`  
Expected: 对当前风险配置非零退出，并列出 Platform、UPMS、data、algorithm、push、dispatcher、schedule、bridge 与 bridge-isc 的命中；不得输出秘密。

- [ ] **步骤 5：编写 Nacos 发布清单。**

创建发布材料，逐项列出：

| Data ID | 已验证风险 | 收口前置条件 | 灰度探针 |
|---|---|---|---|
| `smart-platform.yml` | `/staff/**`、`/articlesrelease/**` | 任务 2、3、4、5 完成；仓外调用方为零或完成迁移 | 无 Token 访问旧路径 401/403；当前 H5、UI、App 通过 |
| `smart-upms-biz.yml` | `/api/**` | Open API App scope 核验完成 | 无 App 身份访问 401/403；合法 App token 成功 |
| `smart-data.yml` | `/**` | Controller 清单逐条分类完成 | 内部 Feign 成功；外部直连拒绝 |
| `smart-algorithm.yml` | `/**` | 人脸/OCR 调用方完成服务令牌迁移 | 内部算法调用成功；外部直连拒绝 |
| `smart-push.yml` | `/**` | 推送回调签名与调用方清单完成 | 签名回调成功；无签名拒绝 |
| `smart-dispatcher.yml` | `/**` | 所有调度 Feign 标识和令牌验证完成 | 定时任务和 Feign 成功 |
| `smart-schedule.yml` | 本地基线为 `/**`，生产状态需复核 | Nacos 读取核验与任务清单完成 | 定时任务无失败 |
| `smart-bridge*.yml`、`smart-bridge-isc*.yml` | `/**` | 每个设备/厂商回调签名和来源确认 | 合法设备回调成功；未签名拒绝 |

每行必须包含：发布前 MD5、Nacos 历史版本号、执行人、开始/结束时间、灰度实例、探针结果和回滚版本。生产内容由人工从 Nacos 导出填入，不能提交真实配置或凭据。

- [ ] **步骤 6：提交。**

```bash
git add scripts/security docs/security/2026-07-22-yuto-prod-dev-nacos-access-control-rollout.md package.json scripts/README.md
git commit -m "test(security): add nacos anonymous route guard"
```

### Task 2：定义最小员工 DTO 并移除外部实体响应

**文件：**

- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/StaffLookupRespDTO.java`
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/StaffSelfCheckInProfileRespDTO.java`
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/InternalStaffAccountRespDTO.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/controller/SmtStaffControllerPrivacyContractTest.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtStaffController.java:151-319`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/SmtStaffService.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtStaffServiceImpl.java:955-961`

**消耗：** 当前 `SmtStaff` 数据和认证主体。  
**产出：** 外部查询不再返回实体；新的 DTO 契约可被 UI、H5 和内部服务分别消费。

- [ ] **步骤 1：写失败的序列化与路由测试。**

```java
@Test
public void staffLookupDtoDoesNotExposeSensitiveProperties() {
    Set<String> names = Arrays.stream(StaffLookupRespDTO.class.getDeclaredFields())
        .map(Field::getName).collect(Collectors.toSet());
    assertEquals(new HashSet<>(Arrays.asList("staffId", "badge", "name", "departmentName")), names);
}

@Test
public void legacyBadgeHandlersAreNotPublicApiHandlers() throws Exception {
    assertThrows(NoSuchMethodException.class,
        () -> SmtStaffController.class.getMethod("getByBadge", String.class));
    assertThrows(NoSuchMethodException.class,
        () -> SmtStaffController.class.getMethod("getOneByBadge", String.class));
}
```

- [ ] **步骤 2：运行失败测试。**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am -Dtest=SmtStaffControllerPrivacyContractTest test`  
Expected: 编译失败，因为 DTO 和测试目标尚不存在。

- [ ] **步骤 3：实现三个 DTO 与投影方法。**

`StaffLookupRespDTO` 只包含 `Long staffId`、`String badge`、`String name`、`String departmentName`。`StaffSelfCheckInProfileRespDTO` 只包含 `String name`、`Boolean profileComplete`、`String maskedCertNo`；不得携带完整证件号、住址、生日或有效期。`InternalStaffAccountRespDTO` 只包含 `Long staffId`、`String badge`、`String name`、`Integer status`；其他内部业务字段必须在 Task 5 按用途单独增加，不能扩充该 DTO。

在 `SmtStaffService` 增加下列方法，并在 `SmtStaffServiceImpl` 内从实体显式赋值：

```java
List<StaffLookupRespDTO> searchStaffForAdmin(String badge, List<Integer> parkIds);
StaffSelfCheckInProfileRespDTO getCheckInProfileForBadge(String badge);
InternalStaffAccountRespDTO getInternalAccountByBadge(String badge);
```

`searchStaffForAdmin` 必须限制 20 条、忽略空工号查询、按认证主体园区过滤；不得调用或暴露旧的 `getSimpleSttaffByBadge` 返回值。

- [ ] **步骤 4：替换 Controller 路由。**

新增：

```java
@GetMapping("/lookup")
@PreAuthorize("@pms.hasPermission('platform_staff_lookup')")
public Result<List<StaffLookupRespDTO>> lookup(@RequestParam String badge) {
    return success(smtStaffService.searchStaffForAdmin(badge, SecurityUtils.getUser().getParkIdList()));
}

@GetMapping("/me/check-in-profile")
public Result<StaffSelfCheckInProfileRespDTO> myCheckInProfile() {
    return success(smtStaffService.getCheckInProfileForBadge(SecurityUtils.getUser().getUsername()));
}
```

从 `SmtStaffController` 删除三个旧公开 `@GetMapping`：`/simple/badge`、`/define/badge`、`/simple/get/badge`。内部替代接口在 Task 5 创建，不能复用这些旧路径。

- [ ] **步骤 5：运行通过测试。**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am -Dtest=SmtStaffControllerPrivacyContractTest test`  
Expected: `BUILD SUCCESS`。

- [ ] **步骤 6：提交。**

```bash
git add smart-module/smart-platform/smart-platform-api smart-module/smart-platform/smart-platform-biz
git commit -m "fix(platform): split public staff lookup contracts"
```

### Task 3：将当前 H5 入住与门锁改为认证主体语义

**文件：**

- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/req/SelfCheckInReqDTO.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtDormitoryRoomServiceImplSelfCheckInTest.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/controller/SmtDormitoryStaffControllerSelfLockTest.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtDormitoryRoomController.java:298-300`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtDormitoryStaffController.java:408-420`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtDormitoryRoomServiceImpl.java:272-477`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtDormitoryStaffServiceImpl.java:1530-1566`
- Modify: `smart-h5/src/features/dorm-services/api.ts:164-181`
- Modify: `smart-h5/src/features/dorm/api.ts:28-58`
- Modify: `smart-h5/src/features/dorm-services/check-in-rules.ts`
- Modify: `smart-h5/src/app/check-in/page.tsx`
- Modify: `smart-h5/src/app/check-in/detail/page.tsx`
- Modify: `smart-h5/src/app/dorm/lock/page.tsx`
- Modify: `smart-h5/src/app/dorm/get-code/page.tsx`
- Modify: `smart-h5/e2e/check-in.spec.ts`
- Create: `smart-h5/e2e/staff-access-control.spec.ts`

**消耗：** Task 2 的本人资料 DTO、OAuth 用户名、当前入住和门锁服务。  
**产出：** H5 不再发送或读取任意工号的员工身份资料和动态码。

- [ ] **步骤 1：写失败的后端归属测试。**

```java
@Test
public void selfCheckInUsesAuthenticatedBadgeAndStoredIdentity() {
    SelfCheckInReqDTO request = new SelfCheckInReqDTO();
    request.setParkId(1);
    request.setRoomType("A");
    service.autoAllotForAuthenticatedStaff("real-badge", request, bedService);
    verify(smtStaffService).getActiveStaffByBadge("real-badge");
}

@Test
public void lockCodeUsesOnlyAuthenticatedUser() {
    assertThrows(AccessDeniedException.class,
        () -> service.getPwdForAuthenticatedStaff(null));
}
```

- [ ] **步骤 2：运行失败测试。**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am -Dtest=SmtDormitoryRoomServiceImplSelfCheckInTest,SmtDormitoryStaffControllerSelfLockTest test`  
Expected: 编译失败，因为本人入口尚不存在。

- [ ] **步骤 3：实现本人入住请求和服务端身份覆盖。**

`SelfCheckInReqDTO` 只保留宿舍选择所需字段：`parkId`、`dormitoryId`、`floorId`、`roomId`、`bedId`、`roomType`。禁止 `badge`、`certno`、`name`、`sex`、`nation`、`address`、证件有效期字段。

新增控制器入口：

```java
@PostMapping("/self/autoallot")
public Result<List<DormitoryQuickStaffRespDTO>> autoAllotForCurrentUser(
        @RequestBody @Valid SelfCheckInReqDTO request) {
    return success(smtDormitoryRoomService.autoAllotForAuthenticatedStaff(
            SecurityUtils.getUser().getUsername(), request, smtDormitoryBedService));
}
```

在 `SmtStaffService` 增加 `SmtStaff getActiveStaffByBadge(String badge)`；`autoAllotForAuthenticatedStaff` 只能调用该方法，以认证工号查询在职员工，并从数据库填充姓名、性别、证件关联和园区资料。原 `/autoallot` 保留给已确认的后台/临时人员流程，但必须增加管理权限；H5 不再调用它。临时人员流程的身份证采集必须由独立、认证、审计的录入接口处理，不能借员工按工号查询接口回填。

- [ ] **步骤 4：实现本人门锁端点。**

新增 `GET /dormitory/staff/me/pwd`、`POST /dormitory/staff/me/lock/pwd` 和 `POST /dormitory/staff/me/pwd`。三者均从 `SecurityUtils.getUser().getUsername()` 获取工号，调用既有服务方法前校验当前用户在住、房间和园区归属。删除对外 `badge` 参数版本的 `/get/pwd`、`/update/lock/pwd`、`/update/pwd` 映射；保留内部管理版本时改入 `/internal/dormitory/staff/...` 并在 Task 5 使用内部认证。

在 `SmtDormitoryStaffService` 增加 `String getPwdForAuthenticatedStaff(String badge)`；该方法对空工号或不存在的在住关系抛出 `AccessDeniedException`，通过后才调用既有 `getPwdByBadge`。更新和刷新门锁密码的本人方法遵循同一归属检查。

- [ ] **步骤 5：迁移当前 H5。**

将 `getStaffIdentity(badge)` 改为无参数 `getMyCheckInProfile()`，请求 `/staff/me/check-in-profile`；将 `submitCheckIn` 改请求 `/dormitory/room/self/autoallot`，提交 `SelfCheckInReqDTO` 字段。将 `getLockPwd`、`updateLockPwd`、`refreshLockPwd` 改为无 `badge` 参数的本人路由。更新 `check-in-rules.ts`，不得把 `certno`、`homeAddress` 等字段映射进提交体。

- [ ] **步骤 6：补当前 H5 自动化测试。**

在 `staff-access-control.spec.ts` 拦截并断言：

```ts
await page.route('**/platform/staff/define/badge*', (route) => route.abort())
await page.route('**/platform/staff/me/check-in-profile', (route) => route.fulfill({
  json: { code: 0, data: { name: '测试员工', profileComplete: true, maskedCertNo: '**************0000' } },
}))
await page.route('**/platform/dormitory/staff/get/pwd*badge=*', (route) => route.abort())
```

断言入住页和门锁页正常显示，浏览器请求 URL 与 request body 不包含 `certno`、`homeAddress`、`badge`。

- [ ] **步骤 7：运行通过测试。**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am -Dtest=SmtDormitoryRoomServiceImplSelfCheckInTest,SmtDormitoryStaffControllerSelfLockTest test`  
Expected: `BUILD SUCCESS`。

Run: `pnpm --dir smart-h5 test -- check-in-rules.test.ts`  
Expected: PASS。

Run: `pnpm --dir smart-h5 e2e -- staff-access-control.spec.ts`  
Expected: PASS。

- [ ] **步骤 8：提交。**

```bash
git add smart-module/smart-platform smart-h5
git commit -m "fix(h5): bind check-in and lock access to current user"
```

### Task 4：收紧物品放行人员查询与详情归属

**文件：**

- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/ReleaseStaffLookupRespDTO.java`
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/req/CreateOfficeReleaseDraftReqDTO.java`
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/OfficeReleaseDraftRespDTO.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/controller/SmtArticlesReleaseControllerAccessTest.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtArticlesReleaseController.java:68-156`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/SmtArticlesReleaseService.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtArticlesReleaseServiceImpl.java`
- Modify: `smart-h5/src/features/good-release/api.ts:164-188`
- Modify: `smart-h5/src/features/good-release/work-draft.ts`
- Modify: `smart-h5/src/features/good-release/staff-search-popup.tsx`
- Modify: `smart-h5/src/app/good-release/work/page.tsx`
- Create: `smart-h5/src/features/good-release/api.test.ts`

**消耗：** 当前认证用户、放行申请草稿或已保存申请、园区数据权限。  
**产出：** 放行人员查询不再匿名，且仅返回人员 ID 和姓名。

- [ ] **步骤 1：写失败的访问控制测试。**

```java
@Test
public void releaseStaffLookupReturnsOnlyIdAndNameForOwnedDraft() {
    ReleaseStaffLookupRespDTO result = service.lookupStaffForRelease("owner-badge", 17L, "A100");
    assertEquals(Integer.valueOf(9), result.getId());
    assertEquals("测试员工", result.getName());
}

@Test
public void releaseStaffLookupRejectsForeignDraft() {
    assertThrows(AccessDeniedException.class,
        () -> service.lookupStaffForRelease("other-badge", 17L, "A100"));
}
```

- [ ] **步骤 2：运行失败测试。**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am -Dtest=SmtArticlesReleaseControllerAccessTest test`  
Expected: 编译失败，因为按申请归属的查询尚不存在。

- [ ] **步骤 3：实现申请归属查询。**

新增 `POST /articlesrelease/office/draft`，请求体为 `CreateOfficeReleaseDraftReqDTO`，响应为 `OfficeReleaseDraftRespDTO { Long releaseId; }`。草稿创建服务固定签名为 `OfficeReleaseDraftRespDTO createOfficeDraft(String ownerBadge, CreateOfficeReleaseDraftReqDTO request)`，必须持久化申请人、园区和 `DRAFT` 状态。

`CreateOfficeReleaseDraftReqDTO` 只包含 `@NotNull Integer parkId`；园区不从浏览器缓存、URL 或待选人员字段推导。`OfficeReleaseDraftRespDTO` 只包含 `Long releaseId`。后续办公区提交接口以该草稿记录为基础更新，不能接受由客户端指定的申请人。

新增 `GET /articlesrelease/{releaseId}/staff/lookup?badge=`，请求主体是认证用户工号。服务端按 `releaseId` 查询申请，验证申请人或当前审批人身份及园区关系后，调用 OA 查询并投影为 `ReleaseStaffLookupRespDTO(id,name)`。`/oa/staff/info/{badge}` 旧路由删除；`/detail/{id}`、`/back/confirm/{id}` 等 H5 外部路由也必须在 Controller 层验证当前用户与申请的申请人、审批人或保安角色关系，不能仅靠路径 ID。

办公区草稿在添加人员前必须先由服务端创建持久化草稿并返回 `releaseId`；前端 `WorkDraftState` 增加 `releaseId?: string | number` 与 `setReleaseId(releaseId: string | number)`，`/good-release/work/page.tsx` 在进入人员选择前调用 `createOfficeReleaseDraft`，`staff-search-popup.tsx` 只允许在 `releaseId` 非空时调用人员查询。草稿所有权以服务端数据库记录为准，不能以浏览器 Zustand 内容判断。

- [ ] **步骤 4：迁移当前 H5。**

将 `getOaStaffInfo(badge)` 改为：

```ts
export function getReleaseStaffInfo(releaseId: string | number, badge: string) {
  return request({ module: 'platform', url: `/articlesrelease/${releaseId}/staff/lookup`, params: { badge } })
}

export function createOfficeReleaseDraft(data: Record<string, unknown>) {
  return request({ module: 'platform', url: '/articlesrelease/office/draft', method: 'POST', data })
}
```

更新新增/编辑人员页面，使其在没有 `releaseId` 时先调用草稿创建接口；将 `id,name` 写入 `WorkPerson`，不在 store、URL、日志或错误提示中保存其他员工字段。

- [ ] **步骤 5：运行通过测试。**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am -Dtest=SmtArticlesReleaseControllerAccessTest test`  
Expected: `BUILD SUCCESS`。

Run: `pnpm --dir smart-h5 test -- api.test.ts work-draft.test.ts`  
Expected: PASS。

- [ ] **步骤 6：提交。**

```bash
git add smart-module/smart-platform smart-h5
git commit -m "fix(platform): restrict release staff lookup by ownership"
```

### Task 5：替换内部 Feign 员工查询并清除敏感日志

**文件：**

- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/feign/RemoteStaffInternalService.java`
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/InternalStaffBindingRespDTO.java`
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/InternalStaffModuleRespDTO.java`
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/InternalStaffPasswordRespDTO.java`
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/InternalStaffIdentityRespDTO.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/InternalStaffController.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/controller/InternalStaffControllerTest.java`
- Modify: `smart-module/smart-app/smart-app-biz/src/main/java/com/tce/smart/app/service/fore/impl/PasswordServiceImpl.java`
- Modify: `smart-module/smart-app/smart-app-biz/src/main/java/com/tce/smart/app/service/fore/impl/ForeModuleServiceImpl.java`
- Modify: `smart-module/smart-app/smart-app-biz/src/main/java/com/tce/smart/app/service/fore/impl/BadgeLossServiceImpl.java`
- Modify: `smart-module/smart-app/smart-app-biz/src/main/java/com/tce/smart/app/service/fore/impl/PerfectInfoServiceImpl.java`
- Modify: `smart-module/smart-app/smart-app-biz/src/main/java/com/tce/smart/app/service/fore/impl/IcbcCommonServiceImpl.java`
- Modify: `smart-module/smart-app/smart-app-biz/src/main/java/com/tce/smart/app/service/impl/AppWechatBindingServiceImpl.java`
- Modify: `smart/smart-upms/smart-upms-api/src/main/java/com/tce/smart/admin/api/feign/RemoteStaffService.java`
- Modify: `smart/smart-upms/smart-upms-biz/src/main/java/com/tce/smart/admin/service/impl/SysUserServiceImpl.java:694-707`
- Create: `smart-module/smart-app/smart-app-biz/src/test/java/com/tce/smart/app/service/fore/InternalStaffContractTest.java`
- Create: `smart/smart-upms/smart-upms-biz/src/test/java/com/tce/smart/admin/service/impl/SysUserServiceStaffContractTest.java`

**消耗：** Task 2 DTO、`SecurityConstants.FROM_IN`、当前 App/UPMS 使用字段。  
**产出：** 仓内调用不再依赖 `/staff/simple/get/badge` 或记录完整员工响应。

- [ ] **步骤 1：写失败的 Feign 契约测试。**

```java
@Test
public void internalStaffMethodsRequireFromInHeader() throws Exception {
    Method method = RemoteStaffInternalService.class.getMethod(
        "getBindingStaff", String.class, String.class);
    assertNotNull(method.getParameters()[1].getAnnotation(RequestHeader.class));
}

@Test
public void noAppServiceLogsWholeStaffResult() throws IOException {
    assertFalse(Files.readString(Paths.get(APP_WECHAT_BINDING)).contains("getSimpleSttaffByBadge==="));
    assertFalse(Files.readString(Paths.get(PASSWORD_SERVICE)).contains("staffResult"));
}
```

- [ ] **步骤 2：运行失败测试。**

Run: `mvn -pl smart-module/smart-app/smart-app-biz -am -Dtest=InternalStaffContractTest test`  
Expected: 编译失败，因为内部 Feign 契约尚不存在。

- [ ] **步骤 3：定义按用途拆分的内部 DTO 与路由。**

定义并固定字段：

```java
public class InternalStaffBindingRespDTO { Long staffId; String badge; String name; Integer status; String certNoLast6; }
public class InternalStaffModuleRespDTO { String badge; String compId; }
public class InternalStaffPasswordRespDTO { Long staffId; String badge; String facePicId; }
public class InternalStaffIdentityRespDTO { Long staffId; String badge; String name; String certno; }
```

`InternalStaffIdentityRespDTO` 只能由 OCR 完善资料和工商银行实名请求两个受控服务消费，Controller 必须记录调用服务、用途和结果，不得返回给 App 客户端。其他内部消费者不得引用其类型。

`RemoteStaffInternalService` 固定方法：

```java
@GetMapping("/internal/staff/binding/{badge}")
Result<InternalStaffBindingRespDTO> getBindingStaff(
    @PathVariable("badge") String badge,
    @RequestHeader(SecurityConstants.FROM) String from);

@GetMapping("/internal/staff/module/{badge}")
Result<InternalStaffModuleRespDTO> getModuleStaff(
    @PathVariable("badge") String badge,
    @RequestHeader(SecurityConstants.FROM) String from);

@GetMapping("/internal/staff/password/{badge}")
Result<InternalStaffPasswordRespDTO> getPasswordStaff(
    @PathVariable("badge") String badge,
    @RequestHeader(SecurityConstants.FROM) String from);

@GetMapping("/internal/staff/ocr/{badge}")
Result<InternalStaffIdentityRespDTO> getIdentityStaff(
    @PathVariable("badge") String badge,
    @RequestHeader(SecurityConstants.FROM) String from);
```

`InternalStaffController` 每个方法标注 `@Inner`，检查 `from` 只能为 `SecurityConstants.FROM_IN`，并调用显式投影服务方法。禁止重新暴露 `SmtStaffDTO`。

- [ ] **步骤 4：逐调用方迁移。**

| 调用方 | 替换 DTO | 可用字段 |
|---|---|---|
| `AppWechatBindingServiceImpl` | `InternalStaffBindingRespDTO` | `status`、`certNoLast6` |
| `BadgeLossServiceImpl` | `InternalStaffBindingRespDTO` | `name` |
| `ForeModuleServiceImpl` | `InternalStaffModuleRespDTO` | `compId` |
| `SysUserServiceImpl` | `InternalStaffBindingRespDTO` | `status` |
| `PasswordServiceImpl` | `InternalStaffPasswordRespDTO` | `staffId`、`badge`、`facePicId` |
| `PerfectInfoServiceImpl`、`IcbcCommonServiceImpl` | `InternalStaffIdentityRespDTO` | `name`、`certno`，仅在服务端比对或银行请求中使用 |

每一个 Feign 调用均传入 `SecurityConstants.FROM_IN`。删除 `remoteStaffService.getSimpleSttaffByBadge` 的所有仓内引用和两处完整 `Result` 日志，改为只记录 `success` 与调用场景；日志中不得拼接员工对象。

- [ ] **步骤 5：运行通过测试。**

Run: `mvn -pl smart-module/smart-app/smart-app-biz -am -Dtest=InternalStaffContractTest test`  
Expected: `BUILD SUCCESS`。

Run: `mvn -pl smart/smart-upms/smart-upms-biz -am -Dtest=SysUserServiceStaffContractTest test`  
Expected: `BUILD SUCCESS`。

Run: `rg -n 'getSimpleSttaffByBadge\\(' smart-module/smart-app smart/smart-upms`  
Expected: 不再有业务调用；仅允许历史注释或被删除的旧接口定义，最终应为零命中。

- [ ] **步骤 6：提交。**

```bash
git add smart-module/smart-platform smart-module/smart-app smart/smart-upms
git commit -m "fix(feign): replace staff entity lookup contracts"
```

### Task 6：完善服务令牌和内部端点强制校验

**文件：**

- Modify: `smart/smart-common/smart-common-security/src/main/java/com/tce/smart/common/security/feign/SmartFeignClientInterceptor.java:42-78`
- Modify: `smart/smart-common/smart-common-security/src/main/java/com/tce/smart/common/security/component/SmartSecurityInnerAspect.java:52-105`
- Modify: `smart/smart-common/smart-common-security/src/test/java/com/tce/smart/common/security/component/SmartSecurityInnerAspectTest.java`
- Create: `smart/smart-common/smart-common-security/src/test/java/com/tce/smart/common/security/feign/SmartFeignClientInterceptorTest.java`
- Modify: `smart/smart-gateway/src/main/java/com/tce/smart/gateway/filter/SmartRequestGlobalFilter.java:40-68`
- Create: `smart/smart-gateway/src/test/java/com/tce/smart/gateway/filter/SmartRequestGlobalFilterTest.java`

**消耗：** Task 5 的 `FROM_IN` Feign 契约和已有 OAuth 客户端配置。  
**产出：** 无 HTTP 请求上下文的 Feign 调用仍携带服务令牌；外部请求不能借 `from` 伪造内部访问。

- [ ] **步骤 1：写失败测试。**

```java
@Test
public void noRequestContextStillAppliesClientCredentialsInterceptor() {
    RequestContextHolder.resetRequestAttributes();
    RequestTemplate template = new RequestTemplate();
    interceptor.apply(template);
    assertTrue(template.headers().get("Authorization").iterator().next().startsWith("Bearer "));
}

@Test
public void gatewayRemovesForgedFromHeaderBeforeForwarding() {
    ServerWebExchange forwarded = filterExchangeWithHeader("from", "Y");
    assertFalse(forwarded.getRequest().getHeaders().containsKey("from"));
}
```

- [ ] **步骤 2：运行失败测试。**

Run: `mvn -pl smart/smart-common/smart-common-security -am -Dtest=SmartFeignClientInterceptorTest,SmartSecurityInnerAspectTest test`  
Expected: 当前无请求上下文分支直接返回，新的服务令牌断言失败。

- [ ] **步骤 3：实现服务令牌回退和 fail-closed 行为。**

将 `SmartFeignClientInterceptor.apply` 改为：有入站请求时复制安全白名单头、保留显式 `FROM_IN`；没有入站请求时仍调用 OAuth 客户端凭据获取逻辑。若服务令牌不可获得，内部调用必须明确失败并记录不含秘密的调用服务、目标服务和路径，不能静默匿名请求。

`SmartSecurityInnerAspect` 保持默认 `AUDIT`，仅在 Nacos 发布任务确认所有目标服务审计日志为零缺失后改为 `ENFORCE`。测试必须保留 `OFF` 紧急回滚、`AUDIT` 零中断和 `ENFORCE` 无 `FROM_IN` 拒绝三种行为。

Gateway 继续剥离外部 `from` 头，并新增 `/internal/**` 外部路由拒绝测试；拒绝响应不得泄露目标服务名或接口参数。

- [ ] **步骤 4：运行通过测试。**

Run: `mvn -pl smart/smart-common/smart-common-security -am -Dtest=SmartFeignClientInterceptorTest,SmartSecurityInnerAspectTest test`  
Expected: `BUILD SUCCESS`。

Run: `mvn -pl smart/smart-gateway -am -Dtest=SmartRequestGlobalFilterTest test`  
Expected: `BUILD SUCCESS`。

- [ ] **步骤 5：提交。**

```bash
git add smart/smart-common/smart-common-security smart/smart-gateway
git commit -m "fix(security): enforce authenticated internal staff calls"
```

### Task 7：迁移 Smart UI 人员搜索与详情接口

**文件：**

- Modify: `smart-ui/src/api/platform/basic/personnel_manage.js:295-305`
- Modify: `smart-ui/src/api/platform/basic/staff_info.js:21-27`
- Create: `smart-ui/src/api/platform/basic/personnel_manage.staff-lookup.test.js`
- Create: `smart-ui/src/api/platform/basic/staff_info.staff-detail.test.js`
- Verify consumers: `smart-ui/src/views/platform/basic/personnel_manage/index.vue`
- Verify consumers: `smart-ui/src/views/platform/basic/personnel_manage/leave.vue`
- Verify consumers: `smart-ui/src/views/platform/basic/personnel_manage/popover-tree/index-single.vue`
- Verify consumers: `smart-ui/src/views/platform/outsourcing/onwork/index.vue`
- Verify consumers: `smart-ui/src/views/platform/outsourcing/onwork/popover-tree/index-single.vue`
- Verify consumers: `smart-ui/src/views/platform/security_area/supplier_person/_supplier.vue`

**消耗：** Task 2 的 `StaffLookupRespDTO` 和受控详情 DTO。  
**产出：** 管理端维持下拉搜索体验，但不会接收完整员工实体。

- [ ] **步骤 1：写失败的 API 请求与字段测试。**

```js
it('searches staff through the authenticated minimal lookup endpoint', async () => {
  await getSearchStaff({ badge: 'A100' })
  expect(request).toHaveBeenCalledWith(expect.objectContaining({
    url: '/platform/staff/lookup', method: 'get', params: { badge: 'A100' },
  }))
})

it('does not expose sensitive columns in search options', () => {
  expect(normalizeLookup({ staffId: 1, badge: 'A100', name: '测试', departmentName: '生产' }))
    .toEqual({ id: 1, badge: 'A100', name: '测试', departmentName: '生产' })
})
```

- [ ] **步骤 2：运行失败测试。**

Run: `pnpm --dir smart-ui test -- personnel_manage.staff-lookup.test.js staff_info.staff-detail.test.js`  
Expected: FAIL，因为 API 仍指向旧路径。

- [ ] **步骤 3：实现 API 适配。**

将 `getSearchStaff` 改为 `/platform/staff/lookup`，保留 `{ badge }` 参数和 Promise 外形。新增 `normalizeLookup`，只映射 `staffId/badge/name/departmentName`。将 `getStaffByBadge` 改为 `/platform/staff/admin/{staffId}`，调用方由搜索结果的 `staffId` 驱动；不得以工号请求后台详情。所有页面继续只读 `id`、`badge`、`name` 与部门显示字段。

- [ ] **步骤 4：运行通过测试和质量门禁。**

Run: `pnpm --dir smart-ui test -- personnel_manage.staff-lookup.test.js staff_info.staff-detail.test.js`  
Expected: PASS。

Run: `pnpm --dir smart-ui check:admin-search`  
Expected: PASS。

Run: `pnpm --dir smart-ui lint`  
Expected: PASS，或仅报告本分支之前已记录的基线告警。

- [ ] **步骤 5：提交。**

```bash
git add smart-ui/src/api smart-ui/src/views
git commit -m "fix(smart-ui): use minimal staff lookup contract"
```

### Task 8：盘点并收紧所有 `/**` 服务端点

**文件：**

- Create: `scripts/security/build-public-route-inventory.mjs`
- Create: `scripts/security/build-public-route-inventory.test.mjs`
- Create: `docs/security/2026-07-22-service-route-inventory.md`
- Modify: `docker/nacos/config/dev/smart-data.yml`
- Modify: `docker/nacos/config/dev/smart-algorithm.yml`
- Modify: `docker/nacos/config/dev/smart-push.yml`
- Modify: `docker/nacos/config/dev/smart-dispatcher.yml`
- Modify: `docker/nacos/config/dev/smart-schedule.yml`
- Modify: `docker/nacos/config/dev/smart-bridge-biz-*.yml`
- Modify: `docker/nacos/config/dev/smart-bridge-isc*.yml`
- Modify: 与已确认内部 Controller 对应的 `@Inner`、Feign 接口及测试文件。

**消耗：** Task 1 检测器、每个 Controller 的 `@RequestMapping`/`@GetMapping`/`@PostMapping`、Nacos 配置。  
**产出：** 每个服务的完整路由分类、精确匿名白名单和内部调用验证。

- [ ] **步骤 1：写失败的路由归类测试。**

```js
assert.deepEqual(
  classifyRoute({ path: '/compare', annotations: ['Inner'], caller: 'platform-feign' }),
  { exposure: 'internal', nacosIgnoreUrl: false, requires: ['service-token', 'FROM_IN'] },
)
assert.deepEqual(
  classifyRoute({ path: '/callback/vendor', annotations: ['SignatureVerified'], caller: 'vendor' }),
  { exposure: 'callback', nacosIgnoreUrl: true, requires: ['signature', 'timestamp', 'nonce'] },
)
```

- [ ] **步骤 2：运行失败测试。**

Run: `node scripts/security/build-public-route-inventory.test.mjs`  
Expected: `ERR_MODULE_NOT_FOUND`。

- [ ] **步骤 3：实现静态库存生成器。**

扫描目标模块的 Controller 注解和 `docker/nacos/config/dev` 文件，生成 `docs/security/2026-07-22-service-route-inventory.md`。每条路由必须具有以下之一：`external-authenticated`、`callback-signed`、`internal`、`retired`。生成器发现“配置匿名但没有 callback-signed 标记”“内部路径没有 `@Inner`”“通配 ignore-url”时退出非零。

- [ ] **步骤 4：逐服务完成路由分类和代码保护。**

按以下顺序处理，确保每个服务可独立部署与回滚：

1. `smart-dispatcher`：全部调度入口为 `internal`，Feign 均有服务令牌和 `FROM_IN`。
2. `smart-algorithm`：人脸、OCR、比对入口为 `internal`，不得作为外网图片处理 API。
3. `smart-data`：EHR、DHR、考勤和薪资视图入口为 `internal`，无匿名数据库查询入口。
4. `smart-push`：内部推送为 `internal`；厂商回调仅在签名、时间窗、nonce 重放检查完整时标为 `callback-signed`。
5. `smart-schedule`：调度触发和任务执行为 `internal`，不接受外网触发。
6. 每个 `smart-bridge-biz-*` 与 `smart-bridge-isc-*`：设备接口按厂商回调或内部调用拆分；回调签名失败返回 401/403，不能退化为 `/**`。

每处理完一个服务，删除该服务本地配置中的 `/**`，仅填入库存文件中 `callback-signed` 的精确路由；若该服务没有签名回调，`ignore-urls` 为空列表。禁止为了使测试通过保留 `/inner/**`、`/test/**` 或 Swagger 通配路径。

- [ ] **步骤 5：运行库存和配置门禁。**

Run: `node scripts/security/build-public-route-inventory.mjs`  
Expected: 生成库存文档，且每条路由均已分类。

Run: `node scripts/security/check-nacos-ignore-urls.mjs docker/nacos/config/dev`  
Expected: exit `0`，没有被禁止的通配匿名规则。

- [ ] **步骤 6：提交。**

```bash
git add scripts/security docker/nacos/config/dev docs/security smart-module
git commit -m "fix(nacos): replace wildcard anonymous routes"
```

### Task 9：验证 Platform 与 UPMS 的精确 Nacos 收口

**文件：**

- Modify: `docker/nacos/config/dev/smart-platform.yml`
- Modify: `docker/nacos/config/dev/smart-upms-biz.yml`
- Modify: `docs/security/2026-07-22-yuto-prod-dev-nacos-access-control-rollout.md`
- Create: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/controller/PublicRouteDenyContractTest.java`
- Create: `smart/smart-upms/smart-upms-biz/src/test/java/com/tce/smart/admin/controller/UpmsOpenApiAccessContractTest.java`

**消耗：** Tasks 2 至 8 的迁移完成状态和 Nacos 路由库存。  
**产出：** Platform、UPMS 不再拥有业务通配匿名路径。

- [ ] **步骤 1：写失败的拒绝契约测试。**

```java
@Test
public void publicLegacyStaffPathsAreAbsentFromIgnoreUrlPolicy() {
    assertFalse(ignoreUrls.contains("/staff/**"));
    assertFalse(ignoreUrls.contains("/staff/simple/badge"));
    assertFalse(ignoreUrls.contains("/articlesrelease/**"));
}

@Test
public void openApiRequiresRegisteredClientScope() {
    assertEquals(403, callWithoutScope("/api/park/list").getStatusCodeValue());
}
```

- [ ] **步骤 2：运行失败测试。**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am -Dtest=PublicRouteDenyContractTest test`  
Expected: FAIL，因为本地 Nacos Platform 配置仍包含旧规则。

- [ ] **步骤 3：收紧本地配置基线。**

从 `smart-platform.yml` 删除 `/staff/**`、`/staff/simple/badge`、`/articlesrelease/**` 及经库存确认可由认证保护的业务匿名路径。保留的每一条必须在库存中标为 `callback-signed` 或无敏感健康检查。

从 `smart-upms-biz.yml` 删除 `/api/**`；合法开放 API 通过 App ID、客户端密钥、scope 和园区绑定认证，不通过 `ignore-urls` 放行。

- [ ] **步骤 4：更新生产发布清单。**

为 Platform 和 UPMS 各新增一行“兼容版本镜像”“灰度实例”“旧接口 QPS=0 观察窗口”“Nacos 前后 MD5”“回滚版本”。发布审批人必须签字确认仓外调用方已清零或已迁移。

- [ ] **步骤 5：运行通过测试。**

Run: `node scripts/security/check-nacos-ignore-urls.mjs docker/nacos/config/dev`  
Expected: exit `0`。

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am -Dtest=PublicRouteDenyContractTest test`  
Expected: `BUILD SUCCESS`。

Run: `mvn -pl smart/smart-upms/smart-upms-biz -am -Dtest=UpmsOpenApiAccessContractTest test`  
Expected: `BUILD SUCCESS`。

- [ ] **步骤 6：提交。**

```bash
git add docker/nacos/config/dev/smart-platform.yml docker/nacos/config/dev/smart-upms-biz.yml docs/security smart-module/smart-platform smart/smart-upms
git commit -m "fix(nacos): require auth for platform and upms routes"
```

### Task 10：执行生产灰度、验收与受控收尾

**文件：**

- Modify: `docs/security/2026-07-22-yuto-prod-dev-nacos-access-control-rollout.md`
- Modify: `docs/security/2026-07-22-service-route-inventory.md`

**消耗：** 已合并或待发布的兼容版本、Nacos 历史版本、网关访问日志、运维网络隔离证明。  
**产出：** 每个 Data ID 的发布记录、验收证据、回滚版本和旧接口下线结论。

- [ ] **步骤 1：发布前只读核验。**

在生产 Nacos `yuto_prod / dev` 读取每个目标 Data ID 的当前 MD5、历史版本和 `ignore-urls`；在网关日志按完整路径统计旧接口、`/internal/**`、设备回调与 `from` 伪造请求。记录统计周期、实例数和日志覆盖窗口，不保存请求参数和响应体。

- [ ] **步骤 2：部署兼容版本。**

先仅部署 Tasks 2 至 7 的代码版本，不修改 Nacos。验证 Smart UI、当前 H5、App、UPMS、Feign、定时任务和设备回调；发生异常只回滚镜像，不修改安全白名单。

- [ ] **步骤 3：按 Data ID 单项灰度 Nacos。**

每次只发布一个 Data ID 到一个灰度实例。执行：无 Token 访问旧路径、伪造 `from=Y`、跨园区人员 ID、不同员工工号、内部 Feign、当前 H5 入住、当前 H5 门锁、物品放行、UI 搜索、App 绑定、UPMS 初始化。所有外部拒绝应为 401/403，所有合法业务流应成功。

- [ ] **步骤 4：观察和扩大。**

观察窗口内记录错误率、5xx、401/403、Feign 失败、任务失败、设备回调失败和旧路由访问量。达到发布清单定义的基线后扩大至全部实例；任何未分类调用、业务错误或回调失败均停止扩大并回滚当前 Data ID 到记录的历史版本。

- [ ] **步骤 5：删除旧接口。**

仅当旧路径 QPS 连续观察窗口为零、仓外调用方完成书面确认、全量回归通过时，删除旧 Controller 映射、旧 Feign 方法、兼容 DTO 和无用前端适配。再次运行 `rg -n 'simple/get/badge|define/badge|simple/badge' smart smart-module smart-ui smart-h5`，预期无生产代码命中。

- [ ] **步骤 6：最终验收。**

Run: `node scripts/security/check-nacos-ignore-urls.mjs docker/nacos/config/dev`  
Expected: exit `0`。

Run: `pnpm --dir smart-h5 check && pnpm --dir smart-h5 test && pnpm --dir smart-h5 e2e`  
Expected: PASS。

Run: `pnpm --dir smart-ui test && pnpm --dir smart-ui lint`  
Expected: PASS，或已记录的历史基线告警。

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test`  
Expected: `BUILD SUCCESS`。

Run: `mvn -pl smart-module/smart-app/smart-app-biz -am test`  
Expected: `BUILD SUCCESS`。

Run: `mvn -pl smart/smart-upms/smart-upms-biz -am test`  
Expected: `BUILD SUCCESS`。

记录每个命令的 commit、镜像、Nacos Data ID、MD5、执行时间和结果。任何未运行项必须标明原因，不得以“命令退出 0”代替业务验证。

- [ ] **步骤 7：提交发布记录。**

```bash
git add docs/security
git commit -m "docs(security): record pii access control rollout"
```

## 实施前最终检查

- [ ] 所有任务都以 TDD 的红灯、最小绿灯、回归三步执行。
- [ ] 每个 commit 只包含一个可验证交付物，不混入构建产物、环境文件、日志、证书或生产导出物。
- [ ] 不推送、不创建 PR、不发布生产；这些动作必须由【旅途】单独授权。
- [ ] 任何 Nacos 生产变更前均获得运维确认：服务端口未直接暴露外网，Gateway 已拒绝 `/internal/**`，且存在可用历史回滚版本。
