package com.tce.smart.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.entity.AppVersionControl;
import com.tce.smart.common.core.model.Result;

import java.util.List;

/**
 * App版本控制
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:31:36
 */
public interface AppVersionControlService extends IService<AppVersionControl> {
    Result addAppVersionControl(AppVersionControl appVersionControl);

    Result updateVersionById(AppVersionControl appVersionControl);

    Result removeVersionById(Integer id);

    IPage<List<AppVersionControl>> getSmtAreaPage(Page page, AppVersionControl appVersionControl);

    Result downVersionById(Integer id);

    /**
     * 版本控制分页查询
     * @param page
     * @param appVersonControl
     * @return IPage<AppVersionControl>
     * @author wuxinjian
     * @date 2019-04-30 17:53
     */
    IPage<AppVersionControl> selectByPage(Page<AppVersionControl> page, AppVersionControl appVersonControl);

    /**
     * 根据ID查询版本控制内容
     * @param id
     * @return
     * @author wuxinjian
     * @date 2019-04-30 17:53
     */
    AppVersionControl selectById(Integer id);

    /**
     * 新增一条版本控制
     * @param appVersonControl
     * @return
     * @author wuxinjian
     * @date 2019-04-30 17:53
     */
    Integer saveAppVersionControl(AppVersionControl appVersonControl);

    /**
     * 根据ID修改单条版本控制
     * @param appVersonControl
     * @author wuxinjian
     * @date 2019-04-30 17:53
     */
    void updateAppVersionControl(AppVersionControl appVersonControl);

    /**
     * 根据ID删除指定版本控制
     * @param id
     * @author wuxinjian
     * @date 2019-04-30 17:53
     */
    void deleteById(Integer id);
}
