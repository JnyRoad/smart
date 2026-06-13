package com.tce.smart.platform.service.settlement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.DormitoryMeterQueryDTO;
import com.tce.smart.platform.api.dto.req.RoomMeterQueryDTO;
import com.tce.smart.platform.api.dto.req.SmtSdMeterreadReqDTO;
import com.tce.smart.platform.api.dto.req.sddto.RoomSDMeterreadReqDTO;
import com.tce.smart.platform.api.dto.req.sddto.SdMeterreadDetailReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.DormitorySDMeterreadNewRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.DormitorySDMeterreadRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.RoomSDMeterreadRespDTO;
import com.tce.smart.platform.core.dto.SmtSdMeterreadDTO;
import com.tce.smart.platform.api.dto.StatementDetailDTO;
import com.tce.smart.platform.core.entity.SmtSdMeterread;
import com.tce.smart.platform.core.entity.SmtStaffStatementDetail;
import com.tce.smart.platform.core.vo.SmtSdMeterreadVO;
import com.tce.smart.platform.service.settlement.SmtCommonSDMeterreadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description: SmtSdMeterreadService
 * @date: 2020-07-10 9:45
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSdMeterreadService extends IService<SmtSdMeterread> {
	/**
	 * 根据条件查询房间水电抄表记录
	 * @param page
	 * @param smtSdMeterreadReqDTO
	 * @return
	 */
	IPage<SmtSdMeterreadVO> getSDMeterreadPage(Page page, SmtSdMeterreadReqDTO smtSdMeterreadReqDTO);

	/**
	 * 根据条件查询房间水电抄表记录
	 * @param smtSdMeterreadReqDTO
	 * @return
	 */
	List<SmtSdMeterreadVO> getRoomSDMeterreadInfo(SmtSdMeterreadReqDTO smtSdMeterreadReqDTO);

	/**
	 * 查询房间抄表信息
	 * @param roomId
	 * @return
	 */
	DormitorySDMeterreadRespDTO getRoomSDMeterread(Integer roomId, Date meterMonth, SmtCommonSDMeterreadService smtCommonSDMeterreadService);

	/**
	 * 查询房间指定月份的公摊水电数据
	 * @param roomId
	 * @param meterMonth
	 * @return
	 */
	List<DormitorySDMeterreadRespDTO.CommonCate> queryRoomCommonSDInfo(Integer roomId,Date meterMonth,SmtCommonSDMeterreadService smtCommonSDMeterreadService);

	/**
	 * 按楼层查询所有房间的抄表信息
	 * @param floorId
	 * @return
	 */
	List<DormitorySDMeterreadRespDTO> getFloorSDMeterread(Integer floorId, Date meterMonth);

	/**
	 * 按楼层查询所有房间的抄表信息
	 * @param roomMeterQueryDTO
	 * @return
	 */
	List<DormitorySDMeterreadNewRespDTO> getFloorSDMeterreadNew(RoomMeterQueryDTO roomMeterQueryDTO);

	/**
	 * 查询楼栋的抄表数据
	 * @param dormitoryMeterQueryDTO
	 * @return
	 */
	List<DormitorySDMeterreadNewRespDTO> getDormitorySDMeterread(DormitoryMeterQueryDTO dormitoryMeterQueryDTO);

	/**
	 * 批量抄表
	 * @param detailReqDTOS
	 * @return
	 */
	Boolean saveBatchSDMeterread(List<SdMeterreadDetailReqDTO> detailReqDTOS);

	/**
	 * 生成房间抄表记录
	 * @param smtSdMeterreadDTO
	 * @return
	 */
	Boolean addSDMeterreadRecord(SmtSdMeterreadDTO smtSdMeterreadDTO);

	/**
	 * 生成所有已抄表的房间水电明细
	 * @return
	 */
	String generateSDStatementDetail(Integer dormitoryId);

	/**
	 * 查询房间水电结算详情
	 * @return
	 */
	StatementDetailDTO queryRoomStatementDetail(Long mrId);

	/**
	 * 查询房间抄表状态
	 * @param roomSDMeterreadReqDTO
	 * @return
	 */
	RoomSDMeterreadRespDTO queryRoomMeterStatus(RoomSDMeterreadReqDTO roomSDMeterreadReqDTO);

	/**
	 * 查询房间列表某月的入住详情
	 * @param roomIds
	 * @param starTime
	 * @param endTime
	 * @return
	 */
	Map<String,List<SmtStaffStatementDetail>> getRoomStayData(List<Integer> roomIds, Date starTime,Date endTime);

	/**
	 * 获取楼栋水电导入模板
	 * @param dormitoryIds
	 * @return
	 */
	ResponseEntity<byte[]> getSdImportTemplate(String meterMonth,List<Integer> dormitoryIds);

	/**
	 * 宿舍水电导入
	 * @param meterMonth
	 * @param multipartFile
	 * @return
	 */
	ResponseEntity<byte[]> importDormitorySd(String meterMonth, MultipartFile multipartFile, HttpServletResponse httpServletResponse);

	/**
	 * 查询员工本月的入住房间数
	 * @param badge
	 * @param meterMonth
	 * @return
	 */
	Integer getInRoomNum(String badge,Date meterMonth);
}
