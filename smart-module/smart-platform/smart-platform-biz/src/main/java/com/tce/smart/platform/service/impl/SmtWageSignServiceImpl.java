package com.tce.smart.platform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.SignMsgReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteSmsManageService;
import com.tce.smart.platform.core.dto.QueryAttendanceSignDTO;
import com.tce.smart.platform.core.dto.WageSignDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtWageSign;
import com.tce.smart.platform.core.mapper.SmtWageSignMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.AttendanceSignVO;
import com.tce.smart.platform.core.vo.WageSignVO;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtWageSignService;
import com.tce.smart.tool.constant.NumberConstants;
import com.tce.smart.tool.constant.ResultStatusConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.constant.VehicleConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.enums.WageSignStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.RegexUtils;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 工资签单
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@Service
public class SmtWageSignServiceImpl extends ServiceImpl<SmtWageSignMapper, SmtWageSign> implements SmtWageSignService {

	@Autowired
	private SmtImageService smtImageService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtParkBuService smtParkBuService;
	@Autowired
	private RemoteSmsManageService remoteSmsManageService;

	private final DateTimeFormatter simpleFormatter = DateTimeFormatter.ofPattern(SymbolConstants.DATE_FORMAT_YYYY_MM);

	@Override
	public IPage<WageSignVO> getPage(Page page, WageSignDTO wageSignDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		wageSignDTO.setParkIds(parkIds);
		IPage<WageSignVO> pageResult = this.baseMapper.getPage(page, wageSignDTO);
		List<WageSignVO> count = this.baseMapper.getCount(wageSignDTO);
		Integer countNotSign = 0;
		Integer countSign = 0;
		if(CollectionUtils.isNotEmpty(count)) {
			countNotSign = count.stream().filter(s -> s.getSignStatus()
					.equals(WageSignStatusEnum.NOT_SIGN.getCode())).collect(Collectors.toList()).size();
			countSign = count.size() - countNotSign;
		}
		List<WageSignVO> records = pageResult.getRecords();
		for (WageSignVO wageSignVO : records) {
			SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, wageSignVO.getBadge()));
			//园区只显示当前登录用户所拥有的园区
			List<SmtPark> parkList = smtParkBuService.getUserParkListByBu(Integer.parseInt(selectOne.getCompId()), parkIds);
			List<String> parkNames = parkList.stream().map(SmtPark::getParkName).collect(Collectors.toList());
			wageSignVO.setParkName(StringUtils.join(parkNames, SymbolConstants.COMMA));
			wageSignVO.setSignStatusDesc(WageSignStatusEnum.desc(wageSignVO.getSignStatus()));
			wageSignVO.setCountNotSign(countNotSign);
			wageSignVO.setCountSign(countSign);
		}
		return pageResult;
	}

	@Override
	public boolean getWageSign(SmtWageSign smtWageSign) {
		SmtWageSign wageSign = this.getOne(Wrappers.<SmtWageSign>query().lambda().eq(SmtWageSign::getBadge, smtWageSign.getBadge())
				.eq(SmtWageSign::getWageDate, smtWageSign.getWageDate()));
		if (Objects.nonNull(wageSign)) {
			return WageSignStatusEnum.status(wageSign.getSignStatus());
		}
		return false;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateToSign(SmtWageSign smtWageSign) {
		String wageDate = smtWageSign.getWageDate();
		if(smtWageSign.getWageDate().split("-").length == 3){
			//裕同提交的格式为 yyyy-MM-dd
			wageDate = wageDate.substring(0,wageDate.lastIndexOf("-"));
		}
		SmtWageSign wageSign = this.getOne(Wrappers.<SmtWageSign>query().lambda().eq(SmtWageSign::getBadge, smtWageSign.getBadge())
				.eq(SmtWageSign::getWageDate, wageDate));

		if(null != wageSign && wageSign.getSignStatus().equals(WageSignStatusEnum.SIGN.getCode())){
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "员工已经签单");
		}

		String driverLicenseId = smtWageSign.getSignImg().replaceAll(VehicleConstants.BASE64_REGEX_PNG, "");
		String blobResult = smtImageService.saveImage(0, driverLicenseId, SmtImageEnum.TYPE_SALAR_SIGN.getCode());
		if (StringUtil.isNullOrEmpty(blobResult)) {
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "签名信息获取失败");
		}
		smtWageSign.setSignImg(blobResult);
		smtWageSign.setCreateTime(LocalDateTime.now());
		smtWageSign.setSignStatus(WageSignStatusEnum.SIGN.getCode());
		if(null == wageSign){
			//添加
			return this.save(smtWageSign);
		}
		return this.update(smtWageSign, Wrappers.<SmtWageSign>query().lambda().eq(SmtWageSign::getBadge, smtWageSign.getBadge())
				.eq(SmtWageSign::getWageDate, wageDate));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean syncStaff() {
		String WageDate = simpleFormatter.format(LocalDateTime.now().plusMonths(-1));
		List<SmtWageSign> wageSign = this.list(Wrappers.<SmtWageSign>query().lambda()
				.eq(SmtWageSign::getWageDate, WageDate));
		if (CollectionUtils.isNotEmpty(wageSign)) {
			throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "本月员工信息已生成");
		}
		List<SmtStaff> staffList = smtStaffService.getSeniorStaff();
		staffList.forEach(smtStaff -> {
			SmtWageSign smtWageSign = new SmtWageSign();
			smtWageSign.setSignStatus(WageSignStatusEnum.NOT_SIGN.getCode());
			smtWageSign.setWageDate(WageDate);
			smtWageSign.setBadge(smtStaff.getBadge());
			smtWageSign.setNoticeStatus(NoticeTypeEnum.NOTICE.getCode());
			this.save(smtWageSign);
		});
		return true;
	}

	@Override
	public Integer countMessage(WageSignDTO wageSignDTO) {
		List<WageSignVO> voList = this.baseMapper.getCount(wageSignDTO);
		if (CollectionUtils.isNotEmpty(voList)) {
			List<WageSignVO> vos= voList.stream().filter(vo -> vo.getPhone() != null
					&& vo.getPhone().matches(RegexUtils.RE_MOBILE_B)).collect(Collectors.toList());
			return vos.size();
		}
		return 0;
	}

	@Override
	public Boolean sendMessage(WageSignDTO wageSignDTO) {
		List<WageSignVO> voList = this.baseMapper.getCount(wageSignDTO);
		if (CollectionUtils.isNotEmpty(voList)) {
			List<WageSignVO> vos= voList.stream().filter(vo -> vo.getPhone() != null
					&& vo.getPhone().matches(RegexUtils.RE_MOBILE_B)).collect(Collectors.toList());
			List<SignMsgReqDTO> signMsgReqDTOS = new ArrayList<>();
			vos.forEach(v -> {
				SignMsgReqDTO signMsgReqDTO = new SignMsgReqDTO();
				signMsgReqDTO.setTempCode(SmsTemplateEnum.SMS_SIGN_10401.getCode());
				signMsgReqDTO.setNumbers(v.getPhone());
				signMsgReqDTO.setPersonName(v.getName());
				signMsgReqDTOS.add(signMsgReqDTO);
			});
			Result result = remoteSmsManageService.sendWageSign(signMsgReqDTOS);
			//注意：此处必须用 isSuccess()（判断依据是 CommonConstants.SUCCESS=0）；
			//SuccessEnum 是另一套独立编码(SUCCESS=1)，与 Result 的编码含义相反，混用会导致成功/失败判断颠倒
			if(result.isSuccess()) {
				List<Integer> ids = vos.stream().map(WageSignVO::getId).collect(Collectors.toList());
				return this.updateNotice(ids);
			}
		}
		return false;
	}

	@Override
	public List<WageSignVO> getMegInfoList(WageSignDTO dto){
		return this.baseMapper.getMegInfo(dto);
	}

	@Override
	public Boolean autoConfirm(WageSignDTO dto){
		return this.baseMapper.autoConfirm(dto);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateNotice(List<Integer> ids) {
		//最大参数只能传1000 所以分段传参
		List<List<Integer>> partitionList = Lists.partition(ids, NumberConstants.maxSize);
		partitionList.forEach(list -> {
			this.baseMapper.updateNotice(list);
		});
		return true;
	}

}
