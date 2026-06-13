package com.tce.smart.platform.wrapper;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.SearchOutDormitoryRespDTO;
import com.tce.smart.platform.core.vo.SearchOutDormitoryVO;
import com.tce.smart.tool.enums.OutDormitoryStatusEnum;

import cn.hutool.core.bean.BeanUtil;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SearchOutDormitoryWrapper extends BaseWrapper<SearchOutDormitoryVO, SearchOutDormitoryRespDTO> {

	@Override
	protected SearchOutDormitoryRespDTO warp(SearchOutDormitoryVO model) throws IOException {
		// TODO Auto-generated method stub
		SearchOutDormitoryRespDTO dto=new SearchOutDormitoryRespDTO();
		BeanUtil.copyProperties(model, dto);
		dto.setId(model.getId());
		dto.setOutAddress(model.getOutAddress());
		dto.setDormitoryType("外宿");
		dto.setStatusDes(OutDormitoryStatusEnum.desc(model.getStatus()));
		return dto;
	}

}
