package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.api.vo.SearchAdjustVO;
import com.tce.smart.app.service.fore.RestApplicationService;
import com.tce.smart.app.vo.fore.*;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.PaginationConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAdjustbasicFullRespDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteLvwAdjustbasicService;
import com.tce.smart.platform.api.dto.SearchBreakoffApplicationDetailDTO;
import com.tce.smart.platform.api.dto.SmtBreakoffApplicationDTO;
import com.tce.smart.platform.api.dto.req.AddBreakOffApplicationReqDTO;
import com.tce.smart.platform.api.dto.req.SearchPatchReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchBreakOffTypeRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchBreakoffApplicationRespDTO;
import com.tce.smart.platform.api.feign.RemoteBreakOffApplicationService;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 调休申请
 *
 * @author  梁圆
 * @date 2019-05-13 16:17:32
 */
@Service
@AllArgsConstructor
@Slf4j
public class RestApplicationServiceImpl implements RestApplicationService {

	private final RemoteBreakOffApplicationService remoteBreakOffApplicationService;
	private final RemoteLvwAdjustbasicService remoteLvwAdjustbasicService;
	/**
	 * 获取调休的类型
	 */
	public RestTypeVo getRestType() {
		//调用远程接口获取列表集合
		RestTypeVo restTypeVo =new RestTypeVo ();
		//根据feign调用请假类型接口
		Result<List<SearchBreakOffTypeRespDTO>> result = remoteBreakOffApplicationService.getBreakOffTypeList(SecurityConstants.FROM_IN);
		//转换接过来的值
		if (CommonConstants.SUCCESS  == result.getCode()) {
			List<SearchBreakOffTypeRespDTO> list = result.getData();
			restTypeVo.setRecords(list);
			restTypeVo.setTotal(list.size());
		}
		return restTypeVo;
	}

	/**
	 * 获取调休的列表
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Page<?> getRestList(Map<String, Object> params,String staffBadge) {

		if (StringUtils.isBlank(staffBadge)) {
			staffBadge = SecurityUtils.getUser().getUsername();
		}
        Result<Page<SearchBreakoffApplicationRespDTO>> result = remoteBreakOffApplicationService.getSmtBreakOffApplicationPage(MapUtil.getInt(params, PaginationConstants.CURRENT), MapUtil.getInt(params, PaginationConstants.SIZE),
		staffBadge,SecurityConstants.FROM_IN);
		Page<SearchBreakoffApplicationRespDTO> pageInfo = result.getData();
		// 转换接过来的值
		if (CommonConstants.SUCCESS == result.getCode()) {
			if (CollectionUtils.isNotEmpty(pageInfo.getRecords())) {
				List restList = new ArrayList();
				RestApplicationVo restApplicationVo = null;
				SearchBreakoffApplicationRespDTO searchBreakoffApplicationVO = null;
				for (int i = 0; i < pageInfo.getRecords().size(); i++) {
					restApplicationVo = new RestApplicationVo();
					searchBreakoffApplicationVO = pageInfo.getRecords().get(i);

					restApplicationVo.setRecordId(String.valueOf(searchBreakoffApplicationVO.getRecordId()));
					restApplicationVo.setRecordTitle(searchBreakoffApplicationVO.getStaffName()+"的调休申请");
					restApplicationVo.setRecordDesc(searchBreakoffApplicationVO.getRestDesc());
					restApplicationVo.setRestTypeDesc(searchBreakoffApplicationVO.getRecordTypeDesc());
					restApplicationVo.setRestDate(searchBreakoffApplicationVO.getRestDate());
					restApplicationVo.setRecordDate(searchBreakoffApplicationVO.getRecordDate());
					restList.add(restApplicationVo);
				}
				pageInfo.setRecords(restList);
			}
		} else {
			throw new TCEException(result.getCode(), result.getMsg());
		}
		return pageInfo;
	}

	/**
	 * 获取调休的详情
	 */
	@Override
	public RestDetailVo getRestDetail(AllApplicationAo restAoId) {

		//获取调休的详情
		Result<SearchBreakoffApplicationDetailDTO> result = remoteBreakOffApplicationService.getById(Integer.parseInt(restAoId.getRecordId()), SecurityConstants.FROM_IN);
		if(Objects.isNull(result.getData())) {
			throw new SmartException("调休记录为空");
		}
		RestDetailVo restDetailVo = new RestDetailVo();
		EmployeeRestDetailVo employee = new EmployeeRestDetailVo ();
		SearchBreakoffApplicationDetailDTO detail = result.getData();
		employee.setRestDate(detail.getEmployee().getRestDate());
		employee.setWorkDate(detail.getEmployee().getWorkDate());
		employee.setVacateTypeDesc(detail.getEmployee().getVacateTypeDesc());
		employee.setRestCount(String.valueOf(detail.getEmployee().getRestCount()));
		employee.setRestAbleCount(String.valueOf(detail.getEmployee().getRestAbleCount()));
		employee.setRestDesc(detail.getEmployee().getRestDesc());
		restDetailVo.setEmployee(employee);
		restDetailVo.setFlow(detail.getFlow());
		restDetailVo.setProcessId(detail.getProcessId());
		return restDetailVo;
	}

	/**
	 * 添加调休申请
	 */
	public void addRest(AddBreakOffApplicationReqDTO addBreakOffApplicationDTO) {
		//添加当前的员工号
		addBreakOffApplicationDTO.setStaffBadge(SecurityUtils.getUser().getUsername());
		//调用接口传入后台
		Result<?> result = remoteBreakOffApplicationService.saveBreakOffApplication(addBreakOffApplicationDTO, SecurityConstants.FROM_IN);
		if (CommonConstants.SUCCESS  != result.getCode()) {
			throw new TCEException(result.getCode(), result.getMsg());
		}
	}

	/**
	 * 获取可调休的天数
	 */
	@Override
	public AdjustVo getAdjust(String staffBadge) {

		AdjustVo adjustVo = new AdjustVo ();
		if (StringUtils.isBlank(staffBadge)) {
			staffBadge = SecurityUtils.getUser().getUsername();
		}
		List<SearchAdjustVO> adjustVoList = new ArrayList<SearchAdjustVO> ();
		Result<List<LvwAdjustbasicFullRespDTO>> result = remoteLvwAdjustbasicService.getByBadge(staffBadge,SecurityConstants.FROM_IN);
		if (CommonConstants.SUCCESS  == result.getCode()) {
			//转换接过来的值
			if (CommonConstants.SUCCESS  == result.getCode()) {
				double dayCount = 0;
				List<LvwAdjustbasicFullRespDTO> list = result.getData();
				for (LvwAdjustbasicFullRespDTO lvwAdjustbasic : list) {
					SearchAdjustVO searchAdjustVO  = new SearchAdjustVO ();
					if(lvwAdjustbasic.getAdjustTime()>0) {
						dayCount+=lvwAdjustbasic.getAdjustTime();
						searchAdjustVO.setWorkDate(lvwAdjustbasic.getTitle());
						searchAdjustVO.setTermCount(lvwAdjustbasic.getAdjustTime());
						searchAdjustVO.setTerm(lvwAdjustbasic.getTerm());
						searchAdjustVO.setTermId(lvwAdjustbasic.getId().toString());
						adjustVoList.add(searchAdjustVO);
					}
//					SearchPatchReqDTO searchPatchDTO = new SearchPatchReqDTO ();
//					searchPatchDTO.setStaffBadge(staffBadge);
//					searchPatchDTO.setPatchDate(DateUtil.format((lvwAdjustbasic.getTerm()), "yyyy-MM-dd HH:mm:ss"));
//					Result<List<SmtBreakoffApplicationDTO>> restCountList = remoteBreakOffApplicationService.getRestCountList(searchPatchDTO, SecurityConstants.FROM_IN);
//					double parseDouble = 0;
//					if (CommonConstants.SUCCESS  == restCountList.getCode()) {
//						List<SmtBreakoffApplicationDTO> listBreakOff = (List<SmtBreakoffApplicationDTO>) restCountList.getData();
//						if(listBreakOff.size()>0) {
//							for (int i = 0; i < listBreakOff.size(); i++) {
//								 parseDouble += Double.parseDouble(listBreakOff.get(i).getRestCount());
//							}
//						}
//							//判断两个值
//							Double value = new BigDecimal(lvwAdjustbasic.getAdjustTime()-parseDouble).doubleValue();
//							if(value>0) {
//								dayCount+=value;
//								searchAdjustVO.setWorkDate(lvwAdjustbasic.getTitle().split(":")[0]+":"+value);
//								searchAdjustVO.setTermCount(value);
//								searchAdjustVO.setTerm(lvwAdjustbasic.getTerm());
//								searchAdjustVO.setTermId(lvwAdjustbasic.getId().toString());
//								adjustVoList.add(searchAdjustVO);
//							}
//					}
				}
				adjustVo.setDayCount(dayCount);
				adjustVo.setRecords(adjustVoList);
			}
		}
		return adjustVo;
	}
}
