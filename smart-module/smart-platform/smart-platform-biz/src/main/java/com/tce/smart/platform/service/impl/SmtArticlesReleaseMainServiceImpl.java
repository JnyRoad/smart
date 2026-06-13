package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtArticlesReleaseMain;
import com.tce.smart.platform.core.mapper.SmtArticlesReleaseMainMapper;
import com.tce.smart.platform.service.SmtArticlesReleaseMainService;
import org.springframework.stereotype.Service;

/**
 * (SmtArticlesReleaseMain)表服务实现类
 *
 * @author sunfujian
 * @date 2021-08-16 13:51:31
 */
@Service
public class SmtArticlesReleaseMainServiceImpl extends ServiceImpl<SmtArticlesReleaseMainMapper, SmtArticlesReleaseMain> implements SmtArticlesReleaseMainService {

	@Override
	public SmtArticlesReleaseMain getByReleaseId(Long releaseId) {
		return getOne(Wrappers.<SmtArticlesReleaseMain>lambdaQuery().eq(SmtArticlesReleaseMain::getReleaseId, releaseId), false);
	}
}
