package com.tce.smart.app.service.fore.impl;

import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.app.emun.AppContentType;
import com.tce.smart.app.emun.ModuleCatalog;
import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppModuleInfoService;
import com.tce.smart.app.service.fore.HomePagetService;
import com.tce.smart.app.vo.fore.SubModuleDetailVo;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.tce.smart.tool.constant.DictConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * App首页服务实现类
 *
 * @author mckaywu
 * @date 2019-06-19 10:33:28
 */
@Service
@Slf4j
public class HomePagetServiceImpl implements HomePagetService {

	@Autowired
	private RemoteDictService remoteDictService;

	@Autowired
	private RemoteStaffService remoteStaffService;

	@Autowired
	private AppCommService appCommService;

	@Autowired
	private AppModuleInfoService appModuleInfoService;

	@Override
	public List<SubModuleDetailVo> getNavigateModule(String parkId) {
		List<SubModuleDetailVo> respList = new ArrayList<SubModuleDetailVo>();

		// 查询员工App模块权限
		String badge = SecurityUtils.getUser().getUsername();
		Result<List<String>> stafAuthModuleRs = remoteStaffService.getStaffAppModule(badge);
		if (!stafAuthModuleRs.isSuccess() || CollectionUtils.isEmpty(stafAuthModuleRs.getData())) {
			log.error("查询员工App授权模块异常,result={}", stafAuthModuleRs);
			return respList;
		}

		// 查询首页导航菜单配置
		Result<List<SysDict>> resultDict = remoteDictService.findByType(DictConstants.APP_NAVIGATE_MODULE,
				SecurityConstants.FROM_IN);
		if (!resultDict.isSuccess() || CollectionUtils.isEmpty(resultDict.getData())
				|| resultDict.getData().size() > 1) {
			log.error("查询员工App首页导航模块异常,result={}", resultDict);
			return respList;
		}

		List<String> staffModuleList = stafAuthModuleRs.getData();

		SysDict tempSysDict = resultDict.getData().get(0);// 只取一条
		// 转化成Intger类型集合
		List<Integer> navigateModuleIds = Arrays.asList(tempSysDict.getValue().split(","))
				.stream()
				.map(Integer::valueOf)
				.collect(Collectors.toList());

		List<AppModuleInfo> appModuleInfo = appModuleInfoService.getSubModuleByIds(navigateModuleIds);
		if (CollectionUtils.isNotEmpty(appModuleInfo)) {
			SubModuleDetailVo subModuleDetailVo = null;

			for (AppModuleInfo tempModule : appModuleInfo) {
				// 只过滤必选模块
				if (ModuleCatalog.BISINE.getType().toString().equals(tempModule.getCatalogCode())) {
					//根据模块权限ID过滤
					if (CollectionUtils.isEmpty(staffModuleList)
							|| !staffModuleList.contains(String.valueOf(tempModule.getId()))) {
						continue;
					}
				}

				subModuleDetailVo = new SubModuleDetailVo();
				subModuleDetailVo.setModuleName(tempModule.getModuleName());
				subModuleDetailVo.setModuleIcon(appCommService.buildModuleImageUrl(tempModule.getId()));
				subModuleDetailVo.setModuleUrl(tempModule.getModuleUrl());

				// 内容类型
				if (StringUtils.isNotBlank(tempModule.getModuleUrl()) && !(tempModule.getModuleUrl().startsWith("http")
						|| tempModule.getModuleUrl().startsWith("HTTP"))) {
					subModuleDetailVo.setContentLinkType(AppContentType.MODULE.getType());
				} else {
					subModuleDetailVo.setContentLinkType(AppContentType.LINK.getType());
				}

				respList.add(subModuleDetailVo);
			}
		}

		return respList;
	}

}
