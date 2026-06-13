package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.service.fore.ExtraWorkService;
import com.tce.smart.app.vo.fore.ExtraWorkApplicationVo;
import com.tce.smart.app.vo.fore.ExtraWorkClassVo;
import com.tce.smart.app.vo.fore.ExtraWorkDetailVo;
import com.tce.smart.app.vo.fore.ExtraWorkTypeVo;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.PaginationConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.AddOverTimeApplicationReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchOverClassTimeTypeRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchOverTimeApplicationDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchOverTimeApplicationRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchOverTimeTypeRespDTO;
import com.tce.smart.platform.api.feign.RemoteOverTimeApplicationService;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 加班接口实现
 *
 * @author ly
 */
@Service
@AllArgsConstructor
@Slf4j
public class ExtraWorkServiceImpl implements ExtraWorkService {

	private final RemoteOverTimeApplicationService remoteOverTimeApplicationService;

	/**
	 * 获取加班类型
	 */
	@Override
	public ExtraWorkTypeVo getExtraWorkType() {
		ExtraWorkTypeVo extraWorkTypeVo = new ExtraWorkTypeVo();
		List<SearchOverTimeTypeRespDTO> list = remoteOverTimeApplicationService.getOverTypeList(SecurityConstants.FROM_IN).data();
		extraWorkTypeVo.setRecords(list);
		extraWorkTypeVo.setTotal(list.size());
		return extraWorkTypeVo;
	}


	/**
	 * 获取加班班别
	 */
	@Override
	public ExtraWorkClassVo getExtraClassType() {
		ExtraWorkClassVo extraWorkClassVo = new ExtraWorkClassVo();
		List<SearchOverClassTimeTypeRespDTO> list = remoteOverTimeApplicationService.getOverClassTypeList(SecurityConstants.FROM_IN).data();
		extraWorkClassVo.setRecords(list);
		extraWorkClassVo.setTotal(list.size());
		return extraWorkClassVo;
	}

	/**
	 * 获取加班的列表
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Page<?> getExtraWorkList(Map<String, Object> params) {
		// 获取员工号
		String staffBadge = SecurityUtils.getUser().getUsername();
		Page<SearchOverTimeApplicationRespDTO> pageInfo = remoteOverTimeApplicationService.getOvertimeApplicationPage(MapUtil.getInt(params, PaginationConstants.CURRENT), MapUtil.getInt(params, PaginationConstants.SIZE),
				staffBadge, SecurityConstants.FROM_IN).getData();
		//判斷值是否為空
		if (CollectionUtils.isNotEmpty(pageInfo.getRecords())) {
			List extraWorkList = new ArrayList();
			ExtraWorkApplicationVo extraWorkApplicationVo = null;
			SearchOverTimeApplicationRespDTO searchOverTimeApplicationVO = null;
			for (int i = 0; i < pageInfo.getRecords().size(); i++) {
				extraWorkApplicationVo = new ExtraWorkApplicationVo();
				searchOverTimeApplicationVO = pageInfo.getRecords().get(i);
				extraWorkApplicationVo.setRecordId(String.valueOf(searchOverTimeApplicationVO.getRecordId()));
				extraWorkApplicationVo.setRecordTitle(searchOverTimeApplicationVO.getStaffName() + "的加班申请");
				extraWorkApplicationVo.setRecordDesc(searchOverTimeApplicationVO.getRecordDesc());
				extraWorkApplicationVo.setExtraworkDate(searchOverTimeApplicationVO.getExtraworkDate());
				extraWorkApplicationVo.setRecordDate(searchOverTimeApplicationVO.getRecordDate());
				extraWorkApplicationVo.setExtraworkCount(searchOverTimeApplicationVO.getExtraworkCount());
				extraWorkApplicationVo.setExtraworkTypeName(searchOverTimeApplicationVO.getExtraworkTypeName());
				extraWorkList.add(extraWorkApplicationVo);
			}
			pageInfo.setRecords(extraWorkList);
		}
		return pageInfo;
	}

	/**
	 * 获取加班详情
	 */
	@Override
	public ExtraWorkDetailVo getExtraWorkDetail(AllApplicationAo vacateAoId) {
		//调用接口获取加班详情
		SearchOverTimeApplicationDetailRespDTO searchOverTimeApplicationDetailRespDTO = remoteOverTimeApplicationService.getOverTimeById(Integer.parseInt(vacateAoId.getRecordId()), SecurityConstants.FROM_IN).data();
		ExtraWorkDetailVo extraWorkDetailVo = new ExtraWorkDetailVo();
		extraWorkDetailVo.setEmployee(searchOverTimeApplicationDetailRespDTO.getEmployee());
		extraWorkDetailVo.setFlow(searchOverTimeApplicationDetailRespDTO.getFlow());
		extraWorkDetailVo.setProcessId(searchOverTimeApplicationDetailRespDTO.getProcessId());
		return extraWorkDetailVo;
	}


	/**
	 * 添加加班申请
	 */
	@Override
	public void addExtraWork(AddOverTimeApplicationReqDTO addOverTimeApplicationDTO) {
		//添加当前的员工号
		addOverTimeApplicationDTO.setStaffBadge(SecurityUtils.getUser().getUsername());
		//调用接口传入后台
		remoteOverTimeApplicationService.save(addOverTimeApplicationDTO, SecurityConstants.FROM_IN).data();
	}
}
