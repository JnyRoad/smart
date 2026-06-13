package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtVehicleApply;
import com.tce.smart.platform.core.model.VehicleList;
import com.tce.smart.platform.core.vo.VehicleVO;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtVehicleApplyService;
import com.tce.smart.tool.constant.VehicleApplyConstants;
import com.tce.smart.tool.enums.VehicleTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class VehicleListWrapper extends BaseWrapper<VehicleVO, VehicleList> {

	@Autowired
	private SmtParkService smtParkService;

	@Autowired
	private SmtVehicleApplyService smtVehicleApplyService;

    @Override
    protected VehicleList warp(VehicleVO vehicle) throws IOException {
	VehicleList vehicleList = new VehicleList();
	BeanUtil.copyProperties(vehicle, vehicleList);
	vehicleList.setVehicleTypeName(VehicleTypeEnum.desc(vehicle.getVehicleType()));
	List<Integer> parkIds=new ArrayList<Integer>();
	//查询车辆已被审批的集合
	List<SmtVehicleApply> listVehicleApply = smtVehicleApplyService.list(Wrappers.<SmtVehicleApply>query().lambda().eq(SmtVehicleApply::getVehicleId, vehicle.getId()).eq(SmtVehicleApply::getStatus, VehicleApplyConstants.APPROVED));
	if(listVehicleApply.size()>0)
	{
		//不要用下面这个集合做计算 否则将改变基础数据
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		//SmtStaff staff = staffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, vehicle.getBadge()));
	//List<SmtPark> parkList = smtParkBuService.getParkListByBu(Integer.parseInt(staff.getCompId()));
			//List<Integer> parks = smtVehicleService.splitStringToInteger(vehicle.getParkId());
		//车辆允许进入的园区集合
		List<Integer> applyPark=new ArrayList<>();
			for (SmtVehicleApply smtVehicleApply : listVehicleApply) {
				applyPark.add(Integer.parseInt(smtVehicleApply.getParkId()));
			}
			List<Integer> retailList = new ArrayList<>();
			retailList.addAll(parkIdList);
			//获取两个集合的交集
			retailList.retainAll(applyPark);

			List<SmtPark> parkList = smtParkService.list(Wrappers.<SmtPark>query().lambda().in(SmtPark::getId, retailList));
	 String parkName="";
		 for (SmtPark smtPark : parkList) {
			 parkName+=smtPark.getParkName()+",";
			 parkIds.add(smtPark.getId());
		}
		 if(!parkName.equals("")) {
			 parkName=parkName.substring(0,parkName.length()-1);
				 vehicleList.setParkIds(parkIds);
		 }
		 vehicleList.setParkName(parkName);

	}
        return vehicleList;
    }
}
