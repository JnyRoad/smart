package com.tce.smart.platform.service.settlement.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.platform.api.dto.req.dailySd.DailyMeterQueryDTO;
import com.tce.smart.platform.api.dto.req.sddto.SdMeterreadDetailReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtSdMeterreadDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.SdMeterreadDetailChangeDTO;
import com.tce.smart.platform.api.dto.resp.dailySd.DailyMeterRespDTO;
import com.tce.smart.platform.core.dto.commonsd.StaffSDRuleRespDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtSDTemplatesMapper;
import com.tce.smart.platform.core.mapper.SmtSdMeterreadDetailDailyMapper;
import com.tce.smart.platform.helper.SdChangeHelper;
import com.tce.smart.platform.service.SmtDormitoryOutRemarkService;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.tce.smart.platform.service.SmtSdMeterreadDetailChangeService;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadDetailDailyService;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadDetailService;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadService;
import com.tce.smart.platform.service.settlement.SmtStaffStatementDetailDailyService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterHistoryService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterHistoryService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterService;
import com.tce.smart.platform.utils.NumberUtils;
import com.tce.smart.tool.enums.SDCategoryEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: SmtSdMeterreadServiceImpl
 * @date: 2020-07-10 9:46
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
@EnableAsync
public class SmtSdMeterreadDetailDailyServiceImpl extends ServiceImpl<SmtSdMeterreadDetailDailyMapper, SmtSdMeterreadDetailDaily> implements SmtSdMeterreadDetailDailyService {
	@Autowired
	private SmtDormitoryRoomService smtDormitoryRoomService;
	@Autowired
	private SmtSDTemplatesMapper smtSDTemplatesMapper;
	@Autowired
	private SmtDormitoryOutRemarkService smtDormitoryOutRemarkService;
	@Autowired
	private SmtDormitoryStaffService smtDormitoryStaffService;
	@Autowired
	private SmtEleMeterHistoryService smtEleMeterHistoryService;
	@Autowired
	private SmtWaterMeterHistoryService smtWaterMeterHistoryService;
	@Autowired
	private SmtStaffStatementDetailDailyService smtStaffStatementDetailDailyService;
	@Autowired
	private SmtSdMeterreadService smtSdMeterreadService;
	@Autowired
	private SmtSdMeterreadDetailService smtSdMeterreadDetailService;
	@Autowired
	private SmtSdMeterreadDetailChangeService smtSdMeterreadDetailChangeService;
	@Autowired
	private SdChangeHelper sdChangeHelper;

	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	@Override
	public List<DailyMeterRespDTO> getFloorSdMeterReadNew(DailyMeterQueryDTO queryDTO) {
		List<DailyMeterRespDTO> respList = this.baseMapper.getFloorSdMeterReadNew(queryDTO.getDormitoryId(),
				queryDTO.getFloorId(), queryDTO.getRoomId(), queryDTO.getStartTime(), queryDTO.getEndTime());
		if (CollUtil.isNotEmpty(respList)) {
			for (DailyMeterRespDTO resp : respList) {
				resp.setActEleNum(NumberUtils.doubleFormat(resp.getCurEleNum() - resp.getPreEleNum()));
				resp.setActWaterNum(NumberUtils.doubleFormat(resp.getCurColdNum() - resp.getPreColdNum()
						+ (resp.getCurHotNum() - resp.getPreHotNum())));
				resp.setCurEleNum(NumberUtils.doubleFormat(resp.getCurEleNum()));
				resp.setPreEleNum(NumberUtils.doubleFormat(resp.getPreEleNum()));
				resp.setCurColdNum(NumberUtils.doubleFormat(resp.getCurColdNum()));
				resp.setPreColdNum(NumberUtils.doubleFormat(resp.getPreColdNum()));
				resp.setCurHotNum(NumberUtils.doubleFormat(resp.getCurHotNum()));
				resp.setPreHotNum(NumberUtils.doubleFormat(resp.getPreHotNum()));
				calcActNum(resp);
				doubleFormat(resp);
			}
		}
		return respList;
	}

	/**
	 * 计算实际用量
	 *
	 * @param resp
	 */
	private void calcActNum(DailyMeterRespDTO resp) {
		if (Objects.isNull(resp.getMeterMonth())) {
			return;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(resp.getMeterMonth());
		int month = cal.get(Calendar.MONTH) + 1;
		SmtDormitoryRoom room = smtDormitoryRoomService.getById(resp.getRoomId());
		StaffSDRuleRespDTO eleRule = smtSDTemplatesMapper.getSDRuleById(room.getSdTemplateId(),
				SDCategoryEnum.ELECTRIC.getCode(), month);
		if (Objects.nonNull(eleRule)) {
			resp.setActEleNum(resp.getActEleNum() - eleRule.getStandardQty());
			if (resp.getActEleNum() < 0) {
				resp.setActEleNum(0.0);
			}
		}
		StaffSDRuleRespDTO coldRule = smtSDTemplatesMapper.getSDRuleById(room.getSdTemplateId(),
				SDCategoryEnum.COLD_WATER.getCode(), month);
		if (Objects.nonNull(coldRule)) {
			resp.setActWaterNum(resp.getActWaterNum() - coldRule.getStandardQty());
			if (resp.getActWaterNum() < 0) {
				resp.setActWaterNum(0.0);
			}
		}
	}

	/**
	 * 格式化
	 *
	 * @param resp
	 */
	private void doubleFormat(DailyMeterRespDTO resp) {
		resp.setPreEleNum(NumberUtils.doubleFormat(resp.getPreEleNum()));
		resp.setCurEleNum(NumberUtils.doubleFormat(resp.getCurEleNum()));
		resp.setActEleNum(NumberUtils.doubleFormat(resp.getActEleNum()));
		resp.setPreColdNum(NumberUtils.doubleFormat(resp.getPreColdNum()));
		resp.setCurColdNum(NumberUtils.doubleFormat(resp.getCurColdNum()));
		resp.setPreHotNum(NumberUtils.doubleFormat(resp.getPreHotNum()));
		resp.setCurHotNum(NumberUtils.doubleFormat(resp.getCurHotNum()));
		resp.setActWaterNum(NumberUtils.doubleFormat(resp.getActWaterNum()));
	}

	@Async
	@Override
	public void genDailyRecord() {
		log.info("每日水电结算开始");
		try {
			LocalDate nowDate = LocalDate.now();
			Date todayDate = new Date();
			DateTime startDate = DateUtil.beginOfDay(todayDate);
			Date beginDay = DateUtil.beginOfMonth(todayDate);
			Date preDate = DateUtil.offsetDay(todayDate, -1);
			List<SmtDormitoryRoom> rooms = smtDormitoryRoomService.list(Wrappers.<SmtDormitoryRoom>lambdaQuery().eq(SmtDormitoryRoom::getParkId, xcParkId));
			if (CollUtil.isEmpty(rooms)) {
				return;
			}
			List<Integer> roomIds = rooms.stream().map(SmtDormitoryRoom::getId).collect(Collectors.toList());
			List<Integer> dormitoryIdList = new ArrayList<>();
			for (SmtDormitoryRoom room : rooms) {
				List<SmtDormitoryStaff> smtDormitoryStaffs = smtDormitoryStaffService.list(Wrappers.<SmtDormitoryStaff>query()
						.lambda().eq(SmtDormitoryStaff::getRoomId, room.getId()));
				dormitoryIdList.add(room.getDormitoryId());
				//计算电费
				BigDecimal eleFee = this.getEleCount(nowDate, room);
				//计算水费
				BigDecimal waterFee = this.getWaterCount(nowDate, room);
				//计算个人总费用
				for (SmtDormitoryStaff staff : smtDormitoryStaffs) {
					//当天是否属于备注天数内
					Integer isRemarkDay = smtDormitoryOutRemarkService.getRemarkDate(staff.getId(), null, todayDate, preDate);
					if (isRemarkDay > 0) {
//						return;
						continue;
					}
					//查询当月备注天数
					Integer remarkDay = smtDormitoryOutRemarkService.getRemarkDate(staff.getId(), null, beginDay, todayDate);
					SmtStaffStatementDetailDaily staffDaily = smtStaffStatementDetailDailyService.getOne(Wrappers.<SmtStaffStatementDetailDaily>lambdaQuery()
							.eq(SmtStaffStatementDetailDaily::getStaffBadge, staff.getStaffBadge())
							.eq(SmtStaffStatementDetailDaily::getMeterMonth, startDate));
					if (Objects.nonNull(staffDaily)) {
						staffDaily.setFee(eleFee.add(waterFee));
						staffDaily.setStayDays(1);
						staffDaily.setRemarkDays(remarkDay);
						staffDaily.updateById();
						continue;
					}
					staffDaily = BeanUtils.transform(SmtStaffStatementDetailDaily.class, staff);
					if (Objects.nonNull(staffDaily) && Objects.nonNull(staffDaily.getStaffBadge())) {
						staffDaily.setInTime(staff.getCreateTime());
						staffDaily.setStayDays(1);
						staffDaily.setRemarkDays(remarkDay);
						staffDaily.setFee(eleFee.add(waterFee));
						staffDaily.setMeterMonth(startDate);
						staffDaily.insert();
					}
				}
			}
			// 覆盖当月房间对应的读数，并计算费用
			dormitoryIdList = dormitoryIdList.stream().distinct().collect(Collectors.toList());
			calRoomRead(dormitoryIdList, roomIds);
		} catch (Exception e) {
			log.error("每日水电结算异常", e);
		}
		log.info("每日水电结算结束");
	}

	/**
	 * 电抄表结算
	 *
	 * @param nowDate
	 * @param room
	 * @return
	 */
	private BigDecimal getEleCount(LocalDate nowDate, SmtDormitoryRoom room) {
		StaffSDRuleRespDTO eleRule = smtSDTemplatesMapper.getSDRuleById(room.getSdTemplateId(),
				SDCategoryEnum.ELECTRIC.getCode(), nowDate.getMonthValue());
		double eleNum = getNumCount(nowDate, room, SDCategoryEnum.ELECTRIC.getCode());
		if (Objects.nonNull(eleRule)) {
			return BigDecimal.valueOf(eleRule.getStandardQty()).multiply(new BigDecimal(eleNum - eleRule.getStandardQty()));
		}
		return BigDecimal.ZERO;
	}

	/**
	 * 水抄表结算
	 *
	 * @param nowDate
	 * @param room
	 * @return
	 */
	private BigDecimal getWaterCount(LocalDate nowDate, SmtDormitoryRoom room) {
		// 查询房间配置的模板
		StaffSDRuleRespDTO coldRule = smtSDTemplatesMapper.getSDRuleById(room.getSdTemplateId(),
				SDCategoryEnum.COLD_WATER.getCode(), nowDate.getMonthValue());
		double coldNum = getNumCount(nowDate, room, SDCategoryEnum.COLD_WATER.getCode());
		double hotNum = getNumCount(nowDate, room, SDCategoryEnum.HOT_WATER.getCode());
		if (Objects.nonNull(coldRule)) {
			return BigDecimal.valueOf(coldRule.getStandardQty()).multiply(new BigDecimal(coldNum + hotNum - coldRule.getStandardQty()));
		}
		return BigDecimal.ZERO;
	}

	/**
	 * 水电抄表读数
	 *
	 * @param nowDate
	 * @param room
	 * @param categoryId
	 * @return
	 */
	private Double getNumCount(LocalDate nowDate, SmtDormitoryRoom room, Integer categoryId) {
		Integer roomId = room.getId();
		SmtSdMeterreadDetailDaily daily = this.getOne(Wrappers.<SmtSdMeterreadDetailDaily>lambdaQuery()
				.eq(SmtSdMeterreadDetailDaily::getCategoryId, categoryId)
				.eq(SmtSdMeterreadDetailDaily::getRoomId, roomId)
				.eq(SmtSdMeterreadDetailDaily::getCreateTime, nowDate)
		);
		if (Objects.isNull(daily)) {
			daily = new SmtSdMeterreadDetailDaily();
		}
		daily.setCategoryId(categoryId);
		daily.setRoomId(roomId);
		daily.setRoomName(room.getRoomName().toString());
		daily.setCreateTime(nowDate.atStartOfDay());
		Long meterId = sdChangeHelper.getMeterId(roomId, categoryId);
		if (Objects.isNull(meterId)) {
			daily.setRemark("未配置" + SDCategoryEnum.desc(categoryId) + "表");
			if (Objects.nonNull(daily.getId())) {
				daily.updateById();
			} else {
				daily.insert();
			}
			return 0.0;
		}
		SmtSdMeterreadDetailDaily lastMonthDaily = this.getOne(Wrappers.<SmtSdMeterreadDetailDaily>lambdaQuery()
				.eq(SmtSdMeterreadDetailDaily::getCategoryId, categoryId)
				.eq(SmtSdMeterreadDetailDaily::getRoomId, roomId)
				.eq(SmtSdMeterreadDetailDaily::getCreateTime,
						DateUtils.parseLocalDateTime(DateUtil.endOfMonth(DateUtils.lastMonth()).toDateStr() + DateUtils.POSTFEX))
		);
		// 判断是否换表
		Long beforeMeterId;
		if (Objects.nonNull(lastMonthDaily)) {
			beforeMeterId = Objects.nonNull(daily.getMeterId()) ? daily.getMeterId() : lastMonthDaily.getMeterId();
		} else {
			beforeMeterId = daily.getMeterId();
		}
		if (Objects.nonNull(beforeMeterId) && !meterId.equals(beforeMeterId)) {
			DateTime meterMonth = DateUtil.beginOfMonth(new Date());
			SmtSdMeterreadDetailRespDTO respDTO = smtSdMeterreadDetailService.getPreMonthDetail(roomId, meterMonth);
			// 上个月结算的读数
			double preNum = 0;
			if (Objects.nonNull(respDTO)) {
				for (SmtSdMeterreadDetailRespDTO.MeterReadDetail meterReadDetail : respDTO.getMeterReadDetailList()) {
					if (categoryId.equals(meterReadDetail.getCategoryId())) {
						preNum = meterReadDetail.getCurMonthNum();
					}
				}
			}
			List<SdMeterreadDetailChangeDTO> detailChanges = smtSdMeterreadDetailChangeService.getList(meterMonth, roomId);
			if (CollUtil.isNotEmpty(detailChanges)) {
				preNum = sdChangeHelper.getMeterInitNum(beforeMeterId, nowDate.withDayOfMonth(1), categoryId);
			}
			SmtSdMeterreadDetailChange meterreadDetailChange = new SmtSdMeterreadDetailChange();
			meterreadDetailChange.setPreMonthNum(preNum);
			meterreadDetailChange.setCurMonthNum(this.getMeterRangeNum(beforeMeterId, nowDate, nowDate, categoryId));
			meterreadDetailChange.setCategoryId(categoryId);
			meterreadDetailChange.setRoomId(roomId);
			meterreadDetailChange.setMeterMonth(meterMonth);
			// 换了表，增加之前表的水电表结算
			smtSdMeterreadDetailChangeService.save(meterreadDetailChange);
		}
		daily.setMeterId(meterId);
		daily.setPreNum(this.getMeterRangeNum(meterId, nowDate.plusDays(-1), nowDate.plusDays(-1), categoryId));
		daily.setCurNum(this.getMeterRangeNum(meterId, nowDate, nowDate, categoryId));
		if (Objects.nonNull(daily.getId())) {
			daily.updateById();
		} else {
			daily.insert();
		}
		if (Objects.nonNull(lastMonthDaily)) {
			return daily.getCurNum() - lastMonthDaily.getCurNum();
		} else {
			return daily.getCurNum();
		}
	}

	/**
	 * 获取电表水表范围内读数
	 *
	 * @return
	 */
	private Double getMeterRangeNum(Long meterId, LocalDate firstDay, LocalDate lastDay, Integer type) {
		if (SDCategoryEnum.ELECTRIC.getCode().equals(type)) {
			return smtEleMeterHistoryService.getMaxMeterReading(meterId, firstDay, lastDay);
		}
		if (SDCategoryEnum.COLD_WATER.getCode().equals(type) || SDCategoryEnum.HOT_WATER.getCode().equals(type)) {
			return smtWaterMeterHistoryService.getMaxMeterReading(meterId, firstDay, lastDay);
		}
		return 0.0;
	}

	public void calRoomRead(List<Integer> dormitoryIdList, List<Integer> roomIds) {
		log.info("覆盖当月房间对应的读数，并计算费用-开始");
		long start = System.currentTimeMillis();
		try {
			DateTime startMonth = DateUtil.beginOfMonth(new Date());
			LocalDate nowDate = LocalDate.now();
			for (Integer roomId : roomIds) {
				SdMeterreadDetailReqDTO reqDTO = new SdMeterreadDetailReqDTO();
				List<SdMeterreadDetailReqDTO.MeterReadDetail> meterReadDetailList = new ArrayList<>();
				reqDTO.setRoomId(roomId);
				reqDTO.setMeterMonth(startMonth);
				SdMeterreadDetailReqDTO.MeterReadDetail hotWater = new SdMeterreadDetailReqDTO.MeterReadDetail();
				SdMeterreadDetailReqDTO.MeterReadDetail coldWater = new SdMeterreadDetailReqDTO.MeterReadDetail();
				SdMeterreadDetailReqDTO.MeterReadDetail electric = new SdMeterreadDetailReqDTO.MeterReadDetail();
				hotWater.setCategoryId(SDCategoryEnum.HOT_WATER.getCode());
				hotWater.setCurMonthNum(getDayRead(SDCategoryEnum.HOT_WATER.getCode(), roomId, nowDate));

				coldWater.setCategoryId(SDCategoryEnum.COLD_WATER.getCode());
				coldWater.setCurMonthNum(getDayRead(SDCategoryEnum.COLD_WATER.getCode(), roomId, nowDate));

				electric.setCategoryId(SDCategoryEnum.ELECTRIC.getCode());
				electric.setCurMonthNum(getDayRead(SDCategoryEnum.ELECTRIC.getCode(),roomId, nowDate));
				SmtSdMeterreadDetailRespDTO respDTO = smtSdMeterreadDetailService.getPreMonthDetail(roomId, startMonth);
				if (Objects.nonNull(respDTO)) {
					for (SmtSdMeterreadDetailRespDTO.MeterReadDetail meterReadDetail : respDTO.getMeterReadDetailList()) {
						if (SDCategoryEnum.HOT_WATER.getCode().equals(meterReadDetail.getCategoryId())) {
							hotWater.setPreMonthNum(meterReadDetail.getCurMonthNum());
						} else if (SDCategoryEnum.COLD_WATER.getCode().equals(meterReadDetail.getCategoryId())) {
							coldWater.setPreMonthNum(meterReadDetail.getCurMonthNum());
						} else {
							electric.setPreMonthNum(meterReadDetail.getCurMonthNum());
						}
					}
				} else {
					hotWater.setPreMonthNum(0.0);
					coldWater.setPreMonthNum(0.0);
					electric.setPreMonthNum(0.0);
				}
				meterReadDetailList.add(hotWater);
				meterReadDetailList.add(coldWater);
				meterReadDetailList.add(electric);
				reqDTO.setMeterReadDetailList(meterReadDetailList);
				smtSdMeterreadDetailService.saveMeterReadDetail(reqDTO, smtSdMeterreadService);
			}
			for (Integer dormitoryId : dormitoryIdList) {
				smtSdMeterreadService.generateSDStatementDetail(dormitoryId);
			}
		} catch (Exception e) {
			log.error("覆盖当月房间对应的读数，并计算费用-异常", e);
		}
		log.info("覆盖当月房间对应的读数，并计算费用-结束，耗时：{}", System.currentTimeMillis() - start);
	}

	/**
	 * 获取房间当日读数最大值
	 */
	private Double getDayRead(Integer categoryId, Integer roomId, LocalDate date) {
		List<SmtSdMeterreadDetailDaily> detailDailies = this.list(Wrappers.<SmtSdMeterreadDetailDaily>lambdaQuery()
				.eq(SmtSdMeterreadDetailDaily::getCategoryId, categoryId)
				.eq(SmtSdMeterreadDetailDaily::getRoomId, roomId)
				.eq(SmtSdMeterreadDetailDaily::getCreateTime, date)
		);
		detailDailies = detailDailies.stream()
				.filter(e -> Objects.nonNull(e.getCurNum())).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(detailDailies)) {
			return detailDailies.stream()
					.mapToDouble(SmtSdMeterreadDetailDaily::getCurNum)
					.max()
					.getAsDouble();
		}
		return 0.0;
	}
}
