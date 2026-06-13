package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.DormitoryStatisticsDTO;
import com.tce.smart.platform.core.dto.StaffInDormitoryHistoryDTO;
import com.tce.smart.platform.core.dto.UpdateDormitoryStaffDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryStaffHistory;
import com.tce.smart.platform.core.vo.DormitoryStaffHistoryVO;
import com.tce.smart.platform.core.vo.DormitoryStatisticsVO;

/**
 * 员工宿舍信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
public interface SmtDormitoryStaffHistoryService extends IService<SmtDormitoryStaffHistory> {


	Result getSmtDormitoryStaffHistory(Page page, StaffInDormitoryHistoryDTO dto, String rangTimeIn,
									   String rangTimeOut);

	Result updateById(UpdateDormitoryStaffDTO updateDormitoryStaffDTO);

	DormitoryStatisticsVO statistics(DormitoryStatisticsDTO dormitoryStatisticsDTO);

	IPage<DormitoryStaffHistoryVO> statisticsDetial(Page page, DormitoryStatisticsDTO dormitoryStatisticsDTO);

	/**
	 * 删除退宿记录
	 *
	 * @param id
	 * @return
	 */
	Boolean deleteDor(Integer id);

	/**
	 * 通过工号获取姓名
	 *
	 * @param badge
	 * @return
	 */
	String getByBadge(String badge);

}
