package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.app.entity.AppSubjectModule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppSubjectModuleMapper extends BaseMapper<AppSubjectModule> {
	void deleteModule(@Param("id") Integer id);
}
