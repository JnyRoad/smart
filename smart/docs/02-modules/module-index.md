# 模块清单（MOD）

> ⚠️ 本文件只列 **smart 仓内部** 的模块。智慧家园 3.0 平台还有 3 个姊妹仓（smart-module / smart-ui / smart-h5），完整平台模块视图见 [yuto-smart/docs/02-modules/module-index.md](../../../docs/02-modules/module-index.md)。

## 姊妹仓对本仓的依赖

| 姊妹仓 | 依赖形式 | 具体 |
|--------|---------|------|
| smart-module | Maven | parent = `com.tce:smart`；各业务模块 pom 直接引用 `smart-common-core / -data / -security / -log / -swagger` 与 `smart-upms-api` |
| smart-module | Feign | 通过 `@FeignClient(name="smart-upms-biz")` 远程调用本仓 UPMS（如读取用户/角色/菜单） |
| smart-module | Nacos | 共用同一 namespace；订阅 `common.yml` 获取 DB/Redis 配置 |
| smart-ui / smart-h5 | HTTP | 经 smart-gateway 调用本仓 + smart-module 的 API |

## 顶层模块

| MOD-ID | 模块 | 端口 | 类型 | 主要职责 |
|--------|------|------|------|---------|
| MOD-GW | smart-gateway | 9990 | Spring Cloud Gateway | 全局入口、路由转发、跨域、验证码校验、密码解密、HTTP Basic、请求头清洗 |
| MOD-AUTH | smart-auth | 3000 | OAuth 2.0 AuthorizationServer | Token 签发/刷新、多渠道登录端点、TokenEnhancer 注入业务字段 |
| MOD-UPMS-BIZ | smart-upms-biz | 4000 | 业务微服务 | 用户/角色/菜单/部门/字典/园区/路由/客户端/日志 CRUD |
| MOD-UPMS-API | smart-upms-api | - | 库 | 跨服务共享的 Entity / DTO / Feign Client |
| MOD-COMMON | smart-common-* | - | 库集合 | 复用组件（见下表） |

## smart-common 子模块

| MOD-ID | 子模块 | 主要类 | 职责 |
|--------|--------|--------|------|
| MOD-CM-BOM | smart-common-bom | pom.xml | 集中管理依赖版本 |
| MOD-CM-CORE | smart-common-core | `Result<T>`、`SecurityConstants`、`TCEException`、`SmartException`、`BaseController` | 统一返回、异常体系、常量、基础控制器、工具类 |
| MOD-CM-DATA | smart-common-data | MyBatis-Plus AutoConfiguration | 数据层增强（分页、逻辑删除、自动填充） |
| MOD-CM-SEC | smart-common-security | `PermissionService(@pms)`、`SmartSecurityInnerAspect`、`ResourceServerConfigurerAdapter` | OAuth2 资源服务器、@PreAuthorize 支持、@Inner 内部调用放行 |
| MOD-CM-GW | smart-common-gateway | `RedisRouteDefinitionWriter`、`DynamicRouteAutoConfiguration` | 动态路由加载与刷新（被 smart-gateway 使用） |
| MOD-CM-LOG | smart-common-log | `@SysLog`、`SysLogAspect`、`SysLogListener` | 操作日志环切 + 异步事件入库 |
| MOD-CM-SWAGGER | smart-common-swagger | `SwaggerAutoConfiguration` | Swagger 2.9 自动装配 + Authorization 全局参数 |

## 依赖关系

```
smart-gateway        →  smart-common-{core, gateway, swagger}
smart-auth           →  smart-common-{core, security, data, swagger, log}, smart-upms-api
smart-upms-biz       →  smart-common-{core, security, data, swagger, log}, smart-upms-api
smart-upms-api       →  smart-common-core
smart-common-*       →  smart-common-bom（版本统一）
```

所有微服务 → Nacos（注册 + 配置）、Redis（Token / 路由 / 缓存）、关系数据库。

## 模块边界原则

- **API 与实现分离**：smart-upms-api 仅含 Entity/DTO/Feign 声明，不承载业务逻辑；其他服务通过引入 api 包进行远程调用。
- **公共组件下沉到 common**：任何被两个及以上服务使用的工具、配置、切面应放入 smart-common-* 的相应子模块。
- **安全切面在 common-security 统一实现**：业务模块只使用注解（@PreAuthorize / @Inner / @SysLog），不重复实现鉴权逻辑。
- **网关只做横切关注点**：路由、限流、跨域、验证码、解密；不做业务判定。
