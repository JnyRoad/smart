package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.SecurityAuthApplyDetailAreaReqDTO;
import com.tce.smart.data.api.dto.msg.req.SecurityAuthApplyDetailReqDTO;
import com.tce.smart.data.api.dto.msg.req.SecurityAuthApplyMainReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendSecurityAuthApplyReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityApplyPersonReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthApplyPageQueryReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthApplyReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthApplyPageRespDTO;
import com.tce.smart.platform.core.ao.SecurityAuthApplyPageQueryAO;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtSecurityArea;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthApplyMapper;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.platform.core.service.SmtSecurityAreaService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.admittance.SmtOaAreaTypeService;
import com.tce.smart.platform.service.securityzone.SmtOaAreaRelationService;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthApplyService;
import com.tce.smart.platform.service.securityzone.SmtSecurityTaskDetailsService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.WeChatMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:31
 */
@Service
@Slf4j
public class SmtSecurityAuthApplyServiceImpl extends ServiceImpl<SmtSecurityAuthApplyMapper, SmtSecurityAuthApply> implements SmtSecurityAuthApplyService {

	@Autowired
	private RemoteOaWorkFlowService remoteOaWorkFlowService;
	@Autowired
	private SmtSecurityApplyPersonServiceImpl smtSecurityApplyPersonService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtSecurityTaskDetailsService smtSecurityTaskDetailsService;
	@Autowired
	private SmtOaAreaTypeService smtOaAreaTypeService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtOaAreaRelationService smtOaAreaRelationService;
	@Autowired
	private SmtMsgTemplateService smtMsgTemplateService;
	@Autowired
	private SmtSecurityAreaService smtSecurityAreaService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveApply(SecurityAuthApplyReqDTO reqDTO) {
		String processId;
		//发送OA申请
		processId = this.sendOaProcess(reqDTO);
		if(StrUtil.isEmpty(processId)) {
			throw new SmartException("发起OA审批流程失败");
		}
		if (("-7").equals(processId)) {
			throw new SmartException("请确认申请人是否存在OA上级审批人");
		}
		//存储申请记录
		SmtSecurityAuthApply authApply = BeanUtils.transform(SmtSecurityAuthApply.class, reqDTO);
		authApply.setProcessId(processId);
		authApply.setAreaType(StrUtil.join(SymbolConstants.COMMA, reqDTO.getAreaType()));
		authApply.setCreateTime(LocalDateTime.now());
		authApply.setOaStatus(ApproveListStateEnum.PENDING.getCode());
		authApply.setDeviceStatus(DeviceDownStatusEnum.WAIT.getCode());
		authApply.setTotalNum(reqDTO.getPersonList().size());
		authApply.setIsMsg(OneOrZeroEnum.ZERO.getCode());
		this.save(authApply);
		//获得申请人员
		List<SecurityApplyPersonReqDTO> personList = reqDTO.getPersonList();
		smtSecurityApplyPersonService.savePerson(personList, authApply.getId());
		//初始化权限下发列表
		return smtSecurityTaskDetailsService.initTask(personList, authApply.getId());
	}

	@Override
	public void updateStatus(SmtSecurityAuthApply authApply) {
		if (ApproveListStateEnum.AGREE.getCode().equals(authApply.getOaStatus())) {
			try {
				smtSecurityTaskDetailsService.downDevice(authApply.getId(), authApply.getApplyBadge());
				authApply.setDeviceStatus(DeviceDownStatusEnum.ALRAEDY.getCode());
			} catch (Exception e) {
				log.error("保密区申请权限下发失败");
			}
		}
		this.updateById(authApply);
	}

	@Override
	public Boolean downDevice(Long applyId) {
		SmtSecurityAuthApply authApply = this.getById(applyId);
		smtSecurityTaskDetailsService.downDevice(applyId, authApply.getApplyBadge());
		authApply.setDeviceStatus(DeviceDownStatusEnum.ALRAEDY.getCode());
		return this.updateById(authApply);
	}

	@Override
	public SmtSecurityAuthApply getByProcessId(String processId) {
		return this.getOne(Wrappers.<SmtSecurityAuthApply>query().lambda().eq(SmtSecurityAuthApply::getProcessId, processId));
	}

	/**
	 * 发送OA申请
	 *
	 * @param reqDTO
	 * @return
	 */
	private String sendOaProcess(SecurityAuthApplyReqDTO reqDTO) {
		SendSecurityAuthApplyReqDTO sendSecurityAuthApplyReqDTO = new SendSecurityAuthApplyReqDTO();
		//主申请单
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(reqDTO.getApplyBadge());
		SecurityAuthApplyMainReqDTO mainReqDTO = new SecurityAuthApplyMainReqDTO();
		String zero = OneOrZeroEnum.ZERO.getCode().toString();
		mainReqDTO.setLcbh("");
		mainReqDTO.setSqjrqy(reqDTO.getAreaId());
		mainReqDTO.setBadge(reqDTO.getApplyBadge());
		mainReqDTO.setName(staff.getName());
		mainReqDTO.setCompid(staff.getCompId());
		List<SmtSecurityArea> areaList = smtSecurityAreaService.list();
		Map<String, List<Field>> fields = Arrays.stream(mainReqDTO.getClass().getDeclaredFields()).collect(Collectors.groupingBy(Field::getName));
		areaList.forEach(area -> {
			List<Field> fs = fields.get(area.getType());
			if (CollUtil.isEmpty(fs)) {
				return;
			}
			Field field = fs.get(0);
			try {
				field.setAccessible(true);
				field.set(mainReqDTO, zero);
			} catch (Exception e) {
				log.error("保密区区域赋初始值异常: type={}, {}", area.getType(), e.getMessage(), e);
			}
		});
//		mainReqDTO.setAa(zero);
//		mainReqDTO.setBb(zero);
//		mainReqDTO.setCc(zero);
//		mainReqDTO.setFf(zero);
//		mainReqDTO.setDd(zero);
//		mainReqDTO.setEe(zero);
//		mainReqDTO.setGg(zero);
//		mainReqDTO.setHh(zero);
//		mainReqDTO.setJj(zero);
//		mainReqDTO.setTt(zero);
//		mainReqDTO.setKk(zero);
//		mainReqDTO.setLl(zero);
//		mainReqDTO.setQq(zero);
//		mainReqDTO.setWw(zero);
//		mainReqDTO.setRr(zero);
//
//		mainReqDTO.setTiantai(zero);
//		mainReqDTO.setLianban(zero);
//		mainReqDTO.setTwoe(zero);
//		mainReqDTO.setThreee(zero);
//		mainReqDTO.setFoure(zero);
//		mainReqDTO.setFivee(zero);
//		mainReqDTO.setSixe(zero);
//		mainReqDTO.setSeven(zero);
//		mainReqDTO.setEighte(zero);
		mainReqDTO.setOo("");
		mainReqDTO.setSqjinruquyu("");
		if(StringUtils.isNotEmpty(reqDTO.getPermitArea())) {
			mainReqDTO.setOo(reqDTO.getPermitArea());
		}
		if(StringUtils.isNotEmpty(reqDTO.getPermitOldArea())) {
			mainReqDTO.setSqjinruquyu(reqDTO.getPermitOldArea());
		}
		this.setArea(mainReqDTO, reqDTO.getAreaType(), areaList);
		//OA的选项值，为固定值
		mainReqDTO.setSqxm("17");
		sendSecurityAuthApplyReqDTO.setSecurityAuthApplyMainReqDTO(mainReqDTO);
		//申请人员
		List<SecurityApplyPersonReqDTO> applyPersonList = reqDTO.getPersonList();
		if (CollUtil.isEmpty(applyPersonList)) {
			return null;
		}
		List<SecurityAuthApplyDetailReqDTO> personReq = applyPersonList.stream().map(person -> {
			List<String> authName = person.getApplyAuths().stream().map(SecurityApplyPersonReqDTO.ApplyAuth::getAuthName).collect(Collectors.toList());
			SecurityAuthApplyDetailReqDTO p = new SecurityAuthApplyDetailReqDTO();
			p.setSqrbm(person.getStaffDepId());
			p.setSqrgh(person.getBadge());
			p.setSqrzw(person.getStaffJobId());
			p.setSqrxm("17");
			p.setSqsy(StringUtils.join(SymbolConstants.BRANCH, authName) + "申请");
			return p;
		}).collect(Collectors.toList());
		sendSecurityAuthApplyReqDTO.setSecurityAuthApplyDetailReqDTOs(personReq);
		//申请区域
		List<SecurityAuthApplyDetailAreaReqDTO> auth = new ArrayList<>();
		SecurityAuthApplyDetailAreaReqDTO areaReq = new SecurityAuthApplyDetailAreaReqDTO();
		areaReq.setSqjrqy(reqDTO.getAreaId());
		auth.add(areaReq);
		sendSecurityAuthApplyReqDTO.setSecurityAuthApplyDetailAreaReqDTOS(auth);
		Result<String> result = remoteOaWorkFlowService.sendSecurityAuthApply(sendSecurityAuthApplyReqDTO);
		if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
			log.debug("门禁申请OA提交异常，错误信息：{}", result.getData());
			throw new SmartException("OA流程提交异常，请确认OA是否存在人员信息");
		}
		if (StringUtils.isNotEmpty(result.getData())) {
			return result.getData();
		}
		return null;
	}

	private SecurityAuthApplyMainReqDTO setArea(SecurityAuthApplyMainReqDTO main, List<Integer> check, List<SmtSecurityArea> areaList) {
		if (CollUtil.isEmpty(check)) {
			return main;
		}
		for (Integer id : check) {
			SmtSecurityArea securityArea = areaList.stream().filter(area -> area.getCode().equals(id)).findFirst().orElse(null);
			if (Objects.isNull(securityArea)) {
				continue;
			}
			try {
				Class<? extends SecurityAuthApplyMainReqDTO> aClass = main.getClass();
				Field[] fields = aClass.getDeclaredFields();
				Field field = Arrays.stream(fields).filter(f -> f.getName().equals(securityArea.getType())).findFirst().orElse(null);
				if (Objects.isNull(field)) {
					continue;
				}
				field.setAccessible(true);
				field.set(main, SymbolConstants.ONE_STRING);
			} catch (IllegalAccessException e) {
				log.error("保密区区域设置字段值异常: id={}", id, e);
//				switch (areaEnum) {
//					case ITEM_0:
//						main.setJj(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_1:
//						main.setKk(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_2:
//						main.setLl(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_3:
//						main.setQq(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_4:
//						main.setWw(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_5:
//						main.setRr(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_6:
//						main.setTt(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_7:
//						main.setAa(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_8:
//						main.setBb(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_9:
//						main.setFf(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_10:
//						main.setCc(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_11:
//						main.setDd(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_12:
//						main.setEe(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_13:
//						main.setGg(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_14:
//						main.setHh(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_27:
//						main.setTiantai(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_28:
//						main.setLianban(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_29:
//						main.setTwoe(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_30:
//						main.setThreee(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_31:
//						main.setFoure(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_32:
//						main.setFivee(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_33:
//						main.setSixe(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_34:
//						main.setSeven(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_35:
//						main.setEighte(SymbolConstants.ONE_STRING);
//						break;
//				}
			}
		}
		return main;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public IPage<SecurityAuthApplyPageRespDTO> getPage(Page page, SecurityAuthApplyPageQueryReqDTO query) {
		SecurityAuthApplyPageQueryAO ao = new SecurityAuthApplyPageQueryAO();
		if (Objects.nonNull(query)) {
			ao = BeanUtils.transform(SecurityAuthApplyPageQueryAO.class, query);
		}
		ao.setParkIds(SecurityUtils.getUser().getParkIdList());
		IPage<SecurityAuthApplyPageRespDTO> pageDTO = this.baseMapper.getPage(page, ao);
		List<SecurityAuthApplyPageRespDTO> record = pageDTO.getRecords();
		record.forEach(r -> {
			SmtPark park = smtParkService.getById(r.getParkId());
			r.setParkName(park.getParkName());
			if (ApproveListStateEnum.AGREE.getCode().equals(r.getOaStatus())
					&& DeviceDownStatusEnum.ALRAEDY.getCode().equals(r.getDeviceStatus())) {
				Long applyId = r.getId();
				//刷新下发状态
				smtSecurityTaskDetailsService.syncTaskStatus(applyId);
				r.setSuccessNum(smtSecurityTaskDetailsService.getCount(applyId, DeviceDownStatusEnum.SUCCESS.getCode()));
				r.setFailNum(smtSecurityTaskDetailsService.getCount(applyId, DeviceDownStatusEnum.FAIL.getCode()));
			}
			r.setOaStatusDesc(ApproveListStateEnum.desc(r.getOaStatus()));
			r.setDeviceStatusDesc(DeviceDownStatusEnum.desc(r.getDeviceStatus()));
		});
		return pageDTO;
	}

	@Override
	public void sendMessage() {
		//获得已下发且未发送短信数据
		List<SmtSecurityAuthApply> applyList = this.list(Wrappers.<SmtSecurityAuthApply>query().lambda()
				.eq(SmtSecurityAuthApply::getDeviceStatus, DeviceDownStatusEnum.ALRAEDY.getCode())
				.eq(SmtSecurityAuthApply::getIsMsg, OneOrZeroEnum.ZERO.getCode()));
		//刷新下发状态
		if (CollUtil.isEmpty(applyList)) {
			return;
		}
		for (SmtSecurityAuthApply apply : applyList) {
			smtSecurityTaskDetailsService.syncTaskStatus(apply.getId());
			Integer initNum = smtSecurityTaskDetailsService.getCount(apply.getId(), DeviceDownStatusEnum.IN_WORK.getCode());
			if (initNum > 0) {
				continue;
			}
			SmtStaff staffName = smtStaffService.getSimpleSttaffByBadge(apply.getApplyBadge());
			String name = staffName.getName();
			Integer failNum = smtSecurityTaskDetailsService.getCount(apply.getId(), DeviceDownStatusEnum.FAIL.getCode());
			SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_SECURITY_11101.getCode());
			String workFlowName = "XCAJ02-许昌裕同保密权限申请表-" + name + '-' +
                    DateUtils.convert("yyyy/MM/dd", apply.getCreateTime());
			String msg = template.getTempContent().replace("{申请人}", name)
					.replace("{OA单标题}", workFlowName)
					.replace("{失败数量}", failNum.toString())
					.replace("{总数量}", apply.getTotalNum().toString());
			Boolean result = Boolean.FALSE;
			try {
				result = WeChatMsgUtil.sendMsg(apply.getApplyBadge(), msg, null, null);
			} catch (Exception e) {
				log.error("保密区微信推送失败：{}", e.getMessage());
			}
			if (result) {
				apply.setIsMsg(OneOrZeroEnum.ONE.getCode());
			}
			this.updateById(apply);
		}
	}
}
