package com.tce.smart.platform.service.manage.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.consume.req.XfPlanMoneyReqDTO;
import com.tce.smart.data.api.dto.consume.resp.WorkTimeRespDTO;
import com.tce.smart.data.api.dto.ehrview.AvaGetskyPayYSHRDTO;
import com.tce.smart.data.api.dto.ehrview.EvwCcdFlstandardDTO;
import com.tce.smart.data.api.dto.ehrview.OvwYsCallOwanceDetailsDTO;
import com.tce.smart.data.api.dto.ehrview.req.AvaGetskyPayYSHRReqDTO;
import com.tce.smart.data.api.feign.attendance.RemoteKQShiftDetailsService;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.data.api.feign.consume.RemoteXfPlanMoneyCardService;
import com.tce.smart.data.api.feign.ehrview.RemoteAvaGetskyPayService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwCcdFlstandardService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsCallOwanceDetailsService;
import com.tce.smart.platform.api.dto.req.manage.RechargePageReqDTO;
import com.tce.smart.platform.core.ao.RechargePageAO;
import com.tce.smart.platform.core.entity.SmtEhrToStaffSetting;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.manage.SmtStaffRecharge;
import com.tce.smart.platform.core.mapper.SmtStaffRechargeMapper;
import com.tce.smart.platform.core.vo.RechargePageVO;
import com.tce.smart.platform.service.SmtEhrToStaffSettingService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.manage.SmtStaffRechargeService;
import com.tce.smart.tool.constant.NumberConstants;
import com.tce.smart.tool.constant.RechargeDownConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.ToolUtils;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.tce.smart.common.core.exception.TCEException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fushiping
 * @date 2020-07-17 16:51:38
 */
@Service
@Slf4j
public class SmtStaffRechargeServiceImpl extends ServiceImpl<SmtStaffRechargeMapper, SmtStaffRecharge> implements SmtStaffRechargeService {

	@Autowired
	private RemoteAvaGetskyPayService remoteAvaGetskyPayService;
	@Autowired
	private RemoteEvwCcdFlstandardService remoteEvwCcdFlstandardService;
	@Autowired
	private RemoteXfPlanMoneyCardService remoteXfPlanMoneyCardService;
	@Autowired
	private RemoteOvwYsCallOwanceDetailsService ovwYsCallOwanceDetailsService;
	@Autowired
	private RemoteRsEmpService remoteRsEmpService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtParkBuService smtParkBuService;
	@Autowired
	private StringRedisTemplate redisTemplate;
	@Autowired
	private SmtEhrToStaffSettingService smtEhrToStaffSettingService;
	@Value("${spring.park-set.lg-park-id}")
	private Integer lgParkId;


	/**
	 * data格式转换类型
	 */
	private final SimpleDateFormat formatter = new SimpleDateFormat(SymbolConstants.DATE_FORMAT_YYYY_MM);
	/**
	 * LocalDateTime格式转换类型
	 */
	private final DateTimeFormatter loFormatter = DateTimeFormatter.ofPattern(SymbolConstants.DATE_FORMAT_YYYY_MM_DD);

	private final DateTimeFormatter simpleLoFormatter = DateTimeFormatter.ofPattern(SymbolConstants.DATE_TIME_OF_MONTH);

	private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern(SymbolConstants.DATE_FORMAT_YYYY_MM);

	/**
	 * 分页返回数据
	 *
	 * @param page
	 * @param reqDTO
	 * @return
	 */
	@Override
	public IPage<RechargePageVO> getPage(Page page, RechargePageReqDTO reqDTO) {
		RechargePageAO ao = this.getQueryAo(reqDTO);
		IPage<RechargePageVO> pageResult = this.baseMapper.getPage(page, ao);
		List<RechargePageVO> records = pageResult.getRecords();
		for (RechargePageVO vo : records) {
			List<SmtPark> parkList = smtParkBuService.getUserParkListByBu(Integer.parseInt(vo.getCompId()), ao.getParkIds());
			List<String> parkNames = parkList.stream().map(SmtPark::getParkName).collect(Collectors.toList());
			vo.setParkNames(StringUtils.join(parkNames, SymbolConstants.COMMA));
		}
		return pageResult;
	}

	/**
	 * 批量删除
	 *
	 * @param reqDTO
	 * @return
	 */
	@Override
	public Boolean deleteInfo(RechargePageReqDTO reqDTO) {
		RechargePageAO ao = this.getQueryAo(reqDTO);
		if (CollUtil.isEmpty(reqDTO.getIds())) {
			return this.baseMapper.deleteInfo(ao);
		}
		return this.removeByIds(reqDTO.getIds());
	}

	/**
	 * 充值名单修改
	 *
	 * @param account
	 * @param remark
	 * @param id
	 * @return
	 */
	@Override
	public Boolean updateRecharge(BigDecimal account, String remark, String id) {
		SmtStaffRecharge recharge = this.getById(Long.parseLong(id));
		if (Objects.isNull(recharge)) {
			throw new TCEException("该充值名单不存在");
		}
		recharge.setAccount(account);
		recharge.setBlank(remark);
		return this.updateById(recharge);
	}

	@Override
	@Async
	public Boolean syncNewStaff() {
		LocalDateTime localDateTime = DateUtils.middleNight(LocalDateTime.now());
		//当月入职员工数据
		List<SmtStaff> staffs = smtStaffService.getNewStaff();
		if (CollectionUtils.isNotEmpty(staffs)) {
			//当月新员工充值名单数据
			List<SmtStaffRecharge> rechargeList = this.getMouthList(localDateTime, RechargeTypeEnum.NEW_EMPLOYEE.getCode());
			//获得当月所有时间
			for (SmtStaff staff : staffs) {
				List<SmtStaffRecharge> reRrecharges = rechargeList.stream()
						.filter(recharge -> recharge.getBadge().equals(staff.getBadge())).collect(Collectors.toList());
				//判断是否已存在员工充值信息 true为员工充值信息不存在
				Boolean b = CollectionUtils.isEmpty(reRrecharges);
				if (b && !staff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
					SmtStaffRecharge recharge = new SmtStaffRecharge();
					recharge.setSyncStatus(RechargeSyncEnum.INIT.getCode());
					recharge.setCheckMonth(monthFormatter.format(localDateTime));
					recharge.setRechargeType(RechargeTypeEnum.NEW_EMPLOYEE.getCode());
					recharge.setBadge(staff.getBadge());
					//recharge.setCompId(staff.getCompId());
					recharge.setAccount(BigDecimal.ZERO);
					recharge.setCreateTime(LocalDateTime.now());
					//查询员工考勤情况
					this.setNewStaffStandard(staff, recharge);
					this.save(recharge);
					continue;
				}
				if (!b && !staff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
					if (Objects.isNull(reRrecharges.get(0).getAccount()) ||
							reRrecharges.get(0).getAccount().equals(BigDecimal.ZERO)) {
						//更新没有同步到餐补的数据
						this.setNewStaffStandard(staff, reRrecharges.get(0));
						this.updateById(reRrecharges.get(0));
						continue;
					}
				}
				//如果员工离职，删除充值名单信息
				if (!b && staff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
					List<Long> ids = reRrecharges.stream().map(SmtStaffRecharge::getId).collect(Collectors.toList());
					this.removeByIds(ids);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public Boolean syncSeniorRecharge() {
		//检查方法是否已在运行
		this.checkKey("sync");
		LocalDateTime localDateTime = DateUtils.middleNight(LocalDateTime.now());
		LocalDateTime preMonthFirstDay = localDateTime.plusMonths(-1).with(TemporalAdjusters.firstDayOfMonth());
		//检查本月数据是否已生成
		try {
			this.seniorFirstRecharge(preMonthFirstDay);
		} catch (Exception e) {
			this.destroyKey("sync");
			throw new TCEException(e.getMessage());
		}
		//方法执行完毕 释放锁
		this.destroyKey("sync");
		return true;
	}

	/**
	 * 再次计算在职员工未计算出充值数据时再次计算
	 *
	 * @param preMonthFirstDay
	 */
	private void seniorNotRecharge(LocalDateTime preMonthFirstDay) {
//		LocalDateTime preLocalDate = DateUtils.middleNight(LocalDateTime.now().plusMonths(-1));
//		List<SmtParkBu> parkBus = smtParkBuService.list();
//		List<SmtParkBu> lgParkBus = parkBus.stream().filter(a -> a.getParkId().equals(lgParkId)).collect(Collectors.toList());
//		List<String> lgParkBuIds = lgParkBus.stream().map(SmtParkBu::getCompId).collect(Collectors.toList());
//		List<SmtStaffRecharge> notCount = this.list(Wrappers.<SmtStaffRecharge>query().lambda()
//				.eq(SmtStaffRecharge::getAccount, BigDecimal.ZERO)
//				.eq(SmtStaffRecharge::getCheckMonth, monthFormatter.format(preLocalDate)));
//		List<SmtStaffRecharge> notCountLg = this.list(Wrappers.<SmtStaffRecharge>query().lambda()
//				.eq(SmtStaffRecharge::getAccount, BigDecimal.ZERO)
//				.in(SmtStaffRecharge::getCompId, lgParkBuIds)
//				.eq(SmtStaffRecharge::getCheckMonth, monthFormatter.format(preLocalDate)));
//		if (CollectionUtils.isEmpty(notCount)) {
//			this.destroyKey("sync");
//			throw new TCEException("信息提示：" + simpleLoFormatter.format(preLocalDate) + "的充值名单已全部生成完毕");
//		}
//
//		List<String> badgeList = notCount.stream().map(SmtStaffRecharge::getBadge).collect(Collectors.toList());
//		List<String> lgStaffBadgeList = notCountLg.stream().map(SmtStaffRecharge::getBadge).collect(Collectors.toList());
//
//		List<AvaGetskyPayYSHRDTO> payList = new ArrayList<>();
//
//		//远程调用最大传参个数为2000，mybatisPlus最大传参个数1000
//		List<List<String>> partitionList = Lists.partition(badgeList, NumberConstants.maxSize);
//		partitionList.forEach(ids -> {
//			AvaGetskyPayYSHRReqDTO dto = new AvaGetskyPayYSHRReqDTO();
//			dto.setBadge(ids);
//			dto.setStartTime(preMonthFirstDay);
//			Result<List<AvaGetskyPayYSHRDTO>> result =
//					remoteAvaGetskyPayService.monthListByBadge(dto, SecurityConstants.FROM_IN);
//			List<AvaGetskyPayYSHRDTO> pay = result.getData();
//			if (CollectionUtils.isNotEmpty(pay)) {
//				payList.addAll(pay);
//			}
//		});
//
//		//龙岗员工本月考勤
//		List<AvaGetskyPayYSHRDTO> lgCurrPayList = new ArrayList<>();
//		LocalDateTime currMonthFirstDay = preMonthFirstDay.plusMonths(1);
//		//龙岗员工工号列表
//		List<List<String>> lgPartitionList = Lists.partition(lgStaffBadgeList, NumberConstants.maxSize);
//		lgPartitionList.forEach(ids -> {
//			AvaGetskyPayYSHRReqDTO dto = new AvaGetskyPayYSHRReqDTO();
//			dto.setBadge(ids);
//			dto.setStartTime(currMonthFirstDay);
//			Result<List<AvaGetskyPayYSHRDTO>> result =
//					remoteAvaGetskyPayService.monthListByBadge(dto, SecurityConstants.FROM_IN);
//			List<AvaGetskyPayYSHRDTO> pay = result.getData();
//			if (CollectionUtils.isNotEmpty(pay)) {
//				lgCurrPayList.addAll(pay);
//			}
//		});
//
//		Map<String, List<AvaGetskyPayYSHRDTO>> lgCurrPayMap = lgCurrPayList.stream().collect(Collectors.groupingBy(AvaGetskyPayYSHRDTO::getBadge));
//
//		Map<String, BigDecimal> flStandard = new ConcurrentHashMap<>();
//		if (CollectionUtils.isNotEmpty(payList)) {
//			for (AvaGetskyPayYSHRDTO ava : payList) {
//				List<SmtStaffRecharge> recharges = notCount.stream().filter(s -> s.getBadge().equals(ava.getBadge())).collect(Collectors.toList());
//				if (CollectionUtils.isNotEmpty(recharges)) {
//					SmtStaffRecharge recharge = recharges.get(NumberConstants.ZERO);
//					SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(recharge.getBadge());
//					//查询员工考勤情况
//					try {
//						if (lgParkBuIds.contains(staff.getCompId())) {
//							if (!lgCurrPayMap.containsKey(staff.getBadge())) {
//								continue;
//							}
//							//龙岗员工充值
//							AvaGetskyPayYSHRDTO avaGetskyPayYSHRDTO = lgCurrPayMap.get(staff.getBadge()).get(0);
//							this.setSeniorStaffStandardForLG(staff, recharge, ava, flStandard, avaGetskyPayYSHRDTO);
//						} else {
//							this.setSeniorStaffStandard(staff, recharge, ava, flStandard);
//						}
//					} catch (Exception e) {
//						log.error("该员工考勤数据或餐补数据异常：" + staff.getBadge());
//						continue;
//					}
//					this.updateById(recharge);
//				}
//				log.error("该员工在smt_staff表中数据为空：" + ava.getBadge());
//			}
//		}
	}

	/**
	 * 在职员工每月第一次生成数据
	 *
	 * @param preMonthFirstDay
	 */
	private void seniorFirstRecharge(LocalDateTime preMonthFirstDay) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime preLocalDate = DateUtils.middleNight(now.plusMonths(-1));
		List<DateTime> dateTimes = DateUtil.rangeToList(DateUtil.beginOfMonth(new Date()), DateUtil.endOfMonth(new Date()), DateField.DAY_OF_YEAR);
		//选择同步到系统的bu员工数据
		List<SmtEhrToStaffSetting> setting = smtEhrToStaffSettingService.list();
		List<String> buIds = setting.stream().map(SmtEhrToStaffSetting::getCompId).collect(Collectors.toList());
		//查询BU和园区的对应关系数据
		List<SmtParkBu> parkBus = smtParkBuService.list();
		List<SmtParkBu> lgParkBus = parkBus.stream().filter(a -> a.getParkId().equals(lgParkId)).collect(Collectors.toList());
		List<String> lgParkBuIds = lgParkBus.stream().map(SmtParkBu::getCompId).collect(Collectors.toList());
		//获取龙岗园区的已同步的BU
		List<String> lgBuIds = lgParkBuIds.stream().filter(a -> buIds.contains(a)).collect(Collectors.toList());
		//查询所有BU上月考勤数据
		Result<List<AvaGetskyPayYSHRDTO>> result =
				remoteAvaGetskyPayService.monthList(preMonthFirstDay, buIds, SecurityConstants.FROM_IN);
		List<AvaGetskyPayYSHRDTO> payList = result.getData();
		if (CollectionUtils.isEmpty(payList)) {
			this.destroyKey("sync");
			throw new TCEException("信息提示：" + simpleLoFormatter.format(preLocalDate) + "的考勤汇总数据，EHR尚未生成，请与考勤员核实是否已关账");
		}
		Map<String, List<AvaGetskyPayYSHRDTO>> payMap = payList.stream().collect(Collectors.groupingBy(AvaGetskyPayYSHRDTO::getBadge));
		//查找外餐人员
		//List<String> badgeList = this.getYsCallOwance();
		//获得在职员工数据并去除
		List<SmtStaff> staffLists = smtStaffService.getSeniorRechargeStaff();
		//List<SmtStaff> staffList = staffLists.stream().filter(staff -> !badgeList.contains(staff.getBadge())).collect(Collectors.toList());
		Map<String, BigDecimal> flStandard = new ConcurrentHashMap<>();
		for (SmtStaff staff : staffLists) {
			String badge = staff.getBadge();
			List<SmtStaffRecharge> reRecharges = this.baseMapper.getByBadge(preLocalDate, badge);
			//TODO检查餐补
			Result<Boolean> booleanResult = ovwYsCallOwanceDetailsService.getInfoByBadge(badge, SecurityConstants.FROM_IN);
			Boolean b = booleanResult.getData();
			if (CollUtil.isNotEmpty(reRecharges)) {
				SmtStaffRecharge recharge = reRecharges.get(0);
				if(Objects.isNull(b)) {
					recharge.setBlank("外餐补贴数据查询失败");
					this.updateById(recharge);
					continue;
				}
				if(Boolean.TRUE.equals(b) && recharge.getSyncStatus().equals(RechargeSyncEnum.INIT.getCode())) {
					recharge.setAccount(BigDecimal.ZERO);
					recharge.setBlank("已申请外餐补贴");
					this.updateById(recharge);
					continue;
				}
				if (recharge.getAccount().equals(BigDecimal.ZERO)) {
					List<AvaGetskyPayYSHRDTO> avas = payMap.get(staff.getBadge());
					if (CollUtil.isEmpty(avas)) {
						recharge.setBlank("该员工考勤数据为空");
						this.updateById(recharge);
						continue;
					}
					if (lgBuIds.contains(staff.getCompId())) {
						this.setSeniorStaffStandardForLG(staff, recharge, avas.get(0), flStandard, dateTimes);
					} else {
						this.setSeniorStaffStandard(staff, recharge, avas.get(0), flStandard);
					}
					this.updateById(recharge);
					continue;
				}
				continue;
			}

			SmtStaffRecharge recharge = new SmtStaffRecharge();
			recharge.setSyncStatus(RechargeSyncEnum.INIT.getCode());
			recharge.setAccount(BigDecimal.ZERO);
			recharge.setRechargeType(RechargeTypeEnum.SENIOR_EMPLOYEE.getCode());
			recharge.setCheckMonth(monthFormatter.format(preLocalDate));
			recharge.setBadge(staff.getBadge());
			//recharge.setCompId(staff.getCompId());
			recharge.setCreateTime(now);
			//查询员工考勤情况
			if(Objects.isNull(b)) {
				recharge.setBlank("外餐补贴数据查询失败");
				this.save(recharge);
				continue;
			}
			if(Boolean.TRUE.equals(b)) {
				recharge.setBlank("已申请外餐补贴");
				this.save(recharge);
				continue;
			}
			List<AvaGetskyPayYSHRDTO> avas = payMap.get(staff.getBadge());
			if (CollUtil.isEmpty(avas)) {
				recharge.setBlank("该员工考勤数据为空");
				this.save(recharge);
				continue;
			}
			if (lgBuIds.contains(staff.getCompId())) {
				this.setSeniorStaffStandardForLG(staff, recharge, avas.get(0), flStandard, dateTimes);
			} else {
				this.setSeniorStaffStandard(staff, recharge, avas.get(0), flStandard);
			}
			this.save(recharge);
			continue;
		}
	}


	/**
	 * 保存特殊充值名单
	 *
	 * @param badges
	 * @param remark
	 */
	@Override
	public String saveSingleRecharge(String badges, String remark) {
		if (StringUtils.isBlank(badges)) {
			throw new TCEException("工号为空");
		}
		//获取上月第一天凌晨时分
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime preLocalDate = DateUtils.middleNight(now.plusMonths(-1));
		LocalDateTime preMonthFirstDay = preLocalDate.with(TemporalAdjusters.firstDayOfMonth());
		List<String> badgeList = ToolUtils.splitBlankString(badges);
		AvaGetskyPayYSHRReqDTO dto = new AvaGetskyPayYSHRReqDTO();
		dto.setStartTime(preMonthFirstDay);
		dto.setBadge(badgeList);
		Result<List<AvaGetskyPayYSHRDTO>> result = remoteAvaGetskyPayService.monthListByBadge(dto, SecurityConstants.FROM_IN);
		List<AvaGetskyPayYSHRDTO> pay = result.getData();
		if (Objects.isNull(pay) || CollUtil.isEmpty(pay)) {
			throw new TCEException("上月考勤数据未生成或工号有误");
		}
		List<DateTime> dateTimes = DateUtil.rangeToList(DateUtil.beginOfMonth(new Date()), DateUtil.endOfMonth(new Date()), DateField.DAY_OF_YEAR);
		List<SmtParkBu> parkBus = smtParkBuService.list();
		List<SmtParkBu> lgParkBus = parkBus.stream().filter(a -> a.getParkId().equals(lgParkId)).collect(Collectors.toList());
		List<String> lgParkBuIds = lgParkBus.stream().map(SmtParkBu::getCompId).collect(Collectors.toList());
		List<String> errorBadge = new ArrayList<>();
		for (String badge : badgeList) {
			SmtStaffRecharge reRecharge = this.getOne(Wrappers.<SmtStaffRecharge>query().lambda().eq(SmtStaffRecharge::getBadge, badge)
					.eq(SmtStaffRecharge::getRechargeType, RechargeTypeEnum.SENIOR_EMPLOYEE.getCode())
					.eq(SmtStaffRecharge::getCheckMonth, monthFormatter.format(preLocalDate)));
			List<AvaGetskyPayYSHRDTO> avas = pay.stream().filter(s -> s.getBadge().equals(badge)).collect(Collectors.toList());
			SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(badge);
			Map<String, BigDecimal> flStandard = new ConcurrentHashMap<>();
			if (Objects.nonNull(reRecharge)) {
				if(reRecharge.getSyncStatus().equals(RechargeSyncEnum.SUCCESS.getCode())) {
					errorBadge.add(reRecharge.getBadge());
					continue;
				}
				if (StringUtils.isNotBlank(remark)) {
					reRecharge.setBlank(remark);
				}
				if (CollUtil.isEmpty(avas)) {
					reRecharge.setBlank("考勤数据为空");
					continue;
				}
				if (Objects.isNull(staff)) {
					reRecharge.setBlank("该员工不存在于员工表中");
					continue;
				}
				if (lgParkBuIds.contains(staff.getCompId())) {
					this.setSeniorStaffStandardForLG(staff, reRecharge, avas.get(0), flStandard, dateTimes);
				} else {
					this.setSeniorStaffStandard(staff, reRecharge, avas.get(0), flStandard);
				}
				this.updateById(reRecharge);
				continue;
			}
			SmtStaffRecharge recharge = new SmtStaffRecharge();
			if (StringUtils.isNotBlank(remark)) {
				recharge.setBlank(remark);
			}
			if (CollUtil.isEmpty(avas)) {
				recharge.setBlank("考勤数据为空");
				continue;
			}
			if (Objects.isNull(staff)) {
				recharge.setBlank("该员工不存在于员工表中");
				continue;
			}

			recharge.setAccount(BigDecimal.ZERO);
			recharge.setSyncStatus(RechargeSyncEnum.INIT.getCode());
			recharge.setRechargeType(RechargeTypeEnum.SENIOR_EMPLOYEE.getCode());
			recharge.setCheckMonth(monthFormatter.format(preLocalDate));
			recharge.setBadge(staff.getBadge());
			//recharge.setCompId(staff.getCompId());
			recharge.setCreateTime(now);
			if (lgParkBuIds.contains(staff.getCompId())) {
				this.setSeniorStaffStandardForLG(staff, recharge, avas.get(0), flStandard, dateTimes);
			} else {
				this.setSeniorStaffStandard(staff, recharge, avas.get(0), flStandard);
			}
			continue;
		}
		if(CollUtil.isNotEmpty(errorBadge)) {
			String errorBadges = StringUtils.join(errorBadge, "，");
			return "工号为：" + errorBadges + "的员工已充值到C6，无法更改数据";
		}
		return null;
	}

	/**
	 * 获得当月外餐补助数据
	 *
	 * @return
	 */
	private List<String> getYsCallOwance() {
		Result<List<OvwYsCallOwanceDetailsDTO>> listResult = ovwYsCallOwanceDetailsService.getInfoByTimeList(10);
		if (Objects.nonNull(listResult.getData())) {
			List<OvwYsCallOwanceDetailsDTO> list = listResult.getData();
			List<String> badges = list.stream().map(OvwYsCallOwanceDetailsDTO::getBadge).collect(Collectors.toList());
			return badges;
		}
		return null;
	}

	/**
	 * 获得应出勤与实出勤
	 *
	 * @param newStaff
	 * @param dateTimes
	 * @return
	 */
	private Boolean getFreeDays(SmtStaff newStaff, List<DateTime> dateTimes, SmtStaffRecharge recharge) {
		//根据部门id获得同部门同职级人员
		List<SmtStaff> staffs = smtStaffService.list(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getDepId, newStaff.getDepId())
				.eq(SmtStaff::getJcheId, newStaff.getJcheId())
				.eq(SmtStaff::getWelfareLevel, newStaff.getWelfareLevel())
				.le(SmtStaff::getCreateTime, LocalDateTime.now().plusMonths(-1).with(TemporalAdjusters.lastDayOfMonth())));
		if (CollUtil.isNotEmpty(staffs)) {
			for (SmtStaff staff : staffs) {
				//获得节假日安排
				Result<WorkTimeRespDTO> result = remoteRsEmpService.getFreeDays(staff.getBadge(), SecurityConstants.FROM_IN);
				if (Objects.nonNull(result.getData()) && CollUtil.isNotEmpty(result.getData().getTimes())) {
					List<DateTime> dateTimes1 = dateTimes;
					//本月时间移除节假日，剩下工作日时间
					dateTimes1.removeAll(result.getData().getTimes());
					//本月实出勤
					Integer actual = dateTimes.stream().filter(date -> date.isAfterOrEquals(newStaff.getCreateTime())).collect(Collectors.toList()).size();
					//本月应出勤
					Integer should = dateTimes.size();
					Double actualOn = 8 * Double.valueOf(actual);
					Double shouldOn = 8 * Double.valueOf(should);
					recharge.setActualOn(actualOn);
					recharge.setShouldOn(shouldOn);
					return Boolean.TRUE;
				}
			}
			recharge.setBlank("获取同部门/级层/福利层次参考人员节假日失败");
			return Boolean.FALSE;
		}
		recharge.setBlank("同部门/级层/福利层次的参考人员不存在");
		return Boolean.FALSE;
	}

	/**
	 * 排除周末获得应出勤与实出勤
	 *
	 * @param newStaff
	 * @return
	 */
	private Boolean getWordDay(SmtStaff newStaff, SmtStaffRecharge recharge) {
		List<DateTime> dateTimes = getMonthWorkDay();
		//本月实出勤
		double actual = dateTimes.stream().filter(date -> date.isAfterOrEquals(newStaff.getCreateTime())).collect(Collectors.toList()).size() - 0.5;
		Calendar cal = Calendar.getInstance();
		cal.setTime(newStaff.getCreateTime());
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w == 6 || w == 7) {
			actual = actual + 1;
		}
		//本月应出勤
		Integer should = dateTimes.size();
		Double actualOn = 8 * actual;
		Double shouldOn = 8 * Double.valueOf(should);
		recharge.setActualOn(actualOn);
		recharge.setShouldOn(shouldOn);
		return Boolean.TRUE;
	}

	/**
	 * 获取一个月的工作日
	 *
	 * @return
	 */
	private static List<DateTime> getMonthWorkDay() {
		List<DateTime> dateList = new ArrayList<>();
		Calendar calendar = DateUtil.beginOfMonth(new Date()).toCalendar();
		Calendar endCalendar = DateUtil.endOfMonth(new Date()).toCalendar();
		while (true) {
			int weekday = calendar.get(Calendar.DAY_OF_WEEK);
			if (weekday != 1 && weekday != 7) {
				dateList.add(DateTime.of(calendar.getTime()));
			}
			calendar.add(Calendar.DATE, 1);
			if (calendar.getTimeInMillis() > endCalendar.getTimeInMillis()) {
				break;
			}
		}
		return dateList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String syncToC6(RechargePageReqDTO req) {
		this.checkKey("toC6");
		try {
			List<RechargePageVO> pageResult;
			if (CollUtil.isEmpty(req.getIds())) {
				RechargePageAO ao = BeanUtils.transform(RechargePageAO.class, req);
				ao.setParkIds(SecurityUtils.getUser().getParkIdList());
				if (StringUtils.isNotBlank(req.getBadge())) {
					List<String> badgeList = ToolUtils.splitBlankString(req.getBadge());
					ao.setBadgeList(badgeList);
				}
				pageResult = this.baseMapper.getList(ao);
			} else {
				pageResult = this.baseMapper.getListByIds(req.getIds());
			}
			List<String> errorBadge = new ArrayList<>();
			List<XfPlanMoneyReqDTO> reqDTOS = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(pageResult)) {
				for (RechargePageVO result : pageResult) {
					if (Objects.nonNull(result.getAccount())) {
						XfPlanMoneyReqDTO reqDTO = new XfPlanMoneyReqDTO();
						reqDTO.setDPTNAME(result.getDepName());
						reqDTO.setDPTNO(result.getDepId());
						reqDTO.setEMPNAME(result.getName());
						reqDTO.setEMPNO(result.getBadge());
						reqDTO.setPlanPutMoneyValue(result.getAccount().doubleValue());
						reqDTOS.add(reqDTO);
						Result<Boolean> b = remoteXfPlanMoneyCardService.saveSinglePlan(reqDTO, SecurityConstants.FROM_IN);
						if (OneOrZeroEnum.ZERO.getCode().equals(b.getCode())) {
							if (b.getData().equals(Boolean.TRUE)) {
								this.baseMapper.updateStateById(result.getId());
								continue;
							}
//								this.update(Wrappers.<SmtStaffRecharge>update().lambda().in(SmtStaffRecharge::getBadge, b.getData())
//										.eq(SmtStaffRecharge::getCheckMonth, req.getCheckMonth()).set(SmtStaffRecharge::getSyncStatus, 1));
							errorBadge.add(result.getBadge());
						}
					}
				}
				if (CollUtil.isNotEmpty(errorBadge)) {
					String errorBadges = StringUtils.join(errorBadge, "，");
					this.destroyKey("toC6");
					return "工号为：" + errorBadges + "的员工厂牌状态有误或已充值，请核对后同步";
				}
			}
		} catch (Exception e) {
			this.destroyKey("toC6");
			throw new TCEException(e.getMessage());
		}
		this.destroyKey("toC6");
		return null;
	}

	@Override
	public String genSerialNumber() {
		LocalDateTime now = LocalDateTime.now();
		String fileName = RechargeDownConstants.RECHARGE_SERIAL_NUM + loFormatter.format(LocalDate.now());
		String serialNum = SymbolConstants.BLANK;
		ValueOperations<String, String> value = redisTemplate.opsForValue();
		try {
			if (value.get(fileName) != null) {
				serialNum = value.get(fileName);
				Integer num = Integer.parseInt(serialNum) + NumberConstants.ONE;
				serialNum = String.format("%3d", num).replace(SymbolConstants.BLANK, NumberConstants.STRING_ZERO);
				value.set(fileName, serialNum, NumberConstants.MINUTES_OF_DAY, TimeUnit.MINUTES);
			} else {
				Integer time = now.getMinute() + now.getHour() * 60;
				serialNum = NumberConstants.SERIAL_NUM;
				value.set(fileName, serialNum, NumberConstants.MINUTES_OF_DAY - time, TimeUnit.MINUTES);
			}
		} catch (Exception e) {
			log.error("生成序列号异常：{}", e);
		}
		return fileName + serialNum;
	}

	public Boolean syncOldStaff() {
		LocalDateTime preLocalDate = DateUtils.middleNight(LocalDateTime.now().plusMonths(-1));
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		//判断是否已生成在职员工当月数据
        return Objects.nonNull(this.checkMonthList(preLocalDate, RechargeTypeEnum.SENIOR_EMPLOYEE.getCode(), parkIds));
    }

	/**
	 * 获取所有当月数据
	 *
	 * @return
	 */
	private List<SmtStaffRecharge> getMouthList(LocalDateTime date, Integer type) {
		return this.baseMapper.getMouthList(date, type);
	}

	/**
	 * 获取所有当月数据
	 *
	 * @return
	 */
	private SmtStaffRecharge checkMonthList(LocalDateTime date, Integer type, List<Integer> parkIds) {
		return this.baseMapper.checkMonthList(date, type, parkIds);
	}


	/**
	 * 计算新入职员工工时与餐补
	 *
	 * @param staff    员工信息
	 * @param recharge 充值名单信息
	 */
	private void setNewStaffStandard(SmtStaff staff, SmtStaffRecharge recharge) {
		//获得应出勤与实出勤
		if (!this.getWordDay(staff, recharge)) {
			return;
		}
		//计算餐补
		if (Objects.nonNull(staff.getPzid())) {
			Result<EvwCcdFlstandardDTO> standardResult = remoteEvwCcdFlstandardService.getById(staff.getJcheId(), staff.getPzid(), SecurityConstants.FROM_IN);
			if (Objects.nonNull(standardResult.getData())) {
				String standards = standardResult.getData().getStandard();
				if (Objects.isNull(standards) || StringUtils.isBlank(standards) || standards.equals("")) {
					recharge.setBlank("餐补数据为空");
					log.info("餐补数据为空，请注意检查{}", standardResult.getData());
					return;
				}
				BigDecimal standard = new BigDecimal(standards);
				recharge.setStandard(standard);
				recharge.setAccount(standard.multiply(BigDecimal.valueOf(recharge.getActualOn())).divide(BigDecimal.valueOf(recharge.getShouldOn()),
						NumberConstants.ONE, RoundingMode.HALF_DOWN));
				recharge.setBlank(" ");
				return;
			}
			recharge.setBlank("餐补数据为空");
			return;
		}
		recharge.setBlank("pzid为空");
    }

	/**
	 * 计算在职职员工工时与餐补
	 *
	 * @param staff    员工信息
	 * @param recharge 充值名单信息
	 */
	private void setSeniorStaffStandard(SmtStaff staff, SmtStaffRecharge recharge, AvaGetskyPayYSHRDTO ava, Map<String, BigDecimal> flStandard) {
		if (Objects.nonNull(ava.getA7()) && Objects.nonNull(ava.getA6())) {
			String createMonth = formatter.format(staff.getCreateTime());
			//计算餐补
			if (Objects.nonNull(staff.getPzid())) {
				String reMonth = monthFormatter.format(LocalDateTime.now().plusMonths(-1));
				//检测是否为上月入职的新员工
				if (createMonth.equals(reMonth)) {
					recharge.setActualOn(ava.getA6());
				} else {
					recharge.setActualOn(ava.getA7());
				}
				recharge.setShouldOn(ava.getA6());
				String sKey = staff.getJcheId() + "_" + staff.getPzid();
				if (flStandard.containsKey(sKey)) {
					BigDecimal standard = flStandard.get(sKey);
					recharge.setStandard(standard);
					recharge.setAccount(standard.multiply(BigDecimal.valueOf(recharge.getActualOn())).divide(BigDecimal.valueOf(recharge.getShouldOn()),
							NumberConstants.ONE, RoundingMode.HALF_DOWN));
					recharge.setBlank(" ");
					return;
				} else {
					Result<EvwCcdFlstandardDTO> standardResult = remoteEvwCcdFlstandardService.getById(staff.getJcheId(), staff.getPzid(), SecurityConstants.FROM_IN);
					if (Objects.nonNull(standardResult.getData())) {
						String standards = standardResult.getData().getStandard();
						if (Objects.isNull(standards) || StringUtils.isBlank(standards) || standards.equals("")) {
							recharge.setBlank("餐补数据为空");
							return;
						}
						BigDecimal standard = new BigDecimal(standards);
						recharge.setStandard(standard);
						recharge.setAccount(standard.multiply(BigDecimal.valueOf(recharge.getActualOn())).divide(BigDecimal.valueOf(recharge.getShouldOn()),
								NumberConstants.ONE, RoundingMode.HALF_DOWN));
						recharge.setBlank(" ");
						flStandard.put(sKey, standard);
						return;
					}
				}
				recharge.setBlank("餐补数据为空");
				return;
			}
			recharge.setBlank("pzid为空");
			return;
		}
		recharge.setBlank("考勤数据异常");
    }

	/**
	 * 龙岗员工充值
	 *
	 * @param staff
	 * @param recharge
	 * @param preAva
	 * @param flStandard
	 */
	private void setSeniorStaffStandardForLG(SmtStaff staff, SmtStaffRecharge recharge, AvaGetskyPayYSHRDTO preAva, Map<String, BigDecimal> flStandard, List<DateTime> dateTimes) {
		if (Objects.nonNull(staff.getPzid())) {
			//获得节假日安排
			Result<WorkTimeRespDTO> result = remoteRsEmpService.getFreeDays(staff.getBadge(), SecurityConstants.FROM_IN);
			if (Objects.nonNull(result.getData()) && CollUtil.isNotEmpty(result.getData().getTimes())) {
				//本月时间移除节假日，剩下工作日时间
				dateTimes.removeAll(result.getData().getTimes());
				//计算餐补
				if (Objects.nonNull(preAva.getA7()) && Objects.nonNull(preAva.getA6())) {
					recharge.setActualOn(Double.valueOf(dateTimes.size()));
					recharge.setShouldOn(preAva.getA6());
					String sKey = staff.getJcheId() + "_" + staff.getPzid();
					//上月缺勤天数
					Double subDay = preAva.getA7() - preAva.getA6();
					if (flStandard.containsKey(sKey)) {
						BigDecimal standard = flStandard.get(sKey);
						recharge.setStandard(standard);
						// 扣款金额=标准金额/本月正班*上月缺勤天数
						BigDecimal subAmount = standard.divide(new BigDecimal(dateTimes.size()), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(subDay));
						//本月充值金额=标准金额-扣款金额
						recharge.setAccount(standard.subtract(subAmount));
						recharge.setBlank(" ");
						return;
					} else {
						Result<EvwCcdFlstandardDTO> standardResult = remoteEvwCcdFlstandardService.getById(staff.getJcheId(), staff.getPzid(), SecurityConstants.FROM_IN);
						if (Objects.nonNull(standardResult.getData())) {
							String standards = standardResult.getData().getStandard();
							if (Objects.isNull(standards) || StringUtils.isBlank(standards) || standards.equals("")) {
								recharge.setBlank("餐补数据为空");
								return;
							}
							BigDecimal standard = new BigDecimal(standards);
							recharge.setStandard(standard);
							// 扣款金额=标准金额/本月正班*上月缺勤天数
							BigDecimal subAmount = standard.divide(new BigDecimal(dateTimes.size()), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(subDay));
							//本月充值金额=标准金额-扣款金额
							recharge.setAccount(standard.subtract(subAmount));
							recharge.setBlank(" ");
							flStandard.put(sKey, standard);
							return;
						}
					}
				}
				recharge.setBlank("餐补数据为空");
				return;
			}
			recharge.setBlank("节假日安排为空");
			return;
		}
		recharge.setBlank("pzid为空");
    }

	/**
	 * 方法运行时加锁
	 *
	 * @param key
	 * @return
	 */
	private Boolean checkKey(String key) {
		ValueOperations<String, String> value = redisTemplate.opsForValue();
		if (Objects.nonNull(value.get(key))) {
			throw new TCEException("数据处理中，请稍后");
		}
		value.set(key, "true", 12, TimeUnit.MINUTES);
		return Boolean.TRUE;
	}

	/**
	 * 方法执行完毕释放锁
	 *
	 * @param key
	 * @return
	 */
	private Boolean destroyKey(String key) {
		ValueOperations<String, String> value = redisTemplate.opsForValue();
		if (Objects.isNull(value.get(key))) {
			return Boolean.TRUE;
		}
		redisTemplate.delete(key);
		return Boolean.TRUE;
	}


	/**
	 * 填充查询参数
	 *
	 * @param reqDTO
	 * @return
	 */
	private RechargePageAO getQueryAo(RechargePageReqDTO reqDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		RechargePageAO ao = BeanUtils.transform(RechargePageAO.class, reqDTO);
		if (CollectionUtils.isNotEmpty(parkIds)) {
			ao.setParkIds(parkIds);
		}
		if (StringUtils.isNotBlank(reqDTO.getBadge())) {
			List<String> badgeList = ToolUtils.splitBlankString(reqDTO.getBadge());
			ao.setBadgeList(badgeList);
		}
		return ao;
	}

}
