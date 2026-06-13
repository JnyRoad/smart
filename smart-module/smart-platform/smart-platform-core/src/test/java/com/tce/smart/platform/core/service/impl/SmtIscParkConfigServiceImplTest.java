package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.req.isc.EditIscParkConfigReqDTO;
import com.tce.smart.platform.core.entity.SmtIscParkConfig;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.enums.DeviceSyncEnum;
import com.tce.smart.platform.core.mapper.SmtIscParkConfigMapper;
import com.tce.smart.platform.core.mapper.SmtParkMapper;
import com.tce.smart.tool.enums.DeleteStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtIscParkConfigServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscParkConfig.class);
	}

	@Test
	public void activeKeyUpdateStrategyWritesNullOnSoftDelete() {
		TableFieldInfo activeKeyInfo = activeKeyFieldInfo();

		Assert.assertEquals(FieldStrategy.IGNORED, activeKeyInfo.getUpdateStrategy());
		String sqlSet = activeKeyInfo.getSqlSet(false, null).toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSet.contains("ACTIVE_KEY"));
		Assert.assertFalse(sqlSet.contains("<IF"));
	}

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void getCardSyncDispatcherParkIdsUsesActiveEnabledBindings() throws Exception {
		SmtIscParkConfigMapper mapper = Mockito.mock(SmtIscParkConfigMapper.class);
		SmtIscParkConfigServiceImpl service = new SmtIscParkConfigServiceImpl();
		setField(service, "baseMapper", mapper);
		SmtIscParkConfig config = new SmtIscParkConfig();
		config.setParkId(5000021);
		config.setDispatcherParkId(6000001);
		config.setCardSyncEnabled(DeviceSyncEnum.YES.getCode());
		config.setDelFlag(DeleteStatusEnum.NOT_DELETE.getCode());
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Arrays.asList(config));
		Set<Integer> businessParkIds = new LinkedHashSet<>(Arrays.asList(5000021));

		Set<Integer> dispatcherParkIds = service.getCardSyncDispatcherParkIds(businessParkIds);

		Assert.assertEquals(new LinkedHashSet<>(Arrays.asList(6000001)), dispatcherParkIds);
		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(mapper).selectList(queryCaptor.capture());
		String sqlSegment = queryCaptor.getValue().getSqlSegment().toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSegment.contains("PARK_ID"));
		Assert.assertTrue(sqlSegment.contains("CARD_SYNC_ENABLED"));
		Assert.assertTrue(sqlSegment.contains("DEL_FLAG"));
		Assert.assertTrue(queryCaptor.getValue().getParamNameValuePairs().values()
				.contains(DeviceSyncEnum.YES.getCode()));
		Assert.assertTrue(queryCaptor.getValue().getParamNameValuePairs().values()
				.contains(DeleteStatusEnum.NOT_DELETE.getCode()));
		Assert.assertTrue(queryCaptor.getValue().getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(value).contains("5000021")));
	}

	@Test
	public void getCardSyncDispatcherParkIdsRejectsPartiallyConfiguredBusinessParks() throws Exception {
		SmtIscParkConfigMapper mapper = Mockito.mock(SmtIscParkConfigMapper.class);
		SmtIscParkConfigServiceImpl service = new SmtIscParkConfigServiceImpl();
		setField(service, "baseMapper", mapper);
		SmtIscParkConfig config = new SmtIscParkConfig();
		config.setParkId(5000021);
		config.setDispatcherParkId(6000001);
		config.setCardSyncEnabled(DeviceSyncEnum.YES.getCode());
		config.setDelFlag(DeleteStatusEnum.NOT_DELETE.getCode());
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Arrays.asList(config));
		Set<Integer> businessParkIds = new LinkedHashSet<>(Arrays.asList(5000021, 5000022));

		try {
			service.getCardSyncDispatcherParkIds(businessParkIds);
			Assert.fail("Expected TCEException");
		} catch (TCEException e) {
			Assert.assertTrue(e.getMessage().contains("未启用ISC卡片同步配置"));
			Assert.assertTrue(e.getMessage().contains("5000022"));
		}
	}

	@Test
	public void getActiveConfigByIdFiltersSoftDeletedRows() throws Exception {
		setCurrentUserParks(5000021, 6000001);
		SmtIscParkConfigMapper mapper = Mockito.mock(SmtIscParkConfigMapper.class);
		SmtIscParkConfigServiceImpl service = new SmtIscParkConfigServiceImpl();
		setField(service, "baseMapper", mapper);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.emptyList());

		Assert.assertNull(service.getActiveConfigById(10L));

		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(mapper).selectList(queryCaptor.capture());
		String sqlSegment = queryCaptor.getValue().getSqlSegment().toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSegment.contains("ID"));
		Assert.assertTrue(sqlSegment.contains("DEL_FLAG"));
		Assert.assertTrue(queryCaptor.getValue().getParamNameValuePairs().values().contains(10L));
		Assert.assertTrue(queryCaptor.getValue().getParamNameValuePairs().values()
				.contains(DeleteStatusEnum.NOT_DELETE.getCode()));
	}

	@Test
	public void getActiveConfigByIdRejectsMissingUserParkContext() throws Exception {
		SmtIscParkConfigServiceImpl service = new SmtIscParkConfigServiceImpl();

		try {
			service.getActiveConfigById(10L);
			Assert.fail("Expected TCEException");
		} catch (TCEException e) {
			Assert.assertTrue(e.getMessage().contains("园区权限"));
		}
	}

	@Test
	public void getActiveConfigByIdFiltersUnauthorizedDispatcherPark() throws Exception {
		setCurrentUserParks(5000021);
		SmtIscParkConfigMapper mapper = Mockito.mock(SmtIscParkConfigMapper.class);
		SmtIscParkConfigServiceImpl service = new SmtIscParkConfigServiceImpl();
		setField(service, "baseMapper", mapper);
		SmtIscParkConfig config = new SmtIscParkConfig();
		config.setId(10L);
		config.setParkId(5000021);
		config.setDispatcherParkId(6000001);
		config.setDelFlag(DeleteStatusEnum.NOT_DELETE.getCode());
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Arrays.asList(config));

		Assert.assertNull(service.getActiveConfigById(10L));
	}

	@Test
	public void editConfigRejectsMissingDispatcherPark() throws Exception {
		setCurrentUserParks(5000021, 6000001);
		SmtIscParkConfigMapper mapper = Mockito.mock(SmtIscParkConfigMapper.class);
		SmtParkMapper parkMapper = Mockito.mock(SmtParkMapper.class);
		SmtIscParkConfigServiceImpl service = new SmtIscParkConfigServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "smtParkMapper", parkMapper);
		Mockito.when(parkMapper.selectById(5000021)).thenReturn(park(5000021, "许昌园区"));
		Mockito.when(parkMapper.selectById(6000001)).thenReturn(null);

		try {
			service.editConfig(editReq(5000021, 6000001));
			Assert.fail("Expected TCEException");
		} catch (TCEException e) {
			Assert.assertTrue(e.getMessage().contains("ISC调度园区不存在"));
		}

		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(SmtIscParkConfig.class));
		Mockito.verify(mapper, Mockito.never()).updateById(Mockito.any(SmtIscParkConfig.class));
	}

	@Test
	public void editConfigRejectsDispatcherParkOutsideCurrentUserParks() throws Exception {
		setCurrentUserParks(5000021);
		SmtIscParkConfigMapper mapper = Mockito.mock(SmtIscParkConfigMapper.class);
		SmtParkMapper parkMapper = Mockito.mock(SmtParkMapper.class);
		SmtIscParkConfigServiceImpl service = new SmtIscParkConfigServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "smtParkMapper", parkMapper);
		Mockito.when(parkMapper.selectById(5000021)).thenReturn(park(5000021, "许昌园区"));
		Mockito.when(parkMapper.selectById(6000001)).thenReturn(park(6000001, "深圳ISC园区"));

		try {
			service.editConfig(editReq(5000021, 6000001));
			Assert.fail("Expected TCEException");
		} catch (TCEException e) {
			Assert.assertTrue(e.getMessage().contains("无权限操作"));
		}

		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(SmtIscParkConfig.class));
		Mockito.verify(mapper, Mockito.never()).updateById(Mockito.any(SmtIscParkConfig.class));
	}

	@Test
	public void removeConfigByIdSoftDeletesConfig() throws Exception {
		setCurrentUserParks(5000021, 6000001);
		SmtIscParkConfigMapper mapper = Mockito.mock(SmtIscParkConfigMapper.class);
		SmtIscParkConfigServiceImpl service = new SmtIscParkConfigServiceImpl();
		setField(service, "baseMapper", mapper);
		SmtIscParkConfig oldConfig = new SmtIscParkConfig();
		oldConfig.setId(10L);
		oldConfig.setParkId(5000021);
		oldConfig.setDispatcherParkId(6000001);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Arrays.asList(oldConfig));
		Mockito.when(mapper.updateById(Mockito.any(SmtIscParkConfig.class))).thenReturn(1);

		Assert.assertTrue(service.removeConfigById(10L));

		ArgumentCaptor<SmtIscParkConfig> configCaptor = ArgumentCaptor.forClass(SmtIscParkConfig.class);
		Mockito.verify(mapper).updateById(configCaptor.capture());
		SmtIscParkConfig config = configCaptor.getValue();
		Assert.assertEquals(Long.valueOf(10L), config.getId());
		Assert.assertEquals(DeleteStatusEnum.IS_DELETE.getCode(), config.getDelFlag());
		Assert.assertNull(config.getActiveKey());
		Assert.assertNotNull(config.getUpdateTime());
	}

	@Test
	public void editConfigSetsActiveKeyForDatabaseUniqueConstraint() throws Exception {
		setCurrentUserParks(5000021, 6000001);
		SmtIscParkConfigMapper mapper = Mockito.mock(SmtIscParkConfigMapper.class);
		SmtParkMapper parkMapper = Mockito.mock(SmtParkMapper.class);
		SmtIscParkConfigServiceImpl service = new SmtIscParkConfigServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "smtParkMapper", parkMapper);
		Mockito.when(parkMapper.selectById(5000021)).thenReturn(park(5000021, "许昌园区"));
		Mockito.when(parkMapper.selectById(6000001)).thenReturn(park(6000001, "许昌ISC"));
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(mapper.insert(Mockito.any(SmtIscParkConfig.class))).thenReturn(1);

		Assert.assertTrue(service.editConfig(editReq(5000021, 6000001)));

		ArgumentCaptor<SmtIscParkConfig> configCaptor = ArgumentCaptor.forClass(SmtIscParkConfig.class);
		Mockito.verify(mapper).insert(configCaptor.capture());
		Assert.assertEquals("5000021", configCaptor.getValue().getActiveKey());
	}

	private void setCurrentUserParks(Integer... parkIds) {
		SmartUser user = new SmartUser(1, 1, "tester", Arrays.asList(parkIds), "password",
				true, true, true, true, AuthorityUtils.NO_AUTHORITIES);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES));
	}

	private EditIscParkConfigReqDTO editReq(Integer parkId, Integer dispatcherParkId) {
		EditIscParkConfigReqDTO reqDTO = new EditIscParkConfigReqDTO();
		reqDTO.setParkId(parkId);
		reqDTO.setDispatcherParkId(dispatcherParkId);
		reqDTO.setCardSyncEnabled(DeviceSyncEnum.YES.getCode());
		return reqDTO;
	}

	private SmtPark park(Integer id, String name) {
		SmtPark park = new SmtPark();
		park.setId(id);
		park.setParkName(name);
		return park;
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException e) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}

	private TableFieldInfo activeKeyFieldInfo() {
		return TableInfoHelper.getTableInfo(SmtIscParkConfig.class).getFieldList().stream()
				.filter(field -> "activeKey".equals(field.getProperty()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("activeKey field not found"));
	}
}
