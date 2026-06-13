package com.tce.smart.platform.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtParkingCount;
import com.tce.smart.platform.core.mapper.SmtParkingCountMapper;
import com.tce.smart.platform.service.SmtParkingCountService;

import lombok.AllArgsConstructor;

/**
 * 车位统计表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:30:56
 */
@Service
@AllArgsConstructor
public class SmtParkingCountServiceImpl extends ServiceImpl<SmtParkingCountMapper, SmtParkingCount> implements SmtParkingCountService {

	private final SmtParkingCountMapper smtParkingCountMapper;

	/**
	 * 获取停车场最新的车位信息
	 *
	 * @param parkingId 停车场Id
	 * @return 校验结果
	 */
	@Override
	public Result getByParkingId(String parkingId) {
		if(parkingId == null) {
			return new Result<>(false,"园区ID不可为空");
		}
		return new Result<>(smtParkingCountMapper.getByParkingId(parkingId));
	}

}
