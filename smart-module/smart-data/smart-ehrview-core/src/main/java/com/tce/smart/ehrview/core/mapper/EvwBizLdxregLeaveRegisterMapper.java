package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwBizLdxregLeaveRegister;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 14:20
 */

public interface EvwBizLdxregLeaveRegisterMapper extends BaseMapper<EvwBizLdxregLeaveRegister> {
	List<EvwBizLdxregLeaveRegister> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);
}
