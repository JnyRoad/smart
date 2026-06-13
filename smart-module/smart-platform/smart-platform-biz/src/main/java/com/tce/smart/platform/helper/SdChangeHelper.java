package com.tce.smart.platform.helper;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.resp.commonsd.DormitorySDMeterreadNewRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.SdMeterreadDetailChangeDTO;
import com.tce.smart.platform.core.dto.meter.MeterReadHisDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeter;
import com.tce.smart.platform.emun.LargeClassEnum;
import com.tce.smart.platform.service.SmtSdMeterreadDetailChangeService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterHistoryService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterHistoryService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterService;
import com.tce.smart.platform.utils.NumberUtils;
import com.tce.smart.tool.enums.SDCategoryEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 18:00
 */
@Component
public class SdChangeHelper {

	@Autowired
	private SmtSdMeterreadDetailChangeService smtSdMeterreadDetailChangeService;
	@Autowired
	private SmtEleMeterHistoryService smtEleMeterHistoryService;
	@Autowired
	private SmtWaterMeterHistoryService smtWaterMeterHistoryService;
	@Autowired
	private SmtWaterMeterService smtWaterMeterService;
	@Autowired
	private SmtEleMeterService smtEleMeterService;
	/**
	 * 水电表生产阈值
	 */
	@Value("${smart.meter.prod}")
	private Integer prod;
	/**
	 * 水电表生活阈值
	 */
	@Value("${smart.meter.life}")
	private Integer life;

	/**
	 * 读取换表结算的数据
	 *
	 * @param respDTOList
	 */
	public void readSdChange(List<DormitorySDMeterreadNewRespDTO> respDTOList) {
		for (DormitorySDMeterreadNewRespDTO respDTO : respDTOList) {
			List<SdMeterreadDetailChangeDTO> changeList = smtSdMeterreadDetailChangeService.getList(respDTO.getMeterMonth(), respDTO.getRoomId());
			if (CollUtil.isNotEmpty(changeList)) {
				StringBuilder remark = new StringBuilder();
				for (SdMeterreadDetailChangeDTO detailChange : changeList) {
					if (StringUtils.isNotBlank(remark)) {
						remark.append(",");
					}
					Integer categoryId = detailChange.getCategoryId();
					remark.append(DateUtils.format(detailChange.getCreateTime())).append("更换过")
							.append(SDCategoryEnum.desc(categoryId)).append("表");
					Long meterId = this.getMeterId(respDTO.getRoomId(), categoryId);
					LocalDate meterMonth = detailChange.getCreateTime().toLocalDate().withDayOfMonth(1);
					if (SDCategoryEnum.ELECTRIC.getCode().equals(categoryId)) {
						respDTO.setEleChangePreMonthNum(this.getMeterInitNum(meterId, meterMonth, categoryId));
					} else if (SDCategoryEnum.COLD_WATER.getCode().equals(categoryId)) {
						respDTO.setColdChangePreMonthNum(this.getMeterInitNum(meterId, meterMonth, categoryId));
					} else if (SDCategoryEnum.HOT_WATER.getCode().equals(categoryId)) {
						respDTO.setHotChangePreMonthNum(this.getMeterInitNum(meterId, meterMonth, categoryId));
					}
				}
				respDTO.setRemark(remark.toString());
			}
			respDTO.setChangeList(changeList);
		}
	}

	/**
	 * 通过房间id和类型获取水电表id
	 *
	 * @param roomId
	 * @param categoryId
	 * @return
	 */
	public Long getMeterId(Integer roomId, Integer categoryId) {
		Long meterId = null;
		if (SDCategoryEnum.ELECTRIC.getCode().equals(categoryId)) {
			SmtEleMeter eleMeter = smtEleMeterService.getOne(Wrappers.<SmtEleMeter>query().lambda().eq(SmtEleMeter::getRoomId, roomId), false);
			if (Objects.nonNull(eleMeter)) {
				meterId = eleMeter.getId();
			}
		} else if (SDCategoryEnum.COLD_WATER.getCode().equals(categoryId)) {
			SmtWaterMeter waterMeter = smtWaterMeterService.getOne(Wrappers.<SmtWaterMeter>query().lambda()
					.eq(SmtWaterMeter::getLargeClass, LargeClassEnum.COLD.getCode())
					.eq(SmtWaterMeter::getRoomId, roomId), false);
			if (Objects.nonNull(waterMeter)) {
				meterId = waterMeter.getId();
			}
		} else {
			SmtWaterMeter waterMeter = smtWaterMeterService.getOne(Wrappers.<SmtWaterMeter>query().lambda()
					.eq(SmtWaterMeter::getLargeClass, LargeClassEnum.HOT.getCode())
					.eq(SmtWaterMeter::getRoomId, roomId), false);
			if (Objects.nonNull(waterMeter)) {
				meterId = waterMeter.getId();
			}
		}
		return meterId;
	}

	/**
	 * 获取电表水表起始读数
	 *
	 * @return
	 */
	public Double getMeterInitNum(Long meterId, LocalDate meterMonth, Integer type) {
		if (SDCategoryEnum.ELECTRIC.getCode().equals(type)) {
			return smtEleMeterHistoryService.getInitMeterReading(meterId, meterMonth);
		}
		if (SDCategoryEnum.COLD_WATER.getCode().equals(type) || SDCategoryEnum.HOT_WATER.getCode().equals(type)) {
			return smtWaterMeterHistoryService.getInitMeterReading(meterId, meterMonth);
		}
		return 0.0;
	}

	/**
	 * 根据阈值读数
	 *
	 * @param records
	 * @return
	 */
	public Double reading(List<MeterReadHisDTO> records) {
		for (int i = 0; i < records.size(); i++) {
			double first = NumberUtils.transDouble(records.get(i).getCurrentReading());
			if (i < records.size() - 1) {
				double second = NumberUtils.transDouble(records.get(i + 1).getCurrentReading());
				// 两个值差值小于水表生活阈值
				if (first - second < life) {
					return first;
				}
			} else {
				return first;
			}
		}
		return 0.0;
	}
}
