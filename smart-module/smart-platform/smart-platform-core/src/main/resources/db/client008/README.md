# Client 008 数据库资源

本目录保存统一客户端服务端增量的版本化 Oracle 初始化资源。应用不会自动执行这些 DDL；生产发布仍需单独授权、变更评审和回滚方案。

`V001__release.sql` 仅创建以下三张保密物品放行新表：

- `SMT_CLIENT_RELEASE`：当前不可变业务快照及 CAS 版本。
- `SMT_CLIENT_RELEASE_EVENT`：逐版本追加的审计事件。
- `SMT_CLIENT_RELEASE_COMMAND`：按服务作用域、操作人和幂等键保存请求摘要及原始回复。

本机集成测试只有在显式设置 `smart.client.008.oracle.test=true` 后才会读取并执行该资源。执行前会核对 Oracle、`FREEPDB1`、`SMART_CLIENT_008` 及上述三表；三表全部不存在时才初始化，已存在时必须与预期结构一致，部分存在或结构不符会失败。测试只删除自身前缀生成的数据，不删除表、schema、数据卷或其他任务数据。

本资源已在本任务独立 Oracle AI Database 26ai Free（`VERSION_FULL 23.26.3.0.0`、ARM）用于本地验证；这不构成生产 Oracle 版本兼容性证明。
