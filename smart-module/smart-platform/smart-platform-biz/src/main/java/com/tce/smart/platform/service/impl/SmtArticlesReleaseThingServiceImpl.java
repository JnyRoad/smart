package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtArticlesReleaseThing;
import com.tce.smart.platform.core.mapper.SmtArticlesReleaseThingMapper;
import com.tce.smart.platform.service.SmtArticlesReleaseThingService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * (SmtArticlesReleaseThing)表服务实现类
 *
 * @author sunfujian
 * @date 2021-08-16 13:54:48
 */
@Service
public class SmtArticlesReleaseThingServiceImpl extends ServiceImpl<SmtArticlesReleaseThingMapper, SmtArticlesReleaseThing> implements SmtArticlesReleaseThingService {

	@Override
	public List<SmtArticlesReleaseThing> getListByReleaseId(Long releaseId) {
		return list(Wrappers.<SmtArticlesReleaseThing>lambdaQuery().eq(SmtArticlesReleaseThing::getReleaseId, releaseId));
	}
}
