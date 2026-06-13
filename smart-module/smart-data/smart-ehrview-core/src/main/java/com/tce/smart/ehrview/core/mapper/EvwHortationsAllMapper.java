package com.tce.smart.ehrview.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwHortationsAll;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Descripition: 按月份查询奖惩记录
 * @Auther: guohongtai
 * @Date: 2020-07-14 09:14
 */

public interface EvwHortationsAllMapper extends BaseMapper<EvwHortationsAll> {
	List<EvwHortationsAll> list(@Param("badge") String badge, @Param("queryMonth") String queryMonth);
}
