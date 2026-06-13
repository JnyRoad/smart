package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwCallowanceCancelAllt;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 11:04
 */

public interface EvwCallowanceCancelAlltMapper extends BaseMapper<EvwCallowanceCancelAllt> {
	List<EvwCallowanceCancelAllt> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);
}
