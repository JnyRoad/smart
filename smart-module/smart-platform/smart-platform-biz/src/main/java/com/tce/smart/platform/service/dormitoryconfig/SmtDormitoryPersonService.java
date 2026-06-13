package com.tce.smart.platform.service.dormitoryconfig;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.dormitoryconfig.DormitoryPersonReqDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.dormitoryconfig.SmtDormitoryPerson;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-09-14 20:14:59
 */
public interface SmtDormitoryPersonService extends IService<SmtDormitoryPerson> {

	/**
	 * 编辑宿管人员楼栋权限
	 * @param personList
	 * @return
	 */
	Boolean editPerson(List<DormitoryPersonReqDTO> personList, Long configId);

	/**
	 * 根据配置id获得人员楼栋权限
	 * @param configId
	 * @return
	 */
	List<SmtDormitoryPerson> getByConfigId(Long configId);

	/**
	 * 根据人员获得账号楼栋权限
	 * @return
	 */
	List<Integer> getDormitoryId(String account, Integer parkId);

	/**
	 * 根据人员获得园区权限
	 * @param account
	 * @return
	 */
	List<Integer> getParkId(String account);

}
