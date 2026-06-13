package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwHortationsAllRespDTO;
import com.tce.smart.ehrview.core.entity.EvwHortationsAll;
import com.tce.smart.tool.enums.EvwEmphrYsEnum;
import com.tce.smart.tool.enums.EvwHortationsAllJchenEnum;
import com.tce.smart.tool.enums.EvwHortationsAllKindEnum;
import com.tce.smart.tool.enums.EvwHortationsAllTypeEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-14 11:34
 */
@Component
public class EvwHortationsAllWrapper extends BaseWrapper<EvwHortationsAll, EvwHortationsAllRespDTO> {
	@Override
	protected EvwHortationsAllRespDTO warp(EvwHortationsAll model) {
		EvwHortationsAllRespDTO evwHortationsAllRespDTO = new EvwHortationsAllRespDTO();
		BeanUtils.copyProperties(model, evwHortationsAllRespDTO);
		if(Objects.nonNull(model.getJchenID()))
			evwHortationsAllRespDTO.setJchenDesc(EvwHortationsAllJchenEnum.desc(model.getJchenID()));
		if(Objects.nonNull(model.getKind()))
			evwHortationsAllRespDTO.setKindDesc(EvwHortationsAllKindEnum.desc(model.getKind()));
		if(Objects.nonNull(model.getType()))
			evwHortationsAllRespDTO.setTypeDesc(EvwHortationsAllTypeEnum.desc(model.getType()));
		if(Objects.nonNull(model.getStatus()))
			evwHortationsAllRespDTO.setStatusDesc(EvwEmphrYsEnum.desc(model.getStatus()));
		return evwHortationsAllRespDTO;
	}
}
