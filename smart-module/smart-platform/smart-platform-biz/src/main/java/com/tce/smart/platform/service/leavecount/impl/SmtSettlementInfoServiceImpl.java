package com.tce.smart.platform.service.leavecount.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementCountReqDTO;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementInfoQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.commonconfig.ConfigSettlementLastDayDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementInfoDhrRespDTO;
import com.tce.smart.platform.core.dto.OrganizeRelationDTO;
import com.tce.smart.platform.core.dto.settlement.DormitoryStaffDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.leavecount.*;
import com.tce.smart.platform.core.mapper.SmtOrganizeRelationMapper;
import com.tce.smart.platform.core.mapper.leavecount.SmtSettlementInfoMapper;
import com.tce.smart.platform.core.service.SmtCommonConfigService;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.leavecount.*;
import com.tce.smart.platform.service.settlement.SmtMeterreadCnfigService;
import com.tce.smart.tool.constant.NumberConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.DormitoryHisotryTypeEnum;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import com.tce.smart.tool.util.ToolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2022-06-21 11:02:12
 */
@Service
public class SmtSettlementInfoServiceImpl extends ServiceImpl<SmtSettlementInfoMapper, SmtSettlementInfo> implements SmtSettlementInfoService {

	@Autowired
	private SmtStaffService smtStaffService;

	@Autowired
	private SmtDormitoryStaffService smtDormitoryStaffService;

	@Autowired
	private SmtDormitoryStaffHistoryService smtDormitoryStaffHistoryService;

	@Autowired
	private SmtMeterreadCnfigService smtMeterreadCnfigService;

	@Autowired
	private SmtDormitoryOutRemarkService smtDormitoryOutRemarkService;

	@Autowired
	private SmtCommonConfigService smtCommonConfigService;

	@Autowired
	private SmtSettlementTemplateRangeService smtSettlementTemplateRangeService;

	@Autowired
	private SmtSettlementTemplateItemService smtSettlementTemplateItemService;

	@Autowired
	private SmtSettlementTemplateRuleService smtSettlementTemplateRuleService;

	@Autowired
	private SmtSettlementTemplateJcheService smtSettlementTemplateJcheService;

	@Autowired
	private SmtParkBuService smtParkBuService;

	@Autowired
	private SmtOrganizeRelationMapper smtOrganizeRelationMapper;

	@Value("${spring.settlement.check-token:}")
	private String pwdToken;

	@Value("${smart.sy-park-id:0}")
	private Integer syParkId;

	@Override
	public IPage<SmtSettlementInfo> getPage(Page page, SettlementInfoQueryReqDTO queryReqDTO) {
		if(Objects.isNull(queryReqDTO)) {
			return this.page(page);
		}
		return this.page(page, Wrappers.<SmtSettlementInfo>query().lambda()
				.eq(Objects.nonNull(queryReqDTO.getParkId()), SmtSettlementInfo::getParkId, queryReqDTO.getParkId())
				.like(StringUtils.isNotEmpty(queryReqDTO.getBadge()), SmtSettlementInfo::getBadge, queryReqDTO.getBadge())
				.like(StringUtils.isNotEmpty(queryReqDTO.getName()), SmtSettlementInfo::getName, queryReqDTO.getName())
				.like(StringUtils.isNotEmpty(queryReqDTO.getBu()), SmtSettlementInfo::getBu, queryReqDTO.getBu())
				.eq(Objects.nonNull(queryReqDTO.getStatus()), SmtSettlementInfo::getStatus, queryReqDTO.getStatus())
				.between(Objects.nonNull(queryReqDTO.getStartLeaveDate()), SmtSettlementInfo::getLeaveDate,
						queryReqDTO.getStartLeaveDate(), queryReqDTO.getEndLeaveDate())
				.between(Objects.nonNull(queryReqDTO.getStartTime()), SmtSettlementInfo::getCreateTime,
						queryReqDTO.getStartTime(), queryReqDTO.getEndTime())
				.orderByDesc(SmtSettlementInfo::getCreateTime)
		);
	}

	@Override
	public SettlementInfoDhrRespDTO getSettlement(SettlementCountReqDTO reqDTO) {
		this.checkParam(reqDTO);
		String leaveDate = reqDTO.getLeaveDate();
		String badge = reqDTO.getBadge();
		SmtStaff staff = smtStaffService.getOne(Wrappers.<SmtStaff>lambdaQuery().eq(SmtStaff::getBadge, badge), false);
		if (Objects.isNull(staff)) {
			throw new SmartException(10002, "园区系统未找到该员工信息");
		}
		SettlementInfoDhrRespDTO respDTO = new SettlementInfoDhrRespDTO();
		respDTO.setBadge(badge);
		respDTO.setName(staff.getName());
		respDTO.setCreateTime(DateUtils.convert(LocalDateTime.now()));
		respDTO.setLeaveDate(leaveDate);

		List<SettlementInfoDhrRespDTO.CountRoom> countRooms = new ArrayList<>();
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime leaveTime = LocalDate.parse(leaveDate, df).atStartOfDay();
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime nextMonth = DateUtils.ofEpochMilli(DateUtil.beginOfMonth(Date.from(now.plusMonths(-1).atZone(ZoneId.systemDefault()).toInstant())).toInstant().toEpochMilli());
		LocalDateTime lastMonth = DateUtils.ofEpochMilli(DateUtil.beginOfMonth(Date.from(now.plusMonths(1).atZone(ZoneId.systemDefault()).toInstant())).toInstant().toEpochMilli());
		if (leaveTime.isAfter(lastMonth) || leaveTime.isBefore(nextMonth)) {
			throw new SmartException(10007, "离职日期不在查询范围内");
		}
		//获得在宿记录费用
		countRooms.addAll(this.getDorFee(leaveTime, staff, false));
		//获得退宿记录费用
		countRooms.addAll(this.getDorFee(leaveTime, staff, true));
		respDTO.setCountRoom(countRooms);
		BigDecimal allFee = countRooms.stream().map(SettlementInfoDhrRespDTO.CountRoom::getFee)
				.reduce(new BigDecimal(0), BigDecimal::add);
		respDTO.setTotalFee(allFee);
		List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(staff.getCompId()));
		Integer parkId = null;
		if (CollUtil.isNotEmpty(parkList)) {
			if (parkList.size() == NumberConstants.ONE) {
				parkId = parkList.get(0).getId();
			} else {
				parkId = syParkId;
			}
		}
		if (Objects.isNull(parkId)) {
			//查询临时BU信息
			OrganizeRelationDTO orgRelation = smtOrganizeRelationMapper.getOrgRelation(Long.parseLong(staff.getCompId()));
			parkId = orgRelation.getParkId();
		}
		//保存记录
		SmtSettlementInfo info = SmtSettlementInfo.builder()
				.parkId(parkId)
				.badge(staff.getBadge())
				.name(staff.getName())
				.bu(staff.getCompName())
				.dept(staff.getDepName())
				.fee(respDTO.getTotalFee())
				.leaveDate(leaveTime)
				.status(OneOrZeroEnum.ONE.getCode())
				.createTime(LocalDateTime.now())
//				.quitDate()
//				.preCollect()
				.leaveDays(countRooms.stream().map(SettlementInfoDhrRespDTO.CountRoom::getCountDays).reduce(0, Integer::sum))
				.build();
		this.save(info);
		respDTO.setNum(info.getId().toString());
		return respDTO;
	}

	/**
	 * 获取 在宿|退宿 记录费用
	 * @param leaveTime
	 * @param staff
	 * @param isOut
	 * @return
	 */
	private List<SettlementInfoDhrRespDTO.CountRoom> getDorFee(LocalDateTime leaveTime, SmtStaff staff, Boolean isOut) {
		List<DormitoryStaffDTO> dormitoryStaffList;
		if (isOut) {
			List<SmtDormitoryStaffHistory> smtDormitoryStaff = smtDormitoryStaffHistoryService.list(Wrappers.<SmtDormitoryStaffHistory>lambdaQuery()
					.eq(SmtDormitoryStaffHistory::getStaffBadge, staff.getBadge())
					.ne(SmtDormitoryStaffHistory::getType, DormitoryHisotryTypeEnum.IN_DORMITORY.getCode())
			);
			if (CollUtil.isEmpty(smtDormitoryStaff)) {
				return Collections.emptyList();
			}
			dormitoryStaffList = BeanUtils.batchTransform(DormitoryStaffDTO.class, smtDormitoryStaff);
		} else {
			List<SmtDormitoryStaff> smtDormitoryStaff = smtDormitoryStaffService.list(Wrappers.<SmtDormitoryStaff>lambdaQuery()
					.eq(SmtDormitoryStaff::getStaffBadge, staff.getBadge()));
			if (CollUtil.isEmpty(smtDormitoryStaff)) {
				return Collections.emptyList();
			}
			dormitoryStaffList = BeanUtils.batchTransform(DormitoryStaffDTO.class, smtDormitoryStaff);
		}
		List<SettlementInfoDhrRespDTO.CountRoom> countRooms = new ArrayList<>();
		Integer month = leaveTime.getMonthValue();
		// 上月自然日
		Integer lastMonthDay = DateUtils.endOfMonth(Date.from(leaveTime.plusMonths(-1).atZone(ZoneId.systemDefault()).toInstant())).dayOfMonth();
		Date leaveDateTime = Date.from(leaveTime.atZone(ZoneId.systemDefault()).toInstant());
		for (DormitoryStaffDTO staffDor : dormitoryStaffList) {
			// 离职天数
			int day = leaveTime.getDayOfMonth();
			SettlementInfoDhrRespDTO.CountRoom countRoom = new SettlementInfoDhrRespDTO.CountRoom();
			//水电配置
			List<SmtSettlementTemplateRule> range = this.getRule(staffDor.getRoomId(), staff.getJcheId());
			countRoom.setRoomInfo(staffDor.getDormitoryName() + SymbolConstants.MINUS +
					staffDor.getDormitoryName() + SymbolConstants.MINUS +
					staffDor.getRoomName());
			// 离职当天是否计算水电
			ConfigSettlementLastDayDTO isCountLaseDay = smtCommonConfigService.getLeaveSettlementApprove(staffDor.getParkId());
			if (Objects.nonNull(isCountLaseDay) && OneOrZeroEnum.ONE.getCode().equals(isCountLaseDay.getIsSettlementLast())) {
				day = day - 1;
			}
			//结算日
			SmtMeterreadConfig config = smtMeterreadCnfigService.getCountDays(staffDor.getParkId(), ToolUtils.localDateTimeToDate(leaveTime));
			//退宿日
			Date quitDate = staffDor.getCreateTime();
			//如果退宿日期早于离职日期，计算天数等于退宿日
			if (isOut && quitDate.compareTo(leaveDateTime) < 0) {
				leaveDateTime = quitDate;
				day = quitDate.getDate();
			}

			int feeDay;
			// 当月退宿记录类型是离职、自离时
			if (!isOut || DormitoryHisotryTypeEnum.OUT_DORMITORY.getCode().equals(staffDor.getType())
					|| DormitoryHisotryTypeEnum.OUT_SELF.getCode().equals(staffDor.getType())) {
				// 上月自然天数 - 上月抄表日期 + 离职天数
				feeDay = lastMonthDay - config.getPreDate() + day;
			} else {
				// 退宿时间 - 入住时间
				feeDay = (int) DateUtils.betweenDay(staffDor.getTime(), staffDor.getInTime(), true);
			}
			if (feeDay < 1) {
				continue;
			}
			Integer remarkDays;
			if (isOut) {
				remarkDays = smtDormitoryOutRemarkService.getRemarkDate(null, staffDor.getId(),
						DateUtils.beginOfMonth(leaveDateTime), leaveDateTime);
			} else {
				remarkDays = smtDormitoryOutRemarkService.getRemarkDate(staffDor.getId(), null,
						DateUtils.beginOfMonth(leaveDateTime), leaveDateTime);
			}
			feeDay = feeDay - remarkDays;
			BigDecimal fee = this.getFee(range, feeDay, month);
			countRoom.setCountDays(feeDay);
			countRoom.setFee(fee);
			countRoom.setRemarkDays(remarkDays);
			countRooms.add(countRoom);
		}
		return countRooms;
	}

	private List<SmtSettlementTemplateRule> getRule(Integer roomId, String jcheId) {
		SmtSettlementTemplateRange range = smtSettlementTemplateRangeService.getOne(Wrappers.<SmtSettlementTemplateRange>lambdaQuery()
				.eq(SmtSettlementTemplateRange::getType, NumberConstants.ONE)
				.eq(SmtSettlementTemplateRange::getValue, roomId.toString()), false);
		if (Objects.isNull(range)) {
			throw new SmartException(10003, "未配置水电离职结算模板");
		}
		List<SmtSettlementTemplateItem> items = smtSettlementTemplateItemService.list(Wrappers.<SmtSettlementTemplateItem>lambdaQuery()
				.eq(SmtSettlementTemplateItem::getTempId, range.getTempId()));
		if (CollUtil.isEmpty(items)) {
			throw new SmartException(10005, "员工职层未配置离职结算模板");
		}
		SmtSettlementTemplateJche jche;
		try {
			jche = smtSettlementTemplateJcheService.getOne(Wrappers.<SmtSettlementTemplateJche>lambdaQuery()
					.in(SmtSettlementTemplateJche::getItemId, items.stream().map(SmtSettlementTemplateItem::getId).collect(Collectors.toList()))
					.eq(SmtSettlementTemplateJche::getJcheId, jcheId));
			if (Objects.isNull(jche)) {
				throw new SmartException(10005, "员工职层未配置离职结算模板");
			}
		} catch (Exception e) {
			log.error("员工职层对应的离职结算模板出现多个", e);
			throw new SmartException(10005, "员工职层未配置离职结算模板");
		}
		return smtSettlementTemplateRuleService.list(Wrappers.<SmtSettlementTemplateRule>lambdaQuery()
				.eq(SmtSettlementTemplateRule::getItemId, jche.getItemId()));
	}

	private BigDecimal getFee(List<SmtSettlementTemplateRule> rule, Integer days, Integer month) {
		List<SmtSettlementTemplateRule> rules = rule.stream().filter(r -> r.getMonthNum().equals(month)).collect(Collectors.toList());
		double fee = 0.0;
		for (SmtSettlementTemplateRule templateRule : rules) {
			if (Objects.nonNull(templateRule.getStandardQty())) {
				fee += templateRule.getStandardQty();
			}
		}
		return BigDecimal.valueOf(fee).multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
	}

	private void checkParam(SettlementCountReqDTO reqDTO) {
		if (Objects.isNull(reqDTO)) {
			throw new SmartException(10006, "请求参数为空");
		}
		if (StringUtils.isEmpty(pwdToken) || !pwdToken.equals(reqDTO.getToken())) {
			throw new SmartException(10001, "密匙校验失败");
		}
		if (StringUtils.isEmpty(reqDTO.getLeaveDate())) {
			throw new SmartException(10004, "离职时间为空");
		}
	}
}
