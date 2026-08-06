package com.tce.smart.platform.core.mapper.energy;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.energy.SmtEnergyIngestionLedger;
import org.apache.ibatis.annotations.Param;

/**
 * 能耗来源事件账本访问接口。
 */
public interface SmtEnergyIngestionLedgerMapper extends BaseMapper<SmtEnergyIngestionLedger> {
	/**
	 * 使用数据库唯一约束登记事件；重复事件返回 0，不产生并发下的先查后插竞态。
	 */
	int insertIgnoreDuplicate(SmtEnergyIngestionLedger ledger);

	/** 查询已登记事件的内容哈希，用于拒绝事件标识复用。 */
	String selectPayloadHash(@Param("sourceEventId") String sourceEventId);
}
