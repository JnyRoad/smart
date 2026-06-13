package com.tce.smart.platform.service.leavecount.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.NumberConstants;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateRangeReqDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementTemplateRangeTreeRespDTO;
import com.tce.smart.platform.core.dto.DormitoryTreeDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateRange;
import com.tce.smart.platform.core.mapper.leavecount.SmtSettlementTemplateRangeMapper;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateRangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2022-06-21 11:01:50
 */
@Service
public class SmtSettlementTemplateRangeServiceImpl extends ServiceImpl<SmtSettlementTemplateRangeMapper, SmtSettlementTemplateRange> implements SmtSettlementTemplateRangeService {

	@Autowired
	private SmtParkService smtParkService;

	@Override
	public Boolean editRangeBatch(List<SettlementTemplateRangeReqDTO> reqDTO) {
		if (CollUtil.isEmpty(reqDTO)) {
			return Boolean.FALSE;
		}
		Long tempId = reqDTO.get(0).getTempId();
		Integer type = reqDTO.get(0).getType();
		//删除原范围
		this.remove(Wrappers.<SmtSettlementTemplateRange>lambdaQuery()
				.eq(SmtSettlementTemplateRange::getType, type).eq(SmtSettlementTemplateRange::getTempId, tempId));
		//新增范围
		List<SmtSettlementTemplateRange> ranges = BeanUtils.batchTransform(SmtSettlementTemplateRange.class, reqDTO);
		return this.saveBatch(ranges);
	}

	@Override
	public Boolean editRangeSingle(SettlementTemplateRangeReqDTO reqDTO) {
		if (Objects.isNull(reqDTO)) {
			return Boolean.FALSE;
		}
		//原范围
		SmtSettlementTemplateRange reRange = this.getOne(Wrappers.<SmtSettlementTemplateRange>lambdaQuery()
				.eq(SmtSettlementTemplateRange::getType, reqDTO.getType())
				.eq(SmtSettlementTemplateRange::getValue, reqDTO.getValue()));
		if(Objects.nonNull(reRange)) {
			reRange.setTempId(reqDTO.getTempId());
			return this.updateById(reRange);
		}
		//新增范围
		SmtSettlementTemplateRange ranges = BeanUtils.transform(SmtSettlementTemplateRange.class, reqDTO);
		return this.save(ranges);
	}

	@Override
	public List<Long> getByRoomId(Integer roomId) {
		List<SmtSettlementTemplateRange> ranges = this.list(Wrappers.<SmtSettlementTemplateRange>lambdaQuery()
				.eq(SmtSettlementTemplateRange::getType, NumberConstants.ONE)
				.eq(SmtSettlementTemplateRange::getValue, roomId.toString()));
		if (CollUtil.isNotEmpty(ranges)) {
			return ranges.stream().map(SmtSettlementTemplateRange::getTempId).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public List<SettlementTemplateRangeTreeRespDTO> getRangeTree(Integer parkId, Long tempId, String type) {
		List<Integer> checkedRange = new ArrayList<>();
		List<Integer> disabledRange = new ArrayList<>();
		List<DormitoryTreeDTO> roomTree = smtParkService.roomTree(parkId);
		List<SmtSettlementTemplateRange> checkedList = this.list(Wrappers.<SmtSettlementTemplateRange>lambdaQuery()
				.eq(SmtSettlementTemplateRange::getParkId, parkId)
				.eq(SmtSettlementTemplateRange::getTempId, tempId)
				.eq(SmtSettlementTemplateRange::getType, type));
		if (CollUtil.isNotEmpty(checkedList)) {
			checkedRange = checkedList.stream().filter(e -> Objects.nonNull(e.getValue()))
					.map(e -> Integer.parseInt(e.getValue())).collect(Collectors.toList());
		}
		List<SmtSettlementTemplateRange> disabledList = this.list(Wrappers.<SmtSettlementTemplateRange>lambdaQuery()
				.eq(SmtSettlementTemplateRange::getParkId, parkId)
				.ne(SmtSettlementTemplateRange::getTempId, tempId)
				.eq(SmtSettlementTemplateRange::getType, type));
		if (CollUtil.isNotEmpty(disabledList)) {
			disabledRange = disabledList.stream().filter(e -> Objects.nonNull(e.getValue()))
					.map(e -> Integer.parseInt(e.getValue())).collect(Collectors.toList());
		}
		if (CollUtil.isNotEmpty(roomTree)) {
			return buildRangeTree(roomTree, checkedRange, disabledRange);
		}
		return Collections.emptyList();
	}

	private List<SettlementTemplateRangeTreeRespDTO> buildRangeTree(
								List<DormitoryTreeDTO> roomTree, List<Integer> checkedRange,
								List<Integer> disabledRange) {
		List<SettlementTemplateRangeTreeRespDTO> rangeTree = new ArrayList<>();
		for (DormitoryTreeDTO room : roomTree) {
			SettlementTemplateRangeTreeRespDTO range = new SettlementTemplateRangeTreeRespDTO();
			range.setId(room.getId());
			range.setLabel(room.getLabel());
            range.setChecked(checkedRange.contains(room.getId()));
            range.setDisabled(disabledRange.contains(room.getId()));
			if (CollUtil.isNotEmpty(room.getChildren())) {
				range.setChildren(buildRangeTree(room.getChildren(), checkedRange, disabledRange));
			}
			rangeTree.add(range);
		}
		return rangeTree;
	}
}
