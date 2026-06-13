package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.dormitorymange.FloorCountQueryReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.FloorStatisticsQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryStatisticsRespDTO;
import com.tce.smart.platform.core.dto.dormitorymanage.DormitoryRoomDetailDTO;
import com.tce.smart.platform.core.dto.dormitorymanage.DormitoryRoomStayDTO;
import com.tce.smart.platform.core.dto.dormitorymanage.FloorRoomListDTO;
import com.tce.smart.platform.core.dto.dormitorymanage.SearchDormitoryRoomDetailDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryBed;
import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import com.tce.smart.platform.core.entity.ext.DormitoryRoomExt;
import com.tce.smart.platform.core.entity.ext.DormitoryStatisticsExt;
import com.tce.smart.platform.core.model.*;
import com.tce.smart.platform.core.vo.DormitoryCountJche;
import com.tce.smart.platform.core.vo.DormitoryRoomVO;
import com.tce.smart.platform.core.vo.RoomInfoVisualVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 园区宿舍楼中每个楼层的房间信息
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:10
 */
public interface SmtDormitoryRoomMapper extends BaseMapper<SmtDormitoryRoom> {

	Integer queryStaffByBed(SmtDormitoryBed bed);

	List<DormitoryCountByFloor> getCountByFloor(@Param("query") FloorCountQueryReqDTO query);

	List<DormitoryCountBySex> getCountBySex(@Param("query") FloorCountQueryReqDTO query);

	List<DormitoryCountByType> getCountByType(@Param("parkId") Integer parkId);

	List<DormitoryCountList> getCountList(@Param("parkId") Integer parkId);

	List<DormitoryCountByBuilding> getCountDormList(@Param("parkId") Integer parkId);

    List<DormitoryCountBuilding> getCountBuilding(@Param("parkId") Integer parkId,@Param("dormitoryIds") List<Integer> dormitoryIds);

    List<DormitoryCountFloor> getCountFloor(@Param("dormitoryId") Integer dormitoryId);

	List<SmtDormitoryRoom> queryRoom(Integer floorId);

	IPage<DormitoryRoomVO> getSmtDormitoryRoomPage(Page page, @Param("query") SmtDormitoryRoom smtDormitoryRoom, @Param("park") List<Integer> parkIdList);

	List<DormitoryRoomVO> getSmtDormitoryRoomList(@Param("query") SmtDormitoryRoom smtDormitoryRoom, @Param("park") List<Integer> parkIdList);

	List<RoomInfoVisualVO> getRoomVisual(@Param("floorId") Integer floorId);

	IPage<DormitoryRoomDetailDTO> getRoomVisualPage(Page page, @Param("query") SearchDormitoryRoomDetailDTO search, @Param("park") List<Integer> parkIdList);

	List<DormitoryCountJche> getCountByJche(@Param("query") FloorCountQueryReqDTO query);

	List<DormitoryRoomExt> getCountByRoomType(@Param("parkId") Integer parkId);

	List<DormitoryCountByFloor> getCountByFree(@Param("query") FloorCountQueryReqDTO query);

	Integer getJcheToalBed(@Param("parkId") Integer parkId, @Param("jcheId") String jcheId, @Param("sex") Integer sex);

	Integer getJcheUseBed(@Param("parkId") Integer parkId, @Param("jcheId") String jcheId, @Param("sex") Integer sex);

	Integer getFreeRoomCount(@Param("parkId")Integer  parkId);

	/**
	 * 通过房间号ID 查询房间当前的入住情况
	 * @param roomIds
	 * @return
	 */
	List<DormitoryRoomStayDTO> getDormitoryRoomStay(@Param("rooms") List<Integer> roomIds);

	List<FloorRoomListDTO> getFloorRoomList(@Param("dormitoryId") Integer dormitoryId);

	List<DormitoryRoomExt> getRoomListByCondition(@Param("room") SmtDormitoryRoom room,
												  @Param("freeBedNum")Integer freeBedNum,
												  @Param("typeIds")List<Integer> typeIds);

	List<DormitoryStatisticsExt> getRoomStatistics(@Param("query") FloorStatisticsQueryReqDTO query);

	Boolean decrementBedNum(@Param("roomId") Integer roomId);

	Boolean incrementBedNum(@Param("roomId") Integer roomId);

}
