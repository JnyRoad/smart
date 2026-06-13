package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.vo.AppAgreeVo;
import com.tce.smart.common.core.model.Result;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppAgreeMapper extends BaseMapper<AppSubject> {
	IPage<AppSubject> getAppAgreePage(Page page, AppSubject appSubject);
}
