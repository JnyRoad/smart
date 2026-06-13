package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwAcardlostAll;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EvwAcardlostAllMapper  extends BaseMapper<EvwAcardlostAll>{
	List<EvwAcardlostAll> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);

}
