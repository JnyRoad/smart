package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtParkingCount;

/**
 * 车位统计表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:30:56
 */
public interface SmtParkingCountMapper extends BaseMapper<SmtParkingCount> {

	/**
	 * 获取停车场最新的车位信息
	 *
	 * @param parkingId 停车场Id
	 * @return 校验结果
	 */
	SmtParkingCount getByParkingId(String parkingId);
}
