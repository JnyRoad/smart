package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryRoomReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.SearchDormitoryRoomDetailReqDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkDataRespDTO;
import com.tce.smart.platform.core.dto.DormitoryTreeDTO;
import com.tce.smart.platform.core.dto.meter.DormitoryLazyQueryDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import com.tce.smart.platform.core.entity.SmtPark;

import java.util.List;

/**
 * 园区表
 *
 * @author 齐佩
 * @date 2019-04-13 13:48:12
 */
public interface SmtParkService extends IService<SmtPark> {

	Result addPark(SmtPark smtPark);

	Result updateParkById(SmtPark smtPark);

	Result removeParkById(Integer id);

	/**
	 * 查询当前登录用户关联的园区
	 * @return
	 */
	List<SmtPark> getParkList();

	/**
	 * 查询宿管账号所有园区
	 * @return
	 */
	List<SmtPark> getDormitoryParks();

	/**
	 * 获取未过滤的园区信息
	 * @return 园区列表
	 */
	List<SmtPark> getUnStrainedParks();

	Result allList();

	/**
	 * 获取账号对应的园区
	 * @return
	 */
	Result lazyPark(DormitoryLazyQueryDTO queryDTO);

	/**
	 * 获取宿舍树形结构 不保护房间
	 * @return
	 */
	List<DormitoryTreeDTO> getDormitoryTreeNonRoom();

	/**
	 * 根据条件 查询宿舍房间的结构树
	 * @param roomDetailReqDTO
	 * @return
	 */
	List<DormitoryTreeDTO> dormRoomTree(SearchDormitoryRoomDetailReqDTO roomDetailReqDTO);

	/**
	 * 根据条件 查询宿舍房间的结构树
	 * @param roomDetailReqDTO
	 * @return
	 */
	List<DormitoryTreeDTO> roomTree(Integer parkId);

	/**
	 * 根据房间ID 查询宿舍房间的结构树
	 * @param roomId
	 * @return
	 */
	List<DormitoryTreeDTO> dormRoomTreeByRoomId(Integer roomId);

	/**
	 * 查询园区到房间的树行结构
	 * @param queryRoom
	 * @param parkId
	 * @return
	 */
	List<DormitoryTreeDTO> getDormRoomTree(List<? extends SmtDormitoryRoom> queryRoom, Integer parkId);

	/**
	 * 根据申请Id 查询宿舍房间的结构树
	 * @param applyId
	 * @return
	 */
	List<DormitoryTreeDTO> dormRoomTreeByApplyId(Long applyId);

	/**
	 * 根据条件查询房间树型结构
	 * @param dormitoryRoomReqDTO
	 * @return
	 */
	List<DormitoryTreeDTO> getRoomTreeByCondition(DormitoryRoomReqDTO dormitoryRoomReqDTO);

	/**
	 * 通过经纬度查询园区
	 *
	 * @param smtPark
	 * @return SmtPark 园区信息
	 */
	SmtPark locationPark(SmtPark smtPark);

	/**
	 * 获取园区宿舍信息
	 * @return
	 */
	Result statistics(SmtDormitoryBedService bedService, SmtDormitoryStaffService dormitoryStaffService,Integer parkId);

	Result dormitoryAllList(SmtDormitoryStaffService dormitoryStaffService);


	Result dormitoryAllListToLock(SmtDormitoryStaffService dormitoryStaffService, Integer parkId);

	/** 按已验证的遗留调用方园区范围查询门锁宿舍树，空集合不得回退为全园区。 */
	Result dormitoryAllListToLock(SmtDormitoryStaffService dormitoryStaffService, List<Integer> parkIds);

	/**
	 * 获取园区数据
	 * @return
	 */
	ParkDataRespDTO getParkData();
}
