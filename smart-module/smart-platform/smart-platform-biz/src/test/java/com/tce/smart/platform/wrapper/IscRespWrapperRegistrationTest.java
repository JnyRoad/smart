package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.wrapper.ControllerWrapper;
import com.tce.smart.platform.api.dto.resp.isc.IscCardImportBatchRespDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscCardImportDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscCardTaskRespDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscParkConfigRespDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscStaffCardRespDTO;
import com.tce.smart.platform.core.entity.SmtIscCardImportBatch;
import com.tce.smart.platform.core.entity.SmtIscCardImportDetail;
import com.tce.smart.platform.core.entity.SmtIscParkConfig;
import com.tce.smart.platform.core.entity.SmtIscStaffCard;
import com.tce.smart.platform.core.vo.IscCardTaskPageVO;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class IscRespWrapperRegistrationTest {

	@Test
	public void controllerWrapperFindsParkConfigConverter() throws Exception {
		SmtIscParkConfig config = new SmtIscParkConfig();
		config.setId(1L);
		config.setParkId(5000021);
		config.setCardSyncEnabled(1);

		IscParkConfigRespDTO dto = controllerWrapper().warp(config, IscParkConfigRespDTO.class);

		Assert.assertEquals(config.getId(), dto.getId());
		Assert.assertEquals(config.getParkId(), dto.getParkId());
		Assert.assertEquals(config.getCardSyncEnabled(), dto.getCardSyncEnabled());
	}

	@Test
	public void controllerWrapperFindsStaffCardConverter() throws Exception {
		SmtIscStaffCard card = new SmtIscStaffCard();
		card.setId(2L);
		card.setStaffId(1001L);
		card.setCardNo("123456");

		IscStaffCardRespDTO dto = controllerWrapper().warp(card, IscStaffCardRespDTO.class);

		Assert.assertEquals(card.getId(), dto.getId());
		Assert.assertEquals(card.getStaffId(), dto.getStaffId());
		Assert.assertEquals(card.getCardNo(), dto.getCardNo());
	}

	@Test
	public void controllerWrapperFindsCardTaskConverter() throws Exception {
		IscCardTaskPageVO task = new IscCardTaskPageVO();
		task.setId(3L);
		task.setParkId(5000021);
		task.setParkName("许昌园区");
		task.setBadge("JA26086");
		task.setName("张三");
		task.setCardNo("123456");
		task.setAction(DeviceTaskActionEnum.DOWN.getCode());
		task.setStatus(DeviceTaskStatusEnum.INIT.getCode());

		IscCardTaskRespDTO dto = controllerWrapper().warp(task, IscCardTaskRespDTO.class);

		Assert.assertEquals(task.getId(), dto.getId());
		Assert.assertEquals(task.getParkId(), dto.getParkId());
		Assert.assertEquals(task.getParkName(), dto.getParkName());
		Assert.assertEquals(task.getBadge(), dto.getBadge());
		Assert.assertEquals(task.getName(), dto.getName());
		Assert.assertEquals(task.getCardNo(), dto.getCardNo());
		Assert.assertEquals("新增卡片", dto.getActionDesc());
		Assert.assertEquals("初始化", dto.getStatusDesc());
	}

	@Test
	public void controllerWrapperFindsCardImportBatchConverter() throws Exception {
		SmtIscCardImportBatch batch = new SmtIscCardImportBatch();
		batch.setId(4L);
		batch.setParkId(5000021);
		batch.setMode("DRY_RUN");
		batch.setStatus("SUCCESS");
		batch.setParamsJson("{\"staffScope\":\"RESIGNED\"}");

		IscCardImportBatchRespDTO dto = controllerWrapper().warp(batch, IscCardImportBatchRespDTO.class);

		Assert.assertEquals(batch.getId(), dto.getId());
		Assert.assertEquals(batch.getParkId(), dto.getParkId());
		Assert.assertEquals(batch.getMode(), dto.getMode());
		Assert.assertEquals(batch.getStatus(), dto.getStatus());
		Assert.assertEquals("RESIGNED", dto.getStaffScope());
		Assert.assertEquals("离职人员", dto.getStaffScopeDesc());
	}

	@Test
	public void controllerWrapperFindsCardImportDetailConverter() throws Exception {
		SmtIscCardImportDetail detail = new SmtIscCardImportDetail();
		detail.setId(5L);
		detail.setBatchId(4L);
		detail.setBadge("JA26086");
		detail.setIscCardNo("67890");
		detail.setResultCode("READY_IMPORT");

		IscCardImportDetailRespDTO dto = controllerWrapper().warp(detail, IscCardImportDetailRespDTO.class);

		Assert.assertEquals(detail.getId(), dto.getId());
		Assert.assertEquals(detail.getBatchId(), dto.getBatchId());
		Assert.assertEquals(detail.getBadge(), dto.getBadge());
		Assert.assertEquals(detail.getIscCardNo(), dto.getIscCardNo());
		Assert.assertEquals(detail.getResultCode(), dto.getResultCode());
	}

	private ControllerWrapper controllerWrapper() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(IscParkConfigRespWrapper.class, IscStaffCardRespWrapper.class, IscCardTaskRespWrapper.class,
				IscCardImportBatchRespWrapper.class, IscCardImportDetailRespWrapper.class);
		context.refresh();
		ControllerWrapper controllerWrapper = new ControllerWrapper();
		controllerWrapper.setApplicationContext(context);
		controllerWrapper.afterPropertiesSet();
		context.close();
		return controllerWrapper;
	}
}
