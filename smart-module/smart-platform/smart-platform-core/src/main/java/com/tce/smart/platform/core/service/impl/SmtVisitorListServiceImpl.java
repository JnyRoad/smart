package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.mapper.SmtVisitorMapper;
import com.tce.smart.platform.core.service.SmtVisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmtVisitorListServiceImpl extends ServiceImpl<SmtVisitorMapper, SmtVisitor> implements SmtVisitorService {

	@Override
	public Boolean updateSmsCode(Long id) {
		return baseMapper.updateSmsCode(id);
	}
}
