package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsdepRespDTO;
import com.tce.smart.data.api.dto.xcvehicle.req.XCVehicleAddDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYscompService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsdepService;
import com.tce.smart.data.api.feign.xcvehicle.RemoteXCVehicleService;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityRelationMapper;
import com.tce.smart.platform.core.mapper.SmtParkMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.mapper.SmtVehicleMapper;
import com.tce.smart.platform.core.model.DepTree;
import com.tce.smart.platform.core.model.VehicleDetail;
import com.tce.smart.platform.core.model.VehicleStaff;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.NotStaffVehicleVO;
import com.tce.smart.platform.core.vo.VehicleCountVO;
import com.tce.smart.platform.core.vo.VehicleVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.*;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 车辆信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
@Slf4j
@Service
public class SmtVehicleServiceImpl extends ServiceImpl<SmtVehicleMapper, SmtVehicle> implements SmtVehicleService {

	@Autowired
	private  SmtVehicleStaffService smtVehicleStaffService;
	@Autowired
	private  RemoteOvwYsdepService remoteOvwYsdepService;
	@Autowired
	private  RemoteOvwYscompService remoteOvwYscompService;
	@Autowired
	private  SmtStaffMapper smtStaffMapper;
	@Autowired
	private SmtJcheAuthService smtJcheAuthService;
	@Autowired
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;
	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;
	@Autowired
	private  SmtVehicleApplyService smtVehicleApplyService;
	@Autowired
	private  SmtNotStaffService smtNotStaffService;
	@Autowired
	private  SmtParkBuService smtParkBuService;
	@Autowired
	private  SmtParkMapper smtParkMapper;
	@Autowired
	private ImageService imageService;
	@Autowired
	private SmtImageService smtImageService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtDeviceAuthorityService smtDeviceAuthorityService;

	@Autowired
	private SmtBusinessDeviceAuthService smtBusinessDeviceAuthService;

	@Autowired
	private RemoteXCVehicleService remoteXCVehicleService;

	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	/**
	 * 保存车辆绑定人员
	 *
	 * @param entity 车辆人员信息
	 * @return 返回保存结果
	 */
	@Override
	@Transactional(rollbackFor=Exception.class)
	public Result<Boolean> saveSmtVehicle(SaveVehicleDTO entity) {
		//判断车辆是否已经添加
		SmtVehicle vehicle = this.getOne(Wrappers.<SmtVehicle>query().lambda()
				.eq(SmtVehicle::getVehiclePlate, StrUtil.removeAll(entity.getVehiclePlate(), " ").toUpperCase())
				.eq(SmtVehicle::getIsDelete, VehicleConstants.UNDELETED));
		if(null == vehicle){
			vehicle = new SmtVehicle();
			BeanUtil.copyProperties(entity, vehicle);
			vehicle.setVehiclePlate(StrUtil.removeAll(entity.getVehiclePlate(), " ").toUpperCase());
			vehicle.setIsDelete(VehicleConstants.UNDELETED);
			vehicle.setCreateTime(LocalDateTime.now());

			//驾驶证图片上传
			if(StrUtil.isNotBlank(entity.getDriverLicenseId())) {
				String driverLicenseIdTemp = entity.getDriverLicenseId().replaceAll(VehicleConstants.BASE64_REGEX,"");
				String driverLicenseImage = smtImageService.saveImage(Integer.parseInt(entity.getParkId()), driverLicenseIdTemp, SmtImageEnum.TYPE_DRIVER_CARD_FRONT.getCode());
				vehicle.setDriverLicenseId(driverLicenseImage);
			}

			//行驶证图片上传
			if(StrUtil.isNotBlank(entity.getDrivinglLicenseId())) {
				String drivinglLicenseTemp = entity.getDrivinglLicenseId().replaceAll(VehicleConstants.BASE64_REGEX,"");
				String drivinglLicenseImage = smtImageService.saveImage(Integer.parseInt(entity.getParkId()), drivinglLicenseTemp, SmtImageEnum.TYPE_DRIVING_CARD_FRONT.getCode());
				vehicle.setDrivinglLicenseId(drivinglLicenseImage);
			}
			//添加车辆记录
			this.save(vehicle);

			//添加车辆关联记录
			SmtVehicleStaff vehicleStaff = new SmtVehicleStaff();
			vehicleStaff.setStaffId(entity.getStaffId());
			vehicleStaff.setVehicleId(vehicle.getId());
			smtVehicleStaffService.save(vehicleStaff);
		}
		String username = SecurityUtils.getUser().getUsername();
		Long vehicleId = vehicle.getId();
		//查询申请记录 一辆车在一个园区只能有一条申请中或申请通过的记录
		SmtVehicleApply applyServiceOne = smtVehicleApplyService.getOne(new LambdaQueryWrapper<SmtVehicleApply>()
				.eq(SmtVehicleApply::getVehicleId, vehicleId)
				.eq(SmtVehicleApply::getParkId, entity.getParkId())
				.ne(SmtVehicleApply::getStatus,VehicleApplyConstants.REJECTED)
		);
		if(null != applyServiceOne){
			throw new TCEException("已存在申请记录");
		}
		//添加申请记录
		SmtVehicleApply vehicleApply = new SmtVehicleApply();
		vehicleApply.setParkId(entity.getParkId());
		vehicleApply.setVehicleId(vehicleId);
		vehicleApply.setApprover(username);
		vehicleApply.setStatus(VehicleApplyConstants.APPROVED);
		vehicleApply.setVehiclePlate(vehicle.getVehiclePlate());
		vehicleApply.setCreateTime(LocalDateTime.now());
		vehicleApply.setAuthorityId(entity.getAuthorityId());
		vehicleApply.insert();

		if(entity.getVehicleAscription().equals(VehicleConstants.STAFF)) {
			if(ObjectUtil.isNotNull(entity.getSource())) {
				SmtStaff staff = this.smtStaffMapper.selectById(entity.getStaffId());
				List<Integer> parkIds = splitStringToInteger(entity.getParkId());
				parkIds.forEach(park -> {
					Integer businessCode = smtJcheAuthService.getJchebusinessCode(Integer.parseInt(staff.getJcheId()), park);
					//下发处理
					register(vehicleId, Integer.parseInt(entity.getParkId()), entity.getVehiclePlate(), entity.getAuthorityId(), DeviceTaskConstants.CAR_STAFF, businessCode);
				});
			}
		}else if(entity.getVehicleAscription().equals(VehicleConstants.PARK)) {
			List<Integer> parkIds = splitStringToInteger(entity.getParkId());
			parkIds.forEach(park -> {
				//下发处理
				register(vehicleId, park, entity.getVehiclePlate(), entity.getAuthorityId(),DeviceTaskConstants.CAR_COMPANY, BusinessAuthorityEnum.IN_STAFF_VEHICLE.getCode());
			});
		}

	    return new Result<>(true);
	}

    @Override
	@Transactional
	public Result saveSmtVehicleOnly(SaveVehicleDTO entity) {
		//判断车辆是否已经添加
		List<SmtVehicle> smtVehicleList = this.list(Wrappers.<SmtVehicle>query().lambda()
				.eq(SmtVehicle::getVehiclePlate, StrUtil.removeAll(entity.getVehiclePlate(), " ").toUpperCase())
				.eq(SmtVehicle::getIsDelete, VehicleConstants.UNDELETED));
		if(CollUtil.isNotEmpty(smtVehicleList)) {
			return new Result<>(Boolean.FALSE, "车辆已经绑定人员，请勿重复绑定");
		}
		SmtVehicle vehicle = new SmtVehicle();
		BeanUtil.copyProperties(entity, vehicle);
		vehicle.setVehiclePlate(StrUtil.removeAll(entity.getVehiclePlate(), " ").toUpperCase());
		//驾驶证图片上传
		if(StrUtil.isNotBlank(entity.getDriverLicenseId())) {
			String driverLicenseIdTemp = entity.getDriverLicenseId().replaceAll(VehicleConstants.BASE64_REGEX,"");
			String driverLicenseImage = smtImageService.saveImage(Integer.parseInt(entity.getParkId()), driverLicenseIdTemp, SmtImageEnum.TYPE_DRIVER_CARD_FRONT.getCode());
			vehicle.setDriverLicenseId(driverLicenseImage);
		}

		//行驶证图片上传
		if(StrUtil.isNotBlank(entity.getDrivinglLicenseId())) {
			String drivinglLicenseTemp = entity.getDrivinglLicenseId().replaceAll(VehicleConstants.BASE64_REGEX,"");
			String drivinglLicenseImage = smtImageService.saveImage(Integer.parseInt(entity.getParkId()), drivinglLicenseTemp, SmtImageEnum.TYPE_DRIVING_CARD_FRONT.getCode());
			vehicle.setDrivinglLicenseId(drivinglLicenseImage);
		}
		vehicle.setIsDelete(VehicleConstants.UNDELETED);
		vehicle.setCreateTime(LocalDateTime.now());
		this.save(vehicle);

		SmtVehicleStaff vehicleStaff = new SmtVehicleStaff();
		vehicleStaff.setStaffId(entity.getStaffId());
		vehicleStaff.setVehicleId(vehicle.getId());
		smtVehicleStaffService.save(vehicleStaff);

		return new Result<>(true);
	}

	/**
	 * 查询车辆信息
	 * @param page 分页
	 * @param entity 查询条件
	 * @return 返回结果集
	 */
	@Override
	public IPage getVehicle(Page page, VehicleDTO entity) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		entity.setParkIds(parkIds);
		if(entity.getVehicleAscription().equals(VehicleConstants.STAFF)) {
			entity.setStatus(VehicleApplyConstants.APPROVED);
			return this.baseMapper.getStaffVehicle(page, entity);
		}else {
			return this.baseMapper.getVehicle(page, entity);
		}
	}

	/**
	 * 查询车辆详情信息
	 * @param id 车辆ID
	 * @return VehicleVO
	 */
	@Override
	public VehicleDetail getVehicleDetail(Long id) {
		VehicleVO vehicleVO = this.baseMapper.getDetail(id);
		VehicleDetail vehicleDetail = new VehicleDetail();
		BeanUtil.copyProperties(vehicleVO, vehicleDetail);
		if(StrUtil.isNotBlank(vehicleVO.getFacePicId())) {
			vehicleDetail.setFacePicId(imageService.buildImageUrl(vehicleVO.getFacePicId()));
		}
		if(StrUtil.isNotBlank(vehicleVO.getDriverLicenseId())) {
			vehicleDetail.setDriverLicenseId(imageService.buildImageUrl(vehicleVO.getDriverLicenseId()));
		}
		if(StrUtil.isNotBlank(vehicleVO.getDrivinglLicenseId())) {
			vehicleDetail.setDrivinglLicenseId(imageService.buildImageUrl(vehicleVO.getDrivinglLicenseId()));
		}
		vehicleDetail.setVehicleColorName(VehicleColorEnum.desc(vehicleDetail.getVehicleColor()));
		vehicleDetail.setVehicleTypeName(VehicleTypeEnum.desc(vehicleDetail.getVehicleType()));
		vehicleDetail.setSexName(vehicleDetail.getSex() + "");

		 List<Integer> applyParkId=new ArrayList<>();
		 List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		 List<SmtVehicleApply> list = smtVehicleApplyService.list(Wrappers.<SmtVehicleApply>query().lambda().eq(SmtVehicleApply::getVehicleId, id)
					.eq(SmtVehicleApply::getStatus,VehicleApplyConstants.APPROVED)
					.in(SmtVehicleApply::getParkId, parkIdList));
		 List<SmtDeviceAuthority> auths =new ArrayList<>();
		 for (SmtVehicleApply smtVehicleApply : list) {
				applyParkId.add(Integer.parseInt(smtVehicleApply.getParkId()) );
				if(Objects.nonNull(smtVehicleApply.getAuthorityId())){
					SmtDeviceAuthority smtDeviceAuthority = this.smtDeviceAuthorityService.getBaseMapper().selectById(smtVehicleApply.getAuthorityId());
					if(smtDeviceAuthority!=null)
					{
						vehicleDetail.setAuthorityId(smtDeviceAuthority.getId());
						vehicleDetail.setAuthorityName(smtDeviceAuthority.getAuthorityName());
						auths.add(smtDeviceAuthority);
					}
				}
			}
		vehicleDetail.setAuths(auths);

		//园区只显示当前登录用户所拥有的园区,取交集
		parkIdList.retainAll(applyParkId);
		List<SmtPark> parkList = smtParkService.list(Wrappers.<SmtPark>query().lambda().in(SmtPark::getId, parkIdList));
		vehicleDetail.setParks(parkList);
		String parkName="";
		 for (SmtPark smtPark : parkList) {
			 parkName+=smtPark.getParkName()+",";
		}
		 if(!parkName.equals("")) {
			 parkName=parkName.substring(0,parkName.length()-1);
		 }
		 vehicleDetail.setParkName(parkName);
		return vehicleDetail;
	}

	/**
	 * 根据部门查询员工信息
	 * @param id 部门ID
	 * @return 返回结果集
	 */
	@Transactional
	@Override
	public boolean deleteVehicle(Long id,List<Integer> parkIds) {
		SmtVehicle smtVehicle = new SmtVehicle();
		smtVehicle.setIsDelete(VehicleConstants.DELETED);
		int num = this.baseMapper.deleteVehilce(id, VehicleConstants.DELETED);

		//如果记录里面有权限配置 直接按照权限配置删除下发权限
//		if(null != vehicle.getAuthorityId()){
//			Logout(id,vehicle.getAuthorityId());
//			return true;
//		}

		//查询申请列表
		List<SmtVehicleApply> vehicleApplyList = smtVehicleApplyService.list(Wrappers.<SmtVehicleApply>query().lambda().eq(SmtVehicleApply::getVehicleId, id)
				.eq(SmtVehicleApply::getStatus,VehicleApplyConstants.APPROVED));
		if(CollectionUtil.isNotEmpty(vehicleApplyList)){
			//遍历删除已生成的权限
			for(SmtVehicleApply apply : vehicleApplyList){
				this.Logout(id,apply.getAuthorityId());
			}
		}

		return true;
	}

	/**
	 * 更新车辆信息
	 */
	@Override
	public Result<Boolean> updateVehicle(VehicleDTO entity) {

		//判断车辆是否已经添加
		List<SmtVehicle> smtVehicleList = this.list(Wrappers.<SmtVehicle>query().lambda()
				.eq(SmtVehicle::getVehiclePlate, StrUtil.removeAll(entity.getVehiclePlate(), " ").toUpperCase())
				.eq(SmtVehicle::getIsDelete, VehicleConstants.UNDELETED)
				.ne(SmtVehicle::getId,entity.getId()));
		if(CollectionUtil.isNotEmpty(smtVehicleList)) {
			return new Result<>(Boolean.FALSE, "车辆已经绑定，请勿重复绑定");
		}
		SmtVehicle vehicle = new SmtVehicle();
		BeanUtils.copyProperties(entity, vehicle);
		vehicle.setVehiclePlate(StrUtil.removeAll(entity.getVehiclePlate(), " ").toUpperCase());
		//驾驶证图片上传
		String driverLicenseId = doDriverCertId(entity.getDriverLicenseId(),entity.getParkId(),SmtImageEnum.TYPE_DRIVER_CARD_FRONT);
		vehicle.setDriverLicenseId(driverLicenseId);
		//行驶证图片上传
		String drivinglLicenseId = doDriverCertId(entity.getDrivinglLicenseId(),entity.getParkId(),SmtImageEnum.TYPE_DRIVING_CARD_FRONT);
		vehicle.setDrivinglLicenseId(drivinglLicenseId);

		//根据多园区筛选所修改园区
		List<Integer> newParkIdList = splitStringToInteger(vehicle.getParkId());
		List<Integer> userParkIdList = SecurityUtils.getUser().getParkIdList();
		SmtVehicle reVehicle = this.getById(entity.getId());
		List<Integer> reParkIdList = splitStringToInteger(reVehicle.getParkId());
		reParkIdList.removeAll(userParkIdList);
		reParkIdList.addAll(newParkIdList);
		vehicle.setParkId(org.apache.commons.lang.StringUtils.join(reParkIdList, SymbolConstants.COMMA));

		List<SmtVehicleApply> list = smtVehicleApplyService.list(Wrappers.<SmtVehicleApply>query().lambda().eq(SmtVehicleApply::getVehicleId, reVehicle.getId())
				.eq(SmtVehicleApply::getStatus,VehicleApplyConstants.APPROVED).eq(SmtVehicleApply::getParkId, reVehicle.getParkId()));
		if(list.size() == 1 && !list.get(0).getAuthorityId().equals(entity.getAuthorityId())){
			//TODO 车辆权限需要修改
			SmtVehicleApply smtVehicleApply = list.get(0);

			//生成权限
			DeviceTaskServiceTypeEnum serviceTypeEnum = DeviceTaskServiceTypeEnum.CAR_STAFF;			//员工车辆
			if(reVehicle.getVehicleAscription().equals(VehicleBelongTypeEnum.PARK_VEHICLE.getCode())){
				//公司车辆
				serviceTypeEnum = DeviceTaskServiceTypeEnum.CAR_COMPANY;
			} else if(reVehicle.getVehicleAscription().equals(VehicleBelongTypeEnum.NON_STAFF_VEHICLE.getCode())){
				//非员工车辆
				serviceTypeEnum = DeviceTaskServiceTypeEnum.CAR_NOT_STAFF;
			}
			smtDeviceTaskService.updateVehicleAuth(reVehicle,smtVehicleApply.getAuthorityId(),entity.getAuthorityId(),serviceTypeEnum.getCode());

			smtVehicleApply.setAuthorityId(entity.getAuthorityId());
			smtVehicleApply.updateById();
			log.info("车辆{}权限由{}修改为{}",reVehicle.getVehiclePlate(),smtVehicleApply.getAuthorityId(),entity.getAuthorityId());
		}

		boolean result = this.updateById(vehicle);
		if(result) {
			smtVehicleStaffService.remove(Wrappers.<SmtVehicleStaff>query().lambda().eq(SmtVehicleStaff::getVehicleId, entity.getId()));
			SmtVehicleStaff vehicleStaff = new SmtVehicleStaff();
			vehicleStaff.setStaffId(entity.getStaffId());
			vehicleStaff.setVehicleId(entity.getId());
			result = smtVehicleStaffService.save(vehicleStaff);
		}
		return new Result<>(result);
	}

	/**
	 * 注册车辆信息封装
	 *
	 * @param cardNo
	 * @param parkId
	 * @param vehiclePlate
	 */
	private void register(Long cardNo,Integer parkId,String vehiclePlate,Integer authorityId,Integer serviceType, Integer businessCode) {
		//如果有特定的权限 则使用特定权限
		List<SmtDeviceAuthorityRelation> selectList = null;
		if(null != authorityId){
			selectList = smtDeviceAuthorityRelationService.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().eq(SmtDeviceAuthorityRelation::getAuthorityId, authorityId));
		} else {
			selectList = smtDeviceAuthorityRelationService.getRelationAuth(parkId, businessCode, DeviceAuthorityEnum.deviceAuthority(authorityId));
		}

		 for (int i = 0; i < selectList.size(); i++) {
		// 车辆下发
			DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
			deviceTaskVO.setServiceType(serviceType);
			deviceTaskVO.setGeneral(vehiclePlate);
			deviceTaskVO.setCardNo(cardNo.toString());
			deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
			deviceTaskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_1.getType());
		deviceTaskVO.setDeviceType(DeviceTaskConstants.CAR);
			deviceTaskVO.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		deviceTaskVO.setOverTime(DeviceTaskConstants.maxTime);
		deviceTaskVO.setStartTime(DateUtil.currentSeconds());
		smtDeviceTaskService.saveTask(deviceTaskVO);
		}
	}

    @Override
    public List<DepTree> getCompTree(List<Integer> parkIds) {
        return this.getParkTree(parkIds);
    }

    private List<DepTree> getParkTree(List<Integer> parkIds){
		List<DepTree> depTreeList = new ArrayList<>();
		List<SmtPark> parks = smtParkMapper.selectList(Wrappers.<SmtPark>query().lambda().in(CollUtil.isNotEmpty(parkIds),SmtPark::getId,parkIds));
		if(CollUtil.isNotEmpty(parks)){
			for (SmtPark park:parks) {
				DepTree depTree = new DepTree();
				depTree.setLabel(park.getParkName());
				depTree.setValue(park.getId());
				depTree.setChildren( this.getComp(park.getId()));
				depTreeList.add(depTree);
			}
		}
		return depTreeList;
	}


	private List<DepTree> getComp(Integer parkId) {
		List<SmtParkBu> listBu = smtParkBuService.list(Wrappers.<SmtParkBu>query().lambda()
				.eq(ObjectUtil.isNotNull(parkId),SmtParkBu::getParkId,parkId));
		List<DepTree>  listTree = new ArrayList<DepTree>();
		// 查询裕同视图中的全部bu
		if(CollUtil.isNotEmpty(listBu)){
			Result<List<OvwYscompRespDTO>> yscompServiceList = remoteOvwYscompService.getList(SecurityConstants.FROM_IN);
			List<OvwYscompRespDTO> yscompRespDTOS = yscompServiceList.getData();
			Map<Integer, List<OvwYscompRespDTO>> ovmYsMap = yscompRespDTOS.stream().collect(Collectors.groupingBy(OvwYscompRespDTO::getCompid));
			listBu.forEach(bu->{
				Integer compId = Integer.parseInt(bu.getCompId());
				if(ovmYsMap.containsKey(compId)) {
					DepTree depTree = new DepTree();
					depTree.setLabel(ovmYsMap.get(compId).get(0).getTitle());
					depTree.setValue(ovmYsMap.get(compId).get(0).getCompid());
					depTree.setChildren(getDepTree(compId));
					listTree.add(depTree);
				}
//				Result<OvwYscompRespDTO> result = remoteOvwYscompService.getByCompId(bu.getCompId(),SecurityConstants.FROM_IN);
//				if(result.isSuccess()) {
//					OvwYscompRespDTO ovwYscompVO = result.getData();
//					if(null == ovwYscompVO){
//						//注意 这里的return并不会结束方法 只是跳出本次循环
//						return;
//					}
//					DepTree depTree = new DepTree();
//					depTree.setLabel(ovwYscompVO.getTitle());
//					depTree.setValue(ovwYscompVO.getCompid());
//					depTree.setChildren(getDepTree(ovwYscompVO.getCompid()));
//					listTree.add(depTree);
//				}
			});
		}
		return listTree;
	}

    public List<DepTree> getDepTree(Integer id) {
	List<DepTree> depTreeList = new ArrayList<>();
	DepTree depTree = null;
        Result<List<OvwYsdepRespDTO>> result = remoteOvwYsdepService.getByCompId(id, SecurityConstants.FROM_IN);
        List<OvwYsdepRespDTO> list = result.getData();
        if(CollectionUtil.isNotEmpty(list)) {
			for (OvwYsdepRespDTO ovwYsdepVO : list) {
				depTree = new DepTree();
				depTree.setLabel(ovwYsdepVO.getDepname());
				depTree.setValue(ovwYsdepVO.getDepid());
				depTreeList.add(depTree);
			}
			return depTreeList;
		}
        return null;
    }

    @Override
    public List<OvwYscompRespDTO> getComp(List<Integer> parkIds) {
		List<SmtParkBu> listBu = smtParkBuService.list(Wrappers.<SmtParkBu>query().lambda().in(CollUtil.isNotEmpty(parkIds),SmtParkBu::getParkId,parkIds));
		List<OvwYscompRespDTO>  listOvwYscompVO = new ArrayList<OvwYscompRespDTO>();
		// 查询裕同视图中的全部bu
		listBu.forEach(bu->{
			Result<OvwYscompRespDTO> result = remoteOvwYscompService.getByCompId(bu.getCompId(),SecurityConstants.FROM_IN);
			if(result.isSuccess()) {
				listOvwYscompVO.add(result.getData());
			}
		});
        return listOvwYscompVO;
    }

    @Override
    public List<OvwYsdepRespDTO> getDep(Integer id) {
        Result<List<OvwYsdepRespDTO>> result = remoteOvwYsdepService.getByCompId(id, SecurityConstants.FROM_IN);
        List<OvwYsdepRespDTO> list = result.getData();
        return list;
    }

    @Override
    public List<VehicleStaff> getStaff(Integer id) {
        return this.baseMapper.getStaff(id);
    }

    /**
     * 注消车辆信息封装
     *
     * @param cardNo
     * @param parkId
     * @param code
     */
    private void Logout(Long cardNo,Integer parkId,DeviceAuthorityEnum code, Integer businessCodeId) {
		List<SmtDeviceAuthorityRelation> selectList = smtDeviceAuthorityRelationService.getRelationAuth(parkId, businessCodeId, code);
        for (int i = 0; i < selectList.size(); i++) {
			DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(DeviceTaskConstants.DEL);
			deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
			deviceTaskVO.setCardNo(cardNo.toString());
		smtDeviceTaskService.saveTask(deviceTaskVO);
        }
    }

	/**
	 * 生成删除权限
	 * @param cardNo
	 * @param authorityId
	 */
	public void Logout(Long cardNo,Integer authorityId) {
		List<SmtDeviceAuthorityRelation> selectList = smtDeviceAuthorityRelationService.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().eq(SmtDeviceAuthorityRelation::getAuthorityId,authorityId));
		for (int i = 0; i < selectList.size(); i++) {
			DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(DeviceTaskConstants.DEL);
			deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
			deviceTaskVO.setCardNo(cardNo.toString());
			deviceTaskVO.setStartTime(DateUtils.currentSeconds());
			deviceTaskVO.setOverTime(DateUtils.currentSeconds());
			smtDeviceTaskService.saveTask(deviceTaskVO);
		}
	}

	@Override
	public SmtStaff getStaffDetail(Long staffId) {
		SmtStaff staff = smtStaffMapper.selectById(staffId);
		return staff;
	}

	@Override
	public VehicleStaff getStaffDetail(String badge,List<Integer> parkIds) {
		VehicleStaff vehicleStaff = new VehicleStaff();
		SmtStaff staff = smtStaffMapper.getStaffIgnoreCase(badge);
		if(Objects.isNull(staff)) {
			log.error("员工不存在");
			return null;
		}
		BeanUtil.copyProperties(staff, vehicleStaff);
		vehicleStaff.setBadge(staff.getBadge());
		vehicleStaff.setStatusDesc(StaffStatusEnum.desc(staff.getStatus()));
		if(ObjectUtil.isNotNull(staff.getFacePicId())) {
			vehicleStaff.setFacePicId(imageService.buildImageUrl(staff.getFacePicId()));
		}
		// 查询正式员工权限
		List<SmtPark> parks = this.baseMapper.getParkBu(staff.getCompId(),parkIds);
		if (CollectionUtil.isEmpty(parks)) {
			// 查询临时员工权限
			parks = this.baseMapper.getParkTempBu(staff.getCompId(),parkIds);
		}
		if(CollectionUtil.isEmpty(parks)) {
			throw new TCEException("对该员工所在园区无操作权限");
		}
		vehicleStaff.setParks(parks);
		return vehicleStaff;
	}


	@Override
	public VehicleCountVO getVehicleCountInfo() {
	List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		VehicleCountVO vehicleCountVO = this.baseMapper.getVehicleCountInfo(parkIdList);
		return vehicleCountVO;
	}


	@Override
	public IPage<NotStaffVehicleVO> getNotStaffVehiclePage(Page page, VehicleDTO smtVehicle) {
		smtVehicle.setIsDelete(VehicleConstants.UNDELETED);
		return this.baseMapper.getNotStaffVehicle(page, smtVehicle);
	}

	@Override
	public NotStaffVehicleVO getNotStaffVehicle(Long id) {
		VehicleDTO smtVehicle = new VehicleDTO();
		smtVehicle.setId(id);
		IPage<NotStaffVehicleVO> page = this.baseMapper.getNotStaffVehicle(new Page(), smtVehicle);
		if(ObjectUtil.isNotNull(page) && CollUtil.isNotEmpty(page.getRecords())){
			return page.getRecords().get(0);
		}
		return null;
	}


	@Override
	public Result saveNotStaffVehicle(NotStaffVehicleDTO notStaffVehicleDTO) {
		//判断车辆是否已经添加
	    List<SmtVehicle> smtVehicleList = this.list(Wrappers.<SmtVehicle>query().lambda()
		.eq(SmtVehicle::getVehiclePlate, StrUtil.removeAll(notStaffVehicleDTO.getVehiclePlate(), " ").toUpperCase())
		.eq(SmtVehicle::getIsDelete, VehicleConstants.UNDELETED)
				.eq(SmtVehicle::getParkId,notStaffVehicleDTO.getParkId())
		);
	    if(CollectionUtil.isNotEmpty(smtVehicleList)) {
		return new Result<>(Boolean.FALSE, "车辆已经绑定人员，请勿重复绑定");
	    }
		SmtVehicle vehicle = new SmtVehicle();
		BeanUtils.copyProperties(notStaffVehicleDTO, vehicle);
		vehicle.setVehiclePlate(StrUtil.removeAll(notStaffVehicleDTO.getVehiclePlate(), " ").toUpperCase());

		//驾驶证图片上传
		String driverLicenseId = doDriverCertId(notStaffVehicleDTO.getDriverLicenseId(),notStaffVehicleDTO.getParkId(),SmtImageEnum.TYPE_DRIVER_CARD_FRONT);
		vehicle.setDriverLicenseId(driverLicenseId);

		//行驶证图片上传
		String drivinglLicenseId = doDriverCertId(notStaffVehicleDTO.getDrivinglLicenseId(),notStaffVehicleDTO.getParkId(),SmtImageEnum.TYPE_DRIVING_CARD_FRONT);
		vehicle.setDrivinglLicenseId(drivinglLicenseId);
		vehicle.setIsDelete(VehicleConstants.UNDELETED);
	    vehicle.setCreateTime(LocalDateTime.now());
	    vehicle.setVehicleAscription(VehicleConstants.NOT_STAFF);
		vehicle.setParkId(notStaffVehicleDTO.getParkId());
	    boolean result = this.save(vehicle);
	    if(result) {
		SmtNotStaff notStaff = new SmtNotStaff();
		notStaff.setName(notStaffVehicleDTO.getName());
		notStaff.setPhone(notStaffVehicleDTO.getPhone());
		notStaff.setRemark(notStaffVehicleDTO.getRemark());
		notStaff.setVehicleId(vehicle.getId());
			notStaff.setParkId(notStaffVehicleDTO.getParkId());

		result = notStaff.insert();

		if(result) {
			SmtVehicleApply vehicleApply = new SmtVehicleApply();
			vehicleApply.setParkId(notStaffVehicleDTO.getParkId());
			vehicleApply.setVehicleId(vehicle.getId());
			vehicleApply.setStatus(VehicleApplyConstants.APPROVED);
			vehicleApply.setVehiclePlate(vehicle.getVehiclePlate());
			vehicleApply.setCreateTime(LocalDateTime.now());
				vehicleApply.setAuthorityId(notStaffVehicleDTO.getAuthorityId());
				vehicleApply.insert();
			register(vehicle.getId(), Integer.parseInt(notStaffVehicleDTO.getParkId()), notStaffVehicleDTO.getVehiclePlate(), notStaffVehicleDTO.getAuthorityId(), DeviceTaskConstants.CAR_NOT_STAFF, BusinessAuthorityEnum.NOT_STAFF_VEHICLE.getCode());
		}
	    }
	    return new Result<>(result);
	}

	@Override
	public List<String> getWelfareLevel() {
		return this.getBaseMapper().getWelfareLevel();
	}

	@Override
	public int getApplyVehicle(Integer parkId, String vehiclePlate, Integer isDelete, Integer status) {
		return this.baseMapper.getApplyVehicle(parkId,vehiclePlate,isDelete,status);
	}


	/**
	 * 处理驾驶证或行驶证的保存
	 * 这里前端传回的值有两种情况
	 *  如果是新添加操作 则传回的是图片的base64编码内容
	 *  如果是修改操作 但是没有修改驾驶证和行驶证的图片 则传回的是当前图片的链接地址
	 *  所以在处理时 如果不是新的图片 则不需要修改图片信息
	 * @param driverCertId
	 * @param parkId
	 * @param smtImageEnum
	 * @return
	 */
	private String doDriverCertId(String driverCertId,String parkId,SmtImageEnum smtImageEnum){
		if(StrUtil.isNotBlank(driverCertId) && !driverCertId.startsWith("http")) {
			String driverLicenseId = driverCertId.replaceAll(VehicleConstants.BASE64_REGEX,"");
			String blobResult = smtImageService.saveImage(Integer.parseInt(parkId),driverLicenseId, smtImageEnum.getCode());
			return blobResult;
		}
		return null;
	}

	/**
	 * 更新非员工车辆信息
	 */
	@Transactional
	@Override
	public boolean updateNotStaffVehicle(NotStaffVehicleDTO notStaffVehicleDTO) {
		SmtVehicle vehicle = new SmtVehicle();
		BeanUtils.copyProperties(notStaffVehicleDTO, vehicle);
		vehicle.setVehiclePlate(StrUtil.removeAll(notStaffVehicleDTO.getVehiclePlate(), " ").toUpperCase());
		//驾驶证图片上传
		String driverLicenseId = doDriverCertId(notStaffVehicleDTO.getDriverLicenseId(),notStaffVehicleDTO.getParkId(),SmtImageEnum.TYPE_DRIVER_CARD_FRONT);
		vehicle.setDriverLicenseId(driverLicenseId);

	    //行驶证图片上传
		String drivinglLicenseId = doDriverCertId(notStaffVehicleDTO.getDrivinglLicenseId(),notStaffVehicleDTO.getParkId(),SmtImageEnum.TYPE_DRIVING_CARD_FRONT);
		vehicle.setDrivinglLicenseId(drivinglLicenseId);

		SmtVehicle reVehicle = this.getById(notStaffVehicleDTO.getId());


		List<SmtVehicleApply> list = smtVehicleApplyService.list(Wrappers.<SmtVehicleApply>query().lambda().eq(SmtVehicleApply::getVehicleId, reVehicle.getId())
				.eq(SmtVehicleApply::getStatus,VehicleApplyConstants.APPROVED).eq(SmtVehicleApply::getParkId, reVehicle.getParkId()));
		if(list.size()==1  ){
			//TODO 车辆权限需要修改
			SmtVehicleApply smtVehicleApply = list.get(0);
			log.info("车辆{}权限由{}修改为{}",reVehicle.getVehiclePlate(),smtVehicleApply.getAuthorityId(),notStaffVehicleDTO.getAuthorityId());
			smtDeviceTaskService.updateVehicleAuth(reVehicle,smtVehicleApply.getAuthorityId(),notStaffVehicleDTO.getAuthorityId(),DeviceTaskConstants.CAR_NOT_STAFF);
			smtVehicleApply.setAuthorityId(notStaffVehicleDTO.getAuthorityId());
			smtVehicleApply.updateById();
		}


		boolean result = this.updateById(vehicle);
	if(result) {
		SmtNotStaff notStaff = new SmtNotStaff();
	notStaff.setName(notStaffVehicleDTO.getName());
	notStaff.setPhone(notStaffVehicleDTO.getPhone());
	notStaff.setRemark(notStaffVehicleDTO.getRemark());
	notStaff.setVehicleId(vehicle.getId());
			notStaff.setParkId(vehicle.getParkId());
	result = notStaff.update(Wrappers.<SmtNotStaff>update().lambda().eq(SmtNotStaff::getVehicleId, notStaff.getVehicleId()));
	}
		return result;
	}

	/**
	 * 删除非员工车辆
	 * @param id 部门ID
	 * @return 返回结果集
	 */
	@Transactional
	@Override
	public boolean deleteNotStaffVehicle(Long id) {
		return deleteVehicle(id,null);
	}

	/**
	 * 分离id
	 *
	 * @param idsStr id组合字符串
	 * @return id集合
	 */
	@Override
	public List<Integer> splitStringToInteger(String idsStr) {
		List<Integer> returnList = new ArrayList<>();
		if (!StringUtil.isNullOrEmpty(idsStr)) {
			int[] idsArray = StringUtils.splitToInt(idsStr, ",");
			returnList.addAll(IntStream.of(idsArray).boxed().collect(Collectors.toList()));
		}

		return returnList;
	}
}
