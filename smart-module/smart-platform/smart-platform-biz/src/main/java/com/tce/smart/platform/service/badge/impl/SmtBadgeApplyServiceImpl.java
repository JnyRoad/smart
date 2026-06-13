package com.tce.smart.platform.service.badge.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.consume.resp.RsEmpRespDTO;
import com.tce.smart.data.api.dto.consume.resp.TxEmpCardRespDTO;
import com.tce.smart.data.api.dto.msg.req.BadgeAgreeMsgReqDTO;
import com.tce.smart.data.api.dto.msg.req.BadgeRefuseMsgReqDTO;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.data.api.feign.consume.RemoteTxEmpCardService;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.api.dto.req.badge.EditBadgeApplyReqDTO;
import com.tce.smart.platform.api.dto.req.badge.QueryApplyListReqDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeApplyExcelRespDTO;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.badge.SmtBadgeApply;
import com.tce.smart.platform.core.mapper.SmtBadgeApplyMapper;
import com.tce.smart.platform.service.IAppMsgPushService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.badge.SmtBadgeApplyService;
import com.tce.smart.platform.service.badge.SmtBadgeRecordService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 厂牌补领
 *
 * @author fushiping
 * @date 2020-07-07 11:47:58
 */
@Service
@Slf4j
public class SmtBadgeApplyServiceImpl extends ServiceImpl<SmtBadgeApplyMapper, SmtBadgeApply> implements SmtBadgeApplyService {

	@Autowired
	private RemoteTxEmpCardService txEmpCardService;
	@Autowired
	private RemoteRsEmpService rsEmpService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtBadgeRecordService smtBadgeRecordService;
	@Autowired
	private IAppMsgPushService appMsgPushService;
	@Autowired
	private RemoteSmsManageService remoteSmsManageService;

	@Override
	public IPage<SmtBadgeApply> getPage(Page page, QueryApplyListReqDTO reqDTO) {
		List<Integer> parkList = SecurityUtils.getUser().getParkIdList();
		return this.page(page, Wrappers.<SmtBadgeApply>query().lambda()
				.eq(Objects.nonNull(reqDTO.getBadge()), SmtBadgeApply::getBadge, reqDTO.getBadge())
				.eq(Objects.nonNull(reqDTO.getCompId()), SmtBadgeApply::getCompId, reqDTO.getCompId())
				.eq(Objects.nonNull(reqDTO.getDepId()), SmtBadgeApply::getDepId, reqDTO.getDepId())
				.eq(Objects.nonNull(reqDTO.getParkId()), SmtBadgeApply::getParkId, reqDTO.getParkId())
				.eq(Objects.nonNull(reqDTO.getState()), SmtBadgeApply::getState, reqDTO.getState())
				.ge(Objects.nonNull(reqDTO.getStartTime()), SmtBadgeApply::getCreateTime, reqDTO.getStartTime())
				.le(Objects.nonNull(reqDTO.getEndTime()), SmtBadgeApply::getCreateTime, reqDTO.getEndTime())
				.in(CollectionUtils.isNotEmpty(parkList), SmtBadgeApply::getParkId, parkList)
				.orderByDesc(SmtBadgeApply::getCreateTime));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveBadgeApply(EditBadgeApplyReqDTO reqDTO) {
		String badge = reqDTO.getStaffNo();
		SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(badge);
		if(Objects.isNull(smtStaff)) {
			throw new SmartException("员工信息关联失败");
		}
		//判断该员工是否存在厂牌
		checkIsExist(badge);
		//判断改员工是否存在正在申请中记录
		checkIsApply(badge);
		//新增厂牌补领记录
		SmtBadgeApply apply = this.insertApply(smtStaff, reqDTO);
		//新增操作记录
		return smtBadgeRecordService.insertRecord(badge, apply.getId(), BadgeOperaStatusEnum.APPLY.getCode());
	}

	@Override
	public Boolean updateBadgeApply(EditBadgeApplyReqDTO reqDTO) {
		if(Objects.nonNull(reqDTO)) {
			//修改厂牌补领信息
			SmtBadgeApply apply = BeanUtils.transform(SmtBadgeApply.class, reqDTO);
			this.updateById(apply);
			//短信发送
			this.sendMessage(reqDTO);
			//新增操作记录
			return smtBadgeRecordService.insertRecord(null, reqDTO.getId(), reqDTO.getState());
		}
		return false;
	}

	/**
	 * 发送短信
	 */
	private void sendMessage(EditBadgeApplyReqDTO reqDTO) {
		if(reqDTO.getState().equals(BadgeOperaStatusEnum.AGREE.getCode())) {
			SmtBadgeApply smtBadgeApply = this.getById(reqDTO.getId());
			SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(smtBadgeApply.getBadge());
			if(Objects.isNull(smtStaff)) {
				throw new SmartException("员工为空");
			}
			//APP消息推送
			AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
			appMsgPushDTO.setBadge(smtStaff.getBadge());
			appMsgPushDTO.setBussiessId(String.valueOf(smtBadgeApply.getId()));
			appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_PUSH_10503.getCode());
			appMsgPushService.pushAppMsg(appMsgPushDTO);

			BadgeAgreeMsgReqDTO msgReqDTO = new BadgeAgreeMsgReqDTO();
			msgReqDTO.setAdress(reqDTO.getAddress());
			msgReqDTO.setTempCode(SmsTemplateEnum.SMS_BADGE_10501.getCode());
			if(Objects.nonNull(smtStaff.getPhone())) {
				msgReqDTO.setNumber(smtStaff.getPhone());
				remoteSmsManageService.sendBadgeAgree(msgReqDTO);
			}
			log.error("工号为："+ smtStaff.getBadge() +"的员工手机号码为空，厂牌补领申请通过通知短信发送失败");
		}
		if(reqDTO.getState().equals(BadgeOperaStatusEnum.REFUSE.getCode())) {
			SmtBadgeApply smtBadgeApply = this.getById(reqDTO.getId());
			SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(smtBadgeApply.getBadge());
			if(Objects.isNull(smtStaff)) {
				throw new SmartException("员工为空");
			}
			//APP消息推送
			AppMsgPushDTO appMsgPushDTO = new AppMsgPushDTO();
			appMsgPushDTO.setBadge(smtStaff.getBadge());
			appMsgPushDTO.setBussiessId(String.valueOf(smtBadgeApply.getId()));
			appMsgPushDTO.setTemplateCode(SmsTemplateEnum.APP_PUSH_10504.getCode());
			appMsgPushService.pushAppMsg(appMsgPushDTO);

			BadgeRefuseMsgReqDTO msgReqDTO = new BadgeRefuseMsgReqDTO();
			msgReqDTO.setRefuseReason(reqDTO.getRefuseReason());
			msgReqDTO.setTempCode(SmsTemplateEnum.SMS_BADGE_10502.getCode());
			if(Objects.nonNull(smtStaff.getPhone())) {
				msgReqDTO.setNumber(smtStaff.getPhone());
				remoteSmsManageService.sendBadgeRefuse(msgReqDTO);
			}
			log.error("工号为："+ smtStaff.getBadge() +"的员工手机号码为空，厂牌补领申请拒绝通知短信发送失败");
		}
	}

	/**
	 * 判断员工之前的厂牌状态
	 * @param staffNo
	 * @return
	 */
	private Boolean checkIsExist(String staffNo) {

		Result<TxEmpCardRespDTO> txResult = txEmpCardService.getCard(staffNo, SecurityConstants.FROM_IN);
		if(Objects.isNull(txResult.getData())) {
			return true;
		}
		if(txResult.getData().getCardStatusID().equals(BadgeStatusEnum.NORMAL.getCode())) {
			throw new SmartException("关联厂牌状态正常，请先挂失在申请厂牌补领");
		}
		return true;
	}

	private Boolean checkIsApply(String staffNo) {
		SmtBadgeApply smtBadgeApply = this.getOne(Wrappers.<SmtBadgeApply>query().lambda()
				.eq(SmtBadgeApply::getBadge, staffNo)
				.lt(SmtBadgeApply::getState, BadgeOperaStatusEnum.CONFIRM.getCode()));
		if(Objects.nonNull(smtBadgeApply)) {
			throw new SmartException("已存在未完结厂牌补领申请");
		}
		return true;
	}

	/**
	 * 新增厂牌挂失记录
	 * @param smtStaff
	 * @param reqDTO
	 * @return
	 */
	private SmtBadgeApply insertApply(SmtStaff smtStaff, EditBadgeApplyReqDTO reqDTO) {
		SmtBadgeApply apply = BeanUtils.transform(SmtBadgeApply.class, smtStaff);
		if(reqDTO.getReason().equals(BadgeApplyReasonEnum.OTHERS.getCode())
				&& StringUtils.isBlank(reqDTO.getRemark())) {
			throw new SmartException("若因其他原因挂失厂牌请填写备注信息");
		}
		apply.setParkId(reqDTO.getParkId());
		//获得园区信息
		SmtPark smtPark = smtParkService.getById(reqDTO.getParkId());
		if(Objects.nonNull(smtPark)) {
			apply.setParkName(smtPark.getParkName());
		}
		apply.setPrice(reqDTO.getPrice());
		apply.setState(BadgeOperaStatusEnum.APPLY.getCode());
		apply.setReason(reqDTO.getReason());
		apply.setCreateTime(LocalDateTime.now());
		apply.setId(null);
		apply.setRemark(reqDTO.getRemark());
		this.save(apply);
		return apply;
	}

	@Override
	public ResponseEntity<byte[]> downLoadExcel(SmtBadgeApply SmtBadgeApply) {
		//获得导出数据
		List<SmtBadgeApply> list = this.list(Wrappers.query(SmtBadgeApply));
		if(CollectionUtils.isEmpty(list)) {
			throw new SmartException("暂无补领记录");
		}
		List<BadgeApplyExcelRespDTO> data = BeanUtils.batchTransform(BadgeApplyExcelRespDTO.class, list);
		ResponseEntity<byte[]> responseEntity;
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), BadgeApplyExcelRespDTO.class, data)){
			//文件名
			String fileName = ExportTypeEnum.BADGE_REPLY.getDesc() + SymbolConstants.FULL_POINT + ExportTypeEnum.BADGE_REPLY.getFileSuffix();
			responseEntity = IOUtils.getExcelResp(fileName, workbook);
		}catch (IOException e){
			log.error("excel导出异常", e);
			throw new SmartException(ExceptionEnum.UNKNOWN.getCode(), "excel导出异常");
		}
		return responseEntity;
	}
}
