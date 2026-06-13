package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.businesstrip.CcdFormtableMainDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainDt2RespDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainRespDTO;
import com.tce.smart.platform.core.dto.SearchTravelDTO;
import com.tce.smart.platform.core.entity.SmtTravelApplication;
import com.tce.smart.platform.core.vo.EmployeeTraveDayVO;
import com.tce.smart.platform.core.vo.FlowVO;

import java.util.List;

/**
 * 出差申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
public interface SmtTravelApplicationService extends IService<SmtTravelApplication> {

	/**
	 * 分页查询出差列表
	 * @param page
	 * @param searchTravelDTO
	 * @return
	 */
	IPage<CcdFormtableMainRespDTO> getSmtTravelApplicationPage(Page page, SearchTravelDTO searchTravelDTO);

	/**
	 * 查询出差详情
	 * @param id
	 * @return
	 */
	CcdFormtableMainDTO getTravelApplicationById(Integer id);

	/**
	 * 查询出差日程
	 * @param id
	 * @return
	 */
	List<EmployeeTraveDayVO> getInfoDay(Integer id);

	/**
	 * 出差报告
	 * @param id
	 * @return
	 */
	Result<List<CcdFormtableMainDt2RespDTO>> getInfoReport(Integer id);

	/**
	 * 获取出差流程
	 * @param id
	 * @return
	 */
	List<FlowVO> getInfoFlow(Integer id);
}
