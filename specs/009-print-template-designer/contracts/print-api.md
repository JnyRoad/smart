# 模板与打印 API 契约

**规格**：[../spec.md](../spec.md)
**数据模型**：[../data-model.md](../data-model.md)
**状态**：模板/版本/组合/合成预览与受控图片接口已进入批次1实现；绑定、任务和设备接口仍为设计。实际验证与未验收项见 quickstart.md。

本文定义平台服务、管理端、可信渲染服务和 Windows 打印客户端之间的逻辑 REST 契约。路径前缀可由现有网关映射，字段、错误码、状态和幂等语义不得因映射改变。正式打印只能由平台创建并冻结任务，再由已授权客户端领取；浏览器上传的最终 PDF 不能绕过平台校验直接打印。

## 1. 通用约定

### 1.1 传输、身份与园区

- 管理端实际网关接口前缀为 `/platform/print/v1`（服务内为 `/print/v1`），使用平台登录态和园区/功能权限。服务端从登录主体和业务对象解析 `parkId`，不信任请求体单独传入的园区。
- 打印客户端接口前缀为 `/api/print-client/v1`，使用独立设备身份（客户端证书或等效设备令牌）；身份绑定一个 `deviceIdentity`、一个获准 `parkId`，并可按授权关联一个或多个 `printerProfileId`。客户端不能借用管理员会话查询人员或模板。
- 渲染服务接口前缀为 `/internal/print-renderer/v1`，只接受 smart-platform 经过鉴权、校验和冻结的模板/数据/受控资源，不向浏览器或公网暴露。
- 请求使用 HTTPS；同机 loopback 内部渲染允许 HTTP，跨主机内部渲染必须使用 HTTPS，均使用服务间认证。时间字段使用 UTC ISO-8601，例如 `2026-09-05T12:00:00Z`。
- 资源只传 `objectId`、`contentHash`、`mediaType`、园区和权限元数据；拒绝任意 URL、绝对路径、路径穿越、脚本、未登记字体和远程资源。
- 跨园区对象或设备即使 ID 有效也返回 `403 PRINT_SCOPE_DENIED`，不通过“查不到”掩盖授权错误。无本园区对象返回 `404`。

### 1.2 最小操作权限矩阵

以下是接口能力的最小边界，实际角色和权限键沿用现有权限体系配置，本规格不宣称新增或固定既有 `permission key`。所有能力还必须通过园区范围校验；未授予的能力默认拒绝。

| 能力 | 允许的最小操作 |
| --- | --- |
| 模板管理 | 查询模板/版本、创建模板、保存草稿、发布、回滚和维护 `PrintTemplatePair`；发布、回滚和改绑需要比只读更高的管理权限。 |
| 预览 | 使用已授权模板、字段和资源生成/读取预览；不能由预览权限创建正式打印任务。 |
| 资源下载 | 下载当前主体有权使用的模板资源或任务制品；只能按对象存储 ID、园区和 hash 校验后读取。 |
| 设备管理 | 查询/创建/修改/验证/禁用本园区打印机档案；能力验证和隔离操作需要设备管理权限。 |
| 打印执行 | 创建/查询/取消本人或被授权业务范围内的 job；不能据此修改模板、档案或人工恢复结果。 |
| 人工恢复 | 翻面确认、输出核对、结果不明处置和设备清空；需要现场操作权限，不能由普通查询权限代替。 |
| 打印客户端 | 仅以设备身份 claim/续租、读取获准 job 制品、提交本设备事件和清空证据；不能查询人员库或跨园区资源。 |

### 1.3 请求头和响应外壳

所有 JSON 请求使用 `Content-Type: application/json`。服务端响应至少带 `X-Request-Id`，响应体沿用项目现有统一响应类型和错误映射。本文只约定以下字段语义，不声明具体 Java 类名：

```json
{
  "requestId": "req_01J...",
  "data": {},
  "error": null
}
```

错误响应：

```json
{
  "requestId": "req_01J...",
  "data": null,
  "error": {
    "code": "DRAFT_REVISION_CONFLICT",
    "message": "草稿已被其他操作员修改，请重新加载后再保存",
    "details": { "currentDraftRevision": 8 },
    "retryable": false
  }
}
```

下列写操作必须带 `Idempotency-Key` 请求头：创建任务、批量创建任务、发布、回滚、模板对新增/改绑/归档、绑定新增/修改、设备 claim/租约续期、取消、翻面确认、人工输出核对、设备清空和客户端事件。值为 1-128 个 ASCII 字符。`commandId` 由服务端在 claim 或人工继续产生新物理 attempt 时分配；客户端只在后续物理事件中回传该值，人工动作请求本身只用 `Idempotency-Key` 去重。

服务端对同一作用域内的 `(principal/deviceIdentity, Idempotency-Key)` 保存 canonical body hash 和原响应：

- key、body hash 相同：返回原响应并标记 `replayed=true`，不得重复创建版本、任务、attempt 或打印命令。
- key 相同但 body hash 不同：返回 `409 IDEMPOTENCY_KEY_REUSED`，不得执行第二次操作。
- 服务端已分配的同一 attempt/`commandId` 只能对应一次物理提交；同一 command 的事件可以有多条不同类型，不能据此重新创建 attempt 或驱动设备。
- 同一 `eventId` 重复且 body hash 相同：返回首次事件处理结果，不重复推进状态。
- 同一 key 或 `eventId` 的 body 不同：返回 `409 IDEMPOTENCY_KEY_REUSED`。服务端不承诺跨服务、驱动和物理设备的分布式 exactly-once 出卡。

### 1.4 字段和规模限制

| 字段/对象 | 契约限制 |
| --- | --- |
| ID | 新建打印领域实体、命令和事件使用长度 36 的 UUID 字符串。既有 `staffId`、`subjectId`、`parkId`、用户 ID 沿用现有主键类型；`requestId`/`renderRequestId` 为不超过 128 字符的追踪标识。`deviceIdentity` 为 1-128 个 ASCII 字符。 |
| 名称 | 1-100 个 Unicode 字符；分类、字段键、设备型号等稳定键 1-64 个字符。 |
| `layoutJson` | UTF-8 JSON 文本不超过 2 MiB；不得包含 URL、脚本或未经登记资源。 |
| `fieldSchemaJson`/`pageSpecJson` | 各不超过 256 KiB；每个 `TemplateVersion` 的 `sideCount` 固定为 1、`schemas` 固定一页；厂牌任务的两页来自 FRONT/BACK 两个版本。 |
| `resourceManifest` | 每个版本最多 32 个资源；单个资源最大 20 MiB，总资源最大 32 MiB；只允许登记用途。20 MiB 上限容纳固定版 Noto Sans CJK SC Regular（16,437,364 bytes），字体按路由加载。 |
| 人员数据快照 | 每个 job 最大 1 MiB；只含打印所需最小字段和受控资源引用。 |
| 批量创建 | 一次最多 100 个业务人员；服务端按人拆成 job，不能把多人塞进一个 job。 |
| 事件 payload | 单个事件最大 64 KiB；不得包含密码、令牌、完整证件号或完整照片字节。 |
| 渲染输出 | 全部面 PDF 解码后合计不超过 32 MiB；内部响应含 Base64 内容，平台验 hash/尺寸/页数后保存为受控制品。 |

超出限制统一返回 `422 PAYLOAD_LIMIT_EXCEEDED`，不截断、不增加页面、不隐式压缩后改变 hash。

## 2. 业务字段和状态返回

### 2.1 模板版本摘要

模板及版本接口返回以下字段（详情接口可增加受权限保护的校验报告）：

```json
{
  "templateId": "00000000-0000-4000-8000-000000000001",
  "parkId": "1",
  "templateKey": "staff-default",
  "name": "员工通用厂牌",
  "printItemType": "STAFF_CARD",
  "personType": "EMPLOYEE",
  "classificationCode": "STAFF_DEFAULT",
  "faceRole": "FRONT",
  "sideCount": 1,
  "draftRevision": 3,
  "currentDraftVersionId": "00000000-0000-4000-8000-000000000002",
  "currentPublishedVersionId": "00000000-0000-4000-8000-000000000003",
  "versions": [
    {
      "templateVersionId": "00000000-0000-4000-8000-000000000003",
      "versionNo": 4,
      "versionStatus": "PUBLISHED",
      "faceRole": "FRONT",
      "sideCount": 1,
      "contentHash": "sha256:…",
      "publishedAt": "2026-09-05T12:00:00Z"
    }
  ]
}
```

每个 `Template` 只维护一个 `faceRole`，每个 `TemplateVersion` 永远返回 `sideCount=1`，其 pdfme `schemas` 只有一页。`STAFF_CARD` 的双面摘要由 `PrintTemplatePair` 返回，不把 FRONT/BACK 塞进同一版本；`VISITOR_SLIP` 只能是 `faceRole=FRONT`。接口不接受客户端增加第二页或改变版本面数绕过服务端校验。

### 2.2 模板对摘要

```json
{
  "pairId": "00000000-0000-4000-8000-000000000016",
  "parkId": "1",
  "name": "外包厂牌默认组合",
  "printItemType": "STAFF_CARD",
  "personType": "OUTSOURCED",
  "classificationCode": "OUTSOURCE_DEFAULT",
  "frontTemplateVersionId": "00000000-0000-4000-8000-000000000003",
  "backTemplateVersionId": "00000000-0000-4000-8000-000000000014",
  "frontVersionNo": 4,
  "backVersionNo": 2,
  "revision": 3,
  "status": "ACTIVE"
}
```

`PrintTemplatePair` 只有稳定 `pairId` 和 `revision`，没有 pairVersion，也没有独立发布/回滚流程。保存或改绑时必须同时校验两份已发布不可变单面版本的园区、面角色、尺寸、方向、介质和适用分类；两个版本号可以不同。多个兼容 pair 可以共享同一个 BACK 版本。发布或回滚任一模板版本不会自动改写已有 pair。

### 2.3 打印任务摘要

```json
{
  "jobId": "00000000-0000-4000-8000-000000000004",
  "parkId": "1",
  "printItemType": "STAFF_CARD",
  "personType": "OUTSOURCED",
  "classificationCode": "OUTSOURCE_DEFAULT",
  "subjectType": "STAFF",
  "subjectId": "123",
  "selection": {
    "kind": "PAIR",
    "pairId": "00000000-0000-4000-8000-000000000016"
  },
  "pairRevision": 3,
  "frontTemplateId": "00000000-0000-4000-8000-000000000001",
  "backTemplateId": "00000000-0000-4000-8000-000000000017",
  "frontTemplateVersionId": "00000000-0000-4000-8000-000000000003",
  "backTemplateVersionId": "00000000-0000-4000-8000-000000000014",
  "printerProfileId": "00000000-0000-4000-8000-000000000005",
  "printMode": "MANUAL_DUPLEX",
  "expectedFaceCount": 2,
  "currentFace": "FRONT",
  "status": "AWAITING_FLIP",
  "faces": [
    { "face": "FRONT", "status": "CONFIRMED_OUT", "attemptId": "00000000-0000-4000-8000-000000000006" },
    { "face": "BACK", "status": "NOT_STARTED", "attemptId": null }
  ],
  "createdAt": "2026-09-05T12:00:00Z"
}
```

详情和日志默认只返回 `subjectId`、摘要、状态和 hash；完整快照、照片和制品需要单独权限。厂牌 job 必须返回冻结的 FRONT/BACK 模板版本；访客 job 的 `expectedFaceCount` 必须为 1，只返回 FRONT 模板版本和 FRONT face。

## 3. 模板、版本和预览 API

### 3.1 端点清单

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `GET` | `/templates` | 按园区、打印物、人员类型、分类、`faceRole`、状态分页查询。 |
| `POST` | `/templates` | 创建模板容器及草稿版本。 |
| `GET` | `/templates/{templateId}` | 读取模板、版本摘要和当前指针。 |
| `PATCH` | `/templates/{templateId}` | 以 `draftRevision` 乐观锁保存草稿。 |
| `GET` | `/templates/{templateId}/versions` | 查询历史版本。 |
| `POST` | `/templates/{templateId}/publish` | 校验并发布该模板的单面版本。 |
| `POST` | `/templates/{templateId}/rollback` | 将当前发布指针回滚到历史发布版本。 |
| `POST` | `/templates/{templateId}/preview` | 生成授权预览制品，不创建打印任务。 |
| `GET` | `/previews/{previewId}` | 查询获准预览状态、违规项与受控面制品下载地址。 |
| `GET` | `/template-versions/{versionId}/resources/{objectId}` | 下载已授权模板资源，校验园区、版本 manifest 和 hash。 |

### 3.2 创建和保存草稿

`POST /templates` 请求：

```json
{
  "name": "员工通用厂牌",
  "printItemType": "STAFF_CARD",
  "personType": "EMPLOYEE",
  "classificationCode": "STAFF_DEFAULT",
  "faceRole": "FRONT",
  "sideCount": 1,
  "layoutJson": { "schemaVersion": 1, "faceRole": "FRONT", "sideCount": 1, "basePdfRef": null, "schemas": [[]] },
  "fieldSchemaJson": { "fields": [] },
  "pageSpecJson": { "widthMm": 85.6, "heightMm": 53.98, "orientation": "LANDSCAPE", "maxPageCount": 1 },
  "resourceManifest": []
}
```

响应 `201` 返回 `templateId`、`currentDraftVersionId` 和 `draftRevision=0`。`TemplateVersion.sideCount` 永远为 `1`，`layoutJson.schemas` 必须恰好一页；`STAFF_CARD` 通过 `faceRole=FRONT` 或 `BACK` 分别创建两个模板容器，不能在一次模板创建请求中塞入两面。`VISITOR_SLIP` 只能为 `faceRole=FRONT`。创建时不发布、不渲染正式打印制品。

`PATCH /templates/{templateId}` 请求必须带当前 `draftRevision`：

```json
{
  "draftRevision": 0,
  "name": "员工通用厂牌 v2",
  "faceRole": "FRONT",
  "sideCount": 1,
  "layoutJson": { "schemaVersion": 1, "faceRole": "FRONT", "sideCount": 1, "basePdfRef": null, "schemas": [[]] },
  "fieldSchemaJson": { "fields": [] },
  "pageSpecJson": { "widthMm": 85.6, "heightMm": 53.98, "orientation": "LANDSCAPE", "maxPageCount": 1 },
  "resourceManifest": []
}
```

成功返回递增后的 `draftRevision`。版本内容与资源清单整体替换并重新计算该单面草稿 hash；修改 FRONT 不会隐式修改 BACK，反之亦然。版本冲突返回 `409 DRAFT_REVISION_CONFLICT`，`details.currentDraftRevision` 提供当前值；客户端必须重新加载并由操作员重新确认，不得自动覆盖。

### 3.3 发布和回滚

`POST /templates/{templateId}/publish` 请求：

```json
{ "draftRevision": 3, "draftVersionId": "00000000-0000-4000-8000-000000000002" }
```

服务端原子执行：校验园区/权限、`faceRole`、单页结构、尺寸、必填字段、边界、字体、二维码/条码区域、资源授权和 `contentHash`，生成该模板不可变递增 `versionNo`，再更新该模板的 `currentPublishedVersionId`。成功返回 `201` 的已发布单面版本摘要；发布只影响后续新 job，不改变已有 job 快照，也不会自动更新引用该模板的 `PrintTemplatePair`，更不触发设备打印。

保密访客分类的发布必须包含二维码组件并绑定受控 `visitorCredentialPayload`；当前源码已确认现有访客业务以 `smsCode` 承载扫码语义，平台适配层只做字段映射，模板契约不新增凭证发码接口。普通访客、员工和其他类型不能用自由字段伪造保密分类。校验失败返回 `422 TEMPLATE_VALIDATION_FAILED`，`details.violations[]` 必须列出 `face`、字段/组件和错误码。

`POST /templates/{templateId}/rollback` 请求（`expectedPublishedVersionId` 必填，用于防止并发切换）：

```json
{ "targetVersionId": "00000000-0000-4000-8000-000000000014", "expectedPublishedVersionId": "00000000-0000-4000-8000-000000000015", "reason": "试打发现方向问题" }
```

目标必须是同一单面模板、同园区、已经发布且仍保留的版本。服务端必须在切换前比较 `expectedPublishedVersionId` 与当前发布指针；不一致返回 `409 PUBLISHED_POINTER_CONFLICT`，不得覆盖并发发布。回滚只切换该模板当前发布指针，不修改目标版本的内容和发布时间，也不自动改写任何 `PrintTemplatePair`；成功返回新的当前发布版本。当前草稿不被删除。

### 3.4 预览

`POST /templates/{templateId}/preview` 请求：

```json
{
  "versionId": "00000000-0000-4000-8000-000000000002",
  "sampleData": {
    "visitorName": "示例访客",
    "parkName": "示例园区",
    "visitorCredentialPayload": "opaque-visitor-credential"
  }
}
```

服务端只允许请求方有权使用的模板、字段和资源；真实人员预览必须额外经过人员与照片权限校验。响应 `202`：

```json
{
  "previewId": "00000000-0000-4000-8000-00000000000f",
  "status": "RENDERING",
  "face": "FRONT",
  "sideCount": 1,
  "artifactId": null,
  "pageCount": 1
}
```

单模板 `GET /previews/{previewId}` 返回 `READY`、一个单面制品 hash、`pageCount=1`、尺寸和受控下载地址，或返回 `RENDER_FAILED` 及违规项。预览制品不能被客户端当作正式打印任务直接提交。需要同时预览厂牌两面的场景由 pair 预览分别读取 FRONT/BACK 两个单面版本，每个版本仍只有一页。

### 3.5 厂牌模板对（系统持久关联）

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `GET` | `/template-pairs` | 按园区、人员类型、分类和状态分页查询厂牌模板对。 |
| `POST` | `/template-pairs` | 保存一组 FRONT/BACK 已发布单面版本的组合。 |
| `GET` | `/template-pairs/{pairId}` | 读取模板对及其两面版本摘要。 |
| `PATCH` | `/template-pairs/{pairId}` | 以 `revision` 乐观锁改绑两面版本。 |
| `POST` | `/template-pairs/{pairId}/archive` | 归档模板对并保留历史引用。 |
| `POST` | `/template-pairs/{pairId}/preview` | 按 pair 当前两面版本分别生成两张单页预览，不创建打印任务。 |

本组关联由系统管理页面维护、平台持久保存并校验权限及审计；不是设计器内存状态。创建/改绑成功后，关闭设计器、刷新、重新登录或服务重启，GET 必须返回相同关联及 revision。

`POST /template-pairs` 请求：

```json
{
  "name": "外包厂牌默认组合",
  "printItemType": "STAFF_CARD",
  "personType": "OUTSOURCED",
  "classificationCode": "OUTSOURCE_DEFAULT",
  "frontTemplateVersionId": "00000000-0000-4000-8000-000000000003",
  "backTemplateVersionId": "00000000-0000-4000-8000-000000000014"
}
```

服务端从登录主体和版本归属确定园区并校验两版本均为 `PUBLISHED`、不可变、`sideCount=1`，且分别为 `faceRole=FRONT`/`BACK`；两面尺寸、方向、介质和适用范围必须兼容。成功返回稳定 `pairId`、`revision=0` 和 `status=ACTIVE`。`PATCH` 必须带当前 `revision`，在同一事务内替换两个版本指针并递增 revision；revision 不一致返回 `409 PAIR_REVISION_CONFLICT`。版本号不要求相同。多个兼容 pair 可以共享一个 BACK 版本。

模板对没有独立 draft、版本号、publish 或 rollback；归档只影响新任务选择，历史任务继续使用冻结版本。发布/回滚任一 `Template` 的新版本不会自动改写 pair，管理员必须显式 PATCH 重新绑定。`frontTemplateVersionId` 和 `backTemplateVersionId` 缺一、面角色错误、跨园区/分类或资源校验失败时，返回 `422 TEMPLATE_VALIDATION_FAILED` 或 `403 PRINT_SCOPE_DENIED`。

`POST /template-pairs/{pairId}/preview` 请求只携带合成或经授权的 `sampleData` 和当前 `revision`（如客户端已缓存），服务端在 pair 级别校验园区和权限后锁定当前两面指针。响应列出 `FRONT`、`BACK` 两个单面版本和各自 `pageCount=1` 的制品；它不是 pairVersion，也不会改变 pair revision，预览制品不能直接提交设备。

### 3.6 模板静态图片

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `POST` | `/resources?parkId=...` | 上传单个PNG/JPEG，返回服务器登记的资源清单项。 |
| `GET` | `/resources/{objectId}?parkId=...` | 按当前园区与资源权限下载不可变图片。 |

上传请求体为原始图片字节，`Content-Type` 为 `image/png` 或 `image/jpeg`；`X-Print-Resource-Purpose` 为 `LOGO` 或 `BACKGROUND`，默认 `BACKGROUND`。需要模板写入权限，园区仍从已登录主体的授权集合验证。单张最多20MiB、宽高各不超过4096且总像素不超过16,000,000；超字节限制返回413，内容不完整/类型伪造/尺寸超限返回422。上传不授予人员照片权限，`PHOTO` 用途拒绝。

成功201的 `data` 为服务端生成的 `{objectId, contentHash, mediaType, sizeBytes, parkId, purpose, accessScope:"TEMPLATE"}`，客户端不能自行声明这些授权元数据。画布保存时使用 `resourceRef={objectId,contentHash}` 和对应 `resourceManifest`，不得持久化 data URI。重开时以当前登录态下载、核验hash后仅在画布内还原图片；下载返回真实MIME、`Cache-Control:no-store`、`X-Content-Type-Options:nosniff` 和 `X-Artifact-Sha256`。

底图/Logo与预览PDF共用 `SMT_PRINT_OBJECT` 私有存储。预览PDF为 `PRINT_PREVIEW` 作用域，只能经所属预览的制品端点下载，不能通过上述模板资源端点读取。预览的全部制品与元数据同事务提交；模板上传后尚未保存关联的图片保留为独立对象，清理策略须在正式容量及发布验收时确认，不由浏览器删除对象。

## 4. 绑定规则 API

### 4.1 端点

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `GET` | `/bindings` | 按园区、打印物、人员类型、分类、scope 分页查询。 |
| `POST` | `/bindings` | 创建公司、供应商或明确默认绑定；正式员工必须带适用职级。 |
| `GET` | `/binding-options/employee-grades` | 返回经验证的人事职级代码/名称选项，受园区和规则管理权限限制；来源未确认时返回明确错误，不造一份静态等级字典。 |
| `PATCH` | `/bindings/{bindingRuleId}` | 以 `revision` 乐观锁修改。 |
| `POST` | `/bindings/{bindingRuleId}/disable` | 停用规则并保留审计。 |
| `GET` | `/bindings/resolve` | 在打印前只读解析并返回模板/版本摘要，不创建任务。 |

`POST /bindings` 请求：

```json
{
  "parkId": "1",
  "printItemType": "STAFF_CARD",
  "personType": "OUTSOURCED",
  "classificationCode": "OUTSOURCE_DEFAULT",
  "scopeType": "COMPANY",
  "scopeId": "company_123",
  "pairId": "00000000-0000-4000-8000-000000000016",
  "priority": 100,
  "validFrom": "2026-09-05T00:00:00Z",
  "validTo": null
}
```

正式员工绑定增加职级条件，同一组合可选择多个职级。以下 `sample-*` 仅为合成示例，正式编码必须取自经确认的人事字典，不把员工级、职员级、经理级的名称或示例码写死：

```json
{
  "parkId": "1",
  "printItemType": "STAFF_CARD",
  "personType": "EMPLOYEE",
  "classificationCode": "STAFF_DEFAULT",
  "scopeType": "EXPLICIT_DEFAULT",
  "scopeId": null,
  "employeeGradeCodes": ["sample-worker-level"],
  "pairId": "00000000-0000-4000-8000-000000000026",
  "priority": 100,
  "validFrom": "2026-09-05T00:00:00Z",
  "validTo": null
}
```

`employeeGradeCodes` 在正式员工厂牌规则中必填，包含 1–100 个已知且去重的稳定代码，每个 1–64 字符，默认规则也不例外；其他人员类型必须省略或为 null。名称仅展示，不允许通配/ALL 或按字符串、数值推测等级范围。保存的持久字段见 BindingRule；同园区/公司范围、同优先级且有效期重叠的规则，其职级集合相交时返回 `409 BINDING_AMBIGUOUS`，集合不相交则可并存。

访客绑定示例只填写 `templateId`，不填写 `pairId`：

```json
{
  "parkId": "1",
  "printItemType": "VISITOR_SLIP",
  "personType": "VISITOR",
  "classificationCode": "VISITOR_SECURITY",
  "scopeType": "EXPLICIT_DEFAULT",
  "scopeId": null,
  "templateId": "00000000-0000-4000-8000-000000000020",
  "priority": 100,
  "validFrom": "2026-09-05T00:00:00Z",
  "validTo": null
}
```

服务端必须验证目标同园区、同打印物、同人员类型/分类，并检查当前发布版本。`STAFF_CARD` 绑定只允许 `pairId`，`VISITOR_SLIP` 绑定只允许 `templateId`；两字段必须按打印物互斥（XOR），同时提供或同时缺失返回 `400 INVALID_REQUEST`。规则解析顺序固定为：同园区/打印物/人员类型/分类 → 正式员工职级候选过滤 → 具体公司或供应商最高优先级 → 同职级适用的 `EXPLICIT_DEFAULT` 明确默认。最高优先级命中多条规则（即使指向同一目标）返回 `409 BINDING_AMBIGUOUS`，不能按创建时间或数据库顺序选一个。厂牌 pair 必须是 `ACTIVE` 且两面版本均有效；保密访客只能解析到包含二维码组件并绑定受控 `visitorCredentialPayload` 的 FRONT 模板；当前源码已确认平台适配层沿用现有 `smsCode` 扫码语义，无 payload 来源时返回 `422 SECURITY_QR_TEMPLATE_REQUIRED`。

`GET /bindings/resolve` 必须至少带 `printItemType`、`personType`、`classificationCode`、`subjectId`；服务端从人员/申请确定公司或供应商和园区，并从已确认的人事来源取得正式员工职级，不接受浏览器传入职级覆盖人员数据。职级缺失返回 `422 EMPLOYEE_GRADE_REQUIRED`，未知或来源映射未完成返回 `422 EMPLOYEE_GRADE_UNMAPPED`；没有对应职级规则返回 `404 EMPLOYEE_GRADE_TEMPLATE_NOT_FOUND`，表示无法自动匹配；页面提供手动选择入口，可选择未关联的已发布模板。自动匹配不退回无职级或其他职级模板。响应返回：

```json
{
  "selection": {
    "kind": "BOUND",
    "pairId": "00000000-0000-4000-8000-000000000016",
    "frontTemplateVersionId": "00000000-0000-4000-8000-000000000003",
    "backTemplateVersionId": "00000000-0000-4000-8000-000000000014"
  },
  "pairRevision": 3,
  "bindingRuleId": "00000000-0000-4000-8000-000000000010",
  "matchLevel": "SPECIFIC_COMPANY",
  "securityQrRequired": false,
  "frontContentHash": "sha256:…",
  "backContentHash": "sha256:…"
}
```

正式员工解析响应额外返回 `employeeGradeCode`、`employeeGradeName`、`employeeGradeSource`、`bindingRevision` 及匹配依据；代码用于决策，名称用于核对。创建任务时冻结这些字段及匹配规则快照，员工晋升或管理员改规则不会改变已有任务。`PAIR` 或 `EXPLICIT` 不要求职级/公司适用规则命中，EXPLICIT 两份版本不要求对应已保存组合。职级仍从服务端人员资料读取并展示，自动解析未命中/歧义或手选与推荐不同只作为核对提示；人员职级缺失/未知、权限或资料校验失败仍不能继续。

访客解析响应的 `selection.kind` 仍为 `BOUND`，但只返回 `templateId`、`frontTemplateVersionId` 和 `frontContentHash`，并省略 `pairId`/BACK 字段。零命中返回 `404 TEMPLATE_NOT_FOUND`；歧义、停用、过期、无发布版本、pair 任一面失效或园区不一致均不得返回兜底模板。

## 5. 打印机档案 API

### 5.1 端点

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `GET` | `/printer-profiles` | 查询当前园区设备档案。 |
| `POST` | `/printer-profiles` | 创建档案。 |
| `PATCH` | `/printer-profiles/{printerProfileId}` | 以 `configRevision` 乐观锁修改。 |
| `POST` | `/printer-profiles/{printerProfileId}/verification` | 记录官方资料/驱动/翻面模块/实机验收证据并更新能力状态。 |
| `POST` | `/printer-profiles/{printerProfileId}/disable` | 禁用或隔离设备。 |

档案请求至少包含 `deviceIdentity`、`parkId`、`manufacturer`、`model`、`deviceType`、`connectionType`、`allowedPrintModes`、`defaultPrintMode`、`capabilityStatus`、`flipCapability`、`mediaSpec`、`calibration` 和 `capabilityEvidence`。同一工作站 `deviceIdentity` 可以关联多个 `printerProfileId`，不能按身份唯一限制实际打印机；`PATCH` 必须携带当前 `configRevision`，已有 job 不受修改影响。

设备规则：

- Brother QL-800 访客档案只能允许 `SINGLE`，`mediaSpec.maxPrintableWidthMm` 必须记录 58；平台输出为经校准的 PDF 面制品，客户端再转图片交给 b-PAC，不能把 PDF bytes 当作原始打印命令。
- HiTi 呈妍设备用户称 `CS220` 时，档案 `model` 可先记录现场铭牌值；未确认是否为 `CS-220e` 时 `capabilityStatus=UNVERIFIED`。Windows/驱动版本、介质/双面、状态回传和实机结果没有证据时必须保持 `UNVERIFIED`。
- `AUTO_DUPLEX` 只有 `capabilityStatus=VERIFIED`、`flipCapability=AUTO_VERIFIED` 且有有效翻面模块证据时才可加入 `allowedPrintModes`；否则创建任务返回 `422 PRINTER_CAPABILITY_UNVERIFIED`，允许操作员改选 `MANUAL_DUPLEX` 后重新创建任务。
- `activeJobId` 是物理占用，不因连接租约过期清除。档案修改不能在活动 job 中途改变其模式或方向。

## 6. 打印任务 API

### 6.1 端点

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `POST` | `/print-jobs` | 为一个人员创建一人一张卡/凭条的任务。 |
| `POST` | `/print-jobs/batch` | 批量请求；服务端按人拆 job，整体校验后一次创建。 |
| `GET` | `/print-jobs` | 按园区、人员、打印物、设备、状态和时间分页查询。 |
| `GET` | `/print-jobs/{jobId}` | 查询任务、每面进度、尝试摘要和当前动作。 |
| `POST` | `/print-jobs/{jobId}/flip-confirmation` | 手动翻面确认并创建唯一 BACK attempt。 |
| `POST` | `/print-jobs/{jobId}/output-check` | 人工确认已出、未出或卡损坏，并明确继续/取消。 |
| `POST` | `/print-jobs/{jobId}/cancel` | 取消未完成任务；有物理风险时等待设备清空。 |
| `GET` | `/print-jobs/{jobId}/events` | 读取受权限保护的审计事件。 |
| `GET` | `/print-jobs/{jobId}/artifacts/{face}/download` | 向获准客户端或管理员下载冻结面制品。 |
| `GET` | `/print-jobs/{jobId}/artifacts/combined/download` | 向获准客户端或管理员下载厂牌 FRONT→BACK 合并制品。 |

### 6.2 创建任务

`POST /print-jobs` 请求：

```json
{
  "parkId": "1",
  "printItemType": "STAFF_CARD",
  "subjectType": "STAFF",
  "subjectId": "123",
  "previewId": "00000000-0000-4000-8000-000000000071",
  "printerProfileId": "00000000-0000-4000-8000-000000000005",
  "printMode": "MANUAL_DUPLEX",
  "selection": {
    "kind": "PAIR",
    "pairId": "00000000-0000-4000-8000-000000000016",
    "pairRevision": 1,
    "manualSelectionConfirmed": true
  }
}
```

`selection.kind` 取 `BOUND`、`PAIR`、`EXPLICIT`：

| kind | `STAFF_CARD` | `VISITOR_SLIP` |
| --- | --- | --- |
| `BOUND` | 按绑定规则解析 `pairId`，取得 FRONT/BACK 两个当前已发布版本。 | 按绑定规则解析 `templateId`，取得一个 FRONT 当前已发布版本。 |
| `PAIR` | 必须提供 `pairId`、操作员预览确认的 `pairRevision` 和 `manualSelectionConfirmed=true`；服务端复核修订、园区、分类、ACTIVE 状态和两面版本，无需存在适用绑定。 | 不允许。 |
| `EXPLICIT` | 必须提供已确认的 `frontTemplateVersionId`、`backTemplateVersionId` 和 `manualSelectionConfirmed=true`；无需已保存组合或适用绑定。提供可选 `pairId` 时须同时提供 `pairRevision`。 | 必须提供 `frontTemplateVersionId` 和 `manualSelectionConfirmed=true`，省略 BACK 和 `pairId`。 |

`EXPLICIT` 厂牌请求示例：

```json
{
  "kind": "EXPLICIT",
  "manualSelectionConfirmed": true,
  "frontTemplateVersionId": "00000000-0000-4000-8000-000000000003",
  "backTemplateVersionId": "00000000-0000-4000-8000-000000000014"
}
```

显式选择的两份版本必须分别属于同园区、同打印物/人员类型/分类的 FRONT/BACK 模板，均为已发布不可变单页版本；若同时提供 `pairId`/`pairRevision`，服务端还必须确认该 pair 当前指针与显式版本完全一致。显式选择不要求绑定规则存在，但必须通过园区、打印物/人员分类、资源、照片、已发布状态、两面兼容和保密二维码校验。手选候选来自现有模板/组合查询接口的权限范围及已发布状态过滤，不以“有绑定”为查询条件，新发布而未关联的模板必须返回。

`PAIR`/`EXPLICIT` 均为手动选择，与打印机的手动/自动翻面模式无关；必须携带 `manualSelectionConfirmed=true`，缺失或 false 返回 `422 MANUAL_SELECTION_CONFIRMATION_REQUIRED`。前端展示人员/职级、实际模板名称及版本、正反面（访客单面）预览，提示未关联或与自动推荐不同，操作员核对后提交；改变人员、模板或组合修订必须重新确认。PAIR 预览后修订变化返回 `409 PAIR_REVISION_CONFLICT`，不隐式采用新两面。已有自动推荐时也允许改选；新模板没有适用绑定或 EXPLICIT 没有组合均不得因此拒绝。

手选快照由服务端记录自动推荐（如有）或无匹配/歧义原因、实际选择及登录主体/服务器确认时间；权限、人员资料错误、接口异常不能被吞掉并记录为无匹配。自动结果只是手选参考，不强制等于实际选择；没有实际使用规则时 `bindingRuleId` 为 null。手选只创建本次任务，不创建或修改组合/绑定。

服务端按以下顺序原子处理：校验登录主体和园区 → 读取人员/申请最小数据 → 按 selection 解析并校验目标 → 检查分类、保密二维码、资源和档案能力/模式 → 冻结同一人员、两份（或一份访客）模板版本、各自布局/输入/资源、可选 pair revision、打印机档案和模式 → 与同园区同操作员的 READY 实际人员预览确认指纹比较 → 创建 `PrintJob` 为 `QUEUED` → 异步可信渲染。请求中的人员字段、布局和 PDF 不作为信任来源；显式版本 ID 也必须经过服务端校验，不能绕过园区、分类、资源、照片或保密二维码规则。

`STAFF_CARD` 必须 `MANUAL_DUPLEX` 或 `AUTO_DUPLEX` 且 `expectedFaceCount=2`，在任务创建前选全 FRONT/BACK 两个已发布单面版本，并且人员必须存在有效的已登记卡关系和非空 `cardNo`；缺卡号直接返回校验错误。`MANUAL_DUPLEX` 与 `AUTO_DUPLEX` 都把两份版本、布局、输入、资源和可选 `pairRevision` 一起冻结；翻面等待中不能重新选择。`VISITOR_SLIP` 忽略/拒绝任何双面模式，固定 `SINGLE`、`expectedFaceCount=1`，只冻结一个 FRONT 版本，不需要实体卡 ID。员工/外包/其他人员按一人一卡创建；访客主访客与随行人员分别按一人一凭条创建。成功返回 `202` 和 `status=QUEUED`，不表示已打印。

`POST /print-jobs/batch` 请求：

```json
{
  "printerProfileId": "00000000-0000-4000-8000-000000000005",
  "printMode": "MANUAL_DUPLEX",
  "selection": {
    "kind": "BOUND"
  },
  "subjects": [
    { "subjectType": "STAFF", "subjectId": "123", "previewId": "00000000-0000-4000-8000-000000000071" },
    { "subjectType": "STAFF", "subjectId": "456", "previewId": "00000000-0000-4000-8000-000000000072" }
  ]
}
```

批量手选时，前端逐人展示职级、所选版本及预览和推荐差异，确认范围覆盖本批全部人员，不能把第一人的自动匹配冒充所有人的匹配。服务端先完整校验所有人员的园区、selection 目标、必填、照片、卡号、两面版本、模式和设备能力；任一失败时整体返回 `422` 并创建 0 个 job，`details.items[]` 标出失败人员。全部通过后每个人创建一个独立 job，返回 `202` 的 `jobs[]`。客户端按 job 串行当前卡，不允许先提交整批 FRONT 再整批 BACK；批量任务的两面选择在创建时一次冻结。

### 6.3 手动翻面确认

仅 `MANUAL_DUPLEX` 且 job 为 `AWAITING_FLIP` 时可调用 `POST /print-jobs/{jobId}/flip-confirmation`：

```json
{
  "attemptId": "00000000-0000-4000-8000-000000000006",
  "operatorId": "user_123",
  "orientationConfirmed": true,
  "message": "已按设备校准方向翻面"
}
```

服务端校验当前 job、同一人员、已冻结的 FRONT/BACK 模板版本、pair revision（如有）、打印机、FRONT attempt、操作权限和 `orientationConfirmed`，原子写入 `FLIP_CONFIRMED` 事件并创建唯一 BACK attempt，将状态改为 `BACK_IN_PROGRESS`。翻面确认只推进已冻结的 BACK，不能在此时重新选择模板或版本。响应返回：

```json
{
  "jobId": "00000000-0000-4000-8000-000000000004",
  "status": "BACK_IN_PROGRESS",
  "attemptId": "00000000-0000-4000-8000-000000000007",
  "commandId": "00000000-0000-4000-8000-000000000008",
  "face": "BACK",
  "artifact": { "face": "BACK", "downloadPath": "/api/print-client/v1/jobs/00000000-0000-4000-8000-000000000004/artifacts/BACK/download", "sha256": "sha256:…" }
}
```

相同 `Idempotency-Key`、相同 body 重复点击返回同一 BACK attempt，不再创建新 attempt；body 不同返回 `409 IDEMPOTENCY_KEY_REUSED`。翻面确认只表示操作员确认翻面，不表示背面已打印。返回的 `commandId` 仅供客户端后续物理事件关联。

### 6.4 输出人工核对和结果不明

`POST /print-jobs/{jobId}/output-check` 请求：

```json
{
  "attemptId": "00000000-0000-4000-8000-000000000007",
  "face": "BACK",
  "decision": "CONFIRMED_NOT_OUT",
  "resolution": "CONTINUE",
  "operatorId": "user_123",
  "physicalCheck": { "state": "NO_CARD_IN_DEVICE", "operatorNote": "设备内无卡，卡槽已清空" },
  "reason": "驱动返回超时后现场确认未出卡"
}
```

`physicalCheck.state` 取 `NO_CARD_IN_DEVICE`、`CARD_IN_DEVICE` 或 `STATE_UNKNOWN`；`operatorNote` 记录现场核对说明。请求只使用 `Idempotency-Key` 去重，不携带打印 `commandId`；若 `CONTINUE` 被允许，服务端在同一事务内生成新的 attempt 和新的 `commandId` 并在响应中返回。

`decision` 取值及服务器行为：

| decision | 必要 resolution | 服务端行为 |
| --- | --- | --- |
| `CONFIRMED_OUT` | `NONE` | 标记该面已出；手动 FRONT 转 `AWAITING_FLIP`，BACK/单面/自动在所有期望面确认后转 `COMPLETED`。 |
| `CONFIRMED_NOT_OUT` | `CONTINUE` 或 `CANCEL` | 满足下述续打证据且 `physicalCheck.state=NO_CARD_IN_DEVICE` 时，显式 `CONTINUE` 创建新的 attempt 和新的 `commandId`，进入相应 `*_IN_PROGRESS`；否则返回 `409 OUTPUT_CHECK_REQUIRED`，保持核对或 `CANCEL`。不得复用旧命令。 |
| `CONFIRMED_DAMAGED` | `CANCEL` | 记录卡损坏/方向错误，取消当前 job；更换卡重打必须创建新 job。 |

续打证据允许两种情况：①驱动明确拒绝且 `submissionAccepted=false`，没有已接受/已完成或未知提交的冲突证据；②手动模式中，操作员核对同一张卡的当前面确实未印，原执行客户端已持久化停止该命令，并确认原驱动/设备队列已终止清空，提交 `COMMAND_RETIRED` 证据。第二种情况可处理回执丢失后的人工续打，但不能只凭超时或“没看到卡”判断。服务端把人工核对关联到原 attempt，旧 command 永不再执行；旧 attempt 的迟到事件只追加审计，不覆盖新 attempt 进度，若出现相矛盾的出卡证据则暂停当前 job 并重新核对。无法终止或核实旧命令时禁止续打；自动模式只允许第一种可证明未提交的情况。

`CONFIRMED_NOT_OUT` 不能自动发送新命令；`CONTINUE` 是操作员明确决定且必须生成新的 attempt/new command。自动双面在不能确认两面均未出时不得选择继续，只能人工核对后完成或取消。缺少 `physicalCheck`、原 attempt、当前面或权限时返回 `422 OUTPUT_CHECK_INCOMPLETE`。输出结果不明时任务保持 `RESULT_UNKNOWN`，直到该接口完成人工处理。

### 6.5 取消

`POST /print-jobs/{jobId}/cancel` 请求：

```json
{ "reason": "现场卡损坏" }
```

`QUEUED`、`RENDERING`、`READY` 可直接取消；已提交设备或处于 `AWAITING_FRONT_CHECK`、`AWAITING_FLIP`、`AWAITING_OUTPUT_CHECK`、`RESULT_UNKNOWN` 时，服务器先记录取消意图和物理清空要求。只要设备内状态未确认安全，`PrinterProfile.activeJobId` 继续保持，不能让下一任务开始。取消不重放在途命令、不删除模板历史和任务事件。

## 7. Windows 打印客户端 API

### 7.1 Claim 与连接租约

`POST /api/print-client/v1/claim` 请求：

```json
{
  "deviceIdentity": "win-gate-01",
  "printerProfileId": "00000000-0000-4000-8000-000000000005",
  "clientInstanceId": "00000000-0000-4000-8000-00000000000d",
  "clientVersion": "0.1.0",
  "supportedPrintModes": ["SINGLE", "MANUAL_DUPLEX"],
  "capabilitySnapshotHash": "sha256:…",
  "resumeJobId": "00000000-0000-4000-8000-000000000004"
}
```

`resumeJobId` 可选，仅用于客户端重启或断线恢复。服务端校验设备身份、园区、档案版本、客户端版本和模式；若档案 `activeJobId` 属于其他 job，返回 `409 PRINTER_OCCUPIED`，不得靠租约过期接管；若 `resumeJobId` 等于档案当前 `activeJobId` 且设备/档案授权匹配，则返回原 claim、当前 action 和原 attempt，不创建新 attempt。档案空闲且有 READY 任务时，服务端在同一事务内先原子写入 `activeJobId`、分配 `commandId` 和 attempt，再返回 `200`：

```json
{
  "claimId": "00000000-0000-4000-8000-00000000000c",
  "leaseExpiresAt": "2026-09-05T12:01:00Z",
  "jobId": "00000000-0000-4000-8000-000000000004",
  "status": "FRONT_IN_PROGRESS",
  "action": {
    "type": "PRINT_FRONT",
    "face": "FRONT",
    "attemptId": "00000000-0000-4000-8000-000000000006",
    "commandId": "00000000-0000-4000-8000-00000000000a",
    "artifact": {
      "downloadPath": "/api/print-client/v1/jobs/00000000-0000-4000-8000-000000000004/artifacts/FRONT/download",
      "sha256": "sha256:…",
      "pageWidthMm": 85.6,
      "pageHeightMm": 53.98
    }
  }
}
```

`action.type` 取 `PRINT_FRONT`、`PRINT_BACK`、`PRINT_BOTH`、`WAIT_FRONT_CHECK`、`WAIT_FLIP`、`WAIT_OUTPUT_CHECK`、`RESULT_UNKNOWN`、`NONE`。`NONE` 返回 `204` 或 `200 data.action.type=NONE`，两者由网关约定但不能创建虚假 job。自动双面只返回 `PRINT_BOTH`，手动背面只返回 `PRINT_BACK`，访客只返回 `PRINT_FRONT`。

客户端收到 action 后必须先把 `jobId`、`attemptId`、`commandId`、面、制品 hash、模板版本 hash、设备档案 hash 和本地时间写入持久日志，并持久化标记 `SUBMISSION_STARTED`，再调用 b-PAC/HiTi 驱动。`INTENT_RECORDED` 只是事件回传，不是是否已提交的唯一依据；客户端崩溃恢复时若看到 `SUBMISSION_STARTED` 而没有可信完成证据，必须把原 attempt 上报为 `RESULT_UNKNOWN`，不能因服务端尚未收到 `INTENT_RECORDED` 重发或创建替代 command。

`POST /api/print-client/v1/claims/{claimId}/renew` 请求带 `deviceIdentity`、`clientInstanceId` 和当前 `leaseExpiresAt`，只续连接租约，不改变 `activeJobId`。租约失效后原 job 仍被占用；重新连接必须使用 `GET /claims/{claimId}/current` 或新的经过恢复校验的 claim 读取原 attempt。

`GET /api/print-client/v1/claims/{claimId}/current` 返回当前 job、attempt、action 和服务端状态。存在 `INTENT_RECORDED`、`SENT`、`UNKNOWN` 的 command 时，必须返回原 `commandId`；不能因为客户端重启生成新的打印 command。

### 7.2 制品下载

`GET /api/print-client/v1/jobs/{jobId}/artifacts/{face}/download` 只允许该 job 的获准设备身份或有权限的管理员访问。`face` 统一取 `FRONT` 或 `BACK`；自动双面也可分别下载两个冻结面制品，`BOTH` 只用于任务 attempt/action，不是下载路径值，也不得自行拼接未冻结页面。厂牌需要整份正式输出时使用固定路径 `GET /api/print-client/v1/jobs/{jobId}/artifacts/combined/download`，服务端只返回渲染器生成并校验过的 FRONT→BACK 双页制品；客户端不能自行把不同版本的页面拼接后提交。

成功返回 PDF 字节流及以下响应头：

```text
Content-Type: application/pdf
Content-Length: <bytes>
ETag: "sha256:..."
X-Artifact-Sha256: sha256:...
X-Job-Id: 00000000-0000-4000-8000-000000000004
X-Face: FRONT
```

客户端必须校验 hash、任务/版本/档案快照 hash 和页面尺寸。QL-800 路径由客户端按档案 DPI、介质及 58mm 有效宽度把 PDF 面渲染成图片，再交 b-PAC；HiTi 路径交官方 Windows 驱动。下载接口不返回可任意改写的模板源，不接受客户端上传替代制品。

### 7.3 客户端事件

`POST /api/print-client/v1/jobs/{jobId}/events` 请求：

```json
{
  "eventId": "00000000-0000-4000-8000-00000000000b",
  "commandId": "00000000-0000-4000-8000-00000000000a",
  "attemptId": "00000000-0000-4000-8000-000000000006",
  "eventType": "DEVICE_ACCEPTED",
  "clientSequence": 18,
  "occurredAt": "2026-09-05T12:00:30Z",
  "artifactHash": "sha256:…",
  "driverJobKey": "driver-123",
  "payload": { "queueAccepted": true }
}
```

允许的客户端事件：`INTENT_RECORDED`、`COMMAND_SENT`、`DRIVER_REJECTED`、`DEVICE_ACCEPTED`、`DEVICE_COMPLETED`、`DEVICE_DISCONNECTED`、`OUTPUT_UNKNOWN`、`COMMAND_RETIRED`、`DEVICE_CLEARED`。客户端不能直接上报 `COMPLETED`、伪造人工 `CONFIRMED_OUT` 或绕过翻面状态；服务器将事件转换为第 5 节模型中的状态。

事件处理规则：

- `DEVICE_ACCEPTED` 只能把 FRONT/BACK/AUTO 从 in-progress 转为等待核对状态；不把队列接受当作出卡。
- `DEVICE_COMPLETED` 若缺少可信的两面/单面证据，只能进入 `AWAITING_OUTPUT_CHECK`；无法判断则 `RESULT_UNKNOWN`。
- `DRIVER_REJECTED` 即使明确表明未提交也进入 `RESULT_UNKNOWN`，由操作员核对；只有在安全未提交条件满足时，`output-check` 的 `CONTINUE` 才能创建新的 attempt/new command。调用已经发生但结果不明时同样进入 `RESULT_UNKNOWN`。
- `DEVICE_DISCONNECTED`、`OUTPUT_UNKNOWN` 不触发重试，保持 `activeJobId`。
- `DEVICE_CLEARED` 只记录设备清空证据；只有 job 已为 `COMPLETED`、`CANCELLED` 或 `FAILED` 且无待打印背面时才释放 `activeJobId`。`RESULT_UNKNOWN` 或仍有待打印面时不能释放；连接租约续期不能替代该事件。
- 重复 `eventId` 且 body 相同返回原状态；同一 `commandId` 可以提交多个不同事件，不能把后续事件按 commandId 去重。eventId body 不同返回 `409 IDEMPOTENCY_KEY_REUSED`；人工动作 body 不同由其 `Idempotency-Key` 返回同一错误。`COMMAND_RETIRED` 只记录本地命令禁止再执行及队列清空证据，不能自行创建重试；事件乱序不能越过服务端当前状态，返回 `409 JOB_STATE_CONFLICT` 并要求客户端读取 current。

成功响应包含：

```json
{
  "jobId": "00000000-0000-4000-8000-000000000004",
  "status": "AWAITING_FRONT_CHECK",
  "eventAccepted": true,
  "replayed": false,
  "nextAction": "CHECK_FRONT_OUTPUT"
}
```

### 7.4 设备清空

`POST /api/print-client/v1/jobs/{jobId}/device-cleared` 请求：

```json
{
  "deviceIdentity": "win-gate-01",
  "physicalState": "NO_CARD_IN_DEVICE",
  "operatorCheckId": "00000000-0000-4000-8000-000000000013",
  "reason": "结果不明后现场取出并核对"
}
```

设备清空接口仅记录 `NO_CARD_IN_DEVICE` 或已确认安全取卡的证据；只有 job 已为 `COMPLETED`、`CANCELLED` 或 `FAILED` 且无待打印面时才清除 `activeJobId`。其他状态记录证据但继续占用。设备仍有卡、卡状态不明或 `operatorCheckId` 无效时返回 `409 DEVICE_CLEARANCE_REQUIRED`。清空不代表任务成功，成功/未出/损坏仍需通过 `output-check` 解决。

## 8. 可信渲染内部 API

### 8.1 请求

`POST /internal/print-renderer/v1/render` 只允许 smart-platform 服务身份调用。`purpose=PRINT` 使用任务冻结快照并要求 `jobId`；每个 `faceSources` 元素必须带已发布不可变的 `templateVersionId`，不得带 `templateId`/`draftRevision`。`purpose=PREVIEW` 使用已授权的草稿或已发布版本和预览数据，要求 `previewId`，不创建 job、不领取设备；草稿预览不要求已发布版本。预览的每个 `faceSources` 元素必须二选一：草稿使用 `templateId` + `draftRevision`，已发布版本使用 `templateVersionId`，不能同时提供或全部省略；pair 预览使用两份已发布版本。单模板预览允许只传一个 `FRONT` 或 `BACK` 面，访客只能传 `FRONT`；正式厂牌打印和厂牌 pair 预览才要求同时传 `FRONT`、`BACK`。两种请求都带 `requestId`，并以 `faceSources` 明确每个面的一份单面模板、解析输入和资源清单。以下为正式厂牌打印示例：

```json
{
  "requestId": "render_req_01J...",
  "purpose": "PRINT",
  "jobId": "00000000-0000-4000-8000-000000000004",
  "printItemType": "STAFF_CARD",
  "printMode": "MANUAL_DUPLEX",
  "expectedFaceCount": 2,
  "pairId": "00000000-0000-4000-8000-000000000016",
  "pairRevision": 3,
  "faceSources": [
    {
      "face": "FRONT",
      "templateVersionId": "00000000-0000-4000-8000-000000000003",
      "template": {
        "schemaVersion": 1,
        "faceRole": "FRONT",
        "sideCount": 1,
        "basePdf": { "width": 85.6, "height": 53.98, "padding": [0, 0, 0, 0] },
        "pageSpecJson": { "widthMm": 85.6, "heightMm": 53.98, "orientation": "LANDSCAPE", "maxPageCount": 1 },
        "schemas": [[]]
      },
      "resolvedInput": {
        "subjectId": "123",
        "subjectType": "STAFF",
        "fields": { "staffName": "示例员工", "staffNo": "E001" }
      },
      "resourceManifest": []
    },
    {
      "face": "BACK",
      "templateVersionId": "00000000-0000-4000-8000-000000000014",
      "template": {
        "schemaVersion": 1,
        "faceRole": "BACK",
        "sideCount": 1,
        "basePdf": { "width": 85.6, "height": 53.98, "padding": [0, 0, 0, 0] },
        "pageSpecJson": { "widthMm": 85.6, "heightMm": 53.98, "orientation": "LANDSCAPE", "maxPageCount": 1 },
        "schemas": [[]]
      },
      "resolvedInput": {
        "subjectId": "123",
        "subjectType": "STAFF",
        "fields": { "staffName": "示例员工", "staffNo": "E001" }
      },
      "resourceManifest": []
    }
  ]
}
```

内部模板可附带 `fieldSchemaJson: { fields: [{ key, schemaName, required }] }`，其中 `key` 属于平台业务白名单、`schemaName` 指向该面组件名称；平台在业务授权后提供 `resolvedInput.fields`，渲染器逐面映射，禁止跨面覆盖。`resourceManifest` 中的图片在平台完成对象授权后增加 `contentBase64`，与 `objectId`、`contentHash`、`mediaType` 一起传输；该字段仅存在于可信内部请求，不允许浏览器绕过对象上传和权限校验。

平台在发起请求前已完成园区/权限/字段/资源授权和 hash 校验；渲染服务不查询人员库、模板库、文件服务或公网 URL，也不接受浏览器传入的最终 PDF。`faceSources` 的每个元素必须是一个 `sideCount=1`、`schemas` 恰好一页的模板，并按上面的用途规则带 `templateVersionId` 或 `templateId`/`draftRevision`。每个元素还必须携带自己的 `pageSpecJson` 和内联 `basePdf`：`basePdf.width`/`height` 使用与 `pageSpecJson.widthMm`/`heightMm` 相同的毫米数值，`padding` 为四个非负数；渲染器只消费这个内联对象，不解析 `basePdfRef`。持久化 `layoutJson` 中的 `basePdfRef` 由 smart-platform 在调用前按授权资源和 contentHash 解析；无底图时生成上述固定尺寸对象，有底图但无法转换为当前渲染器支持的内联形状时拒绝请求，不把 URL、路径或未授权字节传入渲染器。FRONT/BACK 的字段解析彼此隔离，即使字段键相同也不能写入同一共享 map 造成覆盖。实现可分别渲染每面后合并，具体内部函数由渲染器实现决定。

### 8.2 响应

成功返回 `200`：

```json
{
  "renderRequestId": "render_req_01J...",
  "jobId": "00000000-0000-4000-8000-000000000004",
  "status": "READY",
  "manifestHash": "sha256:…",
  "artifacts": [
    {
      "artifactId": "00000000-0000-4000-8000-000000000011",
      "face": "FRONT",
      "mediaType": "application/pdf",
      "sha256": "sha256:…",
      "bytes": 123456,
      "contentBase64": "...",
      "pageCount": 1,
      "widthMm": 85.6,
      "heightMm": 53.98
    },
    {
      "artifactId": "00000000-0000-4000-8000-000000000012",
      "face": "BACK",
      "mediaType": "application/pdf",
      "sha256": "sha256:…",
      "bytes": 120000,
      "contentBase64": "...",
      "pageCount": 1,
      "widthMm": 85.6,
      "heightMm": 53.98
    }
  ],
  "combinedArtifact": {
    "artifactId": "00000000-0000-4000-8000-000000000013",
    "mediaType": "application/pdf",
    "sha256": "sha256:…",
    "bytes": 243456,
    "contentBase64": "...",
    "pageCount": 2
  }
}
```

每面 `contentBase64` 是该单面 PDF 的完整字节编码，双面请求的 `combinedArtifact.contentBase64` 是 FRONT、BACK 按顺序合并后的双页 PDF，示例省略实际内容；平台解码并复核所有 `bytes`、hash、页数、尺寸后保存到自身受控文件存储，不依赖渲染器本地路径或共享磁盘。手动模式按面下载和提交，自动模式可使用合并制品或按客户端能力分别提交，但两种模式都必须使用同一任务冻结的两份版本。单面请求只返回一个面制品，不返回 `combinedArtifact`。预览响应用 `previewId` 代替 `jobId`，制品仅向获准预览主体开放，不能用于正式设备任务。

`purpose=PRINT` 时，`STAFF_CARD` 必须同时提供 FRONT 和 BACK，各自 `pageCount=1`，并返回按 FRONT→BACK 合并的 `combinedArtifact.pageCount=2`；`VISITOR_SLIP` 只能提供 FRONT，返回一个 `pageCount=1` 的单面制品。`purpose=PREVIEW` 的单模板预览允许只提供 FRONT 或只提供 BACK，返回一个 `pageCount=1` 的制品且不返回合并制品；pair 预览必须同时提供 FRONT 和 BACK，并返回两个单面制品及 `combinedArtifact.pageCount=2`。访客在打印和预览中都只能使用 FRONT。两面尺寸、方向和介质必须通过 pair/任务创建时校验。渲染服务发现字体缺失、字段缺失、元素越界、二维码/条码不可生成、单面页数不为 1 或合并页数不符时返回 `422 RENDER_VALIDATION_FAILED`；正式打印由平台把 job 置为 `FAILED`，不得进入客户端 claim，预览则标记 `RENDER_FAILED` 并返回违规项。

## 9. 状态与错误契约

### 9.1 任务状态

服务端只允许下表中的转移；客户端事件不能越级写入终态：

| 当前 | 动作/证据 | 下一状态 |
| --- | --- | --- |
| `QUEUED` | 渲染开始 | `RENDERING` |
| `QUEUED`/`RENDERING` | 用户取消/渲染失败 | `CANCELLED`/`FAILED` |
| `RENDERING` | 所有面制品 hash/尺寸/页数通过 | `READY` |
| `READY` | 客户端 claim 单面或手动 FRONT | `FRONT_IN_PROGRESS` |
| `READY` | 客户端 claim 已验证自动双面 | `AUTO_IN_PROGRESS` |
| `FRONT_IN_PROGRESS` | 手动双面明确提交接受 | `AWAITING_FRONT_CHECK` |
| `FRONT_IN_PROGRESS` | 单面明确提交接受 | `AWAITING_OUTPUT_CHECK` |
| `FRONT_IN_PROGRESS` | 驱动拒绝，即使明确未提交 | `RESULT_UNKNOWN` |
| `FRONT_IN_PROGRESS` | 失联/结果不明 | `RESULT_UNKNOWN` |
| `AWAITING_FRONT_CHECK` | 确认 FRONT 已出 | `AWAITING_FLIP` |
| `AWAITING_FRONT_CHECK` | 确认 FRONT 未出 | `RESULT_UNKNOWN` |
| `AWAITING_FRONT_CHECK` | 确认 FRONT 损坏 | `CANCELLED` |
| `AWAITING_FLIP` | 幂等翻面确认 | `BACK_IN_PROGRESS` |
| `BACK_IN_PROGRESS` | 明确提交接受 | `AWAITING_OUTPUT_CHECK` |
| `BACK_IN_PROGRESS` | 驱动拒绝，即使明确未提交 | `RESULT_UNKNOWN` |
| `AUTO_IN_PROGRESS` | 明确提交但尚无两面完成证据 | `AWAITING_OUTPUT_CHECK` |
| 任意已提交状态 | 回执/设备状态不明 | `RESULT_UNKNOWN` |
| `AWAITING_OUTPUT_CHECK` | 所有期望面确认已出 | `COMPLETED` |
| `AWAITING_OUTPUT_CHECK` | 确认期望面未出 | `RESULT_UNKNOWN` |
| `AWAITING_OUTPUT_CHECK` | 确认期望面损坏 | `CANCELLED` |
| `RESULT_UNKNOWN` | 人工确认已出 | `AWAITING_FLIP`/`COMPLETED` |
| `RESULT_UNKNOWN` | 人工确认未出并明确继续 | 当前面新的 `*_IN_PROGRESS` |
| `RESULT_UNKNOWN` | 人工确认损坏或取消 | `CANCELLED` |

`activeJobId` 的释放遵循 [data-model.md](../data-model.md) 第 6 节：只有任务已经是 `COMPLETED`、`CANCELLED` 或 `FAILED` 且设备清空，才可释放；`RESULT_UNKNOWN` 或仍有待打印 BACK 时，即使收到 `DEVICE_CLEARED` 也不能释放。不能由 `COMPLETED` 的客户端事件或租约超时单独触发。`RESULT_UNKNOWN` 下禁止自动 retry/backoff；任何继续操作都必须由 `output-check` 显式产生新的 attempt 和新的 commandId，并满足安全未提交证据。

### 9.2 HTTP 与业务错误码

| HTTP | code | 触发条件与处理 |
| --- | --- | --- |
| `400` | `INVALID_REQUEST` | 缺字段、格式错误、面/模式组合非法；修正请求后重试。 |
| `401` | `AUTH_REQUIRED` | 登录态、设备身份或服务身份缺失/失效。 |
| `403` | `PRINT_SCOPE_DENIED` | 园区、人员、模板、资源、设备或功能权限不一致；禁止换 ID 重试。 |
| `404` | `TEMPLATE_NOT_FOUND` / `TEMPLATE_PAIR_NOT_FOUND` / `JOB_NOT_FOUND` / `ARTIFACT_NOT_FOUND` | 同园区对象不存在或未发布；先修配置。 |
| `422` | `EMPLOYEE_GRADE_REQUIRED` | 正式员工职级缺失，补齐人员资料后再创建任务。 |
| `422` | `EMPLOYEE_GRADE_UNMAPPED` | 职级代码未知或人事来源尚未映射，不使用岗位/其他枚举代替。 |
| `404` | `EMPLOYEE_GRADE_TEMPLATE_NOT_FOUND` | 无法自动匹配该职级；允许在打印页手动选择已发布模板，本次打印无需先补关联。 |
| `422` | `MANUAL_SELECTION_CONFIRMATION_REQUIRED` | 手选尚未核对确认人员、模板版本及预览，不能创建任务；已确认的合法未关联模板不得因无绑定被拒绝。 |
| `409` | `DRAFT_REVISION_CONFLICT` | 草稿 revision 过期；重新加载再保存。 |
| `409` | `PUBLISHED_POINTER_CONFLICT` | 发布/回滚指针已变化；重新读取模板。 |
| `409` | `PAIR_REVISION_CONFLICT` | 模板对 revision 已变化；重新读取 pair 并由操作员确认两面后再改绑。 |
| `409` | `BINDING_AMBIGUOUS` | 同层最高优先级有多个模板；消除冲突后再解析。 |
| `409` | `PRINTER_OCCUPIED` | `activeJobId` 属于其他 job；读取当前任务，不能强制接管。同一 job 需带匹配的 `resumeJobId` 恢复原 claim。 |
| `409` | `DEVICE_LEASE_MISMATCH` | 租约、设备身份或客户端实例不匹配。 |
| `409` | `JOB_STATE_CONFLICT` | 命令不符合当前状态或事件乱序；读取任务 current。 |
| `409` | `IDEMPOTENCY_KEY_REUSED` | 同 key/command/event 的 body hash 不一致；不得执行。 |
| `409` | `DEVICE_CLEARANCE_REQUIRED` | 任务取消/释放前仍需人工确认设备无卡或安全处理。 |
| `409` | `OUTPUT_CHECK_REQUIRED` | 结果尚未核对，不能完成、释放或继续。 |
| `422` | `TEMPLATE_VALIDATION_FAILED` | `faceRole`、单面页数、尺寸、必填、边界、字体、条码/二维码或资源校验失败；pair 两面不兼容也归入此类。 |
| `422` | `RENDER_VALIDATION_FAILED` | 可信渲染时字段缺失、元素越界、资源/字体不可用、二维码/条码不可生成或面数/页数不符；任务置为 `FAILED`，不能 claim。 |
| `422` | `SECURITY_QR_TEMPLATE_REQUIRED` | 保密访客没有绑定受控 `visitorCredentialPayload` 的可用模板，或 payload 来源缺失；平台适配层沿用现有 `smsCode` 扫码语义。 |
| `422` | `PRINTER_CAPABILITY_UNVERIFIED` | 自动双面、驱动、介质、方向或实机能力未验证。 |
| `422` | `OUTPUT_CHECK_INCOMPLETE` | 人工核对缺少面、attempt、设备清空证据或 resolution。 |
| `422` | `ARTIFACT_HASH_MISMATCH` | 客户端制品 hash 与冻结 manifest 不一致。 |
| `422` | `PAYLOAD_LIMIT_EXCEEDED` | JSON、资源、快照、事件或批量数量超出限制。 |
| `503` | `PRINT_RENDERER_UNAVAILABLE` / `PRINT_CLIENT_UNAVAILABLE` | 依赖服务不可用；不自动创建替代 job 或重放物理命令。 |

仅渲染器明确返回的422模板/内容诊断可保存为 `RENDER_FAILED` 预览；服务身份/路由/超时/5xx等依赖故障返回503，不写入误导性的失败预览。批次1变更审计在 `detailsJson.requestId` 保存与响应头相同的系统追踪ID。

错误响应中的 `retryable=true` 只允许表示安全的读取/渲染状态查询重试；物理打印命令、未知结果和人工翻面命令不得标记为可自动重试。所有错误均带 `requestId`，事件和审计记录引用该 ID 以便追踪。

## 10. API 验收要点

1. 模板保存、发布、回滚、模板对改绑、绑定和预览都验证园区/权限；每个发布后的单面版本不可变，厂牌 job 冻结同一 pair 选择到的 FRONT/BACK 版本，旧 job 仍读原快照。
2. 批量创建以人为原子拆 job；手动模式按当前人员 FRONT→人工确认翻面→BACK 串行，不能先整批打正面。
3. claim、事件、翻面确认、输出核对和取消分别验证相同 body 重放与 body mismatch；重复物理 action `commandId` 不产生第二次驱动调用，一个 command 的不同 `eventId` 事件均保留。
4. 队列接受、驱动提交、设备完成、人工已出卡四类证据分别留痕；没有可信实卡证据不能返回 `COMPLETED`。
5. 断线、客户端重启、回执丢失和 `RESULT_UNKNOWN` 保留 `activeJobId`，人工确认未出/已出/损坏后才能继续、完成或取消；不能靠租约超时释放。
6. Brother QL-800 只接受访客单面；HiTi 手动双面和带翻面模块的自动双面分别验收。具体 Windows、驱动、双色、双面、状态和实机结果未验证前，API 必须返回相应 `UNVERIFIED` 能力边界。


## 批次2实现补充契约

- `GET /printer-profiles/options?parkId=1&page=1&size=20`：仅 execute 权限，返回 ID、status、busy、displayName、deviceType、allowedPrintModes、defaultPrintMode；完整档案仍需 device 权限。设备/任务分页使用 page/size，模板/组合和人员搜索使用 current/size。
- `GET /print-subjects?parkId=1&subjectType=ADMITTANCE&keyword=...&current=1&size=20`：size≤50，仅返回可访问园区的姓名/工号/等级摘要。来源只能是 STAFF、SUPPLIER_PERSON、VISITOR、VISITOR_COMPANION、ADMITTANCE、ADMITTANCE_COMPANION；不按数字ID碰巧命中的表回退。
- `POST /print-subjects/selection`：`{parkId,subjects:[{subjectType,subjectId}]}`，1–100条唯一访客来源记录；重新经过 execute 权限及真实来源校验，只返回 records 内的subjectType/subjectId/displayName。用于旧页登录后的受控交接，不能带 fields、照片、身份证或预约码。
- `POST /print-jobs/preview`：与单人创建共用人员ID、设备、模式及 selection，但不带 previewId，手选可尚未人工确认。返回 previewId/status/subjectSummary/resolution/artifacts；分别查看全部卡面并复核hash后才允许前端确认。`GET /print-jobs/previews/{previewId}` 查询状态，`GET /print-jobs/previews/{previewId}/artifacts/{artifactId}` 下载同操作员的私有预览；它不能被设备领取。
- 创建必须携带本人的 READY `previewId`，batch 每个subjects元素有自己的previewId，不能在顶层复用。服务端在同一创建事务中重新冻结来源、实际版本及关联修订、模板字段/照片、模式及打印机档案，与服务端私有预览指纹比较。缺少/格式无效/未READY为422 `PRINT_PREVIEW_REQUIRED`；内容变化为409 `PRINT_PREVIEW_STALE`，UI清空旧确认、要求重新预览；跨园区或操作员为403 `PRINT_SCOPE_DENIED`。不信任前端传hash，幂等命中重放原结果。
- 模板 `fieldSchemaJson.fields` 可绑定 `{key:"personPhoto",schemaName:<image名称>,required:true}`。草稿只存绑定，不保存真人照或静态resourceRef；业务照片由source以 `{bindingKey,mediaType,sha256,bytesBase64}` 提供，冻结为带subjectType/subjectId归属的私有资源。普通fields不得覆盖照片，坏图/缺照均拒绝。
- 已终态 COMPLETED/FAILED/CANCELLED 的任务可 POST output-check，使用 `decision=DEVICE_CLEARANCE`、`resolution=NONE`、当前attemptId/face、`physicalCheck.state=NO_CARD_IN_DEVICE` 及非空operatorNote；只补录新的无卡证据并保持业务终态。新attempt和任何新提交风险会失效旧证据。
- flip/安全CONTINUE返回新attemptId、commandId、face、制品与冻结hash；幂等重放同一新命令。旧confirmed FRONT正常迟到回执仅审计，不能中断正在进行的BACK；同面退休命令出现冲突仍暂停。
- 设备claim、renew、events、device-cleared均要求 Idempotency-Key。clear响应 `{cleared:false}` 表示本次检查未释放，新检查需新键；网络结果未知重放原请求。自动双面制品路径face为 `combined`，HTTP `X-Face` 同为combined，命令业务面仍为BOTH。
- `GET /cutover?parkId=1` 为唯一匿名只读开关接口，返回visitorMode、legacyVisitorAllowed、newJobCreationEnabled、revision，不返回人员/设备配置。`template-park-ids` 未选为LEGACY；选中且feature+execution开启为TEMPLATE；选中但暂停为PAUSED，禁止自动回退。新创建由execution门禁控制，验收工作台可先验证但未切旧入口。登录回跳只允许固定访客工作台及重新校验的parkId/subjects，不接受任意redirect。

上述配置和接口本地验证不代表真实Oracle/现场/SSO验收通过，启用与回退须按 quickstart 的联合门槛执行。


### 任务恢复的冻结展示

`GET /print-jobs/{id}` 的 `subjectSummary` 包含冻结的 `displayName`、可选 `staffNo` 及既有类型/职级。`printerSummary` 只包含冻结的 `displayName`、`model` 和 `calibration` 白名单（`frontFeedInstruction/backFeedInstruction/frontRotation/backRotation`）。未配置的说明不推断默认。每面的 `artifacts` 保留 hash、大小和面标识；恢复界面按已知 jobId/FRONT/BACK 接口下载并校验，不使用任意返回 URL，也不重新解析模板或人员。
