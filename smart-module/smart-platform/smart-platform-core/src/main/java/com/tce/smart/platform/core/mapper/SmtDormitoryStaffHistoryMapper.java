package com.tce.smart.platform.core.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.DormitoryStatisticsDTO;
import com.tce.smart.platform.core.dto.StaffInDormitoryHistoryDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryStaffHistory;
import com.tce.smart.platform.core.vo.DormitoryStaffHistoryVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


/**
 * 员工宿舍信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
public interface SmtDormitoryStaffHistoryMapper extends BaseMapper<SmtDormitoryStaffHistory> {

	IPage<DormitoryStaffHistoryVO> getSmtDormitoryStaffHistory(Page page,@Param("query") StaffInDormitoryHistoryDTO dto,@Param("park") List<Integer> parkIdList);

	Integer statistics(@Param("query") DormitoryStatisticsDTO dormitoryStatisticsDTO);

	Integer totalBed(@Param("query") DormitoryStatisticsDTO dormitoryStatisticsDTO);

	IPage<DormitoryStaffHistoryVO> statisticsDetial(Page page, @Param("query") DormitoryStatisticsDTO dormitoryStatisticsDTO);

	List<SmtDormitoryStaffHistory> getStatffHistory(@Param("roomIds") List<Integer> roomIds,@Param("startTime") Date startTime,@Param("endTime") Date endTime);

	List<SmtDormitoryStaffHistory> getNeedModifyData();

	List<SmtDormitoryStaffHistory> getNeedModifyStatisData();
}
