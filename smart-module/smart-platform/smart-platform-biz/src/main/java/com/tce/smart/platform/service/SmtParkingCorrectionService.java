package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtParkingCorrection;
import com.tce.smart.platform.core.vo.ParkingCorrectionVO;

/**
 * 停车场车位校正表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:31:55
 */
public interface SmtParkingCorrectionService extends IService<SmtParkingCorrection> {

	/**
	 * 校验停车场车位，并同时更新校验后的数据至车位统计表
	 *
	 * @param entity 校验车位信息
	 * @return 校验结果
	 */
    boolean saveOrUpdateParkingCorrection(SmtParkingCorrection entity);

	/**
	 * 系统重启时调取方法更新车库信息
	 * @return 校验结果
	 */
    boolean initParkingCorrection();

	/**
	 * 获取车位统计信息
	 * @return
	 */
    ParkingCorrectionVO getParkingCountInfo(Integer parkId);
}
