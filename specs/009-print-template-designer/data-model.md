# 模板与打印数据模型

**规格**：[spec.md](spec.md)
**方案**：[plan.md](plan.md)
**状态**：设计资料；功能待开发，不代表数据库已经变更或设备已经验收。

本文是逻辑模型，不是 Oracle DDL。实现时按现有模块的命名、审计字段和迁移发布机制落地；任何真实表结构、索引、字段长度和兼容性仍须在目标 Oracle 上核对。新数据不依赖 Oracle 原生 JSON 类型或 JSON 查询特性，布局、快照、设备档案及事件扩展内容均以经服务端校验的 JSON 文本存入 CLOB。

## 1. 通用约定

| 项目 | 约定 |
| --- | --- |
| 主键 | 新实体使用 UUID 字符串（逻辑类型 `VARCHAR2(36)`）；对外返回同一值，不以自增序号作为客户端标识。 |
| 园区 | `park_id` 必填，沿用平台园区标识的字符串表示；所有模板、绑定、设备、人员和任务必须属于同一授权园区。 |
| 时间 | `created_at`、`updated_at`、业务发生时间按 UTC 保存，展示时由客户端按园区时区转换；逻辑类型为 `TIMESTAMP`。 |
| 操作人 | `created_by`、`updated_by`、`actor_id` 保存平台身份标识；客户端设备身份单独保存，不借用管理员身份。 |
| JSON 文本 | CLOB 内保存 UTF-8 JSON 文本；保存前由应用校验结构、字段白名单、面数和大小，读取时拒绝无法解析或 hash 不一致的内容。 |
| 哈希 | 内容 hash 使用 SHA-256 小写十六进制；模板版本、资源和打印制品的 hash 均对完整字节计算。 |
| 删除 | 模板版本、已引用任务、打印事件和资源引用不物理删除；使用状态或撤销时间停止使用，保留审计链。 |
| 归属 | `park_id` 不从请求体单独信任；服务端从当前登录主体、人员/申请和设备档案交叉校验，跨园区统一返回 `403 PRINT_SCOPE_DENIED`。 |

## 2. 业务枚举与不变量

### 2.1 打印物、人员和面

| 枚举 | 值 | 约束 |
| --- | --- | --- |
| `print_item_type` | `STAFF_CARD`、`VISITOR_SLIP` | 绑定、模板和设备能力必须按打印物匹配；两类打印物不得借用对方的默认模板。 |
| `person_type` | `EMPLOYEE`、`OUTSOURCED`、`DISPATCHED`、`SUPPLIER`、`VISITOR` | 实际人员归类由平台业务数据提供，设计器不能自行改变。 |
| `face_role` | `FRONT`、`BACK` | `Template` 的维护面角色；`STAFF_CARD` 必须分别维护一份 FRONT 模板和一份 BACK 模板，`VISITOR_SLIP` 只能为 `FRONT`。 |
| `side_count` | 固定为 `1` | 每个 `TemplateVersion` 永远只有一个 pdfme `schemas` 页面；厂牌的双面由两个模板版本组合，不由单个版本承载。 |
| `print_mode` | `SINGLE`、`MANUAL_DUPLEX`、`AUTO_DUPLEX` | `SINGLE` 只允许访客凭条；人员厂牌只允许两种双面模式。模式来自打印机档案/任务快照，不能由模板版本改变。 |
| `template_selection_kind` | `BOUND`、`PAIR`、`EXPLICIT` | `BOUND` 按绑定规则解析；`PAIR` 选择一个有效厂牌模板对；`EXPLICIT` 明确选择已发布版本。三种选择都必须经过相同的园区、分类、资源和安全规则。 |
| `pair_status` | `ACTIVE`、`ARCHIVED` | 模板对没有独立版本或发布流程；归档后不能成为新任务的选择目标，历史任务仍读冻结快照。 |
| `security_class` | `NORMAL`、`SECURITY` | `SECURITY` 访客必须解析到包含二维码组件且绑定受控 `visitorCredentialPayload` 的模板；无来源、无码或歧义均拒绝。该值由现有访客凭证/核验业务提供；当前源码已确认其沿用 `smsCode` 扫码语义，平台适配层只做字段映射，本规格不新增发码业务。 |

以下规则是跨实体约束：

1. `STAFF_CARD` 分别维护 `FRONT` 和 `BACK` 两个 `Template`；每个版本 `side_count=1`，由 `PrintTemplatePair` 绑定两个已发布不可变版本。两面可独立编辑、发布和回滚，版本号不要求相同。
2. `VISITOR_SLIP` 的 `Template.face_role` 只能为 `FRONT`，其 `TemplateVersion.side_count` 必须为 1，任务 `expected_face_count=1`，不会创建翻面确认或空白背面。
3. `TemplateVersion` 发布后不可修改。编辑已发布模板必须创建或更新该单面模板的独立草稿；回滚只切换该模板的当前发布指针，不重写历史行，也不自动改写任何 `PrintTemplatePair`。
4. 每个 `PrintJob` 只对应一个业务人员和一张卡/一张访客凭条。批量请求必须在服务端拆成多个 job，不能用一个 job 混装多人。
5. `MANUAL_DUPLEX` 的 FRONT、BACK 各有独立 `PrintAttempt`；`AUTO_DUPLEX` 的两面共用一个 `PrintAttempt(face=BOTH)`；`SINGLE` 只有一个 `FRONT` attempt。
6. `PrintJob` 创建前必须选全厂牌两面；创建后冻结同一人员、两份模板版本、各自布局/输入/资源、可选 pair revision、设备档案、介质、校准和打印模式。翻面等待期间不能重新选择模板或人员。
7. 设备队列接受、驱动回执或客户端收到响应都不是实卡已出证据。没有可信设备完成证据时必须进入 `RESULT_UNKNOWN` 或等待人工输出核对。

## 3. 受控资源值对象

图片、照片、Logo、字体和 pdfme 需要的背景/基础文件不把公网 URL 或任意本地路径写进模板。布局 CLOB 中只允许出现以下资源引用：

```json
{
  "objectId": "00000000-0000-4000-8000-00000000000e",
  "contentHash": "sha256:…",
  "mediaType": "image/png",
  "sizeBytes": 182736,
  "parkId": "park-a",
  "purpose": "PHOTO|LOGO|BACKGROUND|FONT|BASE_PDF",
  "accessScope": "TEMPLATE|STAFF_RECORD|PRINT_JOB",
  "sourceRevision": "…"
}
```

`objectId` 指向受控对象存储；`contentHash`、园区、用途和授权元数据随模板版本或任务快照保存。平台在预览、渲染和下载时重新检查当前主体、园区、用途和 hash，渲染服务只接收平台已经读取并授权的资源字节。API 不接受 `http://`、`https://`、绝对路径、路径穿越、脚本或未登记资源。资源原始文件由受控存储适配器管理。当前源码核对发现历史 `smart-file-biz` 无源码，新闻附件的公开下载也不适用，因此批次1采用打印专属 Oracle 私有对象表，统一保存模板图片和预览 PDF。该选择不复用公开下载路由，实际 schema、容量、备份和发布执行器须在数据库集成前核准。

## 4. 实体

### 4.1 `Template`

模板是业务名称、打印物、人员分类和单面 `face_role` 的稳定容器；布局内容属于该模板的 `TemplateVersion`。厂牌正反面由两个模板容器分别维护。

| 字段 | 类型/要求 | 说明 |
| --- | --- | --- |
| `template_id` | UUID，主键 | 模板稳定标识。 |
| `park_id` | 必填 | 模板所属园区。 |
| `template_key` | 同园区唯一 | 机器稳定键，不能用名称作为外部引用。 |
| `name` | 必填，1-100 字符 | 管理员可见名称。 |
| `print_item_type` | 枚举 | `STAFF_CARD` 或 `VISITOR_SLIP`。 |
| `person_type` | 枚举 | 员工、外包、派遣、供应商或访客。 |
| `classification_code` | 必填，1-64 字符 | 受控分类键，例如 `VISITOR_NORMAL`、`VISITOR_SECURITY`、`STAFF_DEFAULT`；不在此字段塞自由脚本。 |
| `face_role` | `STAFF_CARD` 必填 `FRONT`/`BACK`；`VISITOR_SLIP` 固定 `FRONT` | 该模板只维护一个面；厂牌正反面通过 `PrintTemplatePair` 组合。 |
| `lifecycle_status` | `ACTIVE`、`ARCHIVED` | 归档模板不能成为新绑定目标；历史任务仍可读快照。 |
| `current_draft_version_id` | 可空 | 当前草稿版本；没有草稿时为空。 |
| `current_published_version_id` | 可空 | 当前生效发布版本；只有发布成功后才有值。 |
| `draft_revision` | 非负整数，默认 0 | 草稿乐观锁。每次成功保存草稿原子加一；请求携带旧值时不一致返回 `DRAFT_REVISION_CONFLICT`。 |
| `created_by/created_at` | 必填 | 创建审计。 |
| `updated_by/updated_at` | 必填 | 最后修改审计。 |
| `archived_at` | 可空 | 归档时间。 |

约束：`template_key` 在同一 `park_id` 内唯一；发布前必须存在完整合法的单面版本。`face_role` 创建后不可变，`current_published_version_id` 只能指向同一模板、同一园区、状态为 `PUBLISHED` 的不可变单面版本。`STAFF_CARD` 的 BACK 模板不能通过修改 FRONT 模板的 role 得到，必须使用独立模板容器。

### 4.2 `TemplateVersion`

版本是一个单面模板的任务引用和回滚单元。每个版本永远只包含一页 pdfme 版面；厂牌两面由 `PrintTemplatePair` 在任务创建前组合。

| 字段 | 类型/要求 | 说明 |
| --- | --- | --- |
| `template_version_id` | UUID，主键 | 版本标识。 |
| `template_id` | 必填，外键语义 | 所属模板。 |
| `park_id` | 必填 | 冗余归属，用于每次授权校验。 |
| `version_no` | 同模板递增 | 展示版本号；发布后不复用。 |
| `version_status` | `DRAFT`、`PUBLISHED` | `PUBLISHED` 内容不可更新；回滚不会改变历史版本状态，所有历史版本继续为 `PUBLISHED`，是否生效只由模板的当前发布指针决定。 |
| `face_role` | 从所属 `Template` 冗余保存 | 必须与模板一致；`STAFF_CARD` 为 `FRONT` 或 `BACK`，访客只能为 `FRONT`。 |
| `side_count` | 固定 `1` | 永远为 1；一个版本只对应一个 pdfme `schemas` 页面，不允许通过版本内容增加第二面或第三面。 |
| `layout_json` | CLOB，必填 | 一个单面 pdfme 模板定义，必须包含一个 `schemas` 页面；内容不能包含未授权 URL/脚本/本地路径。 |
| `field_schema_json` | CLOB，必填 | 允许字段、类型、必填性、格式约束和本模板的 `face_role`；字段键必须来自平台白名单。 |
| `resource_manifest_json` | CLOB，必填 | 第 3 节受控资源引用数组及每个资源的 hash/权限元数据。 |
| `page_spec_json` | CLOB，必填 | 本单面宽、高、单位、方向、最大页数和有效打印区域；`maxPageCount` 固定为 1。 |
| `validation_report_json` | CLOB，必填 | 发布校验版本、错误/警告摘要、校验时间；有错误不能发布。 |
| `content_hash` | SHA-256，必填 | 对规范化版面、字段、资源清单和页面规格计算；发布后不可变。 |
| `draft_revision` | 非负整数 | 草稿保存时的版本快照修订号；仅 `DRAFT` 可变。 |
| `published_at/published_by` | 发布时必填 | 发布审计。 |
| `created_at/created_by` | 必填 | 创建审计。 |

`layout_json` 的最小业务形状如下，实际 pdfme 字段由选定锁定版本的库校验：

```json
{
  "schemaVersion": 1,
  "faceRole": "FRONT",
  "sideCount": 1,
  "basePdfRef": { "objectId": "00000000-0000-4000-8000-000000000021", "contentHash": "sha256:…" },
  "pageSpecJson": {
    "widthMm": 85.6,
    "heightMm": 53.98,
    "orientation": "LANDSCAPE",
    "maxPageCount": 1
  },
  "schemas": [[]]
}
```

`schemas` 必须是恰好一页的 pdfme schemas 数组；示例中的 `[[]]` 表示一页空画布，实际保存时由设计器填入该页的组件。`pageSpecJson` 是该面自己的尺寸和方向声明，必须与渲染器收到的 `basePdf` 尺寸一致。持久化的 `basePdfRef` 只允许作为受控资源引用保存，不能原样交给底层 pdfme 渲染；smart-platform 在调用渲染器前必须完成资源授权和 hash 校验，并把每面的尺寸转换为内联 `basePdf={width, height, padding}`（单位为 mm，默认 padding 为 `[0, 0, 0, 0]`）。当前固定页渲染器只接受该内联对象，不读取 `basePdfRef`、外部 URL 或本地路径；无法转换或授权失败时在进入渲染器前拒绝。`faceRole`、`sideCount` 和 `schemas` 页数由服务端校验，不能由客户端改成双面结构。访客版本只能为 `faceRole=FRONT`。保密访客版本的字段定义必须包含 QR 组件和 `visitorCredentialPayload` 绑定；该不透明值由现有访客凭证/核验业务适配提供，当前源码已确认适配层沿用现有 `smsCode` 的扫码语义，本规格不新增凭证发码业务。仅有一张预先生成的图片而没有组件/字段约束不能作为带码模板发布。

### 4.3 `PrintTemplatePair`

`PrintTemplatePair` 是厂牌正反面模板的轻量稳定组合。它没有独立版本表，也没有独立发布流程；变更两个指针时以 `revision` 乐观锁原子保存，并保留审计事件。

| 字段 | 类型/要求 | 说明 |
| --- | --- | --- |
| `pair_id` | UUID，主键 | 模板对的稳定标识，对外绑定和任务选择使用该 ID。 |
| `park_id` | 必填 | 模板对所属园区。 |
| `name` | 必填，1-100 字符 | 管理员可见名称。 |
| `print_item_type` | 固定 `STAFF_CARD` | 模板对只用于员工/外包/派遣/供应商等厂牌。 |
| `person_type` | 必填枚举 | 适用人员类型；必须与两份模板一致。 |
| `classification_code` | 必填，1-64 字符 | 适用分类；必须与两份模板一致。 |
| `front_template_version_id` | 必填，已发布不可变单面版本 | 必须指向同园区、同打印物/人员类型/分类且 `face_role=FRONT`、`side_count=1` 的版本。 |
| `back_template_version_id` | 必填，已发布不可变单面版本 | 必须指向同园区、同打印物/人员类型/分类且 `face_role=BACK`、`side_count=1` 的版本。 |
| `revision` | 非负整数，默认 0 | 绑定指针的乐观锁；每次成功改绑原子加一。 |
| `status` | `ACTIVE`、`ARCHIVED` | 归档后不能作为新任务目标；历史任务继续读取冻结快照。 |
| `created_by/created_at`、`updated_by/updated_at` | 必填 | 审计字段。 |
| `archived_at` | 可空 | 归档时间。 |

保存模板对时必须同时验证两份版本已发布、不可变、归属园区一致，且页面尺寸、方向、介质规格和适用范围兼容。两个版本的 `version_no` 不要求相同。多个兼容的 `PrintTemplatePair` 可以共享同一个已发布 BACK 版本；共享不改变任一模板版本的独立生命周期。发布或回滚任一模板的新版本不会自动更新现有 pair，管理员必须用当前 `revision` 显式重新绑定并产生审计事件。归档 pair 不回收其模板或版本，也不创建 pairVersion。

### 4.4 `BindingRule`

绑定规则在系统中持久保存：厂牌解析到已保存的模板对及其具体版本，访客解析到模板容器并读取当前发布版本。正式员工再以职级条件选择组合；不依赖当前设计器是否打开。

| 字段 | 类型/要求 | 说明 |
| --- | --- | --- |
| `binding_rule_id` | UUID，主键 | 规则标识。 |
| `park_id` | 必填 | 规则所属园区。 |
| `print_item_type` | 必填枚举 | 必须与模板一致。 |
| `person_type` | 必填枚举 | 必须与模板一致。 |
| `classification_code` | 必填 | 必须与模板一致。 |
| `scope_type` | `COMPANY`、`SUPPLIER`、`EXPLICIT_DEFAULT` | 匹配层级。 |
| `scope_id` | COMPANY/SUPPLIER 时必填；默认时必须为空 | 具体组织或供应商 ID。 |
| `template_id` | `VISITOR_SLIP` 必填；`STAFF_CARD` 必须为空 | 访客绑定目标模板容器，必须为同园区、同打印物/人员类型/分类的 `FRONT` 模板。 |
| `pair_id` | `STAFF_CARD` 必填；`VISITOR_SLIP` 必须为空 | 厂牌绑定目标模板对，必须为同园区、同人员类型/分类的 `ACTIVE` pair。 |
| `employee_grade_codes_clob` | EMPLOYEE 厂牌必填，其他分类必须为空 | JSON 字符串数组形式的去重职级代码集合，1–100 项，每项 1–64 字符；来自经确认的现有人事字典。名称仅展示，不接受隐含 ALL、通配、大小比较或未经验证的自定义代码。 |
| `priority` | 整数，默认 100 | 仅在同一 scope 层级内比较；不能用优先级跨越层级。 |
| `valid_from/valid_to` | 必填/可空 | `[valid_from, valid_to)` 生效区间，结束时间必须晚于开始时间。 |
| `status` | `ACTIVE`、`DISABLED`、`EXPIRED` | 只有当前时刻有效的 `ACTIVE` 参与解析。 |
| `revision` | 非负整数 | 修改规则的乐观锁。 |
| `created_by/created_at` | 必填 | 创建审计。 |
| `updated_by/updated_at` | 必填 | 修改审计。 |

自动选择 `BOUND` 的解析顺序固定如下：

1. 校验 `park_id + print_item_type + person_type + classification_code`。正式员工由服务端读取并归一化职级；缺失返回 `EMPLOYEE_GRADE_REQUIRED`，未知或来源映射未完成返回 `EMPLOYEE_GRADE_UNMAPPED`。候选规则先过滤 `employee_grade_codes_clob` 中包含该职级的规则，后面的每个 scope 都只能从过滤后的集合选取。
2. 在同一园区的有效 `COMPANY`/`SUPPLIER` 规则中按具体对象匹配，取最高 `priority`；最高优先级出现多条规则（即使指向同一目标），返回 `BINDING_AMBIGUOUS`，不凭数据库顺序选择。
3. 精确规则零命中时查一个 `EXPLICIT_DEFAULT`，要求同样的四个匹配键以及正式员工职级条件；默认规则出现多条最高优先级规则同样拒绝。
4. 没有明确默认时返回 `TEMPLATE_NOT_FOUND`。正式员工没有该职级的匹配结果时返回 `EMPLOYEE_GRADE_TEMPLATE_NOT_FOUND`，不退回其他职级或无职级的通用厂牌。保密访客不能使用无码模板兜底。
5. `STAFF_CARD` 解析结果必须是有效 `pair_id`，并能取得已发布 FRONT/BACK 两个单面版本；`VISITOR_SLIP` 解析结果必须是有效 `template_id` 且有当前发布 FRONT 版本。任一版本缺失、校验失败或园区不一致均拒绝创建任务。

同一匹配键、同一 scope、同一 scope ID，生效区间重叠的非正式员工规则，不允许存在两个相同优先级的活动规则。正式员工还必须比较职级集合：集合互不相交的规则可以并存；集合相交、生效区间重叠且同优先级则拒绝。规则保存时可提前拒绝确定的冲突；解析时仍必须再次检查，防止并发或历史数据造成歧义。

`template_id` 与 `pair_id` 是互斥字段：必须且只能按 `print_item_type` 选择一个。厂牌绑定只写 `pair_id`，访客绑定只写 `template_id`；同时提供或同时缺失均返回 `INVALID_REQUEST`。

等级与组合是多对一配置：员工级可以使用组合 A，职员级使用组合 B，经理级使用组合 C；这些名称是用户提供的业务例子，代码及模板 ID 必须读取系统数据。多个职级也可绑定同一 pair。pair 自身不增加翻面模式或强制单一职级，背面仍可在同园区/分类兼容的组合间复用。手选 PAIR/EXPLICIT 不要求已有职级或公司适用绑定；EXPLICIT 两份版本无需对应已保存组合。新发布但未关联的模板必须可在打印页选到。自动无匹配/歧义、未关联或与推荐不同均展示为人工核对提示，不能变成手选的关联门槛；人员资料、园区/分类/权限、已发布状态、两面兼容和保密带码等硬性检查仍必须通过。

`PrintTemplatePair`、`BindingRule` 及其修订/审计由平台持久化；系统管理页保存成功后，刷新、重新登录、设计器关闭及服务重启均应读回相同关联。内存验证页不满足此项验收。

### 4.5 `PrinterProfile`

打印机档案描述设备和校准能力；它不是物理打印机本身的证明，`capability_status=VERIFIED` 只能由授权验收记录设置。

| 字段 | 类型/要求 | 说明 |
| --- | --- | --- |
| `printer_profile_id` | UUID，主键 | 平台设备档案标识。 |
| `park_id` | 必填 | 设备授权园区。 |
| `device_identity` | 必填 | 客户端工作站证书/设备身份对应的稳定键；同一工作站身份可以关联多个 `PrinterProfile`，实际打印机由 `printer_profile_id` 区分；不使用浏览器会话或管理员账号。 |
| `display_name` | 必填 | 管理员可见名称。 |
| `manufacturer` | 必填 | 例如 `Brother`、`HiTi`。 |
| `model` | 必填 | 例如 `QL-800`、`CS-220e`；用户称 `CS220` 时铭牌确认前不得把候选型号标为已验证。 |
| `device_type` | `LABEL_PRINTER`、`CARD_PRINTER` | 设备类别。 |
| `connection_type` | `USB_LOCAL_CLIENT`、`LOCAL_AGENT` | 设备不直接由远端 Java 服务操控。 |
| `allowed_print_modes` | CLOB，枚举数组 | 档案允许的任务模式集合；访客 QL-800 只能包含 `SINGLE`，未安装翻面模块的证卡机不能包含 `AUTO_DUPLEX`。 |
| `default_print_mode` | 枚举 | 新任务默认值，创建任务时复制；修改后不影响已有任务。 |
| `capability_status` | `UNVERIFIED`、`VERIFIED`、`REVOKED` | 自动双面只有 `VERIFIED` 才能提交。 |
| `flip_capability` | `NONE`、`MANUAL_ONLY`、`AUTO_VERIFIED` | `AUTO_VERIFIED` 必须有有效翻面模块及验收证据。 |
| `driver_version` | 可空字符串 | Windows 驱动版本；未确认时为空/`UNVERIFIED`，不在这里臆造版本。 |
| `sdk_or_bridge_version` | 可空字符串 | b-PAC/厂商组件/本地客户端版本。 |
| `media_spec_json` | CLOB，必填 | 介质、卡/纸尺寸、颜色能力、可打印区域和最大宽度。QL-800 访客档案必须记录最大可打印宽度 58mm。 |
| `calibration_json` | CLOB，必填 | 进卡方向、旋转、边距、缩放、手动翻面指引和验证时间。 |
| `capability_evidence_json` | CLOB，必填 | 官方资料、驱动/模块、验收样本和状态回传证据的受控引用；未知项明确标 `UNVERIFIED`。 |
| `config_revision` | 非负整数 | 档案乐观锁；任务创建时复制。 |
| `active_job_id` | 可空 UUID | 当前物理占用 job。详见第 6 节。 |
| `lease_owner` | 可空 | 当前客户端连接租约持有人。 |
| `lease_expires_at` | 可空时间 | 连接租约到期时间；不能单独清空 `active_job_id`。 |
| `status` | `ENABLED`、`DISABLED`、`QUARANTINED` | 禁用/隔离设备不能创建新任务。 |
| `created_by/created_at`、`updated_by/updated_at` | 必填 | 档案审计。 |

设备能力约束：

- Brother QL-800 访客任务必须是 `SINGLE`，宽度和黑/红能力校验在预览/渲染及客户端提交前各做一次；图片 b-PAC 适配是候选实现，不能把 PDF 字节直接当成 Brother 原始指令。
- HiTi CS-220e 的 `AUTO_DUPLEX` 只有安装并验收翻面模块、Windows 驱动/设备能力和页序方向后才能出现在 `allowed_print_modes`；未验证仍可用于 `MANUAL_DUPLEX` 的开发/验收安排，但生产任务必须按档案实际能力校验。

### 4.6 `PrintJob`

`PrintJob` 是一人一张卡/一张凭条的不可变业务执行单元。任务创建时完成模板解析、权限校验和快照，不把后续实时查询结果拼回任务。

| 字段 | 类型/要求 | 说明 |
| --- | --- | --- |
| `job_id` | UUID，主键 | 任务标识。 |
| `park_id` | 必填 | 任务园区。 |
| `print_item_type` | 必填 | 打印物。 |
| `person_type`、`classification_code` | 必填 | 创建时解析得到的人员类型和分类。 |
| `subject_type` | `STAFF`、`VISITOR`、`VISITOR_COMPANION` | 业务对象类型。 |
| `subject_id` | 必填 | 一个员工/访客对象；不能是批量 ID 或逗号拼接值。 |
| `card_instance_key` | `STAFF_CARD` 必填且必须指向有效的已登记卡关系/非空 `cardNo`；`VISITOR_SLIP` 必须为空 | 人员厂牌对应已登记实体卡关系的受控业务键；本任务只读卡号登记，不写卡，缺少有效卡号时拒绝创建，访客凭条不需要实体卡 ID。 |
| `selection_kind` | `BOUND`、`PAIR`、`EXPLICIT` | 创建时实际采用的模板选择方式；创建后不可变。 |
| `binding_rule_id` | `BOUND` 时必填，否则可空 | 解析命中的绑定规则；用于审计和重现匹配结果。 |
| `pair_id` | `STAFF_CARD` 且使用绑定/模板对选择时必填，否则可空 | 创建时采用的 `PrintTemplatePair` 稳定 ID；显式选择可以为空。 |
| `pair_revision` | 可空非负整数 | 任务创建时采用的 pair revision；用于审计，显式选择没有 pair 时为空。 |
| `front_template_id`、`back_template_id` | `STAFF_CARD` 两者必填；访客只填 FRONT | 两个面对应的模板容器；访客 BACK 为空。 |
| `front_template_version_id`、`front_template_version_no` | 必填 | 创建时冻结的 FRONT 已发布单面版本；访客也使用该字段。 |
| `back_template_version_id`、`back_template_version_no` | `STAFF_CARD` 必填；`VISITOR_SLIP` 必须为空 | 创建时冻结的 BACK 已发布单面版本；后续发布/回滚不影响本任务。 |
| `printer_profile_id`、`printer_config_revision` | 必填 | 创建时设备档案和配置修订号。 |
| `print_mode` | 必填 | `SINGLE`、`MANUAL_DUPLEX` 或 `AUTO_DUPLEX`，创建后不可变；这是任务执行模式，与版本的 `side_count` 分开。 |
| `expected_face_count` | `1` 或 `2` | 单面/双面不变量。 |
| `current_face` | `FRONT`、`BACK`、`BOTH`、可空 | 当前执行面；自动任务为 `BOTH`。 |
| `status` | 见第 5 节 | 任务状态。 |
| `subject_snapshot_clob` | CLOB，必填 | 经授权读取的姓名、工号/访客标识、照片资源引用、单位和有效期等最小冻结数据。默认列表不返回全文。 |
| `selection_snapshot_clob` | CLOB，必填 | 选择方式、自动解析结果与实际选择；BOUND 保存使用的规则及修订，手选保存未关联/偏离推荐等提示及确认人/时间；正式员工包含职级代码/名称/来源，不要求手选有适用规则。 |
| `template_snapshot_clob` | CLOB，必填 | FRONT（及厂牌 BACK）的单面发布版本布局、字段定义、尺寸、资源 manifest、`content_hash` 和各自输入映射；不把两面合并成一个模板版本。 |
| `printer_snapshot_clob` | CLOB，必填 | 打印机型号、模式、介质、可打印区域、校准、驱动/SDK 版本和 `config_revision`。 |
| `render_artifact_manifest_clob` | CLOB，可空 | 渲染成功后的每面制品及可选合并制品 ID、hash、页数、尺寸、字节数和有效期；厂牌合并制品必须按 FRONT→BACK 顺序。 |
| `idempotency_key` | 创建请求内唯一 | 创建重复请求返回原任务；相同 key 不同 body hash 返回冲突。 |
| `created_by/created_at` | 必填 | 创建审计。 |
| `started_at/completed_at/cancelled_at` | 可空 | 生命周期时间。 |
| `cancel_reason` | 可空，取消时必填 | 操作人取消或人工核对取消原因。 |
| `last_error_code`、`last_error_detail_clob` | 可空 | 结构化错误和受控诊断，禁止写入密钥/完整照片。 |

人员快照至少包含 `subjectId`、`subjectType`、展示字段、园区、分类、有效期、受控照片引用和数据 hash；正式员工额外保存 `employeeGradeCode`、`employeeGradeName`、`employeeGradeSource` 及解析时刻，匹配快照中 BOUND 保存命中规则、修订和适用集合；PAIR/EXPLICIT 保存服务端取得的自动推荐或未匹配/歧义原因、实际版本及操作员确认。没有实际使用绑定规则时 binding_rule_id 为空，不伪造职级绑定证据；访客主访客与随行人员分别生成 job，不能沿用上一人的照片或字段。`subject_snapshot_clob` 不保存未经授权的完整身份证件号。

任务创建时，`STAFF_CARD` 必须在同一事务中解析并选全 FRONT/BACK 两份已发布单面版本，冻结同一人员的两面输入、布局和资源；`MANUAL_DUPLEX` 与 `AUTO_DUPLEX` 都遵守该规则，进入 `AWAITING_FLIP` 后禁止重新选择模板、版本或人员。正式输出按 FRONT、BACK 各渲染一页再组合为双面结果。两面允许使用相同字段键，但渲染器必须按面隔离输入，不能因字段同名互相覆盖。`VISITOR_SLIP` 只能有 FRONT 版本和一个 FRONT 输入，固定 `SINGLE`。

手选的 `selection_snapshot_clob` 记录 `manualSelectionConfirmed=true`、服务端登录主体 `confirmedBy` 和服务器时间 `confirmedAt`；同时保存 `automaticResolution`（MATCHED/NOT_FOUND/AMBIGUOUS、原因码和推荐版本摘要）及实际版本，读取失败不冒充 NOT_FOUND。后续新增或修改绑定不会回写本次快照，手选打印也不会创建或更新 PrintTemplatePair/BindingRule。

### 4.7 `PrintAttempt`

一次实际提交意图的持久记录。服务器不承诺分布式物理 exactly-once；`command_id` 去重只能保证同一逻辑命令不会再次发送，驱动/设备在未知状态下仍需人工核对。

| 字段 | 类型/要求 | 说明 |
| --- | --- | --- |
| `attempt_id` | UUID，主键 | 尝试标识。 |
| `job_id` | 必填 | 所属任务。 |
| `face` | `FRONT`、`BACK`、`BOTH` | 手动两面各一行；自动双面只有一行 `BOTH`。 |
| `attempt_no` | 同 job+face 递增 | 只有操作员在 `RESULT_UNKNOWN` 明确确认未出卡后才允许创建新的 attempt。 |
| `command_id` | UUID，同设备作用域唯一 | 服务端为一次物理提交分配；同一 command 只能对应一个 attempt 和一次驱动意图，但可关联多条不同事件。 |
| `device_identity` | 必填 | 执行设备身份。 |
| `client_instance_id` | 必填 | 本地客户端实例。 |
| `intent_state` | `ALLOCATED`、`RECORDED`、`SENT`、`ACKNOWLEDGED`、`UNKNOWN`、`RESOLVED` | claim 时为 `ALLOCATED`，客户端落盘后为 `RECORDED`；设备提交意图和回执不等于物理完成。 |
| `local_submission_state` | `NOT_STARTED`、`SUBMISSION_STARTED`、`DRIVER_RESULT_RECORDED`、`RECOVERY_REQUIRED` | 客户端本地持久化标记；调用驱动前必须先写 `SUBMISSION_STARTED`。恢复时若该标记存在但没有可信完成证据，必须上报 `RESULT_UNKNOWN`，不能按“服务端未收到事件”重发。 |
| `output_state` | `NOT_CHECKED`、`CONFIRMED_OUT`、`CONFIRMED_NOT_OUT`、`CONFIRMED_DAMAGED` | 人工/可信设备输出结果。 |
| `artifact_hash` | 必填 | 本次实际提交制品的 hash；客户端提交前校验。 |
| `intent_payload_clob` | CLOB，必填 | 面、制品、设备、模式和 hash 的规范化快照。 |
| `local_intent_recorded_at` | claim 时为空，收到意图事件后必填 | 客户端本地持久化意图时间。 |
| `sent_at/acknowledged_at` | 可空 | 客户端调用驱动和收到回执时间。 |
| `resolved_at/resolved_by` | 可空 | `RESULT_UNKNOWN` 后人工核对完成时填写。 |
| `driver_job_key` | 可空 | 厂商驱动/队列标识，不能作为出卡证明。 |
| `error_code`、`error_detail_clob` | 可空 | 受控错误。 |

`(device_identity, command_id)` 在 `PrintAttempt` 中唯一。服务端分配 command 后，一个 command 可以产生 `INTENT_RECORDED`、`COMMAND_SENT`、`DEVICE_ACCEPTED` 等多条不同 `PrintEvent`，事件不能按 commandId 去重。客户端重启后先查询已有 attempt；本地存在 `SUBMISSION_STARTED` 而没有可信完成证据时必须进入 `RESULT_UNKNOWN` 并等待人工核对，不能因为服务端缺少 `INTENT_RECORDED` 就新建同面命令代替原命令。

### 4.8 `PrintEvent`

追加写入的任务审计事件，不能更新或删除历史事件。

| 字段 | 类型/要求 | 说明 |
| --- | --- | --- |
| `event_id` | UUID，主键 | 客户端生成或服务端生成。 |
| `job_id` | 必填 | 所属任务。 |
| `attempt_id` | 可空 | 面执行事件关联的尝试。 |
| `event_type` | 受控枚举 | 见下方事件集合。 |
| `from_status/to_status` | 可空/可空 | 服务端状态转移前后值。客户端不能伪造最终状态。 |
| `actor_type` | `USER`、`SERVICE`、`PRINT_CLIENT`、`DEVICE` | 事件来源。 |
| `actor_id` | 必填 | 用户、服务或设备身份。 |
| `device_identity` | 客户端事件必填，用户/服务事件可空 | 从认证设备身份取得，不能由请求自行指定其他设备；关联获准 job/attempt。 |
| `command_id` | 可空 | 设备命令关联；同一命令可有多条不同事件，事件唯一性由 `event_id` 保证。 |
| `client_sequence` | 可空 | 同一客户端单调递增序号；缺失不能绕过 command 去重。 |
| `event_payload_clob` | CLOB | 结构化最小数据，敏感快照只存引用/hash。 |
| `occurred_at` | 必填 | 来源发生时间；服务端另记接收时间。 |
| `received_at` | 必填 | 平台接收时间。 |

事件至少包括：`JOB_CREATED`、`TEMPLATE_RESOLVED`、`RENDER_STARTED`、`RENDER_READY`、`RENDER_FAILED`、`CLIENT_CLAIMED`、`INTENT_RECORDED`、`COMMAND_SENT`、`DRIVER_REJECTED`、`DEVICE_ACCEPTED`、`DEVICE_COMPLETED`、`DEVICE_DISCONNECTED`、`OUTPUT_UNKNOWN`、`FRONT_OUTPUT_CHECKED`、`FLIP_CONFIRMED`、`BACK_OUTPUT_CHECKED`、`AUTO_OUTPUT_CHECKED`、`RESULT_MARKED_UNKNOWN`、`RESULT_RESOLVED`、`COMMAND_RETIRED`、`JOB_CANCELLED`、`DEVICE_CLEARED`、`LEASE_RENEWED` 和 `JOB_COMPLETED`。

`event_id` 为全局唯一主键；先核对 actor/device 身份和 job 授权，再比较事件 body hash，相同身份的同一 `event_id` 且 body hash 相同才可重放，不能因为 `command_id` 相同而吞掉同一命令后续不同类型的事件。

## 5. 任务状态与完整转移表

`PrintJob.status` 是服务端唯一状态源；客户端上报事件只作为证据，不能直接把任务写成 `COMPLETED`。以下表列出所有允许的业务转移，未列出的组合一律返回 `JOB_STATE_CONFLICT`。

| 当前状态 | 触发方/命令 | 前置条件 | 下一状态 | 说明 |
| --- | --- | --- | --- | --- |
| `QUEUED` | 渲染服务开始 | 任务快照、权限和设备档案已冻结 | `RENDERING` | 未生成制品不占用物理设备。 |
| `QUEUED` | 用户取消 | 尚未渲染/提交 | `CANCELLED` | 不创建 attempt。 |
| `RENDERING` | 渲染成功 | 每个所需面均独立制品、单面页数、尺寸、字体、码区和 hash 校验通过；厂牌另有 FRONT→BACK 合并制品 | `READY` | 只保存只读制品清单。 |
| `RENDERING` | 渲染失败 | 错误已记录 | `FAILED` | 不自动重试；重新打印需新 job。 |
| `RENDERING` | 用户取消 | 渲染尚未提交设备 | `CANCELLED` | 清理临时制品引用，保留事件。 |
| `READY` | 客户端领取单面 FRONT | `SINGLE`、设备档案匹配；服务端事务内原子分配 `command_id`、attempt 和 `active_job_id` | `FRONT_IN_PROGRESS` | 服务端先完成分配，客户端随后持久化本地意图；一个 job 只允许一个执行客户端。 |
| `READY` | 客户端领取手动 FRONT | `MANUAL_DUPLEX`、设备档案匹配；服务端事务内原子分配 `command_id`、attempt 和 `active_job_id` | `FRONT_IN_PROGRESS` | 创建 FRONT attempt，客户端收到动作后再记录本地意图。 |
| `READY` | 客户端领取自动 BOTH | `AUTO_DUPLEX` 且翻面能力 `VERIFIED`；服务端事务内原子分配 `command_id`、attempt 和 `active_job_id` | `AUTO_IN_PROGRESS` | 创建唯一 BOTH attempt；未验证返回能力错误。 |
| `READY` | 用户取消 | 没有设备提交 | `CANCELLED` | 释放尚未占用的任务资源。 |
| `FRONT_IN_PROGRESS` | 客户端报告驱动拒绝且确定未提交 | 同一 commandId，设备确认未接受，且没有已接受/已完成/结果不明事件 | `RESULT_UNKNOWN` | 保留当前 attempt；必须由操作员核对后显式创建新 attempt/command 或取消，不能回到 READY 复用旧 attempt。 |
| `FRONT_IN_PROGRESS` | 客户端报告已提交/队列接受（手动双面） | 仅表示提交接受，不表示出卡 | `AWAITING_FRONT_CHECK` | 必须提示操作员核对正面实卡。 |
| `FRONT_IN_PROGRESS` | 客户端报告已提交/队列接受（单面） | 仅表示提交接受，不表示出卡 | `AWAITING_OUTPUT_CHECK` | 单面没有翻面步骤，直接等待 FRONT 输出核对。 |
| `FRONT_IN_PROGRESS` | 客户端/服务器失联或回执不明 | 可能已提交 | `RESULT_UNKNOWN` | 禁止自动重试，active job 保持。 |
| `AWAITING_FRONT_CHECK` | 操作员确认正面已出 | 当前 FRONT 输出确认 `CONFIRMED_OUT` | `AWAITING_FLIP` | 进入手动翻面步骤。 |
| `AWAITING_FRONT_CHECK` | 操作员确认未出 | 有明确核对结果 | `RESULT_UNKNOWN` | 进入人工处置，只有安全未提交证据才允许新 attempt；不能隐式重印。 |
| `AWAITING_FRONT_CHECK` | 操作员确认损坏 | 已记录卡损坏/方向错误，按现场安全要求处理 | `CANCELLED` | 更换卡重打必须创建新 job；设备未清空前仍保持 active job。 |
| `AWAITING_FRONT_CHECK` | 用户取消 | 已提交，需记录核对结果/设备内是否有卡 | `CANCELLED` | 取消前必须完成安全核对；active job 不因取消请求立即释放。 |
| `AWAITING_FLIP` | 操作员 `flip-confirm` | 当前 job、人员、版本、设备和 FRONT attempt 一致；命令幂等 | `BACK_IN_PROGRESS` | 服务器只接受当前卡翻面确认，并创建唯一 BACK attempt。 |
| `AWAITING_FLIP` | 用户取消 | 卡已取出/设备安全状态已确认 | `CANCELLED` | 保留“未打印背面”原因。 |
| `BACK_IN_PROGRESS` | 客户端报告驱动拒绝且确定未提交 | 同一 commandId，设备确认未接受，且没有已接受/已完成/结果不明事件 | `RESULT_UNKNOWN` | 必须由操作员核对后显式创建新的 BACK attempt/command 或取消；服务器不自动重发。 |
| `BACK_IN_PROGRESS` | 客户端报告已提交/队列接受 | 仅表示背面提交接受 | `AWAITING_OUTPUT_CHECK` | 需要可信设备完成证据或人工核对。 |
| `BACK_IN_PROGRESS` | 失联/回执不明 | 可能已提交 | `RESULT_UNKNOWN` | 不自动重试。 |
| `AUTO_IN_PROGRESS` | 客户端报告已提交/队列接受 | BOTH attempt 已记录 | `AWAITING_OUTPUT_CHECK` | 队列接受不等于同卡两面完成。 |
| `AUTO_IN_PROGRESS` | 设备/客户端确认可能只完成一面 | 无法建立两面可信完成证据 | `RESULT_UNKNOWN` | 操作员核对卡面和方向后处置。 |
| `AUTO_IN_PROGRESS` | 失联/回执不明 | 可能已提交 | `RESULT_UNKNOWN` | 不自动重试。 |
| `AWAITING_OUTPUT_CHECK` | 可信设备证据或操作员确认两面/单面已出 | 每个期望面 `CONFIRMED_OUT`，方向/页序检查通过 | `COMPLETED` | 单面为 FRONT；手动背面为 BACK；自动为 BOTH。 |
| `AWAITING_OUTPUT_CHECK` | 操作员确认未出 | 明确记录未出 | `RESULT_UNKNOWN` | 后续只能显式继续新 attempt 或取消。 |
| `AWAITING_OUTPUT_CHECK` | 操作员确认卡损坏/方向错误 | 已记录损坏原因 | `CANCELLED` | 不能自动重打；设备未清空前仍保持 active job，更换卡必须创建新 job。 |
| `AWAITING_OUTPUT_CHECK` | 用户取消 | 已核对设备内卡状态 | `CANCELLED` | 有物理风险时 active job 继续保持到 `DEVICE_CLEARED`。 |
| `RESULT_UNKNOWN` | 操作员确认已出 | 指定面/`BOTH` 的实卡已核对 | `AWAITING_FLIP` / `COMPLETED` | FRONT 手动任务去 `AWAITING_FLIP`；BACK 或 SINGLE/AUTO 去 `COMPLETED`。 |
| `RESULT_UNKNOWN` | 操作员确认未出并明确继续 | 必须完整满足第 5.1 节及 API 6.4 的续打证据；服务端生成新的 attempt/commandId | `FRONT_IN_PROGRESS` / `BACK_IN_PROGRESS` / `AUTO_IN_PROGRESS` | 这是人工决定的新提交，不是自动重试；自动模式若无法证明未出卡，默认只能取消。 |
| `RESULT_UNKNOWN` | 操作员确认卡损坏或选择取消 | 原卡不再继续使用 | `CANCELLED` | 更换卡/重新打印必须创建新的 job。 |
| `RESULT_UNKNOWN` | 用户取消 | 已记录人工核对 | `CANCELLED` | active job 等设备清空确认后释放。 |
| `FAILED` | 管理员查看 | 终态 | `FAILED` | 不允许从失败任务自动续打；修复后新建 job。 |
| `COMPLETED` | 查询/审计 | 终态且所有面有证据 | `COMPLETED` | 仅允许补充审计，不允许重新驱动。 |
| `CANCELLED` | 查询/审计 | 终态 | `CANCELLED` | 仅允许补充审计；物理占用清除后才释放设备。 |

### 5.1 人工核对结果

`output-check` 必须明确指定 `face` 和 `decision`：

- `CONFIRMED_OUT`：实际看到该面已正确出卡；手动 FRONT 进入翻面，手动 BACK/单面/自动在所有期望面确认后完成。
- `CONFIRMED_NOT_OUT`：确认该面没有出卡，设备内已安全清空。只有驱动可证明未提交，或手动模式已经人工核对同一卡当前面未印、原客户端持久终止旧命令并提交队列清空的 `COMMAND_RETIRED` 证据时，才允许操作员显式选择继续并生成新的 attempt/commandId；自动模式只允许可证明未提交的情况。否则保持核对或取消。迟到旧事件保留审计但不覆盖新 attempt，矛盾出卡证据导致暂停核对。细则见 API 6.4。不能根据超时或“没看到卡”自动发送、复用旧 attempt。
- `CONFIRMED_DAMAGED`：卡已损坏、方向错误或无法继续；当前 job 取消。换卡重打由新 job 执行，防止一张 job 混合两张卡。

## 6. 设备占用、租约和恢复

1. 客户端领取可执行动作时，服务端在同一事务内检查 `PrinterProfile.status`、园区、设备身份、允许模式和 `active_job_id`，空闲时原子写入当前 `job_id` 并分配 command/attempt；已有值属于同一 job 时允许原 claim 恢复/查询，不创建新 attempt；属于其他 job 时返回 `PRINTER_OCCUPIED`，不靠前端按钮禁用防并发。
2. `active_job_id` 从 claim 事务分配开始保持，覆盖本地意图尚未上报、`AWAITING_FRONT_CHECK`、`AWAITING_FLIP`、`AWAITING_OUTPUT_CHECK`、`RESULT_UNKNOWN`、客户端失联和人工取消待清空期间。连接租约过期只表示客户端连接断开，不能清除物理占用。
3. 新客户端重新 claim 时必须先读取原 job/attempt，继续同一 `command_id` 的状态查询或进入人工核对；不能借租约过期接管并重印。
4. 只有任务已处于终态 `COMPLETED`、`CANCELLED` 或 `FAILED`，且设备确认无卡/已安全取出时，服务端才可清空 `active_job_id`。`RESULT_UNKNOWN` 必须先完成人工核对并解析为终态或新的 attempt；即使收到 `DEVICE_CLEARED`，仍有待打印背面时也不能释放占用。否则保持占用并告警。
5. 服务端先在 claim 事务内持久化 command、attempt 和 `active_job_id`，客户端收到动作后把包含 `commandId`、面、制品 hash、任务/版本/档案快照 hash 的意图落到本地持久存储，并先标记 `SUBMISSION_STARTED`，随后调用 b-PAC/HiTi 驱动；恢复时若该标记存在而无可信完成证据，必须上报 `RESULT_UNKNOWN`，不能因服务端未收到 `INTENT_RECORDED` 重发。
6. 服务端按 `eventId` 和 canonical body hash 对事件幂等；人工动作请求按 `Idempotency-Key` 的 body hash 幂等，服务端分配的 command 只关联一次物理提交，但一个 command 可以产生多条不同事件。重复事件返回原处理结果，同 ID 不同 body 返回冲突。这个机制不承诺跨服务、驱动和物理设备的 exactly-once 出卡。

## 7. 快照与保留

- `TemplateVersion` 发布后的布局、字段、资源 manifest、页面规格和 hash 只读；发布/回滚不修改历史版本。
- `PrintJob` 必须保存创建时解析到的 FRONT/BACK（访客仅 FRONT）模板版本、人员快照、打印物、面数、设备档案、模式和各面资源 hash；渲染服务重启/恢复不能重新读取最新模板或人员数据替换快照。
- `PrintAttempt` 保存每次真实提交意图和输出判定，`PrintEvent` 保存状态转移、人工确认、回执和取消原因；默认日志/列表只返回摘要和 hash。
- 与任务关联的 PDF/面图像使用对象存储 ID、hash、园区和授权元数据，不把制品公开到可猜测 URL。下载必须经过平台或已授权打印客户端的园区与设备权限检查。
- 模板版本、任务、尝试和事件的保留期由业务合规策略配置；在保留期内禁止清理仍被任务引用的版本、资源或事件链。

## 8. 与 API 的对应关系

API 字段、错误码和动作边界见 [contracts/print-api.md](contracts/print-api.md)。API 不得新增一个绕过本模型的“直接打印 PDF”入口；正式打印必须经过模板解析、冻结、可信渲染、设备 claim、意图记录和事件回传。

## 15. 批次1受控对象及预览元数据

`SMT_PRINT_OBJECT` 保存不可变对象：`object_id` VARCHAR2(36) PK、`park_id` VARCHAR2(36)、`created_by` VARCHAR2(64)、`purpose` VARCHAR2(32)、`access_scope` VARCHAR2(32)、`owner_id` VARCHAR2(36)、`content_hash` VARCHAR2(80)、`media_type` VARCHAR2(80)、`size_bytes` NUMBER、`created_at` TIMESTAMP、`content_bytes` BLOB。模板上传仅允许 LOGO/BACKGROUND + TEMPLATE，预览对象为 PREVIEW + PRINT_PREVIEW。对象ID由服务端生成且不允许覆盖；浏览器只通过同域授权下载，不能访问对象表或存储地址。人员PHOTO仍走独立人员授权适配，不能用模板图片接口宣称照片权限完成。

`SMT_PRINT_PREVIEW` 保存 `preview_id` VARCHAR2(36) PK、`park_id` VARCHAR2(36)、`created_by` VARCHAR2(64)、`status` VARCHAR2(32)、`created_at` TIMESTAMP、`details_json` CLOB，详情冻结版本/草稿修订与制品引用。只允许创建预览的主体且同时具备当前园区预览权限读回。失去权限后不能下载。制品引用和原始字节都按SHA-256校验，不能用预览生成正式打印任务。

这里只定义待发布的逻辑映射，不是已执行迁移。Oracle DDL、容量/清理策略及真实重启测试尚待确认；不得直接执行H2测试fixture到实际数据库。

### 审计请求关联补充

批次1 `SMT_PRINT_AUDIT.DETAILS_JSON` 保存 `requestId`，来源为打印请求过滤器生成的同一UUID，与HTTP响应头/响应体一致，不从用户提交的请求头复制。离线服务调用生成独立追踪ID。本次无需增加审计列，也未执行数据库迁移。


## 批次2落地补充

- 实際建表清单由 `PrintSchemaManifest` 固定，发布通过显式 CLI 完成；包括14张业务表及 `SMT_PRINT_SCHEMA_RELEASE` 发布账本。字段/索引与 Mapper 的一致性由隔离测试核对；尚未在目标 Oracle 执行。说明见 [数据库发布说明](../../smart-module/smart-platform/docs/print-schema-release.md)。
- `SMT_PRINT_JOB_PREVIEW` 独立保存实际人员预览，归属具体园区和操作员；不会占用打印机。其私有 `detailsJson.confirmation` 保存版本化语义指纹，API不返回内部指纹。任务创建前重新冻结来源、选择、两面布局资源及设备配置，只有与该 READY 预览一致才进入队列。
- 单人任务必填 `previewId`，批量每人各自必填；顶层单一预览不替代逐人核对。复用相同幂等键返回原任务，改变预览或请求内容须使用新键。资料/模板/照片/设备变更要求重新预览。
- 任务 `snapshotJson` 保留来源字段、真实照片字节、实际选择与自动匹配依据、渲染请求、设备完整配置；任务详情仅输出冻结姓名/工号及打印机名称/型号/取放卡说明等白名单。历史卡面从已冻结的制品下载，不按当前人员资料重绘。
- 人员照片通过 `personPhoto` 必填 image 绑定纳入冻结资源，绑定主体和内容 hash，双端校验 PNG/JPEG 完整性、像素与字节上限。模板草稿不存真实人员照片，静态资源接口不能上传人员照片。
- `calibration.frontFeedInstruction/backFeedInstruction` 为已验收设备取放卡说明；未配置时界面提示核对原工作站现场记录，不生成默认方向。设备身份/驱动/SDK/介质/方向变化后须重新验收，旧任务保留原冻结配置。
