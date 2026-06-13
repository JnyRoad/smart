package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtSdMeterread;
import com.tce.smart.platform.core.entity.SmtSdMeterreadDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: SmtSdMeterreadDetailMapper
 * @date: 2020-07-13 15:52
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSdMeterreadDetailMapper extends BaseMapper<SmtSdMeterreadDetail> {
	Integer updateMeterDetailQty(@Param("mrId")Long mrId);

	Integer updateMeterDetailRevById(@Param("id")Long id);

	/**
	 * 分批查询抄表明细，避免Oracle IN子句限制
	 * @param mrIds 抄表记录ID列表
	 * @return 抄表明细列表
	 */
	List<SmtSdMeterreadDetail> selectByMrIdsBatch(@Param("mrIds") List<Long> mrIds);
}
