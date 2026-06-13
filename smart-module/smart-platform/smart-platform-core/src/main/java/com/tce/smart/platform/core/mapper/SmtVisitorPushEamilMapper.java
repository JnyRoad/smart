package com.tce.smart.platform.core.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.tce.smart.platform.core.entity.SmtVisitorPushEamil;


/**
 * 访客消息推送接收email
 *
 * @author QIPEI
 * @date 2019-08-25 14:19:19
 */
public interface SmtVisitorPushEamilMapper extends BaseMapper<SmtVisitorPushEamil> {

	void deleteAll();

}
