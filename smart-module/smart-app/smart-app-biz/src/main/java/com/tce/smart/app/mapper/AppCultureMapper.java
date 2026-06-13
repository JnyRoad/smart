package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.vo.AppCultureVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AppCultureMapper extends BaseMapper<AppSubject> {

    IPage<List<AppSubject>> getByPageOnline(Page page);

    IPage<List<AppCultureVo>> getByPageDown(Page page);

    IPage<List<AppCultureVo>> getPageByNotRelease(Page page);

    List<AppSubject> downByOrderList(Integer order);

    AppCultureVo detailCulture(Integer id);

    AppSubject selectByOrder(@Param("order") Integer order);

    AppSubject selectDown(Integer id);

    int selectNum();
}
