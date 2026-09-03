# 入厂申请记录搜索契约

## Existing request

`GET /platform/manage/admittance/apply/page`

| Parameter | Meaning | Compatibility |
|-----------|---------|---------------|
| `visitorName` | 访客或随行人员姓名的包含式搜索词 | 字段名不变 |
| `certNo` | 主访客或随行人员证件号的包含式搜索词 | 字段名不变 |
| Existing filters | 园区、状态、日期、被访人等条件 | 继续与上述条件组合 |

## Response contract

响应仍以入厂申请单分页返回。任意数量的匹配随行人员不会因随行人员关系额外扩展结果行；
如同一申请单已有多辆车，既有车辆外连接仍可能产生多行，须与随行人员匹配的基数分别验收。
