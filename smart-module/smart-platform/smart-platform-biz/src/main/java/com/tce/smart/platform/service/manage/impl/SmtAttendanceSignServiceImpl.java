package com.tce.smart.platform.service.manage.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.tce.smart.common.core.constant.enums.SuccessEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.SignMsgReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.api.dto.req.manage.AttendanceSignReqDTO;
import com.tce.smart.platform.api.dto.req.manage.QueryAttendanceSignReqDTO;
import com.tce.smart.platform.api.dto.resp.manage.AttendanceSignRespDTO;
import com.tce.smart.platform.core.dto.QueryAttendanceSignDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.manage.SmtAttendanceSign;
import com.tce.smart.platform.core.mapper.SmtAttendanceSignMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.AttendanceSignVO;
import com.tce.smart.platform.core.vo.WageSignVO;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.manage.SmtAttendanceSignService;
import com.tce.smart.tool.constant.NumberConstants;
import com.tce.smart.tool.constant.ResultStatusConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.constant.VehicleConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.enums.AttendanceSignStatusEnum;
import com.tce.smart.tool.util.RegexUtils;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2020-07-27 10:45:43
 */
@Service
public class SmtAttendanceSignServiceImpl extends ServiceImpl<SmtAttendanceSignMapper, SmtAttendanceSign> implements SmtAttendanceSignService {

	@Autowired
	private SmtImageService smtImageService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtParkBuService smtParkBuService;
	@Autowired
	private RemoteSmsManageService remoteSmsManageService;

	@Override
	public IPage<AttendanceSignVO> getPage(Page page, QueryAttendanceSignReqDTO reqDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		reqDTO.setParkIds(parkIds);
		QueryAttendanceSignDTO dto = BeanUtils.transform(QueryAttendanceSignDTO.class, reqDTO);
		IPage<AttendanceSignVO> pageResult = this.baseMapper.queryPage(page, dto);
		List<AttendanceSignVO> count = this.baseMapper.getCount(dto);
		Integer countNotSign = count.stream().filter(s -> s.getSignStatus()
				.equals(AttendanceSignStatusEnum.NOT_SIGN.getCode())).collect(Collectors.toList()).size();
		Integer countSign = count.size() - countNotSign;
		List<AttendanceSignVO> records = pageResult.getRecords();
		for (AttendanceSignVO vo : records) {
			vo.setCountNotSign(countNotSign);
			vo.setCountSign(countSign);
			//vo.setIsObjectionDesc(ObjectionStatusEnum.desc(vo.getIsObjection()));
			vo.setSignStatusDesc(AttendanceSignStatusEnum.desc(vo.getSignStatus()));
			SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, vo.getBadge()));
			if (Objects.nonNull(selectOne)) {
				List<SmtPark> parkList = smtParkBuService.getUserParkListByBu(Integer.parseInt(selectOne.getCompId()), parkIds);
				List<String> parkNames = parkList.stream().map(SmtPark::getParkName).collect(Collectors.toList());
				vo.setParkName(StringUtils.join(parkNames, SymbolConstants.COMMA));
			}
		}
		return pageResult;
	}

	@Override
	public SmtAttendanceSign getByBadge(String badge, String checkDate) {
		SmtAttendanceSign smtAttendanceSign = this.getOne(Wrappers.<SmtAttendanceSign>query().lambda().eq(SmtAttendanceSign::getBadge,badge).eq(SmtAttendanceSign::getCheckDate, checkDate));
		if (ObjectUtil.isNotNull(smtAttendanceSign.getSignImg())) {
			String imgBase64 = smtImageService.getImageBase64ByCode(smtAttendanceSign.getSignImg());
			smtAttendanceSign.setSignImg(imgBase64);
		}
		return smtAttendanceSign;
	}

	@Override
	public Boolean updateToSign(AttendanceSignReqDTO reqDto) {
		String badge = SecurityUtils.getUser().getUsername();
		SmtAttendanceSign attendanceSign = this.getByBadge(badge, reqDto.getAttendanceDate());
		if(Objects.isNull(attendanceSign)) {
			throw new SmartException("上月工资签单数据未生成，请联系管理员");
		}
		if (attendanceSign.getSignStatus().equals(WageSignStatusEnum.SIGN.getCode())) {
			throw new SmartException("员工已经签单");
		}
		String driverLicenseId = reqDto.getSignImg().replaceAll(VehicleConstants.BASE64_REGEX, "");
		String blobResult = smtImageService.saveImage(0, driverLicenseId, SmtImageEnum.TYPE_SALAR_SIGN.getCode());
		if (StringUtil.isNullOrEmpty(blobResult)) {
			throw new SmartException("签名信息获取失败");
		}
		attendanceSign.setSignImg(blobResult);
		attendanceSign.setSignDate(LocalDateTime.now());
		attendanceSign.setSignStatus(WageSignStatusEnum.SIGN.getCode());
		return this.updateById(attendanceSign);

	}

	@Override
	public Integer countNotSign() {
		String badge = SecurityUtils.getUser().getUsername();
		return this.count(Wrappers.<SmtAttendanceSign>query().lambda().eq(SmtAttendanceSign::getBadge, badge)
				.eq(SmtAttendanceSign::getSignStatus, AttendanceSignStatusEnum.NOT_SIGN.getCode()));
	}

	public boolean getSign(SmtAttendanceSign smtAttendanceSign) {
		SmtAttendanceSign sign = this.getOne(Wrappers.<SmtAttendanceSign>query().lambda().eq(SmtAttendanceSign::getBadge, smtAttendanceSign.getBadge())
				.eq(SmtAttendanceSign::getCheckDate, smtAttendanceSign.getCheckDate()));
		if (Objects.nonNull(sign)) {
			return AttendanceSignStatusEnum.status(sign.getSignStatus());
		}
		return false;
	}

	@Override
	public String getSignStatus(String checkDate) {
		String badge = SecurityUtils.getUser().getUsername();
		SmtAttendanceSign attendanceSign = this.getOne(Wrappers.<SmtAttendanceSign>query().lambda().eq(SmtAttendanceSign::getBadge, badge)
				.eq(SmtAttendanceSign::getCheckDate, checkDate));
		if(Objects.nonNull(attendanceSign)) {
			return AttendanceSignStatusEnum.desc(attendanceSign.getSignStatus());
		}
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean syncStaff() {
		DateTimeFormatter simpleFormatter = DateTimeFormatter.ofPattern(SymbolConstants.DATE_FORMAT_YYYY_MM);
		String checkDate = simpleFormatter.format(LocalDateTime.now().plusMonths(-1));
		List<SmtAttendanceSign> attendanceSigns = this.list(Wrappers.<SmtAttendanceSign>query().lambda()
				.eq(SmtAttendanceSign::getCheckDate, checkDate));
		if (CollectionUtils.isNotEmpty(attendanceSigns)) {
			throw new SmartException("本月员工信息已生成");
		}
		List<SmtStaff> staffList = smtStaffService.getSeniorStaff();
		staffList.forEach(smtStaff -> {
			SmtAttendanceSign smtAttendanceSign = new SmtAttendanceSign();
			smtAttendanceSign.setSignStatus(AttendanceSignStatusEnum.NOT_SIGN.getCode());
			smtAttendanceSign.setCheckDate(checkDate);
			smtAttendanceSign.setCreateTime(LocalDateTime.now());
			smtAttendanceSign.setBadge(smtStaff.getBadge());
			smtAttendanceSign.setNoticeStatus(NoticeTypeEnum.NOTICE.getCode());
			this.save(smtAttendanceSign);
		});
		return true;
	}

	@Override
	public Integer countMessage(QueryAttendanceSignReqDTO reqDTO) {
		QueryAttendanceSignDTO dto = BeanUtils.transform(QueryAttendanceSignDTO.class, reqDTO);
		List<AttendanceSignVO> voList = this.baseMapper.getCount(dto);
		if (CollectionUtils.isNotEmpty(voList)) {
			List<AttendanceSignVO> vos = voList.stream().filter(v -> v.getPhone() != null && v.getPhone().matches(RegexUtils.RE_MOBILE_B)).collect(Collectors.toList());
			return vos.size();
		}
		return 0;
	}

	@Override
	public Boolean sendMessage(QueryAttendanceSignReqDTO reqDTO) {
		QueryAttendanceSignDTO dto = BeanUtils.transform(QueryAttendanceSignDTO.class, reqDTO);
		List<AttendanceSignVO> voList = this.baseMapper.getCount(dto);
		if (CollectionUtils.isNotEmpty(voList)) {
			List<SignMsgReqDTO> signMsgReqDTOs = new ArrayList<>();
			List<AttendanceSignVO> vos = voList.stream().filter(v -> v.getPhone() != null && v.getPhone().matches(RegexUtils.RE_MOBILE_B)).collect(Collectors.toList());
			vos.forEach(vo -> {
				SignMsgReqDTO signMsgReq = new SignMsgReqDTO();
				signMsgReq.setNumbers(vo.getPhone());
				signMsgReq.setTempCode(SmsTemplateEnum.SMS_SIGN_10402.getCode());
				signMsgReq.setPersonName(vo.getName());
				signMsgReqDTOs.add(signMsgReq);
			});

			Result result = remoteSmsManageService.sendAttendanceSign(signMsgReqDTOs);
			if (result.getMessage().equals(ResultStatusConstants.SUCCESS)) {
				List<Long> ids = vos.stream().map(AttendanceSignVO::getId).collect(Collectors.toList());
				return this.updateNotice(ids);
			}
		}
		return false;
	}

	@Override
	public List<AttendanceSignVO> getMegInfoList(QueryAttendanceSignDTO dto) {
		return this.baseMapper.getMegInfo(dto);
	}

	@Override
	public Boolean autoConfirm(QueryAttendanceSignDTO dto) {
		return this.baseMapper.autoConfirm(dto);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateNotice(List<Long> ids) {
		//最大参数只能传1000 所以分段传参
		List<List<Long>> partitionList = Lists.partition(ids, NumberConstants.maxSize);
		partitionList.forEach(list -> {
			this.baseMapper.updateNotice(list);
		});
		return true;
	}
}
