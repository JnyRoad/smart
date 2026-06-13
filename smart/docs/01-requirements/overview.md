# 需求总览

> 本文档为**回溯式需求说明**，依据生产代码中已实现的功能反向梳理。任何与许昌园区运营方实际诉求不一致之处，以业务方为准并回写本文。

## 业务目标

为许昌裕同园区提供 **统一身份与权限基座**，让园区内所有业务子系统：

1. 不必各自实现登录与用户管理；
2. 通过统一 OAuth 2.0 协议接入；
3. 复用同一套组织/角色/菜单/权限模型；
4. 通过统一网关暴露，便于安全管控、限流、跨域、灰度。

## 关键需求（REQ）

| ID | 需求 | 验收信号（生产已有实现） |
|----|------|---------------------|
| REQ-001 | 多种登录方式 | smart-auth 提供 /oauth/token、/mobile/token/sms、/ocr/token、/wx/public/token、/yht/token、/mobile/token/social |
| REQ-002 | 单账号多园区 | sys_user_park 表 + Token 内嵌 parkList |
| REQ-003 | 角色-菜单-按钮三级权限 | sys_menu.type 区分菜单(0)/按钮(1)，鉴权通过 @pms.hasPermission('xxx:yyy') |
| REQ-004 | 部门组织树 | sys_dept + sys_dept_relation 维护多级组织 |
| REQ-005 | 字典数据 | sys_dict 集中维护业务枚举 |
| REQ-006 | 网关动态路由 | sys_route_conf 可在运行时增删改路由，Redis + Nacos 事件刷新 |
| REQ-007 | OAuth 客户端管理 | sys_oauth_client_details 表 + /client API |
| REQ-008 | 操作审计 | @SysLog 注解 + 异步事件 + sys_log 表 |
| REQ-009 | 配置敏感数据加密 | Jasypt 加密配置项 |
| REQ-010 | 第三方账号绑定 | sys_social_details（微信、QQ、YHT） |
| REQ-011 | 验证码防爆破 | smart-gateway 的 ValidateCodeGatewayFilter |
| REQ-012 | 密码强度校验 | sys_user.is_strong_pwd + 强密码刷新流程 |
| REQ-013 | 数据迁移任务管理 | sys_move_data_task + /movetask API（旧系统→3.0 切换辅助） |
| REQ-014 | 链路追踪与监控 | Sleuth + Zipkin + Spring Boot Admin |

## 非功能需求

| 类别 | 现状 |
|------|------|
| **可用性** | 单节点部署，未见高可用编排（无 Nginx/SLB/多副本配置） |
| **可扩展性** | 微服务划分支持水平扩展；动态路由支持运行时增加业务模块 |
| **安全性** | OAuth 2.0 + Redis Token + Jasypt + 验证码 + 密码强度策略；XStream 1.4.14 存在已知 CVE，需关注 |
| **性能** | 未见明确 SLA/QPS 目标；MyBatis-Plus + Redis Token 缓存 |
| **可观测性** | Sleuth + Zipkin（链路）+ Spring Boot Admin（健康）+ sys_log（业务审计）；缺统一指标体系 |
| **合规** | 操作日志覆盖关键写操作；个人信息字段（手机号、头像、社交账号）需评估 PIPL 合规（未见专门记录） |

## 范围边界

- **属于本项目**：身份认证、用户/角色/菜单/部门/字典/园区/路由 CRUD、网关、OAuth Token、操作日志。
- **不属于本项目**：门禁、停车、访客、能耗、巡更、设备、考勤、可视化大屏等业务子系统。它们作为 OAuth Client 接入。

## 已知不确定项

- 数据库类型在仓库 bootstrap.yml 中未声明（DB 连接由 Nacos `common.yml` 提供）。从 docker-compose 历史注释中曾出现 `smart-oracle`，疑似 Oracle，需对照运维实际确认。
- 园区粒度的数据隔离规则（用户登录后看到的菜单/数据是否按 park_id 过滤）在代码中未显式实现，Token 仅承载 parkList，**实际隔离由各业务子系统自行根据 parkList 判断**。
- `salaryTypeName` 字段被注入到 Token，业务语义未在仓库注释中说明，疑似与考勤/工资模块联动，待业务方确认。
