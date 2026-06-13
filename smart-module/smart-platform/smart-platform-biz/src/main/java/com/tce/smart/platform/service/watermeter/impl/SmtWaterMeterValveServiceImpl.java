package com.tce.smart.platform.service.watermeter.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.platform.api.dto.req.watermeter.*;
import com.tce.smart.platform.core.dto.OperateLogDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterValve;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterValveConcentrator;
import com.tce.smart.platform.core.mapper.watermeter.SmtWaterMeterValveMapper;
import com.tce.smart.platform.core.mapper.watermeter.SmtWaterValveConcentratorMapper;
import com.tce.smart.platform.emun.ValveStatusEnum;
import com.tce.smart.platform.emun.operateLog.CodeEnum;
import com.tce.smart.platform.emun.operateLog.MeterOperateEnum;
import com.tce.smart.platform.helper.MeterHelper;
import com.tce.smart.platform.service.SmtOperateLogService;
import com.tce.smart.platform.service.watermeter.SmtWaterMeterValveService;
import com.tce.smart.platform.service.watermeter.SmtWaterValveTagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:42
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmtWaterMeterValveServiceImpl extends ServiceImpl<SmtWaterMeterValveMapper, SmtWaterMeterValve> implements SmtWaterMeterValveService {

	private final MeterHelper meterHelper;

	private final SmtWaterValveConcentratorMapper valveConcentratorMapper;

	private final SmtWaterValveTagService valveTagService;

	private final SmtOperateLogService operateLogService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveValve(WaterMeterValveAddDTO dto) {
		SmtWaterValveConcentrator concentrator = valveConcentratorMapper.selectById(dto.getConcentratorId());
		SmtWaterMeterValve valve = SmtWaterMeterValve.builder()
				.name(dto.getName())
				.seq(dto.getSeq())
				.concentratorId(dto.getConcentratorId())
				.parkId(concentrator.getParkId())
				.remark(dto.getRemark())
				.isOpen(ValveStatusEnum.CLOSE.getCode())
				.build();
		boolean save = this.save(valve);
		if (save && CollUtil.isNotEmpty(dto.getTagIds())) {
			// 更新设备对应标签
			WaterValveTagAddDTO tagAddDTO = new WaterValveTagAddDTO();
			tagAddDTO.setValveIds(CollUtil.newArrayList(valve.getId()));
			tagAddDTO.setTagIds(dto.getTagIds());
			save = valveTagService.setValveTag(tagAddDTO);
		}
		return save;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateValve(WaterMeterValveUpdateDTO dto) {
		SmtWaterMeterValve valve = this.getById(dto.getId());
		if (valve == null) {
			throw new SmartException("外置阀门不存在");
		}
		valve.setName(dto.getName());
		valve.setSeq(dto.getSeq());
		valve.setRemark(dto.getRemark());
		valve.setConcentratorId(dto.getConcentratorId());
		if (CollUtil.isNotEmpty(dto.getTagIds())) {
			// 更新设备对应标签
			WaterValveTagAddDTO tagAddDTO = new WaterValveTagAddDTO();
			tagAddDTO.setValveIds(CollUtil.newArrayList(valve.getId()));
			tagAddDTO.setTagIds(dto.getTagIds());
			valveTagService.setValveTag(tagAddDTO);
		}
		return this.updateById(valve);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public Boolean changeValveStatus(Long id, Integer status) {
		SmtWaterMeterValve valve = getById(id);
		if (valve == null) {
			throw new SmartException("外置阀门不存在");
		}
		SmtWaterValveConcentrator concentrator = valveConcentratorMapper.selectById(valve.getConcentratorId());
		if (concentrator == null) {
			throw new SmartException("阀门集中器不存在");
		}
		if (ValveStatusEnum.OPEN.getCode().equals(status)) {
			valve.setIsOpen(ValveStatusEnum.ON_OPEN.getCode());
		} else {
			valve.setIsOpen(ValveStatusEnum.ON_CLOSE.getCode());
		}
		// 新增开阀|关阀操作记录
		OperateLogDTO logDTO = new OperateLogDTO();
		logDTO.setTargetId(valve.getId());
		logDTO.setCode(CodeEnum.METER.getCode());
		logDTO.setCodeDesc(CodeEnum.METER.getDesc());
		logDTO.setAction(ValveStatusEnum.OPEN.getCode().equals(status) ? MeterOperateEnum.OPEN.getAction()
				: MeterOperateEnum.CLOSE.getAction());
		operateLogService.addLog(logDTO);
		if (meterHelper.changeValveStatus(EventEnum.WATER_METER_OUT_VALVE_CONTROL.getCode(), concentrator.getIp(),
				concentrator.getPort(), valve.getConcentratorId().toString(), null,
				valve.getSeq(), status, valve.getParkId())) {
			valve.setIsOpen(status);
			return this.updateById(valve);
		}
		throw new SmartException("操作失败");
	}

    @Override
    public Boolean changeValveRemoteStatus(Long valveId, Integer status) {
		SmtWaterMeterValve valve = getById(valveId);
		if (valve == null) {
			throw new SmartException("外置阀门不存在");
		}
		SmtWaterValveConcentrator concentrator = valveConcentratorMapper.selectById(valve.getConcentratorId());
		if (concentrator == null) {
			throw new SmartException("阀门集中器不存在");
		}
		if (ValveStatusEnum.OPEN.getCode().equals(status)) {
			valve.setIsOpen(ValveStatusEnum.ON_OPEN.getCode());
		} else {
			valve.setIsOpen(ValveStatusEnum.ON_CLOSE.getCode());
		}
		// 新增开|关远程功能操作记录
		OperateLogDTO logDTO = new OperateLogDTO();
		logDTO.setTargetId(valve.getId());
		logDTO.setCode(CodeEnum.METER_REMOTE.getCode());
		logDTO.setCodeDesc(CodeEnum.METER_REMOTE.getDesc());
		logDTO.setAction(ValveStatusEnum.OPEN.getCode().equals(status) ? MeterOperateEnum.OPEN.getAction()
				: MeterOperateEnum.CLOSE.getAction());
		operateLogService.addLog(logDTO);
		if (meterHelper.changeValveStatus(EventEnum.WATER_METER_OUT_VALVE_REMOTE_CONTROL.getCode(), concentrator.getIp(),
				concentrator.getPort(), valve.getConcentratorId().toString(), null,
				valve.getSeq(), status, valve.getParkId())) {
			valve.setRemoteStatus(status);
			return this.updateById(valve);
		}
		throw new SmartException("操作失败");
    }

    @Override
	public Boolean changeValveStatus(SmartValveDataUpdateDTO dto) {
		SmtWaterMeterValve valve = this.getOne(Wrappers.<SmtWaterMeterValve>lambdaQuery()
				.eq(SmtWaterMeterValve::getConcentratorId, dto.getDeviceCode())
				.eq(SmtWaterMeterValve::getSeq, dto.getValveSeq()));
		if (Objects.isNull(valve)) {
			throw new SmartException("外置阀门不存在");
		}
		valve.setIsOpen(Integer.parseInt(dto.getValveState()));
		return this.updateById(valve);
	}

	@Override
	public IPage<SmtWaterMeterValve> getPage(Page page, WaterMeterValveQueryDTO dto) {
		return this.page(page, Wrappers.<SmtWaterMeterValve>lambdaQuery()
						.eq(Objects.nonNull(dto.getOpenStatus()),SmtWaterMeterValve::getIsOpen,dto.getOpenStatus())
						.eq(Objects.nonNull(dto.getRemoteStatus()),SmtWaterMeterValve::getRemoteStatus,dto.getRemoteStatus())
				.like(Objects.nonNull(dto.getName()), SmtWaterMeterValve::getName, dto.getName()));
	}

	@Override
	public Boolean existWaterMeterByConcentratorId(Long conId) {
		return count(Wrappers.<SmtWaterMeterValve>lambdaQuery().eq(SmtWaterMeterValve::getConcentratorId, conId)) > 0;
	}
}
