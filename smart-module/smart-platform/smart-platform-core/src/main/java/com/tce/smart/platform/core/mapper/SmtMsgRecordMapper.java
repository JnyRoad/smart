package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.QueryMsgDTO;
import com.tce.smart.platform.core.entity.SmtMsgRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 消息记录
 *
 * @author mingkai.wu
 * @date 2019-05-15 18:24:52
 */
@Mapper
public interface SmtMsgRecordMapper extends BaseMapper<SmtMsgRecord> {

	/**
	 * 分页查询消息记录
	 *
	 * @param page
	 * @param msgType   消息类型
	 * @param readState 是否已读
	 * @param msgObject 接受对象
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @return
	 */
	IPage<SmtMsgRecord> listRecordByPage(Page page, @Param("msgType") Integer msgType,
										 @Param("readState") Integer readState, @Param("msgObject") String msgObject,
										 @Param("startTime") Date startTime, @Param("endTime") Date endTime);

	/**
	 * 根据条件统计消息条数
	 *
	 * @param msgType      消息类型
	 * @param tempCode     模板编码
	 * @param smtMsgRecord 记录表查询条件
	 * @return 数量
	 */
	Integer countByCondition(@Param("msgType") Integer msgType, @Param("tempCode") Integer tempCode,
							 @Param("condition") SmtMsgRecord smtMsgRecord, @Param("startTime") Date startTime,
							 @Param("endTime") Date endTime);

	/**
	 * 迁移数据到历史表
	 *
	 * @param srcTable   源表
	 * @param destTable  目标表
	 * @param dateColumn 时间列名
	 * @param endDate    转移此日期前的数据
	 */
	void moveData(@Param("srcTable") String srcTable, @Param("destTable") String destTable,
				  @Param("dateColumn") String dateColumn, @Param("endDate") Date endDate);

	/**
	 * 删除源表数据
	 *
	 * @param tableName  源表
	 * @param dateColumn 时间列名
	 * @param endDate    转移此日期前的数据
	 */
	int deleteData(@Param("tableName") String tableName, @Param("dateColumn") String dateColumn,
				   @Param("endDate") Date endDate);

	IPage<SmtMsgRecord> getMsgInfo(Page page, @Param("query") QueryMsgDTO query);

	/**
	 * 查询所有要更改的列表
	 * @param msgObject
	 * @param type
	 * @param readState
	 * @return
	 */
	List<SmtMsgRecord> queryAllRecordToUpdate(@Param("msgObject") String msgObject, @Param("msgType") Integer type, @Param("readState") Integer readState);

}
