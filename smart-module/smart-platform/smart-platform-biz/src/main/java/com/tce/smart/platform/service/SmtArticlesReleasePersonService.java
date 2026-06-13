package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtArticlesReleasePerson;

import java.util.List;

/**
 * (SmtArticlesReleasePerson)表服务接口
 *
 * @author sunfujian
 * @date 2021-08-17 15:00:57
 */
public interface SmtArticlesReleasePersonService extends IService<SmtArticlesReleasePerson> {
	List<SmtArticlesReleasePerson> getListByReleaseId(Long releaseId);
}
