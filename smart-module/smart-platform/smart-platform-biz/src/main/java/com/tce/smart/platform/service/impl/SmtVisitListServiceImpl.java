package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.securityarea.SmtVisitList;
import com.tce.smart.platform.core.mapper.SmtVisitListMapper;
import com.tce.smart.platform.service.SmtVisitListService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @description: SmtVisitListServiceImpl
 * @date: 2020-07-30 9:14
 * @author: wuling
 * @version: 1.0
 */
@Service
@AllArgsConstructor
public class SmtVisitListServiceImpl extends ServiceImpl<SmtVisitListMapper, SmtVisitList> implements SmtVisitListService {
}
