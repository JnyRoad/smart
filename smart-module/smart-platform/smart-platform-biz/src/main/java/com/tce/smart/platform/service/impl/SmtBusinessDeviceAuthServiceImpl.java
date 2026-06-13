package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.core.dto.AddAuthRelationReqDTO;
import com.tce.smart.platform.core.dto.VehicleAuthDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtBusinessDeviceAuthMapper;
import com.tce.smart.platform.core.mapper.SmtVehicleMapper;
import com.tce.smart.platform.core.mapper.SmtVehicleStaffMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.BusinessAuthorityEnum;
import com.tce.smart.tool.enums.DeviceTaskServiceTypeEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import com.tce.smart.tool.enums.VehicleBelongTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 *
 * @author fushiping
 * @date 2020-08-05 18:22:56
 */
@Service
public class SmtBusinessDeviceAuthServiceImpl extends ServiceImpl<SmtBusinessDeviceAuthMapper, SmtBusinessDeviceAuth> implements SmtBusinessDeviceAuthService {

	@Autowired
	private SmtJcheAuthService smtJcheAuthService;

	@Autowired
	private SmtStaffDeviceAuthService smtStaffDeviceAuthService;

	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;

	@Autowired
	private SmtStaffService smtStaffService;

	@Autowired
	private SmtVehicleStaffMapper smtVehicleStaffMapper;

	@Autowired
	private SmtVehicleMapper smtVehicleMapper;

	@Autowired
	private SmtVehicleApplyService smtVehicleApplyService;


	@Override
	public SmtBusinessDeviceAuth getDeviceAuth(Integer parkId, Integer businessCode) {
		return this.getOne(Wrappers.<SmtBusinessDeviceAuth>query().lambda()
				.eq(SmtBusinessDeviceAuth::getParkId, parkId)
				.eq(SmtBusinessDeviceAuth::getBusinessCode, businessCode));
	}

	@Override
	public List<SmtBusinessDeviceAuth> getMulDeviceAuth(List<Integer> parkIds, Integer businessCode) {
		return this.list(Wrappers.<SmtBusinessDeviceAuth>query().lambda()
				.in(Objects.nonNull(parkIds) && parkIds.size() > 0, SmtBusinessDeviceAuth::getParkId, parkIds)
				.eq(SmtBusinessDeviceAuth::getBusinessCode, businessCode));
	}

	@Override
	public Boolean saveAuth(SmtBusinessDeviceAuth auth) {
		SmtBusinessDeviceAuth reAuth = this.getDeviceAuth(auth.getParkId(), auth.getBusinessCode());
		if(Objects.nonNull(reAuth)) {
			reAuth.setAuthId(auth.getAuthId());
			return reAuth.updateById();
		}
		return this.save(auth);
	}

	@Override
	public Boolean saveDeptAuth(String deptId, Integer[] authIdArray) {
		// 查询部门下非离职的员工
		List<SmtStaff> staffList = smtStaffService.list(new LambdaQueryWrapper<SmtStaff>()
				.eq(SmtStaff::getDepId, deptId)
				.ne(SmtStaff::getStatus, StaffStatusEnum.STAFF_STATUS_QUIT.getCode()));
		List<Integer> newAuthIds = ArrayUtil.isEmpty(authIdArray) ? new ArrayList<>() : Arrays.asList(authIdArray);
		staffList.forEach(staff -> {
			List<SmtStaffDeviceAuth> staffDeviceAuthList = smtStaffDeviceAuthService.queryList(staff.getId());
			List<Integer> oldAuthIds = staffDeviceAuthList.stream().map(SmtStaffDeviceAuth::getAuthId).collect(Collectors.toList());
			if (ArrayUtil.isEmpty(authIdArray)) {
				// 新授权ID为空，表示删除，清空旧的权限
				smtDeviceTaskService.delStaffAuthDelay(staff, oldAuthIds, true, DeviceTaskServiceTypeEnum.CARD_STAFF_IMPORT.getCode());
			} else {
				smtDeviceTaskService.updateStaffAuth(staff, oldAuthIds, newAuthIds, DeviceTaskServiceTypeEnum.CARD_STAFF_IMPORT.getCode());
			}
		});
		return true;
	}

	@Override
	public AddAuthRelationReqDTO getList(Integer parkId) {
		if(Objects.isNull(parkId)) {
			return null;
		}
		AddAuthRelationReqDTO addAuthRelationReqDTO = new AddAuthRelationReqDTO();
		List<SmtBusinessDeviceAuth> auth = this.list(Wrappers.<SmtBusinessDeviceAuth>query().lambda().eq(SmtBusinessDeviceAuth::getParkId, parkId)
				.orderByAsc(SmtBusinessDeviceAuth::getBusinessCode));
		List<SmtJcheAuth> jcheId = smtJcheAuthService.list(Wrappers.<SmtJcheAuth>query().lambda()
				.eq(SmtJcheAuth::getBusinessCode, BusinessAuthorityEnum.SPECIAL_JCHE.getCode()).eq(SmtJcheAuth::getParkId, parkId));
		List<Integer> jcheIds = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(jcheId)) {
			jcheIds = jcheId.stream().map(SmtJcheAuth::getJcheId).collect(Collectors.toList());
		}
		addAuthRelationReqDTO.setAuth(auth);
		addAuthRelationReqDTO.setParkId(parkId);
		addAuthRelationReqDTO.setJcheIds(jcheIds);
		if(CollectionUtils.isNotEmpty(auth)) {
			return addAuthRelationReqDTO;
		}
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean batchSaveAuth(AddAuthRelationReqDTO reqDTO) {
		if(CollectionUtils.isEmpty(reqDTO.getAuth())) {
			throw new SmartException("数据列为空");
		}
		Integer parkId = reqDTO.getParkId();
		//查询所有权限设置
		List<SmtBusinessDeviceAuth> businessDeviceAuths = this.list(new LambdaQueryWrapper<SmtBusinessDeviceAuth>().eq(SmtBusinessDeviceAuth::getParkId, parkId));
		if(CollectionUtil.isNotEmpty(businessDeviceAuths)){
			//修改配置
			Map<Integer, List<SmtBusinessDeviceAuth>> deviceAuthCodeMap = businessDeviceAuths.stream()
					.filter(e -> Objects.nonNull(e.getBusinessCode())).collect(Collectors.groupingBy(SmtBusinessDeviceAuth::getBusinessCode));
			this.remove(Wrappers.<SmtBusinessDeviceAuth>query().lambda()
					.eq(SmtBusinessDeviceAuth::getParkId,parkId));
			smtJcheAuthService.remove(Wrappers.<SmtJcheAuth>query().lambda()
					.eq(SmtJcheAuth::getBusinessCode, BusinessAuthorityEnum.SPECIAL_JCHE.getCode()).eq(SmtJcheAuth::getParkId, parkId));
			//比对权限
			for(SmtBusinessDeviceAuth newDeviceAuth : reqDTO.getAuth()){

				//如果是新配置的权限 不需要修改数据权限
				if(CollectionUtil.isEmpty(deviceAuthCodeMap.get(newDeviceAuth.getBusinessCode()))){
					continue;
				}

				//访客人脸和访客车辆权限变动时不修改现有的数据权限
				if(newDeviceAuth.getBusinessCode().equals(BusinessAuthorityEnum.VISITOR_FACE.getCode()) || newDeviceAuth.getBusinessCode().equals(BusinessAuthorityEnum.VISITOR_VEHICLE.getCode())){
					continue;
				}

				//旧权限Id
				Integer oldAuthId = deviceAuthCodeMap.get(newDeviceAuth.getBusinessCode()).get(0).getAuthId();
				//新权限Id
				Integer newAuthId = newDeviceAuth.getAuthId();

				if(newDeviceAuth.getBusinessCode().equals(BusinessAuthorityEnum.STAFF_FACE.getCode())){
					//更新所有关联员工人脸权限
					updateStaffFaceAuth(oldAuthId,newAuthId);
				} else if(newDeviceAuth.getBusinessCode().equals(BusinessAuthorityEnum.IN_OUT_STAFF_VEHICLE.getCode())){
					//更新所有关联员工车辆权限
					updateStaffVehicleAuth(oldAuthId,newAuthId,false);
				} else if(newDeviceAuth.getBusinessCode().equals(BusinessAuthorityEnum.PARK_VEHICLE.getCode())){
					//更新所有关联园区车辆权限
					updateVehicleAuth(oldAuthId,newAuthId,VehicleBelongTypeEnum.IN_VEHICLE.getCode());
				} else if(newDeviceAuth.getBusinessCode().equals(BusinessAuthorityEnum.LOGISTICS_APPOINTMENT.getCode())){
					//更新所有关联物流车通行权限
					updateVehicleAuth(oldAuthId,newAuthId,VehicleBelongTypeEnum.LOGISTICS_VEHICLE.getCode());
				} else if(newDeviceAuth.getBusinessCode().equals(BusinessAuthorityEnum.NOT_STAFF_VEHICLE.getCode())){
					//更新所有关联非员工车辆通行权限
					updateVehicleAuth(oldAuthId,newAuthId,VehicleBelongTypeEnum.NON_STAFF_VEHICLE.getCode());
				} else if(newDeviceAuth.getBusinessCode().equals(BusinessAuthorityEnum.IN_STAFF_VEHICLE.getCode())){
					//更新所有关联公司车辆通行权限
					updateVehicleAuth(oldAuthId,newAuthId,VehicleBelongTypeEnum.PARK_VEHICLE.getCode());
				} else if(newDeviceAuth.getBusinessCode().equals(BusinessAuthorityEnum.SPECIAL_JCHE.getCode())){
					//更新所有关联特殊职层车辆通行权限
					updateStaffVehicleAuth(oldAuthId,newAuthId,true);
				}
			}
		}

		List<Integer> jcheIds = reqDTO.getJcheIds();
		//保存特殊职层
		if(CollectionUtils.isNotEmpty(jcheIds)) {
			List<SmtJcheAuth> jcheAuths = new ArrayList<>();
			jcheIds.forEach(id -> {
				SmtJcheAuth smtJcheAuth = new SmtJcheAuth();
				smtJcheAuth.setJcheId(id);
				smtJcheAuth.setParkId(parkId);
				smtJcheAuth.setBusinessCode(BusinessAuthorityEnum.SPECIAL_JCHE.getCode());
				jcheAuths.add(smtJcheAuth);
			});
			smtJcheAuthService.saveBatch(jcheAuths);
		}
		this.saveBatch(reqDTO.getAuth());
		return true;
	}

	/**
	 * 更新员工人脸权限
	 * @param oldAuthId		旧权限Id
	 * @param newAuthId		新权限Id
	 */
	public void updateStaffFaceAuth(Integer oldAuthId,Integer newAuthId){
		if(!newAuthId.equals(oldAuthId)){
			//权限配置已修改 对应的权限数据需同时修改
			//分页查询所有关联该权限的员工权限数据
			Page authPage = new Page(1,100);
			boolean isNext = false;
			do{
				//查询一页数据
				IPage<SmtStaffDeviceAuth> deviceAuthIPage = smtStaffDeviceAuthService.page(authPage,new LambdaQueryWrapper<SmtStaffDeviceAuth>().eq(SmtStaffDeviceAuth::getAuthId, oldAuthId));

				List<SmtStaffDeviceAuth> smtStaffDeviceAuths = deviceAuthIPage.getRecords();

				if(CollectionUtil.isNotEmpty(smtStaffDeviceAuths)) {
					List<Long> staffIdList = smtStaffDeviceAuths.stream().map(SmtStaffDeviceAuth::getStaffId).collect(Collectors.toList());
					//查询员工数据
					List<SmtStaff> smtStaffList = smtStaffService.list(new LambdaQueryWrapper<SmtStaff>().in(SmtStaff::getId, staffIdList));
					//修改员工权限
					smtStaffList.forEach(item -> {
						if(item.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())){
							//如果员工已离职 不修改设备权限
							return;
						}
						smtDeviceTaskService.updateStaffAuthDelay(item,newAuthId,oldAuthId, DeviceTaskConstants.CARD_STAFF_IMPORT);
					});
					//修改员工权限配置
					smtStaffDeviceAuths.forEach(item -> {
						item.setAuthId(newAuthId);
					});
					smtStaffDeviceAuthService.updateBatchById(smtStaffDeviceAuths);
				}
				if(authPage.hasNext()){
					//下一页
					authPage.setCurrent(authPage.getCurrent() + 1);
					isNext = true;
				} else {
					isNext = false;
				}
			} while (isNext);
		}
	}

	/**
	 * 更新员工车辆权限
	 * @param oldAuthId
	 * @param newAuthId
	 */
	public void updateStaffVehicleAuth(Integer oldAuthId,Integer newAuthId,Boolean isSpecial){
		if(!newAuthId.equals(oldAuthId)){
			//权限配置已修改 对应的权限数据需同时修改
			//查询特殊职层列表
			List<SmtJcheAuth> jcheAuths = smtJcheAuthService.list();
			List<Integer> jcheIdList = jcheAuths.stream().map(SmtJcheAuth::getJcheId).collect(Collectors.toList());
			//分页查询所有关联该权限的车辆权限数据
			Page vehiclePage = new Page(1,100);
			boolean isNext = false;
			do{
				//查询一页数据
				IPage<VehicleAuthDTO> staffVehiclePage = smtVehicleStaffMapper.getStaffVehicleByAuthId(vehiclePage, oldAuthId);

				List<VehicleAuthDTO> VehicleAuthList = staffVehiclePage.getRecords();

				List<SmtVehicleApply> vehicleApplyList = new ArrayList<>();
				if(CollectionUtil.isNotEmpty(VehicleAuthList)) {
					//修改车辆权限
					VehicleAuthList.forEach(item -> {
						if(isSpecial){
							//特殊职层权限修改 则员工职层应该存在特殊职层列表中 若不存在 则不处理
							if(!jcheIdList.contains(item.getJcheId())){
								return;
							}
						} else {
							//如果是非特殊职层权限修改 则员工职层不应该存在特殊职层列表中 若存在 则不处理
							if(jcheIdList.contains(item.getJcheId())){
								return;
							}
						}
						SmtVehicle vehicle = new SmtVehicle();
						vehicle.setId(item.getVid());
						vehicle.setVehiclePlate(item.getVehiclePlate());
						//注意 这里车辆下发时 cardNo的取值是SmtVehicle表的主键id
						smtDeviceTaskService.updateVehicleAuthDelay(vehicle,oldAuthId,newAuthId,DeviceTaskConstants.CAR_STAFF);

						SmtVehicleApply smtVehicleApply = new SmtVehicleApply();
						smtVehicleApply.setId(item.getId());
						smtVehicleApply.setAuthorityId(newAuthId);

						vehicleApplyList.add(smtVehicleApply);
					});
					if(CollectionUtil.isNotEmpty(vehicleApplyList)) {
						smtVehicleApplyService.updateBatchById(vehicleApplyList);
					}
				}
				if(vehiclePage.hasNext()){
					//下一页
					vehiclePage.setCurrent(vehiclePage.getCurrent() + 1);
					isNext = true;
				} else {
					isNext = false;
				}
			} while (isNext);
		}
	}

	/**
	 * 更新其他车辆权限
	 * @param oldAuthId		旧权限Id
	 * @param newAuthId		新权限Id
	 */
	public void updateVehicleAuth(Integer oldAuthId,Integer newAuthId,Integer belongType){
		if(!newAuthId.equals(oldAuthId)){
			//权限配置已修改 对应的权限数据需同时修改
			//分页查询所有关联该权限的车辆权限数据
			Page vehiclePage = new Page(1,100);
			boolean isNext = false;
			do{
				//查询一页数据
				IPage<VehicleAuthDTO> searchVehiclePage = smtVehicleMapper.getVehicleAuth(vehiclePage, oldAuthId, belongType);

				List<VehicleAuthDTO> VehicleAuthList = searchVehiclePage.getRecords();

				List<SmtVehicleApply> vehicleApplyList = new ArrayList<>();
				if(CollectionUtil.isNotEmpty(VehicleAuthList)) {
					//修改车辆权限
					VehicleAuthList.forEach(item -> {
						SmtVehicle vehicle = new SmtVehicle();
						vehicle.setId(item.getVid());
						vehicle.setVehiclePlate(item.getVehiclePlate());
						//注意 这里车辆下发时 cardNo的取值是SmtVehicle表的主键id
						smtDeviceTaskService.updateVehicleAuthDelay(vehicle,oldAuthId,newAuthId,DeviceTaskConstants.CAR_STAFF);

						SmtVehicleApply smtVehicleApply = new SmtVehicleApply();
						smtVehicleApply.setId(item.getId());
						smtVehicleApply.setAuthorityId(newAuthId);

						vehicleApplyList.add(smtVehicleApply);
					});
					smtVehicleApplyService.updateBatchById(vehicleApplyList);
				}
				if(vehiclePage.hasNext()){
					//下一页
					vehiclePage.setCurrent(vehiclePage.getCurrent() + 1);
					isNext = true;
				} else {
					isNext = false;
				}
			} while (isNext);
		}
	}
}
