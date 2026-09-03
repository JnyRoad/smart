# Research: 保密供应商软删除

## Decision: 逻辑删除值显式固定为 0/1

- **Decision**: 两个实体字段使用 `@TableLogic(value = "0", delval = "1")`。
- **Rationale**: 目标表要求 `0` 表示有效、`1` 表示删除；显式声明不依赖全局 MyBatis-Plus 配置。
- **Alternatives considered**: 仅依赖默认逻辑删除值。未采用，因为字段取值是本功能的数据契约。

## Decision: 所有自定义 SQL 显式过滤

- **Decision**: 在供应商分页、通知候选、树/下拉、人员分页及保密区订单详情关联中显式过滤 `DEL_FLAG=0`。
- **Rationale**: `@TableLogic` 只自动作用于 MyBatis-Plus 内置 CRUD；手写 XML 不会自动收敛。
- **Alternatives considered**: 由 Vue 端过滤。未采用，因为会遗漏非 UI 调用方并违反后端查询边界。

## Decision: 关联读取与删除/新增使用同一供应商状态边界

- **Decision**: 服务级人员读取使用人员与供应商的单条关联查询；人员新增/更新和供应商删除在事务内锁定同一条有效供应商记录。
- **Rationale**: 先读取供应商、再读取或新增人员会留下并发窗口，删除可能夹在两次 SQL 之间，使失效数据短暂返回或产生失效供应商下的有效人员。
- **Alternatives considered**: 只靠服务层两次有效性校验。未采用，因为无法保证两次独立 SQL 之间的状态不变化。

## Decision: 订单保留，隐藏失效供应商当前名称

- **Decision**: 订单详情到供应商表的左连接限定为有效供应商；订单主记录仍照常返回。
- **Rationale**: 满足“已删除供应商不显示在 UI”，同时不删除可追溯的保密区订单。
- **Alternatives considered**: 物理删除供应商或删除订单。未采用，因为均破坏历史可追溯性。

## Decision: 不新增索引

- **Decision**: 发布脚本补字段、默认值和 `0/1` 检查约束，不创建索引。
- **Rationale**: 当前 Oracle 的字段、索引、统计信息和执行计划未在本轮获得；不能将推测写成性能承诺。
- **Alternatives considered**: 为 `DEL_FLAG` 单列建索引。未采用，因为低选择性布尔列通常不适合单列索引，是否需要联合索引必须基于生产计划决定。

## Decision: PDA 明确留待后续

- **Decision**: 本次不新增 PDA/扫码的“已失效”接口或文案。
- **Rationale**: 用户明确延后 PDA 端实现；后端查询契约先保证管理端不会读取失效数据。
- **Alternatives considered**: 顺带实现扫码提示。未采用，避免扩大范围。
