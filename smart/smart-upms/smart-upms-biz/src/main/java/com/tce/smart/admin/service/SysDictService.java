package com.tce.smart.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.common.core.constant.enums.ExceptionType;

import java.util.List;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 */
public interface SysDictService extends IService<SysDict> {
    List<SysDict> findByType(String type);

    SysDict findByValue(String type, String value);

	Boolean saveDict(SysDict sysDict);

    Boolean saveDict(String type, String label, String value);
}
