package com.tce.smart.algorithm.wrapper;

import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.dto.resp.AlgorithmFaceDetectRecordDetailDTO;
import com.tce.smart.algorithm.api.enums.FaceDetectTypeEnum;
import com.tce.smart.algorithm.entity.AlgorithmFaceDetectRecord;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.FileUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author wxjason
 */
@Component
@AllArgsConstructor
public class AlgorithmFaceDetectRecordDetailWrapper extends BaseWrapper<AlgorithmFaceDetectRecord, AlgorithmFaceDetectRecordDetailDTO> {

	//private final RemoteFileService remoteImageService;

	@Override
	protected AlgorithmFaceDetectRecordDetailDTO warp(AlgorithmFaceDetectRecord model) throws IOException {
		// AlgorithmFaceDetectRecordDetailDTO dto = BeanUtils.transform(AlgorithmFaceDetectRecordDetailDTO.class, model);
		// dto.setAlgorithmName(AlgorithmTypeEnum.name(model.getAlgorithmType()));
		// dto.setCreateTime(DateUtils.convert(model.getCreateTime()));
		// dto.setFaceDetectTypeName(FaceDetectTypeEnum.name(model.getFaceDetectType()));
		// if (StringUtils.isNotBlank(model.getRequestImageId())) {
		// 	dto.setRequestImageBase64(FileUtils.imageBase64(remoteImageService.getById(model.getRequestImageId(), SecurityConstants.FROM_IN).getData()));
		// }
		// if (FaceDetectTypeEnum.FACE_CUT.equals(FaceDetectTypeEnum.faceDetectType(model.getFaceDetectType()))) {
		// 	dto.setFaceData(FileUtils.imageBase64(remoteImageService.getById(model.getRequestImageId(), SecurityConstants.FROM_IN).getData()));
		// }
		// return dto;
		return null;
	}
}
