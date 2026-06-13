package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.EmployeeNoteAo;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.vo.AppEmployeeNoteVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AppEmployeeNoteMapper extends BaseMapper<AppSubject> {

	IPage<AppSubject> getPageList(@Param("page")Page page, @Param("query") EmployeeNoteAo employeeNoteAo);

    AppEmployeeNoteVo noteDetail(Integer id);
}
