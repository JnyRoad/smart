# 设备卡片"所属权限组"查询扩展到三个设备页面 —— 设计文档

- 日期：2026-07-01
- 所属子项目：`smart-ui`
- 状态：设计已与用户对齐，待写实施计划

## 1. 背景

`xc_guard/index.vue`（设备管理 → 门禁管理）在本次上线的 PR 里已经加了"所属权限组"按钮：点击设备卡片上的按钮，调用后端 `GET /platform/device/{deviceId}/decommission/plan`（`DeviceDecommissionServiceImpl.plan`），弹窗展示这台设备当前绑定的权限组名称及受影响的员工/车辆数。

用户反馈：`gate.vue`（闸机管理）、`attendance.vue`（考勤机管理）、`entrance_guard.vue`（门禁机管理）这三个页面也需要同样的查询入口。

确认过的关键事实：

- 后端 `plan(deviceId)` 只按 `deviceId` 查通用表 `smt_device_authority_relation`，不区分设备类型，闸机/考勤机/门禁机的设备可以直接复用，**后端不需要任何改动**。
- 这三个页面已经 `import { xcGuardApi } from './xc_guard/_service'`（用于清空/重新下发等已有功能），`getDecommissionPlan` 已经在这个共享 service 里，**不需要新增 import 或改 `_service.js`**。
- 三个页面设备卡片的按钮区（`circle-btns` 旁的 `perm-btn` 组）跟 `xc_guard/index.vue` 加按钮之前的结构完全一致（清空/重新下发/通关人员三件套）。

## 2. 目标（本次范围）

在 `gate.vue`、`attendance.vue`、`entrance_guard.vue` 三个文件里，各自原样复刻 `xc_guard/index.vue` 已有的"所属权限组"功能：

1. 设备卡片按钮区，紧跟"通关人员"按钮后面加：
   ```html
   <el-button type="primary" @click="viewAuthorities(item)" class="perm-btn" plain round>所属权限组</el-button>
   ```
2. `methods` 里加入与 `xc_guard/index.vue` 完全一致的 `viewAuthorities(item)` 方法（调用 `xcGuardApi.getDecommissionPlan`，`$msgbox` + `$createElement` 展示列表，空数据/接口报错分支和中文注释原样保留）。

弹窗文案（标题"所属权限组"、列表项"权限组名（N 名员工 / N 辆车）"、空数据"该设备当前未绑定任何权限组"、接口报错提示"查询失败，请稍后重试"）三个页面完全一致，不做任何针对设备类型的文案调整。

**不在本次范围**：

- 不改动后端 `smart-platform-biz`，接口是通用的且已经构建部署过。
- 不新增前端单元测试，跟 `xc_guard` 当初加这个按钮时的处理方式保持一致（`viewAuthorities` 本身没有 `.test.js` 覆盖）。
- 不顺手修 `gate.vue` 里已存在的重复按钮问题（"重新下发"误绑到 `permittedList`）——历史遗留问题，与本次改动无关，不在这次动它。

## 3. 设计

三个文件改动模式完全一致，逐个套用即可，没有需要额外设计的架构决策：

| 文件 | 模板改动 | methods 改动 | 依赖 |
|---|---|---|---|
| `gate.vue` | 按钮区加"所属权限组" | 加 `viewAuthorities` | 已有 `xcGuardApi` |
| `attendance.vue` | 同上 | 同上 | 同上 |
| `entrance_guard.vue` | 同上 | 同上 | 同上 |

## 4. 测试计划

- 不新增前端单元测试（同 `xc_guard` 现有处理方式）。
- 人工验证：分别在 `/platform/device/gate`、`/platform/device/attendance`、`/platform/device/entrance_guard` 三个页面，对已绑定权限组的设备和未绑定的设备各点一次"所属权限组"按钮，确认弹窗展示正常；同时验证接口异常时的报错提示。

## 5. 风险

几乎为零。纯粹是把已经在生产验证过的只读查询功能，复制到结构相同的兄弟页面，没有新接口、没有新状态、没有权限判断分支。
