package com.tce.smart.platform.service.settlement.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtStaffSDMHistory;
import com.tce.smart.platform.core.mapper.SmtStaffSDMHistoryMapper;
import com.tce.smart.platform.service.settlement.SmtStaffSDMHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @description: 员工水电结算入住天数修改历史
 * @date: 2020-11-18 11:20
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class SmtStaffSDMHistoryServiceImpl extends ServiceImpl<SmtStaffSDMHistoryMapper, SmtStaffSDMHistory> implements SmtStaffSDMHistoryService {
}
