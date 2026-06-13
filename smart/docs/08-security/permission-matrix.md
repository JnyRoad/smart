# 权限模型（Authorization）

## 模型

经典 **RBAC**：

```
User ──N:M── Role ──N:M── Menu(Permission)
```

- `sys_menu.type = 0`：菜单（前端导航）
- `sys_menu.type = 1`：按钮 / 操作权限（不显示，只作权限码）
- `sys_menu.permission`：权限码，命名约定 `资源:操作`（如 `sys_user_view` / `sys_user_add` / `sys_user_edit` / `sys_user_del`）

## 鉴权机制

通过 Spring Security 的 `@PreAuthorize` 注解 + 自定义表达式 bean `PermissionService`（别名 `@pms`）：

```java
@PreAuthorize("@pms.hasPermission('sys_user_view')")
public R<IPage<UserVO>> page(Page page, UserDTO user) { ... }
```

`PermissionService.hasPermission(permission)`：
1. 从 `SecurityContextHolder` 取当前用户的权限码集合（登录时已写入）；
2. 用 `PatternMatchUtils.simpleMatch` 做通配符匹配；
3. 命中返回 true，否则 403。

## 超级管理员

约定 `role_code = ROLE_ADMIN` 的用户拥有全部权限，权限码集合预填 `*`，绕过 PatternMatchUtils 检查。

## 内部调用

服务间 Feign 调用需放行权限：被调用方在 Controller / Service 方法加 `@Inner`，由 `SmartSecurityInnerAspect` 检查请求头 `from` 是否等于 `Y`，是则放行不走 OAuth。

> 注意：`from` 头由 `SmartRequestGlobalFilter` 在网关层 **清洗**（强制移除），防止外部伪造。Feign 调用通过专门的 RestTemplate Interceptor 添加 `from=Y`。

## 权限码命名规约（建议）

```
<module>_<resource>_<action>
   ↓        ↓          ↓
   sys     user       view / add / edit / del / export / import
```

示例：

| 权限码 | 含义 |
|--------|------|
| `sys_user_view` | 查看用户列表 |
| `sys_user_add` | 新增用户 |
| `sys_role_perm` | 配置角色权限 |
| `sys_dict_edit` | 修改字典 |
| `sys_log_view` | 查看操作日志 |
| `sys_route_edit` | 修改网关路由 |

业务子系统接入时按各自模块前缀（如 `door_*` 门禁、`park_*` 停车）并写入 `sys_menu.permission`。

## 数据级权限（行权限）

**本中台无内置数据权限框架**。园区维度通过 Token 中的 `parkList` 由业务子系统自行落地；部门维度需要时由业务方在 SQL 中加 `dept_id IN (子树)` 过滤。

后续可考虑引入 MyBatis-Plus 的 `DataPermissionInterceptor` 统一处理。
