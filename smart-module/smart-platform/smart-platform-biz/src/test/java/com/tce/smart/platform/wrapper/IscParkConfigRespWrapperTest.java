package com.tce.smart.platform.wrapper;

import com.tce.smart.platform.api.dto.resp.isc.IscParkConfigRespDTO;
import com.tce.smart.platform.core.entity.SmtIscParkConfig;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

public class IscParkConfigRespWrapperTest {

	@Test
	public void wrapperRegistersParkConfigToRespDto() {
		IscParkConfigRespWrapper wrapper = new IscParkConfigRespWrapper();

		Assert.assertEquals(SmtIscParkConfig.class, wrapper.getModelClass());
		Assert.assertEquals(IscParkConfigRespDTO.class, wrapper.getDataClass());
		Assert.assertNotNull(wrapper.getClass().getAnnotation(Component.class));
	}

	@Test
	public void warpCopiesParkConfigFields() throws Exception {
		LocalDateTime now = LocalDateTime.of(2026, 6, 3, 10, 30);
		SmtIscParkConfig config = new SmtIscParkConfig();
		config.setId(1L);
		config.setParkId(5000021);
		config.setParkName("许昌园区");
		config.setDispatcherParkId(6000001);
		config.setDispatcherParkName("许昌ISC");
		config.setCardSyncEnabled(1);
		config.setDelFlag(0);
		config.setRemark("同步启用");
		config.setCreateTime(now);
		config.setUpdateTime(now.plusHours(1));
		config.setOptUser("admin");

		IscParkConfigRespDTO dto = new IscParkConfigRespWrapper().warp(config);

		Assert.assertEquals(config.getId(), dto.getId());
		Assert.assertEquals(config.getParkId(), dto.getParkId());
		Assert.assertEquals(config.getParkName(), dto.getParkName());
		Assert.assertEquals(config.getDispatcherParkId(), dto.getDispatcherParkId());
		Assert.assertEquals(config.getDispatcherParkName(), dto.getDispatcherParkName());
		Assert.assertEquals(config.getCardSyncEnabled(), dto.getCardSyncEnabled());
		Assert.assertEquals(config.getDelFlag(), dto.getDelFlag());
		Assert.assertEquals(config.getRemark(), dto.getRemark());
		Assert.assertEquals(config.getCreateTime(), dto.getCreateTime());
		Assert.assertEquals(config.getUpdateTime(), dto.getUpdateTime());
		Assert.assertEquals(config.getOptUser(), dto.getOptUser());
	}
}
