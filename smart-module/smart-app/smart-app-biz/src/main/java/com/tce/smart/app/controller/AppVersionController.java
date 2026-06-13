package com.tce.smart.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.app.vo.AppVersionControlVo;
import com.tce.smart.common.core.wrapper.BaseController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.entity.AppVersionControl;
import com.tce.smart.app.service.AppVersionControlService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;

import lombok.AllArgsConstructor;


/**
 * App版本控制
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:31:36
 */
@RestController
@AllArgsConstructor
@RequestMapping("/appversoncontrol")
public class AppVersionController extends BaseController {

    private final AppVersionControlService appVersonControlService;

    /**
     * 分页查询
     *
     * @param page             分页对象
     * @param appVersonControl App版本控制
     * @return
     */
    @GetMapping("/page")
    public Result getAppVersonControlPage(Page<AppVersionControl> page, AppVersionControl appVersonControl) {
        IPage<AppVersionControl> iPage = appVersonControlService.selectByPage(page, appVersonControl);
        return success(iPage, AppVersionControlVo.class);
    }


    /**
     * 通过id查询App版本控制
     *
     * @param id id
     * @return Result
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable("id") Integer id) {
        AppVersionControl appVersionControl = appVersonControlService.selectById(id);
        return success(appVersionControl, AppVersionControlVo.class);
    }

    /**
     * 新增App版本控制
     *
     * @param appVersonControl App版本控制
     * @return Result
     */
    @SysLog("新增App版本控制")
    @PostMapping("/save")
    public Result save(@RequestBody AppVersionControl appVersonControl) {
        Integer id = appVersonControlService.saveAppVersionControl(appVersonControl);
        return success(id);
    }

    /**
     * 修改App版本控制
     *
     * @param appVersonControl App版本控制
     * @return Result
     */
    @SysLog("修改App版本控制")
    @PostMapping("/update")
    public Result updateById(@RequestBody AppVersionControl appVersonControl) {
        appVersonControlService.updateAppVersionControl(appVersonControl);
        return success();
    }

    /**
     * 通过id删除App版本控制
     *
     * @param id id
     * @return Result
     */
    @SysLog("删除App版本控制")
    @PostMapping("/{id}")
    public Result removeById(@PathVariable Integer id) {
        appVersonControlService.deleteById(id);
        return success();
    }

}
