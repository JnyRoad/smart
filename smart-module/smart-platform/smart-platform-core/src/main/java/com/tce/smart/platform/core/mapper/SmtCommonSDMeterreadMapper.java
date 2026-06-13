package com.tce.smart.platform.core.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.dto.commonsd.CommonSDMeterreadDTO;
import com.tce.smart.platform.core.entity.SmtCommonSDMeterread;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


/**
 * 公摊水电抄表
 *
 */
public interface SmtCommonSDMeterreadMapper extends BaseMapper<SmtCommonSDMeterread> {

	List<CommonSDMeterreadDTO> getCommonSDMeterread(@Param("comId") Long comId, @Param("meterDate") Date meterDate,@Param("categoryId") Integer categoryId);

}
