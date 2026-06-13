package com.tce.smart.platform.service.impl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtSelect;
import com.tce.smart.platform.core.mapper.SmtSelectMapper;
import com.tce.smart.platform.service.SmtSelectService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SmtSelectServiceImpl  extends ServiceImpl<SmtSelectMapper, SmtSelect> implements SmtSelectService {

}
