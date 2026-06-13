package com.tce.smart.algorithm.wrapper;

import cn.hutool.json.JSONUtil;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.enums.CardTypeEnum;
import com.tce.smart.algorithm.api.dto.resp.AlgorithmOcrRecordDetailDTO;
import com.tce.smart.algorithm.api.dto.resp.ItemDTO;
import com.tce.smart.algorithm.entity.AlgorithmOcrRecord;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.FileUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
//import com.tce.smart.file.api.feign.RemoteFileService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author wxjason
 */
@Component
@AllArgsConstructor
public class AlgorithmOcrRecordDetailWrapper extends BaseWrapper<AlgorithmOcrRecord, AlgorithmOcrRecordDetailDTO> {

	//private final RemoteFileService remoteImageService;

	@Override
	protected AlgorithmOcrRecordDetailDTO warp(AlgorithmOcrRecord model) throws IOException {
		AlgorithmOcrRecordDetailDTO dto = BeanUtils.transform(AlgorithmOcrRecordDetailDTO.class, model);
		dto.setAlgorithmName(AlgorithmTypeEnum.name(model.getAlgorithmType()));
		dto.setCardTypeName(CardTypeEnum.desc(model.getCardType()));
		dto.setCreateTime(DateUtils.convert(model.getCreateTime()));
		if (StringUtils.isNotBlank(model.getRequestImageId())) {
			//dto.setRequestImageBase64(FileUtils.imageBase64(remoteImageService.getById(model.getRequestImageId(), SecurityConstants.FROM_IN).getData()));
		}
		if (StringUtils.isNotBlank(model.getHandleImageId())) {
			//dto.setHandleImageBase64(FileUtils.imageBase64(remoteImageService.getById(model.getHandleImageId(), SecurityConstants.FROM_IN).getData()));
		}
		if (StringUtils.isNotBlank(model.getHeadImageId())) {
			//dto.setHeadImageBase64(FileUtils.imageBase64(remoteImageService.getById(model.getHeadImageId(), SecurityConstants.FROM_IN).getData()));
		}
		List<ItemDTO> distinguishWords = JSONUtil.toList(JSONUtil.parseArray(model.getDistinguishWords()), ItemDTO.class);
		dto.setDistinguishWords(distinguishWords);
		return dto;
	}
}
