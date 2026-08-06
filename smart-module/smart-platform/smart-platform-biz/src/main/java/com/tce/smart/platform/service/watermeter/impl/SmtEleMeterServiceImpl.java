package com.tce.smart.platform.service.watermeter.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.NumberConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.DeviceDataDTO;
import com.tce.smart.platform.api.dto.req.watermeter.*;
import com.tce.smart.platform.api.dto.resp.watermeter.SdMeterStatisticsRespDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.SdUseStatisticsRespDTO;
import com.tce.smart.platform.core.dto.OperateLogDTO;
import com.tce.smart.platform.core.dto.meter.SdDeviceRecordDTO;
import com.tce.smart.platform.core.dto.meter.SdMonthStatisticsDTO;
import com.tce.smart.platform.core.entity.SmtArea;
import com.tce.smart.platform.core.entity.SmtDormitory;
import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterConcentrator;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterConcentrator;
import com.tce.smart.platform.core.mapper.watermeter.SmtEleMeterMapper;
import com.tce.smart.platform.emun.EleDownChannelEnum;
import com.tce.smart.platform.emun.MeterStatusEnum;
import com.tce.smart.platform.emun.PlaceTypeEnum;
import com.tce.smart.platform.emun.ValveStatusEnum;
import com.tce.smart.platform.emun.operateLog.CodeEnum;
import com.tce.smart.platform.emun.operateLog.MeterOperateEnum;
import com.tce.smart.platform.helper.MeterHelper;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.watermeter.*;
import com.tce.smart.platform.utils.ExcelUtils;
import com.tce.smart.tool.enums.SDCategoryEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:41
 */
@Slf4j
@Service
public class SmtEleMeterServiceImpl extends ServiceImpl<SmtEleMeterMapper, SmtEleMeter> implements SmtEleMeterService {
	@Autowired
	private SmtEleMeterTagService meterTagService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtDormitoryRoomService smtDormitoryRoomService;
	@Autowired
	private SmtDormitoryService smtDormitoryService;
	@Autowired
	private SmtEleMeterConcentratorService concentratorService;

	@Autowired
	private SmtWaterMeterConcentratorService waterMeterConcentratorService;

	@Resource
	private RemoteDispatcherService remoteDispatcherService;
	@Autowired
	private SmtAreaService areaService;
	@Autowired
	private MeterHelper meterHelper;
	@Autowired
	private SmtEleMeterChangeService eleMeterChangeService;
	@Autowired
	private SmtOperateLogService operateLogService;

	@Autowired
	private SmtEleMeterTagService smtEleMeterTagService;
	/**
	 * 批量删除|下载限制条数
	 */
	private final static int BATCH_NUM = 50;

	@Override
	public Boolean existEleMeterByConcentratorId(Long conId) {
		return count(Wrappers.<SmtEleMeter>lambdaQuery().eq(SmtEleMeter::getConcentratorId, conId)) > 0;
	}

	@Override
	public void getReading(EleMeterOperateDTO operate) {
		List<SmtEleMeter> meterList = this.list(Wrappers.<SmtEleMeter>lambdaQuery()
				.in(SmtEleMeter::getId, operate.getMeterIds()));
		for (SmtEleMeter meter : meterList) {
			meter.setIsOnline(startReading(meter) ? MeterStatusEnum.ONLINE.getCode() : MeterStatusEnum.OUTLINE.getCode());
			meter.setUpdateUserId(0);
			meter.setUpdateTime(LocalDateTime.now());
			this.updateById(meter);
			// 每条请求延迟500ms
			ThreadUtil.sleep(500);
		}
	}

	@Override
	public void reDownload(EleMeterOperateDTO operate) {
		List<SmtEleMeter> meterList = this.list(Wrappers.<SmtEleMeter>lambdaQuery()
				.in(SmtEleMeter::getId, operate.getMeterIds()));
		for (SmtEleMeter eleMeter : meterList) {
			SmtEleMeterConcentrator concentrator = concentratorService.getById(eleMeter.getConcentratorId());
			String reason = changeFile(CollUtil.newArrayList(eleMeter), concentrator);
			if (StringUtils.isNotBlank(reason)) {
				log.warn("{}重新下载档案失败，原因：{}", eleMeter.getName(), reason);
			}
		}
	}

	@Override
	public IPage<SmtEleMeter> getPage(Page page, EleMeterQueryDTO dto) {
		List<Long> legalIds = meterTagService.getMeterIdsByTagIds(dto.getTagIds());
		if (CollectionUtils.isNotEmpty(dto.getTagIds()) && CollectionUtils.isEmpty(legalIds)) {
			return new Page<>();
		}
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (CollUtil.isEmpty(parkIdList)) {
			return new Page<>();
		}
		if (Objects.nonNull(dto.getParkId())) {
			parkIdList = new ArrayList<>(1);
			parkIdList.add(dto.getParkId());
		}
		return this.page(page, Wrappers.<SmtEleMeter>lambdaQuery()
				.eq(Objects.nonNull(dto.getConcenId()), SmtEleMeter::getConcentratorId, dto.getConcenId())
				.like(Objects.nonNull(dto.getName()), SmtEleMeter::getName, dto.getName())
				.eq(Objects.nonNull(dto.getStatus()), SmtEleMeter::getIsOnline, dto.getStatus())
				.eq(Objects.nonNull(dto.getDormitoryId()), SmtEleMeter::getDormitoryId, dto.getDormitoryId())
				.eq(Objects.nonNull(dto.getRoomId()), SmtEleMeter::getRoomId, dto.getRoomId())
				.in(SmtEleMeter::getParkId, parkIdList)
				.in(CollectionUtils.isNotEmpty(legalIds), SmtEleMeter::getId, legalIds)
				.like(Objects.nonNull(dto.getAddress()), SmtEleMeter::getAddress, dto.getAddress())
				.and(Objects.nonNull(dto.getPlace()), e -> e.like(SmtEleMeter::getDormitoryName, dto.getPlace())
						.or()
						.like(SmtEleMeter::getRoomName, dto.getPlace())
						.or()
						.like(SmtEleMeter::getAreaName, dto.getPlace())
				)
				.orderByAsc(SmtEleMeter::getConcentratorId,SmtEleMeter::getRoomName));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean addMeter(EleMeterAddDTO dto) {
		SmtEleMeterConcentrator concentrator = concentratorService.getById(dto.getConcentratorId());
		if (Objects.isNull(concentrator)) {
			throw new SmartException("电表集中器不存在");
		}
		SmtEleMeter eleMeter = SmtEleMeter.builder()
				.name(dto.getName())
				.seq(dto.getSeq())
				.port(dto.getPort())
				.ratio(dto.getRatio())
				.concentratorId(dto.getConcentratorId())
				.address(dto.getAddress())
				.placeType(dto.getPlaceType())
				.isOpen(ValveStatusEnum.OPEN.getCode())
				.isOnline(MeterStatusEnum.OUTLINE.getCode()).build();
		saveValid(eleMeter, dto.getRoomId(), dto.getAreaId(), concentrator);
		checkExist(null, dto.getSeq(), concentrator.getId());
		boolean save = this.save(eleMeter);
		if (save && CollUtil.isNotEmpty(dto.getTagIds())) {
			// 更新设备对应标签
			EleMeterTagAddDTO tagAddDTO = new EleMeterTagAddDTO();
			tagAddDTO.setMeterIds(CollUtil.newArrayList(eleMeter.getId()));
			tagAddDTO.setTagIds(dto.getTagIds());
			save = meterTagService.setMeterTag(tagAddDTO);
		}
		String reason = changeFile(CollUtil.newArrayList(eleMeter), concentrator);
		if (StringUtils.isNotBlank(reason)) {
			throw new SmartException(reason);
		}
		return save;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateMeter(EleMeterUpdateDTO dto, Boolean isChangeDevice) {
		SmtEleMeter eleMeter = this.getById(dto.getId());
		if (Objects.isNull(eleMeter)) {
			throw new SmartException("电表不存在");
		}
		Long concentratorId = eleMeter.getConcentratorId();
		String beforeAddress = eleMeter.getAddress();
		String beforePort = eleMeter.getPort();
		Integer seq = eleMeter.getSeq();
		Integer beforeRatio = eleMeter.getRatio();
		SmtEleMeterConcentrator concentrator = concentratorService.getById(dto.getConcentratorId());
		if (Objects.isNull(concentrator)) {
			throw new SmartException("电表集中器不存在");
		}
		// 表号发生变化
		boolean isChangeAddress = !dto.getAddress().equals(eleMeter.getAddress());
		// 电表档案数据发生变化
		boolean isOperate = isChangeAddress || !dto.getSeq().equals(eleMeter.getSeq())
				|| !dto.getPort().equals(eleMeter.getPort()) || !dto.getConcentratorId().equals(eleMeter.getConcentratorId());
		// 换表未修改表号
		if (isChangeDevice && !isChangeAddress) {
			throw new SmartException("无法换表，未修改表号");
		}
		checkExist(eleMeter.getId(), dto.getSeq(), dto.getConcentratorId());
		eleMeter.setSeq(dto.getSeq());
		eleMeter.setName(dto.getName());
		eleMeter.setAddress(dto.getAddress());
		eleMeter.setPort(dto.getPort());
		eleMeter.setRatio(dto.getRatio());
		eleMeter.setConcentratorId(dto.getConcentratorId());
		eleMeter.setPlaceType(dto.getPlaceType());
		saveValid(eleMeter, dto.getRoomId(), dto.getAreaId(), concentrator);
		if (CollUtil.isNotEmpty(dto.getTagIds())) {
			// 更新设备对应标签
			EleMeterTagAddDTO tagAddDTO = new EleMeterTagAddDTO();
			tagAddDTO.setMeterIds(CollUtil.newArrayList(eleMeter.getId()));
			tagAddDTO.setTagIds(dto.getTagIds());
			meterTagService.setMeterTag(tagAddDTO);
		}
		EleMeterUpdateDTO beforeDto = new EleMeterUpdateDTO();
		if (isOperate) {
			String reason = changeFile(CollUtil.newArrayList(eleMeter), concentrator);
			if (StringUtils.isNotBlank(reason)) {
				throw new SmartException(reason);
			}
		}
		if (isChangeDevice) {
			Long beforeId = eleMeter.getId();
			eleMeter.setId(IdWorker.getId());
			this.save(eleMeter);
			// 主动读取更换后电表读数
			startReading(eleMeter);
			beforeDto.setConcentratorId(concentratorId);
			beforeDto.setId(eleMeter.getId());
			beforeDto.setAddress(beforeAddress);
			beforeDto.setPort(beforePort);
			beforeDto.setSeq(seq);
			beforeDto.setRatio(beforeRatio);
			// 新增电表更换记录
			eleMeterChangeService.addRecord(beforeDto, dto);
			return this.removeById(beforeId);
		}
		return this.updateById(eleMeter);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void excelImport(HttpServletRequest request, HttpServletResponse response, InputStream inputStream) {
		String[] excelHead = {"楼栋", "房间号", "设备名称", "集中器IP", "下行通道", "倍率", "设备序号", "电表通信地址"};
		String[] excelHeadAlias = {"dormitory", "room", "name", "concentratorIp", "portDesc", "ratio", "seq", "address"};
		String[] excelFactoryHead = {"地点", "设备名称", "集中器IP", "下行通道", "倍率", "设备序号", "电表通信地址"};
		String[] excelFactoryHeadAlias = {"factory", "name", "concentratorIp", "portDesc", "ratio", "seq", "address"};
		ExcelReader reader = ExcelUtil.getReader(inputStream);
		List<EleMeterImportDTO> importLines = ExcelUtils.importExcel(reader, excelHead, excelHeadAlias,
				PlaceTypeEnum.DORMITORY.getDesc(), EleMeterImportDTO.class, 0, 1);
		List<EleMeterFactoryImportDTO> importFactoryLines = ExcelUtils.importExcel(reader, excelFactoryHead, excelFactoryHeadAlias,
				PlaceTypeEnum.FACTORY.getDesc(), EleMeterFactoryImportDTO.class, 0, 1);
		if (CollectionUtils.isEmpty(importLines) && CollectionUtils.isEmpty(importFactoryLines)) {
			throw new SmartException("没有解析到数据,请检查模板是否正确!");
		}
		List<EleMeterImportDTO> failLines = new ArrayList<>();
		List<EleMeterFactoryImportDTO> factoryFailLines = new ArrayList<>();
		int maxSize = 0;
		if (CollectionUtils.isNotEmpty(importLines)) {
			maxSize += importLines.size();
		}
		if (CollectionUtils.isNotEmpty(importFactoryLines)) {
			maxSize += importFactoryLines.size();
		}
		// 写入剩余导入条数为最大值
		request.getSession().setAttribute("eleMaxImport", maxSize);
		request.getSession().setAttribute("eleRemainImport", maxSize);
		checkData(request, importLines, failLines);
		checkFactoryData(request, importFactoryLines, factoryFailLines);
		// 如果存在失败项返回给前端
		if (failLines.size() > NumberConstants.ZERO || factoryFailLines.size() > NumberConstants.ZERO) {
			try {
				String fileNameEncode = URLEncoder.encode("导入失败电表信息.xls", "UTF-8");
				ExcelUtils.export(request, response, fileNameEncode, PlaceTypeEnum.DORMITORY.getDesc(),
						failLines, PlaceTypeEnum.FACTORY.getDesc(), factoryFailLines);
			} catch (Exception e) {
				throw new SmartException("导出错误数据失败");
			}
		}
	}

	@Override
	public SmtEleMeter getByConcentratorIdAndSeq(Long concentratorId, Integer seq) {
		return getOne(Wrappers.<SmtEleMeter>lambdaQuery()
				.eq(SmtEleMeter::getConcentratorId, concentratorId)
				.eq(SmtEleMeter::getSeq, seq), false);
	}

	@Override
	public List<SmtEleMeter> getByConcentratorId(Long concentratorId) {
		return this.list(Wrappers.<SmtEleMeter>lambdaQuery()
				.eq(SmtEleMeter::getConcentratorId, concentratorId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean changeBrake(Long eleMeterId, Integer status) {
		SmtEleMeter eleMeter = this.getById(eleMeterId);
		return brakeChange(eleMeter, status);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String batchChangeBrake(EleMeterBrakeOperateDTO operate) {
		List<Long> meterIds = operate.getMeterIds();
		if (CollectionUtils.isEmpty(meterIds)) {
			throw new SmartException("请选择电表");
		}
		int isOpen = ValveStatusEnum.OPEN.getCode().equals(operate.getStatus())
				? ValveStatusEnum.CLOSE.getCode() : ValveStatusEnum.OPEN.getCode();
		List<SmtEleMeter> waterMeters = this.list(Wrappers.<SmtEleMeter>lambdaQuery()
				.eq(SmtEleMeter::getIsOpen, isOpen)
				.in(SmtEleMeter::getId, meterIds));
		int failSize = 0;
		String failRoom = "";
		for (SmtEleMeter eleMeter : waterMeters) {
			try {
				boolean change = brakeChange(eleMeter, operate.getStatus());
				if (!change) {
					failSize++;
					failRoom = failRoom.concat("{房间号").concat(eleMeter.getRoomName()).concat("}");
				}
			} catch (Exception e) {
				log.error("电表{}闸门控制失败", eleMeter, e);
				failSize++;
				failRoom = failRoom.concat("{房间号").concat(eleMeter.getRoomName()).concat("}");
			}
		}
		if (failSize > 0) {
			return ValveStatusEnum.desc(operate.getStatus()) + "设备失败" + failSize + "个，为" + failRoom;
		}
		return StringUtils.EMPTY;
	}

	@Override
	public Boolean changeBrakeStatus(SmartBrakeUpdateDTO dto) {
		SmtEleMeter eleMeter = this.getOne(Wrappers.<SmtEleMeter>lambdaQuery()
				.eq(SmtEleMeter::getConcentratorId, dto.getDeviceCode())
				.eq(SmtEleMeter::getSeq, dto.getEleMeterSeq()));
		if (Objects.isNull(eleMeter)) {
			throw new SmartException("电表不存在");
		}
		eleMeter.setIsOpen(Integer.parseInt(dto.getBrakeState()));
		return this.updateById(eleMeter);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String remove(EleMeterOperateDTO delList) {
		List<Long> meterIds = delList.getMeterIds();
		if (CollectionUtils.isEmpty(meterIds)) {
			throw new SmartException("请选择要删除的电表");
		}
		int failSize = 0;
		String failRoom = "";
		for (Long meterId : meterIds) {
			SmtEleMeter meter = this.getById(meterId);
			SmtEleMeterConcentrator concentrator = concentratorService.getById(meter.getConcentratorId());
			// 先删除电表档案
			if (!operateFile(CollUtil.newArrayList(meter), concentrator, EventEnum.ELE_METER_DEL_FILE.getCode())) {
				failSize++;
				failRoom = failRoom.concat("{房间号").concat(meter.getRoomName()).concat("}");
				continue;
			}
			this.removeById(meterId);
		}
		if (failSize > 0) {
			return "删除设备失败" + failSize + "个，为" + failRoom;
		}
		return StringUtils.EMPTY;
	}

    @Override
    public IPage<SdMeterStatisticsRespDTO> getMeterStatisticsPage(Page page, SdMeterStatisticsQueryDTO dto) {

		IPage<SdDeviceRecordDTO> deviceRecordDTOIPage = null;
		if(SDCategoryEnum.ELECTRIC.getCode().equals(dto.getSdType())){
			// 查询电表设备
			deviceRecordDTOIPage = this.baseMapper.getEleMeterStatisticsPage(page,dto);
		} else if(SDCategoryEnum.HOT_WATER.getCode().equals(dto.getSdType()) || SDCategoryEnum.COLD_WATER.getCode().equals(dto.getSdType())){
			// 查询水表设备
			deviceRecordDTOIPage = this.baseMapper.getWaterMeterStatisticsPage(page,dto);
		} else {
			// 查询所有水电设备
			deviceRecordDTOIPage = this.baseMapper.getSdMeterStatisticsPage(page,dto);
		}

		if(Objects.isNull(deviceRecordDTOIPage) || CollectionUtil.isEmpty(deviceRecordDTOIPage.getRecords())){
			return null;
		}

		IPage<SdMeterStatisticsRespDTO> respDTOPage = new Page<>(deviceRecordDTOIPage.getCurrent(),deviceRecordDTOIPage.getSize(),deviceRecordDTOIPage.getTotal());
		List<SdMeterStatisticsRespDTO> respDTOList = new ArrayList<>();
		// 统计每个设备的用量
		List<SdDeviceRecordDTO> sdDeviceRecordDTOS = deviceRecordDTOIPage.getRecords();

		statisticsUse(sdDeviceRecordDTOS,dto.getYear(),respDTOList);

		respDTOPage.setRecords(respDTOList);
		return respDTOPage;
    }

	@Override
	public List<SdMeterStatisticsRespDTO> getMeterStatisticsList(SdMeterStatisticsQueryDTO dto) {
		List<SdDeviceRecordDTO> deviceRecordDTOList = new ArrayList<>();
		if(SDCategoryEnum.ELECTRIC.getCode().equals(dto.getSdType())){
			// 查询电表设备
			deviceRecordDTOList = this.baseMapper.getEleMeterStatisticsList(dto);
		} else if(SDCategoryEnum.HOT_WATER.getCode().equals(dto.getSdType()) || SDCategoryEnum.COLD_WATER.getCode().equals(dto.getSdType())){
			// 查询水表设备
			deviceRecordDTOList = this.baseMapper.getWaterMeterStatisticsList(dto);
		} else {
			// 查询所有水电设备
			deviceRecordDTOList = this.baseMapper.getSdMeterStatisticsList(dto);
		}

		if(CollectionUtil.isEmpty(deviceRecordDTOList)){
			return null;
		}

		List<SdMeterStatisticsRespDTO> respDTOList = new ArrayList<>();
		statisticsUse(deviceRecordDTOList,dto.getYear(),respDTOList);
		return respDTOList;
	}

    @Override
    public IPage<SdUseStatisticsRespDTO> getUseStatisticsPage(Page page, SdUseStatisticsQueryDTO dto,Long[] deviceIds,Long[] deviceTagList) {
		IPage<SdDeviceRecordDTO> deviceRecordDTOIPage = null;
		List<Long> meterIds = new ArrayList<>();
		if(com.tce.smart.common.core.util.CollectionUtils.isNotEmpty(deviceTagList)){
			meterIds = smtEleMeterTagService.getMeterIdsByTagIds(Arrays.asList(deviceTagList));
			if(CollectionUtil.isEmpty(meterIds)){
				// 没有查到设备
				return new Page<>(page.getCurrent(),page.getSize(),0);
			}
		}
		if(com.tce.smart.common.core.util.CollectionUtils.isNotEmpty(deviceIds)){
			meterIds.containsAll(Arrays.asList(deviceIds));
		}
		List<SdUseStatisticsRespDTO> sdUseStatisticsRespDTOList = new ArrayList<>();
		if(SDCategoryEnum.ELECTRIC.getCode().equals(dto.getSdType())){
			// 查询电表设备
			deviceRecordDTOIPage = this.baseMapper.getEleUseStatisticsPage(page,dto,meterIds);
			sdUseStatisticsRespDTOList = getEleHistoryUseStatistics(deviceRecordDTOIPage.getRecords(),dto);
		} else if(SDCategoryEnum.HOT_WATER.getCode().equals(dto.getSdType()) || SDCategoryEnum.COLD_WATER.getCode().equals(dto.getSdType())){
			// 查询水表设备
			deviceRecordDTOIPage = this.baseMapper.getWaterUseStatisticsPage(page,dto,meterIds);
			sdUseStatisticsRespDTOList = getWaterHistoryUseStatistics(deviceRecordDTOIPage.getRecords(),dto);
		} else {
			// 查询所有水电设备
			deviceRecordDTOIPage = this.baseMapper.getSdUseStatisticsPage(page,dto,meterIds);
			sdUseStatisticsRespDTOList = getSdHistoryUseStatistics(deviceRecordDTOIPage.getRecords(),dto);
		}

		if(CollectionUtil.isNotEmpty(sdUseStatisticsRespDTOList)){
			IPage<SdUseStatisticsRespDTO> respDTOIPage = new Page<>(deviceRecordDTOIPage.getCurrent(),deviceRecordDTOIPage.getSize(),deviceRecordDTOIPage.getTotal());
			respDTOIPage.setRecords(sdUseStatisticsRespDTOList);
			return respDTOIPage;
		}
        return null;
    }

	@Override
	public List<SdUseStatisticsRespDTO> getUseStatisticsList(SdUseStatisticsQueryDTO dto,Long[] deviceIds,Long[] deviceTagList) {
		List<SdDeviceRecordDTO> deviceRecordDTOList = null;
		List<Long> meterIds = new ArrayList<>();
		if(com.tce.smart.common.core.util.CollectionUtils.isNotEmpty(deviceTagList)){
			meterIds = smtEleMeterTagService.getMeterIdsByTagIds(Arrays.asList(deviceTagList));
			if(CollectionUtil.isEmpty(meterIds)){
				// 没有查到设备
				return new ArrayList<>();
			}
		}
		if(com.tce.smart.common.core.util.CollectionUtils.isNotEmpty(deviceIds)){
			meterIds.containsAll(Arrays.asList(deviceIds));
		}
		List<SdUseStatisticsRespDTO> sdUseStatisticsRespDTOList = new ArrayList<>();
		if(SDCategoryEnum.ELECTRIC.getCode().equals(dto.getSdType())){
			// 查询电表设备
			deviceRecordDTOList = this.baseMapper.getEleUseStatisticsList(dto,meterIds);
			sdUseStatisticsRespDTOList = getEleHistoryUseStatistics(deviceRecordDTOList,dto);
		} else if(SDCategoryEnum.HOT_WATER.getCode().equals(dto.getSdType()) || SDCategoryEnum.COLD_WATER.getCode().equals(dto.getSdType())){
			// 查询水表设备
			deviceRecordDTOList = this.baseMapper.getWaterUseStatisticsList(dto,meterIds);
			sdUseStatisticsRespDTOList = getWaterHistoryUseStatistics(deviceRecordDTOList,dto);
		} else {
			// 查询所有水电设备
			deviceRecordDTOList = this.baseMapper.getSdUseStatisticsList(dto,meterIds);
			sdUseStatisticsRespDTOList = getSdHistoryUseStatistics(deviceRecordDTOList,dto);
		}

		return sdUseStatisticsRespDTOList;
	}

	private List<SdUseStatisticsRespDTO> getEleHistoryUseStatistics(List<SdDeviceRecordDTO> recordDTOList,SdUseStatisticsQueryDTO dto){
		if(CollectionUtil.isEmpty(recordDTOList)){
			return null;
		}
		List<Long> meterIds = recordDTOList.stream().map(SdDeviceRecordDTO::getId).collect(Collectors.toList());
		List<Long> concentratorIds = recordDTOList.stream().map(SdDeviceRecordDTO::getConcentratorId).collect(Collectors.toList());
		List<SdUseStatisticsRespDTO> resList = new ArrayList<>();
		List<SdUseStatisticsRespDTO> respDTOList = this.baseMapper.getEleHistoryStatistics(meterIds, DateUtils.convert(dto.getStartDate().atStartOfDay()),DateUtils.convert(dto.getEndDate().atTime(23,59,59)));
		Collection<SmtEleMeterConcentrator> smtEleMeterConcentrators = concentratorService.listByIds(concentratorIds);
		Map<Long, List<SmtEleMeterConcentrator>> collectEleMeterConcentrator = smtEleMeterConcentrators.stream().collect(Collectors.groupingBy(SmtEleMeterConcentrator::getId));
		for(SdDeviceRecordDTO recordDTO : recordDTOList){
			SdUseStatisticsRespDTO respDTO = respDTOList.stream().filter(s -> s.getId().equals(recordDTO.getId())).findFirst().orElse(null);
			if(Objects.isNull(respDTO)){
				respDTO = new SdUseStatisticsRespDTO();
			}
			respDTO.setAreaName(recordDTO.getAreaName());
			respDTO.setDeviceName(recordDTO.getDeviceName());
			respDTO.setPlaceType(recordDTO.getPlaceType());
			respDTO.setSdType(recordDTO.getSdType());
			respDTO.setDeviceTag(recordDTO.getDeviceTag());
			SmtEleMeterConcentrator smtEleMeterConcentrator = collectEleMeterConcentrator.get(recordDTO.getConcentratorId()).get(0);
			respDTO.setCommAddress(recordDTO.getAddress());
			respDTO.setConcentratorName(smtEleMeterConcentrator.getName());
			respDTO.setStartDate(DateUtils.format(dto.getStartDate()));
			respDTO.setEndDate(DateUtils.format(dto.getEndDate()));

			resList.add(respDTO);
		}
		return resList;
	}

	private List<SdUseStatisticsRespDTO> getWaterHistoryUseStatistics(List<SdDeviceRecordDTO> recordDTOList,SdUseStatisticsQueryDTO dto){
		if(CollectionUtil.isEmpty(recordDTOList)){
			return null;
		}
		List<Long> meterIds = recordDTOList.stream().map(SdDeviceRecordDTO::getId).collect(Collectors.toList());
		List<Long> concentratorIds = recordDTOList.stream().map(SdDeviceRecordDTO::getConcentratorId).collect(Collectors.toList());
		List<SdUseStatisticsRespDTO> resList = new ArrayList<>();
		List<SdUseStatisticsRespDTO> respDTOList = this.baseMapper.getWaterHistoryStatistics(meterIds, DateUtils.convert(dto.getStartDate().atStartOfDay()),DateUtils.convert(dto.getEndDate().atTime(23,59,59)));
		Collection<SmtWaterMeterConcentrator> smtWaterMeterConcentrators = waterMeterConcentratorService.listByIds(concentratorIds);
		Map<Long, List<SmtWaterMeterConcentrator>> collectEleMeterConcentrator = smtWaterMeterConcentrators.stream().collect(Collectors.groupingBy(SmtWaterMeterConcentrator::getId));
		for(SdDeviceRecordDTO recordDTO : recordDTOList){
			SdUseStatisticsRespDTO respDTO = respDTOList.stream().filter(s -> s.getId().equals(recordDTO.getId())).findFirst().orElse(null);
			if(Objects.isNull(respDTO)){
				respDTO = new SdUseStatisticsRespDTO();
			}
			respDTO.setAreaName(recordDTO.getAreaName());
			respDTO.setDeviceName(recordDTO.getDeviceName());
			respDTO.setPlaceType(recordDTO.getPlaceType());
			respDTO.setSdType(recordDTO.getSdType());
			respDTO.setDeviceTag(recordDTO.getDeviceTag());
			SmtWaterMeterConcentrator smtWaterMeterConcentrator = collectEleMeterConcentrator.get(recordDTO.getConcentratorId()).get(0);
			respDTO.setCommAddress(recordDTO.getAddress());
			respDTO.setConcentratorName(smtWaterMeterConcentrator.getName());
			respDTO.setStartDate(DateUtils.format(dto.getStartDate()));
			respDTO.setEndDate(DateUtils.format(dto.getEndDate()));

			resList.add(respDTO);
		}
		return resList;
	}

	private List<SdUseStatisticsRespDTO> getSdHistoryUseStatistics(List<SdDeviceRecordDTO> recordDTOList,SdUseStatisticsQueryDTO dto){
		if(CollectionUtil.isEmpty(recordDTOList)){
			return null;
		}

		List<SdUseStatisticsRespDTO> respDTOList = new ArrayList<>();

		// 电记录
		List<SdDeviceRecordDTO> eleSdDeviceRecordDTOS = recordDTOList.stream().filter(s -> SDCategoryEnum.ELECTRIC.getCode().equals(s.getSdType())).collect(Collectors.toList());
		List<SdUseStatisticsRespDTO> eleRespDTOS = getEleHistoryUseStatistics(eleSdDeviceRecordDTOS,dto);
		if(CollectionUtils.isNotEmpty(eleRespDTOS)){
			respDTOList.addAll(eleRespDTOS);
		}

		// 水记录
		List<SdDeviceRecordDTO> waterSdDeviceRecordDTOS = recordDTOList.stream().filter(s -> !SDCategoryEnum.ELECTRIC.getCode().equals(s.getSdType())).collect(Collectors.toList());
		List<SdUseStatisticsRespDTO> waterRespDTOS = getWaterHistoryUseStatistics(waterSdDeviceRecordDTOS,dto);
		if(CollectionUtils.isNotEmpty(waterRespDTOS)){
			respDTOList.addAll(waterRespDTOS);
		}

		return respDTOList;
	}


	/**
	 * 组装用量
	 * @param sdDeviceRecordDTOS
	 * @param year
	 * @param respDTOList
	 */
	private void statisticsUse(List<SdDeviceRecordDTO> sdDeviceRecordDTOS,Integer year,List<SdMeterStatisticsRespDTO> respDTOList){
		// 分开水电表
		//电表记录
		List<SdDeviceRecordDTO> eleSdDeviceRecordDTOS = sdDeviceRecordDTOS.stream().filter(s -> SDCategoryEnum.ELECTRIC.getCode().equals(s.getSdType())).collect(Collectors.toList());
		List<Long> eleDeviceIdList = eleSdDeviceRecordDTOS.stream().map(SdDeviceRecordDTO::getId).collect(Collectors.toList());
		List<SdMonthStatisticsDTO> eleSdMonthStatisticsDTOS = statisticsUse(eleDeviceIdList, eleSdDeviceRecordDTOS.get(0).getSdType(), year);
		// 水表记录
		List<SdDeviceRecordDTO> waterSdDeviceRecordDTOS = sdDeviceRecordDTOS.stream().filter(s -> !SDCategoryEnum.ELECTRIC.getCode().equals(s.getSdType())).collect(Collectors.toList());
		List<Long> waterDeviceIdList = waterSdDeviceRecordDTOS.stream().map(SdDeviceRecordDTO::getId).collect(Collectors.toList());
		List<SdMonthStatisticsDTO> waterSdMonthStatisticsDTOS = statisticsUse(waterDeviceIdList, waterSdDeviceRecordDTOS.get(0).getSdType(), year);

		eleSdMonthStatisticsDTOS.addAll(waterSdMonthStatisticsDTOS);

		Map<Long, List<SdMonthStatisticsDTO>> sdMonthStatisticsMap = eleSdMonthStatisticsDTOS.stream().collect(Collectors.groupingBy(SdMonthStatisticsDTO::getId));

		for(SdDeviceRecordDTO deviceRecordDTO : sdDeviceRecordDTOS){
			SdMeterStatisticsRespDTO respDTO = new SdMeterStatisticsRespDTO();
			respDTO.setPlaceType(deviceRecordDTO.getPlaceType());
			respDTO.setSdType(deviceRecordDTO.getSdType());
			respDTO.setAreaName(deviceRecordDTO.getAreaName());
			respDTO.setDeviceName(deviceRecordDTO.getDeviceName());
			statisticsUse(sdMonthStatisticsMap.get(deviceRecordDTO.getId()),respDTO);

			respDTOList.add(respDTO);
		}
	}

	/**
	 * 查询用量
	 * @param deviceList
	 * @param sdType
	 * @param year
	 * @return
	 */
	private List<SdMonthStatisticsDTO> statisticsUse(List<Long> deviceList,Integer sdType,Integer year){
		String startTime = year.toString().concat("-01-01 00:00:00");
		String endTime = year.toString().concat("-12-31 23:59:59");
		List<SdMonthStatisticsDTO> sdMonthUse = new ArrayList<>();
		if(SDCategoryEnum.ELECTRIC.getCode().equals(sdType)){
			// 电表
			sdMonthUse = this.baseMapper.getEleMonthUse(deviceList, startTime, endTime);
		} else {
			// 水表
			sdMonthUse = this.baseMapper.getWaterMonthUse(deviceList, startTime, endTime);
		}
		return sdMonthUse;
	}

	/**
	 * 统计用量
	 * @param sdMonthStatisticsDTOS
	 * @param respDTO
	 */
	private void statisticsUse(List<SdMonthStatisticsDTO> sdMonthStatisticsDTOS,SdMeterStatisticsRespDTO respDTO){
		Double totleUse = 0.0;
		if(CollectionUtil.isEmpty(sdMonthStatisticsDTOS)){
			return;
		}
		for(SdMonthStatisticsDTO sdMonthStatisticsDTO : sdMonthStatisticsDTOS){
			Integer month = Integer.parseInt(sdMonthStatisticsDTO.getMonth().split("-")[1]);
			Double use = new BigDecimal(sdMonthStatisticsDTO.getMonthUse()).setScale(2, RoundingMode.HALF_UP).doubleValue();
			totleUse += use;
			switch (month){
				case 1:
					respDTO.setMonth1Use(use);
					break;
				case 2:
					respDTO.setMonth2Use(use);
					break;
				case 3:
					respDTO.setMonth3Use(use);
					break;
				case 4:
					respDTO.setMonth4Use(use);
					break;
				case 5:
					respDTO.setMonth5Use(use);
					break;
				case 6:
					respDTO.setMonth6Use(use);
					break;
				case 7:
					respDTO.setMonth7Use(use);
					break;
				case 8:
					respDTO.setMonth8Use(use);
					break;
				case 9:
					respDTO.setMonth9Use(use);
					break;
				case 10:
					respDTO.setMonth10Use(use);
					break;
				case 11:
					respDTO.setMonth11Use(use);
					break;
				case 12:
					respDTO.setMonth12Use(use);
					break;
			}
		}
		respDTO.setYearUse(totleUse);
	}

    /**
	 * 开始电表读数
	 */
	private Boolean startReading(SmtEleMeter meter) {
		SmtEleMeterConcentrator concentrator = concentratorService.getById(meter.getConcentratorId());
		try {
			// 园区分发
			DeviceDataDTO deviceInfo = new DeviceDataDTO();
			deviceInfo.setDeviceCode(meter.getConcentratorId().toString());
			deviceInfo.setDeviceIp(concentrator.getIp());
			deviceInfo.setDevicePort(Integer.parseInt(concentrator.getPort()));
			deviceInfo.setElectricMeterSeq(meter.getSeq());

			DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setEventType(EventEnum.ELE_METER_READ.getCode());
			dispatcherDTO.setParkId(meter.getParkId());
			dispatcherDTO.setDeviceId(meter.getConcentratorId().toString());
			dispatcherDTO.setData(deviceInfo);
			Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);

			if (!result.isSuccess()) {
				throw new SmartException("电表" + meter.getId() + "读数请求失败");
			}
			JSONObject object = JSONUtil.parseObj(result.getData());
			String code = "code";
			log.info("电表读数数据：{}", object);
			if (!object.containsKey(code)) {
				throw new SmartException("数据格式不正确，电表读数请求失败");
			}
			return object.getBool("data");
		} catch (Exception e) {
			log.error("电表{}读数请求失败", meter.getId());
			return false;
		}
	}

	/**
	 * 闸门控制
	 */
	private Boolean brakeChange(SmtEleMeter eleMeter, Integer status) {
		if (eleMeter == null) {
			throw new SmartException("电表不存在");
		}
		SmtEleMeterConcentrator concentrator = concentratorService.getById(eleMeter.getConcentratorId());
		if (concentrator == null) {
			throw new SmartException("电表集中器不存在");
		}
		if (ValveStatusEnum.OPEN.getCode().equals(status)) {
			eleMeter.setIsOpen(ValveStatusEnum.ON_OPEN.getCode());
		} else {
			eleMeter.setIsOpen(ValveStatusEnum.ON_CLOSE.getCode());
		}
		boolean isSuccess = false;
		if (meterHelper.changeBrakeStatus(EventEnum.ELE_METER_BRAKE_CONTROL.getCode(), concentrator.getIp(),
				concentrator.getPort(), eleMeter.getConcentratorId().toString(), eleMeter.getSeq(),
				eleMeter.getAddress(), Integer.parseInt(eleMeter.getPort()), status, eleMeter.getParkId())) {
			// 直接改成操作成功
			eleMeter.setIsOpen(status);
			isSuccess = this.updateById(eleMeter);
		}
		// 新增开闸|关闸操作记录
		OperateLogDTO logDTO = new OperateLogDTO();
		logDTO.setTargetId(eleMeter.getId());
		logDTO.setCode(CodeEnum.METER.getCode());
		logDTO.setCodeDesc(CodeEnum.METER.getDesc());
		logDTO.setAction(ValveStatusEnum.OPEN.getCode().equals(status) ? MeterOperateEnum.OPEN.getAction()
				: MeterOperateEnum.CLOSE.getAction());
		operateLogService.addLog(logDTO);
		return isSuccess;
	}

	/**
	 * 新增或修改时写入房间或区域
	 */
	private void saveValid(SmtEleMeter eleMeter, Integer roomId, Integer areaId,
						  SmtEleMeterConcentrator concentrator) {
		Integer parkId;
		Integer placeType = eleMeter.getPlaceType();
		if (PlaceTypeEnum.DORMITORY.getCode().equals(placeType)) {
			if (Objects.isNull(roomId)) {
				throw new SmartException("房间ID不能为空");
			}
			SmtDormitoryRoom room = smtDormitoryRoomService.getById(roomId);
			if (Objects.isNull(room)) {
				throw new SmartException("房间信息不存在");
			}
			SmtDormitory dormitory = smtDormitoryService.getById(room.getDormitoryId());
			if (Objects.isNull(dormitory)) {
				throw new SmartException("楼栋信息不存在");
			}
			eleMeter.setRoomId(room.getId());
			eleMeter.setRoomName(String.valueOf(room.getRoomName()));
			eleMeter.setDormitoryId(dormitory.getId());
			eleMeter.setDormitoryName(dormitory.getDormitoryName());
			parkId = dormitory.getParkId();
		} else {
			if (Objects.isNull(areaId)) {
				throw new SmartException("区域ID不能为空");
			}
			SmtArea area = areaService.getById(areaId);
			if (Objects.isNull(area)) {
				throw new SmartException("区域信息不存在");
			}
			eleMeter.setAreaId(area.getId());
			eleMeter.setAreaName(area.getAreaName());
			parkId = area.getParkId();
		}
		SmtPark park = smtParkService.getById(parkId);
		if (Objects.isNull(park)) {
			throw new SmartException("园区信息不存在");
		}
		if (!park.getId().equals(concentrator.getParkId())) {
			throw new SmartException((PlaceTypeEnum.DORMITORY.getCode().equals(placeType) ? "房间" : "区域")
					+ "所属园区与集中器所属园区不一致");
		}
		eleMeter.setParkId(parkId);
		eleMeter.setParkName(park.getParkName());
	}

	/**
	 * 导入并验证宿舍电表数据
	 */
	private void checkData(HttpServletRequest request, List<EleMeterImportDTO> importLines, List<EleMeterImportDTO> failLines) {
		// 宿舍导入为空
		if (CollectionUtils.isEmpty(importLines)) {
			return;
		}
		// 按电表集中器名称分组
		Map<String, List<EleMeterImportDTO>> concentratorMap = importLines.stream()
				.collect(Collectors.groupingBy(EleMeterImportDTO::getConcentratorIp));
		if (CollectionUtils.isNotEmpty(concentratorMap)) {
			concentratorMap.forEach((concentratorIp, lines) -> {
				// 电表档案列表
				List<SmtEleMeter> eleMeters = new ArrayList<>();
				List<SmtEleMeter> batchMeters = new ArrayList<>();
				List<Integer> existSeq = new ArrayList<>();
				SmtEleMeterConcentrator concentrator = concentratorService.getByIp(concentratorIp);
				if (Objects.isNull(concentrator)) {
					for (EleMeterImportDTO line : lines) {
						line.setMark("电表集中器不存在");
						failLines.add(line);
					}
					flushRemainImport(request, lines.size());
				} else {
					saveEleMeter(request, concentrator, eleMeters, existSeq, failLines, lines);
					for (SmtEleMeter eleMeter : eleMeters) {
						batchMeters.add(eleMeter);
						if (BATCH_NUM == batchMeters.size()) {
							changeFileStart(request, batchMeters, concentrator, failLines);
							batchMeters.clear();
						}
					}
					changeFileStart(request, batchMeters, concentrator, failLines);
				}
			});
		}
	}

	/**
	 * 导入并验证厂区电表数据
	 */
	private void checkFactoryData(HttpServletRequest request, List<EleMeterFactoryImportDTO> importLines,
								  List<EleMeterFactoryImportDTO> failLines) {
		// 厂区导入为空
		if (CollectionUtils.isEmpty(importLines)) {
			return;
		}
		// 按电表集中器名称分组
		Map<String, List<EleMeterFactoryImportDTO>> concentratorMap = importLines.stream()
				.collect(Collectors.groupingBy(EleMeterFactoryImportDTO::getConcentratorIp));
		if (CollectionUtils.isNotEmpty(concentratorMap)) {
			concentratorMap.forEach((concentratorIp, lines) -> {
				// 电表档案列表
				List<SmtEleMeter> eleMeters = new ArrayList<>();
				List<SmtEleMeter> batchMeters = new ArrayList<>();
				List<Integer> existSeq = new ArrayList<>();
				SmtEleMeterConcentrator concentrator = concentratorService.getByIp(concentratorIp);
				if (Objects.isNull(concentrator)) {
					for (EleMeterFactoryImportDTO line : lines) {
						line.setMark("电表集中器不存在");
						failLines.add(line);
					}
					flushRemainImport(request, lines.size());
				} else {
					saveFactoryEleMeter(request, concentrator, eleMeters, existSeq, failLines, lines);
					for (SmtEleMeter eleMeter : eleMeters) {
						batchMeters.add(eleMeter);
						if (BATCH_NUM == batchMeters.size()) {
							changeFactoryFileStart(request, batchMeters, concentrator, failLines);
							batchMeters.clear();
						}
					}
					changeFactoryFileStart(request, batchMeters, concentrator, failLines);
				}
			});
		}
	}

	/**
	 * 宿舍分批次删除|下载档案
	 */
	private void changeFileStart(HttpServletRequest request, List<SmtEleMeter> batchMeters, SmtEleMeterConcentrator concentrator,
								 List<EleMeterImportDTO> failLines) {
		String reason = changeFile(batchMeters, concentrator);
		if (StringUtils.isNotBlank(reason)) {
			for (SmtEleMeter eleMeter : batchMeters) {
				EleMeterImportDTO line = new EleMeterImportDTO();
				line.setDormitory(eleMeter.getDormitoryName());
				line.setRoom(eleMeter.getRoomName());
				line.setName(eleMeter.getName());
				line.setConcentratorIp(concentrator.getIp());
				line.setPortDesc(EleDownChannelEnum.desc(Integer.parseInt(eleMeter.getPort())));
				line.setSeq(eleMeter.getSeq());
				line.setAddress(eleMeter.getAddress());
				line.setMark(reason);
				failLines.add(line);
			}
		} else {
			this.saveBatch(batchMeters);
		}
		flushRemainImport(request, batchMeters.size());
	}

	/**
	 * 厂区分批次删除|下载档案
	 */
	private void changeFactoryFileStart(HttpServletRequest request, List<SmtEleMeter> batchMeters, SmtEleMeterConcentrator concentrator,
								 List<EleMeterFactoryImportDTO> failLines) {
		String reason = changeFile(batchMeters, concentrator);
		if (StringUtils.isNotBlank(reason)) {
			for (SmtEleMeter eleMeter : batchMeters) {
				EleMeterFactoryImportDTO line = new EleMeterFactoryImportDTO();
				line.setFactory(eleMeter.getAreaName());
				line.setName(eleMeter.getName());
				line.setConcentratorIp(concentrator.getIp());
				line.setPortDesc(EleDownChannelEnum.desc(Integer.parseInt(eleMeter.getPort())));
				line.setSeq(eleMeter.getSeq());
				line.setAddress(eleMeter.getAddress());
				line.setMark(reason);
				failLines.add(line);
			}
		} else {
			this.saveBatch(batchMeters);
		}
		flushRemainImport(request, batchMeters.size());
	}

	/**
	 * 刷新剩余导入条数
	 *
	 * @param request
	 * @param handleSize
	 */
	private void flushRemainImport(HttpServletRequest request, int handleSize) {
		Object eleRemainImport = request.getSession().getAttribute("eleRemainImport");
		request.getSession().setAttribute("eleRemainImport", Integer.parseInt(eleRemainImport.toString()) - handleSize);
	}

	/**
	 * 宿舍电表批量导入添加
	 */
	private void saveEleMeter(HttpServletRequest request, SmtEleMeterConcentrator concentrator, List<SmtEleMeter> eleMeters,
							  List<Integer> existSeq, List<EleMeterImportDTO> failLines, List<EleMeterImportDTO> importLines) {
		for (EleMeterImportDTO importLine : importLines) {
			Integer parkId = concentrator.getParkId();
			SmtPark park = smtParkService.getById(parkId);
			if (Objects.isNull(park)) {
				importLine.setMark("园区信息不存在");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			SmtDormitory dormitory = smtDormitoryService.getByParkAndName(parkId, importLine.getDormitory());
			if (Objects.isNull(dormitory)) {
				importLine.setMark("楼栋信息不存在");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			Integer roomName;
			try {
				roomName = Integer.parseInt(importLine.getRoom());
			} catch (Exception e) {
				importLine.setMark("房间号必须为数字");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			SmtDormitoryRoom room = smtDormitoryRoomService.getByDormitoryAndName(dormitory.getId(), roomName);
			if (Objects.isNull(room)) {
				importLine.setMark("房间信息不存在");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			boolean isExist = this.count(Wrappers.<SmtEleMeter>lambdaQuery()
					.eq(SmtEleMeter::getSeq, importLine.getSeq())
					.eq(SmtEleMeter::getConcentratorId, concentrator.getId())
			) > 0;
			if (isExist || existSeq.contains(importLine.getSeq())) {
				importLine.setMark("电表序号已被使用");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			Integer radio = importLine.getRadio();
			if (Objects.isNull(radio)) {
				importLine.setMark("倍率不能为空");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			Integer port = EleDownChannelEnum.code(importLine.getPortDesc());
			if (Objects.isNull(port)) {
				importLine.setMark("下行通道不存在");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			SmtEleMeter eleMeter = SmtEleMeter.builder()
					.name(importLine.getName())
					.seq(importLine.getSeq())
					.port(String.valueOf(port))
					.ratio(radio)
					.concentratorId(concentrator.getId())
					.roomId(room.getId())
					.roomName(String.valueOf(room.getRoomName()))
					.dormitoryId(dormitory.getId())
					.dormitoryName(dormitory.getDormitoryName())
					.parkId(park.getId())
					.parkName(park.getParkName())
					.address(importLine.getAddress())
					.placeType(PlaceTypeEnum.DORMITORY.getCode())
					.isOpen(ValveStatusEnum.OPEN.getCode())
					.isOnline(MeterStatusEnum.OUTLINE.getCode()).build();
			existSeq.add(eleMeter.getSeq());
			eleMeters.add(eleMeter);
		}
	}

	/**
	 * 厂区电表批量导入添加
	 */
	private void saveFactoryEleMeter(HttpServletRequest request, SmtEleMeterConcentrator concentrator, List<SmtEleMeter> eleMeters,
							  List<Integer> existSeq, List<EleMeterFactoryImportDTO> failLines, List<EleMeterFactoryImportDTO> importLines) {
		for (EleMeterFactoryImportDTO importLine : importLines) {
			Integer parkId = concentrator.getParkId();
			SmtPark park = smtParkService.getById(parkId);
			if (Objects.isNull(park)) {
				importLine.setMark("园区信息不存在");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			SmtArea smtArea = areaService.getByName(parkId, importLine.getFactory());
			if (Objects.isNull(smtArea)) {
				importLine.setMark("地点信息不存在");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			boolean isExist = this.count(Wrappers.<SmtEleMeter>lambdaQuery()
					.eq(SmtEleMeter::getSeq, importLine.getSeq())
					.eq(SmtEleMeter::getConcentratorId, concentrator.getId())
			) > 0;
			if (isExist || existSeq.contains(importLine.getSeq())) {
				importLine.setMark("电表序号已被使用");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			Integer radio = importLine.getRadio();
			if (Objects.isNull(radio)) {
				importLine.setMark("倍率不能为空");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			Integer port = EleDownChannelEnum.code(importLine.getPortDesc());
			if (Objects.isNull(port)) {
				importLine.setMark("下行通道不存在");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			SmtEleMeter eleMeter = SmtEleMeter.builder()
					.name(importLine.getName())
					.seq(importLine.getSeq())
					.port(String.valueOf(port))
					.ratio(radio)
					.concentratorId(concentrator.getId())
					.parkId(park.getId())
					.parkName(park.getParkName())
					.address(importLine.getAddress())
					.areaId(smtArea.getId())
					.areaName(smtArea.getAreaName())
					.placeType(PlaceTypeEnum.FACTORY.getCode())
					.isOpen(ValveStatusEnum.OPEN.getCode())
					.isOnline(MeterStatusEnum.OUTLINE.getCode()).build();
			existSeq.add(eleMeter.getSeq());
			eleMeters.add(eleMeter);
		}
	}

	private void checkExist(Long id, Integer seq, Long concentratorId) {
		boolean isExist = this.count(Wrappers.<SmtEleMeter>lambdaQuery()
				.eq(SmtEleMeter::getSeq, seq)
				.eq(SmtEleMeter::getConcentratorId, concentratorId)
				.ne(Objects.nonNull(id), SmtEleMeter::getId, id)
		) > 0;
		if (isExist) {
			throw new SmartException("电表序号已被使用");
		}
	}

	/**
	 * 对电表档案做操作，如果操作失败，返回错误原因
	 *
	 * @param eleMeters
	 * @param concentrator
	 * @return
	 */
	private String changeFile(List<SmtEleMeter> eleMeters, SmtEleMeterConcentrator concentrator) {
		boolean isDel = operateFile(eleMeters, concentrator, EventEnum.ELE_METER_DEL_FILE.getCode());
		if (isDel) {
			boolean isDownLoad = operateFile(eleMeters, concentrator, EventEnum.ELE_METER_DOWNLOAD_FILE.getCode());
			if (!isDownLoad) {
				return "下载电表档案失败";
			}
		} else {
			return "删除电表档案失败";
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 操作电表档案
	 *
	 * @param eleMeters
	 * @param concentrator
	 * @param eventType
	 * @return
	 */
	private Boolean operateFile(List<SmtEleMeter> eleMeters, SmtEleMeterConcentrator concentrator, Integer eventType) {
		JSONArray jsonArray = new JSONArray();
		for (SmtEleMeter meter : eleMeters) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("seq", meter.getSeq());
			jsonObject.put("port", meter.getPort());
			jsonObject.put("address", meter.getAddress());
			jsonArray.add(jsonObject);
		}
		return meterHelper.meterFile(eventType, concentrator.getId(), concentrator.getAddress(),
				concentrator.getParkId(), concentrator.getIp(), concentrator.getPort(), eleMeters.size(), jsonArray.toString());
	}
}
