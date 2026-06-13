package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtArticlesReleasePerson;
import com.tce.smart.platform.core.mapper.SmtArticlesReleasePersonMapper;
import com.tce.smart.platform.service.SmtArticlesReleasePersonService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * (SmtArticlesReleasePerson)表服务实现类
 *
 * @author sunfujian
 * @date 2021-08-16 13:53:22
 */
@Service
public class SmtArticlesReleasePersonServiceImpl extends ServiceImpl<SmtArticlesReleasePersonMapper, SmtArticlesReleasePerson> implements SmtArticlesReleasePersonService {

	@Override
	public List<SmtArticlesReleasePerson> getListByReleaseId(Long releaseId) {
		return list(Wrappers.<SmtArticlesReleasePerson>lambdaQuery().eq(SmtArticlesReleasePerson::getReleaseId, releaseId));
	}
}
