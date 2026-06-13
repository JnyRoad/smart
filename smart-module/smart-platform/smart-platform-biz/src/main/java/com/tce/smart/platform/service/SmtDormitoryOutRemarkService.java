package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.EditDormitoryOutRemarkReqDTO;
import com.tce.smart.platform.core.entity.SmtAlarmDevice;
import com.tce.smart.platform.core.entity.SmtDormitoryOutRemark;
import org.apache.poi.hssf.record.BOFRecord;

import javax.xml.crypto.Data;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * 住宿备注表
 *
 * @author fushiping
 * @date 2019-04-15 11:34:54
 */
public interface SmtDormitoryOutRemarkService extends IService<SmtDormitoryOutRemark> {

	/**
	 * 根据住宿id获得备注列表
	 * @param dorStaffId
	 * @return
	 */
	List<SmtDormitoryOutRemark> getList(Integer dorStaffId);

	/**
	 * 编辑备注信息
	 * @param remarkReqDTO
	 * @return
	 */
	Boolean editRemark(EditDormitoryOutRemarkReqDTO remarkReqDTO);

	/**
	 * 获得最新一条备注
	 * @param dorStaffId
	 * @return
	 */
	String getNewRemark(Integer dorStaffId);

	/**
	 * 员工退宿填入退宿记录ID
	 * @return
	 */
	Boolean updateDorStaffId(Integer dorStaffId, Integer dorHistoryStaffId);

	/**
	 * 查询本月备注天数
	 * @param dorStaffId 住宿ID
	 * @param dorHistoryStaffId 退宿ID
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @return
	 */
	Integer getRemarkDate(Integer dorStaffId, Integer dorHistoryStaffId, Date startTime, Date endTime);

	/**
	 * 换宿操作时转移备注
	 * @param dorStaffId 旧住宿记录id
	 * @param newDorStaffId 新住宿记录id
	 * @return
	 */
	Boolean transferRemark(Integer dorStaffId, Integer newDorStaffId);

	/**
	 * 查询本月备注天数
	 * @param dorStaffId 住宿ID
	 * @param dorHistoryStaffId 退宿ID
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @return
	 */
//	Integer getRemarkDate1(Integer dorStaffId, Integer dorHistoryStaffId, String startTime, String endTime) throws ParseException;


}
