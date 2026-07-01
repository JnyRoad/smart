package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.GenerateStatementDTO;
import com.tce.smart.platform.core.dto.SmtSdMeterreadDTO;
import com.tce.smart.platform.core.dto.StaffInRoomNumDTO;
import com.tce.smart.platform.core.dto.commonsd.DormitorySDMeterreadDTO;
import com.tce.smart.platform.core.entity.SmtSdMeterread;
import com.tce.smart.platform.core.vo.SmtSdMeterreadVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description: SmtSdMeterreadMapper
 * @date: 2020-07-10
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSdMeterreadMapper extends BaseMapper<SmtSdMeterread> {

	/**
	 * 分页查询房间抄表记录
	 * @param page
	 * @param smtSdTemplates
	 * @param parkIdList
	 * @return
	 */
	IPage<SmtSdMeterreadVO> getSDMeterreadPage(Page page, @Param("query") SmtSdMeterreadDTO smtSdTemplates, @Param("park") List<Integer> parkIdList);

	/**
	 * 查询所有
	 * @param smtSdTemplates
	 * @param parkIdList
	 * @return
	 */
	List<SmtSdMeterreadVO> getRoomSDMeterread(@Param("query") SmtSdMeterreadDTO smtSdTemplates, @Param("park") List<Integer> parkIdList);

	/**
	 * 查询楼层已生成水电记录的数据
	 * @param smtSdMeterreadReqDTO
	 * @return
	 */
	List<SmtSdMeterreadVO> getFloorMeterRecord(@Param("query") SmtSdMeterreadDTO smtSdMeterreadReqDTO);

	/**
	 * 查询房间水电记录数据
	 * @param id
	 * @return
	 */
	SmtSdMeterreadVO getRoomMeterRecord(@Param("id") Long id);

	/**
	 * 查询需要结算的数据
	 * @param parkIdList
	 * @return
	 */
	List<GenerateStatementDTO> getNeedStatementRecord(@Param("dormitoryId") Integer dormitoryId,@Param("park") List<Integer> parkIdList);


	/**
	 * 查询房间的抄表数据
	 * @param roomId
	 * @param meterMonth
	 * @return
	 */
	List<DormitorySDMeterreadDTO> getDormitorySDMeterread(@Param("roomId")Integer roomId,@Param("date") Date meterMonth);

	/**
	 * 查询房间列表对应的抄表数据
	 * @param roomIds
	 * @param meterMonth
	 * @return
	 */
	List<DormitorySDMeterreadDTO> getRoomsSDMeterread(@Param("roomIds")List<Integer> roomIds,@Param("date") Date meterMonth);


	/**
	 * 按楼层查询所有房间的水电抄表数据
	 * @param floorId
	 * @param meterMonth
	 * @return
	 */
	List<DormitorySDMeterreadDTO> getFloorSDMeterread(@Param("floorId")Integer floorId,@Param("date") Date meterMonth);

	List<DormitorySDMeterreadDTO> getFloorSDMeterreadNew(@Param("dormitoryId")Integer dormitoryId,@Param("floorId")Integer floorId,
														 @Param("roomId")Integer roomId,@Param("date") Date meterMonth,@Param("dormitoryIds")List<Integer> dormitoryIds);

	Integer getInRoomNum(@Param("badge")String badge,@Param("startTime") Date startTime,@Param("endTime") Date endTime);

	/**
	 * getInRoomNum 的批量版本，一次查询返回多个工号的住房数量，避免调用方按工号循环查询数据库。
	 * @param badges 工号列表，单批不超过 1000 个（Oracle IN 子句上限，由调用方分批）
	 * @param startTime 月份起始时间
	 * @param endTime 月份结束时间
	 * @return 每个有记录的工号对应一条结果；查不到记录的工号不会出现在结果里，调用方需自行兜底默认值
	 */
	List<StaffInRoomNumDTO> getInRoomNumBatch(@Param("badges") List<String> badges, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
