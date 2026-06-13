package com.tce.smart.admin.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysMoveDataTask;
import com.tce.smart.admin.mapper.SysMoveDataTaskMapper;
import com.tce.smart.admin.service.SysMoveDataTaskService;
import com.tce.smart.common.core.constant.enums.DateMoveEeffectEnum;
import com.tce.smart.common.core.constant.enums.DateMoveOptEnum;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据转移务表配置 服务实现类
 *
 * @author mkwu
 * @date 2019-08-06
 */
@Service
@Slf4j
public class SysMoveDataTaskServiceImpl extends ServiceImpl<SysMoveDataTaskMapper, SysMoveDataTask>
		implements SysMoveDataTaskService {

	@Override
	public List<SysMoveDataTask> getModuleTaskList(Integer moduleType) {
		QueryWrapper<SysMoveDataTask> queryWrapper = new QueryWrapper<SysMoveDataTask>();
		queryWrapper.lambda().eq(SysMoveDataTask::getEffectFlag, DateMoveEeffectEnum.ENABLED.getStatus());

		if (Objects.nonNull(moduleType)) {
			queryWrapper.lambda().eq(SysMoveDataTask::getModuleType, moduleType);
		}

		return this.list(queryWrapper);
	}

	@Override
	@Transactional
	public void processData(SysMoveDataTask sysMoveDataTask) {
		if (Objects.isNull(sysMoveDataTask)) {
			return;
		}

		Integer optTytpe = sysMoveDataTask.getOptType();
		String srcTable = sysMoveDataTask.getSrcTable();
		String destTable = sysMoveDataTask.getDestTable();
		String dateColumn = sysMoveDataTask.getDateColumnName();
		try {
			Date endDate = DateUtil.offsetMonth(Calendar.getInstance().getTime(), -sysMoveDataTask.getRetainMonth());

			// 数据转移
			if (DateMoveOptEnum.MOVE.getCode().equals(optTytpe)) {
				this.baseMapper.moveData(srcTable, destTable, dateColumn, endDate);
				int count = this.baseMapper.deleteData(srcTable, dateColumn, endDate);
				log.info("move task deleteData===count={}",count);
			}
			// 数据删除
			else if (DateMoveOptEnum.DELETE.getCode().equals(optTytpe)) {
				int count = this.baseMapper.deleteData(srcTable, dateColumn, endDate);
				log.info("move task deleteData===count={}",count);
			}
		} catch (Exception e) {
			log.error("转移数据异常", e);
		}
	}
}
