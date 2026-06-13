package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.*;


/**
 * 访客表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
public interface SmtVisitorService extends IService<SmtVisitor> {

	Boolean updateSmsCode(Long id);
}
