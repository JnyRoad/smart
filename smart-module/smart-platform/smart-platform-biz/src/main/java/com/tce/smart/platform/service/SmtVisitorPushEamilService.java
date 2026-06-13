package com.tce.smart.platform.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.VisitorPushEamilDTO;
import com.tce.smart.platform.core.entity.SmtVisitorPushEamil;

/**
 * 访客信息推送接收email
 * @author QIPEI
 *
 */
public interface SmtVisitorPushEamilService extends IService<SmtVisitorPushEamil> {



	/**
	 * 查询所有的接收人列表
	 * @return
	 */
	List<SmtVisitorPushEamil> searchAll(SmtVisitorPushEamil smtVisitorPushEamil);

	Boolean add(VisitorPushEamilDTO emails);

	Boolean update(VisitorPushEamilDTO  emails);



}
