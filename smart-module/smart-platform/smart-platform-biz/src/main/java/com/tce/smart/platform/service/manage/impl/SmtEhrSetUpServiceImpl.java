package com.tce.smart.platform.service.manage.impl;

import cn.hutool.crypto.asymmetric.Sign;
import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.data.api.dto.msg.req.SignMsgReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.api.dto.req.manage.EditEhrSetUpReqDTO;
import com.tce.smart.platform.core.dto.QueryAttendanceSignDTO;
import com.tce.smart.platform.core.dto.WageSignDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtWageSign;
import com.tce.smart.platform.core.entity.manage.SmtAttendanceSign;
import com.tce.smart.platform.core.entity.manage.SmtEhrSetUp;
import com.tce.smart.platform.core.mapper.SmtEhrSetUpMapper;
import com.tce.smart.platform.core.vo.AttendanceSignVO;
import com.tce.smart.platform.core.vo.WageSignVO;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtWageSignService;
import com.tce.smart.platform.service.manage.SmtAttendanceSignService;
import com.tce.smart.platform.service.manage.SmtEhrSetUpService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.RegexUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.OverridesAttribute;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2020-07-27 10:45:36
 */
@Service
public class SmtEhrSetUpServiceImpl extends ServiceImpl<SmtEhrSetUpMapper, SmtEhrSetUp> implements SmtEhrSetUpService {

	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtWageSignService smtWageSignService;
	@Autowired
	private SmtAttendanceSignService smtAttendanceSignService;
	@Autowired
	private RemoteSmsManageService remoteSmsManageService;

	@Override
	public Boolean edit(EditEhrSetUpReqDTO reqDTO) {
		this.checkRepeat(reqDTO);
		SmtEhrSetUp smtEhrSetUp = BeanUtils.transform(SmtEhrSetUp.class, reqDTO);
		if (Objects.nonNull(reqDTO.getParkId())) {
			SmtPark smtPark = smtParkService.getById(reqDTO.getParkId());
			if (Objects.nonNull(smtPark)) {
				smtEhrSetUp.setParkName(smtPark.getParkName());
				smtEhrSetUp.setCreateTime(LocalDateTime.now());
			}
		}
		return this.saveOrUpdate(smtEhrSetUp);
	}

	@Override
	public void sendMessage() {
		LocalDateTime now = LocalDateTime.now();
		Integer day = now.getDayOfMonth();
		DateTimeFormatter simpleFormatter = DateTimeFormatter.ofPattern(SymbolConstants.DATE_FORMAT_YYYY_MM);
		String noticeDate = simpleFormatter.format(now.plusMonths(-1));
		List<SmtEhrSetUp> setUpOfWage = this.list();
		for (SmtEhrSetUp wageNotice : setUpOfWage) {
			//判断短信设置是否关闭
			if (wageNotice.getIsMessage().equals(IsMessageEnum.OFF.getCode())) {
				continue;
			}
			//判断当天是否等于提醒日期
			if (day.equals(wageNotice.getDeadline())) {
				if (wageNotice.getSetType().equals(EhrSetUpTypeEnum.ATTENDANCE_SIGN.getCode())) {
					//发送考勤确认提醒
					this.sendAttendanceSign(noticeDate, wageNotice.getParkId());
				}
				if (wageNotice.getSetType().equals(EhrSetUpTypeEnum.WAGE_SIGN.getCode())) {
					//发送工资签收提醒
					this.sendWageSign(noticeDate, wageNotice.getParkId());
				}

			}
		}
	}

	/**
	 * 自动确认定时任务
	 */
	@Override
	public void autoSignTask() {
		List<SmtEhrSetUp> smtEhrSetUps = this.list();
		if(CollectionUtils.isNotEmpty(smtEhrSetUps)) {
			LocalDate now = LocalDate.now();
			//获得考勤确认月份或工资签收月份
			for (SmtEhrSetUp set : smtEhrSetUps) {
				//判断短信设置是否关闭
				if (set.getIsMessage().equals(IsMessageEnum.OFF.getCode())) {
					continue;
				}
				//获得发送短信提示时间
				LocalDate senMsg = now.plusDays(-set.getDelayLine());
				//判断短信发送时间等于当前时间减去自动确认延迟时间
				if(senMsg.getDayOfMonth() == set.getDeadline()) {
					DateTimeFormatter simpleFormatter = DateTimeFormatter.ofPattern(SymbolConstants.DATE_FORMAT_YYYY_MM);
					String noticeDate = simpleFormatter.format(senMsg.plusMonths(-1));
					if (set.getSetType().equals(EhrSetUpTypeEnum.ATTENDANCE_SIGN.getCode())) {
						//自动确认考勤信息
						this.autoConfirmAttendance(noticeDate, set.getParkId());
					}
					if (set.getSetType().equals(EhrSetUpTypeEnum.WAGE_SIGN.getCode())) {
						//自动签收工资信息
						this.autoConfirmWage(noticeDate, set.getParkId());
					}
				}
			}
		}
	}

	/**
	 * 发送工资签单短信提醒
	 */
	private void sendWageSign(String noticeDate, Integer parkId) {
		WageSignDTO dto = new WageSignDTO();
		dto.setParkId(parkId);
		dto.setWageDate(noticeDate);
		dto.setSignStatus(WageSignStatusEnum.NOT_SIGN.getCode());
		List<WageSignVO> voList = smtWageSignService.getMegInfoList(dto);
		if (CollectionUtils.isNotEmpty(voList)) {
			List<WageSignVO> vos= voList.stream().filter(vo -> vo.getPhone() != null
					&& vo.getPhone().matches(RegexUtils.RE_MOBILE_B)).collect(Collectors.toList());
			List<SignMsgReqDTO> signMsgReqDTOs = new ArrayList<>();
			vos.forEach( vo -> {
				SignMsgReqDTO signMsgReqDTO  = new SignMsgReqDTO();
				signMsgReqDTO.setNumbers(vo.getPhone());
				signMsgReqDTO.setTempCode(SmsTemplateEnum.SMS_SIGN_10401.getCode());
				signMsgReqDTO.setPersonName(vo.getName());
				signMsgReqDTOs.add(signMsgReqDTO);
			});
			Result result = remoteSmsManageService.sendWageSign(signMsgReqDTOs);
			//注意：此处必须用 isSuccess()（判断依据是 CommonConstants.SUCCESS=0）；
			//SuccessEnum 是另一套独立编码(SUCCESS=1)，与 Result 的编码含义相反，混用会导致成功/失败判断颠倒
			if (result.isSuccess()) {
				List<Integer> ids = vos.stream().map(WageSignVO::getId).collect(Collectors.toList());
				smtWageSignService.updateNotice(ids);
			}
		}
	}

	/**
	 * 发送考勤汇总确认短信提醒
	 */
	private void sendAttendanceSign(String noticeDate, Integer parkId) {
		QueryAttendanceSignDTO dto = new QueryAttendanceSignDTO();
		dto.setParkId(parkId);
		dto.setCheckDate(noticeDate);
		dto.setSignStatus(AttendanceSignStatusEnum.NOT_SIGN.getCode());
		List<AttendanceSignVO> voList = smtAttendanceSignService.getMegInfoList(dto);
		if (CollectionUtils.isNotEmpty(voList)) {
			List<AttendanceSignVO> vos= voList.stream().filter(vo -> vo.getPhone() != null && vo.getPhone().matches(RegexUtils.RE_MOBILE_B)).collect(Collectors.toList());
			List<SignMsgReqDTO> signMsgReqDTOs = new ArrayList<>();
			vos.forEach( vo -> {
				SignMsgReqDTO signMsgReqDTO  = new SignMsgReqDTO();
				signMsgReqDTO.setNumbers(vo.getPhone());
				signMsgReqDTO.setTempCode(SmsTemplateEnum.SMS_SIGN_10402.getCode());
				signMsgReqDTO.setPersonName(vo.getName());
				signMsgReqDTOs.add(signMsgReqDTO);
			});
			Result result = remoteSmsManageService.sendAttendanceSign(signMsgReqDTOs);
			//注意：此处必须用 isSuccess()（判断依据是 CommonConstants.SUCCESS=0）；
			//SuccessEnum 是另一套独立编码(SUCCESS=1)，与 Result 的编码含义相反，混用会导致成功/失败判断颠倒
			if (result.isSuccess()) {
				List<Long> ids = vos.stream().map(AttendanceSignVO::getId).collect(Collectors.toList());
				smtAttendanceSignService.updateNotice(ids);
			}
		}
	}

	/**
	 * 自动确认考勤汇总
	 * @param noticeDate
	 * @param parkId
	 * @return
	 */
	private Boolean autoConfirmAttendance(String noticeDate, Integer parkId) {
		QueryAttendanceSignDTO dto = new QueryAttendanceSignDTO();
		dto.setParkId(parkId);
		dto.setCheckDate(noticeDate);
		dto.setSignStatus(AttendanceSignStatusEnum.NOT_SIGN.getCode());
		return smtAttendanceSignService.autoConfirm(dto);
	}

	/**
	 * 自动确认工资签单
	 * @param noticeDate
	 * @param parkId
	 * @return
	 */
	private Boolean autoConfirmWage(String noticeDate, Integer parkId) {
		WageSignDTO dto = new WageSignDTO();
		dto.setParkId(parkId);
		dto.setWageDate(noticeDate);
		dto.setSignStatus(WageSignStatusEnum.NOT_SIGN.getCode());
		return smtWageSignService.autoConfirm(dto);
	}

	/**
	 * 检查配置信息是否已存在
	 *
	 * @param reqDTO
	 */
	private void checkRepeat(EditEhrSetUpReqDTO reqDTO) {
		SmtEhrSetUp smtEhrSetUp = this.getOne(Wrappers.<SmtEhrSetUp>query().lambda()
				.eq(SmtEhrSetUp::getParkId, reqDTO.getParkId())
				.eq(SmtEhrSetUp::getSetType, reqDTO.getSetType()));
		if (Objects.nonNull(smtEhrSetUp)) {
			if (Objects.nonNull(reqDTO.getId()) && reqDTO.getId().equals(smtEhrSetUp.getId())) {
				return;
			}
			throw new SmartException("该园区已存在配置信息");
		}
    }
}
