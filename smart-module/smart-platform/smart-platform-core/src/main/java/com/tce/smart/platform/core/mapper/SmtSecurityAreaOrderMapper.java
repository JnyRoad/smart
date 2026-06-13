package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.dto.securityarea.SecurityAreaOrderDTO;
import com.tce.smart.platform.core.entity.securityarea.SmtSecurityAreaOrder;
import org.apache.ibatis.annotations.Param;

/**
 * @description: SmtSecurityAreaOrderMapper
 * @date: 2020-07-30 9:09
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSecurityAreaOrderMapper extends BaseMapper<SmtSecurityAreaOrder> {

	/**
	 * 获取保密区预约详情
	 * @param id
	 * @return
	 */
	SecurityAreaOrderDTO getSecurityAreaOrderDetail(@Param("id") Long id);
}
