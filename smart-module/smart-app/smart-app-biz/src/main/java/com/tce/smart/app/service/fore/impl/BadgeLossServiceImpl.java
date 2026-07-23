package com.tce.smart.app.service.fore.impl;

import com.tce.smart.app.service.fore.BadgeLossService;
import com.tce.smart.app.vo.fore.BadgeInfoVo;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.consume.resp.TxEmpCardRespDTO;
import com.tce.smart.data.api.feign.consume.RemoteTxEmpCardService;
import com.tce.smart.platform.api.dto.resp.InternalStaffBindingRespDTO;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import com.tce.smart.platform.api.feign.badge.RemoteBadgeLossService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 厂牌挂失服务实现类
 *
 * @author fushiping
 * @date 2020/7/9 18:20
 **/
@Service
public class BadgeLossServiceImpl implements BadgeLossService {

	@Autowired
	private RemoteTxEmpCardService txEmpCardService;
	@Autowired
	private RemoteBadgeLossService remoteBadgeLossService;
	@Autowired
	private RemoteStaffInternalService internalStaffService;

	@Override
	public BadgeInfoVo getBadgeInfo() {
		String badge = SecurityUtils.getUser().getUsername();
		Result<InternalStaffBindingRespDTO> staff = internalStaffService.getBindingStaff(badge,
				SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, "badge-loss");
		if (!staff.isSuccess() || Objects.isNull(staff.getData())) {
			throw new SmartException("员工信息关联失败！");
		}
		BadgeInfoVo vo = new BadgeInfoVo();
		Result<TxEmpCardRespDTO> txResult = txEmpCardService.getCard(badge, SecurityConstants.FROM_IN);
		TxEmpCardRespDTO txEmpCardRespDTO = txResult.getData();
		if (Objects.isNull(txResult.getData())) {
			throw new SmartException("尚未找到厂牌关联信息！");
		}
		vo.setBadgeNo(txEmpCardRespDTO.getEmpSysID());
		vo.setBadgeStatus(txEmpCardRespDTO.getCardStatusID());
		vo.setStaffName(staff.getData().getName());
		vo.setStaffNo(badge);
		vo.setCardId(txEmpCardRespDTO.getCardID());
		return vo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean badgeLoss(Long cardId, Integer parkId) {
		//裕同c6服务厂牌挂失
		Result<Boolean> result = txEmpCardService.cardLoss(cardId, SecurityConstants.FROM_IN);
		if (Objects.isNull(result.getData())) {
			throw new SmartException("挂失失败，裕同C6服务连接失败！");
		}
		if (result.getData().equals(Boolean.FALSE)) {
			throw new SmartException("挂失失败，厂牌非正常状态！");
		}
		//储存本地记录
		String staffNo = SecurityUtils.getUser().getUsername();
		Result<Boolean> saveResult = remoteBadgeLossService.save(parkId, staffNo, SecurityConstants.FROM_IN);
		if (!saveResult.getCode().equals(CommonConstants.SUCCESS)) {
			throw new SmartException(saveResult.getMsg());
		}
		return true;
	}

}
