package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.QueryAppMsgRecDTO;
import com.tce.smart.platform.core.dto.QueryMsgDTO;
import com.tce.smart.platform.core.entity.SmtMsgRecord;
import com.tce.smart.platform.core.entity.ext.MoveDataTaskExt;

/**
 * 消息记录接口
 *
 * @author mingkai.wu
 * @date 2019-05-15 18:15:32
 */
public interface SmtMsgRecordService extends IService<SmtMsgRecord> {

	/**
	 * 添加推送记录
	 *
	 * @param SmtMsgRecord
	 * @return Integer
	 */
	Integer addRecord(SmtMsgRecord SmtMsgRecord);

	/**
	 * 更新记录状态
	 *
	 * @param id    state
	 * @param state state
	 * @param remark remark
	 * @return
	 */
	Integer updateRecordState(Integer id, Integer state, String remark);

	/**
	 * 查询设备消息推送记录
	 *
	 * @param page 分页信息
	 * @param queryAppMsgRecDTO 查询条件
	 * @return 消息推送记录列表
	 */
	IPage<SmtMsgRecord> listAppMsgByPage(Page<?> page, QueryAppMsgRecDTO queryAppMsgRecDTO);

	/**
	 * 设置App消息为已读
	 *
	 * @param recordId 记录ID
	 * @return 成功-true,失败-false
	 */
	Boolean updateRecordToRead(Integer recordId);


	/**
	 * 设置全部的App消息为已读
	 * @return
	 */
	Boolean updateAllRecordToRead();

	/**
	 * 统计App推送成功消息条数
	 *
	 * @param queryAppMsgRecDTO 查询条件
	 * @return
	 */
	Integer countAppMsgSuccess(QueryAppMsgRecDTO queryAppMsgRecDTO);

	/**
	 * 统计App推送已读消息条数
	 *
	 * @param queryAppMsgRecDTO 查询条件
	 * @return
	 */
	Integer countAppMsgRead(QueryAppMsgRecDTO queryAppMsgRecDTO);

	/**
	 * 统计App推送未读消息条数
	 *
	 * @param queryAppMsgRecDTO 查询条件
	 * @return
	 */
	Integer countAppMsgUnRead(QueryAppMsgRecDTO queryAppMsgRecDTO);

	/**
	 * 处理历史数据
	 *
	 * @param moveDataTaskExt 转移任务表信息
	 */
	void processData(MoveDataTaskExt moveDataTaskExt);

	/**
	 * 删除App消息
	 *
	 * @param recordId 记录ID
	 * @return 成功-true,失败-false
	 */
	Boolean deleteMsg(Integer recordId);

	/**
	 * 根据条件查询消息记录
	 * @param page page
	 * @param queryMsgDTO queryMsgExt
	 * @return
	 */
	IPage<SmtMsgRecord> getMsg(Page page, QueryMsgDTO queryMsgDTO);

	/**
	 * 跟新所有app消息状态
	 * @param queryAppMsgRecDTO
	 * @return
	 */
	Boolean updateAllRecordToRead(QueryAppMsgRecDTO queryAppMsgRecDTO);

	/**
	 * 删除所有app消息
	 * @param queryAppMsgRecDTO
	 * @return
	 */
	Boolean deleteAllMsg(QueryAppMsgRecDTO queryAppMsgRecDTO);

}
