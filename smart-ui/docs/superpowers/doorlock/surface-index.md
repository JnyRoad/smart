# Web 复刻入口索引

目标目录均为建议落点，实施时需与 Smart 动态菜单的 component 值和既有组件加载约定一并核对。为保证第一版可逐项对照，暂不把多个旧页面压缩为一个重新设计的“综合工作台”。

| ID | 旧组件组 | 第一版目标组件组 | 核心操作 | 详细规格 |
|---|---|---|---|---|
| WEB-L-001 | views/platform/lock/lock_list 与 detail | src/views/platform/lock/lock_list/ | 锁卡片查询、远程开门、四页签详情、设备配置。 | [锁列表与详情](surfaces/WEB-L-001-lock-list-detail.md) |
| WEB-L-002 | views/platform/facility/device | src/views/platform/facility/device/ | 设备筛选、编辑、解绑、启停、管理员密码及批量修改。 | [设备管理](surfaces/WEB-L-002-device-management.md) |
| WEB-L-003 | views/platform/facility/gateway、type | src/views/platform/facility/gateway/、type/ | 网关准入、关联设备、删除；设备型号 CRUD。 | [网关与型号](surfaces/WEB-L-003-gateway-and-model.md) |
| WEB-L-004 | views/platform/key/person | src/views/platform/key/person/ | 人员、密码、卡片、导入导出和员工查询。 | [人员与凭据](surfaces/WEB-L-004-person-credentials.md) |
| WEB-L-005 | views/platform/key/auth | src/views/platform/key/auth/ | 按人员/设备授权、取消、重新授权、编辑、删除、导出。 | [权限管理](surfaces/WEB-L-005-authorizations.md) |
| WEB-L-006 | views/platform/key/auth-result | src/views/platform/key/auth-result/ | 授权下发任务结果查询和分页。 | [下发结果](surfaces/WEB-L-006-delivery-results.md) |
| WEB-L-007 | views/platform/lock/open_record、pwd_record、report | src/views/platform/lock/open_record/、pwd_record/、report/ | 开门记录、密码修改记录、设备通信日志、导出和 JSON 查看。 | [记录与日志](surfaces/WEB-L-007-records-and-logs.md) |
| WEB-L-008 | views/platform/lock/alarm_record、views/platform/setting/alarm | src/views/platform/lock/alarm_record/、src/views/platform/setting/alarm/ | 告警查询、批量处理、详情、园区级告警设置。 | [告警与设置](surfaces/WEB-L-008-alarms-settings.md) |

## 现有 Smart 页面处理

smart-ui 现有的 platform/dormitory/lock_list、lock_bind、grant_auth、door_open_record 和 platform/device/xc_lock_door 只能作为局部实现、样式或接口迁移参考：

- 它们覆盖的能力不足以替代上述 8 组旧入口；
- 现有锁列表的远程开门操作已被注释，不能视为可复刻的完成能力；
- grant_auth 与 door_open_record 可被选择性复用内部组件，但必须先通过 WEB-L-005 / WEB-L-007 的字段和操作对版；
- 重复菜单只能在全量对版、资源码迁移、历史链接和用户培训完成后受控收敛，不能在第一版开始前直接删除。

## 统一页面交付合同

每个 WEB-L 页面组完成时都必须满足：

1. 实现页面、菜单、资源码、园区数据权限和对应 smart-lock API 封装；
2. 保留旧页面可见的查询、重置、分页、详情、确认、禁用、导出或导入动作；
3. 对每个写操作显示“请求校验中、已受理、待网关/设备确认、完成、失败/待人工处理”等真实状态；
4. 使用服务端权限和状态机作为最终裁定；前端隐藏或禁用不等于授权控制；
5. 有旧运行态证据、新页面截图/录屏、自动化用例和安全差异清单。

