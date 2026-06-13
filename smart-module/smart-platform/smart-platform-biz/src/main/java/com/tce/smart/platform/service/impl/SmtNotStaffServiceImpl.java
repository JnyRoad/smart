package com.tce.smart.platform.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtNotStaff;
import com.tce.smart.platform.core.vo.VehicleStaffVO;
import com.tce.smart.platform.core.mapper.SmtNotStaffMapper;
import com.tce.smart.platform.service.SmtNotStaffService;

import lombok.extern.slf4j.Slf4j;

/**
 * 非员工表
 *
 * @date 2019-04-13 18:18:42
 */
@Service
@Slf4j
public class SmtNotStaffServiceImpl extends ServiceImpl<SmtNotStaffMapper, SmtNotStaff> implements SmtNotStaffService {

	@Override
	public VehicleStaffVO getByVehicleID(String cardNo) {
		return this.baseMapper.getByVehicleID(cardNo);
	}

}
