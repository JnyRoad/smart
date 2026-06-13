package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.LocationAo;
import com.tce.smart.app.service.fore.ParkService;
import com.tce.smart.app.vo.fore.ParkVo;
import com.tce.smart.common.core.constant.PaginationConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.dto.resp.SmtParkRespDTO;
import com.tce.smart.platform.api.feign.RemoteParkService;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 园区信息服务接口实现类
 *
 * @author mingkai.wu
 * @date 2019-05-10 16:17:32
 */
@Service
@AllArgsConstructor
@Slf4j
public class ParkServiceImpl implements ParkService {

	private RemoteParkService remoteParkService;

	private RemoteStaffService remoteStaffService;

	@Override
	public ParkVo processlocation(LocationAo locationAO) {
		SmtParkDTO smtParkParms = new SmtParkDTO();
		smtParkParms.setParkLongitude(new BigDecimal(locationAO.getLongitude()));
		smtParkParms.setParkLatitude(new BigDecimal(locationAO.getLatitude()));
		// 调用远程定位园区接口
		Result<SmtParkDTO> result = remoteParkService.locationPark(smtParkParms,SecurityConstants.FROM_IN);
		log.info("remote locationPark result=[{}]", result);
		ParkVo parkVo = new ParkVo();
		if(result.isSuccess()){
			SmtParkDTO smtPark = result.getData();
			if(Objects.nonNull(smtPark)){
				parkVo.setParkId(String.valueOf(smtPark.getId()));
				parkVo.setParkName(smtPark.getParkName());
			}
		}
		return parkVo;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IPage<?> getParkList(Map<String, Object> params, LocationAo locationAo) {
		// 调用远程	获取园区列表
		Result<Page<SmtParkRespDTO>>  result = remoteParkService.getParkByPage(MapUtil.getInt(params, PaginationConstants.CURRENT),
				MapUtil.getInt(params, PaginationConstants.SIZE), locationAo.getParkName(),SecurityConstants.FROM_IN);
		log.info("remote getParkByPage result=[{}]", result);
		IPage<SmtParkRespDTO> pageInfo = result.getData();
		if (result.isSuccess() && Objects.nonNull(result.getData()) && CollectionUtils.isNotEmpty(pageInfo.getRecords())) {
			List parkVoList = new ArrayList();
			ParkVo parkVo = null;
			SmtParkRespDTO smtPark = null;
			for (int i = 0; i < pageInfo.getRecords().size(); i++) {
				parkVo = new ParkVo();
				smtPark = pageInfo.getRecords().get(i);
				parkVo.setParkId(String.valueOf(smtPark.getId()));
				parkVo.setParkName(smtPark.getParkName());
				parkVo.setParkLatitude(smtPark.getParkLatitude());
				parkVo.setParkLongitude(smtPark.getParkLongitude());
				parkVoList.add(parkVo);
			}
			pageInfo.setRecords(parkVoList);
		}

		return pageInfo;
	}

	@Override
	public List<SmtParkRespDTO> getUserPark() {
		String badge = SecurityUtils.getUser().getUsername();
		// 调用远程	获取园区列表
		Result<List<SmtParkRespDTO>>  result = remoteStaffService.getStaffPark(badge,SecurityConstants.FROM_IN);
		log.info("remote getParkByPage result=[{}]", result);
		List<SmtParkRespDTO> parks = result.getData();
		return parks;
	}
}
