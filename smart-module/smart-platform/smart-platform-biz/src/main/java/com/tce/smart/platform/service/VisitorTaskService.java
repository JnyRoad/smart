package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtVisitor;

/**
 * 访客的定时任务
 *
 * @author 梁圆
 */
public interface VisitorTaskService extends IService<SmtVisitor> {

	/**
	 * 访客超时未到定时器入口
	 */
	void visitorOverTime( Integer parkId);


	/**
	 * 访客超时未离开
	 */
	void visitorOverTimeNoLeave( Integer parkId);

	/**
	 * 访客提醒（快到预约时间时提醒、预约时间快结束时提醒）
	 */
	void visitorRemind( Integer parkId);

	/**
	 * 已经到达的访客定时删除入口
	 */
	void visitorComeOnTime();

	/**
	 * 推送访客信息给指定email
	 */
	void toEmail();

}
