package com.tce.smart.platform.api.dto.req;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TempStaffEditReqDTOTest {

	@Test
	public void trimTextFieldsRemovesLeadingAndTrailingWhitespaceFromAllTextFields() {
		TempStaffEditReqDTO request = new TempStaffEditReqDTO();
		request.setName(" 李思翔 ");
		request.setPhone(" 13700893346 ");
		request.setBadge(" HC0460 ");
		request.setCertno(" 411082200603033070 ");
		request.setJobName(" 钳工 ");
		request.setDepName(" 河南汇创 ");
		request.setJcheId(" 8 ");
		request.setJcheName(" 技工层 ");
		request.setFaceImg(" face-data ");
		request.setEntryTime(" 2026-07-21 ");
		request.setDispatch(" 外包 ");
		request.setBadges(" HC0460,HC0461 ");
		request.setIds(Arrays.asList(" 1 ", " 2 "));

		request.trimTextFields();

		Assert.assertEquals("李思翔", request.getName());
		Assert.assertEquals("13700893346", request.getPhone());
		Assert.assertEquals("HC0460", request.getBadge());
		Assert.assertEquals("411082200603033070", request.getCertno());
		Assert.assertEquals("钳工", request.getJobName());
		Assert.assertEquals("河南汇创", request.getDepName());
		Assert.assertEquals("8", request.getJcheId());
		Assert.assertEquals("技工层", request.getJcheName());
		Assert.assertEquals("face-data", request.getFaceImg());
		Assert.assertEquals("2026-07-21", request.getEntryTime());
		Assert.assertEquals("外包", request.getDispatch());
		Assert.assertEquals("HC0460,HC0461", request.getBadges());
		Assert.assertEquals(Arrays.asList("1", "2"), request.getIds());
	}
}
