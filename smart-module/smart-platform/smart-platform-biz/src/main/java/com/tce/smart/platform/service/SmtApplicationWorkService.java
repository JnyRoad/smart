package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtApplicationWork;

import java.util.List;

/**
 * 应聘者工作经验
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:10
 */
public interface SmtApplicationWorkService extends IService<SmtApplicationWork> {

	Result addApplicationWork(SmtApplicationWork smtApplicationWork);

	Result updateApplicationWork(SmtApplicationWork smtApplicationWork);

	List<SmtApplicationWork> getSmtApplicationWorkList(String applicationId);

	Boolean deleteApplicationWorkList(String applicationId);

}
