package com.tce.smart.platform.service.manage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.manage.AttendanceSignReqDTO;
import com.tce.smart.platform.api.dto.req.manage.QueryAttendanceSignReqDTO;
import com.tce.smart.platform.api.dto.resp.manage.AttendanceSignRespDTO;
import com.tce.smart.platform.core.dto.QueryAttendanceSignDTO;
import com.tce.smart.platform.core.entity.manage.SmtAttendanceSign;
import com.tce.smart.platform.core.vo.AttendanceSignVO;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-27 10:45:43
 */
public interface SmtAttendanceSignService extends IService<SmtAttendanceSign> {

	/**
	 * 分页查询接口
	 * @param page
	 * @param
	 */
	IPage<AttendanceSignVO> getPage(Page page, QueryAttendanceSignReqDTO reqDTO);

	/**
	 * 考勤签单
	 * @param signReqDto
	 * @return
	 */
	Boolean updateToSign(AttendanceSignReqDTO signReqDto);

	/**
	 * 根据工号和签单月份获得签单详情
	 * @param checkDate
	 * @return
	 */
	SmtAttendanceSign getByBadge(String badge, String checkDate);

	/**
	 * 计算当前登录人员有几次未签单数据
	 * @return
	 */
	Integer countNotSign();

	/**
	 * 根据月份查询签收状态
	 * @param checkDate
	 * @return
	 */
	String getSignStatus(String checkDate);

	/**
	 * 获得短息发送条数
	 * @param reqDTO
	 * @return
	 */
	Integer countMessage(QueryAttendanceSignReqDTO reqDTO);

	/**
	 * 定时自动签收
	 * @param dto
	 * @return
	 */
	Boolean autoConfirm(QueryAttendanceSignDTO dto);

	/**
	 * 每月定时同步员工信息
	 * @return
	 */
	boolean syncStaff();

	/**
	 * 发送短信提醒
	 * @param reqDTO
	 * @return
	 */
	Boolean sendMessage(QueryAttendanceSignReqDTO reqDTO);

	/**
	 * 获得月内未签收数据
	 * @param dto
	 * @return
	 */
	List<AttendanceSignVO> getMegInfoList(QueryAttendanceSignDTO dto);

	/**
	 * 批量将提醒状态改为已提醒
	 * @param ids
	 * @return
	 */
	Boolean updateNotice(List<Long> ids);

}
