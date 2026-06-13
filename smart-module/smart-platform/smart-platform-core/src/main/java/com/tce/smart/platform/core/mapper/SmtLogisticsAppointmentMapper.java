package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtLogisticsAppointment;

/**
 * 物流车预约信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:27
 */
public interface SmtLogisticsAppointmentMapper extends BaseMapper<SmtLogisticsAppointment> {

	/**
	 * 分页查询物流车信息
	 * @param page 分页对象
     * @param entity 物流车预约信息
	 * @return 返回结果
	 */
	IPage getLogisticsAppointment(Page page, @Param("query") SmtLogisticsAppointment entity, @Param("parkIds") List<Integer> parkIds);

	/**
	 * 根据车牌号查询
	 * @param cardNo 车牌号
	 * @return 返回结果
	 */
	SmtLogisticsAppointment queryByVehicleID(@Param("cardNo") String cardNo);

	/**
	 * 更新超时状态
	 * @return 返回结果
	 */
	Integer updateStatus();

}
