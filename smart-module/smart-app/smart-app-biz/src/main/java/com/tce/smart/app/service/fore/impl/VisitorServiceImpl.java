package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.ApproveVisitorAo;
import com.tce.smart.app.ao.fore.VisitorAo;
import com.tce.smart.app.ao.fore.VisitorIdAo;
import com.tce.smart.app.ao.wechat.AddVisitMemberAo;
import com.tce.smart.app.ao.wechat.AddVisitorAo;
import com.tce.smart.app.ao.wechat.CheckFaceAo;
import com.tce.smart.app.ao.wechat.CheckHostAo;
import com.tce.smart.app.api.dto.WechatVisitorRecordDetailReqDTO;
import com.tce.smart.app.api.dto.WechatVisitorRecordReqDTO;
import com.tce.smart.app.dto.WechatAccessTokenDto;
import com.tce.smart.app.dto.fore.OcrIdCardDto;
import com.tce.smart.app.entity.AppWechatBinding;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppSmsService;
import com.tce.smart.app.service.AppWechatBindingService;
import com.tce.smart.app.service.IOcrService;
import com.tce.smart.app.service.fore.VisitorService;
import com.tce.smart.app.service.wechat.WechatAuthService;
import com.tce.smart.app.vo.fore.*;
import com.tce.smart.app.vo.wechat.AddVisitorVo;
import com.tce.smart.app.vo.wechat.CheckHostVo;
import com.tce.smart.app.vo.wechat.PhotoBaseVisitorVo;
import com.tce.smart.app.vo.wechat.PhotoVisitorVo;
import com.tce.smart.common.core.constant.PaginationConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwEmphrYsService;
import com.tce.smart.platform.api.dto.SaveSmtVisitorDTO;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.dto.SmtVisitorDTO;
import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.req.admittance.VisitorActionCapabilityConsumeReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchAppSmtVisitorRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchAppVisitorDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.VisitorListRespDTO;
import com.tce.smart.platform.api.feign.RemoteParkInternalService;
import com.tce.smart.platform.api.feign.RemoteSmtImageService;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.tce.smart.platform.api.feign.RemoteVisitorService;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.enums.SmtImageEnum;
import com.tce.smart.tool.enums.VisitorEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 访客信息服务接口实现类
 *
 * @author ly
 * @date 2019-05-13 16:17:32
 */
@Service
@AllArgsConstructor
@Slf4j
public class VisitorServiceImpl implements VisitorService {
	private static final String VISITOR_BLACKLIST_PURPOSE = "visitor-blacklist";
	@Autowired
	private RemoteVisitorService remoteVisitorService;

	@Autowired
	private AppSmsService appSmsService;

	@Autowired
	private AppCommService appCommService;

	@Autowired
	private RemoteSmtImageService remoteSmtImageService;

	@Autowired
	private IOcrService ocrService;

	@Autowired
	private RemoteParkInternalService remoteParkInternalService;

	@Autowired
	private RemoteStaffService remoteStaffService;

	@Autowired
	private WechatAuthService wechatAuthService;

	@Autowired
	private AppWechatBindingService wechatBindingService;

	@Autowired
	private RemoteEvwEmphrYsService remoteEvwEmphrYsService;

	/**
	 * 查询来访预约列表
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public IPage<?> getVisitorList(Map<String, Object> params, VisitorAo visitorAo) {
		// 远程根据参数获取来访的预约列表
		// 获取员工号
		String staffBadge = SecurityUtils.getUser().getUsername();
		if (StringUtils.isBlank(staffBadge)) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_PROMOTERBADGE_NULL.getMessage());
		}
		Result<Page<SearchAppSmtVisitorRespDTO>> result = remoteVisitorService.searchAppVisitorPage(
				MapUtil.getInt(params, PaginationConstants.CURRENT),
				MapUtil.getInt(params, PaginationConstants.SIZE),
				staffBadge,
				visitorAo.getVisitListType(),
				SecurityConstants.FROM_IN);
		Page<SearchAppSmtVisitorRespDTO> pageInfo = result.getData();
		// 转换接过来的值
		if (result.isSuccess()) {
			if (CollectionUtils.isNotEmpty(pageInfo.getRecords())) {
				List visitorList = new ArrayList();
				VisitorVo visitorVo;
				SearchAppSmtVisitorRespDTO searchAppSmtVisitorVO;
				for (int i = 0; i < pageInfo.getRecords().size(); i++) {
					visitorVo = new VisitorVo();
					searchAppSmtVisitorVO = pageInfo.getRecords().get(i);
					visitorVo.setVisitId(String.valueOf(searchAppSmtVisitorVO.getVisitorId()));
					visitorVo.setVisitorName(searchAppSmtVisitorVO.getVisitorName());
					visitorVo.setParkName(searchAppSmtVisitorVO.getParkName());
					visitorVo.setParkId(searchAppSmtVisitorVO.getParkId());
					//图片
					visitorVo.setVisitorPhoto(appCommService.buildHqImageUrl(searchAppSmtVisitorVO.getVisitorPhoto()));

					visitorVo.setVisitState(searchAppSmtVisitorVO.getStatus());
					visitorVo.setVisitStateDesc(searchAppSmtVisitorVO.getStatusDesc());
					visitorVo.setStartDate(searchAppSmtVisitorVO.getStartTime());
					visitorVo.setEndDate(searchAppSmtVisitorVO.getEndTime());
					visitorVo.setVisitReason(searchAppSmtVisitorVO.getCauseDesc());
					visitorVo.setProcessName(searchAppSmtVisitorVO.getProcessNodeName());
					visitorList.add(visitorVo);
				}
				pageInfo.setRecords(visitorList);
			}
		} else {
			throw new TCEException("查询预约列表异常");
		}
		return pageInfo;
	}

	/**
	 * 获取详情
	 */
	@Override
	public VisitorDetailVo getVisitorListDeatil(VisitorIdAo visitorId) {

		// 调用远程定位访客详情接口
		SearchAppVisitorDetailRespDTO detail = remoteVisitorService.searchAppVisitorDetail(visitorId.getId(), SecurityConstants.FROM_IN).getData();
		VisitorDetailVo visitorDetailVo = BeanUtils.transform(VisitorDetailVo.class, detail);
		//图片
		visitorDetailVo.setVisitorPhoto(appCommService.buildHqImageUrl(detail.getVisitorPhoto()));
		if (detail.getCause().equals(VisitorEnum.CAUSE_5.getCode()) || detail.getCause().equals(VisitorEnum.CAUSE_7.getCode())) {
			visitorDetailVo.setVisitorFrontPhoto(appCommService.buildHqImageUrl(detail.getVisitorFrontPhoto()));
			visitorDetailVo.setVisitorBackPhoto(appCommService.buildHqImageUrl(detail.getVisitorBackPhoto()));
		}
		visitorDetailVo.setVisitorMobile(detail.getVisitorPhone());
		visitorDetailVo.setVisitorCompany(detail.getCompany());
		visitorDetailVo.setVisitorCertNo(detail.getCertNo() == null ? "" : detail.getCertNo());
		visitorDetailVo.setVisitReason(detail.getCauseDesc());
		visitorDetailVo.setPlateNumber(detail.getVehiclePlate());
		visitorDetailVo.setEmployeeName(detail.getReceptionistName());
		visitorDetailVo.setEmployeeMobile(detail.getReceptionistPhone());
		visitorDetailVo.setStartTime(detail.getStartTime());
		visitorDetailVo.setEndTime(detail.getEndTime());
		visitorDetailVo.setVisitState(detail.getStatus());
		// 判断随行人员是否为空
		if (CollectionUtils.isNotEmpty(detail.getFellowVisitorList())) {
			List<MemberDetailVo> list = new ArrayList<>();
			for (int i = 0; i < detail.getFellowVisitorList().size(); i++) {
				MemberDetailVo memberVo = new MemberDetailVo();
				memberVo.setMemberName(detail.getFellowVisitorList().get(i).getFellowName());

				//图片
				memberVo.setMemberPhoto(appCommService.buildHqImageUrl(detail.getFellowVisitorList().get(i).getFellowPhoto()));
				list.add(memberVo);
				visitorDetailVo.setMember(list);
			}
		}
		visitorDetailVo.setProcessList(detail.getProcessList());
		return visitorDetailVo;
	}

	/**
	 * 查看随行人员
	 */
	@Override
	public MemberVo getMemberListDeatil(VisitorIdAo visitorId) {
		MemberVo memberVo = new MemberVo();
		// 调用远程定位访客详情接口
		SearchAppVisitorDetailRespDTO detail = remoteVisitorService.searchAppVisitorDetail(visitorId.getId(), SecurityConstants.FROM_IN).data();
		// 判断随行人员是否为空
		List<MemberDetailVo> memberList = new ArrayList<>();
		if (Objects.isNull(detail)) {
			return memberVo;
		}
		if (CollectionUtils.isNotEmpty(detail.getFellowVisitorList())) {
			for (int i = 0; i < detail.getFellowVisitorList().size(); i++) {
				MemberDetailVo memberDetailVo = new MemberDetailVo();
				memberDetailVo.setMemberName(detail.getFellowVisitorList().get(i).getFellowName());
				//图片
				memberDetailVo.setMemberPhoto(appCommService.buildHqImageUrl(detail.getFellowVisitorList().get(i).getFellowPhoto()));
				memberList.add(memberDetailVo);
			}
			memberVo.setRecords(memberList);
		}
		return memberVo;

	}

	/**
	 * 添加访客预约
	 */
	@Override
	public AddVisitorVo addVisitor(AddVisitorAo addVisitorAo) {
		// 获取预约人的员工号
		String staffBadge = SecurityUtils.getUser().getUsername();

		if (StringUtils.isEmpty(staffBadge)) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_PROMOTERBADGE_NULL.getMessage());
		}
		SaveSmtVisitorDTO saveSmtVisitor = createRemoteBean(addVisitorAo, staffBadge);
		if(addVisitorAo.getEmployeeMobile().equals(addVisitorAo.getVisitorMobile())) {
			throw new TCEException("自己无法预约自己！");
		}
		String regex = "\\d{15}(\\d{2}[0-9xX])?";
		if (saveSmtVisitor.getCause().equals(VisitorEnum.CAUSE_5.getCode())) {
			log.info("开始识别身份证正面信息");
			OcrIdCardDto frontInfo = ocrService.readIdCardFontImg(addVisitorAo.getVisitorFrontPhoto());

			if (Objects.isNull(frontInfo)) {
				log.error("未识别到身份证正面信息");
				throw new TCEException("识别身份证正面失败，请正对拍摄");
			}
			if (Objects.isNull(frontInfo.getName())) {
				log.error("未识别到姓名信息");
				throw new TCEException("识别身份证正面失败，请正对拍摄");
			}
			if (!frontInfo.getIdentityCard().matches(regex)) {
				log.error("身份证号码格式不正确");
				throw new TCEException("识别身份证正面失败，请正对拍摄");
			}
			log.info("身份证正面信息识别成功");
			//读取身份证背面信息
			log.info("开始识别身份证背面信息");
			OcrIdCardDto backInfo = ocrService.readIdCardBackImg(addVisitorAo.getVisitorBackPhoto());
			if (Objects.isNull(backInfo)) {
				log.error("未识别到身份证背面信息");
				throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "识别身份证背面失败，请正对拍摄");
			}
			if (Objects.isNull(backInfo.getValidityDate())) {
				log.error("未识别到身份有效期限");
				throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "识别身份证背面失败，请正对拍摄");
			}
			log.info("身份证背面信息识别成功");
		}
		Result<SmtVisitorDTO> addSmtVisitor = remoteVisitorService.addSmtVisitor(saveSmtVisitor, SecurityConstants.FROM_IN);
		if(!addSmtVisitor.isSuccess())
		{
			log.info("添加访客失败");
			log.info(addSmtVisitor.getMessage());
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), addSmtVisitor.getMessage());
		}
		SmtVisitorDTO visitorDTO = addSmtVisitor.data();
		AddVisitorVo visitorId = new AddVisitorVo();
		visitorId.setVisitId(visitorDTO.getId().toString());
		return visitorId;
	}

	/**
	 * 从公众号添加访客预约
	 */
	@Override
	public AddVisitorVo addVisitorFromWechat(AddVisitorAo addVisitorAo) {
		//验证短信验证码
		appSmsService.verifySmsCode(addVisitorAo.getVisitorMobile(), addVisitorAo.getSmsCode());

		// 获取预约人的员工号
		String staffBadge = null;

		SaveWechatSmtVisitorReqDTO saveSmtVisitor = createRemoteBeans(addVisitorAo, staffBadge);
		log.info("remoteVisitorService.addWechatVisitor request");
		SmtVisitorDTO smtVisitor = remoteVisitorService.addWechatVisitor(saveSmtVisitor, SecurityConstants.FROM_IN).get();
		AddVisitorVo visitorId = new AddVisitorVo();
		visitorId.setVisitId(smtVisitor.getId().toString());
		return visitorId;
	}

	/**
	 * 添加随行人员
	 */
	@Override
	public void addFellowVisitor(AddVisitMemberAo addVisitMemberAo) {
		AddFellowVisitorReqDTO addFellowVisitorDTO = new AddFellowVisitorReqDTO();
		List<SaveFellowVisitorReqDTO> fellowList = new ArrayList<>();
		addFellowVisitorDTO.setVisitId(Long.valueOf(addVisitMemberAo.getVisitId()));
		if (CollectionUtils.isNotEmpty(addVisitMemberAo.getMember())) {
			for (int i = 0; i < addVisitMemberAo.getMember().size(); i++) {
				SaveFellowVisitorReqDTO saveFellowVisitorDTO = new SaveFellowVisitorReqDTO();
				saveFellowVisitorDTO.setFellowName(addVisitMemberAo.getMember().get(i).getMemberName());
				saveFellowVisitorDTO.setFellowPhoto(addVisitMemberAo.getMember().get(i).getMemberPhoto());
				fellowList.add(saveFellowVisitorDTO);
			}
		}
		addFellowVisitorDTO.setFollowList(fellowList);
		Result result = remoteVisitorService.addFellowVisitor(addFellowVisitorDTO, SecurityConstants.FROM_IN);
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
	}

	/**
	 * 公众号添加随行人员
	 */
	@Override
	public void addFellowVisitorWechat(AddVisitMemberAo addVisitMemberAo) {
		AddWechatFellowVisitorReqDTO addFellowVisitorDTO = new AddWechatFellowVisitorReqDTO();
		List<SaveFellowWechatVisitorReqDTO> fellowList = new ArrayList<SaveFellowWechatVisitorReqDTO>();
		addFellowVisitorDTO.setVisitId(Long.valueOf(addVisitMemberAo.getVisitId()));
		if (CollectionUtils.isNotEmpty(addVisitMemberAo.getMember())) {
			for (int i = 0; i < addVisitMemberAo.getMember().size(); i++) {
				SaveFellowWechatVisitorReqDTO saveFellowVisitorDTO = new SaveFellowWechatVisitorReqDTO();
				saveFellowVisitorDTO.setFellowName(addVisitMemberAo.getMember().get(i).getMemberName());
				saveFellowVisitorDTO.setFellowPhotoId(addVisitMemberAo.getMember().get(i).getMemberPhotoId());
				fellowList.add(saveFellowVisitorDTO);
			}
		}
		addFellowVisitorDTO.setFollowList(fellowList);
		Result result = remoteVisitorService.addWechatFellowVisitor(addFellowVisitorDTO, SecurityConstants.FROM_IN);
		if (!result.isSuccess()) {
			throw new TCEException(result.getMessage());
		}
	}

	@Override
	public VisitorTypeVo getVisitorType() {
		VisitorTypeVo visitorTypeVo = new VisitorTypeVo();
		// 调用远程定位访客详情接口
		List<VisitorTypeDetailVo> visitorTypeList = new ArrayList<VisitorTypeDetailVo>();
		VisitorTypeDetailVo visitorTypeDetailVo1 = new VisitorTypeDetailVo();
		VisitorTypeDetailVo visitorTypeDetailVo2 = new VisitorTypeDetailVo();
		VisitorTypeDetailVo visitorTypeDetailVo3 = new VisitorTypeDetailVo();
		VisitorTypeDetailVo visitorTypeDetailVo4 = new VisitorTypeDetailVo();
		VisitorTypeDetailVo visitorTypeDetailVo5 = new VisitorTypeDetailVo();
		VisitorTypeDetailVo visitorTypeDetailVo6 = new VisitorTypeDetailVo();
		VisitorTypeDetailVo visitorTypeDetailVo7 = new VisitorTypeDetailVo();
		visitorTypeDetailVo1.setReasonTypeCode(VisitorEnum.CAUSE_1.getCode().toString());
		visitorTypeDetailVo1.setReasonTypeName(VisitorEnum.CAUSE_1.getDesc());
		visitorTypeList.add(visitorTypeDetailVo1);
		visitorTypeDetailVo2.setReasonTypeCode(VisitorEnum.CAUSE_2.getCode().toString());
		visitorTypeDetailVo2.setReasonTypeName(VisitorEnum.CAUSE_2.getDesc());
		visitorTypeList.add(visitorTypeDetailVo2);
		visitorTypeDetailVo3.setReasonTypeCode(VisitorEnum.CAUSE_3.getCode().toString());
		visitorTypeDetailVo3.setReasonTypeName(VisitorEnum.CAUSE_3.getDesc());
		visitorTypeList.add(visitorTypeDetailVo3);
		visitorTypeDetailVo4.setReasonTypeCode(VisitorEnum.CAUSE_4.getCode().toString());
		visitorTypeDetailVo4.setReasonTypeName(VisitorEnum.CAUSE_4.getDesc());
		visitorTypeList.add(visitorTypeDetailVo4);
		visitorTypeDetailVo5.setReasonTypeCode(VisitorEnum.CAUSE_5.getCode().toString());
		visitorTypeDetailVo5.setReasonTypeName(VisitorEnum.CAUSE_5.getDesc());
		visitorTypeList.add(visitorTypeDetailVo5);
		visitorTypeDetailVo6.setReasonTypeCode(VisitorEnum.CAUSE_6.getCode().toString());
		visitorTypeDetailVo6.setReasonTypeName(VisitorEnum.CAUSE_6.getDesc());
		visitorTypeList.add(visitorTypeDetailVo6);
		visitorTypeDetailVo7.setReasonTypeCode(VisitorEnum.CAUSE_7.getCode().toString());
		visitorTypeDetailVo7.setReasonTypeName(VisitorEnum.CAUSE_7.getDesc());
		visitorTypeList.add(visitorTypeDetailVo7);
		visitorTypeVo.setRecords(visitorTypeList);
		visitorTypeVo.setTotal(visitorTypeList.size());
		return visitorTypeVo;
	}

	@Override
	public CheckHostVo checkhost(CheckHostAo checkHostAo) {
		SmtVisitorDTO smtVisitorSearch = new SmtVisitorDTO();
		smtVisitorSearch.setReceptionistName(checkHostAo.getHostName());
		smtVisitorSearch.setReceptionistPhone(checkHostAo.getHostMobile());
		smtVisitorSearch.setParkId(checkHostAo.getParkId());

		CheckHostVo checkHostVo = new CheckHostVo();
		try {
			SmtVisitorDTO smtVisitor = remoteVisitorService.SearchReceptionistForWechat(smtVisitorSearch, SecurityConstants.FROM_IN).data();
			checkHostVo.setEmployeeId(smtVisitor.getReceptionistBadge());
		}catch (Exception e){
			throw new TCEException(e.getMessage());
		}
		return checkHostVo;
	}

	/**
	 * 转化Vo
	 *
	 * @param addVisitorAo addVisitorAo
	 * @param staffBadge   staffBadge
	 * @return
	 */
	private SaveWechatSmtVisitorReqDTO createRemoteBeans(AddVisitorAo addVisitorAo, String staffBadge) {
		SaveWechatSmtVisitorReqDTO saveSmtVisitor = new SaveWechatSmtVisitorReqDTO();
		saveSmtVisitor.setParkId(addVisitorAo.getParkId());
		saveSmtVisitor.setReceptionistBadge(addVisitorAo.getEmployeeId());
		saveSmtVisitor.setVisitorName(addVisitorAo.getVisitorName());
		saveSmtVisitor.setVisitorPhotoId(addVisitorAo.getVisitorPhotoId());
		saveSmtVisitor.setVisitorPhone(addVisitorAo.getVisitorMobile());
		saveSmtVisitor.setCompany(addVisitorAo.getVisitorCompany());
		saveSmtVisitor.setCause(Integer.parseInt(addVisitorAo.getVisitReasonCode()));
		saveSmtVisitor.setVehiclePlate(addVisitorAo.getPlateNumber());
		saveSmtVisitor.setStartTime(addVisitorAo.getStartTime());
		saveSmtVisitor.setEndTime(addVisitorAo.getEndTime());
		saveSmtVisitor.setReceptionistName(addVisitorAo.getEmployeeName());
		saveSmtVisitor.setReceptionistPhone(addVisitorAo.getEmployeeMobile());
		saveSmtVisitor.setReceptionistBadge(addVisitorAo.getEmployeeId());
		saveSmtVisitor.setPromoterBadge(staffBadge);
		saveSmtVisitor.setCertNo(addVisitorAo.getCertNo());
		saveSmtVisitor.setVisitorFrontPhoto(addVisitorAo.getVisitorFrontPhoto());
		saveSmtVisitor.setVisitorBackPhoto(addVisitorAo.getVisitorBackPhoto());
		saveSmtVisitor.setRemark(addVisitorAo.getRemark());
		saveSmtVisitor.setCertType(addVisitorAo.getCertType());
		return saveSmtVisitor;
	}

	/**
	 * 转化Vo
	 *
	 * @param addVisitorAo addVisitorAo
	 * @param staffBadge   staffBadge
	 * @return
	 */
	private SaveSmtVisitorDTO createRemoteBean(AddVisitorAo addVisitorAo, String staffBadge) {
		SaveSmtVisitorDTO saveSmtVisitor = new SaveSmtVisitorDTO();
		saveSmtVisitor.setReceptionistBadge(addVisitorAo.getEmployeeId());
		saveSmtVisitor.setVisitorName(addVisitorAo.getVisitorName());
		saveSmtVisitor.setVisitorPhoto(addVisitorAo.getVisitorPhoto());
		saveSmtVisitor.setVisitorPhone(addVisitorAo.getVisitorMobile());
		saveSmtVisitor.setCompany(addVisitorAo.getVisitorCompany());
		saveSmtVisitor.setCause(Integer.parseInt(addVisitorAo.getVisitReasonCode()));
		saveSmtVisitor.setVehiclePlate(addVisitorAo.getPlateNumber());
		saveSmtVisitor.setStartTime(addVisitorAo.getStartTime());
		saveSmtVisitor.setEndTime(addVisitorAo.getEndTime());
		saveSmtVisitor.setReceptionistName(addVisitorAo.getEmployeeName());
		saveSmtVisitor.setReceptionistPhone(addVisitorAo.getEmployeeMobile());
		saveSmtVisitor.setReceptionistBadge(addVisitorAo.getEmployeeId());
		saveSmtVisitor.setPromoterBadge(staffBadge);
		saveSmtVisitor.setCertNo(addVisitorAo.getCertNo());
		saveSmtVisitor.setCertType(addVisitorAo.getCertType());
		saveSmtVisitor.setVisitorBackPhoto(addVisitorAo.getVisitorBackPhoto());
		saveSmtVisitor.setVisitorFrontPhoto(addVisitorAo.getVisitorFrontPhoto());
		saveSmtVisitor.setRemark(addVisitorAo.getRemark());
		saveSmtVisitor.setParkId(addVisitorAo.getParkId());
		return saveSmtVisitor;
	}

	/**
	 * 访客的审核
	 */
	@Override
	public Boolean approveVisitorByVisitId(ApproveVisitorAo approveVisitorAo) {
		//存入相应的数据值
		SmtVisitorDTO smtVisitor = new SmtVisitorDTO();
		smtVisitor.setId(Long.valueOf(approveVisitorAo.getVisitId()));
		smtVisitor.setStatus(approveVisitorAo.getApproveVisitState());
		smtVisitor.setRemark(approveVisitorAo.getRefuseDes());
		if (!StringUtils.isEmpty(approveVisitorAo.getStartTime())) {
			smtVisitor.setStartTime(DateUtils.parse(approveVisitorAo.getStartTime()));
		}
		if (!StringUtils.isEmpty(approveVisitorAo.getEndTime())) {
			smtVisitor.setEndTime(DateUtils.parse(approveVisitorAo.getEndTime()));
		}
		String staffBadge = SecurityUtils.getUser().getUsername();
		smtVisitor.setReceptionistBadge(staffBadge);
		//调用远程定位访客审核接口
		Result<Boolean> b = remoteVisitorService.updateVisitorStatus(smtVisitor, SecurityConstants.FROM_IN);
		log.info("审核接口返回结果{}，{}", b, b.data());
		return Boolean.TRUE;
	}

	/**
	 * 获取未审批的个数
	 */
	@Override
	public VisitorCountVo getToApprovalCount() {
		// 获取预约人的员工号
		String staffBadge = SecurityUtils.getUser().getUsername();
		if (StringUtils.isEmpty(staffBadge)) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_PROMOTERBADGE_NULL.getMessage());
		}
		Result result = remoteVisitorService.searchAppVisitorCount(staffBadge, SecurityConstants.FROM_IN);
		if (result.isSuccess()) {
			VisitorCountVo visitorCountVo = new VisitorCountVo();
			visitorCountVo.setToApprovalCount(result.getData().toString());
			return visitorCountVo;
		}
		throw new TCEException(result.getMessage());

	}

	/**
	 * 判断人脸
	 */
	@Override
	public PhotoVisitorVo checkFace(CheckFaceAo checkFaceAo, String capability, String draftId) {
		requireVisitorActionOrAuthenticatedEmployee(capability, draftId, VisitorActionCapabilityAction.FACE_UPLOAD,
				checkFaceAo == null ? null : sha256(checkFaceAo.getVisitorPhoto()));
		PhotoVisitorVo photoVisitorVo = new PhotoVisitorVo();
		if (StringUtils.isNotEmpty(checkFaceAo.getVisitorPhoto())) {
			//已做了图片人脸剪裁，无需压缩
/*			//进行图片压缩
			byte[] bytes = Base64.decodeBase64(checkFaceAo.getVisitorPhoto());
			log.info("【图片压缩】 图片原大小={}kb", bytes.length / 1024);
			while (bytes.length > 200 * 1024) {
				bytes = compressPicForScale(bytes, (long) ((bytes.length / 1024) * 0.95));
			}
			log.info("【图片压缩】 压缩后大小={}kb", bytes.length / 1024);
			String encodePhoto = Base64.encodeBase64String(bytes);*/
			Result<?> result = new Result<>();
			if(Objects.isNull(checkFaceAo.getPhotoType())) {
				result = remoteSmtImageService.saveImage(SaveImageReqDto.builder()
								.base64String(checkFaceAo.getVisitorPhoto())
								.imageType(SmtImageEnum.TYPE_VISITOR_FACE.getCode())
								.build(),
						SecurityConstants.FROM_IN);
			}else {
				result = remoteSmtImageService.saveImage(SaveImageReqDto.builder()
								.base64String(checkFaceAo.getVisitorPhoto())
								.imageType(checkFaceAo.getPhotoType())
								.build(),
						SecurityConstants.FROM_IN);
			}
			//判断是否成功
			if (result.isSuccess()) {
				photoVisitorVo.setPhotoId(result.getData().toString());
				return photoVisitorVo;
			} else {
				throw new TCEException(ExceptionTypeEnum.VISITOR_PHOTO_ERROR.getMessage());
			}
		} else {
			throw new TCEException(ExceptionTypeEnum.VISITOR_PHOTO_NULL.getMessage());
		}
	}


	/**
	 * 获取图片base64
	 */
	@Override
	public PhotoBaseVisitorVo getFace(PhotoVisitorVo photoVisitorVo) {
		PhotoBaseVisitorVo photoBaseVisitorVo = new PhotoBaseVisitorVo();
		if (!StringUtils.isEmpty(photoVisitorVo.getPhotoId())) {
			//判断是否成功
			photoBaseVisitorVo.setPhoto(appCommService.buildHqImageUrl(photoVisitorVo.getPhotoId()));
			return photoBaseVisitorVo;
		} else {
			throw new TCEException(ExceptionTypeEnum.VISITOR_PHOTO_ID_EMPTY.getMessage());
		}
	}

	@Override
	public Boolean addCheck(AddVisitorAo addVisitorAo) {
		// TODO Auto-generated method stub

		OcrIdCardDto frontInfo = ocrService.readIdCardFontImg(addVisitorAo.getVisitorFrontPhoto());
		log.info("访客身份证正面信息识别完成");

		String regex = "\\d{15}(\\d{2}[0-9xX])?";

		if (Objects.isNull(frontInfo)) {
			log.error("未识别到身份证正面信息");
			throw new TCEException("识别身份证正面失败，请正对拍摄");
		}
		if (Objects.isNull(frontInfo.getName())) {
			log.error("未识别到姓名信息");
			throw new TCEException("识别身份证正面失败，请正对拍摄");
		}
		if (!frontInfo.getIdentityCard().matches(regex)) {
			log.error("身份证号码格式不正确");
			throw new TCEException("识别身份证正面失败，请正对拍摄");
		}
		//读取身份证背面信息
		OcrIdCardDto backInfo = ocrService.readIdCardBackImg(addVisitorAo.getVisitorBackPhoto());
		log.info("访客身份证反面信息识别完成");
		//, JSONUtil.toJsonStr(backInfo)
		//if(Objects.isNull(backInfo) ||Objects.isNull(backInfo.getValidityDate()) )
		//{
		//throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "识别身份证背面失败，请正对拍摄");
		//}
		return true;
	}


	/**
	 * 根据指定大小压缩图片
	 *
	 * @param imageBytes  源图片字节数组
	 * @param desFileSize 指定图片大小，单位kb
	 * @return 压缩质量后的图片字节数组
	 */
	public static byte[] compressPicForScale(byte[] imageBytes, long desFileSize) {
		if (imageBytes == null || imageBytes.length <= 0 || imageBytes.length < desFileSize * 1024) {
			return imageBytes;
		}
		long srcSize = imageBytes.length;
		double accuracy = getAccuracy(srcSize / 1024);
		try {
			while (imageBytes.length > desFileSize * 1024) {
				ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream(imageBytes.length);
				Thumbnails.of(inputStream)
						.scale(accuracy)
						.outputQuality(accuracy)
						.toOutputStream(outputStream);
				imageBytes = outputStream.toByteArray();
			}

		} catch (Exception e) {
			log.error("【图片压缩】msg=图片压缩失败!", e);
		}
		return imageBytes;
	}

	/**
	 * 自动调节精度(经验数值)
	 *
	 * @param size 源图片大小
	 * @return 图片压缩质量比
	 */
	private static double getAccuracy(long size) {
		double accuracy;
		accuracy = 0.85;
		return accuracy;
	}

	@Override
	public Result<?> checkBlackVisitor(AddVisitorAo addVisitorAo, String capability, String draftId) {
		requireVisitorActionOrAuthenticatedEmployee(capability, draftId, VisitorActionCapabilityAction.BLACKLIST_CHECK,
				blacklistPayloadHash(addVisitorAo));
		SmtVisitorDTO smtVisitor = new SmtVisitorDTO();
		smtVisitor.setVisitorName(addVisitorAo.getVisitorName());
		smtVisitor.setCertNo(normalizeCertNo(addVisitorAo.getCertNo()));
		smtVisitor.setParkId(addVisitorAo.getParkId());
		return remoteVisitorService.checkVisitorBlacklist(smtVisitor, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, VISITOR_BLACKLIST_PURPOSE);
	}

	/**
	 * 网关精确放行的两条访客路由仍必须在 App 层二次收口。匿名请求只有在 Platform
	 * 按草稿、动作和图片摘要原子消费 capability 后，才能进入原有存图或黑名单服务。
	 */
	private void requireVisitorActionOrAuthenticatedEmployee(String capability, String draftId,
			VisitorActionCapabilityAction action, String payloadHash) {
		boolean hasCapability = StringUtils.isNotBlank(capability);
		boolean hasDraftId = StringUtils.isNotBlank(draftId);
		if (hasCapability || hasDraftId) {
			if (!hasCapability || !hasDraftId) {
				throw visitorActionDenied();
			}
			VisitorActionCapabilityConsumeReqDTO request = new VisitorActionCapabilityConsumeReqDTO();
			request.setCapability(capability);
			request.setDraftId(draftId);
			request.setAction(action);
			request.setPayloadHash(payloadHash);
			Result<Boolean> consumed = remoteVisitorService.consumeVisitorActionCapability(request,
					SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			if (consumed == null || !consumed.isSuccess() || !Boolean.TRUE.equals(consumed.getData())) {
				log.warn("访客动作 capability 消费失败 action={}", action);
				throw visitorActionDenied();
			}
			return;
		}
		// 员工端仅保留已登录的人脸上传兼容路径；黑名单查询始终要求访客 capability，防止员工身份被用于枚举他人证件信息。
		if (action != VisitorActionCapabilityAction.FACE_UPLOAD || !hasAuthenticatedEmployee()) {
			throw visitorActionDenied();
		}
	}

	/** 历史已登录客户端保留原业务行为，但必须有真实员工主体，不能用匿名或 client token 绕过。 */
	private boolean hasAuthenticatedEmployee() {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		SmartUser user = SecurityUtils.getUser(authentication);
		return user != null && StringUtils.isNotBlank(user.getUsername());
	}

	private String sha256(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		try {
			byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			StringBuilder result = new StringBuilder(digest.length * 2);
			for (byte current : digest) {
				result.append(String.format("%02x", current));
			}
			return result.toString();
		} catch (java.security.NoSuchAlgorithmException exception) {
			throw visitorActionDenied();
		}
	}

	/**
	 * 黑名单 capability 必须绑定实际查询身份，不能只绑定草稿。该规范与 Smart H5 完全一致：
	 * 姓名、证件号移除所有空白，证件号转大写，再以竖线和园区编号组成摘要原文。
	 */
	private String blacklistPayloadHash(AddVisitorAo request) {
		if (request == null || request.getParkId() == null || StringUtils.isBlank(request.getVisitorName())
				|| StringUtils.isBlank(request.getCertNo())) {
			return null;
		}
		String visitorName = request.getVisitorName().replaceAll("\\s+", "");
		String certNo = normalizeCertNo(request.getCertNo());
		return sha256(visitorName + "|" + certNo + "|" + request.getParkId());
	}

	/** capability 摘要与实际查询必须使用同一证件号规范，避免空白或小写 X 绕过黑名单。 */
	private String normalizeCertNo(String certNo) {
		return certNo == null ? null : certNo.replaceAll("\\s+", "").toUpperCase(java.util.Locale.ROOT);
	}

	/** 缺票、错票、过期和重放统一以 403 失败，不暴露图片存储或黑名单查询状态。 */
	private org.springframework.security.access.AccessDeniedException visitorActionDenied() {
		return new org.springframework.security.access.AccessDeniedException("访客操作授权已失效，请重新进入申请流程");
	}

	@Override
	public Result getVisitorRefuseType() {
		return remoteVisitorService.getVisitorRefuseType();
	}

	@Override
	public List<SmtParkDTO> getPark() {
		Result<List<SmtParkDTO>> parkList = remoteParkInternalService.getAllParks(SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, "park-list");
		if (!parkList.isSuccess()) {
			throw new TCEException("获取园区失败");
		}
		return parkList.getData();
	}

	/**
	 * 根据员工号查询该员工所属园区
	 */
	@Override
	public Result<?> getParks(String staffBadge) {
		// TODO Auto-generated method stub
		if (StringUtils.isBlank(staffBadge)) {
			staffBadge = SecurityUtils.getUser().getUsername();
		}
		if (StringUtils.isEmpty(staffBadge)) {
			throw new TCEException(ExceptionTypeEnum.VISITOR_PROMOTERBADGE_NULL.getMessage());
		}
		Result staffPark = remoteStaffService.getStaffPark(staffBadge, SecurityConstants.FROM_IN);
		if (!staffPark.isSuccess()) {
			throw new TCEException("获取园区失败");
		}
		return staffPark;
	}

	@Override
	public Result<?> checkBlackVehicle(AddVisitorAo addVisitorAo) {
		// TODO Auto-generated method stub
		SmtVisitorDTO smtVisitor = new SmtVisitorDTO();
		smtVisitor.setVehiclePlate(addVisitorAo.getPlateNumber());
		smtVisitor.setParkId(addVisitorAo.getParkId());
		return remoteVisitorService.checkVehicleBlacklist(smtVisitor, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, VISITOR_BLACKLIST_PURPOSE);
	}

	@Override
	public IPage<VisitorListRespDTO> getVisitRecord(WechatVisitorRecordReqDTO wechatVisitorRecordReqDTO) {
		//获取微信openid
		WechatAccessTokenDto accessTokenByCode = wechatAuthService.getAccessTokenByCode(wechatVisitorRecordReqDTO.getCode());
		//查询绑定记录
		AppWechatBinding wechatBinding = wechatBindingService.getOne(new LambdaQueryWrapper<AppWechatBinding>().eq(AppWechatBinding::getOpenId, accessTokenByCode.getOpenId()));
		if (wechatBinding == null) {
			log.error("微信未绑定手机号");
			throw new TCEException("微信未绑定手机号");
		}
		//通过手机号远程查询预约记录
		Result<Page<VisitorListRespDTO>> result = remoteVisitorService.wechatGetVisitRecord(wechatVisitorRecordReqDTO.getCurrent(),
				wechatVisitorRecordReqDTO.getSize(),
				wechatBinding.getVisitPhone(), SecurityConstants.FROM_IN);
		log.info("微信公众号查询预约记录, VisitPhone={}, Result={}", wechatBinding.getVisitPhone(), result.isSuccess());
		if (!result.isSuccess()) {
			//远程查询失败
			throw new TCEException("查询预约记录失败");
		}
		return result.getData();
	}

	@Override
	public VisitorDetailVo getVisitRecordDetail(WechatVisitorRecordDetailReqDTO wechatVisitorRecordDetailReqDTO) {
		//获取微信openid
		WechatAccessTokenDto accessTokenByCode = wechatAuthService.getAccessTokenByCode(wechatVisitorRecordDetailReqDTO.getCode());
		//查询绑定记录
		AppWechatBinding wechatBinding = wechatBindingService.getOne(new LambdaQueryWrapper<AppWechatBinding>().eq(AppWechatBinding::getOpenId, accessTokenByCode.getOpenId()));
		if (wechatBinding == null) {
			log.error("微信未绑定手机号");
			throw new TCEException("微信未绑定手机号");
		}
		VisitorIdAo visitorIdAo = new VisitorIdAo();
		//查询预约详细记录
		visitorIdAo.setId(wechatVisitorRecordDetailReqDTO.getId());
		return getVisitorListDeatil(visitorIdAo);
	}

	@Override
	public VisitorDetailVo getVisitRecordDetailById(Long id) {
		//查询预约详细记录
		VisitorIdAo visitorIdAo = new VisitorIdAo();
		visitorIdAo.setId(id);
		return getVisitorListDeatil(visitorIdAo);
	}

	@Override
	public Boolean wechatVisitorAgain(WechatVisitorRecordDetailReqDTO wechatVisitorRecordDetailReqDTO) {

		//获取微信openid
		WechatAccessTokenDto accessTokenByCode = wechatAuthService.getAccessTokenByCode(wechatVisitorRecordDetailReqDTO.getCode());
		//查询绑定记录
		AppWechatBinding wechatBinding = wechatBindingService.getOne(new LambdaQueryWrapper<AppWechatBinding>().eq(AppWechatBinding::getOpenId, accessTokenByCode.getOpenId()));
		if (wechatBinding == null) {
			log.error("微信未绑定手机号");
			throw new TCEException("微信未绑定手机号");
		}

		Result<Boolean> result = remoteVisitorService.wechatVisitorAgain(VisitorAgainReqDTO.builder()
				.id(wechatVisitorRecordDetailReqDTO.getId())
				.startTime(wechatVisitorRecordDetailReqDTO.getStartTime())
				.endTime(wechatVisitorRecordDetailReqDTO.getEndTime())
				.build(), SecurityConstants.FROM_IN);
		if (!result.isSuccess()) {
			//再约一次失败
			log.error("再约一次失败,id=({})", wechatVisitorRecordDetailReqDTO.getId());
			throw new TCEException("再约一次失败");
		}
		return result.getData();
	}


}
