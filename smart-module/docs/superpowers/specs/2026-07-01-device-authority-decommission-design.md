# 设备下线级联清理权限组 —— 设计文档

- 日期：2026-07-01
- 所属子项目：`smart-module`（`smart-platform` 服务）
- 状态：设计已与用户对齐，待写实施计划

## 1. 背景与问题

园区里的卡机/闸机是阶段性使用的：不用时删除设备记录，过一段时间可能移到别的区域重新启用。当前删除一台设备涉及权限清理时存在四个明确的问题：

1. **反向查找缺失**：只能从"权限组 → 设备"方向查询（`SmtDeviceAuthorityRelationService` 现有方法全部是这个方向），没有"设备 → 权限组"的反查能力，管理员只能把权限组一个个翻看，容易漏掉某个权限组仍绑着这台设备。
2. **删除设备不级联清理**：`SmtDeviceServiceImpl.deleteDevice` 只删除 `smt_device` 行和区域关联（`SmtDeviceArea`），完全不touch `smt_device_authority_relation`，导致设备删除后权限组里留下指向已删除设备的野记录。
3. **权限组变空后无法处理**：权限组编辑页要求至少选中一台设备（`checkedlimits` 前端必填校验），只能通过"改名占位"或"整组删除"来绕过，操作繁琐且容易和其他人员/车辆权限清理的前置校验（`deleteDeviceAuthority` 要求组内无关联人员/车辆才能删除）产生依赖顺序的困扰。
4. **单设备解绑影响全组**：一个权限组绑定多台设备时，目前没有"只解绑其中一台、只撤销这台设备权限、其他设备权限保持不变"的便捷入口，只能编辑整个权限组的设备清单手动去重新勾选。

## 2. 目标（本次范围）

- 删除单台设备时，自动完成：撤销该设备在所有关联权限组下的人员/车辆权限 → 解绑该设备与这些权限组的绑定关系 → 若某权限组因此变空则级联删除该权限组（区域默认权限组/系统内置权限组除外）。
- 提供"设备 → 权限组"反查能力，在设备列表/详情页常驻展示，并复用同一套查询给删除前的影响预览弹窗。
- 删除设备前展示影响预览（受影响权限组、人数、车辆数、会被级联删除的组、需要人工关注的区域默认权限组），管理员确认后才真正执行。

**不在本次范围**：批量删除设备（当前系统本来就没有批量删除设备的入口，仅有单台删除）；不修改 `smt_device_authority_relation`/`smt_device_authority`/`smt_staff_device_auth` 等表结构。

## 3. 架构与组件

在 `smart-platform-core` 新增一个职责单一的编排服务，不新增数据表，不改动现有表结构：

```java
public interface DeviceDecommissionService {
    /** 只读计算：给定设备ID，算出会影响哪些权限组及后续处理方式。预览弹窗和实际执行共用同一个结果，避免两者不一致。 */
    DeviceDecommissionPlan plan(String deviceId);

    /** 按 plan 的结果执行清理：撤销权限、解绑设备、按需级联删组。调用方负责事务边界。 */
    void execute(DeviceDecommissionPlan plan);
}
```

`DeviceDecommissionPlan`（纯数据对象）字段：

- `deviceId`
- `affectedAuthorities: List<AffectedAuthority>`，每项包含：`authorityId`、`authorityName`、该组剩余设备数（去掉本设备后）、受影响员工数、受影响车辆数、`willCascadeDelete`（是否因变空被级联删组）、`isProtected`（区域默认权限组或系统内置权限组，变空后仅解绑不删组）。

`SmtDeviceServiceImpl.deleteDevice` 改为：

```java
@Transactional(rollbackFor = Exception.class)
public Boolean deleteDevice(String deviceId) {
    DeviceDecommissionPlan plan = deviceDecommissionService.plan(deviceId);
    deviceDecommissionService.execute(plan);
    this.removeById(deviceId);
    smtDeviceAreaMapper.delete(...); // 原有逻辑不变
    return Boolean.TRUE;
}
```

预览接口和删除接口都调用 `plan()`；删除接口在管理员确认后再调用 `execute(plan)`，两者共享同一份计算，避免"预览时说影响3个组，真删的时候变成4个"的偏差。

## 4. 核心算法

### 4.1 `plan(deviceId)`

1. 反查绑定关系：`SmtDeviceAuthorityRelationMapper` 新增 `selectByDeviceId(deviceId)`（当前只有权限组→设备方向的查询方法，这里补一个反向查询；若 `smt_device_authority_relation.device_id` 列没有索引，随实施一并加上，避免反查全表扫）。得到这台设备当前绑定的权限组列表。
2. 对每个受影响权限组，查询该组完整设备列表（复用现有 `getDeviceIds`/`getRelationByAuthId`），判断去掉本设备后是否变空。
3. 查询该组下员工数（`smt_staff_device_auth` 按 authId count）、车辆数（`smt_vehicle_apply` 按 authorityId count），作为预览"影响 N 人/N 车"的数据来源。
4. 判断该组是否为区域默认权限组（`smt_business_device_auth` 是否引用了它）或系统内置权限组（复用现有 `DeviceAuthorityEnum.existAuthority`），标记 `isProtected`。
5. 汇总为 `DeviceDecommissionPlan` 返回。

### 4.2 `execute(plan)`

对每个 `affectedAuthority` 执行：

1. 复用 `SmtDeviceAuthorityServiceImpl.updateDeviceAuthority` 中已验证过的"设备差异下发"逻辑（抽取为可共用的方法）：把本设备当作唯一的 `devicesToRemove`，为该组下所有员工/车辆精确生成设备删除任务（复用 `updateStaffFaceAuthOptimized`/`updateVehicleAuthOptimized`），只撤销本设备上的权限，组内其他设备的权限不受影响。
2. 删除 `smt_device_authority_relation` 中对应的那一行。
3. 若该权限组因此变空（`willCascadeDelete = true`）：清空该组下所有 `smt_staff_device_auth`/`smt_vehicle_apply`（此时组内已无设备，直接删记录，无需再发设备任务），然后删除权限组本身。
4. 若该权限组变空但 `isProtected = true`（区域默认权限组或系统内置权限组）：仅完成设备解绑，权限组保留为空壳，不动 `smt_business_device_auth` 引用，交由管理员在区域配置里自行处理。

物理设备侧的权限下发/撤销复用现有 `SmtDeviceTaskService`/`SmtBatchDeviceTaskService` 异步任务队列，不新增下发链路。

## 5. 接口与前端改动

**后端新增**（建议新建 `SmtDeviceDecommissionController`，避免继续加重已有 233 行的 `SmtDeviceController`）：

- `GET /device/{deviceId}/decommission/plan` —— 只读，返回 `DeviceDecommissionPlan`，供"所属权限组"面板和删除预览弹窗共用。

**后端行为变更**：

- `GET /device/delete/{id}`（路径不变）：内部改为先 `plan()` 再 `execute()` 再执行原有的设备删除逻辑，全程在同一个事务内。

**前端（`smart-ui`）**：

1. 设备列表/详情页新增"所属权限组"栏目，调用 `plan` 接口展示当前绑定的权限组名称列表，日常查询无需进入删除流程。
2. 删除设备的确认弹窗从当前的"确定删除吗"升级为展示 `plan` 结果的详情弹窗：受影响权限组列表、每组人数/车辆数、会被级联删除的组（高亮）、区域默认权限组/系统内置权限组（高亮提醒"仅解绑，组会保留为空壳，请自行检查区域默认权限配置"）。管理员确认后才真正调用删除接口。

## 6. 边界情况与异常处理

- 设备当前未绑定任何权限组：`plan()` 返回空列表，预览弹窗显示"未绑定任何权限组"，删除走原逻辑，不视为异常。
- 设备绑在权限组里但组内暂无人员/车辆权限：仍需清理 `smt_device_authority_relation` 那一行，避免遗留野记录（这是当前 `deleteDevice` 缺失的部分）。
- 现有 `/device/auth/clear/{deviceId}`（一键清空设备授权）在无授权数据时会抛异常：`plan/execute` 走独立计算路径，不依赖这个接口；顺带将该接口在空数据时的行为由抛异常改为幂等返回 `true`，在实施阶段一并修复。
- 并发与失败回滚：`execute` 与设备删除包在同一个 `@Transactional(rollbackFor = Exception.class)` 事务里，任何异常整体回滚，不会出现"关联清了一半、设备没删掉"的中间状态。
- 权限组同时是系统内置权限：复用现有 `DeviceAuthorityEnum.existAuthority` 保护逻辑，变空后跳过级联删除，与区域默认权限组同等处理。

## 7. 测试计划

- **Unit（多）**：新增 `DeviceDecommissionServiceTest`，覆盖：
  - 单设备权限组解绑后变空 → 级联删组 + 清空员工/车辆权限。
  - 多设备权限组解绑其中一台 → 只撤销这台设备权限，组和其他设备权限不受影响。
  - 权限组是区域默认权限：变空后不删组，仅解绑，plan 标记 `isProtected`。
  - 权限组是系统内置权限：同上，跳过级联删除。
  - 设备未绑定任何权限组：`plan` 为空，`execute` 为空操作。
  - `plan()` 与 `execute()` 结果一致性：同一个 plan 传入 execute，清理动作与 plan 描述完全对应。
- **Integration（适量）**：`SmtDeviceController`/新 Controller 层，验证删除设备整条链路，含事务回滚场景（清理中途抛异常时设备和关联都不应被删除）。
- **回归**：确认 `SmtDeviceAuthorityServiceImplTest`、`StaffDeviceAuthSyncServiceTest` 不受影响（抽取共用逻辑时需保证 `updateDeviceAuthority` 原有行为不变）。
