package com.tce.smart.data.service.guard.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.data.service.guard.IVcallCarService;
import com.tce.smart.guard.core.dto.QueryParkLogisticsDTO;
import com.tce.smart.guard.core.service.GuardVcallCarService;
import com.tce.smart.platform.api.dto.SmtParkLogisticsDTO;
import com.tce.smart.platform.api.feign.RemoteParkLogisticsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 物流车预约
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@Service
public class VcallCarServiceImpl implements IVcallCarService {

	@Autowired
	private GuardVcallCarService guardVcallCarService;

	@Autowired
	private RemoteParkLogisticsService remoteParkLogisticsService;

	/**
	 * 分页查询
	 *
	 * @param page
	 * @return
	 */
	public IPage getVcallCarPage(Page page) {
		List<QueryParkLogisticsDTO> queryDtoList = new ArrayList<>();

		Result<List<SmtParkLogisticsDTO>> result = remoteParkLogisticsService.list(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (result.isSuccess() && CollectionUtils.isNotEmpty(result.getData())) {
			QueryParkLogisticsDTO queryParkLogisticsDTO;
			for (SmtParkLogisticsDTO elemenet : result.getData()) {
				queryParkLogisticsDTO = new QueryParkLogisticsDTO();
				BeanUtils.copyProperties(elemenet, queryParkLogisticsDTO);
				queryDtoList.add(queryParkLogisticsDTO);
			}
		}

		String queryDate = DateUtil.format(DateUtil.beginOfDay(DateUtil.date()), DateUtils.DEFAULT_DATE_TIME_FORMAT);
		return guardVcallCarService.getVcallCarPage(page, queryDate, queryDtoList);
	}
}
