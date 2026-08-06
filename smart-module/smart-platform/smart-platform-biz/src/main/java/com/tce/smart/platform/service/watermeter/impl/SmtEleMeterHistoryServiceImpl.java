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
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterDataUpdateDTO;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterHisQueryDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.EleMeterHisRespDTO;
import com.tce.smart.platform.core.dto.meter.MeterReadHisDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterHistory;
import com.tce.smart.platform.core.mapper.watermeter.SmtEleMeterHistoryMapper;
import com.tce.smart.platform.helper.SdChangeHelper;
import com.tce.smart.platform.service.energy.EnergyProjectionService;
import com.tce.smart.platform.service.energy.EnergyReadingIngestionService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterHistoryService;
import com.tce.smart.platform.service.watermeter.SmtEleMeterService;
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
public class SmtEleMeterHistoryServiceImpl extends ServiceImpl<SmtEleMeterHistoryMapper, SmtEleMeterHistory> implements SmtEleMeterHistoryService {

	@Autowired
	private SmtEleMeterService eleMeterService;
	@Autowired
	private SdChangeHelper sdChangeHelper;
	@Autowired
	private EnergyReadingIngestionService energyReadingIngestionService;
	@Autowired
	private EnergyProjectionService energyProjectionService;
	private static final DateTimeFormatter COLLECT_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);
	/** 水电表生产阈值 */
	@Value("${smart.meter.prod}")
	private Integer prod;
	/** 水电表生活阈值 */
	@Value("${smart.meter.life}")
	private Integer life;

	@Override
	public IPage<SmtEleMeterHistory> getPage(Page page, EleMeterHisQueryDTO dto) {
		return this.page(page, getWrapper(dto));
	}

	@Override
	public List<SmtEleMeterHistory> getList(EleMeterHisQueryDTO dto) {
		return this.list(getWrapper(dto));
	}

	private LambdaQueryWrapper getWrapper(EleMeterHisQueryDTO dto) {
		return Wrappers.<SmtEleMeterHistory>lambdaQuery()
				.eq(SmtEleMeterHistory::getEleMeterId, dto.getEleMeterId())
				.eq(Objects.nonNull(dto.getIsError()), SmtEleMeterHistory::getIsError, dto.getIsError())
				.ge(StringUtils.isNotEmpty(dto.getStartTime()), SmtEleMeterHistory::getCollectTime,
						NumberUtils.convertTime(dto.getStartTime()))
				.le(StringUtils.isNotEmpty(dto.getEndTime()), SmtEleMeterHistory::getCollectTime,
						NumberUtils.convertTime(dto.getEndTime()))
				.orderByDesc(SmtEleMeterHistory::getCreateTime);
	}

	@Override
	public ResponseEntity<byte[]> exportHistory(Long eleMeterId) {
		EleMeterHisQueryDTO dto = new EleMeterHisQueryDTO();
		dto.setEleMeterId(eleMeterId);
		List<SmtEleMeterHistory> historyList = getList(dto);
		if (CollUtil.isEmpty(historyList)) {
			throw new TCEException(CommonConstants.SUCCESS, "查询无数据");
		}
		ResponseEntity<byte[]> responseEntity;
		try (Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), EleMeterHisRespDTO.class, historyList)) {
			String fileName = "电表历史读数导出";
			responseEntity = IOUtils.getExcelResponse(fileName, workbook);
		} catch (IOException e) {
			log.error("excel导出异常", e);
			throw new TCEException(ExceptionEnum.UNKNOWN.getCode(), "excel导出异常");
		}
		return responseEntity;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveCurrentReading(EleMeterDataUpdateDTO dto) {
		ReadingInput input = validateReading(dto == null ? null : dto.getDeviceCode(), dto == null ? null : dto.getEleMeterSeq(),
				dto == null ? null : dto.getEleMeterCurrVal(), dto == null ? null : dto.getCollectTime());
		SmtEleMeter eleMeter = eleMeterService.getByConcentratorIdAndSeq(input.deviceCode, input.sequence);
		if (eleMeter == null) {
			throw new SmartException("电表信息不存在");
		}
		if (this.baseMapper.lockMeterForUpdate(eleMeter.getId()) == null) {
			throw new SmartException("电表信息不存在");
		}
		eleMeter = eleMeterService.getById(eleMeter.getId());
		if (eleMeter == null) {
			throw new SmartException("电表信息不存在");
		}
		Long historyId = IdWorker.getId();
		SmtEleMeterHistory previous = this.baseMapper.selectPreviousByCollectTime(eleMeter.getId(), input.collectTime, historyId);
		String eventPayload = JSONUtil.toJsonStr(dto);
		EnergyReadingIngestionService.RegisterCommand command = new EnergyReadingIngestionService.RegisterCommand(dto.getSourceEventId(), "ELE_READ", "ELE",
				eleMeter.getParkId() == null ? null : eleMeter.getParkId().longValue(), eleMeter.getId(), dto.getDeviceCode(), dto.getEleMeterSeq(), dto.getEleMeterCurrVal(), null, input.collectTime);
		if (!energyReadingIngestionService.register(command, energyReadingIngestionService.hashPayload(eventPayload), eventPayload)) {
			return Boolean.TRUE;
		}
		SmtEleMeterHistory history = SmtEleMeterHistory.builder()
				.id(historyId)
				.eleMeterId(eleMeter.getId())
				.collectTime(input.collectTime)
				.currentReading(input.reading.toPlainString())
				.isError(isError(previous == null ? null : previous.getCurrentReading(), input.reading) ? OneOrZeroEnum.ONE.getCode() : OneOrZeroEnum.ZERO.getCode())
				.build();
		if (!save(history)) {
			throw new SmartException("电表历史读数保存失败");
		}
		SmtEleMeterHistory latest = this.baseMapper.selectLatestByCollectTime(eleMeter.getId());
		if (latest != null && historyId.equals(latest.getId()) && OneOrZeroEnum.ZERO.getCode().equals(history.getIsError())) {
			eleMeter.setIsOnline(NumberConstants.TWO);
			eleMeter.setCurrentReading(history.getCurrentReading());
			eleMeterService.updateById(eleMeter);
		}
		requestProjection("ELE", eleMeter.getId(), input.collectTime.toLocalDate());
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
			log.warn("历史电表读数不是有效数值，当前读数标记异常");
			return true;
		}
		BigDecimal difference = currentReading.subtract(previous);
		if (difference.compareTo(BigDecimal.ZERO) < 0) return true;
		if (life == null || life <= 0) {
			log.warn("电表异常阈值未配置或不大于零，跳过增量阈值判断");
			return false;
		}
		return difference.compareTo(BigDecimal.valueOf(life)) > 0;
	}

	private ReadingInput validateReading(String deviceCode, Integer sequence, String reading, String collectTime) {
		if (deviceCode == null || deviceCode.trim().isEmpty() || sequence == null || sequence < 0 || reading == null || reading.trim().isEmpty() || collectTime == null) {
			throw new SmartException("电表读数参数不完整");
		}
		try {
			Long concentratorId = Long.parseLong(deviceCode);
			if (concentratorId < 0) throw new NumberFormatException();
			BigDecimal value = new BigDecimal(reading);
			if (value.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
			return new ReadingInput(concentratorId, sequence, value, LocalDateTime.parse(collectTime, COLLECT_TIME_FORMATTER));
		} catch (NumberFormatException | DateTimeParseException ex) {
			throw new SmartException("电表读数、集中器标识或采集时间格式不合法");
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
