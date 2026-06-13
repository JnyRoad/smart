package com.tce.smart.admin.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.mapper.SysDictMapper;
import com.tce.smart.admin.service.SysDictService;
import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 */
@Slf4j
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    @Override
    public List<SysDict> findByType(String type) {
        QueryWrapper<SysDict> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type);
        wrapper.orderByAsc("sort");
        return this.list(wrapper);
    }

    @Override
    public SysDict findByValue(String type, String value) {
        return findByValue(type, StringUtils.EMPTY, value);
    }

    private SysDict findByValue(String type, String label, String value) {
        QueryWrapper<SysDict> wrapper = new QueryWrapper<>();
        wrapper.eq("type", type);
        wrapper.eq("value", value);
        if(StringUtils.isNotBlank(label)){
            wrapper.eq("label", label);
        }
        return this.getOne(wrapper);
    }

    @Override
    public Boolean saveDict(SysDict sysDict) {
		ExceptionType exceptionType = insertDict(sysDict);
		log.info(exceptionType.getMessage());
		if(exceptionType.getCode().equals(ExceptionType.SAVE_SUCCESS.getCode())){
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

    @Override
    public Boolean saveDict(String type, String label, String value) {
        ExceptionType exceptionType = insertDict(type, label, value);
        log.info(exceptionType.getMessage());
        if(exceptionType.getCode().equals(ExceptionType.SAVE_SUCCESS.getCode())){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
    private ExceptionType insertDict(String type, String label, String value) {
        SysDict sysDict = new SysDict();
        sysDict.setValue(value);
        sysDict.setLabel(label);
        sysDict.setType(type);
        sysDict.setDescription(label);
        sysDict.setRemarks(label);
        if(NumberUtil.isNumber(value)) {
            sysDict.setSort(NumberUtil.parseInt(value));
        }
        return insertDict(sysDict);
    }
    private ExceptionType insertDict(SysDict sysDict) {
        SysDict dict = findByValue(sysDict.getType(), sysDict.getLabel(), sysDict.getValue());
        if(Objects.nonNull(dict)){
            return ExceptionType.DICT_EXISTS;
        }
        sysDict.setCreateTime(DateUtils.localDateTime());
        sysDict.setUpdateTime(DateUtils.localDateTime());
        return sysDict.insert() ? ExceptionType.SAVE_SUCCESS : ExceptionType.SAVE_FAILD;
    }
}
