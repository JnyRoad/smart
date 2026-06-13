package com.tce.smart.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.dto.AppAgreeDto;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.vo.AppAgreeVo;
import com.tce.smart.app.vo.AppCheckVo;
import com.tce.smart.common.core.model.Result;

import java.util.List;

public interface AppAgreeService extends IService<AppSubject> {

	IPage<AppSubject> getAppQuestionPage(Page page, AppSubject appSubject);

	Integer addAppAgree(AppAgreeDto appAgreeDto);

	void deleteAgree(Integer id);

	AppSubject getAppAgree(Integer id);

	void updateAppAgree(AppAgreeDto appAgreeDto);

	AppCheckVo getInitDate();
}
