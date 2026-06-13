package com.tce.smart.platform.wrapper;

import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.PatchStatisticsRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.vo.PatchStatisticsVo;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.tool.constant.DictConstants;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Objects;

/**
 * @ProjectName smart-module
 * @ClassName: BadgeApplyRecordWrapper
 * @Author fushiping
 * @Date 2020/7/8
 */
@Component
@AllArgsConstructor
public class PatchStatisticsWrapper extends BaseWrapper<PatchStatisticsVo, PatchStatisticsRespDTO> {

	@Autowired
	private RemoteDictService remoteDictService;
	@Autowired
	private SmtParkService smtParkService;

    @Override
    protected PatchStatisticsRespDTO warp(PatchStatisticsVo patchStatisticsVo) throws IOException {
		PatchStatisticsRespDTO respDTO = BeanUtils.transform(PatchStatisticsRespDTO.class, patchStatisticsVo);
		//获取补卡事由字典表
		Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.REPLACE_REASON,patchStatisticsVo.getCause().toString(), SecurityConstants.FROM_IN);
		if(Objects.nonNull(findByType.getData())) {
			respDTO.setCause(findByType.getData().getDescription());
		}
		//获取园区信息
		if(Objects.nonNull(patchStatisticsVo.getParkId())) {
			SmtPark park = smtParkService.getById(patchStatisticsVo.getParkId());
			if(Objects.nonNull(park)) {
				respDTO.setParkName(park.getParkName());
			}

		}
		return respDTO;
    }
}
