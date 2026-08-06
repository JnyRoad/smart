package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.AutoAllotRoomReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.*;
import com.tce.smart.platform.api.dto.resp.*;
import com.tce.smart.platform.api.dto.resp.dormitorymange.*;
import com.tce.smart.platform.core.dto.dormitorymanage.DormitoryRoomAttrDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import com.tce.smart.platform.core.entity.ext.DormitoryRoomExt;
import com.tce.smart.platform.core.model.*;
import com.tce.smart.platform.core.vo.DormitoryCountJche;
import com.tce.smart.platform.core.vo.DormitoryRoomVO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 园区宿舍楼中每个楼层的房间信息
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:10
 */
public interface SmtDormitoryRoomService extends IService<SmtDormitoryRoom> {

	Boolean updateDormitoryRoomById(SmtDormitoryRoom smtDormitoryRoom);

	List<DormitoryCountByFloor> countByFloor(FloorCountQueryReqDTO query);

	List<DormitoryCountBySex> countBySex(FloorCountQueryReqDTO query);

	List<DormitoryCountByType> countByType(Integer parkId);

	String getDormitoryStaffHistory(Integer parkId, String certno);

	List<DormitoryCountFloor> countFloor(Integer dormitoryId);

	/**
	 * 按园区统计入住情况
	 * @param parkId
	 * @return
	 */
	List<DormitoryCountListRespDTO> countList(Integer parkId);

	/**
	 * 按楼栋统计入住情况
	 * @param parkId
	 * @return
	 */
	List<DormitoryCountByBuildingRespDTO> countDormList(Integer parkId);

	/**
	 * 自动分配房间
	 * @param autoAllotRoomReqDTO
	 * @return
	 */
	List<DormitoryQuickStaffRespDTO> autoAllot(AutoAllotRoomReqDTO autoAllotRoomReqDTO, SmtDormitoryBedService smtDormitoryBedService);

	/**
	 * 再次打印凭条
	 * @param recordId
	 * @return
	 */
	DormitoryQuickStaffRespDTO printInfo(Long recordId);

	/**
	 * 推荐房间
	 * @param distReqDTO
	 * @return
	 */
	DormitoryDistRespDTO recommendBed(DormitoryDistReqDTO distReqDTO);

	/**
	 * 查询房间入住详情
	 * @param roomId
	 * @return
	 */
	List<DormitoryRoomDetailRespDTO> bedDetail(Integer roomId);

	/**
	 * 根据楼栋ID查询楼层和房间列表
	 * @param dormitoryId
	 * @return
	 */
	List<FloorRoomListRespDTO> getFloorRoomList(Integer dormitoryId);

	/**
	 * 根据楼层列表查询房间列表
	 * @param floors
	 * @return
	 */
	List<FloorRoomListRespDTO> getRoomListByFloors(List<Integer> floors);

	/**
	 * 根据条件查询房间列表
	 * @param dormitoryRoomReqDTO
	 * @return
	 */
	List<DormitoryRoomRespDTO> getRoomListByCondition(DormitoryRoomReqDTO dormitoryRoomReqDTO);

	/**
	 * 根据园区id获得房间统计
	 * @param parkId
	 * @return
	 */
	List<DormitoryStatisticsListRespDTO> getRoomStatistics(FloorStatisticsQueryReqDTO queryReqDTO);

	/**
	 * 根据园区id获得房间统计下载表格
	 * @param parkId
	 * @return
	 */
	ResponseEntity<byte[]> getRoomStatisticsExcel(FloorStatisticsQueryReqDTO queryReqDTO);

	List<DormitoryCountBuilding> countBuilding(Integer parkId);

	List<RoomBedRespDTO> queryRoom(SearchDormitoryRoomDetailReqDTO smtDormitoryRoom);

	List<SmtDormitoryRoom> queryRoomList(SmtDormitoryRoom smtDormitoryRoom);

	Result removeRoomById(Integer id);

	Result getSmtDormitoryRoomPage(Page page, SmtDormitoryRoom smtDormitoryRoom);

	List<DormitoryRoomVO> getSmtDormitoryRoomList(SmtDormitoryRoom smtDormitoryRoom);

	Result getRoomVisual(String floorList);

	/**
	 * 房间详情可视化查询
	 * @param searchDormitoryRoomDetailReqDTO
	 * @return
	 */
	Page<SearchDormitoryRoomDetailRespDTO> queryRoomVisual(SearchDormitoryRoomDetailReqDTO searchDormitoryRoomDetailReqDTO);

	List<DormitoryCountJche> countByjche(FloorCountQueryReqDTO query);

	/**
	 * 根据房间类型统计
	 * @param parkId
	 * @return
	 */
	List<DormitoryRoomExt> countByRoomType(Integer parkId);

	List<DormitoryCountByFloor> countByFree(FloorCountQueryReqDTO query);

	Integer getJcheFreeBed(Integer parkId, String badge);

	/**
	 * 批量修改房间水电模板
	 * @param dormitoryRoomAttrDTO
	 * @return
	 */
	Integer batchUpdateSDTemp(DormitoryRoomAttrDTO dormitoryRoomAttrDTO);


	/**
	 * 批量修改房间属性 已住人的房间不能修改
	 * @param smtDormitoryRoom
	 * @return
	 */
	Integer batchUpdateRoomAttr (SmtDormitoryRoom smtDormitoryRoom);

	/**
	 * 通过房间ID列表修改房间属性
	 * @return
	 */
	Integer batchUpdateRoomAttrByIds(DormitoryRoomAttrDTO dormitoryRoomAttrDTO);

	/**
	 * 获取空闲的宿舍房间数
	 * @return
	 */
	Integer getFreeRoomCount(Integer parkId);

	/**
	 * 床位数减1
	 * @return
	 */
	Boolean decrementBedNum(Integer roomId);

	/**
	 * 床位数加1
	 * @return
	 */
	Boolean incrementBedNum(Integer roomId);


	/**
	 * 通过楼栋ID和名称获取
	 * @param dormitoryId
	 * @param roomName
	 * @return
	 */
	SmtDormitoryRoom getByDormitoryAndName(Integer dormitoryId, Integer roomName);

}
