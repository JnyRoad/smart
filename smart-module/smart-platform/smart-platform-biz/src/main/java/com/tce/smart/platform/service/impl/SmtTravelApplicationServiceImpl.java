package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.data.api.dto.businesstrip.CcdFormtableMainDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainDt1RespDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainDt2RespDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainRespDTO;
import com.tce.smart.data.api.feign.businesstrip.RemoteFormTableMainService;
import com.tce.smart.platform.core.dto.AddSmtTravelApplicationDTO;
import com.tce.smart.platform.core.dto.SearchTravelDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtTravelApplication;
import com.tce.smart.platform.core.mapper.SmtTravelApplicationMapper;
import com.tce.smart.platform.core.vo.EmployeeTraveDayVO;
import com.tce.smart.platform.core.vo.FlowVO;
import com.tce.smart.platform.core.vo.SearchTravelApplicationVO;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtTravelApplicationService;
import com.tce.smart.tool.enums.ApplicationEnum;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.enums.TransportLargeClassEnum;
import com.tce.smart.tool.enums.TransportSubClassEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 职工出差申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Service
@AllArgsConstructor
public class SmtTravelApplicationServiceImpl extends ServiceImpl<SmtTravelApplicationMapper, SmtTravelApplication> implements SmtTravelApplicationService {

	private final SmtStaffService smtStaffService;
	private final RemoteFormTableMainService remoteFormTableMainService;
	private final IOAWorkflowService oaWorkflowService;
	/**
	 * 获取出差记录分页列表
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IPage<CcdFormtableMainRespDTO> getSmtTravelApplicationPage(Page page,SearchTravelDTO searchTravelDTO) {
		//判断参数是否为空值
		if(StringUtils.isEmpty(searchTravelDTO.getStaffBadge())){
			 throw new TCEException(ExceptionTypeEnum.TRAVEL_STAFF_BADGE_ERROE);
		}
		//根据员工号查询员工的部门和岗位
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, searchTravelDTO.getStaffBadge()));
		if (Objects.isNull(selectOne)) {
			throw new TCEException("未找到员工信息");
		}
		//查询出差的接口 转义
		Result<Page<CcdFormtableMainRespDTO>> result = remoteFormTableMainService.info(page.getCurrent(), page.getSize(), searchTravelDTO.getStaffBadge(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		IPage<CcdFormtableMainRespDTO> pageInfo = result.getData();
		// 转换接过来的值
		if (CommonConstants.SUCCESS == result.getCode()) {
			List travelList = new ArrayList();
			for (int i = 0; i < pageInfo.getRecords().size(); i++) {
//				CcdFormtableMain ccdFormtableMain = new CcdFormtableMain();
				SearchTravelApplicationVO searchTravelApplicationVO = new  SearchTravelApplicationVO ();
				CcdFormtableMainRespDTO ccdFormtableMain = pageInfo.getRecords().get(i);
				//赋值
				searchTravelApplicationVO.setJobName(selectOne.getJobName());
				searchTravelApplicationVO.setCompName(selectOne.getCompName());
				addSearchTravel(searchTravelApplicationVO,ccdFormtableMain);
				travelList.add(searchTravelApplicationVO);
			}
			pageInfo.setRecords(travelList);
		}
		return pageInfo;
	}

	//赋值
	private void addSearchTravel(SearchTravelApplicationVO searchTravelApplicationVO, CcdFormtableMainRespDTO ccdFormtableMain) {
		searchTravelApplicationVO.setRecordId(ccdFormtableMain.getMainId().toString());
		searchTravelApplicationVO.setRecordTitle(ccdFormtableMain.getPedestrianName()+"的出差申请");
		searchTravelApplicationVO.setStaffName(ccdFormtableMain.getPedestrianName());
		//根据id查询出差地点
		searchTravelApplicationVO.setTravelCity(getOATravelCity(ccdFormtableMain.getMainId()));
		//根据员工流程id 获取状态
		searchTravelApplicationVO.setRecordDesc(getOAProcess(ccdFormtableMain.getRequestId()));
		searchTravelApplicationVO.setStartDate(ccdFormtableMain.getTripBeginTime());
		searchTravelApplicationVO.setEndDate(ccdFormtableMain.getTripEndTime());
		searchTravelApplicationVO.setRecordDate(ccdFormtableMain.getApplicationTime());
	}
	/**
	 * 根据id获取出差地点
	 * @param id
	 * @return
	 */
	public String getOATravelCity(Integer id) {
		String travelCity = "";
		Result<List<CcdFormtableMainDt1RespDTO>> result = remoteFormTableMainService.infoDay(id,SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (CommonConstants.SUCCESS == result.getCode()) {
			//判断是否为空
			if(ObjectUtil.isNotNull(result.getData())) {
				@SuppressWarnings("unchecked")
				List<CcdFormtableMainDt1RespDTO> list = result.getData();
				if(list.size()>0) {
					CcdFormtableMainDt1RespDTO ccdFormtableMainDt1 = list.get(0);
					travelCity = ccdFormtableMainDt1.getArrivalCity();
				}

			}
		}
		return travelCity;
	}
	/**
	 * 根据员工流程id
	 * @param processId
	 */
	public String getOAProcess(String processId) {
		String recordDesc = "";
		WorkFlowLogDTO workFlowLogDTO = oaWorkflowService.query(processId);
	if(ObjectUtil.isNotNull(workFlowLogDTO) && workFlowLogDTO.success()) {
		List<WorkFlowLogDataDTO> flowRecords = workFlowLogDTO.getResultdata();
		orderList(flowRecords);
		if(flowRecords.size()>0) {
			if(flowRecords.get(0).getLOGTYPE().equals(ApplicationEnum.RECORD_STATUS_e.getCode()) || flowRecords.get(0).getLOGTYPE().equals(ApplicationEnum.RECORD_STATUS_1.getCode())) {
				recordDesc = ApplicationEnum.RECORD_STATUS_11.getDesc();
			}else {
				recordDesc = ApplicationEnum.RECORD_STATUS_10.getDesc();
			}
		}
	}
		return recordDesc;
	}

	//排序
	public void orderList(List<WorkFlowLogDataDTO> list) {
		//排序
		Collections.sort(list, new Comparator<WorkFlowLogDataDTO>() {
			@Override
			public int compare(WorkFlowLogDataDTO n1, WorkFlowLogDataDTO n2) {
				try {
					if(com.tce.smart.common.core.util.StringUtils.isNotEmpty(String.valueOf(DateUtils.parse(n1.getOPERATETIME()).getTime())) && com.tce.smart.common.core.util.StringUtils.isNotEmpty(String.valueOf(DateUtils.parse(n2.getOPERATETIME()).getTime()))  ) {
						Integer dt1 = Integer.parseInt(String.valueOf(DateUtils.parse(n1.getOPERATEDATE()+" "+n1.getOPERATETIME()).getTime()/1000));
						Integer dt2 = Integer.parseInt(String.valueOf(DateUtils.parse(n2.getOPERATEDATE()+" "+n2.getOPERATETIME()).getTime()/1000));
						if (dt1 > dt2) {
							return -1;
						} else if (dt1 < dt2) {
							return 1;
						} else {
							return 0;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}
		});
	}
	/**
	 * 获取出差对象
	 */
	public SmtTravelApplication getTravelApplication(AddSmtTravelApplicationDTO addSmtTravelApplicationDTO) {
		SmtTravelApplication smtTravelApplication = new SmtTravelApplication ();

		//根据员工编号查询员工的信息
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge,addSmtTravelApplicationDTO.getStaffBadge()));
		smtTravelApplication.setStaffId(selectOne.getId());
		smtTravelApplication.setStaffBadge(addSmtTravelApplicationDTO.getStaffBadge());
		smtTravelApplication.setStaffName(selectOne.getName());
		smtTravelApplication.setStartTime(DateUtils.parse(addSmtTravelApplicationDTO.getStartDate()));
		smtTravelApplication.setEndTime(DateUtils.parse(addSmtTravelApplicationDTO.getEndDate()));
		smtTravelApplication.setDuration(Integer.valueOf(addSmtTravelApplicationDTO.getTravelCount()));
		smtTravelApplication.setCause(addSmtTravelApplicationDTO.getTravelDesc());
		smtTravelApplication.setCreateTime(DateUtil.date());
        smtTravelApplication.setTravleLocal(addSmtTravelApplicationDTO.getTravelCity());
		return smtTravelApplication;
	}

	/**
	 * 根据id获取出差的信息
	 */
	public CcdFormtableMainDTO getTravelApplicationById(Integer id) {
//		CcdFormtableMain ccdFormtableMain =  new CcdFormtableMain ();
		Result<CcdFormtableMainRespDTO> result = remoteFormTableMainService.infoTravel(id, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (CommonConstants.SUCCESS == result.getCode()) {
			CcdFormtableMainRespDTO  ccdFormtableMain = result.getData();
			CcdFormtableMainDTO ccdFormtableMainDTO = new CcdFormtableMainDTO();
			BeanUtils.copyProperties(ccdFormtableMain,ccdFormtableMainDTO);
			return ccdFormtableMainDTO;
		}else {
		    throw new TCEException(ExceptionTypeEnum.TRAVEL_ERROE);
		}
}

	/**
	 * 查询出差日程
	 */
	@Override
	public List<EmployeeTraveDayVO>  getInfoDay(Integer id) {
		List<EmployeeTraveDayVO> listDay  = new ArrayList<EmployeeTraveDayVO> ();
		Result<List<CcdFormtableMainDt1RespDTO>> result = remoteFormTableMainService.infoDay(id,SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (CommonConstants.SUCCESS == result.getCode()) {
			//判断是否为空
			if(ObjectUtil.isNotNull(result.getData())) {
				@SuppressWarnings("unchecked")
				List<CcdFormtableMainDt1RespDTO> list = result.getData();
				if(list.size()>0) {
					for (int i = 0; i < list.size(); i++) {
						CcdFormtableMainDt1RespDTO ccdFormtableMainDt1 = list.get(0);
						EmployeeTraveDayVO employeeTraveDayVO  = new EmployeeTraveDayVO ();
						 BeanUtil.copyProperties(ccdFormtableMainDt1, employeeTraveDayVO);
					      if(ObjectUtil.isNotNull(ccdFormtableMainDt1.getTransportLargeClass())) {
						  employeeTraveDayVO.setTransportLargeClassDesc(TransportLargeClassEnum.desc(ccdFormtableMainDt1.getTransportLargeClass()));
						 }
					      if(ObjectUtil.isNotNull(ccdFormtableMainDt1.getTransportSubClass())) {
						  employeeTraveDayVO.setTransportSubClassDesc(TransportSubClassEnum.desc(ccdFormtableMainDt1.getTransportSubClass()));
					      }
					      listDay.add(employeeTraveDayVO);
					}
				}
			}
		}
		return listDay;
	}

	/**查询出差报告
	 *
	 */
	@Override
	public Result<List<CcdFormtableMainDt2RespDTO>> getInfoReport(Integer id) {
		Result<List<CcdFormtableMainDt2RespDTO>> result = remoteFormTableMainService.infoReport(id,SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		return result;
	}


	@Override
	public List<FlowVO> getInfoFlow(Integer id) {
		List<FlowVO> list = new ArrayList<FlowVO> ();
		CcdFormtableMainRespDTO ccdFormtableMain =  new CcdFormtableMainRespDTO ();
		//查询出差的信息
		Result<CcdFormtableMainRespDTO> result = remoteFormTableMainService.infoTravel(id, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (CommonConstants.SUCCESS == result.getCode()) {
			 ccdFormtableMain = result.getData();
		}
		//判断流程id不为空
		if(!StringUtils.isEmpty(ccdFormtableMain.getRequestId())) {
			getOAProcessFlow(ccdFormtableMain.getRequestId(),list);
		}
		return list;
	}
	//获取流程数据
	public void getOAProcessFlow(String processId,List<FlowVO> list) {
		WorkFlowLogDTO workFlowLogDTO = oaWorkflowService.query(processId);
	if(ObjectUtil.isNotNull(workFlowLogDTO) && workFlowLogDTO.success()) {
		List<WorkFlowLogDataDTO> flowRecords = workFlowLogDTO.getResultdata();
		    if(CollectionUtils.isNotEmpty(flowRecords)){
		        flowRecords.forEach(flowRecord->getProcessRecord(list, flowRecord));
		    }
	}
	}

	private void getProcessRecord(List<FlowVO> list,WorkFlowLogDataDTO process) {
		    FlowVO flowVO = new FlowVO ();
		    if(StrUtil.isEmpty(process.getNODENAME())) {
	             flowVO.setNodeName("");
	            }else {
	             String[] nodeNames = process.getNODENAME().split(" ");
	             if(nodeNames.length == 2) {
	              flowVO.setNodeName(nodeNames[1]);
	             }
	            }
			String dateTime = process.getOPERATEDATE() + " " + process.getOPERATETIME();
			if(StrUtil.isNotBlank(process.getOPERATEDATE()) && StrUtil.isNotBlank(process.getOPERATETIME())) {
				flowVO.setProcessDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
			}
			flowVO.setProcessDesc(ApplicationEnum.desc(process.getLOGTYPE()));
			list.add(flowVO);
	}
}
