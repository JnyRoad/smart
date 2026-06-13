package com.tce.smart.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.ao.AppCultureAo;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.vo.AppCultureVo;
import com.tce.smart.common.core.model.Result;

import java.util.List;

/**
 *  企业文化
 * @author lbw
 */
public interface AppCultureService extends IService<AppSubject>{

    IPage<List<AppSubject>> getByPageOnline(Page page);

    IPage<List<AppCultureVo>> getByPageDown(Page page);

    IPage<List<AppCultureVo>> getPageByNotRelease(Page page);

    Result detailCulture(Integer id);

    Result addAppCulture(AppCultureAo appCultureAo);

    Result downCulture(Integer id);

    Result moveCultureUp(Integer id);

    Result moveCultureDown(Integer id);

    Result delCulture(Integer id);

    Result turnOnline(Integer id);

    Result toNoRelease(Integer id);

    Result updateCulture(AppCultureAo appCultureAo, Integer id);

    Result topCulture(Integer id);

    Result cancelTop(Integer id);

    Result batchOnline(int[] ids);

    Result batchPendRelease(int[] ids);
}
