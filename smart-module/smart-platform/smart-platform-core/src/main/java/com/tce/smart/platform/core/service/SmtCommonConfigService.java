package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.commonconfig.CommonConfigEditReqDTO;
import com.tce.smart.platform.api.dto.req.commonconfig.CommonConfigQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.commonconfig.*;
import com.tce.smart.platform.core.entity.SmtCommonConfig;

import java.util.List;

/**
 * 预约配置表
 *
 * @author fushiping
 * @date 2021-08-13 16:08:16
 */
public interface SmtCommonConfigService extends IService<SmtCommonConfig> {

	/**
	 * 获得配置
	 * @param queryDTO
	 * @return
	 */
	List<SmtCommonConfig> getList(CommonConfigQueryReqDTO queryDTO);

	/**
	 * 根据类型获得配置
	 * @param businessType
	 * @param configType
	 * @return
	 */
	SmtCommonConfig getByType(Integer businessType, Integer configType, Integer parkId);

	/**
	 * 编辑配置
	 * @param editReqDTO
	 * @return
	 */
	Boolean editConfig(CommonConfigEditReqDTO editReqDTO);

	/**
	 * 批量编辑配置
	 * @param editReqDTO
	 * @return
	 */
	Boolean batchEditConfig(List<CommonConfigEditReqDTO> editReqDTO);

	/**
	 * 获得访客审批设置
	 * @return
	 */
	ConfigVisitorApprovalDTO getVisitorApprove(Integer parkId);

	/**
	 * 获得访客温馨提示
	 * @return
	 */
	ConfigVisitorNoticeDTO getVisitorNotice(Integer parkId);

	/**
	 * 获得入厂申请预约温馨提示
	 * @return
	 */
	ConfigVisitorNoticeDTO getAdmittanceNotice(Integer parkId);

	/**
	 * 是否开启健康码
	 * @param parkId
	 * @return
	 */
	ConfigVisitorHealthDTO getVisitorHealth(Integer parkId);

	/**
	 * 离职结算是否计算最后一天
	 * @param parkId
	 * @return
	 */
	ConfigSettlementLastDayDTO getLeaveSettlementApprove(Integer parkId);

	/**
	 * 离职结算日志保留天数
	 * @return
	 */
	ConfigSettlementLogDeleteDTO getSettlementDeleteDay();

}
