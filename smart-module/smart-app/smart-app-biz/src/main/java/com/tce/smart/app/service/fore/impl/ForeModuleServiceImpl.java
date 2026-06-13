package com.tce.smart.app.service.fore.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.app.emun.AppContentType;
import com.tce.smart.app.emun.ModuleCatalog;
import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppModuleInfoService;
import com.tce.smart.app.service.fore.ForeModuleService;
import com.tce.smart.app.vo.fore.ModuleListVo;
import com.tce.smart.app.vo.fore.SubModuleDetailVo;
import com.tce.smart.app.vo.fore.SubModuleInfoVo;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.SmtStaffDTO;
import com.tce.smart.platform.api.dto.resp.SmtParkRespDTO;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.tce.smart.tool.exception.TCEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 手机App模块服务接口
 *
 * @author mckaywu
 * @date 2019-06-13 19:20:24
 */
@Service
public class ForeModuleServiceImpl implements ForeModuleService {

	@Autowired
	private AppModuleInfoService appModuleInfoService;

	@Autowired
	private AppCommService appCommService;

	@Autowired
	private RemoteStaffService remoteStaffService;

	private final String[] excludeServiceMoudle = {"正常离职","请假","调休(月薪)","加班(月薪)","出差查询","考勤补卡","工资查询","部门考勤","补贴申请","EHR自助查询","厂牌管理","招聘","异常离职","消费记录","奖惩记录"};

	@Override
	public ModuleListVo getForeModuleList() {
		ModuleListVo moduleListVo = new ModuleListVo();

		// 查询业务顶级模块
		List<AppModuleInfo> businessModuleList = appModuleInfoService.getTopModule(ModuleCatalog.BISINE.getType());
		if (CollectionUtils.isEmpty(businessModuleList)) {
			throw new TCEException("查询业务模块信息异常");
		} else if (businessModuleList.size() > 1) {// 只会有一个业务模块集合
			throw new TCEException("业务模块配置异常");
		}

		String badge = SecurityUtils.getUser().getUsername();
		Result<List<String>> staffRs = remoteStaffService.getStaffAppModule(badge);
		if(!staffRs.isSuccess()) {
			throw new TCEException("获取模块信息异常");
		}
		List<String> staffModuleList = staffRs.getData();

		List<SubModuleDetailVo> busSubModuleDetaiList = buildForeModuleDetail(businessModuleList.get(0).getId(),staffModuleList,true);

		// 保存"返厂确认"模块
		Optional<SubModuleDetailVo> specialModule = busSubModuleDetaiList.stream().filter(item -> item.getModuleName().equals("返厂确认")).findFirst();
		// 移除"返厂确认"模块
		busSubModuleDetaiList.removeIf(item -> item.getModuleName().equals("返厂确认"));

		//查询员工信息
		Result<SmtStaffDTO> simpleSttaffByBadge = remoteStaffService.getSimpleSttaffByBadge(badge);
		SmtStaffDTO smtStaffDTO = simpleSttaffByBadge.getData();
		//石岩员工隐藏部分服务模块
		Result<List<SmtParkRespDTO>> staffPark = remoteStaffService.getStaffPark(badge, SecurityConstants.FROM_IN);
		if(staffPark.isSuccess()){
			List<SmtParkRespDTO> staffParkData = staffPark.getData();
			for(Object obj : staffParkData){
				JSONObject jsonObject = JSONUtil.parseObj(obj);
				SmtParkRespDTO respDTO = JSONUtil.toBean(jsonObject,SmtParkRespDTO.class);
				if(respDTO.getId().intValue() == 161 && !smtStaffDTO.getCompId().equals("171")){
					//石岩员工
					List<String> strings = Arrays.asList(excludeServiceMoudle);
					List<SubModuleDetailVo> tempBusSubModuleDetaiList = busSubModuleDetaiList.stream().filter(s ->
							!strings.contains(s.getModuleName())).collect(Collectors.toList());
					busSubModuleDetaiList = new ArrayList<>(tempBusSubModuleDetaiList);
					break;
				}
				// 许昌园区并且是保安才返回“返厂确认”菜单
//				if (respDTO.getParkName() != null && respDTO.getParkName().contains("许昌园区")) {
//					if (smtStaffDTO.getJobName() != null && smtStaffDTO.getJobName().contains("保安")) {
//						if (specialModule.isPresent()) {
//							busSubModuleDetaiList.add(specialModule.get());
//						}
//					} else {
//						busSubModuleDetaiList.removeIf(item -> item.getModuleName().equals("扫码放行"));
//					}
//				}
			}
		}

		// 设置业务模块
		moduleListVo.setServiceModule(busSubModuleDetaiList);

		// 查询自定义顶级模块
		List<AppModuleInfo> dbCustomModuleList = appModuleInfoService.getTopModule(ModuleCatalog.CUSTOM.getType());
		if (CollectionUtils.isNotEmpty(dbCustomModuleList)) {
			// 附加模块集合
			List<SubModuleInfoVo> extraModule = new ArrayList<SubModuleInfoVo>();
			SubModuleInfoVo subModuleInfoVo = null;

			for (AppModuleInfo customModule : dbCustomModuleList) {
				subModuleInfoVo = new SubModuleInfoVo();
				subModuleInfoVo.setHubModuleName(customModule.getModuleName());// 自定义模块名称

				//自定义模块不过滤
				List<SubModuleDetailVo> custSubModuleDetaiList = buildForeModuleDetail(customModule.getId(),staffModuleList,false);
				// 设置子模块详情信息集合
				subModuleInfoVo.setSubModule(custSubModuleDetaiList);

				// 自定义模块集合添加子模块
				extraModule.add(subModuleInfoVo);
			}

			// 设置自定义模块
			moduleListVo.setExtraModule(extraModule);
		}
		return moduleListVo;
	}

	/**
	 * 组装App手机端模块详情信息
	 *
	 * @param parentModuleId 父级模块ID
	 * @return
	 */
	private List<SubModuleDetailVo> buildForeModuleDetail(Integer parentModuleId,List<String> staffModuleList,boolean isFilter) {
		// 业务子模块集合
		List<SubModuleDetailVo> subModuleDetaiList = new ArrayList<SubModuleDetailVo>();

		// 查询自定义子模块
		List<AppModuleInfo> dbSubBisinebModule = appModuleInfoService.getSubModuleByPid(parentModuleId);
		if (CollectionUtils.isNotEmpty(dbSubBisinebModule)) {
			SubModuleDetailVo subModuleDetail = null;
			String moduleUrl = "";
			for (AppModuleInfo moduleInfoTemp : dbSubBisinebModule) {
				//过滤用户未分配的模块ID
				if(isFilter){
					if(CollectionUtils.isEmpty(staffModuleList) || !staffModuleList.contains(String.valueOf(moduleInfoTemp.getId()))){
						continue;
					}
				}

				subModuleDetail = new SubModuleDetailVo();
				subModuleDetail.setModuleName(moduleInfoTemp.getModuleName());
				subModuleDetail.setModuleIcon(appCommService.buildModuleImageUrl(moduleInfoTemp.getId()));
				moduleUrl = moduleInfoTemp.getModuleUrl().replace("${badge}", SecurityUtils.getUser().getUsername());
				subModuleDetail.setModuleUrl(moduleUrl);

				// 内容类型
				if (StringUtils.isNotBlank(moduleUrl)
						&& !(moduleUrl.startsWith("http")
								|| moduleUrl.startsWith("HTTP"))) {
					subModuleDetail.setContentLinkType(AppContentType.MODULE.getType());
				} else {
					subModuleDetail.setContentLinkType(AppContentType.LINK.getType());
				}

				// 集合添加子模块详情信息
				subModuleDetaiList.add(subModuleDetail);
			}
		}

		return subModuleDetaiList;
	}

}
