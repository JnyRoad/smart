package com.tce.smart.platform.core.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.WageSignDTO;
import com.tce.smart.platform.core.entity.SmtWageSign;
import com.tce.smart.platform.core.vo.WageSignVO;
import org.omg.CORBA.INTERNAL;

import java.util.List;

/**
 * 工资签单
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
public interface SmtWageSignMapper extends BaseMapper<SmtWageSign> {

	/**
	 * 分页查询
	 * @return
	 */
	IPage<WageSignVO> getPage(Page page,  @Param("query") WageSignDTO wageSignDTO);

	/**
	 * 查询统计数量
	 * @param wageSignDTO
	 * @return
	 */
	List<WageSignVO> getCount(@Param("query") WageSignDTO wageSignDTO);

	/**
	 * 获得短信发送人员电话号码
	 * @param wageSignDTO
	 * @return
	 */
	List<WageSignVO> getMegInfo(@Param("query") WageSignDTO wageSignDTO);

	/**
	 * 批量修改已读状态
	 * @param ids
	 */
	Boolean updateNotice(@Param("ids") List<Integer> ids);

	/**
	 * 自动确认
	 * @param wageSignDTO
	 */
	Boolean autoConfirm(@Param("query") WageSignDTO wageSignDTO);
}
