package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.OperateLogQueryDTO;
import com.tce.smart.platform.core.dto.OperateLogDTO;
import com.tce.smart.platform.core.entity.SmtOperateLog;
import com.tce.smart.platform.core.mapper.SmtOperateLogMapper;
import com.tce.smart.platform.service.SmtOperateLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 14:09
 */
@Service
public class SmtOperateLogServiceImpl extends ServiceImpl<SmtOperateLogMapper, SmtOperateLog> implements SmtOperateLogService {

	@Override
	public IPage<SmtOperateLog> getPage(Page page, OperateLogQueryDTO dto) {
		return this.page(page, getWrapper(dto));
	}

	@Override
	public List<SmtOperateLog> getList(OperateLogQueryDTO dto) {
		return this.list(getWrapper(dto));
	}

	private LambdaQueryWrapper getWrapper(OperateLogQueryDTO dto) {
		return Wrappers.<SmtOperateLog>lambdaQuery()
				.eq(SmtOperateLog::getTargetId, dto.getTargetId())
				.eq(Objects.nonNull(dto.getCode()), SmtOperateLog::getCode, dto.getCode())
				.eq(Objects.nonNull(dto.getAction()), SmtOperateLog::getAction, dto.getAction())
				.orderByDesc(SmtOperateLog::getCreateTime);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean addLog(OperateLogDTO operateLog) {
		String personName = SecurityUtils.getUser().getUsername();
		SmtOperateLog log = BeanUtils.transform(SmtOperateLog.class, operateLog);
		log.setCreateUserName(personName);
		return this.save(log);
	}
}
