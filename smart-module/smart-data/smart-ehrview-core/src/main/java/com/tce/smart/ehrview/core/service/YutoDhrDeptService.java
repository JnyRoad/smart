package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.entity.YutoDhrDept;

import java.util.List;

public interface YutoDhrDeptService extends IService<YutoDhrDept> {

    List<OvwYsdep> getByCompId(Integer compId);

    OvwYsdep getByDepId(Integer depId);

    List<OvwYsdep> getParentDep(Integer depId);
}
