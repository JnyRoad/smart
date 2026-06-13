package com.tce.smart.data;

import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.controller.msg.OaDataManageController;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/5 20:06
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OaInterfaceTest {

	@Autowired
	private OaDataManageController oaDataManageController;

	@Test
	public void test1(){
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8888");
		System.setProperty("https.proxyPort", "8888");
		SendSecurityAuthApplyReqDTO sendSecurityAuthApplyReqDTO = new SendSecurityAuthApplyReqDTO();
		SecurityAuthApplyMainReqDTO securityAuthApplyMainReqDTO = new SecurityAuthApplyMainReqDTO();
		List<SecurityAuthApplyDetailReqDTO> securityAuthApplyDetailReqDTOs = new ArrayList<>();
		List<SecurityAuthApplyDetailAreaReqDTO> securityAuthApplyDetailAreaReqDTOS = new ArrayList<>();
		securityAuthApplyMainReqDTO.setBadge("TEC006");
		securityAuthApplyMainReqDTO.setName("伍令");
		securityAuthApplyMainReqDTO.setCompid("64");
		securityAuthApplyMainReqDTO.setDepid("14010");
		securityAuthApplyMainReqDTO.setJobid("27598");
		sendSecurityAuthApplyReqDTO.setSecurityAuthApplyMainReqDTO(securityAuthApplyMainReqDTO);
		sendSecurityAuthApplyReqDTO.setSecurityAuthApplyDetailReqDTOs(securityAuthApplyDetailReqDTOs);
		sendSecurityAuthApplyReqDTO.setSecurityAuthApplyDetailAreaReqDTOS(securityAuthApplyDetailAreaReqDTOS);
		Object res = oaDataManageController.sendSecurityAuthApply(sendSecurityAuthApplyReqDTO);
		log.warn("发送保密权限申请，结果：{}", res);
	}

	@Test
	public void test2(){
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8888");
		System.setProperty("https.proxyPort", "8888");
		SendEntryFactoryApplyReqDTO sendEntryFactoryApplyReqDTO = new SendEntryFactoryApplyReqDTO();
		EntryFactoryApplyMainReqDTO entryFactoryApplyMainReqDTO = new EntryFactoryApplyMainReqDTO();
		List<EntryFactoryApplyShortDetailReqDTO> entryFactoryApplyShortDetailReqDTOs = new ArrayList<>();
		List<EntryFactoryApplyLongDetailReqDTO> entryFactoryApplyLongDetailReqDTOs = new ArrayList<>();
		List<EntryFactoryApplyCarDetailReqDTO> entryFactoryApplyCarDetailReqDTOs = new ArrayList<>();

		entryFactoryApplyMainReqDTO.setBadge("TEC004");
		entryFactoryApplyMainReqDTO.setName("何青青");
		entryFactoryApplyMainReqDTO.setCompid("64");
		entryFactoryApplyMainReqDTO.setDepid("14010");
		entryFactoryApplyMainReqDTO.setJobid("27599");

		sendEntryFactoryApplyReqDTO.setEntryFactoryApplyMainReqDTO(entryFactoryApplyMainReqDTO);
		sendEntryFactoryApplyReqDTO.setEntryFactoryApplyShortDetailReqDTOs(entryFactoryApplyShortDetailReqDTOs);
		sendEntryFactoryApplyReqDTO.setEntryFactoryApplyLongDetailReqDTOs(entryFactoryApplyLongDetailReqDTOs);
		sendEntryFactoryApplyReqDTO.setEntryFactoryApplyCarDetailReqDTOs(entryFactoryApplyCarDetailReqDTOs);
		Object res = oaDataManageController.sendEntryFactoryApply(sendEntryFactoryApplyReqDTO);
		log.warn("发送入厂申请，结果：{}", res);
	}

	@Test
	public void test3() {
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8888");
		System.setProperty("https.proxyPort", "8888");
		SendReleaseApplyReqDTO sendReleaseApplyReqDTO = new SendReleaseApplyReqDTO();
		ReleaseApplyMainReqDTO releaseApplyMainReqDTO = new ReleaseApplyMainReqDTO();
		List<ReleaseApplyPersonDetailReqDTO> releaseApplyPersonDetailReqDTOs = new ArrayList<>();
		List<ReleaseApplyThingDetailReqDTO> releaseApplyThingDetailReqDTOs = new ArrayList<>();

		releaseApplyMainReqDTO.setBadge("TEC004");
		releaseApplyMainReqDTO.setName("何青青");
		releaseApplyMainReqDTO.setCompid("64");
		releaseApplyMainReqDTO.setDepid("14010");
		releaseApplyMainReqDTO.setJobid("27599");
		sendReleaseApplyReqDTO.setReleaseApplyMainReqDTO(releaseApplyMainReqDTO);
		sendReleaseApplyReqDTO.setReleaseApplyPersonDetailReqDTOs(releaseApplyPersonDetailReqDTOs);
		sendReleaseApplyReqDTO.setReleaseApplyThingDetailReqDTOs(releaseApplyThingDetailReqDTOs);
		Object res = oaDataManageController.sendReleaseApply(sendReleaseApplyReqDTO);
		log.warn("发送放行条申请，结果：{}", res);
	}

	@Test
	public void test4() {
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8888");
		System.setProperty("https.proxyPort", "8888");
		SendWriteBackReturnTimeReqDTO reqDTO = new SendWriteBackReturnTimeReqDTO();
		reqDTO.setRequestid("12475806");
		reqDTO.setFcrq("2021-08-10");
		reqDTO.setFcsj("14:38");
		Object res = oaDataManageController.sendWriteBackReturnTime(reqDTO);
		log.warn("发送回写返厂时间申请，结果：{}", res);
	}

	@Test
	public void test5() {
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("https.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "8888");
		System.setProperty("https.proxyPort", "8888");
		SendSecurityApprovalReqDTO sendSecurityApprovalReqDTO = new SendSecurityApprovalReqDTO();
		sendSecurityApprovalReqDTO.setRequestid("29061");
		sendSecurityApprovalReqDTO.setUserid("10062");
		sendSecurityApprovalReqDTO.setRemark("何青青发起保安审批申请");
		Object res = oaDataManageController.sendSecurityApproval(sendSecurityApprovalReqDTO);
		log.warn("发送保安审批申请，结果：{}", res);
	}
}
