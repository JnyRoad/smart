package com.tce.smart.common.core.wrapper;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.enums.ExceptionEnum;
import com.tce.smart.common.core.exception.WrapperException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description: BaseController
 * @date: 2020-07-02 15:25
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
public class BaseController {

    @Autowired
    protected ControllerWrapper controllerWrapper;

    protected static final Integer DEFAULT_PAGE_SIZE = 20;

    protected static final Integer DEFAULT_MAX_PAGE_SIZE = 50;

    protected static final Integer DEFAULT_PAGE_INDEX = 0;

    protected static final Integer DEFAULT_MAX_PAGE_INDEX = 5000;

    protected Result success() {
        return Result.success(Boolean.TRUE);
    }

    @SuppressWarnings("unchecked")
    protected Result fail(String message) {
        return Result.fail(message);
    }

    protected <T, F> Result<IPage<F>> success(IPage<T> page, Class<F> dataClass) {
        List<F> voList = warp(page.getRecords(), dataClass);
        IPage<F> iPage = new Page<>();
        BeanUtils.copyProperties(page, iPage);
        iPage.setRecords(voList);
        return Result.success(iPage);
    }

    protected <T> Result<List<T>> success(List<T> data) {
        return Result.success(data);
    }

    protected <T, F> Result<List<F>> success(List<T> model, Class<F> dataClass) {
        List<F> voList = warp(model, dataClass);
        return Result.success(voList);
    }

    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }

    protected <T, F> Result<F> success(T model, Class<F> dataClass) {
        F vo = warp(model, dataClass);
        return Result.success(vo);
    }

    protected <T, F> List<F> warp(List<T> model, Class<F> dataClass) {
        if (CollectionUtils.isEmpty(model)) {
            return new ArrayList<>();
        }
        return controllerWrapper.warp(model, dataClass);
    }

    protected <T, F> F warp(T model, Class<F> dataClass) {
        try {
            if (Objects.isNull(model)) {
//				return dataClass.newInstance();
                return null;
            }
            return controllerWrapper.warp(model, dataClass);
        } catch (Exception e) {
            log.error("模型转换异常,源数据:{},转换类型:{}", JSONUtil.toJsonStr(model), dataClass.getName(), e);
            throw new WrapperException(ExceptionEnum.SERVER_ERROR);
        }
    }

    protected Integer pageSize(Number pageSize) {
        if (Objects.isNull(pageSize)) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize.intValue() > DEFAULT_MAX_PAGE_SIZE || pageSize.intValue() < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize.intValue();
    }

    protected Integer pageIndex(Number pageIndex) {
        if (Objects.isNull(pageIndex)) {
            return DEFAULT_PAGE_INDEX;
        }
        if (pageIndex.intValue() < DEFAULT_PAGE_INDEX || pageIndex.intValue() > DEFAULT_MAX_PAGE_INDEX) {
            return DEFAULT_PAGE_INDEX;
        }
        return pageIndex.intValue();
    }
}
