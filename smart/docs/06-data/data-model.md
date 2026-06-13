# 数据模型（DB）

> ⚠️ 仓库中**没有 .sql 初始化脚本**。表结构由 DBA 维护在数据库侧，本文档基于 `smart-upms-api` 的 Entity 反推。生产 DDL 以 DBA 提供版本为准；新增字段须同步更新本表与对应 Entity。

## 实体清单与表映射

| Entity | 表名 | 主键 | 主要字段 | 说明 |
|--------|------|------|---------|------|
| SysUser | `sys_user` | user_id | username, password, salt, phone, avatar, dept_id, wx_openid, qq_openid, full_name, is_strong_pwd, del_flag | 用户主表 |
| SysRole | `sys_role` | role_id | role_name, role_code, role_desc, del_flag | 角色 |
| SysMenu | `sys_menu` | menu_id | name, permission, parent_id, component, path, type(0菜单/1按钮), icon, sort, del_flag | 菜单/按钮权限树 |
| SysDept | `sys_dept` | dept_id | dept_name, parent_id, sort, del_flag | 部门 |
| SysDeptRelation | `sys_dept_relation` | (parent_id, child_id) | - | 维护部门树祖孙路径 |
| SysUserRole | `sys_user_role` | (user_id, role_id) | - | 用户↔角色 |
| SysRoleMenu | `sys_role_menu` | (role_id, menu_id) | - | 角色↔菜单 |
| SysUserPark | `sys_user_park` | (user_id, park_id) | - | **用户↔园区**（多园区核心） |
| SysDict | `sys_dict` | dict_id | dict_type, dict_value, dict_label, sort, remark, del_flag | 业务字典 |
| SysLog | `sys_log` | log_id | username, method, params, operation, time, create_time, type(0正常/1异常) | 操作审计 |
| SysOauthClientDetails | `sys_oauth_client_details` | client_id | client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity | OAuth 客户端 |
| SysSocialDetails | `sys_social_details` | id | user_id, provider(wx/qq/yht), openid, nickname | 第三方账号绑定 |
| SysRouteConf | `sys_route_conf` | id | route_id, predicates, filters, uri, order_number | 网关动态路由（同时写 Redis） |
| SysMoveDataTask | `sys_move_data_task` | task_id | task_name, task_desc, status | 旧系统→3.0 迁移任务 |

## 通用字段

所有业务表均含：

- `create_time` / `update_time`：MyBatis-Plus 自动填充
- `del_flag`：逻辑删除（`0`=正常，`1`=删除），MyBatis-Plus 全局拦截
- 部分表含 `create_by` / `update_by`

## 关系图（文字）

```
sys_user ──N:M── sys_role ──N:M── sys_menu
   │
   ├──N:1── sys_dept ──parent_id─→ sys_dept (自引用)
   │                  ↳ sys_dept_relation (祖孙路径)
   │
   ├──N:M── sys_user_park (park_id 仅记录归属，不外键到 park 表)
   │
   └──1:N── sys_social_details (provider + openid)
```

## 注意点

- `sys_user_park.park_id` **不外键** 到任何园区表；园区元数据存在于业务子系统或集团统一基础数据库（仓库内不维护）。登录时仅把 user 关联的 park_id 列表注入 Token。
- `sys_route_conf` 与 Redis **双写**：管理 API 同步写 DB + Redis，并通过 Nacos 事件通知网关刷新内存路由。
- `sys_oauth_client_details` 同时被 Spring Security OAuth2 的 `JdbcClientDetailsService` 读取，字段名必须与其规范一致。

## DDL 演进流程（建议补建）

仓库内当前没有 migration 工具（Flyway / Liquibase），变更通过 DBA 手工执行。建议：

1. 在 `docs/06-data/migrations/` 目录维护按日期/版本号命名的 SQL 文件。
2. 每次 DDL 变更同步更新本文档的表/字段。
3. 长期引入 Flyway 自动化（见 [tech-debt.md](../12-risks/tech-debt.md)）。
