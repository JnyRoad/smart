package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.dto.AreaDeviceDTO;
import com.tce.smart.platform.core.entity.SmtArea;
import com.tce.smart.platform.core.entity.SmtDeviceArea;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备区域关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:12:58
 */
public interface SmtDeviceAreaMapper extends BaseMapper<SmtDeviceArea> {

	/**
	 * 根据设备ID查询区域信息
	 *
	 * @param deviceId 设备ID
	 * @return 区域信息
	 */
	SmtArea queryByDeviceId(String deviceId);

	/**
	 * 查询区域设备数据
	 * @param parkIdList
	 * @return
	 */
	List<AreaDeviceDTO> queryAreaDevice(@Param("parkId") Integer parkId);
}
