package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.VehicleApplyDTO;
import com.tce.smart.platform.core.entity.SmtVehicleApply;
import org.apache.ibatis.annotations.Param;

/**
 * 入园申请信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
public interface SmtVehicleApplyMapper extends BaseMapper<SmtVehicleApply> {

	/**
	 * 查询入园申请信息
	 * @param page 分页
	 * @param entity 查询条件
	 * @return 返回结果集
	 */
	IPage getVehicleApply(Page page, @Param("query") VehicleApplyDTO entity);
}
