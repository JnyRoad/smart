package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtNotStaff;
import com.tce.smart.platform.core.vo.VehicleStaffVO;

/**
 * 非员工表
 *
 * @date 2019-04-13 18:18:42
 */
public interface SmtNotStaffService extends IService<SmtNotStaff> {

	/**
	 * 获取车主信息
	 * @param cardNo
	 * @return
	 */
	VehicleStaffVO getByVehicleID(String cardNo);
}
