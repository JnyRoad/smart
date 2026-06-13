package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwLdxRegLeaveAll;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:37
 */

public interface EvwLdxRegLeaveAllMapper extends BaseMapper<EvwLdxRegLeaveAll> {
	List<EvwLdxRegLeaveAll> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);

	List<EvwLdxRegLeaveAll> listByDay(@Param("badge") String badge, @Param("queryDay") String queryDay);
}
