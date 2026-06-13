package com.tce.smart.platform.core.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.VehicleAuthDTO;
import com.tce.smart.platform.core.entity.SmtVehicleStaff;
import com.tce.smart.platform.core.vo.VehicleStaffVO;
import org.apache.ibatis.annotations.Param;

/**
 * 车辆员工关联表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:13
 */
public interface SmtVehicleStaffMapper extends BaseMapper<SmtVehicleStaff> {


	/**
	 * 查询员工车辆信息
	 * @param cardNo 车辆ID
	 * @return 返回车辆集合
	 */
	VehicleStaffVO getByVehicleID(String cardNo);

	List<Long> getDeivceByStaffId(Long staffId);

	int deleteByStaffId(Long staffId);

	/**
	 * 通过车辆权限Id分页查询所有关联的员工车辆
	 * @param authId
	 * @return
	 */
	IPage<VehicleAuthDTO> getStaffVehicleByAuthId(Page page, @Param("authId")Integer authId);
}
