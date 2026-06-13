package com.tce.smart.algorithm.wrapper;

import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.dto.resp.AlgorithmFaceDetectRecordListDTO;
import com.tce.smart.algorithm.api.enums.FaceDetectTypeEnum;
import com.tce.smart.algorithm.entity.AlgorithmFaceDetectRecord;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.stereotype.Component;

/**
 * @author wxjason
 */
@Component
public class AlgorithmFaceDetectRecordListWrapper extends BaseWrapper<AlgorithmFaceDetectRecord, AlgorithmFaceDetectRecordListDTO> {

	@Override
	protected AlgorithmFaceDetectRecordListDTO warp(AlgorithmFaceDetectRecord model) {
		AlgorithmFaceDetectRecordListDTO dto = BeanUtils.transform(AlgorithmFaceDetectRecordListDTO.class, model);
		dto.setAlgorithmName(AlgorithmTypeEnum.name(model.getAlgorithmType()));
		dto.setCreateTime(DateUtils.convert(model.getCreateTime()));
		dto.setFaceDetectTypeName(FaceDetectTypeEnum.name(model.getFaceDetectType()));
		return dto;
	}
}
