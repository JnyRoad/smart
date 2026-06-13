package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtArticlesReleaseThing;

import java.util.List;

/**
 * (SmtArticlesReleaseThing)表服务接口
 *
 * @author sunfujian
 * @date 2021-08-16 13:54:48
 */
public interface SmtArticlesReleaseThingService extends IService<SmtArticlesReleaseThing> {
	List<SmtArticlesReleaseThing> getListByReleaseId(Long releaseId);
}
