package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.TaskDownRecordDTO;
import com.tce.smart.platform.core.entity.SmtTaskDownRecord;
import com.tce.smart.platform.core.model.TaskDownRecordPark;
import com.tce.smart.platform.core.vo.TaskDownRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务下发记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
public interface SmtTaskDownRecordMapper extends BaseMapper<SmtTaskDownRecord> {

	/**
	 * 车辆下发记录
	 * @param page 分页
	 * @param taskDownRecordDTO 查询参数
	 * @return page
	 */
	IPage<TaskDownRecordVO> getVehicle(Page page, @Param("query") TaskDownRecordDTO taskDownRecordDTO);

	IPage<TaskDownRecordVO> getPerson(Page page, @Param("query") TaskDownRecordDTO taskDownRecordDTO);

	List<TaskDownRecordPark> getPark(@Param("parkIds") List<Integer> parkIds);

	List<TaskDownRecordPark> getDevice(@Param("parkId") String parkId, @Param("type") Integer type);
}
