# 通关权限「性质切换」+ 设备绑定体验优化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让「通关权限」可以在保密区域/公共区域之间安全切换，同时把设备绑定 UI 从“全展开长树”改造成“可搜索树 + 已选设备平铺列表”。

**Architecture:** 后端新增一个独立的 `switchAreaType` 接口，只改 `smt_device_authority.area_type` 一个字段，写库前做跨权限组冲突校验，冲突就整体拒绝并把冲突设备清单返回给前端；前端新增一个只读确认弹窗触发这个接口，和原有“编辑”页物理隔离。设备绑定则抽出一个新的 `DeviceTreePicker` 组件（搜索 + 树 + 平铺已选列表），替换 `add.vue`/`edit.vue` 里原来裸的 `el-tree`。

**Tech Stack:** 后端 Java 8 / Spring Boot 2.1 / MyBatis-Plus / JUnit4 + Mockito；前端 Vue 2.7 + Element UI 2.15 + Avue，测试用 Vitest 4 + `@vue/test-utils@1.3.6`（Vue2 兼容版）。

**依据设计文档：** [2026-07-01-device-authority-area-type-switch-design.md](../specs/2026-07-01-device-authority-area-type-switch-design.md)

## Global Constraints

- 后端所有新建/修改的 Java 文件注释一律用中文（AGENTS.md「开发约定」）。
- 前端所有新建/修改的 Vue/JS 文件注释一律用中文。
- 提交信息用英文 Conventional Commits：`<type>(scope): <summary>`，不加署名（用户 CLAUDE.md 规则）。
- 不修改 `smt_staff_device_auth.auth_type`、`SmtSecurityZone` 模块——两者与本次改动的 `smt_device_authority.area_type` 是独立体系（设计文档 §3.2）。
- 切换动作走现有 `@SysLog` 记录一般操作日志，不引入审批流（设计文档已确认决策）。
- **测试覆盖现实调整**：`smart-platform-biz` 现有的 `BaseTest`（`smart-platform-biz/src/test/java/com/tce/smart/platform/biz/BaseTest.java`）把 `@SpringBootTest` 注解整行注释掉了，说明这个模块里没有真正能跑起来的 Spring 上下文 / 真实数据库集成测试基座。本计划不新造一套集成测试基础设施，新 SQL 的正确性通过 Task 3 的手工验证步骤确认，其余逻辑分支用现有的 Mockito 单元测试风格覆盖（和 `SmtDeviceAuthorityServiceImplTest.java` 已有测试一致）。
- 前端在本计划开工前 `smart-ui/node_modules` 是空的，需要先 `pnpm install`（已在规划阶段验证过 `vitest` + `@vue/test-utils@1.3.6` + `element-ui` 在这个仓库里可以正常配合渲染 `el-tree`/`el-input` 并触发勾选/过滤，见 Task 4）。

---

### Task 1: 冲突设备查询 —— DTO/VO + Mapper

**Files:**
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/AreaTypeConflictDeviceVO.java`
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/AreaTypeSwitchRespDTO.java`
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/req/AreaTypeSwitchReqDTO.java`
- Modify: `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/mapper/SmtDeviceAuthorityMapper.java`
- Modify: `smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtDeviceAuthorityMapper.xml`

**Interfaces:**
- Produces: `AreaTypeConflictDeviceVO { deviceId: String, deviceName: String, conflictAuthorityId: Integer, conflictAuthorityName: String }`
- Produces: `AreaTypeSwitchRespDTO { success: boolean, conflicts: List<AreaTypeConflictDeviceVO> }`
- Produces: `AreaTypeSwitchReqDTO { id: Integer, areaType: Integer }`
- Produces: `SmtDeviceAuthorityMapper.findAreaTypeConflicts(Integer authorityId, Integer targetAreaType): List<AreaTypeConflictDeviceVO>`

`smart-platform-core` 已经在 pom 里依赖 `smart-platform-api`（`SmtDeviceAuthorityMapper.java` 现有方法已经直接引用 `api.dto.req.AuthDetailQueryDTO`/`api.dto.resp.AuthDetailRespDTO` 做 resultType），所以新 VO 放在 `api` 模块、被 `core` 模块的 mapper 引用是和现有代码一致的方向，不会产生新的模块循环依赖。

- [ ] **Step 1: 新建三个数据类**

`AreaTypeConflictDeviceVO.java`:
```java
package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 通关权限性质切换时的冲突设备明细：
 * 该设备当前被“性质不同”的另一个权限组占用，无法完成切换。
 *
 * @author claude
 * @date 2026-07-01
 */
@Data
public class AreaTypeConflictDeviceVO extends BaseDTO {

	@ApiModelProperty(value = "设备ID")
	private String deviceId;

	@ApiModelProperty(value = "设备名称")
	private String deviceName;

	@ApiModelProperty(value = "占用该设备的其他权限组ID")
	private Integer conflictAuthorityId;

	@ApiModelProperty(value = "占用该设备的其他权限组名称")
	private String conflictAuthorityName;
}
```

`AreaTypeSwitchRespDTO.java`:
```java
package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 通关权限性质切换的返回结果。
 * success=false 时表示存在跨权限组冲突设备，本次切换未写库，
 * conflicts 里是需要管理员先去对应权限组手动移除的设备清单。
 *
 * @author claude
 * @date 2026-07-01
 */
@Data
public class AreaTypeSwitchRespDTO extends BaseDTO {

	@ApiModelProperty(value = "是否切换成功")
	private boolean success;

	@ApiModelProperty(value = "冲突设备清单，仅 success=false 时有值")
	private List<AreaTypeConflictDeviceVO> conflicts;
}
```

`AreaTypeSwitchReqDTO.java`:
```java
package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 通关权限性质切换请求。
 *
 * @author claude
 * @date 2026-07-01
 */
@Data
public class AreaTypeSwitchReqDTO extends BaseDTO {

	@NotNull(message = "权限组ID不能为空")
	private Integer id;

	/**
	 * 目标权限性质 0-公共区域 1-保密区域
	 */
	@NotNull(message = "目标权限性质不能为空")
	private Integer areaType;
}
```

- [ ] **Step 2: Mapper 接口新增方法**

在 `SmtDeviceAuthorityMapper.java` 里，`countByAreaType` 方法下面新增：

```java
	/**
	 * 查询把某个权限组切换到目标性质后，会产生冲突的设备明细。
	 * 冲突定义：该设备同时被“性质不同”的其他权限组（authority_id != authorityId）引用。
	 *
	 * @param authorityId 待切换的权限组ID
	 * @param targetAreaType 目标权限性质 0-公共区域 1-保密区域
	 * @return 冲突设备明细（可能为空）
	 */
	List<AreaTypeConflictDeviceVO> findAreaTypeConflicts(@Param("authorityId") Integer authorityId,
														  @Param("targetAreaType") Integer targetAreaType);
```

同时在文件顶部 import 区新增：
```java
import com.tce.smart.platform.api.dto.resp.AreaTypeConflictDeviceVO;
```

- [ ] **Step 3: Mapper XML 新增 SQL**

在 `SmtDeviceAuthorityMapper.xml` 里，`countByAreaType` 的 `<select>` 后面新增：

```xml
	<select id="findAreaTypeConflicts" resultType="com.tce.smart.platform.api.dto.resp.AreaTypeConflictDeviceVO">
		SELECT DISTINCT
			SDAR.DEVICE_ID AS deviceId,
			SD.DEVICE_NAME AS deviceName,
			SDA2.ID AS conflictAuthorityId,
			SDA2.AUTHORITY_NAME AS conflictAuthorityName
		FROM SMT_DEVICE_AUTHORITY_RELATION SDAR
				 LEFT JOIN SMT_DEVICE SD ON SD.ID = SDAR.DEVICE_ID
				 JOIN SMT_DEVICE_AUTHORITY_RELATION SDAR2
					  ON SDAR2.DEVICE_ID = SDAR.DEVICE_ID AND SDAR2.AUTHORITY_ID != SDAR.AUTHORITY_ID
				 JOIN SMT_DEVICE_AUTHORITY SDA2 ON SDA2.ID = SDAR2.AUTHORITY_ID
		WHERE SDAR.AUTHORITY_ID = #{authorityId}
		  AND SDA2.AREA_TYPE != #{targetAreaType}
	</select>
```

- [ ] **Step 4: 编译确认**

Run（Maven reactor 的根是 `smart-module/pom.xml`，所以要在 `smart-module` 目录下执行 `-pl`，不是仓库根目录）：
`cd smart-module && mvn -pl smart-platform/smart-platform-api,smart-platform/smart-platform-core -am compile -DskipTests`
Expected: `BUILD SUCCESS`（这一步只验证新类型和 mapper 接口能编译通过，规划阶段已经实际跑过一遍确认成功；SQL 本身的正确性留到 Task 3 手工验证）

- [ ] **Step 5: Commit**

```bash
git add smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/AreaTypeConflictDeviceVO.java \
        smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/resp/AreaTypeSwitchRespDTO.java \
        smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/dto/req/AreaTypeSwitchReqDTO.java \
        smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/mapper/SmtDeviceAuthorityMapper.java \
        smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtDeviceAuthorityMapper.xml
git commit -m "feat(smart-platform): add area-type conflict lookup for device authority"
```

---

### Task 2: Service 层 `switchAreaType`（TDD）

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/SmtDeviceAuthorityService.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityServiceImpl.java`
- Test: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityServiceImplTest.java`

**Interfaces:**
- Consumes: `SmtDeviceAuthorityMapper.findAreaTypeConflicts(Integer, Integer): List<AreaTypeConflictDeviceVO>`（Task 1）
- Produces: `SmtDeviceAuthorityService.switchAreaType(AreaTypeSwitchReqDTO reqDTO, List<Integer> parkIds): Result<AreaTypeSwitchRespDTO>`（Task 3 的 Controller 直接调用）

- [ ] **Step 1: 写失败的测试（5 个场景）**

在 `SmtDeviceAuthorityServiceImplTest.java` 顶部 import 区新增：
```java
import com.tce.smart.platform.api.dto.req.AreaTypeSwitchReqDTO;
import com.tce.smart.platform.api.dto.resp.AreaTypeConflictDeviceVO;
import com.tce.smart.platform.api.dto.resp.AreaTypeSwitchRespDTO;
import com.tce.smart.common.core.model.Result;
```

在类里新增 5 个测试方法（放在现有 `deviceAuthRelationDelBatchesVehicleMetadataQueriesRegardlessOfSelectedCount` 测试后面，helper 方法前面）：

```java
	@Test
	public void switchAreaTypeUpdatesWhenNoConflictExists() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setParkId(1);
		authority.setAreaType(1);
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);
		Mockito.when(authorityMapper.findAreaTypeConflicts(100, 0)).thenReturn(Collections.emptyList());
		Mockito.when(authorityMapper.updateById(Mockito.any(SmtDeviceAuthority.class))).thenReturn(1);

		AreaTypeSwitchReqDTO reqDTO = new AreaTypeSwitchReqDTO();
		reqDTO.setId(100);
		reqDTO.setAreaType(0);

		Result<AreaTypeSwitchRespDTO> result = service.switchAreaType(reqDTO, Collections.singletonList(1));

		Assert.assertTrue(result.getData().isSuccess());
		Assert.assertTrue(result.getData().getConflicts().isEmpty());
		ArgumentCaptor<SmtDeviceAuthority> updateCaptor = ArgumentCaptor.forClass(SmtDeviceAuthority.class);
		Mockito.verify(authorityMapper).updateById(updateCaptor.capture());
		Assert.assertEquals(Integer.valueOf(100), updateCaptor.getValue().getId());
		Assert.assertEquals(Integer.valueOf(0), updateCaptor.getValue().getAreaType());
		// 核心回归点：不写库无关的下游数据，切换只应该动 area_type 这一个字段
		// 项目里 Mockito 版本较旧（Spring Boot 2.1 默认管理的 2.x），没有 verifyNoInteractions，
		// 用 verifyZeroInteractions 代替
		Mockito.verifyZeroInteractions(relationService, staffAuthService);
	}

	@Test
	public void switchAreaTypeBlocksAndListsConflictsWithoutWriting() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setParkId(1);
		authority.setAreaType(1);
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);

		AreaTypeConflictDeviceVO conflict = new AreaTypeConflictDeviceVO();
		conflict.setDeviceId("device-A");
		conflict.setDeviceName("1F-2区-超黑面检机-03");
		conflict.setConflictAuthorityId(200);
		conflict.setConflictAuthorityName("门禁_AB栋连廊");
		Mockito.when(authorityMapper.findAreaTypeConflicts(100, 0)).thenReturn(Collections.singletonList(conflict));

		AreaTypeSwitchReqDTO reqDTO = new AreaTypeSwitchReqDTO();
		reqDTO.setId(100);
		reqDTO.setAreaType(0);

		Result<AreaTypeSwitchRespDTO> result = service.switchAreaType(reqDTO, Collections.singletonList(1));

		Assert.assertFalse(result.getData().isSuccess());
		Assert.assertEquals(1, result.getData().getConflicts().size());
		Assert.assertEquals("device-A", result.getData().getConflicts().get(0).getDeviceId());
		// 核心回归点：存在冲突时绝不写库
		Mockito.verify(authorityMapper, Mockito.never()).updateById(Mockito.any());
	}

	@Test
	public void switchAreaTypeIsIdempotentWhenTargetEqualsCurrent() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setParkId(1);
		authority.setAreaType(1);
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);

		AreaTypeSwitchReqDTO reqDTO = new AreaTypeSwitchReqDTO();
		reqDTO.setId(100);
		reqDTO.setAreaType(1);

		Result<AreaTypeSwitchRespDTO> result = service.switchAreaType(reqDTO, Collections.singletonList(1));

		Assert.assertTrue(result.getData().isSuccess());
		// 核心回归点：目标性质和当前一致时幂等短路，既不查冲突也不写库
		Mockito.verify(authorityMapper, Mockito.never()).findAreaTypeConflicts(Mockito.anyInt(), Mockito.anyInt());
		Mockito.verify(authorityMapper, Mockito.never()).updateById(Mockito.any());
	}

	@Test
	public void switchAreaTypeSucceedsWhenAuthorityHasNoDevices() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setParkId(1);
		authority.setAreaType(1);
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);
		Mockito.when(authorityMapper.findAreaTypeConflicts(100, 0)).thenReturn(Collections.emptyList());
		Mockito.when(authorityMapper.updateById(Mockito.any(SmtDeviceAuthority.class))).thenReturn(1);

		AreaTypeSwitchReqDTO reqDTO = new AreaTypeSwitchReqDTO();
		reqDTO.setId(100);
		reqDTO.setAreaType(0);

		Result<AreaTypeSwitchRespDTO> result = service.switchAreaType(reqDTO, Collections.singletonList(1));

		Assert.assertTrue(result.getData().isSuccess());
		Mockito.verify(authorityMapper).updateById(Mockito.any());
	}

	@Test
	public void switchAreaTypeFailsWhenAuthorityOutsideCallerParkScope() throws Exception {
		SmtDeviceAuthorityMapper authorityMapper = Mockito.mock(SmtDeviceAuthorityMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityServiceImpl service = newService(authorityMapper, relationService, staffAuthService);
		setField(service, "baseMapper", authorityMapper);

		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(100);
		authority.setParkId(999);
		authority.setAreaType(1);
		Mockito.when(authorityMapper.selectById(100)).thenReturn(authority);

		AreaTypeSwitchReqDTO reqDTO = new AreaTypeSwitchReqDTO();
		reqDTO.setId(100);
		reqDTO.setAreaType(0);

		Result<AreaTypeSwitchRespDTO> result = service.switchAreaType(reqDTO, Collections.singletonList(1));

		Assert.assertFalse(result.getData().isSuccess());
		Mockito.verify(authorityMapper, Mockito.never()).updateById(Mockito.any());
	}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd smart-module && mvn -pl smart-platform/smart-platform-biz -am test -Dtest=SmtDeviceAuthorityServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false`
Expected: 编译失败（`switchAreaType` 方法还不存在）——这是预期的“红”状态，先确认失败原因确实是“方法未定义”而不是别的错误。

- [ ] **Step 3: Service 接口新增方法签名**

在 `SmtDeviceAuthorityService.java` 里，`deviceAuthRelationAdd` 方法后面新增：
```java
	/**
	 * 变更通关权限性质（公共区域/保密区域）。
	 * 若组内设备存在跨权限组的性质冲突，直接拒绝并返回冲突设备清单，不写库。
	 *
	 * @param reqDTO 目标权限组ID + 目标性质
	 * @param parkIds 当前用户可操作的园区范围
	 * @return 切换结果
	 */
	Result<AreaTypeSwitchRespDTO> switchAreaType(AreaTypeSwitchReqDTO reqDTO, List<Integer> parkIds);
```
文件顶部新增 import：
```java
import com.tce.smart.platform.api.dto.req.AreaTypeSwitchReqDTO;
import com.tce.smart.platform.api.dto.resp.AreaTypeSwitchRespDTO;
```

- [ ] **Step 4: 实现 `switchAreaType`**

在 `SmtDeviceAuthorityServiceImpl.java` 里，`checkIsUsed` 方法后面新增：

```java
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<AreaTypeSwitchRespDTO> switchAreaType(AreaTypeSwitchReqDTO reqDTO, List<Integer> parkIds) {
		SmtDeviceAuthority authority = this.getById(reqDTO.getId());
		AreaTypeSwitchRespDTO resp = new AreaTypeSwitchRespDTO();
		if (authority == null || !parkIds.contains(authority.getParkId())) {
			// 注意：不能写成 new Result<>(false, "...")——方法返回类型是 Result<AreaTypeSwitchRespDTO>，
			// 泛型参数已经被返回类型固定了，构造函数第一个参数必须是 AreaTypeSwitchRespDTO 而不是 Boolean，
			// 否则编译期泛型推断会直接报错（规划阶段已经用真实编译踩过这个坑）
			resp.setSuccess(false);
			resp.setConflicts(Collections.emptyList());
			return new Result<>(resp, "权限策略不存在或不在当前用户可操作的园区范围内");
		}

		// 目标性质和当前一致：幂等短路，不查冲突也不写库
		if (Objects.equals(authority.getAreaType(), reqDTO.getAreaType())) {
			resp.setSuccess(true);
			resp.setConflicts(Collections.emptyList());
			return new Result<>(resp);
		}

		List<AreaTypeConflictDeviceVO> conflicts = smtDeviceAuthorityMapper.findAreaTypeConflicts(
				reqDTO.getId(), reqDTO.getAreaType());
		if (CollUtil.isNotEmpty(conflicts)) {
			// 存在跨权限组冲突：直接拒绝，不动 area_type，也不动任何设备/员工/车辆关联数据
			resp.setSuccess(false);
			resp.setConflicts(conflicts);
			return new Result<>(resp);
		}

		// 无冲突：只更新 area_type 这一个字段，不触碰设备关联关系
		SmtDeviceAuthority update = new SmtDeviceAuthority();
		update.setId(reqDTO.getId());
		update.setAreaType(reqDTO.getAreaType());
		this.updateById(update);

		log.info("通关权限性质已切换: 权限ID={}, {} -> {}", reqDTO.getId(), authority.getAreaType(), reqDTO.getAreaType());

		resp.setSuccess(true);
		resp.setConflicts(Collections.emptyList());
		return new Result<>(resp);
	}
```

文件顶部新增 import（`Objects`/`Collections` 大概率已经在 `java.util.*` 通配符导入里，确认一下现有 import 是否已覆盖，没有就补上）：
```java
import com.tce.smart.platform.api.dto.req.AreaTypeSwitchReqDTO;
import com.tce.smart.platform.api.dto.resp.AreaTypeConflictDeviceVO;
import com.tce.smart.platform.api.dto.resp.AreaTypeSwitchRespDTO;
```

- [ ] **Step 5: 运行测试确认通过**

Run（从 `smart-module` 目录执行，这是 Maven reactor 的根；测试范围只限定本类，避免 `-am` 把没有这个测试的模块也拖进来报错）：
`cd smart-module && mvn -pl smart-platform/smart-platform-biz -am test -Dtest=SmtDeviceAuthorityServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false`
Expected: `Tests run: 9, Failures: 0, Errors: 0`（原有 4 个 + 新增 5 个；已在规划阶段实际跑过一遍确认全部通过）

- [ ] **Step 6: Commit**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/SmtDeviceAuthorityService.java \
        smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityServiceImpl.java \
        smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtDeviceAuthorityServiceImplTest.java
git commit -m "feat(smart-platform): switch device authority area type without touching device bindings"
```

---

### Task 3: Controller 接口 + 手工 SQL 验证

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtDeviceAuthorityController.java`

**Interfaces:**
- Consumes: `SmtDeviceAuthorityService.switchAreaType(AreaTypeSwitchReqDTO, List<Integer>): Result<AreaTypeSwitchRespDTO>`（Task 2）
- Produces: `POST /device/authority/areaType/switch`，网关路径 `POST /platform/device/authority/areaType/switch`

- [ ] **Step 1: 新增 Controller 方法**

在 `SmtDeviceAuthorityController.java` 里，`deviceAuthRelationAdd` 方法后面新增：

```java
	/**
	 * 变更通关权限性质（公共区域/保密区域）
	 * @param reqDTO 目标权限组ID + 目标性质
	 * @return success=false 时表示存在冲突设备，未写库，data.conflicts 里是冲突明细
	 */
	@SysLog("变更通关权限性质")
	@PostMapping("/areaType/switch")
	public Result<AreaTypeSwitchRespDTO> switchAreaType(@RequestBody AreaTypeSwitchReqDTO reqDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return smtDeviceAuthorityService.switchAreaType(reqDTO, parkIds);
	}
```

文件顶部新增 import：
```java
import com.tce.smart.platform.api.dto.req.AreaTypeSwitchReqDTO;
import com.tce.smart.platform.api.dto.resp.AreaTypeSwitchRespDTO;
```

- [ ] **Step 2: 编译确认**

Run: `cd smart-module && mvn -pl smart-platform/smart-platform-biz -am compile -DskipTests`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: 手工验证 SQL（这是 Task 1 新 SQL 的真实正确性验证，前面只验证了编译）**

对照开发库手工执行（把 `?` 换成真实的两个权限组ID，构造成“同一台设备被两个不同 area_type 的权限组各绑定一条 relation”的场景）：

```sql
SELECT DISTINCT
    SDAR.DEVICE_ID AS deviceId,
    SD.DEVICE_NAME AS deviceName,
    SDA2.ID AS conflictAuthorityId,
    SDA2.AUTHORITY_NAME AS conflictAuthorityName
FROM SMT_DEVICE_AUTHORITY_RELATION SDAR
         LEFT JOIN SMT_DEVICE SD ON SD.ID = SDAR.DEVICE_ID
         JOIN SMT_DEVICE_AUTHORITY_RELATION SDAR2
              ON SDAR2.DEVICE_ID = SDAR.DEVICE_ID AND SDAR2.AUTHORITY_ID != SDAR.AUTHORITY_ID
         JOIN SMT_DEVICE_AUTHORITY SDA2 ON SDA2.ID = SDAR2.AUTHORITY_ID
WHERE SDAR.AUTHORITY_ID = ?
  AND SDA2.AREA_TYPE != ?
```
Expected: 命中场景下返回冲突设备行；把两个权限组的 `AREA_TYPE` 改成一致后重跑，应该返回 0 行。

- [ ] **Step 4: 用 curl 冒烟一遍完整接口（需要本地/开发网关 + 有效 token）**

```bash
curl -X POST 'http://<gateway-host>/platform/device/authority/areaType/switch' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token>' \
  -d '{"id": <existing-authority-id>, "areaType": 0}'
```
Expected: `{"code":0,"msg":"success","data":{"success":true,"conflicts":[]}}`（无冲突场景）或 `{"success":false,"conflicts":[...]}`（有冲突场景）。

- [ ] **Step 5: Commit**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtDeviceAuthorityController.java
git commit -m "feat(smart-platform): expose area-type switch endpoint on device authority controller"
```

---

### Task 4: 前端依赖安装 + API 客户端函数

**Files:**
- Modify: `smart-ui/src/api/platform/area/limit.js`

**Interfaces:**
- Produces: `switchAreaType({ id, areaType }): Promise<AxiosResponse<{ data: { code, msg, data: { success, conflicts } } }>>`

- [ ] **Step 1: 安装依赖（仓库里 node_modules 是空的）**

Run: `cd smart-ui && pnpm install`
Expected: 安装完成，无报错（已在规划阶段验证过一次，能装出 `vitest`/`@vue/test-utils`/`element-ui` 等全部依赖）。

- [ ] **Step 2: 新增 API 函数**

在 `smart-ui/src/api/platform/area/limit.js` 末尾新增：
```js
// 变更通关权限性质（公共区域/保密区域），只改 area_type 这一个字段
export function switchAreaType (data) {
  return request({
    url: '/platform/device/authority/areaType/switch',
    method: 'post',
    data: data
  })
}
```

- [ ] **Step 3: Commit**

```bash
git add smart-ui/src/api/platform/area/limit.js
git commit -m "feat(smart-ui): add switchAreaType api client for device authority"
```

---

### Task 5: `DeviceTreePicker.vue` 组件（TDD）

**Files:**
- Create: `smart-ui/src/views/platform/area/limit/components/DeviceTreePicker.vue`
- Test: `smart-ui/src/views/platform/area/limit/components/DeviceTreePicker.test.js`

**Interfaces:**
- Produces: `<DeviceTreePicker :tree-data="Array<{id,label,children}>" v-model="Array<string>" />`（`value`/`input` v-model 语义，`value` 是选中的叶子设备 id 数组）

- [ ] **Step 1: 写失败的组件测试**

```js
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Vue from 'vue'
import ElementUI from 'element-ui'
import DeviceTreePicker from './DeviceTreePicker.vue'

Vue.use(ElementUI)

function buildTreeData () {
  return [
    {
      id: 'building-a',
      label: 'A栋',
      children: [
        {
          id: 'floor-a1',
          label: '1F',
          children: [
            { id: 'device-a1-01', label: '1F-2区-超黑面检机-01' },
            { id: 'device-a1-03', label: '1F-2区-超黑面检机-03' }
          ]
        }
      ]
    },
    {
      id: 'building-b',
      label: 'B栋',
      children: [
        {
          id: 'floor-b3',
          label: '3F',
          children: [
            { id: 'device-b3-01', label: '3F-成品仓闸机-01' }
          ]
        }
      ]
    }
  ]
}

describe('DeviceTreePicker', () => {
  it('搜索到设备后勾选，会通过 v-model 输出选中的设备id数组', async () => {
    // 树默认不展开任何分支（没有 default-expand-all 了），
    // 所以先搜索让 el-tree 自动展开命中项的祖先节点，再去勾选，
    // 这也是真实用户在“搜索 + 勾选”这条路径上的操作方式
    const wrapper = mount(DeviceTreePicker, {
      propsData: { treeData: buildTreeData(), value: [] }
    })

    await wrapper.find('.device-tree-picker__search input').setValue('超黑面检机-01')
    await wrapper.vm.$nextTick()

    const checkboxes = wrapper.findAll('.el-checkbox__original')
    const targetIndex = wrapper.findAll('.el-tree-node__label')
      .wrappers.findIndex(label => label.text() === '1F-2区-超黑面检机-01')
    await checkboxes.at(targetIndex).setChecked()

    expect(wrapper.emitted('input')[0][0]).toEqual(['device-a1-01'])
  })

  it('已选设备面板平铺展示，不按楼栋分组，点击 x 会移除', async () => {
    const wrapper = mount(DeviceTreePicker, {
      propsData: {
        treeData: buildTreeData(),
        value: ['device-a1-01', 'device-b3-01']
      }
    })
    await wrapper.vm.$nextTick()

    // el-icon-close 是图标字体，通过 CSS 伪元素渲染字形，.text() 拿不到它，
    // 所以已选面板每一行的文本内容就是纯设备名
    const selectedLabels = wrapper.findAll('.device-tree-picker__selected-list li').wrappers.map(li => li.text())
    expect(selectedLabels).toEqual(['1F-2区-超黑面检机-01', '3F-成品仓闸机-01'])

    await wrapper.find('.device-tree-picker__selected-list li .el-icon-close').trigger('click')
    expect(wrapper.emitted('input')[0][0]).toEqual(['device-b3-01'])
  })

  it('搜索框过滤后只显示命中的叶子节点', async () => {
    const wrapper = mount(DeviceTreePicker, {
      propsData: { treeData: buildTreeData(), value: [] }
    })

    await wrapper.find('.device-tree-picker__search input').setValue('成品仓')
    await wrapper.vm.$nextTick()

    // 叶子节点(真正的设备)靠 .el-tree-node__expand-icon 上的 is-leaf 类判断——
    // 分支节点(楼栋/楼层)不管是否命中搜索，都会带着 .el-tree-node__children 容器，
    // 用它来判断“是不是叶子”并不可靠，已经在规划阶段用真实 DOM 验证过这一点
    const visibleLeafLabels = wrapper.findAll('.el-tree-node')
      .wrappers.filter(node => {
        const isVisible = !node.classes('is-hidden')
        const isLeaf = node.find('.el-tree-node__expand-icon').classes('is-leaf')
        return isVisible && isLeaf
      })
      .map(node => node.find('.el-tree-node__label').text())
    expect(visibleLeafLabels).toEqual(['3F-成品仓闸机-01'])
  })

  it('已选设备包含预先勾选的设备时，会自动展开到对应楼栋楼层', async () => {
    const wrapper = mount(DeviceTreePicker, {
      propsData: { treeData: buildTreeData(), value: ['device-b3-01'] }
    })
    await wrapper.vm.$nextTick()

    const buildingBNode = wrapper.findAll('.el-tree-node').wrappers
      .find(node => node.find('.el-tree-node__label').text() === 'B栋')
    expect(buildingBNode.classes()).toContain('is-expanded')
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd smart-ui && pnpm exec vitest run src/views/platform/area/limit/components/DeviceTreePicker.test.js`
Expected: FAIL（`DeviceTreePicker.vue` 还不存在）

- [ ] **Step 3: 实现组件**

```vue
<template>
  <div class="device-tree-picker">
    <div class="device-tree-picker__col device-tree-picker__col--tree">
      <el-input
        v-model="filterText"
        placeholder="输入设备名称、楼栋或楼层搜索"
        size="small"
        clearable
        class="device-tree-picker__search"
      >
        <i slot="prefix" class="el-icon-search"></i>
      </el-input>
      <el-tree
        :key="treeRenderKey"
        ref="tree"
        class="device-tree-picker__tree"
        :data="treeData"
        node-key="id"
        show-checkbox
        check-strictly
        :props="elProps"
        :filter-node-method="filterNode"
        :default-expanded-keys="expandedKeys"
        :default-checked-keys="value"
        @check="handleCheck"
      ></el-tree>
    </div>
    <div class="device-tree-picker__col device-tree-picker__col--selected">
      <div class="device-tree-picker__selected-head">
        <span>已选设备（{{ selectedList.length }}）</span>
        <el-button type="text" size="mini" @click="clearAll">清空</el-button>
      </div>
      <ul class="device-tree-picker__selected-list">
        <li v-for="item in selectedList" :key="item.id">
          <span>{{ item.label }}</span>
          <i class="el-icon-close" @click="removeSelected(item.id)"></i>
        </li>
        <li v-if="selectedList.length === 0" class="device-tree-picker__empty">
          还没有选中设备，从左侧勾选或搜索添加
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
// 通关权限设备绑定选择器：左侧可搜索设备树，右侧平铺展示已选设备。
// 替换原来 add.vue/edit.vue 里 default-expand-all 的裸 el-tree，
// 解决“页面被撑得很长”和“找不到已选设备”两个体验问题。
export default {
  name: 'DeviceTreePicker',
  props: {
    treeData: {
      type: Array,
      default: () => []
    },
    value: {
      type: Array,
      default: () => []
    }
  },
  data () {
    return {
      filterText: '',
      leafNameById: {},
      expandedKeys: [],
      treeRenderKey: 0,
      elProps: {
        children: 'children',
        label: 'label'
      }
    }
  },
  watch: {
    treeData: {
      immediate: true,
      handler () {
        this.leafNameById = this.buildLeafNameMap(this.treeData)
        const checkedIdSet = new Set(this.value || [])
        const expandedKeys = []
        this.collectExpandedKeys(this.treeData, checkedIdSet, expandedKeys)
        this.expandedKeys = expandedKeys
        // el-tree 的 default-expanded-keys/default-checked-keys 只在节点创建时生效一次，
        // 树数据变化后必须强制重建组件实例才能让新的默认值重新应用。
        this.treeRenderKey += 1
      }
    },
    filterText (val) {
      this.$nextTick(() => {
        if (this.$refs.tree) {
          this.$refs.tree.filter(val)
        }
      })
    }
  },
  computed: {
    selectedList () {
      return (this.value || []).map(id => ({
        id: id,
        label: this.leafNameById[id] || id
      }))
    }
  },
  methods: {
    filterNode (value, data) {
      if (!value) {
        return true
      }
      return data.label.indexOf(value) !== -1
    },
    // 收集树里所有叶子节点（真正的设备，没有 children 的节点）的 id -> label 映射
    buildLeafNameMap (nodes, out) {
      out = out || {}
      ;(nodes || []).forEach(node => {
        const isLeaf = !node.children || node.children.length === 0
        if (isLeaf) {
          out[node.id] = node.label
        } else {
          this.buildLeafNameMap(node.children, out)
        }
      })
      return out
    },
    // 递归收集“包含已选设备”的非叶子节点 id，用于 default-expanded-keys；
    // 返回值表示这一层节点里是否存在命中的叶子设备（让上一层也跟着展开）。
    collectExpandedKeys (nodes, checkedIdSet, expandedKeys) {
      let anyChecked = false
      ;(nodes || []).forEach(node => {
        const isLeaf = !node.children || node.children.length === 0
        if (isLeaf) {
          if (checkedIdSet.has(node.id)) {
            anyChecked = true
          }
          return
        }
        const childHasChecked = this.collectExpandedKeys(node.children, checkedIdSet, expandedKeys)
        if (childHasChecked) {
          expandedKeys.push(node.id)
          anyChecked = true
        }
      })
      return anyChecked
    },
    handleCheck (data, checkedInfo) {
      this.$emit('input', checkedInfo.checkedKeys)
    },
    removeSelected (id) {
      const nextIds = (this.value || []).filter(existingId => existingId !== id)
      this.$emit('input', nextIds)
      this.$nextTick(() => {
        if (this.$refs.tree) {
          this.$refs.tree.setCheckedKeys(nextIds)
        }
      })
    },
    clearAll () {
      this.$emit('input', [])
      this.$nextTick(() => {
        if (this.$refs.tree) {
          this.$refs.tree.setCheckedKeys([])
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.device-tree-picker {
  display: flex;
  gap: 16px;
  &__col {
    flex: 1;
    min-width: 0;
    border: 1px solid #e5e7eb;
    border-radius: 4px;
  }
  &__search {
    padding: 8px;
  }
  &__tree {
    max-height: 320px;
    overflow-y: auto;
    padding: 0 8px 8px;
  }
  &__selected-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    border-bottom: 1px solid #e5e7eb;
    font-size: 13px;
    color: #606266;
  }
  &__selected-list {
    max-height: 320px;
    overflow-y: auto;
    margin: 0;
    padding: 6px 8px;
    list-style: none;
    li {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 5px 8px;
      font-size: 13px;
      .el-icon-close {
        cursor: pointer;
        color: #909399;
      }
    }
  }
  &__empty {
    color: #909399;
    font-size: 13px;
  }
}
</style>
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd smart-ui && pnpm exec vitest run src/views/platform/area/limit/components/DeviceTreePicker.test.js`
Expected: `4 passed`

- [ ] **Step 5: Commit**

```bash
git add smart-ui/src/views/platform/area/limit/components/DeviceTreePicker.vue \
        smart-ui/src/views/platform/area/limit/components/DeviceTreePicker.test.js
git commit -m "feat(smart-ui): add searchable device tree picker with flat selected list"
```

---

### Task 6: `edit.vue` 接入 `DeviceTreePicker` + 锁死权限性质下拉框

**Files:**
- Modify: `smart-ui/src/views/platform/area/limit/edit.vue`

**Interfaces:**
- Consumes: `DeviceTreePicker`（Task 5）

- [ ] **Step 1: 替换设备树为新组件**

`edit.vue` 模板里，把这一段：
```html
                <el-form-item label="选择设备" prop="checkedlimits">
                  <div class="qt-limit">
                    <el-tree
                      :data="treeData"
                      ref="limitree"
                      node-key="id"
                      show-checkbox
                      default-expand-all
                      :highlight-current="true"
                      :check-strictly="true"
                      :default-checked-keys="editform.checkedlimits"
                      :props="defaultProps"
                    ></el-tree>
                  </div>
                </el-form-item>
```
替换为：
```html
                <el-form-item label="选择设备" prop="checkedlimits">
                  <DeviceTreePicker :tree-data="treeData" v-model="editform.checkedlimits" />
                </el-form-item>
```

- [ ] **Step 2: 锁死权限性质下拉框**

把：
```html
                  <el-form-item label="权限性质" prop="areaType">
                    <el-select v-model="editform.areaType" placeholder="权限性质" :disabled="areaTypeDisable">
```
改为：
```html
                  <el-form-item label="权限性质" prop="areaType">
                    <el-select v-model="editform.areaType" placeholder="权限性质" :disabled="true">
```
（性质变更收口到 Task 9 的独立入口，编辑页职责收窄为只管设备清单/名称/备注）

- [ ] **Step 3: 更新脚本部分**

- 引入并注册组件：在 `<script>` 顶部 import 区新增
  ```js
  import DeviceTreePicker from './components/DeviceTreePicker.vue'
  ```
  在 `export default { name: "limit", ... }` 里新增
  ```js
  components: { DeviceTreePicker },
  ```
- 删除 `data()` 里已经不再需要的 `areaTypeDisable: true` 和 `defaultProps: { children: 'children', label: 'label' }`（渲染逻辑已经移到 `DeviceTreePicker` 内部）。
- 在 `created()` 里，删除这段（不再需要根据 type 计算 `areaTypeDisable`）：
  ```js
      if (this.editform.type === 3) {
        this.areaTypeDisable = true
      } else {
        this.areaTypeDisable = false
      }
  ```
- 删除 `methods` 里的 `resetChecked`/`setCheckedNodes`（已废弃、也没有按钮在调用）。
- `onSubmit` 方法里，删除这一行（`checkedlimits` 现在由 `DeviceTreePicker` 通过 v-model 实时维护，不需要保存时再读 `getCheckedKeys()`）：
  ```js
      this.editform.checkedlimits = this.$refs.limitree.getCheckedKeys();
  ```

- [ ] **Step 4: 编译确认**

Run: `cd smart-ui && pnpm run lint`
Expected: 无新增 lint 报错

- [ ] **Step 5: Commit**

```bash
git add smart-ui/src/views/platform/area/limit/edit.vue
git commit -m "refactor(smart-ui): use DeviceTreePicker in device authority edit page"
```

---

### Task 7: `add.vue` 接入 `DeviceTreePicker`

**Files:**
- Modify: `smart-ui/src/views/platform/area/limit/add.vue`

**Interfaces:**
- Consumes: `DeviceTreePicker`（Task 5）

- [ ] **Step 1: 替换设备树为新组件**

把：
```html
                <el-form-item label="选择设备" prop="checkedlimits">
                  <div class="qt-limit">
                    <el-tree
                      :data="treeData"
                      ref="limitree"
                      node-key="id"
                      show-checkbox
                      default-expand-all
                      :highlight-current="true"
                      :check-strictly="true"
                      :props="defaultProps"
                      @node-click="handleNodeClick"
                    ></el-tree>
                  </div>
                </el-form-item>
```
替换为：
```html
                <el-form-item label="选择设备" prop="checkedlimits">
                  <DeviceTreePicker :tree-data="treeData" v-model="addform.checkedlimits" />
                </el-form-item>
```

- [ ] **Step 2: 更新脚本部分**

- import 区新增 `import DeviceTreePicker from './components/DeviceTreePicker.vue'`，`components: { DeviceTreePicker }`。
- 删除 `data()` 里的 `defaultProps: { children: 'children', label: 'label' }`。
- 删除 `methods` 里的 `handleNodeClick`、`resetChecked`、`setCheckedNodes`（都已废弃，模板里也不再引用）。
- `onSubmit` 方法里删除：
  ```js
      this.addform.checkedlimits = this.$refs.limitree.getCheckedKeys();
  ```

- [ ] **Step 3: 编译确认**

Run: `cd smart-ui && pnpm run lint`
Expected: 无新增 lint 报错

- [ ] **Step 4: Commit**

```bash
git add smart-ui/src/views/platform/area/limit/add.vue
git commit -m "refactor(smart-ui): use DeviceTreePicker in device authority add page"
```

---

### Task 8: `AreaTypeSwitchDialog.vue` 组件（TDD）

**Files:**
- Create: `smart-ui/src/views/platform/area/limit/components/AreaTypeSwitchDialog.vue`
- Test: `smart-ui/src/views/platform/area/limit/components/AreaTypeSwitchDialog.test.js`

**Interfaces:**
- Consumes: `switchAreaType({ id, areaType })`（Task 4）
- Produces: `<AreaTypeSwitchDialog :visible.sync="Boolean" :authority="{id,authorityName,areaType}" @success="..." />`

- [ ] **Step 1: 写失败的组件测试**

```js
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Vue from 'vue'
import ElementUI from 'element-ui'
import AreaTypeSwitchDialog from './AreaTypeSwitchDialog.vue'
import { switchAreaType } from '@/api/platform/area/limit'

Vue.use(ElementUI)

vi.mock('@/api/platform/area/limit', () => ({
  switchAreaType: vi.fn()
}))

describe('AreaTypeSwitchDialog', () => {
  beforeEach(() => {
    switchAreaType.mockReset()
  })

  it('展示当前性质到目标性质的变更方向', async () => {
    const wrapper = mount(AreaTypeSwitchDialog, {
      propsData: {
        visible: true,
        authority: { id: 1, authorityName: '保密_1F2区超黑面检机', areaType: 1 }
      }
    })
    // el-dialog 的内容靠 transition 控制渲染时机，挂载后要等一个 tick 才能拿到弹窗正文
    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('保密区域')
    expect(wrapper.text()).toContain('公共区域')
  })

  it('切换成功时触发 success 事件并关闭弹窗', async () => {
    switchAreaType.mockResolvedValue({ data: { data: { success: true, conflicts: [] } } })
    const wrapper = mount(AreaTypeSwitchDialog, {
      propsData: {
        visible: true,
        authority: { id: 1, authorityName: '保密_1F2区超黑面检机', areaType: 1 }
      }
    })

    await wrapper.find('.area-type-switch__confirm').trigger('click')
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(switchAreaType).toHaveBeenCalledWith({ id: 1, areaType: 0 })
    expect(wrapper.emitted('success')).toBeTruthy()
    expect(wrapper.emitted('update:visible')[0][0]).toBe(false)
  })

  it('存在冲突时展示冲突设备清单并禁用确认按钮', async () => {
    switchAreaType.mockResolvedValue({
      data: {
        data: {
          success: false,
          conflicts: [
            { deviceId: 'device-A', deviceName: '1F-2区-超黑面检机-03', conflictAuthorityId: 200, conflictAuthorityName: '门禁_AB栋连廊' }
          ]
        }
      }
    })
    const wrapper = mount(AreaTypeSwitchDialog, {
      propsData: {
        visible: true,
        authority: { id: 1, authorityName: '保密_1F2区超黑面检机', areaType: 1 }
      }
    })

    await wrapper.find('.area-type-switch__confirm').trigger('click')
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('1F-2区-超黑面检机-03')
    expect(wrapper.text()).toContain('门禁_AB栋连廊')
    expect(wrapper.emitted('success')).toBeFalsy()
    expect(wrapper.find('.area-type-switch__confirm').attributes('disabled')).toBe('disabled')
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd smart-ui && pnpm exec vitest run src/views/platform/area/limit/components/AreaTypeSwitchDialog.test.js`
Expected: FAIL（`AreaTypeSwitchDialog.vue` 还不存在）

- [ ] **Step 3: 实现组件**

```vue
<template>
  <el-dialog
    title="变更通关权限性质"
    :visible.sync="innerVisible"
    width="480px"
    @closed="reset"
  >
    <div class="area-type-switch">
      <p class="area-type-switch__row">
        <span>权限组</span>
        <strong>{{ authority.authorityName }}</strong>
      </p>
      <p class="area-type-switch__row">
        <span>性质变更</span>
        <strong>{{ areaTypeLabel(authority.areaType) }} → {{ areaTypeLabel(targetAreaType) }}</strong>
      </p>
      <div v-if="conflicts.length" class="area-type-switch__conflicts">
        <p class="area-type-switch__conflicts-title">以下设备已被其他权限组占用，无法切换：</p>
        <table>
          <tr v-for="item in conflicts" :key="item.deviceId">
            <td>{{ item.deviceName }}</td>
            <td>{{ item.conflictAuthorityName }}</td>
          </tr>
        </table>
        <p class="area-type-switch__conflicts-tip">
          请先到对应权限组的编辑页移除以上设备，再回来切换性质。
        </p>
      </div>
    </div>
    <div slot="footer">
      <el-button @click="innerVisible = false">取消</el-button>
      <el-button
        type="primary"
        class="area-type-switch__confirm"
        :loading="submitting"
        :disabled="conflicts.length > 0"
        @click="submit"
      >确定切换</el-button>
    </div>
  </el-dialog>
</template>

<script>
// 变更通关权限性质的独立入口：只改 area_type 一个字段，不加载、不展示设备树，
// 和「编辑」页的设备清单编辑物理隔离，避免重新踩“切换性质时设备树被换掉”的坑。
import { switchAreaType } from '@/api/platform/area/limit'

export default {
  name: 'AreaTypeSwitchDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    authority: {
      type: Object,
      default: () => ({})
    }
  },
  data () {
    return {
      submitting: false,
      conflicts: []
    }
  },
  computed: {
    innerVisible: {
      get () {
        return this.visible
      },
      set (val) {
        this.$emit('update:visible', val)
      }
    },
    // 性质只有 0/1 两种取值，切换必然是翻转到另一种，不需要让用户再挑一遍
    targetAreaType () {
      return this.authority.areaType === 1 ? 0 : 1
    }
  },
  methods: {
    areaTypeLabel (val) {
      return val === 1 ? '保密区域' : '公共区域'
    },
    submit () {
      this.submitting = true
      switchAreaType({ id: this.authority.id, areaType: this.targetAreaType }).then(response => {
        this.submitting = false
        const result = response.data.data
        if (result.success) {
          this.$message.success('权限性质已切换')
          this.$emit('success')
          this.innerVisible = false
        } else {
          this.conflicts = result.conflicts || []
        }
      }).catch(() => {
        this.submitting = false
      })
    },
    reset () {
      this.conflicts = []
      this.submitting = false
    }
  }
}
</script>

<style lang="scss" scoped>
.area-type-switch {
  &__row {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
    margin: 0 0 12px;
  }
  &__conflicts {
    border: 1px solid #f5c6a5;
    border-radius: 4px;
    padding: 8px 12px;
    margin-top: 8px;
    table {
      width: 100%;
      font-size: 13px;
    }
  }
  &__conflicts-title {
    font-size: 13px;
    color: #e6a23c;
    margin: 0 0 6px;
  }
  &__conflicts-tip {
    font-size: 12px;
    color: #909399;
    margin: 6px 0 0;
  }
}
</style>
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd smart-ui && pnpm exec vitest run src/views/platform/area/limit/components/AreaTypeSwitchDialog.test.js`
Expected: `3 passed`

- [ ] **Step 5: Commit**

```bash
git add smart-ui/src/views/platform/area/limit/components/AreaTypeSwitchDialog.vue \
        smart-ui/src/views/platform/area/limit/components/AreaTypeSwitchDialog.test.js
git commit -m "feat(smart-ui): add area-type switch confirmation dialog"
```

---

### Task 9: 三个列表页接入「变更性质」入口

**Files:**
- Modify: `smart-ui/src/views/platform/area/limit/indexAccess.vue`
- Modify: `smart-ui/src/views/platform/area/limit/index.vue`
- Modify: `smart-ui/src/views/platform/area/limit/indexAttendance.vue`

**Interfaces:**
- Consumes: `AreaTypeSwitchDialog`（Task 8）

三个文件的 `menu` slot 和 script 结构完全一致（已在规划阶段逐字核对过，只有 `deviceUseType`/`backPageTag` 的具体值不同），下面的改动对三个文件逐一原样应用。

- [ ] **Step 1: 模板里新增按钮**

在每个文件的 `menu` slot 里，`>删除</el-button>` 后面新增：
```html
            <el-button
              type="text"
              icon="el-icon-refresh"
              @click="handleAreaTypeSwitch(scope.row)"
            >变更性质</el-button>
```

在 `</avue-crud>` 后面（`</section>` 之前）新增：
```html
        <AreaTypeSwitchDialog
          :visible.sync="areaTypeSwitchVisible"
          :authority="areaTypeSwitchTarget"
          @success="getList(page, searchForm)"
        ></AreaTypeSwitchDialog>
```

- [ ] **Step 2: 脚本里新增状态和方法**

import 区新增：
```js
import AreaTypeSwitchDialog from './components/AreaTypeSwitchDialog.vue'
```
`export default` 里新增：
```js
  components: { AreaTypeSwitchDialog },
```
`data()` 返回对象里新增：
```js
      areaTypeSwitchVisible: false,
      areaTypeSwitchTarget: {},
```
`methods` 里新增：
```js
    handleAreaTypeSwitch (row) {
      this.areaTypeSwitchTarget = row
      this.areaTypeSwitchVisible = true
    },
```

- [ ] **Step 3: 编译确认**

Run: `cd smart-ui && pnpm run lint`
Expected: 无新增 lint 报错

- [ ] **Step 4: Commit**

```bash
git add smart-ui/src/views/platform/area/limit/indexAccess.vue \
        smart-ui/src/views/platform/area/limit/index.vue \
        smart-ui/src/views/platform/area/limit/indexAttendance.vue
git commit -m "feat(smart-ui): wire area-type switch entry point into device authority list pages"
```

---

### Task 10: 端到端手工验证

**Files:** 无代码改动，纯验证

- [ ] **Step 1: 启动后端 + 前端**

按 `AGENTS.md` 里的命令：在 `smart-module` 目录下 `mvn -pl smart-platform/smart-platform-biz -am package -DskipTests` 打包并按现有部署方式启动；`smart-ui` 目录下 `pnpm dev`。

- [ ] **Step 2: 用 webapp-testing 走一遍设计文档里列的四条路径**

对照 [design doc §5.2](../specs/2026-07-01-device-authority-area-type-switch-design.md)：
1. 公共 → 保密：无冲突场景，切换成功，刷新后列表显示新性质。
2. 保密 → 公共：无冲突场景，同上。
3. 有冲突场景：弹窗展示冲突设备清单，确认按钮禁用，取消后数据未变。
4. 无冲突场景切换后，回到该权限组的「关联员工」/「关联内部车辆」页，确认行数和切换前完全一致。

- [ ] **Step 3: 验证新设备绑定 UI**

打开一个设备较多的权限组编辑页：搜索框输入关键字确认树过滤生效；勾选几个不同楼栋的设备确认右侧平铺列表同步更新（不分组）；点击右侧 × 确认联动取消左侧勾选；点击「清空」确认全部取消；保存后重新打开确认选中集合被正确保留。

- [ ] **Step 4: 回归确认**

确认车辆权限组（`type=3`）的「变更性质」入口也能正常工作（Task 2 的服务层逻辑对人员/车辆一视同仁，没有按 type 做特殊处理）。
