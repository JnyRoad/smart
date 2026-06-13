package com.tce.smart.app.service.fore;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.QueryConsumeRsAo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.consume.resp.ConsumeRecordRespDTO;

import java.math.BigDecimal;

/**
 * 刷卡消费服务接口
 *
 * @author mkwu
 * @date 2019-08-03
 */
public interface ConsumeService {

	/**
	 * 根据员工号分页查询刷卡记录
	 *
	 * @param page 分页信息
	 * @param queryConsumeRsAo 查询条件
	 * @return 消费记录
	 */
	Result<Page<ConsumeRecordRespDTO>> listPage(Page<?> page, QueryConsumeRsAo queryConsumeRsAo);

	/**
	 * 按时间统计消费笔数
	 *
	 * @param queryConsumeRsAo 查询条件
	 * @return 消费笔数
	 */
	Result<BigDecimal> countConsume(QueryConsumeRsAo queryConsumeRsAo);

}
