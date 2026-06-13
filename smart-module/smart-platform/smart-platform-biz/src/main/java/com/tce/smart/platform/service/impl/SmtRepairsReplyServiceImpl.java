package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtRepairsReply;
import com.tce.smart.platform.core.mapper.SmtRepairsReplyMapper;
import com.tce.smart.platform.service.SmtRepairsReplyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @description: SmtRepairsReplyServiceImpl
 * @date: 2020-07-24 16:35
 * @author: wuling
 * @version: 1.0
 */
@Service
@AllArgsConstructor
public class SmtRepairsReplyServiceImpl extends ServiceImpl<SmtRepairsReplyMapper, SmtRepairsReply> implements SmtRepairsReplyService {
}
