package com.tce.smart.platform.core.mapper;

import java.util.List;

import com.tce.smart.platform.api.dto.req.AuthDetailQueryDTO;
import com.tce.smart.platform.api.dto.resp.AuthDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.AreaTypeConflictDeviceVO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.vo.DeviceAuthorityVO;

/**
 * 设备权限表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:34
 */
public interface SmtDeviceAuthorityMapper extends BaseMapper<SmtDeviceAuthority> {

	/**
	 * 查询设备权限信息
	 * @param page 分页对象
	 * @param smtDeviceAuthority 查询条件
	 * @return 返回车辆集合
	 */
	IPage getDeviceAuthority(Page page, @Param("query") SmtDeviceAuthority smtDeviceAuthority);

	IPage<DeviceAuthorityVO> getDeviceAuthPage(Page page,@Param("query") SmtDeviceAuthority smtDeviceAuthority,
			@Param("park") List<Integer> parkIdList);

	List<SmtDeviceAuthority> getByStaffId(@Param("staffId") String staffId);

	IPage<AuthDetailRespDTO> getPersonAuthDetailPage(Page page, @Param("query") AuthDetailQueryDTO queryDTO,@Param("badges") List<String> badges);

	IPage<AuthDetailRespDTO> getVehicleAuthDetailPage(Page page, @Param("query") AuthDetailQueryDTO queryDTO);

	Integer countByAreaType(@Param("areaType") Integer areaType, @Param("deviceId") String deviceId);

	/**
	 * 查询把某个权限组切换到目标性质后，会产生冲突的设备明细。
	 * 冲突定义：该设备同时被"性质不同"的其他权限组（authority_id != authorityId）引用。
	 *
	 * @param authorityId 待切换的权限组ID
	 * @param targetAreaType 目标权限性质 0-公共区域 1-保密区域
	 * @return 冲突设备明细（可能为空）
	 */
	List<AreaTypeConflictDeviceVO> findAreaTypeConflicts(@Param("authorityId") Integer authorityId,
														  @Param("targetAreaType") Integer targetAreaType);

}
