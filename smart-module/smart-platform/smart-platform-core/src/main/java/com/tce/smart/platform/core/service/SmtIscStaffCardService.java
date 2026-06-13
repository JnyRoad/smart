package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.isc.EditIscStaffCardReqDTO;
import com.tce.smart.platform.core.entity.SmtIscCardTask;
import com.tce.smart.platform.core.entity.SmtIscParkConfig;
import com.tce.smart.platform.core.entity.SmtIscStaffCard;
import com.tce.smart.platform.core.entity.SmtStaff;

import java.util.List;

public interface SmtIscStaffCardService extends IService<SmtIscStaffCard> {

	List<SmtIscStaffCard> listStaffCards(Long staffId);

	Boolean saveStaffCard(EditIscStaffCardReqDTO reqDTO);

	Boolean removeStaffCard(Long id);

	Boolean removeStaffCardsByStaffId(Long staffId);

	boolean isActiveStaffCard(Long staffId, String badge, Integer dispatcherParkId, String cardNo);

	void markAddTaskSuccess(SmtIscCardTask task);

	void markAddTaskFailed(SmtIscCardTask task, boolean removeLocalCard);

	String getFirstActiveCardNoByBadge(String badge);

	SmtIscStaffCard importStaffCardFromIsc(SmtStaff staff, SmtIscParkConfig config, String cardNo, String remark);

	SmtIscStaffCard importStaffCardFromIsc(SmtStaff staff, SmtIscParkConfig config, String cardNo,
										   String remark, String optUser);
}
