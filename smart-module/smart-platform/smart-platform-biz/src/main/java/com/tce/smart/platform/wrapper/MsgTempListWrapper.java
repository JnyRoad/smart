package com.tce.smart.platform.wrapper;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.dto.MsgPersonDTO;
import com.tce.smart.platform.core.dto.MsgTempListDTO;
import com.tce.smart.platform.core.entity.SmtMsgTemp;
import com.tce.smart.platform.core.entity.SmtMsgTempPerson;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.service.SmtMsgTempPersonService;
import com.tce.smart.platform.service.SmtParkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Li.JiaJun
 * @since 2022/9/26 15:13
 */
@Component
public class MsgTempListWrapper extends BaseWrapper<SmtMsgTemp, MsgTempListDTO> {

	@Autowired
	private SmtParkService parkService;

	@Autowired
	private SmtMsgTempPersonService smtMsgTempPersonService;

	@Override
	protected MsgTempListDTO warp(SmtMsgTemp model) {
		MsgTempListDTO dto = BeanUtils.transform(MsgTempListDTO.class, model);
		SmtPark smtPark = parkService.getById(model.getParkId());
		if (Objects.nonNull(smtPark)) {
			dto.setParkName(smtPark.getParkName());
		}
		List<SmtMsgTempPerson> tempPersonList = smtMsgTempPersonService.list(new LambdaQueryWrapper<SmtMsgTempPerson>()
				.eq(SmtMsgTempPerson::getTempId, model.getId())
		);
		if(CollectionUtil.isNotEmpty(tempPersonList)){
			List<MsgPersonDTO> personList = tempPersonList.stream().map(item -> {
				MsgPersonDTO dto1 = new MsgPersonDTO();
				dto1.setBadge(item.getBadge());
				dto1.setName(item.getName());
				return dto1;
			}).collect(Collectors.toList());
			dto.setPersonList(personList);
		}
		return dto;
	}
}
