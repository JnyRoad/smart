package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwCallowanceAllt;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 10:53
 */

public interface EvwCallowanceAlltMapper extends BaseMapper<EvwCallowanceAllt> {
	List<EvwCallowanceAllt> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);
}
