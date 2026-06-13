package com.tce.smart.platform.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.core.entity.SmtSecurityArea;
import com.tce.smart.platform.core.service.SmtSecurityAreaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

/**
 * 显示信息
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/security-area")
public class SmtSecurityAreaController extends BaseController {

    private SmtSecurityAreaService smtSecurityAreaService;

    @GetMapping("/page")
    public Result page(Page page, @RequestParam(value = "factoryType", required = false) Integer factoryType,
                       @RequestParam(value = "keyword", required = false) String keyword) {
        return success(smtSecurityAreaService.page(page, Wrappers.<SmtSecurityArea>lambdaQuery()
                .isNotNull(SmtSecurityArea::getId)
                .and(StrUtil.isNotBlank(keyword), e -> e.like(SmtSecurityArea::getType, keyword).or().like(SmtSecurityArea::getDesc, keyword))
                .eq(Objects.nonNull(factoryType), SmtSecurityArea::getFactoryType, factoryType).orderByDesc(SmtSecurityArea::getId)));
    }

    @GetMapping("/get/{id}")
    public Result getById(@PathVariable("id") Long id) {
        return success(smtSecurityAreaService.getById(id));
    }

    @PostMapping("/add")
    public Result add(@Valid @RequestBody SmtSecurityArea smtSecurityArea) {
        long count = smtSecurityAreaService.count(Wrappers.<SmtSecurityArea>lambdaQuery().eq(SmtSecurityArea::getCode, smtSecurityArea.getCode()));
        Assert.isTrue(count == 0, "编号不能重复");
        count = smtSecurityAreaService.count(Wrappers.<SmtSecurityArea>lambdaQuery().eq(SmtSecurityArea::getType, smtSecurityArea.getType()));
        Assert.isTrue(count == 0, "字段名不能重复");
        return success(smtSecurityAreaService.save(smtSecurityArea));
    }

    @PostMapping("/update")
    public Result update(@Valid @RequestBody SmtSecurityArea smtSecurityArea) {
        Assert.notNull(smtSecurityArea.getId(), "数据ID不能为空");
        SmtSecurityArea old = smtSecurityAreaService.getById(smtSecurityArea.getId());
        Assert.notNull(old, "未查询到区域");
        Assert.isTrue(old.getCode().equals(smtSecurityArea.getCode()), "编号不可修改");
        Assert.isTrue(old.getType().equals(smtSecurityArea.getType()), "字段名不可修改");
        Assert.isTrue(old.getFactoryType().equals(smtSecurityArea.getFactoryType()), "所属工厂不可修改");
        return success(smtSecurityAreaService.updateById(smtSecurityArea));
    }

    @GetMapping("/delete/{id}")
    public Result delete(@PathVariable("id") Long id) {
        return success(smtSecurityAreaService.removeById(id));
    }

}
