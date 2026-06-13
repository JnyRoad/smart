package com.tce.smart.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.entity.AppSubjectModule;

/**
 *协议模块关联表
 */
public interface AppSubjectModuleService extends IService<AppSubjectModule> {
	AppSubjectModule getMoudleById(Integer id);
	void  deleteModule(Integer subjectId);
}
