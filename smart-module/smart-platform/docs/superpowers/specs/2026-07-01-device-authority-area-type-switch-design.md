# 通关权限「权限性质」切换 + 设备绑定体验优化 设计文档

- 日期：2026-07-01
- 涉及页面：`/platform/area/limitAccess`、`/platform/area/limit`、`/platform/area/limitAttendance`（同一张表，仅 `deviceUseType` 不同，UI 上呈现为门禁/通行/考勤三个入口）
- 涉及子项目：`smart-module/smart-platform`（后端）、`smart-ui`（前端）

## 1. 背景与根因

`smt_device_authority`（通关权限组）有一个 `area_type` 字段（0-公共区域，1-保密区域）。这个属性并不是挂在设备上的，而是通过 `smt_device_authority_relation` 关联表间接“传导”到设备上：**同一台设备不允许同时被“性质不同”的两个权限组引用**，后端已有的 `SmtDeviceAuthorityMapper.countByAreaType` 会做这个互斥校验（用于新增设备时过滤掉“已被对方性质占用”的设备）。

现状“无法切换权限性质”不是单纯的 UI 锁死，而是一个真实的功能缺陷：

- [edit.vue](../../../../smart-ui/src/views/platform/area/limit/edit.vue) 把 `editform.areaType` 直接绑定为设备树接口 `/device/authority/person/tree/{parkId}/{areaType}` 的查询参数。一旦在编辑页改动权限性质下拉框，设备树会立刻按**新性质**重新拉取。
- 此时权限组在数据库里还是**旧性质**，组内原本已绑定的设备会被 `countByAreaType` 判定为“跨组冲突”（因为它们当前挂在一个 `area_type` 还是旧值的组下），从树里“消失”。
- 如果管理员没注意到就点了保存，`onSubmit` 会读 `this.$refs.limitree.getCheckedKeys()`，此时读到的是被过滤掉部分设备之后的选中集合。`updateDeviceAuthority` 会把“消失”的设备当成用户主动取消勾选，级联撤销这些设备上员工/车辆的通行权限——这是一个会静默丢真实业务数据的坑，不是体验问题。
- 车辆权限组（`type=3`）前端把下拉框写死 `disabled=true` 完全锁死；人员权限组（`type=1`）反而没锁，是个没人踩过的地雷。

结论：**性质切换绝不能和“编辑设备清单”共用同一个表单/同一次保存**，否则任何实现都会重新踩上面这个坑。

## 2. 已确认的产品决策

1. **跨组冲突处理**：切换时如果组内设备同时被“性质不同”的其他权限组占用，**直接阻止切换**，把冲突设备列出来，要求管理员先去对应权限组手动移除，再回来切换。不做自动级联移除。
2. **入口形态**：新增一个**独立的“变更性质”入口**（列表操作列按钮 + 小弹窗），弹窗里只改 `area_type` 这一个字段，不加载、不展示设备树，与“编辑”功能物理隔离。
3. **留痕要求**：走现有 `@SysLog` 记录一般操作日志即可，不需要审批流。

## 3. 后端设计

### 3.1 新增接口

`POST /device/authority/areaType/switch`

请求体：
```json
{ "id": 1, "areaType": 0 }
```

沿用现有园区鉴权模式（`SecurityUtils.getUser().getParkIdList()`），只允许操作自己园区范围内的权限组。

### 3.2 Service 逻辑（`SmtDeviceAuthorityServiceImpl` 新增 `switchAreaType`）

在单个事务内：

1. 校验权限组存在、`parkId` 在当前用户 `parkIds` 范围内；`targetAreaType` 与当前值相同则直接返回成功（幂等，不做无意义写入）。
2. 查询该权限组下所有设备（`smt_device_authority_relation.authority_id = id`）。若为空组，天然无冲突，直接进入第 4 步。
3. 冲突检测：对组内每台设备，检查是否存在**其他**权限组（`authority_id != id`）引用了同一设备，且那个组当前的 `area_type != targetAreaType`。命中即为冲突，收集 `deviceId`、`deviceName`、冲突权限组 `id`/`authorityName`。
   - 复用 `countByAreaType` 现有的判断思路，新增一条批量查询（返回明细而非计数），SQL 写在 `SmtDeviceAuthorityMapper.xml` 里 `countByAreaType` 旁边。
   - 若存在冲突，直接返回失败结果（`Result` 携带结构化冲突列表），**不写库、不动任何 relation/staffAuth/vehicleApply 数据**。
4. 无冲突：只执行 `UPDATE smt_device_authority SET area_type = ? WHERE id = ?`。不 touch `smt_device_authority_relation`、`smt_staff_device_auth`、`smt_vehicle_apply` 等任何下游数据——这是保证不影响现有业务数据的关键，整个操作只改一个字段。
5. `@SysLog("变更通关权限性质")` 记录操作日志。

事务边界：校验和更新在同一个 `@Transactional` 方法内完成，不留跨请求等待窗口，把并发场景下的 TOCTOU 风险降到最低。这是低频后台管理操作，不做 `SELECT ... FOR UPDATE` 级别的加锁，避免过度设计。

**明确不动的范围**（避免实现时误扩大改动面）：
- `smt_staff_device_auth.auth_type`：这是 `SmtSecurityZone`（保密区 OA 审批）模块自己的字段，和 `smt_device_authority.area_type` 是两套独立体系，语义上不相关，**不需要联动更新**。
- 历史审批 / 抓拍 / 通行记录里对权限组名称、性质的展示，均是查询时关联当前 `smt_device_authority` 得到的实时值，切换后旧记录会展示新的性质文案——这是预期行为（性质是权限组当前状态，不是历史快照），本设计不引入快照字段。

### 3.3 数据流

```
管理员点“变更性质” → 前端弹窗只带 {id, targetAreaType} 调用新接口
  → 后端事务内：查关联设备 → 查冲突 → 无冲突则 UPDATE area_type + 记日志 → 提交
  → 前端按返回结果分支：成功则刷新列表；冲突则在弹窗内展示冲突设备清单
```

### 3.4 边界 / 异常

| 场景 | 处理 |
|---|---|
| 权限组不存在 / 不在当前用户园区范围 | 沿用现有 `getById`/`updateById` 的失败返回模式 |
| 目标性质 = 当前性质 | 直接返回成功，幂等短路 |
| 权限组下无设备 | 无冲突，直接放行 |
| 存在冲突设备 | 返回结构化冲突清单，不写库 |

## 4. 前端设计

### 4.1 「变更性质」入口（新增，独立于编辑页）

- 三个列表页（[indexAccess.vue](../../../../smart-ui/src/views/platform/area/limit/indexAccess.vue)、`index.vue`、`indexAttendance.vue`，实现假设三者结构一致，实施时需逐一确认）操作列的 `menu` slot 里，在“编辑/删除/关联员工”旁新增「变更性质」按钮。
- 点击弹出新组件 `AreaTypeSwitchDialog.vue`（新建于 `smart-ui/src/views/platform/area/limit/components/`）：
  - 只读展示权限组名称、当前性质 → 目标性质（单选，只列出“另一个”选项）。
  - **不加载、不渲染设备树**。
  - 提交调用新接口：成功则 toast + 刷新列表；冲突则在弹窗内用小表格展示冲突设备清单（设备名 + 当前占用的权限组名），提示“请先到对应权限组编辑页移除以上设备，再切换性质”，不提供自动移除按钮（对应决策 1）。
- 人员组（type=1）和车辆组（type=3）都支持这个入口，不做类型限制。

### 4.2 收紧 edit.vue 现有的地雷

[edit.vue](../../../../smart-ui/src/views/platform/area/limit/edit.vue:31) 里“权限性质”下拉框的 `:disabled` 固定为 `true`（当前 `areaTypeDisable` 会在 `type!==3` 时变成 `false`，这是那个隐藏地雷）。编辑页的职责收窄为“只管设备清单、名称、备注”，性质变更只走 4.1 的新入口。[add.vue](../../../../smart-ui/src/views/platform/area/limit/add.vue) 保持不变（新增时选性质本来就没有历史数据包袱，不受本设计约束）。

### 4.3 设备绑定 UI 重设计

现状问题：`add.vue`/`edit.vue` 里的 `<el-tree>` 用了 `default-expand-all`，页面会被撑得很长；没有搜索/过滤能力；已选设备只能在树里翻找，看不到一个集中的选中清单。

新增组件 `DeviceTreePicker.vue`（`smart-ui/src/views/platform/area/limit/components/`），替换 `add.vue`/`edit.vue` 里原来那段裸的 `<el-tree>`：

- **左侧**：搜索框 + 树。默认只展开命中搜索或已选中的节点（不再 `default-expand-all`）；搜索复用 [RoomTreePanel.vue](../../../../smart-ui/src/views/platform/dormitory/room/components/RoomTreePanel.vue:21) 里已经在用的 `filterText` + `filter-node-method` 写法，按设备名/楼栋/楼层过滤。
- **右侧**：已选设备面板，**平铺列表、不分组**（讨论中明确过：分组标题是多余信息，设备名本身通常已带楼栋楼层前缀，分组没有额外价值），每项带 × 可直接移除，顶部有“清空”和已选计数。
- **视觉风格**：参照 [authSelectMulti.vue](../../../../smart-ui/src/views/platform/security_area/xc_guard_apply/components/authSelectMulti.vue) 已经做过的两栏样式（计数徽标、hover 态、圆角卡片），保持全站风格一致，不新造一套视觉语言。
- **接口**：`props: { treeData: Array, value: Array }`，`v-model` 语义（emit `input`），组件内部维护选中集合，`add.vue`/`edit.vue` 用 `v-model="editform.checkedlimits"` 接入，不再依赖 `this.$refs.limitree.getCheckedKeys()` 这种在保存时才读取的写法。

组件拆分后，`add.vue`/`edit.vue` 里 `type`/`parkId`/`areaType` 变化时重新拉取 `treeData` 的 watcher 逻辑保持不变，只是把渲染和交互移交给新组件。

## 5. 测试计划

### 5.1 后端

- 单元测试（`SmtDeviceAuthorityServiceImplTest`，mock mapper）：
  - 无冲突场景 → 正确更新 `area_type`，且不调用 relation/staffAuth 的任何写方法。
  - 有冲突场景 → 不写库，返回结构化冲突列表。
  - 目标性质等于当前性质 → 幂等短路，不产生多余 UPDATE。
  - 空组（无设备）→ 直接成功。
  - 权限组不在当前用户 `parkIds` 范围 → 失败返回。
- 集成测试（真实 DB）：构造两个权限组共享一台设备且性质不同，验证切换任一方都会被拦截；解除共享关系后再切换应成功。

### 5.2 前端

- `DeviceTreePicker.vue` 组件测试：搜索过滤命中/不命中、勾选联动右侧列表、右侧 × 移除联动左侧取消勾选、`v-model` 输出正确性。
- 浏览器手动验证（webapp-testing）：
  - 公共→保密、保密→公共、有冲突、无冲突四条路径。
  - 切换性质后确认原有设备关联、关联员工列表行数完全不变（对照切换前后的“关联员工”“关联内部车辆”页数据）。
  - 新设备绑定 UI：大数据量下（模拟几十台设备）搜索、勾选、清空、保存全流程。

## 6. 范围外 / 后续可能的延伸

- 不改动 `SmtSecurityZone`（保密区 OA 审批）模块，两套“保密”概念是独立的，本设计不做打通。
- 不引入审批流；如果后续合规要求变严，可以在 `switchAreaType` 前面插入审批节点，当前设计的“独立接口 + 结构化返回”方式对接审批流改造成本较低。
- 冲突阻断目前要求管理员手动去其他权限组移除设备；如果实际使用中这个手动步骤成为高频痛点，可以再评估“列出冲突设备 + 一键批量移除（需二次确认）”的半自动方案，但本轮不做。
