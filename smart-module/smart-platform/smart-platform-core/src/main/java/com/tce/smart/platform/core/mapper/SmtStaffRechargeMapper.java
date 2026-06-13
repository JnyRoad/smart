package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.ao.RechargePageAO;
import com.tce.smart.platform.core.entity.manage.SmtStaffRecharge;
import com.tce.smart.platform.core.vo.RechargePageVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-17 16:51:38
 */
public interface SmtStaffRechargeMapper extends BaseMapper<SmtStaffRecharge> {

	/**
	 * 分页获取充值名单数据
	 * @param page
	 * @param query
	 * @return
	 */
	IPage<RechargePageVO> getPage(Page page, @Param("query") RechargePageAO query);

	List<RechargePageVO> getList(@Param("query") RechargePageAO query);

	List<RechargePageVO> getListByIds(@Param("ids") List<Long> ids);

	Boolean deleteInfo(@Param("query") RechargePageAO query);

	/**
	 * 获取月份内数据
	 * @param date
	 * @param status
	 * @return
	 */
	List<SmtStaffRecharge> getMouthList(@Param("date") LocalDateTime date, @Param("status") Integer status);


	/**
	 * 根据工号获取数据
	 * @param date
	 * @param badge
	 * @return
	 */
	List<SmtStaffRecharge> getByBadge(@Param("date") LocalDateTime date, @Param("badge") String badge);

	/**
	 * 获取月份内数据
	 * @param date
	 * @param status
	 * @return
	 */
	SmtStaffRecharge checkMonthList(@Param("date") LocalDateTime date, @Param("status") Integer status, @Param("parkIds") List<Integer> parkIds);

	/**
	 * 批量同步状态
	 * @param ids
	 */
	Boolean updateState(@Param("ids") List<Long> ids);

	Boolean updateStateById(@Param("ids") Long ids);
}
