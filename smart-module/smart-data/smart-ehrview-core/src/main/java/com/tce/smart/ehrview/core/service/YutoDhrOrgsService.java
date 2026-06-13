package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.OvwYscomp;
import com.tce.smart.ehrview.core.entity.YutoDhrOrgs;

import java.util.List;

public interface YutoDhrOrgsService extends IService<YutoDhrOrgs> {

    OvwYscomp getByCompId(String compId);

    List<OvwYscomp> getList();
}
