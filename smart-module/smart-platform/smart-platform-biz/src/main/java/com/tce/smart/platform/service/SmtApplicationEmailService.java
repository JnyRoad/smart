package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtApplicationEmail;

/**
 * 应聘者工作经验
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:10
 */
public interface SmtApplicationEmailService extends IService<SmtApplicationEmail> {

	Result<SmtApplicationEmail> getSmtApplicationEmailList(String applicationId);

	Result deleteApplicationEmailList(String applicationId);

	Result addApplicationEmailList(SmtApplicationEmail email);

	Result updateApplicationEmailList(SmtApplicationEmail email);






}
