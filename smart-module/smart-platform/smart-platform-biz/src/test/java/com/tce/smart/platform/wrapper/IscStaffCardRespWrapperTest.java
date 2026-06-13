package com.tce.smart.platform.wrapper;

import com.tce.smart.platform.api.dto.resp.isc.IscStaffCardRespDTO;
import com.tce.smart.platform.core.entity.SmtIscStaffCard;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

public class IscStaffCardRespWrapperTest {

	@Test
	public void wrapperRegistersStaffCardToRespDto() {
		IscStaffCardRespWrapper wrapper = new IscStaffCardRespWrapper();

		Assert.assertEquals(SmtIscStaffCard.class, wrapper.getModelClass());
		Assert.assertEquals(IscStaffCardRespDTO.class, wrapper.getDataClass());
		Assert.assertNotNull(wrapper.getClass().getAnnotation(Component.class));
	}

	@Test
	public void warpCopiesStaffCardFields() throws Exception {
		LocalDateTime now = LocalDateTime.of(2026, 6, 3, 11, 0);
		SmtIscStaffCard card = new SmtIscStaffCard();
		card.setId(2L);
		card.setStaffId(1001L);
		card.setBadge("A1001");
		card.setParkId(5000021);
		card.setParkName("许昌园区");
		card.setDispatcherParkId(6000001);
		card.setDispatcherParkName("许昌ISC");
		card.setCardNo("123456");
		card.setDelFlag(0);
		card.setRemark("实体卡");
		card.setCreateTime(now);
		card.setUpdateTime(now.plusHours(1));
		card.setOptUser("admin");

		IscStaffCardRespDTO dto = new IscStaffCardRespWrapper().warp(card);

		Assert.assertEquals(card.getId(), dto.getId());
		Assert.assertEquals(card.getStaffId(), dto.getStaffId());
		Assert.assertEquals(card.getBadge(), dto.getBadge());
		Assert.assertEquals(card.getParkId(), dto.getParkId());
		Assert.assertEquals(card.getParkName(), dto.getParkName());
		Assert.assertEquals(card.getDispatcherParkId(), dto.getDispatcherParkId());
		Assert.assertEquals(card.getDispatcherParkName(), dto.getDispatcherParkName());
		Assert.assertEquals(card.getCardNo(), dto.getCardNo());
		Assert.assertEquals(card.getDelFlag(), dto.getDelFlag());
		Assert.assertEquals(card.getRemark(), dto.getRemark());
		Assert.assertEquals(card.getCreateTime(), dto.getCreateTime());
		Assert.assertEquals(card.getUpdateTime(), dto.getUpdateTime());
		Assert.assertEquals(card.getOptUser(), dto.getOptUser());
	}
}
