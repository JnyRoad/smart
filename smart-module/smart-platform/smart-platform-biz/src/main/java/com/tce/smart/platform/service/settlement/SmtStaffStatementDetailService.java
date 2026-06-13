package com.tce.smart.platform.service.settlement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.SmtStaffStatementReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.StaffStatementWithDorReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.StaffStayModifyRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.StaffStayRespDTO;
import com.tce.smart.platform.api.dto.resp.sdstatement.SDStatementDetailRespDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDetailDTO;
import com.tce.smart.platform.core.entity.SmtStaffStatementDetail;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description: SmtStaffStatementDetailService
 * @date: 2020-07-16 15:46
 * @author: wuling
 * @version: 1.0
 */
public interface SmtStaffStatementDetailService extends IService<SmtStaffStatementDetail> {
	/**
	 * 分页查询员工水电结算统计数据
	 * @param page
	 * @param smtStaffStatementReqDTO
	 * @return
	 */
	IPage<SmtStaffStatementDTO> getStaffSDStatementDetail(Page page, SmtStaffStatementReqDTO smtStaffStatementReqDTO);

	IPage<SmtStaffStatementDTO> getStaffSDStatementDetailNew(Page page, SmtStaffStatementReqDTO smtStaffStatementReqDTO, SmtSdMeterreadService smtSdMeterreadService);


	List<SmtStaffStatementDTO> getSDMeterreadWithDor(StaffStatementWithDorReqDTO staffStatementWithDorReqDTO,SmtSdMeterreadService smtSdMeterreadService);

	List<SmtStaffStatementDetailDTO> getSDMeterreadDeteilExport(StaffStatementWithDorReqDTO staffStatementWithDorReqDTO, SmtSdMeterreadService smtSdMeterreadService);

	/**
	 * 分页查询员工水电结算详细数据
	 * @param page
	 * @return
	 */
	IPage<SDStatementDetailRespDTO> getStaffSDMeterRecord(Page page,Date meterMonth);


	/**
	 * 修改员工住宿天数
	 * @param staffStayRespDTOS
	 * @return
	 */
	Boolean updateStaffStayNum(Long mrId,List<StaffStayRespDTO> staffStayRespDTOS,SmtSdMeterreadService smtSdMeterreadService);

	/**
	 * 查询员工住宿天数修改记录
	 * @param mrId
	 * @return
	 */
	List<StaffStayModifyRespDTO> queryStaffStayUpdateRecord(Long mrId,SmtSdMeterreadService smtSdMeterreadService);

	/**
	 * 查询抄表记录对应的员工入住情况
	 * @param mrId
	 */
	List<StaffStayRespDTO> queryStaffStayNum(Long mrId);

	/**
	 * 保存员工的水电用量
	 * @param smtStaffStatementDetails
	 * @param roomAvgUse
	 */
	void saveStaffSdRecord(List<SmtStaffStatementDetail> smtStaffStatementDetails,Long mrId,Long mrDetailId,Integer categoryId,Date meterMonth, BigDecimal roomAvgUse);

}
