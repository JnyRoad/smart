# 设备下线级联清理权限组 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 删除一台卡机/闸机设备时，自动撤销它在所有权限组下的人员/车辆权限、解绑设备与权限组的绑定关系，并在权限组因此变空时级联删除该权限组（区域默认权限组/系统内置权限组除外），同时提供"设备→权限组"反查能力和删除前的影响预览。

**Architecture:** 在 `smart-platform-biz` 新增 `DeviceDecommissionService`，`plan()` 只读计算受影响权限组及处理方式，`execute()` 按 plan 执行撤销/解绑/级联删组，`decommissionDevice()` 把 plan+execute+设备删除包成一个事务；不新增数据表，只复用/扩展现有 `SmtDeviceAuthorityService`、`SmtDeviceAuthorityRelationService`、`SmtStaffDeviceAuthService`、`SmtVehicleApplyService`、`SmtBusinessDeviceAuthService`。

**Tech Stack:** Java 8、Spring Boot 2.1、MyBatis-Plus、Oracle（手工 PL/SQL 脚本，无 Flyway/Liquibase）、Vue 2 + Element UI（`smart-ui`）。

**规划阶段发现的关键修正**（与已提交的设计文档 [smart-module/docs/superpowers/specs/2026-07-01-device-authority-decommission-design.md](../specs/2026-07-01-device-authority-decommission-design.md) 相比）：设计文档里写的是"改 `SmtDeviceServiceImpl.deleteDevice`"，但 `smart-platform-core` 模块的 pom 只依赖 `smart-platform-api`/`smart-tool`/`smart-common-security`，**不依赖 `smart-platform-biz`**，而 `SmtDeviceAuthorityService`/`SmtDeviceAuthorityRelationService`/`SmtStaffDeviceAuthService`/`SmtVehicleApplyService`/`SmtBusinessDeviceAuthService` 全部在 `smart-platform-biz`。所以级联清理的编排逻辑不能放进 core 模块的 `SmtDeviceServiceImpl`，必须放在 biz 模块，通过一个新的 `DeviceDecommissionService.decommissionDevice()` 方法调用未改动的 `smtDeviceService.deleteDevice()`（core，原样保留）来完成"设备删除"这最后一步。目标行为和设计文档完全一致，只是落地的类/模块位置更正确。

## Global Constraints

- **模块边界**：`smart-platform-core` 不能依赖 `smart-platform-biz`。本次所有新增的编排类（`DeviceDecommissionService`/`Impl`）和对 `SmtDeviceAuthorityService` 的扩展都放在 `smart-platform-biz`；`smart-platform-core` 里的 `SmtDeviceServiceImpl.deleteDevice` 保持完全不变。
- **权限组类型判断用 `DeviceAuthTypeEnum`，不要用 `DeviceTypeEnum`**：`SmtDeviceAuthority.type` 字段实际含义是 `1=人员权限，3=车辆权限`（对应 `com.tce.smart.tool.enums.DeviceAuthTypeEnum.PERSON`/`VEHICLE`）。现有 `SmtDeviceAuthorityServiceImpl.updateDeviceAuthority` 里用 `DeviceTypeEnum.DEVICE_TYPE_1`/`DEVICE_TYPE_3` 做同样的判断——这是历史遗留的命名混用（`DeviceTypeEnum` 实际语义是物理设备类型：闸机/门禁/道闸/摄像头），只是两个枚举的 code 数值刚好都是 1 和 3。新代码一律用语义正确的 `DeviceAuthTypeEnum`，不要照抄 `DeviceTypeEnum` 这个用法。
- **系统内置权限组保护**：复用 `com.tce.smart.tool.enums.DeviceAuthorityEnum.existAuthority(Integer id)`（`id` 落在 1~9 视为系统内置，如 `STAFF(4)`），与 `SmtDeviceAuthorityServiceImpl.deleteDeviceAuthority` 现有保护逻辑保持一致。
- **不要动 `IDeviceService.removeDevice` / `DeviceServiceImpl.removeDevice`**：这个方法当前是死代码（没有任何 Controller 调用它），内部会调用 `RemoteDispatcherService` 通知其他园区服务、且逻辑不完整（只裸删 `smt_device_authority_relation`，不处理员工/车辆权限撤销）。它和这次要做的功能是两回事，激活它会带来跟本次需求无关的远程调用副作用，本计划不碰它。
- **测试风格**：本模块现有测试（如 `SmtDeviceAuthorityServiceImplTest`）一律是纯 JUnit4 (`org.junit.Test`/`Assert`) + Mockito 手工构造依赖，没有 `@SpringBootTest`/MockMvc。新增测试严格照此风格写，不要引入新的测试基础设施。
- **数据库迁移**：Oracle，没有 Flyway/Liquibase，脚本放 `smart-module/database/manual/`，必须是"先查 `USER_INDEXES`/`USER_TABLES` 判断是否已存在，再生成/执行 DDL"的 PL/SQL 匿名块风格（参考 `20260611_add_isc_access_cleanup_indexes.sql`），执行完更新 `smart-module/database/manual/README.md`。
- **前端安全**：弹窗内容包含权限组名称（`authorityName`，管理员自由文本录入），一律用 Element UI 的 `$createElement` 构造 VNode 展示，不要用 `dangerouslyUseHTMLString` 或字符串拼接 HTML，避免权限组名称里带 HTML 标签导致注入。

---

## Task 1: 权限组关联表新增"按设备反查"能力

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/SmtDeviceAuthorityRelationService.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityRelationServiceImpl.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityRelationServiceImplTest.java`
- Create: `smart-module/database/manual/20260701_add_smt_device_authority_relation_device_id_index.sql`
- Modify: `smart-module/database/manual/README.md`

**Interfaces:**
- Produces: `SmtDeviceAuthorityRelationService.getRelationByDeviceId(String deviceId): List<SmtDeviceAuthorityRelation>` —— 后续 Task 3 的 `plan()` 用它做"设备→权限组"反查。

- [ ] **Step 1: 写失败的单元测试**

```java
// smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityRelationServiceImplTest.java
package com.tce.smart.platform.service.impl;

import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityRelationMapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class SmtDeviceAuthorityRelationServiceImplTest {

	@Test
	public void getRelationByDeviceIdQueriesByDeviceIdColumn() throws Exception {
		SmtDeviceAuthorityRelationMapper mapper = Mockito.mock(SmtDeviceAuthorityRelationMapper.class);
		SmtDeviceAuthorityRelationServiceImpl service = new SmtDeviceAuthorityRelationServiceImpl();
		setField(service, "baseMapper", mapper);

		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setAuthorityId(100);
		relation.setDeviceId("device-A");
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Arrays.asList(relation));

		List<SmtDeviceAuthorityRelation> result = service.getRelationByDeviceId("device-A");

		Assert.assertEquals(1, result.size());
		Assert.assertEquals("device-A", result.get(0).getDeviceId());
		ArgumentCaptor<Object> wrapperCaptor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(mapper).selectList(wrapperCaptor.capture());
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException ignored) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=SmtDeviceAuthorityRelationServiceImplTest -DfailIfNoTests=false`
Expected: 编译失败或 `NoSuchMethodError` —— `getRelationByDeviceId` 还不存在。

- [ ] **Step 3: 在接口和实现类里加方法**

在 `SmtDeviceAuthorityRelationService.java` 接口最后一个方法后面加：

```java
	List<SmtDeviceAuthorityRelation> getRelationByDeviceId(String deviceId);
```

在 `SmtDeviceAuthorityRelationServiceImpl.java` 的 `getRelationByAuthId` 方法后面加：

```java
	@Override
	public List<SmtDeviceAuthorityRelation> getRelationByDeviceId(String deviceId) {
		return this.list(
				Wrappers.<SmtDeviceAuthorityRelation>query().lambda()
						.eq(SmtDeviceAuthorityRelation::getDeviceId, deviceId)
		);
	}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=SmtDeviceAuthorityRelationServiceImplTest -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 5: 新增设备维度索引脚本**

```sql
-- smart-module/database/manual/20260701_add_smt_device_authority_relation_device_id_index.sql
-- Optional. Run this once in the current session before creating indexes.
ALTER SESSION SET DDL_LOCK_TIMEOUT = 600;

-- Step 1. Check the index first.
SELECT TABLE_NAME, INDEX_NAME, STATUS
FROM USER_INDEXES
WHERE INDEX_NAME = 'IX_DEV_AUTH_REL_DEVICE_ID'
ORDER BY TABLE_NAME, INDEX_NAME;

-- Step 2. Generate CREATE INDEX statement only if missing.
WITH REQUIRED_INDEXES AS (
	SELECT
		'IX_DEV_AUTH_REL_DEVICE_ID' AS INDEX_NAME,
		'SMT_DEVICE_AUTHORITY_RELATION' AS TABLE_NAME,
		'CREATE INDEX IX_DEV_AUTH_REL_DEVICE_ID ON SMT_DEVICE_AUTHORITY_RELATION (DEVICE_ID)' AS DDL
	FROM DUAL
)
SELECT REQUIRED_INDEXES.INDEX_NAME, REQUIRED_INDEXES.TABLE_NAME, REQUIRED_INDEXES.DDL
FROM REQUIRED_INDEXES
WHERE NOT EXISTS (
	SELECT 1
	FROM USER_INDEXES EXISTING_INDEX
	WHERE EXISTING_INDEX.INDEX_NAME = REQUIRED_INDEXES.INDEX_NAME
	  AND EXISTING_INDEX.TABLE_NAME = REQUIRED_INDEXES.TABLE_NAME
);

-- Step 3. Manual fallback. Uncomment and run only if Step 2 returned the row.
-- CREATE INDEX IX_DEV_AUTH_REL_DEVICE_ID
-- ON SMT_DEVICE_AUTHORITY_RELATION (DEVICE_ID);
```

在 `smart-module/database/manual/README.md` 的"正向脚本"表格里加一行：

```markdown
| `20260701_add_smt_device_authority_relation_device_id_index.sql` | 需要 | 给 `SMT_DEVICE_AUTHORITY_RELATION.DEVICE_ID` 加索引，支撑"按设备反查权限组"功能的查询性能。脚本内置索引存在性判断，可重复执行。 |
```

- [ ] **Step 6: 提交**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/SmtDeviceAuthorityRelationService.java
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityRelationServiceImpl.java
git add smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityRelationServiceImplTest.java
git add smart-module/database/manual/20260701_add_smt_device_authority_relation_device_id_index.sql
git add smart-module/database/manual/README.md
git commit -m "feat(smart-platform): add device-to-authority reverse lookup query"
```

---

## Task 2: 权限组暴露"按设备撤销权限"能力

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/SmtDeviceAuthorityService.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityServiceImpl.java`
- Test: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityServiceImplTest.java`

**Interfaces:**
- Consumes：`SmtDeviceAuthorityServiceImpl` 现有的私有方法 `updateStaffFaceAuthOptimized(Integer authId, List<String> delAuthList, List<String> addAuthList)` 和 `updateVehicleAuthOptimized(Integer authId, List<String> delAuthList, List<String> addAuthList)`（已存在，不改动其内部逻辑）。
- Produces：`SmtDeviceAuthorityService.revokeDeviceAccess(Integer authorityId, String deviceId): void` —— Task 4 的 `execute()` 用它撤销某权限组下所有人员/车辆在指定设备上的权限，不影响该组绑定的其他设备。

- [ ] **Step 1: 写失败的单元测试**

在现有 `SmtDeviceAuthorityServiceImplTest.java` 里追加（沿用文件已有的 `newService`/`setField`/`relation`/`staffAuth`/`vehicleApply` 辅助方法，不用重写）：

```java
	@Test
	public void revokeDeviceAccessOnlyRemovesGivenDeviceForPersonAuthority() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtBatchDeviceTaskService batchDeviceTaskService = Mockito.mock(SmtBatchDeviceTaskService.class);
		SmtDeviceAuthorityServiceImpl service = new SmtDeviceAuthorityServiceImpl(
				Mockito.mock(SmtDeviceService.class),
				authorityMapper,
				relationService,
				Mockito.mock(SmtBusinessDeviceAuthService.class),
				staffAuthService,
				Mockito.mock(SmtDeviceMapper.class),
				Mockito.mock(SmtDeviceTaskService.class),
				Mockito.mock(SmtIscDeviceTaskService.class),
				Mockito.mock(SmtVehicleApplyService.class),
				staffService,
				Mockito.mock(SmtVehicleMapper.class),
				Mockito.mock(SmtIscDeviceTaskServiceImpl.class),
				batchDeviceTaskService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setType(DeviceAuthTypeEnum.PERSON.getCode());
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);

		SmtStaffDeviceAuth staffAuth = staffAuth(1, 1001L, 100);
		Mockito.when(staffAuthService.list(Mockito.any())).thenReturn(Collections.singletonList(staffAuth));

		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("B001");
		staff.setName("张三");
		staff.setFacePicId("pic-1");
		Mockito.when(staffService.list(Mockito.any())).thenReturn(Collections.singletonList(staff));

		service.revokeDeviceAccess(100, "device-A");

		ArgumentCaptor<List> delListCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<List> addListCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(batchDeviceTaskService).createStaffFaceAuthTasks(
				Mockito.anyList(), delListCaptor.capture(), addListCaptor.capture());
		Assert.assertEquals(Collections.singletonList("device-A"), delListCaptor.getValue());
		Assert.assertTrue(addListCaptor.getValue().isEmpty());
	}
```

新增 import（放到文件已有 import 块里）：

```java
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.impl.SmtIscDeviceTaskServiceImpl;
import java.util.Collections;
```

（`SmtDeviceAuthorityMapper`、`SmtDeviceMapper`、`SmtVehicleMapper`、`SmtDeviceService`、`SmtBusinessDeviceAuthService`、`SmtVehicleApplyService`、`SmtBatchDeviceTaskService`、`DeviceAuthTypeEnum`、`Mockito`、`ArgumentCaptor`、`List` 已在文件里 import 过。）

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=SmtDeviceAuthorityServiceImplTest#revokeDeviceAccessOnlyRemovesGivenDeviceForPersonAuthority -DfailIfNoTests=false`
Expected: 编译失败——`revokeDeviceAccess` 方法不存在。

- [ ] **Step 3: 加接口方法**

在 `SmtDeviceAuthorityService.java` 的 `deviceAuthRelationAdd` 方法后面加：

```java
	/**
	 * 撤销某权限组下所有人员/车辆在指定设备上的访问权限，权限组绑定的其他设备不受影响。
	 * 用于设备下线时按设备精确回收权限，不删除员工/车辆与权限组的绑定关系本身。
	 * @param authorityId 权限组ID
	 * @param deviceId 要撤销权限的设备ID
	 */
	void revokeDeviceAccess(Integer authorityId, String deviceId);
```

- [ ] **Step 4: 实现方法**

在 `SmtDeviceAuthorityServiceImpl.java` 的 `deviceAuthRelationAdd` 方法后面加：

```java
	@Override
	public void revokeDeviceAccess(Integer authorityId, String deviceId) {
		SmtDeviceAuthority authority = this.getById(authorityId);
		if (authority == null) {
			return;
		}
		List<String> devicesToRemove = Collections.singletonList(deviceId);
		if (DeviceAuthTypeEnum.PERSON.getCode().equals(authority.getType())) {
			updateStaffFaceAuthOptimized(authorityId, devicesToRemove, Collections.emptyList());
		} else if (DeviceAuthTypeEnum.VEHICLE.getCode().equals(authority.getType())) {
			updateVehicleAuthOptimized(authorityId, devicesToRemove, Collections.emptyList());
		}
	}
```

`DeviceAuthTypeEnum` 已经通过文件顶部 `import com.tce.smart.tool.enums.*;` 引入，不用新加 import。

- [ ] **Step 5: 运行测试确认通过**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=SmtDeviceAuthorityServiceImplTest -DfailIfNoTests=false`
Expected: PASS（含之前已有的 4 个测试一起跑，确认没有回归）

- [ ] **Step 6: 提交**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/SmtDeviceAuthorityService.java
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityServiceImpl.java
git add smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityServiceImplTest.java
git commit -m "feat(smart-platform): expose per-device authority revocation"
```

---

## Task 3: DeviceDecommissionPlan 数据结构 + plan() 只读计算

**Files:**
- Create: `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/model/DeviceDecommissionPlan.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/DeviceDecommissionService.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImpl.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImplTest.java`

**Interfaces:**
- Consumes：Task 1 的 `SmtDeviceAuthorityRelationService.getRelationByDeviceId(String)`、已有的 `getRelationByAuthId(List<Integer>)`；`SmtDeviceAuthorityService`（`IService<SmtDeviceAuthority>` 自带 `listByIds`）；`SmtStaffDeviceAuthService`/`SmtVehicleApplyService`（`IService` 自带 `list(Wrapper)`）；`SmtBusinessDeviceAuthService`（`IService` 自带 `list(Wrapper)`）；`DeviceAuthorityEnum.existAuthority(Integer)`。
- Produces：`DeviceDecommissionService.plan(String deviceId): DeviceDecommissionPlan`，`DeviceDecommissionPlan` 的字段 `deviceId: String`、`affectedAuthorities: List<AffectedAuthority>`；`AffectedAuthority` 字段 `authorityId: Integer`、`authorityName: String`、`remainingDeviceCount: Integer`、`staffCount: Integer`、`vehicleCount: Integer`、`protectedAuthority: boolean`、`willCascadeDelete: boolean`。Task 4（execute）、Task 5（decommissionDevice）、Task 6（Controller）都依赖这个类型和这个方法。

- [ ] **Step 1: 写 DeviceDecommissionPlan 数据结构**

```java
// smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/model/DeviceDecommissionPlan.java
package com.tce.smart.platform.core.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备下线影响计算结果：一台设备当前绑定了哪些权限组，以及每个权限组在设备解绑后应该如何处理。
 * plan() 只读计算此结构，execute() 严格按此结构执行，两者共享同一份计算，保证预览和实际执行一致。
 */
@Data
public class DeviceDecommissionPlan {

	private String deviceId;

	private List<AffectedAuthority> affectedAuthorities = new ArrayList<>();

	@Data
	public static class AffectedAuthority {

		private Integer authorityId;

		private String authorityName;

		/** 该权限组去掉本设备后还剩几台设备 */
		private Integer remainingDeviceCount;

		/** 该权限组下受影响的员工数 */
		private Integer staffCount;

		/** 该权限组下受影响的车辆数 */
		private Integer vehicleCount;

		/** 是否是区域默认权限组或系统内置权限组：变空后不会被自动删除 */
		private boolean protectedAuthority;

		/** 是否会因为设备解绑后变空而被级联删除 */
		private boolean willCascadeDelete;
	}
}
```

- [ ] **Step 2: 写 DeviceDecommissionService 接口**

```java
// smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/DeviceDecommissionService.java
package com.tce.smart.platform.service;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.model.DeviceDecommissionPlan;

/**
 * 设备下线编排服务：给定一个设备ID，计算它牵连了哪些权限组，并按计算结果执行清理。
 */
public interface DeviceDecommissionService {

	/**
	 * 只读计算：给定设备ID，算出会影响哪些权限组及后续处理方式（解绑 / 连带删组 / 保护不删组）。
	 */
	DeviceDecommissionPlan plan(String deviceId);

	/**
	 * 按 plan 的结果执行清理：撤销权限、解绑设备、按需级联删组。
	 */
	void execute(DeviceDecommissionPlan plan);

	/**
	 * 计算 plan、执行清理、再删除设备本身，整体在一个事务里完成。
	 */
	Result decommissionDevice(String deviceId);
}
```

- [ ] **Step 3: 写失败的单元测试（覆盖 plan 的四种场景）**

```java
// smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImplTest.java
package com.tce.smart.platform.service.impl;

import com.tce.smart.platform.core.entity.SmtBusinessDeviceAuth;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.SmtVehicleApply;
import com.tce.smart.platform.core.model.DeviceDecommissionPlan;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.service.SmtBusinessDeviceAuthService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.platform.service.SmtVehicleApplyService;
import com.tce.smart.tool.enums.DeviceAuthTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeviceDecommissionServiceImplTest {

	@Test
	public void planReturnsEmptyWhenDeviceHasNoAuthorityBinding() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		Mockito.when(relationService.getRelationByDeviceId("device-X")).thenReturn(Collections.emptyList());
		DeviceDecommissionServiceImpl service = newService(relationService,
				Mockito.mock(SmtDeviceAuthorityService.class),
				Mockito.mock(SmtStaffDeviceAuthService.class),
				Mockito.mock(SmtVehicleApplyService.class),
				Mockito.mock(SmtBusinessDeviceAuthService.class));

		DeviceDecommissionPlan plan = service.plan("device-X");

		Assert.assertEquals("device-X", plan.getDeviceId());
		Assert.assertTrue(plan.getAffectedAuthorities().isEmpty());
	}

	@Test
	public void planMarksCascadeDeleteWhenDeviceIsTheOnlyOneInAuthority() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		Mockito.when(relationService.getRelationByDeviceId("device-A"))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));
		Mockito.when(relationService.getRelationByAuthId(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));

		SmtDeviceAuthorityService authorityService = Mockito.mock(SmtDeviceAuthorityService.class);
		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setAuthorityName("一号门禁权限组");
		authority.setType(DeviceAuthTypeEnum.PERSON.getCode());
		Mockito.when(authorityService.listByIds(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(authority));

		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		Mockito.when(staffAuthService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(staffAuth(1, 1001L, 100)));

		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		Mockito.when(vehicleApplyService.list(Mockito.any())).thenReturn(Collections.emptyList());

		SmtBusinessDeviceAuthService businessDeviceAuthService = Mockito.mock(SmtBusinessDeviceAuthService.class);
		Mockito.when(businessDeviceAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());

		DeviceDecommissionServiceImpl service = newService(relationService, authorityService,
				staffAuthService, vehicleApplyService, businessDeviceAuthService);

		DeviceDecommissionPlan plan = service.plan("device-A");

		Assert.assertEquals(1, plan.getAffectedAuthorities().size());
		DeviceDecommissionPlan.AffectedAuthority affected = plan.getAffectedAuthorities().get(0);
		Assert.assertEquals(Integer.valueOf(100), affected.getAuthorityId());
		Assert.assertEquals("一号门禁权限组", affected.getAuthorityName());
		Assert.assertEquals(Integer.valueOf(0), affected.getRemainingDeviceCount());
		Assert.assertEquals(Integer.valueOf(1), affected.getStaffCount());
		Assert.assertEquals(Integer.valueOf(0), affected.getVehicleCount());
		Assert.assertFalse(affected.isProtectedAuthority());
		Assert.assertTrue(affected.isWillCascadeDelete());
	}

	@Test
	public void planKeepsAuthorityAliveWhenOtherDevicesRemain() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		Mockito.when(relationService.getRelationByDeviceId("device-A"))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));
		Mockito.when(relationService.getRelationByAuthId(Collections.singletonList(100)))
				.thenReturn(Arrays.asList(relation(100, "device-A"), relation(100, "device-B")));

		SmtDeviceAuthorityService authorityService = Mockito.mock(SmtDeviceAuthorityService.class);
		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setAuthorityName("多设备权限组");
		authority.setType(DeviceAuthTypeEnum.PERSON.getCode());
		Mockito.when(authorityService.listByIds(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(authority));

		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		Mockito.when(staffAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		Mockito.when(vehicleApplyService.list(Mockito.any())).thenReturn(Collections.emptyList());
		SmtBusinessDeviceAuthService businessDeviceAuthService = Mockito.mock(SmtBusinessDeviceAuthService.class);
		Mockito.when(businessDeviceAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());

		DeviceDecommissionServiceImpl service = newService(relationService, authorityService,
				staffAuthService, vehicleApplyService, businessDeviceAuthService);

		DeviceDecommissionPlan plan = service.plan("device-A");

		DeviceDecommissionPlan.AffectedAuthority affected = plan.getAffectedAuthorities().get(0);
		Assert.assertEquals(Integer.valueOf(1), affected.getRemainingDeviceCount());
		Assert.assertFalse(affected.isWillCascadeDelete());
	}

	@Test
	public void planProtectsAreaDefaultAuthorityFromCascadeDelete() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		Mockito.when(relationService.getRelationByDeviceId("device-A"))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));
		Mockito.when(relationService.getRelationByAuthId(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(relation(100, "device-A")));

		SmtDeviceAuthorityService authorityService = Mockito.mock(SmtDeviceAuthorityService.class);
		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setAuthorityName("区域默认权限组");
		authority.setType(DeviceAuthTypeEnum.PERSON.getCode());
		Mockito.when(authorityService.listByIds(Collections.singletonList(100)))
				.thenReturn(Collections.singletonList(authority));

		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		Mockito.when(staffAuthService.list(Mockito.any())).thenReturn(Collections.emptyList());
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		Mockito.when(vehicleApplyService.list(Mockito.any())).thenReturn(Collections.emptyList());

		SmtBusinessDeviceAuthService businessDeviceAuthService = Mockito.mock(SmtBusinessDeviceAuthService.class);
		SmtBusinessDeviceAuth businessDeviceAuth = new SmtBusinessDeviceAuth();
		businessDeviceAuth.setAuthId(100);
		Mockito.when(businessDeviceAuthService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(businessDeviceAuth));

		DeviceDecommissionServiceImpl service = newService(relationService, authorityService,
				staffAuthService, vehicleApplyService, businessDeviceAuthService);

		DeviceDecommissionPlan plan = service.plan("device-A");

		DeviceDecommissionPlan.AffectedAuthority affected = plan.getAffectedAuthorities().get(0);
		Assert.assertEquals(Integer.valueOf(0), affected.getRemainingDeviceCount());
		Assert.assertTrue(affected.isProtectedAuthority());
		Assert.assertFalse(affected.isWillCascadeDelete());
	}

	private DeviceDecommissionServiceImpl newService(SmtDeviceAuthorityRelationService relationService,
													   SmtDeviceAuthorityService authorityService,
													   SmtStaffDeviceAuthService staffAuthService,
													   SmtVehicleApplyService vehicleApplyService,
													   SmtBusinessDeviceAuthService businessDeviceAuthService) {
		return new DeviceDecommissionServiceImpl(relationService, authorityService, staffAuthService,
				vehicleApplyService, businessDeviceAuthService, Mockito.mock(SmtDeviceService.class));
	}

	private SmtDeviceAuthorityRelation relation(Integer authId, String deviceId) {
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setAuthorityId(authId);
		relation.setDeviceId(deviceId);
		return relation;
	}

	private SmtStaffDeviceAuth staffAuth(Integer id, Long staffId, Integer authId) {
		SmtStaffDeviceAuth staffAuth = new SmtStaffDeviceAuth();
		staffAuth.setId(id);
		staffAuth.setStaffId(staffId);
		staffAuth.setAuthId(authId);
		return staffAuth;
	}
}
```

- [ ] **Step 4: 运行测试确认失败**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=DeviceDecommissionServiceImplTest -DfailIfNoTests=false`
Expected: 编译失败——`DeviceDecommissionServiceImpl` 还不存在。

- [ ] **Step 5: 实现 DeviceDecommissionServiceImpl（本步先只实现 plan，execute/decommissionDevice 留到 Task 4/5）**

```java
// smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImpl.java
package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtBusinessDeviceAuth;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.SmtVehicleApply;
import com.tce.smart.platform.core.model.DeviceDecommissionPlan;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.service.DeviceDecommissionService;
import com.tce.smart.platform.service.SmtBusinessDeviceAuthService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.platform.service.SmtVehicleApplyService;
import com.tce.smart.tool.enums.DeviceAuthorityEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 设备下线编排服务：plan() 只读计算，execute()/decommissionDevice() 按计算结果执行清理。
 */
@Service
@AllArgsConstructor
public class DeviceDecommissionServiceImpl implements DeviceDecommissionService {

	private final SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;

	private final SmtDeviceAuthorityService smtDeviceAuthorityService;

	private final SmtStaffDeviceAuthService smtStaffDeviceAuthService;

	private final SmtVehicleApplyService smtVehicleApplyService;

	private final SmtBusinessDeviceAuthService smtBusinessDeviceAuthService;

	private final SmtDeviceService smtDeviceService;

	@Override
	public DeviceDecommissionPlan plan(String deviceId) {
		DeviceDecommissionPlan resultPlan = new DeviceDecommissionPlan();
		resultPlan.setDeviceId(deviceId);

		List<SmtDeviceAuthorityRelation> ownRelations = smtDeviceAuthorityRelationService.getRelationByDeviceId(deviceId);
		if (CollUtil.isEmpty(ownRelations)) {
			return resultPlan;
		}

		List<Integer> authorityIds = ownRelations.stream()
				.map(SmtDeviceAuthorityRelation::getAuthorityId)
				.distinct()
				.collect(Collectors.toList());

		List<SmtDeviceAuthority> authorities = smtDeviceAuthorityService.listByIds(authorityIds);

		Map<Integer, Long> deviceCountByAuthorityId = smtDeviceAuthorityRelationService.getRelationByAuthId(authorityIds).stream()
				.collect(Collectors.groupingBy(SmtDeviceAuthorityRelation::getAuthorityId, Collectors.counting()));

		Map<Integer, Long> staffCountByAuthorityId = smtStaffDeviceAuthService.list(
						Wrappers.<SmtStaffDeviceAuth>lambdaQuery().in(SmtStaffDeviceAuth::getAuthId, authorityIds)).stream()
				.collect(Collectors.groupingBy(SmtStaffDeviceAuth::getAuthId, Collectors.counting()));

		Map<Integer, Long> vehicleCountByAuthorityId = smtVehicleApplyService.list(
						Wrappers.<SmtVehicleApply>lambdaQuery().in(SmtVehicleApply::getAuthorityId, authorityIds)).stream()
				.collect(Collectors.groupingBy(SmtVehicleApply::getAuthorityId, Collectors.counting()));

		Set<Integer> businessDefaultAuthorityIds = smtBusinessDeviceAuthService.list(
						Wrappers.<SmtBusinessDeviceAuth>lambdaQuery().in(SmtBusinessDeviceAuth::getAuthId, authorityIds)).stream()
				.map(SmtBusinessDeviceAuth::getAuthId)
				.collect(Collectors.toSet());

		List<DeviceDecommissionPlan.AffectedAuthority> affectedList = new ArrayList<>();
		for (SmtDeviceAuthority authority : authorities) {
			long totalDeviceCount = deviceCountByAuthorityId.getOrDefault(authority.getId(), 0L);
			int remainingDeviceCount = (int) Math.max(0, totalDeviceCount - 1);
			boolean protectedAuthority = businessDefaultAuthorityIds.contains(authority.getId())
					|| DeviceAuthorityEnum.existAuthority(authority.getId());
			boolean willCascadeDelete = remainingDeviceCount == 0 && !protectedAuthority;

			DeviceDecommissionPlan.AffectedAuthority affected = new DeviceDecommissionPlan.AffectedAuthority();
			affected.setAuthorityId(authority.getId());
			affected.setAuthorityName(authority.getAuthorityName());
			affected.setRemainingDeviceCount(remainingDeviceCount);
			affected.setStaffCount(staffCountByAuthorityId.getOrDefault(authority.getId(), 0L).intValue());
			affected.setVehicleCount(vehicleCountByAuthorityId.getOrDefault(authority.getId(), 0L).intValue());
			affected.setProtectedAuthority(protectedAuthority);
			affected.setWillCascadeDelete(willCascadeDelete);
			affectedList.add(affected);
		}
		resultPlan.setAffectedAuthorities(affectedList);
		return resultPlan;
	}

	@Override
	public void execute(DeviceDecommissionPlan plan) {
		throw new UnsupportedOperationException("implemented in Task 4");
	}

	@Override
	public Result decommissionDevice(String deviceId) {
		throw new UnsupportedOperationException("implemented in Task 5");
	}
}
```

- [ ] **Step 6: 运行测试确认通过**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=DeviceDecommissionServiceImplTest -DfailIfNoTests=false`
Expected: PASS（4 个 plan 场景全部通过）

- [ ] **Step 7: 提交**

```bash
git add smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/model/DeviceDecommissionPlan.java
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/DeviceDecommissionService.java
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImpl.java
git add smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImplTest.java
git commit -m "feat(smart-platform): compute device decommission impact plan"
```

---

## Task 4: execute() 按 plan 执行撤销/解绑/级联删组

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImpl.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImplTest.java`

**Interfaces:**
- Consumes：Task 2 的 `SmtDeviceAuthorityService.revokeDeviceAccess(Integer, String)`；`SmtStaffDeviceAuthService.removeByAuthId(Integer): Boolean`、`SmtVehicleApplyService.removeByAuthId(Integer): Boolean`（均已存在）；`SmtDeviceAuthorityService`/`SmtDeviceAuthorityRelationService` 的 `IService.removeById`/`remove(Wrapper)`（MyBatis-Plus 自带）。
- Produces：`execute(DeviceDecommissionPlan)` 的完整行为，供 Task 5 的 `decommissionDevice` 调用。

- [ ] **Step 1: 写失败的单元测试**

在 `DeviceDecommissionServiceImplTest.java` 里追加：

```java
	@Test
	public void executeRevokesDeviceAndCascadeDeletesEmptyAuthority() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtDeviceAuthorityService authorityService = Mockito.mock(SmtDeviceAuthorityService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		DeviceDecommissionServiceImpl service = newService(relationService, authorityService,
				staffAuthService, vehicleApplyService, Mockito.mock(SmtBusinessDeviceAuthService.class));

		DeviceDecommissionPlan plan = new DeviceDecommissionPlan();
		plan.setDeviceId("device-A");
		DeviceDecommissionPlan.AffectedAuthority affected = new DeviceDecommissionPlan.AffectedAuthority();
		affected.setAuthorityId(100);
		affected.setRemainingDeviceCount(0);
		affected.setWillCascadeDelete(true);
		plan.setAffectedAuthorities(Collections.singletonList(affected));

		service.execute(plan);

		Mockito.verify(authorityService).revokeDeviceAccess(100, "device-A");
		Mockito.verify(relationService).remove(Mockito.any());
		Mockito.verify(staffAuthService).removeByAuthId(100);
		Mockito.verify(vehicleApplyService).removeByAuthId(100);
		Mockito.verify(authorityService).removeById(100);
	}

	@Test
	public void executeOnlyUnbindsDeviceWhenAuthorityStaysAlive() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtDeviceAuthorityService authorityService = Mockito.mock(SmtDeviceAuthorityService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		DeviceDecommissionServiceImpl service = newService(relationService, authorityService,
				staffAuthService, vehicleApplyService, Mockito.mock(SmtBusinessDeviceAuthService.class));

		DeviceDecommissionPlan plan = new DeviceDecommissionPlan();
		plan.setDeviceId("device-A");
		DeviceDecommissionPlan.AffectedAuthority affected = new DeviceDecommissionPlan.AffectedAuthority();
		affected.setAuthorityId(100);
		affected.setRemainingDeviceCount(1);
		affected.setWillCascadeDelete(false);
		plan.setAffectedAuthorities(Collections.singletonList(affected));

		service.execute(plan);

		Mockito.verify(authorityService).revokeDeviceAccess(100, "device-A");
		Mockito.verify(relationService).remove(Mockito.any());
		Mockito.verify(staffAuthService, Mockito.never()).removeByAuthId(Mockito.anyInt());
		Mockito.verify(vehicleApplyService, Mockito.never()).removeByAuthId(Mockito.anyInt());
		Mockito.verify(authorityService, Mockito.never()).removeById(Mockito.anyInt());
	}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=DeviceDecommissionServiceImplTest -DfailIfNoTests=false`
Expected: 两个新测试 FAIL——`execute` 目前直接抛 `UnsupportedOperationException`。

- [ ] **Step 3: 实现 execute()**

把 `DeviceDecommissionServiceImpl.execute` 方法体替换为：

```java
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void execute(DeviceDecommissionPlan plan) {
		if (plan == null || CollUtil.isEmpty(plan.getAffectedAuthorities())) {
			return;
		}
		String deviceId = plan.getDeviceId();
		for (DeviceDecommissionPlan.AffectedAuthority affected : plan.getAffectedAuthorities()) {
			smtDeviceAuthorityService.revokeDeviceAccess(affected.getAuthorityId(), deviceId);
			smtDeviceAuthorityRelationService.remove(
					Wrappers.<SmtDeviceAuthorityRelation>lambdaQuery()
							.eq(SmtDeviceAuthorityRelation::getAuthorityId, affected.getAuthorityId())
							.eq(SmtDeviceAuthorityRelation::getDeviceId, deviceId));
			if (affected.isWillCascadeDelete()) {
				smtStaffDeviceAuthService.removeByAuthId(affected.getAuthorityId());
				smtVehicleApplyService.removeByAuthId(affected.getAuthorityId());
				smtDeviceAuthorityService.removeById(affected.getAuthorityId());
			}
		}
	}
```

需要新增 import：

```java
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
```

（如果编译器提示 `Wrappers.<SmtDeviceAuthorityRelation>lambdaQuery()` 相关类型缺失，确认 `com.baomidou.mybatisplus.core.toolkit.Wrappers` 已经 import，Task 3 里已经加过。）

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=DeviceDecommissionServiceImplTest -DfailIfNoTests=false`
Expected: PASS（6 个测试全部通过：4 个 plan + 2 个 execute）

- [ ] **Step 5: 提交**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImpl.java
git add smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImplTest.java
git commit -m "feat(smart-platform): execute device decommission plan"
```

---

## Task 5: decommissionDevice() 事务化收尾（plan + execute + 删除设备）

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImpl.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImplTest.java`

**Interfaces:**
- Consumes：`SmtDeviceService.deleteDevice(String): Boolean`（core，已存在，本任务不改动它）。
- Produces：`decommissionDevice(String deviceId): Result`，供 Task 6 的 Controller 调用。

- [ ] **Step 1: 写失败的单元测试**

```java
	@Test
	public void decommissionDeviceRunsPlanExecuteThenDeletesDevice() {
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		Mockito.when(relationService.getRelationByDeviceId("device-A")).thenReturn(Collections.emptyList());
		SmtDeviceService deviceService = Mockito.mock(SmtDeviceService.class);
		Mockito.when(deviceService.deleteDevice("device-A")).thenReturn(true);

		DeviceDecommissionServiceImpl service = new DeviceDecommissionServiceImpl(relationService,
				Mockito.mock(SmtDeviceAuthorityService.class),
				Mockito.mock(SmtStaffDeviceAuthService.class),
				Mockito.mock(SmtVehicleApplyService.class),
				Mockito.mock(SmtBusinessDeviceAuthService.class),
				deviceService);

		com.tce.smart.common.core.model.Result result = service.decommissionDevice("device-A");

		Assert.assertTrue(result.isSuccess());
		Mockito.verify(relationService).getRelationByDeviceId("device-A");
		Mockito.verify(deviceService).deleteDevice("device-A");
	}

	@Test
	public void decommissionDeviceRejectsBlankDeviceId() {
		DeviceDecommissionServiceImpl service = newService(
				Mockito.mock(SmtDeviceAuthorityRelationService.class),
				Mockito.mock(SmtDeviceAuthorityService.class),
				Mockito.mock(SmtStaffDeviceAuthService.class),
				Mockito.mock(SmtVehicleApplyService.class),
				Mockito.mock(SmtBusinessDeviceAuthService.class));

		com.tce.smart.common.core.model.Result result = service.decommissionDevice("  ");

		Assert.assertFalse(result.isSuccess());
	}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=DeviceDecommissionServiceImplTest -DfailIfNoTests=false`
Expected: 两个新测试 FAIL——`decommissionDevice` 目前直接抛 `UnsupportedOperationException`。

- [ ] **Step 3: 实现 decommissionDevice()**

把 `decommissionDevice` 方法体替换为：

```java
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result decommissionDevice(String deviceId) {
		if (StrUtil.isBlank(deviceId)) {
			return new Result<>(false, "设备ID不可为空");
		}
		DeviceDecommissionPlan devicePlan = this.plan(deviceId);
		this.execute(devicePlan);
		return new Result<>(smtDeviceService.deleteDevice(deviceId));
	}
```

新增 import：

```java
import cn.hutool.core.util.StrUtil;
```

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=DeviceDecommissionServiceImplTest -DfailIfNoTests=false`
Expected: PASS（8 个测试全部通过）

- [ ] **Step 5: 提交**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImpl.java
git add smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/DeviceDecommissionServiceImplTest.java
git commit -m "feat(smart-platform): wire decommission plan into device deletion"
```

---

## Task 6: 新增预览接口 + 把设备删除接入 decommissionDevice

**Files:**
- Create: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtDeviceDecommissionController.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtDeviceController.java`

**Interfaces:**
- Consumes：Task 3/5 的 `DeviceDecommissionService.plan(String)`、`decommissionDevice(String)`。
- Produces：`GET /device/{deviceId}/decommission/plan` 返回 `Result<DeviceDecommissionPlan>`；`GET /device/delete/{id}`（路径不变）现在内部走级联清理。

这一步是接口装配，没有独立的业务逻辑分支需要新单元测试（`DeviceDecommissionService` 的行为已经在 Task 3-5 覆盖），用手工验证代替：

- [ ] **Step 1: 新增预览 Controller**

```java
// smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtDeviceDecommissionController.java
package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.model.DeviceDecommissionPlan;
import com.tce.smart.platform.service.DeviceDecommissionService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备下线清理
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-设备下线清理")
@RequestMapping("/device")
public class SmtDeviceDecommissionController {

	private final DeviceDecommissionService deviceDecommissionService;

	/**
	 * 预览删除该设备会影响哪些权限组
	 *
	 * @param deviceId 设备ID
	 * @return Result
	 */
	@GetMapping("/{deviceId}/decommission/plan")
	public Result<DeviceDecommissionPlan> plan(@PathVariable("deviceId") String deviceId) {
		return new Result<>(deviceDecommissionService.plan(deviceId));
	}
}
```

- [ ] **Step 2: 把设备删除接入 decommissionDevice**

在 `SmtDeviceController.java` 里，`import` 块加一行：

```java
import com.tce.smart.platform.service.DeviceDecommissionService;
```

在字段声明区（`private IDeviceService bizDeviceService;` 后面）加：

```java
	@Resource
	private DeviceDecommissionService deviceDecommissionService;
```

把原来的：

```java
	@SysLog("删除设备信息表")
	@GetMapping("/delete/{id}")
	public Result removeById(@PathVariable String id) {
		return new Result<>(smtDeviceService.deleteDevice(id));
	}
```

改成：

```java
	@SysLog("删除设备信息表")
	@GetMapping("/delete/{id}")
	public Result removeById(@PathVariable String id) {
		return deviceDecommissionService.decommissionDevice(id);
	}
```

- [ ] **Step 3: 编译验证**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: 跑全量既有测试确认没有回归**

Run: `mvn -pl smart-module/smart-platform/smart-platform-biz -am test`
Expected: BUILD SUCCESS，所有既有测试（含 `SmtDeviceAuthorityServiceImplTest`、`SmtDeviceAuthorityRelationServiceImplTest`、`DeviceDecommissionServiceImplTest`）全部通过。

- [ ] **Step 5: 提交**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtDeviceDecommissionController.java
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtDeviceController.java
git commit -m "feat(smart-platform): expose decommission preview and wire into device delete"
```

---

## Task 7: 修复 /device/auth/clear/{deviceId} 空数据时的幂等行为

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/service/impl/SmtDeviceServiceImpl.java`
- Create: `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/service/impl/SmtDeviceServiceImplTest.java`

这是设计文档"边界情况"里提到的既有小问题：当前 `getPersonAuth`/`getCarAuth` 在无授权数据时会抛 `SmartException`，导致"一键清空"在设备本来就没人绑权限时反而报错。和本次主线功能相互独立，顺带修掉。

**Interfaces:**
- Produces：`SmtDeviceServiceImpl.clearAuth(String)` 在无授权数据时返回 `true`（幂等空操作）而不是抛异常。

- [ ] **Step 1: 写失败的单元测试**

```java
// smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/service/impl/SmtDeviceServiceImplTest.java
package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.DeviceAuthPersonReqDTO;
import com.tce.smart.platform.core.mapper.SmtDeviceAreaMapper;
import com.tce.smart.platform.core.mapper.SmtParkMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.SmtDevicePersonService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtDeviceVehicleService;
import com.tce.smart.platform.core.vo.DeviceVO;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SmtDeviceServiceImplTest {

	@Test
	public void clearAuthIsNoOpWhenDeviceHasNoAuthorizedPerson() {
		SmtParkMapper parkMapper = Mockito.mock(SmtParkMapper.class);
		SmtDeviceTaskService deviceTaskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtDevicePersonService devicePersonService = Mockito.mock(SmtDevicePersonService.class);
		SmtDeviceVehicleService deviceVehicleService = Mockito.mock(SmtDeviceVehicleService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtDeviceAreaMapper deviceAreaMapper = Mockito.mock(SmtDeviceAreaMapper.class);

		SmtDeviceServiceImpl service = Mockito.spy(new SmtDeviceServiceImpl(parkMapper, deviceTaskService,
				devicePersonService, deviceVehicleService, staffMapper, deviceAreaMapper));

		DeviceVO deviceVO = new DeviceVO();
		deviceVO.setDeviceType(DeviceTypeEnum.DEVICE_TYPE_2.getCode());
		Mockito.doReturn(deviceVO).when(service).getDeviceById("device-A");
		Mockito.when(devicePersonService.getDeviceAuthPerson(Mockito.any(Page.class), Mockito.any(DeviceAuthPersonReqDTO.class)))
				.thenReturn(new Page<>());

		Boolean result = service.clearAuth("device-A");

		Assert.assertTrue(result);
		Mockito.verify(deviceTaskService, Mockito.never()).addDeviceDelTaskImmed(
				Mockito.anyList(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
				Mockito.anyInt(), Mockito.any(), Mockito.anyInt(), Mockito.anyString());
	}
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -pl smart-module/smart-platform/smart-platform-core -am test -Dtest=SmtDeviceServiceImplTest -DfailIfNoTests=false`
Expected: FAIL —— 当前实现会抛 `SmartException("授权人员数据为空")`。

- [ ] **Step 3: 去掉空数据时的异常**

把 `SmtDeviceServiceImpl.java` 里的 `getPersonAuth` 方法：

```java
	private List<DeviceAuthPersonRespDTO> getPersonAuth(String deviceId) {
		// 闸机或门禁
		long current = 0;
		long size = 100;
		Page page = new Page(current, size);
		DeviceAuthPersonReqDTO reqDTO = new DeviceAuthPersonReqDTO();
		reqDTO.setDeviceId(deviceId);
		List<DeviceAuthPersonRespDTO> authPersonDTOS = new ArrayList<>();
		do {
			current++;
			page.setCurrent(current);
			IPage<DeviceAuthPersonRespDTO> authPersonPage = devicePersonService.getDeviceAuthPerson(page, reqDTO);
			if (CollectionUtil.isEmpty(authPersonPage.getRecords())) {
				break;
			}
			authPersonDTOS.addAll(authPersonPage.getRecords());
		} while (page.hasNext());
		if (CollectionUtil.isEmpty(authPersonDTOS)) {
			log.info("授权人员数据为空");
			throw new SmartException("授权人员数据为空");
		}
		log.info("授权人员数量：{}", authPersonDTOS.size());
		return authPersonDTOS;
	}
```

改成（去掉 `throw`，空列表直接返回）：

```java
	private List<DeviceAuthPersonRespDTO> getPersonAuth(String deviceId) {
		// 闸机或门禁
		long current = 0;
		long size = 100;
		Page page = new Page(current, size);
		DeviceAuthPersonReqDTO reqDTO = new DeviceAuthPersonReqDTO();
		reqDTO.setDeviceId(deviceId);
		List<DeviceAuthPersonRespDTO> authPersonDTOS = new ArrayList<>();
		do {
			current++;
			page.setCurrent(current);
			IPage<DeviceAuthPersonRespDTO> authPersonPage = devicePersonService.getDeviceAuthPerson(page, reqDTO);
			if (CollectionUtil.isEmpty(authPersonPage.getRecords())) {
				break;
			}
			authPersonDTOS.addAll(authPersonPage.getRecords());
		} while (page.hasNext());
		log.info("授权人员数量：{}", authPersonDTOS.size());
		return authPersonDTOS;
	}
```

同样把 `getCarAuth` 方法里的：

```java
		if (CollectionUtil.isEmpty(authVehicleRespDTOS)) {
			log.info("授权车辆数据为空");
			throw new SmartException("授权车辆数据为空");
		}
```

删掉这一整段 `if` 块，只保留后面的 `log.info("授权车辆数量：{}", authVehicleRespDTOS.size());` 和 `return authVehicleRespDTOS;`。

如果这两处删除后 `SmartException` 这个 import 在文件里已经没有其他地方使用，把对应的 `import com.tce.smart.common.core.exception.SmartException;` 也删掉（`clearAuth`/`repeatAuth` 里各有一处 `throw new SmartException("设备不存在")`，那两处继续保留，`SmartException` 的 import 不用删）。

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -pl smart-module/smart-platform/smart-platform-core -am test -Dtest=SmtDeviceServiceImplTest -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/service/impl/SmtDeviceServiceImpl.java
git add smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/service/impl/SmtDeviceServiceImplTest.java
git commit -m "fix(smart-platform): make device auth clear idempotent on empty data"
```

---

## Task 8: 前端 —— 新增预览接口调用

**Files:**
- Modify: `smart-ui/src/views/platform/device/xc_guard/_service.js`

**Interfaces:**
- Produces：`xcGuardApi.getDecommissionPlan(deviceId): Promise` —— Task 9、Task 10 都调用它。

- [ ] **Step 1: 加接口函数**

在 `smart-ui/src/views/platform/device/xc_guard/_service.js` 的 `xcGuardApi` 对象里，`equipReissue` 方法后面加：

```js
  getDecommissionPlan (deviceId) {
    return request({
      url: `/platform/device/${deviceId}/decommission/plan`,
      method: 'get'
    })
  }
```

（注意在 `equipReissue` 方法体结尾加一个逗号 `,` 分隔对象属性。）

- [ ] **Step 2: 手工验证**

Run: `cd smart-ui && pnpm lint`
Expected: 无新增 lint 错误。

- [ ] **Step 3: 提交**

```bash
git add smart-ui/src/views/platform/device/xc_guard/_service.js
git commit -m "feat(smart-ui): add device decommission plan api"
```

---

## Task 9: 前端 —— 删除设备前展示影响预览

**Files:**
- Modify: `smart-ui/src/views/platform/device/xc_guard/index.vue`

**Interfaces:**
- Consumes：Task 8 的 `xcGuardApi.getDecommissionPlan(deviceId)`。

- [ ] **Step 1: 引入 xcGuardApi（如尚未引入完整对象）**

确认 `index.vue` 顶部已有 `import { xcGuardApi } from './_service'`（Task 里前面读到的现状已经有这行，不用重复加）。

- [ ] **Step 2: 把 `handleDel` 改成"先拉预览、按内容渲染确认弹窗"**

把现有的：

```js
    handleDel: function(row) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该门禁信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj(row.id);
        })
        .then(dataResponse => {
          _this.getList(_this.page, _this.searchForm);
          _this.$notify({
            title: "删除成功",
            message: "删除成功",
            type: "success",
            duration: 2000
          });
        })
        .catch(error => { console.error(error) });
```

改成：

```js
    handleDel: function(row) {
      var _this = this;
      xcGuardApi.getDecommissionPlan(row.id).then(response => {
        const plan = response.data.data || { affectedAuthorities: [] };
        _this.confirmDecommission(row, plan);
      }).catch(error => {
        console.error(error);
        _this.$message.error("查询设备关联权限组失败，请稍后重试");
      });
    },
    confirmDecommission(row, plan) {
      var _this = this;
      const elm = this.$createElement;
      const affected = plan.affectedAuthorities || [];
      const summaryChildren = [
        elm("p", null, "确认删除该门禁信息？"),
      ];
      if (affected.length === 0) {
        summaryChildren.push(elm("p", { attrs: { class: "smallInfo" } }, "该设备当前未绑定任何权限组。"));
      } else {
        summaryChildren.push(elm("p", { attrs: { class: "smallInfo" } }, `该设备绑定在以下 ${affected.length} 个权限组下：`));
        const listItems = affected.map(item => {
          const parts = [`${item.authorityName}（影响 ${item.staffCount} 名员工 / ${item.vehicleCount} 辆车）`];
          if (item.willCascadeDelete) {
            parts.push("—— 权限组将因变空被自动删除");
          } else if (item.protectedAuthority) {
            parts.push("—— 区域默认/系统内置权限组，仅解绑设备，权限组保留为空壳，请自行检查配置");
          }
          return elm("li", null, parts.join(" "));
        });
        summaryChildren.push(elm("ul", { attrs: { class: "smallInfo" } }, listItems));
      }
      this.$msgbox({
        message: elm("div", { attrs: { class: "smallp" } }, summaryChildren),
        showCancelButton: true,
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj(row.id);
        })
        .then(dataResponse => {
          _this.getList(_this.page, _this.searchForm);
          _this.$notify({
            title: "删除成功",
            message: "删除成功",
            type: "success",
            duration: 2000
          });
        })
        .catch(error => { console.error(error) });
    },
```

- [ ] **Step 3: 手工验证（浏览器）**

启动前端开发服务器（`pnpm dev`，具体命令按仓库现有约定），进入"设备管理 → 门禁管理"页面：
1. 点击一台未绑定任何权限组的设备的删除按钮，确认弹窗显示"该设备当前未绑定任何权限组"。
2. 点击一台绑定了权限组的设备的删除按钮，确认弹窗按权限组名称列出受影响人数/车辆数，且级联删除/受保护的组有对应提示文案。
3. 点"取消"，确认设备没有被删除；点"确定删除"，确认调用了删除接口且列表刷新。

- [ ] **Step 4: 提交**

```bash
git add smart-ui/src/views/platform/device/xc_guard/index.vue
git commit -m "feat(smart-ui): preview affected authorities before deleting a device"
```

---

## Task 10: 前端 —— "所属权限组"随时查看入口

**Files:**
- Modify: `smart-ui/src/views/platform/device/xc_guard/index.vue`

**Interfaces:**
- Consumes：Task 8 的 `xcGuardApi.getDecommissionPlan(deviceId)`。

- [ ] **Step 1: 模板加一个按钮**

在 `index.vue` 模板里，`permittedList(item)` 按钮后面加一个新按钮（第 90 行附近）：

```html
                            <el-button type="primary" @click="viewAuthorities(item)" class="perm-btn" plain round >所属权限组</el-button>
```

- [ ] **Step 2: methods 里加对应方法**

在 `permittedList` 方法后面加：

```js
    /**
     * 查看设备当前绑定的权限组
     */
    viewAuthorities(item) {
      var _this = this;
      xcGuardApi.getDecommissionPlan(item.id).then(response => {
        const plan = response.data.data || { affectedAuthorities: [] };
        const affected = plan.affectedAuthorities || [];
        const elm = _this.$createElement;
        let content;
        if (affected.length === 0) {
          content = elm("p", null, "该设备当前未绑定任何权限组。");
        } else {
          const listItems = affected.map(auth => elm("li", null,
            `${auth.authorityName}（${auth.staffCount} 名员工 / ${auth.vehicleCount} 辆车）`));
          content = elm("ul", { attrs: { class: "smallInfo" } }, listItems);
        }
        _this.$msgbox({
          title: "所属权限组",
          message: content,
          showCancelButton: false,
          confirmButtonText: "关闭",
          customClass: "small_dialog",
          center: true
        }).catch(() => {});
      }).catch(error => {
        console.error(error);
        _this.$message.error("查询失败，请稍后重试");
      });
    },
```

- [ ] **Step 3: 手工验证（浏览器）**

进入"设备管理 → 门禁管理"页面，点一台设备的"所属权限组"按钮，确认弹窗展示当前绑定的权限组名称、人数、车辆数；对未绑定权限组的设备点击，确认展示"该设备当前未绑定任何权限组"。

- [ ] **Step 4: 提交**

```bash
git add smart-ui/src/views/platform/device/xc_guard/index.vue
git commit -m "feat(smart-ui): add on-demand authority lookup for a device"
```

---

## 收尾：完整回归

- [ ] Run: `mvn -pl smart-module/smart-platform/smart-platform-core,smart-module/smart-platform/smart-platform-biz -am test`
Expected: BUILD SUCCESS，所有新增和既有测试全部通过。
- [ ] Run: `cd smart-ui && pnpm lint && pnpm test`
Expected: 无新增 lint/测试失败。
