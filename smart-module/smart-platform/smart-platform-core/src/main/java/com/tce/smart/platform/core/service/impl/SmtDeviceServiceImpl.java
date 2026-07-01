package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.api.dto.req.DeviceAuthPersonReqDTO;
import com.tce.smart.platform.api.dto.req.DeviceAuthVehicleReqDTO;
import com.tce.smart.platform.api.dto.resp.DeviceAuthPersonRespDTO;
import com.tce.smart.platform.api.dto.resp.DeviceAuthVehicleRespDTO;
import com.tce.smart.platform.core.dto.DeviceDTO;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.enums.DeviceSyncEnum;
import com.tce.smart.platform.core.mapper.SmtDeviceAreaMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtParkMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.model.AreaTree;
import com.tce.smart.platform.core.service.SmtDevicePersonService;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtDeviceVehicleService;
import com.tce.smart.platform.core.vo.DeviceVO;
import com.tce.smart.tool.constant.DeviceConstants;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 设备信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Service
@Slf4j
@AllArgsConstructor
public class SmtDeviceServiceImpl extends ServiceImpl<SmtDeviceMapper, SmtDevice> implements SmtDeviceService {
	private final SmtParkMapper smtParkMapper;

	private final SmtDeviceTaskService deviceTaskService;

	private final SmtDevicePersonService devicePersonService;

	private final SmtDeviceVehicleService deviceVehicleService;

	private final SmtStaffMapper smtStaffMapper;

	private final SmtDeviceAreaMapper smtDeviceAreaMapper;

	/**
	 * 查询设备信息
	 *
	 * @param page      分页对象
	 * @param deviceDTO 查询条件
	 * @return 返回设备集合
	 */
	@Override
	public IPage<DeviceVO> getDevice(Page page, DeviceDTO deviceDTO) {
		return this.baseMapper.getDevice(page, deviceDTO);
	}

	/**
	 * 更新设备状态信息
	 *
	 * @param entity 设备信息
	 * @return 返回结果
	 */
	@Override
	public Boolean updateDeviceStatus(SmtDevice entity) {
		log.info("设备状态更新请求参数：{}", entity);
		if (StrUtil.isBlank(entity.getId())) {
			new TCEException("设备ID不可为空");
		}
		if (!entity.getConnectStatus().equals(DeviceConstants.UNCONNECTED)
				&& !entity.getConnectStatus().equals(DeviceConstants.OFF_LINE)
				&& !entity.getConnectStatus().equals(DeviceConstants.ON_LINE)) {
			new TCEException("设备接通状态值：0-未连接；1-离线；2-在线；");
		}
		return this.updateById(entity);
	}

//	@Override
//	public Boolean updateDeviceStatus(BridgeDTO<String> bridgeDTO) {
//		if(StrUtil.isNotBlank(bridgeDTO.getData())){
//			DeviceStatusNoticeVO deviceStatusNoticeVO = JSONUtil.toBean(bridgeDTO.getData(),DeviceStatusNoticeVO.class);
//			log.info("设备状态更新请求参数：{}", deviceStatusNoticeVO);
//			if (StrUtil.isBlank(deviceStatusNoticeVO.getDeviceCode())) {
//				log.info("设备ID为空");
//				return false;
//			}
//			if (!deviceStatusNoticeVO.getDeviceStatus().equals(DeviceConstants.UNCONNECTED)
//					&& !deviceStatusNoticeVO.getDeviceStatus().equals(DeviceConstants.OFF_LINE)
//					&& !deviceStatusNoticeVO.getDeviceStatus().equals(DeviceConstants.ON_LINE)) {
//				log.info("设备接通状态值异常：id:{};statementStatus:{}",deviceStatusNoticeVO.getDeviceCode(),deviceStatusNoticeVO.getDeviceStatus());
//				return false;
//			}
//			SmtDevice smtDevice = new SmtDevice();
//			smtDevice.setConnectStatus(deviceStatusNoticeVO.getDeviceStatus());
//			smtDevice.setId(deviceStatusNoticeVO.getDeviceCode());
//			return this.updateById(smtDevice);
//		}
//		return false;
//	}

	/**
	 * 警报记录补全设备信息
	 *
	 * @param entity 警报记录信息
	 * @return 返回结果
	 */
	@Override
	public void deviceHandle(SmtAlarmRecord entity) {
		SmtDevice device = this.getById(entity.getDeviceId());
		entity.setDeviceName(device.getDeviceName());
		entity.setDeviceType(device.getDeviceType());
	}

	/**
	 * 根据区域子节点获取设备信息
	 *
	 * @param id 区域ID
	 * @return 设备集合
	 */
	@Override
	public List<SmtDevice> getByAreaId(Integer id) {
		return this.baseMapper.getByAreaId(id);
	}

	@Override
	public List<AreaTree> getTree(List<Integer> parkIds) {
		List<AreaTree> list = this.baseMapper.getPark(parkIds);
		for (AreaTree areaTree : list) {
			areaTree.setChildren(this.children(areaTree.getValue(), 0));
		}
		return list;
	}

	private List<AreaTree> children(Integer parkId, Integer pId) {
		List<AreaTree> list = this.baseMapper.getArea(parkId, pId);
		for (AreaTree areaTree : list) {
			areaTree.setChildren(this.baseMapper.getArea(null, areaTree.getValue()));
		}
		return list;
	}

	@Override
	public DeviceVO getDeviceById(String id) {
		return this.baseMapper.getDeviceDetail(id);
	}

	@Override
	public List<SmtDevice> selectDeviceByAuthId(Integer code) {
		return this.baseMapper.selectDeviceByAuthId(code);
	}

	@Override
	public List<SmtPark> getParkList(List<Integer> parkIds) {
		return smtParkMapper.selectList(Wrappers.<SmtPark>query().lambda().in(CollUtil.isNotEmpty(parkIds),
				SmtPark::getId, parkIds).orderByAsc(SmtPark::getParkName));
	}

	@Override
	public int deleteDeviceArea(String deviceId) {
		return this.baseMapper.deleteDeviceArea(deviceId);
	}

	@Override
	public List<String> getDeviceIds(Integer parkId) {
		return this.baseMapper.getDeviceIds(parkId);
	}

	/**
	 * 根据设备ID删除权限
	 *
	 * @param deviceId 设备ID
	 * @return 是否成功
	 */
	@Override
	public Boolean clearAuth(String deviceId) {
		DeviceVO deviceVO = getDeviceById(deviceId);
		if (Objects.isNull(deviceVO)) {
			throw new SmartException("设备不存在");
		}
		List<String> deviceIds = new ArrayList<>(1);
		deviceIds.add(deviceId);
		if (DeviceTypeEnum.DEVICE_TYPE_1.getCode().equals(deviceVO.getDeviceType())
				|| DeviceTypeEnum.DEVICE_TYPE_2.getCode().equals(deviceVO.getDeviceType())) {
			List<DeviceAuthPersonRespDTO> authPersonDTOS = this.getPersonAuth(deviceId);
			authPersonDTOS.forEach(item -> deviceTaskService.addDeviceDelTaskImmed(deviceIds, item.getCardNo(), item.getStaffName(),
					DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskActionEnum.DELAY_DEL.getCode(),
					SmtVisitorEnum.CAR_CARD_TYPE_1, DeviceTaskConstants.CARD, item.getImageId()));
		} else if (DeviceTypeEnum.DEVICE_TYPE_3.getCode().equals(deviceVO.getDeviceType())) {
			List<DeviceAuthVehicleRespDTO> authVehicleRespDTOS = this.getCarAuth(deviceId);
			authVehicleRespDTOS.forEach(item -> deviceTaskService.addDeviceDelTaskImmed(deviceIds, item.getCardNo(),
					item.getPlate(), DeviceTaskConstants.CAR_STAFF, DeviceTaskActionEnum.DELAY_DEL.getCode(),
					SmtVisitorEnum.CAR_CARD_TYPE_1, DeviceTaskConstants.CAR,null));
		}
		return true;
	}

	/**
	 * 根据设备ID重新下发权限
	 *
	 * @param deviceId 设备ID
	 * @return 是否成功
	 */
	@Override
	public Boolean repeatAuth(String deviceId) {
		DeviceVO deviceVO = getDeviceById(deviceId);
		if (Objects.isNull(deviceVO)) {
			throw new SmartException("设备不存在");
		}
		List<String> deviceIds = new ArrayList<>(1);
		deviceIds.add(deviceId);
		//是否普通设备 ISC不用删除后下发
		Boolean flag = DeviceSyncEnum.NO.getCode().equals(deviceVO.getIsSync());
		if (DeviceTypeEnum.DEVICE_TYPE_1.getCode().equals(deviceVO.getDeviceType())
				|| DeviceTypeEnum.DEVICE_TYPE_2.getCode().equals(deviceVO.getDeviceType())) {
			List<DeviceAuthPersonRespDTO> authPersonDTOS = this.getPersonAuth(deviceId);
			//先删除
			authPersonDTOS.forEach(item -> {
				if(flag) { // 如果是普通设备先生成权限删除任务
					deviceTaskService.addDeviceDelTaskImmed(deviceIds, item.getCardNo(), item.getStaffName(),
							DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskActionEnum.DELAY_DEL.getCode(),
							SmtVisitorEnum.CAR_CARD_TYPE_1, DeviceTaskConstants.CARD, item.getImageId());
				}
				SmtStaff staff = smtStaffMapper.selectOne(Wrappers.<SmtStaff>lambdaQuery().eq(SmtStaff::getBadge, item.getBadge()));
				// 判断人员是否为空
				if(Objects.isNull(staff)) {
					return;
				}
				// 判断是否离职
				if(StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(staff.getStatus())) {
					return;
				}
				DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
				deviceTaskVO.setAction(DeviceTaskActionEnum.DELAY_DOWN.getCode());
				deviceTaskVO.setServiceType(item.getServiceType());
				deviceTaskVO.setCardNo( item.getCardNo());
				deviceTaskVO.setDeviceCode(deviceId);
				deviceTaskVO.setGeneral(item.getGeneral());
				deviceTaskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_1.getType());
				deviceTaskVO.setImageId(item.getImageId());
				deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
				deviceTaskVO.setStartTime(item.getStartTime().getTime()/1000);
				deviceTaskVO.setOverTime(item.getOverTime().getTime()/1000);
				deviceTaskService.saveTask(deviceTaskVO);
			});

		} else if (DeviceTypeEnum.DEVICE_TYPE_3.getCode().equals(deviceVO.getDeviceType())) {
			List<DeviceAuthVehicleRespDTO> authVehicleRespDTOS = this.getCarAuth(deviceId);
			authVehicleRespDTOS.forEach(item -> {
				if(flag) {
					deviceTaskService.addDeviceDelTaskImmed(deviceIds, item.getCardNo(),
							item.getPlate(), DeviceTaskConstants.CAR_STAFF, DeviceTaskActionEnum.DELAY_DEL.getCode(),
							SmtVisitorEnum.CAR_CARD_TYPE_1, DeviceTaskConstants.CAR,null);
				}
				DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
				deviceTaskVO.setAction(DeviceTaskActionEnum.DELAY_DOWN.getCode());
				deviceTaskVO.setServiceType(item.getServiceType());
				deviceTaskVO.setCardNo(item.getCardNo());
				deviceTaskVO.setDeviceCode(item.getDeviceCode());
				deviceTaskVO.setGeneral(item.getPlate());
				deviceTaskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_1.getType());
				deviceTaskVO.setDeviceType(DeviceTaskConstants.CAR);
				deviceTaskVO.setStartTime(item.getStartTime().getTime() / 1000);
				deviceTaskVO.setOverTime(item.getOverTime().getTime() / 1000);
				deviceTaskService.saveTask(deviceTaskVO);
			});

		}
		return true;
	}

	/**
	 * 删除设备
	 *
	 * @param deviceId 设备ID
	 * @return 是否成功
	 */
	@Transactional
    @Override
    public Boolean deleteDevice(String deviceId) {
		// 删除设备
		this.removeById(deviceId);
		// 删除设备区域关联
		smtDeviceAreaMapper.delete(new LambdaUpdateWrapper<SmtDeviceArea>()
				.eq(SmtDeviceArea::getDeviceId,deviceId)
		);
        return Boolean.TRUE;
    }

	/**
	 * 获取人员授权信息
	 * @param deviceId
	 * @return 授权人员信息
	 */
    private List<DeviceAuthPersonRespDTO> getPersonAuth(String deviceId) {
		// 闸机或门禁
		long current = 0;
		long size = 100;
		Page page = new Page(current, size);
		DeviceAuthPersonReqDTO reqDTO = new DeviceAuthPersonReqDTO();
		reqDTO.setDeviceId(deviceId);
		List<DeviceAuthPersonRespDTO> authPersonDTOS = new ArrayList<>();
		do {
			current++;
			page.setCurrent(current);
			IPage<DeviceAuthPersonRespDTO> authPersonPage = devicePersonService.getDeviceAuthPerson(page, reqDTO);
			if (CollectionUtil.isEmpty(authPersonPage.getRecords())) {
				break;
			}
			authPersonDTOS.addAll(authPersonPage.getRecords());
		} while (page.hasNext());
		log.info("授权人员数量：{}", authPersonDTOS.size());
		return authPersonDTOS;
	}

	private List<DeviceAuthVehicleRespDTO> getCarAuth(String deviceId) {
		// 道闸
		long current = 0;
		long size = 100;
		Page page = new Page(current, size);
		DeviceAuthVehicleReqDTO reqDTO = new DeviceAuthVehicleReqDTO();
		List<DeviceAuthVehicleRespDTO> authVehicleRespDTOS = new ArrayList<>();
		do {
			current++;
			page.setCurrent(current);
			IPage<DeviceAuthVehicleRespDTO> authVehiclePage = deviceVehicleService.getDeviceAuthVehicle(page, reqDTO);
			if (CollectionUtil.isEmpty(authVehiclePage.getRecords())) {
				break;
			}
			authVehicleRespDTOS.addAll(authVehiclePage.getRecords());
		} while (page.hasNext());
		log.info("授权车辆数量：{}", authVehicleRespDTOS.size());
		return authVehicleRespDTOS;
	}
}