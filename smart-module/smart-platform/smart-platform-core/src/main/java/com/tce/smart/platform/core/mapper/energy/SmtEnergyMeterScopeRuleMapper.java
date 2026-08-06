package com.tce.smart.platform.core.mapper.energy;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.energy.SmtEnergyMeterScopeRule;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 能耗表计统计范围规则访问接口。
 */
public interface SmtEnergyMeterScopeRuleMapper extends BaseMapper<SmtEnergyMeterScopeRule> {
	SmtEnergyMeterScopeRule selectEffectiveRule(@Param("meterSource") String meterSource, @Param("meterId") Long meterId,
											 @Param("statDate") LocalDate statDate);
	List<SmtEnergyMeterScopeRule> selectEffectiveRulesForPark(@Param("parkId") Long parkId, @Param("meterSource") String meterSource,
																 @Param("statDate") LocalDate statDate);
}
