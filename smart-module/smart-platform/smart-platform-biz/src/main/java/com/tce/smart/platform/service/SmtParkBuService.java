package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtParkBu;

import java.util.List;

/**
 * 园区BU关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
public interface SmtParkBuService extends IService<SmtParkBu> {

	List<SmtPark> getParkListByBu(Long compId);

	/**
	 * 获得用户当前可见园区
	 * @param compId
	 * @param parkIds
	 * @return
	 */
	List<SmtPark> getUserParkListByBu(Integer compId, List<Integer> parkIds);

	/**
	 * 根据园区ID查找关联关系
	 *
	 * @param parkId 园区ID
	 * @return List<SmtParkBu>  园区BU关系列表
	 */
	List<SmtParkBu> listByParkId(Integer parkId);

	/**
	 * 根据园区ID删除关联关系
	 *
	 * @param parkId 园区ID
	 * @return Boolean true-成功，fasle-失败
	 */
	Boolean removeByParkId(Integer parkId);

	List<OvwYscompRespDTO> getAllByParkId(Integer parkId);

	/**
	 * 修改园区BU关系(先删除再新增)
	 *
	 * @param parkId       园区ID
	 * @param workCompList BU编号列表
	 * @return Boolean true-成功，fasle-失败
	 */
	Boolean saveParkBu(Integer parkId, List<String> workCompList);

}
