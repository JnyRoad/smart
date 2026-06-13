package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwBizCallowanceFood;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 10:53
 */

public interface EvwBizCallowanceFoodMapper extends BaseMapper<EvwBizCallowanceFood> {
	List<EvwBizCallowanceFood> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);
}
