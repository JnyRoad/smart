package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.AppMsgPushDTO;
import com.tce.smart.platform.core.vo.MsgStateVO;
import com.tce.smart.platform.core.dto.QueryAppMsgRecDTO;
import com.tce.smart.platform.core.entity.SmtMsgRecord;
import com.tce.smart.platform.core.vo.QueryAppMsgRecVO;

import java.util.List;

/**
 * App消息推送服务接口
 *
 * @author mkwu
 * @date 2019-07-05
 */
public interface IAppMsgPushService {

//	/**
//	 * 外向内访客申请,发送给被访员工
//	 *
//	 * @param appMsgPushDTO 消息字段
//	 * @return true-成功,false-失败
//	 */
//	Boolean pushVisitApprove(AppMsgPushDTO appMsgPushDTO);
//
//	/**
//	 * 推送简历投递，发送给HR
//	 *
//	 * @param appMsgPushDTO 消息字段
//	 * @return true-成功,false-失败
//	 */
//	Boolean pushDeliveryJob(AppMsgPushDTO appMsgPushDTO);
//
//
//	/**
//	 * 新员工成功入职消息推送(业务场景待定，新员工没登录过App，没有设备信息，不能推送)
//	 *
//	 * @param appMsgPushDTO 消息字段
//	 * @return true-成功,false-失败
//	 */
//	Boolean pushNewEmpJoin(AppMsgPushDTO appMsgPushDTO);
//
//	/**
//	 * 离职审批通过，发送给离职员工
//	 *
//	 * @param appMsgPushDTO 消息字段
//	 * @return true-成功,false-失败
//	 */
//	Boolean pushDimissionApprovePas(AppMsgPushDTO appMsgPushDTO);
//
//	/**
//	 * 工作交接通知，发送给交接对象
//	 *
//	 * @param appMsgPushDTO 消息字段
//	 * @return true-成功,false-失败
//	 */
//	Boolean pushDimissionWorkHand(AppMsgPushDTO appMsgPushDTO);

	/**
	 * 分页查询App消息推送记录
	 *
	 * @param page 分页信息
	 * @param queryAppMsgRecDTO 查询条件
	 * @return 消息推送记录列表
	 */
	IPage<SmtMsgRecord> queryAppMsgList(Page<?> page,QueryAppMsgRecDTO queryAppMsgRecDTO);

	/**
	 * App消息推送通用方法
	 *
	 * @param appMsgPushDTO 推送业务bean
	 * @return  true-成功,false-失败
	 */
	Boolean pushAppMsg(AppMsgPushDTO appMsgPushDTO);

	/**
	 * 设置App消息为已读
	 *
	 * @param recordId 记录ID
	 * @return 成功-true,失败-false
	 */
	Boolean changeRecordToRead(Integer recordId);


	/**
	 * 设置所有的App消息为已读
	 * @param queryAppMsgRecDTO
	 * @return
	 */
	Boolean changeAllRecordToRead(QueryAppMsgRecDTO queryAppMsgRecDTO);

	/**
	 * 统计App消息
	 *
	 * @param queryAppMsgRecDTO 查询条件
	 * @return 查询App消息推送统计
	 */
	QueryAppMsgRecVO countAppMsg(QueryAppMsgRecDTO queryAppMsgRecDTO);

	/**
	 * 删除App消息
	 *
	 * @param recordId 记录ID
	 * @return 成功-true,失败-false
	 */
	Boolean deleteMsg(Integer recordId);


	/**
	 * 获得信息发送状态
	 *
	 * @return 信息发送状态列表
	 */
	List<MsgStateVO> getState();

	/**
	 * 根据id获得信息
	 * @param id
	 * @return
	 */
	SmtMsgRecord getMsgById(Integer id);

	/**
	 * 删除所有app消息
	 * @param queryAppMsgRecDTO
	 * @return
	 */
	Boolean deleteAllMsg(QueryAppMsgRecDTO queryAppMsgRecDTO);


}
