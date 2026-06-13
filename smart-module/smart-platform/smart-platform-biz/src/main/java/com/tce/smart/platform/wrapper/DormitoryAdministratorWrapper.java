package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.SmtDormitoryAdministratorRespDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryAdministrator;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.service.SmtParkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Title: DormitoryAdministratorWrapper
 * @Descripition:
 * @Auther: guohongtai
 * @Date: 2020-10-14 20:18
 */
@Component
public class DormitoryAdministratorWrapper extends BaseWrapper<SmtDormitoryAdministrator, SmtDormitoryAdministratorRespDTO> {
	@Autowired
	private SmtParkService smtParkService;
	@Override
	protected SmtDormitoryAdministratorRespDTO warp(SmtDormitoryAdministrator smtDormitoryAdministrator) throws IOException {
		SmtDormitoryAdministratorRespDTO smtDormitoryAdministratorRespDTO = new SmtDormitoryAdministratorRespDTO();
		BeanUtil.copyProperties(smtDormitoryAdministrator, smtDormitoryAdministratorRespDTO);

		SmtPark park = smtParkService.getById(smtDormitoryAdministrator.getParkId());
		smtDormitoryAdministratorRespDTO.setParkName(park.getParkName());
		return smtDormitoryAdministratorRespDTO;
	}
}
