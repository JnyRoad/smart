package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.app.ao.fore.AuthParkAo;
import com.tce.smart.app.service.fore.VehicleService;
import com.tce.smart.app.vo.fore.*;
import com.tce.smart.common.core.constant.PaginationConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.SmtVehicleRespDTO;
import com.tce.smart.platform.api.dto.req.AddVehicleReqDTO;
import com.tce.smart.platform.api.dto.req.ApplyAuthReqDTO;
import com.tce.smart.platform.api.dto.resp.VehicleApplyRespDTO;
import com.tce.smart.platform.api.feign.RemoteVehicleService;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.enums.VehicleColorEnum;
import com.tce.smart.tool.enums.VehicleTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.ImageUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 员工车辆管理接口实现
 * @author qipei
 *
 */
@Service
@AllArgsConstructor
@Slf4j
public class VehicleServiceImpl  implements VehicleService{

	private final RemoteVehicleService service;

	private final RemoteDictService remoteDictService;

	/**
	 * 获取员工的车辆信息
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IPage<?> getVehicleList(Map<String, Object> params) {
		// TODO Auto-generated method stub
		if(!params.containsKey(PaginationConstants.CURRENT) || !params.containsKey(PaginationConstants.SIZE)) {
			throw new TCEException(ExceptionTypeEnum.LACK_PAGE_PARAMETER);
		}

		//获取员工号
		String userName=SecurityUtils.getUser().getUsername();

		Result<Page<SmtVehicleRespDTO>> result = service.getMyVehicle(MapUtil.getLong(params, PaginationConstants.CURRENT),
				MapUtil.getLong(params, PaginationConstants.SIZE), userName,SecurityConstants.FROM_IN);

		log.info("remote getMyVehicle result=[{}]", result);
		IPage<SmtVehicleRespDTO> pageInfo = result.getData();
		//获取车辆类型字典表集合
//		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.VEHICLE_TYPE, SecurityConstants.FROM_IN);

		if (CollectionUtils.isNotEmpty(pageInfo.getRecords())) {
			@SuppressWarnings("rawtypes")
			List vehicleList = new ArrayList();
			VehicleVo vehicleVo = null;
			SmtVehicleRespDTO smtVehicle = null;
			for (int i = 0; i < pageInfo.getRecords().size(); i++) {
				vehicleVo = new VehicleVo();
				smtVehicle = pageInfo.getRecords().get(i);
				vehicleVo.setPlateNumber(smtVehicle.getVehiclePlate());
				vehicleVo.setVehicleBrand(StrUtil.isNotEmpty(smtVehicle.getVehicleBrand())?smtVehicle.getVehicleBrand():"");
				//从枚举中获取颜色
				if(ObjectUtil.isNotNull(smtVehicle.getVehicleColor())) {
					for(VehicleColorEnum alarmType : VehicleColorEnum.values()){
		                if(alarmType.getCode().equals(smtVehicle.getVehicleColor())){
			vehicleVo.setVehicleColorDesc(alarmType.getDesc());
			break;
		                }
		            }
				}else {
					vehicleVo.setVehicleColorDesc("");
				}
				if(ObjectUtil.isNotNull(smtVehicle.getVehicleType())) {
				//判断集合是否为空
					for(VehicleTypeEnum alarmType : VehicleTypeEnum.values()){
		                if(alarmType.getCode().equals(smtVehicle.getVehicleType())){
			vehicleVo.setVehicleTypeDesc(alarmType.getDesc());
			break;
		                }
		            }
				}else {
					vehicleVo.setVehicleTypeDesc("");
				}
//				if(findByType.getData().size()>0) {
//					for (int j = 0; j < findByType.getData().size(); j++) {
//						String value=findByType.getData().get(j).getValue();
//						if(value.equals(smtVehicle.getVehicleType().toString()))
//						{
//							vehicleVo.setVehicleTypeDesc(findByType.getData().get(j).getLabel());
//							break;
//						}
//					}
//				}
				vehicleList.add(vehicleVo);
			}
			pageInfo.setRecords(vehicleList);
		}
		return pageInfo;
	}

	/**
	 * 获取车辆通行权限
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<?> getAuthPark(AuthParkAo ao) {
		// TODO Auto-generated method stub
		log.info("remote getVehiclePark plateNumber=",ao.getPlateNumber());
		String plateNumber=ao.getPlateNumber();
		Result<List<VehicleApplyRespDTO>> result = service.getVehiclePark(plateNumber,SecurityConstants.FROM_IN);
		log.info("remote getAuthPark result=[{}]", result);
		List<VehicleApplyRespDTO> list=result.getData();
		List<AuthParkVo> vehicleList = new ArrayList<AuthParkVo>();
		if (list.size()>0) {
			AuthParkVo authParkVo = null;
			VehicleApplyRespDTO vehicleApplyVO = null;
			for (int i = 0; i < list.size(); i++) {
				vehicleApplyVO = list.get(i);
				authParkVo=new AuthParkVo();
				if(vehicleApplyVO!=null)
				{
					authParkVo.setParkName(vehicleApplyVO.getParkName());
					authParkVo.setVehicleAuthkId(vehicleApplyVO.getId().toString());
					authParkVo.setReason(vehicleApplyVO.getReason());
					if(vehicleApplyVO.getStatus().equals(0))
						authParkVo.setAuthDesc("审批中");
					else if(vehicleApplyVO.getStatus().equals(1))
						authParkVo.setAuthDesc("已审批");
					else if(vehicleApplyVO.getStatus().equals(2))
						authParkVo.setAuthDesc("已拒绝");

					vehicleList.add(authParkVo);
				}
			}
		}
		return vehicleList;
	}

	/**
	 * 查看车辆通行权限详情
	 */
	@Override
	public AuthDetailVo getAuthDetail(AuthParkAo ao ) {
		// TODO Auto-generated method stub

		Integer id= Integer.parseInt(ao.getVehicleAuthkId());
		Result<SmtVehicleRespDTO> result = service.getVehicleParkById(id,SecurityConstants.FROM_IN);
		log.info("remote getVehicleParkById result=[{}]", result);

//		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.VEHICLE_TYPE, SecurityConstants.FROM_IN);

		SmtVehicleRespDTO smtVehicle = result.getData();
		AuthDetailVo vo=new AuthDetailVo();
		vo.setPlateNumber(smtVehicle.getVehiclePlate());
		vo.setVehicleBrand(smtVehicle.getVehicleBrand());
		vo.setCarDrivingLicence(ImageUtils.changeFullBase64(smtVehicle.getDrivinglLicenseId()));
		vo.setDrivingLicence(ImageUtils.changeFullBase64(smtVehicle.getDriverLicenseId()));
		vo.setReason(smtVehicle.getReason());
		//从枚举中获取颜色
		for(VehicleColorEnum alarmType : VehicleColorEnum.values()){
            if(alarmType.getCode().equals(smtVehicle.getVehicleColor())){
	vo.setVehicleColorDesc(alarmType.getDesc());
	break;
            }
        }
		//判断集合是否为空
		for(VehicleTypeEnum alarmType : VehicleTypeEnum.values()){
            if(alarmType.getCode().equals(smtVehicle.getVehicleType())){
	vo.setVehicleTypeDesc(alarmType.getDesc());
	break;
            }
        }
//		if(findByType.getData().size()>0) {
//			for (int j = 0; j < findByType.getData().size(); j++) {
//				String value = findByType.getData().get(j).getValue();
//				if(value.equals(smtVehicle.getVehicleType().toString()))
//				{
//					vo.setVehicleTypeDesc(findByType.getData().get(j).getLabel());
//					break;
//				}
//			}
//		}
		return vo;
	}


	/**
	 * 车辆入园申请
	 */
	@Override
	public Result addAuthApply(ApplyAuthReqDTO applyAuthDTO) {
		// TODO Auto-generated method stub
		String badge=SecurityUtils.getUser().getUsername();
		applyAuthDTO.setBadge(badge);
		log.info("remote addVehiclePark applyAuthDTO=[{}]", applyAuthDTO);
		Result<?> result = service.addVehiclePark(applyAuthDTO,SecurityConstants.FROM_IN);
		log.info("remote addVehiclePark result=[{}]", result);
		return result;
	}

	/**
	 * 员工添加车辆
	 */
	@Override
	public Result addVehicle(AddVehicleReqDTO addVehicleDTO) {
		// TODO Auto-generated method stub
		log.info("员工添加车辆: 车牌号:{}  工号:{}", addVehicleDTO.getPlateNumber(),addVehicleDTO.getBadge());
		String badge = SecurityUtils.getUser().getUsername();
		addVehicleDTO.setBadge(badge);
		Result<?> result = service.addVehicle(addVehicleDTO,SecurityConstants.FROM_IN);
		log.info("员工添加车辆处理结果:{}", result);
		return result;
	}

	/**
	 * 车辆颜色列表
	 */
	@Override
	public List getColorType() {
		//获取颜色字典表集合
		//Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.VEHICLE_COLOR, SecurityConstants.FROM_IN);
		List<ColorTypeVo> colorList=new ArrayList<ColorTypeVo>();
		for(VehicleColorEnum alarmType : VehicleColorEnum.values()){
			ColorTypeVo color=new ColorTypeVo();
			color.setColorCode(alarmType.getCode().toString());
			color.setColorName(alarmType.getDesc());
			colorList.add(color);
        }
		return colorList;
	}

	/**
	 * 车辆类型
	 */
	@Override
	public List getVehicleType() {
		// TODO Auto-generated method stub

//		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.VEHICLE_TYPE, SecurityConstants.FROM_IN);

		List<VehicleTypeVo> typeList=new ArrayList<VehicleTypeVo>();
		VehicleTypeVo type=null;
		for(VehicleTypeEnum alarmType : VehicleTypeEnum.values()){
			type=new VehicleTypeVo();
			type.setTypeCode(alarmType.getCode().toString());
			type.setTypeName(alarmType.getDesc());
			typeList.add(type);
        }
//		if(findByType.getData().size()>0) {
//			for (int j = 0; j < findByType.getData().size(); j++) {
//				type=new VehicleTypeVo();
//				type.setTypeCode(findByType.getData().get(j).getValue());
//				type.setTypeName(findByType.getData().get(j).getLabel());
//				typeList.add(type);
//			}
//		}
		return typeList;
	}

	@Override
	public Result delete(AuthParkAo ao) {
		log.info("remote delVehicle plateNumber=",ao.getPlateNumber());
		String plateNumber=ao.getPlateNumber();
		Result result = service.delVehicle(plateNumber,SecurityConstants.FROM_IN);
		log.info("remote delVehicle result=[{}]", result);
		return result;
	}

}
