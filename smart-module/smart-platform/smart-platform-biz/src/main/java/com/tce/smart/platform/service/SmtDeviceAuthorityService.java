package com.tce.smart.platform.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.AreaTypeSwitchReqDTO;
import com.tce.smart.platform.api.dto.req.AuthDetailQueryDTO;
import com.tce.smart.platform.api.dto.req.DeviceAuthRelationAddReqDTO;
import com.tce.smart.platform.api.dto.req.DeviceAuthRelationDelReqDTO;
import com.tce.smart.platform.api.dto.resp.AreaTypeSwitchRespDTO;
import com.tce.smart.platform.api.dto.resp.AuthDetailRespDTO;
import com.tce.smart.platform.core.dto.SmtDeviceAuthorityDTO;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.model.DeviceTree;
import com.tce.smart.platform.core.vo.DeviceAuthorityVO;

/**
 * 设备权限表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:34
 */
public interface SmtDeviceAuthorityService extends IService<SmtDeviceAuthority> {

	/**
	 * 查询设备权限信息
	 * @param page 分页对象
	 * @param entity 查询条件
	 * @return 返回设备权限集合
	 */
	IPage getDeviceAuthority(Page page, SmtDeviceAuthority entity);

	/**
	 * 获取权限策略详情
	 * @param id 权限策略ID
	 * @return 返回结果
	 */
	SmtDeviceAuthorityDTO getDeviceAuthorityById(Integer id,List<Integer> parkIds);

	List<SmtDeviceAuthority> getByStaffId(String staffId);

	/**
	 * 添加设备权限信息
	 * @param entity 设备权限信息
	 * @return 返回结果
	 */
	boolean saveDeviceAuthority(SmtDeviceAuthorityDTO entity);

	/**
	 * 更新设备权限信息
	 * @param entity 设备权限信息
	 * @return 返回结果
	 */
	boolean updateDeviceAuthority(SmtDeviceAuthorityDTO entity,List<Integer> parkIds);

	/**
	 * 删除设备权限信息
	 * @param id 设备权限ID
	 * @return 返回结果
	 */
	Result deleteDeviceAuthority(Integer id,List<Integer> parkIds);

	/**
     * 根据区域信息
     * @param type 权限类型
	 * @param  parkIds 园区ID
     * @return 区域集合信息
     */
    List<DeviceTree> getTree(Integer type,List<Integer> parkIds);

	List<DeviceTree> getTrees(List<Integer> parkIds,Integer areaType);

	List<SmtDeviceAuthority> list(Integer type, Integer parkId, String name);

	List<SmtDeviceAuthority> listAll(Integer parkId);

	IPage<DeviceAuthorityVO> page(Page page, SmtDeviceAuthority smtDeviceAuthority);

	IPage<AuthDetailRespDTO> getAuthDetailPage(Page page, AuthDetailQueryDTO queryDTO);

	Boolean deviceAuthRelationDel(DeviceAuthRelationDelReqDTO reqDTO);

	Boolean deviceAuthRelationClear(Integer id);

	Boolean checkIsUsed(Integer type, String deviceId);

	List<String> deviceAuthRelationAdd(DeviceAuthRelationAddReqDTO reqDTO);

	/**
	 * 撤销某权限组下所有人员/车辆在指定设备上的访问权限，权限组绑定的其他设备不受影响。
	 * 用于设备下线时按设备精确回收权限，不删除员工/车辆与权限组的绑定关系本身。
	 * @param authorityId 权限组ID
	 * @param deviceId 要撤销权限的设备ID
	 */
	void revokeDeviceAccess(Integer authorityId, String deviceId);

	/**
	 * 变更通关权限性质（公共区域/保密区域）。
	 * 若组内设备存在跨权限组的性质冲突，直接拒绝并返回冲突设备清单，不写库。
	 *
	 * @param reqDTO 目标权限组ID + 目标性质
	 * @param parkIds 当前用户可操作的园区范围
	 * @return 切换结果
	 */
	Result<AreaTypeSwitchRespDTO> switchAreaType(AreaTypeSwitchReqDTO reqDTO, List<Integer> parkIds);
}
