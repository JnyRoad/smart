# 入厂申请记录搜索契约

## Existing request

`GET /platform/manage/admittance/apply/page`

| Parameter | Meaning | Compatibility |
|-----------|---------|---------------|
| `visitorName` | 访客或随行人员姓名的包含式搜索词 | 字段名不变 |
| `certNo` | 随行人员证件号的包含式搜索词 | 字段名不变 |
| Existing filters | 园区、状态、日期、被访人等条件 | 继续与上述条件组合 |

## Response contract

响应仍以入厂申请单分页返回。即使多位随行人员匹配，同一申请单最多出现一次。
