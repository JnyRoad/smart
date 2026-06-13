package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtCallowanceCancelRecord;

public interface SmtCallowanceCancelRecordService extends IService<SmtCallowanceCancelRecord> {

	Result save(String badge,String backDate, Integer type);

	void approvalNotice(String staffBadge, String code, String id, boolean flag);

	Result get(Integer badge);

	Result getInfo(String badge);

	Result getOutDormitory(String badge, Integer type);

	Result getCallowanceDetail(String badge, Integer type);

}
