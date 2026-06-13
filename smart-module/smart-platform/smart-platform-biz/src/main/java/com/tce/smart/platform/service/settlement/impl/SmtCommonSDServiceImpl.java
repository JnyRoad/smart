package com.tce.smart.platform.service.settlement.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.sddto.AddCommonSDReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.SearchCommonSDRecordRespDTO;
import com.tce.smart.platform.core.dto.commonsd.CommonSDRecordDTO;
import com.tce.smart.platform.core.entity.SmtCommonSD;
import com.tce.smart.platform.core.entity.SmtCommonSDRoom;
import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import com.tce.smart.platform.core.mapper.SmtCommonSDMapper;
import com.tce.smart.platform.service.settlement.SmtCommonSDRoomService;
import com.tce.smart.platform.service.settlement.SmtCommonSDService;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import com.tce.smart.tool.enums.CommonSDStatuEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: SmtCommonSDServiceImpl
 * @date: 2020/10/9 15:38
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class SmtCommonSDServiceImpl extends ServiceImpl<SmtCommonSDMapper, SmtCommonSD> implements SmtCommonSDService {

	@Resource
	private SmtDormitoryRoomService smtDormitoryRoomService;

	@Resource
	private SmtCommonSDRoomService smtCommonSDRoomService;

	@Override
	public IPage<SearchCommonSDRecordRespDTO> getCommonSDCategoryRecord(Page page,Integer categoryId) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		IPage<CommonSDRecordDTO> commonSDRecord = this.baseMapper.getCommonSDCategoryRecord(page,categoryId, parkIds);

		IPage<SearchCommonSDRecordRespDTO> resPage = new Page<>(commonSDRecord.getCurrent(),commonSDRecord.getSize(),commonSDRecord.getTotal());
		List<SearchCommonSDRecordRespDTO> respDTOList = new ArrayList<>();
		for(CommonSDRecordDTO commonSDRecordDTO : commonSDRecord.getRecords()){
			SearchCommonSDRecordRespDTO respDTO = new SearchCommonSDRecordRespDTO();
			BeanUtils.copyProperties(commonSDRecordDTO,respDTO);

			//查询房间列表
			List<SmtDormitoryRoom> roomList = smtDormitoryRoomService.list(new LambdaQueryWrapper<SmtDormitoryRoom>().in(SmtDormitoryRoom::getId, Arrays.asList(commonSDRecordDTO.getRoomIdList().split(","))));
			List<Integer> roomIds = roomList.stream().map(room -> room.getId()).collect(Collectors.toList());
			List<Integer> roomNameList = roomList.stream().map(room -> room.getRoomName()).collect(Collectors.toList());
			List<Integer> floorIds = roomList.stream().map(room->room.getFloorId()).collect(Collectors.toList());

			respDTO.setRoomIds(roomIds);
			respDTO.setRoomNameList(StringUtils.join(roomNameList,","));
			respDTO.setFloorIds(floorIds);

			respDTOList.add(respDTO);
		}

		resPage.setRecords(respDTOList);
		return resPage;
	}

	@Transactional
	@Override
	public Boolean saveCommonSDRecord(AddCommonSDReqDTO addCommonSDReqDTO) {

		//检查当前用户是否有权限添加公摊水电表
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		if(!parkIds.contains(addCommonSDReqDTO.getParkId())){
			log.error("用户({})没有权限添加园区({})的公摊水电表",SecurityUtils.getUser().getUsername(),addCommonSDReqDTO.getParkId());
			throw new TCEException("没有权限");
		}

		List<String> stringList = addCommonSDReqDTO.getRoomIds().stream().map(String::valueOf).collect(Collectors.toList());

		if(null != addCommonSDReqDTO.getId()){
			//修改
			return this.updateById(SmtCommonSD.builder()
					.id(addCommonSDReqDTO.getId())
					.dormitoryId(addCommonSDReqDTO.getDormitoryId())
					.sdName(addCommonSDReqDTO.getSdName())
					.roomList(stringList.stream().collect(Collectors.joining(",")))
					.updateTime(new Date())
					.build());
		}

		//新增
		SmtCommonSD commonSD = SmtCommonSD.builder()
				.categoryId(addCommonSDReqDTO.getCategoryId())
				.dormitoryId(addCommonSDReqDTO.getDormitoryId())
				.parkId(addCommonSDReqDTO.getParkId())
				.sdName(addCommonSDReqDTO.getSdName())
				.status(CommonSDStatuEnum.ENABLE.getCode())
				.roomList(stringList.stream().collect(Collectors.joining(",")))
				.createTime(new Date())
				.updateTime(new Date())
				.build();
		this.save(commonSD);

		//添加公摊表和房间关联数据
		List<SmtCommonSDRoom> commonSDRooms = new ArrayList<>();
		addCommonSDReqDTO.getRoomIds().forEach(item -> {
			commonSDRooms.add(SmtCommonSDRoom.builder()
					.commonId(commonSD.getId())
					.roomId(item)
					.build()
			);
		});
		if(CollectionUtil.isNotEmpty(commonSDRooms)){
			smtCommonSDRoomService.saveBatch(commonSDRooms);
		}
		return true;
	}

	@Override
	public Boolean delCommonSDRecord(Long id) {
		SmtCommonSD smtCommonSD = this.getById(id);
		if(null == smtCommonSD){
			log.error("公摊水电表记录不存在，id={}",id);
			throw new TCEException("公摊水电表记录不存在");
		}
		List<Integer> roles = SecurityUtils.getRoles();

		//逻辑删除
		return this.updateById(SmtCommonSD.builder()
				.id(id)
				.status(CommonSDStatuEnum.DISABLE.getCode())
				.build());
	}
}
