package com.tce.smart.platform.biz;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.SmartPlatformApplication;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterHistory;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterHistory;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadDetailService;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterHistoryService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterHistoryService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterService;
import com.tce.smart.tool.enums.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @description: AddCarTest
 * @date: 2020/11/25 19:54
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = SmartPlatformApplication.class)
public class XcSdTest {

	@Resource
	private SmtDormitoryRoomService smtDormitoryRoomService;

	@Resource
	private SmtSdMeterreadService smtSdMeterreadService;

	@Resource
	private SmtSdMeterreadDetailService smtSdMeterreadDetailService;

	@Resource
	private SmtWaterMeterService smtWaterMeterService;

	@Resource
	private SmtEleMeterService smtEleMeterService;

	@Resource
	private SmtWaterMeterHistoryService smtWaterMeterHistoryService;

	@Resource
	private SmtEleMeterHistoryService smtEleMeterHistoryService;

	@Value("${smart.xc-park-id:0}")
	private Integer xcParkId;

	//@Test
	public void test1(){
		// 查询许昌的房间
		List<SmtDormitoryRoom> rooms = smtDormitoryRoomService.list(Wrappers.<SmtDormitoryRoom>lambdaQuery().eq(SmtDormitoryRoom::getParkId, xcParkId));
		if (CollUtil.isEmpty(rooms)) {
			return;
		}
		String mm = "2022-09-01 00:00:00";
		DateTime dateTime = DateUtil.parse(mm);
		for(SmtDormitoryRoom room : rooms){

			if(room.getId() == 5013497){
				int a = 1;
			}

			// 查询房间的抄表记录
			SmtSdMeterread sdMeterread = smtSdMeterreadService.getOne(new LambdaQueryWrapper<SmtSdMeterread>()
					.eq(SmtSdMeterread::getRoomId, room.getId())
					.eq(SmtSdMeterread::getMeterMonth, dateTime)
			);

			if(Objects.isNull(sdMeterread)){
				continue;
			}

			//查找房间的抄表详情
			List<SmtSdMeterreadDetail> meterreadDetails = smtSdMeterreadDetailService.list(new LambdaQueryWrapper<SmtSdMeterreadDetail>()
					.eq(SmtSdMeterreadDetail::getMrId, sdMeterread.getId())
			);
			DateTime dateTime930 = DateUtil.parse("2022-09-30");
			DateTime dateTime101 = DateUtil.parse("2022-10-01");
			for(SmtSdMeterreadDetail detail : meterreadDetails){
				if(SDCategoryEnum.HOT_WATER.getCode().equals(detail.getCategoryId())){
					// 热水
					SmtWaterMeter waterMeter = smtWaterMeterService.getOne(new LambdaQueryWrapper<SmtWaterMeter>()
							.eq(SmtWaterMeter::getRoomId, room.getId())
							.like(SmtWaterMeter::getName, "热水表")
							.eq(SmtWaterMeter::getIsDelete, DeleteStatusEnum.NOT_DELETE.getCode())
					);

					if(Objects.isNull(waterMeter)){
						continue;
					}

					//查询9月最后一条抄表记录
					List<SmtWaterMeterHistory> waterMeterHistories = smtWaterMeterHistoryService.list(new LambdaQueryWrapper<SmtWaterMeterHistory>()
							.eq(SmtWaterMeterHistory::getWaterMeterId, waterMeter.getId())
							.between(SmtWaterMeterHistory::getCreateTime, dateTime930, dateTime101)
							.orderByDesc(SmtWaterMeterHistory::getCreateTime)
					);
					if(CollectionUtil.isEmpty(waterMeterHistories)){
						continue;
					}
					detail.setCurMonthNum(Double.parseDouble(waterMeterHistories.get(0).getCurrentReading()));
					smtSdMeterreadDetailService.updateById(detail);
				} else if(SDCategoryEnum.COLD_WATER.getCode().equals(detail.getCategoryId())){
					// 冷水
					SmtWaterMeter waterMeter = smtWaterMeterService.getOne(new LambdaQueryWrapper<SmtWaterMeter>()
							.eq(SmtWaterMeter::getRoomId, room.getId())
							.like(SmtWaterMeter::getName, "冷水表")
							.eq(SmtWaterMeter::getIsDelete, DeleteStatusEnum.NOT_DELETE.getCode())
					);

					if(Objects.isNull(waterMeter)){
						continue;
					}

					//查询9月最后一条抄表记录
					List<SmtWaterMeterHistory> waterMeterHistories = smtWaterMeterHistoryService.list(new LambdaQueryWrapper<SmtWaterMeterHistory>()
							.eq(SmtWaterMeterHistory::getWaterMeterId, waterMeter.getId())
							.between(SmtWaterMeterHistory::getCreateTime, dateTime930, dateTime101)
							.orderByDesc(SmtWaterMeterHistory::getCreateTime)
					);
					if(CollectionUtil.isEmpty(waterMeterHistories)){
						continue;
					}
					detail.setCurMonthNum(Double.parseDouble(waterMeterHistories.get(0).getCurrentReading()));
					smtSdMeterreadDetailService.updateById(detail);
				} else if(SDCategoryEnum.ELECTRIC.getCode().equals(detail.getCategoryId())){
					// 电
					SmtEleMeter smtEleMeter = smtEleMeterService.getOne(new LambdaQueryWrapper<SmtEleMeter>()
							.eq(SmtEleMeter::getRoomId, room.getId())
							.eq(SmtEleMeter::getIsDelete, DeleteStatusEnum.NOT_DELETE.getCode())
					);
					if(Objects.isNull(smtEleMeter)){
						continue;
					}

					//查询9月最后一条抄表记录
					List<SmtEleMeterHistory> eleMeterHistories = smtEleMeterHistoryService.list(new LambdaQueryWrapper<SmtEleMeterHistory>()
							.eq(SmtEleMeterHistory::getEleMeterId, smtEleMeter.getId())
							.between(SmtEleMeterHistory::getCreateTime, dateTime930, dateTime101)
							.orderByDesc(SmtEleMeterHistory::getCreateTime)
					);

					if(CollectionUtil.isEmpty(eleMeterHistories)){
						continue;
					}

					SmtEleMeterHistory smtEleMeterHistory = eleMeterHistories.get(0);
					String reading = String.valueOf(smtEleMeter.getRatio() * Double.parseDouble(smtEleMeterHistory.getCurrentReading()));
					detail.setCurMonthNum(Double.parseDouble(reading));
					smtSdMeterreadDetailService.updateById(detail);
				}
			}
		}
	}
}
