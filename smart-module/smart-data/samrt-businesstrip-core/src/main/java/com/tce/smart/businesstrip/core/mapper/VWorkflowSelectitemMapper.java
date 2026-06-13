package com.tce.smart.businesstrip.core.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.businesstrip.core.entity.VWorkflowSelectitem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VWorkflowSelectitemMapper extends BaseMapper<VWorkflowSelectitem> {

	List<VWorkflowSelectitem> getList(@Param("selectIdList") List<Integer> selectIdList, @Param("fieldId") Integer fieldId);
}
