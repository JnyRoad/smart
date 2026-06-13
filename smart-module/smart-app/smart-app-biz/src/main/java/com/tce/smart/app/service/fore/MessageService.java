package com.tce.smart.app.service.fore;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.SmtMsgRecordDTO;
import com.tce.smart.platform.api.dto.resp.QueryAppMsgRecRespDTO;

/**
 * App消息记录服务接口
 *
 * @author mkwu
 * @date 2019-07-07
 */
public interface MessageService {

	/**
	 * 获取App消息推送记录列表
	 *
	 * @param current  当前页
	 * @param size     大小
	 * @param deviceNo 设备编号
	 * @return 消息信息
	 */
	Page<SmtMsgRecordDTO> getAppMsgList(Long current, Long size, String deviceNo);

	/**
	 * 设置App消息为已读
	 *
	 * @param recordId 记录ID
	 * @return 成功-true,失败-false
	 */
	Boolean changeRecordToRead(Integer recordId);

	/**
	 * 统计App消息
	 *
	 * @param deviceNo 查询条件
	 * @return 查询App消息推送统计
	 */
	QueryAppMsgRecRespDTO countAppMsg(String deviceNo);

	/**
	 * 删除App消息
	 *
	 * @param recordId 记录ID
	 * @return 成功-true,失败-false
	 */
	Boolean deleteMsg(Integer recordId);

	/**
	 * 设置App消息全部为已读
	 *
	 * @param recordId 记录ID
	 * @return 成功-true,失败-false
	 */
	Boolean changeAllRecordToRead(String deviceNo);

	/**
	 * 删除所有App消息
	 *
	 * @param recordId 记录ID
	 * @return 成功-true,失败-false
	 */
	Boolean deleteAllMsg(String deviceNo);

}
