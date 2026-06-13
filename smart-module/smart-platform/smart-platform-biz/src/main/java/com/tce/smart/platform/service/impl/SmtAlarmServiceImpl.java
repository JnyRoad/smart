package com.tce.smart.platform.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtAlarm;
import com.tce.smart.platform.core.mapper.SmtAlarmMapper;
import com.tce.smart.platform.service.SmtAlarmService;

/**
 * 警报信息记录
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@Service
public class SmtAlarmServiceImpl extends ServiceImpl<SmtAlarmMapper, SmtAlarm> implements SmtAlarmService {

}
