package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtStaffEmergency;
import org.apache.ibatis.annotations.Param;

/**
 * 员工紧急联系人
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:30
 */
public interface SmtStaffEmergencyMapper extends BaseMapper<SmtStaffEmergency> {
    /**
     * 根据员工工号查询
     * @param staffId
     * @return
     */
    SmtStaffEmergency selectByStaffId(@Param("staffId") String staffId);
}
