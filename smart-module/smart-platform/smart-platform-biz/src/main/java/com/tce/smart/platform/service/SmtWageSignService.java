package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.QueryAttendanceSignDTO;
import com.tce.smart.platform.core.dto.WageSignDTO;
import com.tce.smart.platform.core.entity.SmtWageSign;
import com.tce.smart.platform.core.vo.AttendanceSignVO;
import com.tce.smart.platform.core.vo.WageSignVO;

import java.util.List;

/**
 * 工资签单
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
public interface SmtWageSignService extends IService<SmtWageSign> {

	/**
	 * 分页查询接口
	 * @param page
	 * @param
	 */
	IPage<WageSignVO> getPage(Page page, WageSignDTO wageSignDTO);

	/**
	 * 根据badge和wageDate获取详情
	 * @param smtWageSign
	 */
	boolean getWageSign(SmtWageSign smtWageSign);

	/**
	 * 更新签字和状态
	 * @param smtWageSign
	 * @return
	 */
	boolean updateToSign(SmtWageSign smtWageSign);

	/**
	 * 每月1号0时同步人员信息任务
	 * @return
	 */
	boolean syncStaff();

	/**
	 * 获得发送短信条数
	 * @param wageSignDTO
	 * @return
	 */
	Integer countMessage(WageSignDTO wageSignDTO);

	/**
	 * 定时自动确认
	 * @param dto
	 * @return
	 */
	Boolean autoConfirm(WageSignDTO dto);

	/**
	 * 工资签收短信提醒
	 * @param wageSignDTO
	 * @return
	 */
	Boolean sendMessage(WageSignDTO wageSignDTO);

	/**
	 * 获得月内未签收数据
	 * @param dto
	 * @return
	 */
	List<WageSignVO> getMegInfoList(WageSignDTO dto);

	/**
	 * 批量将提醒状态改为已提醒
	 * @param ids
	 * @return
	 */
	Boolean updateNotice(List<Integer> ids);
}
