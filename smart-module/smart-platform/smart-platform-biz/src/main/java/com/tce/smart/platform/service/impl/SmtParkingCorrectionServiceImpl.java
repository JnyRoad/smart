package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.ParkingLotDTO;
import com.tce.smart.platform.api.dto.ParkingLotUpdateDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtParkingCorrection;
import com.tce.smart.platform.core.entity.SmtParkingCount;
import com.tce.smart.platform.core.mapper.SmtParkingCorrectionMapper;
import com.tce.smart.platform.core.mapper.SmtParkingMapper;
import com.tce.smart.platform.core.vo.ParkingCorrectionVO;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtParkingCorrectionService;
import com.tce.smart.platform.service.SmtParkingCountService;
import com.tce.smart.tool.constant.ParkingCorrectionConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 停车场车位校正表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:31:55
 */
@Service
@Slf4j
@AllArgsConstructor
public class SmtParkingCorrectionServiceImpl extends ServiceImpl<SmtParkingCorrectionMapper, SmtParkingCorrection> implements SmtParkingCorrectionService{

	private final SmtParkingCountService smtParkingCountService;
	private final SmtParkingCorrectionMapper smtParkingCorrectionMapper;
	private final SmtParkingMapper smtParkingMapper;
	//private final RemoteParkingLotService remoteParkingLotService;

	private final RemoteDispatcherService remoteDispatcherService;

	private final SmtParkService smtParkService;

	/**
	 * 校验停车场车位，并同时更新校验后的数据至车位统计表
	 *
	 * @param entity 校验车位信息
	 * @return 校验结果
	 */
	@Override
	@Transactional
	public boolean saveOrUpdateParkingCorrection(SmtParkingCorrection entity) {
		boolean result = false;
		entity.setCreateTime(LocalDateTime.now());
		SmtParkingCorrection backParkingCorrection = smtParkingCorrectionMapper.selectOne(Wrappers.<SmtParkingCorrection>query().lambda().eq(SmtParkingCorrection::getParkingId, entity.getParkingId()));
		if(backParkingCorrection == null) {
			result = smtParkingCorrectionMapper.insert(entity) > 0;
		}else {
			result = smtParkingCorrectionMapper.update(entity,Wrappers.<SmtParkingCorrection>update().lambda().eq(SmtParkingCorrection::getParkingId, entity.getParkingId())) > 0;
		}
		if(result) {
		    SmtParkingCount smtParkingCount = new SmtParkingCount();
		    smtParkingCount.setTotalCount(entity.getTotalCount());
		    smtParkingCount.setUseCount(entity.getUseCount());
		    smtParkingCount.setFreeCount(entity.getTotalCount() - entity.getUseCount());
		    smtParkingCount.setParkId(entity.getParkId());
		    smtParkingCount.setParkingId(entity.getParkingId());
		    smtParkingCount.setCreateTime(LocalDateTime.now());
		    result = smtParkingCountService.save(smtParkingCount);
		}
		ParkingLotUpdateDTO parkingLotUpdateInfo = getParkingLotUpdateInfo(entity);
		//Result backResult = remoteParkingLotService.updateParkingLot(parkingLotUpdateInfo, SecurityConstants.FROM_IN);
		// 园区分发
		DispatcherDTO<ParkingLotUpdateDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(EventEnum.PARKING_SPACE_UPDATE.getCode());
		dispatcherDTO.setParkId(entity.getParkId());
		dispatcherDTO.setDeviceId(entity.getParkingId());
		dispatcherDTO.setData(parkingLotUpdateInfo);
		Result backResult = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);

		log.debug("车位校验结果：{}",backResult.isSuccess());
		return result;
	}

	private ParkingLotUpdateDTO getParkingLotUpdateInfo(SmtParkingCorrection entity) {
		ParkingLotUpdateDTO parkingLotUpdateInfo = new ParkingLotUpdateDTO();
		parkingLotUpdateInfo.setTotalParkingSpace(entity.getTotalCount());
		parkingLotUpdateInfo.setGarageCode(entity.getParkingId());
		parkingLotUpdateInfo.setRemainParkingSpace(entity.getTotalCount() - entity.getUseCount());
		return parkingLotUpdateInfo;
	}

	@Override
	public boolean initParkingCorrection() {
		List<SmtPark> parks = smtParkService.getUnStrainedParks();
		parks.forEach(park->{
			List<String> list = smtParkingMapper.getParkingIds();
			if(CollectionUtil.isEmpty(list)){
				return;
			}
			Map<String,Object> queryParkingLotReq = new HashMap<>();
			queryParkingLotReq.put("parkingCode",list);
			DispatcherDTO<Map<String,Object>> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setEventType(EventEnum.PARKING_SPACE_SEARCH.getCode());
			dispatcherDTO.setParkId(park.getId());
			dispatcherDTO.setData(queryParkingLotReq);
			//查询停车场当前车位信息
			Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);

			log.debug("初始化车位校验结果：{}", JSONUtil.toJsonStr(result));
			if(ObjectUtil.isNotNull(result) && result.getCode() == ParkingCorrectionConstants.SUCCESS) {
				List<ParkingLotDTO.ParkingLot> parkingLotList = (List<ParkingLotDTO.ParkingLot>) result.getData();
				for (ParkingLotDTO.ParkingLot parkingLot : parkingLotList) {
					updateParkingCorrection(parkingLot);
				}
			}
		});
		return false;
	}

	private void updateParkingCorrection(ParkingLotDTO.ParkingLot parkingLot) {
		SmtParkingCorrection entity = new SmtParkingCorrection();
		entity.setTotalCount(parkingLot.getTotalParkingSpace());
		entity.setUseCount(parkingLot.getTotalParkingSpace()-parkingLot.getRemainParkingSpace());
		entity.setParkingId(parkingLot.getParkingCode());
		entity.setCreateTime(LocalDateTime.now());
		boolean result = false;
		SmtParkingCorrection backParkingCorrection = smtParkingCorrectionMapper.selectOne(Wrappers.<SmtParkingCorrection>query().lambda().eq(SmtParkingCorrection::getParkingId, entity.getParkingId()));
		if(backParkingCorrection == null) {
			result = smtParkingCorrectionMapper.insert(entity) > 0;
		}else {
			result = smtParkingCorrectionMapper.update(entity,Wrappers.<SmtParkingCorrection>update().lambda().eq(SmtParkingCorrection::getParkingId, entity.getParkingId())) > 0;
		}
		if(result) {
		    SmtParkingCount smtParkingCount = new SmtParkingCount();
		    smtParkingCount.setTotalCount(entity.getTotalCount());
		    smtParkingCount.setUseCount(entity.getUseCount());
		    smtParkingCount.setFreeCount(entity.getTotalCount() - entity.getUseCount());
		    smtParkingCount.setParkId(entity.getParkId());
		    smtParkingCount.setParkingId(entity.getParkingId());
		    smtParkingCount.setCreateTime(LocalDateTime.now());
		    result = smtParkingCountService.save(smtParkingCount);
		}
	}

	@Override
	public ParkingCorrectionVO getParkingCountInfo(Integer parkId) {
		return smtParkingCorrectionMapper.getParkingCountInfo(parkId);
	}
}
