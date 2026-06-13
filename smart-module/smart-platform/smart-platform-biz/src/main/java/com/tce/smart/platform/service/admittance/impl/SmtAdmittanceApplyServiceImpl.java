package com.tce.smart.platform.service.admittance.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SnapVehicleConstants;
import com.tce.smart.platform.api.dto.CardDataDTO;
import com.tce.smart.platform.api.dto.req.SmbUrlDTO;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceFellowReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceCarApplyReqDTO;
import com.tce.smart.platform.core.service.*;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.*;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceVehicleReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceApplyReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAreaOptionsRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorWechatIdentityRespDTO;
import com.tce.smart.platform.api.dto.resp.VisitorListRespDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.admittance.*;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.platform.core.vo.*;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.admittance.*;
import com.tce.smart.tool.util.WeChatMsgUtil;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.RegexUtils;
import com.tce.smart.tool.util.ToolUtils;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbSession;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 访客表
 *
 * @author liangyuan
 */

@Slf4j
@Service
public class SmtAdmittanceApplyServiceImpl extends ServiceImpl<SmtAdmittanceApplyMapper, SmtAdmittanceApply> implements SmtAdmittanceApplyService {

	private static final Integer LEGACY_HEFEI_PARK_ID = 20381;

	@Autowired
	private SmtAdmittanceFellowService smtAdmittanceFellowService;
	@Autowired
	private SmtAdmittanceVehicleService smtAdmittanceVehicleService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;
	@Autowired
	private ImageService imageService;
	@Autowired
	private SmtIscDownRecordService smtIscDownRecordService;
	@Autowired
	private SmtIscDeviceTaskService smtIscDeviceTaskService;
	@Autowired
	private SmtVisitJcheLimitService smtVisitJcheLimitService;
	@Autowired
	private SmtImageService smtImageService;
	@Autowired
	private SmtDeviceService smtDeviceService;
	@Autowired
	private SmtSnapVehicleService smtSnapVehicleService;
	@Autowired
	private SmtMsgTemplateService smtMsgTemplateService;
	@Autowired
	private SmtVisitorMapper smtVisitorMapper;
	@Autowired
	private RemoteOaWorkFlowService remoteOaWorkFlowService;
	@Autowired
	private SmtTaskDownRecordService smtTaskDownRecordService;
	@Autowired
	private SmtAdmittanceAreaTypeAuthService smtAdmittanceAreaTypeAuthService;
	@Autowired
	private SmtAdmittanceAreaOptionsService smtAdmittanceAreaOptionsService;
	@Autowired
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;
	@Autowired
	private ApproveListService approveListService;
	@Autowired
	private IOAWorkflowService oaWorkflowService;
	@Autowired
	private SmtVisitorProcessRecordService smtVisitorProcessRecordService;
	@Value("${spring.visitor.put-offset-hour:2}")
	private Integer putOffsetHour;
	@Value("${spring.admittance.sms-url}")
	private String codeUrl;
	@Value("${spring.visitor.overtime-offset-hour:0}")
	private Integer overtimeOffsetHour;
	@Value("${spring.visitor.arrived-offset-hour:0}")
	private Integer arrivedOffsetHour;
	@Value("${spring.visitor.arrived-send:true}")
	private Boolean arrivedSend;
	@Value("${spring.admittance.pretime}")
	private Integer pretime;
	@Value("${spring.admittance.remote-url}")
	private String remoteUrl;
	@Value("${spring.admittance.save-path}")
	private String savePath;
	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;


	/**
	 * 入厂申请去预约添加
	 *
	 * @throws ParseException
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public SmtAdmittanceApply saveAdmittanceApply(SaveAdmittanceApplyReqDTO saveSmtVisitor) {
		visitorEqualCheck(saveSmtVisitor);
		//添加申请信息
		SmtAdmittanceApply apply = this.saveApply(saveSmtVisitor);

		//添加车辆信息
		smtAdmittanceVehicleService.saveVehicle(saveSmtVisitor.getVehicleList(), apply.getId());
		//添加人员信息
		smtAdmittanceFellowService.saveFellow(saveSmtVisitor.getFellowList(), apply.getId());
		//发送OA审批申请
		String processId;
		//发送OA申请
		try {
			processId = this.sendOaApproval(saveSmtVisitor);
			if (StrUtil.isEmpty(processId) && Long.parseLong(processId) < 0) {
				throw new SmartException("发起OA审批流程失败");
			}
		} catch (Exception e) {
			log.error("发起OA审批流程异常{}", e.getMessage());
			throw new SmartException("发起OA审批流程异常");
		}
		//保存oa审批ID
		apply.setProcessId(processId);
		this.updateById(apply);
		return apply;
	}


	/**
	 * 货车预约预约添加
	 *
	 * @throws ParseException
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public SmtAdmittanceApply saveAdmittanceCarApply(SaveAdmittanceCarApplyReqDTO saveSmtVisitor) {
		saveSmtVisitor.setParkId(xcParkId);
		//添加申请信息
		SmtAdmittanceApply apply = this.saveCarApply(saveSmtVisitor);
		//添加车辆信息
		smtAdmittanceVehicleService.saveVehicle(saveSmtVisitor.getVehicleList(), apply.getId());
		//发送OA审批申请
		String processId;
		//发送OA申请
		try {
			processId = this.sendOaCarApproval(saveSmtVisitor);
			if (StrUtil.isEmpty(processId) && Long.parseLong(processId) < 0) {
				throw new SmartException("发起OA审批流程失败");
			}
		} catch (Exception e) {
			log.error("发起OA审批流程异常{}", e.getMessage());
			throw new SmartException("发起OA审批流程异常");
		}
		//保存oa审批ID
		apply.setProcessId(processId);
		this.updateById(apply);
		return apply;
	}

	@Override
	public Boolean visitorEqualCheck(SaveAdmittanceApplyReqDTO saveSmtVisitor) {
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(saveSmtVisitor.getReceptionistBadge());
		if (Objects.isNull(staff)) {
			throw new SmartException("被访人不存在");
		}
		String staffCert = staff.getCertno();
		saveSmtVisitor.getFellowList().forEach(fellow -> {
			if (StrUtil.isNotEmpty(staffCert) && staffCert.equals(fellow.getCertNo())) {
				throw new SmartException("访客" + fellow.getFellowName() + "与被访人重复");
			}
			if (Integer.valueOf(1).equals(fellow.getIsMain())) {
				if (StrUtil.isEmpty(fellow.getCertNo())) {
					throw new SmartException("访客" + fellow.getFellowName() + "证件号码不能为空");
				}
				int count = baseMapper.countActiveMainFellowOverlapByCertNo(fellow.getCertNo(),
						saveSmtVisitor.getStartTime(), saveSmtVisitor.getEndTime());
				if (count > 0) {
					throw new SmartException(fellow.getFellowName() + "此时间段[" + saveSmtVisitor.getStartTime() + "~" + saveSmtVisitor.getEndTime() + "]已有预约，不能重复申请");
				}
			}
		});
		return Boolean.TRUE;
	}

	@Override
	public VisitorWechatIdentityRespDTO getOpenId(String code) {
		String authInfo = ToolUtils.getBadge(code);
		JSONObject authObj = JSONUtil.parseObj(authInfo);
		String openId = authObj.getStr("openId");
		String unionId = authObj.getStr("unionId");
		if (!StringUtils.hasText(unionId)) {
			unionId = authObj.getStr("unionid");
		}
		log.info("openId获取完成");
		if (!StringUtils.hasText(openId)) {
			throw new SmartException("您还未关注公众号,请先关注");
		}
		VisitorWechatIdentityRespDTO response = new VisitorWechatIdentityRespDTO();
		response.setOpenId(openId);
		response.setUnionId(unionId);
		return response;
	}

	/**
	 * 保存入厂申请表
	 *
	 * @param saveSmtVisitor
	 * @return
	 */
	private SmtAdmittanceApply saveApply(SaveAdmittanceApplyReqDTO saveSmtVisitor) {
		//保存预约信息
		SmtAdmittanceApply smtVisitor = BeanUtils.transform(SmtAdmittanceApply.class, saveSmtVisitor);
		smtVisitor.setApplyType(AdmittanceTypeEnum.PERSON.getCode());
		//检查字段
		ExceptionTypeEnum exceptionType = visitorCheck(smtVisitor);
		if (exceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)) {
			exceptionType = validateApplyAreaType(saveSmtVisitor.getParkId(), saveSmtVisitor.getPermitFactoryType(), saveSmtVisitor.getAreaType());
		}
		if (!exceptionType.equals(ExceptionTypeEnum.CHECK_SUCCESS)) {
			throw new TCEException(exceptionType);
		}
		smtVisitor.setAreaType(StrUtil.join(SymbolConstants.COMMA, saveSmtVisitor.getAreaType()));
		smtVisitor.setIsVehicle(SmtVisitorEnum.NOT_VEHICLE.getType());
		if (CollUtil.isNotEmpty(saveSmtVisitor.getVehicleList())) {
			smtVisitor.setIsVehicle(SmtVisitorEnum.IS_VEHICLE.getType());
		}
		smtVisitor.setStatus(VisitorStatusEnum.Status_2.getCode());
		//添加一个访客时默认没有给访客发送预定多少分钟是否提醒的短信
		smtVisitor.setIsSend(SmtVisitorEnum.NOT_IS_SEND.getType());
		smtVisitor.setDeviceStatus(DeviceDownStatusEnum.WAIT.getCode());
		smtVisitor.setCreateTime(LocalDateTime.now());
		smtVisitor.setRemark(saveSmtVisitor.getRemark());
		this.save(smtVisitor);
		return smtVisitor;
	}

	/**
	 * 保存货车预约申请表
	 *
	 * @param saveSmtVisitor
	 * @return
	 */
	private SmtAdmittanceApply saveCarApply(SaveAdmittanceCarApplyReqDTO saveSmtVisitor) {
		//保存预约信息
		SmtAdmittanceApply smtVisitor = BeanUtils.transform(SmtAdmittanceApply.class, saveSmtVisitor);
		smtVisitor.setApplyType(AdmittanceTypeEnum.CAR.getCode());
		smtVisitor.setIsVehicle(SmtVisitorEnum.IS_VEHICLE.getType());
		smtVisitor.setStatus(VisitorStatusEnum.Status_2.getCode());
		//添加一个访客时默认没有给访客发送预定多少分钟是否提醒的短信
		smtVisitor.setDeviceStatus(DeviceDownStatusEnum.WAIT.getCode());
		smtVisitor.setIsSend(SmtVisitorEnum.NOT_IS_SEND.getType());
		smtVisitor.setEndTime(saveSmtVisitor.getStartTime());
		smtVisitor.setCreateTime(LocalDateTime.now());
		smtVisitor.setRemark(saveSmtVisitor.getRemark());
		this.save(smtVisitor);
		return smtVisitor;
	}

	/**
	 * 发送oa入厂申请申请
	 *
	 * @return processId
	 */
	private String sendOaApproval(SaveAdmittanceApplyReqDTO apply) {
		//被访人人信息
		SmtStaff applyStaff = smtStaffService.getSimpleSttaffByBadge(apply.getReceptionistBadge());
		if (Objects.isNull(applyStaff)) {
			throw new SmartException("员工为空");
		}
		SendEntryFactoryApplyReqDTO sendApplyReq = new SendEntryFactoryApplyReqDTO();
		//构造主表
		String zero = OneOrZeroEnum.ZERO.getCode().toString();
		String nullStr = "";
		EntryFactoryApplyMainReqDTO main = EntryFactoryApplyMainReqDTO.builder()
				.lfdw(apply.getCompany())
				.lcbh(nullStr).xdwp(apply.getThing().toString())
				//来访事由 默认短期来访 ：1
				.lflb(OneOrZeroEnum.ONE.getCode().toString())
				.lfsj(DateUtils.format(apply.getStartTime()))
				.sqsj(DateUtils.format(LocalDateTime.now()))
				.lfsy(apply.getCause().toString())
				.lfzl(apply.getPersonType().toString())
				.xdwpnew(AdmittanceCarryItemsEnum.desc(apply.getThing()))
				.sqjrqy(apply.getPermitFactoryType())
				.sfpz(OneOrZeroEnum.ONE.getCode().toString())
				.sqbm(applyStaff.getDepId())
				.dqlf(apply.getPermitOldArea()).cgjt(apply.getPermitArea())
				.sqjrqy1(apply.getPermitFactoryType())
				.jdba(nullStr).rczt(nullStr).sfjrcj(nullStr).cqlf(nullStr).cltxz(nullStr).plfj(nullStr).sqjrqytxt(nullStr)
				.sqjrqynew(apply.getPermitFactoryType()).dqlf(nullStr).cgjt(nullStr)
				.qy(apply.getPermitFactoryType()).aaa(apply.getPermitArea()).bbb(apply.getPermitOldArea())
				.a(zero).b(zero).c(zero).d(zero).e(zero).f(zero).g(zero).h(zero).i(zero).gg(zero).k(zero).l(zero).m(zero)
				.sqr(applyStaff.getBadge()).build();
		main.setBadge(applyStaff.getBadge());
		main.setName(applyStaff.getName());
		main.setCompid(applyStaff.getCompId());
		main.setDepid(applyStaff.getDepId());
		main.setJobid(applyStaff.getJobId());
		//设置访问区域选框
		this.setArea(main, apply.getAreaType());
		sendApplyReq.setEntryFactoryApplyMainReqDTO(main);
		//构造随行人员
		List<EntryFactoryApplyLongDetailReqDTO> entryPersonList = new ArrayList<>();
		apply.getFellowList().forEach(fellow -> {
			String xb = "";
			String zjhm = "";
			if (StrUtil.isNotEmpty(fellow.getCertNo())) {
				xb = ToolUtils.getGenderByIdCard(fellow.getCertNo()).getCode().toString();
				zjhm = fellow.getCertNo();
			}
			EntryFactoryApplyLongDetailReqDTO entryPerson = EntryFactoryApplyLongDetailReqDTO.builder()
					.huji(fellow.getNativePlace()).jrsjd(DateUtils.convert("HH:mm", apply.getStartTime()))
					.xb(xb).xm(fellow.getFellowName()).zjhm(zjhm).zjlx("0").lfzbl("1")
					.zjfj(imageService.buildImageUrl(apply.getParkId(), fellow.getFellowPhotoId()))
					.jrjsrq(DateUtils.format(apply.getEndTime())).jrjssjd(DateUtils.convert("HH:mm", apply.getEndTime()))
					.jrksrq(DateUtils.format(apply.getStartTime())).jrkssjd(DateUtils.convert("HH:mm", apply.getStartTime())).build();
			entryPersonList.add(entryPerson);
		});
		if (CollUtil.isEmpty(entryPersonList)) {
			throw new SmartException("来访人员为空");
		}
		sendApplyReq.setEntryFactoryApplyLongDetailReqDTOs(entryPersonList);
		//构造车辆信息
		if (CollUtil.isNotEmpty(apply.getVehicleList())) {
			List<EntryFactoryApplyCarDetailReqDTO> carList = new ArrayList<>();
			List<AdmittanceVehicleReqDTO> vehicles = apply.getVehicleList();
			vehicles.forEach(vehicle -> {
				EntryFactoryApplyCarDetailReqDTO car = EntryFactoryApplyCarDetailReqDTO.builder()
						.cllx(nullStr).cph(vehicle.getPlate()).cx(nullStr)
						.jjllr(nullStr).jszh(nullStr)
						.llfs(nullStr).sjjg(nullStr)
						.sjxm(vehicle.getName())
						.xgzj(imageService.buildImageUrl(apply.getParkId(), vehicle.getCertImg()))
						.ys(nullStr)
						.zjlx(vehicle.getCertType().toString()).build();
				carList.add(car);
			});
			sendApplyReq.setEntryFactoryApplyCarDetailReqDTOs(carList);
		}
		List<EntryFactoryApplyShortDetailReqDTO> entryFactoryApplyLongDetailReqDTOs = new ArrayList<>();
		EntryFactoryApplyShortDetailReqDTO longDetail = EntryFactoryApplyShortDetailReqDTO.builder().xmm(applyStaff.getName())
				.xb("1")
				.jrjsrq(DateUtils.format(apply.getEndTime())).jrjssjd(DateUtils.convert("HH:mm", apply.getEndTime()))
				.jrksrq(DateUtils.format(apply.getStartTime())).jrkssjd(DateUtils.convert("HH:mm", apply.getStartTime())).build();
		entryFactoryApplyLongDetailReqDTOs.add(longDetail);
		sendApplyReq.setEntryFactoryApplyShortDetailReqDTOs(entryFactoryApplyLongDetailReqDTOs);
		//构造内部人员
		Result<String> result = remoteOaWorkFlowService.sendEntryFactoryApply(sendApplyReq);
		if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
			throw new SmartException("入厂申请OA流程提交异常");
		}
		return result.getData();
	}

	private String sendOaCarApproval(SaveAdmittanceCarApplyReqDTO apply) {

		SendEntryFactoryApplyReqDTO sendApplyReq = new SendEntryFactoryApplyReqDTO();
		//构造主表
		String zero = OneOrZeroEnum.ZERO.getCode().toString();
		String nullStr = "";
		EntryFactoryApplyMainReqDTO main = EntryFactoryApplyMainReqDTO.builder()
				.lfdw(apply.getCompany())
				.lcbh(nullStr).xdwp(nullStr)
				//来访事由 默认短期来访 ：1
				.lflb(OneOrZeroEnum.ONE.getCode().toString())
				.lfsj(DateUtils.format(apply.getStartTime()))
				.sqsj(DateUtils.format(LocalDateTime.now()))
				.lfsy(apply.getCause().toString())
				.lfzl(nullStr).xdwpnew(nullStr).sqjrqy(nullStr)
				.sfpz(OneOrZeroEnum.ONE.getCode().toString())
				.sqbm(nullStr).dqlf(nullStr).cgjt(nullStr)
				.sqjrqy1(nullStr).jdba(nullStr).rczt(nullStr)
				.sfjrcj(nullStr).cqlf(nullStr).cltxz(nullStr).plfj(nullStr).sqjrqytxt(nullStr)
				.sqjrqynew(nullStr).dqlf(nullStr).cgjt(nullStr)
				.qy(nullStr).aaa(nullStr).bbb(nullStr)
				.a(zero).b(zero).c(zero).d(zero).e(zero)
				.f(zero).g(zero).h(zero).i(zero).gg(zero).k(zero).l(zero).m(zero)
				.sqr(nullStr).build();
		main.setBadge("ehr01");
		main.setName("ehr01");
		main.setCompid("901");
		main.setDepid("33742");
		main.setJobid("37682");
		sendApplyReq.setEntryFactoryApplyMainReqDTO(main);
		//构造随行人员
		List<EntryFactoryApplyLongDetailReqDTO> entryPersonList = new ArrayList<>();
		EntryFactoryApplyLongDetailReqDTO entryPerson = EntryFactoryApplyLongDetailReqDTO.builder()
				.huji(nullStr).jrsjd(DateUtils.convert("HH:mm", apply.getStartTime()))
				.xb("0").xm(apply.getVisitorName()).zjhm("11111111111111111").zjlx("0").lfzbl("1")
				.zjfj(nullStr)
				.jrjsrq(DateUtils.format(apply.getStartTime())).jrjssjd(DateUtils.convert("HH:mm", apply.getStartTime()))
				.jrksrq(DateUtils.format(apply.getStartTime())).jrkssjd(DateUtils.convert("HH:mm", apply.getStartTime())).build();
		entryPersonList.add(entryPerson);

		if (CollUtil.isEmpty(entryPersonList)) {
			throw new SmartException("来访人员为空");
		}
		sendApplyReq.setEntryFactoryApplyLongDetailReqDTOs(entryPersonList);
		//构造车辆信息
		if (CollUtil.isNotEmpty(apply.getVehicleList())) {
			List<EntryFactoryApplyCarDetailReqDTO> carList = new ArrayList<>();
			List<AdmittanceVehicleReqDTO> vehicles = apply.getVehicleList();
			vehicles.forEach(vehicle -> {
				EntryFactoryApplyCarDetailReqDTO car = EntryFactoryApplyCarDetailReqDTO.builder()
						.cllx(nullStr).cph(vehicle.getPlate()).cx(nullStr)
						.jjllr(nullStr).jszh(nullStr)
						.llfs(apply.getVisitorPhone()).sjjg(nullStr)
						.sjxm(vehicle.getName()).xgzj(nullStr)
						.ys(nullStr).zjlx(nullStr).build();
				carList.add(car);
			});
			sendApplyReq.setEntryFactoryApplyCarDetailReqDTOs(carList);
		}
		List<EntryFactoryApplyShortDetailReqDTO> entryFactoryApplyLongDetailReqDTOs = new ArrayList<>();
		EntryFactoryApplyShortDetailReqDTO longDetail = EntryFactoryApplyShortDetailReqDTO.builder().xmm(apply.getVisitorName())
				.xb("1")
				.jrjsrq(DateUtils.format(apply.getStartTime())).jrjssjd(DateUtils.convert("HH:mm", apply.getStartTime()))
				.jrksrq(DateUtils.format(apply.getStartTime())).jrkssjd(DateUtils.convert("HH:mm", apply.getStartTime())).build();
		entryFactoryApplyLongDetailReqDTOs.add(longDetail);
		sendApplyReq.setEntryFactoryApplyShortDetailReqDTOs(entryFactoryApplyLongDetailReqDTOs);
		//构造内部人员
		Result<String> result = remoteOaWorkFlowService.sendEntryFactoryApply(sendApplyReq);
		if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
			throw new SmartException("入厂申请OA流程提交异常");
		}
		return result.getData();
	}

	/**
	 * 设置OA进入区域选框
	 *
	 * @param main
	 * @param check
	 * @return
	 */
	private EntryFactoryApplyMainReqDTO setArea(EntryFactoryApplyMainReqDTO main, List<Integer> check) {
		if (CollUtil.isEmpty(check)) {
			return main;
		}
		for (Integer id : check) {
			AdmittanceOaAreaEnum areaEnum = AdmittanceOaAreaEnum.getEnum(id);
			if (Objects.isNull(areaEnum)) {
				throw new SmartException("授权区域配置错误");
			}
			switch (areaEnum) {
				case ITEM_0:
					main.setI(SymbolConstants.ONE_STRING);
					break;
				case ITEM_1:
					main.setGg(SymbolConstants.ONE_STRING);
					break;
				case ITEM_2:
					main.setK(SymbolConstants.ONE_STRING);
					break;
				case ITEM_3:
					main.setL(SymbolConstants.ONE_STRING);
					break;
				case ITEM_6:
					main.setM(SymbolConstants.ONE_STRING);
					break;
				case ITEM_7:
					main.setA(SymbolConstants.ONE_STRING);
					break;
				case ITEM_8:
					main.setB(SymbolConstants.ONE_STRING);
					break;
				case ITEM_9:
					main.setC(SymbolConstants.ONE_STRING);
					break;
				case ITEM_10:
					main.setD(SymbolConstants.ONE_STRING);
					break;
				case ITEM_11:
					main.setE(SymbolConstants.ONE_STRING);
					break;
				case ITEM_12:
					main.setF(SymbolConstants.ONE_STRING);
					break;
				case ITEM_13:
					main.setG(SymbolConstants.ONE_STRING);
					break;
				case ITEM_14:
					main.setH(SymbolConstants.ONE_STRING);
					break;
			}
		}
		return main;
	}

	@Override
	public SmtAdmittanceApply getByProcessId(String processId) {
		return this.getOne(Wrappers.<SmtAdmittanceApply>query().lambda().eq(SmtAdmittanceApply::getProcessId, processId));
	}

	@Override
	public void updateStatus(SmtAdmittanceApply apply) {
		//过期审批不下发 只修改状态
		if (LocalDateTime.now().isAfter(apply.getEndTime())) {
			apply.setStatus(VisitorStatusEnum.CAUSE_6.getCode());
			this.updateById(apply);
			return;
		}
		if (AdmittanceTypeEnum.CAR.getCode().equals(apply.getApplyType())) {
			//设置预约成功的验证码
			apply.setSmsCode(RandomUtil.randomNumbers(6));
			apply.setDeviceStatus(DeviceDownStatusEnum.WAIT.getCode());
			this.updateById(apply);
			return;
		}
		//审批通过
		if (VisitorStatusEnum.Status_0.getCode().equals(apply.getStatus())) {
			//设置预约成功的验证码
			apply.setSmsCode(RandomUtil.randomNumbers(6));
			apply.setDeviceStatus(DeviceDownStatusEnum.ALRAEDY.getCode());
			//下发权限
			this.addDeviceTask(apply);
			//上传照片到远程电脑
			this.smbPutPhoto(apply.getId());
		}
		//预约通知
		try {
			this.sendPassMsg(apply);
		} catch (Exception e) {
			log.error("【入厂申请微信消息推送失败】{},{}", e.getMessage(), e.getStackTrace());
		}
		this.updateById(apply);
	}

	private Boolean sendPassMsg(SmtAdmittanceApply apply) {
		// 判断apply的unionId是否为空
		if (StringUtils.isEmpty(apply.getUnionId())) {
			// 记录错误日志
			log.error("【入厂申请微信推送】id:{} 获取unionid失败", apply.getId());
			return Boolean.FALSE;
		}
		// 判断apply的状态是否为Status_0
		if (VisitorStatusEnum.Status_0.getCode().equals(apply.getStatus())) {
			// 替换url中的id
			String url = codeUrl.replace("{id}", String.valueOf(apply.getId()));
			// 根据模板编码获取模板
			SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_ADMITTANCE_10901.getCode());
			// 替换模板内容中的变量
			String msg = template.getTempContent().replace("{被访人姓名}", apply.getReceptionistName())
					.replace("{URL}", url)
					.replace("{访客姓名}", apply.getVisitorName())
					.replace("{预约来访时间}", DateUtils.format(apply.getStartTime()));
			// 发送微信消息
			WeChatMsgUtil.sendMsg(null, msg, apply.getUnionId(), url);
			return Boolean.TRUE;
		}
		// 预约失败 发送推送
		// 根据模板编码获取模板
		SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_ADMITTANCE_10902.getCode());
		// 替换模板内容中的变量
		String msg = template.getTempContent().replace("{被访人姓名}", apply.getReceptionistName());
		// 发送微信消息
		WeChatMsgUtil.sendMsg(null, msg, apply.getUnionId(), null);
		return Boolean.TRUE;
	}


	/**
	 * 添加下发任务
	 *
	 * @param apply
	 */
	private void addDeviceTask(SmtAdmittanceApply apply) {
		String areaTypeId = apply.getAreaType();
		//查询人员设备权限
		List<SmtDeviceAuthorityRelation> personDeviceList = this.getDeviceList(areaTypeId, DeviceTypeEnum.DEVICE_TYPE_1.getCode(), apply.getParkId());
		//查询车辆的的设备权限
		List<SmtDeviceAuthorityRelation> carDeviceList = this.getDeviceList(areaTypeId, DeviceTypeEnum.DEVICE_TYPE_3.getCode(), apply.getParkId());
		//下发闸机,下发道闸
		addAdmittanceDevice(apply, personDeviceList, carDeviceList);
	}

	/**
	 * 获得关联设备
	 *
	 * @param areaTypeId 申请进入区域类型
	 * @param deviceCode 设备类型
	 * @return
	 */
	// 根据区域类型ID、设备编码、停车场ID获取设备列表
	private List<SmtDeviceAuthorityRelation> getDeviceList(String areaTypeId, Integer deviceCode, Integer parkId) {
		// 根据区域类型ID、设备编码、停车场ID获取权限列表
		List<SmtAdmittanceAreaTypeAuth> authList = smtAdmittanceAreaTypeAuthService.getAuthByType(areaTypeId, deviceCode, parkId);
		// 如果权限列表不为空
		if (CollUtil.isNotEmpty(authList)) {
			// 获取权限ID列表
			List<Integer> authIds = authList.stream().distinct().map(SmtAdmittanceAreaTypeAuth::getAuthId).collect(Collectors.toList());
			// 根据权限ID列表获取设备权限关系列表
			return smtDeviceAuthorityRelationService.getRelationByAuthId(authIds);
		}
		// 如果权限列表为空，返回null
		return null;
	}

	/**
	 * 添加入厂申请的闸机数据
	 *
	 * @param apply            入厂申请
	 * @param personDeviceList 人员下发设备
	 * @param carDeviceList    车辆下发设备
	 */
	private void addAdmittanceDevice(SmtAdmittanceApply apply, List<SmtDeviceAuthorityRelation> personDeviceList, List<SmtDeviceAuthorityRelation> carDeviceList) {
		//添加访客设备权限
		if (CollUtil.isNotEmpty(personDeviceList)) {
			List<SmtAdmittanceFellow> fellowList = smtAdmittanceFellowService.getByApplyId(apply.getId());
			fellowList.forEach(f -> {
				addCard(apply, f,
						personDeviceList.stream().map(SmtDeviceAuthorityRelation::getDeviceId).collect(Collectors.toList()));
			});
		}
		//添加车辆设备权限
		if (CollUtil.isNotEmpty(carDeviceList)) {
			List<SmtAdmittanceVehicle> vehicleList = smtAdmittanceVehicleService.getByApplyId(apply.getId());
			if (CollUtil.isEmpty(vehicleList)) {
				return;
			}
			vehicleList.forEach(v -> {
				addCarCard(apply, v,
						carDeviceList.stream().map(SmtDeviceAuthorityRelation::getDeviceId).collect(Collectors.toList()));
			});
		}
	}

	/**
	 * 访客下发闸机
	 *
	 * @param fellow
	 * @param deviceId
	 */
	private void addCard(SmtAdmittanceApply apply, SmtAdmittanceFellow fellow, String deviceId) {
		if (StrUtil.isNotBlank(fellow.getFellowPhotoId())) {
			DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
			deviceTaskVO.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
			deviceTaskVO.setCardNo(fellow.getId().toString());
			deviceTaskVO.setGeneral(fellow.getFellowName());
			deviceTaskVO.setCardType(SmtVisitorEnum.CARD_TYPE_7.getType());
			deviceTaskVO.setImageId(fellow.getFellowPhotoId());
			deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
			deviceTaskVO.setStartTime(DateUtils.toEpochMilli(apply.getStartTime().plusHours(-putOffsetHour)) / 1000);
			deviceTaskVO.setOverTime(DateUtils.toEpochMilli(apply.getEndTime()) / 1000);
			deviceTaskVO.setApplyBadge(fellow.getCertNo());
			deviceTaskVO.setDeviceCode(deviceId);
			smtDeviceTaskService.saveTask(deviceTaskVO);
		}
	}

	/**
	 * 访客下发闸机
	 *
	 * @param fellow
	 * @param deviceId
	 */
	private void addCard(SmtAdmittanceApply apply, SmtAdmittanceFellow fellow, List<String> deviceId) {
		if (StrUtil.isNotBlank(fellow.getFellowPhotoId())) {
			DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
			deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
			deviceTaskVO.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
			deviceTaskVO.setCardNo(fellow.getId().toString());
			deviceTaskVO.setGeneral(fellow.getFellowName());
			deviceTaskVO.setCardType(SmtVisitorEnum.CARD_TYPE_7.getType());
			deviceTaskVO.setImageId(fellow.getFellowPhotoId());
			deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
			deviceTaskVO.setStartTime(DateUtils.toEpochMilli(apply.getStartTime().plusHours(-putOffsetHour)) / 1000);
			deviceTaskVO.setOverTime(DateUtils.toEpochMilli(apply.getEndTime()) / 1000);
			deviceTaskVO.setApplyBadge(fellow.getCertNo());
			deviceId.forEach(id -> {
				deviceTaskVO.setDeviceCode(id);
				smtDeviceTaskService.saveTask(deviceTaskVO);
			});
		}
	}

	/**
	 * 下发道闸
	 *
	 * @param apply
	 * @param vehicle
	 */
	private void addCarCard(SmtAdmittanceApply apply, SmtAdmittanceVehicle vehicle, String deviceId) {
		DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
		deviceTaskVO.setServiceType(DeviceTaskConstants.CAT_ADMITTANCE);
		deviceTaskVO.setCardNo(vehicle.getId().toString());
		deviceTaskVO.setGeneral(vehicle.getPlate());
		deviceTaskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_0.getType());
		deviceTaskVO.setDeviceType(DeviceTaskConstants.CAR);
		deviceTaskVO.setStartTime(DateUtils.toEpochMilli(apply.getStartTime().plusHours(-putOffsetHour)) / 1000);
		deviceTaskVO.setOverTime(DateUtils.toEpochMilli(apply.getEndTime()) / 1000);
		deviceTaskVO.setApplyBadge(apply.getCertNo());
		deviceTaskVO.setDeviceCode(deviceId);
		smtDeviceTaskService.saveTask(deviceTaskVO);
	}

	/**
	 * 下发道闸
	 *
	 * @param apply
	 * @param vehicle
	 */
	private void addCarCard(SmtAdmittanceApply apply, SmtAdmittanceVehicle vehicle, List<String> deviceId) {
		DeviceTaskVO deviceTaskVO = new DeviceTaskVO();
		deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
		deviceTaskVO.setServiceType(DeviceTaskConstants.CAT_ADMITTANCE);
		deviceTaskVO.setCardNo(vehicle.getId().toString());
		deviceTaskVO.setGeneral(vehicle.getPlate());
		deviceTaskVO.setCardType(SmtVisitorEnum.CAR_CARD_TYPE_0.getType());
		deviceTaskVO.setDeviceType(DeviceTaskConstants.CAR);
		deviceTaskVO.setStartTime(DateUtils.toEpochMilli(apply.getStartTime().plusHours(-putOffsetHour)) / 1000);
		deviceTaskVO.setOverTime(DateUtils.toEpochMilli(apply.getEndTime()) / 1000);
		deviceTaskVO.setApplyBadge(apply.getCertNo());
		deviceId.forEach(id -> {
			deviceTaskVO.setDeviceCode(id);
			smtDeviceTaskService.saveTask(deviceTaskVO);
		});
	}


	/**
	 * @Title:访客的正则判断
	 * @Param :visitor
	 */
	private ExceptionTypeEnum visitorCheck(SmtAdmittanceApply visitor) {

		if (!RegexUtils.matchName(visitor.getVisitorName())) {
			return ExceptionTypeEnum.VISITOR_NAME_LENGTH_ERROR;
		}
		if (!RegexUtils.matchPhone(visitor.getVisitorPhone())) {
			return ExceptionTypeEnum.VISITOR_PHONE_ERROR;
		}
		if (visitor.getEndTime().isBefore(visitor.getStartTime())) {
			return ExceptionTypeEnum.VISITOR_ENDTIME_CANT_BEFORE_STARTTIME;
		}
		return ExceptionTypeEnum.CHECK_SUCCESS;
	}

	private ExceptionTypeEnum validateApplyAreaType(Integer parkId, String permitFactoryType, List<Integer> areaType) {
		if (StrUtil.isBlank(permitFactoryType) && CollUtil.isEmpty(areaType)) {
			return LEGACY_HEFEI_PARK_ID.equals(parkId) ? ExceptionTypeEnum.CHECK_SUCCESS : ExceptionTypeEnum.VISITOR_AREA_TYPE_EMPTY;
		}
		if (CollUtil.isEmpty(areaType)) {
			return ExceptionTypeEnum.VISITOR_AREA_TYPE_EMPTY;
		}
		if (StrUtil.isBlank(permitFactoryType)) {
			return ExceptionTypeEnum.VISITOR_AREA_TYPE_ERROR;
		}
		AdmittanceAreaOptionsRespDTO.FactoryOption factory = getApplyFactoryOption(parkId, permitFactoryType);
		if (Objects.isNull(factory)) {
			return ExceptionTypeEnum.VISITOR_AREA_TYPE_ERROR;
		}
		Set<Integer> validAreaCodes = getAreaCodes(factory);
		if (CollUtil.isEmpty(validAreaCodes)) {
			return ExceptionTypeEnum.VISITOR_AREA_TYPE_ERROR;
		}
		for (Integer areaCode : areaType) {
			if (Objects.isNull(areaCode) || !validAreaCodes.contains(areaCode)) {
				return ExceptionTypeEnum.VISITOR_AREA_TYPE_ERROR;
			}
		}
		return ExceptionTypeEnum.CHECK_SUCCESS;
	}

	private AdmittanceAreaOptionsRespDTO.FactoryOption getApplyFactoryOption(Integer parkId, String permitFactoryType) {
		if (Objects.isNull(smtAdmittanceAreaOptionsService)) {
			return null;
		}
		AdmittanceAreaOptionsRespDTO areaOptions = smtAdmittanceAreaOptionsService.getAreaOptions(parkId);
		if (Objects.isNull(areaOptions) || CollUtil.isEmpty(areaOptions.getFactories())) {
			return null;
		}
		return areaOptions.getFactories().stream()
				.filter(factory -> permitFactoryType.equals(factory.getFactoryType()))
				.findFirst()
				.orElse(null);
	}

	private Set<Integer> getAreaCodes(AdmittanceAreaOptionsRespDTO.FactoryOption factory) {
		if (Objects.isNull(factory) || CollUtil.isEmpty(factory.getAreas())) {
			return Collections.emptySet();
		}
		return factory.getAreas().stream()
				.map(AdmittanceAreaOptionsRespDTO.AreaOption::getAreaCode)
				.filter(Objects::nonNull)
				.filter(areaCode -> Objects.nonNull(AdmittanceOaAreaEnum.getEnum(areaCode)))
				.collect(Collectors.toSet());
	}


	/**
	 * 入厂申请车辆抓拍记录访客车辆信息补充
	 *
	 * @param entity 抓拍车辆信息
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void admittanceSnapVehicleHandle(AddSnapVehicleDTO entity) {
		if (!StringUtils.isEmpty(entity.getCardNo())) {
			SmtAdmittanceVehicle vehicle = smtAdmittanceVehicleService.getById(Long.parseLong(entity.getCardNo()));
			if (Objects.isNull(vehicle)) {
				return;
			}
			SmtAdmittanceApply selectOne = this.getById(vehicle.getVisitorId());
			if (Objects.nonNull(selectOne)) {
				//首次进门，并是未到达的状态下发短信
				if (entity.getEventType().equals(VehicleEventTypEnum.IN.getCode()) && !selectOne.getStatus().equals(SmtVisitorEnum.COME_STATUS.getType())) {
					selectOne.setStatus(SmtVisitorEnum.COME_STATUS.getType());
					this.updateById(selectOne);
					//到访信息发送
					SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_ADMITTANCE_10904.getCode());
					SmtDevice selectDeviceById = smtDeviceService.getById(entity.getDeviceId());
					String msg = template.getTempContent().replace("{来访单位}", selectOne.getCompany())
							.replace("{实际来访时间}", DateUtils.convert(LocalDateTime.now()))
							.replace("{访客姓名}", selectOne.getVisitorName())
							.replace("{刷脸的门}", selectDeviceById.getDeviceName());
					WeChatMsgUtil.sendMsg(selectOne.getReceptionistBadge(), msg, null, null);
				}
				//访客车辆首次出门下发短信,除驻场人员外
				if (entity.getEventType().equals(VehicleEventTypEnum.OUT.getCode())) {
					this.updateById(selectOne);
					//离开短信发送
					int snapCount = smtSnapVehicleService.count(Wrappers.<SmtSnapVehicle>query().lambda()
							.eq(SmtSnapVehicle::getDriverId, selectOne.getId())
							.eq(SmtSnapVehicle::getVehicleAscription, SnapVehicleConstants.VISITOR_VEHICLE)
							.eq(SmtSnapVehicle::getEventType, EventTypeEnum.EVENT_TYPE_2.getCode()));
					selectOne.setStatus(VisitorStatusEnum.CAUSE_5.getCode());
					//判断访客是否首次出门
					if (snapCount == 0) {
						SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_ADMITTANCE_10905.getCode());
						SmtDevice selectDeviceById = smtDeviceService.getById(entity.getDeviceId());
						String msg = template.getTempContent().replace("{来访单位}", selectOne.getCompany())
								.replace("{访客姓名}", selectOne.getVisitorName())
								.replace("{实际离开时间}", DateUtils.convert(LocalDateTime.now()))
								.replace("{被访人姓名}", selectOne.getReceptionistName())
								.replace("{刷脸的门}", selectDeviceById.getDeviceName());
						WeChatMsgUtil.sendMsg(selectOne.getReceptionistBadge(), msg, null, null);
					}
					//TODO 【出门后权限立刻删除注释，为预约结束当天00:00删除】
//					//访客出门后删除车辆，删除访客，删除随行人员
//					//查询人员设备权限
//					List<SmtDeviceAuthorityRelation> personDeviceList = this.getDeviceList(selectOne.getId(), DeviceTypeEnum.DEVICE_TYPE_1.getCode(), selectOne.getParkId());
//					//查询车辆的的设备权限
//					List<SmtDeviceAuthorityRelation> carDeviceList = this.getDeviceList(selectOne.getId(), DeviceTypeEnum.DEVICE_TYPE_3.getCode(), selectOne.getParkId());
//					//查询访客车辆的的设备权限
//					delPersonCardTask(selectOne, personDeviceList);
//					delCarCardTask(selectOne, carDeviceList);
				}

				/*if(!selectOne.getStatementStatus().equals(SmtVisitorEnum.COME_STATUS.getType())) {
					selectOne.setStatementStatus(SmtVisitorEnum.COME_STATUS.getType());
					this.smtVisitorMapper.updateById(selectOne);
				}*/
				entity.setVehicleAscription(VehicleBelongTypeEnum.VISITOR_VEHICLE.getCode());
				entity.setDriverId(vehicle.getId());
				entity.setDriverName(vehicle.getName());
				entity.setDriverPhone(selectOne.getVisitorPhone());
				entity.setDriverType(VehicleBelongTypeEnum.VISITOR_VEHICLE.getCode());
			}
		}
	}

	/**
	 * 后台查询访客的详细信息
	 */
	@Override
	public SmtAdmittanceApply searchDetailById(Long id) {
		return this.getById(id);
	}

	/**
	 * 查询访客的分页信息
	 */
	@Override
	public IPage<SearchSmtVisitorVO> getSmtVisitorPage(Page page, SearchSmtVisitorDTO searchSmtVisitorDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		IPage<SearchSmtVisitorVO> smtVisitorPage = this.baseMapper.getSmtVisitorPage(page, searchSmtVisitorDTO, parkIdList);
		if (CollUtil.isNotEmpty(smtVisitorPage.getRecords())) {
			smtVisitorPage.getRecords().forEach(record -> {
				record.setDeviceStatusDesc(DeviceDownStatusEnum.desc(record.getDeviceStatus()));
				if (Objects.nonNull(record.getApplyType())) {
					record.setApplyTypeDesc(AdmittanceTypeEnum.desc(record.getApplyType()));
				}
				if (AdmittanceTypeEnum.PERSON.getCode().equals(record.getApplyType())) {
					//根据图片的id获取图片的访问url
					if (ObjectUtil.isNotNull(record.getVisitorPhotoId())) {
						record.setVisitorPhoto(imageService.buildImageUrl(record.getVisitorPhotoId()));
					}
					//查询访客人脸是否存在下发成功记录
					List<SmtAdmittanceFellow> fellows = smtAdmittanceFellowService.getByApplyId(Long.parseLong(record.getId()));
					List<String> fellowIds = fellows.stream().map(fellow -> {
						return fellow.getId().toString();
					}).collect(Collectors.toList());
						int cardCount = smtTaskDownRecordService.count(new LambdaQueryWrapper<SmtTaskDownRecord>()
								.in(SmtTaskDownRecord::getCardNo, fellowIds)
								.eq(SmtTaskDownRecord::getDeviceType, DeviceTaskConstants.CARD)
								.eq(SmtTaskDownRecord::getServiceType, DeviceTaskConstants.CARD_ADMITTANCE));
						if (cardCount == 0) {
							//查询ISC任务是否存在成功记录
							cardCount = smtIscDownRecordService.count(new LambdaQueryWrapper<SmtIscDownRecord>()
									.in(SmtIscDownRecord::getCardNo, fellowIds)
									.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CARD)
									.eq(SmtIscDownRecord::getServiceType, DeviceTaskConstants.CARD_ADMITTANCE));
						}
					//查询访客车辆是否存在下发成功记录
					int carCount = 0;
					if (SmtVisitorEnum.IS_VEHICLE.getType().equals(record.getIsVehicle())) {
						List<SmtAdmittanceVehicle> vehicles = smtAdmittanceVehicleService.getByApplyId(Long.parseLong(record.getId()));
						List<String> vehicleIds = vehicles.stream().map(fellow -> {
							return fellow.getId().toString();
						}).collect(Collectors.toList());
						List<String> vehiclePlats = vehicles.stream().map(SmtAdmittanceVehicle::getPlate).collect(Collectors.toList());
						//存在车牌
							carCount = smtTaskDownRecordService.count(new LambdaQueryWrapper<SmtTaskDownRecord>()
									.in(SmtTaskDownRecord::getCardNo, vehicleIds)
									.in(SmtTaskDownRecord::getGeneral, vehiclePlats)
									.eq(SmtTaskDownRecord::getDeviceType, DeviceTaskConstants.CAR)
									.eq(SmtTaskDownRecord::getServiceType, DeviceTaskConstants.CAT_ADMITTANCE)
							);
						if (carCount == 0) {
							carCount = smtIscDownRecordService.count(new LambdaQueryWrapper<SmtIscDownRecord>()
										.in(SmtIscDownRecord::getCardNo, vehicleIds)
										.in(SmtIscDownRecord::getGeneral, vehiclePlats)
										.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CAR)
										.eq(SmtIscDownRecord::getServiceType, DeviceTaskConstants.CAT_ADMITTANCE));
							}
						}
					if (Objects.nonNull(record.getPersonType())) {
						record.setPersonTypeDesc(AdmittancePersonTypeEnum.desc(record.getPersonType()));
					}
					//此处的权限状态 只用于展示 因此没有使用状态枚举
					record.setHasAuth(OneOrZeroEnum.ZERO.getCode());
					if (cardCount > 0 || carCount > 0) {
						record.setHasAuth(OneOrZeroEnum.ONE.getCode());
					}
				}
			});
		}
		return smtVisitorPage;
	}

	/**
	 * 查询被访人是否存在
	 */
	@Override
	public SmtAdmittanceApply searchReceptionist(SmtAdmittanceApply apply) {
		SmtVisitor visitor = new SmtVisitor();
		visitor.setParkId(apply.getParkId());
		visitor.setReceptionistName(apply.getReceptionistName());
		visitor.setReceptionistPhone(apply.getReceptionistPhone());
		List<SmtStaff> smtStaffList = this.smtVisitorMapper.searchReceptionist(visitor);
		if (CollectionUtils.isEmpty(smtStaffList)) {
			smtStaffList = this.smtVisitorMapper.searchReceptionistForTemp(visitor);
		}
		if (smtStaffList.size() <= 0) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_RECEPTIONIST_ERROR);
		}
		List<SmtVisitJcheLimit> jcheLimit = smtVisitJcheLimitService.listByJcheId(apply.getParkId(),
				smtStaffList.get(0).getJcheId(), ConfigBusinessEnum.ADMITTANCE.getCode());
		if (CollUtil.isNotEmpty(jcheLimit)) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_RECEPTIONIST_JCHE_ERROR);
		}
		apply.setReceptionistBadge(smtStaffList.get(0).getBadge());
		return apply;
	}

	@Override
	public IPage<VisitorListRespDTO> getVisitRecord(Page page, String visitorPhone) {
		IPage iPage = this.page(page, new LambdaQueryWrapper<SmtAdmittanceApply>()
				.eq(SmtAdmittanceApply::getVisitorPhone, visitorPhone)
				.orderByDesc(SmtAdmittanceApply::getCreateTime));
		List<VisitorListRespDTO> visitorListRespDTOList = new ArrayList<>();
		iPage.getRecords().forEach(record -> {
			SmtAdmittanceApply apply = (SmtAdmittanceApply) record;
			VisitorListRespDTO visitorListRespDTO = new VisitorListRespDTO();
			BeanUtils.copyProperties(apply, visitorListRespDTO);
			if (AdmittanceTypeEnum.PERSON.getCode().equals(apply.getApplyType())) {
				visitorListRespDTO.setCauseDes(AdmittanceCauseEnum.desc(apply.getCause()));
			}
			if (AdmittanceTypeEnum.CAR.getCode().equals(apply.getApplyType())) {
				visitorListRespDTO.setCauseDes(AdmittanceCarCauseEnum.desc(apply.getCause()));
			}
			visitorListRespDTO.setVisitorImg(imageService.buildImageUrl(apply.getVisitorPhotoId()));
			visitorListRespDTOList.add(visitorListRespDTO);
		});
		return iPage;
	}

	@Override
	public Boolean repeatVisitorDeviceAuth(Long id) {
		//查询访客记录
		SmtAdmittanceApply apply = this.getById(id);
		if (Objects.isNull(apply)) {
			throw new TCEException("入厂申请预约记录不存在");
		}

		if (apply.getEndTime().isBefore(LocalDateTime.now())) {
			throw new TCEException("预约时间已到期");
		}
		Long applyId = apply.getId();
		//查询人员设备权限
		List<SmtDeviceAuthorityRelation> visitorDeviceList = this.getDeviceList(apply.getAreaType(),
				DeviceTypeEnum.DEVICE_TYPE_1.getCode(), apply.getParkId());
		List<SmtAdmittanceFellow> fellows = smtAdmittanceFellowService.getByApplyId(applyId);
		List<SmtAdmittanceVehicle> vehicles = smtAdmittanceVehicleService.getByApplyId(applyId);
		if (CollUtil.isNotEmpty(visitorDeviceList)) {
			for (SmtDeviceAuthorityRelation relation : visitorDeviceList) {
				fellows.forEach(fellow -> {
					//检查该设备是否已经下发成功过或者正在下发
					Boolean record = checkDeviceTaskRecord(fellow.getId().toString(), relation.getDeviceId());
					if (!record) {
						addCard(apply, fellow, relation.getDeviceId());
					}
				});
			}
		}
		if (CollUtil.isNotEmpty(vehicles)) {
			//查询访客车辆的的设备权限
			//查询车辆的的设备权限
			List<SmtDeviceAuthorityRelation> carDeviceList = this.getDeviceList(apply.getAreaType(), DeviceTypeEnum.DEVICE_TYPE_3.getCode(), apply.getParkId());
			if (CollUtil.isNotEmpty(carDeviceList)) {
				for (SmtDeviceAuthorityRelation relation : carDeviceList) {
					vehicles.forEach(vehicle -> {
						Boolean record = checkDeviceTaskRecord(vehicle.getId().toString(), relation.getDeviceId());
						if (!record) {
							//生成新的下发任务
							addCarCard(apply, vehicle, relation.getDeviceId());
						}
					});
				}
			}
		}
		return true;
	}

	/**
	 * 根据访客邀请码查询访客信息
	 * @param code 访客邀请码
	 * @return 访客信息
	 */
	@Override
	public SmtAdmittanceApply searchVisitorByCode(String code) {
		try {
			// 根据邀请码查询访客信息
			// SmtAdmittanceApply smtApply = this.baseMapper.selectOne(Wrappers.<SmtAdmittanceApply>query().lambda().eq(SmtAdmittanceApply::getSmsCode, code));
			List<SmtAdmittanceApply> smtApplyList = this.baseMapper.selectList(Wrappers.<SmtAdmittanceApply>query().lambda()
					.eq(SmtAdmittanceApply::getSmsCode, code)
					.orderByDesc(SmtAdmittanceApply::getCreateTime));

			SmtAdmittanceApply smtApply = smtApplyList.get(0);
			// 如果查询结果为空，抛出异常
			if (Objects.isNull(smtApply)) {
				throw new TCEException(ExceptionTypeEnum.VISITOR_CODE_ERROR);
			}
			// 返回查询结果
			return smtApply;
		} catch (Exception e) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_CODE_ERROR);
		}
	}

	/**
	 * 检查是否下发成功或正在下发
	 *
	 * @param cardNo
	 * @param deviceId
	 * @return
	 */
	private Boolean checkDeviceTaskRecord(String cardNo, String deviceId) {
		// 检查该设备是否已经下发成功过
/*		int device_count = smtTaskDownRecordService.count(new LambdaQueryWrapper<SmtTaskDownRecord>()
				.eq(SmtTaskDownRecord::getCardNo, cardNo)
				.eq(SmtTaskDownRecord::getDeviceCode, deviceId)
		);

		// 检查该设备在ISC平台是否已经下发成功过
		int isc_count = smtIscDownRecordService.count(new LambdaQueryWrapper<SmtIscDownRecord>()
				.eq(SmtIscDownRecord::getCardNo, cardNo)
				.eq(SmtIscDownRecord::getDeviceCode, deviceId));

		if (device_count > 0 || isc_count > 0)
			return true;*/

		// 查询是否存在正在下发中的记录
		int device_task_count = smtDeviceTaskService.count(new LambdaQueryWrapper<SmtDeviceTask>()
				.eq(SmtDeviceTask::getCardNo, cardNo)
				.eq(SmtDeviceTask::getDeviceCode, deviceId)
				.eq(SmtDeviceTask::getAction, DeviceTaskActionEnum.DOWN.getCode())
				//.eq(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode())
				.and(wrapper -> wrapper.ne(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.FAIL.getCode())
						.ne(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.CANCEL.getCode())
						.ne(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.SUCCESS.getCode()))
		);

		if (device_task_count > 0)
			return true;

		int isc_task_count = smtIscDeviceTaskService.count(new LambdaQueryWrapper<SmtIscDeviceTask>()
				.eq(SmtIscDeviceTask::getCardNo, cardNo)
				.eq(SmtIscDeviceTask::getDeviceCode, deviceId)
				.eq(SmtIscDeviceTask::getAction, DeviceTaskActionEnum.DOWN.getCode())
				//.eq(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.INIT.getCode()));
				.and(wrapper -> wrapper.ne(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.FAIL.getCode())
						.ne(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.CANCEL.getCode())
						.ne(SmtIscDeviceTask::getStatus, DeviceTaskStatusEnum.SUCCESS.getCode()))
		);

		// 存在正在下发中的任务
        return isc_task_count > 0;
    }


	/**
	 * 卡片删除任务
	 *
	 * @param apply
	 * @param
	 */
	private void delTask(SmtAdmittanceApply apply) {
		// 定义设备任务VO对象
		DeviceTaskVO deviceTaskVO;
		// 获取访客设备列表
		List<SmtDeviceAuthorityRelation> visitorDeviceList = this.getDeviceList(apply.getAreaType(),
				DeviceTypeEnum.DEVICE_TYPE_1.getCode(), apply.getParkId());
		// 获取访客同行人列表
		List<SmtAdmittanceFellow> fellows = smtAdmittanceFellowService.getByApplyId(apply.getId());
		// 如果访客设备列表不为空
		if (CollUtil.isNotEmpty(visitorDeviceList)) {
			// 遍历访客同行人列表
				for (SmtAdmittanceFellow fellow : fellows) {
					// 遍历访客设备列表
					for (SmtDeviceAuthorityRelation auth : visitorDeviceList) {
						// 查询是否已生成删除任务
						SmtDeviceTask deviceTask = findReusableDeviceDeleteTask(fellow.getId().toString(),
								auth.getDeviceId(), DeviceTaskConstants.CARD,
								DeviceTaskServiceTypeEnum.CARD_ADMITTANCE.getCode());
						SmtIscDeviceTask deviceIscTask = findReusableIscDeleteTask(fellow.getId().toString(),
								auth.getDeviceId(), DeviceTaskConstants.CARD,
								DeviceTaskServiceTypeEnum.CARD_ADMITTANCE.getCode());
						// 如果已生成删除任务
						if (Objects.nonNull(deviceTask)) {
							// 访客预约已存在待处理的删除任务 访客出门后 把删除时间调整为当前
							moveDeviceDeleteTaskToNow(deviceTask);
						} else if (Objects.nonNull(deviceIscTask)) {
							// 访客预约已存在待处理的删除任务 访客出门后 把删除时间调整为当前
							moveIscDeleteTaskToNow(deviceIscTask);
						} else {
							// 生成新的删除任务
							deviceTaskVO = new DeviceTaskVO();
						deviceTaskVO.setAction(DeviceTaskConstants.DEL);
						deviceTaskVO.setCardNo(fellow.getId().toString());
						deviceTaskVO.setDeviceCode(auth.getDeviceId());
							deviceTaskVO.setStartTime(DateUtils.currentSeconds());
							deviceTaskVO.setOverTime(DateUtils.currentSeconds());
							deviceTaskVO.setImageId(apply.getVisitorPhotoId());
							deviceTaskVO.setGeneral(apply.getVisitorName());
							deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
							deviceTaskVO.setServiceType(DeviceTaskServiceTypeEnum.CARD_ADMITTANCE.getCode());
							deviceTaskVO.setApplyBadge(fellow.getCertNo());
							smtDeviceTaskService.saveTask(deviceTaskVO);
						}
					}
				}
		}
		// 如果访客预约包含车辆
		if (OneOrZeroEnum.ONE.getCode().equals(apply.getIsVehicle())) {
			// 获取车辆列表
			List<SmtAdmittanceVehicle> vehicles = smtAdmittanceVehicleService.getByApplyId(apply.getId());
			// 获取车辆设备列表
			List<SmtDeviceAuthorityRelation> carDeviceList = this.getDeviceList(apply.getAreaType(), DeviceTypeEnum.DEVICE_TYPE_3.getCode(), apply.getParkId());
			// 如果车辆设备列表为空
			if (CollUtil.isEmpty(carDeviceList))
				return;

				// 遍历车辆列表
				for (SmtAdmittanceVehicle vehicle : vehicles) {
					// 遍历车辆设备列表
					for (SmtDeviceAuthorityRelation auth : carDeviceList) {
						String vehicleId = vehicle.getId().toString();
						// 查询是否已生成删除任务
						SmtDeviceTask deviceTask = findReusableDeviceDeleteTask(vehicleId,
								auth.getDeviceId(), DeviceTaskConstants.CAR,
								DeviceTaskServiceTypeEnum.CAR_ADMITTANCE.getCode());
						SmtIscDeviceTask deviceIscTask = findReusableIscDeleteTask(vehicleId,
								auth.getDeviceId(), DeviceTaskConstants.CAR,
								DeviceTaskServiceTypeEnum.CAR_ADMITTANCE.getCode());
						// 如果已生成删除任务
						if (null != deviceTask) {
							// 访客预约已存在待处理的删除任务 访客出门后 把删除时间调整为当前
							moveDeviceDeleteTaskToNow(deviceTask);
						} else if (Objects.nonNull(deviceIscTask)) {
							// 访客预约已存在待处理的删除任务 访客出门后 把删除时间调整为当前
							moveIscDeleteTaskToNow(deviceIscTask);
						} else {
							// 生成新的删除任务
							deviceTaskVO = new DeviceTaskVO();
							deviceTaskVO.setAction(DeviceTaskConstants.DEL);
							deviceTaskVO.setCardNo(vehicleId);
							deviceTaskVO.setDeviceCode(auth.getDeviceId());
								deviceTaskVO.setStartTime(DateUtils.currentSeconds());
								deviceTaskVO.setOverTime(DateUtils.currentSeconds());
								deviceTaskVO.setGeneral(vehicle.getPlate());
								deviceTaskVO.setDeviceType(DeviceTaskConstants.CAR);
								deviceTaskVO.setServiceType(DeviceTaskServiceTypeEnum.CAR_ADMITTANCE.getCode());
								smtDeviceTaskService.saveTask(deviceTaskVO);
							}
					}
				}
			}
		}

	private SmtDeviceTask findReusableDeviceDeleteTask(String cardNo, String deviceCode, Integer deviceType,
													   Integer serviceType) {
		List<SmtDeviceTask> tasks = smtDeviceTaskService.list(new LambdaQueryWrapper<SmtDeviceTask>()
				.eq(SmtDeviceTask::getCardNo, cardNo)
				.eq(SmtDeviceTask::getDeviceCode, deviceCode)
				.eq(SmtDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
				.eq(SmtDeviceTask::getDeviceType, deviceType)
				.eq(SmtDeviceTask::getServiceType, serviceType));
		if (CollUtil.isEmpty(tasks)) {
			return null;
		}
		return tasks.stream().filter(this::isReusableDeleteTask).findFirst().orElse(null);
	}

	private SmtIscDeviceTask findReusableIscDeleteTask(String cardNo, String deviceCode, Integer deviceType,
													  Integer serviceType) {
		List<SmtIscDeviceTask> tasks = smtIscDeviceTaskService.list(new LambdaQueryWrapper<SmtIscDeviceTask>()
				.eq(SmtIscDeviceTask::getCardNo, cardNo)
				.eq(SmtIscDeviceTask::getDeviceCode, deviceCode)
				.eq(SmtIscDeviceTask::getAction, DeviceTaskActionEnum.DEL.getCode())
				.eq(SmtIscDeviceTask::getDeviceType, deviceType)
				.eq(SmtIscDeviceTask::getServiceType, serviceType)
				.and(wrapper -> wrapper.isNull(SmtIscDeviceTask::getTimes)
						.or().lt(SmtIscDeviceTask::getTimes, DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES)));
		if (CollUtil.isEmpty(tasks)) {
			return null;
		}
		return tasks.stream().filter(this::isReusableDeleteTask).findFirst().orElse(null);
	}

	private boolean isReusableDeleteTask(SmtDeviceTask task) {
		return isReusableDeleteTaskStatus(task.getStatus());
	}

	private boolean isReusableDeleteTask(SmtIscDeviceTask task) {
		return isReusableDeleteTaskStatus(task.getStatus()) && !hasReachedAuthConfigMaxRetryTimes(task);
	}

	private boolean isReusableDeleteTaskStatus(Integer status) {
		return status == null
				|| Objects.equals(status, DeviceTaskStatusEnum.INIT.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.DOING.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.FAIL.getCode())
				|| Objects.equals(status, DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode());
	}

	private boolean hasReachedAuthConfigMaxRetryTimes(SmtIscDeviceTask task) {
		return task.getTimes() != null && task.getTimes() >= DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES;
	}

	private void moveDeviceDeleteTaskToNow(SmtDeviceTask deviceTask) {
		deviceTask.setOverTime(DateUtils.currentSeconds());
		if (!Objects.equals(deviceTask.getStatus(), DeviceTaskStatusEnum.DOING.getCode())) {
			deviceTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
			deviceTask.setRemark(null);
			deviceTask.setCode(null);
		}
		deviceTask.setUpdateTime(LocalDateTime.now());
		smtDeviceTaskService.updateById(deviceTask);
	}

	private void moveIscDeleteTaskToNow(SmtIscDeviceTask deviceIscTask) {
		deviceIscTask.setOverTime(DateUtils.currentSeconds());
		if (!Objects.equals(deviceIscTask.getStatus(), DeviceTaskStatusEnum.DOING.getCode())) {
			deviceIscTask.setStatus(DeviceTaskStatusEnum.INIT.getCode());
			deviceIscTask.setIscTaskId(null);
			deviceIscTask.setRemark(null);
			deviceIscTask.setCode(null);
		}
		deviceIscTask.setUpdateTime(LocalDateTime.now());
		smtIscDeviceTaskService.updateById(deviceIscTask);
	}


	/**
	 * 根据身份证号查询同行人信息
	 * @param idCard 身份证号
	 * @return 返回查询到的申请信息列表
	 */
	@Override
	public List<SmtAdmittanceApply> getByIdCard(String idCard) {
		// 根据身份证号查询同行人信息
		List<SmtAdmittanceFellow> fellows = smtAdmittanceFellowService.list(Wrappers.<SmtAdmittanceFellow>query().lambda()
				.eq(SmtAdmittanceFellow::getCertNo, idCard));
		// 如果没有同行人信息，则返回空列表
		if (CollUtil.isEmpty(fellows)) {
			return new ArrayList<>();
		}
		// 获取同行人信息中的访客ID
		List<Long> applyIds = fellows.stream().map(SmtAdmittanceFellow::getVisitorId).collect(Collectors.toList());
		// 根据访客ID查询申请信息
		List<SmtAdmittanceApply> applies = this.list(Wrappers.<SmtAdmittanceApply>query().lambda()
				.in(SmtAdmittanceApply::getId, applyIds)
				.eq(SmtAdmittanceApply::getStatus, VisitorStatusEnum.Status_0.getCode())
				.ge(SmtAdmittanceApply::getStartTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
				.le(SmtAdmittanceApply::getEndTime, LocalDateTime.now())
				.orderByDesc(SmtAdmittanceApply::getCreateTime));
		// 返回申请信息
		return applies;
	}

	@Override
	public SmtAdmittanceApply getLastByIdCard(String idCard) {
		List<SmtAdmittanceApply> applies = this.getByIdCard(idCard);
		if (CollUtil.isEmpty(applies)) {
			throw new SmartException("本日内暂不存在审批已通过且在预约时间内的预约");
		}
		return applies.get(0);
	}

	/**
	 * 根据申请ID删除相关的同行人和车辆设备授权
	 * @param id 申请ID
	 * @return 是否成功
	 */
	@Override
	public Boolean delDeviceAuth(Long id) {
		// 根据申请ID获取同行人列表
		List<SmtAdmittanceFellow> fellows = smtAdmittanceFellowService.getByApplyId(id);
		// 根据申请ID获取车辆列表
		List<SmtAdmittanceVehicle> vehicles = smtAdmittanceVehicleService.getByApplyId(id);
		// 遍历同行人列表，删除同行人设备授权
		fellows.forEach(fellow -> {
			smtDeviceTaskService.delVisitorDeviceAuth(fellow.getId());
		});
		// 遍历车辆列表，删除车辆设备授权
		vehicles.forEach(vehicle -> {
			smtDeviceTaskService.delVisitorDeviceAuth(vehicle.getId());
		});
		// 返回true
		return Boolean.TRUE;
	}

	/**
	 * 将照片上传到远程电脑的共享文件夹
	 * @param id 申请ID，用于获取相应的访客信息
	 * @return 是否成功
	 */
	@Override
	public Boolean smbPutPhoto(Long id) {
		// 定义一个布尔变量，用于判断上传是否成功
		Boolean isSuccess = true;
		// 获取远程URL，并替换其中的IP地址
		// String url = remoteUrl.replace("{ip}", WebUtils.getIP());
		// 打印日志，记录上传客户端共享文件夹的IP地址
		// log.info("上传客户端共享文件夹的IP: " + url);
		// 根据申请ID获取同行访客列表
		List<SmtAdmittanceFellow> fellowVisitorList = smtAdmittanceFellowService.getByApplyId(id);
		try {
			// 定义一个字节数组，用于存储同行访客照片的二进制数据
			byte[] fellowVisitorBytes;
			// 遍历同行访客列表
			for (SmtAdmittanceFellow fellowVisitorVO : fellowVisitorList) {
				// 根据同行访客照片ID获取照片的二进制数据
				fellowVisitorBytes = smtImageService.getImageBinaryByCode(fellowVisitorVO.getFellowPhotoId());
				// 如果照片的二进制数据不为空且长度大于0
				if (Objects.nonNull(fellowVisitorBytes) && fellowVisitorBytes.length > 0) {
					// 定义文件名
					String fileName = fellowVisitorVO.getFellowPhotoId().concat(".png");
					try {
						// 使用HttpUtil创建一个POST请求，上传照片到远程URL
						HttpResponse response = HttpUtil.createPost(remoteUrl).form("file", fellowVisitorBytes, fileName).form("filePath", savePath.concat(fileName)).execute();
						// 如果上传失败
						if (!response.isOk()) {
							// 打印日志，记录上传图片失败的信息
							log.error("【入厂申请上传照片到远程电脑失败】上传图片失败: fileName={}, response={}", fileName, response.body());
							// 返回false
							return false;
						}
						// 打印日志，记录上传图片成功的信息
						log.info("【入厂申请上传照片到远程电脑失败】上传图片成功: fileName={}", fileName);
					} catch (Exception e) {
						// 打印日志，记录上传图片失败的异常信息
						log.error("【入厂申请上传照片到远程电脑失败】{},{}", remoteUrl, e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			// 打印异常堆栈信息
			e.printStackTrace();
			// 打印日志，记录上传图片失败的异常信息
			log.error("【入厂申请上传照片到远程电脑失败】{},{}", e.getCause().getMessage(), e.getStackTrace());
		}
		// 返回上传是否成功
		return isSuccess;
	}


	/**
	 * 访客是否超时
	 */
	@Override
	public void visitorOverTime() {
		//查询状态为0的访客，判断是否已经超时
		List<SmtAdmittanceApply> selectList = this.list(Wrappers.<SmtAdmittanceApply>query().lambda()
				.eq(SmtAdmittanceApply::getStatus, VisitorStatusEnum.Status_0.getCode())
				.lt(SmtAdmittanceApply::getEndTime, LocalDateTime.now().plusHours(overtimeOffsetHour)));
		removeVisitor(selectList);
		//查询超时未审批
		List<SmtAdmittanceApply> selectListNoPass = this.list(
				Wrappers.<SmtAdmittanceApply>query().lambda()
						.eq(SmtAdmittanceApply::getStatus, VisitorStatusEnum.Status_2.getCode())
						.lt(SmtAdmittanceApply::getEndTime, LocalDateTime.now().plusHours(overtimeOffsetHour)));
		updateNoPass(selectListNoPass);
	}

	/**
	 * 检查已到达并且访问时间早于当前时间的访客并删除
	 */
	@Override
	public void visitorComeOnTime() {
		//查询状态为3的访客，判断是否已经超时
		List<SmtAdmittanceApply> selectList = this.list(
				Wrappers.<SmtAdmittanceApply>query().lambda()
						.eq(SmtAdmittanceApply::getStatus, SmtVisitorEnum.COME_STATUS.getType())
						.ge(SmtAdmittanceApply::getEndTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
						// .le(SmtAdmittanceApply::getEndTime, LocalDateTime.of(LocalDate.now(), LocalTime.MAX)));
						.le(SmtAdmittanceApply::getEndTime, LocalDateTime.now()));
		if (CollUtil.isNotEmpty(selectList)) {
			for (SmtAdmittanceApply apply : selectList) {
				this.delTask(apply);
			}
		}
	}

	/**
	 * 未到达的访客，和没有发过短信的访客发送短信
	 */
	@Override
	public void visitorRemind() {
		//查询状态为0的访客，且没有发送过短信的访客信息
		List<SmtAdmittanceApply> selectList = this.list(
				Wrappers.<SmtAdmittanceApply>query().lambda()
						.eq(SmtAdmittanceApply::getStatus, VisitorStatusEnum.Status_0.getCode())
						.eq(SmtAdmittanceApply::getIsSend, SmtVisitorEnum.NOT_IS_SEND.getType())
						.ge(SmtAdmittanceApply::getStartTime, LocalDateTime.now())
						.le(SmtAdmittanceApply::getStartTime, LocalDateTime.now().plusMinutes(pretime)));
		if (CollectionUtils.isNotEmpty(selectList)) {
			log.info("未到达的入厂申请, 提前短信通知：{}", selectList);
			selectList.forEach(v -> {
				//发送来访到时通知
				SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_ADMITTANCE_10903.getCode());
				String msg = template.getTempContent().replace("{被访人姓名}", v.getReceptionistName())
						.replace("{倒计时}", pretime.toString())
						.replace("{预计来访时间}", DateUtils.convert(v.getStartTime()));
/*
				String unionId = smtWechatBandingService.getUnionId(v.getUnionId());*/
				if (StrUtil.isNotEmpty(v.getUnionId())) {
					WeChatMsgUtil.sendMsg(null, msg, v.getUnionId(), null);
					v.setIsSend(SmtVisitorEnum.IS_SEND.getType());
				}

				v.updateById();
			});
		}
	}

	/**
	 * 超时未离开的访客要发短息
	 */
	@Override
	public void visitorOverTimeNoLeave() {
		//查询状态为3(已到达)的访客，判断是否已经超时
		List<SmtAdmittanceApply> selectList = this.list(
				Wrappers.<SmtAdmittanceApply>query().lambda()
						.eq(SmtAdmittanceApply::getStatus, VisitorStatusEnum.Status_3.getCode())
						.ge(SmtAdmittanceApply::getEndTime, LocalDateTime.now().plusMinutes(-150))
						.le(SmtAdmittanceApply::getEndTime, LocalDateTime.now().plusMinutes(-30)));
		for (SmtAdmittanceApply apply : selectList) {
			log.info("超时未离开的入厂申请, 提前通知：{}", selectList);
			//超时未离开通知
			SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_ADMITTANCE_10906.getCode());
			String msg = template.getTempContent().replace("{访客姓名}", apply.getVisitorName())
					.replace("{来访单位}", apply.getCompany());
			WeChatMsgUtil.sendMsg(apply.getReceptionistBadge(), msg, null, null);
		}
	}

	/**
	 * 预约审批超时
	 *
	 * @param selectListNoPass
	 */
	private void updateNoPass(List<SmtAdmittanceApply> selectListNoPass) {
		for (SmtAdmittanceApply v : selectListNoPass) {
			v.setStatus(VisitorStatusEnum.CAUSE_6.getCode());
			this.updateById(v);
			//修改待我审批里的状态
			List<ApproveList> selectListApprove = approveListService.list(Wrappers.<ApproveList>query().lambda()
					.eq(ApproveList::getBusinessId, v.getId()).eq(ApproveList::getApproveType, ApproveListTypeConstants.VISITOR));
			List<SmtVisitorProcessRecord> processList = smtVisitorProcessRecordService.list(Wrappers.<SmtVisitorProcessRecord>query()
					.lambda().eq(SmtVisitorProcessRecord::getVisitorId, v.getId())
					.eq(SmtVisitorProcessRecord::getStatus, VisitorProcessEnum.WATING_2.getCode()));
			if (CollectionUtils.isNotEmpty(processList)) {
				processList.forEach(process -> {
					process.setStatus(VisitorProcessEnum.WATING_3.getCode());
					process.setStatusName(VisitorProcessEnum.WATING_3.getDesc());
					smtVisitorProcessRecordService.updateById(process);
				});
			}
			if (CollUtil.isNotEmpty(selectListApprove)) {
				ApproveList approveList = new ApproveList();
				approveList.setBusinessId(v.getId().toString());
				approveList.setApproveState(VisitorStatusEnum.CAUSE_6.getCode());
				approveList.setApproveType(ApproveListTypeConstants.VISITOR);
				approveList.setApproveBadge(v.getReceptionistBadge());
				approveListService.update(approveList, new LambdaUpdateWrapper<ApproveList>()
						.eq(ApproveList::getBusinessId, approveList.getBusinessId())
						.eq(ApproveList::getApproveBadge, v.getReceptionistBadge()));
			}
		}
	}

	/**
	 * 修改超时未到的访客
	 *
	 * @param selectList
	 */
	private void removeVisitor(List<SmtAdmittanceApply> selectList) {
		if (CollectionUtils.isNotEmpty(selectList)) {
			selectList.forEach(v -> {
				//修改访客的状态为超时未到 4
				v.setStatus(VisitorStatusEnum.CAUSE_4.getCode());
				//删除超时未到的访客
				this.updateById(v);
				//修改待我审批里的状态
				List<ApproveList> selectListApprove = approveListService.list(Wrappers.<ApproveList>query().lambda()
						.eq(ApproveList::getBusinessId, v.getId())
						.eq(ApproveList::getApproveType, ApproveListTypeConstants.VISITOR));
				if (CollUtil.isNotEmpty(selectListApprove)) {
					ApproveList approveList = new ApproveList();
					approveList.setBusinessId(v.getId().toString());
					approveList.setApproveState(VisitorStatusEnum.CAUSE_4.getCode());
					approveList.setApproveType(ApproveListTypeConstants.VISITOR);
					approveList.setApproveBadge(v.getReceptionistBadge());
					approveListService.update(approveList, new LambdaUpdateWrapper<ApproveList>()
							.eq(ApproveList::getBusinessId, approveList.getBusinessId())
							.eq(ApproveList::getApproveBadge, v.getReceptionistBadge()));
				}
			});
		}
	}

	@Override
	public String getRemoteUrl() {
		return null;
	}

	@Override
	public void updateOaStatusTask() {
		List<SmtAdmittanceApply> applyList = this.list(Wrappers.<SmtAdmittanceApply>query().lambda()
				.eq(SmtAdmittanceApply::getStatus, VisitorStatusEnum.Status_2.getCode()));
		if (CollUtil.isEmpty(applyList)) {
			return;
		}
		for (SmtAdmittanceApply id : applyList) {
			WorkFlowLogDTO workFlowLogDTO = oaWorkflowService.query(id.getProcessId());
			if (ObjectUtil.isNull(workFlowLogDTO) && !workFlowLogDTO.success()) {
				continue;
			}
			List<WorkFlowLogDataDTO> data = workFlowLogDTO.getResultdata();
			if (CollUtil.isEmpty(data)) {
				continue;
			}
			WorkFlowLogDataDTO dataDTO = data.get(data.size() - 1);
			if (OaFinalStatusEnum.CAUSE_3.getCode().toString().equals(dataDTO.getCURRENTNODETYPE())) {
				id.setStatus(VisitorStatusEnum.Status_0.getCode());
				this.updateStatus(id);
			}
			if (OaFinalStatusEnum.CAUSE_0.getCode().toString().equals(dataDTO.getCURRENTNODETYPE())) {
				id.setStatus(VisitorStatusEnum.Status_1.getCode());
				this.updateStatus(id);
			}
		}
	}

}
