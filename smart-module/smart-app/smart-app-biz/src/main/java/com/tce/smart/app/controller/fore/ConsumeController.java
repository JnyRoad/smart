package com.tce.smart.app.controller.fore;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.QueryConsumeRsAo;
import com.tce.smart.app.service.fore.ConsumeService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.data.api.dto.consume.resp.ConsumeRecordRespDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 刷卡消费控制器
 *
 * @author mkwu
 * @date 2019-08-03
 */
@RestController
@AllArgsConstructor
@RequestMapping("/consume")
public class ConsumeController extends BaseController {

	private final ConsumeService consumeService;

	/**
	 * 根据员工号分页查询刷卡记录
	 *
	 * @param page             分页参数
	 * @param queryConsumeRsAo 查询条件
	 * @return 消费记录
	 */
	@PostMapping("/record/list")
	public Result<Page<ConsumeRecordRespDTO>> listPage(Page<?> page, @RequestBody QueryConsumeRsAo queryConsumeRsAo) {
		return consumeService.listPage(page,queryConsumeRsAo);
	}

	/**
	 * 按时间统计消费笔数
	 *
	 * @param queryConsumeRsAo 查询条件
	 * @return 消费笔数
	 */
	@PostMapping("/record/count")
	public Result<BigDecimal> countConsume(@RequestBody QueryConsumeRsAo queryConsumeRsAo) {
		return consumeService.countConsume(queryConsumeRsAo);
	}
}
