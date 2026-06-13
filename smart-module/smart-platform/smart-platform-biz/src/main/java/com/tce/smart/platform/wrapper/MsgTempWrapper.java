package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.dto.MsgTempDTO;
import com.tce.smart.platform.core.entity.SmtMsgTemp;
import com.tce.smart.platform.core.service.SmtMsgTempPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Li.JiaJun
 * @since 2022/9/26 15:14
 */
@Component
public class MsgTempWrapper extends BaseWrapper<SmtMsgTemp, MsgTempDTO> {

	@Autowired
	private SmtMsgTempPersonService personService;

	@Override
	protected MsgTempDTO warp(SmtMsgTemp model) {
		MsgTempDTO dto = BeanUtils.transform(MsgTempDTO.class, model);
		dto.setPersonList(personService.getList(model.getId()));
		return dto;
	}
}
