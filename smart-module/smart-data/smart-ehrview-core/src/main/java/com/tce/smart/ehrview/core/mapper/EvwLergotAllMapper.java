package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwLergotAll;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EvwLergotAllMapper extends BaseMapper<EvwLergotAll> {
	List<EvwLergotAll> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);
}
