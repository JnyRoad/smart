package com.tce.smart.platform.service.watermeter.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.NumberConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.DeviceDataDTO;
import com.tce.smart.platform.api.dto.req.watermeter.*;
import com.tce.smart.platform.core.entity.SmtArea;
import com.tce.smart.platform.core.entity.SmtDormitory;
import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeter;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterConcentrator;
import com.tce.smart.platform.core.mapper.watermeter.SmtWaterMeterMapper;
import com.tce.smart.platform.emun.*;
import com.tce.smart.platform.helper.MeterHelper;
import com.tce.smart.platform.service.SmtAreaService;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import com.tce.smart.platform.service.SmtDormitoryService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterChangeService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterConcentratorService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterTagService;
import com.tce.smart.platform.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:41
 */
@Slf4j
@Service
public class SmtWaterMeterServiceImpl extends ServiceImpl<SmtWaterMeterMapper, SmtWaterMeter> implements SmtWaterMeterService {

	@Autowired
	private SmtWaterMeterTagService meterTagService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtDormitoryRoomService smtDormitoryRoomService;
	@Autowired
	private SmtDormitoryService smtDormitoryService;
	@Autowired
	private SmtWaterMeterConcentratorService concentratorService;
	@Resource
	private RemoteDispatcherService remoteDispatcherService;
	@Autowired
	private SmtAreaService areaService;
	@Autowired
	private MeterHelper meterHelper;
	@Autowired
	private SmtWaterMeterChangeService waterMeterChangeService;
	/**
	 * 批量删除|下载限制条数
	 */
	private final static int BATCH_NUM = 50;

	@Override
	public Boolean existWaterMeterByConcentratorId(Long conId) {
		return count(Wrappers.<SmtWaterMeter>lambdaQuery().eq(SmtWaterMeter::getConcentratorId, conId)) > 0;
	}

	@Override
	public void getReading(WaterMeterOperateDTO operate) {
		List<SmtWaterMeter> meterList = this.list(Wrappers.<SmtWaterMeter>lambdaQuery()
				.in(SmtWaterMeter::getId, operate.getMeterIds()));
		for (SmtWaterMeter meter : meterList) {
			meter.setIsOnline(startReading(meter) ? MeterStatusEnum.ONLINE.getCode() : MeterStatusEnum.OUTLINE.getCode());
			meter.setUpdateUserId(0);
			meter.setUpdateTime(LocalDateTime.now());
			this.updateById(meter);
			// 每条请求延迟500ms
			ThreadUtil.sleep(500);
		}
	}

	@Override
	public void reDownload(WaterMeterOperateDTO operate) {
		List<SmtWaterMeter> meterList = this.list(Wrappers.<SmtWaterMeter>lambdaQuery()
				.in(SmtWaterMeter::getId, operate.getMeterIds()));
		for (SmtWaterMeter waterMeter : meterList) {
			SmtWaterMeterConcentrator concentrator = concentratorService.getById(waterMeter.getConcentratorId());
			String reason = changeFile(CollUtil.newArrayList(waterMeter), concentrator);
			if (StringUtils.isNotBlank(reason)) {
				log.warn("{}重新下载档案失败，原因：{}", waterMeter.getName(), reason);
			}
		}
	}

	@Override
	public IPage<SmtWaterMeter> getPage(Page page, WaterMeterQueryDTO dto) {
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
		return this.page(page, Wrappers.<SmtWaterMeter>lambdaQuery()
				.eq(Objects.nonNull(dto.getConcenId()), SmtWaterMeter::getConcentratorId, dto.getConcenId())
				.like(Objects.nonNull(dto.getName()), SmtWaterMeter::getName, dto.getName())
				.eq(Objects.nonNull(dto.getStatus()), SmtWaterMeter::getIsOnline, dto.getStatus())
				.eq(Objects.nonNull(dto.getDormitoryId()), SmtWaterMeter::getDormitoryId, dto.getDormitoryId())
				.eq(Objects.nonNull(dto.getRoomId()), SmtWaterMeter::getRoomId, dto.getRoomId())
				.in(SmtWaterMeter::getParkId, parkIdList)
				.in(CollUtil.isNotEmpty(legalIds), SmtWaterMeter::getId, legalIds)
				.like(Objects.nonNull(dto.getAddress()), SmtWaterMeter::getAddress, dto.getAddress())
				.and(Objects.nonNull(dto.getPlace()), e -> e.like(SmtWaterMeter::getDormitoryName, dto.getPlace())
						.or()
						.like(SmtWaterMeter::getRoomName, dto.getPlace())
						.or()
						.like(SmtWaterMeter::getAreaName, dto.getPlace())
				)
				.orderByAsc(SmtWaterMeter::getConcentratorId,SmtWaterMeter::getRoomName));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean addMeter(WaterMeterAddDTO dto) {
		SmtWaterMeterConcentrator concentrator = concentratorService.getById(dto.getConcentratorId());
		if (Objects.isNull(concentrator)) {
			throw new SmartException("水表集中器不存在");
		}
		SmtWaterMeter waterMeter = SmtWaterMeter.builder()
				.name(dto.getName())
				.seq(dto.getSeq())
				.port(dto.getPort())
				.largeClass(dto.getLargeClass())
				.concentratorId(dto.getConcentratorId())
				.isOpen(ValveStatusEnum.CLOSE.getCode())
				.address(dto.getAddress())
				.placeType(dto.getPlaceType())
				.isOnline(MeterStatusEnum.OUTLINE.getCode()).build();
		saveValid(waterMeter, dto.getRoomId(), dto.getAreaId(), concentrator);
		checkExist(null, dto.getSeq(), concentrator.getId());
		boolean save = this.save(waterMeter);
		if (save && CollUtil.isNotEmpty(dto.getTagIds())) {
			// 更新设备对应标签
			WaterMeterTagAddDTO tagAddDTO = new WaterMeterTagAddDTO();
			tagAddDTO.setMeterIds(CollUtil.newArrayList(waterMeter.getId()));
			tagAddDTO.setTagIds(dto.getTagIds());
			save = meterTagService.setMeterTag(tagAddDTO);
		}
		String reason = changeFile(CollUtil.newArrayList(waterMeter), concentrator);
		if (StringUtils.isNotBlank(reason)) {
			throw new SmartException(reason);
		}
		return save;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateMeter(WaterMeterUpdateDTO dto, Boolean isChangeDevice) {
		SmtWaterMeter waterMeter = this.getById(dto.getId());
		if (waterMeter == null) {
			throw new SmartException("水表不存在");
		}
		Long concentratorId = waterMeter.getConcentratorId();
		String beforeAddress = waterMeter.getAddress();
		String beforePort = waterMeter.getPort();
		String largeClass = waterMeter.getLargeClass();
		Integer seq = waterMeter.getSeq();
		SmtWaterMeterConcentrator concentrator = concentratorService.getById(dto.getConcentratorId());
		if (Objects.isNull(concentrator)) {
			throw new SmartException("水表集中器不存在");
		}
		checkExist(waterMeter.getId(), dto.getSeq(), dto.getConcentratorId());
		// 表号发生变化
		boolean isChangeAddress = !dto.getAddress().equals(waterMeter.getAddress());
		// 水表档案数据发生变化
		boolean isOperate = isChangeAddress || !dto.getSeq().equals(waterMeter.getSeq())
				|| !dto.getPort().equals(waterMeter.getPort()) || !dto.getLargeClass().equals(waterMeter.getLargeClass())
				|| !dto.getConcentratorId().equals(waterMeter.getConcentratorId());
		// 换表未修改表号
		if (isChangeDevice && !isChangeAddress) {
			throw new SmartException("无法换表，未修改表号");
		}
		waterMeter.setName(dto.getName());
		waterMeter.setAddress(dto.getAddress());
		waterMeter.setSeq(dto.getSeq());
		waterMeter.setPort(dto.getPort());
		waterMeter.setLargeClass(dto.getLargeClass());
		waterMeter.setConcentratorId(dto.getConcentratorId());
		waterMeter.setPlaceType(dto.getPlaceType());
		saveValid(waterMeter, dto.getRoomId(), dto.getAreaId(), concentrator);
		if (CollUtil.isNotEmpty(dto.getTagIds())) {
			// 更新设备对应标签
			WaterMeterTagAddDTO tagAddDTO = new WaterMeterTagAddDTO();
			tagAddDTO.setMeterIds(CollUtil.newArrayList(waterMeter.getId()));
			tagAddDTO.setTagIds(dto.getTagIds());
			meterTagService.setMeterTag(tagAddDTO);
		}
		if (isOperate) {
			String reason = changeFile(CollUtil.newArrayList(waterMeter), concentrator);
			if (StringUtils.isNotBlank(reason)) {
				throw new SmartException(reason);
			}
		}
		if (isChangeDevice) {
			Long beforeId = waterMeter.getId();
			waterMeter.setId(IdWorker.getId());
			this.save(waterMeter);
			// 主动读取更换后水表读数
			startReading(waterMeter);
			// 新增水表更换记录
			waterMeterChangeService.addRecord(concentratorId, waterMeter.getId(), beforeAddress,
					beforePort, largeClass, seq, dto);
			return this.removeById(beforeId);
		}
		return this.updateById(waterMeter);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void excelImport(HttpServletRequest request, HttpServletResponse response, InputStream inputStream) {
		String[] excelHead = {"楼栋", "房间号", "设备名称", "集中器IP", "下行通道", "用户大类", "设备序号", "水表通信地址"};
		String[] excelHeadAlias = {"dormitory", "room", "name", "concentratorIp", "portDesc", "largeClassDesc", "seq", "address"};
		String[] excelFactoryHead = {"地点", "设备名称", "集中器IP", "下行通道", "用户大类", "设备序号", "水表通信地址"};
		String[] excelFactoryHeadAlias = {"factory", "name", "concentratorIp", "portDesc", "largeClassDesc", "seq", "address"};
		ExcelReader reader = ExcelUtil.getReader(inputStream);
		List<WaterMeterImportDTO> importLines = ExcelUtils.importExcel(reader, excelHead, excelHeadAlias,
				PlaceTypeEnum.DORMITORY.getDesc(), WaterMeterImportDTO.class, 0, 1);
		List<WaterMeterFactoryImportDTO> importFactoryLines = ExcelUtils.importExcel(reader, excelFactoryHead, excelFactoryHeadAlias,
				PlaceTypeEnum.FACTORY.getDesc(), WaterMeterFactoryImportDTO.class, 0, 1);
		if (CollectionUtils.isEmpty(importLines) && CollectionUtils.isEmpty(importFactoryLines)) {
			throw new SmartException("没有解析到数据,请检查模板是否正确!");
		}
		List<WaterMeterImportDTO> failLines = new ArrayList<>();
		List<WaterMeterFactoryImportDTO> factoryFailLines = new ArrayList<>();
		int maxSize = 0;
		if (CollectionUtils.isNotEmpty(importLines)) {
			maxSize += importLines.size();
		}
		if (CollectionUtils.isNotEmpty(importFactoryLines)) {
			maxSize += importFactoryLines.size();
		}
		// 写入剩余导入条数为最大值
		request.getSession().setAttribute("waterMaxImport", maxSize);
		request.getSession().setAttribute("waterRemainImport", maxSize);
		checkData(request, importLines, failLines);
		checkFactoryData(request, importFactoryLines, factoryFailLines);
		// 如果存在失败项返回给前端
		if (failLines.size() > NumberConstants.ZERO || factoryFailLines.size() > NumberConstants.ZERO) {
			try {
				String fileNameEncode = URLEncoder.encode("导入失败水表信息.xls", "UTF-8");
				ExcelUtils.export(request, response, fileNameEncode, PlaceTypeEnum.DORMITORY.getDesc(),
						failLines, PlaceTypeEnum.FACTORY.getDesc(), factoryFailLines);
			} catch (Exception e) {
				throw new SmartException("导出错误数据失败");
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean changeValveStatus(Long id, Integer status) {
		SmtWaterMeter waterMeter = this.getById(id);
		return valveChange(waterMeter, status);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String batchChangeValveStatus(WaterMeterValveOperateDTO operate) {
		List<Long> meterIds = operate.getMeterIds();
		if (CollectionUtils.isEmpty(meterIds)) {
			throw new SmartException("请选择水表");
		}
		int isOpen = ValveStatusEnum.OPEN.getCode().equals(operate.getStatus())
				? ValveStatusEnum.CLOSE.getCode() : ValveStatusEnum.OPEN.getCode();
		List<SmtWaterMeter> waterMeters = this.list(Wrappers.<SmtWaterMeter>lambdaQuery()
				.eq(SmtWaterMeter::getIsOpen, isOpen)
				.in(SmtWaterMeter::getId, meterIds));
		int failSize = 0;
		String failRoom = "";
		for (SmtWaterMeter waterMeter : waterMeters) {
			try {
				boolean change = valveChange(waterMeter, operate.getStatus());
				if (!change) {
					failSize++;
					failRoom = failRoom.concat("{房间号").concat(waterMeter.getRoomName()).concat("}");
				}
			} catch (Exception e) {
				log.error("水表{}阀门控制失败", waterMeter, e);
				failSize++;
				failRoom = failRoom.concat("{房间号").concat(waterMeter.getRoomName()).concat("}");
			}
		}
		if (failSize > 0) {
			return ValveStatusEnum.desc(operate.getStatus()) + "设备失败" + failSize + "个，为" + failRoom;
		}
		return StringUtils.EMPTY;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean changeValveStatus(SmartValveDataUpdateDTO dto) {
		SmtWaterMeter waterMeter = this.getOne(Wrappers.<SmtWaterMeter>lambdaQuery()
				.eq(SmtWaterMeter::getConcentratorId, dto.getDeviceCode())
				.eq(SmtWaterMeter::getSeq, dto.getWaterMeterSeq()));
		if (Objects.isNull(waterMeter)) {
			throw new SmartException("水表不存在");
		}
		waterMeter.setIsOpen(Integer.parseInt(dto.getValveState()));
		return this.updateById(waterMeter);
	}

	@Override
	public SmtWaterMeter getByConcentratorIdAndSeq(Long concentratorId, Integer seq) {
		return getOne(Wrappers.<SmtWaterMeter>lambdaQuery()
				.eq(SmtWaterMeter::getConcentratorId, concentratorId)
				.eq(SmtWaterMeter::getSeq, seq), false);
	}

	@Override
	public List<SmtWaterMeter> getByConcentratorId(Long concentratorId) {
		return this.list(Wrappers.<SmtWaterMeter>lambdaQuery()
				.eq(SmtWaterMeter::getConcentratorId, concentratorId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String remove(WaterMeterOperateDTO delList) {
		List<Long> meterIds = delList.getMeterIds();
		if (CollectionUtils.isEmpty(meterIds)) {
			throw new SmartException("请选择要删除的水表");
		}
		int failSize = 0;
		String failRoom = "";
		for (Long meterId : meterIds) {
			SmtWaterMeter meter = this.getById(meterId);
			SmtWaterMeterConcentrator concentrator = concentratorService.getById(meter.getConcentratorId());
			// 先删除水表档案
			if (!operateFile(CollUtil.newArrayList(meter), concentrator, EventEnum.WATER_METER_DEL_FILE.getCode())) {
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

	/**
	 * 水表读数开始
	 */
	private Boolean startReading(SmtWaterMeter meter) {
		SmtWaterMeterConcentrator concentrator = concentratorService.getById(meter.getConcentratorId());
		try {
			// 园区分发
			DeviceDataDTO deviceInfo = new DeviceDataDTO();
			deviceInfo.setDeviceCode(meter.getConcentratorId().toString());
			deviceInfo.setDeviceIp(concentrator.getIp());
			deviceInfo.setDevicePort(Integer.parseInt(concentrator.getPort()));
			deviceInfo.setWaterMeterSeq(meter.getSeq());

			DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setEventType(EventEnum.WATER_METER_READ.getCode());
			dispatcherDTO.setParkId(meter.getParkId());
			dispatcherDTO.setDeviceId(meter.getConcentratorId().toString());
			dispatcherDTO.setData(deviceInfo);
			Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			if (!result.isSuccess()) {
				throw new SmartException("水表" + meter.getId() + "读数请求失败");
			}
			JSONObject object = JSONUtil.parseObj(result.getData());
			String code = "code";
			log.info("水表读数数据：{}", object);
			if (!object.containsKey(code)) {
				throw new SmartException("数据格式不正确，水表读数请求失败");
			}
			return object.getBool("data");
		} catch (Exception e) {
			log.error("水表{}读数请求失败", meter.getId());
			return false;
		}
	}

	/**
	 * 阀门控制
	 */
	private Boolean valveChange(SmtWaterMeter waterMeter, Integer status) {
		if (waterMeter == null) {
			throw new SmartException("水表不存在");
		}
		SmtWaterMeterConcentrator concentrator = concentratorService.getById(waterMeter.getConcentratorId());
		if (concentrator == null) {
			throw new SmartException("水表集中器不存在");
		}
		if (ValveStatusEnum.OPEN.getCode().equals(status)) {
			waterMeter.setIsOpen(ValveStatusEnum.ON_OPEN.getCode());
		} else {
			waterMeter.setIsOpen(ValveStatusEnum.ON_CLOSE.getCode());
		}
		if (meterHelper.changeValveStatus(EventEnum.WATER_METER_IN_VALVE_CONTROL.getCode(), concentrator.getIp(),
				concentrator.getPort(), waterMeter.getConcentratorId().toString(), waterMeter.getSeq(),
				null, status, waterMeter.getParkId())) {
			return this.updateById(waterMeter);
		}
		return Boolean.FALSE;
	}

	/**
	 * 新增或修改时写入房间或区域
	 */
	private void saveValid(SmtWaterMeter waterMeter, Integer roomId, Integer areaId,
						   SmtWaterMeterConcentrator concentrator) {
		Integer parkId;
		Integer placeType = waterMeter.getPlaceType();
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
			waterMeter.setRoomId(room.getId());
			waterMeter.setRoomName(String.valueOf(room.getRoomName()));
			waterMeter.setDormitoryId(dormitory.getId());
			waterMeter.setDormitoryName(dormitory.getDormitoryName());
			parkId = dormitory.getParkId();
		} else {
			if (Objects.isNull(areaId)) {
				throw new SmartException("区域ID不能为空");
			}
			SmtArea area = areaService.getById(areaId);
			if (Objects.isNull(area)) {
				throw new SmartException("区域信息不存在");
			}
			waterMeter.setAreaId(area.getId());
			waterMeter.setAreaName(area.getAreaName());
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
		waterMeter.setParkId(parkId);
		waterMeter.setParkName(park.getParkName());
	}

	/**
	 * 导入并验证水表数据
	 */
	private void checkData(HttpServletRequest request, List<WaterMeterImportDTO> importLines, List<WaterMeterImportDTO> failLines) {
		// 宿舍导入为空
		if (CollectionUtils.isEmpty(importLines)) {
			return;
		}
		// 按水表集中器名称分组
		Map<String, List<WaterMeterImportDTO>> concentratorMap = importLines.stream()
				.collect(Collectors.groupingBy(WaterMeterImportDTO::getConcentratorIp));
		if (CollectionUtils.isNotEmpty(concentratorMap)) {
			concentratorMap.forEach((concentratorIp, lines) -> {
				// 水表档案列表
				List<SmtWaterMeter> waterMeters = new ArrayList<>();
				List<SmtWaterMeter> batchMeters = new ArrayList<>();
				List<Integer> existSeq = new ArrayList<>();
				SmtWaterMeterConcentrator concentrator = concentratorService.getByIp(concentratorIp);
				if (Objects.isNull(concentrator)) {
					for (WaterMeterImportDTO line : lines) {
						line.setMark("水表集中器不存在");
						failLines.add(line);
					}
					flushRemainImport(request, lines.size());
				} else {
					saveWaterMeter(request, concentrator, waterMeters, existSeq, failLines, lines);
					for (SmtWaterMeter waterMeter : waterMeters) {
						batchMeters.add(waterMeter);
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
	 * 厂区导入并验证水表数据
	 */
	private void checkFactoryData(HttpServletRequest request, List<WaterMeterFactoryImportDTO> importLines,
								  List<WaterMeterFactoryImportDTO> failLines) {
		// 厂区导入为空
		if (CollectionUtils.isEmpty(importLines)) {
			return;
		}
		// 按水表集中器名称分组
		Map<String, List<WaterMeterFactoryImportDTO>> concentratorMap = importLines.stream()
				.collect(Collectors.groupingBy(WaterMeterFactoryImportDTO::getConcentratorIp));
		if (CollectionUtils.isNotEmpty(concentratorMap)) {
			concentratorMap.forEach((concentratorIp, lines) -> {
				// 水表档案列表
				List<SmtWaterMeter> waterMeters = new ArrayList<>();
				List<SmtWaterMeter> batchMeters = new ArrayList<>();
				List<Integer> existSeq = new ArrayList<>();
				SmtWaterMeterConcentrator concentrator = concentratorService.getByIp(concentratorIp);
				if (Objects.isNull(concentrator)) {
					for (WaterMeterFactoryImportDTO line : lines) {
						line.setMark("水表集中器不存在");
						failLines.add(line);
					}
					flushRemainImport(request, lines.size());
				} else {
					saveFactoryWaterMeter(request, concentrator, waterMeters, existSeq, failLines, lines);
					for (SmtWaterMeter waterMeter : waterMeters) {
						batchMeters.add(waterMeter);
						if (BATCH_NUM == batchMeters.size()) {
							factoryChangeFileStart(request, batchMeters, concentrator, failLines);
							batchMeters.clear();
						}
					}
					factoryChangeFileStart(request, batchMeters, concentrator, failLines);
				}
			});
		}
	}

	/**
	 * 宿舍分批次删除|下载档案
	 */
	private void changeFileStart(HttpServletRequest request, List<SmtWaterMeter> batchMeters, SmtWaterMeterConcentrator concentrator,
								 List<WaterMeterImportDTO> failLines) {
		String reason = changeFile(batchMeters, concentrator);
		if (StringUtils.isNotBlank(reason)) {
			for (SmtWaterMeter waterMeter : batchMeters) {
				WaterMeterImportDTO line = new WaterMeterImportDTO();
				line.setDormitory(waterMeter.getDormitoryName());
				line.setRoom(waterMeter.getRoomName());
				line.setName(waterMeter.getName());
				line.setConcentratorIp(concentrator.getIp());
				line.setPortDesc(DownChannelEnum.desc(Integer.parseInt(waterMeter.getPort())));
				line.setLargeClassDesc(LargeClassEnum.desc(Integer.parseInt(waterMeter.getLargeClass())));
				line.setSeq(waterMeter.getSeq());
				line.setAddress(waterMeter.getAddress());
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
	private void factoryChangeFileStart(HttpServletRequest request, List<SmtWaterMeter> batchMeters, SmtWaterMeterConcentrator concentrator,
								 List<WaterMeterFactoryImportDTO> failLines) {
		String reason = changeFile(batchMeters, concentrator);
		if (StringUtils.isNotBlank(reason)) {
			for (SmtWaterMeter waterMeter : batchMeters) {
				WaterMeterFactoryImportDTO line = new WaterMeterFactoryImportDTO();
				line.setFactory(waterMeter.getAreaName());
				line.setName(waterMeter.getName());
				line.setConcentratorIp(concentrator.getIp());
				line.setPortDesc(DownChannelEnum.desc(Integer.parseInt(waterMeter.getPort())));
				line.setLargeClassDesc(LargeClassEnum.desc(Integer.parseInt(waterMeter.getLargeClass())));
				line.setSeq(waterMeter.getSeq());
				line.setAddress(waterMeter.getAddress());
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
		Object eleRemainImport = request.getSession().getAttribute("waterRemainImport");
		request.getSession().setAttribute("waterRemainImport", Integer.parseInt(eleRemainImport.toString()) - handleSize);
	}

	/**
	 * 宿舍水表批量导入添加
	 */
	private void saveWaterMeter(HttpServletRequest request, SmtWaterMeterConcentrator concentrator, List<SmtWaterMeter> waterMeters,
								List<Integer> existSeq, List<WaterMeterImportDTO> failLines, List<WaterMeterImportDTO> importLines) {
		for (WaterMeterImportDTO importLine : importLines) {
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
			boolean isExistSeq = this.count(Wrappers.<SmtWaterMeter>lambdaQuery()
					.eq(SmtWaterMeter::getSeq, importLine.getSeq())
					.eq(SmtWaterMeter::getConcentratorId, concentrator.getId())
			) > 0;
			if (isExistSeq || existSeq.contains(importLine.getSeq())) {
				importLine.setMark("水表序号已被使用");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			Integer port = DownChannelEnum.code(importLine.getPortDesc());
			if (Objects.isNull(port)) {
				importLine.setMark("下行通道不存在");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			Integer largeClass = LargeClassEnum.code(importLine.getLargeClassDesc());
			if (Objects.isNull(largeClass)) {
				importLine.setMark("用户大类不存在");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			SmtWaterMeter waterMeter = SmtWaterMeter.builder()
					.name(importLine.getName())
					.seq(importLine.getSeq())
					.port(String.valueOf(port))
					.largeClass(String.valueOf(largeClass))
					.concentratorId(concentrator.getId())
					.roomId(room.getId())
					.roomName(String.valueOf(room.getRoomName()))
					.dormitoryId(dormitory.getId())
					.dormitoryName(dormitory.getDormitoryName())
					.parkId(park.getId())
					.parkName(park.getParkName())
					.isOpen(ValveStatusEnum.CLOSE.getCode())
					.address(importLine.getAddress())
					.placeType(PlaceTypeEnum.DORMITORY.getCode())
					.isOnline(MeterStatusEnum.OUTLINE.getCode()).build();
			existSeq.add(waterMeter.getSeq());
			waterMeters.add(waterMeter);
		}
	}

	/**
	 * 厂区水表批量导入添加
	 */
	private void saveFactoryWaterMeter(HttpServletRequest request, SmtWaterMeterConcentrator concentrator, List<SmtWaterMeter> waterMeters,
								List<Integer> existSeq, List<WaterMeterFactoryImportDTO> failLines, List<WaterMeterFactoryImportDTO> importLines) {
		for (WaterMeterFactoryImportDTO importLine : importLines) {
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
			boolean isExistSeq = this.count(Wrappers.<SmtWaterMeter>lambdaQuery()
					.eq(SmtWaterMeter::getSeq, importLine.getSeq())
					.eq(SmtWaterMeter::getConcentratorId, concentrator.getId())
			) > 0;
			if (isExistSeq || existSeq.contains(importLine.getSeq())) {
				importLine.setMark("水表序号已被使用");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			Integer port = DownChannelEnum.code(importLine.getPortDesc());
			if (Objects.isNull(port)) {
				importLine.setMark("下行通道不存在");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			Integer largeClass = LargeClassEnum.code(importLine.getLargeClassDesc());
			if (Objects.isNull(largeClass)) {
				importLine.setMark("用户大类不存在");
				failLines.add(importLine);
				flushRemainImport(request, NumberConstants.ONE);
				continue;
			}
			SmtWaterMeter waterMeter = SmtWaterMeter.builder()
					.name(importLine.getName())
					.seq(importLine.getSeq())
					.port(String.valueOf(port))
					.largeClass(String.valueOf(largeClass))
					.concentratorId(concentrator.getId())
					.parkId(park.getId())
					.parkName(park.getParkName())
					.isOpen(ValveStatusEnum.CLOSE.getCode())
					.address(importLine.getAddress())
					.placeType(PlaceTypeEnum.FACTORY.getCode())
					.areaId(smtArea.getId())
					.areaName(smtArea.getAreaName())
					.isOnline(MeterStatusEnum.OUTLINE.getCode()).build();
			existSeq.add(waterMeter.getSeq());
			waterMeters.add(waterMeter);
		}
	}

	private void checkExist(Long id, Integer seq, Long concentratorId) {
		boolean isExist = this.count(Wrappers.<SmtWaterMeter>lambdaQuery()
				.eq(SmtWaterMeter::getSeq, seq)
				.eq(SmtWaterMeter::getConcentratorId, concentratorId)
				.ne(Objects.nonNull(id), SmtWaterMeter::getId, id)
		) > 0;
		if (isExist) {
			throw new SmartException("水表序号已被使用");
		}
	}

	/**
	 * 对水表档案做操作，如果操作失败，返回错误原因
	 *
	 * @param waterMeters
	 * @param concentrator
	 * @return
	 */
	private String changeFile(List<SmtWaterMeter> waterMeters, SmtWaterMeterConcentrator concentrator) {
		boolean isDel = operateFile(waterMeters, concentrator, EventEnum.WATER_METER_DEL_FILE.getCode());
		if (isDel) {
			boolean isDownLoad = operateFile(waterMeters, concentrator, EventEnum.WATER_METER_DOWNLOAD_FILE.getCode());
			if (!isDownLoad) {
				return "下载水表档案失败";
			}
		} else {
			return "删除水表档案失败";
		}
		return StringUtils.EMPTY;
	}

	/**
	 * 操作水表档案
	 *
	 * @param waterMeters
	 * @param concentrator
	 * @param eventType
	 * @return
	 */
	private Boolean operateFile(List<SmtWaterMeter> waterMeters, SmtWaterMeterConcentrator concentrator, Integer eventType) {
		JSONArray jsonArray = new JSONArray();
		for (SmtWaterMeter meter : waterMeters) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("seq", meter.getSeq());
			jsonObject.put("port", meter.getPort());
			jsonObject.put("address", meter.getAddress());
			jsonObject.put("largeClass", meter.getLargeClass());
			jsonArray.add(jsonObject);
		}
		return meterHelper.meterFile(eventType, concentrator.getId(), concentrator.getAddress(),
				concentrator.getParkId(), concentrator.getIp(), concentrator.getPort(), waterMeters.size(), jsonArray.toString());
	}
}
