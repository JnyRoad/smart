package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.DeviceDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.model.AreaTree;
import com.tce.smart.platform.core.vo.DeviceVO;
import com.tce.smart.platform.core.vo.GetDeviceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
public interface SmtDeviceMapper extends BaseMapper<SmtDevice> {
	/**
	 * 根据设备id查询设备信息
	 * @param id
	 * @return GetDeviceVO
	 */
	GetDeviceVO getDeviceById(String id);

	/**
	 * 查询设备信息
	 * @param page 分页对象
	 * @param deviceDTO 查询条件
	 * @return 返回设备集合
	 */
	IPage<DeviceVO> getDevice(Page page, @Param("query") DeviceDTO deviceDTO);

	/**
	 * 根据区域ID获取查询设备信息
	 * @param id 区域ID
	 * @return 返回设备集合
	 */
	List<SmtDevice> getByAreaId(Integer id);

	List<AreaTree> getPark(@Param("parkIds") List<Integer> parkIds);

	List<AreaTree> getArea(@Param("parkId") Integer parkId, @Param("pId") Integer pId);

	DeviceVO getDeviceDetail(@Param("deviceId") String deviceId);

	int deleteDeviceArea(@Param("deviceId") String deviceId);

	List<String> getDeviceIds(@Param("parkId")  Integer parkId);

	List<SmtDevice> selectDeviceByAuthId(@Param("authId")  Integer authId);
}
