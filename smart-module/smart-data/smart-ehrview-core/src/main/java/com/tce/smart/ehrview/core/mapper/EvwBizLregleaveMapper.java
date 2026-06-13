package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwBizLregleave;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-20 19:03
 */

public interface EvwBizLregleaveMapper extends BaseMapper<EvwBizLregleave> {
	List<EvwBizLregleave> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);
}
