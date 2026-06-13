package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtStaffFamily;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmtStaffFamilyMapper  extends BaseMapper<SmtStaffFamily>{

    /**
     * 根据员工id查询
     * @param staffId
     * @return
     */
    List<SmtStaffFamily> selectByStaffId(@Param("staffId") String staffId);
}
