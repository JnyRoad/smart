package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.QueryConsumeRsAo;
import com.tce.smart.app.service.fore.ConsumeService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.consume.req.QueryConsumeReqDto;
import com.tce.smart.data.api.dto.consume.resp.ConsumeRecordRespDTO;
import com.tce.smart.data.api.feign.consume.RemoteConsumeService;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * 刷卡消费服务实现类
 *
 * @author mkwu
 * @date 2019-08-03
 */
@Service
@Slf4j
public class ConsumeServiceImpl implements ConsumeService {

	/**
	 * 公司账户
	 */
	private static final int PUB_ACCOUNT = 1;

	/**
	 * 个人账户
	 */
	private static final int PRIV_ACCOUNT = 2;

	@Autowired
	private RemoteConsumeService remoteConsumeService;

	@SuppressWarnings("rawtypes")
	@Override
	public Result<Page<ConsumeRecordRespDTO>> listPage(Page<?> page, QueryConsumeRsAo queryConsumeRsAo) {
		if (Objects.isNull(queryConsumeRsAo) || Objects.isNull(queryConsumeRsAo.getAcctType())
				|| StringUtil.isNullOrEmpty(queryConsumeRsAo.getQueryDate())) {
			throw new TCEException("参数不全");
		}

		// 构造查询条件
		QueryConsumeReqDto queryConsumeReDto = buildQueryCondition(page, queryConsumeRsAo);

		Result<Page<ConsumeRecordRespDTO>> rs;
		switch (queryConsumeRsAo.getAcctType()) {
			case PUB_ACCOUNT:
				rs = remoteConsumeService.listPubPage(queryConsumeReDto, SecurityConstants.FROM_IN);
				break;
			case PRIV_ACCOUNT:
				rs = remoteConsumeService.listPrivPage(queryConsumeReDto, SecurityConstants.FROM_IN);
				break;
			default:
				rs = new Result<>();
				break;
		}

		log.info("remoteConsumeService.listPubPage rs={}", rs);

		return rs;
	}

	@Override
	public Result<BigDecimal> countConsume(QueryConsumeRsAo queryConsumeRsAo) {
		if (Objects.isNull(queryConsumeRsAo) || Objects.isNull(queryConsumeRsAo.getAcctType())
				|| StringUtil.isNullOrEmpty(queryConsumeRsAo.getQueryDate())) {
			throw new TCEException("参数不全");
		}

		// 构造查询条件
		QueryConsumeReqDto queryConsumeReDto = buildQueryCondition(null, queryConsumeRsAo);

		Result<BigDecimal> rs;
		switch (queryConsumeRsAo.getAcctType()) {
			case PUB_ACCOUNT:
				rs = remoteConsumeService.countPubConsume(queryConsumeReDto, SecurityConstants.FROM_IN);
				break;
			case PRIV_ACCOUNT:
				rs = remoteConsumeService.countPrivConsume(queryConsumeReDto, SecurityConstants.FROM_IN);
				break;
			default:
				rs = new Result<>(new BigDecimal(0));
				break;
		}

		log.info("remoteConsumeService.listPubPage rs={}", rs);

		return rs;
	}

	/**
	 * 构造查询条件
	 *
	 * @param page             分页参数
	 * @param queryConsumeRsAo 查询Ao
	 * @return remote 查询条件
	 */
	private QueryConsumeReqDto buildQueryCondition(Page<?> page, QueryConsumeRsAo queryConsumeRsAo) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Date queryDate = null;
		try {
			queryDate = sdf.parse(queryConsumeRsAo.getQueryDate());
		} catch (ParseException e) {
			throw new TCEException("转换时间异常");
		}

		Date startDate = DateUtil.beginOfMonth(queryDate);
		Date endDate = DateUtil.endOfMonth(queryDate);

		QueryConsumeReqDto queryConsumeReDto = new QueryConsumeReqDto();
		if (Objects.isNull(queryConsumeRsAo.getEmpNo())) {
			queryConsumeReDto.setEmpNo(SecurityUtils.getUser().getUsername());
		} else {
			queryConsumeReDto.setEmpNo(queryConsumeRsAo.getEmpNo());
		}

		Page queryPage = page;
		if (Objects.isNull(page)) {
			queryPage = new Page();
		}
		queryConsumeReDto.setPage(queryPage);

		queryConsumeReDto.setStartDate(startDate);
		queryConsumeReDto.setEndDate(endDate);
		return queryConsumeReDto;
	}
}
