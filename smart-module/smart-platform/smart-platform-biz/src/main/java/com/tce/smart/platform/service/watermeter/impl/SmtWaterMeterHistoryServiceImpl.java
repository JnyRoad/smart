package com.tce.smart.platform.service.watermeter.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.NumberConstants;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterDataUpdateDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterHisQueryDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.WaterMeterHisRespDTO;
import com.tce.smart.platform.core.dto.meter.MeterReadHisDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterHistory;
import com.tce.smart.platform.core.mapper.watermeter.SmtWaterMeterHistoryMapper;
import com.tce.smart.platform.emun.ValveStatusEnum;
import com.tce.smart.platform.helper.SdChangeHelper;
import com.tce.smart.platform.service.energy.EnergyProjectionService;
import com.tce.smart.platform.service.energy.EnergyReadingIngestionService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterHistoryService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterService;
import com.tce.smart.platform.utils.NumberUtils;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import com.tce.smart.tool.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Objects;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:41
 */
@Slf4j
@Service
public class SmtWaterMeterHistoryServiceImpl extends ServiceImpl<SmtWaterMeterHistoryMapper, SmtWaterMeterHistory> implements SmtWaterMeterHistoryService {

	@Autowired
	private SmtWaterMeterService waterMeterService;
	@Autowired
	private SdChangeHelper sdChangeHelper;
	@Autowired
	private EnergyReadingIngestionService energyReadingIngestionService;
	@Autowired
	private EnergyProjectionService energyProjectionService;
	private static final DateTimeFormatter COLLECT_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);
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

	@Override
	public IPage<SmtWaterMeterHistory> getPage(Page page, WaterMeterHisQueryDTO dto) {
		return this.page(page, getWrapper(dto));
	}

	@Override
	public List<SmtWaterMeterHistory> getList(WaterMeterHisQueryDTO dto) {
		return this.list(getWrapper(dto));
	}

	private LambdaQueryWrapper getWrapper(WaterMeterHisQueryDTO dto) {
		return Wrappers.<SmtWaterMeterHistory>lambdaQuery()
				.eq(SmtWaterMeterHistory::getWaterMeterId, dto.getWaterMeterId())
				.eq(Objects.nonNull(dto.getIsError()), SmtWaterMeterHistory::getIsError, dto.getIsError())
				.ge(StringUtils.isNotEmpty(dto.getStartTime()), SmtWaterMeterHistory::getCollectTime,
						NumberUtils.convertTime(dto.getStartTime()))
				.le(StringUtils.isNotEmpty(dto.getEndTime()), SmtWaterMeterHistory::getCollectTime,
						NumberUtils.convertTime(dto.getEndTime()))
				.orderByDesc(SmtWaterMeterHistory::getCreateTime);
	}

	@Override
	public ResponseEntity<byte[]> exportHistory(Long waterMeterId) {
		WaterMeterHisQueryDTO dto = new WaterMeterHisQueryDTO();
		dto.setWaterMeterId(waterMeterId);
		List<SmtWaterMeterHistory> historyList = getList(dto);
		if (CollUtil.isEmpty(historyList)) {
			throw new TCEException(CommonConstants.SUCCESS, "查询无数据");
		}
		ResponseEntity<byte[]> responseEntity;
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), WaterMeterHisRespDTO.class, historyList)) {
			String fileName = "水表历史读数导出";
			responseEntity = IOUtils.getExcelResponse(fileName, workbook);
		} catch (IOException e) {
			log.error("excel导出异常", e);
			throw new TCEException(ExceptionEnum.UNKNOWN.getCode(), "excel导出异常");
		}
		return responseEntity;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveCurrentReading(WaterMeterDataUpdateDTO dto) {
		ReadingInput input = validateReading(dto == null ? null : dto.getDeviceCode(), dto == null ? null : dto.getWaterMeterSeq(),
				dto == null ? null : dto.getWaterMeterCurrVal(), dto == null ? null : dto.getCollectTime());
		SmtWaterMeter waterMeter = waterMeterService.getByConcentratorIdAndSeq(input.deviceCode, input.sequence);
		if (waterMeter == null) {
			throw new SmartException("水表信息不存在");
		}
		if (this.baseMapper.lockMeterForUpdate(waterMeter.getId()) == null) {
			throw new SmartException("水表信息不存在");
		}
		waterMeter = waterMeterService.getById(waterMeter.getId());
		if (waterMeter == null) {
			throw new SmartException("水表信息不存在");
		}
		Long historyId = IdWorker.getId();
		SmtWaterMeterHistory previous = this.baseMapper.selectPreviousByCollectTime(waterMeter.getId(), input.collectTime, historyId);
		String eventPayload = JSONUtil.toJsonStr(dto);
		EnergyReadingIngestionService.RegisterCommand command = new EnergyReadingIngestionService.RegisterCommand(dto.getSourceEventId(), "WATER_READ", "WATER",
				waterMeter.getParkId() == null ? null : waterMeter.getParkId().longValue(), waterMeter.getId(), dto.getDeviceCode(), dto.getWaterMeterSeq(), dto.getWaterMeterCurrVal(), dto.getValveState(), input.collectTime);
		if (!energyReadingIngestionService.register(command, energyReadingIngestionService.hashPayload(eventPayload), eventPayload)) {
			return Boolean.TRUE;
		}
		SmtWaterMeterHistory history = SmtWaterMeterHistory.builder()
				.id(historyId)
				.waterMeterId(waterMeter.getId())
				.collectTime(input.collectTime)
				.currentReading(input.reading.toPlainString())
				.isError(isError(previous == null ? null : previous.getCurrentReading(), input.reading) ? OneOrZeroEnum.ONE.getCode() : OneOrZeroEnum.ZERO.getCode())
				.build();
		if (!save(history)) {
			throw new SmartException("水表历史读数保存失败");
		}
		SmtWaterMeterHistory latest = this.baseMapper.selectLatestByCollectTime(waterMeter.getId());
		if (latest != null && historyId.equals(latest.getId()) && OneOrZeroEnum.ZERO.getCode().equals(history.getIsError())) {
			waterMeter.setCurrentReading(history.getCurrentReading());
			waterMeter.setIsOnline(NumberConstants.TWO);
			waterMeter.setIsOpen(dto.getValveState() == null ? ValveStatusEnum.NO_RELATION.getCode() : dto.getValveState());
			waterMeterService.updateById(waterMeter);
		}
		requestProjection("WATER", waterMeter.getId(), input.collectTime.toLocalDate());
		return Boolean.TRUE;
	}

	/** 同一读数会影响其所在日以及相邻两个日的边界。 */
	private void requestProjection(String source, Long meterId, LocalDate collectDate) {
		for (int offset = -1; offset <= 1; offset++) {
			energyProjectionService.requestProjection(source, meterId, collectDate.plusDays(offset));
		}
	}

	private boolean isError(String previousReading, BigDecimal currentReading) {
		if (previousReading == null) return false;
		BigDecimal previous;
		try {
			previous = new BigDecimal(previousReading);
		} catch (NumberFormatException ex) {
			log.warn("历史水表读数不是有效数值，当前读数标记异常");
			return true;
		}
		BigDecimal difference = currentReading.subtract(previous);
		if (difference.compareTo(BigDecimal.ZERO) < 0) return true;
		if (life == null || life <= 0) {
			log.warn("水表异常阈值未配置或不大于零，跳过增量阈值判断");
			return false;
		}
		return difference.compareTo(BigDecimal.valueOf(life)) > 0;
	}

	private ReadingInput validateReading(String deviceCode, Integer sequence, String reading, String collectTime) {
		if (deviceCode == null || deviceCode.trim().isEmpty() || sequence == null || sequence < 0 || reading == null || reading.trim().isEmpty() || collectTime == null) {
			throw new SmartException("水表读数参数不完整");
		}
		try {
			Long concentratorId = Long.parseLong(deviceCode);
			if (concentratorId < 0) throw new NumberFormatException();
			BigDecimal value = new BigDecimal(reading);
			if (value.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
			return new ReadingInput(concentratorId, sequence, value, LocalDateTime.parse(collectTime, COLLECT_TIME_FORMATTER));
		} catch (NumberFormatException | DateTimeParseException ex) {
			throw new SmartException("水表读数、集中器标识或采集时间格式不合法");
		}
	}

	private static class ReadingInput {
		private final Long deviceCode;
		private final Integer sequence;
		private final BigDecimal reading;
		private final LocalDateTime collectTime;
		private ReadingInput(Long deviceCode, Integer sequence, BigDecimal reading, LocalDateTime collectTime) {
			this.deviceCode = deviceCode;
			this.sequence = sequence;
			this.reading = reading;
			this.collectTime = collectTime;
		}
	}

	@Override
	public Double getMaxMeterReading(Long meterId, LocalDate firstDay, LocalDate lastDay) {
		Page page = new Page();
		page.setSize(10L);
		page.setCurrent(1L);
		IPage<MeterReadHisDTO> maxList = this.baseMapper.maxReading(page, meterId, DateUtils.convert(LocalDateTime.of(firstDay, LocalTime.MIN)),
				DateUtils.convert(LocalDateTime.of(lastDay, LocalTime.MAX)));
		if (CollUtil.isEmpty(maxList.getRecords())) {
			maxList = this.baseMapper.maxReading(page, meterId, null, DateUtils.convert(LocalDateTime.of(lastDay, LocalTime.MAX)));
		}
		return sdChangeHelper.reading(maxList.getRecords());
	}

	@Override
	public Double getInitMeterReading(Long meterId, LocalDate meterMonth) {
		Page page = new Page();
		page.setSize(10L);
		page.setCurrent(1L);
		IPage<MeterReadHisDTO> maxList = this.baseMapper.initReading(page, meterId, DateUtils.convert(LocalDateTime.of(meterMonth, LocalTime.MIN)));
		return sdChangeHelper.reading(maxList.getRecords());
	}
}
