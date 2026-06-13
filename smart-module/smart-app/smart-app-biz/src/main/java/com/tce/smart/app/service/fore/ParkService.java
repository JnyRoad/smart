package com.tce.smart.app.service.fore;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.app.ao.fore.LocationAo;
import com.tce.smart.app.vo.fore.ParkVo;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.dto.resp.SmtParkRespDTO;

import java.util.List;
import java.util.Map;

/**
 * 园区信息服务接口
 *
 * @author mingkai.wu
 * @date 2019-05-10 16:16:08
 */
public interface ParkService {

	/**
	 * 根据经纬度查询园区信息
	 *
	 * @param longitude 经纬度
	 * @return ParkVO 园区信息
	 */
	ParkVo processlocation(LocationAo locationAO);

	/**
	 * 获取园区列表
	 *
	 * @param params
	 * @param locationAo
	 * @return
	 */
	IPage<?> getParkList(Map<String, Object> params, LocationAo locationAo);

	/**
	 * 获取用户关联园区列表
	 * @return
	 */
	List<SmtParkRespDTO> getUserPark();

}
