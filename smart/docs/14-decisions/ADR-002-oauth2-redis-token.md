# ADR-002：OAuth 2.0 + Redis Token（而非 JWT）

- 状态：Accepted（生产运行中）
- 日期：约 2020（推断）

## 背景

Token 持久化策略可选：

| 方案 | 优点 | 缺点 |
|------|------|------|
| JWT 自包含 | 无状态、易扩展、不依赖中间件 | 难以提前吊销；变更权限需 Token 全部过期 |
| Redis 集中存储（不透明 Token） | 可即时吊销；权限变更立即生效；体积小 | 强依赖 Redis 可用性 |

## 决策

采用 **Spring Security OAuth `RedisTokenStore`**，前缀 `smart_oauth:`，配合自定义 `TokenEnhancer` 注入业务字段（`user_id`、`parkList`、`isStrongPwd` 等）。

## 理由

1. **多园区与权限频繁调整**：管理员授权 / 收回需立即生效，集中存储天然支持；
2. **登出即失效**：园区场景下要求严格，JWT 难实现；
3. **Token 体积**：移动端/网关传输成本敏感；
4. **Refresh Token 不重复使用**：通过 Redis 标记已使用，防止重放；
5. 团队已熟悉 Redis 运维。

## 后果

正面：
- Token 管理完全可控；
- 业务字段（如 `parkList`）可一次注入、各业务子系统读取，无重复查询。

负面：
- Redis 是登录链路单点，必须保证高可用；
- Spring Security OAuth 2.3.4 已 EOL；未来迁移到 Spring Authorization Server 时需重写存储层（[TD-004](../12-risks/tech-debt.md)）；
- 跨集团/跨园区联邦场景下，自包含 JWT 会更便利，未来若有联邦需求需重评。

## 相关

- [08-security/authentication.md](../08-security/authentication.md)
- [05-tech-stack/tech-stack.md](../05-tech-stack/tech-stack.md)
