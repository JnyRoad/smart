package com.tce.smart.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.ao.AppIntroductionAo;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.vo.AppIntroductionVo;
import com.tce.smart.common.core.model.Result;

import java.util.List;

public interface AppIntroductionService extends IService<AppSubject> {

    IPage<List<AppSubject>> getByPageOnline(Page page);

    IPage<List<AppIntroductionVo>> getByPageDown(Page page);

    IPage<List<AppIntroductionVo>> getPageByNotRelease(Page page);

    Result detailIntroduction(Integer id);

    Result addAppIntroduction(AppIntroductionAo appIntroductionAo);

    Result downIntroduction(Integer id);

    Result moveIntroductionUp(Integer id);

    Result moveIntroductionDown(Integer id);

    Result delCulture(Integer id);

    Result turnOnline(Integer id);

    Result toNoRelease(Integer id);

    Result updateIntroduction(AppIntroductionAo appIntroductionAo, Integer id);

    Result topIntroduction(Integer id);

    Result cancelTop(Integer id);

    Result batchOnline(int[] ids);

    Result batchPendRelease(int[] ids);
}
