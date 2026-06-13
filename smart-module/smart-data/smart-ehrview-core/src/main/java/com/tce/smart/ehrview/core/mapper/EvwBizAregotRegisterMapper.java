package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwBizAregotRegister;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EvwBizAregotRegisterMapper extends BaseMapper<EvwBizAregotRegister> {
	List<EvwBizAregotRegister> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);
}
