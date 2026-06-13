package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwBizCallowance;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 09:41
 */

public interface EvwBizCallowanceMapper extends BaseMapper<EvwBizCallowance> {
	List<EvwBizCallowance> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);
}
