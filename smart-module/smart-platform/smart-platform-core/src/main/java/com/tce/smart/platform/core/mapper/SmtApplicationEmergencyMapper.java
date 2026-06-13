package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtApplicationEmergency;
import org.apache.ibatis.annotations.Param;

/**
 * 应聘者紧急联系人
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:30
 */
public interface SmtApplicationEmergencyMapper extends BaseMapper<SmtApplicationEmergency> {
    /**
     * 根据应聘者工号查询
     * @param ApplicationId
     * @return
     */
    SmtApplicationEmergency selectByApplicationId(@Param("applicationId") String applicationId);
}
