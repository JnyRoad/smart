package com.tce.smart.platform.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtPerson;
import com.tce.smart.platform.core.mapper.SmtPersonMapper;
import com.tce.smart.platform.service.SmtPersonService;

/**
 * 警报人员表
 *
 * @author 王艳勇
 * @date 2019-04-15 14:43:28
 */
@Service
public class SmtPersonServiceImpl extends ServiceImpl<SmtPersonMapper, SmtPerson> implements SmtPersonService {

}
