package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.CardDTO;
import com.tce.smart.platform.api.dto.CardDataDTO;
import com.tce.smart.platform.api.dto.CardDelDTO;
import com.tce.smart.platform.api.dto.DeviceDataQueryDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceTaskMapper;
import com.tce.smart.schedule.service.platform.IDevicePersonService;
import com.tce.smart.tool.constant.DeviceConstants;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备人员关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:38
 */
@Slf4j
@Service
@AllArgsConstructor
public class DevicePersonServiceImpl implements IDevicePersonService {

	private final SmtDeviceTaskMapper smtDeviceTaskMapper;

	private final SmtDeviceMapper smtDeviceMapper;

	private final RemoteDispatcherService remoteDispatcherService;

	//private final RemoteDeviceDataService remoteDeviceDataService;

	//private final RemoteCardService remoteCardService;

	@Override
	public void syncPerson() {
		// 注释原因：从设备上查询人员权限接口未实现
		/*List<SmtDevice> list =
				smtDeviceMapper.selectList(Wrappers.<SmtDevice>query().lambda().eq(SmtDevice::getDeviceType,
						DeviceTypeEnum.DEVICE_TYPE_2.getCode()).eq(SmtDevice::getConnectStatus,
						DeviceConstants.ON_LINE));
		list.forEach(smtDevice -> {
			Long startIndex = 0L;
			Long queryNum = 20L;
			Long pages = 0L;
			DeviceDataQueryDTO deviceDataQueryDTO = new DeviceDataQueryDTO();
			deviceDataQueryDTO.setDeviceCode(smtDevice.getId());
			deviceDataQueryDTO.setQueryNum(queryNum);
			deviceDataQueryDTO.setStartIndex(startIndex);
			do {
				deviceDataQueryDTO.setStartIndex(startIndex * queryNum);
				log.info("人员数据deviceDataQueryDTO:{}", deviceDataQueryDTO);
				//CardDataVO cardDataVO = remoteDeviceDataService.person(deviceDataQueryDTO, SecurityConstants.FROM_IN);
				// 园区分发
				DispatcherDTO<DeviceDataQueryDTO> dispatcherDTO = new DispatcherDTO<>();
				dispatcherDTO.setEventId(IdUtil.simpleUUID());
				dispatcherDTO.setEventType(EventEnum.PARKING_ENTRANCE_AUTH_ADD.getCode());
				dispatcherDTO.setParkId(smtDevice.getParkId());
				dispatcherDTO.setDeviceId(smtDevice.getDeviceCode());
				dispatcherDTO.setData(deviceDataQueryDTO);
				Result result = remoteDispatcherService.dispatch(dispatcherDTO,SecurityConstants.FROM_IN);
				if(result.isSuccess()){
					CardDataDTO cardData = JSONUtil.parseObj(result.getData()).toBean(CardDataDTO.class);
					log.info("获取设备要上人员数据:{}", cardData);
					if (ObjectUtil.isNotNull(cardData)) {
						pages = (cardData.getTotalNum() % queryNum) > 0 ? (cardData.getTotalNum() / queryNum) + 1 :
								cardData.getTotalNum() / queryNum;
						log.info("获取设备要上人员数据页数:{}", pages);
						this.syncTask(cardData.getAccessList());
					}
				}
				startIndex++;
			} while (startIndex > pages);

		});*/

	}

/*	private void syncTask(List<CardDTO> list) {
		if (CollUtil.isNotEmpty(list)) {
			list.forEach(cardDTO -> {
				SmtDeviceTask smtDeviceTask =
						smtDeviceTaskMapper.selectOne(Wrappers.<SmtDeviceTask>query().lambda().eq(SmtDeviceTask::getDeviceCode, cardDTO.getDeviceCode())
						.eq(SmtDeviceTask::getCardNo, cardDTO.getCardNo()).eq(SmtDeviceTask::getStatementStatus,
										DeviceTaskConstants.DOWN_SUCCESS));
				if (ObjectUtil.isNull(smtDeviceTask)) {
					CardDelDTO cardDelDTO = new CardDelDTO();
					BeanUtil.copyProperties(cardDTO, cardDelDTO);
					//Result result = remoteCardService.delete(cardDelDTO, SecurityConstants.FROM_IN);
					DispatcherDTO<CardDTO> dispatcherDTO = new DispatcherDTO<>();
					dispatcherDTO.setEventId(IdUtil.simpleUUID());
					dispatcherDTO.setEventType(EventEnum.PARKING_ENTRANCE_AUTH_ADD.getCode());
					dispatcherDTO.setParkId(cardDTO.getParkId());
					dispatcherDTO.setDeviceId(cardDTO.getDeviceCode());
					dispatcherDTO.setData(cardDTO);
					Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
					log.info("同步-卡片人员删除，修改时间：{}，请求参数：cardNo：{}，deviceCode：{}，result.code：{}",
							DateUtil.formatDateTime(DateUtil.date()), cardDelDTO.getCardNo(),
							cardDelDTO.getDeviceCode(), result.getCode());
				}
			});
		}
	}*/

}
