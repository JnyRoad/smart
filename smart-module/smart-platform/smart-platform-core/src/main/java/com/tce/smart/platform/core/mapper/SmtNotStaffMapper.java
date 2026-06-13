package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtNotStaff;
import com.tce.smart.platform.core.vo.VehicleStaffVO;


/**
 * 非员工表
 *
 * @date 2019-04-13 18:18:42
 */
public interface SmtNotStaffMapper extends BaseMapper<SmtNotStaff> {


	/**
	 * 查询员工车辆信息
	 * @param cardNo 车辆ID
	 * @return 返回车辆集合
	 */
	VehicleStaffVO getByVehicleID(String cardNo);
}
