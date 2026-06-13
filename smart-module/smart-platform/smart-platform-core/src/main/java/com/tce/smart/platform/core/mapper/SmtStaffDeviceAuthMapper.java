package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmtStaffDeviceAuthMapper extends BaseMapper<SmtStaffDeviceAuth> {

	/**
	 * 根据园区ID查询保密区权限
	 * @param parkId
	 * @return
	 */
	List<SmtStaffDeviceAuth> getSecurityAuth(@Param("parkId") Integer parkId);

}
