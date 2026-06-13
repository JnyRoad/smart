package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtEmailReceive;

import java.util.List;


/**
 *
 *
 * @author 齐佩
 * @date 2019-04-13 13:48:18
 */
public interface SmtEmailReceiveService extends IService<SmtEmailReceive> {

	Result updateId(SmtEmailReceive email);

	Result add(SmtEmailReceive email);

	Result getEmailById(Integer templateId);

	Result<List<SmtEmailReceive>> getByCode(String templateCode,Integer parkId);
}
