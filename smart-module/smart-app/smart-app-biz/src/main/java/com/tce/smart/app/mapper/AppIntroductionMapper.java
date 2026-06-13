package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.vo.AppIntroductionVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AppIntroductionMapper extends BaseMapper<AppSubject> {

    IPage<List<AppSubject>> getByPageOnline(Page page);

    IPage<List<AppIntroductionVo>> getByPageDown(Page page);

    IPage<List<AppIntroductionVo>> getPageByNotRelease(Page page);

    List<AppSubject> downByOrderList(Integer order);

    AppIntroductionVo detailIntroduction(Integer id);

    AppSubject selectByOrder(@Param("order") Integer order);

    AppSubject selectDown(Integer id);

    int selectNum();
}
