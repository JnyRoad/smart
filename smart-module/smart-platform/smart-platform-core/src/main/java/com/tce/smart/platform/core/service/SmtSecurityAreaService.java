package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtSecurityArea;
import com.tce.smart.platform.core.entity.SmtStaff;

import java.util.Arrays;
import java.util.List;

public interface SmtSecurityAreaService extends IService<SmtSecurityArea> {

    List<SmtSecurityArea> list(Integer factory);

    List<SmtSecurityArea> list(List<Integer> typeList);
}
