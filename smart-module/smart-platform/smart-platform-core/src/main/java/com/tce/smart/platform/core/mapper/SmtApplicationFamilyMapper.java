package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtApplicationFamily;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmtApplicationFamilyMapper  extends BaseMapper<SmtApplicationFamily>{

    /**
     * 根据应聘者id查询
     * @param ApplicationId
     * @return
     */
    List<SmtApplicationFamily> selectByApplicationId(@Param("applicationId") String applicationId);
}
