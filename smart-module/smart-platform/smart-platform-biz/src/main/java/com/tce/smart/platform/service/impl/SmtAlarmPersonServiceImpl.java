package com.tce.smart.platform.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtAlarmPerson;
import com.tce.smart.platform.core.mapper.SmtAlarmPersonMapper;
import com.tce.smart.platform.service.SmtAlarmPersonService;

/**
 * 警报人员关联表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:49
 */
@Service
public class SmtAlarmPersonServiceImpl extends ServiceImpl<SmtAlarmPersonMapper, SmtAlarmPerson> implements SmtAlarmPersonService {

}
