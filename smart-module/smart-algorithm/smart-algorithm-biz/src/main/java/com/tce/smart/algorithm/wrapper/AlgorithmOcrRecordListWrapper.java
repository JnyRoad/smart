package com.tce.smart.algorithm.wrapper;

import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.enums.CardTypeEnum;
import com.tce.smart.algorithm.api.dto.resp.AlgorithmOcrRecordListDTO;
import com.tce.smart.algorithm.entity.AlgorithmOcrRecord;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.stereotype.Component;

/**
 * @author wxjason
 */
@Component
public class AlgorithmOcrRecordListWrapper extends BaseWrapper<AlgorithmOcrRecord, AlgorithmOcrRecordListDTO> {

	@Override
	protected AlgorithmOcrRecordListDTO warp(AlgorithmOcrRecord model) {
		AlgorithmOcrRecordListDTO dto = BeanUtils.transform(AlgorithmOcrRecordListDTO.class, model);
		dto.setAlgorithmName(AlgorithmTypeEnum.name(model.getAlgorithmType()));
		dto.setCardTypeName(CardTypeEnum.desc(model.getCardType()));
		dto.setCreateTime(DateUtils.convert(model.getCreateTime()));
		return dto;
	}
}
