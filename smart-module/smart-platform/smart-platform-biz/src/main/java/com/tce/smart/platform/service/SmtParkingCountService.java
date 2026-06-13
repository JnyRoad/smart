package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtParkingCount;

/**
 * 车位统计表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:30:56
 */
public interface SmtParkingCountService extends IService<SmtParkingCount> {

	/**
	 * 获取停车场最新的车位信息
	 *
	 * @param parkingId 停车场ID
	 * @return 结果
	 */
    Result getByParkingId(String parkingId);
}
