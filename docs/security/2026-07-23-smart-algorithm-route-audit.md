# SmartAlgorithm 路由最终审计

审计范围：`docs/security/2026-07-22-service-route-inventory.md` 中 SmartAlgorithm 的 25 条 `REVIEW_REQUIRED` 路由；结论基于本仓源码和当前受管配置快照，不以猜测替代生产流量证据。

## 已修复的发现

1. `GET /config/{algorithmType}` 会通过 `AlgorithmConfigDetailDTO.configList[].key/value` 返回算法运行配置；这些值可能包含第三方算法供应商的连接参数或密钥。原先四个 `/config` 读写路由只要求普通登录态，现统一要求 `algorithm_config_manage`。
2. `/test` 的三个 GET 演示入口原先只要求普通登录态；其配套 POST 演示入口虽然已是内部端点，但静态演示前端仍尝试直连。现六条 `/test` 路由均要求内部 `server` 客户端令牌，不再是可被普通员工 token 使用的图片/证件演示 API；其中 `/test/algorithms` 还必须有 `algorithm_config_manage`，因此旧演示客户端默认不可用。`from=Y` 在 Nacos 灰度切换到 `ENFORCE` 后一并强制。

## 逐路由结果

| 路由 | 最终分类与认证 | 数据风险 | 真实调用/处置 | 源码证据 |
| --- | --- | --- | --- | --- |
| `POST /compare/{algorithmType}/{id}` | internal；`@Inner` + `@OpenApi("server")` | 两张人脸图、相似度 | `RemoteAlgorithmService.compare`；App 密码找回/岗位服务等携带 `FROM_IN` 与服务令牌 | `CompareController.java`、`RemoteAlgorithmService.java` |
| `POST /compare/id/{algorithmType}/{id}` | internal；同上 | 图片 ID、相似度 | Feign `compareByImageId`；契约含来源和服务令牌 | 同上 |
| `POST /config` | 管理端专用；登录态 + `algorithm_config_manage` | 可修改算法运行参数/密钥 | 仓内无 Smart UI/H5/App 调用；发布前仅给算法配置管理员授予新权限 | `AlgorithmConfigController.java` |
| `GET /config/{algorithmType}` | 管理端专用；登录态 + `algorithm_config_manage` | **敏感**：配置 `key/value` | 仓内无客户端调用；普通员工 token 已拒绝 | `AlgorithmConfigController.java`、`AlgorithmConfigDetailDTO.java`、`ConfigDetailDTO.java` |
| `GET /config/algorithms` | 管理端专用；登录态 + `algorithm_config_manage` | 算法名称/类型 | 仓内无生产客户端调用 | `AlgorithmConfigController.java` |
| `GET /config/page` | 管理端专用；登录态 + `algorithm_config_manage` | 算法名称/类型 | 仓内无生产客户端调用 | `AlgorithmConfigController.java` |
| `POST /facedetect/{algorithmType}/{faceDetectType}/{id}` | internal；`@Inner` + `@OpenApi("server")` | 人脸/证件 Base64、识别结果 | Feign `faceDetect`；服务令牌契约 | `FaceDetectController.java`、`RemoteAlgorithmService.java` |
| `POST /facedetect/id/{algorithmType}/{faceDetectType}/{id}` | internal；同上 | 图片 ID、识别结果 | Feign `faceDetectByImageId`；服务令牌契约 | 同上 |
| `GET /facedetect/type` | external-authenticated；未使用客户端 token，普通用户仅能得到枚举 | 无 PII，仅检测类型枚举 | 仅被已收口的 `/test/face/detect/type` 复用；仓内无 H5/UI/App 直连 | `FaceDetectController.java`、`TestController.java` |
| `POST /faceservice/featuresExtract` | internal；`@Inner` + `@OpenApi("server")` | 人脸 Base64、特征向量 | Feign `getFaceFeatures`；App 完善资料服务携带服务令牌 | `FaceController.java`、`RemoteAlgorithmService.java` |
| `POST /inner/compare/{algorithmType}/{id}` | internal；`@Inner` + `@OpenApi("server")` | 两张人脸图、相似度 | Feign `compare` 的正式目标 | `CompareApiController.java`、`RemoteAlgorithmService.java` |
| `POST /inner/compare/id/{algorithmType}/{id}` | internal；同上 | 图片 ID、相似度 | Feign `compareByImageId` 的正式目标 | 同上 |
| `POST /inner/face/cut` | internal；`@Inner` + `@OpenApi("server")` | 人脸图片/裁剪结果 | `smart-transfer` 的 `FaceCropInternalClient`，携带服务令牌 | `FaceImgCutController.java`、`FaceCropInternalClient.java` |
| `POST /inner/facedetect/{algorithmType}/{faceDetectType}/{id}` | internal；`@Inner` + `@OpenApi("server")` | 人脸/证件 Base64、识别结果 | Feign `faceDetect` 的正式目标 | `FaceDetectApiController.java`、`RemoteAlgorithmService.java` |
| `POST /inner/facedetect/id/{algorithmType}/{faceDetectType}/{id}` | internal；同上 | 图片 ID、识别结果 | Feign `faceDetectByImageId` 的正式目标 | 同上 |
| `POST /inner/ocr/{algorithmType}/{cardType}/{id}` | internal；`@Inner` + `@OpenApi("server")` | **敏感**：证件 Base64、OCR 文本 | Feign `ocr` 的正式目标 | `OcrApiController.java`、`RemoteAlgorithmService.java` |
| `POST /inner/ocr/id/{algorithmType}/{cardType}/{id}` | internal；同上 | **敏感**：证件图片 ID、OCR 文本 | Feign `ocrByImageId` 的正式目标 | 同上 |
| `GET /test` | internal；`@Inner` + `@OpenApi("server")` | 静态算法演示页 | 无生产调用；旧演示前端不应再经网关发布 | `TestController.java`、`src/frontend/` |
| `GET /test/algorithms` | internal；`@Inner` + `@OpenApi("server")` + `algorithm_config_manage` | 算法类型 | 仅旧演示前端；无生产客户端调用；普通用户和无该权限的服务令牌均拒绝 | `TestController.java` |
| `POST /test/compare/{algorithmType}/{id}` | internal；`@Inner` + `@OpenApi("server")` | 两张人脸图、相似度 | 仅旧演示前端；无 Feign 调用 | `TestController.java` |
| `POST /test/face/detect/{algorithmType}/{faceDetectType}/{id}` | internal；`@Inner` + `@OpenApi("server")` | 人脸/证件 Base64、识别结果 | 仅旧演示前端；无 Feign 调用 | `TestController.java` |
| `GET /test/face/detect/type` | internal；`@Inner` + `@OpenApi("server")` | 无 PII，检测类型枚举 | 仅旧演示前端；无生产客户端调用 | `TestController.java` |
| `POST /test/ocr/{algorithmType}/{cardType}/{id}` | internal；`@Inner` + `@OpenApi("server")` | **敏感**：证件 Base64、OCR 文本 | 仅旧演示前端；无 Feign 调用 | `TestController.java` |
| `POST /ocr/{algorithmType}/{cardType}/{id}` | internal；`@Inner` + `@OpenApi("server")` | **敏感**：证件 Base64、OCR 文本 | Feign `ocr` 已迁至 `/inner/ocr/...`；保留端点仍受内部服务令牌保护 | `OcrController.java`、`RemoteAlgorithmService.java` |
| `POST /ocr/id/{algorithmType}/{cardType}/{id}` | internal；`@Inner` + `@OpenApi("server")` | **敏感**：证件图片 ID、OCR 文本 | Feign `ocrByImageId` 已迁至 `/inner/ocr/id/...`；保留端点仍受保护 | 同上 |

## 可追溯证据索引

| 覆盖路由 | `file:line` 证据 |
| --- | --- |
| `/config/**` 四条 | `smart-module/smart-algorithm/smart-algorithm-biz/src/main/java/com/tce/smart/algorithm/controller/AlgorithmConfigController.java:40`、`:55`、`:70`、`:85`；详情 DTO 的 `configList` / `key` / `value` 在 `smart-module/smart-algorithm/smart-algorithm-api/src/main/java/com/tce/smart/algorithm/api/dto/resp/AlgorithmConfigDetailDTO.java:38`、`ConfigDetailDTO.java:23`、`:26`。 |
| `/compare/**` 和 `/inner/compare/**` 四条 | `smart-module/smart-algorithm/smart-algorithm-biz/src/main/java/com/tce/smart/algorithm/controller/CompareController.java:27`、`:37`、`:49`；`CompareApiController.java:27`、`:36`、`:49`；Feign 正式目标 `smart-module/smart-algorithm/smart-algorithm-api/src/main/java/com/tce/smart/algorithm/api/feign/RemoteAlgorithmService.java:131`、`:146`。 |
| `/facedetect/**` 和 `/inner/facedetect/**` 五条 | `smart-module/smart-algorithm/smart-algorithm-biz/src/main/java/com/tce/smart/algorithm/controller/FaceDetectController.java:31`、`:41`、`:54`、`:69`；`FaceDetectApiController.java:27`、`:36`、`:50`；Feign `RemoteAlgorithmService.java:35`、`:51`。 |
| `/faceservice/featuresExtract` | `smart-module/smart-algorithm/smart-algorithm-biz/src/main/java/com/tce/smart/algorithm/controller/FaceController.java:26`、`:38`、`:40`；Feign `RemoteAlgorithmService.java:177`。 |
| `/inner/face/cut` | `smart-module/smart-algorithm/smart-algorithm-biz/src/main/java/com/tce/smart/algorithm/controller/FaceImgCutController.java:26`、`:34`、`:36`；Feign `RemoteAlgorithmService.java:191`；真实 Transfer 调用 `smart-module/smart-transfer/src/main/java/com/tce/smart/transfer/service/FaceCropInternalClient.java:40`、`:41`。 |
| `/ocr/**` 和 `/inner/ocr/**` 四条 | `smart-module/smart-algorithm/smart-algorithm-biz/src/main/java/com/tce/smart/algorithm/controller/OcrController.java:26`、`:36`、`:49`；`OcrApiController.java:27`、`:36`、`:50`；Feign `RemoteAlgorithmService.java:68`、`:84`。 |
| `/test/**` 六条 | `smart-module/smart-algorithm/smart-algorithm-biz/src/main/java/com/tce/smart/algorithm/controller/TestController.java:41`、`:49`、`:51`、`:58`、`:66`、`:77`、`:88`；其中 `:51` 为遗留算法列表的额外配置管理权限。 |
| Nacos、网关和服务令牌收口 | `docker/nacos/config/dev/smart-algorithm.yml:4` 明确内部审计/强制开关，`:10` 为 `ignore-urls: []`；外网伪造来源头剥离在 `smart/smart-gateway/src/main/java/com/tce/smart/gateway/filter/SmartRequestGlobalFilter.java:45-46`；scope 判定在 `smart/smart-common/smart-common-security/src/main/java/com/tce/smart/common/security/openapi/OpenApiInterceptor.java:53-65`；独立 `client_credentials` 资源和 fail-closed 校验在 `SmartInternalServiceTokenResourceFactory.java:18-27`、`SmartInternalServiceTokenInterceptor.java:29-43`。 |

## Nacos 和上线收口

`docker/nacos/config/dev/smart-algorithm.yml` 的 `security.oauth2.client.ignore-urls` 已是空数组；不得为算法服务添加 `/**`、`/test/**`、`/config/**` 或任意图片/OCR 路由白名单。`@OpenApi("server")` 会拒绝用户 token，并只允许 scope 包含 `server` 的纯客户端令牌；网关同时会剥离外网伪造的 `from` 头。`security.inner.mode` 明确由环境变量控制，默认 `AUDIT` 仅记录历史直连，观察完成后以热更新切至 `ENFORCE`，才会额外拒绝缺少 `from=Y` 的直连请求。

公共 Feign 已在源码中把带内部服务选择头的调用切换到独立 `client_credentials` 资源，固定最小 `server` scope，且缺配置时 fail-closed；算法 Feign 契约也显式传入 `from=Y` 与该选择头。生产 Nacos 的真实凭据、认证服务 client 注册和实际 token 仍未在本次本地审计中读取，均为发布门禁：未在预发探针验证前，禁止把 `SMART_ALGORITHM_INNER_MODE` 切换为 `ENFORCE`，也不能宣称内部调用已通过生产验证。

上线顺序：

1. 在预发用公共 Feign 的独立 `client_credentials` 服务令牌验证其为纯客户端 `server` scope；未验证时不得切换 `ENFORCE`。
2. 在 UPMS 菜单/角色中创建并仅授予算法配置管理员 `algorithm_config_manage`；未配置该权限时接口会安全地返回 403，不能先发布代码再补授权。
3. 在预发验证：普通员工 token 调四条 `/config` 均为 403；算法配置管理员读写成功；用户 token 调任一 `/test/**`、图片、OCR、比对路由为 403；合法 `server` 客户端令牌的现有 Feign 调用成功。
4. 先以 `AUDIT` 灰度 15 分钟，按网关和算法服务日志核验无 `/test/**` 外部流量、无未登记的 `inner-audit` 调用，且 App、Platform、Transfer 的人脸/OCR/裁剪调用成功；然后热更新 `SMART_ALGORITHM_INNER_MODE=ENFORCE` 并复验合法 Feign 带 `from=Y` 成功、缺少该头为 403。若发现未登记调用方，仅为该调用方补齐专属 client_credentials 与服务令牌，不扩大 `ignore-urls`。

## 自动化验证

`AlgorithmConfigurationAccessContractTest` 固化四条配置路由的专用权限、遗留算法列表的额外权限、六条演示路由的内部服务令牌要求，以及 Nacos 不得白名单放行的精确收口；`AlgorithmInternalImageRouteContractTest` 固化全部图片处理控制器及 Feign 头契约。执行：

```text
mvn -pl smart-algorithm-biz -am -Dtest=AlgorithmConfigurationAccessContractTest,AlgorithmInternalImageRouteContractTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test
```
