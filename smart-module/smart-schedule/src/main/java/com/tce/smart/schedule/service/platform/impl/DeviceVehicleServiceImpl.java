package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.CarCardDTO;
import com.tce.smart.platform.api.dto.CarCardDelDTO;
import com.tce.smart.platform.api.dto.DeviceDataQueryDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceTaskMapper;
import com.tce.smart.schedule.service.platform.IDeviceVehicleService;
import com.tce.smart.tool.constant.DeviceConstants;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备车辆关联
 *
 * @author 王艳勇
 * @date 2019-04-16 16:06:14
 */
@Slf4j
@Service
@AllArgsConstructor
public class DeviceVehicleServiceImpl implements IDeviceVehicleService {
	private final SmtDeviceTaskMapper smtDeviceTaskMapper;

	private final SmtDeviceMapper smtDeviceMapper;

	//private final RemoteDeviceDataService remoteDeviceDataService;

	//private final RemoteCarCardService remoteCarCardService;

	private final RemoteDispatcherService remoteDispatcherService;

	@Override
	public void syncVehicle() {
		// 注释的原因： v1/barrier/access/query 开始 胡跃军 规划了，但后面去掉未实现
		/*List<SmtDevice> list =
				smtDeviceMapper.selectList(Wrappers.<SmtDevice>query().lambda().eq(SmtDevice::getDeviceType,
						DeviceTypeEnum.DEVICE_TYPE_3.getCode()).eq(SmtDevice::getConnectStatus,
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
				log.info("车辆数据deviceDataQueryDTO:{}", deviceDataQueryDTO);
				//CarCardDataVO carCardDataVO = remoteDeviceDataService.vehicle(deviceDataQueryDTO, SecurityConstants.FROM_IN);
				log.info("获取设备要上车辆数据:{}", carCardDataVO);
				if (ObjectUtil.isNotNull(carCardDataVO)) {
					pages = (carCardDataVO.getTotalNum() % queryNum) > 0 ?
							(carCardDataVO.getTotalNum() / queryNum) + 1 : carCardDataVO.getTotalNum() / queryNum;
					log.info("获取设备要上车辆数据页数:{}", pages);
					this.syncTask(carCardDataVO.getAccessList());
				}
				startIndex++;
			} while (startIndex > pages);
		});*/
	}

	/*private void syncTask(List<CarCardDTO> list) {
		if (CollUtil.isNotEmpty(list)) {
			list.forEach(carCardDTO -> {
				SmtDeviceTask smtDeviceTask =
						smtDeviceTaskMapper.selectOne(Wrappers.<SmtDeviceTask>query().lambda().eq(SmtDeviceTask::getDeviceCode, carCardDTO.getDeviceCode())
						.eq(SmtDeviceTask::getCardNo, carCardDTO.getCardNo()).eq(SmtDeviceTask::getStatementStatus,
										DeviceTaskConstants.DOWN_SUCCESS));
				if (ObjectUtil.isNull(smtDeviceTask)) {
					CarCardDelDTO carCardDelDTO = new CarCardDelDTO();
					BeanUtil.copyProperties(carCardDTO, carCardDelDTO);
					Result result = new Result();//remoteCarCardService.delete(carCardDelDTO, SecurityConstants
					// .FROM_IN);
					log.info("同步-卡片车辆删除，修改时间：{}，请求参数：cardNo：{}，deviceCode：{}，result.code：{}",
							DateUtil.formatDateTime(DateUtil.date()), carCardDelDTO.getCardNo(),
							carCardDelDTO.getDeviceCode(), result.getCode());
				}
			});
		}
	}*/

}
