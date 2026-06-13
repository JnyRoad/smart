package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtDormitoryLevel;
import com.tce.smart.platform.core.mapper.SmtDormitoryLevelMapper;
import com.tce.smart.platform.service.SmtDormitoryLevelService;

import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.List;

/**
 * 宿舍职层关联表
 *
 * @author 齐佩
 * @date 2019-04-18 14:47:57
 */
@Service
public class SmtDormitoryLevelServiceImpl extends ServiceImpl<SmtDormitoryLevelMapper, SmtDormitoryLevel> implements SmtDormitoryLevelService {

	@Override
	public List<SmtDormitoryLevel> getByType(Integer typeId) {
		return this.list(Wrappers.<SmtDormitoryLevel>query().lambda().eq(SmtDormitoryLevel::getDormitoryTypeId, typeId));
	}

	@Override
	public List<SmtDormitoryLevel> getByJcheId(String jcheId) {
		return this.list(Wrappers.<SmtDormitoryLevel>query().lambda().eq(SmtDormitoryLevel::getJcheId, jcheId));
	}
}
