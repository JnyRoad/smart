package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtArticlesReleaseMain;

/**
 * (SmtArticlesReleaseMain)表服务接口
 *
 * @author sunfujian
 * @date 2021-08-16 13:51:31
 */
public interface SmtArticlesReleaseMainService extends IService<SmtArticlesReleaseMain> {
	SmtArticlesReleaseMain getByReleaseId(Long releaseId);
}
