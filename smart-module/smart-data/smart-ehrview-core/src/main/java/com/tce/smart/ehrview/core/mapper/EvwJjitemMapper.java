package com.tce.smart.ehrview.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.ehrview.core.entity.EvwEapprais;
import com.tce.smart.ehrview.core.entity.EvwJjitem;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
public interface EvwJjitemMapper extends BaseMapper<EvwJjitem> {

    List<EvwJjitem> getEvwJjitem(@Param("ezid") Integer ezid);
}
