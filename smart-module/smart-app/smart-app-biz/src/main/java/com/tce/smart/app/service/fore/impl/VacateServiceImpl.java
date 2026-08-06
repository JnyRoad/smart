package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.ao.fore.VacateClassAo;
import com.tce.smart.app.service.fore.VacateService;
import com.tce.smart.app.vo.fore.*;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.PaginationConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.attendance.resp.KQShiftDetailsRespDTO;
import com.tce.smart.data.api.dto.ehrview.LvwLcdLeavetypeDTO;
import com.tce.smart.data.api.feign.attendance.RemoteKQShiftDetailsService;
import com.tce.smart.data.api.feign.ehrview.RemoteLvwLcdLeavetypeService;
import com.tce.smart.platform.api.dto.req.AddAskLeavelApplicationReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchAskLeaveApplicationDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchAskLeaveApplicationRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchAskLeaveTypeRespDTO;
import com.tce.smart.platform.api.feign.RemoteAskLeaveService;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * 請假接口实现
 * @author ly
 *
 */
@Service
@AllArgsConstructor
@Slf4j
public class VacateServiceImpl implements VacateService {


	private RemoteAskLeaveService remoteAskLeaveService;
	private RemoteLvwLcdLeavetypeService remoteLvwLcdLeavetypeService;
	private RemoteDictService remoteDictService;
	private final RemoteKQShiftDetailsService remoteKQShiftDetailsService;

	/**
	 * 获取请假类型
	 */
	@Override
	public VacateTypeVo getVacateType() {
		VacateTypeVo vacateTypeVo =new VacateTypeVo ();
		//根据feign调用请假类型接口
		Result<List<SearchAskLeaveTypeRespDTO>> result = remoteAskLeaveService.getAskTypeList(SecurityConstants.FROM_IN);
		//转换接过来的值
		if (CommonConstants.SUCCESS  == result.getCode()) {
			@SuppressWarnings("unchecked")
			List<SearchAskLeaveTypeRespDTO> list = result.getData();
			vacateTypeVo.setRecords(list);
			vacateTypeVo.setTotal(list.size());
		}
		return vacateTypeVo;
	}

	/**
	 * 获取请假列表
	 */
	@SuppressWarnings("unchecked")
	public Page<?> getVacateList(Map<String, Object> params) {
		// 获取员工号
		String staffBadge = SecurityUtils.getUser().getUsername();
		Result<Page<SearchAskLeaveApplicationRespDTO>> result = remoteAskLeaveService.getAskLeavePage(MapUtil.getInt(params, PaginationConstants.CURRENT), MapUtil.getInt(params, PaginationConstants.SIZE),
				staffBadge,SecurityConstants.FROM_IN);

		Page<SearchAskLeaveApplicationRespDTO> pageInfo = result.getData();
		//判斷值是否為空
		if (CollectionUtils.isNotEmpty(pageInfo.getRecords())) {
			@SuppressWarnings("rawtypes")
			List vacateList = new ArrayList();
			VacateApplicationVo vacateApplicationVo = null;
			SearchAskLeaveApplicationRespDTO searchAskLeaveApplicationVO = null;
			for (int i = 0; i < pageInfo.getRecords().size(); i++) {
				vacateApplicationVo = new VacateApplicationVo();
				searchAskLeaveApplicationVO = pageInfo.getRecords().get(i);
				vacateApplicationVo.setRecordId(String.valueOf(searchAskLeaveApplicationVO.getRecordId()));
				vacateApplicationVo.setRecordTitle(searchAskLeaveApplicationVO.getStaffName()+"的请假申请");
				vacateApplicationVo.setRecordDesc(searchAskLeaveApplicationVO.getRecordDesc());
				vacateApplicationVo.setStartDate(searchAskLeaveApplicationVO.getStartDate());
				vacateApplicationVo.setEndDate(searchAskLeaveApplicationVO.getEndDate());
				vacateApplicationVo.setVacateCount(searchAskLeaveApplicationVO.getVacateCount());
				vacateApplicationVo.setRecordDate(searchAskLeaveApplicationVO.getRecordDate());
				vacateApplicationVo.setUnit(searchAskLeaveApplicationVO.getUnit());
				vacateList.add(vacateApplicationVo);
			}
			pageInfo.setRecords(vacateList);
		}
		return pageInfo;
	}

	/**
	 * 获取请假详情
	 */
	public VacateDetailVo getVacateDetail(AllApplicationAo vacateAoId) {
		Result<SearchAskLeaveApplicationDetailRespDTO> result = remoteAskLeaveService.getAskLeaveById(Integer.parseInt(vacateAoId.getRecordId()), SecurityConstants.FROM_IN);
		VacateDetailVo vacateDetailVo = new VacateDetailVo();
		EmployeeVacateDetailVo employee = new EmployeeVacateDetailVo ();
		SearchAskLeaveApplicationDetailRespDTO detail = result.getData();
		employee.setEmployeeId(String.valueOf(detail.getEmployee().getEmployeeId()));
		employee.setEmployeeName(detail.getEmployee().getEmployeeName());
		employee.setEmployeeBadge(detail.getEmployee().getEmployeeBadge());
		employee.setBuName(detail.getEmployee().getBuName());
		employee.setDeptName(detail.getEmployee().getDeptName());
		employee.setJobName(detail.getEmployee().getJobName());
		employee.setVacateTypeDesc(detail.getEmployee().getVacateTypeDesc());
		employee.setStartDate(detail.getEmployee().getStartDate());
		employee.setEndDate(detail.getEmployee().getEndDate());
		employee.setVacateCount(detail.getEmployee().getVacateCount());
		employee.setVacateDesc(detail.getEmployee().getVacateDesc());
		employee.setClassName(detail.getEmployee().getClassName());
		employee.setSecondEnter(detail.getEmployee().getSecondEnter());
		employee.setSecondOut(detail.getEmployee().getSecondOut());
		employee.setFourthEnter(detail.getEmployee().getFourthEnter());
		employee.setFourthOut(detail.getEmployee().getFourthOut());
		employee.setFifthEnter(detail.getEmployee().getFifthEnter());
		employee.setFifthOut(detail.getEmployee().getFifthOut());
		employee.setPhoto(detail.getEmployee().getPhoto());
		vacateDetailVo.setEmployee(employee);
		vacateDetailVo.setFlow(detail.getFlow());
		vacateDetailVo.setProcessId(result.getData().getProcessId());
		return vacateDetailVo;
	}

	/**
	 * 添加请假申请
	 */
	@Override
	public void addVacate(AddAskLeavelApplicationReqDTO addAskLeavelApplicationDTO) {
		//添加当前的员工号
		addAskLeavelApplicationDTO.setStaffBadge(SecurityUtils.getUser().getUsername());
		//调用接口传入后台
		Result<?> result = remoteAskLeaveService.add(addAskLeavelApplicationDTO, SecurityConstants.FROM_IN);
		if(!CommonConstants.SUCCESS .equals(result.getCode()) ){
			throw new TCEException(result.getCode(), result.getMsg());
		}
	}

	/**
	 * 查询班次信息
	 */
	public VacateClassVo getVacateClasses(VacateClassAo vacateClassAo) {
		VacateClassVo vacateClassVo = new VacateClassVo ();
		Result<KQShiftDetailsRespDTO> result = remoteKQShiftDetailsService.info(SecurityUtils.getUser().getUsername(), vacateClassAo.getQueryDay(), SecurityConstants.FROM_IN);
		if (CommonConstants.SUCCESS  == result.getCode()) {
			if(ObjectUtil.isNotNull(result.getData())) {
				vacateClassVo.setClassDesc(result.getData().getRunName());
				vacateClassVo.setSecondEnter(result.getData().getRun2StartTime());
				vacateClassVo.setSecondOut(result.getData().getRun2EndTime());
				vacateClassVo.setFourthEnter(result.getData().getRun4StartTime());
				vacateClassVo.setFourthOut(result.getData().getRun4EndTime());
				vacateClassVo.setFifthEnter(result.getData().getRun5StartTime());
				vacateClassVo.setFifthOut(result.getData().getRun5EndTime());
			}
		}
		return vacateClassVo;

	}


	/**
	 * 获取时长单位
	 */
	public VacateUnitVo getUnitByVacateType(String vacateCode) {

		//判断请假类型是否为空
		if(StringUtils.isEmpty(vacateCode)) {
			throw new TCEException(ExceptionTypeEnum.ASK_LEAVE_TYPE_ERROR);
		}
		VacateUnitVo vacateUnitVo = new VacateUnitVo ();
		Result<LvwLcdLeavetypeDTO> result = remoteLvwLcdLeavetypeService.info(Integer.parseInt(vacateCode), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (CommonConstants.SUCCESS  == result.getCode()) {
			if(ObjectUtil.isNotNull(result.getData())) {
				//根据时长id获取时长单位描述
				Result<SysDict> findByType = remoteDictService.findByValue(DictConstants.LEAVE_UNIT,result.getData().getXunit().toString(), SecurityConstants.FROM_IN);
				//判断是否为空
				if(ObjectUtil.isNotNull(findByType.getData())) {
					//根据字典表查询补卡原因类型数据
					vacateUnitVo.setUnit(findByType.getData().getLabel());
				}
			}
		}
		return vacateUnitVo;
	}

}
