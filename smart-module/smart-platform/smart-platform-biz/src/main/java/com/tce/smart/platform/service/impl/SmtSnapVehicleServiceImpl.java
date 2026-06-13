package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SnapVehicleConstants;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkParkingRespDTO;
import com.tce.smart.platform.core.dto.AddSnapVehicleDTO;
import com.tce.smart.platform.core.dto.SnapVehicleAccessDTO;
import com.tce.smart.platform.core.dto.SnapVehicleCountDTO;
import com.tce.smart.platform.core.dto.SnapVehicleDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtSnapVehicleMapper;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.vo.SearchOneSnapVehicleVO;
import com.tce.smart.platform.core.vo.SearchSmtSnapVehicleVO;
import com.tce.smart.platform.core.vo.SnapVehicleCountVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceVehicleService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.SnapVehicleAuthConstants;
import com.tce.smart.tool.enums.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 车辆抓拍记录表
 *
 * @author 王艳勇
 * @date 2019-04-13 18:18:20
 */
@Service
@Slf4j
@AllArgsConstructor
public class SmtSnapVehicleServiceImpl extends ServiceImpl<SmtSnapVehicleMapper, SmtSnapVehicle> implements SmtSnapVehicleService {

	@Autowired
	private SmtParkingCorrectionService smtParkingCorrectionService;
	@Autowired
	private SmtParkingCountService smtParkingCountService;
	@Autowired
	private SmtLogisticsAppointmentService smtLogisticsAppointmentService;
	@Autowired
	private SmtDeviceAreaService smtDeviceAreaService;
	@Autowired
	private SmtSnapVehicleMapper smtSnapVehicleMapper;
	@Autowired
	private SmtVehicleStaffService smtVehicleStaffService;
	@Autowired
	private SmtDeviceService smtDeviceService;
	@Autowired
	private SmtVisitorService smtVisitorService;
	@Autowired
	private ImageService smtImageService;
	@Autowired
	private SmtParkService  smtParkService;
	@Autowired
	private SmtAdmittanceApplyService smtAdmittanceApplyService;

	/**
	 * 保存抓拍车辆信息并更新车位使用情况
	 *
	 * @param entity 抓拍车辆信息
	 * @return 校验结果
	 */
	@Transactional
	@Override
	public boolean saveSnapVehicle(AddSnapVehicleDTO entity) {
		//log.info("车辆抓拍记录请求参数：{}",JSONUtil.toJsonPrettyStr(entity));
		SmtDevice smtDevice = smtDeviceService.getById(entity.getDeviceId());
		if(ObjectUtil.isNotNull(smtDevice) && ObjectUtil.isNotNull(smtDevice.getEventType())) {
			entity.setEventType(smtDevice.getEventType());
		}
		entity.setAuthority(StringUtils.isEmpty(entity.getCardNo()) ? SnapVehicleAuthConstants.NO : SnapVehicleAuthConstants.YES);
		//设备信息处理
		smtDeviceAreaService.areaHandle(entity);
		//物流车信息处理
		smtLogisticsAppointmentService.logisticsAppointmentHandle(entity);
		//员工车辆信息处理
		smtVehicleStaffService.vehicleStaffHandle(entity);
		//访客车辆信息处理
		smtVisitorService.visitorSnapHandle(entity, this);
		//入厂申请车辆信息处理
		smtAdmittanceApplyService.admittanceSnapVehicleHandle(entity);
		entity.setCreateTime(LocalDateTime.now());
		if(ObjectUtil.isNull(entity.getDriverType())){
			entity.setDriverType(SnapVehicleConstants.OTHER_MASTER);
		}
		boolean result = false;
		//保存抓拍的车辆信息
		result = this.save(entity);
		if(result) {
			// 更新车位信息
			result = this.updateParking(entity);
		}
		return result;
	}
//	public boolean saveSnapVehicle(BridgeDTO<String> bridgeDTO) {
//		log.info("车辆抓拍记录请求参数：{}",JSONUtil.toJsonPrettyStr(bridgeDTO));
//		VehicleSnapNoticeVO vehicleSnapNoticeVO = null;
//		if(StrUtil.isNotBlank(bridgeDTO.getData())){
//			vehicleSnapNoticeVO = JSONUtil.toBean(bridgeDTO.getData(),VehicleSnapNoticeVO.class);
//
//			SmtDevice smtDevice = smtDeviceService.getById(vehicleSnapNoticeVO.getDeviceCode());
//			String snapPhotoId = null;
//			if(StringUtils.isNotBlank(vehicleSnapNoticeVO.getSnapPhoto())){
//				// 暂时存储图片获取ID
////				snapPhotoId = blobService.saveBlobCompress(vehicleSnapNoticeVO.getSnapPhoto());
//			}
//			// 为了避免其他调用方法的修改，此处依然使用 AddSnapVehicleDTO 封装请求参数
//			AddSnapVehicleDTO entity = new AddSnapVehicleDTO();
//			entity.setTotalCount(vehicleSnapNoticeVO.getTotalParkingLot());
//			entity.setFreeCount(vehicleSnapNoticeVO.getRemainParkingLot());
//			entity.setCardNo(vehicleSnapNoticeVO.getCardNo());
//			entity.setVehicleColor(vehicleSnapNoticeVO.getVehicleColor());
//			entity.setVehicleBrand(vehicleSnapNoticeVO.getVehicleBrand());
//			entity.setVehiclePlate(vehicleSnapNoticeVO.getVehicleLicence());
//			entity.setChannelNo(vehicleSnapNoticeVO.getChnNo());
//			entity.setDeviceId(vehicleSnapNoticeVO.getDeviceCode());
//			Long eventTime = vehicleSnapNoticeVO.getEventTime();
//			Date snapTime = new Date();
//			if(null!=eventTime){
//				snapTime.setTime(eventTime *1000);
//			}
//			entity.setSnapTime(snapTime);
//			entity.setSnapPhotoId(snapPhotoId);
//			entity.setLetPass(vehicleSnapNoticeVO.getLetPass());
//
//
//
//			if(ObjectUtil.isNotNull(smtDevice) && ObjectUtil.isNotNull(smtDevice.getEventType())) {
//				entity.setEventType(smtDevice.getEventType());
//			}
//
//			entity.setAuthority(StringUtils.isEmpty(vehicleSnapNoticeVO.getCardNo()) ? SnapVehicleAuthConstants.NO : SnapVehicleAuthConstants.YES);
//			//设备信息处理
//			smtDeviceAreaService.areaHandle(entity);
//			//物流车信息处理
//			smtLogisticsAppointmentService.logisticsAppointmentHandle(entity);
//			//员工车辆信息处理
//			smtVehicleStaffService.vehicleStaffHandle(entity);
//			//访客车辆信息处理
//			smtVisitorService.visitorSnapHandle(entity);
//			entity.setCreateTime(DateUtil.date());
//			if(ObjectUtil.isNull(entity.getDriverType())){
//				entity.setDriverType(SnapVehicleConstants.OTHER_MASTER);
//			}
//			boolean result = false;
//			//保存抓拍的车辆信息
//			result = this.save(entity);
//			if(result) {
//				// 更新车位信息
//				result = this.parking(entity);
//			}
//			return result;
//
//		}
//	return false;
//
//	}

	@Override
	public SnapVehicleCountVO getVehicleCountBySnapTime(Integer parkId) {
		Integer[] inTotal = new Integer[] {0,0,0,0,0,0};
		Integer[] outTotal = new Integer[] {0,0,0,0,0,0};
		List<SnapVehicleCountDTO> list = this.baseMapper.getVehicleCountBySnapTime(parkId);
		list.forEach(v->{
			if(v.getEventType().equals(VehicleEventTypEnum.IN.getCode())) {
				inTotal[v.getOrderIndex()] = v.getTotal();
			}else if(v.getEventType().equals(VehicleEventTypEnum.OUT.getCode())) {
				outTotal[v.getOrderIndex()] = v.getTotal();
			}
		});
		SnapVehicleCountVO snapVehicleCountVO = new SnapVehicleCountVO();
		snapVehicleCountVO.setIndoorNums(inTotal);
		snapVehicleCountVO.setOutdoorNums(outTotal);
		return snapVehicleCountVO;
	}

	/**
	 * 更新停车场信息
	 *
	 * @param entity 抓拍车辆信息
	 * @return 返回更新结果
	 */
	@Transactional
	public boolean updateParking(AddSnapVehicleDTO entity) {
		//查询抓拍的设备信息
		SmtDevice device = smtDeviceService.getById(entity.getDeviceId());
		if(ObjectUtil.isNotNull(device) && ObjectUtil.isNotNull(device.getDeviceSubtype())){
			//抓拍设备关联了停车场
			//查询停车场信息
			int count = 0;
			if(VehicleEventTypEnum.IN.getCode().equals(device.getEventType())){
				//进
				count -= 1;
			} else if(VehicleEventTypEnum.OUT.getCode().equals(device.getEventType())){
				//出
				count += 1;
			}

			//修改停车场矫正表
			SmtParkingCorrection backParkingCorrection = smtParkingCorrectionService.getOne(Wrappers.<SmtParkingCorrection>query().lambda().eq(SmtParkingCorrection::getParkingId, device.getDeviceSubtype()));
			if(null != backParkingCorrection) {
				backParkingCorrection.setUseCount(backParkingCorrection.getUseCount() - count);
				smtParkingCorrectionService.updateById(backParkingCorrection);

				//添加新的车辆统计信息
				SmtParkingCount smtParkingCount = new SmtParkingCount();
				smtParkingCount.setTotalCount(backParkingCorrection.getTotalCount());
				smtParkingCount.setUseCount(backParkingCorrection.getUseCount());
				smtParkingCount.setFreeCount(backParkingCorrection.getTotalCount() - backParkingCorrection.getUseCount());
				smtParkingCount.setParkId(device.getParkId());
				smtParkingCount.setParkingId(device.getDeviceSubtype());
				smtParkingCount.setCreateTime(LocalDateTime.now());
				smtParkingCountService.save(smtParkingCount);
			}
		}
		return true;
	}

	/**
	 * 根据园区ID更新或保存停车场车位信息
	 *
	 * @param entity 抓拍车辆信息
	 * @return 返回更新结果
	 */
	private boolean parking(AddSnapVehicleDTO entity) {
		if(null == entity.getTotalCount() || null == entity.getFreeCount()){
			//TODO 这里暂时先这样处理
			return true;
		}
		SmtDevice device = smtDeviceService.getById(entity.getDeviceId());
		boolean result = false;
		SmtParkingCorrection parkingCorrection = new SmtParkingCorrection();
		parkingCorrection.setTotalCount(entity.getTotalCount());
		parkingCorrection.setUseCount(entity.getTotalCount() - entity.getFreeCount());
		parkingCorrection.setCreateTime(LocalDateTime.now());
		if(ObjectUtil.isNotNull(device) && ObjectUtil.isNotNull(device.getDeviceSubtype())) {
			parkingCorrection.setParkingId(device.getDeviceSubtype());
			parkingCorrection.setParkId(device.getParkId());
			SmtParkingCorrection backParkingCorrection = smtParkingCorrectionService.getOne(Wrappers.<SmtParkingCorrection>query().lambda().eq(SmtParkingCorrection::getParkingId, device.getDeviceSubtype()));
			if(backParkingCorrection == null) {
				result = smtParkingCorrectionService.save(parkingCorrection);
			}else {
				result = smtParkingCorrectionService.update(parkingCorrection,Wrappers.<SmtParkingCorrection>update().lambda().eq(SmtParkingCorrection::getParkingId, device.getDeviceSubtype()));
			}
		}
		if(result) {
		    SmtParkingCount smtParkingCount = new SmtParkingCount();
		    smtParkingCount.setTotalCount(entity.getTotalCount());
		    smtParkingCount.setUseCount(entity.getTotalCount() - entity.getFreeCount());
		    smtParkingCount.setFreeCount(entity.getFreeCount());
		    smtParkingCount.setParkId(device.getParkId());
		    smtParkingCount.setParkingId(device.getDeviceSubtype());
		    smtParkingCount.setCreateTime(LocalDateTime.now());
		    result = smtParkingCountService.save(smtParkingCount);
		}
		return result;
	}

	/**
	 * 查询车辆记录信息
	 * @param page 分页对象
	 * @param snapVehicleDTO 查询条件
	 * @return 返回车辆集合
	 */
	@Override
	public IPage getSnapVehicle(Page page,SnapVehicleDTO snapVehicleDTO) {
		if(snapVehicleDTO.getAllFlag().equals(SnapVehicleConstants.ALL)) {
			return smtSnapVehicleMapper.getSnapVehicle(page, snapVehicleDTO);
		}else{
			snapVehicleDTO.setEventType(SnapVehicleConstants.DIRECTION_IN);
			return smtSnapVehicleMapper.getCurrentVehicle(page, snapVehicleDTO);
		}
	}

	@Override
	public SmtSnapVehicle getSnapVehicleDetail(Long id) {
		SmtSnapVehicle snapVehicle = this.getById(id);
//		snapVehicle.setSnapPhotoId(remoteBlobService.getBlob(snapVehicle.getSnapPhotoId(), SecurityConstants.FROM_IN).getData());
		return snapVehicle;
	}
	/**
	 * 查询车辆出入信息
	 */
	@Override
	public IPage<SearchSmtSnapVehicleVO> searchVehicleAccess(Page page,SnapVehicleAccessDTO snapVehicleAccessDto,String snapTime) {

		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if(StringUtils.isNotBlank(snapTime)) {
			snapVehicleAccessDto.setStartTime(snapTime.split(",")[0]);
			snapVehicleAccessDto.setEndTime(snapTime.split(",")[1]);
		}
		 //判断是否为外部人员
			if(snapVehicleAccessDto.getVehicleAscription().equals(DeviceTaskConstants.CAR_NOT_STAFF)) {
				IPage<SearchSmtSnapVehicleVO> searchVehicleVisitorAccess = smtSnapVehicleMapper.searchVehicleVisitorAccess(page,snapVehicleAccessDto,parkIdList);
				if(CollUtil.isEmpty(searchVehicleVisitorAccess.getRecords())) {
					searchVehicleVisitorAccess = smtSnapVehicleMapper.searchVehicleAdmittanceAccess(page,snapVehicleAccessDto,parkIdList);
				}

				return searchVehicleVisitorAccess;
			}
		//默认查询内部人员
			IPage<SearchSmtSnapVehicleVO> searchVehicleStaffAccess = smtSnapVehicleMapper.searchVehicleStaffAccess(page, snapVehicleAccessDto,parkIdList);
/*			for (int i = 0; i < searchVehicleStaffAccess.getRecords().size(); i++) {
				for (int j = 0; j < searchVehicleStaffAccess.getRecords().get(i).size(); j++) {
					searchVehicleStaffAccess.getRecords().get(i).get(j).setSnapPhoto(getPhoto(searchVehicleStaffAccess.getRecords().get(i).get(j).getSnapPhotoId()));
				}
			}*/

			return searchVehicleStaffAccess;
	}


	/**
	 * 根据id查询车辆出入记录
	 */
	@Override
	public SearchOneSnapVehicleVO searchVehicleAccessById(Integer id) {
		//根据id查询车辆出入的信息
		SmtSnapVehicle byId = this.getById(id);
		SmtPark park = new SmtPark();
		if(ObjectUtil.isNotNull(byId.getParkId()))
		{
			park = smtParkService.getById(byId.getParkId());
		}
		//判断是否为外来人员的车辆信息
		 if(ObjectUtil.isNull(byId.getVehicleAscription())) {
				//查询外来车辆的值
				SearchOneSnapVehicleVO searchVehicleAccessVisitorById = this.baseMapper.searchVehicleAccessById(byId);
				searchVehicleAccessVisitorById.setSnapPhoto(smtImageService.buildImageUrl(byId.getParkId(),searchVehicleAccessVisitorById.getSnapPhotoId()));
				searchVehicleAccessVisitorById.setEventTypeDesc(EventTypeEnum.desc(searchVehicleAccessVisitorById.getEventType()));
				searchVehicleAccessVisitorById.setParkName(park.getParkName());
				searchVehicleAccessVisitorById.setParkId(byId.getParkId());
				return searchVehicleAccessVisitorById;
	}
		 else if(byId.getVehicleAscription().equals(VehicleBelongTypeEnum.VISITOR_VEHICLE.getCode())) {
			//查询关联访客的车辆信息
			SearchOneSnapVehicleVO searchVehicleAccessVisitorById = smtSnapVehicleMapper.searchVehicleAccessVisitorById(byId);
			if(Objects.isNull(searchVehicleAccessVisitorById)) {
				searchVehicleAccessVisitorById = smtSnapVehicleMapper.searchVehicleAccessAdmittanceById(byId);
			}
			searchVehicleAccessVisitorById.setSnapPhoto(smtImageService.buildImageUrl(byId.getParkId(),searchVehicleAccessVisitorById.getSnapPhotoId()));
			searchVehicleAccessVisitorById.setEventTypeDesc(EventTypeEnum.desc(searchVehicleAccessVisitorById.getEventType()));
			searchVehicleAccessVisitorById.setParkName(park.getParkName());
			 searchVehicleAccessVisitorById.setParkId(byId.getParkId());
			return searchVehicleAccessVisitorById;
		}
		//查询关联员工的车辆信息
		SearchOneSnapVehicleVO searchVehicleAccessStaffById = smtSnapVehicleMapper.searchVehicleAccessStaffById(byId);
		searchVehicleAccessStaffById.setSnapPhoto(smtImageService.buildImageUrl(byId.getParkId(),searchVehicleAccessStaffById.getSnapPhotoId()));
		searchVehicleAccessStaffById.setEventTypeDesc(EventTypeEnum.desc(searchVehicleAccessStaffById.getEventType()));
		searchVehicleAccessStaffById.setVehicleColorDesc(VehicleColorEnum.desc(searchVehicleAccessStaffById.getVehicleColor()));
		searchVehicleAccessStaffById.setParkName(park.getParkName());
		searchVehicleAccessStaffById.setParkId(byId.getParkId());
		return searchVehicleAccessStaffById;
	}

    @Override
    public List<ParkParkingRespDTO.InOutRecord> getInOutRecord(Integer parkId) {
		List<SmtSnapVehicle> snapVehicleListLasted = this.baseMapper.getSnapVehicleListLasted(parkId);
		List<ParkParkingRespDTO.InOutRecord> inOutRecords = new ArrayList<>();
		for(SmtSnapVehicle smtSnapVehicle : snapVehicleListLasted){
			inOutRecords.add(ParkParkingRespDTO.InOutRecord.builder()
					.areaName(smtSnapVehicle.getAreaName())
					.snapTime(smtSnapVehicle.getSnapTime())
					.vehiclePlate(smtSnapVehicle.getVehiclePlate())
					.typeDes(VehicleEventTypEnum.desc(smtSnapVehicle.getEventType()))
					.build());
		}
		return inOutRecords;
    }

}
