package com.tce.smart.algorithm.wrapper;

import com.tce.smart.algorithm.api.dto.resp.FaceDetectTypeDTO;
import com.tce.smart.algorithm.api.enums.FaceDetectTypeEnum;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.stereotype.Component;

/**
 * @author wxjason
 */
@Component
public class FaceDetectTypeWrapper extends BaseWrapper<FaceDetectTypeEnum, FaceDetectTypeDTO> {

	@Override
	protected FaceDetectTypeDTO warp(FaceDetectTypeEnum model) {
		FaceDetectTypeDTO dto = new FaceDetectTypeDTO();
		dto.setType(model.getType());
		dto.setName(model.getName());
		return dto;
	}
}
