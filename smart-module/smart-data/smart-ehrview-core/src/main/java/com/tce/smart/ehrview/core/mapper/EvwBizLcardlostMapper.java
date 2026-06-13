package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwBizLcardlost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EvwBizLcardlostMapper extends BaseMapper<EvwBizLcardlost> {
	List<EvwBizLcardlost> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);
}
