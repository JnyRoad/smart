package com.tce.smart.platform.core.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtParkingCorrection;
import com.tce.smart.platform.core.vo.ParkingCorrectionVO;

/**
 * 停车场车位校正表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:31:55
 */
public interface SmtParkingCorrectionMapper extends BaseMapper<SmtParkingCorrection> {

	/**
	 * 获取车位统计信息
	 * @return
	 */
	ParkingCorrectionVO getParkingCountInfo(@Param("parkId") Integer parkId);
}
