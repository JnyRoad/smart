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

import java.util.List;
import java.util.Map;

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

	/**
	 * getByBadge 的批量版本：一次性按工号列表查询姓名，避免调用方按工号循环查询数据库。
	 * 同一工号存在多条历史记录时，和 getByBadge 一样只取其中一条——两者的 SQL 都没有
	 * ORDER BY，具体拿到哪一条不保证稳定，不要依赖“第一条”是特定语义（如最早/最新入住）。
	 *
	 * @param badges 工号列表
	 * @return 工号 -&gt; 姓名；查不到记录的工号不会出现在结果里，调用方需自行兜底默认值
	 */
	Map<String, String> getByBadgeBatch(List<String> badges);

}
