# Research: 入厂申请随行人员搜索

## Decision 1: 使用相关 `EXISTS` 子查询

- **Decision**: 姓名条件使用“主申请人姓名匹配 OR 存在随行人员姓名匹配”；证件号条件
  使用“存在随行人员证件号匹配”。
- **Rationale**: 现行 SQL 无条件外连随行表。实库聚合显示默认列表产生 9,648 行而仅有
  7,756 张申请；移除随行外连后为 7,764 行。`EXISTS` 保持主申请单的结果粒度。
- **Alternatives considered**: 继续复用外连接会造成一对多行数放大；在外连接结果上增加
  `OR` 条件会混合空扩展行和匹配行，难以保证语义及去重。

## Decision 2: 本次不创建搜索索引

- **Decision**: 不创建 `FELLOW_NAME` 或 `CERT_NO` 的普通 B-tree 索引。
- **Rationale**: 既有搜索是包含式匹配，前置通配符不能被普通 B-tree 有效使用；当前随行表
  仅约 2 MiB。创建索引并不能兑现本功能的查询效率目标。
- **Alternatives considered**: Oracle Text 或改变为前缀/精确匹配会改变运行与产品语义，超出本次范围。

## Decision 3: 数据库治理单列后续项

- **Decision**: 不在功能修复中创建索引、刷新统计信息、清理 25 条孤儿随行记录或补约束。
- **Rationale**: 这些操作影响真实数据与数据库对象，需要独立授权、回滚计划及更新后的
  执行计划验证。
- **Alternatives considered**: 直接在本次提交 DDL 会违反零数据库变更范围。
