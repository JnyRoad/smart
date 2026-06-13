package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.QueryAttendanceSignDTO;
import com.tce.smart.platform.core.dto.WageSignDTO;
import com.tce.smart.platform.core.entity.manage.SmtAttendanceSign;
import com.tce.smart.platform.core.vo.AttendanceSignVO;
import com.tce.smart.platform.core.vo.WageSignVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-27 10:45:43
 */
public interface SmtAttendanceSignMapper extends BaseMapper<SmtAttendanceSign> {

	/**
	 * 分页查询
	 * @return
	 */
	IPage<AttendanceSignVO> queryPage(Page page, @Param("query") QueryAttendanceSignDTO dto);

	/**
	 * 查询统计数量
	 * @param dto
	 * @return
	 */
	List<AttendanceSignVO> getCount(@Param("query") QueryAttendanceSignDTO dto);

	/**
	 * 获得短信发送人员电话号码
	 * @param dto
	 * @return
	 */
	List<AttendanceSignVO> getMegInfo(@Param("query") QueryAttendanceSignDTO dto);

	Boolean autoConfirm(@Param("query") QueryAttendanceSignDTO dto);

	/**
	 * 批量修改已读状态
	 * @param ids
	 */
	Boolean updateNotice(@Param("ids") List<Long> ids);
}
