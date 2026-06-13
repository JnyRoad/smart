package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtIdCardInfo;
import com.tce.smart.platform.core.mapper.SmtIdCardInfoMapper;
import com.tce.smart.platform.service.SmtIdCardInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @description: 身份证信息
 * @date: 2020-11-18 11:20
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class SmtIdCardInfoServiceImpl extends ServiceImpl<SmtIdCardInfoMapper, SmtIdCardInfo> implements SmtIdCardInfoService {
}
