package com.tce.smart.platform.service.watermeter.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
		SmtEleMeter eleMeter = eleMeterService.getByConcentratorIdAndSeq(Long.parseLong(dto.getDeviceCode()), dto.getEleMeterSeq());
		if (eleMeter == null) {
			throw new SmartException("电表信息不存在");
		}
		double before = NumberUtils.transDouble(eleMeter.getCurrentReading());
		double after = NumberUtils.transDouble(dto.getEleMeterCurrVal());
		double differ = after - before;
		eleMeter.setIsOnline(NumberConstants.TWO);
		eleMeter.setCurrentReading(dto.getEleMeterCurrVal());
		SmtEleMeterHistory history = SmtEleMeterHistory.builder()
				.eleMeterId(eleMeter.getId())
				.collectTime(LocalDateTime.now())
				.currentReading(dto.getEleMeterCurrVal())
				.isError((differ < life) ? OneOrZeroEnum.ZERO.getCode() : OneOrZeroEnum.ONE.getCode())
				.build();
		eleMeterService.updateById(eleMeter);
		return save(history);
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
