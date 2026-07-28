package com.tce.smart.platform.wrapper.manage;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.ehrview.AvaGetskyPayYSHRDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteAvaGetskyPayService;
import com.tce.smart.platform.api.dto.resp.manage.AttendanceSignDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.manage.AvaGetskyPayRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.manage.SmtAttendanceSign;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.AttendanceSignStatusEnum;
import com.tce.smart.tool.enums.NoticeTypeEnum;
import com.tce.smart.tool.enums.ObjectionStatusEnum;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: AttendanceSignDetailWrapper
 * @Author fushiping
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class AttendanceSignDetailWrapper extends BaseWrapper<SmtAttendanceSign, AttendanceSignDetailRespDTO> {
	@Autowired
	private final SmtStaffService staffService;

	@Autowired
	private final SmtImageService smtImageService;

	@Autowired
	private SmtParkBuService smtParkBuService;
	@Autowired
	private RemoteAvaGetskyPayService remoteAvaGetskyPayService;

	@Override
	protected AttendanceSignDetailRespDTO warp(SmtAttendanceSign smtAttendanceSign) throws IOException {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		AttendanceSignDetailRespDTO attendanceSign = BeanUtils.transform(AttendanceSignDetailRespDTO.class, smtAttendanceSign);
		SmtStaff staff = staffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, smtAttendanceSign.getBadge()));
		attendanceSign.setCompName(staff.getCompName());
		attendanceSign.setDepName(staff.getDepName());
		attendanceSign.setName(staff.getName());
		attendanceSign.setNoticeStatus(NoticeTypeEnum.desc(smtAttendanceSign.getNoticeStatus()));
		//attendanceSign.setIsObjectionDesc(ObjectionStatusEnum.desc(smtAttendanceSign.getIsObjection()));
		//设置园区
		List<SmtPark> parkList = smtParkBuService.getUserParkListByBu(Integer.parseInt(staff.getCompId()), parkIds);
		List<String> parkNames = parkList.stream().map(SmtPark::getParkName).collect(Collectors.toList());
		attendanceSign.setParkName(StringUtils.join(parkNames, SymbolConstants.COMMA));
		if (ObjectUtil.isNotNull(smtAttendanceSign.getSignImg())) {
			String imgBase64 = smtImageService.getImageBase64ByCode(smtAttendanceSign.getSignImg());
			attendanceSign.setSignImg(imgBase64);
		}
		attendanceSign.setSignStatusDesc(AttendanceSignStatusEnum.desc(smtAttendanceSign.getSignStatus()));
		if(Objects.nonNull(smtAttendanceSign.getSignDate())) {
			DateTimeFormatter simpleFormatter = DateTimeFormatter.ofPattern(SymbolConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM);
			attendanceSign.setSignDate(simpleFormatter.format(smtAttendanceSign.getSignDate()));
		}
		//考勤详情

		Result<AvaGetskyPayYSHRDTO> mothInfo = remoteAvaGetskyPayService.info(smtAttendanceSign.getBadge(),
				smtAttendanceSign.getCheckDate() + "-01 00:00:00", SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if(Objects.nonNull(mothInfo.getData())) {
			AvaGetskyPayRespDTO payRespDTO = BeanUtils.transform(AvaGetskyPayRespDTO.class, mothInfo.getData());
			attendanceSign.setAvaGetskyPayYSHRDTO(payRespDTO);
		}
		return attendanceSign;
	}
}
