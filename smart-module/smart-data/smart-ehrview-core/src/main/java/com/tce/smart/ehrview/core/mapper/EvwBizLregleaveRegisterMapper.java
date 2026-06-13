package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwBizLregleaveRegister;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EvwBizLregleaveRegisterMapper extends BaseMapper<EvwBizLregleaveRegister> {
	List<EvwBizLregleaveRegister> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);
}
