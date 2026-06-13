package com.tce.smart.platform.service.settlement.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtSDPreMHistory;
import com.tce.smart.platform.core.mapper.SmtSDPreMHistoryMapper;
import com.tce.smart.platform.service.settlement.SmtSDPreMHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @description: 水电上月止度修改历史记录表
 * @date: 2020-11-18 11:20
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class SmtSDPreMHistoryServiceImpl extends ServiceImpl<SmtSDPreMHistoryMapper, SmtSDPreMHistory> implements SmtSDPreMHistoryService {
}
