package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtSecurityArea;
import com.tce.smart.platform.core.mapper.SmtSecurityAreaMapper;
import com.tce.smart.platform.core.service.SmtSecurityAreaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SmtSecurityAreaServiceImpl extends ServiceImpl<SmtSecurityAreaMapper, SmtSecurityArea> implements SmtSecurityAreaService {

    @Override
    public List<SmtSecurityArea> list(Integer factory) {
        return list(Wrappers.<SmtSecurityArea>lambdaQuery().eq(Objects.nonNull(factory), SmtSecurityArea::getFactoryType, factory));
    }

    @Override
    public List<SmtSecurityArea> list(List<Integer> typeList) {
        return list(Wrappers.<SmtSecurityArea>lambdaQuery().in(SmtSecurityArea::getCode, typeList));
    }
}
