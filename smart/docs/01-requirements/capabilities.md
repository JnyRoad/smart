# 产品能力清单（CAP）

| CAP-ID | 能力 | 关联 REQ | 关联模块 |
|--------|------|---------|---------|
| CAP-AUTH | 多协议身份认证（账密 / 短信 / 人脸 / 微信 / YHT / 社交） | REQ-001, REQ-010 | smart-auth |
| CAP-TOKEN | OAuth 2.0 Token 签发、刷新、撤销 | REQ-001 | smart-auth + common-security |
| CAP-USER | 用户全生命周期管理（CRUD、密码、强密码刷新、状态） | REQ-001, REQ-012 | smart-upms-biz |
| CAP-ROLE | 角色与权限分配 | REQ-003 | smart-upms-biz |
| CAP-MENU | 菜单树与按钮权限维护 | REQ-003 | smart-upms-biz |
| CAP-DEPT | 部门树管理 | REQ-004 | smart-upms-biz |
| CAP-DICT | 业务字典维护 | REQ-005 | smart-upms-biz |
| CAP-PARK | 用户-园区多对多绑定 | REQ-002 | smart-upms-biz |
| CAP-ROUTE | 网关动态路由配置 | REQ-006 | smart-upms-biz + smart-gateway + common-gateway |
| CAP-CLIENT | OAuth 客户端注册与管理 | REQ-007 | smart-upms-biz |
| CAP-LOG | 操作日志采集与查询 | REQ-008 | common-log + smart-upms-biz |
| CAP-CAPTCHA | 登录验证码防爆破 | REQ-011 | smart-gateway |
| CAP-MIGRATE | 旧系统数据迁移任务跟踪 | REQ-013 | smart-upms-biz |
| CAP-OBSERVE | 链路追踪与服务健康监控 | REQ-014 | 所有模块（Sleuth/Zipkin/Actuator） |
| CAP-CONFIG | 配置中心 + 敏感字段加密 | REQ-009 | 所有模块（Nacos + Jasypt） |
